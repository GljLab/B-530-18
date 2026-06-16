package com.example.permission.controller;

import com.example.permission.common.PageResult;
import com.example.permission.common.Result;
import com.example.permission.entity.BookingRule;
import com.example.permission.entity.BookingRuleExemption;
import com.example.permission.entity.BookingRuleValidationLog;
import com.example.permission.security.LoginUser;
import com.example.permission.service.BookingRuleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/bookingRule")
public class BookingRuleController {

    @Autowired
    private BookingRuleService bookingRuleService;

    @GetMapping("/page")
    @PreAuthorize("hasAuthority('inventory:rule:query')")
    public Result<PageResult<BookingRule>> getRulePage(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String ruleName,
            @RequestParam(required = false) Integer ruleType,
            @RequestParam(required = false) Integer enabled) {
        PageResult<BookingRule> result = bookingRuleService.getRulePage(pageNum, pageSize, ruleName, ruleType, enabled);
        return Result.success(result);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('inventory:rule:query')")
    public Result<BookingRule> getRuleById(@PathVariable Long id) {
        BookingRule result = bookingRuleService.getRuleById(id);
        return Result.success(result);
    }

    @PostMapping
    @PreAuthorize("hasAuthority('inventory:rule:add')")
    public Result<Void> createRule(@RequestBody BookingRule rule) {
        LoginUser loginUser = (LoginUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        bookingRuleService.createRule(rule, loginUser);
        return Result.success();
    }

    @PutMapping
    @PreAuthorize("hasAuthority('inventory:rule:edit')")
    public Result<Void> updateRule(@RequestBody BookingRule rule) {
        bookingRuleService.updateRule(rule);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('inventory:rule:delete')")
    public Result<Void> deleteRule(@PathVariable Long id) {
        bookingRuleService.deleteRule(id);
        return Result.success();
    }

    @PutMapping("/{id}/toggle")
    @PreAuthorize("hasAuthority('inventory:rule:edit')")
    public Result<Void> toggleRule(@PathVariable Long id, @RequestBody Map<String, Object> params) {
        Integer enabled = params.get("enabled") != null ? Integer.valueOf(params.get("enabled").toString()) : null;
        bookingRuleService.toggleRule(id, enabled);
        return Result.success();
    }

    @PostMapping("/validate")
    @PreAuthorize("hasAuthority('booking:create')")
    public Result<List<Map>> validateBooking(@RequestBody Map<String, Object> bookingParams) {
        LoginUser loginUser = (LoginUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        List<Map> result = bookingRuleService.validateBooking(bookingParams, loginUser);
        return Result.success(result);
    }

    @PostMapping("/exempt")
    @PreAuthorize("hasAuthority('inventory:rule:exempt')")
    public Result<Void> exemptRule(@RequestBody Map<String, Object> params) {
        LoginUser loginUser = (LoginUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Long bookingId = params.get("bookingId") != null ? Long.valueOf(params.get("bookingId").toString()) : null;
        Long ruleId = params.get("ruleId") != null ? Long.valueOf(params.get("ruleId").toString()) : null;
        String exemptionReason = params.get("exemptionReason") != null ? params.get("exemptionReason").toString() : null;
        bookingRuleService.exemptRule(bookingId, ruleId, exemptionReason, loginUser);
        return Result.success();
    }

    @GetMapping("/exemption/{bookingId}")
    @PreAuthorize("hasAuthority('inventory:rule:query')")
    public Result<List<BookingRuleExemption>> getExemptionsByBooking(@PathVariable Long bookingId) {
        List<BookingRuleExemption> result = bookingRuleService.getExemptionsByBooking(bookingId);
        return Result.success(result);
    }

    @GetMapping("/validationLog/{bookingId}")
    @PreAuthorize("hasAuthority('inventory:rule:query')")
    public Result<List<BookingRuleValidationLog>> getValidationLogs(@PathVariable Long bookingId) {
        List<BookingRuleValidationLog> result = bookingRuleService.getValidationLogs(bookingId);
        return Result.success(result);
    }
}
