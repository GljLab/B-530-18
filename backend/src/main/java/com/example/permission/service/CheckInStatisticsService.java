package com.example.permission.service;

import com.example.permission.entity.*;
import com.example.permission.mapper.*;
import com.mybatisflex.core.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

import static com.example.permission.entity.table.CheckInTableDef.CHECK_IN;
import static com.example.permission.entity.table.CheckOutRecordTableDef.CHECK_OUT_RECORD;
import static com.example.permission.entity.table.RoomTableDef.ROOM;
import static com.example.permission.entity.table.RoomTypeTableDef.ROOM_TYPE;
import static com.example.permission.entity.table.FloorTableDef.FLOOR;
import static com.example.permission.entity.table.CustomerTableDef.CUSTOMER;

@Service
public class CheckInStatisticsService {

    @Autowired
    private CheckInMapper checkInMapper;

    @Autowired
    private CheckOutRecordMapper checkOutRecordMapper;

    @Autowired
    private RoomMapper roomMapper;

    @Autowired
    private RoomTypeMapper roomTypeMapper;

    @Autowired
    private FloorMapper floorMapper;

    @Autowired
    private CustomerMapper customerMapper;

    public Map<String, Object> getOverviewStats() {
        Map<String, Object> result = new HashMap<>();
        LocalDate today = LocalDate.now();

        Long totalRooms = roomMapper.selectCountByQuery(
                QueryWrapper.create().from(Room.class)
                        .where(ROOM.DELETED.eq(0))
                        .and(ROOM.STATUS.ne(7))
        );

        Long inHouseRooms = checkInMapper.selectCountByQuery(
                QueryWrapper.create().from(CheckIn.class)
                        .where(CHECK_IN.STATUS.eq(1))
                        .and(CHECK_IN.DELETED.eq(0))
        );

        BigDecimal occupancyRate = totalRooms > 0
                ? BigDecimal.valueOf(inHouseRooms).multiply(BigDecimal.valueOf(100))
                        .divide(BigDecimal.valueOf(totalRooms), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        Long todayCheckIn = checkInMapper.selectCountByQuery(
                QueryWrapper.create().from(CheckIn.class)
                        .where(CHECK_IN.CHECK_IN_DATE.eq(today))
                        .and(CHECK_IN.DELETED.eq(0))
        );

        Long todayCheckOut = checkOutRecordMapper.selectCountByQuery(
                QueryWrapper.create().from(CheckOutRecord.class)
                        .where(CHECK_OUT_RECORD.CHECK_OUT_DATE.eq(today))
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

        result.put("totalRooms", totalRooms);
        result.put("inHouseRooms", inHouseRooms);
        result.put("occupancyRate", occupancyRate);
        result.put("todayCheckIn", todayCheckIn);
        result.put("todayCheckOut", todayCheckOut);
        result.put("expectedCheckOut", expectedCheckOut);
        result.put("overdueCount", overdueCount);

        return result;
    }

    public List<Map<String, Object>> getCheckInTrend(Integer days) {
        if (days == null || days <= 0) {
            days = 7;
        }
        List<Map<String, Object>> result = new ArrayList<>();
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(days - 1);

        for (int i = 0; i < days; i++) {
            LocalDate date = startDate.plusDays(i);
            Map<String, Object> dayData = new HashMap<>();

            Long checkInCount = checkInMapper.selectCountByQuery(
                    QueryWrapper.create().from(CheckIn.class)
                            .where(CHECK_IN.CHECK_IN_DATE.eq(date))
                            .and(CHECK_IN.DELETED.eq(0))
            );

            Long checkOutCount = checkOutRecordMapper.selectCountByQuery(
                    QueryWrapper.create().from(CheckOutRecord.class)
                            .where(CHECK_OUT_RECORD.CHECK_OUT_DATE.eq(date))
            );

            dayData.put("date", date.toString());
            dayData.put("checkInCount", checkInCount);
            dayData.put("checkOutCount", checkOutCount);
            result.add(dayData);
        }

        return result;
    }

    public List<Map<String, Object>> getRoomTypeStats() {
        List<RoomType> roomTypes = roomTypeMapper.selectAll();
        List<Map<String, Object>> result = new ArrayList<>();

        for (RoomType roomType : roomTypes) {
            Map<String, Object> data = new HashMap<>();

            Long totalRooms = roomMapper.selectCountByQuery(
                    QueryWrapper.create().from(Room.class)
                            .where(ROOM.ROOM_TYPE_ID.eq(roomType.getId()))
                            .and(ROOM.DELETED.eq(0))
                            .and(ROOM.STATUS.ne(7))
            );

            Long inHouse = checkInMapper.selectCountByQuery(
                    QueryWrapper.create().from(CheckIn.class)
                            .where(CHECK_IN.ROOM_TYPE_ID.eq(roomType.getId()))
                            .and(CHECK_IN.STATUS.eq(1))
                            .and(CHECK_IN.DELETED.eq(0))
            );

            BigDecimal occupancyRate = totalRooms > 0
                    ? BigDecimal.valueOf(inHouse).multiply(BigDecimal.valueOf(100))
                            .divide(BigDecimal.valueOf(totalRooms), 2, RoundingMode.HALF_UP)
                    : BigDecimal.ZERO;

            data.put("roomTypeId", roomType.getId());
            data.put("roomTypeName", roomType.getTypeName());
            data.put("totalRooms", totalRooms);
            data.put("inHouse", inHouse);
            data.put("occupancyRate", occupancyRate);
            result.add(data);
        }

        return result;
    }

    public List<Map<String, Object>> getFloorStats() {
        List<Floor> floors = floorMapper.selectAll();
        List<Map<String, Object>> result = new ArrayList<>();

        for (Floor floor : floors) {
            Map<String, Object> data = new HashMap<>();

            Long totalRooms = roomMapper.selectCountByQuery(
                    QueryWrapper.create().from(Room.class)
                            .where(ROOM.FLOOR_ID.eq(floor.getId()))
                            .and(ROOM.DELETED.eq(0))
                            .and(ROOM.STATUS.ne(7))
            );

            List<Room> floorRooms = roomMapper.selectListByQuery(
                    QueryWrapper.create().from(Room.class)
                            .where(ROOM.FLOOR_ID.eq(floor.getId()))
                            .and(ROOM.DELETED.eq(0))
            );

            Long inHouse = 0L;
            if (!floorRooms.isEmpty()) {
                List<Long> roomIds = floorRooms.stream().map(Room::getId).collect(Collectors.toList());
                inHouse = checkInMapper.selectCountByQuery(
                        QueryWrapper.create().from(CheckIn.class)
                                .where(CHECK_IN.ROOM_ID.in(roomIds))
                                .and(CHECK_IN.STATUS.eq(1))
                                .and(CHECK_IN.DELETED.eq(0))
                );
            }

            BigDecimal occupancyRate = totalRooms > 0
                    ? BigDecimal.valueOf(inHouse).multiply(BigDecimal.valueOf(100))
                            .divide(BigDecimal.valueOf(totalRooms), 2, RoundingMode.HALF_UP)
                    : BigDecimal.ZERO;

            data.put("floorId", floor.getId());
            data.put("floorName", floor.getFloorName());
            data.put("floorNumber", floor.getFloorNumber());
            data.put("totalRooms", totalRooms);
            data.put("inHouse", inHouse);
            data.put("occupancyRate", occupancyRate);
            result.add(data);
        }

        return result;
    }

    public Map<String, Object> getSourceStats() {
        Map<String, Object> result = new HashMap<>();
        List<Map<String, Object>> sourceData = new ArrayList<>();

        int[] sources = {1, 2, 3, 4, 5, 6};
        String[] sourceNames = {"前台", "官网", "第三方平台", "协议单位", "旅行社", "其他"};

        long total = 0;
        for (int i = 0; i < sources.length; i++) {
            Long count = checkInMapper.selectCountByQuery(
                    QueryWrapper.create().from(CheckIn.class)
                            .where(CHECK_IN.BOOKING_SOURCE.eq(sources[i]))
                            .and(CHECK_IN.DELETED.eq(0))
            );
            Map<String, Object> data = new HashMap<>();
            data.put("source", sources[i]);
            data.put("sourceName", sourceNames[i]);
            data.put("count", count);
            sourceData.add(data);
            total += count;
        }

        result.put("total", total);
        result.put("sourceData", sourceData);

        return result;
    }

    public Map<String, Object> getCustomerTypeStats() {
        Map<String, Object> result = new HashMap<>();
        List<Map<String, Object>> typeData = new ArrayList<>();

        int[] types = {1, 2, 3, 4};
        String[] typeNames = {"散客", "会员", "协议客户", "团队"};

        long total = 0;
        for (int i = 0; i < types.length; i++) {
            Long count = checkInMapper.selectCountByQuery(
                    QueryWrapper.create().from(CheckIn.class)
                            .where(CHECK_IN.CUSTOMER_TYPE.eq(types[i]))
                            .and(CHECK_IN.DELETED.eq(0))
            );
            Map<String, Object> data = new HashMap<>();
            data.put("type", types[i]);
            data.put("typeName", typeNames[i]);
            data.put("count", count);
            typeData.add(data);
            total += count;
        }

        result.put("total", total);
        result.put("typeData", typeData);

        return result;
    }

    public Map<String, Object> getAverageStayDays() {
        Map<String, Object> result = new HashMap<>();

        List<CheckOutRecord> records = checkOutRecordMapper.selectAll();
        if (records.isEmpty()) {
            result.put("overallAvg", BigDecimal.ZERO);
            result.put("roomTypeData", new ArrayList<>());
            return result;
        }

        int totalDays = 0;
        for (CheckOutRecord record : records) {
            if (record.getStayedDays() != null) {
                totalDays += record.getStayedDays();
            }
        }
        BigDecimal overallAvg = BigDecimal.valueOf(totalDays)
                .divide(BigDecimal.valueOf(records.size()), 2, RoundingMode.HALF_UP);
        result.put("overallAvg", overallAvg);

        List<RoomType> roomTypes = roomTypeMapper.selectAll();
        List<Map<String, Object>> roomTypeData = new ArrayList<>();

        for (RoomType roomType : roomTypes) {
            List<CheckIn> checkIns = checkInMapper.selectListByQuery(
                    QueryWrapper.create().from(CheckIn.class)
                            .where(CHECK_IN.ROOM_TYPE_ID.eq(roomType.getId()))
                            .and(CHECK_IN.STATUS.eq(2))
                            .and(CHECK_IN.DELETED.eq(0))
            );

            BigDecimal avgDays = BigDecimal.ZERO;
            if (!checkIns.isEmpty()) {
                int days = 0;
                for (CheckIn checkIn : checkIns) {
                    if (checkIn.getStayedDays() != null) {
                        days += checkIn.getStayedDays();
                    }
                }
                avgDays = BigDecimal.valueOf(days)
                        .divide(BigDecimal.valueOf(checkIns.size()), 2, RoundingMode.HALF_UP);
            }

            Map<String, Object> data = new HashMap<>();
            data.put("roomTypeId", roomType.getId());
            data.put("roomTypeName", roomType.getTypeName());
            data.put("avgDays", avgDays);
            data.put("checkInCount", checkIns.size());
            roomTypeData.add(data);
        }

        result.put("roomTypeData", roomTypeData);

        int[] distribution = {0, 0, 0, 0, 0};
        String[] distributionLabels = {"1天", "2-3天", "4-7天", "8-14天", "15天以上"};

        for (CheckOutRecord record : records) {
            int days = record.getStayedDays() != null ? record.getStayedDays() : 0;
            if (days == 1) {
                distribution[0]++;
            } else if (days <= 3) {
                distribution[1]++;
            } else if (days <= 7) {
                distribution[2]++;
            } else if (days <= 14) {
                distribution[3]++;
            } else {
                distribution[4]++;
            }
        }

        List<Map<String, Object>> distributionData = new ArrayList<>();
        for (int i = 0; i < distribution.length; i++) {
            Map<String, Object> data = new HashMap<>();
            data.put("label", distributionLabels[i]);
            data.put("count", distribution[i]);
            distributionData.add(data);
        }
        result.put("distributionData", distributionData);

        return result;
    }

    public Map<String, Object> getRevenueStats(Integer days) {
        if (days == null || days <= 0) {
            days = 7;
        }
        Map<String, Object> result = new HashMap<>();
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(days - 1);

        List<Map<String, Object>> dailyRevenue = new ArrayList<>();
        BigDecimal totalRevenue = BigDecimal.ZERO;

        for (int i = 0; i < days; i++) {
            LocalDate date = startDate.plusDays(i);
            Map<String, Object> dayData = new HashMap<>();

            List<CheckOutRecord> dayRecords = checkOutRecordMapper.selectListByQuery(
                    QueryWrapper.create().from(CheckOutRecord.class)
                            .where(CHECK_OUT_RECORD.CHECK_OUT_DATE.eq(date))
            );

            BigDecimal dayRevenue = BigDecimal.ZERO;
            for (CheckOutRecord record : dayRecords) {
                if (record.getTotalAmount() != null) {
                    dayRevenue = dayRevenue.add(record.getTotalAmount());
                }
            }

            dayData.put("date", date.toString());
            dayData.put("revenue", dayRevenue);
            dailyRevenue.add(dayData);
            totalRevenue = totalRevenue.add(dayRevenue);
        }

        result.put("dailyRevenue", dailyRevenue);
        result.put("totalRevenue", totalRevenue);

        return result;
    }

    public Map<String, Object> getRegionStats() {
        Map<String, Object> result = new HashMap<>();

        List<Customer> customers = customerMapper.selectListByQuery(
                QueryWrapper.create().from(Customer.class)
                        .where(CUSTOMER.STATUS.eq(1))
                        .and(CUSTOMER.DELETED.eq(0))
                        .limit(100)
        );

        Map<String, Integer> regionMap = new HashMap<>();
        for (Customer customer : customers) {
            String region = "未知";
            if (customer.getNationality() != null && !customer.getNationality().isEmpty()) {
                region = customer.getNationality();
            }
            regionMap.put(region, regionMap.getOrDefault(region, 0) + 1);
        }

        List<Map<String, Object>> regionData = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : regionMap.entrySet()) {
            Map<String, Object> data = new HashMap<>();
            data.put("region", entry.getKey());
            data.put("count", entry.getValue());
            regionData.add(data);
        }

        regionData.sort((a, b) -> (Integer) b.get("count") - (Integer) a.get("count"));

        result.put("regionData", regionData);

        return result;
    }

    public List<Map<String, Object>> getOccupancyRateTrend(Integer days) {
        if (days == null || days <= 0) {
            days = 7;
        }
        List<Map<String, Object>> result = new ArrayList<>();
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(days - 1);

        Long totalRooms = roomMapper.selectCountByQuery(
                QueryWrapper.create().from(Room.class)
                        .where(ROOM.DELETED.eq(0))
                        .and(ROOM.STATUS.ne(7))
        );

        for (int i = 0; i < days; i++) {
            LocalDate date = startDate.plusDays(i);
            Map<String, Object> dayData = new HashMap<>();

            Long inHouseCount = checkInMapper.selectCountByQuery(
                    QueryWrapper.create().from(CheckIn.class)
                            .where(CHECK_IN.CHECK_IN_DATE.le(date))
                            .and(CHECK_IN.CHECK_OUT_DATE.gt(date))
                            .and(CHECK_IN.DELETED.eq(0))
            );

            BigDecimal occupancyRate = totalRooms > 0
                    ? BigDecimal.valueOf(inHouseCount).multiply(BigDecimal.valueOf(100))
                            .divide(BigDecimal.valueOf(totalRooms), 2, RoundingMode.HALF_UP)
                    : BigDecimal.ZERO;

            dayData.put("date", date.toString());
            dayData.put("occupancyRate", occupancyRate);
            result.add(dayData);
        }

        return result;
    }
}
