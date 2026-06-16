package com.example.permission.service;

import com.example.permission.common.BusinessException;
import com.example.permission.entity.CustomerOperationLog;
import com.example.permission.entity.CustomerTag;
import com.example.permission.entity.CustomerTagRelation;
import com.example.permission.mapper.CustomerOperationLogMapper;
import com.example.permission.mapper.CustomerTagMapper;
import com.example.permission.mapper.CustomerTagRelationMapper;
import com.mybatisflex.core.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.example.permission.entity.table.CustomerTagRelationTableDef.CUSTOMER_TAG_RELATION;
import static com.example.permission.entity.table.CustomerTagTableDef.CUSTOMER_TAG;

@Service
public class CustomerTagService {

    @Autowired
    private CustomerTagMapper customerTagMapper;

    @Autowired
    private CustomerTagRelationMapper customerTagRelationMapper;

    @Autowired
    private CustomerOperationLogMapper customerOperationLogMapper;

    public List<CustomerTag> listAll() {
        QueryWrapper query = QueryWrapper.create()
                .from(CustomerTag.class)
                .where(CUSTOMER_TAG.DELETED.eq(0))
                .orderBy(CUSTOMER_TAG.CREATE_TIME.desc());
        List<CustomerTag> tags = customerTagMapper.selectListByQuery(query);
        for (CustomerTag tag : tags) {
            QueryWrapper countQuery = QueryWrapper.create()
                    .from(CustomerTagRelation.class)
                    .where(CUSTOMER_TAG_RELATION.TAG_ID.eq(tag.getId()));
            long count = customerTagRelationMapper.selectCountByQuery(countQuery);
            tag.setCustomerCount(count);
        }
        return tags;
    }

    public CustomerTag addTag(CustomerTag tag) {
        QueryWrapper query = QueryWrapper.create()
                .from(CustomerTag.class)
                .where(CUSTOMER_TAG.TAG_NAME.eq(tag.getTagName()))
                .and(CUSTOMER_TAG.DELETED.eq(0));
        long count = customerTagMapper.selectCountByQuery(query);
        if (count > 0) {
            throw new BusinessException("标签名称已存在");
        }
        tag.setDeleted(0);
        tag.setCreateTime(LocalDateTime.now());
        customerTagMapper.insert(tag);
        return tag;
    }

    public void updateTag(CustomerTag tag) {
        CustomerTag existing = customerTagMapper.selectOneById(tag.getId());
        if (existing == null || existing.getDeleted() == 1) {
            throw new BusinessException("标签不存在");
        }
        if (!existing.getTagName().equals(tag.getTagName())) {
            QueryWrapper query = QueryWrapper.create()
                    .from(CustomerTag.class)
                    .where(CUSTOMER_TAG.TAG_NAME.eq(tag.getTagName()))
                    .and(CUSTOMER_TAG.DELETED.eq(0))
                    .and(CUSTOMER_TAG.ID.ne(tag.getId()));
            long count = customerTagMapper.selectCountByQuery(query);
            if (count > 0) {
                throw new BusinessException("标签名称已存在");
            }
        }
        tag.setUpdateTime(LocalDateTime.now());
        customerTagMapper.update(tag);
    }

    @Transactional
    public void deleteTag(Long id) {
        CustomerTag existing = customerTagMapper.selectOneById(id);
        if (existing == null || existing.getDeleted() == 1) {
            throw new BusinessException("标签不存在");
        }
        existing.setDeleted(1);
        existing.setUpdateTime(LocalDateTime.now());
        customerTagMapper.update(existing);

        QueryWrapper relationQuery = QueryWrapper.create()
                .from(CustomerTagRelation.class)
                .where(CUSTOMER_TAG_RELATION.TAG_ID.eq(id));
        List<CustomerTagRelation> relations = customerTagRelationMapper.selectListByQuery(relationQuery);
        for (CustomerTagRelation relation : relations) {
            customerTagRelationMapper.deleteById(relation.getId());
        }

        saveOperationLog(null, null, 2, null, null, "删除标签：" + existing.getTagName());
    }

