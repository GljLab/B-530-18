package com.example.permission.service;

import com.example.permission.common.BusinessException;
import com.example.permission.entity.ReviewMetric;
import com.example.permission.mapper.ReviewMetricMapper;
import com.mybatisflex.core.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

import static com.example.permission.entity.table.ReviewMetricTableDef.REVIEW_METRIC;

@Service
public class ReviewMetricService {

    @Autowired
    private ReviewMetricMapper reviewMetricMapper;

    public List<ReviewMetric> listAll() {
        QueryWrapper query = QueryWrapper.create()
                .from(ReviewMetric.class)
                .where(REVIEW_METRIC.DELETED.eq(0))
                .orderBy(REVIEW_METRIC.SORT_ORDER.asc(), REVIEW_METRIC.ID.asc());
        return reviewMetricMapper.selectListByQuery(query);
    }

    public List<ReviewMetric> listEnabled() {
        QueryWrapper query = QueryWrapper.create()
                .from(ReviewMetric.class)
                .where(REVIEW_METRIC.DELETED.eq(0))
                .and(REVIEW_METRIC.STATUS.eq(1))
                .orderBy(REVIEW_METRIC.SORT_ORDER.asc(), REVIEW_METRIC.ID.asc());
        return reviewMetricMapper.selectListByQuery(query);
    }

    public ReviewMetric getById(Long id) {
        ReviewMetric metric = reviewMetricMapper.selectOneById(id);
        if (metric == null || metric.getDeleted() == 1) {
            throw new BusinessException("评价指标不存在");
        }
        return metric;
    }

    public ReviewMetric add(ReviewMetric metric) {
        if (metric.getMetricName() == null || metric.getMetricName().trim().isEmpty()) {
            throw new BusinessException("指标名称不能为空");
        }
        if (metric.getWeight() == null || metric.getWeight() < 0 || metric.getWeight() > 100) {
            throw new BusinessException("指标权重必须在0-100之间");
        }
        if (metric.getSortOrder() == null || metric.getSortOrder() < 0) {
            throw new BusinessException("显示排序必须大于等于0");
        }

        QueryWrapper query = QueryWrapper.create()
                .from(ReviewMetric.class)
                .where(REVIEW_METRIC.METRIC_NAME.eq(metric.getMetricName().trim()))
                .and(REVIEW_METRIC.DELETED.eq(0));
        long count = reviewMetricMapper.selectCountByQuery(query);
        if (count > 0) {
            throw new BusinessException("指标名称已存在");
        }

        metric.setMetricName(metric.getMetricName().trim());
        if (metric.getMetricDesc() != null) {
            metric.setMetricDesc(metric.getMetricDesc().trim());
        }
        if (metric.getScoreMin() == null) metric.setScoreMin(1);
        if (metric.getScoreMax() == null) metric.setScoreMax(5);
        if (metric.getIsRequired() == null) metric.setIsRequired(0);
        if (metric.getStatus() == null) metric.setStatus(1);
        metric.setDeleted(0);
        metric.setCreateTime(LocalDateTime.now());
        reviewMetricMapper.insert(metric);
        return metric;
    }

    public void update(ReviewMetric metric) {
        ReviewMetric existing = reviewMetricMapper.selectOneById(metric.getId());
        if (existing == null || existing.getDeleted() == 1) {
            throw new BusinessException("评价指标不存在");
        }
        if (metric.getMetricName() == null || metric.getMetricName().trim().isEmpty()) {
            throw new BusinessException("指标名称不能为空");
        }
        if (metric.getWeight() == null || metric.getWeight() < 0 || metric.getWeight() > 100) {
            throw new BusinessException("指标权重必须在0-100之间");
        }
        if (metric.getSortOrder() == null || metric.getSortOrder() < 0) {
            throw new BusinessException("显示排序必须大于等于0");
        }

        if (!existing.getMetricName().equals(metric.getMetricName().trim())) {
            QueryWrapper query = QueryWrapper.create()
                    .from(ReviewMetric.class)
                    .where(REVIEW_METRIC.METRIC_NAME.eq(metric.getMetricName().trim()))
                    .and(REVIEW_METRIC.DELETED.eq(0))
                    .and(REVIEW_METRIC.ID.ne(metric.getId()));
            long count = reviewMetricMapper.selectCountByQuery(query);
            if (count > 0) {
                throw new BusinessException("指标名称已存在");
            }
        }

        existing.setMetricName(metric.getMetricName().trim());
        existing.setMetricDesc(metric.getMetricDesc() != null ? metric.getMetricDesc().trim() : null);
        existing.setWeight(metric.getWeight());
        existing.setIsRequired(metric.getIsRequired());
        existing.setSortOrder(metric.getSortOrder());
        existing.setUpdateTime(LocalDateTime.now());
        reviewMetricMapper.update(existing);
    }

    public void delete(Long id) {
        ReviewMetric existing = reviewMetricMapper.selectOneById(id);
        if (existing == null || existing.getDeleted() == 1) {
            throw new BusinessException("评价指标不存在");
        }

        QueryWrapper enabledQuery = QueryWrapper.create()
                .from(ReviewMetric.class)
                .where(REVIEW_METRIC.DELETED.eq(0))
                .and(REVIEW_METRIC.STATUS.eq(1))
                .and(REVIEW_METRIC.ID.ne(id));
        long enabledCount = reviewMetricMapper.selectCountByQuery(enabledQuery);
        if (existing.getStatus() == 1 && enabledCount == 0) {
            throw new BusinessException("至少需要保留一个启用的评价指标");
        }

        existing.setDeleted(1);
        existing.setUpdateTime(LocalDateTime.now());
        reviewMetricMapper.update(existing);
    }

    public void updateStatus(Long id, Integer status) {
        ReviewMetric existing = reviewMetricMapper.selectOneById(id);
        if (existing == null || existing.getDeleted() == 1) {
            throw new BusinessException("评价指标不存在");
        }

        if (status == 0) {
            QueryWrapper enabledQuery = QueryWrapper.create()
                    .from(ReviewMetric.class)
                    .where(REVIEW_METRIC.DELETED.eq(0))
                    .and(REVIEW_METRIC.STATUS.eq(1))
                    .and(REVIEW_METRIC.ID.ne(id));
            long enabledCount = reviewMetricMapper.selectCountByQuery(enabledQuery);
            if (enabledCount == 0) {
                throw new BusinessException("至少需要保留一个启用的评价指标");
            }
        }

        existing.setStatus(status);
        existing.setUpdateTime(LocalDateTime.now());
        reviewMetricMapper.update(existing);
    }

    public int getTotalWeight() {
        List<ReviewMetric> metrics = listEnabled();
        return metrics.stream().mapToInt(ReviewMetric::getWeight).sum();
    }
}
