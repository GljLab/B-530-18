package com.example.permission.controller;

import com.example.permission.common.BusinessException;
import com.example.permission.common.Result;
import com.example.permission.entity.Booking;
import com.example.permission.entity.Channel;
import com.example.permission.entity.ChannelInventory;
import com.example.permission.entity.Floor;
import com.example.permission.entity.Room;
import com.example.permission.entity.RoomType;
import com.example.permission.mapper.BookingMapper;
import com.example.permission.mapper.ChannelInventoryMapper;
import com.example.permission.mapper.ChannelMapper;
import com.example.permission.mapper.FloorMapper;
import com.example.permission.mapper.RoomMapper;
import com.example.permission.mapper.RoomTypeMapper;
import com.mybatisflex.core.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.example.permission.entity.table.BookingTableDef.BOOKING;
import static com.example.permission.entity.table.ChannelInventoryTableDef.CHANNEL_INVENTORY;
import static com.example.permission.entity.table.RoomTableDef.ROOM;

@RestController
@RequestMapping("/api/visual")
public class VisualCalendarController {

    @Autowired
    private BookingMapper bookingMapper;

    @Autowired
    private RoomMapper roomMapper;

    @Autowired
    private RoomTypeMapper roomTypeMapper;

    @Autowired
    private FloorMapper floorMapper;

    @Autowired
    private ChannelInventoryMapper channelInventoryMapper;

    @Autowired
    private ChannelMapper channelMapper;

    @GetMapping("/roomStatus")
    @PreAuthorize("hasAuthority('visual:roomStatus:query')")
    public Result<List<Map>> getRoomStatusCalendar(
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate,
            @RequestParam(required = false) Long roomTypeId,
            @RequestParam(required = false) Long floorId) {
        QueryWrapper roomQuery = QueryWrapper.create()
                .from(Room.class)
                .where(ROOM.DELETED.eq(0));
        if (roomTypeId != null) {
            roomQuery.and(ROOM.ROOM_TYPE_ID.eq(roomTypeId));
        }
        if (floorId != null) {
            roomQuery.and(ROOM.FLOOR_ID.eq(floorId));
        }
        roomQuery.orderBy(ROOM.ROOM_NUMBER.asc());
        List<Room> rooms = roomMapper.selectListByQuery(roomQuery);

        List<Long> roomIds = rooms.stream().map(Room::getId).collect(Collectors.toList());

        QueryWrapper bookingQuery = QueryWrapper.create()
                .from(Booking.class)
                .where(BOOKING.ROOM_ID.in(roomIds))
                .and(BOOKING.STATUS.in(1, 2, 3, 4))
                .and(BOOKING.CHECK_IN_DATE.le(endDate))
                .and(BOOKING.CHECK_OUT_DATE.gt(startDate))
                .and(BOOKING.DELETED.eq(0));
        List<Booking> bookings = bookingMapper.selectListByQuery(bookingQuery);

        Map<Long, List<Booking>> bookingsByRoom = new HashMap<>();
        for (Booking booking : bookings) {
            bookingsByRoom.computeIfAbsent(booking.getRoomId(), k -> new ArrayList<>()).add(booking);
        }

        List<Map> result = new ArrayList<>();
        LocalDate current = startDate;
        while (!current.isAfter(endDate)) {
            for (Room room : rooms) {
                String status = "available";
                List<Booking> roomBookings = bookingsByRoom.getOrDefault(room.getId(), new ArrayList<>());
                for (Booking booking : roomBookings) {
                    if (!booking.getCheckInDate().isAfter(current) && booking.getCheckOutDate().isAfter(current)) {
                        if (booking.getStatus() == 1) {
                            status = "reserved";
                        } else if (booking.getStatus() == 2) {
                            status = "checked_in";
                        } else if (booking.getStatus() == 3) {
                            status = "checked_in";
                        } else if (booking.getStatus() == 4) {
                            status = "checked_out_pending";
                        }
                        break;
                    }
                }

                Map<String, Object> item = new HashMap<>();
                item.put("date", current);
                item.put("room_id", room.getId());
                item.put("room_number", room.getRoomNumber());
                item.put("room_type_id", room.getRoomTypeId());
                item.put("floor_id", room.getFloorId());
                item.put("status", status);
                result.add(item);
            }
            current = current.plusDays(1);
        }
        return Result.success(result);
    }

