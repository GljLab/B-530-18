package com.example.permission.controller;

import com.example.permission.common.PageResult;
import com.example.permission.common.Result;
import com.example.permission.entity.RefundRecord;
import com.example.permission.security.LoginUser;
import com.example.permission.service.RefundRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/refund")
public class RefundController {

    @Autowired
    private RefundRecordService refundRecordService;

    @GetMapping("/page")
    @PreAuthorize("hasAuthority('booking:refund:approve')")
    public Result<PageResult<RefundRecord>> getRefundPage(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String bookingNo,
            @RequestParam(required = false) String customerName) {
        PageResult<RefundRecord> result = refundRecordService.getRefundPage(pageNum, pageSize,
                status, bookingNo, customerName);
        return Result.success(result);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('booking:refund:approve')")
    public Result<RefundRecord> getRefundDetail(@PathVariable Long id) {
        RefundRecord result = refundRecordService.getRefundDetail(id);
        return Result.success(result);
    }

    @PutMapping("/{id}/approve")
    @PreAuthorize("hasAuthority('booking:refund:approve')")
    @Transactional(rollbackFor = Exception.class)
    public Result<Void> approveRefund(@PathVariable Long id,
                                      @RequestBody Map<String, Object> params) {
        LoginUser loginUser = (LoginUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Boolean approved = params.get("approved") != null ? (Boolean) params.get("approved") : false;
        String approveRemark = params.get("approveRemark") != null ? params.get("approveRemark").toString() : null;
        refundRecordService.approveRefund(id, approved, approveRemark, loginUser);
        return Result.success();
    }

    @PutMapping("/{id}/process")
    @PreAuthorize("hasAuthority('booking:refund:approve')")
    @Transactional(rollbackFor = Exception.class)
    public Result<Void> processRefund(@PathVariable Long id) {
        LoginUser loginUser = (LoginUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        refundRecordService.processRefund(id, loginUser);
        return Result.success();
    }
}
