package com.example.permission.controller;

import com.example.permission.common.Result;
import com.example.permission.entity.BadDebt;
import com.example.permission.entity.BadDebtAction;
import com.example.permission.service.BadDebtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/finance/bad-debt")
public class BadDebtController {

    @Autowired
    private BadDebtService badDebtService;

    @GetMapping("/page")
    @PreAuthorize("hasAuthority('finance:badDebt:query')")
    public Result<Map<String, Object>> getBadDebtPage(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer debtType,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        Map<String, Object> result = badDebtService.getBadDebtPage(keyword, debtType, status, startDate, endDate, pageNum, pageSize);
        return Result.success(result);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('finance:badDebt:query')")
    public Result<BadDebt> getBadDebtById(@PathVariable Long id) {
        BadDebt badDebt = badDebtService.getBadDebtById(id);
        return Result.success(badDebt);
    }

    @GetMapping("/{id}/actions")
    @PreAuthorize("hasAuthority('finance:badDebt:query')")
    public Result<List<BadDebtAction>> getBadDebtActions(@PathVariable Long id) {
        List<BadDebtAction> actions = badDebtService.getBadDebtActions(id);
        return Result.success(actions);
    }

    @PostMapping("/identify")
    @PreAuthorize("hasAuthority('finance:badDebt:query')")
    public Result<String> identifyBadDebts() {
        badDebtService.identifyBadDebts();
        return Result.success("识别完成");
    }

    @PostMapping("/{id}/collect")
    @PreAuthorize("hasAuthority('finance:badDebt:collect')")
    public Result<BadDebtAction> collectDebt(
            @PathVariable Long id,
            @RequestParam Integer method,
            @RequestParam String result,
            @RequestParam Long operatorId,
            @RequestParam String operatorName) {
        BadDebtAction action = badDebtService.collectDebt(id, method, result, operatorId, operatorName);
        return Result.success(action);
    }

    @PostMapping("/{id}/recover")
    @PreAuthorize("hasAuthority('finance:badDebt:recover')")
    public Result<BadDebtAction> recoverDebt(
            @PathVariable Long id,
            @RequestParam BigDecimal amount,
            @RequestParam Integer paymentMethod,
            @RequestParam Long operatorId,
            @RequestParam String operatorName) {
        BadDebtAction action = badDebtService.recoverDebt(id, amount, paymentMethod, operatorId, operatorName);
        return Result.success(action);
    }

    @PutMapping("/{id}/writeoff")
    @PreAuthorize("hasAuthority('finance:badDebt:writeoff')")
    public Result<BadDebt> writeOffDebt(
            @PathVariable Long id,
            @RequestParam String reason,
            @RequestParam(required = false) String proof,
            @RequestParam Long approverId,
            @RequestParam String approverName) {
        BadDebt badDebt = badDebtService.writeOffDebt(id, reason, proof, approverId, approverName);
        return Result.success(badDebt);
    }

    @PutMapping("/{id}/legal")
    @PreAuthorize("hasAuthority('finance:badDebt:legal')")
    public Result<BadDebt> legalPursuit(
            @PathVariable Long id,
            @RequestParam Integer legalStatus,
            @RequestParam(required = false) String legalInfo,
            @RequestParam Long operatorId) {
        BadDebt badDebt = badDebtService.legalPursuit(id, legalStatus, legalInfo, operatorId);
        return Result.success(badDebt);
    }

    @GetMapping("/statistics")
    @PreAuthorize("hasAuthority('finance:badDebt:query')")
    public Result<Map<String, Object>> getBadDebtStatistics() {
        Map<String, Object> result = badDebtService.getBadDebtStatistics();
        return Result.success(result);
    }

    @GetMapping("/export")
    @PreAuthorize("hasAuthority('finance:badDebt:export')")
    public Result<List<BadDebt>> exportBadDebts(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer debtType,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        List<BadDebt> list = badDebtService.exportBadDebts(keyword, debtType, status, startDate, endDate);
        return Result.success(list);
    }
}
