package com.example.permission.controller;

import com.example.permission.common.Result;
import com.example.permission.entity.PointConsumeRule;
import com.example.permission.entity.PointEarnRule;
import com.example.permission.service.PointRuleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/member/pointRule")
public class PointRuleController {

    @Autowired
    private PointRuleService pointRuleService;

    @GetMapping("/earn/list")
    @PreAuthorize("hasAuthority('member:pointRule:earn:query')")
    public Result<List<PointEarnRule>> listEarnRules() {
        List<PointEarnRule> rules = pointRuleService.listEarnRules();
        return Result.success(rules);
    }

    @GetMapping("/earn/enabled")
    @PreAuthorize("hasAuthority('member:pointRule:earn:query')")
    public Result<List<PointEarnRule>> listEnabledEarnRules() {
        List<PointEarnRule> rules = pointRuleService.listEnabledEarnRules();
        return Result.success(rules);
    }

    @GetMapping("/earn/{id}")
    @PreAuthorize("hasAuthority('member:pointRule:earn:query')")
    public Result<PointEarnRule> getEarnRule(@PathVariable Long id) {
        PointEarnRule rule = pointRuleService.getEarnRuleById(id);
        return Result.success(rule);
    }

    @PostMapping("/earn")
    @PreAuthorize("hasAuthority('member:pointRule:earn:add')")
    public Result<PointEarnRule> addEarnRule(@RequestBody PointEarnRule rule) {
        PointEarnRule created = pointRuleService.addEarnRule(rule);
        return Result.success(created);
    }

    @PutMapping("/earn")
    @PreAuthorize("hasAuthority('member:pointRule:earn:edit')")
    public Result<Void> updateEarnRule(@RequestBody PointEarnRule rule) {
        pointRuleService.updateEarnRule(rule);
        return Result.success();
    }

    @DeleteMapping("/earn/{id}")
    @PreAuthorize("hasAuthority('member:pointRule:earn:delete')")
    public Result<Void> deleteEarnRule(@PathVariable Long id) {
        pointRuleService.deleteEarnRule(id);
        return Result.success();
    }

    @PutMapping("/earn/{id}/status")
    @PreAuthorize("hasAuthority('member:pointRule:earn:edit')")
    public Result<Void> updateEarnRuleStatus(@PathVariable Long id, @RequestParam Integer status) {
        pointRuleService.updateEarnRuleStatus(id, status);
        return Result.success();
    }

    @GetMapping("/consume/list")
    @PreAuthorize("hasAuthority('member:pointRule:consume:query')")
    public Result<List<PointConsumeRule>> listConsumeRules() {
        List<PointConsumeRule> rules = pointRuleService.listConsumeRules();
        return Result.success(rules);
    }

    @GetMapping("/consume/enabled")
    @PreAuthorize("hasAuthority('member:pointRule:consume:query')")
    public Result<List<PointConsumeRule>> listEnabledConsumeRules() {
        List<PointConsumeRule> rules = pointRuleService.listEnabledConsumeRules();
        return Result.success(rules);
    }

    @GetMapping("/consume/{id}")
    @PreAuthorize("hasAuthority('member:pointRule:consume:query')")
    public Result<PointConsumeRule> getConsumeRule(@PathVariable Long id) {
        PointConsumeRule rule = pointRuleService.getConsumeRuleById(id);
        return Result.success(rule);
    }

    @PostMapping("/consume")
    @PreAuthorize("hasAuthority('member:pointRule:consume:add')")
    public Result<PointConsumeRule> addConsumeRule(@RequestBody PointConsumeRule rule) {
        PointConsumeRule created = pointRuleService.addConsumeRule(rule);
        return Result.success(created);
    }

    @PutMapping("/consume")
    @PreAuthorize("hasAuthority('member:pointRule:consume:edit')")
    public Result<Void> updateConsumeRule(@RequestBody PointConsumeRule rule) {
        pointRuleService.updateConsumeRule(rule);
        return Result.success();
    }

    @DeleteMapping("/consume/{id}")
    @PreAuthorize("hasAuthority('member:pointRule:consume:delete')")
    public Result<Void> deleteConsumeRule(@PathVariable Long id) {
        pointRuleService.deleteConsumeRule(id);
        return Result.success();
    }

    @PutMapping("/consume/{id}/status")
    @PreAuthorize("hasAuthority('member:pointRule:consume:edit')")
    public Result<Void> updateConsumeRuleStatus(@PathVariable Long id, @RequestParam Integer status) {
        pointRuleService.updateConsumeRuleStatus(id, status);
        return Result.success();
    }
}
