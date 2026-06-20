package com.example.permission.service;

import com.example.permission.common.BusinessException;
import com.example.permission.common.PageResult;
import com.example.permission.entity.*;
import com.example.permission.mapper.*;
import com.example.permission.security.LoginUser;
import com.example.permission.service.MemberService;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import static com.example.permission.entity.table.BookingChangeLogTableDef.BOOKING_CHANGE_LOG;
import static com.example.permission.entity.table.BookingDetailTableDef.BOOKING_DETAIL;
import static com.example.permission.entity.table.BookingStatusLogTableDef.BOOKING_STATUS_LOG;
import static com.example.permission.entity.table.BookingTableDef.BOOKING;
import static com.example.permission.entity.table.CustomerBlacklistTableDef.CUSTOMER_BLACKLIST;
import static com.example.permission.entity.table.RefundRecordTableDef.REFUND_RECORD;
import static com.example.permission.entity.table.RoomStatusLogTableDef.ROOM_STATUS_LOG;
import static com.example.permission.entity.table.RoomTableDef.ROOM;
import static com.example.permission.entity.table.RoomTypeTableDef.ROOM_TYPE;

@Service
public class BookingService {

    @Autowired
    private BookingMapper bookingMapper;

    @Autowired
    private BookingDetailMapper bookingDetailMapper;

    @Autowired
    private BookingStatusLogMapper bookingStatusLogMapper;

    @Autowired
    private BookingChangeLogMapper bookingChangeLogMapper;

    @Autowired
    private RefundRecordMapper refundRecordMapper;

    @Autowired
    private RoomMapper roomMapper;

    @Autowired
    private RoomTypeMapper roomTypeMapper;

    @Autowired
    private BuildingMapper buildingMapper;

    @Autowired
    private FloorMapper floorMapper;

    @Autowired
    private CustomerMapper customerMapper;

    @Autowired
    private CustomerBlacklistMapper customerBlacklistMapper;

    @Autowired
    private RoomStatusLogMapper roomStatusLogMapper;

    @Autowired
    private RoomService roomService;

    @Autowired
    private MemberService memberService;

    public Map<Long, List<Room>> queryAvailableRooms(LocalDate checkInDate, LocalDate checkOutDate,
                                                      Long roomTypeId, Long floorId, String orientation,
                                                      String viewType) {
        List<Room> availableRooms = queryAvailableRoomList(checkInDate, checkOutDate, roomTypeId, null, floorId, orientation, viewType);
        return availableRooms.stream()
                .collect(Collectors.groupingBy(Room::getRoomTypeId));
    }

    public List<Room> queryAvailableRoomList(LocalDate checkInDate, LocalDate checkOutDate,
                                              Long roomTypeId, Long buildingId, Long floorId,
                                              String orientation, String viewType) {
        if (checkInDate == null || checkOutDate == null) {
            throw new BusinessException("入住日期和退房日期不能为空");
        }
        if (checkOutDate.isBefore(checkInDate) || checkOutDate.isEqual(checkInDate)) {
            throw new BusinessException("退房日期必须晚于入住日期");
        }

        QueryWrapper roomQuery = QueryWrapper.create()
                .from(Room.class)
                .where(ROOM.DELETED.eq(0))
                .and(ROOM.STATUS.eq(1));

        if (roomTypeId != null) {
            roomQuery.and(ROOM.ROOM_TYPE_ID.eq(roomTypeId));
        }
        if (buildingId != null) {
            roomQuery.and(ROOM.BUILDING_ID.eq(buildingId));
        }
        if (floorId != null) {
            roomQuery.and(ROOM.FLOOR_ID.eq(floorId));
        }
        if (StringUtils.hasText(orientation)) {
            roomQuery.and(ROOM.ORIENTATION.eq(orientation));
        }
        if (StringUtils.hasText(viewType)) {
            roomQuery.and(ROOM.VIEW_TYPE.eq(viewType));
        }

        List<Room> allRooms = roomMapper.selectListByQuery(roomQuery);

        List<Long> bookedRoomIds = findBookedRoomIds(checkInDate, checkOutDate, null);

        List<Room> availableRooms = allRooms.stream()
                .filter(room -> !bookedRoomIds.contains(room.getId()))
                .collect(Collectors.toList());

        for (Room room : availableRooms) {
            Building building = buildingMapper.selectOneById(room.getBuildingId());
            if (building != null) {
                room.setBuildingName(building.getBuildingName());
            }
            Floor floor = floorMapper.selectOneById(room.getFloorId());
            if (floor != null) {
                room.setFloorName(floor.getFloorName());
                room.setFloorNumber(floor.getFloorNumber());
            }
            RoomType roomType = roomTypeMapper.selectOneById(room.getRoomTypeId());
            if (roomType != null) {
                room.setRoomTypeName(roomType.getTypeName());
                room.setRoomType(roomType);
            }
        }

        return availableRooms;
    }

