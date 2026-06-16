<template>
  <div class="booking-gantt-container">
    <el-card class="filter-card" shadow="never">
      <el-row :gutter="16" align="middle">
        <el-col :xs="24" :sm="8" :md="6">
          <el-date-picker
            v-model="queryForm.dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            style="width: 100%"
          />
        </el-col>
        <el-col :xs="8" :sm="4" :md="3">
          <el-select v-model="queryForm.roomTypeId" placeholder="房型" clearable style="width: 100%">
            <el-option v-for="rt in roomTypes" :key="rt.typeId" :label="rt.typeName" :value="rt.typeId" />
          </el-select>
        </el-col>
        <el-col :xs="8" :sm="4" :md="3">
          <el-select v-model="queryForm.floorId" placeholder="楼层" clearable style="width: 100%">
            <el-option v-for="f in floors" :key="f.floorId" :label="f.floorName" :value="f.floorId" />
          </el-select>
        </el-col>
        <el-col :xs="8" :sm="4" :md="3">
          <el-button type="primary" @click="handleQuery" :loading="loading">查询</el-button>
        </el-col>
      </el-row>
    </el-card>

    <el-card class="gantt-card" shadow="hover">
      <div class="gantt-wrapper" v-if="dates.length > 0 && rooms.length > 0">
        <div class="gantt-grid">
          <div class="gantt-header">
            <div class="gantt-corner">房号/日期</div>
            <div class="gantt-date-row">
              <div
                v-for="date in dates"
                :key="date"
                class="gantt-date-cell"
              >
                {{ formatDate(date) }}
              </div>
            </div>
          </div>
          <div class="gantt-body">
            <div
              v-for="room in rooms"
              :key="room.roomId"
              class="gantt-row"
            >
              <div class="gantt-room-label">{{ room.roomNo }}</div>
              <div class="gantt-bar-track">
                <div class="gantt-date-lines">
                  <div
                    v-for="date in dates"
                    :key="date"
                    class="gantt-date-line"
                  ></div>
                </div>
                <div
                  v-for="booking in getRoomBookings(room.roomId)"
                  :key="booking.bookingId"
                  class="gantt-bar"
                  :style="getBarStyle(booking)"
                  :class="'bar-status-' + booking.status"
                  @mouseenter="showTooltip($event, booking)"
                  @mouseleave="hideTooltip"
                  @click="showBookingDetail(booking)"
                >
                  <span class="gantt-bar-text">{{ booking.customerName }}</span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
      <el-empty v-else description="暂无数据" />

      <div class="legend-bar">
        <div class="legend-item">
          <span class="legend-dot" style="background-color: #909399"></span>
          <span>待确认</span>
        </div>
        <div class="legend-item">
          <span class="legend-dot" style="background-color: #409EFF"></span>
          <span>已确认</span>
        </div>
        <div class="legend-item">
          <span class="legend-dot" style="background-color: #E6A23C"></span>
          <span>已入住</span>
        </div>
        <div class="legend-item">
          <span class="legend-dot" style="background-color: #67C23A"></span>
          <span>已完成</span>
        </div>
        <div class="legend-item">
          <span class="legend-dot" style="background-color: #F56C6C"></span>
          <span>已取消</span>
        </div>
      </div>
    </el-card>

    <div v-if="tooltipVisible" class="custom-tooltip" :style="tooltipStyle">
      <div>预订号: {{ tooltipData.bookingNo }}</div>
      <div>客户: {{ tooltipData.customerName }}</div>
      <div>入住: {{ tooltipData.checkInDate }}</div>
      <div>退房: {{ tooltipData.checkOutDate }}</div>
      <div>状态: {{ bookingStatusLabels[tooltipData.status] }}</div>
    </div>

    <el-dialog
      v-model="detailDialogVisible"
      title="预订详情"
      width="600px"
    >
      <el-descriptions :column="2" border>
        <el-descriptions-item label="预订号">{{ detailData.bookingNo }}</el-descriptions-item>
        <el-descriptions-item label="客户姓名">{{ detailData.customerName }}</el-descriptions-item>
        <el-descriptions-item label="房间号">{{ detailData.roomNo }}</el-descriptions-item>
        <el-descriptions-item label="房型">{{ detailData.roomTypeName }}</el-descriptions-item>
        <el-descriptions-item label="入住日期">{{ detailData.checkInDate }}</el-descriptions-item>
        <el-descriptions-item label="退房日期">{{ detailData.checkOutDate }}</el-descriptions-item>
        <el-descriptions-item label="预订状态">{{ bookingStatusLabels[detailData.status] }}</el-descriptions-item>
        <el-descriptions-item label="总价">¥{{ detailData.totalPrice }}</el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button @click="detailDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount } from 'vue'
