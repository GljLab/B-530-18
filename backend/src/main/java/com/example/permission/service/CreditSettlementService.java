package com.example.permission.service;

import com.example.permission.common.BusinessException;
import com.example.permission.common.PageResult;
import com.example.permission.entity.AgreementUnit;
import com.example.permission.entity.CreditBill;
import com.example.permission.entity.CreditSettlement;
import com.example.permission.mapper.AgreementUnitMapper;
import com.example.permission.mapper.CreditBillMapper;
import com.example.permission.mapper.CreditSettlementMapper;
import com.example.permission.security.LoginUser;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Random;

import static com.example.permission.entity.table.CreditBillTableDef.CREDIT_BILL;
import static com.example.permission.entity.table.CreditSettlementTableDef.CREDIT_SETTLEMENT;

@Service
public class CreditSettlementService {

    @Autowired
    private CreditSettlementMapper creditSettlementMapper;

    @Autowired
    private CreditBillMapper creditBillMapper;

    @Autowired
    private AgreementUnitMapper agreementUnitMapper;

    @Autowired
    private CreditBillService creditBillService;

    public PageResult<CreditSettlement> getPage(Integer pageNum, Integer pageSize, Long agreementUnitId, Integer status) {
        QueryWrapper query = QueryWrapper.create()
                .from(CreditSettlement.class)
                .where(CREDIT_SETTLEMENT.ID.isNotNull());
        if (agreementUnitId != null) {
            query.and(CREDIT_SETTLEMENT.AGREEMENT_UNIT_ID.eq(agreementUnitId));
        }
        if (status != null) {
            query.and(CREDIT_SETTLEMENT.STATUS.eq(status));
        }
        query.orderBy(CREDIT_SETTLEMENT.CREATE_TIME.desc());
        Page<CreditSettlement> page = creditSettlementMapper.paginate(Page.of(pageNum, pageSize), query);
        return new PageResult<>(page.getTotalRow(), page.getRecords(),
                (long) page.getPageNumber(), (long) page.getPageSize());
    }

    public CreditSettlement getById(Long id) {
        CreditSettlement settlement = creditSettlementMapper.selectOneById(id);
        if (settlement == null) {
            throw new BusinessException("结算单不存在");
        }
        return settlement;
    }

    @Transactional(rollbackFor = Exception.class)
    public void createSettlement(CreditSettlement settlement, List<Long> billIds, LoginUser loginUser) {
        String settlementNo = generateSettlementNo();
        settlement.setSettlementNo(settlementNo);
        settlement.setOperatorId(loginUser.getUserId());
        settlement.setOperatorName(loginUser.getUser() != null && loginUser.getUser().getNickname() != null ? loginUser.getUser().getNickname() : loginUser.getUsername());
        settlement.setStatus(0);
        settlement.setBillCount(billIds.size());
        settlement.setCreateTime(LocalDateTime.now());

        BigDecimal totalAmount = BigDecimal.ZERO;
        for (Long billId : billIds) {
            CreditBill bill = creditBillMapper.selectOneById(billId);
            if (bill != null && bill.getTotalAmount() != null) {
                totalAmount = totalAmount.add(bill.getTotalAmount());
            }
        }
        settlement.setTotalAmount(totalAmount);

        creditSettlementMapper.insert(settlement);

        for (Long billId : billIds) {
            creditBillService.settleBill(billId, settlement.getId());
        }

        AgreementUnit unit = agreementUnitMapper.selectOneById(settlement.getAgreementUnitId());
        if (unit != null) {
            BigDecimal currentDebt = unit.getCurrentDebt() != null ? unit.getCurrentDebt() : BigDecimal.ZERO;
            unit.setCurrentDebt(currentDebt.subtract(totalAmount));
            unit.setUpdateTime(LocalDateTime.now());
            agreementUnitMapper.update(unit);
        }
    }

    public void confirmSettlement(Long id) {
        CreditSettlement settlement = getById(id);
        settlement.setStatus(1);
        settlement.setUpdateTime(LocalDateTime.now());
        creditSettlementMapper.update(settlement);
    }

    public String generateSettlementNo() {
        String prefix = "CS" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        Random random = new Random();
        String suffix = String.format("%04d", random.nextInt(10000));
        String settlementNo = prefix + suffix;
        QueryWrapper checkQuery = QueryWrapper.create()
                .from(CreditSettlement.class)
                .where(CREDIT_SETTLEMENT.SETTLEMENT_NO.eq(settlementNo));
        long count = creditSettlementMapper.selectCountByQuery(checkQuery);
        if (count > 0) {
            return generateSettlementNo();
        }
        return settlementNo;
    }
}
