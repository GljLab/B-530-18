package com.example.permission.service;

import com.example.permission.common.BusinessException;
import com.example.permission.common.PageResult;
import com.example.permission.entity.Customer;
import com.example.permission.entity.CustomerAddress;
import com.example.permission.entity.CustomerOperationLog;
import com.example.permission.entity.CustomerTag;
import com.example.permission.entity.CustomerTagRelation;
import com.example.permission.mapper.CustomerAddressMapper;
import com.example.permission.mapper.CustomerMapper;
import com.example.permission.mapper.CustomerOperationLogMapper;
import com.example.permission.mapper.CustomerTagMapper;
import com.example.permission.mapper.CustomerTagRelationMapper;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static com.example.permission.entity.table.CustomerAddressTableDef.CUSTOMER_ADDRESS;
import static com.example.permission.entity.table.CustomerOperationLogTableDef.CUSTOMER_OPERATION_LOG;
import static com.example.permission.entity.table.CustomerTableDef.CUSTOMER;

@Service
public class CustomerService {

    @Autowired
    private CustomerMapper customerMapper;

    @Autowired
    private CustomerAddressMapper customerAddressMapper;

    @Autowired
    private CustomerOperationLogMapper customerOperationLogMapper;

    @Autowired
    private CustomerTagMapper customerTagMapper;

    @Autowired
    private CustomerTagRelationMapper customerTagRelationMapper;

    public PageResult<Customer> pageList(Integer pageNum, Integer pageSize, String keyword,
                                          List<Integer> customerType, List<Integer> customerSource,
                                          List<Integer> status, List<Integer> importance,
                                          String createTimeStart, String createTimeEnd,
                                          List<Long> tagIds, String tagLogic,
                                          Long operatorId, List<String> operatorRoles) {
        QueryWrapper query = QueryWrapper.create()
                .from(Customer.class)
                .where(CUSTOMER.DELETED.eq(0));

        if (StringUtils.hasText(keyword)) {
            query.and(CUSTOMER.NAME.like(keyword)
                    .or(CUSTOMER.PHONE.like(keyword))
                    .or(CUSTOMER.ID_NUMBER.like(keyword)));
        }
        if (customerType != null && !customerType.isEmpty()) {
            query.and(CUSTOMER.CUSTOMER_TYPE.in(customerType));
        }
        if (customerSource != null && !customerSource.isEmpty()) {
            query.and(CUSTOMER.CUSTOMER_SOURCE.in(customerSource));
        }
        if (status != null && !status.isEmpty()) {
            query.and(CUSTOMER.STATUS.in(status));
        }
        if (importance != null && !importance.isEmpty()) {
            query.and(CUSTOMER.IMPORTANCE.in(importance));
        }
        if (StringUtils.hasText(createTimeStart)) {
            query.and(CUSTOMER.CREATE_TIME.ge(createTimeStart + " 00:00:00"));
        }
        if (StringUtils.hasText(createTimeEnd)) {
            query.and(CUSTOMER.CREATE_TIME.le(createTimeEnd + " 23:59:59"));
        }

        if (tagIds != null && !tagIds.isEmpty()) {
            List<Long> customerIdsByTag = getCustomerIdsByTagIds(tagIds, tagLogic);
            if (customerIdsByTag.isEmpty()) {
                customerIdsByTag.add(-1L);
            }
            query.and(CUSTOMER.ID.in(customerIdsByTag));
        }

        query.orderBy(CUSTOMER.CREATE_TIME.desc());

        Page<Customer> page = customerMapper.paginate(Page.of(pageNum, pageSize), query);
        for (Customer customer : page.getRecords()) {
            fillComputedFields(customer);
            if (!canViewFinance(operatorRoles)) {
                customer.setTotalSpent(null);
            }
        }

        return new PageResult<>(page.getTotalRow(), page.getRecords(),
                (long) page.getPageNumber(), (long) page.getPageSize());
    }

    public Customer getById(Long id, List<String> operatorRoles) {
        Customer customer = customerMapper.selectOneById(id);
        if (customer == null || customer.getDeleted() == 1) {
            throw new BusinessException("客户不存在");
        }
        fillComputedFields(customer);
        loadAddresses(customer);
        loadReferrerName(customer);
        loadTags(customer);
        if (!canViewFinance(operatorRoles)) {
            customer.setTotalSpent(null);
        }
        return customer;
    }

