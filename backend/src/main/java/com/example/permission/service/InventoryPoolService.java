package com.example.permission.service;

import com.example.permission.common.BusinessException;
import com.example.permission.entity.Booking;
import com.example.permission.entity.RoomInventoryPool;
import com.example.permission.entity.RoomType;
import com.example.permission.mapper.BookingMapper;
import com.example.permission.mapper.RoomInventoryPoolMapper;
import com.example.permission.mapper.RoomTypeMapper;
import com.mybatisflex.core.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.example.permission.entity.table.BookingTableDef.BOOKING;
import static com.example.permission.entity.table.RoomInventoryPoolTableDef.ROOM_INVENTORY_POOL;
import static com.example.permission.entity.table.RoomTypeTableDef.ROOM_TYPE;

@Service
public class InventoryPoolService {

    @Autowired
    private RoomInventoryPoolMapper roomInventoryPoolMapper;

    @Autowired
    private RoomTypeMapper roomTypeMapper;

    @Autowired
    private BookingMapper bookingMapper;

    @Autowired
    private OverbookingStrategyService overbookingStrategyService;

    public List<RoomInventoryPool> getInventoryByRoomTypeAndDateRange(Long roomTypeId, LocalDate startDate, LocalDate endDate) {
        if (roomTypeId == null || startDate == null || endDate == null) {
            throw new BusinessException("房型ID和日期范围不能为空");
        }
        QueryWrapper query = QueryWrapper.create()
                .from(RoomInventoryPool.class)
                .where(ROOM_INVENTORY_POOL.ROOM_TYPE_ID.eq(roomTypeId))
                .and(ROOM_INVENTORY_POOL.DATE.ge(startDate))
                .and(ROOM_INVENTORY_POOL.DATE.le(endDate))
                .orderBy(ROOM_INVENTORY_POOL.DATE.asc());
        return roomInventoryPoolMapper.selectListByQuery(query);
    }

    public List<Map> getInventoryByDateRange(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null) {
            throw new BusinessException("日期范围不能为空");
        }
        QueryWrapper roomTypeQuery = QueryWrapper.create()
                .from(RoomType.class)
                .where(ROOM_TYPE.DELETED.eq(0));
        List<RoomType> roomTypes = roomTypeMapper.selectListByQuery(roomTypeQuery);

