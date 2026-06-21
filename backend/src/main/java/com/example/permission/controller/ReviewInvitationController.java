package com.example.permission.controller;

import com.example.permission.common.BusinessException;
import com.example.permission.common.PageResult;
import com.example.permission.common.Result;
import com.example.permission.entity.CheckIn;
import com.example.permission.entity.ReviewInvitation;
import com.example.permission.service.CheckInService;
import com.example.permission.service.ReviewInvitationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/review/invitation")
public class ReviewInvitationController {

    @Autowired
    private ReviewInvitationService reviewInvitationService;

    @Autowired
    private CheckInService checkInService;

    @GetMapping("/page")
    @PreAuthorize("hasAuthority('review:invitation:query')")
    public Result<PageResult<ReviewInvitation>> page(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer reviewStatus,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        Map<String, Object> params = new HashMap<>();
        params.put("keyword", keyword);
        params.put("reviewStatus", reviewStatus);
        params.put("startDate", startDate);
        params.put("endDate", endDate);
        PageResult<ReviewInvitation> page = reviewInvitationService.list(params, pageNum, pageSize);
        return Result.success(page);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('review:invitation:query')")
    public Result<ReviewInvitation> getById(@PathVariable Long id) {
        ReviewInvitation invitation = reviewInvitationService.getById(id);
        return Result.success(invitation);
    }

    @GetMapping("/checkIn/{checkInId}")
    @PreAuthorize("hasAuthority('review:invitation:query')")
    public Result<ReviewInvitation> getByCheckInId(@PathVariable Long checkInId) {
        ReviewInvitation invitation = reviewInvitationService.getByCheckInId(checkInId);
        return Result.success(invitation);
    }

    @PostMapping
    @PreAuthorize("hasAuthority('review:invitation:add')")
    public Result<ReviewInvitation> create(@RequestBody Map<String, Object> params) {
        Long checkInId = Long.valueOf(params.get("checkInId").toString());
        CheckIn checkIn = checkInService.getDetail(checkInId);
        ReviewInvitation invitation = reviewInvitationService.createInvitation(checkIn);
        return Result.success(invitation);
    }

    @PutMapping("/send/{id}")
    @PreAuthorize("hasAuthority('review:invitation:send')")
    public Result<Void> send(@PathVariable Long id, @RequestParam Integer sendMethod) {
        if (sendMethod == null || (sendMethod != 1 && sendMethod != 2)) {
            throw new BusinessException("发送方式参数错误，1-短信, 2-邮件");
        }
        reviewInvitationService.sendInvitation(id, sendMethod);
        return Result.success();
    }

    @GetMapping("/link/{id}")
    @PreAuthorize("hasAuthority('review:invitation:copy')")
    public Result<String> getReviewLink(@PathVariable Long id) {
        String link = reviewInvitationService.getReviewLink(id);
        return Result.success(link);
    }

    @GetMapping("/validate")
    @PreAuthorize("hasAuthority('review:invitation:query')")
    public Result<ReviewInvitation> validate(
            @RequestParam String order,
            @RequestParam String code) {
        ReviewInvitation invitation = reviewInvitationService.validateLink(order, code);
        return Result.success(invitation);
    }
}
