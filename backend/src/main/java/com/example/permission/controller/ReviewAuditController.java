package com.example.permission.controller;

import com.example.permission.common.PageResult;
import com.example.permission.common.Result;
import com.example.permission.entity.Member;
import com.example.permission.entity.ReviewRecord;
import com.example.permission.entity.ReviewTag;
import com.example.permission.service.ReviewAuditService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/review/audit")
public class ReviewAuditController {

    @Autowired
    private ReviewAuditService reviewAuditService;

    @GetMapping("/page")
    @PreAuthorize("hasAuthority('review:audit:list')")
    public Result<PageResult<ReviewRecord>> getAuditPage(
            @RequestParam(required = false) Integer reviewStatus,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer scoreFilter,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate,
            @RequestParam(defaultValue = "1") Long pageNum,
            @RequestParam(defaultValue = "10") Long pageSize) {
        PageResult<ReviewRecord> page = reviewAuditService.getAuditPage(
                reviewStatus, keyword, scoreFilter, startDate, endDate, pageNum, pageSize);
        return Result.success(page);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('review:audit:query')")
    public Result<Map<String, Object>> getDetail(@PathVariable Long id) {
        ReviewRecord record = reviewAuditService.getDetail(id);
        List<ReviewTag> tags = reviewAuditService.getTagDetailsByIds(record.getSelectedTags());
        Member member = reviewAuditService.getMemberInfo(record.getMemberId());
        Map<String, Object> result = new HashMap<>();
        result.put("review", record);
        result.put("tags", tags);
        result.put("member", member);
        return Result.success(result);
    }

    @PutMapping("/approve/{id}")
    @PreAuthorize("hasAuthority('review:audit:approve')")
    public Result<Void> approve(@PathVariable Long id) {
        reviewAuditService.approve(id);
        return Result.success();
    }

    @PutMapping("/reject/{id}")
    @PreAuthorize("hasAuthority('review:audit:reject')")
    public Result<Void> reject(@PathVariable Long id, @RequestBody Map<String, String> body) {
        String reasonType = body.get("reasonType");
        String remark = body.get("remark");
        reviewAuditService.reject(id, reasonType, remark);
        return Result.success();
    }

    @PutMapping("/hide/{id}")
    @PreAuthorize("hasAuthority('review:audit:hide')")
    public Result<Void> hide(@PathVariable Long id, @RequestBody Map<String, String> body) {
        String remark = body.get("remark");
        reviewAuditService.hide(id, remark);
        return Result.success();
    }

    @PutMapping("/reapprove/{id}")
    @PreAuthorize("hasAuthority('review:audit:approve')")
    public Result<Void> reapprove(@PathVariable Long id) {
        reviewAuditService.reapprove(id);
        return Result.success();
    }

    @PutMapping("/reply/{id}")
    @PreAuthorize("hasAuthority('review:reply:add')")
    public Result<Void> reply(@PathVariable Long id, @RequestBody Map<String, String> body) {
        String replyContent = body.get("replyContent");
        reviewAuditService.reply(id, replyContent);
        return Result.success();
    }

    @PutMapping("/reply/edit/{id}")
    @PreAuthorize("hasAuthority('review:reply:edit')")
    public Result<Void> editReply(@PathVariable Long id, @RequestBody Map<String, String> body) {
        String replyContent = body.get("replyContent");
        reviewAuditService.editReply(id, replyContent);
        return Result.success();
    }
}
