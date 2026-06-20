package com.example.permission.service;

import com.example.permission.common.BusinessException;
import com.example.permission.common.PageResult;
import com.example.permission.entity.Customer;
import com.example.permission.entity.Member;
import com.example.permission.entity.MemberLevel;
import com.example.permission.entity.MemberLevelLog;
import com.example.permission.entity.MemberPointLog;
import com.example.permission.entity.PointConsumeRule;
import com.example.permission.entity.PointEarnRule;
import com.example.permission.mapper.CustomerMapper;
import com.example.permission.mapper.MemberLevelLogMapper;
import com.example.permission.mapper.MemberLevelMapper;
import com.example.permission.mapper.MemberMapper;
import com.example.permission.mapper.MemberPointLogMapper;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.example.permission.entity.table.MemberLevelTableDef.MEMBER_LEVEL;
import static com.example.permission.entity.table.MemberLevelLogTableDef.MEMBER_LEVEL_LOG;
import static com.example.permission.entity.table.MemberPointLogTableDef.MEMBER_POINT_LOG;
import static com.example.permission.entity.table.MemberTableDef.MEMBER;
import static com.example.permission.entity.table.CustomerTableDef.CUSTOMER;

@Service
public class MemberService {

    @Autowired
    private MemberMapper memberMapper;

    @Autowired
    private MemberLevelMapper memberLevelMapper;

    @Autowired
    private MemberPointLogMapper memberPointLogMapper;

    @Autowired
    private MemberLevelLogMapper memberLevelLogMapper;

    @Autowired
    private CustomerMapper customerMapper;

    @Autowired
    private PointRuleService pointRuleService;

