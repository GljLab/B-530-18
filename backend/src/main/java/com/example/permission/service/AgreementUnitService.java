package com.example.permission.service;

import com.example.permission.common.BusinessException;
import com.example.permission.common.PageResult;
import com.example.permission.entity.AgreementUnit;
import com.example.permission.mapper.AgreementUnitMapper;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static com.example.permission.entity.table.AgreementUnitTableDef.AGREEMENT_UNIT;

@Service
public class AgreementUnitService {

    @Autowired
    private AgreementUnitMapper agreementUnitMapper;

    public PageResult<AgreementUnit> getPage(Integer pageNum, Integer pageSize, String unitName, Integer status, Integer cooperationType) {
        QueryWrapper query = QueryWrapper.create()
                .from(AgreementUnit.class)
                .where(AGREEMENT_UNIT.DELETED.eq(0));
        if (StringUtils.hasText(unitName)) {
            query.and(AGREEMENT_UNIT.UNIT_NAME.like(unitName));
        }
        if (status != null) {
            query.and(AGREEMENT_UNIT.STATUS.eq(status));
        }
        if (cooperationType != null) {
            query.and(AGREEMENT_UNIT.COOPERATION_TYPE.eq(cooperationType));
        }
        query.orderBy(AGREEMENT_UNIT.CREATE_TIME.desc());
        Page<AgreementUnit> page = agreementUnitMapper.paginate(Page.of(pageNum, pageSize), query);
        return new PageResult<>(page.getTotalRow(), page.getRecords(),
                (long) page.getPageNumber(), (long) page.getPageSize());
    }

    public AgreementUnit getById(Long id) {
        AgreementUnit unit = agreementUnitMapper.selectOneById(id);
        if (unit == null) {
            throw new BusinessException("协议单位不存在");
        }
        return unit;
    }

    public void add(AgreementUnit unit) {
        if (unit.getCreditLimit() == null || unit.getCreditLimit().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("信用额度必须大于0");
        }
        unit.setCreateTime(LocalDateTime.now());
        unit.setUpdateTime(LocalDateTime.now());
        if (unit.getCurrentDebt() == null) {
            unit.setCurrentDebt(BigDecimal.ZERO);
        }
        if (unit.getStatus() == null) {
            unit.setStatus(1);
        }
        agreementUnitMapper.insert(unit);
    }

    public void update(AgreementUnit unit) {
        if (unit.getCreditLimit() == null || unit.getCreditLimit().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("信用额度必须大于0");
        }
        unit.setUpdateTime(LocalDateTime.now());
        agreementUnitMapper.update(unit);
    }

    public void delete(Long id) {
        AgreementUnit unit = getById(id);
        unit.setDeleted(1);
        unit.setUpdateTime(LocalDateTime.now());
        agreementUnitMapper.update(unit);
    }

    public void updateStatus(Long id, Integer status) {
        AgreementUnit unit = getById(id);
        unit.setStatus(status);
        unit.setUpdateTime(LocalDateTime.now());
        agreementUnitMapper.update(unit);
    }

    public List<AgreementUnit> listAll() {
        QueryWrapper query = QueryWrapper.create()
                .from(AgreementUnit.class)
                .where(AGREEMENT_UNIT.DELETED.eq(0))
                .and(AGREEMENT_UNIT.STATUS.eq(1))
                .orderBy(AGREEMENT_UNIT.CREATE_TIME.desc());
        return agreementUnitMapper.selectListByQuery(query);
    }

    public boolean checkCreditLimit(Long unitId, BigDecimal amount) {
        AgreementUnit unit = getById(unitId);
        BigDecimal total = unit.getCurrentDebt().add(amount);
        return total.compareTo(unit.getCreditLimit()) <= 0;
    }
}
