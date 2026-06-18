<template>
  <div class="comparison-analysis">
    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <span class="title">对比分析</span>
          <el-button type="success" @click="handleExport">导出</el-button>
        </div>
      </template>

      <el-radio-group v-model="dimension" @change="handleDimensionChange" class="dimension-group">
        <el-radio-button label="time">时间对比</el-radio-button>
        <el-radio-button label="target">目标对比</el-radio-button>
        <el-radio-button label="roomType">房型对比</el-radio-button>
        <el-radio-button label="channel">渠道对比</el-radio-button>
      </el-radio-group>

      <div v-loading="loading" class="content-area">
        <template v-if="dimension === 'time'">
          <el-row :gutter="16" class="time-section">
            <el-col :span="8" v-for="item in timeCards" :key="item.key">
              <el-card shadow="hover" class="compare-card">
                <template #header>
                  <span class="compare-card-title">{{ item.label }}</span>
                </template>
                <div class="metric-row" v-for="metric in item.metrics" :key="metric.name">
                  <div class="metric-name">{{ metric.name }}</div>
                  <div class="metric-values">
                    <span class="current">¥{{ formatMoney(metric.current) }}</span>
                    <span class="vs">vs</span>
                    <span class="previous">¥{{ formatMoney(metric.previous) }}</span>
                  </div>
                  <div class="growth-row">
                    <el-tag :type="metric.growth >= 0 ? 'success' : 'danger'" size="small" effect="dark">
                      <span>{{ metric.growth >= 0 ? '↑' : '↓' }} {{ Math.abs(metric.growth).toFixed(2) }}%</span>
                    </el-tag>
                  </div>
                </div>
              </el-card>
            </el-col>
          </el-row>
        </template>

        <template v-if="dimension === 'target'">
          <el-row :gutter="16" class="target-section">
            <el-col :span="6">
              <div class="stat-card blue">
                <div class="stat-info">
                  <div class="stat-value">¥{{ formatMoney(targetData.actualRevenue) }}</div>
                  <div class="stat-label">实际营收</div>
                </div>
              </div>
            </el-col>
            <el-col :span="6">
              <div class="stat-card green">
                <div class="stat-info">
                  <div class="stat-value">¥{{ formatMoney(targetData.targetRevenue) }}</div>
                  <div class="stat-label">目标营收</div>
                </div>
              </div>
            </el-col>
            <el-col :span="6">
              <div class="stat-card" :class="targetData.gapAmount >= 0 ? 'green' : 'red'">
                <div class="stat-info">
                  <div class="stat-value">¥{{ formatMoney(Math.abs(targetData.gapAmount)) }}</div>
                  <div class="stat-label">{{ targetData.gapAmount >= 0 ? '超出金额' : '差距金额' }}</div>
                </div>
              </div>
            </el-col>
            <el-col :span="6">
              <div class="stat-card orange">
                <div class="stat-info">
                  <div class="stat-label">完成率</div>
                  <el-progress
                    type="dashboard"
                    :percentage="Number((targetData.completionRate || 0).toFixed(1))"
                    :width="100"
                    :color="targetData.completionRate >= 100 ? '#67c23a' : '#409eff'"
                    class="completion-progress"
                  />
                </div>
              </div>
            </el-col>
          </el-row>
          <div ref="targetChartRef" class="chart-container"></div>
        </template>

        <template v-if="dimension === 'roomType'">
          <el-table :data="roomTypeData" border stripe style="width: 100%; margin-bottom: 20px">
            <el-table-column prop="roomTypeName" label="房型名称" width="160" />
            <el-table-column label="营收" width="160" align="right">
              <template #default="{ row }">
                <span class="price">¥{{ formatMoney(row.revenue) }}</span>
              </template>
            </el-table-column>
            <el-table-column label="入住率" width="140" align="center">
              <template #default="{ row }">
                <el-progress
                  :percentage="Number((row.occupancyRate || 0).toFixed(1))"
                  :stroke-width="14"
                  :color="getOccupancyColor(row.occupancyRate)"
                />
              </template>
            </el-table-column>
            <el-table-column label="ADR" width="160" align="right">
              <template #default="{ row }">
                <span>¥{{ formatMoney(row.adr) }}</span>
              </template>
            </el-table-column>
            <el-table-column label="营收占比" min-width="200">
              <template #default="{ row }">
                <div class="percent-cell">
                  <el-progress
                    :percentage="Number((row.revenuePercentage || 0).toFixed(1))"
                    :stroke-width="14"
                    color="#409eff"
                  />
                  <span class="percent-text">{{ (row.revenuePercentage || 0).toFixed(1) }}%</span>
                </div>
              </template>
            </el-table-column>
          </el-table>
          <div ref="roomTypeChartRef" class="chart-container"></div>
        </template>

        <template v-if="dimension === 'channel'">
          <el-table :data="channelData" border stripe style="width: 100%; margin-bottom: 20px">
            <el-table-column prop="channelName" label="渠道名称" width="160" />
            <el-table-column label="营收" width="160" align="right">
              <template #default="{ row }">
                <span class="price">¥{{ formatMoney(row.revenue) }}</span>
              </template>
            </el-table-column>
            <el-table-column label="佣金成本" width="160" align="right">
              <template #default="{ row }">
                <span class="cost">¥{{ formatMoney(row.commissionCost) }}</span>
              </template>
            </el-table-column>
            <el-table-column label="净收益" width="160" align="right">
              <template #default="{ row }">
                <span class="price">¥{{ formatMoney(row.netRevenue) }}</span>
              </template>
            </el-table-column>
            <el-table-column label="净收益率" min-width="200">
              <template #default="{ row }">
                <div class="percent-cell">
                  <el-progress
                    :percentage="Number((row.netRevenueRate || 0).toFixed(1))"
                    :stroke-width="14"
                    :color="getNetRateColor(row.netRevenueRate)"
                  />
                  <span class="percent-text">{{ (row.netRevenueRate || 0).toFixed(1) }}%</span>
                </div>
              </template>
            </el-table-column>
          </el-table>
          <div ref="channelChartRef" class="chart-container"></div>
        </template>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, nextTick, onBeforeUnmount } from 'vue'
