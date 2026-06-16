package com.example.permission.service;

import com.example.permission.entity.*;
import com.example.permission.mapper.*;
import com.mybatisflex.core.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.*;

import static com.example.permission.entity.table.CustomerAddressTableDef.CUSTOMER_ADDRESS;
import static com.example.permission.entity.table.CustomerBlacklistTableDef.CUSTOMER_BLACKLIST;
import static com.example.permission.entity.table.CustomerNoteTableDef.CUSTOMER_NOTE;
import static com.example.permission.entity.table.CustomerOperationLogTableDef.CUSTOMER_OPERATION_LOG;
import static com.example.permission.entity.table.CustomerPreferenceTableDef.CUSTOMER_PREFERENCE;
import static com.example.permission.entity.table.CustomerTableDef.CUSTOMER;
import static com.example.permission.entity.table.CustomerTagRelationTableDef.CUSTOMER_TAG_RELATION;

@Service
public class CustomerMergeService {

    @Autowired
    private CustomerMapper customerMapper;

    @Autowired
    private CustomerTagRelationMapper customerTagRelationMapper;

    @Autowired
    private CustomerPreferenceMapper customerPreferenceMapper;

    @Autowired
    private CustomerNoteMapper customerNoteMapper;

    @Autowired
    private CustomerOperationLogMapper customerOperationLogMapper;

    @Autowired
    private CustomerMergeLogMapper customerMergeLogMapper;

    @Autowired
    private CustomerAddressMapper customerAddressMapper;

    @Autowired
    private CustomerTagMapper customerTagMapper;

    @Autowired
    private CustomerBlacklistMapper customerBlacklistMapper;

    public List<Map<String, Object>> checkDuplicate(String name, String idNumber, Integer idType, Long excludeId) {
        List<Map<String, Object>> results = new ArrayList<>();

        if (StringUtils.hasText(idNumber) && idType != null) {
            QueryWrapper idQuery = QueryWrapper.create()
                    .from(Customer.class)
                    .where(CUSTOMER.ID_NUMBER.eq(idNumber))
                    .and(CUSTOMER.ID_TYPE.eq(idType))
                    .and(CUSTOMER.DELETED.eq(0));
            if (excludeId != null) {
                idQuery.and(CUSTOMER.ID.ne(excludeId));
            }
            List<Customer> idMatches = customerMapper.selectListByQuery(idQuery);
            for (Customer c : idMatches) {
                Map<String, Object> item = new HashMap<>();
                item.put("id", c.getId());
                item.put("name", c.getName());
                item.put("phone", c.getPhone());
                item.put("idType", c.getIdType());
                item.put("idNumber", c.getIdNumber());
                item.put("similarity", 100);
                results.add(item);
            }
        }

        if (StringUtils.hasText(name)) {
            QueryWrapper nameQuery = QueryWrapper.create()
                    .from(Customer.class)
                    .where(CUSTOMER.NAME.like(name))
                    .and(CUSTOMER.DELETED.eq(0));
            if (excludeId != null) {
                nameQuery.and(CUSTOMER.ID.ne(excludeId));
            }
            List<Customer> nameMatches = customerMapper.selectListByQuery(nameQuery);
            Set<Long> existingIds = new HashSet<>();
            for (Map<String, Object> r : results) {
                existingIds.add((Long) r.get("id"));
            }
            for (Customer c : nameMatches) {
                if (existingIds.contains(c.getId())) {
                    continue;
                }
                Map<String, Object> item = new HashMap<>();
                item.put("id", c.getId());
                item.put("name", c.getName());
                item.put("phone", c.getPhone());
                item.put("idType", c.getIdType());
                item.put("idNumber", c.getIdNumber());
                item.put("similarity", 60);
                results.add(item);
            }
        }

        results.sort((a, b) -> (Integer) b.get("similarity") - (Integer) a.get("similarity"));
        return results;
    }

