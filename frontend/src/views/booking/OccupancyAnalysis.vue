<template>
  <div class="occupancy-analysis-container">
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
        <el-col :xs="12" :sm="4" :md="3">
          <el-select v-model="queryForm.roomTypeId" placeholder="房型" clearable style="width: 100%">
            <el-option v-for="rt in roomTypes" :key="rt.typeId" :label="rt.typeName" :value="rt.typeId" />
          </el-select>
        </el-col>
        <el-col :xs="12" :sm="4" :md="3">
          <el-select v-model="queryForm.floorId" placeholder="楼层" clearable style="width: 100%">
            <el-option v-for="f in floors" :key="f.floorId" :label="f.floorName" :value="f.floorId" />
          </el-select>
        </el-col>
        <el-col :xs="12" :sm="4" :md="3">
          <el-select v-model="queryForm.period" placeholder="统计周期" style="width: 100%">
            <el-option label="按日" value="day" />
            <el-option label="按周" value="week" />
            <el-option label="按月" value="month" />
          </el-select>
        </el-col>
        <el-col :xs="12" :sm="4" :md="3">
          <el-button type="primary" @click="handleQuery" :loading="loading">查询</el-button>
        </el-col>
      </el-row>
    </el-card>

    <el-row :gutter="20" class="stat-row">
      <el-col :xs="8">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-content">
            <div class="stat-icon" style="background: linear-gradient(135deg, #667eea 0%, #764ba2 100%)">
              <el-icon :size="28"><TrendCharts /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ summary.avgRate }}%</div>
              <div class="stat-label">平均入住率</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="8">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-content">
            <div class="stat-icon" style="background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%)">
              <el-icon :size="28"><Top /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ summary.maxRate }}%</div>
              <div class="stat-label">最高入住率</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="8">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-content">
            <div class="stat-icon" style="background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%)">
              <el-icon :size="28"><Bottom /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ summary.minRate }}%</div>
              <div class="stat-label">最低入住率</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-card class="chart-card" shadow="hover">
      <template #header>
        <div class="card-header">
          <span>入住率趋势</span>
        </div>
      </template>
      <div ref="lineChartRef" class="chart-container"></div>
    </el-card>

    <el-card class="table-card" shadow="hover">
      <template #header>
        <div class="card-header">
          <span>入住率明细</span>
          <el-button type="primary" plain @click="handleExport" :loading="exporting">
            <el-icon><Download /></el-icon>导出
          </el-button>
        </div>
      </template>
      <el-table :data="tableData" stripe style="width: 100%">
        <el-table-column prop="date" label="日期" min-width="120" />
        <el-table-column prop="roomTypeName" label="房型" min-width="120" />
        <el-table-column prop="totalRooms" label="总房量" width="100" align="center" />
        <el-table-column prop="bookedRooms" label="已预订" width="100" align="center" />
        <el-table-column label="入住率(%)" width="120" align="center">
          <template #default="{ row }">
            <span :style="{ color: getRateColor(row.occupancyRate) }">{{ row.occupancyRate }}%</span>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import { TrendCharts, Top, Bottom, Download } from '@element-plus/icons-vue'
import * as echarts from 'echarts'
import api from '@/api'

const loading = ref(false)
const exporting = ref(false)

const queryForm = ref({
  dateRange: [],
  roomTypeId: null,
  floorId: null,
  period: 'day'
})

const roomTypes = ref([])
const floors = ref([])
const summary = ref({ avgRate: 0, maxRate: 0, minRate: 0 })
const tableData = ref([])
const trendData = ref([])

const lineChartRef = ref(null)
let lineChart = null

const getRateColor = (rate) => {
  if (rate >= 90) return '#F56C6C'
  if (rate >= 70) return '#E6A23C'
  return '#67C23A'
}