import { ElMessage } from 'element-plus'
import * as echarts from 'echarts'
import api from '@/api'

const loading = ref(false)
const dimension = ref('time')
const targetChartRef = ref(null)
const roomTypeChartRef = ref(null)
const channelChartRef = ref(null)

let targetChart = null
let roomTypeChart = null
let channelChart = null

const timeData = ref({
  month: { currentRevenue: 0, previousRevenue: 0, revenueGrowth: 0, currentCollection: 0, previousCollection: 0, collectionGrowth: 0, currentRefund: 0, previousRefund: 0, refundGrowth: 0, currentReceivable: 0, previousReceivable: 0, receivableGrowth: 0 },
  week: { currentRevenue: 0, previousRevenue: 0, revenueGrowth: 0, currentCollection: 0, previousCollection: 0, collectionGrowth: 0, currentRefund: 0, previousRefund: 0, refundGrowth: 0, currentReceivable: 0, previousReceivable: 0, receivableGrowth: 0 },
  day: { currentRevenue: 0, previousRevenue: 0, revenueGrowth: 0, currentCollection: 0, previousCollection: 0, collectionGrowth: 0, currentRefund: 0, previousRefund: 0, refundGrowth: 0, currentReceivable: 0, previousReceivable: 0, receivableGrowth: 0 }
})

const targetData = ref({
  actualRevenue: 0,
  targetRevenue: 0,
  completionRate: 0,
  gapAmount: 0,
  monthlyTarget: []
})

const roomTypeData = ref([])
const channelData = ref([])

const timeCards = computed(() => [
  {
    key: 'month',
    label: '本月 vs 上月',
    metrics: [
      { name: '营收', current: timeData.value.month.currentRevenue, previous: timeData.value.month.previousRevenue, growth: timeData.value.month.revenueGrowth },
      { name: '收款', current: timeData.value.month.currentCollection, previous: timeData.value.month.previousCollection, growth: timeData.value.month.collectionGrowth },
      { name: '退款', current: timeData.value.month.currentRefund, previous: timeData.value.month.previousRefund, growth: timeData.value.month.refundGrowth },
      { name: '应收', current: timeData.value.month.currentReceivable, previous: timeData.value.month.previousReceivable, growth: timeData.value.month.receivableGrowth }
    ]
  },
  {
    key: 'week',
    label: '本周 vs 上周',
    metrics: [
      { name: '营收', current: timeData.value.week.currentRevenue, previous: timeData.value.week.previousRevenue, growth: timeData.value.week.revenueGrowth },
      { name: '收款', current: timeData.value.week.currentCollection, previous: timeData.value.week.previousCollection, growth: timeData.value.week.collectionGrowth },
      { name: '退款', current: timeData.value.week.currentRefund, previous: timeData.value.week.previousRefund, growth: timeData.value.week.refundGrowth },
      { name: '应收', current: timeData.value.week.currentReceivable, previous: timeData.value.week.previousReceivable, growth: timeData.value.week.receivableGrowth }
    ]
  },
  {
    key: 'day',
    label: '今天 vs 昨天',
    metrics: [
      { name: '营收', current: timeData.value.day.currentRevenue, previous: timeData.value.day.previousRevenue, growth: timeData.value.day.revenueGrowth },
      { name: '收款', current: timeData.value.day.currentCollection, previous: timeData.value.day.previousCollection, growth: timeData.value.day.collectionGrowth },
      { name: '退款', current: timeData.value.day.currentRefund, previous: timeData.value.day.previousRefund, growth: timeData.value.day.refundGrowth },
      { name: '应收', current: timeData.value.day.currentReceivable, previous: timeData.value.day.previousReceivable, growth: timeData.value.day.receivableGrowth }
    ]
  }
])

