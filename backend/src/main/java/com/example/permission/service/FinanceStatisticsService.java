package com.example.permission.service;

import com.example.permission.entity.AgreementUnit;
import com.example.permission.entity.CheckOutRecord;
import com.example.permission.entity.CreditBill;
import com.example.permission.entity.RefundApply;
import com.example.permission.mapper.AgreementUnitMapper;
import com.example.permission.mapper.CheckOutRecordMapper;
import com.example.permission.mapper.CreditBillMapper;
import com.example.permission.mapper.DailyReconciliationMapper;
import com.example.permission.mapper.RefundApplyMapper;
import com.example.permission.mapper.ShiftReconciliationMapper;
import com.mybatisflex.core.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.example.permission.entity.table.AgreementUnitTableDef.AGREEMENT_UNIT;
import static com.example.permission.entity.table.CheckOutRecordTableDef.CHECK_OUT_RECORD;
import static com.example.permission.entity.table.CreditBillTableDef.CREDIT_BILL;
import static com.example.permission.entity.table.RefundApplyTableDef.REFUND_APPLY;

@Service
public class FinanceStatisticsService {

    @Autowired
    private CheckOutRecordMapper checkOutRecordMapper;

    @Autowired
    private CreditBillMapper creditBillMapper;

    @Autowired
    private RefundApplyMapper refundApplyMapper;

    @Autowired
    private AgreementUnitMapper agreementUnitMapper;

    @Autowired
    private DailyReconciliationMapper dailyReconciliationMapper;

    @Autowired
    private ShiftReconciliationMapper shiftReconciliationMapper;

    public Map<String, Object> getPaymentSummary(String startDate, String endDate) {
        LocalDateTime startDateTime = LocalDateTime.parse(startDate + "T00:00:00");
        LocalDateTime endDateTime = LocalDateTime.parse(endDate + "T23:59:59");

        QueryWrapper checkoutQuery = QueryWrapper.create()
                .from(CheckOutRecord.class)
                .where(CHECK_OUT_RECORD.CREATE_TIME.ge(startDateTime))
                .and(CHECK_OUT_RECORD.CREATE_TIME.le(endDateTime));
        List<CheckOutRecord> checkoutRecords = checkOutRecordMapper.selectListByQuery(checkoutQuery);

        BigDecimal cashTotal = BigDecimal.ZERO;
        long cashCount = 0;
        BigDecimal cardTotal = BigDecimal.ZERO;
        long cardCount = 0;
        BigDecimal mobileTotal = BigDecimal.ZERO;
        long mobileCount = 0;
        BigDecimal creditTotal = BigDecimal.ZERO;
        long creditCount = 0;

        for (CheckOutRecord record : checkoutRecords) {
            BigDecimal amount = record.getTotalAmount() != null ? record.getTotalAmount() : BigDecimal.ZERO;
            Integer method = record.getPaymentMethod();
            if (method != null) {
                switch (method) {
                    case 1:
                        cashTotal = cashTotal.add(amount);
                        cashCount++;
                        break;
                    case 2:
                        cardTotal = cardTotal.add(amount);
                        cardCount++;
                        break;
                    case 3:
                        mobileTotal = mobileTotal.add(amount);
                        mobileCount++;
                        break;
                    case 4:
                        creditTotal = creditTotal.add(amount);
                        creditCount++;
                        break;
                }
            }
        }

        QueryWrapper refundQuery = QueryWrapper.create()
                .from(RefundApply.class)
                .where(REFUND_APPLY.STATUS.eq(3))
                .and(REFUND_APPLY.REFUND_TIME.ge(startDateTime))
                .and(REFUND_APPLY.REFUND_TIME.le(endDateTime));
        List<RefundApply> refundRecords = refundApplyMapper.selectListByQuery(refundQuery);

        BigDecimal refundTotal = BigDecimal.ZERO;
        long refundCount = 0;
        for (RefundApply refund : refundRecords) {
            BigDecimal amount = refund.getApprovedAmount() != null ? refund.getApprovedAmount() : BigDecimal.ZERO;
            refundTotal = refundTotal.add(amount);
            refundCount++;
        }

        QueryWrapper creditQuery = QueryWrapper.create()
                .from(CreditBill.class)
                .where(CREDIT_BILL.BILL_TIME.ge(startDateTime))
                .and(CREDIT_BILL.BILL_TIME.le(endDateTime));
        List<CreditBill> creditBills = creditBillMapper.selectListByQuery(creditQuery);

        BigDecimal creditBillTotal = BigDecimal.ZERO;
        for (CreditBill bill : creditBills) {
            BigDecimal amount = bill.getTotalAmount() != null ? bill.getTotalAmount() : BigDecimal.ZERO;
            creditBillTotal = creditBillTotal.add(amount);
        }

        BigDecimal grandTotal = cashTotal.add(cardTotal).add(mobileTotal).add(creditTotal).subtract(refundTotal);
        long totalCount = cashCount + cardCount + mobileCount + creditCount;

        Map<String, Object> result = new HashMap<>();
        result.put("cashTotal", cashTotal);
        result.put("cashCount", cashCount);
        result.put("cardTotal", cardTotal);
        result.put("cardCount", cardCount);
        result.put("mobileTotal", mobileTotal);
        result.put("mobileCount", mobileCount);
        result.put("creditTotal", creditTotal.add(creditBillTotal));
        result.put("creditCount", creditCount + creditBills.size());
        result.put("refundTotal", refundTotal);
        result.put("refundCount", refundCount);
        result.put("grandTotal", grandTotal);
        result.put("totalCount", totalCount);

        List<Map<String, Object>> paymentDetails = new ArrayList<>();
        Map<String, Object> cashDetail = new HashMap<>();
        cashDetail.put("method", "现金");
        cashDetail.put("amount", cashTotal);
        cashDetail.put("count", cashCount);
        paymentDetails.add(cashDetail);

        Map<String, Object> cardDetail = new HashMap<>();
        cardDetail.put("method", "刷卡");
        cardDetail.put("amount", cardTotal);
        cardDetail.put("count", cardCount);
        paymentDetails.add(cardDetail);

        Map<String, Object> mobileDetail = new HashMap<>();
        mobileDetail.put("method", "移动支付");
        mobileDetail.put("amount", mobileTotal);
        mobileDetail.put("count", mobileCount);
        paymentDetails.add(mobileDetail);

        Map<String, Object> creditDetail = new HashMap<>();
        creditDetail.put("method", "挂账");
        creditDetail.put("amount", creditTotal.add(creditBillTotal));
        creditDetail.put("count", creditCount + creditBills.size());
        paymentDetails.add(creditDetail);

        result.put("paymentDetails", paymentDetails);
        return result;
    }

