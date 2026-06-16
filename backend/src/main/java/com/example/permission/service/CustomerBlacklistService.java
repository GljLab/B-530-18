package com.example.permission.service;

import com.example.permission.common.PageResult;
import com.example.permission.entity.Customer;
import com.example.permission.entity.CustomerBlacklist;
import com.example.permission.entity.CustomerOperationLog;
import com.example.permission.mapper.CustomerBlacklistMapper;
import com.example.permission.mapper.CustomerMapper;
import com.example.permission.mapper.CustomerOperationLogMapper;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.example.permission.entity.table.CustomerBlacklistTableDef.CUSTOMER_BLACKLIST;
import static com.example.permission.entity.table.CustomerTableDef.CUSTOMER;

@Service
public class CustomerBlacklistService {

    @Autowired
    private CustomerBlacklistMapper customerBlacklistMapper;

    @Autowired
    private CustomerMapper customerMapper;

    @Autowired
    private CustomerOperationLogMapper customerOperationLogMapper;

    @Transactional
    public CustomerBlacklist submit(CustomerBlacklist bl, Long applicantId, String applicantName) {
        QueryWrapper existingQuery = QueryWrapper.create()
                .from(CustomerBlacklist.class)
                .where(CUSTOMER_BLACKLIST.CUSTOMER_ID.eq(bl.getCustomerId()))
                .and(CUSTOMER_BLACKLIST.STATUS.in(1, 2, 5));
        Long existingCount = customerBlacklistMapper.selectCountByQuery(existingQuery);
        if (existingCount != null && existingCount > 0) {
            throw new com.example.permission.common.BusinessException("该客户已在黑名单中或有正在处理的申请，请勿重复提交");
        }

        bl.setStatus(1);
        bl.setApplicantId(applicantId);
        bl.setApplicantName(applicantName);
        bl.setApplyTime(LocalDateTime.now());
        customerBlacklistMapper.insert(bl);
        return bl;
    }

    @Transactional
    public void approve(Long id, Long approverId, String approverName, String opinion) {
        CustomerBlacklist bl = customerBlacklistMapper.selectOneById(id);
        if (bl == null) {
            return;
        }
        bl.setStatus(2);
        bl.setApproverId(approverId);
        bl.setApproverName(approverName);
        bl.setApproveTime(LocalDateTime.now());
        bl.setApproveOpinion(opinion);
        bl.setUpdateTime(LocalDateTime.now());
        customerBlacklistMapper.update(bl);

        Customer customer = customerMapper.selectOneById(bl.getCustomerId());
        if (customer != null) {
            customer.setStatus(3);
            customer.setUpdateTime(LocalDateTime.now());
            customerMapper.update(customer);
        }

        saveOperationLog(bl.getCustomerId(), 9, approverId, approverName, "审批通过加入黑名单");
    }

    @Transactional
    public void reject(Long id, Long approverId, String approverName, String reason) {
        CustomerBlacklist bl = customerBlacklistMapper.selectOneById(id);
        if (bl == null) {
            return;
        }
        bl.setStatus(3);
        bl.setApproverId(approverId);
        bl.setApproverName(approverName);
        bl.setApproveTime(LocalDateTime.now());
        bl.setRejectReason(reason);
        bl.setUpdateTime(LocalDateTime.now());
        customerBlacklistMapper.update(bl);

        saveOperationLog(bl.getCustomerId(), 9, approverId, approverName, "审批拒绝加入黑名单：" + reason);
    }

    @Transactional
    public void submitRemove(Long id, Long applicantId, String applicantName, String reason) {
        CustomerBlacklist bl = customerBlacklistMapper.selectOneById(id);
        if (bl == null) {
            return;
        }
        bl.setStatus(5);
        bl.setRemoveApplicantId(applicantId);
        bl.setRemoveApplicantName(applicantName);
        bl.setRemoveApplyTime(LocalDateTime.now());
        bl.setRemoveReason(reason);
        bl.setUpdateTime(LocalDateTime.now());
        customerBlacklistMapper.update(bl);
    }

    @Transactional
    public void approveRemove(Long id, Long approverId, String approverName, String opinion) {
        CustomerBlacklist bl = customerBlacklistMapper.selectOneById(id);
        if (bl == null) {
            return;
        }
        bl.setStatus(4);
        bl.setRemoveApproverId(approverId);
        bl.setRemoveApproverName(approverName);
        bl.setRemoveApproveTime(LocalDateTime.now());
        bl.setRemoveApproveOpinion(opinion);
        bl.setUpdateTime(LocalDateTime.now());
        customerBlacklistMapper.update(bl);

        Customer customer = customerMapper.selectOneById(bl.getCustomerId());
        if (customer != null) {
            customer.setStatus(1);
            customer.setUpdateTime(LocalDateTime.now());
            customerMapper.update(customer);
        }

        saveOperationLog(bl.getCustomerId(), 10, approverId, approverName, "审批通过解除黑名单");
    }

