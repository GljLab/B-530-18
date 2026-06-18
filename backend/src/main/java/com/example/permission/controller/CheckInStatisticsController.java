package com.example.permission.controller;

import com.example.permission.common.Result;
import com.example.permission.service.CheckInStatisticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/checkin/statistics")
public class CheckInStatisticsController {

    @Autowired
    private CheckInStatisticsService checkInStatisticsService;

    @GetMapping("/overview")
    public Result<Map<String, Object>> getOverviewStats() {
        Map<String, Object> stats = checkInStatisticsService.getOverviewStats();
        return Result.success(stats);
    }

    @GetMapping("/trend")
    public Result<List<Map<String, Object>>> getCheckInTrend(
            @RequestParam(required = false, defaultValue = "7") Integer days) {
        List<Map<String, Object>> data = checkInStatisticsService.getCheckInTrend(days);
        return Result.success(data);
    }

    @GetMapping("/roomType")
    public Result<List<Map<String, Object>>> getRoomTypeStats() {
        List<Map<String, Object>> data = checkInStatisticsService.getRoomTypeStats();
        return Result.success(data);
    }

    @GetMapping("/floor")
    public Result<List<Map<String, Object>>> getFloorStats() {
        List<Map<String, Object>> data = checkInStatisticsService.getFloorStats();
        return Result.success(data);
    }

    @GetMapping("/source")
    public Result<Map<String, Object>> getSourceStats() {
        Map<String, Object> data = checkInStatisticsService.getSourceStats();
        return Result.success(data);
    }

    @GetMapping("/customerType")
    public Result<Map<String, Object>> getCustomerTypeStats() {
        Map<String, Object> data = checkInStatisticsService.getCustomerTypeStats();
        return Result.success(data);
    }

    @GetMapping("/averageStay")
    public Result<Map<String, Object>> getAverageStayDays() {
        Map<String, Object> data = checkInStatisticsService.getAverageStayDays();
        return Result.success(data);
    }

    @GetMapping("/revenue")
    public Result<Map<String, Object>> getRevenueStats(
            @RequestParam(required = false, defaultValue = "7") Integer days) {
        Map<String, Object> data = checkInStatisticsService.getRevenueStats(days);
        return Result.success(data);
    }

    @GetMapping("/region")
    public Result<Map<String, Object>> getRegionStats() {
        Map<String, Object> data = checkInStatisticsService.getRegionStats();
        return Result.success(data);
    }

    @GetMapping("/occupancyRate")
    public Result<List<Map<String, Object>>> getOccupancyRateTrend(
            @RequestParam(required = false, defaultValue = "7") Integer days) {
        List<Map<String, Object>> data = checkInStatisticsService.getOccupancyRateTrend(days);
        return Result.success(data);
    }
}
