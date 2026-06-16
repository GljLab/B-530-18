package com.example.permission.controller;

import com.example.permission.common.Result;
import com.example.permission.service.AdvancedStatisticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/analytics")
public class AdvancedStatisticsController {

    @Autowired
    private AdvancedStatisticsService advancedStatisticsService;

    @GetMapping("/occupancy")
    @PreAuthorize("hasAuthority('analytics:occupancy:query')")
    public Result<List<Map>> getOccupancyRate(
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate,
            @RequestParam(required = false) Long roomTypeId,
            @RequestParam(required = false) Long floorId,
            @RequestParam(required = false) String period) {
        List<Map> result = advancedStatisticsService.getOccupancyRate(startDate, endDate, roomTypeId, floorId, period);
        return Result.success(result);
    }

    @GetMapping("/bookingCycle")
    @PreAuthorize("hasAuthority('analytics:cycle:query')")
    public Result<Map> getBookingCycleAnalysis(
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate,
            @RequestParam(required = false) Long roomTypeId,
            @RequestParam(required = false) Long channelId,
            @RequestParam(required = false) String customerType) {
        Map result = advancedStatisticsService.getBookingCycleAnalysis(startDate, endDate, roomTypeId, channelId, customerType);
        return Result.success(result);
    }

    @GetMapping("/customerBehavior")
    @PreAuthorize("hasAuthority('analytics:behavior:query')")
    public Result<Map> getCustomerBehaviorAnalysis(
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate) {
        Map result = advancedStatisticsService.getCustomerBehaviorAnalysis(startDate, endDate);
        return Result.success(result);
    }

    @GetMapping("/revenue")
    @PreAuthorize("hasAuthority('analytics:revenue:query')")
    public Result<Map> getRevenueAnalysis(
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate,
            @RequestParam(required = false) Long roomTypeId,
            @RequestParam(required = false) Long channelId) {
        Map result = advancedStatisticsService.getRevenueAnalysis(startDate, endDate, roomTypeId, channelId);
        return Result.success(result);
    }

    @PostMapping("/export")
    @PreAuthorize("hasAuthority('analytics:revenue:export')")
    public ResponseEntity<byte[]> exportAnalytics(@RequestBody Map<String, Object> params) throws Exception {
        String type = params.get("type") != null ? params.get("type").toString() : null;
        LocalDate startDate = params.get("startDate") != null ? LocalDate.parse(params.get("startDate").toString()) : null;
        LocalDate endDate = params.get("endDate") != null ? LocalDate.parse(params.get("endDate").toString()) : null;
        byte[] data = advancedStatisticsService.exportAnalytics(type, startDate, endDate, params);

        String fileName = type + "_分析数据_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".xlsx";
        String encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8.name()).replaceAll("\\+", "%20");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
        headers.setContentDispositionFormData("attachment", encodedFileName);
        headers.add("Content-Disposition", "attachment; filename*=UTF-8''" + encodedFileName);

        return ResponseEntity.ok()
                .headers(headers)
                .body(data);
    }
}