    public PageResult<CustomerBlacklist> pageList(Integer pageNum, Integer pageSize, Integer reason, Integer blacklistType, Integer status) {
        QueryWrapper query = QueryWrapper.create()
                .from(CustomerBlacklist.class)
                .where(CUSTOMER_BLACKLIST.ID.isNotNull());

        if (reason != null) {
            query.and(CUSTOMER_BLACKLIST.REASON.eq(reason));
        }
        if (blacklistType != null) {
            query.and(CUSTOMER_BLACKLIST.BLACKLIST_TYPE.eq(blacklistType));
        }
        if (status != null) {
            query.and(CUSTOMER_BLACKLIST.STATUS.eq(status));
        }

        query.orderBy(CUSTOMER_BLACKLIST.CREATE_TIME.desc());

        Page<CustomerBlacklist> page = customerBlacklistMapper.paginate(Page.of(pageNum, pageSize), query);
        for (CustomerBlacklist bl : page.getRecords()) {
            fillCustomerInfo(bl);
        }

        return new PageResult<>(page.getTotalRow(), page.getRecords(),
                (long) page.getPageNumber(), (long) page.getPageSize());
    }

    public CustomerBlacklist getByCustomerId(Long customerId) {
        QueryWrapper query = QueryWrapper.create()
                .from(CustomerBlacklist.class)
                .where(CUSTOMER_BLACKLIST.CUSTOMER_ID.eq(customerId))
                .and(CUSTOMER_BLACKLIST.STATUS.eq(2));
        return customerBlacklistMapper.selectOneByQuery(query);
    }

    public Map<String, Object> checkBlacklist(Long customerId) {
        CustomerBlacklist bl = getByCustomerId(customerId);
        if (bl == null) {
            Map<String, Object> result = new HashMap<>();
            result.put("blacklisted", false);
            return result;
        }
        Map<String, Object> result = new HashMap<>();
        result.put("blacklisted", true);
        result.put("reason", bl.getReason());
        result.put("detailDescription", bl.getDetailDescription());
        result.put("blacklistType", bl.getBlacklistType());
        return result;
    }

    public List<CustomerBlacklist> listPending() {
        QueryWrapper query = QueryWrapper.create()
                .from(CustomerBlacklist.class)
                .where(CUSTOMER_BLACKLIST.STATUS.eq(1))
                .orderBy(CUSTOMER_BLACKLIST.APPLY_TIME.desc());
        List<CustomerBlacklist> list = customerBlacklistMapper.selectListByQuery(query);
        for (CustomerBlacklist bl : list) {
            fillCustomerInfo(bl);
        }
        return list;
    }

    public List<CustomerBlacklist> listPendingRemove() {
        QueryWrapper query = QueryWrapper.create()
                .from(CustomerBlacklist.class)
                .where(CUSTOMER_BLACKLIST.STATUS.eq(5))
                .orderBy(CUSTOMER_BLACKLIST.REMOVE_APPLY_TIME.desc());
        List<CustomerBlacklist> list = customerBlacklistMapper.selectListByQuery(query);
        for (CustomerBlacklist bl : list) {
            fillCustomerInfo(bl);
        }
        return list;
    }

    public CustomerBlacklist getById(Long id) {
        CustomerBlacklist bl = customerBlacklistMapper.selectOneById(id);
        if (bl != null) {
            fillCustomerInfo(bl);
        }
        return bl;
    }

    @Transactional
    public void checkExpired() {
        QueryWrapper query = QueryWrapper.create()
                .from(CustomerBlacklist.class)
                .where(CUSTOMER_BLACKLIST.BLACKLIST_TYPE.eq(1))
                .and(CUSTOMER_BLACKLIST.EXPIRE_TIME.lt(LocalDateTime.now()))
                .and(CUSTOMER_BLACKLIST.STATUS.eq(2));
        List<CustomerBlacklist> expired = customerBlacklistMapper.selectListByQuery(query);
        for (CustomerBlacklist bl : expired) {
            bl.setStatus(5);
            bl.setUpdateTime(LocalDateTime.now());
            customerBlacklistMapper.update(bl);
        }
    }

    public List<CustomerBlacklist> exportBlacklist(Integer reason, Integer blacklistType, Integer status) {
        QueryWrapper query = QueryWrapper.create()
                .from(CustomerBlacklist.class)
                .where(CUSTOMER_BLACKLIST.ID.isNotNull());

        if (reason != null) {
            query.and(CUSTOMER_BLACKLIST.REASON.eq(reason));
        }
        if (blacklistType != null) {
            query.and(CUSTOMER_BLACKLIST.BLACKLIST_TYPE.eq(blacklistType));
        }
        if (status != null) {
            query.and(CUSTOMER_BLACKLIST.STATUS.eq(status));
        }

        query.orderBy(CUSTOMER_BLACKLIST.CREATE_TIME.desc());
        List<CustomerBlacklist> list = customerBlacklistMapper.selectListByQuery(query);
        for (CustomerBlacklist bl : list) {
            fillCustomerInfo(bl);
        }
        return list;
    }

    private void fillCustomerInfo(CustomerBlacklist bl) {
        Customer customer = customerMapper.selectOneById(bl.getCustomerId());
        if (customer != null) {
            bl.setCustomerName(customer.getName());
            bl.setCustomerPhone(customer.getPhone());
            bl.setCustomerIdNumber(customer.getIdNumber());
        }
    }

    private void saveOperationLog(Long customerId, Integer operationType, Long operatorId, String operatorName, String remark) {
        CustomerOperationLog log = new CustomerOperationLog();
        log.setCustomerId(customerId);
        log.setOperationType(operationType);
        log.setOperatorId(operatorId);
        log.setOperatorName(operatorName);
        log.setRemark(remark);
        customerOperationLogMapper.insert(log);
    }
}
