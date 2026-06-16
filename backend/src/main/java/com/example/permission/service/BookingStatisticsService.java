package com.example.permission.service;

import com.example.permission.common.BusinessException;
import com.example.permission.entity.Booking;
import com.example.permission.entity.Room;
import com.example.permission.entity.RoomType;
import com.example.permission.mapper.BookingMapper;
import com.example.permission.mapper.RefundRecordMapper;
import com.example.permission.mapper.RoomMapper;
import com.example.permission.mapper.RoomTypeMapper;
import com.mybatisflex.core.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.example.permission.entity.table.BookingTableDef.BOOKING;
import static com.example.permission.entity.table.RoomTableDef.ROOM;
import static com.example.permission.entity.table.RoomTypeTableDef.ROOM_TYPE;

@Service
public class BookingStatisticsService {

    @Autowired
    private BookingMapper bookingMapper;

    @Autowired
    private RefundRecordMapper refundRecordMapper;

    @Autowired
    private RoomMapper roomMapper;

    @Autowired
    private RoomTypeMapper roomTypeMapper;

    public Map<String, Object> getTodayStats() {
        LocalDate today = LocalDate.now();
        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime endOfDay = today.atTime(LocalTime.MAX);

        QueryWrapper newBookingQuery = QueryWrapper.create()
                .from(Booking.class)
                .where(BOOKING.CREATE_TIME.between(startOfDay, endOfDay))
                .and(BOOKING.DELETED.eq(0));
        long newBookingCount = bookingMapper.selectCountByQuery(newBookingQuery);

        QueryWrapper pendingCheckInQuery = QueryWrapper.create()
                .from(Booking.class)
                .where(BOOKING.CHECK_IN_DATE.eq(today))
                .and(BOOKING.STATUS.eq(1))
                .and(BOOKING.DELETED.eq(0));
        long pendingCheckInCount = bookingMapper.selectCountByQuery(pendingCheckInQuery);

        QueryWrapper checkedInQuery = QueryWrapper.create()
                .from(Booking.class)
                .where(BOOKING.STATUS.eq(3))
                .and(BOOKING.DELETED.eq(0));
        long checkedInCount = bookingMapper.selectCountByQuery(checkedInQuery);

        QueryWrapper cancelQuery = QueryWrapper.create()
                .from(Booking.class)
                .where(BOOKING.CANCEL_TIME.between(startOfDay, endOfDay))
                .and(BOOKING.DELETED.eq(0));
        long cancelCount = bookingMapper.selectCountByQuery(cancelQuery);

        Map<String, Object> result = new HashMap<>();
        result.put("newBookingCount", newBookingCount);
        result.put("pendingCheckInCount", pendingCheckInCount);
        result.put("checkedInCount", checkedInCount);
        result.put("cancelCount", cancelCount);
        return result;
    }

    public List<Map<String, Object>> getBookingStatusStats() {
        List<Map<String, Object>> result = new ArrayList<>();
        int[] statuses = {0, 1, 2, 3, 4, 5, 6, 7};
        String[] statusNames = {"待确认", "待入住", "已确认", "已入住", "已退房", "已结账", "已取消", "已退款"};

        for (int i = 0; i < statuses.length; i++) {
            QueryWrapper query = QueryWrapper.create()
                    .from(Booking.class)
                    .where(BOOKING.STATUS.eq(statuses[i]))
                    .and(BOOKING.DELETED.eq(0));
            long count = bookingMapper.selectCountByQuery(query);
            Map<String, Object> item = new HashMap<>();
            item.put("status", statuses[i]);
            item.put("statusName", statusNames[i]);
            item.put("count", count);
            result.add(item);
        }
        return result;
    }

