package com.example.permission.service;

import com.example.permission.entity.CustomerOperationLog;
import com.example.permission.entity.CustomerPreference;
import com.example.permission.mapper.CustomerOperationLogMapper;
import com.example.permission.mapper.CustomerPreferenceMapper;
import com.mybatisflex.core.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static com.example.permission.entity.table.CustomerPreferenceTableDef.CUSTOMER_PREFERENCE;

@Service
public class CustomerPreferenceService {

    @Autowired
    private CustomerPreferenceMapper customerPreferenceMapper;

    @Autowired
    private CustomerOperationLogMapper customerOperationLogMapper;

    public CustomerPreference getByCustomerId(Long customerId) {
        QueryWrapper query = QueryWrapper.create()
                .from(CustomerPreference.class)
                .where(CUSTOMER_PREFERENCE.CUSTOMER_ID.eq(customerId));
        return customerPreferenceMapper.selectOneByQuery(query);
    }

    @Transactional
    public CustomerPreference saveOrUpdate(CustomerPreference pref, Long operatorId, String operatorName) {
        CustomerPreference existing = getByCustomerId(pref.getCustomerId());
        if (existing != null) {
            pref.setId(existing.getId());
            pref.setUpdateTime(LocalDateTime.now());
            customerPreferenceMapper.update(pref);
        } else {
            pref.setCreateTime(LocalDateTime.now());
            customerPreferenceMapper.insert(pref);
        }

        saveOperationLog(pref.getCustomerId(), null, 7, operatorId, operatorName, "修改客户偏好");
        return pref;
    }

    private void saveOperationLog(Long customerId, String customerName, Integer operationType,
                                   Long operatorId, String operatorName, String remark) {
        CustomerOperationLog log = new CustomerOperationLog();
        log.setCustomerId(customerId);
        log.setCustomerName(customerName);
        log.setOperationType(operationType);
        log.setOperatorId(operatorId);
        log.setOperatorName(operatorName);
        log.setRemark(remark);
        customerOperationLogMapper.insert(log);
    }
}
