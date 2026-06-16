<template>
  <div class="booking-statistics-container">
    <div class="page-header">
      <h2 class="page-title">预订统计与报表</h2>
      <el-button
        v-if="hasPermission('booking:statistics:export')"
        type="primary"
        @click="openExportDialog"
        :loading="exporting"
      >
        <el-icon><Download /></el-icon>导出报表
      </el-button>
    </div>

    <el-row :gutter="20" class="stat-row">
      <el-col :xs="12" :sm="6">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-content">
            <div class="stat-icon" style="background: linear-gradient(135deg, #667eea 0%, #764ba2 100%)">
              <el-icon :size="28"><Plus /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ todayStats.newBookings || 0 }}</div>
              <div class="stat-label">今日新增预订</div>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :xs="12" :sm="6">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-content">
            <div class="stat-icon" style="background: linear-gradient(135deg, #ffa726 0%, #ff9800 100%)">
              <el-icon :size="28"><Clock /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ todayStats.pendingCheckin || 0 }}</div>
              <div class="stat-label">今日待入住</div>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :xs="12" :sm="6">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-content">
            <div class="stat-icon" style="background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%)">
              <el-icon :size="28"><User /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ todayStats.checkedIn || 0 }}</div>
              <div class="stat-label">今日已入住</div>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :xs="12" :sm="6">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-content">
            <div class="stat-icon" style="background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%)">
              <el-icon :size="28"><Close /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ todayStats.cancelled || 0 }}</div>
              <div class="stat-label">今日取消数</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" class="section-row">
      <el-col :xs="24" :sm="12">
        <el-card class="chart-card" shadow="hover">
          <template #header>
            <span>预订状态统计</span>
          </template>
          <div ref="statusChartRef" class="chart-container"></div>
        </el-card>
      </el-col>

      <el-col :xs="24" :sm="12">
        <el-card class="chart-card" shadow="hover">
          <template #header>
            <span>预订来源统计</span>
          </template>
          <div ref="sourceChartRef" class="chart-container"></div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" class="section-row">
      <el-col :xs="24" :sm="12">
        <el-card class="chart-card" shadow="hover">
          <template #header>
            <span>近7日预订趋势</span>
          </template>
          <div ref="trendChartRef" class="chart-container"></div>
        </el-card>
      </el-col>

      <el-col :xs="24" :sm="12">
        <el-card class="chart-card" shadow="hover">
          <template #header>
            <span>房型预订统计</span>
          </template>
          <div ref="roomTypeChartRef" class="chart-container"></div>
        </el-card>
      </el-col>
    </el-row>

    <el-dialog
      v-model="exportDialogVisible"
      title="导出统计报表"
      width="560px"
      :close-on-click-modal="false"
    >
      <el-form :model="exportForm" label-width="100px">
        <el-form-item label="日期范围">
          <el-date-picker
            v-model="exportForm.dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="预订状态">
          <el-select v-model="exportForm.statuses" multiple placeholder="全部状态" style="width: 100%">
            <el-option label="待确认" :value="1" />
            <el-option label="已确认" :value="2" />
            <el-option label="已入住" :value="3" />
            <el-option label="已完成" :value="4" />
            <el-option label="已取消" :value="5" />
          </el-select>
        </el-form-item>
        <el-form-item label="预订来源">
          <el-select v-model="exportForm.sources" multiple placeholder="全部来源" style="width: 100%">
            <el-option label="线上平台" :value="1" />
            <el-option label="电话预订" :value="2" />
            <el-option label="前台预订" :value="3" />
            <el-option label="会员预订" :value="4" />
            <el-option label="其他" :value="5" />
          </el-select>
        </el-form-item>
        <el-form-item label="包含图表">
          <el-switch v-model="exportForm.includeChart" />
        </el-form-item>
        <el-form-item label="导出预览">
          <el-descriptions :column="1" border size="small" class="export-preview">
            <el-descriptions-item label="日期范围">
              {{ formatPreviewDateRange }}
            </el-descriptions-item>
            <el-descriptions-item label="预订状态">
              {{ exportForm.statuses.length === 0 ? '全部' : exportForm.statuses.length + ' 种' }}
            </el-descriptions-item>
            <el-descriptions-item label="预订来源">
              {{ exportForm.sources.length === 0 ? '全部' : exportForm.sources.length + ' 种' }}
            </el-descriptions-item>
            <el-descriptions-item label="包含图表">
              {{ exportForm.includeChart ? '是' : '否' }}
            </el-descriptions-item>
          </el-descriptions>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="exportDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="exporting" @click="handleConfirmExport">确认导出</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onBeforeUnmount, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import { Download, Plus, Clock, User, Close } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import * as echarts from 'echarts'
