package com.example.permission.controller;

import com.example.permission.common.Result;
import com.example.permission.entity.ReviewTag;
import com.example.permission.service.ReviewTagService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/review/tag")
public class ReviewTagController {

    @Autowired
    private ReviewTagService reviewTagService;

    @GetMapping("/list")
    @PreAuthorize("hasAuthority('review:tag:query')")
    public Result<List<ReviewTag>> list(@RequestParam(required = false) Integer tagType) {
        List<ReviewTag> list = reviewTagService.listByType(tagType);
        return Result.success(list);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('review:tag:query')")
    public Result<ReviewTag> getById(@PathVariable Long id) {
        ReviewTag tag = reviewTagService.getById(id);
        return Result.success(tag);
    }

    @PostMapping
    @PreAuthorize("hasAuthority('review:tag:add')")
    public Result<ReviewTag> add(@RequestBody ReviewTag tag) {
        ReviewTag created = reviewTagService.add(tag);
        return Result.success(created);
    }

    @PutMapping
    @PreAuthorize("hasAuthority('review:tag:edit')")
    public Result<Void> update(@RequestBody ReviewTag tag) {
        reviewTagService.update(tag);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('review:tag:delete')")
    public Result<Void> delete(@PathVariable Long id) {
        reviewTagService.delete(id);
        return Result.success();
    }

    @PutMapping("/sort")
    @PreAuthorize("hasAuthority('review:tag:edit')")
    public Result<Void> updateSort(@RequestBody List<Long> ids) {
        reviewTagService.updateSort(ids);
        return Result.success();
    }
}
