package com.example.permission.controller;

import com.example.permission.common.BusinessException;
import com.example.permission.common.Result;
import com.example.permission.entity.Booking;
import com.example.permission.security.LoginUser;
import com.example.permission.service.BookingStatisticsService;
import com.example.permission.service.ExportService;
import com.example.permission.service.SysUserService;
import com.example.permission.utils.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/booking/statistics")
public class BookingStatisticsController {

    @Autowired
    private BookingStatisticsService bookingStatisticsService;

    @Autowired
    private SysUserService sysUserService;

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private ExportService exportService;

    @GetMapping("/today")
    @PreAuthorize("hasAuthority('booking:statistics:query')")
    public Result<Map<String, Object>> getTodayStats() {
        Map<String, Object> result = bookingStatisticsService.getTodayStats();
        return Result.success(result);
    }

    @GetMapping("/status")
    @PreAuthorize("hasAuthority('booking:statistics:query')")
    public Result<List<Map<String, Object>>> getBookingStatusStats() {
        List<Map<String, Object>> result = bookingStatisticsService.getBookingStatusStats();
        return Result.success(result);
    }

    @GetMapping("/source")
    @PreAuthorize("hasAuthority('booking:statistics:query')")
    public Result<List<Map<String, Object>>> getBookingSourceStats() {
        List<Map<String, Object>> result = bookingStatisticsService.getBookingSourceStats();
        return Result.success(result);
    }

    @GetMapping("/trend")
    @PreAuthorize("hasAuthority('booking:statistics:query')")
    public Result<List<Map<String, Object>>> getBookingTrend(
            @RequestParam(required = false) Integer days) {
        List<Map<String, Object>> result = bookingStatisticsService.getBookingTrend(days);
        return Result.success(result);
    }

    @GetMapping("/roomType")
    @PreAuthorize("hasAuthority('booking:statistics:query')")
    public Result<List<Map<String, Object>>> getRoomTypeBookingStats() {
        List<Map<String, Object>> result = bookingStatisticsService.getRoomTypeBookingStats();
        return Result.success(result);
    }

    @GetMapping("/calendar")
    @PreAuthorize("hasAuthority('booking:calendar:view')")
    public Result<List<Map<String, Object>>> getCalendarData(
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month) {
        if (year == null || month == null) {
            LocalDate now = LocalDate.now();
            year = now.getYear();
            month = now.getMonthValue();
        }
        List<Map<String, Object>> result = bookingStatisticsService.getCalendarData(year, month);
        return Result.success(result);
    }

    @GetMapping("/day/{date}")
    @PreAuthorize("hasAuthority('booking:calendar:view')")
    public Result<List<Booking>> getDayBookings(@PathVariable String date) {
        LocalDate localDate = null;
        try {
            localDate = LocalDate.parse(date);
        } catch (Exception e) {
            throw new BusinessException("日期格式不正确，请使用 yyyy-MM-dd 格式");
        }
        List<Booking> result = bookingStatisticsService.getDayBookings(localDate);
        return Result.success(result);
    }

    @PostMapping("/export")
    @PreAuthorize("hasAuthority('booking:statistics:export')")
    public ResponseEntity<byte[]> exportStatistics(@RequestBody Map<String, Object> params,
                                                    @RequestHeader("Authorization") String token) throws Exception {
        String actualToken = token.replace("Bearer ", "");
        Long userId = jwtUtils.getUserIdFromToken(actualToken);
        String username = sysUserService.getUserDetail(userId).getUsername();

        String scope = params.get("scope") != null ? params.get("scope").toString() : "current";

        Map<String, Object> todayStats = null;
        List<Map<String, Object>> statusStats = null;
        List<Map<String, Object>> sourceStats = null;
        List<Map<String, Object>> trendStats = null;
        List<Map<String, Object>> roomTypeStats = null;

        if ("all".equals(scope)) {
            todayStats = bookingStatisticsService.getTodayStats();
            statusStats = bookingStatisticsService.getBookingStatusStats();
            sourceStats = bookingStatisticsService.getBookingSourceStats();
            trendStats = bookingStatisticsService.getBookingTrend(30);
            roomTypeStats = bookingStatisticsService.getRoomTypeBookingStats();
        } else {
            List<String> exportTypes = (List<String>) params.get("exportTypes");
            if (exportTypes != null && !exportTypes.isEmpty()) {
                if (exportTypes.contains("today")) {
                    todayStats = bookingStatisticsService.getTodayStats();
                }
                if (exportTypes.contains("status")) {
                    statusStats = bookingStatisticsService.getBookingStatusStats();
                }
                if (exportTypes.contains("source")) {
                    sourceStats = bookingStatisticsService.getBookingSourceStats();
                }
                if (exportTypes.contains("trend")) {
                    Integer days = params.get("trendDays") != null ?
                            Integer.valueOf(params.get("trendDays").toString()) : 7;
                    trendStats = bookingStatisticsService.getBookingTrend(days);
                }
                if (exportTypes.contains("roomType")) {
                    roomTypeStats = bookingStatisticsService.getRoomTypeBookingStats();
                }
            }
        }

        params.put("todayStats", todayStats);
        params.put("statusStats", statusStats);
        params.put("sourceStats", sourceStats);
        params.put("trendStats", trendStats);
        params.put("roomTypeStats", roomTypeStats);

        String filterDesc = buildStatisticsFilterDesc(params);

        byte[] excelData = exportService.exportStatistics(params, username, filterDesc);
        String fileName = "预订统计_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".xlsx";
        String encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8.name()).replaceAll("\\+", "%20");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
        headers.setContentDispositionFormData("attachment", encodedFileName);
        headers.add("Content-Disposition", "attachment; filename*=UTF-8''" + encodedFileName);

        return ResponseEntity.ok()
                .headers(headers)
                .body(excelData);
    }

    private String buildStatisticsFilterDesc(Map<String, Object> params) {
        StringBuilder sb = new StringBuilder();
        List<String> exportTypes = (List<String>) params.get("exportTypes");
        if (exportTypes != null && !exportTypes.isEmpty()) {
            sb.append("导出内容:");
            if (exportTypes.contains("today")) sb.append("今日统计,");
            if (exportTypes.contains("status")) sb.append("状态统计,");
            if (exportTypes.contains("source")) sb.append("来源统计,");
            if (exportTypes.contains("trend")) sb.append("预订趋势,");
            if (exportTypes.contains("roomType")) sb.append("房型统计,");
            if (sb.length() > 0) sb.deleteCharAt(sb.length() - 1);
        }
        return sb.length() == 0 ? "全部统计数据" : sb.toString();
    }
}
