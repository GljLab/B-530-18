package com.example.permission.controller;

import com.example.permission.common.Result;
import com.example.permission.entity.ReviewRecord;
import com.example.permission.service.ReviewSubmissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/review/submission")
public class ReviewSubmissionController {

    @Autowired
    private ReviewSubmissionService reviewSubmissionService;

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('review:invitation:viewReview')")
    public Result<ReviewRecord> getById(@PathVariable Long id) {
        ReviewRecord record = reviewSubmissionService.getById(id);
        return Result.success(record);
    }

    @GetMapping("/byCheckIn/{checkInId}")
    @PreAuthorize("hasAuthority('review:invitation:viewReview')")
    public Result<ReviewRecord> getByCheckInId(@PathVariable Long checkInId) {
        ReviewRecord record = reviewSubmissionService.getByCheckInId(checkInId);
        return Result.success(record);
    }
}