import api from '@/api'

const userStore = useUserStore()
const hasPermission = (p) => userStore.hasPermission(p)

const todayStats = ref({})
const statusStats = ref([])
const sourceStats = ref([])
const trendStats = ref([])
const roomTypeStats = ref([])
const exporting = ref(false)

const exportDialogVisible = ref(false)
const exportForm = ref({
  dateRange: [],
  statuses: [],
  sources: [],
  includeChart: true
})

const statusChartRef = ref(null)
const sourceChartRef = ref(null)
const trendChartRef = ref(null)
const roomTypeChartRef = ref(null)

let statusChart = null
let sourceChart = null
let trendChart = null
let roomTypeChart = null

const statusLabels = { 1: '待确认', 2: '已确认', 3: '已入住', 4: '已完成', 5: '已取消' }
const statusColors = { 1: '#E6A23C', 2: '#409EFF', 3: '#67C23A', 4: '#909399', 5: '#F56C6C' }

const sourceLabels = { 1: '线上平台', 2: '电话预订', 3: '前台预订', 4: '会员预订', 5: '其他' }
const sourceColors = ['#667eea', '#43e97b', '#f093fb', '#4facfe', '#ffa726']

const formatPreviewDateRange = computed(() => {
  if (!exportForm.value.dateRange || exportForm.value.dateRange.length === 0) return '全部'
  const [start, end] = exportForm.value.dateRange
  return `${start} 至 ${end}`
})

const loadTodayStats = async () => {
  try {
    const res = await api.booking.stats.today()
    if (res.code === 200 && res.data) {
      todayStats.value = res.data
    }
  } catch {}
}

const loadStatusStats = async () => {
  try {
    const res = await api.booking.stats.status()
    if (res.code === 200) {
      statusStats.value = res.data || []
      await nextTick()
      renderStatusChart(statusStats.value)
    }
  } catch {}
}

const loadSourceStats = async () => {
  try {
    const res = await api.booking.stats.source()
    if (res.code === 200) {
      sourceStats.value = res.data || []
      await nextTick()
      renderSourceChart(sourceStats.value)
    }
  } catch {}
}

const loadTrendStats = async () => {
  try {
    const res = await api.booking.stats.trend({ days: 7 })
    if (res.code === 200) {
      trendStats.value = res.data || []
      await nextTick()
      renderTrendChart(trendStats.value)
    }
  } catch {}
}

const loadRoomTypeStats = async () => {
  try {
    const res = await api.booking.stats.roomType()
    if (res.code === 200) {
      roomTypeStats.value = res.data || []
      await nextTick()
      renderRoomTypeChart(roomTypeStats.value)
    }
  } catch {}
}

const renderStatusChart = (data) => {
  if (!statusChartRef.value) return
  if (!statusChart) {
    statusChart = echarts.init(statusChartRef.value)
  }
  statusChart.setOption({
    tooltip: { trigger: 'item', formatter: '{b}: {c} ({d}%)' },
    legend: { bottom: 0, textStyle: { color: '#718096' } },
    series: [{
      type: 'pie',
      radius: ['40%', '70%'],
      center: ['50%', '45%'],
      avoidLabelOverlap: true,
      itemStyle: { borderRadius: 8, borderColor: '#fff', borderWidth: 2 },
      label: { show: true, formatter: '{b}\n{d}%', color: '#4a5568' },
      data: data.map(d => ({
        name: statusLabels[d.status] || d.statusName || '未知',
        value: d.count || d.value
      })),
      color: data.map(d => statusColors[d.status] || '#909399')
    }]
  })
}

const renderSourceChart = (data) => {
  if (!sourceChartRef.value) return
  if (!sourceChart) {
    sourceChart = echarts.init(sourceChartRef.value)
  }
  sourceChart.setOption({
    tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' }, formatter: '{b}<br/>预订数: {c}' },
    grid: { left: 60, right: 30, top: 20, bottom: 40 },
    xAxis: {
      type: 'category',
      data: data.map(d => sourceLabels[d.source] || d.sourceName || '未知'),
      axisLabel: { color: '#718096' }
    },
    yAxis: {
      type: 'value',
      name: '预订数',
      axisLabel: { color: '#718096' },
      splitLine: { lineStyle: { type: 'dashed' } }
    },
    series: [{
      type: 'bar',
      data: data.map(d => d.count || d.value),
      barWidth: '50%',
      itemStyle: {
        borderRadius: [6, 6, 0, 0],
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: '#667eea' },
          { offset: 1, color: '#764ba2' }
        ])
      },
      label: { show: true, position: 'top', color: '#4a5568', formatter: '{c}' }
    }]
  })
}

