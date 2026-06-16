<template>
  <div class="booking-calendar-container">
    <div class="page-header">
      <h2 class="page-title">预订日历视图</h2>
      <div class="header-actions">
        <el-date-picker
          v-model="currentDate"
          type="month"
          format="YYYY 年 MM 月"
          value-format="YYYY-MM"
          placeholder="选择月份"
          @change="loadCalendarData"
          size="default"
        />
      </div>
    </div>

    <el-card class="calendar-card" shadow="hover">
      <div class="calendar-header">
        <div
          v-for="day in weekDays"
          :key="day"
          class="calendar-header-cell"
          :class="{ 'weekend': day === '六' || day === '日' }"
        >
          {{ day }}
        </div>
      </div>
      <div class="calendar-body">
        <div
          v-for="(week, weekIndex) in calendarData"
          :key="weekIndex"
          class="calendar-week"
        >
          <div
            v-for="(day, dayIndex) in week"
            :key="dayIndex"
            class="calendar-day"
            :class="getDayClass(day)"
            @click="day.date && openDayDetail(day)"
          >
            <template v-if="day.date">
              <div class="day-header">
                <span class="day-number" :class="{ 'today': day.isToday }">{{ day.day }}</span>
                <span class="day-rate">{{ (day.occupancyRate * 100).toFixed(0) }}%</span>
              </div>
              <div class="day-stats">
                <div class="stat-item">
                  <span class="stat-label">入住</span>
                  <span class="stat-value checkin">{{ day.checkinCount || 0 }}</span>
                </div>
                <div class="stat-item">
                  <span class="stat-label">退房</span>
                  <span class="stat-value checkout">{{ day.checkoutCount || 0 }}</span>
                </div>
                <div class="stat-item">
                  <span class="stat-label">在住</span>
                  <span class="stat-value occupied">{{ day.occupiedCount || 0 }}</span>
                </div>
              </div>
            </template>
          </div>
        </div>
      </div>
    </el-card>

    <div class="legend">
      <div class="legend-item">
        <span class="legend-color high"></span>
        <span>出租率 > 80%</span>
      </div>
      <div class="legend-item">
        <span class="legend-color medium"></span>
        <span>出租率 50% - 80%</span>
      </div>
      <div class="legend-item">
        <span class="legend-color low"></span>
        <span>出租率 < 50%</span>
      </div>
    </div>

    <el-dialog
      v-model="dayDetailVisible"
      :title="`${selectedDate} 预订详情`"
      width="900px"
      :close-on-click-modal="false"
    >
      <el-table :data="dayBookings" stripe style="width: 100%" v-loading="loadingDayDetail">
        <el-table-column prop="bookingNo" label="预订单号" min-width="140" />
        <el-table-column label="客户信息" min-width="150">
          <template #default="{ row }">
            <div>{{ row.customerName || '-' }}</div>
            <div class="sub-text">{{ row.customerPhone || '-' }}</div>
          </template>
        </el-table-column>
        <el-table-column prop="roomNo" label="房间号" width="100" align="center" />
        <el-table-column prop="roomTypeName" label="房型" min-width="100" />
        <el-table-column label="入住日期" width="120" align="center">
          <template #default="{ row }">
            {{ row.checkinDate ? row.checkinDate.substring(0, 10) : '-' }}
          </template>
        </el-table-column>
        <el-table-column label="退房日期" width="120" align="center">
          <template #default="{ row }">
            {{ row.checkoutDate ? row.checkoutDate.substring(0, 10) : '-' }}
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="getStatusTagType(row.status)" size="small">
              {{ getStatusLabel(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="100" align="center">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="goToDetail(row.bookingId)">
              详情
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      <template #footer>
        <el-button @click="dayDetailVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Calendar } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import api from '@/api'

const router = useRouter()
const userStore = useUserStore()
const hasPermission = (p) => userStore.hasPermission(p)

const weekDays = ['一', '二', '三', '四', '五', '六', '日']
const statusLabels = { 1: '待确认', 2: '已确认', 3: '已入住', 4: '已完成', 5: '已取消' }

const currentDate = ref('')
const calendarData = ref([])
const dayDetailVisible = ref(false)
const selectedDate = ref('')
const selectedDayData = ref(null)
const dayBookings = ref([])
const loadingDayDetail = ref(false)

const initCurrentDate = () => {
  const now = new Date()
  const year = now.getFullYear()
  const month = String(now.getMonth() + 1).padStart(2, '0')
  currentDate.value = `${year}-${month}`
}

const getYearMonth = () => {
  if (!currentDate.value) {
    const now = new Date()
    return { year: now.getFullYear(), month: now.getMonth() + 1 }
  }
  const [year, month] = currentDate.value.split('-').map(Number)
  return { year, month }
}

const generateCalendarStructure = (year, month, apiData = []) => {
  const firstDay = new Date(year, month - 1, 1)
  const lastDay = new Date(year, month, 0)
  const daysInMonth = lastDay.getDate()

  let firstDayOfWeek = firstDay.getDay()
  firstDayOfWeek = firstDayOfWeek === 0 ? 6 : firstDayOfWeek - 1

  const today = new Date()
  const todayStr = `${today.getFullYear()}-${String(today.getMonth() + 1).padStart(2, '0')}-${String(today.getDate()).padStart(2, '0')}`

  const apiDataMap = {}
  apiData.forEach(item => {
    apiDataMap[item.date] = item
  })

  const calendar = []
  let currentWeek = []

  for (let i = 0; i < firstDayOfWeek; i++) {
    currentWeek.push({ date: null })
  }

  for (let day = 1; day <= daysInMonth; day++) {
    const dateStr = `${year}-${String(month).padStart(2, '0')}-${String(day).padStart(2, '0')}`
    const dayOfWeek = new Date(year, month - 1, day).getDay()
    const isWeekend = dayOfWeek === 0 || dayOfWeek === 6
    const isToday = dateStr === todayStr

    const dayData = apiDataMap[dateStr] || {}

    currentWeek.push({
      date: dateStr,
      day,
      year,
      month,
      isWeekend,
      isToday,
      checkinCount: dayData.checkinCount || 0,
      checkoutCount: dayData.checkoutCount || 0,
      occupiedCount: dayData.occupiedCount || 0,
      occupancyRate: dayData.occupancyRate || 0,
      totalRooms: dayData.totalRooms || 0
    })

    if (currentWeek.length === 7) {
      calendar.push(currentWeek)
      currentWeek = []
    }
  }

  if (currentWeek.length > 0) {
    while (currentWeek.length < 7) {
      currentWeek.push({ date: null })
    }
    calendar.push(currentWeek)
  }

  return calendar
}

const getDayClass = (day) => {
  if (!day.date) return 'empty'
  const classes = []
  if (day.isWeekend) classes.push('weekend')
  if (day.isToday) classes.push('today')

  const rate = day.occupancyRate * 100
  if (rate > 80) {
    classes.push('high-occupancy')
  } else if (rate >= 50) {
    classes.push('medium-occupancy')
  } else {
    classes.push('low-occupancy')
  }

  return classes.join(' ')
}

const loadCalendarData = async () => {
  try {
    const { year, month } = getYearMonth()
    const res = await api.booking.stats.calendar({ year, month })
    if (res.code === 200) {
      const data = res.data || []
      calendarData.value = generateCalendarStructure(year, month, data)
    }
  } catch {
    const { year, month } = getYearMonth()
    calendarData.value = generateCalendarStructure(year, month, [])
  }
}

const openDayDetail = async (day) => {
  if (!hasPermission('booking:calendar:view')) {
    ElMessage.warning('没有权限查看详情')
    return
  }
  selectedDate.value = day.date
  selectedDayData.value = day
  dayDetailVisible.value = true
  dayBookings.value = []
  loadingDayDetail.value = true
  try {
    const res = await api.booking.stats.day(day.date)
    if (res.code === 200) {
      dayBookings.value = res.data || []
    }
  } catch {
    dayBookings.value = []
  } finally {
    loadingDayDetail.value = false
  }
}

const getStatusLabel = (status) => {
  return statusLabels[status] || '未知'
}

const getStatusTagType = (status) => {
  const types = { 1: 'warning', 2: 'primary', 3: 'success', 4: 'info', 5: 'danger' }
  return types[status] || 'info'
}

const goToDetail = (id) => {
  if (id) {
    router.push(`/booking/detail/${id}`)
  }
}

onMounted(() => {
  initCurrentDate()
  nextTick(() => {
    loadCalendarData()
  })
})

watch(currentDate, () => {
  loadCalendarData()
})
</script>

<style scoped>
.booking-calendar-container {
  padding: 10px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.page-title {
  font-size: 20px;
  font-weight: 600;
  color: #2d3748;
  margin: 0;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 12px;
}

.calendar-card {
  border-radius: 12px;
  border: none;
  padding: 16px;
}

.calendar-header {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  gap: 8px;
  margin-bottom: 12px;
}

.calendar-header-cell {
  text-align: center;
  padding: 12px;
  font-weight: 600;
  color: #4a5568;
  background: #f7fafc;
  border-radius: 8px;
}

.calendar-header-cell.weekend {
  color: #a0aec0;
  background: #edf2f7;
}

.calendar-body {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.calendar-week {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  gap: 8px;
}

.calendar-day {
  min-height: 120px;
  padding: 12px;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s ease;
  border: 2px solid transparent;
  display: flex;
  flex-direction: column;
}

.calendar-day.empty {
  background: transparent;
  cursor: default;
  border: none;
}

.calendar-day:not(.empty):hover {
  transform: translateY(-3px);
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.15);
}

.calendar-day.low-occupancy {
  background: linear-gradient(135deg, #f0fff4 0%, #c6f6d5 100%);
  border-color: #9ae6b4;
}

.calendar-day.medium-occupancy {
  background: linear-gradient(135deg, #fffbeb 0%, #feebc8 100%);
  border-color: #fbd38d;
}

.calendar-day.high-occupancy {
  background: linear-gradient(135deg, #fff5f5 0%, #fed7d7 100%);
  border-color: #fc8181;
}

.calendar-day.weekend:not(.empty) {
  opacity: 0.85;
}

.calendar-day.today:not(.empty) {
  border-color: #409eff;
  border-width: 3px;
}

.day-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.day-number {
  font-size: 18px;
  font-weight: 700;
  color: #2d3748;
}

.day-number.today {
  color: #409eff;
}

.day-rate {
  font-size: 14px;
  font-weight: 600;
  padding: 2px 8px;
  border-radius: 12px;
  background: rgba(255, 255, 255, 0.7);
}

.low-occupancy .day-rate {
  color: #2f855a;
}

.medium-occupancy .day-rate {
  color: #c05621;
}

.high-occupancy .day-rate {
  color: #c53030;
}

.day-stats {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.stat-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 12px;
}

.stat-label {
  color: #718096;
}

.stat-value {
  font-weight: 600;
}

.stat-value.checkin {
  color: #67c23a;
}

.stat-value.checkout {
  color: #f56c6c;
}

.stat-value.occupied {
  color: #409eff;
}

.legend {
  display: flex;
  justify-content: center;
  gap: 32px;
  margin-top: 20px;
  padding: 16px;
  background: #f7fafc;
  border-radius: 8px;
}

.legend-item {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
  color: #4a5568;
}

.legend-color {
  width: 20px;
  height: 20px;
  border-radius: 4px;
}

.legend-color.high {
  background: linear-gradient(135deg, #fff5f5 0%, #fed7d7 100%);
  border: 2px solid #fc8181;
}

.legend-color.medium {
  background: linear-gradient(135deg, #fffbeb 0%, #feebc8 100%);
  border: 2px solid #fbd38d;
}

.legend-color.low {
  background: linear-gradient(135deg, #f0fff4 0%, #c6f6d5 100%);
  border: 2px solid #9ae6b4;
}

.sub-text {
  font-size: 12px;
  color: #718096;
  margin-top: 2px;
}
</style>