    public PageResult<Member> pageList(Integer pageNum, Integer pageSize, String keyword,
                                        List<Long> levelIds, List<Integer> status,
                                        String registerTimeStart, String registerTimeEnd,
                                        BigDecimal pointsMin, BigDecimal pointsMax,
                                        String sortField, String sortOrder) {
        QueryWrapper query = QueryWrapper.create()
                .from(Member.class)
                .where(MEMBER.DELETED.eq(0));

        if (keyword != null && !keyword.isEmpty()) {
            query.and(MEMBER.MEMBER_NO.like(keyword)
                    .or(MEMBER.CUSTOMER_NAME.like(keyword))
                    .or(MEMBER.PHONE.like(keyword)));
        }
        if (levelIds != null && !levelIds.isEmpty()) {
            query.and(MEMBER.LEVEL_ID.in(levelIds));
        }
        if (status != null && !status.isEmpty()) {
            query.and(MEMBER.STATUS.in(status));
        }
        if (registerTimeStart != null && !registerTimeStart.isEmpty()) {
            query.and(MEMBER.REGISTER_TIME.ge(LocalDateTime.parse(registerTimeStart + " 00:00:00", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))));
        }
        if (registerTimeEnd != null && !registerTimeEnd.isEmpty()) {
            query.and(MEMBER.REGISTER_TIME.le(LocalDateTime.parse(registerTimeEnd + " 23:59:59", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))));
        }
        if (pointsMin != null) {
            query.and(MEMBER.CURRENT_POINTS.ge(pointsMin));
        }
        if (pointsMax != null) {
            query.and(MEMBER.CURRENT_POINTS.le(pointsMax));
        }

        if (sortField != null && !sortField.isEmpty()) {
            if ("points".equals(sortField)) {
                query.orderBy(MEMBER.CURRENT_POINTS, "asc".equalsIgnoreCase(sortOrder));
            } else if ("totalSpent".equals(sortField)) {
                query.orderBy(MEMBER.TOTAL_SPENT, "asc".equalsIgnoreCase(sortOrder));
            } else if ("registerTime".equals(sortField)) {
                query.orderBy(MEMBER.REGISTER_TIME, "asc".equalsIgnoreCase(sortOrder));
            } else {
                query.orderBy(MEMBER.CREATE_TIME.desc());
            }
        } else {
            query.orderBy(MEMBER.CREATE_TIME.desc());
        }

        Page<Member> page = memberMapper.paginate(pageNum, pageSize, query);
        List<Member> records = page.getRecords();
        for (Member member : records) {
            enrichMemberInfo(member);
        }

        return new PageResult<>(page.getTotalRow(), records, (long) pageNum, (long) pageSize);
    }

    public Member getById(Long id) {
        Member member = memberMapper.selectOneById(id);
        if (member == null || member.getDeleted() == 1) {
            throw new BusinessException("会员不存在");
        }
        enrichMemberInfo(member);
        return member;
    }

    public Member getByCustomerId(Long customerId) {
        QueryWrapper query = QueryWrapper.create()
                .from(Member.class)
                .where(MEMBER.CUSTOMER_ID.eq(customerId))
                .and(MEMBER.DELETED.eq(0));
        Member member = memberMapper.selectOneByQuery(query);
        if (member != null) {
            enrichMemberInfo(member);
        }
        return member;
    }

    public Member getByMemberNo(String memberNo) {
        QueryWrapper query = QueryWrapper.create()
                .from(Member.class)
                .where(MEMBER.MEMBER_NO.eq(memberNo))
                .and(MEMBER.DELETED.eq(0));
        Member member = memberMapper.selectOneByQuery(query);
        if (member != null) {
            enrichMemberInfo(member);
        }
        return member;
    }

    @Transactional
    public Member registerFromCustomer(Long customerId, Integer registerSource, Long referrerId,
                                       Long operatorId, String operatorName) {
        Customer customer = customerMapper.selectOneById(customerId);
        if (customer == null || customer.getDeleted() == 1) {
            throw new BusinessException("客户不存在");
        }

        Member existingMember = getByCustomerId(customerId);
        if (existingMember != null) {
            throw new BusinessException("该客户已是会员");
        }

        Member referrer = null;
        if (referrerId != null) {
            referrer = memberMapper.selectOneById(referrerId);
            if (referrer == null || referrer.getDeleted() == 1) {
                throw new BusinessException("推荐人不存在");
            }
        }

        MemberLevel lowestLevel = getLowestLevel();

        Member member = new Member();
        member.setMemberNo(generateMemberNo());
        member.setCustomerId(customerId);
        member.setCustomerName(customer.getName());
        member.setPhone(customer.getPhone());
        member.setLevelId(lowestLevel.getId());
        member.setLevelName(lowestLevel.getLevelName());
        member.setRegisterSource(registerSource != null ? registerSource : 1);
        if (referrer != null) {
            member.setReferrerId(referrer.getId());
            member.setReferrerNo(referrer.getMemberNo());
        }
        member.setTotalPoints(BigDecimal.ZERO);
        member.setCurrentPoints(BigDecimal.ZERO);
        member.setTotalSpent(BigDecimal.ZERO);
        member.setYearlySpent(BigDecimal.ZERO);
        member.setStayCount(0);
        member.setStatus(1);
        member.setRegisterTime(LocalDateTime.now());
        member.setCreateTime(LocalDateTime.now());
        member.setDeleted(0);

        memberMapper.insert(member);

        MemberLevelLog levelLog = new MemberLevelLog();
        levelLog.setMemberId(member.getId());
        levelLog.setMemberNo(member.getMemberNo());
        levelLog.setChangeType(3);
        levelLog.setNewLevelId(lowestLevel.getId());
        levelLog.setNewLevelName(lowestLevel.getLevelName());
        levelLog.setReason("会员注册");
        levelLog.setOperatorId(operatorId);
        levelLog.setOperatorName(operatorName);
        levelLog.setCreateTime(LocalDateTime.now());
        memberLevelLogMapper.insert(levelLog);

        if (referrer != null) {
            PointEarnRule referralRule = pointRuleService.getEnabledEarnRuleByType(4);
            BigDecimal referralPoints = referralRule != null ? referralRule.getPointAmount() : new BigDecimal("100");

            if (referrer.getStatus() != 0) {
                BigDecimal balanceBefore = referrer.getCurrentPoints();
                BigDecimal balanceAfter = balanceBefore.add(referralPoints);

                referrer.setCurrentPoints(balanceAfter);
                referrer.setTotalPoints(referrer.getTotalPoints().add(referralPoints));
                referrer.setUpdateTime(LocalDateTime.now());
                memberMapper.update(referrer);

                MemberPointLog refLog = new MemberPointLog();
                refLog.setMemberId(referrer.getId());
                refLog.setMemberNo(referrer.getMemberNo());
                refLog.setPointType(1);
                refLog.setPoints(referralPoints);
                refLog.setBalanceBefore(balanceBefore);
                refLog.setBalanceAfter(balanceAfter);
                refLog.setReasonType(4);
                refLog.setReason("推荐积分");
                refLog.setDetail("推荐会员" + member.getMemberNo() + "（" + member.getCustomerName() + "）注册，获得" + referralPoints + "积分");
                refLog.setRelatedOrderType(2);
                refLog.setRelatedOrderId(member.getId());
                refLog.setOperatorId(operatorId);
                refLog.setOperatorName(operatorName);
                refLog.setCreateTime(LocalDateTime.now());
                memberPointLogMapper.insert(refLog);

                checkAndUpgradeLevel(referrer, operatorId, operatorName);
            }
        }

        return member;
    }

    @Transactional
    public Member registerNewMember(Customer customer, Integer registerSource, Long referrerId,
                                    Long operatorId, String operatorName) {
        customer.setDeleted(0);
        customer.setCreateTime(LocalDateTime.now());
        customer.setStatus(1);
        customer.setCustomerType(1);
        customerMapper.insert(customer);

        return registerFromCustomer(customer.getId(), registerSource, referrerId, operatorId, operatorName);
    }

    @Transactional
    public void updateMember(Member member) {
        Member existing = memberMapper.selectOneById(member.getId());
        if (existing == null || existing.getDeleted() == 1) {
            throw new BusinessException("会员不存在");
        }

        existing.setUpdateTime(LocalDateTime.now());
        memberMapper.update(existing);
    }

    @Transactional
    public void addPoints(Long memberId, BigDecimal points, Integer reasonType, String reason,
                          String detail, Long operatorId, String operatorName) {
        if (points == null || points.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("积分数量必须大于0");
        }

        Member member = memberMapper.selectOneById(memberId);
        if (member == null || member.getDeleted() == 1) {
            throw new BusinessException("会员不存在");
        }
        if (member.getStatus() == 0) {
            throw new BusinessException("会员已冻结，无法发放积分");
        }

        BigDecimal balanceBefore = member.getCurrentPoints();
        BigDecimal balanceAfter = balanceBefore.add(points);

        member.setCurrentPoints(balanceAfter);
        member.setTotalPoints(member.getTotalPoints().add(points));
        member.setUpdateTime(LocalDateTime.now());
        memberMapper.update(member);

        MemberPointLog log = new MemberPointLog();
        log.setMemberId(memberId);
        log.setMemberNo(member.getMemberNo());
        log.setPointType(1);
        log.setPoints(points);
        log.setBalanceBefore(balanceBefore);
        log.setBalanceAfter(balanceAfter);
        log.setReasonType(reasonType);
        log.setReason(reason);
        log.setDetail(detail);
        log.setOperatorId(operatorId);
        log.setOperatorName(operatorName);
        log.setCreateTime(LocalDateTime.now());
        memberPointLogMapper.insert(log);

        checkAndUpgradeLevel(member, operatorId, operatorName);
    }

    @Transactional
    public void usePoints(Long memberId, BigDecimal points, Integer reasonType, String reason,
                          String detail, Long operatorId, String operatorName) {
        if (points == null || points.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("积分数量必须大于0");
        }

        Member member = memberMapper.selectOneById(memberId);
        if (member == null || member.getDeleted() == 1) {
            throw new BusinessException("会员不存在");
        }
        if (member.getStatus() == 0) {
            throw new BusinessException("会员已冻结，无法使用积分");
        }
        if (member.getCurrentPoints().compareTo(points) < 0) {
            throw new BusinessException("积分不足");
        }

        BigDecimal balanceBefore = member.getCurrentPoints();
        BigDecimal balanceAfter = balanceBefore.subtract(points);

        member.setCurrentPoints(balanceAfter);
        member.setUpdateTime(LocalDateTime.now());
        memberMapper.update(member);

        MemberPointLog log = new MemberPointLog();
        log.setMemberId(memberId);
        log.setMemberNo(member.getMemberNo());
        log.setPointType(2);
        log.setPoints(points);
        log.setBalanceBefore(balanceBefore);
        log.setBalanceAfter(balanceAfter);
        log.setReasonType(reasonType);
        log.setReason(reason);
        log.setDetail(detail);
        log.setOperatorId(operatorId);
        log.setOperatorName(operatorName);
        log.setCreateTime(LocalDateTime.now());
        memberPointLogMapper.insert(log);
    }

    @Transactional
    public void refundPoints(Long memberId, BigDecimal points, Integer reasonType, String reason,
                             String detail, Long operatorId, String operatorName) {
        if (points == null || points.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("退还积分数量必须大于0");
        }

        Member member = memberMapper.selectOneById(memberId);
        if (member == null || member.getDeleted() == 1) {
            throw new BusinessException("会员不存在");
        }

        BigDecimal balanceBefore = member.getCurrentPoints();
        BigDecimal balanceAfter = balanceBefore.add(points);

        member.setCurrentPoints(balanceAfter);
        member.setUpdateTime(LocalDateTime.now());
        memberMapper.update(member);

        MemberPointLog log = new MemberPointLog();
        log.setMemberId(memberId);
        log.setMemberNo(member.getMemberNo());
        log.setPointType(1);
        log.setPoints(points);
        log.setBalanceBefore(balanceBefore);
        log.setBalanceAfter(balanceAfter);
        log.setReasonType(reasonType);
        log.setReason(reason);
        log.setDetail(detail);
        log.setOperatorId(operatorId);
        log.setOperatorName(operatorName);
        log.setCreateTime(LocalDateTime.now());
        memberPointLogMapper.insert(log);
    }

    @Transactional
    public void adjustLevel(Long memberId, Long newLevelId, String reason,
                            Long operatorId, String operatorName) {
        Member member = memberMapper.selectOneById(memberId);
        if (member == null || member.getDeleted() == 1) {
            throw new BusinessException("会员不存在");
        }

        MemberLevel newLevel = memberLevelMapper.selectOneById(newLevelId);
        if (newLevel == null || newLevel.getDeleted() == 1) {
            throw new BusinessException("目标等级不存在");
        }

        Long oldLevelId = member.getLevelId();
        String oldLevelName = member.getLevelName();

        member.setLevelId(newLevelId);
        member.setLevelName(newLevel.getLevelName());
        member.setUpdateTime(LocalDateTime.now());
        memberMapper.update(member);

        MemberLevelLog levelLog = new MemberLevelLog();
        levelLog.setMemberId(memberId);
        levelLog.setMemberNo(member.getMemberNo());
        levelLog.setChangeType(3);
        levelLog.setOldLevelId(oldLevelId);
        levelLog.setOldLevelName(oldLevelName);
        levelLog.setNewLevelId(newLevelId);
        levelLog.setNewLevelName(newLevel.getLevelName());
        levelLog.setReason(reason);
        levelLog.setOperatorId(operatorId);
        levelLog.setOperatorName(operatorName);
        levelLog.setCreateTime(LocalDateTime.now());
        memberLevelLogMapper.insert(levelLog);
    }

    @Transactional
    public void freezeMember(Long memberId, String reason, Long operatorId, String operatorName) {
        Member member = memberMapper.selectOneById(memberId);
        if (member == null || member.getDeleted() == 1) {
            throw new BusinessException("会员不存在");
        }
        if (member.getStatus() == 0) {
            throw new BusinessException("会员已冻结");
        }

        member.setStatus(0);
        member.setFreezeReason(reason);
        member.setFreezeTime(LocalDateTime.now());
        member.setFreezeOperatorId(operatorId);
        member.setFreezeOperatorName(operatorName);
        member.setUpdateTime(LocalDateTime.now());
        memberMapper.update(member);
    }

    @Transactional
    public void unfreezeMember(Long memberId, String reason, Long operatorId, String operatorName) {
        Member member = memberMapper.selectOneById(memberId);
        if (member == null || member.getDeleted() == 1) {
            throw new BusinessException("会员不存在");
        }
        if (member.getStatus() == 1) {
            throw new BusinessException("会员未冻结");
        }

        member.setStatus(1);
        member.setUpdateTime(LocalDateTime.now());
        memberMapper.update(member);
    }

    public List<MemberPointLog> getPointLogs(Long memberId) {
        QueryWrapper query = QueryWrapper.create()
                .from(MemberPointLog.class)
                .where(MEMBER_POINT_LOG.MEMBER_ID.eq(memberId))
                .orderBy(MEMBER_POINT_LOG.CREATE_TIME.desc());
        return memberPointLogMapper.selectListByQuery(query);
    }

    public PageResult<MemberPointLog> getPointLogPage(Integer pageNum, Integer pageSize, Long memberId) {
        QueryWrapper query = QueryWrapper.create()
                .from(MemberPointLog.class)
                .where(MEMBER_POINT_LOG.MEMBER_ID.eq(memberId))
                .orderBy(MEMBER_POINT_LOG.CREATE_TIME.desc());
        Page<MemberPointLog> page = memberPointLogMapper.paginate(pageNum, pageSize, query);
        return new PageResult<>(page.getTotalRow(), page.getRecords(), (long) pageNum, (long) pageSize);
    }

    public List<MemberLevelLog> getLevelLogs(Long memberId) {
        QueryWrapper query = QueryWrapper.create()
                .from(MemberLevelLog.class)
                .where(MEMBER_LEVEL_LOG.MEMBER_ID.eq(memberId))
                .orderBy(MEMBER_LEVEL_LOG.CREATE_TIME.desc());
        return memberLevelLogMapper.selectListByQuery(query);
    }

    public PageResult<MemberLevelLog> getLevelLogPage(Integer pageNum, Integer pageSize, Long memberId) {
        QueryWrapper query = QueryWrapper.create()
                .from(MemberLevelLog.class)
                .where(MEMBER_LEVEL_LOG.MEMBER_ID.eq(memberId))
                .orderBy(MEMBER_LEVEL_LOG.CREATE_TIME.desc());
        Page<MemberLevelLog> page = memberLevelLogMapper.paginate(pageNum, pageSize, query);
        return new PageResult<>(page.getTotalRow(), page.getRecords(), (long) pageNum, (long) pageSize);
    }

    public Map<String, Object> getStatistics() {
        Map<String, Object> result = new HashMap<>();

        long totalMembers = memberMapper.selectCountByQuery(
                QueryWrapper.create().from(Member.class).where(MEMBER.DELETED.eq(0))
        );

        LocalDateTime monthStart = LocalDate.now().withDayOfMonth(1).atStartOfDay();
        long newMembersThisMonth = memberMapper.selectCountByQuery(
                QueryWrapper.create()
                        .from(Member.class)
                        .where(MEMBER.DELETED.eq(0))
                        .and(MEMBER.REGISTER_TIME.ge(monthStart))
        );

        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        long activeMembersThisMonth = memberMapper.selectCountByQuery(
                QueryWrapper.create()
                        .from(Member.class)
                        .where(MEMBER.DELETED.eq(0))
                        .and(MEMBER.LAST_STAY_TIME.ge(monthStart))
        );

        result.put("totalMembers", totalMembers);
        result.put("newMembersThisMonth", newMembersThisMonth);
        result.put("activeMembersThisMonth", activeMembersThisMonth);

        List<Map<String, Object>> levelDistribution = getLevelDistribution();
        result.put("levelDistribution", levelDistribution);

        List<Map<String, Object>> growthTrend = getGrowthTrend();
        result.put("growthTrend", growthTrend);

        return result;
    }

    public List<Map<String, Object>> getLevelDistribution() {
        List<Map<String, Object>> list = new java.util.ArrayList<>();
        long total = memberMapper.selectCountByQuery(
                QueryWrapper.create().from(Member.class).where(MEMBER.DELETED.eq(0))
        );

        QueryWrapper levelQuery = QueryWrapper.create()
                .from(MemberLevel.class)
                .where(MEMBER_LEVEL.DELETED.eq(0))
                .orderBy(MEMBER_LEVEL.SORT_ORDER.asc());
        List<MemberLevel> levels = memberLevelMapper.selectListByQuery(levelQuery);

        for (MemberLevel level : levels) {
            long memberCount = memberMapper.selectCountByQuery(
                    QueryWrapper.create()
                            .from(Member.class)
                            .where(MEMBER.DELETED.eq(0))
                            .and(MEMBER.LEVEL_ID.eq(level.getId()))
            );
            Map<String, Object> item = new HashMap<>();
            item.put("id", level.getId());
            item.put("levelName", level.getLevelName());
            item.put("levelColor", level.getLevelColor());
            item.put("levelIcon", level.getLevelIcon());
            item.put("count", memberCount);
            item.put("percentage", total > 0 ? (double) memberCount / total * 100 : 0);
            list.add(item);
        }

        return list;
    }

    public List<Map<String, Object>> getGrowthTrend() {
        List<Map<String, Object>> trend = new java.util.ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM");

        for (int i = 11; i >= 0; i--) {
            LocalDate monthDate = LocalDate.now().minusMonths(i).withDayOfMonth(1);
            String monthStr = monthDate.format(formatter);
            LocalDateTime monthStart = monthDate.atStartOfDay();
            LocalDateTime monthEnd = monthDate.plusMonths(1).atStartOfDay().minusNanos(1);

            long count = memberMapper.selectCountByQuery(
                    QueryWrapper.create()
                            .from(Member.class)
                            .where(MEMBER.DELETED.eq(0))
                            .and(MEMBER.REGISTER_TIME.between(monthStart, monthEnd))
            );

            Map<String, Object> item = new HashMap<>();
            item.put("month", monthStr);
            item.put("count", count);
            trend.add(item);
        }

        return trend;
    }

    private void enrichMemberInfo(Member member) {
        MemberLevel level = memberLevelMapper.selectOneById(member.getLevelId());
        if (level != null) {
            member.setLevelIcon(level.getLevelIcon());
            member.setLevelColor(level.getLevelColor());
            member.setMemberLevel(level);
        }

        Customer customer = customerMapper.selectOneById(member.getCustomerId());
        if (customer != null) {
            member.setCustomer(customer);
        }
    }

    private MemberLevel getLowestLevel() {
        QueryWrapper query = QueryWrapper.create()
                .from(MemberLevel.class)
                .where(MEMBER_LEVEL.DELETED.eq(0))
                .and(MEMBER_LEVEL.STATUS.eq(1))
                .orderBy(MEMBER_LEVEL.SORT_ORDER.asc())
                .limit(1);
        MemberLevel level = memberLevelMapper.selectOneByQuery(query);
        if (level == null) {
            throw new BusinessException("没有可用的会员等级");
        }
        return level;
    }

    private synchronized String generateMemberNo() {
        String prefix = "MB";
        String dateStr = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String prefixDate = prefix + dateStr;

        QueryWrapper query = QueryWrapper.create()
                .from(Member.class)
                .where(MEMBER.MEMBER_NO.like(prefixDate + "%"))
                .orderBy(MEMBER.MEMBER_NO.desc())
                .limit(1);
        Member lastMember = memberMapper.selectOneByQuery(query);

        int sequence = 1;
        if (lastMember != null) {
            String lastNo = lastMember.getMemberNo();
            String seqStr = lastNo.substring(prefixDate.length());
            try {
                sequence = Integer.parseInt(seqStr) + 1;
            } catch (NumberFormatException e) {
                sequence = 1;
            }
        }

        return prefixDate + String.format("%05d", sequence);
    }

    private void checkAndUpgradeLevel(Member member, Long operatorId, String operatorName) {
        MemberLevel currentLevel = memberLevelMapper.selectOneById(member.getLevelId());
        if (currentLevel == null) return;

        QueryWrapper query = QueryWrapper.create()
                .from(MemberLevel.class)
                .where(MEMBER_LEVEL.DELETED.eq(0))
                .and(MEMBER_LEVEL.STATUS.eq(1))
                .and(MEMBER_LEVEL.SORT_ORDER.gt(currentLevel.getSortOrder()))
                .orderBy(MEMBER_LEVEL.SORT_ORDER.asc());
        List<MemberLevel> higherLevels = memberLevelMapper.selectListByQuery(query);

        for (MemberLevel level : higherLevels) {
            boolean canUpgrade = false;
            if (level.getUpgradeType() == 1) {
                if (member.getTotalSpent().compareTo(level.getUpgradeCondition()) >= 0) {
                    canUpgrade = true;
                }
            } else if (level.getUpgradeType() == 2) {
                if (member.getTotalPoints().compareTo(level.getUpgradeCondition()) >= 0) {
                    canUpgrade = true;
                }
            }

            if (canUpgrade) {
                Long oldLevelId = member.getLevelId();
                String oldLevelName = member.getLevelName();

                member.setLevelId(level.getId());
                member.setLevelName(level.getLevelName());
                memberMapper.update(member);

                MemberLevelLog levelLog = new MemberLevelLog();
                levelLog.setMemberId(member.getId());
                levelLog.setMemberNo(member.getMemberNo());
                levelLog.setChangeType(1);
                levelLog.setOldLevelId(oldLevelId);
                levelLog.setOldLevelName(oldLevelName);
                levelLog.setNewLevelId(level.getId());
                levelLog.setNewLevelName(level.getLevelName());
                levelLog.setReason("自动升级");
                levelLog.setOperatorId(operatorId);
                levelLog.setOperatorName(operatorName);
                levelLog.setCreateTime(LocalDateTime.now());
                memberLevelLogMapper.insert(levelLog);

                break;
            }
        }
    }

    @Transactional
    public void batchAddPoints(List<Long> memberIds, BigDecimal points, Integer reasonType, String reason,
                               Long operatorId, String operatorName) {
        for (Long memberId : memberIds) {
            try {
                addPoints(memberId, points, reasonType, reason, null, operatorId, operatorName);
            } catch (Exception e) {
            }
        }
    }

    @Transactional
    public Map<String, Object> earnPointsOnCheckout(Long memberId, BigDecimal consumeAmount,
                                                     Long operatorId, String operatorName,
                                                     Integer relatedOrderType, Long relatedOrderId) {
        Member member = memberMapper.selectOneById(memberId);
        if (member == null || member.getDeleted() == 1) {
            throw new BusinessException("会员不存在");
        }
        if (member.getStatus() == 0) {
            throw new BusinessException("会员已冻结，无法发放积分");
        }

        MemberLevel level = memberLevelMapper.selectOneById(member.getLevelId());
        BigDecimal pointRate = level != null && level.getPointRate() != null ? level.getPointRate() : BigDecimal.ONE;

        BigDecimal earnedPoints = pointRuleService.calculateEarnPoints(consumeAmount, pointRate, 1);

        Map<String, Object> result = new HashMap<>();
        result.put("consumeAmount", consumeAmount);
        result.put("basePoints", consumeAmount);
        result.put("pointRate", pointRate);
        result.put("levelName", level != null ? level.getLevelName() : "普通会员");
        result.put("earnedPoints", earnedPoints);

        if (earnedPoints.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal balanceBefore = member.getCurrentPoints();
            BigDecimal balanceAfter = balanceBefore.add(earnedPoints);

            member.setCurrentPoints(balanceAfter);
            member.setTotalPoints(member.getTotalPoints().add(earnedPoints));
            member.setUpdateTime(LocalDateTime.now());
            memberMapper.update(member);

            MemberPointLog log = new MemberPointLog();
            log.setMemberId(memberId);
            log.setMemberNo(member.getMemberNo());
            log.setPointType(1);
            log.setPoints(earnedPoints);
            log.setBalanceBefore(balanceBefore);
            log.setBalanceAfter(balanceAfter);
            log.setReasonType(1);
            log.setReason("消费赠送");
            log.setDetail("消费金额" + consumeAmount + "元，基础积分" + consumeAmount.intValue() + "，等级倍率" + pointRate + "，实际获得" + earnedPoints + "积分");
            log.setRelatedOrderType(relatedOrderType);
            log.setRelatedOrderId(relatedOrderId);
            log.setOperatorId(operatorId);
            log.setOperatorName(operatorName);
            log.setCreateTime(LocalDateTime.now());
            memberPointLogMapper.insert(log);

            checkAndUpgradeLevel(member, operatorId, operatorName);

            result.put("balanceAfter", balanceAfter);
        } else {
            result.put("balanceAfter", member.getCurrentPoints());
        }

        return result;
    }

    @Transactional
    public Map<String, Object> usePointsWithRule(Long memberId, BigDecimal points, BigDecimal orderAmount,
                                                  Long operatorId, String operatorName,
                                                  Integer relatedOrderType, Long relatedOrderId) {
        if (points == null || points.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("积分数量必须大于0");
        }

        Member member = memberMapper.selectOneById(memberId);
        if (member == null || member.getDeleted() == 1) {
            throw new BusinessException("会员不存在");
        }
        if (member.getStatus() == 0) {
            throw new BusinessException("会员已冻结，无法使用积分");
        }
        if (member.getCurrentPoints().compareTo(points) < 0) {
            throw new BusinessException("积分不足，当前可用积分：" + member.getCurrentPoints());
        }

        PointConsumeRule rule = pointRuleService.getEnabledConsumeRuleByType(1);
        if (rule == null) {
            throw new BusinessException("未配置积分抵现规则或规则未启用");
        }

        if (rule.getMaxPointsPerUse() != null && rule.getMaxPointsPerUse().compareTo(BigDecimal.ZERO) > 0) {
            if (points.compareTo(rule.getMaxPointsPerUse()) > 0) {
                throw new BusinessException("单次最多使用" + rule.getMaxPointsPerUse() + "积分");
            }
        }

        if (rule.getMinOrderAmount() != null && rule.getMinOrderAmount().compareTo(BigDecimal.ZERO) > 0) {
            if (orderAmount == null || orderAmount.compareTo(rule.getMinOrderAmount()) < 0) {
                throw new BusinessException("订单金额需满" + rule.getMinOrderAmount() + "元才能使用积分");
            }
        }

        BigDecimal deductionAmount = BigDecimal.ZERO;
        if (rule.getExchangePoints() != null && rule.getExchangePoints().compareTo(BigDecimal.ZERO) > 0
                && rule.getExchangeAmount() != null && rule.getExchangeAmount().compareTo(BigDecimal.ZERO) > 0) {
            deductionAmount = points.multiply(rule.getExchangeAmount()).divide(rule.getExchangePoints(), 2, BigDecimal.ROUND_DOWN);
        }

        if (rule.getDeductionCap() != null && rule.getDeductionCap().compareTo(BigDecimal.ZERO) > 0
                && orderAmount != null && orderAmount.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal maxDeduction = orderAmount.multiply(rule.getDeductionCap()).divide(new BigDecimal("100"), 2, BigDecimal.ROUND_DOWN);
            if (deductionAmount.compareTo(maxDeduction) > 0) {
                deductionAmount = maxDeduction;
            }
        }

        BigDecimal balanceBefore = member.getCurrentPoints();
        BigDecimal balanceAfter = balanceBefore.subtract(points);

        member.setCurrentPoints(balanceAfter);
        member.setUpdateTime(LocalDateTime.now());
        memberMapper.update(member);

        MemberPointLog log = new MemberPointLog();
        log.setMemberId(memberId);
        log.setMemberNo(member.getMemberNo());
        log.setPointType(2);
        log.setPoints(points);
        log.setBalanceBefore(balanceBefore);
        log.setBalanceAfter(balanceAfter);
        log.setReasonType(6);
        log.setReason("积分抵现");
        log.setDetail("使用" + points + "积分抵扣" + deductionAmount + "元");
        log.setRelatedOrderType(relatedOrderType);
        log.setRelatedOrderId(relatedOrderId);
        log.setOperatorId(operatorId);
        log.setOperatorName(operatorName);
        log.setCreateTime(LocalDateTime.now());
        memberPointLogMapper.insert(log);

        Map<String, Object> result = new HashMap<>();
        result.put("pointsUsed", points);
        result.put("deductionAmount", deductionAmount);
        result.put("balanceAfter", balanceAfter);
        result.put("payableAmount", orderAmount != null ? orderAmount.subtract(deductionAmount) : deductionAmount.negate());

        return result;
    }

    public PageResult<MemberPointLog> getPointLogPageFiltered(Integer pageNum, Integer pageSize, Long memberId,
                                                               Integer pointType, String startTime, String endTime) {
        QueryWrapper query = QueryWrapper.create()
                .from(MemberPointLog.class)
                .where(MEMBER_POINT_LOG.MEMBER_ID.eq(memberId));

        if (pointType != null) {
            query.and(MEMBER_POINT_LOG.POINT_TYPE.eq(pointType));
        }
        if (startTime != null && !startTime.isEmpty()) {
            query.and(MEMBER_POINT_LOG.CREATE_TIME.ge(LocalDateTime.parse(startTime + " 00:00:00",
                    DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))));
        }
        if (endTime != null && !endTime.isEmpty()) {
            query.and(MEMBER_POINT_LOG.CREATE_TIME.le(LocalDateTime.parse(endTime + " 23:59:59",
                    DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))));
        }

        query.orderBy(MEMBER_POINT_LOG.CREATE_TIME.desc());
        Page<MemberPointLog> page = memberPointLogMapper.paginate(pageNum, pageSize, query);
        return new PageResult<>(page.getTotalRow(), page.getRecords(), (long) pageNum, (long) pageSize);
    }

    public Map<String, Object> getMemberPointSummary(Long memberId) {
        Member member = memberMapper.selectOneById(memberId);
        if (member == null || member.getDeleted() == 1) {
            throw new BusinessException("会员不存在");
        }

        Map<String, Object> summary = new HashMap<>();
        summary.put("currentPoints", member.getCurrentPoints());

        BigDecimal totalEarned = BigDecimal.ZERO;
        QueryWrapper earnQuery = QueryWrapper.create()
                .from(MemberPointLog.class)
                .where(MEMBER_POINT_LOG.MEMBER_ID.eq(memberId))
                .and(MEMBER_POINT_LOG.POINT_TYPE.eq(1));
        List<MemberPointLog> earnLogs = memberPointLogMapper.selectListByQuery(earnQuery);
        for (MemberPointLog log : earnLogs) {
            if (log.getPoints() != null) {
                totalEarned = totalEarned.add(log.getPoints());
            }
        }
        summary.put("totalEarned", totalEarned);

        BigDecimal totalUsed = BigDecimal.ZERO;
        QueryWrapper useQuery = QueryWrapper.create()
                .from(MemberPointLog.class)
                .where(MEMBER_POINT_LOG.MEMBER_ID.eq(memberId))
                .and(MEMBER_POINT_LOG.POINT_TYPE.eq(2));
        List<MemberPointLog> useLogs = memberPointLogMapper.selectListByQuery(useQuery);
        for (MemberPointLog log : useLogs) {
            if (log.getPoints() != null) {
                totalUsed = totalUsed.add(log.getPoints());
            }
        }
        summary.put("totalUsed", totalUsed);

        return summary;
    }
}
