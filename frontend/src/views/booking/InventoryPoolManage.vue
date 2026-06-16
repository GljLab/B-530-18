<template>
  <div class="inventory-pool-container">
    <div class="page-header">
      <h2 class="page-title">房量池管理</h2>
      <div class="header-actions">
        <el-select v-model="selectedRoomType" placeholder="选择房型" clearable style="width: 180px" @change="loadPoolData">
          <el-option v-for="rt in roomTypes" :key="rt.id" :label="rt.typeName" :value="rt.id" />
        </el-select>
        <el-date-picker
          v-model="currentMonth"
          type="month"
          format="YYYY 年 MM 月"
          value-format="YYYY-MM"
          placeholder="选择月份"
          style="width: 180px"
          @change="loadPoolData"
        />
        <el-button type="primary" @click="openBatchDialog">
          <el-icon><Setting /></el-icon>批量设置
        </el-button>
      </div>
    </div>

    <el-card shadow="hover" class="calendar-card">
      <div class="calendar-header">
        <div v-for="day in weekDays" :key="day" class="calendar-header-cell">{{ day }}</div>
      </div>
      <div class="calendar-body">
        <div v-for="(week, wi) in calendarGrid" :key="wi" class="calendar-week">
          <div
            v-for="(cell, di) in week"
            :key="di"
            class="calendar-day"
            :class="getCellClass(cell)"
            @click="cell.date && openEditDialog(cell)"
          >
            <template v-if="cell.date">
              <div class="day-header">
                <span class="day-number" :class="{ today: cell.isToday }">{{ cell.day }}</span>
              </div>
              <div class="day-stats">
                <div class="stat-row">
                  <span class="stat-label">总房量</span>
                  <span class="stat-value">{{ cell.totalRooms }}</span>
                </div>
                <div class="stat-row">
                  <span class="stat-label">可售</span>
                  <span class="stat-value available">{{ cell.availableRooms }}</span>
                </div>
                <div class="stat-row">
                  <span class="stat-label">预留</span>
                  <span class="stat-value reserved">{{ cell.reservedRooms }}</span>
                </div>
              </div>
            </template>
          </div>
        </div>
      </div>
    </el-card>

    <div class="legend">
      <div class="legend-item">
        <span class="legend-color green"></span>
        <span>可售 > 50%</span>
      </div>
      <div class="legend-item">
        <span class="legend-color yellow"></span>
        <span>可售 20%-50%</span>
      </div>
      <div class="legend-item">
        <span class="legend-color red"></span>
        <span>可售 < 20%</span>
      </div>
    </div>

    <el-dialog v-model="editDialogVisible" :title="`设置房量 - ${editForm.date}`" width="440px" destroy-on-close>
      <el-form ref="editFormRef" :model="editForm" :rules="editRules" label-width="80px">
        <el-form-item label="日期">
          <span>{{ editForm.date }}</span>
        </el-form-item>
        <el-form-item label="总房量" prop="totalRooms">
          <el-input-number v-model="editForm.totalRooms" :min="0" style="width: 100%" />
        </el-form-item>
        <el-form-item label="可售房量" prop="availableRooms">
          <el-input-number v-model="editForm.availableRooms" :min="0" style="width: 100%" />
        </el-form-item>
        <el-form-item label="预留房量" prop="reservedRooms">
          <el-input-number v-model="editForm.reservedRooms" :min="0" style="width: 100%" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="editSaving" @click="handleEditSave">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="batchDialogVisible" title="批量设置房量" width="520px" destroy-on-close>
      <el-form ref="batchFormRef" :model="batchForm" :rules="batchRules" label-width="100px">
        <el-form-item label="房型" prop="roomTypeId">
          <el-select v-model="batchForm.roomTypeId" placeholder="选择房型" style="width: 100%">
            <el-option v-for="rt in roomTypes" :key="rt.id" :label="rt.typeName" :value="rt.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="日期范围" prop="dateRange">
          <el-date-picker
            v-model="batchForm.dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            value-format="YYYY-MM-DD"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="总房量" prop="totalRooms">
          <el-input-number v-model="batchForm.totalRooms" :min="0" style="width: 100%" />
        </el-form-item>
        <el-form-item label="可售房量" prop="availableRooms">
          <el-input-number v-model="batchForm.availableRooms" :min="0" style="width: 100%" />
        </el-form-item>
        <el-form-item label="预留房量" prop="reservedRooms">
          <el-input-number v-model="batchForm.reservedRooms" :min="0" style="width: 100%" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="batchDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="batchSaving" @click="handleBatchSave">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Setting } from '@element-plus/icons-vue'
