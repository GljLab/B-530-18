package com.example.permission.controller;

import com.example.permission.common.Result;
import com.example.permission.entity.Customer;
import com.example.permission.security.LoginUser;
import com.example.permission.service.CustomerImportExportService;
import com.example.permission.service.CustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/customer/import-export")
public class CustomerImportExportController {

    @Autowired
    private CustomerImportExportService customerImportExportService;

    @Autowired
    private CustomerService customerService;

    @GetMapping("/template")
    @PreAuthorize("hasAuthority('customer:import')")
    public ResponseEntity<byte[]> template() {
        byte[] data = customerImportExportService.getImportTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=customer_import_template.xlsx");
        return ResponseEntity.ok().headers(headers).body(data);
    }

    @PostMapping("/import")
    @PreAuthorize("hasAuthority('customer:import')")
    public Result<Map<String, Object>> importCustomers(@RequestParam("file") MultipartFile file) throws Exception {
        LoginUser loginUser = (LoginUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        byte[] fileData = file.getBytes();
        Map<String, Object> result = customerImportExportService.importCustomers(fileData,
                loginUser.getUserId(), loginUser.getUser().getNickname());
        return Result.success(result);
    }

    @PostMapping("/export")
    @PreAuthorize("hasAuthority('customer:export')")
    public ResponseEntity<byte[]> export(@RequestBody Map<String, Object> params) {
        LoginUser loginUser = (LoginUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        @SuppressWarnings("unchecked")
        List<Long> customerIds = params.get("customerIds") != null
                ? ((List<Number>) params.get("customerIds")).stream().map(Number::longValue).collect(Collectors.toList())
                : null;
        String exportRange = params.get("exportRange") != null ? params.get("exportRange").toString() : null;
        @SuppressWarnings("unchecked")
        List<String> exportFields = (List<String>) params.get("exportFields");
        if ("all".equals(exportRange)) {
            List<Customer> allCustomers = customerService.listAll();
            customerIds = allCustomers.stream().map(Customer::getId).collect(Collectors.toList());
        }
        byte[] data = customerImportExportService.exportCustomers(customerIds, exportRange, exportFields,
                loginUser.getUserId(), loginUser.getUser().getNickname());
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=customers_export.xlsx");
        return ResponseEntity.ok().headers(headers).body(data);
    }
}
