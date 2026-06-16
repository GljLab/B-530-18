package com.example.permission.service;

import com.example.permission.common.BusinessException;
import com.example.permission.entity.Booking;
import com.example.permission.entity.Channel;
import com.example.permission.entity.Floor;
import com.example.permission.entity.Room;
import com.example.permission.entity.RoomType;
import com.example.permission.mapper.BookingMapper;
import com.example.permission.mapper.ChannelMapper;
import com.example.permission.mapper.FloorMapper;
import com.example.permission.mapper.RoomMapper;
import com.example.permission.mapper.RoomTypeMapper;
import com.mybatisflex.core.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.example.permission.entity.table.BookingTableDef.BOOKING;
import static com.example.permission.entity.table.ChannelTableDef.CHANNEL;
import static com.example.permission.entity.table.FloorTableDef.FLOOR;
import static com.example.permission.entity.table.RoomTableDef.ROOM;
import static com.example.permission.entity.table.RoomTypeTableDef.ROOM_TYPE;

@Service
public class AdvancedStatisticsService {

    @Autowired
    private BookingMapper bookingMapper;

    @Autowired
    private RoomMapper roomMapper;

    @Autowired
    private RoomTypeMapper roomTypeMapper;

    @Autowired
    private FloorMapper floorMapper;

    @Autowired
    private ChannelMapper channelMapper;