import api from '@/api'

const weekDays = ['一', '二', '三', '四', '五', '六', '日']

const roomTypes = ref([])
const selectedRoomType = ref(null)
const currentMonth = ref('')
const poolData = ref([])
const calendarGrid = ref([])

const editDialogVisible = ref(false)
const editSaving = ref(false)
const editFormRef = ref(null)
const editForm = reactive({
  date: '',
  roomTypeId: null,
  totalRooms: 0,
  availableRooms: 0,
  reservedRooms: 0
})
const editRules = {
  totalRooms: [{ required: true, message: '请输入总房量', trigger: 'blur' }],
  availableRooms: [{ required: true, message: '请输入可售房量', trigger: 'blur' }],
  reservedRooms: [{ required: true, message: '请输入预留房量', trigger: 'blur' }]
}

const batchDialogVisible = ref(false)
const batchSaving = ref(false)
const batchFormRef = ref(null)
const batchForm = reactive({
  roomTypeId: null,
  dateRange: [],
  totalRooms: 0,
  availableRooms: 0,
  reservedRooms: 0
})
const batchRules = {
  roomTypeId: [{ required: true, message: '请选择房型', trigger: 'change' }],
  dateRange: [{ required: true, message: '请选择日期范围', trigger: 'change' }],
  totalRooms: [{ required: true, message: '请输入总房量', trigger: 'blur' }],
  availableRooms: [{ required: true, message: '请输入可售房量', trigger: 'blur' }],
  reservedRooms: [{ required: true, message: '请输入预留房量', trigger: 'blur' }]
}

const initCurrentMonth = () => {
  const now = new Date()
  const y = now.getFullYear()
  const m = String(now.getMonth() + 1).padStart(2, '0')
  currentMonth.value = `${y}-${m}`
}

const getYearMonth = () => {
  if (!currentMonth.value) {
    const now = new Date()
    return { year: now.getFullYear(), month: now.getMonth() + 1 }
  }
  const [year, month] = currentMonth.value.split('-').map(Number)
  return { year, month }
}

const buildCalendarGrid = (year, month, dataMap) => {
  const firstDay = new Date(year, month - 1, 1)
  const daysInMonth = new Date(year, month, 0).getDate()
  let startOffset = firstDay.getDay()
  startOffset = startOffset === 0 ? 6 : startOffset - 1

  const today = new Date()
  const todayStr = `${today.getFullYear()}-${String(today.getMonth() + 1).padStart(2, '0')}-${String(today.getDate()).padStart(2, '0')}`

  const grid = []
  let week = []

  for (let i = 0; i < startOffset; i++) {
    week.push({ date: null })
  }

  for (let d = 1; d <= daysInMonth; d++) {
    const dateStr = `${year}-${String(month).padStart(2, '0')}-${String(d).padStart(2, '0')}`
    const item = dataMap[dateStr] || {}
    week.push({
      date: dateStr,
      day: d,
      isToday: dateStr === todayStr,
      totalRooms: item.totalRooms || 0,
      availableRooms: item.availableRooms || 0,
      reservedRooms: item.reservedRooms || 0
    })
    if (week.length === 7) {
      grid.push(week)
      week = []
    }
  }

  if (week.length > 0) {
    while (week.length < 7) week.push({ date: null })
    grid.push(week)
  }

  return grid
}

const getCellClass = (cell) => {
  if (!cell.date) return 'empty'
  const classes = []
  if (cell.isToday) classes.push('today')
  if (cell.totalRooms === 0) return classes.join(' ')

  const ratio = cell.availableRooms / cell.totalRooms
  if (ratio > 0.5) {
    classes.push('green-cell')
  } else if (ratio >= 0.2) {
    classes.push('yellow-cell')
  } else {
    classes.push('red-cell')
  }
  return classes.join(' ')
}

const loadRoomTypes = async () => {
  try {
    const res = await api.hotel.getRoomTypes()
    if (res.code === 200) {
      roomTypes.value = res.data || []
      if (roomTypes.value.length > 0 && !selectedRoomType.value) {
        selectedRoomType.value = roomTypes.value[0].id
      }
    }
  } catch {
    roomTypes.value = []
  }
}

