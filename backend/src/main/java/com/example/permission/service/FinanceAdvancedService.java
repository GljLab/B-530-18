package com.example.permission.service;

import com.example.permission.entity.AgreementUnit;
import com.example.permission.entity.CheckIn;
import com.example.permission.entity.CheckOutRecord;
import com.example.permission.entity.CreditBill;
import com.example.permission.entity.RefundApply;
import com.example.permission.entity.RoomType;
import com.example.permission.entity.SysUser;
import com.example.permission.mapper.AgreementUnitMapper;
import com.example.permission.mapper.CheckInMapper;
import com.example.permission.mapper.CheckOutRecordMapper;
import com.example.permission.mapper.CreditBillMapper;
import com.example.permission.mapper.RefundApplyMapper;
import com.example.permission.mapper.RoomTypeMapper;
import com.example.permission.mapper.SysUserMapper;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.example.permission.entity.table.CheckOutRecordTableDef.CHECK_OUT_RECORD;
import static com.example.permission.entity.table.CheckInTableDef.CHECK_IN;
import static com.example.permission.entity.table.CreditBillTableDef.CREDIT_BILL;
import static com.example.permission.entity.table.RefundApplyTableDef.REFUND_APPLY;
import static com.example.permission.entity.table.AgreementUnitTableDef.AGREEMENT_UNIT;
import static com.example.permission.entity.table.RoomTypeTableDef.ROOM_TYPE;
import static com.example.permission.entity.table.SysUserTableDef.SYS_USER;

@Service
public class FinanceAdvancedService {

    @Autowired
    private CheckOutRecordMapper checkOutRecordMapper;

    @Autowired
    private CheckInMapper checkInMapper;

    @Autowired
    private CreditBillMapper creditBillMapper;

    @Autowired
    private RefundApplyMapper refundApplyMapper;

    @Autowired
    private AgreementUnitMapper agreementUnitMapper;

    @Autowired
    private RoomTypeMapper roomTypeMapper;

    @Autowired
    private SysUserMapper sysUserMapper;

