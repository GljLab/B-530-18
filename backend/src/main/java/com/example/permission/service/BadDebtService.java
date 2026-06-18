package com.example.permission.service;

import com.example.permission.common.BusinessException;
import com.example.permission.entity.BadDebt;
import com.example.permission.entity.BadDebtAction;
import com.example.permission.entity.CheckIn;
import com.example.permission.entity.CheckOutRecord;
import com.example.permission.entity.CreditBill;
import com.example.permission.mapper.BadDebtActionMapper;
import com.example.permission.mapper.BadDebtMapper;
import com.example.permission.mapper.CheckInMapper;
import com.example.permission.mapper.CheckOutRecordMapper;
import com.example.permission.mapper.CreditBillMapper;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import static com.example.permission.entity.table.BadDebtTableDef.BAD_DEBT;
import static com.example.permission.entity.table.BadDebtActionTableDef.BAD_DEBT_ACTION;
import static com.example.permission.entity.table.CreditBillTableDef.CREDIT_BILL;
import static com.example.permission.entity.table.CheckInTableDef.CHECK_IN;
import static com.example.permission.entity.table.CheckOutRecordTableDef.CHECK_OUT_RECORD;

@Service
public class BadDebtService {

    @Autowired
    private BadDebtMapper badDebtMapper;

    @Autowired
    private BadDebtActionMapper badDebtActionMapper;

    @Autowired
    private CreditBillMapper creditBillMapper;

    @Autowired
    private CheckInMapper checkInMapper;

    @Autowired
    private CheckOutRecordMapper checkOutRecordMapper;

    public Map<String, Object> getBadDebtPage(String keyword, Integer debtType, Integer status, String startDate, String endDate, int pageNum, int pageSize) {
        QueryWrapper query = QueryWrapper.create()
                .from(BadDebt.class)
                .where(BAD_DEBT.DELETED.eq(0));
        if (StringUtils.hasText(keyword)) {
            query.and(BAD_DEBT.CUSTOMER_NAME.like(keyword)
                    .or(BAD_DEBT.RELATED_ORDER_NO.like(keyword)));
        }
        if (debtType != null) {
            query.and(BAD_DEBT.DEBT_TYPE.eq(debtType));
        }
        if (status != null) {
            query.and(BAD_DEBT.STATUS.eq(status));
        }
        if (StringUtils.hasText(startDate)) {
            LocalDateTime startDateTime = LocalDateTime.parse(startDate + "T00:00:00");
            query.and(BAD_DEBT.CREATE_TIME.ge(startDateTime));
        }
        if (StringUtils.hasText(endDate)) {
            LocalDateTime endDateTime = LocalDateTime.parse(endDate + "T23:59:59");
            query.and(BAD_DEBT.CREATE_TIME.le(endDateTime));
        }
        query.orderBy(BAD_DEBT.CREATE_TIME.desc());
        Page<BadDebt> page = badDebtMapper.paginate(Page.of(pageNum, pageSize), query);
        Map<String, Object> result = new HashMap<>();
        result.put("list", page.getRecords());
        result.put("total", page.getTotalRow());
        return result;
    }

    public BadDebt getBadDebtById(Long id) {
        QueryWrapper query = QueryWrapper.create()
                .from(BadDebt.class)
                .where(BAD_DEBT.ID.eq(id))
                .and(BAD_DEBT.DELETED.eq(0));
        BadDebt badDebt = badDebtMapper.selectOneByQuery(query);
        if (badDebt == null) {
            throw new BusinessException("坏账记录不存在");
        }
        return badDebt;
    }

    public List<BadDebtAction> getBadDebtActions(Long badDebtId) {
        QueryWrapper query = QueryWrapper.create()
                .from(BadDebtAction.class)
                .where(BAD_DEBT_ACTION.BAD_DEBT_ID.eq(badDebtId))
                .orderBy(BAD_DEBT_ACTION.ACTION_TIME.desc());
        return badDebtActionMapper.selectListByQuery(query);
    }

    @Transactional(rollbackFor = Exception.class)
    public void identifyBadDebts() {
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);

        QueryWrapper creditQuery = QueryWrapper.create()
                .from(CreditBill.class)
                .where(CREDIT_BILL.STATUS.eq(0))
                .and(CREDIT_BILL.BILL_TIME.le(thirtyDaysAgo));
        List<CreditBill> overdueCreditBills = creditBillMapper.selectListByQuery(creditQuery);

