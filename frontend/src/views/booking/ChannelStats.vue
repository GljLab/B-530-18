<template>
  <div class="channel-stats-container">
    <div class="page-header">
      <h2 class="page-title">渠道统计</h2>
      <div class="header-actions">
        <el-date-picker
          v-model="dateRange"
          type="daterange"
          range-separator="至"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
          value-format="YYYY-MM-DD"
          style="width: 280px"
        />
        <el-button type="primary" @click="loadAllData">查询</el-button>
      </div>
    </div>

    <el-row :gutter="20" class="stat-row">
      <el-col :xs="12" :sm="8">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-content">
            <div class="stat-icon" style="background: linear-gradient(135deg, #667eea 0%, #764ba2 100%)">
              <el-icon :size="28"><Tickets /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ summary.totalBookings || 0 }}</div>
              <div class="stat-label">总预订量</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="12" :sm="8">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-content">
            <div class="stat-icon" style="background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%)">
              <el-icon :size="28"><Money /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">¥{{ formatNumber(summary.totalRevenue) }}</div>
              <div class="stat-label">总营收</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="12" :sm="8">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-content">
            <div class="stat-icon" style="background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%)">
              <el-icon :size="28"><PieChart /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ (summary.avgCommissionRate * 100).toFixed(2) }}%</div>
              <div class="stat-label">平均佣金率</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" class="section-row">
      <el-col :xs="24" :sm="12">
        <el-card class="chart-card" shadow="hover">
          <template #header>
            <span>各渠道预订量对比</span>
          </template>
          <div ref="barChartRef" class="chart-container"></div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="12">
        <el-card class="chart-card" shadow="hover">
          <template #header>
            <span>各渠道营收占比</span>
          </template>
          <div ref="pieChartRef" class="chart-container"></div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" class="section-row">
      <el-col :span="24">
        <el-card class="chart-card" shadow="hover">
          <template #header>
            <span>渠道趋势</span>
          </template>
          <div ref="lineChartRef" class="chart-container"></div>
        </el-card>
      </el-col>
    </el-row>

    <el-card shadow="hover" class="table-card">
      <template #header>
        <span>渠道排名</span>
      </template>
      <el-table :data="rankList" v-loading="loading" stripe>
        <el-table-column label="排名" width="70">
          <template #default="{ $index }">
            <span class="rank-badge" :class="getRankClass($index)">{{ $index + 1 }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="channelName" label="渠道名称" min-width="120" />
        <el-table-column prop="bookingCount" label="预订量" min-width="90" />
        <el-table-column label="营收" min-width="110">
          <template #default="{ row }">
            ¥{{ formatNumber(row.revenue) }}
          </template>
        </el-table-column>
        <el-table-column label="占比(%)" min-width="90">
          <template #default="{ row }">
            {{ row.proportion != null ? row.proportion.toFixed(2) : '-' }}
          </template>
        </el-table-column>
        <el-table-column label="效率" min-width="100">
          <template #default="{ row }">
            <el-progress
              :percentage="Math.min(100, row.efficiency != null ? Number((row.efficiency * 100).toFixed(1)) : 0)"
              :stroke-width="12"
              :color="getEfficiencyColor(row.efficiency)"
            />
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import { Tickets, Money, PieChart } from '@element-plus/icons-vue'
import * as echarts from 'echarts'
import api from '@/api'

const dateRange = ref([])
const loading = ref(false)
const summary = ref({ totalBookings: 0, totalRevenue: 0, avgCommissionRate: 0 })
const rankList = ref([])

const barChartRef = ref(null)
const pieChartRef = ref(null)
const lineChartRef = ref(null)

let barChart = null
let pieChart = null
let lineChart = null

const initDateRange = () => {
  const now = new Date()
  const start = new Date(now.getFullYear(), now.getMonth(), 1)
  const fmt = (d) => `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`
  dateRange.value = [fmt(start), fmt(now)]
}

const formatNumber = (num) => {
  if (num == null) return '0'
  return Number(num).toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
}

const getRankClass = (index) => {
  if (index === 0) return 'rank-1'
  if (index === 1) return 'rank-2'
  if (index === 2) return 'rank-3'
  return ''
}

const getEfficiencyColor = (efficiency) => {
  if (efficiency == null) return '#909399'
  const rate = efficiency * 100
  if (rate >= 80) return '#67c23a'
  if (rate >= 50) return '#e6a23c'
  return '#f56c6c'
}

const loadStatistics = async () => {
  if (!dateRange.value || dateRange.value.length !== 2) return
  try {
    const res = await api.channel.statistics({
      startDate: dateRange.value[0],
      endDate: dateRange.value[1]
    })
    if (res.code === 200 && res.data) {
      summary.value = {
        totalBookings: res.data.totalBookings || 0,
        totalRevenue: res.data.totalRevenue || 0,
        avgCommissionRate: res.data.avgCommissionRate || 0
      }
      const channels = res.data.channels || []
      await nextTick()
      renderBarChart(channels)
      renderPieChart(channels)
      rankList.value = channels
    }
  } catch {}
}

const loadTrend = async () => {
  if (!dateRange.value || dateRange.value.length !== 2) return
  try {
    const res = await api.channel.trend({
      startDate: dateRange.value[0],
      endDate: dateRange.value[1]
    })
    if (res.code === 200 && res.data) {
      await nextTick()
      renderLineChart(res.data)
    }
  } catch {}
}

const loadEfficiency = async () => {
  if (!dateRange.value || dateRange.value.length !== 2) return
  try {
    const res = await api.channel.efficiency({
      startDate: dateRange.value[0],
      endDate: dateRange.value[1]
    })
    if (res.code === 200 && res.data) {
      const effMap = {}
      ;(res.data || []).forEach(item => {
        effMap[item.channelId || item.channelName] = item.efficiency
      })
      rankList.value = rankList.value.map(item => ({
        ...item,
        efficiency: effMap[item.channelId || item.channelName] ?? item.efficiency ?? null
      }))
    }
  } catch {}
}

const loadAllData = async () => {
  if (!dateRange.value || dateRange.value.length !== 2) {
    ElMessage.warning('请选择日期范围')
    return
  }
  loading.value = true
  try {
    await loadStatistics()
    await loadTrend()
    await loadEfficiency()
  } finally {
    loading.value = false
  }
}

const chartColors = ['#667eea', '#43e97b', '#f5576c', '#ffa726', '#4facfe', '#f093fb', '#38f9d7', '#ff6b6b']

const renderBarChart = (data) => {
  if (!barChartRef.value) return
  if (!barChart) {
    barChart = echarts.init(barChartRef.value)
  }
  const names = data.map(d => d.channelName)
  const counts = data.map(d => d.bookingCount || 0)
  barChart.setOption({
    tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
    grid: { left: 60, right: 30, top: 20, bottom: 40 },
    xAxis: {
      type: 'category',
      data: names,
      axisLabel: { color: '#718096', rotate: 15 }
    },
    yAxis: {
      type: 'value',
      name: '预订量',
      axisLabel: { color: '#718096' },
      splitLine: { lineStyle: { type: 'dashed' } }
    },
    series: [{
      type: 'bar',
      data: counts,
      barWidth: '50%',
      itemStyle: {
        borderRadius: [6, 6, 0, 0],
        color: (params) => chartColors[params.dataIndex % chartColors.length]
      },
      label: { show: true, position: 'top', color: '#4a5568', formatter: '{c}' }
    }]
  })
}

const renderPieChart = (data) => {
  if (!pieChartRef.value) return
  if (!pieChart) {
    pieChart = echarts.init(pieChartRef.value)
  }
  const pieData = data.map(d => ({
    name: d.channelName,
    value: d.revenue || 0
  }))
  pieChart.setOption({
    tooltip: { trigger: 'item', formatter: '{b}: ¥{c} ({d}%)' },
    legend: { bottom: 0, textStyle: { color: '#718096' } },
    series: [{
      type: 'pie',
      radius: ['40%', '70%'],
      center: ['50%', '45%'],
      avoidLabelOverlap: true,
      itemStyle: { borderRadius: 8, borderColor: '#fff', borderWidth: 2 },
      label: { show: true, formatter: '{b}\n{d}%', color: '#4a5568' },
      data: pieData,
      color: chartColors
    }]
  })
}

const renderLineChart = (data) => {
  if (!lineChartRef.value) return
  if (!lineChart) {
    lineChart = echarts.init(lineChartRef.value)
  }
  const trendData = Array.isArray(data) ? data : data.records || []
  const dateSet = new Set()
  trendData.forEach(item => {
    if (item.dates) {
      item.dates.forEach(d => dateSet.add(d))
    } else if (item.date) {
      dateSet.add(item.date)
    }
  })
  const dates = Array.from(dateSet).sort()
  const channelMap = {}
  trendData.forEach(item => {
    const name = item.channelName
    if (!channelMap[name]) {
      channelMap[name] = {}
    }
    if (item.dates && item.values) {
      item.dates.forEach((d, i) => {
        channelMap[name][d] = item.values[i]
      })
    } else {
      channelMap[name][item.date] = item.bookingCount || item.value || 0
    }
  })
  const series = Object.keys(channelMap).map((name, index) => ({
    name,
    type: 'line',
    smooth: true,
    data: dates.map(d => channelMap[name][d] || 0),
    itemStyle: { color: chartColors[index % chartColors.length] },
    lineStyle: { width: 2 },
    symbol: 'circle',
    symbolSize: 6
  }))
  lineChart.setOption({
    tooltip: { trigger: 'axis' },
    legend: { top: 0, textStyle: { color: '#718096' } },
    grid: { left: 60, right: 30, top: 50, bottom: 40 },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: dates,
      axisLabel: { color: '#718096' }
    },
    yAxis: {
      type: 'value',
      name: '预订量',
      axisLabel: { color: '#718096' },
      splitLine: { lineStyle: { type: 'dashed' } }
    },
    series
  })
}

const handleResize = () => {
  barChart?.resize()
  pieChart?.resize()
  lineChart?.resize()
}

onMounted(async () => {
  initDateRange()
  await loadAllData()
  window.addEventListener('resize', handleResize)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize)
  barChart?.dispose()
  pieChart?.dispose()
  lineChart?.dispose()
})
</script>

<style scoped>
.channel-stats-container {
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
  font-size: 24px;
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

.chart-card {
  border-radius: 12px;
  border: none;
}

.chart-container {
  width: 100%;
  height: 350px;
}

.table-card {
  border-radius: 12px;
  margin-top: 20px;
}

.rank-badge {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 28px;
  height: 28px;
  border-radius: 50%;
  font-size: 14px;
  font-weight: 700;
  background: #f0f0f0;
  color: #718096;
}

.rank-badge.rank-1 {
  background: linear-gradient(135deg, #ffd700, #ffb300);
  color: #fff;
}

.rank-badge.rank-2 {
  background: linear-gradient(135deg, #c0c0c0, #a0a0a0);
  color: #fff;
}

.rank-badge.rank-3 {
  background: linear-gradient(135deg, #cd7f32, #b8860b);
  color: #fff;
}
</style>