    private List<Long> findBookedRoomIds(LocalDate checkInDate, LocalDate checkOutDate, Long excludeBookingId) {
        QueryWrapper bookingQuery = QueryWrapper.create()
                .from(Booking.class)
                .where(BOOKING.DELETED.eq(0))
                .and(BOOKING.STATUS.in(Arrays.asList(1, 2, 3, 5)))
                .and(BOOKING.CHECK_OUT_DATE.gt(checkInDate))
                .and(BOOKING.CHECK_IN_DATE.lt(checkOutDate));

        if (excludeBookingId != null) {
            bookingQuery.and(BOOKING.ID.ne(excludeBookingId));
        }

        List<Booking> bookings = bookingMapper.selectListByQuery(bookingQuery);
        return bookings.stream()
                .map(Booking::getRoomId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
    }

    public boolean checkRoomAvailability(Long roomId, LocalDate checkInDate, LocalDate checkOutDate,
                                         Long excludeBookingId) {
        if (roomId == null || checkInDate == null || checkOutDate == null) {
            return false;
        }
        List<Long> bookedRoomIds = findBookedRoomIds(checkInDate, checkOutDate, excludeBookingId);
        return !bookedRoomIds.contains(roomId);
    }

    public boolean isCustomerBlacklisted(Long customerId) {
        if (customerId == null) {
            return false;
        }
        QueryWrapper query = QueryWrapper.create()
                .from(CustomerBlacklist.class)
                .where(CUSTOMER_BLACKLIST.CUSTOMER_ID.eq(customerId))
                .and(CUSTOMER_BLACKLIST.STATUS.eq(2))
                .and(CUSTOMER_BLACKLIST.EXPIRE_TIME.isNull()
                        .or(CUSTOMER_BLACKLIST.EXPIRE_TIME.gt(LocalDateTime.now())));
        return customerBlacklistMapper.selectCountByQuery(query) > 0;
    }

    public BigDecimal calculatePrice(RoomType roomType, LocalDate checkInDate, LocalDate checkOutDate,
                                     Integer extraBedCount, BigDecimal extraBedPrice) {
        if (roomType == null || checkInDate == null || checkOutDate == null) {
            return BigDecimal.ZERO;
        }

        BigDecimal total = BigDecimal.ZERO;
        LocalDate currentDate = checkInDate;

        while (currentDate.isBefore(checkOutDate)) {
            DayOfWeek dayOfWeek = currentDate.getDayOfWeek();
            BigDecimal dayPrice;
            if (dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY) {
                dayPrice = roomType.getWeekendPrice() != null ? roomType.getWeekendPrice() : roomType.getBasePrice();
            } else {
                dayPrice = roomType.getBasePrice() != null ? roomType.getBasePrice() : BigDecimal.ZERO;
            }
            total = total.add(dayPrice);
            currentDate = currentDate.plusDays(1);
        }

        if (extraBedCount != null && extraBedCount > 0 && extraBedPrice != null) {
            int days = (int) java.time.temporal.ChronoUnit.DAYS.between(checkInDate, checkOutDate);
            BigDecimal extraBedTotal = extraBedPrice.multiply(BigDecimal.valueOf(extraBedCount))
                    .multiply(BigDecimal.valueOf(days));
            total = total.add(extraBedTotal);
        }

        return total.setScale(2, RoundingMode.HALF_UP);
    }

    public Map<String, Object> calculatePriceWithDetails(RoomType roomType, LocalDate checkInDate,
                                                         LocalDate checkOutDate, Integer extraBedCount,
                                                         BigDecimal extraBedPrice) {
        if (roomType == null || checkInDate == null || checkOutDate == null) {
            throw new BusinessException("房型和日期不能为空");
        }
        if (checkOutDate.isBefore(checkInDate) || checkOutDate.isEqual(checkInDate)) {
            throw new BusinessException("退房日期必须晚于入住日期");
        }

        List<Map<String, Object>> details = new ArrayList<>();
        BigDecimal roomTotal = BigDecimal.ZERO;
        LocalDate currentDate = checkInDate;
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String[] weekdays = {"日", "一", "二", "三", "四", "五", "六"};

        while (currentDate.isBefore(checkOutDate)) {
            DayOfWeek dayOfWeek = currentDate.getDayOfWeek();
            BigDecimal dayPrice;
            String dayType;
            String description;
            if (dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY) {
                dayPrice = roomType.getWeekendPrice() != null ? roomType.getWeekendPrice() : roomType.getBasePrice();
                dayType = "周末";
                description = "周末房价";
            } else {
                dayPrice = roomType.getBasePrice() != null ? roomType.getBasePrice() : BigDecimal.ZERO;
                dayType = "平日";
                description = "平日房价";
            }

            Map<String, Object> roomDetail = new HashMap<>();
            roomDetail.put("date", currentDate.format(dateFormatter));
            roomDetail.put("weekday", "周" + weekdays[dayOfWeek.getValue() % 7]);
            roomDetail.put("type", "room");
            roomDetail.put("dayType", dayType);
            roomDetail.put("description", description);
            roomDetail.put("price", dayPrice.setScale(2, RoundingMode.HALF_UP));
            details.add(roomDetail);

            roomTotal = roomTotal.add(dayPrice);

            if (extraBedCount != null && extraBedCount > 0 && extraBedPrice != null) {
                BigDecimal extraBedDayPrice = extraBedPrice.multiply(BigDecimal.valueOf(extraBedCount));
                Map<String, Object> extraBedDetail = new HashMap<>();
                extraBedDetail.put("date", currentDate.format(dateFormatter));
                extraBedDetail.put("weekday", "周" + weekdays[dayOfWeek.getValue() % 7]);
                extraBedDetail.put("type", "extraBed");
                extraBedDetail.put("dayType", dayType);
                extraBedDetail.put("description", "加床" + extraBedCount + "张");
                extraBedDetail.put("price", extraBedDayPrice.setScale(2, RoundingMode.HALF_UP));
                details.add(extraBedDetail);
            }

            currentDate = currentDate.plusDays(1);
        }

        int days = (int) java.time.temporal.ChronoUnit.DAYS.between(checkInDate, checkOutDate);
        BigDecimal extraBedTotal = BigDecimal.ZERO;
        if (extraBedCount != null && extraBedCount > 0 && extraBedPrice != null) {
            extraBedTotal = extraBedPrice.multiply(BigDecimal.valueOf(extraBedCount))
                    .multiply(BigDecimal.valueOf(days));
        }

        BigDecimal totalPrice = roomTotal.add(extraBedTotal).setScale(2, RoundingMode.HALF_UP);

        Map<String, Object> roomTypeInfo = new HashMap<>();
        roomTypeInfo.put("id", roomType.getId());
        roomTypeInfo.put("typeName", roomType.getTypeName());
        roomTypeInfo.put("basePrice", roomType.getBasePrice());
        roomTypeInfo.put("weekendPrice", roomType.getWeekendPrice());
        roomTypeInfo.put("costPrice", roomType.getCostPrice());
        roomTypeInfo.put("extraBedPrice", extraBedPrice);

        Map<String, Object> result = new HashMap<>();
        result.put("details", details);
        result.put("roomTypeInfo", roomTypeInfo);
        result.put("roomTotal", roomTotal.setScale(2, RoundingMode.HALF_UP));
        result.put("extraBedTotal", extraBedTotal.setScale(2, RoundingMode.HALF_UP));
        result.put("totalPrice", totalPrice);
        result.put("days", days);

        return result;
    }

    public String generateBookingNo() {
        String datetime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String random = String.format("%04d", new Random().nextInt(10000));
        return datetime + random;
    }

    @Transactional(rollbackFor = Exception.class)
    public Booking createBooking(Booking booking, List<BookingDetail> details, LoginUser loginUser) {
        if (booking == null) {
            throw new BusinessException("预订信息不能为空");
        }
        if (booking.getRoomId() == null) {
            throw new BusinessException("请选择房间");
        }
        if (booking.getCheckInDate() == null || booking.getCheckOutDate() == null) {
            throw new BusinessException("入住日期和退房日期不能为空");
        }
        if (booking.getCustomerId() == null) {
            throw new BusinessException("请选择客户");
        }

        if (isCustomerBlacklisted(booking.getCustomerId())) {
            throw new BusinessException("该客户在黑名单中，无法创建预订");
        }

        if (!checkRoomAvailability(booking.getRoomId(), booking.getCheckInDate(),
                booking.getCheckOutDate(), null)) {
            throw new BusinessException("所选房间在该日期范围内已被预订");
        }

        Room room = roomMapper.selectOneById(booking.getRoomId());
        if (room == null) {
            throw new BusinessException("房间不存在");
        }

        RoomType roomType = roomTypeMapper.selectOneById(room.getRoomTypeId());
        if (roomType == null) {
            throw new BusinessException("房型不存在");
        }

        Customer customer = customerMapper.selectOneById(booking.getCustomerId());
        if (customer == null) {
            throw new BusinessException("客户不存在");
        }

        int days = (int) java.time.temporal.ChronoUnit.DAYS.between(
                booking.getCheckInDate(), booking.getCheckOutDate());

        BigDecimal roomTotal = calculatePrice(roomType, booking.getCheckInDate(),
                booking.getCheckOutDate(), booking.getExtraBedCount(), booking.getExtraBedPrice());

        BigDecimal extraBedTotal = BigDecimal.ZERO;
        if (booking.getExtraBedCount() != null && booking.getExtraBedCount() > 0
                && booking.getExtraBedPrice() != null) {
            extraBedTotal = booking.getExtraBedPrice()
                    .multiply(BigDecimal.valueOf(booking.getExtraBedCount()))
                    .multiply(BigDecimal.valueOf(days));
        }

        BigDecimal totalAmount = roomTotal.add(extraBedTotal)
                .subtract(booking.getDiscount() != null ? booking.getDiscount() : BigDecimal.ZERO)
                .add(booking.getOtherFee() != null ? booking.getOtherFee() : BigDecimal.ZERO);

        String bookingNo = generateBookingNo();

        booking.setBookingNo(bookingNo);
        booking.setRoomTypeId(room.getRoomTypeId());
        booking.setRoomTypeName(roomType.getTypeName());
        booking.setRoomNumber(room.getRoomNumber());
        booking.setCustomerName(customer.getName());
        booking.setCustomerPhone(customer.getPhone());
        booking.setDays(days);
        booking.setRoomPrice(roomType.getBasePrice());
        booking.setRoomTotal(roomTotal);
        booking.setExtraBedTotal(extraBedTotal);
        booking.setTotalAmount(totalAmount.setScale(2, RoundingMode.HALF_UP));
        booking.setStatus(1);
        booking.setDeleted(0);
        booking.setCreateUserId(loginUser.getUserId());
        booking.setCreateUserName(loginUser.getUser().getNickname() != null ?
                loginUser.getUser().getNickname() : loginUser.getUsername());
        booking.setCreateTime(LocalDateTime.now());
        booking.setUpdateTime(LocalDateTime.now());

        bookingMapper.insert(booking);

        if (booking.getPointsUsed() != null && booking.getPointsUsed().compareTo(BigDecimal.ZERO) > 0) {
            if (booking.getMemberId() == null) {
                throw new BusinessException("使用积分需关联会员");
            }
            Map<String, Object> pointsResult = memberService.usePointsWithRule(
                    booking.getMemberId(), booking.getPointsUsed(), totalAmount,
                    loginUser.getUserId(), loginUser.getUser().getNickname() != null ?
                            loginUser.getUser().getNickname() : loginUser.getUsername(),
                    1, booking.getId());
            BigDecimal deductionAmount = (BigDecimal) pointsResult.get("deductionAmount");
            booking.setPointsDeductionAmount(deductionAmount);
            BigDecimal finalAmount = totalAmount.subtract(deductionAmount);
            booking.setTotalAmount(finalAmount.setScale(2, RoundingMode.HALF_UP));
            booking.setUpdateTime(LocalDateTime.now());
            bookingMapper.update(booking);
        }

        // 统一自动生成预订明细，不依赖前端传入
        generateBookingDetails(booking, roomType);

        room.setStatus(2);
        room.setUpdateTime(LocalDateTime.now());
        roomMapper.update(room);

        RoomStatusLog roomStatusLog = new RoomStatusLog();
        roomStatusLog.setRoomId(room.getId());
        roomStatusLog.setRoomNumber(room.getRoomNumber());
        roomStatusLog.setOldStatus(1);
        roomStatusLog.setNewStatus(2);
        roomStatusLog.setOperatorId(loginUser.getUserId());
        roomStatusLog.setOperator(loginUser.getUser().getNickname() != null ?
                loginUser.getUser().getNickname() : loginUser.getUsername());
        roomStatusLog.setRemark("创建预订：" + bookingNo);
        roomStatusLog.setCreateTime(LocalDateTime.now());
        roomStatusLogMapper.insert(roomStatusLog);

        recordStatusLog(booking.getId(), bookingNo, null, 1, "创建预订",
                loginUser.getUserId(), loginUser.getUser().getNickname() != null ?
                        loginUser.getUser().getNickname() : loginUser.getUsername());

        return booking;
    }

    private void generateBookingDetails(Booking booking, RoomType roomType) {
        LocalDate currentDate = booking.getCheckInDate();
        while (currentDate.isBefore(booking.getCheckOutDate())) {
            DayOfWeek dayOfWeek = currentDate.getDayOfWeek();
            BigDecimal price;
            String dayType;
            if (dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY) {
                price = roomType.getWeekendPrice() != null ? roomType.getWeekendPrice() : roomType.getBasePrice();
                dayType = "周末";
            } else {
                price = roomType.getBasePrice() != null ? roomType.getBasePrice() : BigDecimal.ZERO;
                dayType = "平日";
            }

            BookingDetail detail = new BookingDetail();
            detail.setBookingId(booking.getId());
            detail.setBookingNo(booking.getBookingNo());
            detail.setStayDate(currentDate);
            detail.setPrice(price);
            detail.setDayType(dayType);
            detail.setCreateTime(LocalDateTime.now());
            bookingDetailMapper.insert(detail);

            currentDate = currentDate.plusDays(1);
        }
    }

    private void recordStatusLog(Long bookingId, String bookingNo, Integer oldStatus, Integer newStatus,
                                 String remark, Long operatorId, String operatorName) {
        BookingStatusLog log = new BookingStatusLog();
        log.setBookingId(bookingId);
        log.setBookingNo(bookingNo);
        log.setOldStatus(oldStatus);
        log.setNewStatus(newStatus);
        log.setRemark(remark);
        log.setOperatorId(operatorId);
        log.setOperatorName(operatorName);
        log.setCreateTime(LocalDateTime.now());
        bookingStatusLogMapper.insert(log);
    }

    private void recordChangeLog(Long bookingId, String bookingNo, String fieldName,
                                 String oldValue, String newValue, String changeType,
                                 String remark, Long operatorId, String operatorName) {
        BookingChangeLog log = new BookingChangeLog();
        log.setBookingId(bookingId);
        log.setBookingNo(bookingNo);
        log.setFieldName(fieldName);
        log.setOldValue(oldValue);
        log.setNewValue(newValue);
        log.setChangeType(changeType);
        log.setRemark(remark);
        log.setOperatorId(operatorId);
        log.setOperatorName(operatorName);
        log.setCreateTime(LocalDateTime.now());
        bookingChangeLogMapper.insert(log);
    }

    public PageResult<Booking> getBookingPage(Integer pageNum, Integer pageSize, String bookingNo,
                                              Long customerId, String customerName, String customerPhone,
                                              Long roomTypeId, Long roomId, String roomNumber,
                                              List<Integer> status, LocalDate checkInDateStart,
                                              LocalDate checkInDateEnd, LocalDate checkOutDateStart,
                                              LocalDate checkOutDateEnd, String bookingSource,
                                              String sortField, String sortOrder) {
        QueryWrapper query = buildBookingQuery(bookingNo, customerId, customerName, customerPhone,
                roomTypeId, roomId, roomNumber, status, checkInDateStart, checkInDateEnd,
                checkOutDateStart, checkOutDateEnd, bookingSource);

        if (StringUtils.hasText(sortField)) {
            boolean isAsc = "asc".equalsIgnoreCase(sortOrder);
            switch (sortField) {
                case "createTime":
                    query.orderBy(BOOKING.CREATE_TIME, isAsc);
                    break;
                case "checkInDate":
                    query.orderBy(BOOKING.CHECK_IN_DATE, isAsc);
                    break;
                case "checkOutDate":
                    query.orderBy(BOOKING.CHECK_OUT_DATE, isAsc);
                    break;
                case "totalAmount":
                    query.orderBy(BOOKING.TOTAL_AMOUNT, isAsc);
                    break;
                default:
                    query.orderBy(BOOKING.CREATE_TIME.desc());
            }
        } else {
            query.orderBy(BOOKING.CREATE_TIME.desc());
        }

        Page<Booking> page = bookingMapper.paginate(Page.of(pageNum, pageSize), query);
        return new PageResult<>(page.getTotalRow(), page.getRecords(),
                (long) page.getPageNumber(), (long) page.getPageSize());
    }

    private QueryWrapper buildBookingQuery(String bookingNo, Long customerId, String customerName,
                                           String customerPhone, Long roomTypeId, Long roomId,
                                           String roomNumber, List<Integer> status,
                                           LocalDate checkInDateStart, LocalDate checkInDateEnd,
                                           LocalDate checkOutDateStart, LocalDate checkOutDateEnd,
                                           String bookingSource) {
        QueryWrapper query = QueryWrapper.create()
                .from(Booking.class)
                .where(BOOKING.DELETED.eq(0));

        if (StringUtils.hasText(bookingNo)) {
            query.and(BOOKING.BOOKING_NO.like(bookingNo));
        }
        if (customerId != null) {
            query.and(BOOKING.CUSTOMER_ID.eq(customerId));
        }
        if (StringUtils.hasText(customerName)) {
            query.and(BOOKING.CUSTOMER_NAME.like(customerName));
        }
        if (StringUtils.hasText(customerPhone)) {
            query.and(BOOKING.CUSTOMER_PHONE.like(customerPhone));
        }
        if (roomTypeId != null) {
            query.and(BOOKING.ROOM_TYPE_ID.eq(roomTypeId));
        }
        if (roomId != null) {
            query.and(BOOKING.ROOM_ID.eq(roomId));
        }
        if (StringUtils.hasText(roomNumber)) {
            query.and(BOOKING.ROOM_NUMBER.like(roomNumber));
        }
        if (status != null && !status.isEmpty()) {
            query.and(BOOKING.STATUS.in(status));
        }
        if (checkInDateStart != null) {
            query.and(BOOKING.CHECK_IN_DATE.ge(checkInDateStart));
        }
        if (checkInDateEnd != null) {
            query.and(BOOKING.CHECK_IN_DATE.le(checkInDateEnd));
        }
        if (checkOutDateStart != null) {
            query.and(BOOKING.CHECK_OUT_DATE.ge(checkOutDateStart));
        }
        if (checkOutDateEnd != null) {
            query.and(BOOKING.CHECK_OUT_DATE.le(checkOutDateEnd));
        }
        if (StringUtils.hasText(bookingSource)) {
            query.and(BOOKING.BOOKING_SOURCE.eq(bookingSource));
        }

        return query;
    }

    public Map<String, Object> getBookingDetail(Long id) {
        if (id == null) {
            throw new BusinessException("预订ID不能为空");
        }

        Booking booking = bookingMapper.selectOneById(id);
        if (booking == null) {
            throw new BusinessException("预订不存在");
        }

        QueryWrapper detailQuery = QueryWrapper.create()
                .from(BookingDetail.class)
                .where(BOOKING_DETAIL.BOOKING_ID.eq(id))
                .orderBy(BOOKING_DETAIL.STAY_DATE.asc());
        List<BookingDetail> details = bookingDetailMapper.selectListByQuery(detailQuery);

        QueryWrapper statusLogQuery = QueryWrapper.create()
                .from(BookingStatusLog.class)
                .where(BOOKING_STATUS_LOG.BOOKING_ID.eq(id))
                .orderBy(BOOKING_STATUS_LOG.CREATE_TIME.asc());
        List<BookingStatusLog> statusLogs = bookingStatusLogMapper.selectListByQuery(statusLogQuery);

        QueryWrapper changeLogQuery = QueryWrapper.create()
                .from(BookingChangeLog.class)
                .where(BOOKING_CHANGE_LOG.BOOKING_ID.eq(id))
                .orderBy(BOOKING_CHANGE_LOG.CREATE_TIME.asc());
        List<BookingChangeLog> changeLogs = bookingChangeLogMapper.selectListByQuery(changeLogQuery);

        Map<String, Object> result = new HashMap<>();
        result.put("booking", booking);
        result.put("details", details);
        result.put("statusLogs", statusLogs);
        result.put("changeLogs", changeLogs);

        return result;
    }

    @Transactional(rollbackFor = Exception.class)
    public void confirmBooking(Long id, LoginUser loginUser) {
        if (id == null) {
            throw new BusinessException("预订ID不能为空");
        }

        Booking booking = bookingMapper.selectOneById(id);
        if (booking == null) {
            throw new BusinessException("预订不存在");
        }
        if (booking.getStatus() != 1) {
            throw new BusinessException("只有待确认的预订才能确认");
        }

        Integer oldStatus = booking.getStatus();
        booking.setStatus(2);
        booking.setUpdateTime(LocalDateTime.now());
        bookingMapper.update(booking);

        recordStatusLog(id, booking.getBookingNo(), oldStatus, 2, "确认预订",
                loginUser.getUserId(), loginUser.getUser().getNickname() != null ?
                        loginUser.getUser().getNickname() : loginUser.getUsername());
    }

    @Transactional(rollbackFor = Exception.class)
    public void cancelBooking(Long id, String cancelReason, String cancelDetail,
                              Boolean chargePenalty, LoginUser loginUser) {
        if (id == null) {
            throw new BusinessException("预订ID不能为空");
        }

        Booking booking = bookingMapper.selectOneById(id);
        if (booking == null) {
            throw new BusinessException("预订不存在");
        }
        if (booking.getStatus() == 4 || booking.getStatus() == 9) {
            throw new BusinessException("该预订已取消或已退房，无法重复取消");
        }

        BigDecimal cancelPenalty = BigDecimal.ZERO;
        if (Boolean.TRUE.equals(chargePenalty) && booking.getTotalAmount() != null) {
            cancelPenalty = booking.getTotalAmount().multiply(new BigDecimal("0.3"))
                    .setScale(2, RoundingMode.HALF_UP);
        }

        Integer oldStatus = booking.getStatus();
        booking.setStatus(4);
        booking.setCancelReason(cancelReason);
        booking.setCancelDetail(cancelDetail);
        booking.setCancelPenalty(cancelPenalty);
        booking.setCancelTime(LocalDateTime.now());
        booking.setCancelOperatorId(loginUser.getUserId());
        booking.setCancelOperatorName(loginUser.getUser().getNickname() != null ?
                loginUser.getUser().getNickname() : loginUser.getUsername());
        booking.setUpdateTime(LocalDateTime.now());
        bookingMapper.update(booking);

        if (booking.getPointsUsed() != null && booking.getPointsUsed().compareTo(BigDecimal.ZERO) > 0
                && booking.getMemberId() != null) {
            memberService.refundPoints(booking.getMemberId(), booking.getPointsUsed(), 8,
                    "取消预订退还积分", "取消预订" + booking.getBookingNo() + "，退还" + booking.getPointsUsed() + "积分",
                    loginUser.getUserId(), loginUser.getUser().getNickname() != null ?
                            loginUser.getUser().getNickname() : loginUser.getUsername());
        }

        if (booking.getRoomId() != null) {
            Room room = roomMapper.selectOneById(booking.getRoomId());
            if (room != null) {
                Integer oldRoomStatus = room.getStatus();
                room.setStatus(1);
                room.setUpdateTime(LocalDateTime.now());
                roomMapper.update(room);

                RoomStatusLog roomStatusLog = new RoomStatusLog();
                roomStatusLog.setRoomId(room.getId());
                roomStatusLog.setRoomNumber(room.getRoomNumber());
                roomStatusLog.setOldStatus(oldRoomStatus);
                roomStatusLog.setNewStatus(1);
                roomStatusLog.setOperatorId(loginUser.getUserId());
                roomStatusLog.setOperator(loginUser.getUser().getNickname() != null ?
                        loginUser.getUser().getNickname() : loginUser.getUsername());
                roomStatusLog.setRemark("预订取消：" + booking.getBookingNo());
                roomStatusLog.setCreateTime(LocalDateTime.now());
                roomStatusLogMapper.insert(roomStatusLog);
            }
        }

        recordStatusLog(id, booking.getBookingNo(), oldStatus, 4,
                "取消预订，原因：" + (cancelReason != null ? cancelReason : ""),
                loginUser.getUserId(), loginUser.getUser().getNickname() != null ?
                        loginUser.getUser().getNickname() : loginUser.getUsername());
    }

    @Transactional(rollbackFor = Exception.class)
    public void checkInBooking(Long id, LocalDateTime checkInTime, LoginUser loginUser) {
        if (id == null) {
            throw new BusinessException("预订ID不能为空");
        }

        Booking booking = bookingMapper.selectOneById(id);
        if (booking == null) {
            throw new BusinessException("预订不存在");
        }
        if (booking.getStatus() != 2 && booking.getStatus() != 3 && booking.getStatus() != 5) {
            throw new BusinessException("只有已确认、已支付或续住的预订才能办理入住");
        }

        Integer oldStatus = booking.getStatus();
        booking.setStatus(5);
        booking.setCheckInTime(checkInTime != null ? checkInTime : LocalDateTime.now());
        booking.setUpdateTime(LocalDateTime.now());
        bookingMapper.update(booking);

        recordStatusLog(id, booking.getBookingNo(), oldStatus, 5, "办理入住",
                loginUser.getUserId(), loginUser.getUser().getNickname() != null ?
                        loginUser.getUser().getNickname() : loginUser.getUsername());
    }

    @Transactional(rollbackFor = Exception.class)
    public void updateBooking(Booking booking, Map<String, Object> changedFields, LoginUser loginUser) {
        if (booking == null || booking.getId() == null) {
            throw new BusinessException("预订ID不能为空");
        }

        Booking existing = bookingMapper.selectOneById(booking.getId());
        if (existing == null) {
            throw new BusinessException("预订不存在");
        }

        boolean dateChanged = false;
        LocalDate newCheckInDate = existing.getCheckInDate();
        LocalDate newCheckOutDate = existing.getCheckOutDate();

        if (changedFields != null && !changedFields.isEmpty()) {
            String operatorName = loginUser.getUser().getNickname() != null ?
                    loginUser.getUser().getNickname() : loginUser.getUsername();

            for (Map.Entry<String, Object> entry : changedFields.entrySet()) {
                String fieldName = entry.getKey();
                Object newValue = entry.getValue();
                String oldValueStr = getFieldValue(existing, fieldName);
                String newValueStr = newValue != null ? newValue.toString() : "";

                switch (fieldName) {
                    case "checkInDate":
                        newCheckInDate = (LocalDate) newValue;
                        dateChanged = true;
                        break;
                    case "checkOutDate":
                        newCheckOutDate = (LocalDate) newValue;
                        dateChanged = true;
                        break;
                    case "customerName":
                        existing.setCustomerName((String) newValue);
                        break;
                    case "customerPhone":
                        existing.setCustomerPhone((String) newValue);
                        break;
                    case "guestCount":
                        existing.setGuestCount((Integer) newValue);
                        break;
                    case "guestNames":
                        existing.setGuestNames((String) newValue);
                        break;
                    case "guestPhone":
                        existing.setGuestPhone((String) newValue);
                        break;
                    case "extraBedCount":
                        existing.setExtraBedCount((Integer) newValue);
                        break;
                    case "extraBedPrice":
                        existing.setExtraBedPrice((BigDecimal) newValue);
                        break;
                    case "discount":
                        existing.setDiscount((BigDecimal) newValue);
                        break;
                    case "otherFee":
                        existing.setOtherFee((BigDecimal) newValue);
                        break;
                    case "specialRequirements":
                        existing.setSpecialRequirements((String) newValue);
                        break;
                    case "bookingSource":
                        existing.setBookingSource((String) newValue);
                        break;
                    case "sourceRemark":
                        existing.setSourceRemark((String) newValue);
                        break;
                    case "guaranteeType":
                        existing.setGuaranteeType((String) newValue);
                        break;
                    case "expectedArrivalTime":
                        existing.setExpectedArrivalTime((LocalDateTime) newValue);
                        break;
                }

                recordChangeLog(existing.getId(), existing.getBookingNo(), fieldName,
                        oldValueStr, newValueStr, "修改",
                        "修改预订信息", loginUser.getUserId(), operatorName);
            }
        }

        if (dateChanged) {
            if (newCheckOutDate.isBefore(newCheckInDate) || newCheckOutDate.isEqual(newCheckInDate)) {
                throw new BusinessException("退房日期必须晚于入住日期");
            }
            if (!checkRoomAvailability(existing.getRoomId(), newCheckInDate,
                    newCheckOutDate, existing.getId())) {
                throw new BusinessException("所选房间在新的日期范围内已被预订");
            }

            RoomType roomType = roomTypeMapper.selectOneById(existing.getRoomTypeId());
            if (roomType != null) {
                int days = (int) java.time.temporal.ChronoUnit.DAYS.between(newCheckInDate, newCheckOutDate);
                BigDecimal roomTotal = calculatePrice(roomType, newCheckInDate, newCheckOutDate,
                        existing.getExtraBedCount(), existing.getExtraBedPrice());

                BigDecimal extraBedTotal = BigDecimal.ZERO;
                if (existing.getExtraBedCount() != null && existing.getExtraBedCount() > 0
                        && existing.getExtraBedPrice() != null) {
                    extraBedTotal = existing.getExtraBedPrice()
                            .multiply(BigDecimal.valueOf(existing.getExtraBedCount()))
                            .multiply(BigDecimal.valueOf(days));
                }

                BigDecimal totalAmount = roomTotal.add(extraBedTotal)
                        .subtract(existing.getDiscount() != null ? existing.getDiscount() : BigDecimal.ZERO)
                        .add(existing.getOtherFee() != null ? existing.getOtherFee() : BigDecimal.ZERO);

                existing.setCheckInDate(newCheckInDate);
                existing.setCheckOutDate(newCheckOutDate);
                existing.setDays(days);
                existing.setRoomTotal(roomTotal);
                existing.setExtraBedTotal(extraBedTotal);
                existing.setTotalAmount(totalAmount.setScale(2, RoundingMode.HALF_UP));

                QueryWrapper deleteDetailQuery = QueryWrapper.create()
                        .from(BookingDetail.class)
                        .where(BOOKING_DETAIL.BOOKING_ID.eq(existing.getId()));
                bookingDetailMapper.deleteByQuery(deleteDetailQuery);

                generateBookingDetails(existing, roomType);
            }
        }

        existing.setUpdateTime(LocalDateTime.now());
        bookingMapper.update(existing);
    }

    private String getFieldValue(Booking booking, String fieldName) {
        if (booking == null || fieldName == null) {
            return "";
        }
        switch (fieldName) {
            case "checkInDate":
                return booking.getCheckInDate() != null ? booking.getCheckInDate().toString() : "";
            case "checkOutDate":
                return booking.getCheckOutDate() != null ? booking.getCheckOutDate().toString() : "";
            case "customerName":
                return booking.getCustomerName() != null ? booking.getCustomerName() : "";
            case "customerPhone":
                return booking.getCustomerPhone() != null ? booking.getCustomerPhone() : "";
            case "guestCount":
                return booking.getGuestCount() != null ? booking.getGuestCount().toString() : "";
            case "guestNames":
                return booking.getGuestNames() != null ? booking.getGuestNames() : "";
            case "guestPhone":
                return booking.getGuestPhone() != null ? booking.getGuestPhone() : "";
            case "extraBedCount":
                return booking.getExtraBedCount() != null ? booking.getExtraBedCount().toString() : "";
            case "extraBedPrice":
                return booking.getExtraBedPrice() != null ? booking.getExtraBedPrice().toString() : "";
            case "discount":
                return booking.getDiscount() != null ? booking.getDiscount().toString() : "";
            case "otherFee":
                return booking.getOtherFee() != null ? booking.getOtherFee().toString() : "";
            case "specialRequirements":
                return booking.getSpecialRequirements() != null ? booking.getSpecialRequirements() : "";
            case "bookingSource":
                return booking.getBookingSource() != null ? booking.getBookingSource() : "";
            case "sourceRemark":
                return booking.getSourceRemark() != null ? booking.getSourceRemark() : "";
            case "guaranteeType":
                return booking.getGuaranteeType() != null ? booking.getGuaranteeType() : "";
            case "expectedArrivalTime":
                return booking.getExpectedArrivalTime() != null ? booking.getExpectedArrivalTime().toString() : "";
            default:
                return "";
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> changeRoom(Long bookingId, Long newRoomId, LoginUser loginUser) {
        if (bookingId == null || newRoomId == null) {
            throw new BusinessException("预订ID和新房间ID不能为空");
        }

        Booking booking = bookingMapper.selectOneById(bookingId);
        if (booking == null) {
            throw new BusinessException("预订不存在");
        }
        if (booking.getStatus() == 4 || booking.getStatus() == 9) {
            throw new BusinessException("该预订已取消或已退房，无法更换房间");
        }
        if (newRoomId.equals(booking.getRoomId())) {
            throw new BusinessException("新房间不能与原房间相同");
        }

        if (!checkRoomAvailability(newRoomId, booking.getCheckInDate(),
                booking.getCheckOutDate(), bookingId)) {
            throw new BusinessException("新房间在该日期范围内已被预订");
        }

        Room oldRoom = roomMapper.selectOneById(booking.getRoomId());
        Room newRoom = roomMapper.selectOneById(newRoomId);
        if (newRoom == null) {
            throw new BusinessException("新房间不存在");
        }

        RoomType oldRoomType = roomTypeMapper.selectOneById(booking.getRoomTypeId());
        RoomType newRoomType = roomTypeMapper.selectOneById(newRoom.getRoomTypeId());

        BigDecimal oldRoomTotal = booking.getRoomTotal();
        BigDecimal newRoomTotal = calculatePrice(newRoomType, booking.getCheckInDate(),
                booking.getCheckOutDate(), booking.getExtraBedCount(), booking.getExtraBedPrice());

        BigDecimal priceDiff = newRoomTotal.subtract(oldRoomTotal);

        if (oldRoom != null) {
            Integer oldRoomStatus = oldRoom.getStatus();
            oldRoom.setStatus(1);
            oldRoom.setUpdateTime(LocalDateTime.now());
            roomMapper.update(oldRoom);

            RoomStatusLog oldRoomLog = new RoomStatusLog();
            oldRoomLog.setRoomId(oldRoom.getId());
            oldRoomLog.setRoomNumber(oldRoom.getRoomNumber());
            oldRoomLog.setOldStatus(oldRoomStatus);
            oldRoomLog.setNewStatus(1);
            oldRoomLog.setOperatorId(loginUser.getUserId());
            oldRoomLog.setOperator(loginUser.getUser().getNickname() != null ?
                    loginUser.getUser().getNickname() : loginUser.getUsername());
            oldRoomLog.setRemark("更换房间，原房间释放：" + booking.getBookingNo());
            oldRoomLog.setCreateTime(LocalDateTime.now());
            roomStatusLogMapper.insert(oldRoomLog);
        }

        Integer newRoomOldStatus = newRoom.getStatus();
        newRoom.setStatus(2);
        newRoom.setUpdateTime(LocalDateTime.now());
        roomMapper.update(newRoom);

        RoomStatusLog newRoomLog = new RoomStatusLog();
        newRoomLog.setRoomId(newRoomId);
        newRoomLog.setRoomNumber(newRoom.getRoomNumber());
        newRoomLog.setOldStatus(newRoomOldStatus);
        newRoomLog.setNewStatus(2);
        newRoomLog.setOperatorId(loginUser.getUserId());
        newRoomLog.setOperator(loginUser.getUser().getNickname() != null ?
                loginUser.getUser().getNickname() : loginUser.getUsername());
        newRoomLog.setRemark("更换房间，新房间占用：" + booking.getBookingNo());
        newRoomLog.setCreateTime(LocalDateTime.now());
        roomStatusLogMapper.insert(newRoomLog);

        String operatorName = loginUser.getUser().getNickname() != null ?
                loginUser.getUser().getNickname() : loginUser.getUsername();

        recordChangeLog(bookingId, booking.getBookingNo(), "roomId",
                booking.getRoomId() != null ? booking.getRoomId().toString() : "",
                newRoomId.toString(), "换房",
                "更换房间：" + (oldRoom != null ? oldRoom.getRoomNumber() : "") +
                        " → " + newRoom.getRoomNumber(),
                loginUser.getUserId(), operatorName);

        recordChangeLog(bookingId, booking.getBookingNo(), "roomNumber",
                booking.getRoomNumber() != null ? booking.getRoomNumber() : "",
                newRoom.getRoomNumber(), "换房",
                "更换房间", loginUser.getUserId(), operatorName);

        recordChangeLog(bookingId, booking.getBookingNo(), "roomTypeId",
                booking.getRoomTypeId() != null ? booking.getRoomTypeId().toString() : "",
                newRoom.getRoomTypeId().toString(), "换房",
                "更换房间", loginUser.getUserId(), operatorName);

        recordChangeLog(bookingId, booking.getBookingNo(), "roomTypeName",
                booking.getRoomTypeName() != null ? booking.getRoomTypeName() : "",
                newRoomType != null ? newRoomType.getTypeName() : "",
                "换房", "更换房间", loginUser.getUserId(), operatorName);

        int days = booking.getDays() != null ? booking.getDays() :
                (int) java.time.temporal.ChronoUnit.DAYS.between(
                        booking.getCheckInDate(), booking.getCheckOutDate());

        BigDecimal extraBedTotal = booking.getExtraBedTotal() != null ?
                booking.getExtraBedTotal() : BigDecimal.ZERO;
        BigDecimal totalAmount = newRoomTotal.add(extraBedTotal)
                .subtract(booking.getDiscount() != null ? booking.getDiscount() : BigDecimal.ZERO)
                .add(booking.getOtherFee() != null ? booking.getOtherFee() : BigDecimal.ZERO);

        booking.setRoomId(newRoomId);
        booking.setRoomNumber(newRoom.getRoomNumber());
        booking.setRoomTypeId(newRoom.getRoomTypeId());
        booking.setRoomTypeName(newRoomType != null ? newRoomType.getTypeName() : "");
        booking.setRoomPrice(newRoomType != null ? newRoomType.getBasePrice() : BigDecimal.ZERO);
        booking.setRoomTotal(newRoomTotal);
        booking.setTotalAmount(totalAmount.setScale(2, RoundingMode.HALF_UP));
        booking.setUpdateTime(LocalDateTime.now());
        bookingMapper.update(booking);

        QueryWrapper deleteDetailQuery = QueryWrapper.create()
                .from(BookingDetail.class)
                .where(BOOKING_DETAIL.BOOKING_ID.eq(bookingId));
        bookingDetailMapper.deleteByQuery(deleteDetailQuery);

        if (newRoomType != null) {
            generateBookingDetails(booking, newRoomType);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("oldRoomTotal", oldRoomTotal);
        result.put("newRoomTotal", newRoomTotal);
        result.put("priceDiff", priceDiff);
        result.put("newTotalAmount", totalAmount.setScale(2, RoundingMode.HALF_UP));

        return result;
    }

    @Transactional(rollbackFor = Exception.class)
    public RefundRecord applyRefund(Long bookingId, String refundReason, LoginUser loginUser) {
        if (bookingId == null) {
            throw new BusinessException("预订ID不能为空");
        }

        Booking booking = bookingMapper.selectOneById(bookingId);
        if (booking == null) {
            throw new BusinessException("预订不存在");
        }

        Customer customer = customerMapper.selectOneById(booking.getCustomerId());

        BigDecimal totalAmount = booking.getTotalAmount() != null ? booking.getTotalAmount() : BigDecimal.ZERO;
        BigDecimal paidAmount = booking.getPaidAmount() != null ? booking.getPaidAmount() : BigDecimal.ZERO;
        BigDecimal cancelPenalty = booking.getCancelPenalty() != null ? booking.getCancelPenalty() : BigDecimal.ZERO;

        BigDecimal refundableAmount = paidAmount.subtract(cancelPenalty);
        if (refundableAmount.compareTo(BigDecimal.ZERO) < 0) {
            refundableAmount = BigDecimal.ZERO;
        }

        String refundNo = "RF" + generateBookingNo();

        RefundRecord refundRecord = new RefundRecord();
        refundRecord.setRefundNo(refundNo);
        refundRecord.setBookingId(bookingId);
        refundRecord.setBookingNo(booking.getBookingNo());
        refundRecord.setCustomerId(booking.getCustomerId());
        refundRecord.setCustomerName(customer != null ? customer.getName() : booking.getCustomerName());
        refundRecord.setTotalAmount(totalAmount);
        refundRecord.setPaidAmount(paidAmount);
        refundRecord.setRefundableAmount(refundableAmount);
        refundRecord.setDeductionAmount(cancelPenalty);
        refundRecord.setActualRefundAmount(refundableAmount);
        refundRecord.setRefundReason(refundReason);
        refundRecord.setStatus(1);
        refundRecord.setApplicantId(loginUser.getUserId());
        refundRecord.setApplicantName(loginUser.getUser().getNickname() != null ?
                loginUser.getUser().getNickname() : loginUser.getUsername());
        refundRecord.setCreateTime(LocalDateTime.now());
        refundRecord.setUpdateTime(LocalDateTime.now());

        refundRecordMapper.insert(refundRecord);

        return refundRecord;
    }

    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> batchConfirm(List<Long> ids, LoginUser loginUser) {
        if (ids == null || ids.isEmpty()) {
            throw new BusinessException("请选择要确认的预订");
        }

        int success = 0;
        List<Map<String, String>> failures = new ArrayList<>();
        String operatorName = loginUser.getUser().getNickname() != null ?
                loginUser.getUser().getNickname() : loginUser.getUsername();

        for (Long id : ids) {
            try {
                Booking booking = bookingMapper.selectOneById(id);
                if (booking == null) {
                    Map<String, String> fail = new HashMap<>();
                    fail.put("bookingId", String.valueOf(id));
                    fail.put("reason", "预订不存在");
                    failures.add(fail);
                    continue;
                }
                if (booking.getStatus() != 1) {
                    Map<String, String> fail = new HashMap<>();
                    fail.put("bookingId", String.valueOf(id));
                    fail.put("bookingNo", booking.getBookingNo());
                    fail.put("reason", "只有待确认的预订才能确认");
                    failures.add(fail);
                    continue;
                }

                Integer oldStatus = booking.getStatus();
                booking.setStatus(2);
                booking.setUpdateTime(LocalDateTime.now());
                bookingMapper.update(booking);

                recordStatusLog(id, booking.getBookingNo(), oldStatus, 2, "批量确认预订",
                        loginUser.getUserId(), operatorName);

                success++;
            } catch (Exception e) {
                Map<String, String> fail = new HashMap<>();
                fail.put("bookingId", String.valueOf(id));
                fail.put("reason", e.getMessage());
                failures.add(fail);
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("success", success);
        result.put("failures", failures);

        return result;
    }

    public List<Booking> exportBookings(String bookingNo, Long customerId, String customerName,
                                        String customerPhone, Long roomTypeId, Long roomId,
                                        String roomNumber, List<Integer> status,
                                        LocalDate checkInDateStart, LocalDate checkInDateEnd,
                                        LocalDate checkOutDateStart, LocalDate checkOutDateEnd,
                                        String bookingSource) {
        QueryWrapper query = buildBookingQuery(bookingNo, customerId, customerName, customerPhone,
                roomTypeId, roomId, roomNumber, status, checkInDateStart, checkInDateEnd,
                checkOutDateStart, checkOutDateEnd, bookingSource);
        query.orderBy(BOOKING.CREATE_TIME.desc());

        List<Booking> bookings = bookingMapper.selectListByQuery(query);

        for (Booking booking : bookings) {
            QueryWrapper detailQuery = QueryWrapper.create()
                    .from(BookingDetail.class)
                    .where(BOOKING_DETAIL.BOOKING_ID.eq(booking.getId()))
                    .orderBy(BOOKING_DETAIL.STAY_DATE.asc());
            List<BookingDetail> details = bookingDetailMapper.selectListByQuery(detailQuery);
        }

        return bookings;
    }

    public List<Booking> exportBookingsWithScope(String bookingNo, Long customerId, String customerName,
                                                 String customerPhone, Long roomTypeId, Long roomId,
                                                 String roomNumber, List<Integer> status,
                                                 LocalDate checkInDateStart, LocalDate checkInDateEnd,
                                                 LocalDate checkOutDateStart, LocalDate checkOutDateEnd,
                                                 String bookingSource, String scope, List<Long> ids,
                                                 Integer pageNum, Integer pageSize) {
        QueryWrapper query;
        if ("selected".equals(scope) && ids != null && !ids.isEmpty()) {
            query = QueryWrapper.create()
                    .from(Booking.class)
                    .where(BOOKING.ID.in(ids))
                    .orderBy(BOOKING.CREATE_TIME.desc());
        } else if ("current".equals(scope) && pageNum != null && pageSize != null) {
            query = buildBookingQuery(bookingNo, customerId, customerName, customerPhone,
                    roomTypeId, roomId, roomNumber, status, checkInDateStart, checkInDateEnd,
                    checkOutDateStart, checkOutDateEnd, bookingSource);
            query.orderBy(BOOKING.CREATE_TIME.desc());
            query.limit((pageNum - 1) * pageSize, pageSize);
        } else {
            query = buildBookingQuery(bookingNo, customerId, customerName, customerPhone,
                    roomTypeId, roomId, roomNumber, status, checkInDateStart, checkInDateEnd,
                    checkOutDateStart, checkOutDateEnd, bookingSource);
            query.orderBy(BOOKING.CREATE_TIME.desc());
        }

        List<Booking> bookings = bookingMapper.selectListByQuery(query);

        for (Booking booking : bookings) {
            QueryWrapper detailQuery = QueryWrapper.create()
                    .from(BookingDetail.class)
                    .where(BOOKING_DETAIL.BOOKING_ID.eq(booking.getId()))
                    .orderBy(BOOKING_DETAIL.STAY_DATE.asc());
            List<BookingDetail> details = bookingDetailMapper.selectListByQuery(detailQuery);
        }

        return bookings;
    }
}
