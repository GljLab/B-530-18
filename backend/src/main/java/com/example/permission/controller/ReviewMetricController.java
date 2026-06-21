package com.example.permission.controller;

import com.example.permission.common.Result;
import com.example.permission.entity.ReviewMetric;
import com.example.permission.service.ReviewMetricService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/review/metric")
public class ReviewMetricController {

    @Autowired
    private ReviewMetricService reviewMetricService;

    @GetMapping("/list")
    @PreAuthorize("hasAuthority('review:metric:query')")
    public Result<List<ReviewMetric>> list() {
        List<ReviewMetric> list = reviewMetricService.listAll();
        return Result.success(list);
    }

    @GetMapping("/enabled")
    @PreAuthorize("hasAuthority('review:metric:query')")
    public Result<List<ReviewMetric>> listEnabled() {
        List<ReviewMetric> list = reviewMetricService.listEnabled();
        return Result.success(list);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('review:metric:query')")
    public Result<ReviewMetric> getById(@PathVariable Long id) {
        ReviewMetric metric = reviewMetricService.getById(id);
        return Result.success(metric);
    }

    @GetMapping("/totalWeight")
    @PreAuthorize("hasAuthority('review:metric:query')")
    public Result<Map<String, Object>> getTotalWeight() {
        int totalWeight = reviewMetricService.getTotalWeight();
        Map<String, Object> result = new HashMap<>();
        result.put("totalWeight", totalWeight);
        result.put("isHundred", totalWeight == 100);
        return Result.success(result);
    }

    @PostMapping
    @PreAuthorize("hasAuthority('review:metric:add')")
    public Result<ReviewMetric> add(@RequestBody ReviewMetric metric) {
        ReviewMetric created = reviewMetricService.add(metric);
        return Result.success(created);
    }

    @PutMapping
    @PreAuthorize("hasAuthority('review:metric:edit')")
    public Result<Void> update(@RequestBody ReviewMetric metric) {
        reviewMetricService.update(metric);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('review:metric:delete')")
    public Result<Void> delete(@PathVariable Long id) {
        reviewMetricService.delete(id);
        return Result.success();
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasAuthority('review:metric:status')")
    public Result<Void> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        reviewMetricService.updateStatus(id, status);
        return Result.success();
    }
}
