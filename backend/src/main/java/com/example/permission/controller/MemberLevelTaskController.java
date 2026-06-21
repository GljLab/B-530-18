package com.example.permission.controller;

import com.example.permission.common.PageResult;
import com.example.permission.common.Result;
import com.example.permission.entity.MemberLevelTaskLog;
import com.example.permission.security.LoginUser;
import com.example.permission.service.MemberLevelUpgradeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/member/task")
public class MemberLevelTaskController {

    @Autowired
    private MemberLevelUpgradeService memberLevelUpgradeService;

    @GetMapping("/list")
    @PreAuthorize("hasAuthority('member:task:list')")
    public Result<List<Map<String, Object>>> getTaskList() {
        List<Map<String, Object>> tasks = new ArrayList<>();

        Map<String, Object> upgradeTask = new HashMap<>();
        upgradeTask.put("taskType", 1);
        upgradeTask.put("taskName", "会员升级检查");
        upgradeTask.put("description", "每日凌晨01:00执行，检查所有会员是否满足升级条件");
        upgradeTask.put("cronExpression", "0 0 1 * * ?");
        upgradeTask.put("status", 1);
        tasks.add(upgradeTask);

        Map<String, Object> downgradeTask = new HashMap<>();
        downgradeTask.put("taskType", 2);
        downgradeTask.put("taskName", "保级降级检查");
        downgradeTask.put("description", "每年1月1日凌晨01:00执行，检查非最低等级会员是否满足保级条件");
        downgradeTask.put("cronExpression", "0 0 1 1 1 ?");
        downgradeTask.put("status", 1);
        tasks.add(downgradeTask);

        Map<String, Object> resetTask = new HashMap<>();
        resetTask.put("taskType", 3);
        resetTask.put("taskName", "年度消费金额重置");
        resetTask.put("description", "每年1月1日凌晨01:00执行，重置所有会员的本年度消费金额为0");
        resetTask.put("cronExpression", "0 0 1 1 1 ?");
        resetTask.put("status", 1);
        tasks.add(resetTask);

        return Result.success(tasks);
    }

    @PostMapping("/trigger/{taskType}")
    @PreAuthorize("hasAuthority('member:task:trigger')")
    public Result<Map<String, Object>> triggerTask(@PathVariable Integer taskType) {
        LoginUser loginUser = (LoginUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Map<String, Object> result;
        if (taskType == 1) {
            result = memberLevelUpgradeService.executeUpgradeCheck(
                    loginUser.getUserId(), loginUser.getUser().getNickname(), true);
        } else if (taskType == 2) {
            result = memberLevelUpgradeService.executeDowngradeCheck(
                    loginUser.getUserId(), loginUser.getUser().getNickname(), true);
        } else if (taskType == 3) {
            result = memberLevelUpgradeService.executeYearlyReset(
                    loginUser.getUserId(), loginUser.getUser().getNickname(), true);
        } else {
            return Result.error("无效的任务类型");
        }
        return Result.success(result);
    }

    @GetMapping("/log/page")
    @PreAuthorize("hasAuthority('member:task:query')")
    public Result<PageResult<MemberLevelTaskLog>> getTaskLogPage(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Integer taskType,
            @RequestParam(required = false) Integer executeResult,
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime) {
        PageResult<MemberLevelTaskLog> result = memberLevelUpgradeService.getTaskLogPage(
                pageNum, pageSize, taskType, executeResult, startTime, endTime);
        return Result.success(result);
    }
}