const formatMoney = (val) => {
  if (val === null || val === undefined) return '0.00'
  return Number(val).toFixed(2)
}

const getOccupancyColor = (rate) => {
  if (rate >= 80) return '#67c23a'
  if (rate >= 50) return '#e6a23c'
  return '#f56c6c'
}

const getNetRateColor = (rate) => {
  if (rate >= 80) return '#67c23a'
  if (rate >= 50) return '#409eff'
  return '#f56c6c'
}

const loadData = async () => {
  loading.value = true
  try {
    const res = await api.finance.advanced.comparison({ type: dimension.value })
    if (res.code === 200) {
      const data = res.data || {}
      if (dimension.value === 'time') {
        timeData.value = data.timeComparison || timeData.value
      } else if (dimension.value === 'target') {
        targetData.value = data.targetComparison || targetData.value
        await nextTick()
        renderTargetChart()
      } else if (dimension.value === 'roomType') {
        roomTypeData.value = data.roomTypeComparison || []
        await nextTick()
        renderRoomTypeChart()
      } else if (dimension.value === 'channel') {
        channelData.value = data.channelComparison || []
        await nextTick()
        renderChannelChart()
      }
    }
  } catch {
    ElMessage.error('加载数据失败')
  } finally {
    loading.value = false
  }
}

const handleDimensionChange = () => {
  disposeCharts()
  loadData()
}

const disposeCharts = () => {
  if (targetChart) { targetChart.dispose(); targetChart = null }
  if (roomTypeChart) { roomTypeChart.dispose(); roomTypeChart = null }
  if (channelChart) { channelChart.dispose(); channelChart = null }
}

const renderTargetChart = () => {
  if (!targetChartRef.value) return
  targetChart = echarts.init(targetChartRef.value)
  const monthly = targetData.value.monthlyTarget || []
  targetChart.setOption({
    title: { text: '月度实际 vs 目标', left: 'center', textStyle: { fontSize: 14 } },
    tooltip: { trigger: 'axis' },
    legend: { bottom: 0, data: ['实际营收', '目标营收'] },
    grid: { top: 40, bottom: 40, left: 60, right: 20 },
    xAxis: { type: 'category', data: monthly.map(i => i.month) },
    yAxis: { type: 'value', axisLabel: { formatter: v => '¥' + (v / 10000).toFixed(0) + '万' } },
    series: [
      { name: '实际营收', type: 'bar', data: monthly.map(i => i.actual), itemStyle: { color: '#409eff' } },
      { name: '目标营收', type: 'bar', data: monthly.map(i => i.target), itemStyle: { color: '#67c23a' } }
    ]
  })
}

const renderRoomTypeChart = () => {
  if (!roomTypeChartRef.value) return
  roomTypeChart = echarts.init(roomTypeChartRef.value)
  const data = roomTypeData.value || []
  roomTypeChart.setOption({
    title: { text: '房型营收对比', left: 'center', textStyle: { fontSize: 14 } },
    tooltip: { trigger: 'axis' },
    grid: { top: 40, bottom: 20, left: 60, right: 20 },
    xAxis: { type: 'category', data: data.map(i => i.roomTypeName) },
    yAxis: { type: 'value', axisLabel: { formatter: v => '¥' + (v / 10000).toFixed(0) + '万' } },
    series: [
      { name: '营收', type: 'bar', data: data.map(i => i.revenue), itemStyle: { color: '#409eff' }, barWidth: '40%' }
    ]
  })
}

const renderChannelChart = () => {
  if (!channelChartRef.value) return
  channelChart = echarts.init(channelChartRef.value)
  const data = channelData.value || []
  channelChart.setOption({
    title: { text: '渠道营收对比', left: 'center', textStyle: { fontSize: 14 } },
    tooltip: { trigger: 'axis' },
    grid: { top: 40, bottom: 20, left: 60, right: 20 },
    xAxis: { type: 'category', data: data.map(i => i.channelName) },
    yAxis: { type: 'value', axisLabel: { formatter: v => '¥' + (v / 10000).toFixed(0) + '万' } },
    series: [
      { name: '营收', type: 'bar', data: data.map(i => i.revenue), itemStyle: { color: '#409eff' }, barWidth: '40%' }
    ]
  })
}

