<template>
  <div class="revenue-analysis-container">
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
          <el-select v-model="queryForm.channelId" placeholder="渠道" clearable style="width: 100%">
            <el-option v-for="ch in channels" :key="ch.channelId" :label="ch.channelName" :value="ch.channelId" />
          </el-select>
        </el-col>
        <el-col :xs="8" :sm="4" :md="3">
          <el-button type="primary" @click="handleQuery" :loading="loading">查询</el-button>
        </el-col>
      </el-row>
    </el-card>

    <el-row :gutter="20" class="stat-row">
      <el-col :xs="12" :sm="6">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-content">
            <div class="stat-icon" style="background: linear-gradient(135deg, #667eea 0%, #764ba2 100%)">
              <el-icon :size="28"><Wallet /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ summary.totalRevenue }}</div>
              <div class="stat-label">总营收</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="12" :sm="6">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-content">
            <div class="stat-icon" style="background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%)">
              <el-icon :size="28"><Coin /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ summary.dailyAvgRevenue }}</div>
              <div class="stat-label">日均营收</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="12" :sm="6">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-content">
            <div class="stat-icon" style="background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%)">
              <el-icon :size="28"><TrendCharts /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ summary.revpar }}</div>
              <div class="stat-label">RevPAR</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="12" :sm="6">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-content">
            <div class="stat-icon" style="background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%)">
              <el-icon :size="28"><DataLine /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ summary.adr }}</div>
              <div class="stat-label">ADR</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-card class="chart-card" shadow="hover">
      <template #header>
        <span>营收趋势</span>
      </template>
      <div ref="revenueTrendRef" class="chart-container"></div>
    </el-card>

    <el-row :gutter="20" class="section-row">
      <el-col :xs="24" :sm="12">
        <el-card class="chart-card" shadow="hover">
          <template #header>
            <span>按房型营收对比</span>
          </template>
          <div ref="roomTypeBarRef" class="chart-container"></div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="12">
        <el-card class="chart-card" shadow="hover">
          <template #header>
            <span>按渠道营收占比</span>
          </template>
          <div ref="channelPieRef" class="chart-container"></div>
        </el-card>
      </el-col>
    </el-row>

    <el-card class="table-card" shadow="hover">
      <template #header>
        <div class="card-header">
          <span>营收明细</span>
          <el-button type="primary" plain @click="handleExport" :loading="exporting">
            <el-icon><Download /></el-icon>导出
          </el-button>
        </div>
      </template>
      <el-table :data="tableData" stripe style="width: 100%">
        <el-table-column prop="roomTypeName" label="房型" min-width="120" />
        <el-table-column prop="channelName" label="渠道" min-width="120" />
        <el-table-column prop="bookingCount" label="预订量" width="100" align="center" />
        <el-table-column prop="revenue" label="营收" width="130" align="right" />
        <el-table-column label="占比" width="100" align="center">
          <template #default="{ row }">
            {{ row.percentage }}%
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import { Wallet, Coin, TrendCharts, DataLine, Download } from '@element-plus/icons-vue'
import * as echarts from 'echarts'
import api from '@/api'

const loading = ref(false)
const exporting = ref(false)

const queryForm = ref({
  dateRange: [],
  roomTypeId: null,
  channelId: null
})

const roomTypes = ref([])
const channels = ref([])
const summary = ref({ totalRevenue: 0, dailyAvgRevenue: 0, revpar: 0, adr: 0 })
const revenueTrend = ref([])
const roomTypeRevenue = ref([])
const channelRevenue = ref([])
const tableData = ref([])

const revenueTrendRef = ref(null)
const roomTypeBarRef = ref(null)
const channelPieRef = ref(null)
let revenueTrendChart = null
let roomTypeBarChart = null
let channelPieChart = null

const loadOptions = async () => {
  try {
    const [rtRes, chRes] = await Promise.all([
      api.hotel.getRoomTypes(),
      api.channel.list()
    ])
    if (rtRes.code === 200) roomTypes.value = rtRes.data || []
    if (chRes.code === 200) channels.value = chRes.data || []
  } catch {}
}

const loadData = async () => {
  const params = {}
  if (queryForm.value.dateRange && queryForm.value.dateRange.length === 2) {
    params.startDate = queryForm.value.dateRange[0]
    params.endDate = queryForm.value.dateRange[1]
  }
  if (queryForm.value.roomTypeId) params.roomTypeId = queryForm.value.roomTypeId
  if (queryForm.value.channelId) params.channelId = queryForm.value.channelId

  try {
    loading.value = true
    const res = await api.analytics.revenue(params)
    if (res.code === 200 && res.data) {
      summary.value = res.data.summary || { totalRevenue: 0, dailyAvgRevenue: 0, revpar: 0, adr: 0 }
      revenueTrend.value = res.data.trend || []
      roomTypeRevenue.value = res.data.roomTypeRevenue || []
      channelRevenue.value = res.data.channelRevenue || []
      tableData.value = res.data.details || []
      await nextTick()
      renderRevenueTrendChart()
      renderRoomTypeBarChart()
      renderChannelPieChart()
    }
  } catch {
  } finally {
    loading.value = false
  }
}

