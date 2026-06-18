package com.example.permission.controller;

import com.example.permission.common.PageResult;
import com.example.permission.common.Result;
import com.example.permission.entity.ShiftReconciliation;
import com.example.permission.security.LoginUser;
import com.example.permission.service.ShiftReconciliationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/finance/shift-reconciliation")
public class ShiftReconciliationController {

    @Autowired
    private ShiftReconciliationService shiftReconciliationService;

    @GetMapping("/page")
    @PreAuthorize("hasAuthority('finance:shift:query')")
    public Result<PageResult<ShiftReconciliation>> getPage(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Integer shiftType,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        PageResult<ShiftReconciliation> result = shiftReconciliationService.getPage(pageNum, pageSize, status, shiftType, startDate, endDate);
        return Result.success(result);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('finance:shift:query')")
    public Result<ShiftReconciliation> getById(@PathVariable Long id) {
        ShiftReconciliation result = shiftReconciliationService.getById(id);
        return Result.success(result);
    }

    @PostMapping
    @PreAuthorize("hasAuthority('finance:shift:add')")
    public Result<Void> createShift(@RequestBody ShiftReconciliation shiftReconciliation) {
        LoginUser loginUser = (LoginUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        shiftReconciliationService.createShift(shiftReconciliation, loginUser);
        return Result.success();
    }

    @PutMapping("/{id}/takeover")
    @PreAuthorize("hasAuthority('finance:shift:confirm')")
    public Result<Void> confirmTakeover(@PathVariable Long id, @RequestBody Map<String, Object> params) {
        Long takeoverUserId = params.get("takeoverUserId") != null ? Long.valueOf(params.get("takeoverUserId").toString()) : null;
        String takeoverUserName = params.get("takeoverUserName") != null ? params.get("takeoverUserName").toString() : null;
        String takeoverRemark = params.get("takeoverRemark") != null ? params.get("takeoverRemark").toString() : null;
        shiftReconciliationService.confirmTakeover(id, takeoverUserId, takeoverUserName, takeoverRemark);
        return Result.success();
    }
}