const loadPoolData = async () => {
  if (!selectedRoomType.value) return
  const { year, month } = getYearMonth()
  const startDate = `${year}-${String(month).padStart(2, '0')}-01`
  const lastDay = new Date(year, month, 0).getDate()
  const endDate = `${year}-${String(month).padStart(2, '0')}-${String(lastDay).padStart(2, '0')}`
  try {
    const res = await api.inventory.poolList({
      roomTypeId: selectedRoomType.value,
      startDate,
      endDate
    })
    const dataMap = {}
    if (res.code === 200 && res.data) {
      const list = Array.isArray(res.data) ? res.data : res.data.records || []
      list.forEach(item => {
        dataMap[item.date] = item
      })
    }
    calendarGrid.value = buildCalendarGrid(year, month, dataMap)
  } catch {
    calendarGrid.value = buildCalendarGrid(year, month, {})
  }
}

const openEditDialog = (cell) => {
  editForm.date = cell.date
  editForm.roomTypeId = selectedRoomType.value
  editForm.totalRooms = cell.totalRooms
  editForm.availableRooms = cell.availableRooms
  editForm.reservedRooms = cell.reservedRooms
  editDialogVisible.value = true
}

const handleEditSave = async () => {
  const valid = await editFormRef.value.validate().catch(() => false)
  if (!valid) return
  editSaving.value = true
  try {
    const res = await api.inventory.poolSet({
      date: editForm.date,
      roomTypeId: editForm.roomTypeId,
      totalRooms: editForm.totalRooms,
      availableRooms: editForm.availableRooms,
      reservedRooms: editForm.reservedRooms
    })
    if (res.code === 200) {
      ElMessage.success('设置成功')
      editDialogVisible.value = false
      await loadPoolData()
    } else {
      ElMessage.error(res.message || '设置失败')
    }
  } catch {
    ElMessage.error('设置失败')
  } finally {
    editSaving.value = false
  }
}

const openBatchDialog = () => {
  batchForm.roomTypeId = selectedRoomType.value
  batchForm.dateRange = []
  batchForm.totalRooms = 0
  batchForm.availableRooms = 0
  batchForm.reservedRooms = 0
  batchDialogVisible.value = true
}

const handleBatchSave = async () => {
  const valid = await batchFormRef.value.validate().catch(() => false)
  if (!valid) return
  if (!batchForm.dateRange || batchForm.dateRange.length !== 2) {
    ElMessage.warning('请选择日期范围')
    return
  }
  batchSaving.value = true
  try {
    const res = await api.inventory.poolBatch({
      roomTypeId: batchForm.roomTypeId,
      startDate: batchForm.dateRange[0],
      endDate: batchForm.dateRange[1],
      totalRooms: batchForm.totalRooms,
      availableRooms: batchForm.availableRooms,
      reservedRooms: batchForm.reservedRooms
    })
    if (res.code === 200) {
      ElMessage.success('批量设置成功')
      batchDialogVisible.value = false
      await loadPoolData()
    } else {
      ElMessage.error(res.message || '批量设置失败')
    }
  } catch {
    ElMessage.error('批量设置失败')
  } finally {
    batchSaving.value = false
  }
}

onMounted(async () => {
  initCurrentMonth()
  await loadRoomTypes()
  if (selectedRoomType.value) {
    await loadPoolData()
  }
})
</script>

<style scoped>
.inventory-pool-container {
  padding: 16px;
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
  min-height: 110px;
  padding: 10px;
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
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(0, 0, 0, 0.12);
}

.calendar-day.green-cell {
  background: linear-gradient(135deg, #f0fff4, #c6f6d5);
  border-color: #9ae6b4;
}

.calendar-day.yellow-cell {
  background: linear-gradient(135deg, #fffbeb, #feebc8);
  border-color: #fbd38d;
}

.calendar-day.red-cell {
  background: linear-gradient(135deg, #fff5f5, #fed7d7);
  border-color: #fc8181;
}

.calendar-day.today {
  border-color: #409eff;
  border-width: 3px;
}

.day-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.day-number {
  font-size: 16px;
  font-weight: 700;
  color: #2d3748;
}

.day-number.today {
  color: #409eff;
}

.day-stats {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.stat-row {
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
  color: #4a5568;
}

.stat-value.available {
  color: #67c23a;
}

.stat-value.reserved {
  color: #e6a23c;
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

.legend-color.green {
  background: linear-gradient(135deg, #f0fff4, #c6f6d5);
  border: 2px solid #9ae6b4;
}

.legend-color.yellow {
  background: linear-gradient(135deg, #fffbeb, #feebc8);
  border: 2px solid #fbd38d;
}

.legend-color.red {
  background: linear-gradient(135deg, #fff5f5, #fed7d7);
  border: 2px solid #fc8181;
}
</style>
