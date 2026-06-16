package com.example.permission.service;

import com.example.permission.common.BusinessException;
import com.example.permission.entity.Booking;
import com.example.permission.entity.OverbookingGlobalConfig;
import com.example.permission.entity.OverbookingStrategy;
import com.example.permission.entity.RoomInventoryPool;
import com.example.permission.entity.RoomType;
import com.example.permission.mapper.BookingMapper;
import com.example.permission.mapper.OverbookingGlobalConfigMapper;
import com.example.permission.mapper.OverbookingStrategyMapper;
import com.example.permission.mapper.RoomInventoryPoolMapper;
import com.example.permission.mapper.RoomTypeMapper;
import com.mybatisflex.core.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.example.permission.entity.table.BookingTableDef.BOOKING;
import static com.example.permission.entity.table.OverbookingGlobalConfigTableDef.OVERBOOKING_GLOBAL_CONFIG;
import static com.example.permission.entity.table.OverbookingStrategyTableDef.OVERBOOKING_STRATEGY;
import static com.example.permission.entity.table.RoomInventoryPoolTableDef.ROOM_INVENTORY_POOL;
import static com.example.permission.entity.table.RoomTypeTableDef.ROOM_TYPE;

@Service
public class OverbookingStrategyService {

    @Autowired
    private OverbookingGlobalConfigMapper overbookingGlobalConfigMapper;

    @Autowired
    private OverbookingStrategyMapper overbookingStrategyMapper;

    @Autowired
    private RoomTypeMapper roomTypeMapper;

    @Autowired
    private RoomInventoryPoolMapper roomInventoryPoolMapper;

    @Autowired
    private BookingMapper bookingMapper;

    public OverbookingGlobalConfig getGlobalConfig() {
        QueryWrapper query = QueryWrapper.create()
                .from(OverbookingGlobalConfig.class)
                .limit(1);
        OverbookingGlobalConfig config = overbookingGlobalConfigMapper.selectOneByQuery(query);
        if (config == null) {
            config = new OverbookingGlobalConfig();
            config.setEnabled(0);
            config.setCreateTime(LocalDateTime.now());
            config.setUpdateTime(LocalDateTime.now());
            overbookingGlobalConfigMapper.insert(config);
        }
        return config;
    }

    @Transactional(rollbackFor = Exception.class)
    public void updateGlobalConfig(Integer enabled) {
        OverbookingGlobalConfig config = getGlobalConfig();
        config.setEnabled(enabled);
        config.setUpdateTime(LocalDateTime.now());
        overbookingGlobalConfigMapper.update(config);
    }

    public OverbookingStrategy getStrategyByRoomType(Long roomTypeId) {
        if (roomTypeId == null) {
            throw new BusinessException("房型ID不能为空");
        }
        QueryWrapper query = QueryWrapper.create()
                .from(OverbookingStrategy.class)
                .where(OVERBOOKING_STRATEGY.ROOM_TYPE_ID.eq(roomTypeId));
        return overbookingStrategyMapper.selectOneByQuery(query);
    }

    public List<Map> getAllStrategies() {
        QueryWrapper roomTypeQuery = QueryWrapper.create()
                .from(RoomType.class)
                .where(ROOM_TYPE.DELETED.eq(0));
        List<RoomType> roomTypes = roomTypeMapper.selectListByQuery(roomTypeQuery);

        List<Map> result = new ArrayList<>();
        for (RoomType roomType : roomTypes) {
            OverbookingStrategy strategy = getStrategyByRoomType(roomType.getId());
            Map<String, Object> item = new HashMap<>();
            item.put("roomTypeId", roomType.getId());
            item.put("typeName", roomType.getTypeName());
            item.put("roomCount", roomType.getRoomCount());
            if (strategy != null) {
                item.put("id", strategy.getId());
                item.put("enabled", strategy.getEnabled());
                item.put("overbookingRatio", strategy.getOverbookingRatio());
                item.put("maxOverbooking", strategy.getMaxOverbooking());
            } else {
                item.put("enabled", 0);
                item.put("overbookingRatio", BigDecimal.ZERO);
                item.put("maxOverbooking", 0);
            }
            result.add(item);
        }
        return result;
    }

    @Transactional(rollbackFor = Exception.class)
    public void saveStrategy(OverbookingStrategy strategy) {
        if (strategy.getRoomTypeId() == null) {
            throw new BusinessException("房型ID不能为空");
        }
        if (strategy.getOverbookingRatio() != null) {
            if (strategy.getOverbookingRatio().compareTo(BigDecimal.ZERO) < 0
                    || strategy.getOverbookingRatio().compareTo(new BigDecimal("0.5")) > 0) {
                throw new BusinessException("超售比例必须在0到0.5之间");
            }
        }

        OverbookingStrategy existing = getStrategyByRoomType(strategy.getRoomTypeId());
        if (existing != null) {
            existing.setEnabled(strategy.getEnabled());
            existing.setOverbookingRatio(strategy.getOverbookingRatio());
            existing.setMaxOverbooking(strategy.getMaxOverbooking());
            existing.setUpdateTime(LocalDateTime.now());
            overbookingStrategyMapper.update(existing);
        } else {
            strategy.setCreateTime(LocalDateTime.now());
            strategy.setUpdateTime(LocalDateTime.now());
            overbookingStrategyMapper.insert(strategy);
        }
    }

