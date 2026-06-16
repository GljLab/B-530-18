package com.example.permission.service;

import com.example.permission.common.BusinessException;
import com.example.permission.common.PageResult;
import com.example.permission.entity.Booking;
import com.example.permission.entity.Channel;
import com.example.permission.entity.ChannelInventory;
import com.example.permission.entity.ChannelPrice;
import com.example.permission.entity.RoomInventoryPool;
import com.example.permission.entity.RoomType;
import com.example.permission.mapper.BookingMapper;
import com.example.permission.mapper.ChannelInventoryMapper;
import com.example.permission.mapper.ChannelMapper;
import com.example.permission.mapper.ChannelPriceMapper;
import com.example.permission.mapper.RoomInventoryPoolMapper;
import com.example.permission.mapper.RoomTypeMapper;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.example.permission.entity.table.BookingTableDef.BOOKING;
import static com.example.permission.entity.table.ChannelInventoryTableDef.CHANNEL_INVENTORY;
import static com.example.permission.entity.table.ChannelPriceTableDef.CHANNEL_PRICE;
import static com.example.permission.entity.table.ChannelTableDef.CHANNEL;
import static com.example.permission.entity.table.RoomInventoryPoolTableDef.ROOM_INVENTORY_POOL;
import static com.example.permission.entity.table.RoomTypeTableDef.ROOM_TYPE;

@Service
public class ChannelService {

    @Autowired
    private ChannelMapper channelMapper;

    @Autowired
    private ChannelInventoryMapper channelInventoryMapper;

    @Autowired
    private ChannelPriceMapper channelPriceMapper;

    @Autowired
    private RoomTypeMapper roomTypeMapper;

    @Autowired
    private RoomInventoryPoolMapper roomInventoryPoolMapper;

    @Autowired
    private BookingMapper bookingMapper;

    public PageResult<Channel> getChannelPage(Integer pageNum, Integer pageSize, String channelName, Integer channelType, Integer cooperationStatus) {
        QueryWrapper query = QueryWrapper.create()
                .from(Channel.class)
                .where(CHANNEL.DELETED.eq(0));

        if (StringUtils.hasText(channelName)) {
            query.and(CHANNEL.CHANNEL_NAME.like(channelName));
        }
        if (channelType != null) {
            query.and(CHANNEL.CHANNEL_TYPE.eq(channelType));
        }
        if (cooperationStatus != null) {
            query.and(CHANNEL.COOPERATION_STATUS.eq(cooperationStatus));
        }
        query.orderBy(CHANNEL.CREATE_TIME.desc());

        Page<Channel> page = channelMapper.paginate(Page.of(pageNum, pageSize), query);
        return new PageResult<>(page.getTotalRow(), page.getRecords(),
                (long) page.getPageNumber(), (long) page.getPageSize());
    }

    public Channel getChannelById(Long id) {
        if (id == null) {
            throw new BusinessException("渠道ID不能为空");
        }
        Channel channel = channelMapper.selectOneById(id);
        if (channel == null) {
            throw new BusinessException("渠道不存在");
        }
        return channel;
    }

    @Transactional(rollbackFor = Exception.class)
    public void createChannel(Channel channel) {
        if (channel == null) {
            throw new BusinessException("渠道信息不能为空");
        }
        if (!StringUtils.hasText(channel.getChannelName())) {
            throw new BusinessException("渠道名称不能为空");
        }
        if (!StringUtils.hasText(channel.getChannelCode())) {
            throw new BusinessException("渠道编码不能为空");
        }

        QueryWrapper codeQuery = QueryWrapper.create()
                .from(Channel.class)
                .where(CHANNEL.CHANNEL_CODE.eq(channel.getChannelCode()))
                .and(CHANNEL.DELETED.eq(0));
        if (channelMapper.selectCountByQuery(codeQuery) > 0) {
            throw new BusinessException("渠道编码已存在");
        }

        channel.setCreateTime(LocalDateTime.now());
        channel.setUpdateTime(LocalDateTime.now());
        channel.setDeleted(0);
        channelMapper.insert(channel);
    }

