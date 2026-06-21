package com.example.permission.service;

import com.example.permission.common.BusinessException;
import com.example.permission.common.PageResult;
import com.example.permission.entity.*;
import com.example.permission.mapper.*;
import com.example.permission.security.LoginUser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

import static com.example.permission.entity.table.MemberBenefitLogTableDef.MEMBER_BENEFIT_LOG;
import static com.example.permission.entity.table.MemberLevelTableDef.MEMBER_LEVEL;
import static com.example.permission.entity.table.MemberTableDef.MEMBER;
import static com.example.permission.entity.table.RoomTableDef.ROOM;
import static com.example.permission.entity.table.RoomTypeTableDef.ROOM_TYPE;
import static com.example.permission.entity.table.BookingTableDef.BOOKING;

@Service
public class MemberBenefitService {

    @Autowired
    private MemberBenefitLogMapper benefitLogMapper;

    @Autowired
    private MemberMapper memberMapper;

    @Autowired
    private MemberLevelMapper memberLevelMapper;

    @Autowired
    private RoomTypeMapper roomTypeMapper;

    @Autowired
    private RoomMapper roomMapper;

    @Autowired
    private BookingMapper bookingMapper;

    @Autowired
    private CheckInMapper checkInMapper;

    @Autowired
    private CheckInService checkInService;

    @Autowired
    private MemberService memberService;

    @Autowired
    private ObjectMapper objectMapper;

    public static final int BENEFIT_TYPE_DISCOUNT = 1;
    public static final int BENEFIT_TYPE_UPGRADE = 2;
    public static final int BENEFIT_TYPE_DEPOSIT = 3;
    public static final int BENEFIT_TYPE_LATE_CHECKOUT = 4;
    public static final int BENEFIT_TYPE_POINTS = 5;
    public static final int BENEFIT_TYPE_EARLY_CHECKIN = 6;
    public static final int BENEFIT_TYPE_BREAKFAST = 7;

    public static final int ORDER_TYPE_BOOKING = 1;
    public static final int ORDER_TYPE_CHECKIN = 2;

    private static final Map<Integer, String> BENEFIT_TYPE_NAMES = new HashMap<>();

    static {
        BENEFIT_TYPE_NAMES.put(BENEFIT_TYPE_DISCOUNT, "房费折扣");
        BENEFIT_TYPE_NAMES.put(BENEFIT_TYPE_UPGRADE, "免费升级房型");
        BENEFIT_TYPE_NAMES.put(BENEFIT_TYPE_DEPOSIT, "押金减免");
        BENEFIT_TYPE_NAMES.put(BENEFIT_TYPE_LATE_CHECKOUT, "延迟退房");
        BENEFIT_TYPE_NAMES.put(BENEFIT_TYPE_POINTS, "积分发放");
        BENEFIT_TYPE_NAMES.put(BENEFIT_TYPE_EARLY_CHECKIN, "提前入住");
        BENEFIT_TYPE_NAMES.put(BENEFIT_TYPE_BREAKFAST, "免费早餐");
    }

    private String getBenefitTypeName(int type) {
        return BENEFIT_TYPE_NAMES.getOrDefault(type, "其他");
    }

    public Map<String, Object> getMemberBenefits(Long customerId) {
        if (customerId == null) {
            return null;
        }

        Member member = memberService.getByCustomerId(customerId);
        if (member == null || member.getStatus() != 1) {
            return null;
        }

        MemberLevel level = memberLevelMapper.selectOneById(member.getLevelId());
        if (level != null) {
            member.setLevelIcon(level.getLevelIcon());
            member.setLevelColor(level.getLevelColor());
            member.setMemberLevel(level);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("member", member);
        result.put("level", level);
        result.put("services", parseJsonArray(level != null ? level.getServices() : null));
        result.put("otherBenefits", parseJsonArray(level != null ? level.getOtherBenefits() : null));

        return result;
    }

    private List<String> parseJsonArray(String json) {
        if (!StringUtils.hasText(json)) {
            return Collections.emptyList();
        }
        try {
            return objectMapper.readValue(json, List.class);
        } catch (JsonProcessingException e) {
            return Collections.emptyList();
        }
    }

    public Map<String, Object> calculateBookingBenefits(Long memberId, BigDecimal roomTotal, Long roomTypeId) {
        Map<String, Object> result = new HashMap<>();

        if (memberId == null) {
            result.put("hasDiscount", false);
            return result;
        }

        Member member = memberMapper.selectOneById(memberId);
        if (member == null) {
            result.put("hasDiscount", false);
            return result;
        }

        MemberLevel level = memberLevelMapper.selectOneById(member.getLevelId());
        if (level == null) {
            result.put("hasDiscount", false);
            return result;
        }

        BigDecimal discountRate = level.getRoomDiscount();
        if (discountRate == null || discountRate.compareTo(new BigDecimal("100")) >= 0) {
            result.put("hasDiscount", false);
            return result;
        }

        BigDecimal discountAmount = roomTotal.multiply(new BigDecimal("100").subtract(discountRate))
                .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);

        BigDecimal discountedPrice = roomTotal.subtract(discountAmount);

        result.put("hasDiscount", true);
        result.put("originalPrice", roomTotal);
        result.put("discountRate", discountRate);
        result.put("discountAmount", discountAmount);
        result.put("discountedPrice", discountedPrice);
        result.put("discountRemark", level.getLevelName() + "会员" + discountRate.stripTrailingZeros().toPlainString() + "折优惠");
        result.put("memberLevel", level.getLevelName());
        result.put("memberLevelIcon", level.getLevelIcon());
        result.put("memberLevelColor", level.getLevelColor());
        result.put("services", parseJsonArray(level.getServices()));
        result.put("lateCheckoutTime", level.getLateCheckoutTime());
        result.put("freeUpgrade", level.getFreeUpgrade());
        result.put("freeBreakfast", level.getFreeBreakfast());
        result.put("depositReduction", level.getDepositReduction());

        return result;
    }

