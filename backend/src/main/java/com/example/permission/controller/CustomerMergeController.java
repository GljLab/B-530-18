package com.example.permission.controller;

import com.example.permission.common.Result;
import com.example.permission.entity.Customer;
import com.example.permission.security.LoginUser;
import com.example.permission.service.CustomerMergeService;
import com.example.permission.service.CustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/customer/merge")
public class CustomerMergeController {

    @Autowired
    private CustomerMergeService customerMergeService;

    @PostMapping("/check-duplicate")
    @PreAuthorize("hasAuthority('customer:dedup')")
    public Result<List<Map<String, Object>>> checkDuplicate(@RequestBody Map<String, Object> params) {
        String name = params.get("name") != null ? params.get("name").toString() : null;
        String idNumber = params.get("idNumber") != null ? params.get("idNumber").toString() : null;
        Integer idType = params.get("idType") != null ? Integer.valueOf(params.get("idType").toString()) : null;
        Long excludeId = params.get("excludeId") != null ? Long.valueOf(params.get("excludeId").toString()) : null;
        List<Map<String, Object>> result = customerMergeService.checkDuplicate(name, idNumber, idType, excludeId);
        return Result.success(result);
    }

    @GetMapping("/scan-duplicates")
    @PreAuthorize("hasAuthority('customer:dedup')")
    public Result<List<Map<String, Object>>> scanDuplicates() {
        List<Map<String, Object>> result = customerMergeService.scanAllDuplicates();
        return Result.success(result);
    }

    @PostMapping("/merge")
    @PreAuthorize("hasAuthority('customer:merge')")
    public Result<Void> merge(@RequestBody Map<String, Object> params) {
        Long sourceId = Long.valueOf(params.get("sourceId").toString());
        Long targetId = Long.valueOf(params.get("targetId").toString());
        @SuppressWarnings("unchecked")
        Map<String, Object> fieldSelection = (Map<String, Object>) params.get("fieldSelection");
        LoginUser loginUser = (LoginUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        customerMergeService.mergeCustomers(sourceId, targetId, fieldSelection,
                loginUser.getUserId(), loginUser.getUser().getNickname());
        return Result.success();
    }
}
