package com.example.permission.service;

import com.example.permission.common.PageResult;
import com.example.permission.entity.*;
import com.example.permission.mapper.*;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

import static com.example.permission.entity.table.BookingTableDef.BOOKING;
import static com.example.permission.entity.table.CheckInTableDef.CHECK_IN;
import static com.example.permission.entity.table.MemberTableDef.MEMBER;
import static com.example.permission.entity.table.MemberLevelTableDef.MEMBER_LEVEL;
import static com.example.permission.entity.table.MemberLevelLogTableDef.MEMBER_LEVEL_LOG;
import static com.example.permission.entity.table.MemberBenefitLogTableDef.MEMBER_BENEFIT_LOG;
import static com.example.permission.entity.table.MemberPointLogTableDef.MEMBER_POINT_LOG;

@Service
public class MemberAnalyticsService {

    @Autowired
    private MemberMapper memberMapper;

    @Autowired
    private MemberLevelMapper memberLevelMapper;

    @Autowired
    private MemberLevelLogMapper memberLevelLogMapper;

    @Autowired
    private MemberBenefitLogMapper memberBenefitLogMapper;

    @Autowired
    private BookingMapper bookingMapper;

    @Autowired
    private CheckInMapper checkInMapper;

    @Autowired
    private CustomerMapper customerMapper;

    // ==================== 1. 会员价值分析 ====================

    public Map<String, Object> getMemberValueAnalysis() {
        Map<String, Object> result = new HashMap<>();
        result.put("memberVsNonMember", getMemberVsNonMember());
        result.put("revenueContribution", getMemberRevenueContribution());
        result.put("ltvAnalysis", getMemberLTVAnalysis());
        return result;
    }

    public Map<String, Object> getMemberVsNonMember() {
        Map<String, Object> result = new HashMap<>();

        long totalMembers = memberMapper.selectCountByQuery(
                QueryWrapper.create().from(Member.class).where(MEMBER.DELETED.eq(0)).and(MEMBER.STATUS.eq(1))
        );

        long totalNonMembers = customerMapper.selectCountByQuery(
                QueryWrapper.create().from(Customer.class)
                        .where("id NOT IN (SELECT customer_id FROM member WHERE deleted = 0)")
        );

        QueryWrapper memberCheckInQuery = QueryWrapper.create()
                .from(CheckIn.class)
                .where(CHECK_IN.MEMBER_ID.isNotNull())
                .and(CHECK_IN.DELETED.eq(0))
                .and(CHECK_IN.STATUS.eq(3));

        long memberCheckInCount = checkInMapper.selectCountByQuery(memberCheckInQuery);

        QueryWrapper nonMemberCheckInQuery = QueryWrapper.create()
                .from(CheckIn.class)
                .where(CHECK_IN.MEMBER_ID.isNull())
                .and(CHECK_IN.DELETED.eq(0))
                .and(CHECK_IN.STATUS.eq(3));

        long nonMemberCheckInCount = checkInMapper.selectCountByQuery(nonMemberCheckInQuery);

        BigDecimal memberTotalAmount = getTotalAmountByQuery(memberCheckInQuery);
        BigDecimal nonMemberTotalAmount = getTotalAmountByQuery(nonMemberCheckInQuery);

        BigDecimal memberAvgPerOrder = memberCheckInCount > 0
                ? memberTotalAmount.divide(BigDecimal.valueOf(memberCheckInCount), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        BigDecimal nonMemberAvgPerOrder = nonMemberCheckInCount > 0
                ? nonMemberTotalAmount.divide(BigDecimal.valueOf(nonMemberCheckInCount), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        BigDecimal avgDiff = memberAvgPerOrder.subtract(nonMemberAvgPerOrder);
        BigDecimal avgDiffPercent = nonMemberAvgPerOrder.compareTo(BigDecimal.ZERO) > 0
                ? avgDiff.divide(nonMemberAvgPerOrder, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100))
                : BigDecimal.ZERO;

        BigDecimal memberAvgStays = totalMembers > 0
                ? BigDecimal.valueOf(memberCheckInCount).divide(BigDecimal.valueOf(totalMembers), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        BigDecimal nonMemberAvgStays = totalNonMembers > 0
                ? BigDecimal.valueOf(nonMemberCheckInCount).divide(BigDecimal.valueOf(totalNonMembers), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        long memberRepeatCount = getRepeatCustomerCount(true);
        long nonMemberRepeatCount = getRepeatCustomerCount(false);

        BigDecimal memberRepeatRate = totalMembers > 0
                ? BigDecimal.valueOf(memberRepeatCount).divide(BigDecimal.valueOf(totalMembers), 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100))
                : BigDecimal.ZERO;

        BigDecimal nonMemberRepeatRate = totalNonMembers > 0
                ? BigDecimal.valueOf(nonMemberRepeatCount).divide(BigDecimal.valueOf(totalNonMembers), 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100))
                : BigDecimal.ZERO;

        BigDecimal repeatRateDiff = memberRepeatRate.subtract(nonMemberRepeatRate);

        result.put("totalMembers", totalMembers);
        result.put("totalNonMembers", totalNonMembers);
        result.put("memberCheckInCount", memberCheckInCount);
        result.put("nonMemberCheckInCount", nonMemberCheckInCount);

        result.put("memberAvgPerOrder", memberAvgPerOrder);
        result.put("nonMemberAvgPerOrder", nonMemberAvgPerOrder);
        result.put("avgDiff", avgDiff);
        result.put("avgDiffPercent", avgDiffPercent);

        result.put("memberAvgStays", memberAvgStays);
        result.put("nonMemberAvgStays", nonMemberAvgStays);
        result.put("avgStaysDiff", memberAvgStays.subtract(nonMemberAvgStays));

        result.put("memberRepeatCount", memberRepeatCount);
        result.put("nonMemberRepeatCount", nonMemberRepeatCount);
        result.put("memberRepeatRate", memberRepeatRate);
        result.put("nonMemberRepeatRate", nonMemberRepeatRate);
        result.put("repeatRateDiff", repeatRateDiff);

        return result;
    }

    private BigDecimal getTotalAmountByQuery(QueryWrapper query) {
        List<CheckIn> list = checkInMapper.selectListByQuery(query);
        return list.stream()
                .map(CheckIn::getTotalAmount)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private long getRepeatCustomerCount(boolean isMember) {
        QueryWrapper query = QueryWrapper.create()
                .select("customer_id, COUNT(*) as stay_count")
                .from(CheckIn.class)
                .where(CHECK_IN.DELETED.eq(0))
                .and(CHECK_IN.STATUS.eq(3));

        if (isMember) {
            query.and(CHECK_IN.MEMBER_ID.isNotNull());
        } else {
            query.and(CHECK_IN.MEMBER_ID.isNull());
        }

        query.groupBy("customer_id");
        query.having("COUNT(*) >= 2");

        List<Map<String, Object>> result = checkInMapper.selectListByQueryAs(query, Map.class);
        return result.size();
    }

    public Map<String, Object> getMemberRevenueContribution() {
        Map<String, Object> result = new HashMap<>();

        QueryWrapper allCheckInQuery = QueryWrapper.create()
                .from(CheckIn.class)
                .where(CHECK_IN.DELETED.eq(0))
                .and(CHECK_IN.STATUS.eq(3));

        QueryWrapper memberCheckInQuery = QueryWrapper.create()
                .from(CheckIn.class)
                .where(CHECK_IN.MEMBER_ID.isNotNull())
                .and(CHECK_IN.DELETED.eq(0))
                .and(CHECK_IN.STATUS.eq(3));

        BigDecimal totalRevenue = getTotalAmountByQuery(allCheckInQuery);
        BigDecimal memberRevenue = getTotalAmountByQuery(memberCheckInQuery);

        BigDecimal revenueRatio = totalRevenue.compareTo(BigDecimal.ZERO) > 0
                ? memberRevenue.divide(totalRevenue, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100))
                : BigDecimal.ZERO;

        List<Map<String, Object>> monthlyTrend = getMemberRevenueMonthlyTrend();

        result.put("memberTotalRevenue", memberRevenue);
        result.put("totalRevenue", totalRevenue);
        result.put("revenueRatio", revenueRatio);
        result.put("monthlyTrend", monthlyTrend);
        result.put("yoyGrowth", null);

        return result;
    }

    private List<Map<String, Object>> getMemberRevenueMonthlyTrend() {
        List<Map<String, Object>> result = new ArrayList<>();
        LocalDate now = LocalDate.now();

        for (int i = 11; i >= 0; i--) {
            LocalDate monthStart = now.minusMonths(i).withDayOfMonth(1);
            LocalDate monthEnd = monthStart.plusMonths(1).minusDays(1);

            QueryWrapper query = QueryWrapper.create()
                    .from(CheckIn.class)
                    .where(CHECK_IN.MEMBER_ID.isNotNull())
                    .and(CHECK_IN.DELETED.eq(0))
                    .and(CHECK_IN.STATUS.eq(3))
                    .and(CHECK_IN.CHECK_IN_DATE.ge(monthStart))
                    .and(CHECK_IN.CHECK_IN_DATE.le(monthEnd));

            BigDecimal monthRevenue = getTotalAmountByQuery(query);

            QueryWrapper allQuery = QueryWrapper.create()
                    .from(CheckIn.class)
                    .where(CHECK_IN.DELETED.eq(0))
                    .and(CHECK_IN.STATUS.eq(3))
                    .and(CHECK_IN.CHECK_IN_DATE.ge(monthStart))
                    .and(CHECK_IN.CHECK_IN_DATE.le(monthEnd));

            BigDecimal allRevenue = getTotalAmountByQuery(allQuery);

            BigDecimal ratio = allRevenue.compareTo(BigDecimal.ZERO) > 0
                    ? monthRevenue.divide(allRevenue, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100))
                    : BigDecimal.ZERO;

            Map<String, Object> item = new HashMap<>();
            item.put("month", monthStart.getYear() + "-" + String.format("%02d", monthStart.getMonthValue()));
            item.put("memberRevenue", monthRevenue);
            item.put("totalRevenue", allRevenue);
            item.put("ratio", ratio);
            result.add(item);
        }

        return result;
    }

