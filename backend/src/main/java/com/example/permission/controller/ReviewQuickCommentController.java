package com.example.permission.controller;

import com.example.permission.common.Result;
import com.example.permission.entity.ReviewQuickComment;
import com.example.permission.service.ReviewQuickCommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/review/quickComment")
public class ReviewQuickCommentController {

    @Autowired
    private ReviewQuickCommentService reviewQuickCommentService;

    @GetMapping("/list")
    @PreAuthorize("hasAuthority('review:tag:query')")
    public Result<List<ReviewQuickComment>> list(@RequestParam(required = false) Integer commentType) {
        List<ReviewQuickComment> list;
        if (commentType != null) {
            list = reviewQuickCommentService.listByType(commentType);
        } else {
            list = reviewQuickCommentService.listAll();
        }
        return Result.success(list);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('review:tag:query')")
    public Result<ReviewQuickComment> getById(@PathVariable Long id) {
        ReviewQuickComment comment = reviewQuickCommentService.getById(id);
        return Result.success(comment);
    }

    @PostMapping
    @PreAuthorize("hasAuthority('review:comment:add')")
    public Result<ReviewQuickComment> add(@RequestBody ReviewQuickComment comment) {
        ReviewQuickComment created = reviewQuickCommentService.add(comment);
        return Result.success(created);
    }

    @PutMapping
    @PreAuthorize("hasAuthority('review:comment:edit')")
    public Result<Void> update(@RequestBody ReviewQuickComment comment) {
        reviewQuickCommentService.update(comment);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('review:comment:delete')")
    public Result<Void> delete(@PathVariable Long id) {
        reviewQuickCommentService.delete(id);
        return Result.success();
    }
}
