package com.example.permission.service;

import com.example.permission.entity.Member;
import com.example.permission.entity.MemberPointLog;
import com.example.permission.mapper.MemberMapper;
import com.example.permission.mapper.MemberPointLogMapper;
import com.mybatisflex.core.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.example.permission.entity.table.MemberPointLogTableDef.MEMBER_POINT_LOG;
import static com.example.permission.entity.table.MemberTableDef.MEMBER;

@Service
public class PointStatisticsService {

    @Autowired
    private MemberPointLogMapper memberPointLogMapper;

    @Autowired
    private MemberMapper memberMapper;

    public Map<String, Object> getOverview() {
        Map<String, Object> result = new HashMap<>();

        BigDecimal totalCurrentPoints = BigDecimal.ZERO;
        QueryWrapper allMemberQuery = QueryWrapper.create()
                .from(Member.class)
                .where(MEMBER.DELETED.eq(0))
                .and(MEMBER.STATUS.eq(1));
        List<Member> allMembers = memberMapper.selectListByQuery(allMemberQuery);
        for (Member m : allMembers) {
            if (m.getCurrentPoints() != null) {
                totalCurrentPoints = totalCurrentPoints.add(m.getCurrentPoints());
            }
        }
        result.put("totalCurrentPoints", totalCurrentPoints);

        LocalDateTime monthStart = LocalDate.now().withDayOfMonth(1).atStartOfDay();
        LocalDateTime monthEnd = LocalDate.now().plusMonths(1).withDayOfMonth(1).atStartOfDay().minusNanos(1);

        BigDecimal monthEarned = BigDecimal.ZERO;
        QueryWrapper earnQuery = QueryWrapper.create()
                .from(MemberPointLog.class)
                .where(MEMBER_POINT_LOG.POINT_TYPE.eq(1))
                .and(MEMBER_POINT_LOG.CREATE_TIME.between(monthStart, monthEnd));
        List<MemberPointLog> earnLogs = memberPointLogMapper.selectListByQuery(earnQuery);
        for (MemberPointLog log : earnLogs) {
            if (log.getPoints() != null) {
                monthEarned = monthEarned.add(log.getPoints());
            }
        }
        result.put("monthEarned", monthEarned);

        BigDecimal monthUsed = BigDecimal.ZERO;
        QueryWrapper useQuery = QueryWrapper.create()
                .from(MemberPointLog.class)
                .where(MEMBER_POINT_LOG.POINT_TYPE.eq(2))
                .and(MEMBER_POINT_LOG.CREATE_TIME.between(monthStart, monthEnd));
        List<MemberPointLog> useLogs = memberPointLogMapper.selectListByQuery(useQuery);
        for (MemberPointLog log : useLogs) {
            if (log.getPoints() != null) {
                monthUsed = monthUsed.add(log.getPoints());
            }
        }
        result.put("monthUsed", monthUsed);

        BigDecimal usageRate = BigDecimal.ZERO;
        if (monthEarned.compareTo(BigDecimal.ZERO) > 0) {
            usageRate = monthUsed.divide(monthEarned, 4, RoundingMode.HALF_UP).multiply(new BigDecimal("100"));
        }
        result.put("usageRate", usageRate);

        result.put("pointLiability", totalCurrentPoints);

        return result;
    }

    public List<Map<String, Object>> getSourceAnalysis() {
        List<Map<String, Object>> result = new ArrayList<>();
        int[] reasonTypes = {1, 2, 4, 5, 7};
        String[] reasonNames = {"消费积分", "活动积分", "推荐积分", "生日积分", "其他"};

        BigDecimal totalEarned = BigDecimal.ZERO;
        List<BigDecimal> amounts = new ArrayList<>();
        for (int i = 0; i < reasonTypes.length; i++) {
            QueryWrapper query = QueryWrapper.create()
                    .from(MemberPointLog.class)
                    .where(MEMBER_POINT_LOG.POINT_TYPE.eq(1))
                    .and(MEMBER_POINT_LOG.REASON_TYPE.eq(reasonTypes[i]));
            List<MemberPointLog> logs = memberPointLogMapper.selectListByQuery(query);
            BigDecimal amount = BigDecimal.ZERO;
            for (MemberPointLog log : logs) {
                if (log.getPoints() != null) {
                    amount = amount.add(log.getPoints());
                }
            }
            amounts.add(amount);
            totalEarned = totalEarned.add(amount);
        }

        for (int i = 0; i < reasonTypes.length; i++) {
            Map<String, Object> item = new HashMap<>();
            item.put("name", reasonNames[i]);
            item.put("value", amounts.get(i));
            item.put("percentage", totalEarned.compareTo(BigDecimal.ZERO) > 0
                    ? amounts.get(i).divide(totalEarned, 4, RoundingMode.HALF_UP).multiply(new BigDecimal("100"))
                    : BigDecimal.ZERO);
            result.add(item);
        }

        return result;
    }

