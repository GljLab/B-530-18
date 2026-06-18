package com.example.permission.controller;

import com.example.permission.common.PageResult;
import com.example.permission.common.Result;
import com.example.permission.entity.CreditBill;
import com.example.permission.entity.CreditSettlement;
import com.example.permission.security.LoginUser;
import com.example.permission.service.CreditBillService;
import com.example.permission.service.CreditSettlementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/finance/credit-settlement")
public class CreditSettlementController {

    @Autowired
    private CreditSettlementService creditSettlementService;

    @Autowired
    private CreditBillService creditBillService;

    @GetMapping("/page")
    @PreAuthorize("hasAuthority('finance:settlement:query')")
    public Result<PageResult<CreditSettlement>> getPage(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Long agreementUnitId,
            @RequestParam(required = false) Integer status) {
        PageResult<CreditSettlement> result = creditSettlementService.getPage(pageNum, pageSize, agreementUnitId, status);
        return Result.success(result);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('finance:settlement:query')")
    public Result<CreditSettlement> getById(@PathVariable Long id) {
        CreditSettlement result = creditSettlementService.getById(id);
        return Result.success(result);
    }

    @GetMapping("/bills")
    @PreAuthorize("hasAuthority('finance:settlement:query')")
    public Result<List<CreditBill>> getBills(
            @RequestParam Long agreementUnitId,
            @RequestParam String periodStart,
            @RequestParam String periodEnd) {
        List<CreditBill> result = creditBillService.getBillsByUnitAndPeriod(agreementUnitId, periodStart, periodEnd);
        return Result.success(result);
    }

    @PostMapping
    @PreAuthorize("hasAuthority('finance:settlement:add')")
    public Result<Void> createSettlement(@RequestBody Map<String, Object> params) {
        LoginUser loginUser = (LoginUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        CreditSettlement settlement = new CreditSettlement();
        settlement.setAgreementUnitId(Long.valueOf(params.get("agreementUnitId").toString()));
        settlement.setAgreementUnitName(params.get("agreementUnitName") != null ? params.get("agreementUnitName").toString() : null);
        settlement.setPeriodStart(params.get("periodStart") != null ? java.time.LocalDate.parse(params.get("periodStart").toString()) : null);
        settlement.setPeriodEnd(params.get("periodEnd") != null ? java.time.LocalDate.parse(params.get("periodEnd").toString()) : null);
        settlement.setSettlementMethod(params.get("settlementMethod") != null ? Integer.valueOf(params.get("settlementMethod").toString()) : null);
        settlement.setSettlementDate(params.get("settlementDate") != null ? java.time.LocalDate.parse(params.get("settlementDate").toString()) : null);
        settlement.setVoucherNo(params.get("voucherNo") != null ? params.get("voucherNo").toString() : null);
        settlement.setInvoiceNo(params.get("invoiceNo") != null ? params.get("invoiceNo").toString() : null);
        settlement.setRemark(params.get("remark") != null ? params.get("remark").toString() : null);
        @SuppressWarnings("unchecked")
        List<Number> rawBillIds = (List<Number>) params.get("billIds");
        List<Long> billIds = rawBillIds != null ? rawBillIds.stream().map(Number::longValue).toList() : List.of();
        creditSettlementService.createSettlement(settlement, billIds, loginUser);
        return Result.success();
    }

    @PutMapping("/{id}/confirm")
    @PreAuthorize("hasAuthority('finance:settlement:approve')")
    public Result<Void> confirmSettlement(@PathVariable Long id) {
        creditSettlementService.confirmSettlement(id);
        return Result.success();
    }
}
