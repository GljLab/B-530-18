package com.example.permission.controller;

import com.example.permission.common.Result;
import com.example.permission.entity.OverbookingGlobalConfig;
import com.example.permission.entity.OverbookingStrategy;
import com.example.permission.service.OverbookingStrategyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/overbooking")
public class OverbookingStrategyController {

    @Autowired
    private OverbookingStrategyService overbookingStrategyService;

    @GetMapping("/global")
    @PreAuthorize("hasAuthority('inventory:overbooking:query')")
    public Result<OverbookingGlobalConfig> getGlobalConfig() {
        OverbookingGlobalConfig result = overbookingStrategyService.getGlobalConfig();
        return Result.success(result);
    }

    @PutMapping("/global")
    @PreAuthorize("hasAuthority('inventory:overbooking:edit')")
    public Result<Void> updateGlobalConfig(@RequestBody Map<String, Object> params) {
        Integer enabled = params.get("enabled") != null ? Integer.valueOf(params.get("enabled").toString()) : null;
        overbookingStrategyService.updateGlobalConfig(enabled);
        return Result.success();
    }

    @GetMapping("/list")
    @PreAuthorize("hasAuthority('inventory:overbooking:query')")
    public Result<List<Map>> getAllStrategies() {
        List<Map> result = overbookingStrategyService.getAllStrategies();
        return Result.success(result);
    }

    @GetMapping("/{roomTypeId}")
    @PreAuthorize("hasAuthority('inventory:overbooking:query')")
    public Result<OverbookingStrategy> getStrategyByRoomType(@PathVariable Long roomTypeId) {
        OverbookingStrategy result = overbookingStrategyService.getStrategyByRoomType(roomTypeId);
        return Result.success(result);
    }

    @PostMapping("/save")
    @PreAuthorize("hasAuthority('inventory:overbooking:edit')")
    public Result<Void> saveStrategy(@RequestBody OverbookingStrategy strategy) {
        overbookingStrategyService.saveStrategy(strategy);
        return Result.success();
    }

    @GetMapping("/maxSalable")
    @PreAuthorize("hasAuthority('inventory:overbooking:query')")
    public Result<Integer> getMaxSalableRooms(
            @RequestParam Long roomTypeId,
            @RequestParam LocalDate date) {
        Integer result = overbookingStrategyService.getMaxSalableRooms(roomTypeId, date);
        return Result.success(result);
    }

    @GetMapping("/alert")
    @PreAuthorize("hasAuthority('inventory:overbooking:query')")
    public Result<Map> checkOverbookingAlert(
            @RequestParam Long roomTypeId,
            @RequestParam LocalDate date,
            @RequestParam Integer bookedCount) {
        Map result = overbookingStrategyService.checkOverbookingAlert(roomTypeId, date, bookedCount);
        return Result.success(result);
    }
}