    public List<Map<String, Object>> scanAllDuplicates() {
        List<Map<String, Object>> results = new ArrayList<>();

        QueryWrapper query = QueryWrapper.create()
                .from(Customer.class)
                .where(CUSTOMER.DELETED.eq(0));
        List<Customer> allCustomers = customerMapper.selectListByQuery(query);

        for (int i = 0; i < allCustomers.size(); i++) {
            for (int j = i + 1; j < allCustomers.size(); j++) {
                Customer a = allCustomers.get(i);
                Customer b = allCustomers.get(j);

                boolean idMatch = false;
                boolean nameMatch = false;
                String reason = "";

                if (StringUtils.hasText(a.getIdNumber()) && a.getIdNumber().equals(b.getIdNumber())
                        && a.getIdType() != null && a.getIdType().equals(b.getIdType())) {
                    idMatch = true;
                    reason = "证件号码+证件类型相同";
                }

                if (StringUtils.hasText(a.getName()) && StringUtils.hasText(b.getName())
                        && (a.getName().equals(b.getName()) || a.getName().contains(b.getName()) || b.getName().contains(a.getName()))) {
                    nameMatch = true;
                    if (!reason.isEmpty()) {
                        reason += "，姓名相似";
                    } else {
                        reason = "姓名相似";
                    }
                }

                if (idMatch || nameMatch) {
                    Map<String, Object> pair = new HashMap<>();
                    Map<String, Object> customerA = new HashMap<>();
                    customerA.put("id", a.getId());
                    customerA.put("name", a.getName());
                    customerA.put("phone", a.getPhone());
                    customerA.put("idType", a.getIdType());
                    customerA.put("idNumber", a.getIdNumber());
                    Map<String, Object> customerB = new HashMap<>();
                    customerB.put("id", b.getId());
                    customerB.put("name", b.getName());
                    customerB.put("phone", b.getPhone());
                    customerB.put("idType", b.getIdType());
                    customerB.put("idNumber", b.getIdNumber());

                    pair.put("customerA", customerA);
                    pair.put("customerB", customerB);
                    pair.put("similarity", idMatch ? 100 : 60);
                    pair.put("reason", reason);
                    results.add(pair);
                }
            }
        }

        results.sort((a, b) -> (Integer) b.get("similarity") - (Integer) a.get("similarity"));
        return results;
    }

