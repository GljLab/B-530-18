<template>
  <div class="channel-price-container">
    <div class="page-header">
      <h2 class="page-title">渠道价格管理</h2>
      <div class="header-actions">
        <el-select v-model="selectedChannel" placeholder="选择渠道" clearable style="width: 180px" @change="loadPrice">
          <el-option v-for="ch in channels" :key="ch.id" :label="ch.channelName" :value="ch.id" />
        </el-select>
        <el-select v-model="selectedRoomType" placeholder="选择房型" clearable style="width: 180px" @change="loadPrice">
          <el-option v-for="rt in roomTypes" :key="rt.id" :label="rt.typeName" :value="rt.id" />
        </el-select>
        <el-date-picker
          v-model="dateRange"
          type="daterange"
          range-separator="至"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
          value-format="YYYY-MM-DD"
          style="width: 280px"
          @change="loadPrice"
        />
        <el-button type="primary" @click="openSetDialog(null)">设置价格</el-button>
      </div>
    </div>

    <el-row :gutter="20">
      <el-col :span="16">
        <el-card shadow="hover" class="calendar-card">
          <template #header>
            <span>价格日历</span>
          </template>
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
                @click="cell.date && openSetDialog(cell)"
              >
                <template v-if="cell.date">
                  <div class="day-header">
                    <span class="day-number" :class="{ today: cell.isToday }">{{ cell.day }}</span>
                  </div>
                  <div class="day-price-info">
                    <div class="price-mode">{{ getPriceModeLabel(cell.priceMode) }}</div>
                    <div class="final-price">
                      <span class="price-symbol">¥</span>
                      <span class="price-value">{{ cell.finalPrice || '-' }}</span>
                    </div>
                  </div>
                </template>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :span="8">
        <el-card shadow="hover" class="table-card">
          <template #header>
            <span>价格明细</span>
          </template>
          <el-table :data="priceList" v-loading="loading" stripe max-height="600">
            <el-table-column prop="date" label="日期" width="110" />
            <el-table-column label="价格模式" min-width="100">
              <template #default="{ row }">
                {{ getPriceModeLabel(row.priceMode) }}
              </template>
            </el-table-column>
            <el-table-column label="价格值" min-width="80">
              <template #default="{ row }">
                {{ row.priceValue }}
              </template>
            </el-table-column>
            <el-table-column label="最终价格" min-width="90">
              <template #default="{ row }">
                <span class="final-price-text">¥{{ row.finalPrice }}</span>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
    </el-row>

    <el-dialog v-model="setDialogVisible" :title="dialogTitle" width="560px" destroy-on-close :close-on-click-modal="false">
      <el-form ref="setFormRef" :model="setForm" :rules="setRules" label-width="100px">
        <el-form-item label="日期范围" prop="dateRange">
          <el-date-picker
            v-model="setForm.dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            value-format="YYYY-MM-DD"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="价格模式" prop="priceMode">
          <el-select v-model="setForm.priceMode" style="width: 100%" @change="handleModeChange">
            <el-option v-for="m in priceModes" :key="m.value" :label="m.label" :value="m.value" />
          </el-select>
        </el-form-item>
        <el-form-item :label="priceValueLabel" prop="priceValue">
          <el-input-number v-model="setForm.priceValue" :min="0" :precision="2" style="width: 100%" />
        </el-form-item>
        <el-form-item label="价格预览">
          <div class="price-preview">
            <el-descriptions :column="1" border size="small">
              <el-descriptions-item label="价格模式">{{ getPriceModeLabel(setForm.priceMode) }}</el-descriptions-item>
              <el-descriptions-item :label="priceValueLabel">{{ setForm.priceValue }}</el-descriptions-item>
              <el-descriptions-item label="预计最终价格">
                <span class="preview-price">¥{{ previewPrice }}</span>
              </el-descriptions-item>
            </el-descriptions>
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="setDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="setSaving" @click="handleSetSave">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import api from '@/api'

const weekDays = ['一', '二', '三', '四', '五', '六', '日']

const priceModes = [
  { value: 1, label: '跟随门市价' },
  { value: 2, label: '固定差价' },
  { value: 3, label: '固定折扣' },
  { value: 4, label: '独立定价' }
]

const getPriceModeLabel = (mode) => {
  const item = priceModes.find(m => m.value === mode)
  return item ? item.label : '-'
}

const channels = ref([])
const roomTypes = ref([])
const selectedChannel = ref(null)
const selectedRoomType = ref(null)
const dateRange = ref([])
const loading = ref(false)
const priceList = ref([])
const calendarGrid = ref([])

const setDialogVisible = ref(false)
const setSaving = ref(false)
const setFormRef = ref(null)
const setForm = reactive({
  dateRange: [],
  priceMode: 1,
  priceValue: 0
})
const setRules = {
  dateRange: [{ required: true, message: '请选择日期范围', trigger: 'change' }],
  priceMode: [{ required: true, message: '请选择价格模式', trigger: 'change' }],
  priceValue: [{ required: true, message: '请输入价格值', trigger: 'blur' }]
}

const basePrice = ref(0)

const priceValueLabel = computed(() => {
  switch (setForm.priceMode) {
    case 1: return '差价金额'
    case 2: return '差价金额'
    case 3: return '折扣率(0-1)'
    case 4: return '价格金额'
    default: return '价格值'
  }
})

const previewPrice = computed(() => {
  switch (setForm.priceMode) {
    case 1: return basePrice.value + setForm.priceValue
    case 2: return Math.max(0, basePrice.value + setForm.priceValue)
    case 3: return (basePrice.value * setForm.priceValue).toFixed(2)
    case 4: return setForm.priceValue
    default: return 0
  }
})

