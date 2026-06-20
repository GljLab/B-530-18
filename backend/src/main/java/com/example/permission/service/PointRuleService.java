package com.example.permission.service;

import com.example.permission.common.BusinessException;
import com.example.permission.entity.MemberLevel;
import com.example.permission.entity.PointConsumeRule;
import com.example.permission.entity.PointEarnRule;
import com.example.permission.mapper.MemberLevelMapper;
import com.example.permission.mapper.PointConsumeRuleMapper;
import com.example.permission.mapper.PointEarnRuleMapper;
import com.mybatisflex.core.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static com.example.permission.entity.table.PointEarnRuleTableDef.POINT_EARN_RULE;
import static com.example.permission.entity.table.PointConsumeRuleTableDef.POINT_CONSUME_RULE;

@Service
public class PointRuleService {

    @Autowired
    private PointEarnRuleMapper pointEarnRuleMapper;

    @Autowired
    private PointConsumeRuleMapper pointConsumeRuleMapper;

    @Autowired
    private MemberLevelMapper memberLevelMapper;

    public List<PointEarnRule> listEarnRules() {
        QueryWrapper query = QueryWrapper.create()
                .from(PointEarnRule.class)
                .where(POINT_EARN_RULE.DELETED.eq(0))
                .orderBy(POINT_EARN_RULE.RULE_TYPE.asc(), POINT_EARN_RULE.CREATE_TIME.desc());
        return pointEarnRuleMapper.selectListByQuery(query);
    }

    public List<PointEarnRule> listEnabledEarnRules() {
        QueryWrapper query = QueryWrapper.create()
                .from(PointEarnRule.class)
                .where(POINT_EARN_RULE.DELETED.eq(0))
                .and(POINT_EARN_RULE.STATUS.eq(1))
                .orderBy(POINT_EARN_RULE.RULE_TYPE.asc());
        LocalDateTime now = LocalDateTime.now();
        query.and(POINT_EARN_RULE.START_TIME.le(now).or(POINT_EARN_RULE.START_TIME.isNull()));
        query.and(POINT_EARN_RULE.END_TIME.ge(now).or(POINT_EARN_RULE.END_TIME.isNull()));
        return pointEarnRuleMapper.selectListByQuery(query);
    }

    public PointEarnRule getEarnRuleById(Long id) {
        PointEarnRule rule = pointEarnRuleMapper.selectOneById(id);
        if (rule == null || rule.getDeleted() == 1) {
            throw new BusinessException("积分获取规则不存在");
        }
        return rule;
    }

    public PointEarnRule getEnabledEarnRuleByType(Integer ruleType) {
        QueryWrapper query = QueryWrapper.create()
                .from(PointEarnRule.class)
                .where(POINT_EARN_RULE.DELETED.eq(0))
                .and(POINT_EARN_RULE.STATUS.eq(1))
                .and(POINT_EARN_RULE.RULE_TYPE.eq(ruleType));
        LocalDateTime now = LocalDateTime.now();
        query.and(POINT_EARN_RULE.START_TIME.le(now).or(POINT_EARN_RULE.START_TIME.isNull()));
        query.and(POINT_EARN_RULE.END_TIME.ge(now).or(POINT_EARN_RULE.END_TIME.isNull()));
        query.limit(1);
        return pointEarnRuleMapper.selectOneByQuery(query);
    }

    @Transactional
    public PointEarnRule addEarnRule(PointEarnRule rule) {
        validateEarnRule(rule);
        rule.setDeleted(0);
        rule.setCreateTime(LocalDateTime.now());
        pointEarnRuleMapper.insert(rule);
        return rule;
    }

    @Transactional
    public void updateEarnRule(PointEarnRule rule) {
        PointEarnRule existing = pointEarnRuleMapper.selectOneById(rule.getId());
        if (existing == null || existing.getDeleted() == 1) {
            throw new BusinessException("积分获取规则不存在");
        }
        validateEarnRule(rule);
        rule.setUpdateTime(LocalDateTime.now());
        pointEarnRuleMapper.update(rule);
    }

    @Transactional
    public void deleteEarnRule(Long id) {
        PointEarnRule existing = pointEarnRuleMapper.selectOneById(id);
        if (existing == null || existing.getDeleted() == 1) {
            throw new BusinessException("积分获取规则不存在");
        }
        existing.setDeleted(1);
        existing.setUpdateTime(LocalDateTime.now());
        pointEarnRuleMapper.update(existing);
    }