    public List<Map> getOccupancyRate(LocalDate startDate, LocalDate endDate, Long roomTypeId, Long floorId, String period) {
        if (startDate == null || endDate == null) {
            throw new BusinessException("日期范围不能为空");
        }

        QueryWrapper roomQuery = QueryWrapper.create()
                .from(Room.class)
                .where(ROOM.DELETED.eq(0));
        if (roomTypeId != null) {
            roomQuery.and(ROOM.ROOM_TYPE_ID.eq(roomTypeId));
        }
        if (floorId != null) {
            roomQuery.and(ROOM.FLOOR_ID.eq(floorId));
        }
        long totalRooms = roomMapper.selectCountByQuery(roomQuery);

        List<Map> result = new ArrayList<>();
        LocalDate current = startDate;

        while (!current.isAfter(endDate)) {
            QueryWrapper bookingQuery = QueryWrapper.create()
                    .from(Booking.class)
                    .where(BOOKING.CHECK_IN_DATE.le(current))
                    .and(BOOKING.CHECK_OUT_DATE.gt(current))
                    .and(BOOKING.STATUS.in(1, 2, 3, 4))
                    .and(BOOKING.DELETED.eq(0));
            if (roomTypeId != null) {
                bookingQuery.and(BOOKING.ROOM_TYPE_ID.eq(roomTypeId));
            }
            if (floorId != null) {
                List<Long> floorRoomIds = getRoomIdsByFloor(floorId);
                if (!floorRoomIds.isEmpty()) {
                    bookingQuery.and(BOOKING.ROOM_ID.in(floorRoomIds));
                }
            }
            long occupiedRooms = bookingMapper.selectCountByQuery(bookingQuery);

            BigDecimal occupancyRate = totalRooms > 0
                    ? BigDecimal.valueOf(occupiedRooms).divide(BigDecimal.valueOf(totalRooms), 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100))
                    : BigDecimal.ZERO;

            Map<String, Object> item = new HashMap<>();
            item.put("date", current);
            item.put("totalRooms", totalRooms);
            item.put("occupiedRooms", occupiedRooms);
            item.put("occupancyRate", occupancyRate);
            result.add(item);

            if ("week".equals(period)) {
                current = current.plusWeeks(1);
            } else if ("month".equals(period)) {
                current = current.plusMonths(1);
            } else {
                current = current.plusDays(1);
            }
        }
        return result;
    }

    private List<Long> getRoomIdsByFloor(Long floorId) {
        QueryWrapper query = QueryWrapper.create()
                .from(Room.class)
                .where(ROOM.FLOOR_ID.eq(floorId))
                .and(ROOM.DELETED.eq(0));
        List<Room> rooms = roomMapper.selectListByQuery(query);
        return rooms.stream().map(Room::getId).collect(Collectors.toList());
    }

    public Map getBookingCycleAnalysis(LocalDate startDate, LocalDate endDate, Long roomTypeId, Long channelId, String customerType) {
        QueryWrapper query = QueryWrapper.create()
                .from(Booking.class)
                .where(BOOKING.CREATE_TIME.ge(startDate.atStartOfDay()))
                .and(BOOKING.CREATE_TIME.le(endDate.atTime(23, 59, 59)))
                .and(BOOKING.DELETED.eq(0));
        if (roomTypeId != null) {
            query.and(BOOKING.ROOM_TYPE_ID.eq(roomTypeId));
        }
        if (channelId != null) {
            Channel channel = channelMapper.selectOneById(channelId);
            if (channel != null) {
                query.and(BOOKING.BOOKING_SOURCE.eq(channel.getChannelCode()));
            }
        }
        List<Booking> bookings = bookingMapper.selectListByQuery(query);

        List<Long> advanceDaysList = new ArrayList<>();
        int instantBookingCount = 0;
        int cancelCount = 0;
        long totalBookings = bookings.size();

        for (Booking booking : bookings) {
            if (booking.getCreateTime() != null && booking.getCheckInDate() != null) {
                long advanceDays = ChronoUnit.DAYS.between(booking.getCreateTime().toLocalDate(), booking.getCheckInDate());
                advanceDaysList.add(advanceDays);
                if (advanceDays == 0) {
                    instantBookingCount++;
                }
            }
            if (booking.getStatus() != null && booking.getStatus() == 4) {
                cancelCount++;
            }
        }

        Map<String, Integer> advanceDaysDistribution = new HashMap<>();
        for (Long days : advanceDaysList) {
            String key;
            if (days == 0) {
                key = "当日";
            } else if (days <= 3) {
                key = "1-3天";
            } else if (days <= 7) {
                key = "4-7天";
            } else if (days <= 14) {
                key = "8-14天";
            } else if (days <= 30) {
                key = "15-30天";
            } else {
                key = "30天以上";
            }
            advanceDaysDistribution.merge(key, 1, Integer::sum);
        }

        double avgAdvanceDays = advanceDaysList.stream()
                .mapToLong(Long::longValue)
                .average()
                .orElse(0.0);

        BigDecimal instantBookingRate = totalBookings > 0
                ? BigDecimal.valueOf(instantBookingCount).divide(BigDecimal.valueOf(totalBookings), 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100))
                : BigDecimal.ZERO;

        BigDecimal cancelRate = totalBookings > 0
                ? BigDecimal.valueOf(cancelCount).divide(BigDecimal.valueOf(totalBookings), 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100))
                : BigDecimal.ZERO;

        Map<String, Object> result = new HashMap<>();
        result.put("advanceDaysDistribution", advanceDaysDistribution);
        result.put("avgAdvanceDays", BigDecimal.valueOf(avgAdvanceDays).setScale(1, RoundingMode.HALF_UP));
        result.put("instantBookingRate", instantBookingRate);
        result.put("cancelRate", cancelRate);
        result.put("totalBookings", totalBookings);
        return result;
    }

    public Map getCustomerBehaviorAnalysis(LocalDate startDate, LocalDate endDate) {
        QueryWrapper query = QueryWrapper.create()
                .from(Booking.class)
                .where(BOOKING.CREATE_TIME.ge(startDate.atStartOfDay()))
                .and(BOOKING.CREATE_TIME.le(endDate.atTime(23, 59, 59)))
                .and(BOOKING.DELETED.eq(0));
        List<Booking> bookings = bookingMapper.selectListByQuery(query);

        List<Integer> stayDurations = new ArrayList<>();
        Map<Long, Integer> roomTypeBookingCount = new HashMap<>();
        Map<Long, Integer> customerBookingCount = new HashMap<>();

        for (Booking booking : bookings) {
            if (booking.getDays() != null) {
                stayDurations.add(booking.getDays());
            }
            if (booking.getRoomTypeId() != null) {
                roomTypeBookingCount.merge(booking.getRoomTypeId(), 1, Integer::sum);
            }
            if (booking.getCustomerId() != null) {
                customerBookingCount.merge(booking.getCustomerId(), 1, Integer::sum);
            }
        }

        double avgStayDays = stayDurations.stream()
                .mapToInt(Integer::intValue)
                .average()
                .orElse(0.0);

        Map<String, Integer> stayDurationDistribution = new HashMap<>();
        for (Integer days : stayDurations) {
            String key;
            if (days == 1) {
                key = "1晚";
            } else if (days <= 3) {
                key = "2-3晚";
            } else if (days <= 7) {
                key = "4-7晚";
            } else {
                key = "7晚以上";
            }
            stayDurationDistribution.merge(key, 1, Integer::sum);
        }

        List<Map<String, Object>> roomTypePreference = new ArrayList<>();
        for (Map.Entry<Long, Integer> entry : roomTypeBookingCount.entrySet()) {
            RoomType roomType = roomTypeMapper.selectOneById(entry.getKey());
            Map<String, Object> item = new HashMap<>();
            item.put("roomTypeId", entry.getKey());
            item.put("typeName", roomType != null ? roomType.getTypeName() : "");
            item.put("bookingCount", entry.getValue());
            roomTypePreference.add(item);
        }
        roomTypePreference.sort((a, b) -> (Integer) b.get("bookingCount") - (Integer) a.get("bookingCount"));

        long repeatGuestCount = customerBookingCount.values().stream()
                .filter(count -> count > 1)
                .count();
        BigDecimal repeatGuestRate = !customerBookingCount.isEmpty()
                ? BigDecimal.valueOf(repeatGuestCount).divide(BigDecimal.valueOf(customerBookingCount.size()), 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100))
                : BigDecimal.ZERO;

        Map<String, Object> result = new HashMap<>();
        result.put("avgStayDays", BigDecimal.valueOf(avgStayDays).setScale(1, RoundingMode.HALF_UP));
        result.put("stayDurationDistribution", stayDurationDistribution);
        result.put("roomTypePreference", roomTypePreference);
        result.put("repeatGuestCount", repeatGuestCount);
        result.put("totalCustomers", customerBookingCount.size());
        result.put("repeatGuestRate", repeatGuestRate);
        return result;
    }

    public Map getRevenueAnalysis(LocalDate startDate, LocalDate endDate, Long roomTypeId, Long channelId) {
        QueryWrapper query = QueryWrapper.create()
                .from(Booking.class)
                .where(BOOKING.CREATE_TIME.ge(startDate.atStartOfDay()))
                .and(BOOKING.CREATE_TIME.le(endDate.atTime(23, 59, 59)))
                .and(BOOKING.DELETED.eq(0));
        if (roomTypeId != null) {
            query.and(BOOKING.ROOM_TYPE_ID.eq(roomTypeId));
        }
        if (channelId != null) {
            Channel channel = channelMapper.selectOneById(channelId);
            if (channel != null) {
                query.and(BOOKING.BOOKING_SOURCE.eq(channel.getChannelCode()));
            }
        }
        List<Booking> bookings = bookingMapper.selectListByQuery(query);

        BigDecimal totalRevenue = bookings.stream()
                .map(b -> b.getTotalAmount() != null ? b.getTotalAmount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        long totalDays = ChronoUnit.DAYS.between(startDate, endDate) + 1;
        BigDecimal dailyAvg = totalDays > 0
                ? totalRevenue.divide(BigDecimal.valueOf(totalDays), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        QueryWrapper totalRoomQuery = QueryWrapper.create()
                .from(Room.class)
                .where(ROOM.DELETED.eq(0));
        long totalRooms = roomMapper.selectCountByQuery(totalRoomQuery);
        BigDecimal revPAR = totalRooms > 0 && totalDays > 0
                ? totalRevenue.divide(BigDecimal.valueOf(totalRooms * totalDays), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        long bookedRoomNights = bookings.stream()
                .filter(b -> b.getDays() != null)
                .mapToLong(Booking::getDays)
                .sum();
        BigDecimal adr = bookedRoomNights > 0
                ? totalRevenue.divide(BigDecimal.valueOf(bookedRoomNights), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        Map<Long, BigDecimal> roomTypeRevenue = new HashMap<>();
        Map<Long, BigDecimal> channelRevenue = new HashMap<>();
        List<Map<String, Object>> trend = new ArrayList<>();

        for (Booking booking : bookings) {
            if (booking.getRoomTypeId() != null && booking.getTotalAmount() != null) {
                roomTypeRevenue.merge(booking.getRoomTypeId(), booking.getTotalAmount(), BigDecimal::add);
            }
            if (booking.getBookingSource() != null && booking.getTotalAmount() != null) {
                channelRevenue.merge(-1L, booking.getTotalAmount(), BigDecimal::add);
            }
        }

        List<Map<String, Object>> revenueByRoomType = new ArrayList<>();
        for (Map.Entry<Long, BigDecimal> entry : roomTypeRevenue.entrySet()) {
            RoomType roomType = roomTypeMapper.selectOneById(entry.getKey());
            Map<String, Object> item = new HashMap<>();
            item.put("roomTypeId", entry.getKey());
            item.put("typeName", roomType != null ? roomType.getTypeName() : "");
            item.put("revenue", entry.getValue());
            revenueByRoomType.add(item);
        }

        List<Map<String, Object>> revenueByChannel = new ArrayList<>();
        Map<String, BigDecimal> sourceRevenue = new HashMap<>();
        for (Booking booking : bookings) {
            String source = booking.getBookingSource() != null ? booking.getBookingSource() : "unknown";
            BigDecimal amount = booking.getTotalAmount() != null ? booking.getTotalAmount() : BigDecimal.ZERO;
            sourceRevenue.merge(source, amount, BigDecimal::add);
        }
        for (Map.Entry<String, BigDecimal> entry : sourceRevenue.entrySet()) {
            Map<String, Object> item = new HashMap<>();
            item.put("source", entry.getKey());
            item.put("revenue", entry.getValue());
            revenueByChannel.add(item);
        }

        LocalDate current = startDate;
        while (!current.isAfter(endDate)) {
            BigDecimal dayRevenue = BigDecimal.ZERO;
            for (Booking booking : bookings) {
                if (booking.getCreateTime() != null && booking.getCreateTime().toLocalDate().equals(current) && booking.getTotalAmount() != null) {
                    dayRevenue = dayRevenue.add(booking.getTotalAmount());
                }
            }
            Map<String, Object> item = new HashMap<>();
            item.put("date", current);
            item.put("revenue", dayRevenue);
            trend.add(item);
            current = current.plusDays(1);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("totalRevenue", totalRevenue);
        result.put("dailyAvg", dailyAvg);
        result.put("revPAR", revPAR);
        result.put("adr", adr);
        result.put("byRoomType", revenueByRoomType);
        result.put("byChannel", revenueByChannel);
        result.put("trend", trend);
        return result;
    }

    public byte[] exportAnalytics(String type, LocalDate startDate, LocalDate endDate, Map params) {
        throw new BusinessException("导出功能需要配置Excel依赖后实现");
    }
}