    public List<CustomerTag> getTagsByCustomerId(Long customerId) {
        QueryWrapper query = QueryWrapper.create()
                .select(CUSTOMER_TAG.ID, CUSTOMER_TAG.TAG_NAME, CUSTOMER_TAG.CREATE_TIME, CUSTOMER_TAG.UPDATE_TIME, CUSTOMER_TAG.DELETED)
                .from(CustomerTag.class)
                .leftJoin(CustomerTagRelation.class).on(CUSTOMER_TAG_RELATION.TAG_ID.eq(CUSTOMER_TAG.ID))
                .where(CUSTOMER_TAG_RELATION.CUSTOMER_ID.eq(customerId))
                .and(CUSTOMER_TAG.DELETED.eq(0));
        return customerTagMapper.selectListByQuery(query);
    }

    @Transactional
    public void addTagsToCustomer(Long customerId, List<Long> tagIds, Long operatorId, String operatorName) {
        for (Long tagId : tagIds) {
            QueryWrapper existsQuery = QueryWrapper.create()
                    .from(CustomerTagRelation.class)
                    .where(CUSTOMER_TAG_RELATION.CUSTOMER_ID.eq(customerId))
                    .and(CUSTOMER_TAG_RELATION.TAG_ID.eq(tagId));
            long exists = customerTagRelationMapper.selectCountByQuery(existsQuery);
            if (exists == 0) {
                CustomerTagRelation relation = new CustomerTagRelation();
                relation.setCustomerId(customerId);
                relation.setTagId(tagId);
                relation.setCreateTime(LocalDateTime.now());
                customerTagRelationMapper.insert(relation);
            }

            CustomerTag tag = customerTagMapper.selectOneById(tagId);
            String tagName = tag != null ? tag.getTagName() : "";
            saveOperationLog(customerId, null, 5, operatorId, operatorName, "添加标签：" + tagName);
        }
    }

    @Transactional
    public void removeTagFromCustomer(Long customerId, Long tagId, Long operatorId, String operatorName) {
        QueryWrapper query = QueryWrapper.create()
                .from(CustomerTagRelation.class)
                .where(CUSTOMER_TAG_RELATION.CUSTOMER_ID.eq(customerId))
                .and(CUSTOMER_TAG_RELATION.TAG_ID.eq(tagId));
        List<CustomerTagRelation> relations = customerTagRelationMapper.selectListByQuery(query);
        for (CustomerTagRelation relation : relations) {
            customerTagRelationMapper.deleteById(relation.getId());
        }

        CustomerTag tag = customerTagMapper.selectOneById(tagId);
        String tagName = tag != null ? tag.getTagName() : "";
        saveOperationLog(customerId, null, 6, operatorId, operatorName, "移除标签：" + tagName);
    }

    public List<Long> getCustomerIdsByTagIds(List<Long> tagIds, String logic) {
        if (tagIds == null || tagIds.isEmpty()) {
            return new ArrayList<>();
        }
        QueryWrapper query = QueryWrapper.create()
                .from(CustomerTagRelation.class)
                .where(CUSTOMER_TAG_RELATION.TAG_ID.in(tagIds));
        List<CustomerTagRelation> allRelations = customerTagRelationMapper.selectListByQuery(query);

        if ("AND".equalsIgnoreCase(logic)) {
            Map<Long, Long> customerTagCount = allRelations.stream()
                    .collect(Collectors.groupingBy(CustomerTagRelation::getCustomerId, Collectors.counting()));
            return customerTagCount.entrySet().stream()
                    .filter(e -> e.getValue() >= tagIds.size())
                    .map(Map.Entry::getKey)
                    .collect(Collectors.toList());
        } else {
            return allRelations.stream()
                    .map(CustomerTagRelation::getCustomerId)
                    .distinct()
                    .collect(Collectors.toList());
        }
    }

    public List<Map<String, Object>> getTagStatistics() {
        List<CustomerTag> tags = listAll();
        List<Map<String, Object>> statistics = new ArrayList<>();
        for (CustomerTag tag : tags) {
            Map<String, Object> item = new HashMap<>();
            item.put("tagName", tag.getTagName());
            item.put("customerCount", tag.getCustomerCount());
            statistics.add(item);
        }
        return statistics;
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
