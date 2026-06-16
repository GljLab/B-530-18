package com.example.permission.controller;

import com.example.permission.common.PageResult;
import com.example.permission.common.Result;
import com.example.permission.entity.CustomerBlacklist;
import com.example.permission.security.LoginUser;
import com.example.permission.service.CustomerBlacklistService;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayOutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/customer/blacklist")
public class CustomerBlacklistController {

    @Autowired
    private CustomerBlacklistService customerBlacklistService;

    @GetMapping("/page")
    @PreAuthorize("hasAuthority('customer:blacklist:list')")
    public Result<PageResult<CustomerBlacklist>> pageList(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Integer reason,
            @RequestParam(required = false) Integer blacklistType,
            @RequestParam(required = false) Integer status) {
        PageResult<CustomerBlacklist> result = customerBlacklistService.pageList(pageNum, pageSize, reason, blacklistType, status);
        return Result.success(result);
    }

    @GetMapping("/pending")
    @PreAuthorize("hasAuthority('customer:blacklist:approve')")
    public Result<List<CustomerBlacklist>> listPending() {
        List<CustomerBlacklist> list = customerBlacklistService.listPending();
        return Result.success(list);
    }

    @GetMapping("/pending-remove")
    @PreAuthorize("hasAuthority('customer:blacklist:approve')")
    public Result<List<CustomerBlacklist>> listPendingRemove() {
        List<CustomerBlacklist> list = customerBlacklistService.listPendingRemove();
        return Result.success(list);
    }

    @GetMapping("/check/{customerId}")
    @PreAuthorize("hasAuthority('customer:list')")
    public Result<Map<String, Object>> check(@PathVariable Long customerId) {
        Map<String, Object> result = customerBlacklistService.checkBlacklist(customerId);
        return Result.success(result);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('customer:blacklist:list')")
    public Result<CustomerBlacklist> getById(@PathVariable Long id) {
        CustomerBlacklist bl = customerBlacklistService.getById(id);
        return Result.success(bl);
    }

    @PostMapping("/submit")
    @PreAuthorize("hasAuthority('customer:blacklist:submit')")
    public Result<CustomerBlacklist> submit(@RequestBody CustomerBlacklist blacklist) {
        LoginUser loginUser = (LoginUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        CustomerBlacklist created = customerBlacklistService.submit(blacklist, loginUser.getUserId(), loginUser.getUser().getNickname());
        return Result.success(created);
    }

    @PutMapping("/approve/{id}")
    @PreAuthorize("hasAuthority('customer:blacklist:approve')")
    public Result<Void> approve(@PathVariable Long id, @RequestBody Map<String, String> params) {
        LoginUser loginUser = (LoginUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String opinion = params.get("opinion");
        customerBlacklistService.approve(id, loginUser.getUserId(), loginUser.getUser().getNickname(), opinion);
        return Result.success();
    }

    @PutMapping("/reject/{id}")
    @PreAuthorize("hasAuthority('customer:blacklist:approve')")
    public Result<Void> reject(@PathVariable Long id, @RequestBody Map<String, String> params) {
        LoginUser loginUser = (LoginUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String reason = params.get("reason");
        customerBlacklistService.reject(id, loginUser.getUserId(), loginUser.getUser().getNickname(), reason);
        return Result.success();
    }

    @PutMapping("/remove/{id}")
    @PreAuthorize("hasAuthority('customer:blacklist:remove')")
    public Result<Void> submitRemove(@PathVariable Long id, @RequestBody Map<String, String> params) {
        LoginUser loginUser = (LoginUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String reason = params.get("reason");
        customerBlacklistService.submitRemove(id, loginUser.getUserId(), loginUser.getUser().getNickname(), reason);
        return Result.success();
    }

    @PutMapping("/remove-approve/{id}")
    @PreAuthorize("hasAuthority('customer:blacklist:approve')")
    public Result<Void> approveRemove(@PathVariable Long id, @RequestBody Map<String, String> params) {
        LoginUser loginUser = (LoginUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String opinion = params.get("opinion");
        customerBlacklistService.approveRemove(id, loginUser.getUserId(), loginUser.getUser().getNickname(), opinion);
        return Result.success();
    }

    @GetMapping("/export")
    @PreAuthorize("hasAuthority('customer:blacklist:export')")
    public ResponseEntity<byte[]> export(
            @RequestParam(required = false) Integer reason,
            @RequestParam(required = false) Integer blacklistType,
            @RequestParam(required = false) Integer status) {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("黑名单数据");
            CellStyle headerStyle = createHeaderStyle(workbook);

            int rowIdx = 0;
            Row headerRow = sheet.createRow(rowIdx++);
            String[] headers = {"姓名", "手机号", "证件号", "原因", "类型", "加入时间", "到期时间", "状态"};
            for (int i = 0; i < headers.length; i++) {
                Cell c = headerRow.createCell(i);
                c.setCellValue(headers[i]);
                c.setCellStyle(headerStyle);
            }

            List<CustomerBlacklist> list = customerBlacklistService.exportBlacklist(reason, blacklistType, status);
            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            Map<Integer, String> statusMap = Map.of(1, "待审批", 2, "已生效", 3, "已拒绝", 4, "已解除", 5, "待解除审批");
            Map<Integer, String> reasonMap = Map.of(1, "欠款", 2, "违规", 3, "投诉", 4, "其他");
            Map<Integer, String> typeMap = Map.of(1, "临时", 2, "永久");

            for (CustomerBlacklist bl : list) {
                Row dataRow = sheet.createRow(rowIdx++);
                dataRow.createCell(0).setCellValue(bl.getCustomerName() != null ? bl.getCustomerName() : "");
                dataRow.createCell(1).setCellValue(bl.getCustomerPhone() != null ? bl.getCustomerPhone() : "");
                dataRow.createCell(2).setCellValue(bl.getCustomerIdNumber() != null ? bl.getCustomerIdNumber() : "");
                dataRow.createCell(3).setCellValue(bl.getReason() != null ? reasonMap.getOrDefault(bl.getReason(), String.valueOf(bl.getReason())) : "");
                dataRow.createCell(4).setCellValue(bl.getBlacklistType() != null ? typeMap.getOrDefault(bl.getBlacklistType(), String.valueOf(bl.getBlacklistType())) : "");
                dataRow.createCell(5).setCellValue(bl.getApproveTime() != null ? bl.getApproveTime().format(fmt) : "");
                dataRow.createCell(6).setCellValue(bl.getExpireTime() != null ? bl.getExpireTime().format(fmt) : "");
                dataRow.createCell(7).setCellValue(bl.getStatus() != null ? statusMap.getOrDefault(bl.getStatus(), String.valueOf(bl.getStatus())) : "");
            }

            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
                int width = sheet.getColumnWidth(i);
                if (width < 3000) width = 3000;
                if (width > 15000) width = 15000;
                sheet.setColumnWidth(i, width);
            }

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);
            byte[] bytes = out.toByteArray();

            String fileName = URLEncoder.encode("黑名单数据_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".xlsx", StandardCharsets.UTF_8);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=" + fileName)
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(bytes);
        } catch (Exception e) {
            throw new RuntimeException("导出黑名单Excel失败：" + e.getMessage(), e);
        }
    }

    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setAlignment(HorizontalAlignment.CENTER);
        return style;
    }
}
