package com.example.permission.controller;

import com.example.permission.common.Result;
import com.example.permission.service.ReviewSubmissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/h5/review")
public class ReviewH5Controller {

    @Autowired
    private ReviewSubmissionService reviewSubmissionService;

    @GetMapping("/data")
    public Result<Map<String, Object>> getReviewPageData(
            @RequestParam String order,
            @RequestParam String code) {
        Map<String, Object> data = reviewSubmissionService.getReviewPageData(order, code);
        return Result.success(data);
    }

    @PostMapping("/submit")
    public Result<Map<String, Long>> submitReview(@RequestBody Map<String, Object> reviewData) {
        Long reviewRecordId = reviewSubmissionService.submitReview(reviewData);
        Map<String, Long> result = Map.of("id", reviewRecordId);
        return Result.success(result);
    }

    @GetMapping("/success")
    public Result<Map<String, Object>> getReviewSuccessData(@RequestParam Long id) {
        Map<String, Object> data = reviewSubmissionService.getReviewSuccessData(id);
        return Result.success(data);
    }
}