const renderTrendChart = (data) => {
  if (!trendChartRef.value) return
  if (!trendChart) {
    trendChart = echarts.init(trendChartRef.value)
  }
  trendChart.setOption({
    tooltip: { trigger: 'axis', formatter: '{b}<br/>预订数: {c}' },
    grid: { left: 60, right: 30, top: 30, bottom: 40 },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: data.map(d => d.date || d.name),
      axisLabel: { color: '#718096' }
    },
    yAxis: {
      type: 'value',
      name: '预订数',
      axisLabel: { color: '#718096' },
      splitLine: { lineStyle: { type: 'dashed' } }
    },
    series: [{
      type: 'line',
      smooth: true,
      data: data.map(d => d.count || d.value || 0),
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
      label: { show: true, position: 'top', color: '#4a5568', formatter: '{c}' }
    }]
  })
}

const renderRoomTypeChart = (data) => {
  if (!roomTypeChartRef.value) return
  if (!roomTypeChart) {
    roomTypeChart = echarts.init(roomTypeChartRef.value)
  }
  const names = data.map(d => d.typeName || d.name)
  const counts = data.map(d => d.bookingCount || d.count || 0)
  const rates = data.map(d => {
    const rate = d.bookingRate || d.rate || 0
    return typeof rate === 'number' ? Number((rate * 100).toFixed(1)) : Number(rate)
  })

  roomTypeChart.setOption({
    tooltip: {
      trigger: 'axis',
      axisPointer: { type: 'shadow' },
      formatter: (params) => {
        const name = params[0].axisValue
        let result = `${name}<br/>`
        params.forEach(p => {
          const unit = p.seriesName === '预订数' ? ' 单' : '%'
          result += `${p.marker} ${p.seriesName}: ${p.value}${unit}<br/>`
        })
        return result
      }
    },
    legend: { top: 0, textStyle: { color: '#718096' }, data: ['预订数', '预订率'] },
    grid: { left: 60, right: 60, top: 50, bottom: 60 },
    xAxis: {
      type: 'category',
      data: names,
      axisLabel: { color: '#718096', rotate: 20 }
    },
    yAxis: [
      {
        type: 'value',
        name: '预订数',
        axisLabel: { color: '#718096' },
        splitLine: { lineStyle: { type: 'dashed' } }
      },
      {
        type: 'value',
        name: '预订率(%)',
        max: 100,
        axisLabel: { color: '#718096', formatter: '{value}%' },
        splitLine: { show: false }
      }
    ],
    series: [
      {
        name: '预订数',
        type: 'bar',
        data: counts,
        barWidth: '35%',
        itemStyle: {
          borderRadius: [4, 4, 0, 0],
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: '#667eea' },
            { offset: 1, color: '#764ba2' }
          ])
        }
      },
      {
        name: '预订率',
        type: 'line',
        yAxisIndex: 1,
        smooth: true,
        data: rates,
        itemStyle: { color: '#f5576c' },
        lineStyle: { color: '#f5576c', width: 2 },
        symbol: 'circle',
        symbolSize: 6,
        label: { show: true, position: 'top', color: '#4a5568', formatter: '{c}%' }
      }
    ]
  })
}

const handleResize = () => {
  statusChart?.resize()
  sourceChart?.resize()
  trendChart?.resize()
  roomTypeChart?.resize()
}

const openExportDialog = () => {
  exportDialogVisible.value = true
}

const handleConfirmExport = async () => {
  try {
    exporting.value = true
    const params = {
      dateRange: exportForm.value.dateRange,
      statuses: exportForm.value.statuses,
      sources: exportForm.value.sources,
      includeChart: exportForm.value.includeChart
    }
    const res = await api.booking.stats.export(params)
    const blob = new Blob([res], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' })
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.setAttribute('download', `预订统计报表_${new Date().getTime()}.xlsx`)
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    window.URL.revokeObjectURL(url)
    exportDialogVisible.value = false
    ElMessage.success('导出成功')
  } catch {
    ElMessage.error('导出失败')
  } finally {
    exporting.value = false
  }
}

onMounted(() => {
  Promise.all([
    loadTodayStats(),
    loadStatusStats(),
    loadSourceStats(),
    loadTrendStats(),
    loadRoomTypeStats()
  ])
  window.addEventListener('resize', handleResize)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize)
  statusChart?.dispose()
  sourceChart?.dispose()
  trendChart?.dispose()
  roomTypeChart?.dispose()
})
</script>

<style scoped>
.booking-statistics-container {
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

.chart-card {
  border-radius: 12px;
  border: none;
}

.chart-container {
  width: 100%;
  height: 350px;
}

.export-preview {
  width: 100%;
}

.export-preview :deep(.el-descriptions__label) {
  width: 100px;
  background: #fafafa;
}
</style>