import * as echarts from 'echarts'
import api from '@/api'

const bookingStatusLabels = { 1: '待确认', 2: '已确认', 3: '已入住', 4: '已完成', 5: '已取消' }
const barStatusColors = { 1: '#909399', 2: '#409EFF', 3: '#E6A23C', 4: '#67C23A', 5: '#F56C6C' }

const loading = ref(false)
const queryForm = ref({
  dateRange: [],
  roomTypeId: null,
  floorId: null
})

const roomTypes = ref([])
const floors = ref([])
const dates = ref([])
const rooms = ref([])
const bookings = ref([])

const tooltipVisible = ref(false)
const tooltipStyle = ref({})
const tooltipData = ref({})

const detailDialogVisible = ref(false)
const detailData = ref({})

const getDefaultDateRange = () => {
  const end = new Date()
  const start = new Date()
  start.setDate(start.getDate() + 13)
  return [end, start]
}

const formatDate = (dateStr) => {
  if (!dateStr) return ''
  const d = new Date(dateStr)
  return `${d.getMonth() + 1}/${d.getDate()}`
}

const getRoomBookings = (roomId) => {
  return bookings.value.filter(b => b.roomId === roomId)
}

const getBarStyle = (booking) => {
  if (dates.value.length === 0) return {}
  const startDate = dates.value[0]
  const endDate = dates.value[dates.value.length - 1]
  const totalDays = daysBetween(startDate, endDate) + 1
  const offsetDays = daysBetween(startDate, booking.checkInDate)
  const durationDays = daysBetween(booking.checkInDate, booking.checkOutDate)
  const left = Math.max(0, (offsetDays / totalDays) * 100)
  const width = Math.max(2, (durationDays / totalDays) * 100)
  return {
    left: left + '%',
    width: width + '%',
    backgroundColor: barStatusColors[booking.status] || '#909399'
  }
}

const daysBetween = (d1, d2) => {
  const date1 = new Date(d1)
  const date2 = new Date(d2)
  return Math.round((date2 - date1) / (1000 * 60 * 60 * 24))
}

const loadOptions = async () => {
  try {
    const [rtRes, buildingRes] = await Promise.all([
      api.hotel.getRoomTypes(),
      api.hotel.getBuildings()
    ])
    if (rtRes.code === 200) roomTypes.value = rtRes.data || []
    if (buildingRes.code === 200) {
      const buildings = buildingRes.data || []
      const allFloors = []
      for (const b of buildings) {
        const fRes = await api.hotel.getFloors(b.buildingId)
        if (fRes.code === 200) {
          allFloors.push(...(fRes.data || []))
        }
      }
      floors.value = allFloors
    }
  } catch {}
}

const loadData = async () => {
  const params = {}
  if (queryForm.value.dateRange && queryForm.value.dateRange.length === 2) {
    params.startDate = queryForm.value.dateRange[0]
    params.endDate = queryForm.value.dateRange[1]
  } else {
    const range = getDefaultDateRange()
    params.startDate = range[0]
    params.endDate = range[1]
  }
  if (queryForm.value.roomTypeId) params.roomTypeId = queryForm.value.roomTypeId
  if (queryForm.value.floorId) params.floorId = queryForm.value.floorId

  try {
    loading.value = true
    const res = await api.visual.gantt(params)
    if (res.code === 200 && res.data) {
      dates.value = res.data.dates || []
      rooms.value = res.data.rooms || []
      bookings.value = res.data.bookings || []
    }
  } catch {
  } finally {
    loading.value = false
  }
}

