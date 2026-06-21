package com.example.permission.service;

import com.example.permission.common.PageResult;
import com.example.permission.entity.Member;
import com.example.permission.entity.MemberLevel;
import com.example.permission.entity.MemberLevelLog;
import com.example.permission.entity.MemberLevelTaskLog;
import com.example.permission.mapper.MemberLevelLogMapper;
import com.example.permission.mapper.MemberLevelMapper;
import com.example.permission.mapper.MemberLevelTaskLogMapper;
import com.example.permission.mapper.MemberMapper;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.update.UpdateWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static com.example.permission.entity.table.MemberLevelLogTableDef.MEMBER_LEVEL_LOG;
import static com.example.permission.entity.table.MemberLevelTableDef.MEMBER_LEVEL;
import static com.example.permission.entity.table.MemberLevelTaskLogTableDef.MEMBER_LEVEL_TASK_LOG;
import static com.example.permission.entity.table.MemberTableDef.MEMBER;

@Service
public class MemberLevelUpgradeService {

    @Autowired
    private MemberMapper memberMapper;

    @Autowired
    private MemberLevelMapper memberLevelMapper;

    @Autowired
    private MemberLevelLogMapper memberLevelLogMapper;

    @Autowired
    private MemberLevelTaskLogMapper memberLevelTaskLogMapper;

    @Transactional
    public Map<String, Object> executeUpgradeCheck(Long operatorId, String operatorName, boolean isManual) {
        int processCount = 0;
        int upgradeCount = 0;
        List<MemberLevel> allLevels = getAllLevelsSorted();
        if (allLevels.isEmpty()) {
            return buildResult(0, 0, 0, 0);
        }

        QueryWrapper memberQuery = QueryWrapper.create()
                .from(Member.class)
                .where(MEMBER.DELETED.eq(0))
                .and(MEMBER.STATUS.eq(1));
        List<Member> members = memberMapper.selectListByQuery(memberQuery);

        for (Member member : members) {
            processCount++;
            try {
                MemberLevel targetLevel = findHighestUpgradeLevel(member, allLevels);
                if (targetLevel != null && !targetLevel.getId().equals(member.getLevelId())) {
                    doUpgrade(member, targetLevel);
                    upgradeCount++;
                }
            } catch (Exception e) {
                // 单个会员失败不影响整体
            }
        }

        saveTaskLog(1, isManual ? "手动触发-会员升级检查" : "定时任务-会员升级检查",
                processCount, upgradeCount, 0, 0, null, operatorId, operatorName, isManual ? 2 : 1);

        return buildResult(processCount, upgradeCount, 0, 0);
    }

    @Transactional
    public Map<String, Object> executeDowngradeCheck(Long operatorId, String operatorName, boolean isManual) {
        int processCount = 0;
        int downgradeCount = 0;
        List<MemberLevel> allLevels = getAllLevelsSorted();
        if (allLevels.size() < 2) {
            return buildResult(0, 0, 0, 0);
        }
        MemberLevel lowestLevel = allLevels.get(0);

        QueryWrapper memberQuery = QueryWrapper.create()
                .from(Member.class)
                .where(MEMBER.DELETED.eq(0))
                .and(MEMBER.STATUS.eq(1))
                .and(MEMBER.LEVEL_ID.ne(lowestLevel.getId()));
        List<Member> members = memberMapper.selectListByQuery(memberQuery);

        Map<Long, MemberLevel> levelMap = new HashMap<>();
        for (MemberLevel level : allLevels) {
            levelMap.put(level.getId(), level);
        }

        for (Member member : members) {
            processCount++;
            try {
                MemberLevel currentLevel = levelMap.get(member.getLevelId());
                if (currentLevel == null) continue;

                BigDecimal yearlySpent = member.getYearlySpent() != null ? member.getYearlySpent() : BigDecimal.ZERO;
                BigDecimal keepCondition = currentLevel.getKeepCondition() != null ? currentLevel.getKeepCondition() : BigDecimal.ZERO;

                if (yearlySpent.compareTo(keepCondition) < 0) {
                    MemberLevel nextLowerLevel = findNextLowerLevel(currentLevel, allLevels);
                    if (nextLowerLevel != null) {
                        doDowngrade(member, currentLevel, nextLowerLevel, yearlySpent, keepCondition);
                        downgradeCount++;
                    }
                }
            } catch (Exception e) {
                // 单个会员失败不影响整体
            }
        }

        saveTaskLog(2, isManual ? "手动触发-保级降级检查" : "定时任务-保级降级检查",
                processCount, 0, downgradeCount, 0, null, operatorId, operatorName, isManual ? 2 : 1);

        return buildResult(processCount, 0, downgradeCount, 0);
    }