const dialogTitle = computed(() => {
  if (setForm.dateRange && setForm.dateRange.length === 2) {
    return `设置价格 - ${setForm.dateRange[0]} 至 ${setForm.dateRange[1]}`
  }
  return '设置价格'
})

const initDateRange = () => {
  const now = new Date()
  const start = new Date(now.getFullYear(), now.getMonth(), 1)
  const end = new Date(now.getFullYear(), now.getMonth() + 1, 0)
  const fmt = (d) => `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`
  dateRange.value = [fmt(start), fmt(end)]
}

const buildCalendarGrid = (dataMap) => {
  if (!dateRange.value || dateRange.value.length !== 2) return []
  const startParts = dateRange.value[0].split('-').map(Number)
  const year = startParts[0]
  const month = startParts[1]

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
      priceMode: item.priceMode || null,
      priceValue: item.priceValue || 0,
      finalPrice: item.finalPrice || 0
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
  if (cell.finalPrice > 0) {
    if (cell.priceMode === 4) classes.push('independent-cell')
    else if (cell.priceMode === 3) classes.push('discount-cell')
    else classes.push('normal-cell')
  }
  return classes.join(' ')
}

const loadChannels = async () => {
  try {
    const res = await api.channel.list()
    if (res.code === 200) {
      channels.value = res.data || []
      if (channels.value.length > 0 && !selectedChannel.value) {
        selectedChannel.value = channels.value[0].id
      }
    }
  } catch {
    channels.value = []
  }
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

const loadPrice = async () => {
  if (!selectedChannel.value || !selectedRoomType.value || !dateRange.value || dateRange.value.length !== 2) return
  loading.value = true
  try {
    const res = await api.channel.price({
      channelId: selectedChannel.value,
      roomTypeId: selectedRoomType.value,
      startDate: dateRange.value[0],
      endDate: dateRange.value[1]
    })
    if (res.code === 200 && res.data) {
      const list = Array.isArray(res.data) ? res.data : res.data.records || []
      priceList.value = list
      if (list.length > 0 && list[0].basePrice != null) {
        basePrice.value = list[0].basePrice
      }
      const dataMap = {}
      list.forEach(item => {
        dataMap[item.date] = item
      })
      calendarGrid.value = buildCalendarGrid(dataMap)
    }
  } catch {
    priceList.value = []
    calendarGrid.value = buildCalendarGrid({})
  } finally {
    loading.value = false
  }
}

const handleModeChange = () => {
  setForm.priceValue = setForm.priceMode === 3 ? 1 : 0
}

const openSetDialog = (cell) => {
  if (cell && cell.date) {
    setForm.dateRange = [cell.date, cell.date]
    setForm.priceMode = cell.priceMode || 1
    setForm.priceValue = cell.priceValue || 0
  } else {
    setForm.dateRange = dateRange.value && dateRange.value.length === 2 ? [...dateRange.value] : []
    setForm.priceMode = 1
    setForm.priceValue = 0
  }
  setDialogVisible.value = true
}

const handleSetSave = async () => {
  const valid = await setFormRef.value.validate().catch(() => false)
  if (!valid) return
  if (!setForm.dateRange || setForm.dateRange.length !== 2) {
    ElMessage.warning('请选择日期范围')
    return
  }
  setSaving.value = true
  try {
    const res = await api.channel.priceSet({
      channelId: selectedChannel.value,
      roomTypeId: selectedRoomType.value,
      startDate: setForm.dateRange[0],
      endDate: setForm.dateRange[1],
      priceMode: setForm.priceMode,
      priceValue: setForm.priceValue
    })
    if (res.code === 200) {
      ElMessage.success('设置成功')
      setDialogVisible.value = false
      await loadPrice()
    } else {
      ElMessage.error(res.message || '设置失败')
    }
  } catch {
    ElMessage.error('设置失败')
  } finally {
    setSaving.value = false
  }
}

onMounted(async () => {
  initDateRange()
  await Promise.all([loadChannels(), loadRoomTypes()])
  if (selectedChannel.value && selectedRoomType.value) {
    await loadPrice()
  }
})
</script>

<style scoped>
.channel-price-container {
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
  min-height: 90px;
  padding: 8px;
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

.calendar-day.normal-cell {
  background: linear-gradient(135deg, #ebf5ff, #dbeafe);
  border-color: #93c5fd;
}

.calendar-day.discount-cell {
  background: linear-gradient(135deg, #fff7ed, #fed7aa);
  border-color: #fdba74;
}

.calendar-day.independent-cell {
  background: linear-gradient(135deg, #f0fdf4, #bbf7d0);
  border-color: #86efac;
}

.calendar-day.today {
  border-color: #409eff;
  border-width: 3px;
}

.day-header {
  margin-bottom: 6px;
}

.day-number {
  font-size: 14px;
  font-weight: 700;
  color: #2d3748;
}

.day-number.today {
  color: #409eff;
}

.day-price-info {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.price-mode {
  font-size: 10px;
  color: #718096;
}

.final-price {
  display: flex;
  align-items: baseline;
}

.price-symbol {
  font-size: 12px;
  color: #e53e3e;
  margin-right: 2px;
}

.price-value {
  font-size: 18px;
  font-weight: 700;
  color: #e53e3e;
}

.table-card {
  border-radius: 12px;
}

.final-price-text {
  font-weight: 600;
  color: #e53e3e;
}

.price-preview {
  width: 100%;
}

.preview-price {
  font-size: 18px;
  font-weight: 700;
  color: #e53e3e;
}
</style>
