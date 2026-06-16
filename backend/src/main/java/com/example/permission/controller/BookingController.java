package com.example.permission.controller;

import com.example.permission.common.BusinessException;
import com.example.permission.common.PageResult;
import com.example.permission.common.Result;
import com.example.permission.entity.Booking;
import com.example.permission.entity.BookingDetail;
import com.example.permission.entity.RefundRecord;
import com.example.permission.entity.Room;
import com.example.permission.entity.RoomType;
import com.example.permission.security.LoginUser;
import com.example.permission.service.BookingService;
import com.example.permission.service.ExportService;
import com.example.permission.service.RoomTypeService;
import com.example.permission.service.SysUserService;
import com.example.permission.utils.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/booking")
public class BookingController {

    @Autowired
    private BookingService bookingService;

    @Autowired
    private RoomTypeService roomTypeService;

    @Autowired
    private SysUserService sysUserService;

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private ExportService exportService;

    @GetMapping("/room/query")
    @PreAuthorize("hasAuthority('booking:roomQuery:query')")
    public Result<List<Room>> queryAvailableRooms(
            @RequestParam(required = false) LocalDate checkInDate,
            @RequestParam(required = false) LocalDate checkOutDate,
            @RequestParam(required = false) Long roomTypeId,
            @RequestParam(required = false) Long buildingId,
            @RequestParam(required = false) Long floorId,
            @RequestParam(required = false) String orientation,
            @RequestParam(required = false) String viewType) {
        List<Room> result = bookingService.queryAvailableRoomList(checkInDate, checkOutDate,
                roomTypeId, buildingId, floorId, orientation, viewType);
        return Result.success(result);
    }

