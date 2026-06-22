package com.example.permission.controller;

import com.example.permission.common.PageResult;
import com.example.permission.common.Result;
import com.example.permission.entity.ReviewRecord;
import com.example.permission.service.ReviewAnalyticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/review/analytics")
public class ReviewAnalyticsController {

    @Autowired
    private ReviewAnalyticsService reviewAnalyticsService;

    @GetMapping("/overview")
    @PreAuthorize("hasAuthority('review:analytics:overview')")
    public Result<Map<String, Object>> getOverview() {
        Map<String, Object> data = reviewAnalyticsService.getOverviewStatistics();
        return Result.success(data);
    }

    @GetMapping("/trend/quantity")
    @PreAuthorize("hasAuthority('review:analytics:trend')")
    public Result<Map<String, Object>> getQuantityTrend(@RequestParam(defaultValue = "12") int months) {
        Map<String, Object> data = reviewAnalyticsService.getQuantityTrend(months);
        return Result.success(data);
    }

    @GetMapping("/trend/score")
    @PreAuthorize("hasAuthority('review:analytics:trend')")
    public Result<Map<String, Object>> getScoreTrend(@RequestParam(defaultValue = "12") int months) {
        Map<String, Object> data = reviewAnalyticsService.getScoreTrend(months);
        return Result.success(data);
    }

    @GetMapping("/metric/radar")
    @PreAuthorize("hasAuthority('review:analytics:metric')")
    public Result<Map<String, Object>> getMetricRadar() {
        Map<String, Object> data = reviewAnalyticsService.getMetricRadar();
        return Result.success(data);
    }

    @GetMapping("/metric/comparison")
    @PreAuthorize("hasAuthority('review:analytics:metric')")
    public Result<Map<String, Object>> getMetricComparison(@RequestParam(defaultValue = "month") String periodType) {
        Map<String, Object> data = reviewAnalyticsService.getMetricComparison(periodType);
        return Result.success(data);
    }

    @GetMapping("/tag/statistics")
    @PreAuthorize("hasAuthority('review:analytics:tag')")
    public Result<Map<String, Object>> getTagStatistics() {
        Map<String, Object> data = reviewAnalyticsService.getTagStatistics();
        return Result.success(data);
    }

    @GetMapping("/tag/trend")
    @PreAuthorize("hasAuthority('review:analytics:tag')")
    public Result<Map<String, Object>> getTagTrend(@RequestParam Long tagId, @RequestParam(defaultValue = "12") int months) {
        Map<String, Object> data = reviewAnalyticsService.getTagTrend(tagId, months);
        return Result.success(data);
    }

    @GetMapping("/roomType/list")
    @PreAuthorize("hasAuthority('review:analytics:roomType')")
    public Result<Map<String, Object>> getRoomTypeAnalysis() {
        Map<String, Object> data = reviewAnalyticsService.getRoomTypeAnalysis();
        return Result.success(data);
    }

    @GetMapping("/roomType/metrics")
    @PreAuthorize("hasAuthority('review:analytics:roomType')")
    public Result<Map<String, Object>> getRoomTypeMetrics(@RequestParam Long roomTypeId) {
        Map<String, Object> data = reviewAnalyticsService.getRoomTypeMetrics(roomTypeId);
        return Result.success(data);
    }

    @GetMapping("/reviews/byScore")
    @PreAuthorize("hasAuthority('review:analytics:list')")
    public Result<PageResult<ReviewRecord>> getReviewsByScore(
            @RequestParam Integer score,
            @RequestParam(defaultValue = "1") Long pageNum,
            @RequestParam(defaultValue = "10") Long pageSize) {
        PageResult<ReviewRecord> data = reviewAnalyticsService.getReviewsByScore(score, pageNum, pageSize);
        return Result.success(data);
    }

    @GetMapping("/reviews/byRoomType")
    @PreAuthorize("hasAuthority('review:analytics:list')")
    public Result<PageResult<ReviewRecord>> getReviewsByRoomType(
            @RequestParam Long roomTypeId,
            @RequestParam(defaultValue = "1") Long pageNum,
            @RequestParam(defaultValue = "10") Long pageSize) {
        PageResult<ReviewRecord> data = reviewAnalyticsService.getReviewsByRoomType(roomTypeId, pageNum, pageSize);
        return Result.success(data);
    }
}
