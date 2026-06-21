package com.example.permission.controller;

import com.example.permission.common.PageResult;
import com.example.permission.common.Result;
import com.example.permission.entity.MemberLevelLog;
import com.example.permission.security.LoginUser;
import com.example.permission.service.MemberLevelUpgradeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/member/levelChange")
public class MemberLevelChangeController {

    @Autowired
    private MemberLevelUpgradeService memberLevelUpgradeService;

    @GetMapping("/log/page")
    @PreAuthorize("hasAuthority('member:levelLog:list')")
    public Result<PageResult<MemberLevelLog>> getLevelLogPage(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Long memberId,
            @RequestParam(required = false) String memberNo,
            @RequestParam(required = false) Integer changeType,
            @RequestParam(required = false) Integer triggerType,
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime) {
        PageResult<MemberLevelLog> result = memberLevelUpgradeService.getLevelLogPage(
                pageNum, pageSize, memberId, memberNo, changeType, triggerType, startTime, endTime);
        return Result.success(result);
    }

    @GetMapping("/statistics")
    @PreAuthorize("hasAuthority('member:levelChange:statistics')")
    public Result<Map<String, Object>> getStatistics() {
        Map<String, Object> statistics = memberLevelUpgradeService.getLevelChangeStatistics();
        return Result.success(statistics);
    }
}
