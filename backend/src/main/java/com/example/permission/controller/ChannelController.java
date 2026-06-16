package com.example.permission.controller;

import com.example.permission.common.PageResult;
import com.example.permission.common.Result;
import com.example.permission.entity.Channel;
import com.example.permission.entity.ChannelInventory;
import com.example.permission.entity.ChannelPrice;
import com.example.permission.service.ChannelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/channel")
public class ChannelController {

    @Autowired
    private ChannelService channelService;

    @GetMapping("/page")
    @PreAuthorize("hasAuthority('channel:query')")
    public Result<PageResult<Channel>> getChannelPage(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String channelName,
            @RequestParam(required = false) Integer channelType,
            @RequestParam(required = false) Integer cooperationStatus) {
        PageResult<Channel> result = channelService.getChannelPage(pageNum, pageSize, channelName, channelType, cooperationStatus);
        return Result.success(result);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('channel:query')")
    public Result<Channel> getChannelById(@PathVariable Long id) {
        Channel result = channelService.getChannelById(id);
        return Result.success(result);
    }

    @GetMapping("/list")
    @PreAuthorize("hasAuthority('channel:query')")
    public Result<List<Channel>> getAllChannels() {
        List<Channel> result = channelService.getAllChannels();
        return Result.success(result);
    }

    @PostMapping
    @PreAuthorize("hasAuthority('channel:add')")
    public Result<Void> createChannel(@RequestBody Channel channel) {
        channelService.createChannel(channel);
        return Result.success();
    }

    @PutMapping
    @PreAuthorize("hasAuthority('channel:edit')")
    public Result<Void> updateChannel(@RequestBody Channel channel) {
        channelService.updateChannel(channel);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('channel:delete')")
    public Result<Void> deleteChannel(@PathVariable Long id) {
        channelService.deleteChannel(id);
        return Result.success();
    }

    @GetMapping("/inventory")
    @PreAuthorize("hasAuthority('channel:inventory:query')")
    public Result<List<ChannelInventory>> getChannelInventory(
            @RequestParam Long channelId,
            @RequestParam Long roomTypeId,
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate) {
        List<ChannelInventory> result = channelService.getChannelInventory(channelId, roomTypeId, startDate, endDate);
        return Result.success(result);
    }

    @PostMapping("/inventory/set")
    @PreAuthorize("hasAuthority('channel:inventory:edit')")
    public Result<Void> setChannelInventory(@RequestBody Map<String, Object> params) {
        Long channelId = params.get("channelId") != null ? Long.valueOf(params.get("channelId").toString()) : null;
        Long roomTypeId = params.get("roomTypeId") != null ? Long.valueOf(params.get("roomTypeId").toString()) : null;
        LocalDate startDate = params.get("startDate") != null ? LocalDate.parse(params.get("startDate").toString()) : null;
        LocalDate endDate = params.get("endDate") != null ? LocalDate.parse(params.get("endDate").toString()) : null;
        Integer allocatedRooms = params.get("allocatedRooms") != null ? Integer.valueOf(params.get("allocatedRooms").toString()) : null;
        Integer shareMode = params.get("shareMode") != null ? Integer.valueOf(params.get("shareMode").toString()) : null;
        channelService.setChannelInventory(channelId, roomTypeId, startDate, endDate, allocatedRooms, shareMode);
        return Result.success();
    }

    @PostMapping("/inventory/batch")
    @PreAuthorize("hasAuthority('channel:inventory:edit')")
    public Result<Void> batchSetChannelInventory(@RequestBody Map<String, Object> params) {
        Long channelId = params.get("channelId") != null ? Long.valueOf(params.get("channelId").toString()) : null;
        Long roomTypeId = params.get("roomTypeId") != null ? Long.valueOf(params.get("roomTypeId").toString()) : null;
        LocalDate startDate = params.get("startDate") != null ? LocalDate.parse(params.get("startDate").toString()) : null;
        LocalDate endDate = params.get("endDate") != null ? LocalDate.parse(params.get("endDate").toString()) : null;
        Integer allocatedRooms = params.get("allocatedRooms") != null ? Integer.valueOf(params.get("allocatedRooms").toString()) : null;
        Integer shareMode = params.get("shareMode") != null ? Integer.valueOf(params.get("shareMode").toString()) : null;
        channelService.batchSetChannelInventory(channelId, roomTypeId, startDate, endDate, allocatedRooms, shareMode);
        return Result.success();
    }

    @GetMapping("/price")
    @PreAuthorize("hasAuthority('channel:price:query')")
    public Result<List<ChannelPrice>> getChannelPrice(
            @RequestParam Long channelId,
            @RequestParam Long roomTypeId,
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate) {
        List<ChannelPrice> result = channelService.getChannelPrice(channelId, roomTypeId, startDate, endDate);
        return Result.success(result);
    }

    @PostMapping("/price/set")
    @PreAuthorize("hasAuthority('channel:price:edit')")
    public Result<Void> setChannelPrice(@RequestBody Map<String, Object> params) {
        Long channelId = params.get("channelId") != null ? Long.valueOf(params.get("channelId").toString()) : null;
        Long roomTypeId = params.get("roomTypeId") != null ? Long.valueOf(params.get("roomTypeId").toString()) : null;
        LocalDate startDate = params.get("startDate") != null ? LocalDate.parse(params.get("startDate").toString()) : null;
        LocalDate endDate = params.get("endDate") != null ? LocalDate.parse(params.get("endDate").toString()) : null;
        Integer priceMode = params.get("priceMode") != null ? Integer.valueOf(params.get("priceMode").toString()) : null;
        BigDecimal priceValue = params.get("priceValue") != null ? new BigDecimal(params.get("priceValue").toString()) : null;
        channelService.setChannelPrice(channelId, roomTypeId, startDate, endDate, priceMode, priceValue);
        return Result.success();
    }

    @GetMapping("/price/calculate")
    @PreAuthorize("hasAuthority('channel:price:query')")
    public Result<BigDecimal> calculateChannelPrice(
            @RequestParam Long channelId,
            @RequestParam Long roomTypeId,
            @RequestParam LocalDate date) {
        BigDecimal result = channelService.calculateChannelPrice(channelId, roomTypeId, date);
        return Result.success(result);
    }

    @GetMapping("/statistics")
    @PreAuthorize("hasAuthority('channel:statistics:query')")
    public Result<Map> getChannelStatistics(
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate) {
        Map result = channelService.getChannelStatistics(startDate, endDate);
        return Result.success(result);
    }

    @GetMapping("/statistics/trend")
    @PreAuthorize("hasAuthority('channel:statistics:query')")
    public Result<List<Map>> getChannelTrend(
            @RequestParam Long channelId,
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate) {
        List<Map> result = channelService.getChannelTrend(channelId, startDate, endDate);
        return Result.success(result);
    }

    @GetMapping("/statistics/efficiency")
    @PreAuthorize("hasAuthority('channel:statistics:query')")
    public Result<List<Map>> getChannelEfficiency(
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate) {
        List<Map> result = channelService.getChannelEfficiency(startDate, endDate);
        return Result.success(result);
    }
}
