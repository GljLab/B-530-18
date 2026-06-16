<template>
  <div class="room-status-calendar-container">
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

    <el-card class="grid-card" shadow="hover">
      <div class="grid-wrapper" v-if="dates.length > 0 && rooms.length > 0">
        <div class="grid-table">
          <div class="grid-header">
            <div class="grid-corner">房号/日期</div>
            <div
              v-for="date in dates"
              :key="date"
              class="grid-date-cell"
            >
              {{ formatDate(date) }}
            </div>
          </div>
          <div
            v-for="room in rooms"
            :key="room.roomId"
            class="grid-row"
          >
            <div class="grid-room-cell">{{ room.roomNo }}</div>
            <div
              v-for="date in dates"
              :key="`${room.roomId}-${date}`"
              class="grid-cell"
              :style="{ backgroundColor: getStatusColor(getRoomDateStatus(room.roomId, date)) }"
              @mouseenter="showTooltip($event, room, date)"
              @mouseleave="hideTooltip"
              @click="handleCellClick(room, date)"
            ></div>
          </div>
        </div>
      </div>
      <el-empty v-else description="暂无数据" />

      <div class="legend-bar">
        <div class="legend-item">
          <span class="legend-dot" style="background-color: #67C23A"></span>
          <span>空闲</span>
        </div>
        <div class="legend-item">
          <span class="legend-dot" style="background-color: #409EFF"></span>
          <span>已预订</span>
        </div>
        <div class="legend-item">
          <span class="legend-dot" style="background-color: #E6A23C"></span>
          <span>已入住</span>
        </div>
        <div class="legend-item">
          <span class="legend-dot" style="background-color: #F56C6C"></span>
          <span>维修中</span>
        </div>
        <div class="legend-item">
          <span class="legend-dot" style="background-color: #C0C4CC"></span>
          <span>停用</span>
        </div>
      </div>
    </el-card>

    <div v-if="tooltipVisible" class="custom-tooltip" :style="tooltipStyle">
      <div>房号: {{ tooltipData.roomNo }}</div>
      <div>房型: {{ tooltipData.roomTypeName }}</div>
      <div>状态: {{ statusLabels[tooltipData.status] }}</div>
      <div>日期: {{ tooltipData.date }}</div>
    </div>

    <el-dialog
      v-model="detailDialogVisible"
      :title="detailData.roomNo + ' - 房间详情'"
      width="500px"
    >
      <el-descriptions :column="1" border>
        <el-descriptions-item label="房号">{{ detailData.roomNo }}</el-descriptions-item>
        <el-descriptions-item label="房型">{{ detailData.roomTypeName }}</el-descriptions-item>
        <el-descriptions-item label="楼层">{{ detailData.floorName }}</el-descriptions-item>
        <el-descriptions-item label="当前状态">{{ statusLabels[detailData.status] }}</el-descriptions-item>
        <el-descriptions-item label="日期">{{ detailData.date }}</el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button @click="detailDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount } from 'vue'
import { useRouter } from 'vue-router'
import * as echarts from 'echarts'
import api from '@/api'

const router = useRouter()

const statusLabels = { 1: '空闲', 2: '已预订', 3: '已入住', 6: '维修中', 7: '停用' }
const statusColorMap = { 1: '#67C23A', 2: '#409EFF', 3: '#E6A23C', 4: '#909399', 5: '#909399', 6: '#F56C6C', 7: '#C0C4CC' }

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
const statusMap = ref({})

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

const getStatusColor = (status) => {
  return statusColorMap[status] || '#909399'
}

const getRoomDateStatus = (roomId, date) => {
  const key = `${roomId}_${date}`
  return statusMap.value[key] || 1
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
    const res = await api.visual.roomStatus(params)
    if (res.code === 200 && res.data) {
      dates.value = res.data.dates || []
      rooms.value = res.data.rooms || []
      const map = {}
      const statuses = res.data.statuses || []
      statuses.forEach(s => {
        map[`${s.roomId}_${s.date}`] = s.status
      })
      statusMap.value = map
    }
  } catch {
  } finally {
    loading.value = false
  }
}

const showTooltip = (event, room, date) => {
  const status = getRoomDateStatus(room.roomId, date)
  tooltipData.value = {
    roomNo: room.roomNo,
    roomTypeName: room.roomTypeName || '',
    status: status,
    date: date
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

const handleCellClick = (room, date) => {
  const status = getRoomDateStatus(room.roomId, date)
  detailData.value = {
    roomNo: room.roomNo,
    roomTypeName: room.roomTypeName || '',
    floorName: room.floorName || '',
    status: status,
    date: date
  }
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
.room-status-calendar-container {
  padding: 10px;
  position: relative;
}

.filter-card {
  margin-bottom: 20px;
  border-radius: 12px;
  border: none;
}

.grid-card {
  border-radius: 12px;
  border: none;
}

.grid-wrapper {
  overflow-x: auto;
}

.grid-table {
  display: table;
  min-width: 100%;
}

.grid-header {
  display: flex;
  position: sticky;
  top: 0;
  z-index: 2;
  background: #f5f7fa;
}

.grid-corner {
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
}

.grid-date-cell {
  min-width: 50px;
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

.grid-row {
  display: flex;
}

.grid-room-cell {
  min-width: 80px;
  height: 36px;
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
}

.grid-cell {
  min-width: 50px;
  height: 36px;
  border-bottom: 1px solid #ebeef5;
  border-right: 1px solid #ebeef5;
  cursor: pointer;
  transition: opacity 0.2s;
}

.grid-cell:hover {
  opacity: 0.7;
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
