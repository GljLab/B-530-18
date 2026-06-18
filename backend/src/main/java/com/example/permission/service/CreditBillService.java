package com.example.permission.service;

import com.example.permission.common.BusinessException;
import com.example.permission.common.PageResult;
import com.example.permission.entity.AgreementUnit;
import com.example.permission.entity.CreditBill;
import com.example.permission.mapper.AgreementUnitMapper;
import com.example.permission.mapper.CreditBillMapper;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Random;

import static com.example.permission.entity.table.CreditBillTableDef.CREDIT_BILL;

@Service
public class CreditBillService {

    @Autowired
    private CreditBillMapper creditBillMapper;

    @Autowired
    private AgreementUnitMapper agreementUnitMapper;

    @Autowired
    private AgreementUnitService agreementUnitService;

    public PageResult<CreditBill> getPage(Integer pageNum, Integer pageSize, Long agreementUnitId, Integer status, String billNo) {
        QueryWrapper query = QueryWrapper.create()
                .from(CreditBill.class)
                .where(CREDIT_BILL.ID.isNotNull());
        if (agreementUnitId != null) {
            query.and(CREDIT_BILL.AGREEMENT_UNIT_ID.eq(agreementUnitId));
        }
        if (status != null) {
            query.and(CREDIT_BILL.STATUS.eq(status));
        }
        if (StringUtils.hasText(billNo)) {
            query.and(CREDIT_BILL.BILL_NO.like(billNo));
        }
        query.orderBy(CREDIT_BILL.CREATE_TIME.desc());
        Page<CreditBill> page = creditBillMapper.paginate(Page.of(pageNum, pageSize), query);
        return new PageResult<>(page.getTotalRow(), page.getRecords(),
                (long) page.getPageNumber(), (long) page.getPageSize());
    }

    public CreditBill getById(Long id) {
        CreditBill bill = creditBillMapper.selectOneById(id);
        if (bill == null) {
            throw new BusinessException("信用账单不存在");
        }
        return bill;
    }

    public List<CreditBill> getBillsByUnitAndPeriod(Long agreementUnitId, String periodStart, String periodEnd) {
        QueryWrapper query = QueryWrapper.create()
                .from(CreditBill.class)
                .where(CREDIT_BILL.AGREEMENT_UNIT_ID.eq(agreementUnitId))
                .and(CREDIT_BILL.STATUS.eq(0))
                .and(CREDIT_BILL.BILL_TIME.ge(periodStart))
                .and(CREDIT_BILL.BILL_TIME.le(periodEnd))
                .orderBy(CREDIT_BILL.BILL_TIME.asc());
        return creditBillMapper.selectListByQuery(query);
    }

    @Transactional(rollbackFor = Exception.class)
    public void createCreditBill(CreditBill bill) {
        AgreementUnit unit = agreementUnitService.getById(bill.getAgreementUnitId());
        if (unit.getStatus() != 1) {
            throw new BusinessException("协议单位未启用，无法创建信用账单");
        }
        BigDecimal totalAmount = bill.getTotalAmount() != null ? bill.getTotalAmount() : BigDecimal.ZERO;
        if (!agreementUnitService.checkCreditLimit(bill.getAgreementUnitId(), totalAmount)) {
            throw new BusinessException("超出协议单位信用额度");
        }
        bill.setBillNo(generateBillNo());
        bill.setBillTime(LocalDateTime.now());
        bill.setStatus(0);
        bill.setCreateTime(LocalDateTime.now());
        bill.setUpdateTime(LocalDateTime.now());
        creditBillMapper.insert(bill);
        unit.setCurrentDebt(unit.getCurrentDebt().add(totalAmount));
        unit.setUpdateTime(LocalDateTime.now());
        agreementUnitMapper.update(unit);
    }

    public void settleBill(Long billId, Long settlementId) {
        CreditBill bill = getById(billId);
        bill.setStatus(1);
        bill.setSettlementId(settlementId);
        bill.setUpdateTime(LocalDateTime.now());
        creditBillMapper.update(bill);
    }

    public String generateBillNo() {
        String prefix = "CB" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        Random random = new Random();
        String suffix = String.format("%04d", random.nextInt(10000));
        String billNo = prefix + suffix;
        QueryWrapper checkQuery = QueryWrapper.create()
                .from(CreditBill.class)
                .where(CREDIT_BILL.BILL_NO.eq(billNo));
        long count = creditBillMapper.selectCountByQuery(checkQuery);
        if (count > 0) {
            return generateBillNo();
        }
        return billNo;
    }
}
