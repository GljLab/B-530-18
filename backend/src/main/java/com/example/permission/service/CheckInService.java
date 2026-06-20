package com.example.permission.service;

import com.example.permission.common.BusinessException;
import com.example.permission.common.PageResult;
import com.example.permission.entity.*;
import com.example.permission.mapper.AgreementUnitMapper;
import com.example.permission.mapper.CreditBillMapper;
import com.example.permission.mapper.*;
import com.example.permission.security.LoginUser;
import com.example.permission.service.MemberService;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

import static com.example.permission.entity.table.CheckInTableDef.CHECK_IN;
import static com.example.permission.entity.table.CheckInGuestTableDef.CHECK_IN_GUEST;
import static com.example.permission.entity.table.CheckInOperationLogTableDef.CHECK_IN_OPERATION_LOG;
import static com.example.permission.entity.table.CheckOutRecordTableDef.CHECK_OUT_RECORD;
import static com.example.permission.entity.table.ConsumptionRecordTableDef.CONSUMPTION_RECORD;
import static com.example.permission.entity.table.ExtendStayRecordTableDef.EXTEND_STAY_RECORD;
import static com.example.permission.entity.table.KeyCardRecordTableDef.KEY_CARD_RECORD;
import static com.example.permission.entity.table.RoomChangeRecordTableDef.ROOM_CHANGE_RECORD;
import static com.example.permission.entity.table.RoomTableDef.ROOM;
import static com.example.permission.entity.table.RoomTypeTableDef.ROOM_TYPE;
import static com.example.permission.entity.table.BookingTableDef.BOOKING;
import static com.example.permission.entity.table.CustomerTableDef.CUSTOMER;

@Service
public class CheckInService {

    @Autowired
    private CheckInMapper checkInMapper;

    @Autowired
    private CheckInGuestMapper checkInGuestMapper;

    @Autowired
    private KeyCardRecordMapper keyCardRecordMapper;

    @Autowired
    private RoomChangeRecordMapper roomChangeRecordMapper;

    @Autowired
    private ExtendStayRecordMapper extendStayRecordMapper;

    @Autowired
    private CheckOutRecordMapper checkOutRecordMapper;

    @Autowired
    private CheckInOperationLogMapper checkInOperationLogMapper;

    @Autowired
    private ConsumptionRecordMapper consumptionRecordMapper;

    @Autowired
    private RoomMapper roomMapper;

    @Autowired
    private RoomTypeMapper roomTypeMapper;

    @Autowired
    private BookingMapper bookingMapper;

    @Autowired
    private CustomerMapper customerMapper;

    @Autowired
    private RoomStatusLogService roomStatusLogService;

    @Autowired
    private BookingService bookingService;

    @Autowired
    private AgreementUnitMapper agreementUnitMapper;

    @Autowired
    private CreditBillMapper creditBillMapper;

    @Autowired
    private CreditBillService creditBillService;

    @Autowired
    private MemberService memberService;

