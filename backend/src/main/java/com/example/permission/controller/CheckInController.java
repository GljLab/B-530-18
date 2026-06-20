package com.example.permission.controller;

import com.example.permission.common.PageResult;
import com.example.permission.common.Result;
import com.example.permission.entity.*;
import com.example.permission.service.CheckInService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/checkin")
public class CheckInController {

    @Autowired
    private CheckInService checkInService;

    private List<CheckInGuest> convertGuests(Object guestsObj) {
        List<CheckInGuest> guests = new ArrayList<>();
        if (guestsObj == null) {
            return guests;
        }
        if (guestsObj instanceof List) {
            List<?> list = (List<?>) guestsObj;
            for (Object item : list) {
                if (item instanceof Map) {
                    Map<String, Object> map = (Map<String, Object>) item;
                    CheckInGuest guest = new CheckInGuest();
                    if (map.get("isMain") != null) {
                        guest.setIsMain(Integer.valueOf(map.get("isMain").toString()));
                    }
                    guest.setName(map.get("name") != null ? map.get("name").toString() : null);
                    guest.setPhone(map.get("phone") != null ? map.get("phone").toString() : null);
                    if (map.get("gender") != null) {
                        guest.setGender(Integer.valueOf(map.get("gender").toString()));
                    }
                    if (map.get("idType") != null) {
                        guest.setIdType(Integer.valueOf(map.get("idType").toString()));
                    }
                    guest.setIdNumber(map.get("idNumber") != null ? map.get("idNumber").toString() : null);
                    guests.add(guest);
                }
            }
        }
        return guests;
    }

    @PostMapping("/fromBooking")
    public Result<CheckIn> checkInFromBooking(@RequestBody Map<String, Object> params) {
        Long bookingId = Long.valueOf(params.get("bookingId").toString());
        Long roomId = Long.valueOf(params.get("roomId").toString());
        List<CheckInGuest> guests = convertGuests(params.get("guests"));
        BigDecimal depositAmount = params.get("depositAmount") != null ? new BigDecimal(params.get("depositAmount").toString()) : BigDecimal.ZERO;
        Integer depositMethod = params.get("depositMethod") != null ? Integer.valueOf(params.get("depositMethod").toString()) : 1;
        String depositVoucherNo = params.get("depositVoucherNo") != null ? params.get("depositVoucherNo").toString() : null;
        Integer keyCardCount = params.get("keyCardCount") != null ? Integer.valueOf(params.get("keyCardCount").toString()) : 1;
        String specialRequirements = params.get("specialRequirements") != null ? params.get("specialRequirements").toString() : null;
        String remark = params.get("remark") != null ? params.get("remark").toString() : null;

        CheckIn checkIn = checkInService.checkInFromBooking(bookingId, roomId, guests,
                depositAmount, depositMethod, depositVoucherNo, keyCardCount, specialRequirements, remark);
        return Result.success(checkIn);
    }