    @Transactional
    public Map<String, Object> executeYearlyReset(Long operatorId, String operatorName, boolean isManual) {
        int resetCount = 0;

        QueryWrapper query = QueryWrapper.create()
                .from(Member.class)
                .where(MEMBER.DELETED.eq(0));
        List<Member> members = memberMapper.selectListByQuery(query);
        for (Member member : members) {
            member.setYearlySpent(BigDecimal.ZERO);
            memberMapper.update(member);
            resetCount++;
        }

        saveTaskLog(3, isManual ? "手动触发-年度消费金额重置" : "定时任务-年度消费金额重置",
                resetCount, 0, 0, resetCount, null, operatorId, operatorName, isManual ? 2 : 1);

        return buildResult(0, 0, 0, resetCount);
    }

    private void doUpgrade(Member member, MemberLevel newLevel) {
        Long oldLevelId = member.getLevelId();
        String oldLevelName = member.getLevelName();

        member.setLevelId(newLevel.getId());
        member.setLevelName(newLevel.getLevelName());
        member.setUpdateTime(LocalDateTime.now());
        memberMapper.update(member);

        String reason;
        if (newLevel.getUpgradeType() != null && newLevel.getUpgradeType() == 2) {
            reason = "累计积分达到" + newLevel.getUpgradeCondition() + "分，自动升级";
        } else {
            reason = "累计消费达到" + newLevel.getUpgradeCondition() + "元，自动升级";
        }

        MemberLevelLog levelLog = new MemberLevelLog();
        levelLog.setMemberId(member.getId());
        levelLog.setMemberNo(member.getMemberNo());
        levelLog.setChangeType(1);
        levelLog.setOldLevelId(oldLevelId);
        levelLog.setOldLevelName(oldLevelName);
        levelLog.setNewLevelId(newLevel.getId());
        levelLog.setNewLevelName(newLevel.getLevelName());
        levelLog.setReason(reason);
        levelLog.setTriggerType(1);
        levelLog.setCreateTime(LocalDateTime.now());
        memberLevelLogMapper.insert(levelLog);
    }

    private void doDowngrade(Member member, MemberLevel currentLevel, MemberLevel newLevel,
                              BigDecimal yearlySpent, BigDecimal keepCondition) {
        Long oldLevelId = member.getLevelId();
        String oldLevelName = member.getLevelName();

        member.setLevelId(newLevel.getId());
        member.setLevelName(newLevel.getLevelName());
        member.setUpdateTime(LocalDateTime.now());
        memberMapper.update(member);

        String reason = "年度消费仅" + yearlySpent + "元，未达到保级要求" + keepCondition + "元，自动降级";

        MemberLevelLog levelLog = new MemberLevelLog();
        levelLog.setMemberId(member.getId());
        levelLog.setMemberNo(member.getMemberNo());
        levelLog.setChangeType(2);
        levelLog.setOldLevelId(oldLevelId);
        levelLog.setOldLevelName(oldLevelName);
        levelLog.setNewLevelId(newLevel.getId());
        levelLog.setNewLevelName(newLevel.getLevelName());
        levelLog.setReason(reason);
        levelLog.setTriggerType(1);
        levelLog.setCreateTime(LocalDateTime.now());
        memberLevelLogMapper.insert(levelLog);
    }

    private MemberLevel findHighestUpgradeLevel(Member member, List<MemberLevel> allLevels) {
        MemberLevel currentLevel = null;
        for (MemberLevel level : allLevels) {
            if (level.getId().equals(member.getLevelId())) {
                currentLevel = level;
                break;
            }
        }
        if (currentLevel == null) return null;

        MemberLevel highest = currentLevel;
        for (MemberLevel level : allLevels) {
            if (level.getSortOrder() <= currentLevel.getSortOrder()) continue;
            if (level.getStatus() == null || level.getStatus() != 1) continue;

            boolean canUpgrade = false;
            if (level.getUpgradeType() != null && level.getUpgradeType() == 2) {
                BigDecimal totalPoints = member.getTotalPoints() != null ? member.getTotalPoints() : BigDecimal.ZERO;
                BigDecimal condition = level.getUpgradeCondition() != null ? level.getUpgradeCondition() : BigDecimal.ZERO;
                if (totalPoints.compareTo(condition) >= 0) {
                    canUpgrade = true;
                }
            } else {
                BigDecimal totalSpent = member.getTotalSpent() != null ? member.getTotalSpent() : BigDecimal.ZERO;
                BigDecimal condition = level.getUpgradeCondition() != null ? level.getUpgradeCondition() : BigDecimal.ZERO;
                if (totalSpent.compareTo(condition) >= 0) {
                    canUpgrade = true;
                }
            }

            if (canUpgrade && level.getSortOrder() > highest.getSortOrder()) {
                highest = level;
            }
        }
        return highest;
    }

