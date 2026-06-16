package com.example.permission.controller;

import com.example.permission.common.Result;
import com.example.permission.entity.CustomerPreference;
import com.example.permission.security.LoginUser;
import com.example.permission.service.CustomerPreferenceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/customer/preference")
public class CustomerPreferenceController {

    @Autowired
    private CustomerPreferenceService customerPreferenceService;

    @GetMapping("/{customerId}")
    @PreAuthorize("hasAuthority('customer:preference:query')")
    public Result<CustomerPreference> getByCustomerId(@PathVariable Long customerId) {
        CustomerPreference preference = customerPreferenceService.getByCustomerId(customerId);
        return Result.success(preference);
    }

    @PostMapping
    @PreAuthorize("hasAuthority('customer:preference:manage')")
    public Result<CustomerPreference> save(@RequestBody CustomerPreference customerPreference) {
        LoginUser loginUser = (LoginUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        CustomerPreference saved = customerPreferenceService.saveOrUpdate(customerPreference, loginUser.getUserId(), loginUser.getUser().getNickname());
        return Result.success(saved);
    }
}