    private LoginUser getCurrentUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof LoginUser) {
            return (LoginUser) principal;
        }
        throw new BusinessException("用户未登录");
    }

    private String generateCheckInNo() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String random = String.format("%04d", new Random().nextInt(10000));
        return timestamp + random;
    }

    private String generateChangeNo() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String random = String.format("%04d", new Random().nextInt(10000));
        return "CH" + timestamp + random;
    }

    private String generateExtendNo() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String random = String.format("%04d", new Random().nextInt(10000));
        return "EX" + timestamp + random;
    }

    private String generateCheckOutNo() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String random = String.format("%04d", new Random().nextInt(10000));
        return "CO" + timestamp + random;
    }

    private String generateConsumptionNo() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String random = String.format("%04d", new Random().nextInt(10000));
        return "CS" + timestamp + random;
    }

    @Transactional
    public CheckIn checkInFromBooking(Long bookingId, Long roomId, List<CheckInGuest> guests,
                                       BigDecimal depositAmount, Integer depositMethod, String depositVoucherNo,
                                       Integer keyCardCount, String specialRequirements, String remark) {
        LoginUser user = getCurrentUser();
        Booking booking = bookingMapper.selectOneById(bookingId);
        if (booking == null) {
            throw new BusinessException("预订单不存在");
        }
        if (booking.getStatus() == 4) {
            throw new BusinessException("该预订单已办理入住");
        }
        if (booking.getStatus() == 5 || booking.getStatus() == 6) {
            throw new BusinessException("预订单状态不正确，无法办理入住");
        }

        Room room = roomMapper.selectOneById(roomId);
        if (room == null) {
            throw new BusinessException("房间不存在");
        }
        if (room.getStatus() != 1 && room.getStatus() != 2) {
            throw new BusinessException("房间状态不正确，无法入住");
        }
        if (!room.getRoomTypeId().equals(booking.getRoomTypeId())) {
            throw new BusinessException("房间房型与预订房型不一致");
        }

        Customer customer = customerMapper.selectOneById(booking.getCustomerId());
        if (customer == null) {
            throw new BusinessException("客户信息不存在");
        }

        if (guests == null || guests.isEmpty()) {
            throw new BusinessException("请至少登记一位入住人");
        }
        long mainGuestCount = guests.stream().filter(g -> g.getIsMain() != null && g.getIsMain() == 1).count();
        if (mainGuestCount != 1) {
            throw new BusinessException("有且仅有一位主入住人");
        }
        for (CheckInGuest guest : guests) {
            if (!StringUtils.hasText(guest.getName())) {
                throw new BusinessException("入住人姓名不能为空");
            }
            if (StringUtils.hasText(guest.getIdNumber()) && guest.getIdType() == null) {
                throw new BusinessException("填写证件号时请选择证件类型");
            }
        }

        if (depositAmount == null || depositAmount.compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessException("押金金额不能为负数");
        }

        LocalDate checkInDate = LocalDate.now();
        LocalDate checkOutDate = booking.getCheckOutDate();
        int days = (int) ChronoUnit.DAYS.between(checkInDate, checkOutDate);
        if (days <= 0) {
            days = 1;
            checkOutDate = checkInDate.plusDays(1);
        }

        BigDecimal roomPrice = booking.getRoomPrice();
        BigDecimal roomTotal = roomPrice.multiply(BigDecimal.valueOf(days));

        CheckIn checkIn = new CheckIn();
        checkIn.setCheckInNo(generateCheckInNo());
        checkIn.setBookingId(bookingId);
        checkIn.setBookingNo(booking.getBookingNo());
        checkIn.setCustomerId(booking.getCustomerId());
        checkIn.setCustomerName(booking.getCustomerName());
        checkIn.setCustomerPhone(booking.getCustomerPhone());
        checkIn.setCustomerType(customer.getCustomerType());
        checkIn.setMemberLevel(0);
        checkIn.setMemberId(booking.getMemberId());
        checkIn.setMemberNo(booking.getMemberNo());
        if (booking.getMemberId() != null) {
            Member member = memberService.getById(booking.getMemberId());
            if (member != null) {
                checkIn.setMemberLevel(member.getLevelId() != null ? member.getLevelId().intValue() : 0);
            }
        }
        checkIn.setRoomTypeId(booking.getRoomTypeId());
        checkIn.setRoomTypeName(booking.getRoomTypeName());
        checkIn.setRoomId(roomId);
        checkIn.setRoomNumber(room.getRoomNumber());
        checkIn.setCheckInDate(checkInDate);
        checkIn.setCheckOutDate(checkOutDate);
        checkIn.setActualCheckInTime(LocalDateTime.now());
        checkIn.setDays(days);
        checkIn.setStayedDays(0);
        checkIn.setRoomPrice(roomPrice);
        checkIn.setRoomTotal(roomTotal);
        checkIn.setExtraBedCount(booking.getExtraBedCount());
        checkIn.setExtraBedPrice(booking.getExtraBedPrice());
        checkIn.setExtraBedTotal(booking.getExtraBedTotal());
        checkIn.setOtherFee(BigDecimal.ZERO);
        checkIn.setDiscount(booking.getDiscount() != null ? booking.getDiscount() : BigDecimal.ZERO);
        checkIn.setTotalAmount(roomTotal.add(booking.getExtraBedTotal()).subtract(checkIn.getDiscount()));
        checkIn.setPaidAmount(booking.getPaidAmount() != null ? booking.getPaidAmount() : BigDecimal.ZERO);
        checkIn.setPayableAmount(checkIn.getTotalAmount().subtract(checkIn.getPaidAmount()));
        checkIn.setDepositAmount(depositAmount);
        checkIn.setDepositMethod(depositMethod);
        checkIn.setDepositVoucherNo(depositVoucherNo);
        checkIn.setKeyCardCount(keyCardCount != null ? keyCardCount : 1);
        checkIn.setKeyCardReturned(0);
        checkIn.setGuestCount(guests.size());
        checkIn.setSpecialRequirements(StringUtils.hasText(specialRequirements) ? specialRequirements : booking.getSpecialRequirements());
        checkIn.setBookingSource(StringUtils.hasText(booking.getBookingSource()) ? Integer.valueOf(booking.getBookingSource()) : 1);
        checkIn.setStatus(1);
        checkIn.setIsOverdue(0);
        checkIn.setOperatorId(user.getUserId());
        checkIn.setOperatorName(user.getUsername());
        checkIn.setRemark(remark);
        checkIn.setCreateTime(LocalDateTime.now());
        checkIn.setUpdateTime(LocalDateTime.now());
        checkInMapper.insert(checkIn);

        for (CheckInGuest guest : guests) {
            guest.setCheckInId(checkIn.getId());
            guest.setCheckInNo(checkIn.getCheckInNo());
            guest.setCreateTime(LocalDateTime.now());
            guest.setUpdateTime(LocalDateTime.now());
            checkInGuestMapper.insert(guest);
        }

        for (int i = 0; i < checkIn.getKeyCardCount(); i++) {
            KeyCardRecord keyCard = new KeyCardRecord();
            keyCard.setCheckInId(checkIn.getId());
            keyCard.setCheckInNo(checkIn.getCheckInNo());
            keyCard.setRoomId(roomId);
            keyCard.setRoomNumber(room.getRoomNumber());
            keyCard.setCardNo("K" + checkIn.getId() + "-" + (i + 1));
            keyCard.setCardType(1);
            keyCard.setIssueTime(LocalDateTime.now());
            keyCard.setExpireTime(checkOutDate.atTime(12, 0));
            keyCard.setStatus(1);
            keyCard.setOperatorId(user.getUserId());
            keyCard.setOperatorName(user.getUsername());
            keyCardRecordMapper.insert(keyCard);
        }

        booking.setStatus(4);
        booking.setCheckInTime(LocalDateTime.now());
        booking.setRoomId(roomId);
        booking.setRoomNumber(room.getRoomNumber());
        bookingMapper.update(booking);

        Integer oldRoomStatus = room.getStatus();
        room.setStatus(3);
        roomMapper.update(room);

        RoomStatusLog roomStatusLog = new RoomStatusLog();
        roomStatusLog.setRoomId(roomId);
        roomStatusLog.setRoomNumber(room.getRoomNumber());
        roomStatusLog.setOldStatus(oldRoomStatus);
        roomStatusLog.setNewStatus(3);
        roomStatusLog.setOperatorId(user.getUserId());
        roomStatusLog.setOperator(user.getUsername());
        roomStatusLog.setRemark("办理入住，入住单号：" + checkIn.getCheckInNo());
        roomStatusLogService.add(roomStatusLog);

        addOperationLog(checkIn.getId(), checkIn.getCheckInNo(), 1, "办理入住",
                "从预订单" + booking.getBookingNo() + "办理入住，房间：" + room.getRoomNumber(),
                user.getUserId(), user.getUsername(), "酒店管理员");

        return checkIn;
    }

    @Transactional
    public CheckIn walkInCheckIn(Long roomTypeId, Long roomId, Customer customer, List<CheckInGuest> guests,
                                  LocalDate checkInDate, LocalDate checkOutDate,
                                  BigDecimal depositAmount, Integer depositMethod, String depositVoucherNo,
                                  Integer keyCardCount, String specialRequirements, String remark, Integer bookingSource,
                                  Long agreementUnitId, Integer guaranteeType, Long memberId) {
        LoginUser user = getCurrentUser();

        Room room = roomMapper.selectOneById(roomId);
        if (room == null) {
            throw new BusinessException("房间不存在");
        }
        if (room.getStatus() != 1) {
            throw new BusinessException("房间状态不正确，无法入住");
        }
        if (!room.getRoomTypeId().equals(roomTypeId)) {
            throw new BusinessException("房间房型与选择房型不一致");
        }

        RoomType roomType = roomTypeMapper.selectOneById(roomTypeId);
        if (roomType == null) {
            throw new BusinessException("房型不存在");
        }

        if (customer == null || !StringUtils.hasText(customer.getName()) || !StringUtils.hasText(customer.getPhone())) {
            throw new BusinessException("请填写客户信息");
        }

        Customer savedCustomer = saveOrUpdateCustomer(customer);

        if (guests == null || guests.isEmpty()) {
            throw new BusinessException("请至少登记一位入住人");
        }
        long mainGuestCount = guests.stream().filter(g -> g.getIsMain() != null && g.getIsMain() == 1).count();
        if (mainGuestCount != 1) {
            throw new BusinessException("有且仅有一位主入住人");
        }
        for (CheckInGuest guest : guests) {
            if (!StringUtils.hasText(guest.getName())) {
                throw new BusinessException("入住人姓名不能为空");
            }
            if (StringUtils.hasText(guest.getIdNumber()) && guest.getIdType() == null) {
                throw new BusinessException("填写证件号时请选择证件类型");
            }
        }

        if (depositAmount == null || depositAmount.compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessException("押金金额不能为负数");
        }

        if (checkInDate == null) {
            checkInDate = LocalDate.now();
        }
        if (checkOutDate == null) {
            checkOutDate = checkInDate.plusDays(1);
        }
        int days = (int) ChronoUnit.DAYS.between(checkInDate, checkOutDate);
        if (days <= 0) {
            throw new BusinessException("退房日期必须晚于入住日期");
        }

        BigDecimal roomPrice = roomType.getBasePrice() != null ? roomType.getBasePrice() : BigDecimal.ZERO;
        BigDecimal roomTotal = roomPrice.multiply(BigDecimal.valueOf(days));

        CheckIn checkIn = new CheckIn();
        checkIn.setCheckInNo(generateCheckInNo());
        checkIn.setCustomerId(savedCustomer.getId());
        checkIn.setCustomerName(savedCustomer.getName());
        checkIn.setCustomerPhone(savedCustomer.getPhone());
        checkIn.setCustomerType(1);
        checkIn.setMemberLevel(0);
        if (memberId != null) {
            Member member = memberService.getById(memberId);
            if (member != null) {
                checkIn.setMemberId(memberId);
                checkIn.setMemberNo(member.getMemberNo());
                checkIn.setMemberLevel(member.getLevelId() != null ? member.getLevelId().intValue() : 0);
            }
        }
        checkIn.setRoomTypeId(roomTypeId);
        checkIn.setRoomTypeName(roomType.getTypeName());
        checkIn.setRoomId(roomId);
        checkIn.setRoomNumber(room.getRoomNumber());
        checkIn.setCheckInDate(checkInDate);
        checkIn.setCheckOutDate(checkOutDate);
        checkIn.setActualCheckInTime(LocalDateTime.now());
        checkIn.setDays(days);
        checkIn.setStayedDays(0);
        checkIn.setRoomPrice(roomPrice);
        checkIn.setRoomTotal(roomTotal);
        checkIn.setExtraBedCount(0);
        checkIn.setExtraBedPrice(BigDecimal.ZERO);
        checkIn.setExtraBedTotal(BigDecimal.ZERO);
        checkIn.setOtherFee(BigDecimal.ZERO);
        checkIn.setDiscount(BigDecimal.ZERO);
        checkIn.setTotalAmount(roomTotal);
        checkIn.setPaidAmount(BigDecimal.ZERO);
        checkIn.setPayableAmount(roomTotal);
        checkIn.setDepositAmount(depositAmount);
        checkIn.setDepositMethod(depositMethod);
        checkIn.setDepositVoucherNo(depositVoucherNo);
        checkIn.setKeyCardCount(keyCardCount != null ? keyCardCount : 1);
        checkIn.setKeyCardReturned(0);
        checkIn.setGuestCount(guests.size());
        checkIn.setSpecialRequirements(specialRequirements);
        checkIn.setBookingSource(bookingSource != null ? bookingSource : 1);
        checkIn.setStatus(1);
        checkIn.setIsOverdue(0);
        checkIn.setOperatorId(user.getUserId());
        checkIn.setOperatorName(user.getUsername());
        checkIn.setRemark(remark);
        if (agreementUnitId != null && guaranteeType != null && guaranteeType == 2) {
            AgreementUnit unit = agreementUnitMapper.selectOneById(agreementUnitId);
            if (unit == null) {
                throw new BusinessException("协议单位不存在");
            }
            if (unit.getStatus() != 1) {
                throw new BusinessException("协议单位已禁用");
            }
            if (unit.getCurrentDebt().add(roomTotal).compareTo(unit.getCreditLimit()) > 0) {
                throw new BusinessException("该单位信用额度不足，当前欠款" + unit.getCurrentDebt() + "元，信用额度" + unit.getCreditLimit() + "元");
            }
            checkIn.setAgreementUnitId(agreementUnitId);
            checkIn.setAgreementUnitName(unit.getUnitName());
            checkIn.setGuaranteeType(guaranteeType);
            checkIn.setIsCredit(1);
            if (unit.getDiscountRate() != null && unit.getDiscountRate().compareTo(BigDecimal.ONE) < 0) {
                BigDecimal discountAmount = roomTotal.multiply(BigDecimal.ONE.subtract(unit.getDiscountRate()));
                checkIn.setDiscount(discountAmount);
                checkIn.setTotalAmount(roomTotal.subtract(discountAmount));
                checkIn.setPayableAmount(checkIn.getTotalAmount());
            }
        }
        checkIn.setCreateTime(LocalDateTime.now());
        checkIn.setUpdateTime(LocalDateTime.now());
        checkInMapper.insert(checkIn);

        for (CheckInGuest guest : guests) {
            guest.setCheckInId(checkIn.getId());
            guest.setCheckInNo(checkIn.getCheckInNo());
            guest.setCreateTime(LocalDateTime.now());
            guest.setUpdateTime(LocalDateTime.now());
            checkInGuestMapper.insert(guest);
        }

        for (int i = 0; i < checkIn.getKeyCardCount(); i++) {
            KeyCardRecord keyCard = new KeyCardRecord();
            keyCard.setCheckInId(checkIn.getId());
            keyCard.setCheckInNo(checkIn.getCheckInNo());
            keyCard.setRoomId(roomId);
            keyCard.setRoomNumber(room.getRoomNumber());
            keyCard.setCardNo("K" + checkIn.getId() + "-" + (i + 1));
            keyCard.setCardType(1);
            keyCard.setIssueTime(LocalDateTime.now());
            keyCard.setExpireTime(checkOutDate.atTime(12, 0));
            keyCard.setStatus(1);
            keyCard.setOperatorId(user.getUserId());
            keyCard.setOperatorName(user.getUsername());
            keyCardRecordMapper.insert(keyCard);
        }

        Integer oldRoomStatus = room.getStatus();
        room.setStatus(3);
        roomMapper.update(room);

        RoomStatusLog roomStatusLog = new RoomStatusLog();
        roomStatusLog.setRoomId(roomId);
        roomStatusLog.setRoomNumber(room.getRoomNumber());
        roomStatusLog.setOldStatus(oldRoomStatus);
        roomStatusLog.setNewStatus(3);
        roomStatusLog.setOperatorId(user.getUserId());
        roomStatusLog.setOperator(user.getUsername());
        roomStatusLog.setRemark("散客入住，入住单号：" + checkIn.getCheckInNo());
        roomStatusLogService.add(roomStatusLog);

        addOperationLog(checkIn.getId(), checkIn.getCheckInNo(), 1, "散客入住",
                "散客入住，房间：" + room.getRoomNumber() + "，客户：" + savedCustomer.getName(),
                user.getUserId(), user.getUsername(), "酒店管理员");

        return checkIn;
    }

    private Customer saveOrUpdateCustomer(Customer customer) {
        if (customer.getId() != null) {
            Customer existing = customerMapper.selectOneById(customer.getId());
            if (existing != null) {
                return existing;
            }
        }
        if (StringUtils.hasText(customer.getPhone())) {
            QueryWrapper query = QueryWrapper.create()
                    .from(Customer.class)
                    .where(CUSTOMER.PHONE.eq(customer.getPhone()))
                    .and(CUSTOMER.DELETED.eq(0))
                    .limit(1);
            Customer existing = customerMapper.selectOneByQuery(query);
            if (existing != null) {
                return existing;
            }
        }
        customer.setStatus(1);
        customer.setCustomerType(1);
        customer.setCustomerSource(1);
        customerMapper.insert(customer);
        return customer;
    }

    public PageResult<CheckIn> list(Map<String, Object> params, int pageNum, int pageSize) {
        QueryWrapper query = QueryWrapper.create()
                .from(CheckIn.class)
                .where(CHECK_IN.DELETED.eq(0));

        String keyword = (String) params.get("keyword");
        if (StringUtils.hasText(keyword)) {
            query.and(CHECK_IN.CHECK_IN_NO.like(keyword)
                    .or(CHECK_IN.CUSTOMER_NAME.like(keyword))
                    .or(CHECK_IN.CUSTOMER_PHONE.like(keyword))
                    .or(CHECK_IN.ROOM_NUMBER.like(keyword)));
        }

        Long roomTypeId = (Long) params.get("roomTypeId");
        if (roomTypeId != null) {
            query.and(CHECK_IN.ROOM_TYPE_ID.eq(roomTypeId));
        }

        Integer status = (Integer) params.get("status");
        if (status != null) {
            query.and(CHECK_IN.STATUS.eq(status));
        }

        Integer isOverdue = (Integer) params.get("isOverdue");
        if (isOverdue != null) {
            query.and(CHECK_IN.IS_OVERDUE.eq(isOverdue));
        }

        LocalDate startDate = (LocalDate) params.get("startDate");
        if (startDate != null) {
            query.and(CHECK_IN.CHECK_IN_DATE.ge(startDate));
        }

        LocalDate endDate = (LocalDate) params.get("endDate");
        if (endDate != null) {
            query.and(CHECK_IN.CHECK_IN_DATE.le(endDate));
        }

        Long floorId = (Long) params.get("floorId");
        if (floorId != null) {
            List<Room> rooms = roomMapper.selectListByQuery(
                    QueryWrapper.create().from(Room.class).where(ROOM.FLOOR_ID.eq(floorId))
            );
            if (!rooms.isEmpty()) {
                List<Long> roomIds = rooms.stream().map(Room::getId).collect(Collectors.toList());
                query.and(CHECK_IN.ROOM_ID.in(roomIds));
            } else {
                query.and(CHECK_IN.ID.eq(-1L));
            }
        }

        String sortField = (String) params.get("sortField");
        String sortOrder = (String) params.get("sortOrder");
        if (StringUtils.hasText(sortField)) {
            if ("checkInTime".equals(sortField)) {
                query.orderBy(CHECK_IN.ACTUAL_CHECK_IN_TIME, "desc".equalsIgnoreCase(sortOrder));
            } else if ("roomNumber".equals(sortField)) {
                query.orderBy(CHECK_IN.ROOM_NUMBER, "asc".equalsIgnoreCase(sortOrder));
            } else if ("stayedDays".equals(sortField)) {
                query.orderBy(CHECK_IN.STAYED_DAYS, "desc".equalsIgnoreCase(sortOrder));
            }
        } else {
            query.orderBy(CHECK_IN.CREATE_TIME, false);
        }

        Page<CheckIn> page = checkInMapper.paginate(pageNum, pageSize, query);
        return new PageResult<>(page.getTotalRow(), page.getRecords(), (long) page.getPageNumber(), (long) page.getPageSize());
    }

    public CheckIn getDetail(Long id) {
        CheckIn checkIn = checkInMapper.selectOneById(id);
        if (checkIn == null) {
            throw new BusinessException("入住单不存在");
        }

        List<CheckInGuest> guests = checkInGuestMapper.selectListByQuery(
                QueryWrapper.create().from(CheckInGuest.class)
                        .where(CHECK_IN_GUEST.CHECK_IN_ID.eq(id))
                        .orderBy(CHECK_IN_GUEST.IS_MAIN, false)
        );
        checkIn.setGuests(guests);

        List<KeyCardRecord> keyCards = keyCardRecordMapper.selectListByQuery(
                QueryWrapper.create().from(KeyCardRecord.class)
                        .where(KEY_CARD_RECORD.CHECK_IN_ID.eq(id))
                        .orderBy(KEY_CARD_RECORD.CREATE_TIME, true)
        );
        checkIn.setKeyCards(keyCards);

        List<ConsumptionRecord> consumptions = consumptionRecordMapper.selectListByQuery(
                QueryWrapper.create().from(ConsumptionRecord.class)
                        .where(CONSUMPTION_RECORD.CHECK_IN_ID.eq(id))
                        .and(CONSUMPTION_RECORD.IS_SETTLED.eq(0))
                        .orderBy(CONSUMPTION_RECORD.CREATE_TIME, false)
        );
        checkIn.setConsumptions(consumptions);

        List<CheckInOperationLog> logs = checkInOperationLogMapper.selectListByQuery(
                QueryWrapper.create().from(CheckInOperationLog.class)
                        .where(CHECK_IN_OPERATION_LOG.CHECK_IN_ID.eq(id))
                        .orderBy(CHECK_IN_OPERATION_LOG.CREATE_TIME, false)
        );
        checkIn.setOperationLogs(logs);

        Room room = roomMapper.selectOneById(checkIn.getRoomId());
        checkIn.setRoom(room);

        RoomType roomType = roomTypeMapper.selectOneById(checkIn.getRoomTypeId());
        checkIn.setRoomType(roomType);

        return checkIn;
    }

    @Transactional
    public RoomChangeRecord changeRoom(Long checkInId, Long newRoomId, Integer changeReason, String changeDetail) {
        LoginUser user = getCurrentUser();
        CheckIn checkIn = checkInMapper.selectOneById(checkInId);
        if (checkIn == null) {
            throw new BusinessException("入住单不存在");
        }
        if (checkIn.getStatus() != 1) {
            throw new BusinessException("只有在住状态才能换房");
        }

        Room newRoom = roomMapper.selectOneById(newRoomId);
        if (newRoom == null) {
            throw new BusinessException("新房间不存在");
        }
        if (newRoom.getStatus() != 1) {
            throw new BusinessException("新房间状态不正确，无法换入");
        }

        Room oldRoom = roomMapper.selectOneById(checkIn.getRoomId());
        if (oldRoom == null) {
            throw new BusinessException("原房间不存在");
        }

        RoomType oldRoomType = roomTypeMapper.selectOneById(checkIn.getRoomTypeId());
        RoomType newRoomType = roomTypeMapper.selectOneById(newRoom.getRoomTypeId());

        BigDecimal oldPrice = checkIn.getRoomPrice();
        BigDecimal newPrice = newRoomType.getBasePrice() != null ? newRoomType.getBasePrice() : oldPrice;
        BigDecimal priceDiff = newPrice.subtract(oldPrice);

        LocalDate today = LocalDate.now();
        int remainingDays = (int) ChronoUnit.DAYS.between(today, checkIn.getCheckOutDate());
        if (remainingDays < 0) remainingDays = 0;

        BigDecimal totalDiff = priceDiff.multiply(BigDecimal.valueOf(remainingDays));

        RoomChangeRecord record = new RoomChangeRecord();
        record.setChangeNo(generateChangeNo());
        record.setCheckInId(checkInId);
        record.setCheckInNo(checkIn.getCheckInNo());
        record.setOldRoomId(checkIn.getRoomId());
        record.setOldRoomNumber(checkIn.getRoomNumber());
        record.setOldRoomTypeId(checkIn.getRoomTypeId());
        record.setOldRoomTypeName(checkIn.getRoomTypeName());
        record.setOldRoomPrice(oldPrice);
        record.setNewRoomId(newRoomId);
        record.setNewRoomNumber(newRoom.getRoomNumber());
        record.setNewRoomTypeId(newRoom.getRoomTypeId());
        record.setNewRoomTypeName(newRoomType.getTypeName());
        record.setNewRoomPrice(newPrice);
        record.setPriceDiff(totalDiff);
        record.setChangeReason(changeReason);
        record.setChangeDetail(changeDetail);
        record.setChangeTime(LocalDateTime.now());
        record.setOperatorId(user.getUserId());
        record.setOperatorName(user.getUsername());
        roomChangeRecordMapper.insert(record);

        BigDecimal newRoomTotal = newPrice.multiply(BigDecimal.valueOf(checkIn.getDays()));
        BigDecimal newTotal = newRoomTotal.add(checkIn.getExtraBedTotal()).subtract(checkIn.getDiscount());
        checkIn.setRoomId(newRoomId);
        checkIn.setRoomNumber(newRoom.getRoomNumber());
        checkIn.setRoomTypeId(newRoom.getRoomTypeId());
        checkIn.setRoomTypeName(newRoomType.getTypeName());
        checkIn.setRoomPrice(newPrice);
        checkIn.setRoomTotal(newRoomTotal);
        checkIn.setTotalAmount(newTotal);
        checkIn.setPayableAmount(newTotal.subtract(checkIn.getPaidAmount()));
        checkInMapper.update(checkIn);

        Integer oldRoomOldStatus = oldRoom.getStatus();
        oldRoom.setStatus(4);
        roomMapper.update(oldRoom);

        Integer newRoomOldStatus = newRoom.getStatus();
        newRoom.setStatus(3);
        roomMapper.update(newRoom);

        RoomStatusLog oldRoomLog = new RoomStatusLog();
        oldRoomLog.setRoomId(oldRoom.getId());
        oldRoomLog.setRoomNumber(oldRoom.getRoomNumber());
        oldRoomLog.setOldStatus(oldRoomOldStatus);
        oldRoomLog.setNewStatus(4);
        oldRoomLog.setOperatorId(user.getUserId());
        oldRoomLog.setOperator(user.getUsername());
        oldRoomLog.setRemark("换房搬出，新房间：" + newRoom.getRoomNumber());
        roomStatusLogService.add(oldRoomLog);

        RoomStatusLog newRoomLog = new RoomStatusLog();
        newRoomLog.setRoomId(newRoomId);
        newRoomLog.setRoomNumber(newRoom.getRoomNumber());
        newRoomLog.setOldStatus(newRoomOldStatus);
        newRoomLog.setNewStatus(3);
        newRoomLog.setOperatorId(user.getUserId());
        newRoomLog.setOperator(user.getUsername());
        newRoomLog.setRemark("换房搬入，原房间：" + oldRoom.getRoomNumber());
        roomStatusLogService.add(newRoomLog);

        List<KeyCardRecord> keyCards = keyCardRecordMapper.selectListByQuery(
                QueryWrapper.create().from(KeyCardRecord.class)
                        .where(KEY_CARD_RECORD.CHECK_IN_ID.eq(checkInId))
                        .and(KEY_CARD_RECORD.STATUS.eq(1))
        );
        for (KeyCardRecord keyCard : keyCards) {
            keyCard.setStatus(4);
            keyCard.setReturnTime(LocalDateTime.now());
            keyCard.setReturnOperatorId(user.getUserId());
            keyCard.setReturnOperatorName(user.getUsername());
            keyCardRecordMapper.update(keyCard);
        }

        for (int i = 0; i < checkIn.getKeyCardCount(); i++) {
            KeyCardRecord keyCard = new KeyCardRecord();
            keyCard.setCheckInId(checkInId);
            keyCard.setCheckInNo(checkIn.getCheckInNo());
            keyCard.setRoomId(newRoomId);
            keyCard.setRoomNumber(newRoom.getRoomNumber());
            keyCard.setCardNo("K" + checkInId + "-" + (i + 1) + "-N");
            keyCard.setCardType(1);
            keyCard.setIssueTime(LocalDateTime.now());
            keyCard.setExpireTime(checkIn.getCheckOutDate().atTime(12, 0));
            keyCard.setStatus(1);
            keyCard.setOperatorId(user.getUserId());
            keyCard.setOperatorName(user.getUsername());
            keyCardRecordMapper.insert(keyCard);
        }

        addOperationLog(checkInId, checkIn.getCheckInNo(), 3, "办理换房",
                "从" + checkIn.getRoomNumber() + "换到" + newRoom.getRoomNumber() + "，差价：" + totalDiff + "元",
                user.getUserId(), user.getUsername(), "酒店管理员");

        return record;
    }

    @Transactional
    public ExtendStayRecord extendStay(Long checkInId, LocalDate newCheckOutDate, String reason) {
        LoginUser user = getCurrentUser();
        CheckIn checkIn = checkInMapper.selectOneById(checkInId);
        if (checkIn == null) {
            throw new BusinessException("入住单不存在");
        }
        if (checkIn.getStatus() != 1) {
            throw new BusinessException("只有在住状态才能续住");
        }

        LocalDate originalCheckOutDate = checkIn.getCheckOutDate();
        if (newCheckOutDate == null || !newCheckOutDate.isAfter(originalCheckOutDate)) {
            throw new BusinessException("新退房日期必须晚于原退房日期");
        }

        int extendDays = (int) ChronoUnit.DAYS.between(originalCheckOutDate, newCheckOutDate);
        if (extendDays <= 0) {
            throw new BusinessException("续住天数必须大于0");
        }

        boolean available = checkRoomAvailability(checkIn.getRoomId(), originalCheckOutDate, newCheckOutDate);
        if (!available) {
            throw new BusinessException("该房间在续住日期内已有预订，无法续住");
        }

        BigDecimal extendAmount = checkIn.getRoomPrice().multiply(BigDecimal.valueOf(extendDays));

        ExtendStayRecord record = new ExtendStayRecord();
        record.setExtendNo(generateExtendNo());
        record.setCheckInId(checkInId);
        record.setCheckInNo(checkIn.getCheckInNo());
        record.setRoomId(checkIn.getRoomId());
        record.setRoomNumber(checkIn.getRoomNumber());
        record.setOriginalCheckOutDate(originalCheckOutDate);
        record.setNewCheckOutDate(newCheckOutDate);
        record.setExtendDays(extendDays);
        record.setRoomPrice(checkIn.getRoomPrice());
        record.setExtendAmount(extendAmount);
        record.setReason(reason);
        record.setExtendTime(LocalDateTime.now());
        record.setOperatorId(user.getUserId());
        record.setOperatorName(user.getUsername());
        extendStayRecordMapper.insert(record);

        int totalDays = checkIn.getDays() + extendDays;
        BigDecimal newRoomTotal = checkIn.getRoomPrice().multiply(BigDecimal.valueOf(totalDays));
        BigDecimal newTotalAmount = newRoomTotal.add(checkIn.getExtraBedTotal()).subtract(checkIn.getDiscount());
        BigDecimal newPayableAmount = newTotalAmount.subtract(checkIn.getPaidAmount());

        checkIn.setCheckOutDate(newCheckOutDate);
        checkIn.setDays(totalDays);
        checkIn.setRoomTotal(newRoomTotal);
        checkIn.setTotalAmount(newTotalAmount);
        checkIn.setPayableAmount(newPayableAmount);
        checkIn.setIsOverdue(0);
        checkInMapper.update(checkIn);

        List<KeyCardRecord> keyCards = keyCardRecordMapper.selectListByQuery(
                QueryWrapper.create().from(KeyCardRecord.class)
                        .where(KEY_CARD_RECORD.CHECK_IN_ID.eq(checkInId))
                        .and(KEY_CARD_RECORD.STATUS.eq(1))
        );
        for (KeyCardRecord keyCard : keyCards) {
            keyCard.setExpireTime(newCheckOutDate.atTime(12, 0));
            keyCardRecordMapper.update(keyCard);
        }

        addOperationLog(checkInId, checkIn.getCheckInNo(), 4, "办理续住",
                "续住" + extendDays + "天，从" + originalCheckOutDate + "到" + newCheckOutDate + "，续住费用：" + extendAmount + "元",
                user.getUserId(), user.getUsername(), "酒店管理员");

        return record;
    }

    private boolean checkRoomAvailability(Long roomId, LocalDate startDate, LocalDate endDate) {
        List<Booking> bookings = bookingMapper.selectListByQuery(
                QueryWrapper.create().from(Booking.class)
                        .where(BOOKING.ROOM_ID.eq(roomId))
                        .and(BOOKING.STATUS.in(1, 2, 3, 4))
                        .and(BOOKING.DELETED.eq(0))
                        .and(BOOKING.CHECK_IN_DATE.lt(endDate))
                        .and(BOOKING.CHECK_OUT_DATE.gt(startDate))
        );
        return bookings.isEmpty();
    }

    public List<Room> getAvailableRoomsForChange(Long checkInId, Long roomTypeId) {
        CheckIn checkIn = checkInMapper.selectOneById(checkInId);
        if (checkIn == null) {
            throw new BusinessException("入住单不存在");
        }

        QueryWrapper query = QueryWrapper.create()
                .from(Room.class)
                .where(ROOM.STATUS.eq(1))
                .and(ROOM.DELETED.eq(0));

        if (roomTypeId != null) {
            query.and(ROOM.ROOM_TYPE_ID.eq(roomTypeId));
        }

        query.and(ROOM.ID.ne(checkIn.getRoomId()));

        return roomMapper.selectListByQuery(query);
    }

    public List<Room> getAvailableRoomsForWalkIn(Long roomTypeId, LocalDate checkInDate, LocalDate checkOutDate) {
        if (checkInDate == null) {
            checkInDate = LocalDate.now();
        }
        if (checkOutDate == null) {
            checkOutDate = checkInDate.plusDays(1);
        }

        return bookingService.queryAvailableRoomList(
                checkInDate, checkOutDate, roomTypeId, null, null, null, null
        );
    }

    @Transactional
    public CheckOutRecord checkOut(Long checkInId, Integer paymentMethod, String paymentVoucherNo,
                                    Integer depositMethod, Integer keyCardReturned, Integer keyCardLost,
                                    Integer roomChecked, Integer roomCheckResult, String damageItems,
                                    String damageDescription, BigDecimal damageCompensation, String remark) {
        LoginUser user = getCurrentUser();
        CheckIn checkIn = checkInMapper.selectOneById(checkInId);
        if (checkIn == null) {
            throw new BusinessException("入住单不存在");
        }
        if (checkIn.getStatus() != 1 && checkIn.getStatus() != 3) {
            throw new BusinessException("只有在住或超期未退状态才能退房");
        }

        if (keyCardReturned == null) {
            keyCardReturned = 0;
        }
        if (keyCardReturned > checkIn.getKeyCardCount()) {
            throw new BusinessException("回收房卡数量不能超过发放数量");
        }

        LocalDate checkOutDate = LocalDate.now();
        LocalDateTime actualCheckOutTime = LocalDateTime.now();
        int stayedDays = (int) ChronoUnit.DAYS.between(checkIn.getCheckInDate(), checkOutDate);
        if (stayedDays <= 0) {
            stayedDays = 1;
        }

        BigDecimal roomTotal = checkIn.getRoomPrice().multiply(BigDecimal.valueOf(stayedDays));
        BigDecimal extraBedTotal = checkIn.getExtraBedTotal();
        BigDecimal otherFee = checkIn.getOtherFee();
        BigDecimal damageComp = damageCompensation != null ? damageCompensation : BigDecimal.ZERO;
        BigDecimal discount = checkIn.getDiscount();
        BigDecimal totalAmount = roomTotal.add(extraBedTotal).add(otherFee).add(damageComp).subtract(discount);

        BigDecimal paidAmount = checkIn.getPaidAmount();
        BigDecimal depositAmount = checkIn.getDepositAmount();

        BigDecimal payable = totalAmount.subtract(paidAmount);
        BigDecimal depositDeducted = BigDecimal.ZERO;
        BigDecimal depositRefund = BigDecimal.ZERO;
        BigDecimal additionalPayment = BigDecimal.ZERO;
        BigDecimal refundAmount = BigDecimal.ZERO;

        if (depositAmount.compareTo(BigDecimal.ZERO) > 0) {
            if (payable.compareTo(BigDecimal.ZERO) > 0) {
                if (depositAmount.compareTo(payable) >= 0) {
                    depositDeducted = payable;
                    depositRefund = depositAmount.subtract(payable);
                    payable = BigDecimal.ZERO;
                } else {
                    depositDeducted = depositAmount;
                    payable = payable.subtract(depositAmount);
                }
            } else {
                depositRefund = depositAmount;
            }
        }

        if (payable.compareTo(BigDecimal.ZERO) > 0) {
            additionalPayment = payable;
        } else if (payable.compareTo(BigDecimal.ZERO) < 0) {
            refundAmount = payable.negate();
        }

        CheckOutRecord record = new CheckOutRecord();
        record.setCheckOutNo(generateCheckOutNo());
        record.setCheckInId(checkInId);
        record.setCheckInNo(checkIn.getCheckInNo());
        record.setCustomerId(checkIn.getCustomerId());
        record.setCustomerName(checkIn.getCustomerName());
        record.setRoomId(checkIn.getRoomId());
        record.setRoomNumber(checkIn.getRoomNumber());
        record.setCheckInDate(checkIn.getCheckInDate());
        record.setCheckOutDate(checkOutDate);
        record.setActualCheckOutTime(actualCheckOutTime);
        record.setStayedDays(stayedDays);
        record.setRoomTotal(roomTotal);
        record.setExtraBedTotal(extraBedTotal);
        record.setOtherFee(otherFee);
        record.setDamageCompensation(damageComp);
        record.setDiscount(discount);
        record.setTotalAmount(totalAmount);
        record.setPaidAmount(paidAmount);
        record.setDepositAmount(depositAmount);
        record.setDepositDeducted(depositDeducted);
        record.setDepositRefund(depositRefund);
        record.setAdditionalPayment(additionalPayment);
        record.setRefundAmount(refundAmount);
        record.setPayableAmount(additionalPayment.subtract(refundAmount));
        record.setPaymentMethod(paymentMethod);
        record.setPaymentVoucherNo(paymentVoucherNo);
        record.setDepositMethod(depositMethod);
        record.setKeyCardReturned(keyCardReturned);
        record.setKeyCardLost(keyCardLost != null ? keyCardLost : 0);
        record.setRoomChecked(roomChecked != null && roomChecked == 1 ? 1 : 0);
        record.setRoomCheckResult(roomCheckResult);
        record.setDamageItems(damageItems);
        record.setDamageDescription(damageDescription);
        record.setStatus(1);
        record.setOperatorId(user.getUserId());
        record.setOperatorName(user.getUsername());
        record.setRemark(remark);
        checkOutRecordMapper.insert(record);

        checkIn.setStatus(2);
        checkIn.setActualCheckOutTime(actualCheckOutTime);
        checkIn.setStayedDays(stayedDays);
        checkIn.setKeyCardReturned(keyCardReturned);
        checkIn.setCheckOutOperatorId(user.getUserId());
        checkIn.setCheckOutOperatorName(user.getUsername());
        checkInMapper.update(checkIn);

        if (checkIn.getIsCredit() != null && checkIn.getIsCredit() == 1 && checkIn.getAgreementUnitId() != null) {
            CreditBill creditBill = new CreditBill();
            creditBill.setAgreementUnitId(checkIn.getAgreementUnitId());
            creditBill.setAgreementUnitName(checkIn.getAgreementUnitName());
            creditBill.setCheckInId(checkInId);
            creditBill.setCheckInNo(checkIn.getCheckInNo());
            creditBill.setCustomerName(checkIn.getCustomerName());
            creditBill.setRoomNumber(checkIn.getRoomNumber());
            creditBill.setCheckInDate(checkIn.getCheckInDate());
            creditBill.setCheckOutDate(checkOutDate);
            creditBill.setRoomFee(roomTotal);
            creditBill.setExtraFee(extraBedTotal.add(otherFee));
            creditBill.setDiscountAmount(discount);
            creditBill.setTotalAmount(totalAmount);
            creditBillService.createCreditBill(creditBill);
        }

        Room room = roomMapper.selectOneById(checkIn.getRoomId());
        Integer oldRoomStatus = room.getStatus();
        room.setStatus(4);
        roomMapper.update(room);

        RoomStatusLog roomStatusLog = new RoomStatusLog();
        roomStatusLog.setRoomId(checkIn.getRoomId());
        roomStatusLog.setRoomNumber(checkIn.getRoomNumber());
        roomStatusLog.setOldStatus(oldRoomStatus);
        roomStatusLog.setNewStatus(4);
        roomStatusLog.setOperatorId(user.getUserId());
        roomStatusLog.setOperator(user.getUsername());
        roomStatusLog.setRemark("办理退房，退房单号：" + record.getCheckOutNo());
        roomStatusLogService.add(roomStatusLog);

        List<KeyCardRecord> keyCards = keyCardRecordMapper.selectListByQuery(
                QueryWrapper.create().from(KeyCardRecord.class)
                        .where(KEY_CARD_RECORD.CHECK_IN_ID.eq(checkInId))
                        .and(KEY_CARD_RECORD.STATUS.eq(1))
        );
        for (KeyCardRecord keyCard : keyCards) {
            keyCard.setStatus(2);
            keyCard.setReturnTime(actualCheckOutTime);
            keyCard.setReturnOperatorId(user.getUserId());
            keyCard.setReturnOperatorName(user.getUsername());
            keyCardRecordMapper.update(keyCard);
        }

        List<ConsumptionRecord> consumptions = consumptionRecordMapper.selectListByQuery(
                QueryWrapper.create().from(ConsumptionRecord.class)
                        .where(CONSUMPTION_RECORD.CHECK_IN_ID.eq(checkInId))
                        .and(CONSUMPTION_RECORD.IS_SETTLED.eq(0))
        );
        for (ConsumptionRecord consumption : consumptions) {
            consumption.setIsSettled(1);
            consumption.setSettleTime(actualCheckOutTime);
            consumptionRecordMapper.update(consumption);
        }

        addOperationLog(checkInId, checkIn.getCheckInNo(), 9, "办理退房",
                "退房，总费用：" + totalAmount + "元，押金退还：" + depositRefund + "元，补收：" + additionalPayment + "元",
                user.getUserId(), user.getUsername(), "酒店管理员");

        Map<String, Object> pointResult = null;
        if (checkIn.getMemberId() != null) {
            pointResult = memberService.earnPointsOnCheckout(checkIn.getMemberId(), totalAmount,
                    user.getUserId(), user.getUsername(), 2, checkInId);
            BigDecimal earnedPoints = pointResult != null && pointResult.get("earnedPoints") != null
                    ? (BigDecimal) pointResult.get("earnedPoints") : BigDecimal.ZERO;
            checkIn.setEarnedPoints(earnedPoints);
            checkInMapper.update(checkIn);
        }

        record.setPointInfo(pointResult);
        record.setMemberId(checkIn.getMemberId());
        record.setMemberNo(checkIn.getMemberNo());
        record.setEarnedPoints(checkIn.getEarnedPoints());

        return record;
    }

    public CheckOutRecord getCheckOutRecord(Long checkOutId) {
        return checkOutRecordMapper.selectOneById(checkOutId);
    }

    public CheckOutRecord getCheckOutByCheckInId(Long checkInId) {
        return checkOutRecordMapper.selectOneByQuery(
                QueryWrapper.create().from(CheckOutRecord.class)
                        .where(CHECK_OUT_RECORD.CHECK_IN_ID.eq(checkInId))
                        .orderBy(CHECK_OUT_RECORD.CREATE_TIME, false)
                        .limit(1)
        );
    }

    @Transactional
    public void addConsumption(Long checkInId, ConsumptionRecord consumption) {
        LoginUser user = getCurrentUser();
        CheckIn checkIn = checkInMapper.selectOneById(checkInId);
        if (checkIn == null) {
            throw new BusinessException("入住单不存在");
        }
        if (checkIn.getStatus() != 1) {
            throw new BusinessException("只有在住状态才能添加消费");
        }

        if (consumption.getConsumptionType() == null) {
            throw new BusinessException("请选择消费类型");
        }
        if (!StringUtils.hasText(consumption.getItemName())) {
            throw new BusinessException("请输入消费项目名称");
        }
        if (consumption.getUnitPrice() == null || consumption.getUnitPrice().compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessException("请输入有效的单价");
        }
        if (consumption.getQuantity() == null || consumption.getQuantity().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("请输入有效的数量");
        }

        BigDecimal totalAmount = consumption.getUnitPrice().multiply(consumption.getQuantity());

        consumption.setConsumptionNo(generateConsumptionNo());
        consumption.setCheckInId(checkInId);
        consumption.setCheckInNo(checkIn.getCheckInNo());
        consumption.setRoomId(checkIn.getRoomId());
        consumption.setRoomNumber(checkIn.getRoomNumber());
        consumption.setCustomerId(checkIn.getCustomerId());
        consumption.setCustomerName(checkIn.getCustomerName());
        consumption.setTotalAmount(totalAmount);
        consumption.setConsumptionTime(LocalDateTime.now());
        consumption.setBillingMethod(1);
        consumption.setIsSettled(0);
        consumption.setOperatorId(user.getUserId());
        consumption.setOperatorName(user.getUsername());
        consumptionRecordMapper.insert(consumption);

        BigDecimal newOtherFee = checkIn.getOtherFee().add(totalAmount);
        BigDecimal newTotalAmount = checkIn.getTotalAmount().add(totalAmount);
        BigDecimal newPayableAmount = checkIn.getPayableAmount().add(totalAmount);

        checkIn.setOtherFee(newOtherFee);
        checkIn.setTotalAmount(newTotalAmount);
        checkIn.setPayableAmount(newPayableAmount);
        checkInMapper.update(checkIn);

        addOperationLog(checkInId, checkIn.getCheckInNo(), 5, "添加消费",
                "添加消费：" + consumption.getItemName() + "，金额：" + totalAmount + "元",
                user.getUserId(), user.getUsername(), "酒店管理员");
    }

    @Transactional
    public void updateDeposit(Long checkInId, BigDecimal depositAmount, Integer depositMethod, String depositVoucherNo) {
        LoginUser user = getCurrentUser();
        CheckIn checkIn = checkInMapper.selectOneById(checkInId);
        if (checkIn == null) {
            throw new BusinessException("入住单不存在");
        }
        if (checkIn.getStatus() != 1) {
            throw new BusinessException("只有在住状态才能修改押金");
        }

        if (depositAmount == null || depositAmount.compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessException("押金金额不能为负数");
        }

        checkIn.setDepositAmount(depositAmount);
        if (depositMethod != null) {
            checkIn.setDepositMethod(depositMethod);
        }
        if (StringUtils.hasText(depositVoucherNo)) {
            checkIn.setDepositVoucherNo(depositVoucherNo);
        }
        checkInMapper.update(checkIn);

        addOperationLog(checkInId, checkIn.getCheckInNo(), 6, "押金变更",
                "押金变更为：" + depositAmount + "元",
                user.getUserId(), user.getUsername(), "酒店管理员");
    }

    public List<RoomChangeRecord> getChangeRecords(Long checkInId) {
        return roomChangeRecordMapper.selectListByQuery(
                QueryWrapper.create().from(RoomChangeRecord.class)
                        .where(ROOM_CHANGE_RECORD.CHECK_IN_ID.eq(checkInId))
                        .orderBy(ROOM_CHANGE_RECORD.CREATE_TIME, false)
        );
    }

    public List<ExtendStayRecord> getExtendRecords(Long checkInId) {
        return extendStayRecordMapper.selectListByQuery(
                QueryWrapper.create().from(ExtendStayRecord.class)
                        .where(EXTEND_STAY_RECORD.CHECK_IN_ID.eq(checkInId))
                        .orderBy(EXTEND_STAY_RECORD.CREATE_TIME, false)
        );
    }

    private void addOperationLog(Long checkInId, String checkInNo, Integer operationType,
                                  String operationDesc, String detail, Long operatorId,
                                  String operatorName, String operatorRole) {
        CheckInOperationLog log = new CheckInOperationLog();
        log.setCheckInId(checkInId);
        log.setCheckInNo(checkInNo);
        log.setOperationType(operationType);
        log.setOperationDesc(operationDesc);
        log.setDetail(detail);
        log.setOperatorId(operatorId);
        log.setOperatorName(operatorName);
        log.setOperatorRole(operatorRole);
        checkInOperationLogMapper.insert(log);
    }

    @Transactional
    public void updateOverdueStatus() {
        LocalDate today = LocalDate.now();
        List<CheckIn> overdueList = checkInMapper.selectListByQuery(
                QueryWrapper.create().from(CheckIn.class)
                        .where(CHECK_IN.STATUS.eq(1))
                        .and(CHECK_IN.IS_OVERDUE.eq(0))
                        .and(CHECK_IN.CHECK_OUT_DATE.lt(today))
                        .and(CHECK_IN.DELETED.eq(0))
        );

        for (CheckIn checkIn : overdueList) {
            checkIn.setStatus(3);
            checkIn.setIsOverdue(1);
            checkInMapper.update(checkIn);

            addOperationLog(checkIn.getId(), checkIn.getCheckInNo(), 11, "超期未退标记",
                    "系统自动标记超期未退，预计退房日期：" + checkIn.getCheckOutDate(),
                    null, "系统", "系统");
        }
    }

    public Map<String, Object> getTodayStats() {
        LocalDate today = LocalDate.now();
        Map<String, Object> result = new HashMap<>();

        Long todayCheckIn = checkInMapper.selectCountByQuery(
                QueryWrapper.create().from(CheckIn.class)
                        .where(CHECK_IN.CHECK_IN_DATE.eq(today))
                        .and(CHECK_IN.DELETED.eq(0))
        );

        Long todayCheckOut = checkOutRecordMapper.selectCountByQuery(
                QueryWrapper.create().from(CheckOutRecord.class)
                        .where(CHECK_OUT_RECORD.CHECK_OUT_DATE.eq(today))
        );

        Long currentInHouse = checkInMapper.selectCountByQuery(
                QueryWrapper.create().from(CheckIn.class)
                        .where(CHECK_IN.STATUS.eq(1))
                        .and(CHECK_IN.DELETED.eq(0))
        );

        Long expectedCheckOut = checkInMapper.selectCountByQuery(
                QueryWrapper.create().from(CheckIn.class)
                        .where(CHECK_IN.STATUS.eq(1))
                        .and(CHECK_IN.CHECK_OUT_DATE.eq(today))
                        .and(CHECK_IN.DELETED.eq(0))
        );

        Long overdueCount = checkInMapper.selectCountByQuery(
                QueryWrapper.create().from(CheckIn.class)
                        .where(CHECK_IN.IS_OVERDUE.eq(1))
                        .and(CHECK_IN.DELETED.eq(0))
        );

        result.put("todayCheckIn", todayCheckIn);
        result.put("todayCheckOut", todayCheckOut);
        result.put("currentInHouse", currentInHouse);
        result.put("expectedCheckOut", expectedCheckOut);
        result.put("overdueCount", overdueCount);

        return result;
    }
}