    public Map<String, Object> getMemberLTVAnalysis() {
        Map<String, Object> result = new HashMap<>();

        QueryWrapper query = QueryWrapper.create()
                .from(Member.class)
                .where(MEMBER.DELETED.eq(0))
                .and(MEMBER.STATUS.eq(1));

        List<Member> members = memberMapper.selectListByQuery(query);
        long totalMembers = members.size();

        BigDecimal totalSpent = members.stream()
                .map(Member::getTotalSpent)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal avgLTV = totalMembers > 0
                ? totalSpent.divide(BigDecimal.valueOf(totalMembers), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        List<Map<String, Object>> ltvByLevel = getLTVByLevel(members);

        List<Map<String, Object>> ltvDistribution = getLTVDistribution(members);

        result.put("avgLTV", avgLTV);
        result.put("totalMembers", totalMembers);
        result.put("totalLTV", totalSpent);
        result.put("ltvByLevel", ltvByLevel);
        result.put("ltvDistribution", ltvDistribution);

        return result;
    }

    private List<Map<String, Object>> getLTVByLevel(List<Member> members) {
        Map<Long, List<Member>> levelGroups = members.stream()
                .collect(Collectors.groupingBy(Member::getLevelId));

        List<MemberLevel> levels = memberLevelMapper.selectListByQuery(
                QueryWrapper.create().from(MemberLevel.class).where(MEMBER_LEVEL.STATUS.eq(1))
                        .orderBy(MEMBER_LEVEL.SORT_ORDER.asc())
        );

        List<Map<String, Object>> result = new ArrayList<>();
        for (MemberLevel level : levels) {
            List<Member> levelMembers = levelGroups.getOrDefault(level.getId(), Collections.emptyList());
            BigDecimal levelTotalSpent = levelMembers.stream()
                    .map(Member::getTotalSpent)
                    .filter(Objects::nonNull)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal avgLTV = !levelMembers.isEmpty()
                    ? levelTotalSpent.divide(BigDecimal.valueOf(levelMembers.size()), 2, RoundingMode.HALF_UP)
                    : BigDecimal.ZERO;

            Map<String, Object> item = new HashMap<>();
            item.put("levelId", level.getId());
            item.put("levelName", level.getLevelName());
            item.put("levelColor", level.getLevelColor());
            item.put("levelIcon", level.getLevelIcon());
            item.put("memberCount", levelMembers.size());
            item.put("totalSpent", levelTotalSpent);
            item.put("avgLTV", avgLTV);
            result.add(item);
        }

        return result;
    }

    private List<Map<String, Object>> getLTVDistribution(List<Member> members) {
        int[] ranges = {0, 1000, 5000, 10000};
        String[] labels = {"0-1000元", "1000-5000元", "5000-10000元", "10000元以上"};
        long[] counts = new long[4];

        for (Member member : members) {
            BigDecimal spent = member.getTotalSpent() != null ? member.getTotalSpent() : BigDecimal.ZERO;
            if (spent.compareTo(BigDecimal.valueOf(1000)) < 0) {
                counts[0]++;
            } else if (spent.compareTo(BigDecimal.valueOf(5000)) < 0) {
                counts[1]++;
            } else if (spent.compareTo(BigDecimal.valueOf(10000)) < 0) {
                counts[2]++;
            } else {
                counts[3]++;
            }
        }

        List<Map<String, Object>> result = new ArrayList<>();
        long total = members.size();
        for (int i = 0; i < 4; i++) {
            Map<String, Object> item = new HashMap<>();
            item.put("range", labels[i]);
            item.put("count", counts[i]);
            item.put("percentage", total > 0
                    ? BigDecimal.valueOf(counts[i]).divide(BigDecimal.valueOf(total), 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100))
                    : BigDecimal.ZERO);
            result.add(item);
        }

        return result;
    }