    public List<Map<String, Object>> getUsageAnalysis() {
        List<Map<String, Object>> result = new ArrayList<>();
        int[] reasonTypes = {6, 7};
        String[] reasonNames = {"积分抵现", "积分兑换"};

        BigDecimal totalUsed = BigDecimal.ZERO;
        List<BigDecimal> amounts = new ArrayList<>();
        for (int i = 0; i < reasonTypes.length; i++) {
            QueryWrapper query = QueryWrapper.create()
                    .from(MemberPointLog.class)
                    .where(MEMBER_POINT_LOG.POINT_TYPE.eq(2))
                    .and(MEMBER_POINT_LOG.REASON_TYPE.eq(reasonTypes[i]));
            List<MemberPointLog> logs = memberPointLogMapper.selectListByQuery(query);
            BigDecimal amount = BigDecimal.ZERO;
            for (MemberPointLog log : logs) {
                if (log.getPoints() != null) {
                    amount = amount.add(log.getPoints());
                }
            }
            amounts.add(amount);
            totalUsed = totalUsed.add(amount);
        }

        for (int i = 0; i < reasonTypes.length; i++) {
            Map<String, Object> item = new HashMap<>();
            item.put("name", reasonNames[i]);
            item.put("value", amounts.get(i));
            item.put("percentage", totalUsed.compareTo(BigDecimal.ZERO) > 0
                    ? amounts.get(i).divide(totalUsed, 4, RoundingMode.HALF_UP).multiply(new BigDecimal("100"))
                    : BigDecimal.ZERO);
            result.add(item);
        }

        return result;
    }

    public List<Map<String, Object>> getTrend(Integer months) {
        if (months == null || months <= 0) {
            months = 12;
        }
        List<Map<String, Object>> trend = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM");

        for (int i = months - 1; i >= 0; i--) {
            LocalDate monthDate = LocalDate.now().minusMonths(i).withDayOfMonth(1);
            String monthStr = monthDate.format(formatter);
            LocalDateTime monthStart = monthDate.atStartOfDay();
            LocalDateTime monthEnd = monthDate.plusMonths(1).atStartOfDay().minusNanos(1);

            BigDecimal earned = BigDecimal.ZERO;
            QueryWrapper earnQuery = QueryWrapper.create()
                    .from(MemberPointLog.class)
                    .where(MEMBER_POINT_LOG.POINT_TYPE.eq(1))
                    .and(MEMBER_POINT_LOG.CREATE_TIME.between(monthStart, monthEnd));
            List<MemberPointLog> earnLogs = memberPointLogMapper.selectListByQuery(earnQuery);
            for (MemberPointLog log : earnLogs) {
                if (log.getPoints() != null) {
                    earned = earned.add(log.getPoints());
                }
            }

            BigDecimal used = BigDecimal.ZERO;
            QueryWrapper useQuery = QueryWrapper.create()
                    .from(MemberPointLog.class)
                    .where(MEMBER_POINT_LOG.POINT_TYPE.eq(2))
                    .and(MEMBER_POINT_LOG.CREATE_TIME.between(monthStart, monthEnd));
            List<MemberPointLog> useLogs = memberPointLogMapper.selectListByQuery(useQuery);
            for (MemberPointLog log : useLogs) {
                if (log.getPoints() != null) {
                    used = used.add(log.getPoints());
                }
            }

            Map<String, Object> item = new HashMap<>();
            item.put("month", monthStr);
            item.put("earned", earned);
            item.put("used", used);
            trend.add(item);
        }

        return trend;
    }

    public Map<String, Object> getConversionAnalysis() {
        Map<String, Object> result = new HashMap<>();

        long totalMembers = memberMapper.selectCountByQuery(
                QueryWrapper.create().from(Member.class).where(MEMBER.DELETED.eq(0))
        );

        QueryWrapper activeQuery = QueryWrapper.create()
                .from(MemberPointLog.class)
                .where(MEMBER_POINT_LOG.POINT_TYPE.eq(2));
        List<MemberPointLog> useLogs = memberPointLogMapper.selectListByQuery(activeQuery);

        java.util.Set<Long> activeMemberIds = new java.util.HashSet<>();
        long totalUseCount = 0;
        BigDecimal totalUsePoints = BigDecimal.ZERO;

        for (MemberPointLog log : useLogs) {
            activeMemberIds.add(log.getMemberId());
            totalUseCount++;
            if (log.getPoints() != null) {
                totalUsePoints = totalUsePoints.add(log.getPoints());
            }
        }

        long activeMemberCount = activeMemberIds.size();
        BigDecimal activeRate = totalMembers > 0
                ? new BigDecimal(activeMemberCount).divide(new BigDecimal(totalMembers), 4, RoundingMode.HALF_UP).multiply(new BigDecimal("100"))
                : BigDecimal.ZERO;

        BigDecimal avgUseFrequency = activeMemberCount > 0
                ? new BigDecimal(totalUseCount).divide(new BigDecimal(activeMemberCount), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        BigDecimal avgPointsPerUse = totalUseCount > 0
                ? totalUsePoints.divide(new BigDecimal(totalUseCount), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        result.put("activeMemberCount", activeMemberCount);
        result.put("activeRate", activeRate);
        result.put("avgUseFrequency", avgUseFrequency);
        result.put("avgPointsPerUse", avgPointsPerUse);

        return result;
    }
}
