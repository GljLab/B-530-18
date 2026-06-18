package com.example.permission.controller;

import com.example.permission.common.Result;
import com.example.permission.entity.MemberLevel;
import com.example.permission.security.LoginUser;
import com.example.permission.service.MemberLevelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/member/level")
public class MemberLevelController {

    @Autowired
    private MemberLevelService memberLevelService;

    @GetMapping("/list")
    @PreAuthorize("hasAuthority('member:level:list')")
    public Result<List<MemberLevel>> list() {
        List<MemberLevel> levels = memberLevelService.listAll();
        return Result.success(levels);
    }

    @GetMapping("/enabled")
    @PreAuthorize("hasAuthority('member:level:query')")
    public Result<List<MemberLevel>> listEnabled() {
        List<MemberLevel> levels = memberLevelService.listEnabled();
        return Result.success(levels);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('member:level:query')")
    public Result<MemberLevel> getById(@PathVariable Long id) {
        MemberLevel level = memberLevelService.getById(id);
        return Result.success(level);
    }

    @PostMapping
    @PreAuthorize("hasAuthority('member:level:add')")
    public Result<MemberLevel> add(@RequestBody MemberLevel level) {
        MemberLevel created = memberLevelService.addLevel(level);
        return Result.success(created);
    }

    @PutMapping
    @PreAuthorize("hasAuthority('member:level:edit')")
    public Result<Void> update(@RequestBody MemberLevel level) {
        memberLevelService.updateLevel(level);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('member:level:delete')")
    public Result<Void> delete(@PathVariable Long id) {
        memberLevelService.deleteLevel(id);
        return Result.success();
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasAuthority('member:level:edit')")
    public Result<Void> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        memberLevelService.updateStatus(id, status);
        return Result.success();
    }
}