    @PostMapping("/walkin")
    public Result<CheckIn> walkInCheckIn(@RequestBody Map<String, Object> params) {
        Long roomTypeId = Long.valueOf(params.get("roomTypeId").toString());
        Long roomId = Long.valueOf(params.get("roomId").toString());
        Map<String, Object> customerMap = (Map<String, Object>) params.get("customer");
        List<CheckInGuest> guests = convertGuests(params.get("guests"));
        LocalDate checkInDate = params.get("checkInDate") != null ? LocalDate.parse(params.get("checkInDate").toString()) : null;
        LocalDate checkOutDate = params.get("checkOutDate") != null ? LocalDate.parse(params.get("checkOutDate").toString()) : null;
        BigDecimal depositAmount = params.get("depositAmount") != null ? new BigDecimal(params.get("depositAmount").toString()) : BigDecimal.ZERO;
        Integer depositMethod = params.get("depositMethod") != null ? Integer.valueOf(params.get("depositMethod").toString()) : 1;
        String depositVoucherNo = params.get("depositVoucherNo") != null ? params.get("depositVoucherNo").toString() : null;
        Integer keyCardCount = params.get("keyCardCount") != null ? Integer.valueOf(params.get("keyCardCount").toString()) : 1;
        String specialRequirements = params.get("specialRequirements") != null ? params.get("specialRequirements").toString() : null;
        String remark = params.get("remark") != null ? params.get("remark").toString() : null;
        Integer bookingSource = params.get("bookingSource") != null ? Integer.valueOf(params.get("bookingSource").toString()) : 1;
        Long agreementUnitId = params.get("agreementUnitId") != null ? Long.valueOf(params.get("agreementUnitId").toString()) : null;
        Integer guaranteeType = params.get("guaranteeType") != null ? Integer.valueOf(params.get("guaranteeType").toString()) : null;
        Long memberId = params.get("memberId") != null ? Long.valueOf(params.get("memberId").toString()) : null;

        Customer customer = new Customer();
        if (customerMap != null) {
            if (customerMap.get("id") != null) {
                customer.setId(Long.valueOf(customerMap.get("id").toString()));
            }
            customer.setName(customerMap.get("name") != null ? customerMap.get("name").toString() : null);
            customer.setPhone(customerMap.get("phone") != null ? customerMap.get("phone").toString() : null);
            if (customerMap.get("gender") != null) {
                customer.setGender(Integer.valueOf(customerMap.get("gender").toString()));
            }
            if (customerMap.get("idType") != null) {
                customer.setIdType(Integer.valueOf(customerMap.get("idType").toString()));
            }
            customer.setIdNumber(customerMap.get("idNumber") != null ? customerMap.get("idNumber").toString() : null);
        } else {
            if (params.get("customerId") != null) {
                customer.setId(Long.valueOf(params.get("customerId").toString()));
            }
            customer.setName(params.get("customerName") != null ? params.get("customerName").toString() : null);
            customer.setPhone(params.get("customerPhone") != null ? params.get("customerPhone").toString() : null);
            if (params.get("gender") != null) {
                customer.setGender(Integer.valueOf(params.get("gender").toString()));
            }
            if (params.get("idType") != null) {
                customer.setIdType(Integer.valueOf(params.get("idType").toString()));
            }
            customer.setIdNumber(params.get("idNumber") != null ? params.get("idNumber").toString() : null);
        }

        CheckIn checkIn = checkInService.walkInCheckIn(roomTypeId, roomId, customer, guests,
                checkInDate, checkOutDate, depositAmount, depositMethod, depositVoucherNo,
                keyCardCount, specialRequirements, remark, bookingSource, agreementUnitId, guaranteeType, memberId);
        return Result.success(checkIn);
    }