    public Map<String, Object> getCollectionDetail(String startDate, String endDate, Integer paymentType, Integer paymentMethod, int pageNum, int pageSize) {
        QueryWrapper query = QueryWrapper.create()
                .from(CheckOutRecord.class)
                .where(CHECK_OUT_RECORD.ID.isNotNull());
        if (StringUtils.hasText(startDate)) {
            LocalDateTime startDateTime = LocalDateTime.parse(startDate + "T00:00:00");
            query.and(CHECK_OUT_RECORD.CREATE_TIME.ge(startDateTime));
        }
        if (StringUtils.hasText(endDate)) {
            LocalDateTime endDateTime = LocalDateTime.parse(endDate + "T23:59:59");
            query.and(CHECK_OUT_RECORD.CREATE_TIME.le(endDateTime));
        }
        if (paymentMethod != null) {
            query.and(CHECK_OUT_RECORD.PAYMENT_METHOD.eq(paymentMethod));
        }
        query.orderBy(CHECK_OUT_RECORD.CREATE_TIME.desc());
        Page<CheckOutRecord> page = checkOutRecordMapper.paginate(Page.of(pageNum, pageSize), query);
        List<CheckOutRecord> records = page.getRecords();

        List<Map<String, Object>> detailList = new ArrayList<>();
        for (CheckOutRecord record : records) {
            Map<String, Object> item = new HashMap<>();
            item.put("time", record.getCreateTime());
            item.put("orderNo", record.getCheckOutNo());
            item.put("customer", record.getCustomerName());
            String type = "房费";
            if (paymentType != null) {
                if (paymentType == 1) {
                    type = "房费";
                } else if (paymentType == 2) {
                    type = "押金";
                } else if (paymentType == 3) {
                    type = "加床费";
                }
            }
            item.put("type", type);
            item.put("paymentMethod", record.getPaymentMethod());
            item.put("amount", record.getTotalAmount());
            item.put("cashier", record.getOperatorName());
            detailList.add(item);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("list", detailList);
        result.put("total", page.getTotalRow());
        return result;
    }

    public Map<String, Object> getCollectionTrend(int days) {
        LocalDateTime endTime = LocalDateTime.now();
        LocalDateTime startTime = endTime.minusDays(days);

        QueryWrapper query = QueryWrapper.create()
                .from(CheckOutRecord.class)
                .where(CHECK_OUT_RECORD.CREATE_TIME.ge(startTime))
                .and(CHECK_OUT_RECORD.CREATE_TIME.le(endTime));
        List<CheckOutRecord> records = checkOutRecordMapper.selectListByQuery(query);

        Map<String, BigDecimal> dailyTotal = new HashMap<>();
        Map<Integer, BigDecimal> byMethod = new HashMap<>();
        for (CheckOutRecord record : records) {
            String day = record.getCreateTime() != null
                    ? record.getCreateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                    : null;
            if (day != null) {
                BigDecimal amount = record.getTotalAmount() != null ? record.getTotalAmount() : BigDecimal.ZERO;
                dailyTotal.merge(day, amount, BigDecimal::add);
            }
            if (record.getPaymentMethod() != null) {
                BigDecimal amount = record.getTotalAmount() != null ? record.getTotalAmount() : BigDecimal.ZERO;
                byMethod.merge(record.getPaymentMethod(), amount, BigDecimal::add);
            }
        }

        List<Map<String, Object>> trend = new ArrayList<>();
        for (int i = days - 1; i >= 0; i--) {
            LocalDate date = LocalDate.now().minusDays(i);
            String dayStr = date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            Map<String, Object> dayItem = new HashMap<>();
            dayItem.put("date", dayStr);
            dayItem.put("amount", dailyTotal.getOrDefault(dayStr, BigDecimal.ZERO));
            trend.add(dayItem);
        }

        Map<String, Object> weekComparison = new HashMap<>();
        LocalDateTime thisWeekStart = endTime.minusDays(7);
        LocalDateTime lastWeekStart = endTime.minusDays(14);
        BigDecimal thisWeekTotal = BigDecimal.ZERO;
        BigDecimal lastWeekTotal = BigDecimal.ZERO;
        for (CheckOutRecord record : records) {
            if (record.getCreateTime() != null && !record.getCreateTime().isBefore(thisWeekStart)) {
                thisWeekTotal = thisWeekTotal.add(record.getTotalAmount() != null ? record.getTotalAmount() : BigDecimal.ZERO);
            }
            if (record.getCreateTime() != null && !record.getCreateTime().isBefore(lastWeekStart) && record.getCreateTime().isBefore(thisWeekStart)) {
                lastWeekTotal = lastWeekTotal.add(record.getTotalAmount() != null ? record.getTotalAmount() : BigDecimal.ZERO);
            }
        }
        weekComparison.put("thisWeek", thisWeekTotal);
        weekComparison.put("lastWeek", lastWeekTotal);

        List<Map<String, Object>> methodBreakdown = new ArrayList<>();
        for (Map.Entry<Integer, BigDecimal> entry : byMethod.entrySet()) {
            Map<String, Object> m = new HashMap<>();
            m.put("method", entry.getKey());
            m.put("amount", entry.getValue());
            methodBreakdown.add(m);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("trend", trend);
        result.put("byMethod", methodBreakdown);
        result.put("weekComparison", weekComparison);
        result.put("yoy", new HashMap<>());
        result.put("mom", new HashMap<>());
        return result;
    }

    public Map<String, Object> getPaymentMethodAnalysis(String startDate, String endDate) {
        QueryWrapper query = QueryWrapper.create()
                .from(CheckOutRecord.class)
                .where(CHECK_OUT_RECORD.ID.isNotNull());
        if (StringUtils.hasText(startDate)) {
            LocalDateTime startDateTime = LocalDateTime.parse(startDate + "T00:00:00");
            query.and(CHECK_OUT_RECORD.CREATE_TIME.ge(startDateTime));
        }
        if (StringUtils.hasText(endDate)) {
            LocalDateTime endDateTime = LocalDateTime.parse(endDate + "T23:59:59");
            query.and(CHECK_OUT_RECORD.CREATE_TIME.le(endDateTime));
        }
        List<CheckOutRecord> records = checkOutRecordMapper.selectListByQuery(query);

        Map<Integer, Long> frequency = new HashMap<>();
        Map<Integer, BigDecimal> amountMap = new HashMap<>();
        for (CheckOutRecord record : records) {
            Integer method = record.getPaymentMethod();
            if (method != null) {
                frequency.merge(method, 1L, Long::sum);
                BigDecimal amount = record.getTotalAmount() != null ? record.getTotalAmount() : BigDecimal.ZERO;
                amountMap.merge(method, amount, BigDecimal::add);
            }
        }

        List<Map<String, Object>> pieData = new ArrayList<>();
        for (Map.Entry<Integer, Long> entry : frequency.entrySet()) {
            Map<String, Object> item = new HashMap<>();
            item.put("method", entry.getKey());
            item.put("frequency", entry.getValue());
            item.put("amount", amountMap.getOrDefault(entry.getKey(), BigDecimal.ZERO));
            pieData.add(item);
        }

        Map<String, Object> cashTrend = new HashMap<>();
        List<CheckOutRecord> cashRecords = new ArrayList<>();
        for (CheckOutRecord r : records) {
            if (r.getPaymentMethod() != null && r.getPaymentMethod() == 1) {
                cashRecords.add(r);
            }
        }
        Map<String, BigDecimal> dailyCash = new HashMap<>();
        for (CheckOutRecord r : cashRecords) {
            if (r.getCreateTime() != null) {
                String day = r.getCreateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                BigDecimal amount = r.getTotalAmount() != null ? r.getTotalAmount() : BigDecimal.ZERO;
                dailyCash.merge(day, amount, BigDecimal::add);
            }
        }
        List<Map<String, Object>> cashTrendList = new ArrayList<>();
        for (Map.Entry<String, BigDecimal> entry : dailyCash.entrySet()) {
            Map<String, Object> d = new HashMap<>();
            d.put("date", entry.getKey());
            d.put("amount", entry.getValue());
            cashTrendList.add(d);
        }
        cashTrend.put("trend", cashTrendList);

        Map<String, Object> result = new HashMap<>();
        result.put("pieData", pieData);
        result.put("cashTrend", cashTrend);
        result.put("customerTypeAnalysis", new ArrayList<>());
        result.put("roomTypeAnalysis", new ArrayList<>());
        return result;
    }

    public Map<String, Object> getCashierStatistics(String startDate, String endDate) {
        QueryWrapper query = QueryWrapper.create()
                .from(CheckOutRecord.class)
                .where(CHECK_OUT_RECORD.ID.isNotNull());
        if (StringUtils.hasText(startDate)) {
            LocalDateTime startDateTime = LocalDateTime.parse(startDate + "T00:00:00");
            query.and(CHECK_OUT_RECORD.CREATE_TIME.ge(startDateTime));
        }
        if (StringUtils.hasText(endDate)) {
            LocalDateTime endDateTime = LocalDateTime.parse(endDate + "T23:59:59");
            query.and(CHECK_OUT_RECORD.CREATE_TIME.le(endDateTime));
        }
        List<CheckOutRecord> records = checkOutRecordMapper.selectListByQuery(query);

        Map<Long, Map<String, Object>> cashierMap = new HashMap<>();
        for (CheckOutRecord record : records) {
            Long cashierId = record.getOperatorId();
            if (cashierId == null) {
                continue;
            }
            Map<String, Object> stats = cashierMap.computeIfAbsent(cashierId, k -> {
                Map<String, Object> m = new HashMap<>();
                m.put("cashierId", cashierId);
                m.put("cashierName", record.getOperatorName());
                m.put("count", 0L);
                m.put("total", BigDecimal.ZERO);
                m.put("methodBreakdown", new HashMap<Integer, BigDecimal>());
                return m;
            });
            stats.put("count", (Long) stats.get("count") + 1);
            BigDecimal amount = record.getTotalAmount() != null ? record.getTotalAmount() : BigDecimal.ZERO;
            stats.put("total", ((BigDecimal) stats.get("total")).add(amount));
            if (record.getPaymentMethod() != null) {
                Map<Integer, BigDecimal> methodMap = (Map<Integer, BigDecimal>) stats.get("methodBreakdown");
                methodMap.merge(record.getPaymentMethod(), amount, BigDecimal::add);
            }
        }

        List<Map<String, Object>> cashierList = new ArrayList<>(cashierMap.values());
        for (Map<String, Object> stats : cashierList) {
            long count = (Long) stats.get("count");
            BigDecimal total = (BigDecimal) stats.get("total");
            BigDecimal avg = count > 0 ? total.divide(new BigDecimal(count), 2, RoundingMode.HALF_UP) : BigDecimal.ZERO;
            stats.put("avg", avg);
            Map<Integer, BigDecimal> methodMap = (Map<Integer, BigDecimal>) stats.get("methodBreakdown");
            List<Map<String, Object>> methodList = new ArrayList<>();
            for (Map.Entry<Integer, BigDecimal> entry : methodMap.entrySet()) {
                Map<String, Object> m = new HashMap<>();
                m.put("method", entry.getKey());
                m.put("amount", entry.getValue());
                methodList.add(m);
            }
            stats.put("methodBreakdown", methodList);
        }

        cashierList.sort((a, b) -> ((BigDecimal) b.get("total")).compareTo((BigDecimal) a.get("total")));
        List<Map<String, Object>> rankedByAmount = new ArrayList<>(cashierList);
        cashierList.sort((a, b) -> Long.compare((Long) b.get("count"), (Long) a.get("count")));
        List<Map<String, Object>> rankedByCount = new ArrayList<>(cashierList);

        Map<String, Object> result = new HashMap<>();
        result.put("cashiers", rankedByAmount);
        result.put("rankedByAmount", rankedByAmount);
        result.put("rankedByCount", rankedByCount);
        return result;
    }

    public Map<String, Object> getCashierDetail(Long cashierId, String startDate, String endDate) {
        QueryWrapper query = QueryWrapper.create()
                .from(CheckOutRecord.class)
                .where(CHECK_OUT_RECORD.OPERATOR_ID.eq(cashierId));
        if (StringUtils.hasText(startDate)) {
            LocalDateTime startDateTime = LocalDateTime.parse(startDate + "T00:00:00");
            query.and(CHECK_OUT_RECORD.CREATE_TIME.ge(startDateTime));
        }
        if (StringUtils.hasText(endDate)) {
            LocalDateTime endDateTime = LocalDateTime.parse(endDate + "T23:59:59");
            query.and(CHECK_OUT_RECORD.CREATE_TIME.le(endDateTime));
        }
        query.orderBy(CHECK_OUT_RECORD.CREATE_TIME.desc());
        List<CheckOutRecord> records = checkOutRecordMapper.selectListByQuery(query);

        SysUser cashier = sysUserMapper.selectOneById(cashierId);
        String cashierName = cashier != null ? (cashier.getNickname() != null ? cashier.getNickname() : cashier.getUsername()) : "";

        List<Map<String, Object>> detailList = new ArrayList<>();
        for (CheckOutRecord record : records) {
            Map<String, Object> item = new HashMap<>();
            item.put("id", record.getId());
            item.put("orderNo", record.getCheckOutNo());
            item.put("customerName", record.getCustomerName());
            item.put("amount", record.getTotalAmount());
            item.put("paymentMethod", record.getPaymentMethod());
            item.put("time", record.getCreateTime());
            detailList.add(item);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("cashierId", cashierId);
        result.put("cashierName", cashierName);
        result.put("records", detailList);
        result.put("totalCount", records.size());
        return result;
    }

    public Map<String, Object> getRevenueAnalysis(String startDate, String endDate) {
        QueryWrapper query = QueryWrapper.create()
                .from(CheckOutRecord.class)
                .where(CHECK_OUT_RECORD.ID.isNotNull());
        if (StringUtils.hasText(startDate)) {
            LocalDateTime startDateTime = LocalDateTime.parse(startDate + "T00:00:00");
            query.and(CHECK_OUT_RECORD.CREATE_TIME.ge(startDateTime));
        }
        if (StringUtils.hasText(endDate)) {
            LocalDateTime endDateTime = LocalDateTime.parse(endDate + "T23:59:59");
            query.and(CHECK_OUT_RECORD.CREATE_TIME.le(endDateTime));
        }
        List<CheckOutRecord> records = checkOutRecordMapper.selectListByQuery(query);

        BigDecimal roomFee = BigDecimal.ZERO;
        BigDecimal extraBedFee = BigDecimal.ZERO;
        BigDecimal otherFee = BigDecimal.ZERO;
        for (CheckOutRecord record : records) {
            roomFee = roomFee.add(record.getRoomTotal() != null ? record.getRoomTotal() : BigDecimal.ZERO);
            extraBedFee = extraBedFee.add(record.getExtraBedTotal() != null ? record.getExtraBedTotal() : BigDecimal.ZERO);
            otherFee = otherFee.add(record.getOtherFee() != null ? record.getOtherFee() : BigDecimal.ZERO);
        }

        Map<String, Object> composition = new HashMap<>();
        composition.put("roomFee", roomFee);
        composition.put("extraBedFee", extraBedFee);
        composition.put("otherFee", otherFee);
        composition.put("total", roomFee.add(extraBedFee).add(otherFee));

        Map<String, BigDecimal> dailyRevenue = new HashMap<>();
        for (CheckOutRecord record : records) {
            if (record.getCreateTime() != null) {
                String day = record.getCreateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                BigDecimal amount = record.getTotalAmount() != null ? record.getTotalAmount() : BigDecimal.ZERO;
                dailyRevenue.merge(day, amount, BigDecimal::add);
            }
        }
        List<Map<String, Object>> trend = new ArrayList<>();
        for (Map.Entry<String, BigDecimal> entry : dailyRevenue.entrySet()) {
            Map<String, Object> item = new HashMap<>();
            item.put("date", entry.getKey());
            item.put("amount", entry.getValue());
            trend.add(item);
        }

        List<RoomType> roomTypes = roomTypeMapper.selectListByQuery(
                QueryWrapper.create().from(RoomType.class).where(ROOM_TYPE.DELETED.eq(0)));

        List<Map<String, Object>> roomTypeAnalysis = new ArrayList<>();
        for (RoomType roomType : roomTypes) {
            Map<String, Object> item = new HashMap<>();
            item.put("roomTypeId", roomType.getId());
            item.put("roomTypeName", roomType.getTypeName());

            QueryWrapper checkInQuery = QueryWrapper.create()
                    .from(CheckIn.class)
                    .where(CHECK_IN.ROOM_TYPE_ID.eq(roomType.getId()));
            if (StringUtils.hasText(startDate)) {
                LocalDate start = LocalDate.parse(startDate);
                checkInQuery.and(CHECK_IN.CHECK_IN_DATE.ge(start));
            }
            if (StringUtils.hasText(endDate)) {
                LocalDate end = LocalDate.parse(endDate);
                checkInQuery.and(CHECK_IN.CHECK_OUT_DATE.le(end));
            }
            List<CheckIn> checkIns = checkInMapper.selectListByQuery(checkInQuery);

            BigDecimal roomRevenue = BigDecimal.ZERO;
            long totalStayDays = 0;
            for (CheckIn ci : checkIns) {
                roomRevenue = roomRevenue.add(ci.getRoomTotal() != null ? ci.getRoomTotal() : BigDecimal.ZERO);
                totalStayDays += ci.getStayedDays() != null ? ci.getStayedDays() : 0;
            }

            int roomCount = roomType.getRoomCount() != null ? roomType.getRoomCount() : 0;
            long daysDiff = 1;
            if (StringUtils.hasText(startDate) && StringUtils.hasText(endDate)) {
                daysDiff = ChronoUnit.DAYS.between(LocalDate.parse(startDate), LocalDate.parse(endDate));
                if (daysDiff == 0) daysDiff = 1;
            }
            long totalRoomNights = (long) roomCount * daysDiff;
            BigDecimal occupancyRate = totalRoomNights > 0
                    ? new BigDecimal(totalStayDays).multiply(new BigDecimal("100")).divide(new BigDecimal(totalRoomNights), 2, RoundingMode.HALF_UP)
                    : BigDecimal.ZERO;
            BigDecimal adr = totalStayDays > 0
                    ? roomRevenue.divide(new BigDecimal(totalStayDays), 2, RoundingMode.HALF_UP)
                    : BigDecimal.ZERO;
            BigDecimal revpar = adr.multiply(occupancyRate).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
            BigDecimal avgStayDays = checkIns.size() > 0
                    ? new BigDecimal(totalStayDays).divide(new BigDecimal(checkIns.size()), 1, RoundingMode.HALF_UP)
                    : BigDecimal.ZERO;

            item.put("revenue", roomRevenue);
            item.put("RevPAR", revpar);
            item.put("ADR", adr);
            item.put("occupancyRate", occupancyRate);
            item.put("avgStayDays", avgStayDays);
            roomTypeAnalysis.add(item);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("composition", composition);
        result.put("trend", trend);
        result.put("roomTypeAnalysis", roomTypeAnalysis);
        return result;
    }

    public Map<String, Object> getCostProfitAnalysis(String startDate, String endDate) {
        BigDecimal totalRevenue = BigDecimal.ZERO;
        QueryWrapper query = QueryWrapper.create()
                .from(CheckOutRecord.class)
                .where(CHECK_OUT_RECORD.ID.isNotNull());
        if (StringUtils.hasText(startDate)) {
            LocalDateTime startDateTime = LocalDateTime.parse(startDate + "T00:00:00");
            query.and(CHECK_OUT_RECORD.CREATE_TIME.ge(startDateTime));
        }
        if (StringUtils.hasText(endDate)) {
            LocalDateTime endDateTime = LocalDateTime.parse(endDate + "T23:59:59");
            query.and(CHECK_OUT_RECORD.CREATE_TIME.le(endDateTime));
        }
        List<CheckOutRecord> records = checkOutRecordMapper.selectListByQuery(query);
        for (CheckOutRecord record : records) {
            totalRevenue = totalRevenue.add(record.getTotalAmount() != null ? record.getTotalAmount() : BigDecimal.ZERO);
        }

        Map<String, Object> costComposition = new HashMap<>();
        costComposition.put("labor", BigDecimal.ZERO);
        costComposition.put("energy", BigDecimal.ZERO);
        costComposition.put("material", BigDecimal.ZERO);
        costComposition.put("maintenance", BigDecimal.ZERO);
        costComposition.put("other", BigDecimal.ZERO);
        costComposition.put("total", BigDecimal.ZERO);

        Map<String, Object> profit = new HashMap<>();
        profit.put("grossProfit", totalRevenue);
        profit.put("netProfit", totalRevenue);
        profit.put("grossMargin", totalRevenue.compareTo(BigDecimal.ZERO) > 0 ? new BigDecimal("100") : BigDecimal.ZERO);
        profit.put("netMargin", totalRevenue.compareTo(BigDecimal.ZERO) > 0 ? new BigDecimal("100") : BigDecimal.ZERO);

        List<RoomType> roomTypes = roomTypeMapper.selectListByQuery(
                QueryWrapper.create().from(RoomType.class).where(ROOM_TYPE.DELETED.eq(0)));
        int totalRooms = 0;
        for (RoomType rt : roomTypes) {
            totalRooms += rt.getRoomCount() != null ? rt.getRoomCount() : 0;
        }
        BigDecimal avgRoomPrice = BigDecimal.ZERO;
        if (!records.isEmpty()) {
            BigDecimal sum = BigDecimal.ZERO;
            for (CheckOutRecord r : records) {
                sum = sum.add(r.getRoomTotal() != null ? r.getRoomTotal() : BigDecimal.ZERO);
            }
            avgRoomPrice = sum.divide(new BigDecimal(records.size()), 2, RoundingMode.HALF_UP);
        }
        long daysDiff = 1;
        if (StringUtils.hasText(startDate) && StringUtils.hasText(endDate)) {
            daysDiff = ChronoUnit.DAYS.between(LocalDate.parse(startDate), LocalDate.parse(endDate));
            if (daysDiff == 0) daysDiff = 1;
        }
        BigDecimal fixedCost = BigDecimal.ZERO;
        BigDecimal breakevenOccupancy = avgRoomPrice.compareTo(BigDecimal.ZERO) > 0 && totalRooms > 0
                ? fixedCost.divide(avgRoomPrice.multiply(new BigDecimal(totalRooms)).multiply(new BigDecimal(daysDiff)), 4, RoundingMode.HALF_UP).multiply(new BigDecimal("100"))
                : BigDecimal.ZERO;

        Map<String, Object> result = new HashMap<>();
        result.put("costComposition", costComposition);
        result.put("profit", profit);
        result.put("breakevenOccupancyRate", breakevenOccupancy);
        return result;
    }

    public Map<String, Object> getReceivableAnalysis(String startDate, String endDate) {
        QueryWrapper creditQuery = QueryWrapper.create()
                .from(CreditBill.class)
                .where(CREDIT_BILL.STATUS.eq(0));
        if (StringUtils.hasText(startDate)) {
            LocalDateTime startDateTime = LocalDateTime.parse(startDate + "T00:00:00");
            creditQuery.and(CREDIT_BILL.BILL_TIME.ge(startDateTime));
        }
        if (StringUtils.hasText(endDate)) {
            LocalDateTime endDateTime = LocalDateTime.parse(endDate + "T23:59:59");
            creditQuery.and(CREDIT_BILL.BILL_TIME.le(endDateTime));
        }
        List<CreditBill> creditBills = creditBillMapper.selectListByQuery(creditQuery);

        BigDecimal totalReceivable = BigDecimal.ZERO;
        BigDecimal age30 = BigDecimal.ZERO;
        BigDecimal age60 = BigDecimal.ZERO;
        BigDecimal age90 = BigDecimal.ZERO;
        BigDecimal age90Plus = BigDecimal.ZERO;
        LocalDateTime now = LocalDateTime.now();

        Map<Long, BigDecimal> byUnit = new HashMap<>();
        Map<Long, String> unitNames = new HashMap<>();
        int overdueCount = 0;

        for (CreditBill bill : creditBills) {
            BigDecimal amount = bill.getTotalAmount() != null ? bill.getTotalAmount() : BigDecimal.ZERO;
            totalReceivable = totalReceivable.add(amount);

            if (bill.getBillTime() != null) {
                long days = ChronoUnit.DAYS.between(bill.getBillTime(), now);
                if (days <= 30) {
                    age30 = age30.add(amount);
                } else if (days <= 60) {
                    age60 = age60.add(amount);
                } else if (days <= 90) {
                    age90 = age90.add(amount);
                } else {
                    age90Plus = age90Plus.add(amount);
                }
                if (days > 30) {
                    overdueCount++;
                }
            }

            if (bill.getAgreementUnitId() != null) {
                byUnit.merge(bill.getAgreementUnitId(), amount, BigDecimal::add);
                if (bill.getAgreementUnitName() != null) {
                    unitNames.put(bill.getAgreementUnitId(), bill.getAgreementUnitName());
                }
            }
        }

        List<Map<String, Object>> agingAnalysis = new ArrayList<>();
        Map<String, Object> age30Item = new HashMap<>();
        age30Item.put("range", "0-30天");
        age30Item.put("amount", age30);
        agingAnalysis.add(age30Item);
        Map<String, Object> age60Item = new HashMap<>();
        age60Item.put("range", "31-60天");
        age60Item.put("amount", age60);
        agingAnalysis.add(age60Item);
        Map<String, Object> age90Item = new HashMap<>();
        age90Item.put("range", "61-90天");
        age90Item.put("amount", age90);
        agingAnalysis.add(age90Item);
        Map<String, Object> age90PlusItem = new HashMap<>();
        age90PlusItem.put("range", "90天以上");
        age90PlusItem.put("amount", age90Plus);
        agingAnalysis.add(age90PlusItem);

        List<Map<String, Object>> byUnitList = new ArrayList<>();
        for (Map.Entry<Long, BigDecimal> entry : byUnit.entrySet()) {
            Map<String, Object> item = new HashMap<>();
            item.put("unitId", entry.getKey());
            item.put("unitName", unitNames.getOrDefault(entry.getKey(), ""));
            item.put("amount", entry.getValue());
            byUnitList.add(item);
        }

        BigDecimal turnoverRate = totalReceivable.compareTo(BigDecimal.ZERO) > 0
                ? new BigDecimal(creditBills.size()).divide(totalReceivable, 4, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        Map<String, Object> result = new HashMap<>();
        result.put("totalReceivable", totalReceivable);
        result.put("agingAnalysis", agingAnalysis);
        result.put("byUnit", byUnitList);
        result.put("turnoverRate", turnoverRate);
        result.put("overdueCount", overdueCount);
        result.put("overdueAmount", age60.add(age90).add(age90Plus));
        return result;
    }

    public Map<String, Object> getCashFlowAnalysis(String startDate, String endDate) {
        QueryWrapper outQuery = QueryWrapper.create()
                .from(CheckOutRecord.class)
                .where(CHECK_OUT_RECORD.ID.isNotNull());
        if (StringUtils.hasText(startDate)) {
            LocalDateTime startDateTime = LocalDateTime.parse(startDate + "T00:00:00");
            outQuery.and(CHECK_OUT_RECORD.CREATE_TIME.ge(startDateTime));
        }
        if (StringUtils.hasText(endDate)) {
            LocalDateTime endDateTime = LocalDateTime.parse(endDate + "T23:59:59");
            outQuery.and(CHECK_OUT_RECORD.CREATE_TIME.le(endDateTime));
        }
        List<CheckOutRecord> checkOutRecords = checkOutRecordMapper.selectListByQuery(outQuery);

        BigDecimal roomFeeIn = BigDecimal.ZERO;
        BigDecimal depositIn = BigDecimal.ZERO;
        BigDecimal otherIn = BigDecimal.ZERO;
        for (CheckOutRecord record : checkOutRecords) {
            roomFeeIn = roomFeeIn.add(record.getRoomTotal() != null ? record.getRoomTotal() : BigDecimal.ZERO);
            depositIn = depositIn.add(record.getDepositDeducted() != null ? record.getDepositDeducted() : BigDecimal.ZERO);
            otherIn = otherIn.add(record.getOtherFee() != null ? record.getOtherFee() : BigDecimal.ZERO);
        }

        QueryWrapper creditQuery = QueryWrapper.create()
                .from(CreditBill.class)
                .where(CREDIT_BILL.STATUS.eq(1));
        if (StringUtils.hasText(startDate)) {
            LocalDateTime startDateTime = LocalDateTime.parse(startDate + "T00:00:00");
            creditQuery.and(CREDIT_BILL.CREATE_TIME.ge(startDateTime));
        }
        if (StringUtils.hasText(endDate)) {
            LocalDateTime endDateTime = LocalDateTime.parse(endDate + "T23:59:59");
            creditQuery.and(CREDIT_BILL.CREATE_TIME.le(endDateTime));
        }
        List<CreditBill> settledCredits = creditBillMapper.selectListByQuery(creditQuery);
        BigDecimal creditRecovery = BigDecimal.ZERO;
        for (CreditBill bill : settledCredits) {
            creditRecovery = creditRecovery.add(bill.getTotalAmount() != null ? bill.getTotalAmount() : BigDecimal.ZERO);
        }

        Map<String, Object> inflow = new HashMap<>();
        inflow.put("roomFee", roomFeeIn);
        inflow.put("deposit", depositIn);
        inflow.put("other", otherIn);
        inflow.put("creditRecovery", creditRecovery);
        BigDecimal totalInflow = roomFeeIn.add(depositIn).add(otherIn).add(creditRecovery);
        inflow.put("total", totalInflow);

        QueryWrapper refundQuery = QueryWrapper.create()
                .from(RefundApply.class)
                .where(REFUND_APPLY.STATUS.eq(3));
        if (StringUtils.hasText(startDate)) {
            LocalDateTime startDateTime = LocalDateTime.parse(startDate + "T00:00:00");
            refundQuery.and(REFUND_APPLY.REFUND_TIME.ge(startDateTime));
        }
        if (StringUtils.hasText(endDate)) {
            LocalDateTime endDateTime = LocalDateTime.parse(endDate + "T23:59:59");
            refundQuery.and(REFUND_APPLY.REFUND_TIME.le(endDateTime));
        }
        List<RefundApply> refundApplies = refundApplyMapper.selectListByQuery(refundQuery);
        BigDecimal refundOut = BigDecimal.ZERO;
        for (RefundApply refund : refundApplies) {
            refundOut = refundOut.add(refund.getApprovedAmount() != null ? refund.getApprovedAmount() : BigDecimal.ZERO);
        }

        BigDecimal compensationOut = BigDecimal.ZERO;
        for (CheckOutRecord record : checkOutRecords) {
            compensationOut = compensationOut.add(record.getDamageCompensation() != null ? record.getDamageCompensation() : BigDecimal.ZERO);
        }

        Map<String, Object> outflow = new HashMap<>();
        outflow.put("refund", refundOut);
        outflow.put("compensation", compensationOut);
        outflow.put("other", BigDecimal.ZERO);
        BigDecimal totalOutflow = refundOut.add(compensationOut);
        outflow.put("total", totalOutflow);

        BigDecimal netCashFlow = totalInflow.subtract(totalOutflow);

        Map<String, BigDecimal> dailyInflow = new HashMap<>();
        Map<String, BigDecimal> dailyOutflow = new HashMap<>();
        for (CheckOutRecord record : checkOutRecords) {
            if (record.getCreateTime() != null) {
                String day = record.getCreateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                BigDecimal amount = record.getTotalAmount() != null ? record.getTotalAmount() : BigDecimal.ZERO;
                dailyInflow.merge(day, amount, BigDecimal::add);
            }
        }
        for (RefundApply refund : refundApplies) {
            if (refund.getRefundTime() != null) {
                String day = refund.getRefundTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                BigDecimal amount = refund.getApprovedAmount() != null ? refund.getApprovedAmount() : BigDecimal.ZERO;
                dailyOutflow.merge(day, amount, BigDecimal::add);
            }
        }
        List<Map<String, Object>> trend = new ArrayList<>();
        java.util.Set<String> allDays = new java.util.HashSet<>();
        allDays.addAll(dailyInflow.keySet());
        allDays.addAll(dailyOutflow.keySet());
        for (String day : allDays) {
            Map<String, Object> item = new HashMap<>();
            item.put("date", day);
            item.put("inflow", dailyInflow.getOrDefault(day, BigDecimal.ZERO));
            item.put("outflow", dailyOutflow.getOrDefault(day, BigDecimal.ZERO));
            item.put("net", dailyInflow.getOrDefault(day, BigDecimal.ZERO).subtract(dailyOutflow.getOrDefault(day, BigDecimal.ZERO)));
            trend.add(item);
        }

        BigDecimal healthRatio = totalOutflow.compareTo(BigDecimal.ZERO) > 0
                ? totalInflow.divide(totalOutflow, 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        Map<String, Object> result = new HashMap<>();
        result.put("inflow", inflow);
        result.put("outflow", outflow);
        result.put("netCashFlow", netCashFlow);
        result.put("trend", trend);
        result.put("healthRatio", healthRatio);
        return result;
    }

    public Map<String, Object> getFinanceDashboard() {
        LocalDate today = LocalDate.now();
        LocalDateTime todayStart = today.atStartOfDay();
        LocalDateTime todayEnd = today.atTime(23, 59, 59);

        QueryWrapper todayQuery = QueryWrapper.create()
                .from(CheckOutRecord.class)
                .where(CHECK_OUT_RECORD.CREATE_TIME.ge(todayStart))
                .and(CHECK_OUT_RECORD.CREATE_TIME.le(todayEnd));
        List<CheckOutRecord> todayRecords = checkOutRecordMapper.selectListByQuery(todayQuery);

        BigDecimal todayRevenue = BigDecimal.ZERO;
        long todayCount = todayRecords.size();
        for (CheckOutRecord record : todayRecords) {
            todayRevenue = todayRevenue.add(record.getTotalAmount() != null ? record.getTotalAmount() : BigDecimal.ZERO);
        }

        Map<String, Object> todayData = new HashMap<>();
        todayData.put("revenue", todayRevenue);
        todayData.put("count", todayCount);

        LocalDateTime monthStart = today.withDayOfMonth(1).atStartOfDay();
        QueryWrapper monthQuery = QueryWrapper.create()
                .from(CheckOutRecord.class)
                .where(CHECK_OUT_RECORD.CREATE_TIME.ge(monthStart))
                .and(CHECK_OUT_RECORD.CREATE_TIME.le(todayEnd));
        List<CheckOutRecord> monthRecords = checkOutRecordMapper.selectListByQuery(monthQuery);

        BigDecimal monthRevenue = BigDecimal.ZERO;
        long monthCount = monthRecords.size();
        for (CheckOutRecord record : monthRecords) {
            monthRevenue = monthRevenue.add(record.getTotalAmount() != null ? record.getTotalAmount() : BigDecimal.ZERO);
        }

        Map<String, Object> monthData = new HashMap<>();
        monthData.put("revenue", monthRevenue);
        monthData.put("count", monthCount);

        QueryWrapper creditQuery = QueryWrapper.create()
                .from(CreditBill.class)
                .where(CREDIT_BILL.STATUS.eq(0));
        long receivableCount = creditBillMapper.selectCountByQuery(creditQuery);

        Map<String, Object> indicators = new HashMap<>();
        indicators.put("todayRevenue", todayRevenue);
        indicators.put("monthRevenue", monthRevenue);
        indicators.put("receivableCount", receivableCount);
        indicators.put("avgRoomPrice", todayCount > 0 ? todayRevenue.divide(new BigDecimal(todayCount), 2, RoundingMode.HALF_UP) : BigDecimal.ZERO);

        List<Map<String, Object>> weekTrend = new ArrayList<>();
        for (int i = 6; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            LocalDateTime dayStart = date.atStartOfDay();
            LocalDateTime dayEnd = date.atTime(23, 59, 59);
            QueryWrapper dayQuery = QueryWrapper.create()
                    .from(CheckOutRecord.class)
                    .where(CHECK_OUT_RECORD.CREATE_TIME.ge(dayStart))
                    .and(CHECK_OUT_RECORD.CREATE_TIME.le(dayEnd));
            List<CheckOutRecord> dayRecords = checkOutRecordMapper.selectListByQuery(dayQuery);
            BigDecimal dayTotal = BigDecimal.ZERO;
            for (CheckOutRecord r : dayRecords) {
                dayTotal = dayTotal.add(r.getTotalAmount() != null ? r.getTotalAmount() : BigDecimal.ZERO);
            }
            Map<String, Object> dayItem = new HashMap<>();
            dayItem.put("date", date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
            dayItem.put("revenue", dayTotal);
            weekTrend.add(dayItem);
        }

        List<Map<String, Object>> warnings = new ArrayList<>();
        QueryWrapper overDueCreditQuery = QueryWrapper.create()
                .from(CreditBill.class)
                .where(CREDIT_BILL.STATUS.eq(0));
        List<CreditBill> overdueBills = creditBillMapper.selectListByQuery(overDueCreditQuery);
        for (CreditBill bill : overdueBills) {
            if (bill.getBillTime() != null && ChronoUnit.DAYS.between(bill.getBillTime(), LocalDateTime.now()) > 30) {
                Map<String, Object> w = new HashMap<>();
                w.put("type", "overdue");
                w.put("message", bill.getAgreementUnitName() + " 有逾期超过30天的应收账款");
                w.put("amount", bill.getTotalAmount());
                warnings.add(w);
                if (warnings.size() >= 5) break;
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("todayData", todayData);
        result.put("monthData", monthData);
        result.put("indicators", indicators);
        result.put("weekTrend", weekTrend);
        result.put("warnings", warnings);
        return result;
    }

    public Map<String, Object> getComparisonAnalysis(String type) {
        LocalDate today = LocalDate.now();
        Map<String, Object> timeComparison = new HashMap<>();

        if ("month".equals(type)) {
            LocalDate thisMonthStart = today.withDayOfMonth(1);
            LocalDate lastMonthStart = thisMonthStart.minusMonths(1);
            LocalDate lastMonthEnd = thisMonthStart.minusDays(1);

            BigDecimal thisMonthRevenue = getRevenueBetween(thisMonthStart.atStartOfDay(), today.atTime(23, 59, 59));
            BigDecimal lastMonthRevenue = getRevenueBetween(lastMonthStart.atStartOfDay(), lastMonthEnd.atTime(23, 59, 59));
            BigDecimal growthRate = lastMonthRevenue.compareTo(BigDecimal.ZERO) > 0
                    ? thisMonthRevenue.subtract(lastMonthRevenue).multiply(new BigDecimal("100")).divide(lastMonthRevenue, 2, RoundingMode.HALF_UP)
                    : BigDecimal.ZERO;

            timeComparison.put("thisPeriod", thisMonthRevenue);
            timeComparison.put("lastPeriod", lastMonthRevenue);
            timeComparison.put("growthRate", growthRate);
        } else if ("week".equals(type)) {
            LocalDate thisWeekStart = today.minusDays(today.getDayOfWeek().getValue() - 1);
            LocalDate lastWeekStart = thisWeekStart.minusWeeks(1);
            LocalDate lastWeekEnd = thisWeekStart.minusDays(1);

            BigDecimal thisWeekRevenue = getRevenueBetween(thisWeekStart.atStartOfDay(), today.atTime(23, 59, 59));
            BigDecimal lastWeekRevenue = getRevenueBetween(lastWeekStart.atStartOfDay(), lastWeekEnd.atTime(23, 59, 59));
            BigDecimal growthRate = lastWeekRevenue.compareTo(BigDecimal.ZERO) > 0
                    ? thisWeekRevenue.subtract(lastWeekRevenue).multiply(new BigDecimal("100")).divide(lastWeekRevenue, 2, RoundingMode.HALF_UP)
                    : BigDecimal.ZERO;

            timeComparison.put("thisPeriod", thisWeekRevenue);
            timeComparison.put("lastPeriod", lastWeekRevenue);
            timeComparison.put("growthRate", growthRate);
        } else {
            BigDecimal todayRevenue = getRevenueBetween(today.atStartOfDay(), today.atTime(23, 59, 59));
            BigDecimal yesterdayRevenue = getRevenueBetween(today.minusDays(1).atStartOfDay(), today.minusDays(1).atTime(23, 59, 59));
            BigDecimal growthRate = yesterdayRevenue.compareTo(BigDecimal.ZERO) > 0
                    ? todayRevenue.subtract(yesterdayRevenue).multiply(new BigDecimal("100")).divide(yesterdayRevenue, 2, RoundingMode.HALF_UP)
                    : BigDecimal.ZERO;

            timeComparison.put("thisPeriod", todayRevenue);
            timeComparison.put("lastPeriod", yesterdayRevenue);
            timeComparison.put("growthRate", growthRate);
        }

        Map<String, Object> targetComparison = new HashMap<>();
        targetComparison.put("target", BigDecimal.ZERO);
        targetComparison.put("actual", timeComparison.get("thisPeriod"));
        targetComparison.put("completionRate", BigDecimal.ZERO);

        List<RoomType> roomTypes = roomTypeMapper.selectListByQuery(
                QueryWrapper.create().from(RoomType.class).where(ROOM_TYPE.DELETED.eq(0)));
        List<Map<String, Object>> roomTypeComparison = new ArrayList<>();
        for (RoomType rt : roomTypes) {
            Map<String, Object> item = new HashMap<>();
            item.put("roomTypeId", rt.getId());
            item.put("roomTypeName", rt.getTypeName());
            item.put("revenue", BigDecimal.ZERO);
            item.put("growthRate", BigDecimal.ZERO);
            roomTypeComparison.add(item);
        }

        Map<String, Object> channelComparison = new HashMap<>();
        channelComparison.put("data", new ArrayList<>());
        channelComparison.put("growthRate", BigDecimal.ZERO);

        Map<String, Object> result = new HashMap<>();
        result.put("timeComparison", timeComparison);
        result.put("targetComparison", targetComparison);
        result.put("roomTypeComparison", roomTypeComparison);
        result.put("channelComparison", channelComparison);
        return result;
    }

    private BigDecimal getRevenueBetween(LocalDateTime start, LocalDateTime end) {
        QueryWrapper query = QueryWrapper.create()
                .from(CheckOutRecord.class)
                .where(CHECK_OUT_RECORD.CREATE_TIME.ge(start))
                .and(CHECK_OUT_RECORD.CREATE_TIME.le(end));
        List<CheckOutRecord> records = checkOutRecordMapper.selectListByQuery(query);
        BigDecimal total = BigDecimal.ZERO;
        for (CheckOutRecord record : records) {
            total = total.add(record.getTotalAmount() != null ? record.getTotalAmount() : BigDecimal.ZERO);
        }
        return total;
    }
}