    public Integer getMaxSalableRooms(Long roomTypeId, LocalDate date) {
        if (roomTypeId == null || date == null) {
            return 0;
        }
        OverbookingGlobalConfig globalConfig = getGlobalConfig();
        if (globalConfig.getEnabled() == null || globalConfig.getEnabled() != 1) {
            QueryWrapper inventoryQuery = QueryWrapper.create()
                    .from(RoomInventoryPool.class)
                    .where(ROOM_INVENTORY_POOL.ROOM_TYPE_ID.eq(roomTypeId))
                    .and(ROOM_INVENTORY_POOL.DATE.eq(date));
            RoomInventoryPool inventory = roomInventoryPoolMapper.selectOneByQuery(inventoryQuery);
            return inventory != null && inventory.getAvailableRooms() != null ? inventory.getAvailableRooms() : 0;
        }

        OverbookingStrategy strategy = getStrategyByRoomType(roomTypeId);
        if (strategy == null || strategy.getEnabled() == null || strategy.getEnabled() != 1) {
            QueryWrapper inventoryQuery = QueryWrapper.create()
                    .from(RoomInventoryPool.class)
                    .where(ROOM_INVENTORY_POOL.ROOM_TYPE_ID.eq(roomTypeId))
                    .and(ROOM_INVENTORY_POOL.DATE.eq(date));
            RoomInventoryPool inventory = roomInventoryPoolMapper.selectOneByQuery(inventoryQuery);
            return inventory != null && inventory.getAvailableRooms() != null ? inventory.getAvailableRooms() : 0;
        }

        QueryWrapper inventoryQuery = QueryWrapper.create()
                .from(RoomInventoryPool.class)
                .where(ROOM_INVENTORY_POOL.ROOM_TYPE_ID.eq(roomTypeId))
                .and(ROOM_INVENTORY_POOL.DATE.eq(date));
        RoomInventoryPool inventory = roomInventoryPoolMapper.selectOneByQuery(inventoryQuery);
        int availableRooms = inventory != null && inventory.getAvailableRooms() != null ? inventory.getAvailableRooms() : 0;

        BigDecimal ratio = strategy.getOverbookingRatio() != null ? strategy.getOverbookingRatio() : BigDecimal.ZERO;
        int maxSalable = BigDecimal.valueOf(availableRooms)
                .multiply(BigDecimal.ONE.add(ratio))
                .intValue();

        if (strategy.getMaxOverbooking() != null && strategy.getMaxOverbooking() > 0) {
            int absoluteMax = availableRooms + strategy.getMaxOverbooking();
            if (maxSalable > absoluteMax) {
                maxSalable = absoluteMax;
            }
        }

        return maxSalable;
    }

    public Map checkOverbookingAlert(Long roomTypeId, LocalDate date, Integer bookedCount) {
        if (roomTypeId == null || date == null || bookedCount == null) {
            return null;
        }

        QueryWrapper inventoryQuery = QueryWrapper.create()
                .from(RoomInventoryPool.class)
                .where(ROOM_INVENTORY_POOL.ROOM_TYPE_ID.eq(roomTypeId))
                .and(ROOM_INVENTORY_POOL.DATE.eq(date));
        RoomInventoryPool inventory = roomInventoryPoolMapper.selectOneByQuery(inventoryQuery);

        int availableRooms = inventory != null && inventory.getAvailableRooms() != null ? inventory.getAvailableRooms() : 0;
        int totalRooms = inventory != null && inventory.getTotalRooms() != null ? inventory.getTotalRooms() : 0;

        Integer maxSalable = getMaxSalableRooms(roomTypeId, date);

        String alertLevel = null;
        String message = null;

        if (totalRooms > 0) {
            BigDecimal usageRate = BigDecimal.valueOf(bookedCount)
                    .divide(BigDecimal.valueOf(totalRooms), 4, java.math.RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100));

            if (bookedCount > maxSalable) {
                alertLevel = "exceeded";
                message = "已超过最大可售数，超售限制已突破";
            } else if (bookedCount > availableRooms) {
                alertLevel = "overbooked";
                message = "已超过实际可用房数，进入超售状态";
            } else if (usageRate.compareTo(BigDecimal.valueOf(90)) >= 0) {
                alertLevel = "near_full";
                message = "接近满房，入住率已达" + usageRate.setScale(1, java.math.RoundingMode.HALF_UP) + "%";
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("alertLevel", alertLevel);
        result.put("message", message);
        result.put("availableRooms", availableRooms);
        result.put("maxSalable", maxSalable);
        result.put("bookedCount", bookedCount);
        return result;
    }
}