    private MemberLevel findNextLowerLevel(MemberLevel currentLevel, List<MemberLevel> allLevels) {
        MemberLevel nextLower = null;
        for (MemberLevel level : allLevels) {
            if (level.getSortOrder() < currentLevel.getSortOrder()) {
                if (nextLower == null || level.getSortOrder() > nextLower.getSortOrder()) {
                    nextLower = level;
                }
            }
        }
        return nextLower;
    }

    private List<MemberLevel> getAllLevelsSorted() {
        QueryWrapper query = QueryWrapper.create()
                .from(MemberLevel.class)
                .where(MEMBER_LEVEL.DELETED.eq(0))
                .orderBy(MEMBER_LEVEL.SORT_ORDER.asc());
        return memberLevelMapper.selectListByQuery(query);
    }

    private void saveTaskLog(int taskType, String taskName, int processCount, int upgradeCount,
                              int downgradeCount, int resetCount, String errorMsg,
                              Long operatorId, String operatorName, int triggerType) {
        MemberLevelTaskLog log = new MemberLevelTaskLog();
        log.setTaskType(taskType);
        log.setTaskName(taskName);
        log.setExecuteTime(LocalDateTime.now());
        log.setExecuteResult(errorMsg == null ? 1 : 2);
        log.setProcessCount(processCount);
        log.setUpgradeCount(upgradeCount);
        log.setDowngradeCount(downgradeCount);
        log.setResetCount(resetCount);
        log.setErrorMsg(errorMsg);
        log.setOperatorId(operatorId);
        log.setOperatorName(operatorName);
        log.setTriggerType(triggerType);
        log.setCreateTime(LocalDateTime.now());
        memberLevelTaskLogMapper.insert(log);
    }

    private Map<String, Object> buildResult(int processCount, int upgradeCount, int downgradeCount, int resetCount) {
        Map<String, Object> result = new HashMap<>();
        result.put("processCount", processCount);
        result.put("upgradeCount", upgradeCount);
        result.put("downgradeCount", downgradeCount);
        result.put("resetCount", resetCount);
        return result;
    }

