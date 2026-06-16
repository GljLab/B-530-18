package com.example.permission.controller;

import com.example.permission.common.Result;
import com.example.permission.entity.CustomerTag;
import com.example.permission.security.LoginUser;
import com.example.permission.service.CustomerTagService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/customer/tag")
public class CustomerTagController {

    @Autowired
    private CustomerTagService customerTagService;

    @GetMapping("/list")
    @PreAuthorize("hasAuthority('customer:tag:query')")
    public Result<List<CustomerTag>> list() {
        List<CustomerTag> tags = customerTagService.listAll();
        return Result.success(tags);
    }

    @PostMapping
    @PreAuthorize("hasAuthority('customer:tag:manage')")
    public Result<CustomerTag> add(@RequestBody CustomerTag customerTag) {
        CustomerTag created = customerTagService.addTag(customerTag);
        return Result.success(created);
    }

    @PutMapping
    @PreAuthorize("hasAuthority('customer:tag:manage')")
    public Result<Void> update(@RequestBody CustomerTag customerTag) {
        customerTagService.updateTag(customerTag);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('customer:tag:manage')")
    public Result<Void> delete(@PathVariable Long id) {
        customerTagService.deleteTag(id);
        return Result.success();
    }

    @GetMapping("/customer/{customerId}")
    @PreAuthorize("hasAuthority('customer:tag:query')")
    public Result<List<CustomerTag>> listByCustomerId(@PathVariable Long customerId) {
        List<CustomerTag> tags = customerTagService.getTagsByCustomerId(customerId);
        return Result.success(tags);
    }

    @PostMapping("/customer/{customerId}/tags")
    @PreAuthorize("hasAuthority('customer:tag:assign')")
    public Result<Void> addTagsToCustomer(@PathVariable Long customerId, @RequestBody List<Long> tagIds) {
        LoginUser loginUser = (LoginUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        customerTagService.addTagsToCustomer(customerId, tagIds, loginUser.getUserId(), loginUser.getUser().getNickname());
        return Result.success();
    }

    @DeleteMapping("/customer/{customerId}/tag/{tagId}")
    @PreAuthorize("hasAuthority('customer:tag:assign')")
    public Result<Void> removeTagFromCustomer(@PathVariable Long customerId, @PathVariable Long tagId) {
        LoginUser loginUser = (LoginUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        customerTagService.removeTagFromCustomer(customerId, tagId, loginUser.getUserId(), loginUser.getUser().getNickname());
        return Result.success();
    }

    @GetMapping("/statistics")
    @PreAuthorize("hasAuthority('customer:tag:query')")
    public Result<List<Map<String, Object>>> statistics() {
        List<Map<String, Object>> stats = customerTagService.getTagStatistics();
        return Result.success(stats);
    }
}
