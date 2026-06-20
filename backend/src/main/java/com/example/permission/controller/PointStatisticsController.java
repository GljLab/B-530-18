package com.example.permission.controller;

import com.example.permission.common.Result;
import com.example.permission.service.PointStatisticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/member/pointStatistics")
public class PointStatisticsController {

    @Autowired
    private PointStatisticsService pointStatisticsService;

    @GetMapping("/overview")
    @PreAuthorize("hasAuthority('member:pointStatistics:query')")
    public Result<Map<String, Object>> getOverview() {
        Map<String, Object> overview = pointStatisticsService.getOverview();
        return Result.success(overview);
    }

    @GetMapping("/source")
    @PreAuthorize("hasAuthority('member:pointStatistics:query')")
    public Result<List<Map<String, Object>>> getSourceAnalysis() {
        List<Map<String, Object>> analysis = pointStatisticsService.getSourceAnalysis();
        return Result.success(analysis);
    }

    @GetMapping("/usage")
    @PreAuthorize("hasAuthority('member:pointStatistics:query')")
    public Result<List<Map<String, Object>>> getUsageAnalysis() {
        List<Map<String, Object>> analysis = pointStatisticsService.getUsageAnalysis();
        return Result.success(analysis);
    }

    @GetMapping("/trend")
    @PreAuthorize("hasAuthority('member:pointStatistics:query')")
    public Result<List<Map<String, Object>>> getTrend(@RequestParam(required = false, defaultValue = "12") Integer months) {
        List<Map<String, Object>> trend = pointStatisticsService.getTrend(months);
        return Result.success(trend);
    }

    @GetMapping("/conversion")
    @PreAuthorize("hasAuthority('member:pointStatistics:query')")
    public Result<Map<String, Object>> getConversionAnalysis() {
        Map<String, Object> analysis = pointStatisticsService.getConversionAnalysis();
        return Result.success(analysis);
    }
}