    public List<Map<String, Object>> getBookingSourceStats() {
        List<Map<String, Object>> result = new ArrayList<>();
        QueryWrapper query = QueryWrapper.create()
                .select(BOOKING.BOOKING_SOURCE)
                .from(Booking.class)
                .where(BOOKING.DELETED.eq(0))
                .groupBy(BOOKING.BOOKING_SOURCE);
        List<Booking> bookings = bookingMapper.selectListByQuery(query);

        for (Booking booking : bookings) {
            Map<String, Object> item = new HashMap<>();
            item.put("source", booking.getBookingSource());
            item.put("sourceName", getSourceName(booking.getBookingSource()));
            QueryWrapper countQuery = QueryWrapper.create()
                    .from(Booking.class)
                    .where(BOOKING.BOOKING_SOURCE.eq(booking.getBookingSource()))
                    .and(BOOKING.DELETED.eq(0));
            item.put("count", bookingMapper.selectCountByQuery(countQuery));
            result.add(item);
        }
        return result;
    }

    public List<Map<String, Object>> getBookingTrend(Integer days) {
        if (days == null || days <= 0) {
            days = 7;
        }
        List<Map<String, Object>> result = new ArrayList<>();
        LocalDate today = LocalDate.now();

        for (int i = days - 1; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            LocalDateTime startOfDay = date.atStartOfDay();
            LocalDateTime endOfDay = date.atTime(LocalTime.MAX);

            QueryWrapper bookingQuery = QueryWrapper.create()
                    .from(Booking.class)
                    .where(BOOKING.CREATE_TIME.between(startOfDay, endOfDay))
                    .and(BOOKING.DELETED.eq(0));
            long bookingCount = bookingMapper.selectCountByQuery(bookingQuery);

            QueryWrapper revenueQuery = QueryWrapper.create()
                    .from(Booking.class)
                    .where(BOOKING.CREATE_TIME.between(startOfDay, endOfDay))
                    .and(BOOKING.DELETED.eq(0));
            List<Booking> dayBookings = bookingMapper.selectListByQuery(revenueQuery);
            BigDecimal totalRevenue = BigDecimal.ZERO;
            for (Booking b : dayBookings) {
                if (b.getTotalAmount() != null) {
                    totalRevenue = totalRevenue.add(b.getTotalAmount());
                }
            }

            Map<String, Object> item = new HashMap<>();
            item.put("date", date.toString());
            item.put("bookingCount", bookingCount);
            item.put("revenue", totalRevenue);
            result.add(item);
        }
        return result;
    }

