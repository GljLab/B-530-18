package com.example.permission.controller;

import com.example.permission.common.Result;
import com.example.permission.entity.Invoice;
import com.example.permission.service.InvoiceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/finance/invoice")
public class InvoiceController {

    @Autowired
    private InvoiceService invoiceService;

    @GetMapping("/page")
    @PreAuthorize("hasAuthority('finance:invoice:query')")
    public Result<Map<String, Object>> getInvoicePage(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer invoiceType,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        Map<String, Object> result = invoiceService.getInvoicePage(keyword, invoiceType, status, startDate, endDate, pageNum, pageSize);
        return Result.success(result);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('finance:invoice:query')")
    public Result<Invoice> getInvoiceById(@PathVariable Long id) {
        Invoice invoice = invoiceService.getInvoiceById(id);
        return Result.success(invoice);
    }

    @PostMapping
    @PreAuthorize("hasAuthority('finance:invoice:add')")
    public Result<Invoice> createInvoice(@RequestBody Invoice invoice) {
        Invoice created = invoiceService.createInvoice(invoice);
        return Result.success(created);
    }

    @PutMapping("/{id}/process")
    @PreAuthorize("hasAuthority('finance:invoice:process')")
    public Result<Invoice> processInvoice(
            @PathVariable Long id,
            @RequestParam String invoiceCode,
            @RequestParam String invoiceNumber,
            @RequestParam(required = false) String invoicePdf,
            @RequestParam Long processorId,
            @RequestParam String processorName) {
        Invoice invoice = invoiceService.processInvoice(id, invoiceCode, invoiceNumber, invoicePdf, processorId, processorName);
        return Result.success(invoice);
    }

    @PutMapping("/{id}/mail")
    @PreAuthorize("hasAuthority('finance:invoice:mail')")
    public Result<Invoice> mailInvoice(
            @PathVariable Long id,
            @RequestParam String mailRecipient,
            @RequestParam String mailAddress,
            @RequestParam String mailCompany,
            @RequestParam String mailTrackingNo,
            @RequestParam Long operatorId) {
        Invoice invoice = invoiceService.mailInvoice(id, mailRecipient, mailAddress, mailCompany, mailTrackingNo, operatorId);
        return Result.success(invoice);
    }

    @GetMapping("/statistics")
    @PreAuthorize("hasAuthority('finance:invoice:query')")
    public Result<Map<String, Object>> getInvoiceStatistics(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        Map<String, Object> result = invoiceService.getInvoiceStatistics(startDate, endDate);
        return Result.success(result);
    }

    @GetMapping("/export")
    @PreAuthorize("hasAuthority('finance:invoice:export')")
    public Result<List<Invoice>> exportInvoices(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer invoiceType,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        List<Invoice> list = invoiceService.exportInvoices(keyword, invoiceType, status, startDate, endDate);
        return Result.success(list);
    }
}
