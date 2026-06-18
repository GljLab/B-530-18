package com.example.permission.controller;

import com.example.permission.common.Result;
import com.example.permission.service.FinanceAdvancedService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/finance/advanced")
public class FinanceAdvancedController {

    @Autowired
    private FinanceAdvancedService financeAdvancedService;

    @GetMapping("/collection/detail")
    @PreAuthorize("hasAuthority('finance:collection:query')")
    public Result<Map<String, Object>> getCollectionDetail(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) Integer paymentType,
            @RequestParam(required = false) Integer paymentMethod,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        Map<String, Object> result = financeAdvancedService.getCollectionDetail(startDate, endDate, paymentType, paymentMethod, pageNum, pageSize);
        return Result.success(result);
    }

    @GetMapping("/collection/trend")
    @PreAuthorize("hasAuthority('finance:collection:query')")
    public Result<Map<String, Object>> getCollectionTrend(
            @RequestParam(defaultValue = "30") int days) {
        Map<String, Object> result = financeAdvancedService.getCollectionTrend(days);
        return Result.success(result);
    }

    @GetMapping("/payment-method/analysis")
    @PreAuthorize("hasAuthority('finance:collection:query')")
    public Result<Map<String, Object>> getPaymentMethodAnalysis(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        Map<String, Object> result = financeAdvancedService.getPaymentMethodAnalysis(startDate, endDate);
        return Result.success(result);
    }

    @GetMapping("/cashier/statistics")
    @PreAuthorize("hasAuthority('finance:cashier:query')")
    public Result<Map<String, Object>> getCashierStatistics(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        Map<String, Object> result = financeAdvancedService.getCashierStatistics(startDate, endDate);
        return Result.success(result);
    }

    @GetMapping("/cashier/{cashierId}/detail")
    @PreAuthorize("hasAuthority('finance:cashier:query')")
    public Result<Map<String, Object>> getCashierDetail(
            @PathVariable Long cashierId,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        Map<String, Object> result = financeAdvancedService.getCashierDetail(cashierId, startDate, endDate);
        return Result.success(result);
    }

    @GetMapping("/revenue/analysis")
    @PreAuthorize("hasAuthority('finance:revenue:query')")
    public Result<Map<String, Object>> getRevenueAnalysis(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        Map<String, Object> result = financeAdvancedService.getRevenueAnalysis(startDate, endDate);
        return Result.success(result);
    }

    @GetMapping("/cost-profit/analysis")
    @PreAuthorize("hasAuthority('finance:costprofit:query')")
    public Result<Map<String, Object>> getCostProfitAnalysis(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        Map<String, Object> result = financeAdvancedService.getCostProfitAnalysis(startDate, endDate);
        return Result.success(result);
    }

    @GetMapping("/receivable/analysis")
    @PreAuthorize("hasAuthority('finance:receivable:query')")
    public Result<Map<String, Object>> getReceivableAnalysis(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        Map<String, Object> result = financeAdvancedService.getReceivableAnalysis(startDate, endDate);
        return Result.success(result);
    }

    @GetMapping("/cashflow/analysis")
    @PreAuthorize("hasAuthority('finance:cashflow:query')")
    public Result<Map<String, Object>> getCashFlowAnalysis(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        Map<String, Object> result = financeAdvancedService.getCashFlowAnalysis(startDate, endDate);
        return Result.success(result);
    }

    @GetMapping("/dashboard")
    @PreAuthorize("hasAuthority('finance:dashboard:query')")
    public Result<Map<String, Object>> getFinanceDashboard() {
        Map<String, Object> result = financeAdvancedService.getFinanceDashboard();
        return Result.success(result);
    }

    @GetMapping("/comparison")
    @PreAuthorize("hasAuthority('finance:comparison:query')")
    public Result<Map<String, Object>> getComparisonAnalysis(
            @RequestParam(defaultValue = "time") String type) {
        Map<String, Object> result = financeAdvancedService.getComparisonAnalysis(type);
        return Result.success(result);
    }
}