    public PageResult<MemberLevelLog> getLevelLogPage(Integer pageNum, Integer pageSize,
                                                       Long memberId, String memberNo,
                                                       Integer changeType, Integer triggerType,
                                                       String startTime, String endTime) {
        QueryWrapper query = QueryWrapper.create()
                .from(MemberLevelLog.class)
                .where(MEMBER_LEVEL_LOG.ID.isNotNull());

        if (memberId != null) {
            query.and(MEMBER_LEVEL_LOG.MEMBER_ID.eq(memberId));
        }
        if (memberNo != null && !memberNo.isEmpty()) {
            query.and(MEMBER_LEVEL_LOG.MEMBER_NO.like(memberNo));
        }
        if (changeType != null) {
            query.and(MEMBER_LEVEL_LOG.CHANGE_TYPE.eq(changeType));
        }
        if (triggerType != null) {
            query.and(MEMBER_LEVEL_LOG.TRIGGER_TYPE.eq(triggerType));
        }
        if (startTime != null && !startTime.isEmpty()) {
            query.and(MEMBER_LEVEL_LOG.CREATE_TIME.ge(LocalDateTime.parse(startTime + " 00:00:00",
                    DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))));
        }
        if (endTime != null && !endTime.isEmpty()) {
            query.and(MEMBER_LEVEL_LOG.CREATE_TIME.le(LocalDateTime.parse(endTime + " 23:59:59",
                    DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))));
        }

        query.orderBy(MEMBER_LEVEL_LOG.CREATE_TIME.desc());

        Page<MemberLevelLog> page = memberLevelLogMapper.paginate(pageNum, pageSize, query);
        return new PageResult<>(page.getTotalRow(), page.getRecords(), (long) pageNum, (long) pageSize);
    }

    public Map<String, Object> getLevelChangeStatistics() {
        Map<String, Object> result = new HashMap<>();

        LocalDateTime monthStart = LocalDate.now().withDayOfMonth(1).atStartOfDay();

        long upgradeThisMonth = memberLevelLogMapper.selectCountByQuery(
                QueryWrapper.create()
                        .from(MemberLevelLog.class)
                        .where(MEMBER_LEVEL_LOG.CHANGE_TYPE.eq(1))
                        .and(MEMBER_LEVEL_LOG.CREATE_TIME.ge(monthStart))
        );

        long downgradeThisMonth = memberLevelLogMapper.selectCountByQuery(
                QueryWrapper.create()
                        .from(MemberLevelLog.class)
                        .where(MEMBER_LEVEL_LOG.CHANGE_TYPE.eq(2))
                        .and(MEMBER_LEVEL_LOG.CREATE_TIME.ge(monthStart))
        );

        result.put("upgradeThisMonth", upgradeThisMonth);
        result.put("downgradeThisMonth", downgradeThisMonth);

        List<Map<String, Object>> byLevelStats = getStatsByLevel();
        result.put("byLevel", byLevelStats);

        List<Map<String, Object>> trend = getTrend12Months();
        result.put("trend", trend);

        return result;
    }

    private List<Map<String, Object>> getStatsByLevel() {
        List<Map<String, Object>> list = new ArrayList<>();
        List<MemberLevel> levels = getAllLevelsSorted();

        for (MemberLevel level : levels) {
            long upgradeCount = memberLevelLogMapper.selectCountByQuery(
                    QueryWrapper.create()
                            .from(MemberLevelLog.class)
                            .where(MEMBER_LEVEL_LOG.CHANGE_TYPE.eq(1))
                            .and(MEMBER_LEVEL_LOG.NEW_LEVEL_ID.eq(level.getId()))
            );
            long downgradeCount = memberLevelLogMapper.selectCountByQuery(
                    QueryWrapper.create()
                            .from(MemberLevelLog.class)
                            .where(MEMBER_LEVEL_LOG.CHANGE_TYPE.eq(2))
                            .and(MEMBER_LEVEL_LOG.NEW_LEVEL_ID.eq(level.getId()))
            );
            Map<String, Object> item = new HashMap<>();
            item.put("levelId", level.getId());
            item.put("levelName", level.getLevelName());
            item.put("levelColor", level.getLevelColor());
            item.put("upgradeCount", upgradeCount);
            item.put("downgradeCount", downgradeCount);
            list.add(item);
        }
        return list;
    }

    private List<Map<String, Object>> getTrend12Months() {
        List<Map<String, Object>> trend = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM");

        for (int i = 11; i >= 0; i--) {
            LocalDate monthDate = LocalDate.now().minusMonths(i).withDayOfMonth(1);
            String monthStr = monthDate.format(formatter);
            LocalDateTime monthStart = monthDate.atStartOfDay();
            LocalDateTime monthEnd = monthDate.plusMonths(1).atStartOfDay().minusNanos(1);

            long upgradeCount = memberLevelLogMapper.selectCountByQuery(
                    QueryWrapper.create()
                            .from(MemberLevelLog.class)
                            .where(MEMBER_LEVEL_LOG.CHANGE_TYPE.eq(1))
                            .and(MEMBER_LEVEL_LOG.CREATE_TIME.between(monthStart, monthEnd))
            );
            long downgradeCount = memberLevelLogMapper.selectCountByQuery(
                    QueryWrapper.create()
                            .from(MemberLevelLog.class)
                            .where(MEMBER_LEVEL_LOG.CHANGE_TYPE.eq(2))
                            .and(MEMBER_LEVEL_LOG.CREATE_TIME.between(monthStart, monthEnd))
            );

            Map<String, Object> item = new HashMap<>();
            item.put("month", monthStr);
            item.put("upgradeCount", upgradeCount);
            item.put("downgradeCount", downgradeCount);
            trend.add(item);
        }
        return trend;
    }

    public PageResult<MemberLevelTaskLog> getTaskLogPage(Integer pageNum, Integer pageSize,
                                                           Integer taskType, Integer executeResult,
                                                           String startTime, String endTime) {
        QueryWrapper query = QueryWrapper.create()
                .from(MemberLevelTaskLog.class)
                .where(MEMBER_LEVEL_TASK_LOG.ID.isNotNull());

        if (taskType != null) {
            query.and(MEMBER_LEVEL_TASK_LOG.TASK_TYPE.eq(taskType));
        }
        if (executeResult != null) {
            query.and(MEMBER_LEVEL_TASK_LOG.EXECUTE_RESULT.eq(executeResult));
        }
        if (startTime != null && !startTime.isEmpty()) {
            query.and(MEMBER_LEVEL_TASK_LOG.EXECUTE_TIME.ge(LocalDateTime.parse(startTime + " 00:00:00",
                    DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))));
        }
        if (endTime != null && !endTime.isEmpty()) {
            query.and(MEMBER_LEVEL_TASK_LOG.EXECUTE_TIME.le(LocalDateTime.parse(endTime + " 23:59:59",
                    DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))));
        }

        query.orderBy(MEMBER_LEVEL_TASK_LOG.EXECUTE_TIME.desc());

        Page<MemberLevelTaskLog> page = memberLevelTaskLogMapper.paginate(pageNum, pageSize, query);
        return new PageResult<>(page.getTotalRow(), page.getRecords(), (long) pageNum, (long) pageSize);
    }
}