    @Transactional
    public void mergeCustomers(Long sourceId, Long targetId, Map<String, Object> fieldSelection,
                                Long operatorId, String operatorName) {
        Customer source = customerMapper.selectOneById(sourceId);
        Customer target = customerMapper.selectOneById(targetId);

        if (source == null || source.getDeleted() == 1) {
            throw new com.example.permission.common.BusinessException("源客户不存在");
        }
        if (target == null || target.getDeleted() == 1) {
            throw new com.example.permission.common.BusinessException("目标客户不存在");
        }
        if (sourceId.equals(targetId)) {
            throw new com.example.permission.common.BusinessException("不能合并同一个客户");
        }

        if (fieldSelection != null) {
            boolean needClearSourceUnique = false;

            for (Map.Entry<String, Object> entry : fieldSelection.entrySet()) {
                String fieldName = entry.getKey();
                String choice = entry.getValue() != null ? entry.getValue().toString() : "target";

                try {
                    Field field = Customer.class.getDeclaredField(fieldName);
                    field.setAccessible(true);
                    Object value = "source".equals(choice) ? field.get(source) : field.get(target);
                    if (value != null) {
                        field.set(target, value);
                        if ("phone".equals(fieldName) || "idNumber".equals(fieldName) || "idType".equals(fieldName)) {
                            needClearSourceUnique = true;
                        }
                    }
                } catch (NoSuchFieldException | IllegalAccessException e) {
                    continue;
                }
            }

            if (needClearSourceUnique) {
                source.setPhone(source.getPhone() + "_merged_" + sourceId);
                if (source.getIdNumber() != null) {
                    source.setIdNumber(source.getIdNumber() + "_merged_" + sourceId);
                }
                customerMapper.update(source);
            }
        }

        QueryWrapper sourceTagQuery = QueryWrapper.create()
                .from(CustomerTagRelation.class)
                .where(CUSTOMER_TAG_RELATION.CUSTOMER_ID.eq(sourceId));
        List<CustomerTagRelation> sourceTags = customerTagRelationMapper.selectListByQuery(sourceTagQuery);

        QueryWrapper targetTagQuery = QueryWrapper.create()
                .from(CustomerTagRelation.class)
                .where(CUSTOMER_TAG_RELATION.CUSTOMER_ID.eq(targetId));
        List<CustomerTagRelation> targetTags = customerTagRelationMapper.selectListByQuery(targetTagQuery);

        Set<Long> targetTagIds = new HashSet<>();
        for (CustomerTagRelation tr : targetTags) {
            targetTagIds.add(tr.getTagId());
        }
        for (CustomerTagRelation str : sourceTags) {
            if (!targetTagIds.contains(str.getTagId())) {
                CustomerTagRelation newRel = new CustomerTagRelation();
                newRel.setCustomerId(targetId);
                newRel.setTagId(str.getTagId());
                customerTagRelationMapper.insert(newRel);
            }
        }

        CustomerPreference sourcePref = customerPreferenceMapper.selectOneByQuery(
                QueryWrapper.create().from(CustomerPreference.class)
                        .where(CUSTOMER_PREFERENCE.CUSTOMER_ID.eq(sourceId)));
        CustomerPreference targetPref = customerPreferenceMapper.selectOneByQuery(
                QueryWrapper.create().from(CustomerPreference.class)
                        .where(CUSTOMER_PREFERENCE.CUSTOMER_ID.eq(targetId)));

        if (sourcePref != null) {
            if (targetPref == null) {
                CustomerPreference newPref = new CustomerPreference();
                newPref.setCustomerId(targetId);
                newPref.setPreferredRoomType(sourcePref.getPreferredRoomType());
                newPref.setPreferredFloor(sourcePref.getPreferredFloor());
                newPref.setPreferredOrientation(sourcePref.getPreferredOrientation());
                newPref.setPreferredBedType(sourcePref.getPreferredBedType());
                newPref.setPreferredView(sourcePref.getPreferredView());
                newPref.setSpecialNeeds(sourcePref.getSpecialNeeds());
                newPref.setServicePreference(sourcePref.getServicePreference());
                newPref.setDietVegetarian(sourcePref.getDietVegetarian());
                newPref.setDietHalal(sourcePref.getDietHalal());
                newPref.setDietSeafoodAllergy(sourcePref.getDietSeafoodAllergy());
                newPref.setDietNoSpicy(sourcePref.getDietNoSpicy());
                newPref.setDietOtherAllergy(sourcePref.getDietOtherAllergy());
                customerPreferenceMapper.insert(newPref);
            } else {
                mergePreferenceFields(sourcePref, targetPref);
                customerPreferenceMapper.update(targetPref);
            }
        }

        QueryWrapper sourceNoteQuery = QueryWrapper.create()
                .from(CustomerNote.class)
                .where(CUSTOMER_NOTE.CUSTOMER_ID.eq(sourceId));
        List<CustomerNote> sourceNotes = customerNoteMapper.selectListByQuery(sourceNoteQuery);
        for (CustomerNote note : sourceNotes) {
            note.setCustomerId(targetId);
            customerNoteMapper.update(note);
        }

        QueryWrapper sourceAddrQuery = QueryWrapper.create()
                .from(CustomerAddress.class)
                .where(CUSTOMER_ADDRESS.CUSTOMER_ID.eq(sourceId));
        List<CustomerAddress> sourceAddrs = customerAddressMapper.selectListByQuery(sourceAddrQuery);
        for (CustomerAddress addr : sourceAddrs) {
            addr.setCustomerId(targetId);
            customerAddressMapper.update(addr);
        }

        QueryWrapper sourceLogQuery = QueryWrapper.create()
                .from(CustomerOperationLog.class)
                .where(CUSTOMER_OPERATION_LOG.CUSTOMER_ID.eq(sourceId));
        List<CustomerOperationLog> sourceLogs = customerOperationLogMapper.selectListByQuery(sourceLogQuery);
        for (CustomerOperationLog log : sourceLogs) {
            log.setCustomerId(targetId);
            customerOperationLogMapper.update(log);
        }

        CustomerBlacklist sourceBlacklist = customerBlacklistMapper.selectOneByQuery(
                QueryWrapper.create().from(CustomerBlacklist.class)
                        .where(CUSTOMER_BLACKLIST.CUSTOMER_ID.eq(sourceId))
                        .and(CUSTOMER_BLACKLIST.STATUS.eq(2)));
        if (sourceBlacklist != null) {
            CustomerBlacklist targetBlacklist = customerBlacklistMapper.selectOneByQuery(
                    QueryWrapper.create().from(CustomerBlacklist.class)
                            .where(CUSTOMER_BLACKLIST.CUSTOMER_ID.eq(targetId))
                            .and(CUSTOMER_BLACKLIST.STATUS.eq(2)));
            if (targetBlacklist == null) {
                sourceBlacklist.setCustomerId(targetId);
                customerBlacklistMapper.update(sourceBlacklist);
            }
        }

        if (source.getTotalOrders() != null) {
            target.setTotalOrders((target.getTotalOrders() != null ? target.getTotalOrders() : 0) + source.getTotalOrders());
        }
        if (source.getTotalSpent() != null) {
            target.setTotalSpent(target.getTotalSpent() != null ? target.getTotalSpent().add(source.getTotalSpent()) : source.getTotalSpent());
        }

        target.setUpdateTime(LocalDateTime.now());
        customerMapper.update(target);

        source.setDeleted(1);
        source.setStatus(0);
        source.setUpdateTime(LocalDateTime.now());
        customerMapper.update(source);

        CustomerMergeLog mergeLog = new CustomerMergeLog();
        mergeLog.setSourceCustomerId(sourceId);
        mergeLog.setTargetCustomerId(targetId);
        mergeLog.setSourceCustomerName(source.getName());
        mergeLog.setTargetCustomerName(target.getName());
        mergeLog.setMergeDetails(fieldSelection != null ? fieldSelection.toString() : "");
        mergeLog.setOperatorId(operatorId);
        mergeLog.setOperatorName(operatorName);
        mergeLog.setMergeTime(LocalDateTime.now());
        customerMergeLogMapper.insert(mergeLog);

        saveOperationLog(targetId, target.getName(), 11, operatorId, operatorName,
                "合并客户：将 " + source.getName() + " 合并到 " + target.getName());
    }