    @Transactional(rollbackFor = Exception.class)
    public void updateChannel(Channel channel) {
        if (channel == null || channel.getId() == null) {
            throw new BusinessException("渠道ID不能为空");
        }
        Channel existing = channelMapper.selectOneById(channel.getId());
        if (existing == null) {
            throw new BusinessException("渠道不存在");
        }
        channel.setUpdateTime(LocalDateTime.now());
        channelMapper.update(channel);
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteChannel(Long id) {
        if (id == null) {
            throw new BusinessException("渠道ID不能为空");
        }
        Channel channel = channelMapper.selectOneById(id);
        if (channel == null) {
            throw new BusinessException("渠道不存在");
        }
        channel.setDeleted(1);
        channel.setUpdateTime(LocalDateTime.now());
        channelMapper.update(channel);
    }

    public List<Channel> getAllChannels() {
        QueryWrapper query = QueryWrapper.create()
                .from(Channel.class)
                .where(CHANNEL.DELETED.eq(0))
                .orderBy(CHANNEL.CREATE_TIME.desc());
        return channelMapper.selectListByQuery(query);
    }

    public List<ChannelInventory> getChannelInventory(Long channelId, Long roomTypeId, LocalDate startDate, LocalDate endDate) {
        QueryWrapper query = QueryWrapper.create()
                .from(ChannelInventory.class)
                .where(CHANNEL_INVENTORY.CHANNEL_ID.eq(channelId))
                .and(CHANNEL_INVENTORY.ROOM_TYPE_ID.eq(roomTypeId))
                .and(CHANNEL_INVENTORY.DATE.ge(startDate))
                .and(CHANNEL_INVENTORY.DATE.le(endDate))
                .orderBy(CHANNEL_INVENTORY.DATE.asc());
        return channelInventoryMapper.selectListByQuery(query);
    }

    @Transactional(rollbackFor = Exception.class)
    public void setChannelInventory(Long channelId, Long roomTypeId, LocalDate startDate, LocalDate endDate, Integer allocatedRooms, Integer shareMode) {
        if (channelId == null || roomTypeId == null || startDate == null || endDate == null) {
            throw new BusinessException("渠道ID、房型ID和日期范围不能为空");
        }
        if (shareMode != null && shareMode == 0) {
            QueryWrapper poolQuery = QueryWrapper.create()
                    .from(RoomInventoryPool.class)
                    .where(ROOM_INVENTORY_POOL.ROOM_TYPE_ID.eq(roomTypeId))
                    .and(ROOM_INVENTORY_POOL.DATE.ge(startDate))
                    .and(ROOM_INVENTORY_POOL.DATE.le(endDate));
            List<RoomInventoryPool> pools = roomInventoryPoolMapper.selectListByQuery(poolQuery);
            for (RoomInventoryPool pool : pools) {
                if (allocatedRooms != null && pool.getAvailableRooms() != null && allocatedRooms > pool.getAvailableRooms()) {
                    throw new BusinessException("独占模式分配房数不能超过可用房数，日期：" + pool.getDate());
                }
            }
        }

        LocalDate current = startDate;
        while (!current.isAfter(endDate)) {
            QueryWrapper query = QueryWrapper.create()
                    .from(ChannelInventory.class)
                    .where(CHANNEL_INVENTORY.CHANNEL_ID.eq(channelId))
                    .and(CHANNEL_INVENTORY.ROOM_TYPE_ID.eq(roomTypeId))
                    .and(CHANNEL_INVENTORY.DATE.eq(current));
            ChannelInventory existing = channelInventoryMapper.selectOneByQuery(query);

            if (existing != null) {
                existing.setAllocatedRooms(allocatedRooms);
                existing.setShareMode(shareMode);
                existing.setUpdateTime(LocalDateTime.now());
                channelInventoryMapper.update(existing);
            } else {
                ChannelInventory inventory = new ChannelInventory();
                inventory.setChannelId(channelId);
                inventory.setRoomTypeId(roomTypeId);
                inventory.setDate(current);
                inventory.setAllocatedRooms(allocatedRooms);
                inventory.setUsedRooms(0);
                inventory.setShareMode(shareMode);
                inventory.setCreateTime(LocalDateTime.now());
                inventory.setUpdateTime(LocalDateTime.now());
                channelInventoryMapper.insert(inventory);
            }
            current = current.plusDays(1);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void batchSetChannelInventory(Long channelId, Long roomTypeId, LocalDate startDate, LocalDate endDate, Integer allocatedRooms, Integer shareMode) {
        setChannelInventory(channelId, roomTypeId, startDate, endDate, allocatedRooms, shareMode);
    }

    public List<ChannelPrice> getChannelPrice(Long channelId, Long roomTypeId, LocalDate startDate, LocalDate endDate) {
        QueryWrapper query = QueryWrapper.create()
                .from(ChannelPrice.class)
                .where(CHANNEL_PRICE.CHANNEL_ID.eq(channelId))
                .and(CHANNEL_PRICE.ROOM_TYPE_ID.eq(roomTypeId))
                .and(CHANNEL_PRICE.DATE.ge(startDate))
                .and(CHANNEL_PRICE.DATE.le(endDate))
                .orderBy(CHANNEL_PRICE.DATE.asc());
        return channelPriceMapper.selectListByQuery(query);
    }

    @Transactional(rollbackFor = Exception.class)
    public void setChannelPrice(Long channelId, Long roomTypeId, LocalDate startDate, LocalDate endDate, Integer priceMode, BigDecimal priceValue) {
        if (channelId == null || roomTypeId == null || startDate == null || endDate == null) {
            throw new BusinessException("渠道ID、房型ID和日期范围不能为空");
        }

        LocalDate current = startDate;
        while (!current.isAfter(endDate)) {
            RoomType roomType = roomTypeMapper.selectOneById(roomTypeId);
            BigDecimal finalPrice = calculateFinalPrice(priceMode, priceValue, roomType, current);

            QueryWrapper query = QueryWrapper.create()
                    .from(ChannelPrice.class)
                    .where(CHANNEL_PRICE.CHANNEL_ID.eq(channelId))
                    .and(CHANNEL_PRICE.ROOM_TYPE_ID.eq(roomTypeId))
                    .and(CHANNEL_PRICE.DATE.eq(current));
            ChannelPrice existing = channelPriceMapper.selectOneByQuery(query);

            if (existing != null) {
                existing.setPriceMode(priceMode);
                existing.setPriceValue(priceValue);
                existing.setFinalPrice(finalPrice);
                existing.setUpdateTime(LocalDateTime.now());
                channelPriceMapper.update(existing);
            } else {
                ChannelPrice price = new ChannelPrice();
                price.setChannelId(channelId);
                price.setRoomTypeId(roomTypeId);
                price.setDate(current);
                price.setPriceMode(priceMode);
                price.setPriceValue(priceValue);
                price.setFinalPrice(finalPrice);
                price.setCreateTime(LocalDateTime.now());
                price.setUpdateTime(LocalDateTime.now());
                channelPriceMapper.insert(price);
            }
            current = current.plusDays(1);
        }
    }

    private BigDecimal calculateFinalPrice(Integer priceMode, BigDecimal priceValue, RoomType roomType, LocalDate date) {
        if (priceMode == null || priceValue == null || roomType == null) {
            return BigDecimal.ZERO;
        }
        BigDecimal basePrice;
        DayOfWeek dayOfWeek = date.getDayOfWeek();
        if (dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY) {
            basePrice = roomType.getWeekendPrice() != null ? roomType.getWeekendPrice() : roomType.getBasePrice();
        } else {
            basePrice = roomType.getBasePrice() != null ? roomType.getBasePrice() : BigDecimal.ZERO;
        }

        switch (priceMode) {
            case 1:
                return priceValue;
            case 2:
                return basePrice.multiply(priceValue).setScale(2, RoundingMode.HALF_UP);
            case 3:
                return basePrice.add(priceValue).setScale(2, RoundingMode.HALF_UP);
            case 4:
                return basePrice.subtract(priceValue).setScale(2, RoundingMode.HALF_UP);
            default:
                return basePrice;
        }
    }

    public BigDecimal calculateChannelPrice(Long channelId, Long roomTypeId, LocalDate date) {
        if (channelId == null || roomTypeId == null || date == null) {
            return BigDecimal.ZERO;
        }
        QueryWrapper query = QueryWrapper.create()
                .from(ChannelPrice.class)
                .where(CHANNEL_PRICE.CHANNEL_ID.eq(channelId))
                .and(CHANNEL_PRICE.ROOM_TYPE_ID.eq(roomTypeId))
                .and(CHANNEL_PRICE.DATE.eq(date));
        ChannelPrice channelPrice = channelPriceMapper.selectOneByQuery(query);

        if (channelPrice != null && channelPrice.getFinalPrice() != null) {
            return channelPrice.getFinalPrice();
        }

        RoomType roomType = roomTypeMapper.selectOneById(roomTypeId);
        if (roomType == null) {
            return BigDecimal.ZERO;
        }
        DayOfWeek dayOfWeek = date.getDayOfWeek();
        if (dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY) {
            return roomType.getWeekendPrice() != null ? roomType.getWeekendPrice() : roomType.getBasePrice();
        }
        return roomType.getBasePrice() != null ? roomType.getBasePrice() : BigDecimal.ZERO;
    }

    public Map getChannelStatistics(LocalDate startDate, LocalDate endDate) {
        List<Channel> channels = getAllChannels();
        List<Map<String, Object>> channelStats = new ArrayList<>();
        BigDecimal totalRevenue = BigDecimal.ZERO;
        long totalBookings = 0;

        for (Channel channel : channels) {
            QueryWrapper bookingQuery = QueryWrapper.create()
                    .from(Booking.class)
                    .where(BOOKING.BOOKING_SOURCE.eq(channel.getChannelCode()))
                    .and(BOOKING.CREATE_TIME.ge(startDate.atStartOfDay()))
                    .and(BOOKING.CREATE_TIME.le(endDate.atTime(23, 59, 59)))
                    .and(BOOKING.DELETED.eq(0));
            List<Booking> bookings = bookingMapper.selectListByQuery(bookingQuery);
            long count = bookings.size();
            BigDecimal revenue = bookings.stream()
                    .map(b -> b.getTotalAmount() != null ? b.getTotalAmount() : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            totalBookings += count;
            totalRevenue = totalRevenue.add(revenue);

            Map<String, Object> stat = new HashMap<>();
            stat.put("channelId", channel.getId());
            stat.put("channelName", channel.getChannelName());
            stat.put("channelCode", channel.getChannelCode());
            stat.put("bookingCount", count);
            stat.put("revenue", revenue);
            channelStats.add(stat);
        }

        for (Map<String, Object> stat : channelStats) {
            BigDecimal revenue = (BigDecimal) stat.get("revenue");
            long count = (long) stat.get("bookingCount");
            stat.put("percentage", totalRevenue.compareTo(BigDecimal.ZERO) > 0
                    ? revenue.divide(totalRevenue, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100))
                    : BigDecimal.ZERO);
        }

        channelStats.sort((a, b) -> ((BigDecimal) b.get("revenue")).compareTo((BigDecimal) a.get("revenue")));
        for (int i = 0; i < channelStats.size(); i++) {
            channelStats.get(i).put("ranking", i + 1);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("totalBookings", totalBookings);
        result.put("totalRevenue", totalRevenue);
        result.put("channelStats", channelStats);
        return result;
    }

    public List<Map> getChannelTrend(Long channelId, LocalDate startDate, LocalDate endDate) {
        Channel channel = channelMapper.selectOneById(channelId);
        if (channel == null) {
            throw new BusinessException("渠道不存在");
        }

        List<Map> result = new ArrayList<>();
        LocalDate current = startDate;
        while (!current.isAfter(endDate)) {
            QueryWrapper query = QueryWrapper.create()
                    .from(Booking.class)
                    .where(BOOKING.BOOKING_SOURCE.eq(channel.getChannelCode()))
                    .and(BOOKING.CHECK_IN_DATE.le(current))
                    .and(BOOKING.CHECK_OUT_DATE.gt(current))
                    .and(BOOKING.DELETED.eq(0));
            long count = bookingMapper.selectCountByQuery(query);

            Map<String, Object> item = new HashMap<>();
            item.put("date", current);
            item.put("bookingCount", count);
            result.add(item);
            current = current.plusDays(1);
        }
        return result;
    }

    public List<Map> getChannelEfficiency(LocalDate startDate, LocalDate endDate) {
        List<Channel> channels = getAllChannels();
        List<Map> result = new ArrayList<>();

        for (Channel channel : channels) {
            QueryWrapper inventoryQuery = QueryWrapper.create()
                    .from(ChannelInventory.class)
                    .where(CHANNEL_INVENTORY.CHANNEL_ID.eq(channel.getId()))
                    .and(CHANNEL_INVENTORY.DATE.ge(startDate))
                    .and(CHANNEL_INVENTORY.DATE.le(endDate));
            List<ChannelInventory> inventories = channelInventoryMapper.selectListByQuery(inventoryQuery);

            int totalAllocated = inventories.stream()
                    .mapToInt(i -> i.getAllocatedRooms() != null ? i.getAllocatedRooms() : 0)
                    .sum();

            QueryWrapper bookingQuery = QueryWrapper.create()
                    .from(Booking.class)
                    .where(BOOKING.BOOKING_SOURCE.eq(channel.getChannelCode()))
                    .and(BOOKING.CHECK_IN_DATE.ge(startDate))
                    .and(BOOKING.CHECK_IN_DATE.le(endDate))
                    .and(BOOKING.DELETED.eq(0));
            long bookingCount = bookingMapper.selectCountByQuery(bookingQuery);

            BigDecimal efficiency = totalAllocated > 0
                    ? BigDecimal.valueOf(bookingCount).divide(BigDecimal.valueOf(totalAllocated), 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100))
                    : BigDecimal.ZERO;

            Map<String, Object> item = new HashMap<>();
            item.put("channelId", channel.getId());
            item.put("channelName", channel.getChannelName());
            item.put("totalAllocated", totalAllocated);
            item.put("bookingCount", bookingCount);
            item.put("efficiency", efficiency);
            result.add(item);
        }
        return result;
    }
}
