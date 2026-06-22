package com.example.permission.controller;

import com.example.permission.common.PageResult;
import com.example.permission.common.Result;
import com.example.permission.entity.Complaint;
import com.example.permission.entity.ComplaintVisit;
import com.example.permission.entity.SysUser;
import com.example.permission.service.ComplaintService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/complaint")
public class ComplaintController {

    @Autowired
    private ComplaintService complaintService;

    @GetMapping("/page")
    @PreAuthorize("hasAuthority('complaint:list')")
    public Result<PageResult<Complaint>> getComplaintPage(
            @RequestParam(required = false) Integer complaintStatus,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer complaintType,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate,
            @RequestParam(required = false) Boolean myTask,
            @RequestParam(defaultValue = "1") Long pageNum,
            @RequestParam(defaultValue = "10") Long pageSize) {
        PageResult<Complaint> page = complaintService.getComplaintPage(
                complaintStatus, keyword, complaintType, startDate, endDate, myTask, pageNum, pageSize);
        return Result.success(page);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('complaint:query')")
    public Result<Complaint> getComplaintDetail(@PathVariable Long id) {
        Complaint complaint = complaintService.getComplaintDetail(id);
        return Result.success(complaint);
    }

    @GetMapping("/staff/list")
    @PreAuthorize("hasAuthority('complaint:accept')")
    public Result<List<SysUser>> getAvailableStaff() {
        List<SysUser> list = complaintService.getAvailableStaff();
        return Result.success(list);
    }

    @PutMapping("/accept/{id}")
    @PreAuthorize("hasAuthority('complaint:accept')")
    public Result<Void> acceptComplaint(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        Long assignUserId = body.get("assignUserId") != null ? Long.valueOf(body.get("assignUserId").toString()) : null;
        String acceptRemark = body.get("acceptRemark") != null ? body.get("acceptRemark").toString() : null;
        complaintService.acceptComplaint(id, assignUserId, acceptRemark);
        return Result.success();
    }

    @PutMapping("/reject/{id}")
    @PreAuthorize("hasAuthority('complaint:reject')")
    public Result<Void> rejectComplaint(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        Integer rejectReason = body.get("rejectReason") != null ? Integer.valueOf(body.get("rejectReason").toString()) : null;
        String rejectRemark = body.get("rejectRemark") != null ? body.get("rejectRemark").toString() : null;
        complaintService.rejectComplaint(id, rejectReason, rejectRemark);
        return Result.success();
    }

    @PutMapping("/handle/{id}")
    @PreAuthorize("hasAuthority('complaint:handle')")
    public Result<Void> handleComplaint(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        String handleSolution = body.get("handleSolution") != null ? body.get("handleSolution").toString() : null;
        Integer handleResult = body.get("handleResult") != null ? Integer.valueOf(body.get("handleResult").toString()) : null;
        String compensationPlan = body.get("compensationPlan") != null ? body.get("compensationPlan").toString() : null;
        String handleRemark = body.get("handleRemark") != null ? body.get("handleRemark").toString() : null;
        complaintService.handleComplaint(id, handleSolution, handleResult, compensationPlan, handleRemark);
        return Result.success();
    }

    @PostMapping("/visit/{id}")
    @PreAuthorize("hasAuthority('complaint:visit')")
    public Result<Void> addVisit(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        LocalDateTime visitTime = body.get("visitTime") != null ? LocalDateTime.parse(body.get("visitTime").toString()) : null;
        Integer visitMethod = body.get("visitMethod") != null ? Integer.valueOf(body.get("visitMethod").toString()) : null;
        Integer satisfaction = body.get("satisfaction") != null ? Integer.valueOf(body.get("satisfaction").toString()) : null;
        String visitRemark = body.get("visitRemark") != null ? body.get("visitRemark").toString() : null;
        Integer needReprocess = body.get("needReprocess") != null ? Integer.valueOf(body.get("needReprocess").toString()) : null;
        complaintService.addVisit(id, visitTime, visitMethod, satisfaction, visitRemark, needReprocess);
        return Result.success();
    }

    @GetMapping("/{id}/visits")
    @PreAuthorize("hasAuthority('complaint:query')")
    public Result<List<ComplaintVisit>> getVisits(@PathVariable Long id) {
        List<ComplaintVisit> visits = complaintService.getVisitsByComplaintId(id);
        return Result.success(visits);
    }
}
