package com.example.permission.controller;

import com.example.permission.common.PageResult;
import com.example.permission.common.Result;
import com.example.permission.entity.RefundApply;
import com.example.permission.security.LoginUser;
import com.example.permission.service.RefundApplyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/api/finance/refund-apply")
public class RefundApplyController {

    @Autowired
    private RefundApplyService refundApplyService;

    @GetMapping("/page")
    @PreAuthorize("hasAuthority('finance:refund:query')")
    public Result<PageResult<RefundApply>> getPage(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Integer refundType,
            @RequestParam(required = false) String keyword) {
        PageResult<RefundApply> result = refundApplyService.getPage(pageNum, pageSize, status, refundType, keyword);
        return Result.success(result);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('finance:refund:query')")
    public Result<RefundApply> getById(@PathVariable Long id) {
        RefundApply result = refundApplyService.getById(id);
        return Result.success(result);
    }

    @PostMapping
    @PreAuthorize("hasAuthority('finance:refund:add')")
    public Result<Void> apply(@RequestBody RefundApply refundApply) {
        LoginUser loginUser = (LoginUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        refundApplyService.apply(refundApply, loginUser);
        return Result.success();
    }

    @PutMapping("/{id}/approve")
    @PreAuthorize("hasAuthority('finance:refund:approve')")
    public Result<Void> approve(@PathVariable Long id, @RequestBody Map<String, Object> params) {
        LoginUser loginUser = (LoginUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Boolean approved = params.get("approved") != null ? (Boolean) params.get("approved") : false;
        String approveRemark = params.get("approveRemark") != null ? params.get("approveRemark").toString() : null;
        BigDecimal approvedAmount = params.get("approvedAmount") != null ? new BigDecimal(params.get("approvedAmount").toString()) : null;
        refundApplyService.approve(id, approved, approveRemark, approvedAmount, loginUser);
        return Result.success();
    }

    @PutMapping("/{id}/execute")
    @PreAuthorize("hasAuthority('finance:refund:approve')")
    public Result<Void> executeRefund(@PathVariable Long id, @RequestBody Map<String, Object> params) {
        LoginUser loginUser = (LoginUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Integer refundMethod = params.get("refundMethod") != null ? Integer.valueOf(params.get("refundMethod").toString()) : null;
        String refundVoucherNo = params.get("refundVoucherNo") != null ? params.get("refundVoucherNo").toString() : null;
        refundApplyService.executeRefund(id, refundMethod, refundVoucherNo, loginUser);
        return Result.success();
    }
}