    @Transactional
    public Map<String, Object> applyBookingDiscount(Long bookingId, LoginUser loginUser) {
        Booking booking = bookingMapper.selectOneById(bookingId);
        if (booking == null) {
            throw new BusinessException("预订单不存在");
        }
        if (booking.getMemberId() == null) {
            throw new BusinessException("该预订没有关联会员");
        }

        Member member = memberMapper.selectOneById(booking.getMemberId());
        MemberLevel level = memberLevelMapper.selectOneById(member.getLevelId());
        if (level == null) {
            throw new BusinessException("会员等级不存在");
        }

        BigDecimal roomTotal = booking.getRoomTotal() != null ? booking.getRoomTotal() : BigDecimal.ZERO;
        BigDecimal discountRate = level.getRoomDiscount();

        if (discountRate == null || discountRate.compareTo(new BigDecimal("100")) == 0) {
            Map<String, Object> result = new HashMap<>();
            result.put("applied", false);
            result.put("message", "该会员等级不享受折扣");
            return result;
        }

        BigDecimal discountAmount = roomTotal.multiply(new BigDecimal("100").subtract(discountRate))
                .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);

        booking.setMemberDiscount(discountAmount);
        booking.setMemberDiscountRate(discountRate);
        booking.setMemberDiscountRemark(level.getLevelName() + "会员" + discountRate.stripTrailingZeros().toPlainString() + "折优惠");
        booking.setDiscount(booking.getDiscount() != null ? booking.getDiscount().add(discountAmount) : discountAmount);
        booking.setTotalAmount(booking.getTotalAmount().subtract(discountAmount).setScale(2, RoundingMode.HALF_UP));
        booking.setUpdateTime(LocalDateTime.now());
        bookingMapper.update(booking);

        Map<String, Object> benefitContent = new HashMap<>();
        benefitContent.put("discountRate", discountRate);
        benefitContent.put("originalPrice", roomTotal);
        benefitContent.put("discountPrice", roomTotal.subtract(discountAmount));

        recordBenefitLog(booking.getMemberId(),
            booking.getMemberNo(),
            booking.getCustomerName(),
            BENEFIT_TYPE_DISCOUNT,
            ORDER_TYPE_BOOKING,
            booking.getId(),
            booking.getBookingNo(),
            benefitContent,
            roomTotal,
            discountAmount,
            roomTotal.subtract(discountAmount),
            booking.getMemberDiscountRemark(),
            loginUser);

