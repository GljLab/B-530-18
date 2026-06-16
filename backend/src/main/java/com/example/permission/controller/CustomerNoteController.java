package com.example.permission.controller;

import com.example.permission.common.Result;
import com.example.permission.entity.CustomerNote;
import com.example.permission.security.LoginUser;
import com.example.permission.service.CustomerNoteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/customer/note")
public class CustomerNoteController {

    @Autowired
    private CustomerNoteService customerNoteService;

    @GetMapping("/{customerId}")
    @PreAuthorize("hasAuthority('customer:note:query')")
    public Result<List<CustomerNote>> list(@PathVariable Long customerId) {
        List<CustomerNote> notes = customerNoteService.listByCustomerId(customerId);
        return Result.success(notes);
    }

    @PostMapping
    @PreAuthorize("hasAuthority('customer:note:add')")
    public Result<CustomerNote> add(@RequestBody CustomerNote note) {
        LoginUser loginUser = (LoginUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        CustomerNote created = customerNoteService.addNote(note, loginUser.getUserId(), loginUser.getUser().getNickname());
        return Result.success(created);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('customer:note:add')")
    public Result<Void> delete(@PathVariable Long id) {
        LoginUser loginUser = (LoginUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        customerNoteService.deleteNote(id, loginUser.getUserId(), loginUser.getUser().getNickname());
        return Result.success();
    }

    @PutMapping("/{id}/pin")
    @PreAuthorize("hasAuthority('customer:note:add')")
    public Result<Void> pin(@PathVariable Long id) {
        LoginUser loginUser = (LoginUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        customerNoteService.pinNote(id, loginUser.getUserId(), loginUser.getUser().getNickname());
        return Result.success();
    }

    @PutMapping("/{id}/unpin")
    @PreAuthorize("hasAuthority('customer:note:add')")
    public Result<Void> unpin(@PathVariable Long id) {
        LoginUser loginUser = (LoginUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        customerNoteService.unpinNote(id, loginUser.getUserId(), loginUser.getUser().getNickname());
        return Result.success();
    }
}
