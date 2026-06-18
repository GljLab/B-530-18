package com.example.permission.service;

import com.example.permission.common.BusinessException;
import com.example.permission.common.PageResult;
import com.example.permission.entity.ShiftReconciliation;
import com.example.permission.mapper.ShiftReconciliationMapper;
import com.example.permission.security.LoginUser;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static com.example.permission.entity.table.ShiftReconciliationTableDef.SHIFT_RECONCILIATION;

@Service
public class ShiftReconciliationService {

    @Autowired
    private ShiftReconciliationMapper shiftReconciliationMapper;

    public PageResult<ShiftReconciliation> getPage(Integer pageNum, Integer pageSize, Integer status, String startDate, String endDate) {
        QueryWrapper query = QueryWrapper.create()
                .from(ShiftReconciliation.class)
                .where(SHIFT_RECONCILIATION.ID.isNotNull());
        if (status != null) {
            query.and(SHIFT_RECONCILIATION.STATUS.eq(status));
        }
        if (StringUtils.hasText(startDate)) {
            query.and(SHIFT_RECONCILIATION.SHIFT_DATE.ge(LocalDate.parse(startDate)));
        }
        if (StringUtils.hasText(endDate)) {
            query.and(SHIFT_RECONCILIATION.SHIFT_DATE.le(LocalDate.parse(endDate)));
        }
        query.orderBy(SHIFT_RECONCILIATION.SHIFT_DATE.desc());
        Page<ShiftReconciliation> page = shiftReconciliationMapper.paginate(Page.of(pageNum, pageSize), query);
        return new PageResult<>(page.getTotalRow(), page.getRecords(),
                (long) page.getPageNumber(), (long) page.getPageSize());
    }

    public ShiftReconciliation getById(Long id) {
        ShiftReconciliation record = shiftReconciliationMapper.selectOneById(id);
        if (record == null) {
            throw new BusinessException("交班记录不存在");
        }
        return record;
    }

    @Transactional(rollbackFor = Exception.class)
    public void createShift(ShiftReconciliation shift, LoginUser loginUser) {
        shift.setHandoverUserId(loginUser.getUserId());
        shift.setHandoverUserName(loginUser.getUser().getNickname() != null ? loginUser.getUser().getNickname() : loginUser.getUsername());
        shift.setStatus(0);
        shift.setTakeoverConfirmed(0);
        shift.setReceivableTotal(shift.getCashTotal().add(shift.getCardTotal())
                .add(shift.getMobileTotal()).add(shift.getCreditTotal()));
        shift.setDifference(shift.getActualTotal().subtract(shift.getReceivableTotal()));
        shift.setCreateTime(LocalDateTime.now());
        shift.setUpdateTime(LocalDateTime.now());
        shiftReconciliationMapper.insert(shift);
    }

    @Transactional(rollbackFor = Exception.class)
    public void confirmTakeover(Long id, Long takeoverUserId, String takeoverUserName, String takeoverRemark) {
        ShiftReconciliation record = shiftReconciliationMapper.selectOneById(id);
        if (record == null) {
            throw new BusinessException("交班记录不存在");
        }
        if (record.getStatus() != 0) {
            throw new BusinessException("该交班记录已确认，不可重复操作");
        }
        record.setTakeoverUserId(takeoverUserId);
        record.setTakeoverUserName(takeoverUserName);
        record.setTakeoverRemark(takeoverRemark);
        record.setTakeoverConfirmed(1);
        record.setTakeoverConfirmTime(LocalDateTime.now());
        record.setStatus(1);
        record.setUpdateTime(LocalDateTime.now());
        shiftReconciliationMapper.update(record);
    }
}