const loadOptions = async () => {
  try {
    const [rtRes, floorRes] = await Promise.all([
      api.hotel.getRoomTypes(),
      api.hotel.getBuildings()
    ])
    if (rtRes.code === 200) roomTypes.value = rtRes.data || []
    if (floorRes.code === 200) {
      const buildings = floorRes.data || []
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
  }
  if (queryForm.value.roomTypeId) params.roomTypeId = queryForm.value.roomTypeId
  if (queryForm.value.floorId) params.floorId = queryForm.value.floorId
  params.period = queryForm.value.period

  try {
    loading.value = true
    const res = await api.analytics.occupancy(params)
    if (res.code === 200 && res.data) {
      summary.value = res.data.summary || { avgRate: 0, maxRate: 0, minRate: 0 }
      trendData.value = res.data.trend || []
      tableData.value = res.data.details || []
      await nextTick()
      renderLineChart()
    }
  } catch {
  } finally {
    loading.value = false
  }
}

const renderLineChart = () => {
  if (!lineChartRef.value) return
  if (!lineChart) {
    lineChart = echarts.init(lineChartRef.value)
  }
  const data = trendData.value
  lineChart.setOption({
    tooltip: {
      trigger: 'axis',
      formatter: (params) => {
        const p = params[0]
        return `${p.axisValue}<br/>入住率: ${p.value}%`
      }
    },
    grid: { left: 60, right: 30, top: 30, bottom: 40 },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: data.map(d => d.date),
      axisLabel: { color: '#718096' }
    },
    yAxis: {
      type: 'value',
      name: '入住率(%)',
      max: 100,
      axisLabel: { color: '#718096', formatter: '{value}%' },
      splitLine: { lineStyle: { type: 'dashed' } }
    },
    series: [{
      type: 'line',
      smooth: true,
      data: data.map(d => d.rate),
      itemStyle: { color: '#667eea' },
      lineStyle: { color: '#667eea', width: 3 },
      symbol: 'circle',
      symbolSize: 8,
      areaStyle: {
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: 'rgba(102, 126, 234, 0.3)' },
          { offset: 1, color: 'rgba(102, 126, 234, 0.02)' }
        ])
      },
      label: { show: true, position: 'top', color: '#4a5568', formatter: '{c}%' }
    }]
  })
}

const handleQuery = () => {
  loadData()
}

const handleExport = async () => {
  try {
    exporting.value = true
    const params = {
      type: 'occupancy',
      dateRange: queryForm.value.dateRange,
      roomTypeId: queryForm.value.roomTypeId,
      floorId: queryForm.value.floorId,
      period: queryForm.value.period
    }
    const res = await api.analytics.export(params)
    const blob = new Blob([res], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' })
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.setAttribute('download', `入住率分析_${new Date().getTime()}.xlsx`)
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    window.URL.revokeObjectURL(url)
    ElMessage.success('导出成功')
  } catch {
    ElMessage.error('导出失败')
  } finally {
    exporting.value = false
  }
}

const handleResize = () => {
  lineChart?.resize()
}

onMounted(() => {
  loadOptions()
  loadData()
  window.addEventListener('resize', handleResize)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize)
  lineChart?.dispose()
})
</script>

<style scoped>
.occupancy-analysis-container {
  padding: 10px;
}

.filter-card {
  margin-bottom: 20px;
  border-radius: 12px;
  border: none;
}

.stat-row {
  margin-bottom: 20px;
}

.stat-card {
  border-radius: 12px;
  border: none;
  transition: transform 0.3s;
}

.stat-card:hover {
  transform: translateY(-5px);
}

.stat-content {
  display: flex;
  align-items: center;
  gap: 15px;
  padding: 10px;
}

.stat-icon {
  width: 60px;
  height: 60px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
}

.stat-info {
  flex: 1;
}

.stat-value {
  font-size: 28px;
  font-weight: 700;
  color: #2d3748;
}

.stat-label {
  font-size: 14px;
  color: #718096;
}

.chart-card,
.table-card {
  margin-bottom: 20px;
  border-radius: 12px;
  border: none;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.chart-container {
  width: 100%;
  height: 400px;
}
</style>
