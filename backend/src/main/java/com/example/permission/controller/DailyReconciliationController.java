package com.example.permission.controller;

import com.example.permission.common.PageResult;
import com.example.permission.common.Result;
import com.example.permission.entity.DailyReconciliation;
import com.example.permission.security.LoginUser;
import com.example.permission.service.DailyReconciliationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/finance/daily-reconciliation")
public class DailyReconciliationController {

    @Autowired
    private DailyReconciliationService dailyReconciliationService;

    @GetMapping("/page")
    @PreAuthorize("hasAuthority('finance:daily:query')")
    public Result<PageResult<DailyReconciliation>> getPage(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        PageResult<DailyReconciliation> result = dailyReconciliationService.getPage(pageNum, pageSize, status, startDate, endDate);
        return Result.success(result);
    }

    @GetMapping("/date/{date}")
    @PreAuthorize("hasAuthority('finance:daily:query')")
    public Result<DailyReconciliation> getByDate(@PathVariable String date) {
        DailyReconciliation result = dailyReconciliationService.getByDate(date);
        return Result.success(result);
    }

    @PostMapping("/auto-calculate")
    @PreAuthorize("hasAuthority('finance:daily:add')")
    public Result<Void> autoCalculate(@RequestBody Map<String, String> params) {
        LoginUser loginUser = (LoginUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String date = params.get("reconcileDate");
        dailyReconciliationService.autoCalculate(date, loginUser);
        return Result.success();
    }

    @PutMapping("/{id}/confirm")
    @PreAuthorize("hasAuthority('finance:daily:approve')")
    public Result<Void> confirmDaily(@PathVariable Long id, @RequestBody DailyReconciliation dailyReconciliation) {
        dailyReconciliationService.confirmDaily(id, dailyReconciliation);
        return Result.success();
    }
}