        Map<String, Object> result = new HashMap<>();
        result.put("applied", true);
        result.put("originalPrice", roomTotal);
        result.put("discountRate", discountRate);
        result.put("discountAmount", discountAmount);
        result.put("discountedPrice", roomTotal.subtract(discountAmount));
        result.put("discountRemark", booking.getMemberDiscountRemark());
        return result;
    }

    public Map<String, Object> calculateDepositReduction(Long memberId, BigDecimal standardDeposit) {
        Map<String, Object> result = new HashMap<>();

        if (memberId == null || standardDeposit == null) {
            result.put("standardDeposit", standardDeposit);
            result.put("reductionAmount", BigDecimal.ZERO);
            result.put("reductionRate", BigDecimal.ZERO);
            result.put("actualDeposit", standardDeposit);
            result.put("hasReduction", false);
            return result;
        }

        Member member = memberMapper.selectOneById(memberId);
        if (member == null) {
            result.put("standardDeposit", standardDeposit);
            result.put("reductionAmount", BigDecimal.ZERO);
            result.put("reductionRate", BigDecimal.ZERO);
            result.put("actualDeposit", standardDeposit);
            result.put("hasReduction", false);
            return result;
        }

        MemberLevel level = memberLevelMapper.selectOneById(member.getLevelId());
        if (level == null || level.getDepositReduction() == null || level.getDepositReduction().compareTo(BigDecimal.ZERO) == 0) {
            result.put("standardDeposit", standardDeposit);
            result.put("reductionAmount", BigDecimal.ZERO);
            result.put("reductionRate", BigDecimal.ZERO);
            result.put("actualDeposit", standardDeposit);
            result.put("hasReduction", false);
            return result;
        }

        BigDecimal reductionRate = level.getDepositReduction();
        BigDecimal reductionAmount = standardDeposit.multiply(reductionRate)
                .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
        BigDecimal actualDeposit = standardDeposit.subtract(reductionAmount);

        result.put("standardDeposit", standardDeposit);
        result.put("reductionAmount", reductionAmount);
        result.put("reductionRate", reductionRate);
        result.put("actualDeposit", actualDeposit);
        result.put("hasReduction", true);

        if (reductionRate.compareTo(new BigDecimal("100")) == 0) {
            result.put("reductionRemark", level.getLevelName() + "免押金");
        } else {
            result.put("reductionRemark", level.getLevelName() + "减免" + reductionRate.stripTrailingZeros().toPlainString() + "%押金");
        }

        return result;
    }

    @Transactional
    public Map<String, Object> applyDepositReduction(Long checkInId, BigDecimal standardDeposit, LoginUser loginUser) {
        CheckIn checkIn = checkInMapper.selectOneById(checkInId);
        if (checkIn == null) {
            throw new BusinessException("入住单不存在");
        }
        if (checkIn.getMemberId() == null) {
            throw new BusinessException("该入住没有关联会员");
        }

        Map<String, Object> reductionResult = calculateDepositReduction(checkIn.getMemberId(), standardDeposit);

        if (Boolean.TRUE.equals(reductionResult.get("hasReduction"))) {
            checkIn.setStandardDeposit(standardDeposit);
            checkIn.setDepositReductionAmount((BigDecimal) reductionResult.get("reductionAmount"));
            checkIn.setDepositReductionRate((BigDecimal) reductionResult.get("reductionRate"));
            checkIn.setDepositAmount((BigDecimal) reductionResult.get("actualDeposit"));
            checkIn.setUpdateTime(LocalDateTime.now());
            checkInMapper.update(checkIn);

            Map<String, Object> benefitContent = new HashMap<>();
            benefitContent.put("standardDeposit", standardDeposit);
            benefitContent.put("reductionRate", reductionResult.get("reductionRate"));
            benefitContent.put("actualDeposit", reductionResult.get("actualDeposit"));

            recordBenefitLog(checkIn.getMemberId(),
                    checkIn.getMemberNo(),
                    checkIn.getCustomerName(),
                    BENEFIT_TYPE_DEPOSIT,
                    ORDER_TYPE_CHECKIN,
                    checkIn.getId(),
                    checkIn.getCheckInNo(),
                    benefitContent,
                    standardDeposit,
                    (BigDecimal) reductionResult.get("reductionAmount"),
                    (BigDecimal) reductionResult.get("actualDeposit"),
                    (String) reductionResult.get("reductionRemark"),
                    loginUser);
        }

        return reductionResult;
    }

    public Map<String, Object> getUpgradeEligibleRoomTypes(Long memberId, Long currentRoomTypeId, LocalDate checkInDate, LocalDate checkOutDate) {
        Map<String, Object> result = new HashMap<>();
        result.put("eligible", false);
        result.put("availableRoomTypes", Collections.emptyList());

        if (memberId == null || currentRoomTypeId == null) {
            return result;
        }

        Member member = memberMapper.selectOneById(memberId);
        if (member == null) {
            return result;
        }

        MemberLevel level = memberLevelMapper.selectOneById(member.getLevelId());
        if (level == null || level.getFreeUpgrade() == null || level.getFreeUpgrade() != 1) {
            return result;
        }

        RoomType currentType = roomTypeMapper.selectOneById(currentRoomTypeId);
        if (currentType == null) {
            return result;
        }

        QueryWrapper typeQuery = QueryWrapper.create()
                .from(RoomType.class)
                .where(ROOM_TYPE.DELETED.eq(0))
                .and(ROOM_TYPE.SALE_STATUS.eq(1))
                .and(ROOM_TYPE.BASE_PRICE.gt(currentType.getBasePrice()));
        List<RoomType> higherTypes = roomTypeMapper.selectListByQuery(typeQuery);

        if (higherTypes.isEmpty()) {
            return result;
        }

        List<RoomType> availableTypes = new ArrayList<>();
        for (RoomType type : higherTypes) {
            QueryWrapper roomQuery = QueryWrapper.create()
                    .from(Room.class)
                    .where(ROOM.DELETED.eq(0))
                    .and(ROOM.STATUS.eq(1))
                    .and(ROOM.ROOM_TYPE_ID.eq(type.getId()));

            List<Room> rooms = roomMapper.selectListByQuery(roomQuery);

            List<Long> roomIds = rooms.stream().map(Room::getId).collect(Collectors.toList());
            if (roomIds.isEmpty()) {
                continue;
            }

            QueryWrapper bookingQuery = QueryWrapper.create()
                    .from(Booking.class)
                    .where(BOOKING.DELETED.eq(0))
                    .and(BOOKING.STATUS.in(Arrays.asList(1, 2, 3, 5)))
                    .and(BOOKING.CHECK_OUT_DATE.gt(checkInDate))
                    .and(BOOKING.CHECK_IN_DATE.lt(checkOutDate))
                    .and(BOOKING.ROOM_ID.in(roomIds));
            List<Booking> bookings = bookingMapper.selectListByQuery(bookingQuery);

            List<Long> bookedRoomIds = bookings.stream()
                    .map(Booking::getRoomId)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

            boolean hasAvailable = rooms.stream().anyMatch(r -> !bookedRoomIds.contains(r.getId()));
            if (hasAvailable) {
                availableTypes.add(type);
            }
        }

        if (availableTypes.isEmpty()) {
            return result;
        }

        result.put("eligible", true);
        result.put("availableRoomTypes", availableTypes);
        result.put("message", "该会员可免费升级房型");
        return result;
    }

    @Transactional
    public Map<String, Object> applyRoomUpgrade(Long checkInId, Long newRoomTypeId, LoginUser loginUser) {
        CheckIn checkIn = checkInMapper.selectOneById(checkInId);
        if (checkIn == null) {
            throw new BusinessException("入住单不存在");
        }
        if (checkIn.getMemberId() == null) {
            throw new BusinessException("该入住没有关联会员");
        }

        Member member = memberMapper.selectOneById(checkIn.getMemberId());
        MemberLevel level = memberLevelMapper.selectOneById(member.getLevelId());
        if (level.getFreeUpgrade() == null || level.getFreeUpgrade() != 1) {
            throw new BusinessException("该会员不享有免费升级权益");
        }

        Long oldRoomTypeId = checkIn.getRoomTypeId();
        String oldRoomTypeName = checkIn.getRoomTypeName();

        RoomType newType = roomTypeMapper.selectOneById(newRoomTypeId);
        if (newType == null) {
            throw new BusinessException("新房型不存在");
        }

        QueryWrapper roomQuery = QueryWrapper.create()
                .from(Room.class)
                .where(ROOM.DELETED.eq(0))
                .and(ROOM.STATUS.eq(1))
                .and(ROOM.ROOM_TYPE_ID.eq(newRoomTypeId));
        List<Room> rooms = roomMapper.selectListByQuery(roomQuery);

        List<Long> roomIds = rooms.stream().map(Room::getId).collect(Collectors.toList());
        if (roomIds.isEmpty()) {
            throw new BusinessException("该房型没有可用房间");
        }

        QueryWrapper bookingQuery = QueryWrapper.create()
                .from(Booking.class)
                .where(BOOKING.DELETED.eq(0))
                .and(BOOKING.STATUS.in(Arrays.asList(1, 2, 3, 5)))
                .and(BOOKING.CHECK_OUT_DATE.gt(checkIn.getCheckInDate()))
                .and(BOOKING.CHECK_IN_DATE.lt(checkIn.getCheckOutDate()))
                .and(BOOKING.ROOM_ID.in(roomIds));
        List<Booking> bookings = bookingMapper.selectListByQuery(bookingQuery);
        List<Long> bookedRoomIds = bookings.stream()
                .map(Booking::getRoomId)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        Room availableRoom = rooms.stream()
                .filter(r -> !bookedRoomIds.contains(r.getId()))
                .findFirst()
                .orElseThrow(() -> new BusinessException("该房型没有可用房间"));

        Room oldRoom = roomMapper.selectOneById(checkIn.getRoomId());
        if (oldRoom != null) {
            oldRoom.setStatus(1);
            oldRoom.setUpdateTime(LocalDateTime.now());
            roomMapper.update(oldRoom);
        }

        availableRoom.setStatus(3);
        availableRoom.setUpdateTime(LocalDateTime.now());
        roomMapper.update(availableRoom);

        BigDecimal originalRoomTotal = checkIn.getRoomTotal();

        checkIn.setIsUpgraded(1);
        checkIn.setOriginalRoomTypeId(oldRoomTypeId);
        checkIn.setOriginalRoomTypeName(oldRoomTypeName);
        checkIn.setUpgradeReason(level.getLevelName() + "会员免费升级");
        checkIn.setRoomId(availableRoom.getId());
        checkIn.setRoomNumber(availableRoom.getRoomNumber());
        checkIn.setRoomTypeId(newType.getId());
        checkIn.setRoomTypeName(newType.getTypeName());
        checkIn.setRoomTotal(originalRoomTotal);
        checkIn.setUpdateTime(LocalDateTime.now());
        checkInMapper.update(checkIn);

        Map<String, Object> benefitContent = new HashMap<>();
        benefitContent.put("originalRoomType", oldRoomTypeName);
        benefitContent.put("originalRoomTypeId", oldRoomTypeId);
        benefitContent.put("newRoomType", newType.getTypeName());
        benefitContent.put("newRoomTypeId", newType.getId());
        benefitContent.put("newRoomNumber", availableRoom.getRoomNumber());

        BigDecimal upgradeValue = newType.getBasePrice() != null ?
                newType.getBasePrice().multiply(BigDecimal.valueOf(checkIn.getDays())) :
                BigDecimal.ZERO;

        recordBenefitLog(checkIn.getMemberId(),
                checkIn.getMemberNo(),
                checkIn.getCustomerName(),
                BENEFIT_TYPE_UPGRADE,
                ORDER_TYPE_CHECKIN,
                checkIn.getId(),
                checkIn.getCheckInNo(),
                benefitContent,
                BigDecimal.ZERO,
                upgradeValue,
                BigDecimal.ZERO,
                level.getLevelName() + "会员免费升级房型",
                loginUser);

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("oldRoomTypeName", oldRoomTypeName);
        result.put("newRoomTypeName", newType.getTypeName());
        result.put("newRoomNumber", availableRoom.getRoomNumber());
        result.put("originalRoomTotal", originalRoomTotal);
        return result;
    }

    public Map<String, Object> getLateCheckoutEligibility(Long memberId) {
        Map<String, Object> result = new HashMap<>();
        result.put("eligible", false);
        result.put("standardCheckoutTime", "12:00");

        if (memberId == null) {
            return result;
        }

        Member member = memberMapper.selectOneById(memberId);
        if (member == null) {
            return result;
        }

        MemberLevel level = memberLevelMapper.selectOneById(member.getLevelId());
        if (level == null || level.getLateCheckoutTime() == null) {
            return result;
        }

        String standardTime = "12:00";
        String lateTime = level.getLateCheckoutTime();

        if (standardTime.equals(lateTime)) {
            result.put("eligible", false);
            result.put("message", "该会员不享有延迟退房权益");
            return result;
        }

        result.put("eligible", true);
        result.put("standardCheckoutTime", standardTime);
        result.put("lateCheckoutTime", lateTime);
        result.put("message", level.getLevelName() + "会员可延迟退房至" + lateTime);
        return result;
    }

    @Transactional
    public Map<String, Object> applyLateCheckout(Long checkInId, LoginUser loginUser) {
        CheckIn checkIn = checkInMapper.selectOneById(checkInId);
        if (checkIn == null) {
            throw new BusinessException("入住单不存在");
        }
        if (checkIn.getMemberId() == null) {
            throw new BusinessException("该入住没有关联会员");
        }

        Map<String, Object> eligibility = getLateCheckoutEligibility(checkIn.getMemberId());
        if (!Boolean.TRUE.equals(eligibility.get("eligible"))) {
            throw new BusinessException("该会员不享有延迟退房权益");
        }

        LocalTime lateTime = LocalTime.parse((String) eligibility.get("lateCheckoutTime"));
        LocalDateTime actualCheckOutTime = LocalDateTime.of(checkIn.getCheckOutDate(), lateTime);

        checkIn.setActualCheckOutTime(actualCheckOutTime);
        checkIn.setIsLateCheckout(1);
        checkIn.setLateCheckoutReason(checkIn.getMemberLevel() + "会员延迟退房");
        checkIn.setUpdateTime(LocalDateTime.now());
        checkInMapper.update(checkIn);

        Map<String, Object> benefitContent = new HashMap<>();
        benefitContent.put("standardCheckout", eligibility.get("standardCheckoutTime"));
        benefitContent.put("actualCheckout", eligibility.get("lateCheckoutTime"));

        Member member = memberMapper.selectOneById(checkIn.getMemberId());
        MemberLevel level = memberLevelMapper.selectOneById(member.getLevelId());

        recordBenefitLog(checkIn.getMemberId(),
                checkIn.getMemberNo(),
                checkIn.getCustomerName(),
                BENEFIT_TYPE_LATE_CHECKOUT,
                ORDER_TYPE_CHECKIN,
                checkIn.getId(),
                checkIn.getCheckInNo(),
                benefitContent,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                (String) eligibility.get("message"),
                loginUser);

        Map<String, Object> result = new HashMap<>();
        result.put("applied", true);
        result.put("lateCheckoutTime", eligibility.get("lateCheckoutTime"));
        result.put("message", eligibility.get("message"));
        return result;
    }

    @Transactional
    public Map<String, Object> awardPointsForCheckIn(Long checkInId, LoginUser loginUser) {
        CheckIn checkIn = checkInMapper.selectOneById(checkInId);
        if (checkIn == null) {
            throw new BusinessException("入住单不存在");
        }
        if (checkIn.getMemberId() == null) {
            Map<String, Object> result = new HashMap<>();
            result.put("awarded", false);
            result.put("message", "该入住没有关联会员");
            return result;
        }

        Member member = memberMapper.selectOneById(checkIn.getMemberId());
        if (member == null) {
            throw new BusinessException("会员不存在");
        }

        MemberLevel level = memberLevelMapper.selectOneById(member.getLevelId());
        BigDecimal consumptionAmount = checkIn.getTotalAmount() != null ?
                checkIn.getTotalAmount() : BigDecimal.ZERO;

        BigDecimal basePoints = consumptionAmount.setScale(0, RoundingMode.DOWN);
        BigDecimal pointRate = level.getPointRate() != null ? level.getPointRate() : BigDecimal.ONE;
        BigDecimal earnedPoints = basePoints.multiply(pointRate).setScale(0, RoundingMode.HALF_UP);

        memberService.addPoints(member.getId(), earnedPoints, 1, "消费赠送",
                "入住" + checkIn.getCheckInNo() + "消费赠送积分",
                loginUser.getUserId(), loginUser.getUser().getNickname() != null ?
                        loginUser.getUser().getNickname() : loginUser.getUsername());

        checkIn.setEarnedPoints(earnedPoints);
        checkIn.setUpdateTime(LocalDateTime.now());
        checkInMapper.update(checkIn);

        Map<String, Object> benefitContent = new HashMap<>();
        benefitContent.put("basePoints", basePoints);
        benefitContent.put("rate", pointRate);
        benefitContent.put("earnedPoints", earnedPoints);
        benefitContent.put("consumptionAmount", consumptionAmount);

        recordBenefitLog(checkIn.getMemberId(),
                checkIn.getMemberNo(),
                checkIn.getCustomerName(),
                BENEFIT_TYPE_POINTS,
                ORDER_TYPE_CHECKIN,
                checkIn.getId(),
                checkIn.getCheckInNo(),
                benefitContent,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                "消费赠送" + earnedPoints + "积分",
                loginUser);

        member = memberMapper.selectOneById(checkIn.getMemberId());

        Map<String, Object> result = new HashMap<>();
        result.put("awarded", true);
        result.put("basePoints", basePoints);
        result.put("pointRate", pointRate);
        result.put("earnedPoints", earnedPoints);
        result.put("currentPoints", member.getCurrentPoints());
        result.put("message", "本次消费获得" + earnedPoints + "积分");
        return result;
    }

    public List<Room> getPreferredRooms(List<Room> rooms) {
        if (rooms == null || rooms.isEmpty()) {
            return rooms;
        }
        return rooms.stream()
                .sorted((r1, r2) -> {
                    int p1 = r1.getIsPreferred() != null && r1.getIsPreferred() == 1 ? 1 : 0;
                    int p2 = r2.getIsPreferred() != null && r2.getIsPreferred() == 1 ? 1 : 0;
                    return Integer.compare(p2, p1);
                })
                .collect(Collectors.toList());
    }

    private void recordBenefitLog(Long memberId, String memberNo, String memberName,
                              int benefitType, int orderType, Long orderId, String orderNo,
                              Object benefitContent, BigDecimal originalAmount, BigDecimal benefitAmount,
                              BigDecimal actualAmount, String remark, LoginUser loginUser) {
        MemberBenefitLog log = new MemberBenefitLog();
        log.setMemberId(memberId);
        log.setMemberNo(memberNo);
        log.setMemberName(memberName);
        log.setBenefitType(benefitType);
        log.setBenefitTypeName(getBenefitTypeName(benefitType));
        log.setRelatedOrderType(orderType);
        log.setRelatedOrderId(orderId);
        log.setRelatedOrderNo(orderNo);
        try {
            log.setBenefitContent(objectMapper.writeValueAsString(benefitContent));
        } catch (JsonProcessingException e) {
            log.setBenefitContent(benefitContent.toString());
        }
        log.setOriginalAmount(originalAmount);
        log.setBenefitAmount(benefitAmount);
        log.setActualAmount(actualAmount);
        log.setRemark(remark);
        log.setOperatorId(loginUser.getUserId());
        log.setOperatorName(loginUser.getUser().getNickname() != null ?
                loginUser.getUser().getNickname() : loginUser.getUsername());
        log.setCreateTime(LocalDateTime.now());
        benefitLogMapper.insert(log);
    }

    public PageResult<MemberBenefitLog> getBenefitLogPage(Integer pageNum, Integer pageSize,
                                                      Long memberId, String memberNo, String memberName,
                                                      Integer benefitType, Integer relatedOrderType,
                                                      String relatedOrderNo, String createTimeStart,
                                                      String createTimeEnd, String sortField, String sortOrder) {
        QueryWrapper query = QueryWrapper.create()
                .from(MemberBenefitLog.class)
                .where(MEMBER_BENEFIT_LOG.ID.isNotNull());

        if (memberId != null) {
            query.and(MEMBER_BENEFIT_LOG.MEMBER_ID.eq(memberId));
        }
        if (StringUtils.hasText(memberNo)) {
            query.and(MEMBER_BENEFIT_LOG.MEMBER_NO.like(memberNo));
        }
        if (StringUtils.hasText(memberName)) {
            query.and(MEMBER_BENEFIT_LOG.MEMBER_NAME.like(memberName));
        }
        if (benefitType != null) {
            query.and(MEMBER_BENEFIT_LOG.BENEFIT_TYPE.eq(benefitType));
        }
        if (relatedOrderType != null) {
            query.and(MEMBER_BENEFIT_LOG.RELATED_ORDER_TYPE.eq(relatedOrderType));
        }
        if (StringUtils.hasText(relatedOrderNo)) {
            query.and(MEMBER_BENEFIT_LOG.RELATED_ORDER_NO.like(relatedOrderNo));
        }
        if (StringUtils.hasText(createTimeStart)) {
            query.and(MEMBER_BENEFIT_LOG.CREATE_TIME.ge(LocalDateTime.parse(createTimeStart + " 00:00:00",
                    java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))));
        }
        if (StringUtils.hasText(createTimeEnd)) {
            query.and(MEMBER_BENEFIT_LOG.CREATE_TIME.le(LocalDateTime.parse(createTimeEnd + " 23:59:59",
                    java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))));
        }

        if (StringUtils.hasText(sortField)) {
            boolean isAsc = "asc".equalsIgnoreCase(sortOrder);
            if ("createTime".equals(sortField)) {
                query.orderBy(MEMBER_BENEFIT_LOG.CREATE_TIME, isAsc);
            } else if ("benefitType".equals(sortField)) {
                query.orderBy(MEMBER_BENEFIT_LOG.BENEFIT_TYPE, isAsc);
            } else {
                query.orderBy(MEMBER_BENEFIT_LOG.CREATE_TIME.desc());
            }
        } else {
            query.orderBy(MEMBER_BENEFIT_LOG.CREATE_TIME.desc());
        }

        Page<MemberBenefitLog> page = benefitLogMapper.paginate(Page.of(pageNum, pageSize), query);
        return new PageResult<>(page.getTotalRow(), page.getRecords(),
                (long) page.getPageNumber(), (long) page.getPageSize());
    }

    public List<MemberBenefitLog> getBenefitLogList(Long memberId, String createTimeStart, String createTimeEnd) {
        QueryWrapper query = QueryWrapper.create()
                .from(MemberBenefitLog.class)
                .where(MEMBER_BENEFIT_LOG.MEMBER_ID.eq(memberId))
                .orderBy(MEMBER_BENEFIT_LOG.CREATE_TIME.desc());

        if (StringUtils.hasText(createTimeStart)) {
            query.and(MEMBER_BENEFIT_LOG.CREATE_TIME.ge(LocalDateTime.parse(createTimeStart + " 00:00:00",
                    java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))));
        }
        if (StringUtils.hasText(createTimeEnd)) {
            query.and(MEMBER_BENEFIT_LOG.CREATE_TIME.le(LocalDateTime.parse(createTimeEnd + " 23:59:59",
                    java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))));
        }

        return benefitLogMapper.selectListByQuery(query);
    }

    public Map<String, Object> getLateCheckoutByCheckIn(Long checkInId) {
        Map<String, Object> result = new HashMap<>();
        CheckIn checkIn = checkInMapper.selectOneById(checkInId);
        if (checkIn == null) {
            result.put("eligible", false);
            result.put("standardTime", "12:00");
            result.put("remark", "入住单不存在");
            return result;
        }

        if (checkIn.getMemberId() == null) {
            result.put("eligible", false);
            result.put("standardTime", "12:00");
            result.put("remark", "非会员退房时间为12:00，超时需收取费用");
            return result;
        }

        return getLateCheckoutEligibility(checkIn.getMemberId());
    }

    public Map<String, Object> checkout(Map<String, Object> params, LoginUser loginUser) {
        Long checkInId = Long.valueOf(params.get("checkInId").toString());
        Map<String, Object> result = new HashMap<>();

        try {
            Integer paymentMethod = params.get("paymentMethod") != null ? Integer.valueOf(params.get("paymentMethod").toString()) : null;
            String paymentVoucherNo = params.get("paymentVoucherNo") != null ? params.get("paymentVoucherNo").toString() : null;
            Integer depositMethod = params.get("depositMethod") != null ? Integer.valueOf(params.get("depositMethod").toString()) : null;
            Integer keyCardReturned = params.get("keyCardReturned") != null ? Integer.valueOf(params.get("keyCardReturned").toString()) : null;
            Integer keyCardLost = params.get("keyCardLost") != null ? Integer.valueOf(params.get("keyCardLost").toString()) : null;
            Integer roomChecked = params.get("roomChecked") != null ? Integer.valueOf(params.get("roomChecked").toString()) : null;
            Integer roomCheckResult = params.get("roomCheckResult") != null ? Integer.valueOf(params.get("roomCheckResult").toString()) : null;
            String damageItems = params.get("damageItems") != null ? params.get("damageItems").toString() : null;
            String damageDescription = params.get("damageDescription") != null ? params.get("damageDescription").toString() : null;
            BigDecimal damageCompensation = params.get("damageCompensation") != null ? new BigDecimal(params.get("damageCompensation").toString()) : BigDecimal.ZERO;
            String remark = params.get("remark") != null ? params.get("remark").toString() : null;

            CheckIn checkIn = checkInMapper.selectOneById(checkInId);
            if (checkIn != null && checkIn.getMemberId() != null) {
                try {
                    applyLateCheckout(checkInId, loginUser);
                    result.put("lateCheckoutApplied", true);
                } catch (Exception e) {
                    result.put("lateCheckoutApplied", false);
                }
            }

            Object checkoutRecord = checkInService.checkOut(checkInId, paymentMethod, paymentVoucherNo,
                    depositMethod, keyCardReturned, keyCardLost, roomChecked, roomCheckResult,
                    damageItems, damageDescription, damageCompensation, remark);

            checkIn = checkInMapper.selectOneById(checkInId);
            if (checkIn != null && checkIn.getMemberId() != null) {
                try {
                    Map<String, Object> pointsResult = awardPointsForCheckIn(checkInId, loginUser);
                    result.put("pointsAwarded", true);
                    result.put("pointsInfo", pointsResult);
                } catch (Exception e) {
                    result.put("pointsAwarded", false);
                    result.put("pointsError", e.getMessage());
                }
            }

            result.put("success", true);
            result.put("message", "退房成功");
            result.put("data", checkoutRecord);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "退房失败：" + e.getMessage());
        }

        return result;
    }

    public void exportBenefitLogs(Long memberId, String memberNo, String memberName, Integer benefitType,
                                  String relatedOrderNo, String startTime, String endTime,
                                  javax.servlet.http.HttpServletResponse response) throws Exception {
        List<MemberBenefitLog> logs = getBenefitLogListForExport(memberId, memberNo, memberName, benefitType, relatedOrderNo, startTime, endTime);

        response.setContentType("application/vnd.ms-excel;charset=UTF-8");
        response.setHeader("Content-Disposition", "attachment;filename=" +
                java.net.URLEncoder.encode("权益使用记录.xlsx", "UTF-8"));

        StringBuilder sb = new StringBuilder();
        sb.append("使用时间\t会员卡号\t会员姓名\t权益类型\t关联单号\t权益内容\t原始金额\t优惠金额\t实际金额\t操作人\t备注\n");

        for (MemberBenefitLog log : logs) {
            sb.append(log.getCreateTime() != null ? log.getCreateTime().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) : "").append("\t");
            sb.append(log.getMemberNo() != null ? log.getMemberNo() : "").append("\t");
            sb.append(log.getMemberName() != null ? log.getMemberName() : "").append("\t");
            sb.append(log.getBenefitTypeName() != null ? log.getBenefitTypeName() : "").append("\t");
            sb.append(log.getRelatedOrderNo() != null ? log.getRelatedOrderNo() : "").append("\t");
            sb.append(log.getBenefitContent() != null ? log.getBenefitContent().replace("\t", " ") : "").append("\t");
            sb.append(log.getOriginalAmount() != null ? log.getOriginalAmount() : "").append("\t");
            sb.append(log.getBenefitAmount() != null ? log.getBenefitAmount() : "").append("\t");
            sb.append(log.getActualAmount() != null ? log.getActualAmount() : "").append("\t");
            sb.append(log.getOperatorName() != null ? log.getOperatorName() : "").append("\t");
            sb.append(log.getRemark() != null ? log.getRemark().replace("\t", " ") : "").append("\n");
        }

        try (java.io.OutputStream os = response.getOutputStream()) {
            os.write(sb.toString().getBytes("UTF-8"));
            os.flush();
        }
    }

    private List<MemberBenefitLog> getBenefitLogListForExport(Long memberId, String memberNo, String memberName,
                                                               Integer benefitType, String relatedOrderNo,
                                                               String createTimeStart, String createTimeEnd) {
        QueryWrapper query = QueryWrapper.create()
                .from(MemberBenefitLog.class)
                .orderBy(MEMBER_BENEFIT_LOG.CREATE_TIME.desc());

        if (memberId != null) {
            query.and(MEMBER_BENEFIT_LOG.MEMBER_ID.eq(memberId));
        }
        if (StringUtils.hasText(memberNo)) {
            query.and(MEMBER_BENEFIT_LOG.MEMBER_NO.like(memberNo));
        }
        if (StringUtils.hasText(memberName)) {
            query.and(MEMBER_BENEFIT_LOG.MEMBER_NAME.like(memberName));
        }
        if (benefitType != null) {
            query.and(MEMBER_BENEFIT_LOG.BENEFIT_TYPE.eq(benefitType));
        }
        if (StringUtils.hasText(relatedOrderNo)) {
            query.and(MEMBER_BENEFIT_LOG.RELATED_ORDER_NO.like(relatedOrderNo));
        }
        if (StringUtils.hasText(createTimeStart)) {
            query.and(MEMBER_BENEFIT_LOG.CREATE_TIME.ge(LocalDateTime.parse(createTimeStart + " 00:00:00",
                    java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))));
        }
        if (StringUtils.hasText(createTimeEnd)) {
            query.and(MEMBER_BENEFIT_LOG.CREATE_TIME.le(LocalDateTime.parse(createTimeEnd + " 23:59:59",
                    java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))));
        }

        return benefitLogMapper.selectListByQuery(query);
    }
}
