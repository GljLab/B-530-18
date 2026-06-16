package com.example.permission.controller;

import com.example.permission.common.Result;
import com.example.permission.entity.RoomInventoryPool;
import com.example.permission.service.InventoryPoolService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/inventory")
public class InventoryPoolController {

    @Autowired
    private InventoryPoolService inventoryPoolService;

    @GetMapping("/pool/list")
    @PreAuthorize("hasAuthority('inventory:pool:query')")
    public Result<List<RoomInventoryPool>> getInventoryByRoomTypeAndDateRange(
            @RequestParam Long roomTypeId,
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate) {
        List<RoomInventoryPool> result = inventoryPoolService.getInventoryByRoomTypeAndDateRange(roomTypeId, startDate, endDate);
        return Result.success(result);
    }

    @GetMapping("/pool/all")
    @PreAuthorize("hasAuthority('inventory:pool:query')")
    public Result<List<Map>> getInventoryByDateRange(
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate) {
        List<Map> result = inventoryPoolService.getInventoryByDateRange(startDate, endDate);
        return Result.success(result);
    }

    @PostMapping("/pool/set")
    @PreAuthorize("hasAuthority('inventory:pool:edit')")
    public Result<Void> setInventory(@RequestBody Map<String, Object> params) {
        Long roomTypeId = params.get("roomTypeId") != null ? Long.valueOf(params.get("roomTypeId").toString()) : null;
        LocalDate date = params.get("date") != null ? LocalDate.parse(params.get("date").toString()) : null;
        Integer totalRooms = params.get("totalRooms") != null ? Integer.valueOf(params.get("totalRooms").toString()) : null;
        Integer availableRooms = params.get("availableRooms") != null ? Integer.valueOf(params.get("availableRooms").toString()) : null;
        Integer reservedRooms = params.get("reservedRooms") != null ? Integer.valueOf(params.get("reservedRooms").toString()) : null;
        inventoryPoolService.setInventory(roomTypeId, date, totalRooms, availableRooms, reservedRooms);
        return Result.success();
    }

    @PostMapping("/pool/batch")
    @PreAuthorize("hasAuthority('inventory:pool:batch')")
    public Result<Void> batchSetInventory(@RequestBody Map<String, Object> params) {
        Long roomTypeId = params.get("roomTypeId") != null ? Long.valueOf(params.get("roomTypeId").toString()) : null;
        LocalDate startDate = params.get("startDate") != null ? LocalDate.parse(params.get("startDate").toString()) : null;
        LocalDate endDate = params.get("endDate") != null ? LocalDate.parse(params.get("endDate").toString()) : null;
        Integer totalRooms = params.get("totalRooms") != null ? Integer.valueOf(params.get("totalRooms").toString()) : null;
        Integer availableRooms = params.get("availableRooms") != null ? Integer.valueOf(params.get("availableRooms").toString()) : null;
        Integer reservedRooms = params.get("reservedRooms") != null ? Integer.valueOf(params.get("reservedRooms").toString()) : null;
        inventoryPoolService.batchSetInventory(roomTypeId, startDate, endDate, totalRooms, availableRooms, reservedRooms);
        return Result.success();
    }

    @GetMapping("/monitor")
    @PreAuthorize("hasAuthority('inventory:monitor:query')")
    public Result<List<Map>> getMonitorData(
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate) {
        List<Map> result = inventoryPoolService.getMonitorData(startDate, endDate);
        return Result.success(result);
    }

    @PostMapping("/monitor/export")
    @PreAuthorize("hasAuthority('inventory:monitor:export')")
    public ResponseEntity<byte[]> exportMonitorData(@RequestBody Map<String, Object> params) throws Exception {
        LocalDate startDate = params.get("startDate") != null ? LocalDate.parse(params.get("startDate").toString()) : null;
        LocalDate endDate = params.get("endDate") != null ? LocalDate.parse(params.get("endDate").toString()) : null;
        List<Map> data = inventoryPoolService.exportMonitorData(startDate, endDate);

        StringBuilder sb = new StringBuilder();
        sb.append("日期,房型ID,房型名称,总房数,可用房数,已预订数,剩余数,预订率,最大可售数,超售状态\n");
        for (Map item : data) {
            sb.append(item.get("date")).append(",")
                    .append(item.get("roomTypeId")).append(",")
                    .append(item.get("typeName")).append(",")
                    .append(item.get("totalRooms")).append(",")
                    .append(item.get("availableRooms")).append(",")
                    .append(item.get("bookedCount")).append(",")
                    .append(item.get("remaining")).append(",")
                    .append(item.get("bookingRate")).append(",")
                    .append(item.get("maxSalable")).append(",")
                    .append(item.get("overbookingStatus")).append("\n");
        }

        byte[] csvData = sb.toString().getBytes(StandardCharsets.UTF_8);
        String fileName = "库存监控_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".csv";
        String encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8.name()).replaceAll("\\+", "%20");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("text/csv"));
        headers.setContentDispositionFormData("attachment", encodedFileName);
        headers.add("Content-Disposition", "attachment; filename*=UTF-8''" + encodedFileName);

        return ResponseEntity.ok()
                .headers(headers)
                .body(csvData);
    }
}