    public List<Map<String, Object>> getReceivableMonitor() {
        QueryWrapper unitQuery = QueryWrapper.create()
                .from(AgreementUnit.class)
                .where(AGREEMENT_UNIT.DELETED.eq(0))
                .and(AGREEMENT_UNIT.CURRENT_DEBT.gt(BigDecimal.ZERO));
        List<AgreementUnit> units = agreementUnitMapper.selectListByQuery(unitQuery);

        List<Map<String, Object>> result = new ArrayList<>();
        for (AgreementUnit unit : units) {
            Map<String, Object> item = new HashMap<>();
            item.put("unitName", unit.getUnitName());
            item.put("currentDebt", unit.getCurrentDebt());
            item.put("creditLimit", unit.getCreditLimit());

            BigDecimal creditLimit = unit.getCreditLimit() != null ? unit.getCreditLimit() : BigDecimal.ZERO;
            BigDecimal currentDebt = unit.getCurrentDebt() != null ? unit.getCurrentDebt() : BigDecimal.ZERO;
            BigDecimal usageRate = creditLimit.compareTo(BigDecimal.ZERO) > 0
                    ? currentDebt.multiply(new BigDecimal("100")).divide(creditLimit, 2, RoundingMode.HALF_UP)
                    : BigDecimal.ZERO;
            item.put("usageRate", usageRate);

            QueryWrapper billQuery = QueryWrapper.create()
                    .from(CreditBill.class)
                    .where(CREDIT_BILL.AGREEMENT_UNIT_ID.eq(unit.getId()))
                    .and(CREDIT_BILL.STATUS.eq(0))
                    .orderBy(CREDIT_BILL.BILL_TIME.asc())
                    .limit(1);
            List<CreditBill> earliestBills = creditBillMapper.selectListByQuery(billQuery);
            LocalDateTime earliestDebtTime = null;
            if (!earliestBills.isEmpty()) {
                earliestDebtTime = earliestBills.get(0).getBillTime();
            }
            item.put("earliestDebtTime", earliestDebtTime);

            int warningLevel = 0;
            if (usageRate.compareTo(new BigDecimal("95")) >= 0) {
                warningLevel = 2;
            } else if (usageRate.compareTo(new BigDecimal("80")) >= 0) {
                warningLevel = 1;
            }
            if (earliestDebtTime != null) {
                long daysOverdue = ChronoUnit.DAYS.between(earliestDebtTime, LocalDateTime.now());
                if (daysOverdue > 30) {
                    warningLevel = 3;
                }
            }
            item.put("warningLevel", warningLevel);

            result.add(item);
        }
        return result;
    }
}
