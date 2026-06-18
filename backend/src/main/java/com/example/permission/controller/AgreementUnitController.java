package com.example.permission.controller;

import com.example.permission.common.PageResult;
import com.example.permission.common.Result;
import com.example.permission.entity.AgreementUnit;
import com.example.permission.service.AgreementUnitService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/finance/agreement-unit")
public class AgreementUnitController {

    @Autowired
    private AgreementUnitService agreementUnitService;

    @GetMapping("/page")
    @PreAuthorize("hasAuthority('finance:agreement:query')")
    public Result<PageResult<AgreementUnit>> getPage(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String unitName,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Integer cooperationType) {
        PageResult<AgreementUnit> result = agreementUnitService.getPage(pageNum, pageSize, unitName, status, cooperationType);
        return Result.success(result);
    }

    @GetMapping("/list")
    @PreAuthorize("hasAuthority('finance:agreement:query')")
    public Result<List<AgreementUnit>> listAll() {
        List<AgreementUnit> result = agreementUnitService.listAll();
        return Result.success(result);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('finance:agreement:query')")
    public Result<AgreementUnit> getById(@PathVariable Long id) {
        AgreementUnit result = agreementUnitService.getById(id);
        return Result.success(result);
    }

    @PostMapping
    @PreAuthorize("hasAuthority('finance:agreement:add')")
    public Result<Void> add(@RequestBody AgreementUnit agreementUnit) {
        agreementUnitService.add(agreementUnit);
        return Result.success();
    }

    @PutMapping
    @PreAuthorize("hasAuthority('finance:agreement:edit')")
    public Result<Void> update(@RequestBody AgreementUnit agreementUnit) {
        agreementUnitService.update(agreementUnit);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('finance:agreement:delete')")
    public Result<Void> delete(@PathVariable Long id) {
        agreementUnitService.delete(id);
        return Result.success();
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasAuthority('finance:agreement:edit')")
    public Result<Void> updateStatus(@PathVariable Long id, @RequestBody Map<String, Integer> params) {
        Integer status = params.get("status");
        agreementUnitService.updateStatus(id, status);
        return Result.success();
    }
}