const showTooltip = (event, booking) => {
  tooltipData.value = {
    bookingNo: booking.bookingNo,
    customerName: booking.customerName,
    checkInDate: booking.checkInDate,
    checkOutDate: booking.checkOutDate,
    status: booking.status
  }
  const rect = event.target.getBoundingClientRect()
  tooltipStyle.value = {
    left: rect.left + rect.width / 2 + 'px',
    top: rect.top - 10 + 'px',
    transform: 'translate(-50%, -100%)'
  }
  tooltipVisible.value = true
}

const hideTooltip = () => {
  tooltipVisible.value = false
}

const showBookingDetail = (booking) => {
  detailData.value = { ...booking }
  detailDialogVisible.value = true
}

const handleQuery = () => {
  loadData()
}

onMounted(() => {
  queryForm.value.dateRange = getDefaultDateRange()
  loadOptions()
  loadData()
})
</script>

<style scoped>
.booking-gantt-container {
  padding: 10px;
  position: relative;
}

.filter-card {
  margin-bottom: 20px;
  border-radius: 12px;
  border: none;
}

.gantt-card {
  border-radius: 12px;
  border: none;
}

.gantt-wrapper {
  overflow-x: auto;
}

.gantt-grid {
  min-width: 100%;
}

.gantt-header {
  display: flex;
  position: sticky;
  top: 0;
  z-index: 2;
}

.gantt-corner {
  min-width: 80px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 600;
  font-size: 13px;
  color: #2d3748;
  border-bottom: 1px solid #ebeef5;
  border-right: 1px solid #ebeef5;
  background: #f5f7fa;
  position: sticky;
  left: 0;
  z-index: 3;
  flex-shrink: 0;
}

.gantt-date-row {
  display: flex;
  flex: 1;
}

.gantt-date-cell {
  min-width: 50px;
  flex: 1;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  color: #718096;
  border-bottom: 1px solid #ebeef5;
  border-right: 1px solid #ebeef5;
  background: #f5f7fa;
}

.gantt-body {
  display: flex;
  flex-direction: column;
}

.gantt-row {
  display: flex;
  height: 40px;
}

.gantt-room-label {
  min-width: 80px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 13px;
  font-weight: 500;
  color: #2d3748;
  border-bottom: 1px solid #ebeef5;
  border-right: 1px solid #ebeef5;
  background: #fafafa;
  position: sticky;
  left: 0;
  z-index: 1;
  flex-shrink: 0;
}

.gantt-bar-track {
  flex: 1;
  position: relative;
  height: 40px;
  border-bottom: 1px solid #ebeef5;
}

.gantt-date-lines {
  display: flex;
  height: 100%;
  width: 100%;
  position: absolute;
  top: 0;
  left: 0;
}

.gantt-date-line {
  flex: 1;
  border-right: 1px solid #f0f0f0;
}

.gantt-bar {
  position: absolute;
  top: 6px;
  height: 28px;
  border-radius: 4px;
  cursor: pointer;
  display: flex;
  align-items: center;
  padding: 0 8px;
  z-index: 1;
  transition: opacity 0.2s;
  overflow: hidden;
  white-space: nowrap;
}

.gantt-bar:hover {
  opacity: 0.8;
}

.gantt-bar-text {
  font-size: 11px;
  color: #fff;
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.2);
  overflow: hidden;
  text-overflow: ellipsis;
}

.legend-bar {
  display: flex;
  gap: 24px;
  padding: 16px 0 4px;
  justify-content: center;
  flex-wrap: wrap;
}

.legend-item {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: #718096;
}

.legend-dot {
  width: 14px;
  height: 14px;
  border-radius: 3px;
}

.custom-tooltip {
  position: fixed;
  z-index: 9999;
  background: #303133;
  color: #fff;
  padding: 8px 12px;
  border-radius: 6px;
  font-size: 13px;
  line-height: 1.6;
  pointer-events: none;
  white-space: nowrap;
}
</style>
