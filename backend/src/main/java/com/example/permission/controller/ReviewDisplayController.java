package com.example.permission.controller;

import com.example.permission.common.PageResult;
import com.example.permission.common.Result;
import com.example.permission.entity.Member;
import com.example.permission.entity.ReviewRecord;
import com.example.permission.entity.ReviewTag;
import com.example.permission.entity.RoomType;
import com.example.permission.service.ReviewAuditService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/review/display")
public class ReviewDisplayController {

    @Autowired
    private ReviewAuditService reviewAuditService;

    @GetMapping("/statistics")
    @PreAuthorize("hasAuthority('review:display:view')")
    public Result<Map<String, Object>> getStatistics() {
        Map<String, Object> stats = reviewAuditService.getPublicStatistics();
        return Result.success(stats);
    }

    @GetMapping("/roomTypes")
    @PreAuthorize("hasAuthority('review:display:view')")
    public Result<List<RoomType>> getRoomTypeList() {
        List<RoomType> list = reviewAuditService.getRoomTypeList();
        return Result.success(list);
    }

    @GetMapping("/page")
    @PreAuthorize("hasAuthority('review:display:view')")
    public Result<PageResult<Map<String, Object>>> getPublicPage(
            @RequestParam(required = false) Integer scoreFilter,
            @RequestParam(required = false) Long roomTypeId,
            @RequestParam(required = false) Boolean hasImage,
            @RequestParam(defaultValue = "time_desc") String sortType,
            @RequestParam(defaultValue = "1") Long pageNum,
            @RequestParam(defaultValue = "10") Long pageSize) {
        PageResult<ReviewRecord> page = reviewAuditService.getPublicPage(
                scoreFilter, roomTypeId, hasImage, sortType, pageNum, pageSize);

        List<Map<String, Object>> enrichedList = new java.util.ArrayList<>();
        for (ReviewRecord record : page.getList()) {
            Map<String, Object> item = new HashMap<>();
            item.put("review", record);
            List<ReviewTag> tags = reviewAuditService.getTagDetailsByIds(record.getSelectedTags());
            item.put("tags", tags);
            Member member = reviewAuditService.getMemberInfo(record.getMemberId());
            item.put("member", member);
            enrichedList.add(item);
        }

        PageResult<Map<String, Object>> result = new PageResult<>(
                page.getTotal(), enrichedList, page.getPageNum(), page.getPageSize());
        return Result.success(result);
    }
}
