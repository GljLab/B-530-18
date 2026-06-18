package com.example.permission.service;

import com.example.permission.common.BusinessException;
import com.example.permission.common.PageResult;
import com.example.permission.entity.DailyReconciliation;
import com.example.permission.mapper.CheckOutRecordMapper;
import com.example.permission.mapper.ConsumptionRecordMapper;
import com.example.permission.mapper.DailyReconciliationMapper;
import com.example.permission.security.LoginUser;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static com.example.permission.entity.table.DailyReconciliationTableDef.DAILY_RECONCILIATION;

@Service
public class DailyReconciliationService {

    @Autowired
    private DailyReconciliationMapper dailyReconciliationMapper;

    @Autowired
    private CheckOutRecordMapper checkOutRecordMapper;

    @Autowired
    private ConsumptionRecordMapper consumptionRecordMapper;

    public PageResult<DailyReconciliation> getPage(Integer pageNum, Integer pageSize, Integer status, String startDate, String endDate) {
        QueryWrapper query = QueryWrapper.create()
                .from(DailyReconciliation.class)
                .where(DAILY_RECONCILIATION.ID.isNotNull());
        if (status != null) {
            query.and(DAILY_RECONCILIATION.STATUS.eq(status));
        }
        if (StringUtils.hasText(startDate)) {
            query.and(DAILY_RECONCILIATION.RECONCILE_DATE.ge(LocalDate.parse(startDate)));
        }
        if (StringUtils.hasText(endDate)) {
            query.and(DAILY_RECONCILIATION.RECONCILE_DATE.le(LocalDate.parse(endDate)));
        }
        query.orderBy(DAILY_RECONCILIATION.RECONCILE_DATE.desc());
        Page<DailyReconciliation> page = dailyReconciliationMapper.paginate(Page.of(pageNum, pageSize), query);
        return new PageResult<>(page.getTotalRow(), page.getRecords(),
                (long) page.getPageNumber(), (long) page.getPageSize());
    }

    public DailyReconciliation getByDate(String date) {
        QueryWrapper query = QueryWrapper.create()
                .from(DailyReconciliation.class)
                .where(DAILY_RECONCILIATION.RECONCILE_DATE.eq(LocalDate.parse(date)));
        return dailyReconciliationMapper.selectOneByQuery(query);
    }

    @Transactional(rollbackFor = Exception.class)
    public void autoCalculate(String date, LoginUser loginUser) {
        DailyReconciliation existing = getByDate(date);
        if (existing != null) {
            throw new BusinessException("该日期已存在日结记录");
        }
        DailyReconciliation record = new DailyReconciliation();
        record.setReconcileDate(LocalDate.parse(date));
        record.setCashRoom(BigDecimal.ZERO);
        record.setCashDeposit(BigDecimal.ZERO);
        record.setCashOther(BigDecimal.ZERO);
        record.setCashTotal(BigDecimal.ZERO);
        record.setCardRoom(BigDecimal.ZERO);
        record.setCardDeposit(BigDecimal.ZERO);
        record.setCardOther(BigDecimal.ZERO);
        record.setCardTotal(BigDecimal.ZERO);
        record.setMobileAlipay(BigDecimal.ZERO);
        record.setMobileWechat(BigDecimal.ZERO);
        record.setMobileTotal(BigDecimal.ZERO);
        record.setCreditAmount(BigDecimal.ZERO);
        record.setPrepaidAmount(BigDecimal.ZERO);
        record.setRefundAmount(BigDecimal.ZERO);
        record.setReceivableTotal(record.getCashTotal().add(record.getCardTotal())
                .add(record.getMobileTotal()).add(record.getCreditAmount())
                .add(record.getPrepaidAmount()).subtract(record.getRefundAmount()));
        record.setActualCash(BigDecimal.ZERO);
        record.setActualCard(BigDecimal.ZERO);
        record.setActualMobile(BigDecimal.ZERO);
        record.setActualTotal(BigDecimal.ZERO);
        record.setDifference(BigDecimal.ZERO);
        record.setOperatorId(loginUser.getUserId());
        record.setOperatorName(loginUser.getUser().getNickname() != null ? loginUser.getUser().getNickname() : loginUser.getUsername());
        record.setStatus(0);
        record.setCreateTime(LocalDateTime.now());
        record.setUpdateTime(LocalDateTime.now());
        dailyReconciliationMapper.insert(record);
    }

    @Transactional(rollbackFor = Exception.class)
    public void confirmDaily(Long id, DailyReconciliation data) {
        DailyReconciliation record = dailyReconciliationMapper.selectOneById(id);
        if (record == null) {
            throw new BusinessException("日结记录不存在");
        }
        if (record.getStatus() != 0) {
            throw new BusinessException("该日结记录已确认，不可重复操作");
        }
        record.setActualCash(data.getActualCash());
        record.setActualCard(data.getActualCard());
        record.setActualMobile(data.getActualMobile());
        record.setActualTotal(data.getActualTotal());
        record.setDifference(data.getActualTotal().subtract(record.getReceivableTotal()));
        record.setDifferenceReason(data.getDifferenceReason());
        record.setDifferenceProof(data.getDifferenceProof());
        record.setStatus(1);
        record.setUpdateTime(LocalDateTime.now());
        dailyReconciliationMapper.update(record);
    }
}
