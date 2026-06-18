package com.example.permission.controller;

import com.example.permission.common.Result;
import com.example.permission.service.FinanceStatisticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/finance/statistics")
public class FinanceStatisticsController {

    @Autowired
    private FinanceStatisticsService financeStatisticsService;

    @GetMapping("/payment-summary")
    @PreAuthorize("hasAuthority('finance:summary:query')")
    public Result<Map<String, Object>> getPaymentSummary(
            @RequestParam String startDate,
            @RequestParam String endDate) {
        Map<String, Object> result = financeStatisticsService.getPaymentSummary(startDate, endDate);
        return Result.success(result);
    }

    @GetMapping("/receivable-monitor")
    @PreAuthorize("hasAuthority('finance:receivable:query')")
    public Result<List<Map<String, Object>>> getReceivableMonitor() {
        List<Map<String, Object>> result = financeStatisticsService.getReceivableMonitor();
        return Result.success(result);
    }
}