const renderRevenueTrendChart = () => {
  if (!revenueTrendRef.value) return
  if (!revenueTrendChart) {
    revenueTrendChart = echarts.init(revenueTrendRef.value)
  }
  const data = revenueTrend.value
  revenueTrendChart.setOption({
    tooltip: {
      trigger: 'axis',
      formatter: (params) => {
        const p = params[0]
        return `${p.axisValue}<br/>营收: ¥${p.value}`
      }
    },
    grid: { left: 80, right: 30, top: 30, bottom: 40 },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: data.map(d => d.date),
      axisLabel: { color: '#718096' }
    },
    yAxis: {
      type: 'value',
      name: '营收(元)',
      axisLabel: { color: '#718096' },
      splitLine: { lineStyle: { type: 'dashed' } }
    },
    series: [{
      type: 'line',
      smooth: true,
      data: data.map(d => d.revenue),
      itemStyle: { color: '#667eea' },
      lineStyle: { color: '#667eea', width: 3 },
      symbol: 'circle',
      symbolSize: 8,
      areaStyle: {
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: 'rgba(102, 126, 234, 0.3)' },
          { offset: 1, color: 'rgba(102, 126, 234, 0.02)' }
        ])
      }
    }]
  })
}

const renderRoomTypeBarChart = () => {
  if (!roomTypeBarRef.value) return
  if (!roomTypeBarChart) {
    roomTypeBarChart = echarts.init(roomTypeBarRef.value)
  }
  const data = roomTypeRevenue.value
  roomTypeBarChart.setOption({
    tooltip: {
      trigger: 'axis',
      axisPointer: { type: 'shadow' },
      formatter: '{b}<br/>营收: ¥{c}'
    },
    grid: { left: 80, right: 30, top: 20, bottom: 40 },
    xAxis: {
      type: 'category',
      data: data.map(d => d.typeName || d.name),
      axisLabel: { color: '#718096' }
    },
    yAxis: {
      type: 'value',
      name: '营收(元)',
      axisLabel: { color: '#718096' },
      splitLine: { lineStyle: { type: 'dashed' } }
    },
    series: [{
      type: 'bar',
      data: data.map(d => d.revenue),
      barWidth: '50%',
      itemStyle: {
        borderRadius: [6, 6, 0, 0],
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: '#43e97b' },
          { offset: 1, color: '#38f9d7' }
        ])
      },
      label: { show: true, position: 'top', color: '#4a5568', formatter: '¥{c}' }
    }]
  })
}

const renderChannelPieChart = () => {
  if (!channelPieRef.value) return
  if (!channelPieChart) {
    channelPieChart = echarts.init(channelPieRef.value)
  }
  const data = channelRevenue.value.map(d => ({
    name: d.channelName || d.name,
    value: d.revenue
  }))
  channelPieChart.setOption({
    tooltip: { trigger: 'item', formatter: '{b}: ¥{c} ({d}%)' },
    legend: { bottom: 0, textStyle: { color: '#718096' } },
    series: [{
      type: 'pie',
      radius: ['40%', '70%'],
      center: ['50%', '45%'],
      avoidLabelOverlap: true,
      itemStyle: { borderRadius: 8, borderColor: '#fff', borderWidth: 2 },
      label: { show: true, formatter: '{b}\n{d}%', color: '#4a5568' },
      data: data,
      color: ['#667eea', '#43e97b', '#f093fb', '#4facfe', '#f5576c', '#ffa726', '#ab47bc', '#26c6da']
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
      type: 'revenue',
      dateRange: queryForm.value.dateRange,
      roomTypeId: queryForm.value.roomTypeId,
      channelId: queryForm.value.channelId
    }
    const res = await api.analytics.export(params)
    const blob = new Blob([res], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' })
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.setAttribute('download', `营收分析_${new Date().getTime()}.xlsx`)
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
  revenueTrendChart?.resize()
  roomTypeBarChart?.resize()
  channelPieChart?.resize()
}

onMounted(() => {
  loadOptions()
  loadData()
  window.addEventListener('resize', handleResize)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize)
  revenueTrendChart?.dispose()
  roomTypeBarChart?.dispose()
  channelPieChart?.dispose()
})
</script>

<style scoped>
.revenue-analysis-container {
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

.section-row {
  margin-bottom: 20px;
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
  height: 350px;
}
</style>
