package com.example.permission.controller;

import com.example.permission.common.PageResult;
import com.example.permission.common.Result;
import com.example.permission.entity.Customer;
import com.example.permission.entity.Member;
import com.example.permission.entity.MemberLevelLog;
import com.example.permission.entity.MemberPointLog;
import com.example.permission.security.LoginUser;
import com.example.permission.service.MemberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/member")
public class MemberController {

    @Autowired
    private MemberService memberService;

    @GetMapping("/page")
    @PreAuthorize("hasAuthority('member:list')")
    public Result<PageResult<Member>> pageList(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) List<Long> levelIds,
            @RequestParam(required = false) List<Integer> status,
            @RequestParam(required = false) String registerTimeStart,
            @RequestParam(required = false) String registerTimeEnd,
            @RequestParam(required = false) BigDecimal pointsMin,
            @RequestParam(required = false) BigDecimal pointsMax,
            @RequestParam(required = false, defaultValue = "registerTime") String sortField,
            @RequestParam(required = false, defaultValue = "desc") String sortOrder) {
        PageResult<Member> result = memberService.pageList(pageNum, pageSize, keyword,
                levelIds, status, registerTimeStart, registerTimeEnd,
                pointsMin, pointsMax, sortField, sortOrder);
        return Result.success(result);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('member:query')")
    public Result<Member> getById(@PathVariable Long id) {
        Member member = memberService.getById(id);
        return Result.success(member);
    }

    @GetMapping("/customer/{customerId}")
    @PreAuthorize("hasAuthority('member:query')")
    public Result<Member> getByCustomerId(@PathVariable Long customerId) {
        Member member = memberService.getByCustomerId(customerId);
        return Result.success(member);
    }

    @GetMapping("/no/{memberNo}")
    @PreAuthorize("hasAuthority('member:query')")
    public Result<Member> getByMemberNo(@PathVariable String memberNo) {
        Member member = memberService.getByMemberNo(memberNo);
        return Result.success(member);
    }

    @PostMapping("/register/fromCustomer")
    @PreAuthorize("hasAuthority('member:add')")
    public Result<Member> registerFromCustomer(@RequestBody Map<String, Object> params) {
        LoginUser loginUser = (LoginUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Long customerId = Long.valueOf(params.get("customerId").toString());
        Integer registerSource = params.get("registerSource") != null ? Integer.valueOf(params.get("registerSource").toString()) : 1;
        Long referrerId = params.get("referrerId") != null ? Long.valueOf(params.get("referrerId").toString()) : null;

        Member member = memberService.registerFromCustomer(customerId, registerSource, referrerId,
                loginUser.getUserId(), loginUser.getUser().getNickname());
        return Result.success(member);
    }

    @PostMapping("/register/new")
    @PreAuthorize("hasAuthority('member:add')")
    public Result<Member> registerNewMember(@RequestBody Map<String, Object> params) {
        LoginUser loginUser = (LoginUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        Customer customer = new Customer();
        customer.setName((String) params.get("name"));
        customer.setGender(params.get("gender") != null ? Integer.valueOf(params.get("gender").toString()) : null);
        customer.setPhone((String) params.get("phone"));
        customer.setIdType(params.get("idType") != null ? Integer.valueOf(params.get("idType").toString()) : null);
        customer.setIdNumber((String) params.get("idNumber"));

        Integer registerSource = params.get("registerSource") != null ? Integer.valueOf(params.get("registerSource").toString()) : 1;
        Long referrerId = params.get("referrerId") != null ? Long.valueOf(params.get("referrerId").toString()) : null;

        Member member = memberService.registerNewMember(customer, registerSource, referrerId,
                loginUser.getUserId(), loginUser.getUser().getNickname());
        return Result.success(member);
    }

    @PutMapping
    @PreAuthorize("hasAuthority('member:edit')")
    public Result<Void> update(@RequestBody Member member) {
        memberService.updateMember(member);
        return Result.success();
    }

    @PostMapping("/points/add")
    @PreAuthorize("hasAuthority('member:point:add')")
    public Result<Void> addPoints(@RequestBody Map<String, Object> params) {
        LoginUser loginUser = (LoginUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Long memberId = Long.valueOf(params.get("memberId").toString());
        BigDecimal points = new BigDecimal(params.get("points").toString());
        Integer reasonType = params.get("reasonType") != null ? Integer.valueOf(params.get("reasonType").toString()) : 7;
        String reason = (String) params.get("reason");
        String detail = (String) params.get("detail");

        memberService.addPoints(memberId, points, reasonType, reason, detail,
                loginUser.getUserId(), loginUser.getUser().getNickname());
        return Result.success();
    }

    @PostMapping("/points/use")
    @PreAuthorize("hasAuthority('member:point:use')")
    public Result<Void> usePoints(@RequestBody Map<String, Object> params) {
        LoginUser loginUser = (LoginUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Long memberId = Long.valueOf(params.get("memberId").toString());
        BigDecimal points = new BigDecimal(params.get("points").toString());
        Integer reasonType = params.get("reasonType") != null ? Integer.valueOf(params.get("reasonType").toString()) : 6;
        String reason = (String) params.get("reason");
        String detail = (String) params.get("detail");

        memberService.usePoints(memberId, points, reasonType, reason, detail,
                loginUser.getUserId(), loginUser.getUser().getNickname());
        return Result.success();
    }

    @PostMapping("/level/adjust")
    @PreAuthorize("hasAuthority('member:level:adjust')")
    public Result<Void> adjustLevel(@RequestBody Map<String, Object> params) {
        LoginUser loginUser = (LoginUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Long memberId = Long.valueOf(params.get("memberId").toString());
        Long newLevelId = Long.valueOf(params.get("newLevelId").toString());
        String reason = (String) params.get("reason");

        memberService.adjustLevel(memberId, newLevelId, reason,
                loginUser.getUserId(), loginUser.getUser().getNickname());
        return Result.success();
    }

    @PutMapping("/{id}/freeze")
    @PreAuthorize("hasAuthority('member:freeze')")
    public Result<Void> freeze(@PathVariable Long id, @RequestBody Map<String, String> params) {
        LoginUser loginUser = (LoginUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String reason = params.get("reason");
        memberService.freezeMember(id, reason, loginUser.getUserId(), loginUser.getUser().getNickname());
        return Result.success();
    }

    @PutMapping("/{id}/unfreeze")
    @PreAuthorize("hasAuthority('member:unfreeze')")
    public Result<Void> unfreeze(@PathVariable Long id, @RequestBody Map<String, String> params) {
        LoginUser loginUser = (LoginUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String reason = params.get("reason");
        memberService.unfreezeMember(id, reason, loginUser.getUserId(), loginUser.getUser().getNickname());
        return Result.success();
    }

    @GetMapping("/{id}/pointLogs")
    @PreAuthorize("hasAuthority('member:query')")
    public Result<List<MemberPointLog>> getPointLogs(@PathVariable Long id) {
        List<MemberPointLog> logs = memberService.getPointLogs(id);
        return Result.success(logs);
    }

    @GetMapping("/{id}/pointLogs/page")
    @PreAuthorize("hasAuthority('member:query')")
    public Result<PageResult<MemberPointLog>> getPointLogPage(
            @PathVariable Long id,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        PageResult<MemberPointLog> result = memberService.getPointLogPage(pageNum, pageSize, id);
        return Result.success(result);
    }

    @GetMapping("/{id}/levelLogs")
    @PreAuthorize("hasAuthority('member:query')")
    public Result<List<MemberLevelLog>> getLevelLogs(@PathVariable Long id) {
        List<MemberLevelLog> logs = memberService.getLevelLogs(id);
        return Result.success(logs);
    }

    @GetMapping("/{id}/levelLogs/page")
    @PreAuthorize("hasAuthority('member:query')")
    public Result<PageResult<MemberLevelLog>> getLevelLogPage(
            @PathVariable Long id,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        PageResult<MemberLevelLog> result = memberService.getLevelLogPage(pageNum, pageSize, id);
        return Result.success(result);
    }

    @GetMapping("/statistics")
    @PreAuthorize("hasAuthority('member:statistics:list')")
    public Result<Map<String, Object>> getStatistics() {
        Map<String, Object> statistics = memberService.getStatistics();
        return Result.success(statistics);
    }

    @PostMapping("/points/batchAdd")
    @PreAuthorize("hasAuthority('member:point:add')")
    public Result<Void> batchAddPoints(@RequestBody Map<String, Object> params) {
        LoginUser loginUser = (LoginUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        List<Long> memberIds = (List<Long>) params.get("memberIds");
        BigDecimal points = new BigDecimal(params.get("points").toString());
        Integer reasonType = params.get("reasonType") != null ? Integer.valueOf(params.get("reasonType").toString()) : 2;
        String reason = (String) params.get("reason");

        memberService.batchAddPoints(memberIds, points, reasonType, reason,
                loginUser.getUserId(), loginUser.getUser().getNickname());
        return Result.success();
    }

    @PostMapping("/points/earnOnCheckout")
    @PreAuthorize("hasAuthority('member:point:add')")
    public Result<Map<String, Object>> earnPointsOnCheckout(@RequestBody Map<String, Object> params) {
        LoginUser loginUser = (LoginUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Long memberId = Long.valueOf(params.get("memberId").toString());
        BigDecimal consumeAmount = new BigDecimal(params.get("consumeAmount").toString());

        Map<String, Object> result = memberService.earnPointsOnCheckout(memberId, consumeAmount,
                loginUser.getUserId(), loginUser.getUser().getNickname());
        return Result.success(result);
    }

    @PostMapping("/points/useWithRule")
    @PreAuthorize("hasAuthority('member:point:use')")
    public Result<Map<String, Object>> usePointsWithRule(@RequestBody Map<String, Object> params) {
        LoginUser loginUser = (LoginUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Long memberId = Long.valueOf(params.get("memberId").toString());
        BigDecimal points = new BigDecimal(params.get("points").toString());
        BigDecimal orderAmount = params.get("orderAmount") != null ? new BigDecimal(params.get("orderAmount").toString()) : null;

        Map<String, Object> result = memberService.usePointsWithRule(memberId, points, orderAmount,
                loginUser.getUserId(), loginUser.getUser().getNickname());
        return Result.success(result);
    }

    @GetMapping("/{id}/pointSummary")
    @PreAuthorize("hasAuthority('member:query')")
    public Result<Map<String, Object>> getPointSummary(@PathVariable Long id) {
        Map<String, Object> summary = memberService.getMemberPointSummary(id);
        return Result.success(summary);
    }

    @GetMapping("/{id}/pointLogs/filtered")
    @PreAuthorize("hasAuthority('member:query')")
    public Result<PageResult<MemberPointLog>> getPointLogPageFiltered(
            @PathVariable Long id,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Integer pointType,
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime) {
        PageResult<MemberPointLog> result = memberService.getPointLogPageFiltered(pageNum, pageSize, id, pointType, startTime, endTime);
        return Result.success(result);
    }
}