    // ==================== 2. 等级分布分析 ====================

    public Map<String, Object> getLevelDistributionAnalysis() {
        Map<String, Object> result = new HashMap<>();
        result.put("levelDataComparison", getLevelDataComparison());
        result.put("levelFlowAnalysis", getLevelFlowAnalysis());
        result.put("levelHealthAnalysis", getLevelHealthAnalysis());
        return result;
    }

    public List<Map<String, Object>> getLevelDataComparison() {
        List<MemberLevel> levels = memberLevelMapper.selectListByQuery(
                QueryWrapper.create().from(MemberLevel.class)
                        .where(MEMBER_LEVEL.STATUS.eq(1))
                        .orderBy(MEMBER_LEVEL.SORT_ORDER.asc())
        );

        List<Map<String, Object>> result = new ArrayList<>();
        for (MemberLevel level : levels) {
            Map<String, Object> item = new HashMap<>();

            QueryWrapper memberQuery = QueryWrapper.create()
                    .from(Member.class)
                    .where(MEMBER.LEVEL_ID.eq(level.getId()))
                    .and(MEMBER.DELETED.eq(0))
                    .and(MEMBER.STATUS.eq(1));

            List<Member> levelMembers = memberMapper.selectListByQuery(memberQuery);
            int memberCount = levelMembers.size();

            BigDecimal totalSpent = levelMembers.stream()
                    .map(Member::getTotalSpent)
                    .filter(Objects::nonNull)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal avgSpent = memberCount > 0
                    ? totalSpent.divide(BigDecimal.valueOf(memberCount), 2, RoundingMode.HALF_UP)
                    : BigDecimal.ZERO;

            BigDecimal avgPoints = memberCount > 0
                    ? levelMembers.stream()
                    .map(Member::getCurrentPoints)
                    .filter(Objects::nonNull)
                    .reduce(BigDecimal.ZERO, BigDecimal::add)
                    .divide(BigDecimal.valueOf(memberCount), 2, RoundingMode.HALF_UP)
                    : BigDecimal.ZERO;

            double avgStays = memberCount > 0
                    ? levelMembers.stream().mapToInt(m -> m.getStayCount() != null ? m.getStayCount() : 0).average().orElse(0)
                    : 0;

            long repeatCount = levelMembers.stream().filter(m -> m.getStayCount() != null && m.getStayCount() >= 2).count();
            BigDecimal repeatRate = memberCount > 0
                    ? BigDecimal.valueOf(repeatCount).divide(BigDecimal.valueOf(memberCount), 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100))
                    : BigDecimal.ZERO;

            QueryWrapper checkInQuery = QueryWrapper.create()
                    .from(CheckIn.class)
                    .where(CHECK_IN.DELETED.eq(0))
                    .and(CHECK_IN.STATUS.eq(3))
                    .and(CHECK_IN.MEMBER_LEVEL.eq(level.getSortOrder()));

            BigDecimal revenueContribution = getTotalAmountByQuery(checkInQuery);

            item.put("levelId", level.getId());
            item.put("levelName", level.getLevelName());
            item.put("levelColor", level.getLevelColor());
            item.put("levelIcon", level.getLevelIcon());
            item.put("memberCount", memberCount);
            item.put("avgSpent", avgSpent);
            item.put("avgPoints", avgPoints);
            item.put("avgStays", BigDecimal.valueOf(avgStays).setScale(2, RoundingMode.HALF_UP));
            item.put("repeatRate", repeatRate);
            item.put("revenueContribution", revenueContribution);
            result.add(item);
        }

