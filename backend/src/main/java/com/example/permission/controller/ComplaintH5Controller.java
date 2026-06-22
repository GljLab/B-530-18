package com.example.permission.controller;

import com.example.permission.common.Result;
import com.example.permission.service.ComplaintH5Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/h5/complaint")
public class ComplaintH5Controller {

    @Autowired
    private ComplaintH5Service complaintH5Service;

    @GetMapping("/data")
    public Result<Map<String, Object>> getComplaintPageData(
            @RequestParam String order,
            @RequestParam String code) {
        Map<String, Object> data = complaintH5Service.getComplaintPageData(order, code);
        return Result.success(data);
    }

    @PostMapping("/submit")
    public Result<Map<String, Long>> submitComplaint(@RequestBody Map<String, Object> data) {
        Long id = complaintH5Service.submitComplaint(data);
        Map<String, Long> result = Map.of("id", id);
        return Result.success(result);
    }

    @GetMapping("/success")
    public Result<Map<String, Object>> getComplaintSuccessData(@RequestParam Long id) {
        Map<String, Object> data = complaintH5Service.getComplaintSuccessData(id);
        return Result.success(data);
    }
}