        for (CreditBill bill : overdueCreditBills) {
            QueryWrapper existQuery = QueryWrapper.create()
                    .from(BadDebt.class)
                    .where(BAD_DEBT.RELATED_ORDER_TYPE.eq(1))
                    .and(BAD_DEBT.RELATED_ORDER_ID.eq(bill.getId()))
                    .and(BAD_DEBT.DELETED.eq(0));
            long existCount = badDebtMapper.selectCountByQuery(existQuery);
            if (existCount > 0) {
                continue;
            }
            BadDebt badDebt = new BadDebt();
            badDebt.setDebtNo(generateDebtNo());
            badDebt.setRelatedOrderType(1);
            badDebt.setRelatedOrderId(bill.getId());
            badDebt.setRelatedOrderNo(bill.getBillNo());
            badDebt.setCustomerId(null);
            badDebt.setCustomerName(bill.getCustomerName());
            badDebt.setDebtType(1);
            badDebt.setOriginalAmount(bill.getTotalAmount() != null ? bill.getTotalAmount() : BigDecimal.ZERO);
            badDebt.setRecoveredAmount(BigDecimal.ZERO);
            badDebt.setRemainingAmount(bill.getTotalAmount() != null ? bill.getTotalAmount() : BigDecimal.ZERO);
            badDebt.setDebtTime(bill.getBillTime());
            long overdueDays = ChronoUnit.DAYS.between(bill.getBillTime(), LocalDateTime.now());
            badDebt.setOverdueDays((int) overdueDays);
            badDebt.setStatus(0);
            badDebt.setCreateTime(LocalDateTime.now());
            badDebt.setUpdateTime(LocalDateTime.now());
            badDebt.setDeleted(0);
            badDebtMapper.insert(badDebt);
        }

        QueryWrapper checkInQuery = QueryWrapper.create()
                .from(CheckIn.class)
                .where(CHECK_IN.IS_CREDIT.eq(1))
                .and(CHECK_IN.STATUS.eq(3))
                .and(CHECK_IN.CREATE_TIME.le(thirtyDaysAgo));
        List<CheckIn> overdueCheckIns = checkInMapper.selectListByQuery(checkInQuery);

        for (CheckIn checkIn : overdueCheckIns) {
            QueryWrapper existQuery = QueryWrapper.create()
                    .from(BadDebt.class)
                    .where(BAD_DEBT.RELATED_ORDER_TYPE.eq(2))
                    .and(BAD_DEBT.RELATED_ORDER_ID.eq(checkIn.getId()))
                    .and(BAD_DEBT.DELETED.eq(0));
            long existCount = badDebtMapper.selectCountByQuery(existQuery);
            if (existCount > 0) {
                continue;
            }
            BadDebt badDebt = new BadDebt();
            badDebt.setDebtNo(generateDebtNo());
            badDebt.setRelatedOrderType(2);
            badDebt.setRelatedOrderId(checkIn.getId());
            badDebt.setRelatedOrderNo(checkIn.getCheckInNo());
            badDebt.setCustomerId(checkIn.getCustomerId());
            badDebt.setCustomerName(checkIn.getCustomerName());
            badDebt.setDebtType(2);
            BigDecimal payAmount = checkIn.getPayableAmount() != null ? checkIn.getPayableAmount() : BigDecimal.ZERO;
            BigDecimal paidAmount = checkIn.getPaidAmount() != null ? checkIn.getPaidAmount() : BigDecimal.ZERO;
            BigDecimal remaining = payAmount.subtract(paidAmount);
            if (remaining.compareTo(BigDecimal.ZERO) <= 0) {
                continue;
            }
            badDebt.setOriginalAmount(remaining);
            badDebt.setRecoveredAmount(BigDecimal.ZERO);
            badDebt.setRemainingAmount(remaining);
            badDebt.setDebtTime(checkIn.getCreateTime());
            long overdueDays = ChronoUnit.DAYS.between(checkIn.getCreateTime(), LocalDateTime.now());
            badDebt.setOverdueDays((int) overdueDays);
            badDebt.setStatus(0);
            badDebt.setCreateTime(LocalDateTime.now());
            badDebt.setUpdateTime(LocalDateTime.now());
            badDebt.setDeleted(0);
            badDebtMapper.insert(badDebt);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public BadDebtAction collectDebt(Long id, Integer method, String result, Long operatorId, String operatorName) {
        BadDebt badDebt = getBadDebtById(id);
        BadDebtAction action = new BadDebtAction();
        action.setBadDebtId(id);
        action.setActionType(1);
        action.setActionTime(LocalDateTime.now());
        action.setActionMethod(method);
        action.setActionResult(result);
        action.setOperatorId(operatorId);
        action.setOperatorName(operatorName);
        action.setCreateTime(LocalDateTime.now());
        badDebtActionMapper.insert(action);

        badDebt.setStatus(1);
        badDebt.setUpdateTime(LocalDateTime.now());
        badDebtMapper.update(badDebt);
        return action;
    }

    @Transactional(rollbackFor = Exception.class)
    public BadDebtAction recoverDebt(Long id, BigDecimal amount, Integer paymentMethod, Long operatorId, String operatorName) {
        BadDebt badDebt = getBadDebtById(id);
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("收回金额必须大于0");
        }
        BigDecimal currentRecovered = badDebt.getRecoveredAmount() != null ? badDebt.getRecoveredAmount() : BigDecimal.ZERO;
        BigDecimal currentRemaining = badDebt.getRemainingAmount() != null ? badDebt.getRemainingAmount() : BigDecimal.ZERO;
        if (amount.compareTo(currentRemaining) > 0) {
            throw new BusinessException("收回金额不能大于剩余金额");
        }
        BadDebtAction action = new BadDebtAction();
        action.setBadDebtId(id);
        action.setActionType(2);
        action.setActionTime(LocalDateTime.now());
        action.setRecoveredAmount(amount);
        action.setPaymentMethod(paymentMethod);
        action.setPaymentTime(LocalDateTime.now());
        action.setOperatorId(operatorId);
        action.setOperatorName(operatorName);
        action.setCreateTime(LocalDateTime.now());
        badDebtActionMapper.insert(action);

        BigDecimal newRecovered = currentRecovered.add(amount);
        BigDecimal newRemaining = currentRemaining.subtract(amount);
        badDebt.setRecoveredAmount(newRecovered);
        badDebt.setRemainingAmount(newRemaining);
        if (newRemaining.compareTo(BigDecimal.ZERO) == 0) {
            badDebt.setStatus(5);
        } else {
            badDebt.setStatus(4);
        }
        badDebt.setUpdateTime(LocalDateTime.now());
        badDebtMapper.update(badDebt);
        return action;
    }