const handleExport = () => {
  let csv = '\uFEFF'
  if (dimension.value === 'time') {
    csv += '对比维度,指标,当前值,对比值,增长率\n'
    timeCards.value.forEach(card => {
      card.metrics.forEach(m => {
        csv += `${card.label},${m.name},${m.current},${m.previous},${m.growth}\n`
      })
    })
  } else if (dimension.value === 'target') {
    csv += '实际营收,目标营收,完成率,差距金额\n'
    csv += `${targetData.value.actualRevenue},${targetData.value.targetRevenue},${targetData.value.completionRate},${targetData.value.gapAmount}\n`
  } else if (dimension.value === 'roomType') {
    csv += '房型名称,营收,入住率,ADR,营收占比\n'
    roomTypeData.value.forEach(i => {
      csv += `${i.roomTypeName},${i.revenue},${i.occupancyRate},${i.adr},${i.revenuePercentage}\n`
    })
  } else if (dimension.value === 'channel') {
    csv += '渠道名称,营收,佣金成本,净收益,净收益率\n'
    channelData.value.forEach(i => {
      csv += `${i.channelName},${i.revenue},${i.commissionCost},${i.netRevenue},${i.netRevenueRate}\n`
    })
  }
  const blob = new Blob([csv], { type: 'text/csv;charset=utf-8;' })
  const link = document.createElement('a')
  link.href = URL.createObjectURL(blob)
  const names = { time: '时间对比', target: '目标对比', roomType: '房型对比', channel: '渠道对比' }
  link.download = `对比分析_${names[dimension.value]}_${new Date().toISOString().slice(0, 10)}.csv`
  link.click()
  URL.revokeObjectURL(link.href)
  ElMessage.success('导出成功')
}

const handleResize = () => {
  targetChart && targetChart.resize()
  roomTypeChart && roomTypeChart.resize()
  channelChart && channelChart.resize()
}

onMounted(() => {
  loadData()
  window.addEventListener('resize', handleResize)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize)
  disposeCharts()
})
</script>

<style scoped>
.comparison-analysis {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.title {
  font-size: 16px;
  font-weight: 600;
}

.dimension-group {
  margin-bottom: 20px;
}

.content-area {
  min-height: 300px;
}

.time-section {
  margin-top: 8px;
}

.compare-card {
  height: 100%;
}

.compare-card :deep(.el-card__header) {
  padding: 12px 16px;
  background: #f5f7fa;
}

.compare-card-title {
  font-size: 15px;
  font-weight: 600;
  color: #303133;
}

.metric-row {
  padding: 10px 0;
  border-bottom: 1px solid #f0f0f0;
}

.metric-row:last-child {
  border-bottom: none;
}

.metric-name {
  font-size: 13px;
  color: #909399;
  margin-bottom: 6px;
}

.metric-values {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 6px;
}

.metric-values .current {
  font-size: 16px;
  font-weight: 700;
  color: #303133;
}

.metric-values .vs {
  font-size: 12px;
  color: #c0c4cc;
}

.metric-values .previous {
  font-size: 14px;
  color: #909399;
}

.growth-row {
  display: flex;
  align-items: center;
}

.target-section {
  margin-top: 8px;
}

.stat-card {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
  border-radius: 8px;
  background: #f5f7fa;
  min-height: 120px;
}

.stat-card.blue {
  background: linear-gradient(135deg, #ecf5ff 0%, #d9ecff 100%);
}

.stat-card.green {
  background: linear-gradient(135deg, #f0f9eb 0%, #e1f3d8 100%);
}

.stat-card.red {
  background: linear-gradient(135deg, #fef0f0 0%, #fde2e2 100%);
}

.stat-card.orange {
  background: linear-gradient(135deg, #fdf6ec 0%, #faecd8 100%);
}

.stat-card .stat-info {
  text-align: center;
}

.stat-info .stat-value {
  font-size: 22px;
  font-weight: 700;
  color: #303133;
  line-height: 1.3;
}

.stat-info .stat-label {
  font-size: 13px;
  color: #909399;
  margin-top: 4px;
}

.stat-card.red .stat-value {
  color: #f56c6c;
}

.completion-progress {
  margin-top: 8px;
}

.chart-container {
  width: 100%;
  height: 400px;
  margin-top: 20px;
}

.price {
  color: #409eff;
  font-weight: 600;
}

.cost {
  color: #f56c6c;
  font-weight: 600;
}

.percent-cell {
  display: flex;
  align-items: center;
  gap: 10px;
}

.percent-cell :deep(.el-progress) {
  flex: 1;
}

.percent-text {
  font-size: 13px;
  font-weight: 500;
  color: #606266;
  min-width: 50px;
  text-align: right;
}
</style>