        return result;
    }

    public Map<String, Object> getLevelFlowAnalysis() {
        Map<String, Object> result = new HashMap<>();

        LocalDate now = LocalDate.now();
        LocalDate monthStart = now.withDayOfMonth(1);
        LocalDateTime monthStartDateTime = monthStart.atStartOfDay();

        long upgradeCount = memberLevelLogMapper.selectCountByQuery(
                QueryWrapper.create().from(MemberLevelLog.class)
                        .where(MEMBER_LEVEL_LOG.CHANGE_TYPE.eq(1))
                        .and(MEMBER_LEVEL_LOG.CREATE_TIME.ge(monthStartDateTime))
        );

        long downgradeCount = memberLevelLogMapper.selectCountByQuery(
                QueryWrapper.create().from(MemberLevelLog.class)
                        .where(MEMBER_LEVEL_LOG.CHANGE_TYPE.eq(2))
                        .and(MEMBER_LEVEL_LOG.CREATE_TIME.ge(monthStartDateTime))
        );

        long totalMembers = memberMapper.selectCountByQuery(
                QueryWrapper.create().from(Member.class)
                        .where(MEMBER.DELETED.eq(0))
                        .and(MEMBER.STATUS.eq(1))
        );

        BigDecimal upgradeRate = totalMembers > 0
                ? BigDecimal.valueOf(upgradeCount).divide(BigDecimal.valueOf(totalMembers), 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100))
                : BigDecimal.ZERO;

        BigDecimal downgradeRate = totalMembers > 0
                ? BigDecimal.valueOf(downgradeCount).divide(BigDecimal.valueOf(totalMembers), 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100))
                : BigDecimal.ZERO;

        long remainCount = totalMembers - upgradeCount - downgradeCount;
        BigDecimal remainRate = totalMembers > 0
                ? BigDecimal.valueOf(remainCount).divide(BigDecimal.valueOf(totalMembers), 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100))
                : BigDecimal.ZERO;

        List<Map<String, Object>> flowTrend = getLevelFlowTrend();

        result.put("monthUpgradeCount", upgradeCount);
        result.put("monthDowngradeCount", downgradeCount);
        result.put("monthRemainCount", remainCount);
        result.put("upgradeRate", upgradeRate);
        result.put("downgradeRate", downgradeRate);
        result.put("remainRate", remainRate);
        result.put("flowTrend", flowTrend);

        return result;
    }

    private List<Map<String, Object>> getLevelFlowTrend() {
        List<Map<String, Object>> result = new ArrayList<>();
        LocalDate now = LocalDate.now();

        for (int i = 11; i >= 0; i--) {
            LocalDate monthStart = now.minusMonths(i).withDayOfMonth(1);
            LocalDate monthEnd = monthStart.plusMonths(1).minusDays(1);
            LocalDateTime start = monthStart.atStartOfDay();
            LocalDateTime end = monthEnd.atTime(23, 59, 59);

            long upgradeCount = memberLevelLogMapper.selectCountByQuery(
                    QueryWrapper.create().from(MemberLevelLog.class)
                            .where(MEMBER_LEVEL_LOG.CHANGE_TYPE.eq(1))
                            .and(MEMBER_LEVEL_LOG.CREATE_TIME.ge(start))
                            .and(MEMBER_LEVEL_LOG.CREATE_TIME.le(end))
            );

            long downgradeCount = memberLevelLogMapper.selectCountByQuery(
                    QueryWrapper.create().from(MemberLevelLog.class)
                            .where(MEMBER_LEVEL_LOG.CHANGE_TYPE.eq(2))
                            .and(MEMBER_LEVEL_LOG.CREATE_TIME.ge(start))
                            .and(MEMBER_LEVEL_LOG.CREATE_TIME.le(end))
            );

            Map<String, Object> item = new HashMap<>();
            item.put("month", monthStart.getYear() + "-" + String.format("%02d", monthStart.getMonthValue()));
            item.put("upgradeCount", upgradeCount);
            item.put("downgradeCount", downgradeCount);
            result.add(item);
        }

        return result;
    }

    public List<Map<String, Object>> getLevelHealthAnalysis() {
        List<MemberLevel> levels = memberLevelMapper.selectListByQuery(
                QueryWrapper.create().from(MemberLevel.class)
                        .where(MEMBER_LEVEL.STATUS.eq(1))
                        .orderBy(MEMBER_LEVEL.SORT_ORDER.asc())
        );

        LocalDateTime threeMonthsAgo = LocalDateTime.now().minusMonths(3);
        LocalDateTime sixMonthsAgo = LocalDateTime.now().minusMonths(6);

        List<Map<String, Object>> result = new ArrayList<>();
        for (MemberLevel level : levels) {
            Map<String, Object> item = new HashMap<>();

            QueryWrapper memberQuery = QueryWrapper.create()
                    .from(Member.class)
                    .where(MEMBER.LEVEL_ID.eq(level.getId()))
                    .and(MEMBER.DELETED.eq(0))
                    .and(MEMBER.STATUS.eq(1));

            List<Member> levelMembers = memberMapper.selectListByQuery(memberQuery);
            int totalCount = levelMembers.size();

            long activeCount = levelMembers.stream()
                    .filter(m -> m.getLastStayTime() != null && m.getLastStayTime().isAfter(threeMonthsAgo))
                    .count();

            long dormantCount = levelMembers.stream()
                    .filter(m -> m.getLastStayTime() == null || m.getLastStayTime().isBefore(sixMonthsAgo))
                    .count();

            BigDecimal activeRate = totalCount > 0
                    ? BigDecimal.valueOf(activeCount).divide(BigDecimal.valueOf(totalCount), 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100))
                    : BigDecimal.ZERO;

            BigDecimal dormantRate = totalCount > 0
                    ? BigDecimal.valueOf(dormantCount).divide(BigDecimal.valueOf(totalCount), 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100))
                    : BigDecimal.ZERO;

            String healthLevel;
            if (activeRate.compareTo(BigDecimal.valueOf(60)) >= 0) {
                healthLevel = "健康";
            } else if (activeRate.compareTo(BigDecimal.valueOf(40)) >= 0) {
                healthLevel = "一般";
            } else {
                healthLevel = "需关注";
            }

            item.put("levelId", level.getId());
            item.put("levelName", level.getLevelName());
            item.put("levelColor", level.getLevelColor());
            item.put("levelIcon", level.getLevelIcon());
            item.put("totalCount", totalCount);
            item.put("activeCount", activeCount);
            item.put("activeRate", activeRate);
            item.put("dormantCount", dormantCount);
            item.put("dormantRate", dormantRate);
            item.put("healthLevel", healthLevel);
            result.add(item);
        }

        return result;
    }

    // ==================== 3. 行为特征分析 ====================

    public Map<String, Object> getMemberBehaviorAnalysis() {
        Map<String, Object> result = new HashMap<>();
        result.put("consumptionFrequency", getConsumptionFrequencyAnalysis());
        result.put("consumptionAmount", getConsumptionAmountAnalysis());
        result.put("timePreference", getTimePreferenceAnalysis());
        return result;
    }

    public Map<String, Object> getConsumptionFrequencyAnalysis() {
        Map<String, Object> result = new HashMap<>();

        QueryWrapper query = QueryWrapper.create()
                .from(Member.class)
                .where(MEMBER.DELETED.eq(0))
                .and(MEMBER.STATUS.eq(1));

        List<Member> members = memberMapper.selectListByQuery(query);

        String[] labels = {"尝鲜型(1次)", "偶尔型(2-5次)", "常客型(6-10次)", "忠诚型(10次以上)"};
        long[] counts = new long[4];
        BigDecimal[] totalSpent = new BigDecimal[4];
        for (int i = 0; i < 4; i++) {
            totalSpent[i] = BigDecimal.ZERO;
        }

        for (Member member : members) {
            int stays = member.getStayCount() != null ? member.getStayCount() : 0;
            BigDecimal spent = member.getTotalSpent() != null ? member.getTotalSpent() : BigDecimal.ZERO;
            int idx;
            if (stays <= 1) {
                idx = 0;
            } else if (stays <= 5) {
                idx = 1;
            } else if (stays <= 10) {
                idx = 2;
            } else {
                idx = 3;
            }
            counts[idx]++;
            totalSpent[idx] = totalSpent[idx].add(spent);
        }

        List<Map<String, Object>> distribution = new ArrayList<>();
        long total = members.size();
        for (int i = 0; i < 4; i++) {
            Map<String, Object> item = new HashMap<>();
            item.put("type", labels[i]);
            item.put("count", counts[i]);
            item.put("percentage", total > 0
                    ? BigDecimal.valueOf(counts[i]).divide(BigDecimal.valueOf(total), 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100))
                    : BigDecimal.ZERO);
            item.put("totalSpent", totalSpent[i]);
            item.put("avgSpent", counts[i] > 0
                    ? totalSpent[i].divide(BigDecimal.valueOf(counts[i]), 2, RoundingMode.HALF_UP)
                    : BigDecimal.ZERO);
            distribution.add(item);
        }

        result.put("distribution", distribution);
        result.put("totalMembers", total);
        result.put("repurchaseTrend", getRepurchaseTrend());

        return result;
    }

    private List<Map<String, Object>> getRepurchaseTrend() {
        List<Map<String, Object>> result = new ArrayList<>();
        LocalDate now = LocalDate.now();

        for (int i = 11; i >= 0; i--) {
            LocalDate monthStart = now.minusMonths(i).withDayOfMonth(1);
            LocalDate monthEnd = monthStart.plusMonths(1).minusDays(1);

            QueryWrapper newMemberQuery = QueryWrapper.create()
                    .from(Member.class)
                    .where(MEMBER.REGISTER_TIME.ge(monthStart.atStartOfDay()))
                    .and(MEMBER.REGISTER_TIME.le(monthEnd.atTime(23, 59, 59)))
                    .and(MEMBER.DELETED.eq(0));

            long newMemberCount = memberMapper.selectCountByQuery(newMemberQuery);

            long repeatCount = 0;
            List<Member> newMembers = memberMapper.selectListByQuery(newMemberQuery);
            for (Member member : newMembers) {
                if (member.getStayCount() != null && member.getStayCount() >= 2) {
                    repeatCount++;
                }
            }

            Map<String, Object> item = new HashMap<>();
            item.put("month", monthStart.getYear() + "-" + String.format("%02d", monthStart.getMonthValue()));
            item.put("newMembers", newMemberCount);
            item.put("repeatMembers", repeatCount);
            item.put("repurchaseRate", newMemberCount > 0
                    ? BigDecimal.valueOf(repeatCount).divide(BigDecimal.valueOf(newMemberCount), 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100))
                    : BigDecimal.ZERO);
            result.add(item);
        }

        return result;
    }

    public Map<String, Object> getConsumptionAmountAnalysis() {
        Map<String, Object> result = new HashMap<>();

        QueryWrapper query = QueryWrapper.create()
                .from(Member.class)
                .where(MEMBER.DELETED.eq(0))
                .and(MEMBER.STATUS.eq(1));

        List<Member> members = memberMapper.selectListByQuery(query);

        String[] labels = {"低消费(0-1000)", "中消费(1000-5000)", "高消费(5000-10000)", "超高消费(10000+)"};
        long[] counts = new long[4];

        for (Member member : members) {
            BigDecimal spent = member.getTotalSpent() != null ? member.getTotalSpent() : BigDecimal.ZERO;
            int idx;
            if (spent.compareTo(BigDecimal.valueOf(1000)) < 0) {
                idx = 0;
            } else if (spent.compareTo(BigDecimal.valueOf(5000)) < 0) {
                idx = 1;
            } else if (spent.compareTo(BigDecimal.valueOf(10000)) < 0) {
                idx = 2;
            } else {
                idx = 3;
            }
            counts[idx]++;
        }

        List<Map<String, Object>> distribution = new ArrayList<>();
        long total = members.size();
        String[] colors = {"#909399", "#67c23a", "#e6a23c", "#f56c6c"};
        for (int i = 0; i < 4; i++) {
            Map<String, Object> item = new HashMap<>();
            item.put("level", labels[i]);
            item.put("count", counts[i]);
            item.put("color", colors[i]);
            item.put("percentage", total > 0
                    ? BigDecimal.valueOf(counts[i]).divide(BigDecimal.valueOf(total), 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100))
                    : BigDecimal.ZERO);
            distribution.add(item);
        }

        List<Map<String, Object>> topValueMembers = getTopValueMembers(members);

        result.put("distribution", distribution);
        result.put("totalMembers", total);
        result.put("topValueMembers", topValueMembers);

        return result;
    }

    private List<Map<String, Object>> getTopValueMembers(List<Member> members) {
        int topCount = Math.max(1, (int) Math.ceil(members.size() * 0.1));

        return members.stream()
                .sorted((a, b) -> {
                    BigDecimal spentA = a.getTotalSpent() != null ? a.getTotalSpent() : BigDecimal.ZERO;
                    BigDecimal spentB = b.getTotalSpent() != null ? b.getTotalSpent() : BigDecimal.ZERO;
                    return spentB.compareTo(spentA);
                })
                .limit(topCount)
                .map(m -> {
                    Map<String, Object> item = new HashMap<>();
                    item.put("memberNo", m.getMemberNo());
                    item.put("customerName", m.getCustomerName());
                    item.put("levelName", m.getLevelName());
                    item.put("totalSpent", m.getTotalSpent());
                    item.put("stayCount", m.getStayCount());
                    item.put("currentPoints", m.getCurrentPoints());
                    return item;
                })
                .collect(Collectors.toList());
    }

    public Map<String, Object> getTimePreferenceAnalysis() {
        Map<String, Object> result = new HashMap<>();

        result.put("monthPreference", getMonthPreference());
        result.put("weekPreference", getWeekPreference());
        result.put("bookingAdvance", getBookingAdvanceAnalysis());

        return result;
    }

    private List<Map<String, Object>> getMonthPreference() {
        List<Map<String, Object>> result = new ArrayList<>();

        QueryWrapper query = QueryWrapper.create()
                .from(CheckIn.class)
                .where(CHECK_IN.MEMBER_ID.isNotNull())
                .and(CHECK_IN.DELETED.eq(0))
                .and(CHECK_IN.STATUS.eq(3));

        List<CheckIn> checkIns = checkInMapper.selectListByQuery(query);

        Map<Integer, Long> monthCounts = new HashMap<>();
        for (int i = 1; i <= 12; i++) {
            monthCounts.put(i, 0L);
        }

        for (CheckIn checkIn : checkIns) {
            if (checkIn.getCheckInDate() != null) {
                int month = checkIn.getCheckInDate().getMonthValue();
                monthCounts.put(month, monthCounts.get(month) + 1);
            }
        }

        String[] monthNames = {"1月", "2月", "3月", "4月", "5月", "6月", "7月", "8月", "9月", "10月", "11月", "12月"};
        for (int i = 1; i <= 12; i++) {
            Map<String, Object> item = new HashMap<>();
            item.put("month", monthNames[i - 1]);
            item.put("count", monthCounts.get(i));
            result.add(item);
        }

        return result;
    }

    private List<Map<String, Object>> getWeekPreference() {
        List<Map<String, Object>> result = new ArrayList<>();

        QueryWrapper query = QueryWrapper.create()
                .from(CheckIn.class)
                .where(CHECK_IN.MEMBER_ID.isNotNull())
                .and(CHECK_IN.DELETED.eq(0))
                .and(CHECK_IN.STATUS.eq(3));

        List<CheckIn> checkIns = checkInMapper.selectListByQuery(query);

        int[] weekCounts = new int[7];
        String[] weekNames = {"周一", "周二", "周三", "周四", "周五", "周六", "周日"};

        for (CheckIn checkIn : checkIns) {
            if (checkIn.getCheckInDate() != null) {
                int dayOfWeek = checkIn.getCheckInDate().getDayOfWeek().getValue() - 1;
                weekCounts[dayOfWeek]++;
            }
        }

        for (int i = 0; i < 7; i++) {
            Map<String, Object> item = new HashMap<>();
            item.put("day", weekNames[i]);
            item.put("count", weekCounts[i]);
            item.put("isWeekend", i >= 5);
            result.add(item);
        }

        return result;
    }

    private Map<String, Object> getBookingAdvanceAnalysis() {
        Map<String, Object> result = new HashMap<>();

        QueryWrapper bookingQuery = QueryWrapper.create()
                .from(Booking.class)
                .where(BOOKING.MEMBER_ID.isNotNull())
                .and(BOOKING.DELETED.eq(0))
                .and(BOOKING.STATUS.in(2, 3, 4));

        List<Booking> bookings = bookingMapper.selectListByQuery(bookingQuery);

        int sameDay = 0;
        int oneToThree = 0;
        int fourToSeven = 0;
        int moreThanSeven = 0;
        long totalDays = 0;
        int validCount = 0;

        for (Booking booking : bookings) {
            if (booking.getCreateTime() != null && booking.getCheckInDate() != null) {
                long days = ChronoUnit.DAYS.between(
                        booking.getCreateTime().toLocalDate(),
                        booking.getCheckInDate()
                );
                if (days >= 0) {
                    validCount++;
                    totalDays += days;
                    if (days == 0) {
                        sameDay++;
                    } else if (days <= 3) {
                        oneToThree++;
                    } else if (days <= 7) {
                        fourToSeven++;
                    } else {
                        moreThanSeven++;
                    }
                }
            }
        }

        BigDecimal avgAdvanceDays = validCount > 0
                ? BigDecimal.valueOf(totalDays).divide(BigDecimal.valueOf(validCount), 1, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        result.put("avgAdvanceDays", avgAdvanceDays);
        result.put("sameDayCount", sameDay);
        result.put("oneToThreeCount", oneToThree);
        result.put("fourToSevenCount", fourToSeven);
        result.put("moreThanSevenCount", moreThanSeven);
        result.put("totalBookings", validCount);

        return result;
    }

    // ==================== 4. 流失预警 ====================

    public Map<String, Object> getChurnWarningAnalysis() {
        Map<String, Object> result = new HashMap<>();
        result.put("riskSummary", getChurnRiskSummary());
        result.put("churnTrend", getChurnTrend());
        result.put("churnByLevel", getChurnByLevel());
        return result;
    }

    public Map<String, Object> getChurnRiskSummary() {
        Map<String, Object> result = new HashMap<>();

        QueryWrapper query = QueryWrapper.create()
                .from(Member.class)
                .where(MEMBER.DELETED.eq(0))
                .and(MEMBER.STATUS.eq(1));

        List<Member> members = memberMapper.selectListByQuery(query);

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime threeMonthsAgo = now.minusMonths(3);
        LocalDateTime sixMonthsAgo = now.minusMonths(6);
        LocalDateTime twelveMonthsAgo = now.minusMonths(12);

        long lowRisk = 0;
        long mediumRisk = 0;
        long highRisk = 0;
        long active = 0;

        for (Member member : members) {
            LocalDateTime lastStay = member.getLastStayTime();
            if (lastStay == null) {
                lowRisk++;
            } else if (lastStay.isAfter(threeMonthsAgo)) {
                active++;
            } else if (lastStay.isAfter(sixMonthsAgo)) {
                lowRisk++;
            } else if (lastStay.isAfter(twelveMonthsAgo)) {
                mediumRisk++;
            } else {
                highRisk++;
            }
        }

        long total = members.size();

        result.put("totalMembers", total);
        result.put("activeCount", active);
        result.put("lowRiskCount", lowRisk);
        result.put("mediumRiskCount", mediumRisk);
        result.put("highRiskCount", highRisk);
        result.put("lowRiskRate", total > 0
                ? BigDecimal.valueOf(lowRisk).divide(BigDecimal.valueOf(total), 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100))
                : BigDecimal.ZERO);
        result.put("mediumRiskRate", total > 0
                ? BigDecimal.valueOf(mediumRisk).divide(BigDecimal.valueOf(total), 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100))
                : BigDecimal.ZERO);
        result.put("highRiskRate", total > 0
                ? BigDecimal.valueOf(highRisk).divide(BigDecimal.valueOf(total), 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100))
                : BigDecimal.ZERO);

        return result;
    }

    public PageResult<Map<String, Object>> getChurnWarningList(int pageNum, int pageSize,
                                                                String riskLevel, List<Long> levelIds,
                                                                String lastStayStart, String lastStayEnd) {
        QueryWrapper query = QueryWrapper.create()
                .from(Member.class)
                .where(MEMBER.DELETED.eq(0))
                .and(MEMBER.STATUS.eq(1));

        if (levelIds != null && !levelIds.isEmpty()) {
            query.and(MEMBER.LEVEL_ID.in(levelIds));
        }

        LocalDateTime threeMonthsAgo = LocalDateTime.now().minusMonths(3);
        LocalDateTime sixMonthsAgo = LocalDateTime.now().minusMonths(6);
        LocalDateTime twelveMonthsAgo = LocalDateTime.now().minusMonths(12);

        if (riskLevel != null && !riskLevel.isEmpty()) {
            switch (riskLevel) {
                case "low":
                    query.and(MEMBER.LAST_STAY_TIME.le(threeMonthsAgo))
                            .and(MEMBER.LAST_STAY_TIME.gt(sixMonthsAgo));
                    break;
                case "medium":
                    query.and(MEMBER.LAST_STAY_TIME.le(sixMonthsAgo))
                            .and(MEMBER.LAST_STAY_TIME.gt(twelveMonthsAgo));
                    break;
                case "high":
                    query.and(MEMBER.LAST_STAY_TIME.le(twelveMonthsAgo));
                    break;
                case "active":
                    query.and(MEMBER.LAST_STAY_TIME.gt(threeMonthsAgo));
                    break;
            }
        }

        if (lastStayStart != null && !lastStayStart.isEmpty()) {
            query.and(MEMBER.LAST_STAY_TIME.ge(LocalDateTime.parse(lastStayStart + " 00:00:00",
                    java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))));
        }
        if (lastStayEnd != null && !lastStayEnd.isEmpty()) {
            query.and(MEMBER.LAST_STAY_TIME.le(LocalDateTime.parse(lastStayEnd + " 23:59:59",
                    java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))));
        }

        query.orderBy(MEMBER.LAST_STAY_TIME.asc().nullsLast());

        Page<Member> page = memberMapper.paginate(pageNum, pageSize, query);
        List<Member> records = page.getRecords();

        List<Map<String, Object>> resultList = new ArrayList<>();
        for (Member member : records) {
            Map<String, Object> item = new HashMap<>();
            item.put("id", member.getId());
            item.put("memberNo", member.getMemberNo());
            item.put("customerName", member.getCustomerName());
            item.put("phone", member.getPhone());
            item.put("levelId", member.getLevelId());
            item.put("levelName", member.getLevelName());
            item.put("lastStayTime", member.getLastStayTime());
            item.put("totalSpent", member.getTotalSpent());
            item.put("stayCount", member.getStayCount());
            item.put("currentPoints", member.getCurrentPoints());

            String risk = calcRiskLevel(member.getLastStayTime());
            item.put("riskLevel", risk);

            resultList.add(item);
        }

        return new PageResult<>(page.getTotalRow(), resultList, (long) pageNum, (long) pageSize);
    }

    private String calcRiskLevel(LocalDateTime lastStayTime) {
        if (lastStayTime == null) {
            return "low";
        }
        LocalDateTime now = LocalDateTime.now();
        if (lastStayTime.isAfter(now.minusMonths(3))) {
            return "active";
        } else if (lastStayTime.isAfter(now.minusMonths(6))) {
            return "low";
        } else if (lastStayTime.isAfter(now.minusMonths(12))) {
            return "medium";
        } else {
            return "high";
        }
    }

    public List<Map<String, Object>> getChurnTrend() {
        List<Map<String, Object>> result = new ArrayList<>();
        LocalDate now = LocalDate.now();

        for (int i = 11; i >= 0; i--) {
            LocalDate monthEnd = now.minusMonths(i).withDayOfMonth(1).plusMonths(1).minusDays(1);
            LocalDateTime monthEndTime = monthEnd.atTime(23, 59, 59);

            LocalDateTime sixMonthsBefore = monthEndTime.minusMonths(6);

            QueryWrapper query = QueryWrapper.create()
                    .from(Member.class)
                    .where(MEMBER.DELETED.eq(0))
                    .and(MEMBER.STATUS.eq(1))
                    .and(MEMBER.LAST_STAY_TIME.le(sixMonthsBefore));

            long churnCount = memberMapper.selectCountByQuery(query);

            long totalCount = memberMapper.selectCountByQuery(
                    QueryWrapper.create().from(Member.class)
                            .where(MEMBER.DELETED.eq(0))
                            .and(MEMBER.STATUS.eq(1))
                            .and(MEMBER.REGISTER_TIME.le(monthEndTime))
            );

            Map<String, Object> item = new HashMap<>();
            item.put("month", monthEnd.getYear() + "-" + String.format("%02d", monthEnd.getMonthValue()));
            item.put("churnCount", churnCount);
            item.put("churnRate", totalCount > 0
                    ? BigDecimal.valueOf(churnCount).divide(BigDecimal.valueOf(totalCount), 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100))
                    : BigDecimal.ZERO);
            result.add(item);
        }

        return result;
    }

    public List<Map<String, Object>> getChurnByLevel() {
        List<MemberLevel> levels = memberLevelMapper.selectListByQuery(
                QueryWrapper.create().from(MemberLevel.class)
                        .where(MEMBER_LEVEL.STATUS.eq(1))
                        .orderBy(MEMBER_LEVEL.SORT_ORDER.asc())
        );

        LocalDateTime sixMonthsAgo = LocalDateTime.now().minusMonths(6);

        List<Map<String, Object>> result = new ArrayList<>();
        for (MemberLevel level : levels) {
            Map<String, Object> item = new HashMap<>();

            QueryWrapper memberQuery = QueryWrapper.create()
                    .from(Member.class)
                    .where(MEMBER.LEVEL_ID.eq(level.getId()))
                    .and(MEMBER.DELETED.eq(0))
                    .and(MEMBER.STATUS.eq(1));

            long totalCount = memberMapper.selectCountByQuery(memberQuery);

            long churnCount = memberMapper.selectCountByQuery(
                    QueryWrapper.create().from(Member.class)
                            .where(MEMBER.LEVEL_ID.eq(level.getId()))
                            .and(MEMBER.DELETED.eq(0))
                            .and(MEMBER.STATUS.eq(1))
                            .and(MEMBER.LAST_STAY_TIME.le(sixMonthsAgo))
            );

            item.put("levelId", level.getId());
            item.put("levelName", level.getLevelName());
            item.put("levelColor", level.getLevelColor());
            item.put("levelIcon", level.getLevelIcon());
            item.put("totalMembers", totalCount);
            item.put("churnCount", churnCount);
            item.put("churnRate", totalCount > 0
                    ? BigDecimal.valueOf(churnCount).divide(BigDecimal.valueOf(totalCount), 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100))
                    : BigDecimal.ZERO);
            result.add(item);
        }

        return result;
    }

    // ==================== 5. 权益使用与成本统计 ====================

    public Map<String, Object> getBenefitUsageAnalysis() {
        Map<String, Object> result = new HashMap<>();
        result.put("usageRates", getBenefitUsageRates());
        result.put("costAnalysis", getBenefitCostAnalysis());
        return result;
    }

    public Map<String, Object> getBenefitUsageRates() {
        Map<String, Object> result = new HashMap<>();

        QueryWrapper memberOrderQuery = QueryWrapper.create()
                .from(CheckIn.class)
                .where(CHECK_IN.MEMBER_ID.isNotNull())
                .and(CHECK_IN.DELETED.eq(0))
                .and(CHECK_IN.STATUS.eq(3));

        long memberOrderCount = checkInMapper.selectCountByQuery(memberOrderQuery);

        QueryWrapper discountQuery = QueryWrapper.create()
                .from(MemberBenefitLog.class)
                .where(MEMBER_BENEFIT_LOG.BENEFIT_TYPE.eq(1));

        long discountUseCount = memberBenefitLogMapper.selectCountByQuery(discountQuery);
        BigDecimal discountTotalAmount = getBenefitTotalAmount(discountQuery);

        QueryWrapper upgradeQuery = QueryWrapper.create()
                .from(MemberBenefitLog.class)
                .where(MEMBER_BENEFIT_LOG.BENEFIT_TYPE.eq(2));

        long upgradeUseCount = memberBenefitLogMapper.selectCountByQuery(upgradeQuery);
        long upgradeMemberCount = getBenefitMemberCount(upgradeQuery);

        QueryWrapper lateCheckoutQuery = QueryWrapper.create()
                .from(MemberBenefitLog.class)
                .where(MEMBER_BENEFIT_LOG.BENEFIT_TYPE.eq(3));

        long lateCheckoutUseCount = memberBenefitLogMapper.selectCountByQuery(lateCheckoutQuery);
        long lateCheckoutMemberCount = getBenefitMemberCount(lateCheckoutQuery);

        QueryWrapper depositQuery = QueryWrapper.create()
                .from(MemberBenefitLog.class)
                .where(MEMBER_BENEFIT_LOG.BENEFIT_TYPE.eq(4));

        long depositUseCount = memberBenefitLogMapper.selectCountByQuery(depositQuery);
        BigDecimal depositTotalAmount = getBenefitTotalAmount(depositQuery);
        BigDecimal avgDepositReduction = depositUseCount > 0
                ? depositTotalAmount.divide(BigDecimal.valueOf(depositUseCount), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        BigDecimal discountUsageRate = memberOrderCount > 0
                ? BigDecimal.valueOf(discountUseCount).divide(BigDecimal.valueOf(memberOrderCount), 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100))
                : BigDecimal.ZERO;

        long totalMembers = memberMapper.selectCountByQuery(
                QueryWrapper.create().from(Member.class)
                        .where(MEMBER.DELETED.eq(0))
                        .and(MEMBER.STATUS.eq(1))
        );

        BigDecimal upgradeUsageRate = totalMembers > 0
                ? BigDecimal.valueOf(upgradeMemberCount).divide(BigDecimal.valueOf(totalMembers), 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100))
                : BigDecimal.ZERO;

        BigDecimal lateCheckoutUsageRate = totalMembers > 0
                ? BigDecimal.valueOf(lateCheckoutMemberCount).divide(BigDecimal.valueOf(totalMembers), 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100))
                : BigDecimal.ZERO;

        BigDecimal depositUsageRate = memberOrderCount > 0
                ? BigDecimal.valueOf(depositUseCount).divide(BigDecimal.valueOf(memberOrderCount), 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100))
                : BigDecimal.ZERO;

        Map<String, Object> discount = new HashMap<>();
        discount.put("useCount", discountUseCount);
        discount.put("totalAmount", discountTotalAmount);
        discount.put("usageRate", discountUsageRate);
        result.put("discount", discount);

        Map<String, Object> upgrade = new HashMap<>();
        upgrade.put("useCount", upgradeUseCount);
        upgrade.put("memberCount", upgradeMemberCount);
        upgrade.put("usageRate", upgradeUsageRate);
        upgrade.put("upgradeCost", null);
        result.put("upgrade", upgrade);

        Map<String, Object> lateCheckout = new HashMap<>();
        lateCheckout.put("useCount", lateCheckoutUseCount);
        lateCheckout.put("memberCount", lateCheckoutMemberCount);
        lateCheckout.put("usageRate", lateCheckoutUsageRate);
        result.put("lateCheckout", lateCheckout);

        Map<String, Object> deposit = new HashMap<>();
        deposit.put("useCount", depositUseCount);
        deposit.put("totalReductionAmount", depositTotalAmount);
        deposit.put("avgReductionAmount", avgDepositReduction);
        deposit.put("usageRate", depositUsageRate);
        deposit.put("riskAmount", depositTotalAmount);
        deposit.put("actualLoss", BigDecimal.ZERO);
        result.put("deposit", deposit);

        result.put("memberOrderCount", memberOrderCount);
        result.put("totalMembers", totalMembers);

        return result;
    }

    private BigDecimal getBenefitTotalAmount(QueryWrapper query) {
        List<MemberBenefitLog> logs = memberBenefitLogMapper.selectListByQuery(query);
        return logs.stream()
                .map(MemberBenefitLog::getBenefitAmount)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private long getBenefitMemberCount(QueryWrapper query) {
        List<MemberBenefitLog> logs = memberBenefitLogMapper.selectListByQuery(query);
        return logs.stream().map(MemberBenefitLog::getMemberId).distinct().count();
    }

    public Map<String, Object> getBenefitCostAnalysis() {
        Map<String, Object> result = new HashMap<>();

        Map<String, Object> usageRates = getBenefitUsageRates();

        BigDecimal discountCost = (BigDecimal) ((Map) usageRates.get("discount")).get("totalAmount");

        QueryWrapper memberOrderQuery = QueryWrapper.create()
                .from(CheckIn.class)
                .where(CHECK_IN.MEMBER_ID.isNotNull())
                .and(CHECK_IN.DELETED.eq(0))
                .and(CHECK_IN.STATUS.eq(3));

        BigDecimal memberTotalRevenue = getTotalAmountByQuery(memberOrderQuery);

        BigDecimal discountCostRatio = memberTotalRevenue.compareTo(BigDecimal.ZERO) > 0
                ? discountCost.divide(memberTotalRevenue, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100))
                : BigDecimal.ZERO;

        BigDecimal upgradeCost = BigDecimal.ZERO;
        BigDecimal totalBenefitCost = discountCost.add(upgradeCost);

        BigDecimal benefitCostRate = memberTotalRevenue.compareTo(BigDecimal.ZERO) > 0
                ? totalBenefitCost.divide(memberTotalRevenue, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100))
                : BigDecimal.ZERO;

        QueryWrapper nonMemberOrderQuery = QueryWrapper.create()
                .from(CheckIn.class)
                .where(CHECK_IN.MEMBER_ID.isNull())
                .and(CHECK_IN.DELETED.eq(0))
                .and(CHECK_IN.STATUS.eq(3));

        long nonMemberOrderCount = checkInMapper.selectCountByQuery(nonMemberOrderQuery);
        BigDecimal nonMemberTotalRevenue = getTotalAmountByQuery(nonMemberOrderQuery);
        BigDecimal nonMemberAvgOrder = nonMemberOrderCount > 0
                ? nonMemberTotalRevenue.divide(BigDecimal.valueOf(nonMemberOrderCount), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        long memberOrderCount = checkInMapper.selectCountByQuery(memberOrderQuery);
        BigDecimal memberAvgOrder = memberOrderCount > 0
                ? memberTotalRevenue.divide(BigDecimal.valueOf(memberOrderCount), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        BigDecimal extraRevenuePerOrder = memberAvgOrder.subtract(nonMemberAvgOrder);
        BigDecimal extraRevenueTotal = extraRevenuePerOrder.multiply(BigDecimal.valueOf(memberOrderCount));
        BigDecimal roi = totalBenefitCost.compareTo(BigDecimal.ZERO) > 0
                ? extraRevenueTotal.divide(totalBenefitCost, 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        result.put("discountCost", discountCost);
        result.put("discountCostRatio", discountCostRatio);
        result.put("upgradeCost", upgradeCost);
        result.put("upgradeCostReserved", true);
        result.put("totalBenefitCost", totalBenefitCost);
        result.put("benefitCostRate", benefitCostRate);
        result.put("memberTotalRevenue", memberTotalRevenue);
        result.put("extraRevenueTotal", extraRevenueTotal);
        result.put("roi", roi);

        return result;
    }
}
