package com.example.permission.controller;

import com.example.permission.common.PageResult;
import com.example.permission.common.Result;
import com.example.permission.entity.MemberBenefitLog;
import com.example.permission.entity.Room;
import com.example.permission.security.LoginUser;
import com.example.permission.service.MemberBenefitService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/member/benefit")
public class MemberBenefitController {

    @Autowired
    private MemberBenefitService memberBenefitService;

    private LoginUser getLoginUser() {
        return (LoginUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    @GetMapping("/info/{customerId}")
    @PreAuthorize("hasAuthority('member:benefit:query')")
    public Result<Map<String, Object>> getMemberBenefits(@PathVariable Long customerId) {
        Map<String, Object> benefits = memberBenefitService.getMemberBenefits(customerId);
        return Result.success(benefits);
    }

    @GetMapping("/calculate-booking")
    @PreAuthorize("hasAuthority('member:benefit:query')")
    public Result<Map<String, Object>> calculateBookingBenefits(
            @RequestParam Long memberId,
            @RequestParam BigDecimal roomTotal,
            @RequestParam(required = false) Long roomTypeId) {
        Map<String, Object> result = memberBenefitService.calculateBookingBenefits(memberId, roomTotal, roomTypeId);
        return Result.success(result);
    }

    @PostMapping("/apply-booking-discount/{bookingId}")
    @PreAuthorize("hasAuthority('member:benefit:apply')")
    public Result<Map<String, Object>> applyBookingDiscount(@PathVariable Long bookingId) {
        Map<String, Object> result = memberBenefitService.applyBookingDiscount(bookingId, getLoginUser());
        return Result.success(result);
    }

    @GetMapping("/calculate-deposit")
    @PreAuthorize("hasAuthority('member:benefit:query')")
    public Result<Map<String, Object>> calculateDepositReduction(
            @RequestParam Long memberId,
            @RequestParam BigDecimal standardDeposit) {
        Map<String, Object> result = memberBenefitService.calculateDepositReduction(memberId, standardDeposit);
        return Result.success(result);
    }

    @PostMapping("/apply-deposit-reduction/{checkInId}")
    @PreAuthorize("hasAuthority('member:benefit:apply')")
    public Result<Map<String, Object>> applyDepositReduction(
            @PathVariable Long checkInId,
            @RequestParam BigDecimal standardDeposit) {
        Map<String, Object> result = memberBenefitService.applyDepositReduction(checkInId, standardDeposit, getLoginUser());
        return Result.success(result);
    }

    @GetMapping("/upgrade-room-types")
    @PreAuthorize("hasAuthority('member:benefit:query')")
    public Result<Map<String, Object>> getUpgradeEligibleRoomTypes(
            @RequestParam Long memberId,
            @RequestParam Long currentRoomTypeId,
            @RequestParam LocalDate checkInDate,
            @RequestParam LocalDate checkOutDate) {
        Map<String, Object> result = memberBenefitService.getUpgradeEligibleRoomTypes(
                memberId, currentRoomTypeId, checkInDate, checkOutDate);
        return Result.success(result);
    }

    @PostMapping("/apply-room-upgrade/{checkInId}")
    @PreAuthorize("hasAuthority('member:benefit:apply')")
    public Result<Map<String, Object>> applyRoomUpgrade(
            @PathVariable Long checkInId,
            @RequestParam Long newRoomTypeId) {
        Map<String, Object> result = memberBenefitService.applyRoomUpgrade(checkInId, newRoomTypeId, getLoginUser());
        return Result.success(result);
    }

    @GetMapping("/late-checkout-eligibility/{memberId}")
    @PreAuthorize("hasAuthority('member:benefit:query')")
    public Result<Map<String, Object>> getLateCheckoutEligibility(@PathVariable Long memberId) {
        Map<String, Object> result = memberBenefitService.getLateCheckoutEligibility(memberId);
        return Result.success(result);
    }

    @PostMapping("/apply-late-checkout/{checkInId}")
    @PreAuthorize("hasAuthority('member:benefit:apply')")
    public Result<Map<String, Object>> applyLateCheckout(@PathVariable Long checkInId) {
        Map<String, Object> result = memberBenefitService.applyLateCheckout(checkInId, getLoginUser());
        return Result.success(result);
    }

    @PostMapping("/award-points/{checkInId}")
    @PreAuthorize("hasAuthority('member:benefit:apply')")
    public Result<Map<String, Object>> awardPointsForCheckIn(@PathVariable Long checkInId) {
        Map<String, Object> result = memberBenefitService.awardPointsForCheckIn(checkInId, getLoginUser());
        return Result.success(result);
    }

    @PostMapping("/preferred-rooms")
    @PreAuthorize("hasAuthority('member:benefit:query')")
    public Result<List<Room>> getPreferredRooms(@RequestBody List<Room> rooms) {
        List<Room> result = memberBenefitService.getPreferredRooms(rooms);
        return Result.success(result);
    }

    @GetMapping("/log/page")
    @PreAuthorize("hasAuthority('member:benefit:log:list')")
    public Result<PageResult<MemberBenefitLog>> getBenefitLogPage(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Long memberId,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer benefitType,
            @RequestParam(required = false) Integer relatedOrderType,
            @RequestParam(required = false) String relatedOrderNo,
            @RequestParam(required = false) String createTimeStart,
            @RequestParam(required = false) String createTimeEnd,
            @RequestParam(required = false) String sortField,
            @RequestParam(required = false) String sortOrder) {
        String memberNo = keyword;
        String memberName = keyword;
        PageResult<MemberBenefitLog> result = memberBenefitService.getBenefitLogPage(
                pageNum, pageSize, memberId, memberNo, memberName, benefitType,
                relatedOrderType, relatedOrderNo, createTimeStart, createTimeEnd,
                sortField, sortOrder);
        return Result.success(result);
    }

    @GetMapping("/log/list")
    @PreAuthorize("hasAuthority('member:benefit:log:list')")
    public Result<List<MemberBenefitLog>> getBenefitLogList(
            @RequestParam(required = false) Long memberId,
            @RequestParam(required = false) String createTimeStart,
            @RequestParam(required = false) String createTimeEnd) {
        List<MemberBenefitLog> result = memberBenefitService.getBenefitLogList(
                memberId, createTimeStart, createTimeEnd);
        return Result.success(result);
    }
}