    public List<Map<String, Object>> getRoomTypeBookingStats() {
        List<Map<String, Object>> result = new ArrayList<>();
        QueryWrapper roomTypeQuery = QueryWrapper.create()
                .from(RoomType.class)
                .where(ROOM_TYPE.DELETED.eq(0))
                .orderBy(ROOM_TYPE.ID.asc());
        List<RoomType> roomTypes = roomTypeMapper.selectListByQuery(roomTypeQuery);

        QueryWrapper totalRoomQuery = QueryWrapper.create()
                .from(Room.class)
                .where(ROOM.DELETED.eq(0));
        long totalRooms = roomMapper.selectCountByQuery(totalRoomQuery);

        LocalDate today = LocalDate.now();
        for (RoomType roomType : roomTypes) {
            QueryWrapper bookingQuery = QueryWrapper.create()
                    .from(Booking.class)
                    .where(BOOKING.ROOM_TYPE_ID.eq(roomType.getId()))
                    .and(BOOKING.CHECK_IN_DATE.le(today))
                    .and(BOOKING.CHECK_OUT_DATE.ge(today))
                    .and(BOOKING.STATUS.in(1, 2, 3))
                    .and(BOOKING.DELETED.eq(0));
            long bookingCount = bookingMapper.selectCountByQuery(bookingQuery);

            QueryWrapper roomCountQuery = QueryWrapper.create()
                    .from(Room.class)
                    .where(ROOM.ROOM_TYPE_ID.eq(roomType.getId()))
                    .and(ROOM.DELETED.eq(0));
            long typeRoomCount = roomMapper.selectCountByQuery(roomCountQuery);

            BigDecimal bookingRate = BigDecimal.ZERO;
            if (typeRoomCount > 0) {
                bookingRate = BigDecimal.valueOf(bookingCount)
                        .divide(BigDecimal.valueOf(typeRoomCount), 4, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100));
            }

            Map<String, Object> item = new HashMap<>();
            item.put("roomTypeId", roomType.getId());
            item.put("roomTypeName", roomType.getTypeName());
            item.put("bookingCount", bookingCount);
            item.put("roomCount", typeRoomCount);
            item.put("bookingRate", bookingRate);
            result.add(item);
        }
        return result;
    }

    public List<Map<String, Object>> getCalendarData(Integer year, Integer month) {
        if (year == null || month == null) {
            throw new BusinessException("年份和月份不能为空");
        }
        List<Map<String, Object>> result = new ArrayList<>();
        LocalDate firstDay = LocalDate.of(year, month, 1);
        int daysInMonth = firstDay.lengthOfMonth();

        for (int day = 1; day <= daysInMonth; day++) {
            LocalDate date = LocalDate.of(year, month, day);
            Map<String, Object> dayData = new HashMap<>();
            dayData.put("date", date.toString());
            dayData.put("day", day);
            dayData.put("weekday", date.getDayOfWeek().getValue());

            QueryWrapper bookingQuery = QueryWrapper.create()
                    .from(Booking.class)
                    .where(BOOKING.CHECK_IN_DATE.le(date))
                    .and(BOOKING.CHECK_OUT_DATE.ge(date))
                    .and(BOOKING.STATUS.in(1, 2, 3))
                    .and(BOOKING.DELETED.eq(0));
            long bookingCount = bookingMapper.selectCountByQuery(bookingQuery);

            QueryWrapper checkInQuery = QueryWrapper.create()
                    .from(Booking.class)
                    .where(BOOKING.CHECK_IN_DATE.eq(date))
                    .and(BOOKING.DELETED.eq(0));
            long checkInCount = bookingMapper.selectCountByQuery(checkInQuery);

            QueryWrapper checkOutQuery = QueryWrapper.create()
                    .from(Booking.class)
                    .where(BOOKING.CHECK_OUT_DATE.eq(date))
                    .and(BOOKING.DELETED.eq(0));
            long checkOutCount = bookingMapper.selectCountByQuery(checkOutQuery);

            dayData.put("bookingCount", bookingCount);
            dayData.put("checkInCount", checkInCount);
            dayData.put("checkOutCount", checkOutCount);
            dayData.put("occupancyRate", getOccupancyRate(date));
            result.add(dayData);
        }
        return result;
    }

    public List<Booking> getDayBookings(LocalDate date) {
        if (date == null) {
            throw new BusinessException("日期不能为空");
        }
        QueryWrapper query = QueryWrapper.create()
                .from(Booking.class)
                .where(BOOKING.CHECK_IN_DATE.le(date))
                .and(BOOKING.CHECK_OUT_DATE.ge(date))
                .and(BOOKING.DELETED.eq(0))
                .orderBy(BOOKING.CREATE_TIME.desc());
        return bookingMapper.selectListByQuery(query);
    }

    public BigDecimal getOccupancyRate(LocalDate date) {
        if (date == null) {
            throw new BusinessException("日期不能为空");
        }
        QueryWrapper totalRoomQuery = QueryWrapper.create()
                .from(Room.class)
                .where(ROOM.DELETED.eq(0));
        long totalRooms = roomMapper.selectCountByQuery(totalRoomQuery);

        if (totalRooms == 0) {
            return BigDecimal.ZERO;
        }

        QueryWrapper occupiedQuery = QueryWrapper.create()
                .from(Booking.class)
                .where(BOOKING.CHECK_IN_DATE.le(date))
                .and(BOOKING.CHECK_OUT_DATE.ge(date))
                .and(BOOKING.STATUS.in(1, 2, 3))
                .and(BOOKING.DELETED.eq(0));
        long occupiedRooms = bookingMapper.selectCountByQuery(occupiedQuery);

        return BigDecimal.valueOf(occupiedRooms)
                .divide(BigDecimal.valueOf(totalRooms), 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));
    }

    private String getSourceName(String source) {
        if (source == null) {
            return "未知";
        }
        switch (source) {
            case "online":
                return "线上平台";
            case "offline":
                return "线下门店";
            case "phone":
                return "电话预订";
            case "wechat":
                return "微信预订";
            case "member":
                return "会员预订";
            default:
                return source;
        }
    }
}