    public List<Customer> listByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return java.util.Collections.emptyList();
        }
        QueryWrapper query = QueryWrapper.create()
                .from(Customer.class)
                .where(CUSTOMER.DELETED.eq(0))
                .and(CUSTOMER.ID.in(ids));
        return customerMapper.selectListByQuery(query);
    }

    public List<Customer> listAll() {
        QueryWrapper query = QueryWrapper.create()
                .from(Customer.class)
                .where(CUSTOMER.DELETED.eq(0));
        return customerMapper.selectListByQuery(query);
    }

    @Transactional
    public Customer add(Customer customer, Long operatorId, String operatorName) {
        validateCustomer(customer);

        if (StringUtils.hasText(customer.getPhone())) {
            checkPhoneUnique(customer.getPhone(), null);
        }
        if (StringUtils.hasText(customer.getIdNumber())) {
            checkIdNumberUnique(customer.getIdNumber(), customer.getIdType(), null);
        }

        customer.setStatus(1);
        customer.setDeleted(0);
        customer.setTotalOrders(0);
        customer.setTotalSpent(BigDecimal.ZERO);
        customerMapper.insert(customer);

        if (customer.getAddresses() != null) {
            for (CustomerAddress addr : customer.getAddresses()) {
                addr.setCustomerId(customer.getId());
                addr.setDeleted(0);
                customerAddressMapper.insert(addr);
            }
        }

        saveOperationLog(customer.getId(), customer.getName(), 1, operatorId, operatorName, "创建客户");
        return customer;
    }

    @Transactional
    public void update(Customer customer, Long operatorId, String operatorName) {
        Customer existing = customerMapper.selectOneById(customer.getId());
        if (existing == null || existing.getDeleted() == 1) {
            throw new BusinessException("客户不存在");
        }

        if (StringUtils.hasText(customer.getPhone()) && !customer.getPhone().equals(existing.getPhone())) {
            checkPhoneUnique(customer.getPhone(), customer.getId());
        }
        if (StringUtils.hasText(customer.getIdNumber())
                && (!customer.getIdNumber().equals(existing.getIdNumber())
                    || !customer.getIdType().equals(existing.getIdType()))) {
            checkIdNumberUnique(customer.getIdNumber(), customer.getIdType(), customer.getId());
        }

        customer.setUpdateTime(LocalDateTime.now());
        customerMapper.update(customer);

        saveOperationLog(customer.getId(), customer.getName(), 2, operatorId, operatorName, "修改客户信息");
    }

    @Transactional
    public void delete(Long id, Long operatorId, String operatorName) {
        Customer customer = customerMapper.selectOneById(id);
        if (customer == null || customer.getDeleted() == 1) {
            throw new BusinessException("客户不存在");
        }
        if (customer.getTotalOrders() != null && customer.getTotalOrders() > 0) {
            throw new BusinessException("该客户有关联的订单，不能删除，只能冻结");
        }

        customer.setDeleted(1);
        customerMapper.update(customer);

        saveOperationLog(id, customer.getName(), 2, operatorId, operatorName, "删除客户");
    }

    @Transactional
    public void freeze(Long id, String reason, Long operatorId, String operatorName) {
        Customer customer = customerMapper.selectOneById(id);
        if (customer == null || customer.getDeleted() == 1) {
            throw new BusinessException("客户不存在");
        }
        if (customer.getStatus() == 2) {
            throw new BusinessException("客户已经是冻结状态");
        }
        if (!StringUtils.hasText(reason)) {
            throw new BusinessException("冻结原因不能为空");
        }

        customer.setStatus(2);
        customer.setFreezeReason(reason);
        customer.setFreezeTime(LocalDateTime.now());
        customer.setFreezeOperatorId(operatorId);
        customer.setFreezeOperatorName(operatorName);
        customerMapper.update(customer);

        saveOperationLog(id, customer.getName(), 3, operatorId, operatorName, "冻结客户：" + reason);
    }

    @Transactional
    public void unfreeze(Long id, String reason, Long operatorId, String operatorName) {
        Customer customer = customerMapper.selectOneById(id);
        if (customer == null || customer.getDeleted() == 1) {
            throw new BusinessException("客户不存在");
        }
        if (customer.getStatus() != 2) {
            throw new BusinessException("客户不是冻结状态");
        }

        customer.setStatus(1);
        customer.setUnfreezeReason(reason);
        customer.setUnfreezeTime(LocalDateTime.now());
        customer.setUnfreezeOperatorId(operatorId);
        customer.setUnfreezeOperatorName(operatorName);
        customerMapper.update(customer);

        saveOperationLog(id, customer.getName(), 4, operatorId, operatorName, "解冻客户：" + reason);
    }

    public List<CustomerOperationLog> getOperationLogs(Long customerId) {
        QueryWrapper query = QueryWrapper.create()
                .from(CustomerOperationLog.class)
                .where(CUSTOMER_OPERATION_LOG.CUSTOMER_ID.eq(customerId))
                .orderBy(CUSTOMER_OPERATION_LOG.CREATE_TIME.desc());
        return customerOperationLogMapper.selectListByQuery(query);
    }

    public PageResult<CustomerOperationLog> queryOperationLogs(Integer pageNum, Integer pageSize,
                                                                 Long customerId, Long operatorId,
                                                                 Integer operationType,
                                                                 String startTime, String endTime) {
        QueryWrapper query = QueryWrapper.create()
                .from(CustomerOperationLog.class);
        if (customerId != null) {
            query.and(CUSTOMER_OPERATION_LOG.CUSTOMER_ID.eq(customerId));
        }
        if (operatorId != null) {
            query.and(CUSTOMER_OPERATION_LOG.OPERATOR_ID.eq(operatorId));
        }
        if (operationType != null) {
            query.and(CUSTOMER_OPERATION_LOG.OPERATION_TYPE.eq(operationType));
        }
        if (StringUtils.hasText(startTime)) {
            query.and(CUSTOMER_OPERATION_LOG.CREATE_TIME.ge(startTime + " 00:00:00"));
        }
        if (StringUtils.hasText(endTime)) {
            query.and(CUSTOMER_OPERATION_LOG.CREATE_TIME.le(endTime + " 23:59:59"));
        }
        query.orderBy(CUSTOMER_OPERATION_LOG.CREATE_TIME.desc());
        Page<CustomerOperationLog> page = customerOperationLogMapper.paginate(Page.of(pageNum, pageSize), query);
        return new PageResult<>(page.getTotalRow(), page.getRecords(),
                (long) page.getPageNumber(), (long) page.getPageSize());
    }

    private void validateCustomer(Customer customer) {
        if (!StringUtils.hasText(customer.getName())) {
            throw new BusinessException("姓名不能为空");
        }
        if (!StringUtils.hasText(customer.getPhone())) {
            throw new BusinessException("手机号不能为空");
        }
        if (!customer.getPhone().matches("^1[3-9]\\d{9}$")) {
            throw new BusinessException("手机号格式不正确");
        }
        if (StringUtils.hasText(customer.getEmail()) && !customer.getEmail().matches("^[\\w.-]+@[\\w.-]+\\.\\w+$")) {
            throw new BusinessException("邮箱格式不正确");
        }
        if (StringUtils.hasText(customer.getIdNumber())) {
            validateIdNumber(customer.getIdNumber(), customer.getIdType());
        }
    }

    private void validateIdNumber(String idNumber, Integer idType) {
        if (idType == null) return;
        if (idType == 1) {
            if (!idNumber.matches("^(\\d{15}|\\d{17}[\\dXx])$")) {
                throw new BusinessException("身份证号格式不正确，应为15或18位");
            }
        } else if (idType == 2) {
            if (!idNumber.matches("^[A-Za-z0-9]{5,20}$")) {
                throw new BusinessException("护照号格式不正确");
            }
        }
    }

    private void checkPhoneUnique(String phone, Long excludeId) {
        QueryWrapper query = QueryWrapper.create()
                .from(Customer.class)
                .where(CUSTOMER.PHONE.eq(phone))
                .and(CUSTOMER.DELETED.eq(0));
        if (excludeId != null) {
            query.and(CUSTOMER.ID.ne(excludeId));
        }
        long count = customerMapper.selectCountByQuery(query);
        if (count > 0) {
            throw new BusinessException("该手机号已被使用");
        }
    }

    private void checkIdNumberUnique(String idNumber, Integer idType, Long excludeId) {
        QueryWrapper query = QueryWrapper.create()
                .from(Customer.class)
                .where(CUSTOMER.ID_NUMBER.eq(idNumber))
                .and(CUSTOMER.ID_TYPE.eq(idType))
                .and(CUSTOMER.DELETED.eq(0));
        if (excludeId != null) {
            query.and(CUSTOMER.ID.ne(excludeId));
        }
        long count = customerMapper.selectCountByQuery(query);
        if (count > 0) {
            throw new BusinessException("该证件号已被使用");
        }
    }

    private void fillComputedFields(Customer customer) {
        if (customer.getTotalOrders() != null && customer.getTotalOrders() > 0
                && customer.getTotalSpent() != null) {
            customer.setAvgSpent(customer.getTotalSpent()
                    .divide(BigDecimal.valueOf(customer.getTotalOrders()), 2, RoundingMode.HALF_UP));
        } else {
            customer.setAvgSpent(BigDecimal.ZERO);
        }

        if (customer.getCreateTime() != null) {
            customer.setLifecycleDays(ChronoUnit.DAYS.between(customer.getCreateTime(), LocalDateTime.now()));
        } else {
            customer.setLifecycleDays(0L);
        }
    }

    private void loadAddresses(Customer customer) {
        QueryWrapper query = QueryWrapper.create()
                .from(CustomerAddress.class)
                .where(CUSTOMER_ADDRESS.CUSTOMER_ID.eq(customer.getId()))
                .and(CUSTOMER_ADDRESS.DELETED.eq(0))
                .orderBy(CUSTOMER_ADDRESS.IS_DEFAULT.desc());
        List<CustomerAddress> addresses = customerAddressMapper.selectListByQuery(query);
        customer.setAddresses(addresses);
    }

    private void loadReferrerName(Customer customer) {
        if (customer.getReferrerId() != null) {
            Customer referrer = customerMapper.selectOneById(customer.getReferrerId());
            if (referrer != null) {
                customer.setReferrerName(referrer.getName());
            }
        }
    }

    private void loadTags(Customer customer) {
        QueryWrapper relQuery = QueryWrapper.create()
                .from(CustomerTagRelation.class)
                .where(com.example.permission.entity.table.CustomerTagRelationTableDef.CUSTOMER_TAG_RELATION.CUSTOMER_ID.eq(customer.getId()));
        List<CustomerTagRelation> relations = customerTagRelationMapper.selectListByQuery(relQuery);
        List<CustomerTag> tags = new java.util.ArrayList<>();
        for (CustomerTagRelation rel : relations) {
            CustomerTag tag = customerTagMapper.selectOneById(rel.getTagId());
            if (tag != null && tag.getDeleted() == 0) {
                tags.add(tag);
            }
        }
        customer.setTags(tags);
    }

    private List<Long> getCustomerIdsByTagIds(List<Long> tagIds, String logic) {
        QueryWrapper relQuery = QueryWrapper.create()
                .from(CustomerTagRelation.class)
                .where(com.example.permission.entity.table.CustomerTagRelationTableDef.CUSTOMER_TAG_RELATION.TAG_ID.in(tagIds));
        List<CustomerTagRelation> relations = customerTagRelationMapper.selectListByQuery(relQuery);

        if ("AND".equalsIgnoreCase(logic)) {
            java.util.Map<Long, Integer> countMap = new java.util.HashMap<>();
            for (CustomerTagRelation rel : relations) {
                countMap.merge(rel.getCustomerId(), 1, Integer::sum);
            }
            List<Long> result = new java.util.ArrayList<>();
            for (java.util.Map.Entry<Long, Integer> entry : countMap.entrySet()) {
                if (entry.getValue() >= tagIds.size()) {
                    result.add(entry.getKey());
                }
            }
            return result;
        } else {
            return relations.stream()
                    .map(CustomerTagRelation::getCustomerId)
                    .distinct()
                    .collect(java.util.stream.Collectors.toList());
        }
    }

    private boolean canViewFinance(List<String> roles) {
        if (roles == null) return false;
        return roles.contains("admin") || roles.contains("hotel_admin")
                || roles.contains("service_manager") || roles.contains("finance_staff")
                || roles.contains("frontdesk_manager");
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