    @Transactional
    public void updateEarnRuleStatus(Long id, Integer status) {
        PointEarnRule existing = pointEarnRuleMapper.selectOneById(id);
        if (existing == null || existing.getDeleted() == 1) {
            throw new BusinessException("积分获取规则不存在");
        }
        existing.setStatus(status);
        existing.setUpdateTime(LocalDateTime.now());
        pointEarnRuleMapper.update(existing);
    }

    private void validateEarnRule(PointEarnRule rule) {
        if (rule.getRuleName() == null || rule.getRuleName().isEmpty()) {
            throw new BusinessException("规则名称不能为空");
        }
        if (rule.getRuleType() == null) {
            throw new BusinessException("规则类型不能为空");
        }
        if (rule.getRuleType() == 1) {
            if (rule.getPointValue() == null || rule.getPointValue().compareTo(BigDecimal.ZERO) <= 0) {
                throw new BusinessException("消费积分的每元金额必须大于0");
            }
            if (rule.getPointAmount() == null || rule.getPointAmount().compareTo(BigDecimal.ZERO) <= 0) {
                throw new BusinessException("消费积分的获得积分数必须大于0");
            }
        } else {
            if (rule.getPointAmount() == null || rule.getPointAmount().compareTo(BigDecimal.ZERO) <= 0) {
                throw new BusinessException("积分数量必须大于0");
            }
        }
        if (rule.getPointRate() == null || rule.getPointRate().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("积分倍率必须大于0");
        }
    }

    public List<PointConsumeRule> listConsumeRules() {
        QueryWrapper query = QueryWrapper.create()
                .from(PointConsumeRule.class)
                .where(POINT_CONSUME_RULE.DELETED.eq(0))
                .orderBy(POINT_CONSUME_RULE.RULE_TYPE.asc(), POINT_CONSUME_RULE.CREATE_TIME.desc());
        List<PointConsumeRule> rules = pointConsumeRuleMapper.selectListByQuery(query);
        for (PointConsumeRule rule : rules) {
            parseApplicableLevels(rule);
        }
        return rules;
    }

    public List<PointConsumeRule> listEnabledConsumeRules() {
        QueryWrapper query = QueryWrapper.create()
                .from(PointConsumeRule.class)
                .where(POINT_CONSUME_RULE.DELETED.eq(0))
                .and(POINT_CONSUME_RULE.STATUS.eq(1))
                .orderBy(POINT_CONSUME_RULE.RULE_TYPE.asc());
        LocalDateTime now = LocalDateTime.now();
        query.and(POINT_CONSUME_RULE.START_TIME.le(now).or(POINT_CONSUME_RULE.START_TIME.isNull()));
        query.and(POINT_CONSUME_RULE.END_TIME.ge(now).or(POINT_CONSUME_RULE.END_TIME.isNull()));
        List<PointConsumeRule> rules = pointConsumeRuleMapper.selectListByQuery(query);
        for (PointConsumeRule rule : rules) {
            parseApplicableLevels(rule);
        }
        return rules;
    }

    public PointConsumeRule getConsumeRuleById(Long id) {
        PointConsumeRule rule = pointConsumeRuleMapper.selectOneById(id);
        if (rule == null || rule.getDeleted() == 1) {
            throw new BusinessException("积分消耗规则不存在");
        }
        parseApplicableLevels(rule);
        return rule;
    }

    public PointConsumeRule getEnabledConsumeRuleByType(Integer ruleType) {
        QueryWrapper query = QueryWrapper.create()
                .from(PointConsumeRule.class)
                .where(POINT_CONSUME_RULE.DELETED.eq(0))
                .and(POINT_CONSUME_RULE.STATUS.eq(1))
                .and(POINT_CONSUME_RULE.RULE_TYPE.eq(ruleType));
        LocalDateTime now = LocalDateTime.now();
        query.and(POINT_CONSUME_RULE.START_TIME.le(now).or(POINT_CONSUME_RULE.START_TIME.isNull()));
        query.and(POINT_CONSUME_RULE.END_TIME.ge(now).or(POINT_CONSUME_RULE.END_TIME.isNull()));
        query.limit(1);
        PointConsumeRule rule = pointConsumeRuleMapper.selectOneByQuery(query);
        if (rule != null) {
            parseApplicableLevels(rule);
        }
        return rule;
    }

    @Transactional
    public PointConsumeRule addConsumeRule(PointConsumeRule rule) {
        validateConsumeRule(rule);
        serializeApplicableLevels(rule);
        rule.setDeleted(0);
        rule.setCreateTime(LocalDateTime.now());
        pointConsumeRuleMapper.insert(rule);
        return rule;
    }

