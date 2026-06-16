<template>
  <div class="inventory-compare-container">
    <el-card class="filter-card" shadow="never">
      <el-row :gutter="16" align="middle">
        <el-col :xs="24" :sm="8" :md="6">
          <el-select
            v-model="queryForm.channelIds"
            multiple
            collapse-tags
            collapse-tags-tooltip
            placeholder="选择渠道(最多5个)"
            :max-collapse-tags="3"
            style="width: 100%"
          >
            <el-option
              v-for="ch in channels"
              :key="ch.channelId"
              :label="ch.channelName"
              :value="ch.channelId"
              :disabled="queryForm.channelIds.length >= 5 && !queryForm.channelIds.includes(ch.channelId)"
            />
          </el-select>
        </el-col>
        <el-col :xs="8" :sm="4" :md="3">
          <el-select v-model="queryForm.roomTypeId" placeholder="房型" clearable style="width: 100%">
            <el-option v-for="rt in roomTypes" :key="rt.typeId" :label="rt.typeName" :value="rt.typeId" />
          </el-select>
        </el-col>
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
          <el-button type="primary" @click="handleQuery" :loading="loading">查询</el-button>
        </el-col>
      </el-row>
    </el-card>

    <el-card class="calendar-card" shadow="hover">
      <div class="calendar-grid" v-if="calendarDays.length > 0">
        <div
          v-for="day in calendarDays"
          :key="day.date"
          class="calendar-day"
          @click="openDayDetail(day)"
        >
          <div class="day-header">{{ formatDay(day.date) }}</div>
          <div class="day-weekday">{{ getWeekday(day.date) }}</div>
          <div class="day-chart" :ref="el => setDayChartRef(el, day.date)"></div>
        </div>
      </div>
      <el-empty v-else description="暂无数据" />
    </el-card>

    <el-dialog
      v-model="detailDialogVisible"
      :title="'房量对比 - ' + detailDate"
      width="700px"
    >
      <el-table :data="detailTableData" stripe style="width: 100%">
        <el-table-column prop="channelName" label="渠道" min-width="120" />
        <el-table-column prop="allocated" label="分配房量" width="100" align="center" />
        <el-table-column prop="used" label="已使用" width="100" align="center" />
        <el-table-column prop="remaining" label="剩余" width="100" align="center" />
        <el-table-column label="使用率" width="140" align="center">
          <template #default="{ row }">
            <el-progress
              :percentage="row.usageRate"
              :color="getUsageColor(row.usageRate)"
              :stroke-width="10"
            />
          </template>
        </el-table-column>
      </el-table>
      <template #footer>
        <el-button @click="detailDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount, nextTick } from 'vue'
import * as echarts from 'echarts'
import api from '@/api'

const loading = ref(false)
const queryForm = ref({
  channelIds: [],
  roomTypeId: null,
  dateRange: []
})

const channels = ref([])
const roomTypes = ref([])
const calendarDays = ref([])

const detailDialogVisible = ref(false)
const detailDate = ref('')
const detailTableData = ref([])

const dayChartRefs = {}
const dayChartInstances = {}

const setDayChartRef = (el, date) => {
  if (el) {
    dayChartRefs[date] = el
  }
}

const formatDay = (dateStr) => {
  if (!dateStr) return ''
  const d = new Date(dateStr)
  return `${d.getMonth() + 1}/${d.getDate()}`
}

const getWeekday = (dateStr) => {
  if (!dateStr) return ''
  const weekdays = ['日', '一', '二', '三', '四', '五', '六']
  const d = new Date(dateStr)
  return '周' + weekdays[d.getDay()]
}

const getUsageColor = (rate) => {
  if (rate >= 90) return '#F56C6C'
  if (rate >= 70) return '#E6A23C'
  return '#67C23A'
}

const loadOptions = async () => {
  try {
    const [chRes, rtRes] = await Promise.all([
      api.channel.list(),
      api.hotel.getRoomTypes()
    ])
    if (chRes.code === 200) channels.value = chRes.data || []
    if (rtRes.code === 200) roomTypes.value = rtRes.data || []
  } catch {}
}

