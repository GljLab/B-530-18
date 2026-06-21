package com.example.permission.controller;

import com.example.permission.common.PageResult;
import com.example.permission.common.Result;
import com.example.permission.service.MemberAnalyticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/member/analytics")
public class MemberAnalyticsController {

    @Autowired
    private MemberAnalyticsService memberAnalyticsService;

    // ==================== 1. 会员价值分析 ====================

    @GetMapping("/value")
    @PreAuthorize("hasAuthority('member:analytics:value')")
    public Result<Map<String, Object>> getMemberValueAnalysis() {
        Map<String, Object> result = memberAnalyticsService.getMemberValueAnalysis();
        return Result.success(result);
    }

    @GetMapping("/value/vsNonMember")
    @PreAuthorize("hasAuthority('member:analytics:value')")
    public Result<Map<String, Object>> getMemberVsNonMember() {
        Map<String, Object> result = memberAnalyticsService.getMemberVsNonMember();
        return Result.success(result);
    }

    @GetMapping("/value/revenueContribution")
    @PreAuthorize("hasAuthority('member:analytics:value')")
    public Result<Map<String, Object>> getMemberRevenueContribution() {
        Map<String, Object> result = memberAnalyticsService.getMemberRevenueContribution();
        return Result.success(result);
    }

    @GetMapping("/value/ltv")
    @PreAuthorize("hasAuthority('member:analytics:value')")
    public Result<Map<String, Object>> getMemberLTVAnalysis() {
        Map<String, Object> result = memberAnalyticsService.getMemberLTVAnalysis();
        return Result.success(result);
    }

    // ==================== 2. 等级分布分析 ====================

    @GetMapping("/level")
    @PreAuthorize("hasAuthority('member:analytics:level')")
    public Result<Map<String, Object>> getLevelDistributionAnalysis() {
        Map<String, Object> result = memberAnalyticsService.getLevelDistributionAnalysis();
        return Result.success(result);
    }

    @GetMapping("/level/comparison")
    @PreAuthorize("hasAuthority('member:analytics:level')")
    public Result<List<Map<String, Object>>> getLevelDataComparison() {
        List<Map<String, Object>> result = memberAnalyticsService.getLevelDataComparison();
        return Result.success(result);
    }

    @GetMapping("/level/flow")
    @PreAuthorize("hasAuthority('member:analytics:level')")
    public Result<Map<String, Object>> getLevelFlowAnalysis() {
        Map<String, Object> result = memberAnalyticsService.getLevelFlowAnalysis();
        return Result.success(result);
    }

    @GetMapping("/level/health")
    @PreAuthorize("hasAuthority('member:analytics:level')")
    public Result<List<Map<String, Object>>> getLevelHealthAnalysis() {
        List<Map<String, Object>> result = memberAnalyticsService.getLevelHealthAnalysis();
        return Result.success(result);
    }

    // ==================== 3. 行为特征分析 ====================

    @GetMapping("/behavior")
    @PreAuthorize("hasAuthority('member:analytics:behavior')")
    public Result<Map<String, Object>> getMemberBehaviorAnalysis() {
        Map<String, Object> result = memberAnalyticsService.getMemberBehaviorAnalysis();
        return Result.success(result);
    }

    @GetMapping("/behavior/frequency")
    @PreAuthorize("hasAuthority('member:analytics:behavior')")
    public Result<Map<String, Object>> getConsumptionFrequencyAnalysis() {
        Map<String, Object> result = memberAnalyticsService.getConsumptionFrequencyAnalysis();
        return Result.success(result);
    }

    @GetMapping("/behavior/amount")
    @PreAuthorize("hasAuthority('member:analytics:behavior')")
    public Result<Map<String, Object>> getConsumptionAmountAnalysis() {
        Map<String, Object> result = memberAnalyticsService.getConsumptionAmountAnalysis();
        return Result.success(result);
    }

    @GetMapping("/behavior/timePreference")
    @PreAuthorize("hasAuthority('member:analytics:behavior')")
    public Result<Map<String, Object>> getTimePreferenceAnalysis() {
        Map<String, Object> result = memberAnalyticsService.getTimePreferenceAnalysis();
        return Result.success(result);
    }

    // ==================== 4. 流失预警 ====================

    @GetMapping("/churn")
    @PreAuthorize("hasAuthority('member:analytics:churn')")
    public Result<Map<String, Object>> getChurnWarningAnalysis() {
        Map<String, Object> result = memberAnalyticsService.getChurnWarningAnalysis();
        return Result.success(result);
    }

    @GetMapping("/churn/summary")
    @PreAuthorize("hasAuthority('member:analytics:churn')")
    public Result<Map<String, Object>> getChurnRiskSummary() {
        Map<String, Object> result = memberAnalyticsService.getChurnRiskSummary();
        return Result.success(result);
    }

    @GetMapping("/churn/list")
    @PreAuthorize("hasAuthority('member:analytics:churn')")
    public Result<PageResult<Map<String, Object>>> getChurnWarningList(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String riskLevel,
            @RequestParam(required = false) List<Long> levelIds,
            @RequestParam(required = false) String lastStayStart,
            @RequestParam(required = false) String lastStayEnd) {
        PageResult<Map<String, Object>> result = memberAnalyticsService.getChurnWarningList(
                pageNum, pageSize, riskLevel, levelIds, lastStayStart, lastStayEnd);
        return Result.success(result);
    }

    @GetMapping("/churn/trend")
    @PreAuthorize("hasAuthority('member:analytics:churn')")
    public Result<List<Map<String, Object>>> getChurnTrend() {
        List<Map<String, Object>> result = memberAnalyticsService.getChurnTrend();
        return Result.success(result);
    }

    @GetMapping("/churn/byLevel")
    @PreAuthorize("hasAuthority('member:analytics:churn')")
    public Result<List<Map<String, Object>>> getChurnByLevel() {
        List<Map<String, Object>> result = memberAnalyticsService.getChurnByLevel();
        return Result.success(result);
    }

    // ==================== 5. 权益使用与成本统计 ====================

    @GetMapping("/benefit")
    @PreAuthorize("hasAuthority('member:analytics:benefit')")
    public Result<Map<String, Object>> getBenefitUsageAnalysis() {
        Map<String, Object> result = memberAnalyticsService.getBenefitUsageAnalysis();
        return Result.success(result);
    }

    @GetMapping("/benefit/usageRates")
    @PreAuthorize("hasAuthority('member:analytics:benefit')")
    public Result<Map<String, Object>> getBenefitUsageRates() {
        Map<String, Object> result = memberAnalyticsService.getBenefitUsageRates();
        return Result.success(result);
    }

    @GetMapping("/benefit/cost")
    @PreAuthorize("hasAuthority('member:analytics:benefit')")
    public Result<Map<String, Object>> getBenefitCostAnalysis() {
        Map<String, Object> result = memberAnalyticsService.getBenefitCostAnalysis();
        return Result.success(result);
    }
}