        List<Map> result = new ArrayList<>();
        for (RoomType roomType : roomTypes) {
            QueryWrapper inventoryQuery = QueryWrapper.create()
                    .from(RoomInventoryPool.class)
                    .where(ROOM_INVENTORY_POOL.ROOM_TYPE_ID.eq(roomType.getId()))
                    .and(ROOM_INVENTORY_POOL.DATE.ge(startDate))
                    .and(ROOM_INVENTORY_POOL.DATE.le(endDate))
                    .orderBy(ROOM_INVENTORY_POOL.DATE.asc());
            List<RoomInventoryPool> inventories = roomInventoryPoolMapper.selectListByQuery(inventoryQuery);

            for (RoomInventoryPool inventory : inventories) {
                QueryWrapper bookedQuery = QueryWrapper.create()
                        .from(Booking.class)
                        .where(BOOKING.ROOM_TYPE_ID.eq(roomType.getId()))
                        .and(BOOKING.STATUS.in(1, 2, 3, 4))
                        .and(BOOKING.CHECK_IN_DATE.le(inventory.getDate()))
                        .and(BOOKING.CHECK_OUT_DATE.gt(inventory.getDate()))
                        .and(BOOKING.DELETED.eq(0));
                long bookedCount = bookingMapper.selectCountByQuery(bookedQuery);

                Map<String, Object> item = new HashMap<>();
                item.put("roomTypeId", roomType.getId());
                item.put("typeName", roomType.getTypeName());
                item.put("date", inventory.getDate());
                item.put("totalRooms", inventory.getTotalRooms());
                item.put("availableRooms", inventory.getAvailableRooms());
                item.put("reservedRooms", inventory.getReservedRooms());
                item.put("bookedCount", bookedCount);
                result.add(item);
            }
        }
        return result;
    }

    @Transactional(rollbackFor = Exception.class)
    public void setInventory(Long roomTypeId, LocalDate date, Integer totalRooms, Integer availableRooms, Integer reservedRooms) {
        if (roomTypeId == null || date == null) {
            throw new BusinessException("房型ID和日期不能为空");
        }
        QueryWrapper query = QueryWrapper.create()
                .from(RoomInventoryPool.class)
                .where(ROOM_INVENTORY_POOL.ROOM_TYPE_ID.eq(roomTypeId))
                .and(ROOM_INVENTORY_POOL.DATE.eq(date));
        RoomInventoryPool existing = roomInventoryPoolMapper.selectOneByQuery(query);

        if (existing != null) {
            existing.setTotalRooms(totalRooms);
            existing.setAvailableRooms(availableRooms);
            existing.setReservedRooms(reservedRooms);
            existing.setUpdateTime(LocalDateTime.now());
            roomInventoryPoolMapper.update(existing);
        } else {
            RoomInventoryPool pool = new RoomInventoryPool();
            pool.setRoomTypeId(roomTypeId);
            pool.setDate(date);
            pool.setTotalRooms(totalRooms);
            pool.setAvailableRooms(availableRooms);
            pool.setReservedRooms(reservedRooms);
            pool.setCreateTime(LocalDateTime.now());
            pool.setUpdateTime(LocalDateTime.now());
            roomInventoryPoolMapper.insert(pool);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void batchSetInventory(Long roomTypeId, LocalDate startDate, LocalDate endDate, Integer totalRooms, Integer availableRooms, Integer reservedRooms) {
        if (roomTypeId == null || startDate == null || endDate == null) {
            throw new BusinessException("房型ID和日期范围不能为空");
        }
        LocalDate current = startDate;
        while (!current.isAfter(endDate)) {
            setInventory(roomTypeId, current, totalRooms, availableRooms, reservedRooms);
            current = current.plusDays(1);
        }
    }

    public List<Map> getMonitorData(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null) {
            throw new BusinessException("日期范围不能为空");
        }
        List<Map> result = new ArrayList<>();

        QueryWrapper roomTypeQuery = QueryWrapper.create()
                .from(RoomType.class)
                .where(ROOM_TYPE.DELETED.eq(0));
        List<RoomType> roomTypes = roomTypeMapper.selectListByQuery(roomTypeQuery);

        LocalDate current = startDate;
        while (!current.isAfter(endDate)) {
            for (RoomType roomType : roomTypes) {
                QueryWrapper inventoryQuery = QueryWrapper.create()
                        .from(RoomInventoryPool.class)
                        .where(ROOM_INVENTORY_POOL.ROOM_TYPE_ID.eq(roomType.getId()))
                        .and(ROOM_INVENTORY_POOL.DATE.eq(current));
                RoomInventoryPool inventory = roomInventoryPoolMapper.selectOneByQuery(inventoryQuery);

                QueryWrapper bookedQuery = QueryWrapper.create()
                        .from(Booking.class)
                        .where(BOOKING.ROOM_TYPE_ID.eq(roomType.getId()))
                        .and(BOOKING.STATUS.in(1, 2, 3, 4))
                        .and(BOOKING.CHECK_IN_DATE.le(current))
                        .and(BOOKING.CHECK_OUT_DATE.gt(current))
                        .and(BOOKING.DELETED.eq(0));
                long bookedCount = bookingMapper.selectCountByQuery(bookedQuery);

                int totalRooms = inventory != null ? (inventory.getTotalRooms() != null ? inventory.getTotalRooms() : 0) : (roomType.getRoomCount() != null ? roomType.getRoomCount() : 0);
                int availableRooms = inventory != null ? (inventory.getAvailableRooms() != null ? inventory.getAvailableRooms() : 0) : totalRooms;
                int remaining = availableRooms - (int) bookedCount;
                BigDecimal bookingRate = totalRooms > 0
                        ? BigDecimal.valueOf(bookedCount).divide(BigDecimal.valueOf(totalRooms), 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100))
                        : BigDecimal.ZERO;

                Integer maxSalable = overbookingStrategyService.getMaxSalableRooms(roomType.getId(), current);
                String overbookingStatus = null;
                if (bookedCount > maxSalable) {
                    overbookingStatus = "exceeded";
                } else if (bookedCount > availableRooms) {
                    overbookingStatus = "overbooked";
                } else if (totalRooms > 0 && bookingRate.compareTo(BigDecimal.valueOf(90)) >= 0) {
                    overbookingStatus = "near_full";
                }

                Map<String, Object> item = new HashMap<>();
                item.put("date", current);
                item.put("roomTypeId", roomType.getId());
                item.put("typeName", roomType.getTypeName());
                item.put("totalRooms", totalRooms);
                item.put("availableRooms", availableRooms);
                item.put("bookedCount", bookedCount);
                item.put("remaining", remaining);
                item.put("bookingRate", bookingRate);
                item.put("maxSalable", maxSalable);
                item.put("overbookingStatus", overbookingStatus);
                result.add(item);
            }
            current = current.plusDays(1);
        }
        return result;
    }

    public List<Map> exportMonitorData(LocalDate startDate, LocalDate endDate) {
        return getMonitorData(startDate, endDate);
    }
}