    private void mergePreferenceFields(CustomerPreference source, CustomerPreference target) {
        if (source.getPreferredRoomType() != null && target.getPreferredRoomType() == null) {
            target.setPreferredRoomType(source.getPreferredRoomType());
        }
        if (source.getPreferredFloor() != null && target.getPreferredFloor() == null) {
            target.setPreferredFloor(source.getPreferredFloor());
        }
        if (source.getPreferredOrientation() != null && target.getPreferredOrientation() == null) {
            target.setPreferredOrientation(source.getPreferredOrientation());
        }
        if (source.getPreferredBedType() != null && target.getPreferredBedType() == null) {
            target.setPreferredBedType(source.getPreferredBedType());
        }
        if (source.getPreferredView() != null && target.getPreferredView() == null) {
            target.setPreferredView(source.getPreferredView());
        }
        if (source.getSpecialNeeds() != null && target.getSpecialNeeds() == null) {
            target.setSpecialNeeds(source.getSpecialNeeds());
        }
        if (source.getServicePreference() != null && target.getServicePreference() == null) {
            target.setServicePreference(source.getServicePreference());
        }
        if (source.getDietVegetarian() != null && target.getDietVegetarian() == null) {
            target.setDietVegetarian(source.getDietVegetarian());
        }
        if (source.getDietHalal() != null && target.getDietHalal() == null) {
            target.setDietHalal(source.getDietHalal());
        }
        if (source.getDietSeafoodAllergy() != null && target.getDietSeafoodAllergy() == null) {
            target.setDietSeafoodAllergy(source.getDietSeafoodAllergy());
        }
        if (source.getDietNoSpicy() != null && target.getDietNoSpicy() == null) {
            target.setDietNoSpicy(source.getDietNoSpicy());
        }
        if (source.getDietOtherAllergy() != null && target.getDietOtherAllergy() == null) {
            target.setDietOtherAllergy(source.getDietOtherAllergy());
        }
        target.setUpdateTime(LocalDateTime.now());
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