    @GetMapping("/list")
    public Result<PageResult<CheckIn>> list(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long roomTypeId,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Integer isOverdue,
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate,
            @RequestParam(required = false) Long floorId,
            @RequestParam(required = false) String sortField,
            @RequestParam(required = false) String sortOrder,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "20") int pageSize) {

        Map<String, Object> params = new java.util.HashMap<>();
        params.put("keyword", keyword);
        params.put("roomTypeId", roomTypeId);
        params.put("status", status);
        params.put("isOverdue", isOverdue);
        params.put("startDate", startDate);
        params.put("endDate", endDate);
        params.put("floorId", floorId);
        params.put("sortField", sortField);
        params.put("sortOrder", sortOrder);

        PageResult<CheckIn> page = checkInService.list(params, pageNum, pageSize);
        return Result.success(page);
    }

    @GetMapping("/{id}")
    public Result<CheckIn> getDetail(@PathVariable Long id) {
        CheckIn checkIn = checkInService.getDetail(id);
        return Result.success(checkIn);
    }

    @PostMapping("/changeRoom")
    public Result<RoomChangeRecord> changeRoom(@RequestBody Map<String, Object> params) {
        Long checkInId = Long.valueOf(params.get("checkInId").toString());
        Long newRoomId = Long.valueOf(params.get("newRoomId").toString());
        Integer changeReason = params.get("changeReason") != null ? Integer.valueOf(params.get("changeReason").toString()) : null;
        String changeDetail = params.get("changeDetail") != null ? params.get("changeDetail").toString() : null;

        RoomChangeRecord record = checkInService.changeRoom(checkInId, newRoomId, changeReason, changeDetail);
        return Result.success(record);
    }

    @PostMapping("/extend")
    public Result<ExtendStayRecord> extendStay(@RequestBody Map<String, Object> params) {
        Long checkInId = Long.valueOf(params.get("checkInId").toString());
        LocalDate newCheckOutDate = LocalDate.parse(params.get("newCheckOutDate").toString());
        String reason = params.get("reason") != null ? params.get("reason").toString() : null;

        ExtendStayRecord record = checkInService.extendStay(checkInId, newCheckOutDate, reason);
        return Result.success(record);
    }

    @GetMapping("/availableRooms/change")
    public Result<List<Room>> getAvailableRoomsForChange(
            @RequestParam Long checkInId,
            @RequestParam(required = false) Long roomTypeId) {
        List<Room> rooms = checkInService.getAvailableRoomsForChange(checkInId, roomTypeId);
        return Result.success(rooms);
    }

    @GetMapping("/availableRooms/walkin")
    public Result<List<Room>> getAvailableRoomsForWalkIn(
            @RequestParam(required = false) Long roomTypeId,
            @RequestParam(required = false) LocalDate checkInDate,
            @RequestParam(required = false) LocalDate checkOutDate) {
        List<Room> rooms = checkInService.getAvailableRoomsForWalkIn(roomTypeId, checkInDate, checkOutDate);
        return Result.success(rooms);
    }

    @PostMapping("/checkout")
    public Result<CheckOutRecord> checkOut(@RequestBody Map<String, Object> params) {
        Long checkInId = Long.valueOf(params.get("checkInId").toString());
        Integer paymentMethod = params.get("paymentMethod") != null ? Integer.valueOf(params.get("paymentMethod").toString()) : null;
        String paymentVoucherNo = params.get("paymentVoucherNo") != null ? params.get("paymentVoucherNo").toString() : null;
        Integer depositMethod = params.get("depositMethod") != null ? Integer.valueOf(params.get("depositMethod").toString()) : null;
        Integer keyCardReturned = params.get("keyCardReturned") != null ? Integer.valueOf(params.get("keyCardReturned").toString()) : null;
        Integer keyCardLost = params.get("keyCardLost") != null ? Integer.valueOf(params.get("keyCardLost").toString()) : null;
        Integer roomChecked = params.get("roomChecked") != null ? Integer.valueOf(params.get("roomChecked").toString()) : null;
        Integer roomCheckResult = params.get("roomCheckResult") != null ? Integer.valueOf(params.get("roomCheckResult").toString()) : null;
        String damageItems = params.get("damageItems") != null ? params.get("damageItems").toString() : null;
        String damageDescription = params.get("damageDescription") != null ? params.get("damageDescription").toString() : null;
        BigDecimal damageCompensation = params.get("damageCompensation") != null ? new BigDecimal(params.get("damageCompensation").toString()) : BigDecimal.ZERO;
        String remark = params.get("remark") != null ? params.get("remark").toString() : null;

        CheckOutRecord record = checkInService.checkOut(checkInId, paymentMethod, paymentVoucherNo,
                depositMethod, keyCardReturned, keyCardLost, roomChecked, roomCheckResult,
                damageItems, damageDescription, damageCompensation, remark);
        return Result.success(record);
    }

    @GetMapping("/checkout/{id}")
    public Result<CheckOutRecord> getCheckOutRecord(@PathVariable Long id) {
        CheckOutRecord record = checkInService.getCheckOutRecord(id);
        return Result.success(record);
    }

    @GetMapping("/checkout/byCheckIn/{checkInId}")
    public Result<CheckOutRecord> getCheckOutByCheckInId(@PathVariable Long checkInId) {
        CheckOutRecord record = checkInService.getCheckOutByCheckInId(checkInId);
        return Result.success(record);
    }

    @PostMapping("/consumption")
    public Result<Void> addConsumption(@RequestBody ConsumptionRecord consumption) {
        if (consumption.getCheckInId() == null) {
            return Result.error("入住单ID不能为空");
        }
        checkInService.addConsumption(consumption.getCheckInId(), consumption);
        return Result.success();
    }

    @PutMapping("/deposit")
    public Result<Void> updateDeposit(@RequestBody Map<String, Object> params) {
        Long checkInId = Long.valueOf(params.get("checkInId").toString());
        BigDecimal depositAmount = new BigDecimal(params.get("depositAmount").toString());
        Integer depositMethod = params.get("depositMethod") != null ? Integer.valueOf(params.get("depositMethod").toString()) : null;
        String depositVoucherNo = params.get("depositVoucherNo") != null ? params.get("depositVoucherNo").toString() : null;

        checkInService.updateDeposit(checkInId, depositAmount, depositMethod, depositVoucherNo);
        return Result.success();
    }

    @GetMapping("/changeRecords/{checkInId}")
    public Result<List<RoomChangeRecord>> getChangeRecords(@PathVariable Long checkInId) {
        List<RoomChangeRecord> records = checkInService.getChangeRecords(checkInId);
        return Result.success(records);
    }

    @GetMapping("/extendRecords/{checkInId}")
    public Result<List<ExtendStayRecord>> getExtendRecords(@PathVariable Long checkInId) {
        List<ExtendStayRecord> records = checkInService.getExtendRecords(checkInId);
        return Result.success(records);
    }

    @GetMapping("/todayStats")
    public Result<Map<String, Object>> getTodayStats() {
        Map<String, Object> stats = checkInService.getTodayStats();
        return Result.success(stats);
    }

    @PostMapping("/updateOverdue")
    public Result<Void> updateOverdueStatus() {
        checkInService.updateOverdueStatus();
        return Result.success();
    }
}