    @Transactional(rollbackFor = Exception.class)
    public BadDebt writeOffDebt(Long id, String reason, String proof, Long approverId, String approverName) {
        BadDebt badDebt = getBadDebtById(id);
        if (approverId == null) {
            throw new BusinessException("核销必须指定审批人");
        }
        badDebt.setStatus(2);
        badDebt.setWriteOffReason(reason);
        badDebt.setWriteOffProof(proof);
        badDebt.setWriteOffApproverId(approverId);
        badDebt.setWriteOffApproverName(approverName);
        badDebt.setWriteOffApproveTime(LocalDateTime.now());
        badDebt.setUpdateTime(LocalDateTime.now());
        badDebtMapper.update(badDebt);
        return badDebt;
    }

    @Transactional(rollbackFor = Exception.class)
    public BadDebt legalPursuit(Long id, Integer legalStatus, String legalInfo, Long operatorId) {
        BadDebt badDebt = getBadDebtById(id);
        badDebt.setLegalStatus(legalStatus);
        badDebt.setLegalInfo(legalInfo);
        badDebt.setStatus(3);
        badDebt.setUpdateTime(LocalDateTime.now());
        badDebtMapper.update(badDebt);
        return badDebt;
    }

    public Map<String, Object> getBadDebtStatistics() {
        QueryWrapper query = QueryWrapper.create()
                .from(BadDebt.class)
                .where(BAD_DEBT.DELETED.eq(0));
        List<BadDebt> allDebts = badDebtMapper.selectListByQuery(query);

        BigDecimal totalDebt = BigDecimal.ZERO;
        Map<Integer, BigDecimal> byType = new HashMap<>();
        Map<Integer, Long> byStatus = new HashMap<>();
        for (BadDebt debt : allDebts) {
            BigDecimal amount = debt.getOriginalAmount() != null ? debt.getOriginalAmount() : BigDecimal.ZERO;
            totalDebt = totalDebt.add(amount);
            if (debt.getDebtType() != null) {
                byType.merge(debt.getDebtType(), amount, BigDecimal::add);
            }
            if (debt.getStatus() != null) {
                byStatus.merge(debt.getStatus(), 1L, Long::sum);
            }
        }

        QueryWrapper revenueQuery = QueryWrapper.create()
                .from(CheckOutRecord.class);
        List<CheckOutRecord> allCheckOuts = checkOutRecordMapper.selectListByQuery(revenueQuery);
        BigDecimal totalRevenue = BigDecimal.ZERO;
        for (CheckOutRecord record : allCheckOuts) {
            BigDecimal amount = record.getTotalAmount() != null ? record.getTotalAmount() : BigDecimal.ZERO;
            totalRevenue = totalRevenue.add(amount);
        }
        BigDecimal badDebtRate = totalRevenue.compareTo(BigDecimal.ZERO) > 0
                ? totalDebt.multiply(new BigDecimal("100")).divide(totalRevenue, 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        List<Map<String, Object>> byTypeList = new ArrayList<>();
        for (Map.Entry<Integer, BigDecimal> entry : byType.entrySet()) {
            Map<String, Object> item = new HashMap<>();
            item.put("debtType", entry.getKey());
            item.put("amount", entry.getValue());
            byTypeList.add(item);
        }

        List<Map<String, Object>> byStatusList = new ArrayList<>();
        for (Map.Entry<Integer, Long> entry : byStatus.entrySet()) {
            Map<String, Object> item = new HashMap<>();
            item.put("status", entry.getKey());
            item.put("count", entry.getValue());
            byStatusList.add(item);
        }

        List<Map<String, Object>> trend = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();
        for (int i = 11; i >= 0; i--) {
            LocalDateTime monthStart = now.minusMonths(i).withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
            LocalDateTime monthEnd = monthStart.plusMonths(1);
            BigDecimal monthTotal = BigDecimal.ZERO;
            for (BadDebt debt : allDebts) {
                if (debt.getCreateTime() != null
                        && !debt.getCreateTime().isBefore(monthStart)
                        && debt.getCreateTime().isBefore(monthEnd)) {
                    BigDecimal amount = debt.getOriginalAmount() != null ? debt.getOriginalAmount() : BigDecimal.ZERO;
                    monthTotal = monthTotal.add(amount);
                }
            }
            Map<String, Object> monthItem = new HashMap<>();
            monthItem.put("month", monthStart.format(DateTimeFormatter.ofPattern("yyyy-MM")));
            monthItem.put("amount", monthTotal);
            trend.add(monthItem);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("totalDebt", totalDebt);
        result.put("totalCount", allDebts.size());
        result.put("badDebtRate", badDebtRate);
        result.put("byType", byTypeList);
        result.put("byStatus", byStatusList);
        result.put("trend", trend);
        return result;
    }

    public List<BadDebt> exportBadDebts(String keyword, Integer debtType, Integer status, String startDate, String endDate) {
        QueryWrapper query = QueryWrapper.create()
                .from(BadDebt.class)
                .where(BAD_DEBT.DELETED.eq(0));
        if (StringUtils.hasText(keyword)) {
            query.and(BAD_DEBT.CUSTOMER_NAME.like(keyword)
                    .or(BAD_DEBT.RELATED_ORDER_NO.like(keyword)));
        }
        if (debtType != null) {
            query.and(BAD_DEBT.DEBT_TYPE.eq(debtType));
        }
        if (status != null) {
            query.and(BAD_DEBT.STATUS.eq(status));
        }
        if (StringUtils.hasText(startDate)) {
            LocalDateTime startDateTime = LocalDateTime.parse(startDate + "T00:00:00");
            query.and(BAD_DEBT.CREATE_TIME.ge(startDateTime));
        }
        if (StringUtils.hasText(endDate)) {
            LocalDateTime endDateTime = LocalDateTime.parse(endDate + "T23:59:59");
            query.and(BAD_DEBT.CREATE_TIME.le(endDateTime));
        }
        query.orderBy(BAD_DEBT.CREATE_TIME.desc());
        return badDebtMapper.selectListByQuery(query);
    }

    private String generateDebtNo() {
        String prefix = "BD" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        Random random = new Random();
        String suffix = String.format("%04d", random.nextInt(10000));
        String debtNo = prefix + suffix;
        QueryWrapper checkQuery = QueryWrapper.create()
                .from(BadDebt.class)
                .where(BAD_DEBT.DEBT_NO.eq(debtNo));
        long count = badDebtMapper.selectCountByQuery(checkQuery);
        if (count > 0) {
            return generateDebtNo();
        }
        return debtNo;
    }
}
