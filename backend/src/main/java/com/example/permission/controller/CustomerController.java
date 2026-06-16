package com.example.permission.controller;

import com.example.permission.common.PageResult;
import com.example.permission.common.Result;
import com.example.permission.entity.Customer;
import com.example.permission.entity.CustomerAddress;
import com.example.permission.entity.CustomerOperationLog;
import com.example.permission.security.LoginUser;
import com.example.permission.service.CustomerAddressService;
import com.example.permission.service.CustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/customer")
public class CustomerController {

    @Autowired
    private CustomerService customerService;

    @Autowired
    private CustomerAddressService customerAddressService;

    @GetMapping("/page")
    @PreAuthorize("hasAuthority('customer:list')")
    public Result<PageResult<Customer>> pageList(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) List<Integer> customerType,
            @RequestParam(required = false) List<Integer> customerSource,
            @RequestParam(required = false) List<Integer> status,
            @RequestParam(required = false) List<Integer> importance,
            @RequestParam(required = false) String createTimeStart,
            @RequestParam(required = false) String createTimeEnd,
            @RequestParam(required = false) List<Long> tagIds,
            @RequestParam(required = false, defaultValue = "OR") String tagLogic) {
        LoginUser loginUser = (LoginUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        List<String> roles = loginUser.getPermissions();
        PageResult<Customer> result = customerService.pageList(pageNum, pageSize, keyword,
                customerType, customerSource, status, importance,
                createTimeStart, createTimeEnd, tagIds, tagLogic,
                loginUser.getUserId(), roles);
        return Result.success(result);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('customer:query')")
    public Result<Customer> getById(@PathVariable Long id) {
        LoginUser loginUser = (LoginUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Customer customer = customerService.getById(id, loginUser.getPermissions());
        return Result.success(customer);
    }

    @PostMapping
    @PreAuthorize("hasAuthority('customer:add')")
    public Result<Customer> add(@RequestBody Customer customer) {
        LoginUser loginUser = (LoginUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Customer created = customerService.add(customer, loginUser.getUserId(), loginUser.getUser().getNickname());
        return Result.success(created);
    }

    @PutMapping
    @PreAuthorize("hasAuthority('customer:edit')")
    public Result<Void> update(@RequestBody Customer customer) {
        LoginUser loginUser = (LoginUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        customerService.update(customer, loginUser.getUserId(), loginUser.getUser().getNickname());
        return Result.success();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('customer:delete')")
    public Result<Void> delete(@PathVariable Long id) {
        LoginUser loginUser = (LoginUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        customerService.delete(id, loginUser.getUserId(), loginUser.getUser().getNickname());
        return Result.success();
    }

    @PutMapping("/{id}/freeze")
    @PreAuthorize("hasAuthority('customer:freeze')")
    public Result<Void> freeze(@PathVariable Long id, @RequestBody Map<String, String> params) {
        LoginUser loginUser = (LoginUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String reason = params.get("reason");
        customerService.freeze(id, reason, loginUser.getUserId(), loginUser.getUser().getNickname());
        return Result.success();
    }

    @PutMapping("/{id}/unfreeze")
    @PreAuthorize("hasAuthority('customer:unfreeze')")
    public Result<Void> unfreeze(@PathVariable Long id, @RequestBody Map<String, String> params) {
        LoginUser loginUser = (LoginUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String reason = params.get("reason");
        customerService.unfreeze(id, reason, loginUser.getUserId(), loginUser.getUser().getNickname());
        return Result.success();
    }

    @GetMapping("/{id}/logs")
    @PreAuthorize("hasAuthority('customer:query')")
    public Result<List<CustomerOperationLog>> getOperationLogs(@PathVariable Long id) {
        List<CustomerOperationLog> logs = customerService.getOperationLogs(id);
        return Result.success(logs);
    }

    @GetMapping("/logs/page")
    @PreAuthorize("hasAuthority('customer:log:query')")
    public Result<PageResult<CustomerOperationLog>> queryOperationLogs(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Long customerId,
            @RequestParam(required = false) Long operatorId,
            @RequestParam(required = false) Integer operationType,
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime) {
        PageResult<CustomerOperationLog> result = customerService.queryOperationLogs(
                pageNum, pageSize, customerId, operatorId, operationType, startTime, endTime);
        return Result.success(result);
    }

    @GetMapping("/{customerId}/addresses")
    @PreAuthorize("hasAuthority('customer:query')")
    public Result<List<CustomerAddress>> listAddresses(@PathVariable Long customerId) {
        List<CustomerAddress> addresses = customerAddressService.listByCustomerId(customerId);
        return Result.success(addresses);
    }

    @PostMapping("/address")
    @PreAuthorize("hasAuthority('customer:address:manage')")
    public Result<CustomerAddress> addAddress(@RequestBody CustomerAddress address) {
        CustomerAddress created = customerAddressService.add(address);
        return Result.success(created);
    }

    @PutMapping("/address")
    @PreAuthorize("hasAuthority('customer:address:manage')")
    public Result<CustomerAddress> updateAddress(@RequestBody CustomerAddress address) {
        CustomerAddress updated = customerAddressService.update(address);
        return Result.success(updated);
    }

    @DeleteMapping("/address/{id}")
    @PreAuthorize("hasAuthority('customer:address:manage')")
    public Result<Void> deleteAddress(@PathVariable Long id) {
        customerAddressService.delete(id);
        return Result.success();
    }

    @PutMapping("/address/{id}/default")
    @PreAuthorize("hasAuthority('customer:address:manage')")
    public Result<Void> setDefaultAddress(@PathVariable Long id) {
        customerAddressService.setDefault(id);
        return Result.success();
    }
}