    @PostMapping("/create")
    @PreAuthorize("hasAuthority('booking:create')")
    @Transactional(rollbackFor = Exception.class)
    public Result<Booking> createBooking(@RequestBody Map<String, Object> params,
                                         @RequestHeader("Authorization") String token) {
        LoginUser loginUser = (LoginUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        Long roomId = params.get("roomId") != null ? Long.valueOf(params.get("roomId").toString()) : null;
        Long customerId = params.get("customerId") != null ? Long.valueOf(params.get("customerId").toString()) : null;
        LocalDate checkInDate = params.get("checkInDate") != null ? LocalDate.parse(params.get("checkInDate").toString()) : null;
        LocalDate checkOutDate = params.get("checkOutDate") != null ? LocalDate.parse(params.get("checkOutDate").toString()) : null;
        Integer extraBedCount = params.get("extraBedCount") != null ? Integer.valueOf(params.get("extraBedCount").toString()) : null;
        BigDecimal extraBedPrice = params.get("extraBedPrice") != null ? new BigDecimal(params.get("extraBedPrice").toString()) : null;
        BigDecimal discount = params.get("discount") != null ? new BigDecimal(params.get("discount").toString()) : null;
        BigDecimal otherFee = params.get("otherFee") != null ? new BigDecimal(params.get("otherFee").toString()) : null;
        Integer guestCount = params.get("guestCount") != null ? Integer.valueOf(params.get("guestCount").toString()) : null;
        String guestNames = params.get("guestNames") != null ? params.get("guestNames").toString() : null;
        String guestPhone = params.get("guestPhone") != null ? params.get("guestPhone").toString() : null;
        String bookingSource = params.get("bookingSource") != null ? params.get("bookingSource").toString() : null;
        String sourceRemark = params.get("sourceRemark") != null ? params.get("sourceRemark").toString() : null;
        String guaranteeType = params.get("guaranteeType") != null ? params.get("guaranteeType").toString() : null;
        LocalDateTime expectedArrivalTime = params.get("expectedArrivalTime") != null ?
                LocalDateTime.parse(params.get("expectedArrivalTime").toString(), DateTimeFormatter.ISO_LOCAL_DATE_TIME) : null;
        String specialRequirements = params.get("specialRequirements") != null ? params.get("specialRequirements").toString() : null;

        List<Map<String, Object>> detailList = params.get("details") != null ?
                (List<Map<String, Object>>) params.get("details") : null;
        List<BookingDetail> details = null;
        if (detailList != null && !detailList.isEmpty()) {
            details = new ArrayList<>();
            for (Map<String, Object> detailMap : detailList) {
                BookingDetail detail = new BookingDetail();
                detail.setStayDate(detailMap.get("stayDate") != null ?
                        LocalDate.parse(detailMap.get("stayDate").toString()) : null);
                detail.setPrice(detailMap.get("price") != null ?
                        new BigDecimal(detailMap.get("price").toString()) : null);
                detail.setDayType(detailMap.get("dayType") != null ?
                        detailMap.get("dayType").toString() : null);
                details.add(detail);
            }
        }

        Booking booking = new Booking();
        booking.setRoomId(roomId);
        booking.setCustomerId(customerId);
        booking.setCheckInDate(checkInDate);
        booking.setCheckOutDate(checkOutDate);
        booking.setExtraBedCount(extraBedCount);
        booking.setExtraBedPrice(extraBedPrice);
        booking.setDiscount(discount);
        booking.setOtherFee(otherFee);
        booking.setGuestCount(guestCount);
        booking.setGuestNames(guestNames);
        booking.setGuestPhone(guestPhone);
        booking.setBookingSource(bookingSource);
        booking.setSourceRemark(sourceRemark);
        booking.setGuaranteeType(guaranteeType);
        booking.setExpectedArrivalTime(expectedArrivalTime);
        booking.setSpecialRequirements(specialRequirements);

        Booking result = bookingService.createBooking(booking, details, loginUser);
        return Result.success(result);
    }

    @GetMapping("/page")
    @PreAuthorize("hasAuthority('booking:list')")
    public Result<PageResult<Booking>> getBookingPage(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String bookingNo,
            @RequestParam(required = false) Long customerId,
            @RequestParam(required = false) String customerName,
            @RequestParam(required = false) String customerPhone,
            @RequestParam(required = false) Long roomTypeId,
            @RequestParam(required = false) Long roomId,
            @RequestParam(required = false) String roomNumber,
            @RequestParam(required = false) List<Integer> status,
            @RequestParam(required = false) LocalDate checkInDateStart,
            @RequestParam(required = false) LocalDate checkInDateEnd,
            @RequestParam(required = false) LocalDate checkOutDateStart,
            @RequestParam(required = false) LocalDate checkOutDateEnd,
            @RequestParam(required = false) String bookingSource,
            @RequestParam(required = false) String sortField,
            @RequestParam(required = false) String sortOrder) {
        PageResult<Booking> result = bookingService.getBookingPage(pageNum, pageSize, bookingNo,
                customerId, customerName, customerPhone, roomTypeId, roomId, roomNumber, status,
                checkInDateStart, checkInDateEnd, checkOutDateStart, checkOutDateEnd, bookingSource,
                sortField, sortOrder);
        return Result.success(result);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('booking:query')")
    public Result<Map<String, Object>> getBookingDetail(@PathVariable Long id) {
        Map<String, Object> result = bookingService.getBookingDetail(id);
        return Result.success(result);
    }

    @PutMapping("/{id}/confirm")
    @PreAuthorize("hasAuthority('booking:confirm')")
    @Transactional(rollbackFor = Exception.class)
    public Result<Void> confirmBooking(@PathVariable Long id) {
        LoginUser loginUser = (LoginUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        bookingService.confirmBooking(id, loginUser);
        return Result.success();
    }

    @PutMapping("/{id}/cancel")
    @PreAuthorize("hasAuthority('booking:cancel')")
    @Transactional(rollbackFor = Exception.class)
    public Result<Void> cancelBooking(@PathVariable Long id,
                                      @RequestBody Map<String, Object> params) {
        LoginUser loginUser = (LoginUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String cancelReason = params.get("cancelReason") != null ? params.get("cancelReason").toString() : null;
        String cancelDetail = params.get("cancelDetail") != null ? params.get("cancelDetail").toString() : null;
        Boolean chargePenalty = params.get("chargePenalty") != null ? (Boolean) params.get("chargePenalty") : false;
        bookingService.cancelBooking(id, cancelReason, cancelDetail, chargePenalty, loginUser);
        return Result.success();
    }

    @PutMapping("/{id}/checkin")
    @PreAuthorize("hasAuthority('booking:checkin')")
    @Transactional(rollbackFor = Exception.class)
    public Result<Void> checkInBooking(@PathVariable Long id,
                                       @RequestBody(required = false) Map<String, Object> params) {
        LoginUser loginUser = (LoginUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        LocalDateTime checkInTime = null;
        if (params != null && params.get("checkInTime") != null) {
            checkInTime = LocalDateTime.parse(params.get("checkInTime").toString(), DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        }
        bookingService.checkInBooking(id, checkInTime, loginUser);
        return Result.success();
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('booking:edit')")
    @Transactional(rollbackFor = Exception.class)
    public Result<Void> updateBooking(@PathVariable Long id,
                                      @RequestBody Map<String, Object> params) {
        LoginUser loginUser = (LoginUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Booking booking = new Booking();
        booking.setId(id);
        Map<String, Object> changedFields = (Map<String, Object>) params.get("changedFields");
        bookingService.updateBooking(booking, changedFields, loginUser);
        return Result.success();
    }

    @PutMapping("/{id}/changeRoom")
    @PreAuthorize("hasAuthority('booking:changeRoom')")
    @Transactional(rollbackFor = Exception.class)
    public Result<Map<String, Object>> changeRoom(@PathVariable Long id,
                                                   @RequestBody Map<String, Object> params) {
        LoginUser loginUser = (LoginUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Long newRoomId = params.get("newRoomId") != null ? Long.valueOf(params.get("newRoomId").toString()) : null;
        Map<String, Object> result = bookingService.changeRoom(id, newRoomId, loginUser);
        return Result.success(result);
    }

    @PostMapping("/{id}/refund")
    @PreAuthorize("hasAuthority('booking:refund:apply')")
    @Transactional(rollbackFor = Exception.class)
    public Result<RefundRecord> applyRefund(@PathVariable Long id,
                                             @RequestBody Map<String, Object> params) {
        LoginUser loginUser = (LoginUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String refundReason = params.get("refundReason") != null ? params.get("refundReason").toString() : null;
        RefundRecord result = bookingService.applyRefund(id, refundReason, loginUser);
        return Result.success(result);
    }

    @PostMapping("/batch/confirm")
    @PreAuthorize("hasAuthority('booking:batchConfirm')")
    @Transactional(rollbackFor = Exception.class)
    public Result<Map<String, Object>> batchConfirm(@RequestBody Map<String, Object> params) {
        LoginUser loginUser = (LoginUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        List<?> rawIds = (List<?>) params.get("ids");
        List<Long> ids = new ArrayList<>();
        if (rawIds != null) {
            for (Object id : rawIds) {
                ids.add(Long.valueOf(id.toString()));
            }
        }
        Map<String, Object> result = bookingService.batchConfirm(ids, loginUser);
        return Result.success(result);
    }

    @PostMapping("/export")
    @PreAuthorize("hasAuthority('booking:export')")
    public ResponseEntity<byte[]> exportBookings(@RequestBody Map<String, Object> params,
                                                  @RequestHeader("Authorization") String token) throws Exception {
        String actualToken = token.replace("Bearer ", "");
        Long userId = jwtUtils.getUserIdFromToken(actualToken);
        String username = sysUserService.getUserDetail(userId).getUsername();

        String bookingNo = params.get("bookingNo") != null ? params.get("bookingNo").toString() : null;
        Long customerId = params.get("customerId") != null ? Long.valueOf(params.get("customerId").toString()) : null;
        String customerName = params.get("customerName") != null ? params.get("customerName").toString() : null;
        String customerPhone = params.get("customerPhone") != null ? params.get("customerPhone").toString() : null;
        Long roomTypeId = params.get("roomTypeId") != null ? Long.valueOf(params.get("roomTypeId").toString()) : null;
        Long roomId = params.get("roomId") != null ? Long.valueOf(params.get("roomId").toString()) : null;
        String roomNumber = params.get("roomNumber") != null ? params.get("roomNumber").toString() : null;
        List<Integer> status = (List<Integer>) params.get("statusList");
        LocalDate checkInDateStart = params.get("checkInDateStart") != null ?
                LocalDate.parse(params.get("checkInDateStart").toString()) : null;
        LocalDate checkInDateEnd = params.get("checkInDateEnd") != null ?
                LocalDate.parse(params.get("checkInDateEnd").toString()) : null;
        LocalDate checkOutDateStart = params.get("checkOutDateStart") != null ?
                LocalDate.parse(params.get("checkOutDateStart").toString()) : null;
        LocalDate checkOutDateEnd = params.get("checkOutDateEnd") != null ?
                LocalDate.parse(params.get("checkOutDateEnd").toString()) : null;
        String bookingSource = params.get("bookingSource") != null ? params.get("bookingSource").toString() : null;
        String scope = params.get("scope") != null ? params.get("scope").toString() : "all";
        List<?> rawIds = (List<?>) params.get("ids");
        Integer pageNum = params.get("pageNum") != null ? Integer.valueOf(params.get("pageNum").toString()) : null;
        Integer pageSize = params.get("pageSize") != null ? Integer.valueOf(params.get("pageSize").toString()) : null;

        List<Long> ids = new ArrayList<>();
        if (rawIds != null) {
            for (Object id : rawIds) {
                ids.add(Long.valueOf(id.toString()));
            }
        }

        List<Booking> bookings = bookingService.exportBookingsWithScope(bookingNo, customerId, customerName,
                customerPhone, roomTypeId, roomId, roomNumber, status, checkInDateStart, checkInDateEnd,
                checkOutDateStart, checkOutDateEnd, bookingSource, scope, ids, pageNum, pageSize);

        List<String> exportFields = (List<String>) params.get("exportFields");
        if (exportFields == null || exportFields.isEmpty()) {
            throw new BusinessException("请选择要导出的字段");
        }

        String filterDesc = buildBookingFilterDesc(bookingNo, customerName, customerPhone,
                roomNumber, status, bookingSource);

        byte[] excelData = exportService.exportStatistics(params, username, filterDesc);
        String fileName = "预订数据_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".xlsx";
        String encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8.name()).replaceAll("\\+", "%20");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
        headers.setContentDispositionFormData("attachment", encodedFileName);
        headers.add("Content-Disposition", "attachment; filename*=UTF-8''" + encodedFileName);

        return ResponseEntity.ok()
                .headers(headers)
                .body(excelData);
    }

    @GetMapping("/price/calculate")
    @PreAuthorize("hasAuthority('booking:create')")
    public Result<Map<String, Object>> calculatePrice(
            @RequestParam Long roomTypeId,
            @RequestParam LocalDate checkInDate,
            @RequestParam LocalDate checkOutDate,
            @RequestParam(required = false) Integer extraBedCount,
            @RequestParam(required = false) BigDecimal extraBedPrice) {
        RoomType roomType = roomTypeService.getById(roomTypeId);
        if (roomType == null) {
            throw new BusinessException("房型不存在");
        }
        Map<String, Object> result = bookingService.calculatePriceWithDetails(roomType, checkInDate, checkOutDate,
                extraBedCount, extraBedPrice);
        return Result.success(result);
    }

    private String buildBookingFilterDesc(String bookingNo, String customerName, String customerPhone,
                                          String roomNumber, List<Integer> status, String bookingSource) {
        StringBuilder sb = new StringBuilder();
        if (bookingNo != null && !bookingNo.isEmpty()) sb.append("预订号:").append(bookingNo).append("; ");
        if (customerName != null && !customerName.isEmpty()) sb.append("客户姓名:").append(customerName).append("; ");
        if (customerPhone != null && !customerPhone.isEmpty()) sb.append("客户电话:").append(customerPhone).append("; ");
        if (roomNumber != null && !roomNumber.isEmpty()) sb.append("房号:").append(roomNumber).append("; ");
        if (status != null && !status.isEmpty()) sb.append("状态:").append(status).append("; ");
        if (bookingSource != null && !bookingSource.isEmpty()) sb.append("来源:").append(bookingSource).append("; ");
        return sb.length() == 0 ? "全部预订" : sb.toString().trim();
    }
}