    @Transactional
    public void updateConsumeRule(PointConsumeRule rule) {
        PointConsumeRule existing = pointConsumeRuleMapper.selectOneById(rule.getId());
        if (existing == null || existing.getDeleted() == 1) {
            throw new BusinessException("积分消耗规则不存在");
        }
        validateConsumeRule(rule);
        serializeApplicableLevels(rule);
        rule.setUpdateTime(LocalDateTime.now());
        pointConsumeRuleMapper.update(rule);
    }

    @Transactional
    public void deleteConsumeRule(Long id) {
        PointConsumeRule existing = pointConsumeRuleMapper.selectOneById(id);
        if (existing == null || existing.getDeleted() == 1) {
            throw new BusinessException("积分消耗规则不存在");
        }
        existing.setDeleted(1);
        existing.setUpdateTime(LocalDateTime.now());
        pointConsumeRuleMapper.update(existing);
    }

    @Transactional
    public void updateConsumeRuleStatus(Long id, Integer status) {
        PointConsumeRule existing = pointConsumeRuleMapper.selectOneById(id);
        if (existing == null || existing.getDeleted() == 1) {
            throw new BusinessException("积分消耗规则不存在");
        }
        existing.setStatus(status);
        existing.setUpdateTime(LocalDateTime.now());
        pointConsumeRuleMapper.update(existing);
    }

    private void validateConsumeRule(PointConsumeRule rule) {
        if (rule.getRuleName() == null || rule.getRuleName().isEmpty()) {
            throw new BusinessException("规则名称不能为空");
        }
        if (rule.getRuleType() == null) {
            throw new BusinessException("规则类型不能为空");
        }
        if (rule.getRuleType() == 1) {
            if (rule.getExchangePoints() == null || rule.getExchangePoints().compareTo(BigDecimal.ZERO) <= 0) {
                throw new BusinessException("兑换积分数必须大于0");
            }
            if (rule.getExchangeAmount() == null || rule.getExchangeAmount().compareTo(BigDecimal.ZERO) <= 0) {
                throw new BusinessException("兑换金额必须大于0");
            }
            if (rule.getMaxPointsPerUse() != null && rule.getMaxPointsPerUse().compareTo(BigDecimal.ZERO) < 0) {
                throw new BusinessException("单次最多使用积分不能为负数");
            }
            if (rule.getMinOrderAmount() != null && rule.getMinOrderAmount().compareTo(BigDecimal.ZERO) < 0) {
                throw new BusinessException("最低消费要求不能为负数");
            }
            if (rule.getDeductionCap() != null && (rule.getDeductionCap().compareTo(BigDecimal.ZERO) < 0 || rule.getDeductionCap().compareTo(new BigDecimal("100")) > 0)) {
                throw new BusinessException("抵扣上限必须在0-100%之间");
            }
        }
    }

    private void parseApplicableLevels(PointConsumeRule rule) {
        if (rule.getApplicableLevels() != null && !rule.getApplicableLevels().isEmpty()) {
            rule.setApplicableLevelList(
                    Arrays.stream(rule.getApplicableLevels().split(","))
                            .map(String::trim)
                            .filter(s -> !s.isEmpty())
                            .map(Long::parseLong)
                            .collect(Collectors.toList())
            );
        } else {
            rule.setApplicableLevelList(new ArrayList<>());
        }
    }

    private void serializeApplicableLevels(PointConsumeRule rule) {
        if (rule.getApplicableLevelList() != null && !rule.getApplicableLevelList().isEmpty()) {
            rule.setApplicableLevels(
                    rule.getApplicableLevelList().stream()
                            .map(String::valueOf)
                            .collect(Collectors.joining(","))
            );
        } else {
            rule.setApplicableLevels("");
        }
    }

    public BigDecimal calculateEarnPoints(BigDecimal consumeAmount, BigDecimal pointRate, Integer ruleType) {
        PointEarnRule rule = getEnabledEarnRuleByType(ruleType != null ? ruleType : 1);
        if (rule == null) {
            return BigDecimal.ZERO;
        }

        BigDecimal basePoints;
        if (rule.getRuleType() == 1) {
            if (rule.getPointValue() == null || rule.getPointValue().compareTo(BigDecimal.ZERO) == 0) {
                return BigDecimal.ZERO;
            }
            basePoints = consumeAmount.multiply(rule.getPointAmount()).divide(rule.getPointValue(), 0, BigDecimal.ROUND_DOWN);
        } else {
            basePoints = rule.getPointAmount();
        }

        BigDecimal effectiveRate = pointRate != null ? pointRate : rule.getPointRate();
        return basePoints.multiply(effectiveRate).setScale(0, BigDecimal.ROUND_DOWN);
    }
}