    @GetMapping("/gantt")
    @PreAuthorize("hasAuthority('visual:gantt:query')")
    public Result<List<Map>> getBookingGantt(
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate,
            @RequestParam(required = false) Long roomTypeId,
            @RequestParam(required = false) Long floorId) {
        QueryWrapper roomQuery = QueryWrapper.create()
                .from(Room.class)
                .where(ROOM.DELETED.eq(0));
        if (roomTypeId != null) {
            roomQuery.and(ROOM.ROOM_TYPE_ID.eq(roomTypeId));
        }
        if (floorId != null) {
            roomQuery.and(ROOM.FLOOR_ID.eq(floorId));
        }
        List<Room> rooms = roomMapper.selectListByQuery(roomQuery);
        List<Long> roomIds = rooms.stream().map(Room::getId).collect(Collectors.toList());

        QueryWrapper bookingQuery = QueryWrapper.create()
                .from(Booking.class)
                .where(BOOKING.ROOM_ID.in(roomIds))
                .and(BOOKING.STATUS.in(1, 2, 3, 4))
                .and(BOOKING.CHECK_IN_DATE.le(endDate))
                .and(BOOKING.CHECK_OUT_DATE.gt(startDate))
                .and(BOOKING.DELETED.eq(0))
                .orderBy(BOOKING.CHECK_IN_DATE.asc());
        List<Booking> bookings = bookingMapper.selectListByQuery(bookingQuery);

        Map<Long, Room> roomMap = rooms.stream().collect(Collectors.toMap(Room::getId, r -> r));

        List<Map> result = new ArrayList<>();
        for (Booking booking : bookings) {
            Map<String, Object> item = new HashMap<>();
            item.put("room_id", booking.getRoomId());
            Room room = roomMap.get(booking.getRoomId());
            item.put("room_number", room != null ? room.getRoomNumber() : null);
            item.put("booking_id", booking.getId());
            item.put("booking_no", booking.getBookingNo());
            item.put("check_in_date", booking.getCheckInDate());
            item.put("check_out_date", booking.getCheckOutDate());
            item.put("status", booking.getStatus());
            item.put("customer_name", booking.getCustomerName());
            result.add(item);
        }
        return Result.success(result);
    }

    @GetMapping("/inventoryCompare")
    @PreAuthorize("hasAuthority('visual:compare:query')")
    public Result<List<Map>> getInventoryCompareCalendar(
            @RequestParam String channelIds,
            @RequestParam Long roomTypeId,
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate) {
        List<String> idStrList = Arrays.asList(channelIds.split(","));
        if (idStrList.size() > 5) {
            throw new BusinessException("最多支持5个渠道对比");
        }
        List<Long> channelIdList = idStrList.stream()
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(Long::valueOf)
                .collect(Collectors.toList());

        Map<Long, Channel> channelMap = new HashMap<>();
        for (Long channelId : channelIdList) {
            Channel channel = channelMapper.selectOneById(channelId);
            if (channel != null) {
                channelMap.put(channelId, channel);
            }
        }

        List<Map> result = new ArrayList<>();
        LocalDate current = startDate;
        while (!current.isAfter(endDate)) {
            for (Long channelId : channelIdList) {
                QueryWrapper query = QueryWrapper.create()
                        .from(ChannelInventory.class)
                        .where(CHANNEL_INVENTORY.CHANNEL_ID.eq(channelId))
                        .and(CHANNEL_INVENTORY.ROOM_TYPE_ID.eq(roomTypeId))
                        .and(CHANNEL_INVENTORY.DATE.eq(current));
                ChannelInventory inventory = channelInventoryMapper.selectOneByQuery(query);

                Channel channel = channelMap.get(channelId);
                Map<String, Object> item = new HashMap<>();
                item.put("date", current);
                item.put("channelId", channelId);
                item.put("channelName", channel != null ? channel.getChannelName() : "");
                item.put("allocatedRooms", inventory != null && inventory.getAllocatedRooms() != null ? inventory.getAllocatedRooms() : 0);
                item.put("usedRooms", inventory != null && inventory.getUsedRooms() != null ? inventory.getUsedRooms() : 0);
                item.put("shareMode", inventory != null && inventory.getShareMode() != null ? inventory.getShareMode() : 0);
                result.add(item);
            }
            current = current.plusDays(1);
        }
        return Result.success(result);
    }
}