const loadData = async () => {
  const params = {}
  if (queryForm.value.channelIds.length > 0) {
    params.channelIds = queryForm.value.channelIds.join(',')
  }
  if (queryForm.value.roomTypeId) params.roomTypeId = queryForm.value.roomTypeId
  if (queryForm.value.dateRange && queryForm.value.dateRange.length === 2) {
    params.startDate = queryForm.value.dateRange[0]
    params.endDate = queryForm.value.dateRange[1]
  }

  try {
    loading.value = true
    const res = await api.visual.inventoryCompare(params)
    if (res.code === 200 && res.data) {
      calendarDays.value = res.data.days || []
      await nextTick()
      renderDayCharts()
    }
  } catch {
  } finally {
    loading.value = false
  }
}

const renderDayCharts = () => {
  Object.keys(dayChartInstances).forEach(key => {
    dayChartInstances[key]?.dispose()
    delete dayChartInstances[key]
  })

  calendarDays.value.forEach(day => {
    const el = dayChartRefs[day.date]
    if (!el) return
    const chart = echarts.init(el)
    dayChartInstances[day.date] = chart

    const channelData = day.channels || []
    const names = channelData.map(c => c.channelName)
    const usedData = channelData.map(c => c.used || 0)
    const remainData = channelData.map(c => c.remaining || 0)

    chart.setOption({
      tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
      grid: { left: 5, right: 5, top: 5, bottom: 5, containLabel: false },
      xAxis: {
        type: 'value',
        show: false
      },
      yAxis: {
        type: 'category',
        data: names,
        show: false
      },
      series: [
        {
          name: '已使用',
          type: 'bar',
          stack: 'total',
          data: usedData,
          itemStyle: {
            color: '#667eea',
            borderRadius: [0, 0, 0, 0]
          },
          barWidth: '60%'
        },
        {
          name: '剩余',
          type: 'bar',
          stack: 'total',
          data: remainData,
          itemStyle: {
            color: '#e2e8f0',
            borderRadius: [0, 2, 2, 0]
          },
          barWidth: '60%'
        }
      ]
    })
  })
}

const openDayDetail = (day) => {
  detailDate.value = formatDay(day.date)
  const channelData = day.channels || []
  detailTableData.value = channelData.map(c => ({
    channelName: c.channelName,
    allocated: c.allocated || 0,
    used: c.used || 0,
    remaining: c.remaining || 0,
    usageRate: c.allocated > 0 ? Math.round((c.used / c.allocated) * 100) : 0
  }))
  detailDialogVisible.value = true
}

const handleQuery = () => {
  loadData()
}

const handleResize = () => {
  Object.values(dayChartInstances).forEach(chart => {
    chart?.resize()
  })
}

onMounted(() => {
  loadOptions()
  loadData()
  window.addEventListener('resize', handleResize)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize)
  Object.keys(dayChartInstances).forEach(key => {
    dayChartInstances[key]?.dispose()
    delete dayChartInstances[key]
  })
})
</script>

<style scoped>
.inventory-compare-container {
  padding: 10px;
}

.filter-card {
  margin-bottom: 20px;
  border-radius: 12px;
  border: none;
}

.calendar-card {
  border-radius: 12px;
  border: none;
}

.calendar-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(140px, 1fr));
  gap: 12px;
}

.calendar-day {
  border: 1px solid #ebeef5;
  border-radius: 8px;
  padding: 8px;
  cursor: pointer;
  transition: all 0.2s;
  background: #fff;
}

.calendar-day:hover {
  border-color: #667eea;
  box-shadow: 0 2px 8px rgba(102, 126, 234, 0.15);
}

.day-header {
  font-size: 14px;
  font-weight: 600;
  color: #2d3748;
  text-align: center;
}

.day-weekday {
  font-size: 12px;
  color: #718096;
  text-align: center;
  margin-bottom: 4px;
}

.day-chart {
  width: 100%;
  height: 80px;
}
</style>
