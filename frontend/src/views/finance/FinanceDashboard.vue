<template>
  <div class="finance-dashboard">
    <el-row :gutter="16" class="section-row">
      <el-col :span="6">
        <div class="stat-card blue">
          <div class="stat-icon">
            <el-icon :size="28"><Wallet /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">¥{{ formatMoney(dashboard.todayData.revenue) }}</div>
            <div class="stat-label">今日营收</div>
          </div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card green">
          <div class="stat-icon">
            <el-icon :size="28"><Document /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">¥{{ formatMoney(dashboard.todayData.collection) }}</div>
            <div class="stat-label">今日收款</div>
          </div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card red">
          <div class="stat-icon">
            <el-icon :size="28"><RefreshLeft /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">¥{{ formatMoney(dashboard.todayData.refund) }}</div>
            <div class="stat-label">今日退款</div>
          </div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card orange">
          <div class="stat-icon">
            <el-icon :size="28"><List /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">¥{{ formatMoney(dashboard.todayData.newReceivable) }}</div>
            <div class="stat-label">今日新增应收</div>
          </div>
        </div>
      </el-col>
    </el-row>

    <el-row :gutter="16" class="section-row">
      <el-col :span="6">
        <el-card shadow="never" class="month-card">
          <div class="month-title">本月营收 / 目标</div>
          <div class="month-values">
            <span class="month-amount">¥{{ formatMoney(dashboard.monthData.revenue) }}</span>
            <span class="month-target"> / ¥{{ formatMoney(dashboard.monthData.target) }}</span>
          </div>
          <el-progress
            :percentage="dashboard.monthData.completionRate || 0"
            :stroke-width="10"
            :color="getProgressColor(dashboard.monthData.completionRate || 0)"
            style="margin-top: 10px"
          />
          <div class="month-rate">完成率 {{ (dashboard.monthData.completionRate || 0).toFixed(1) }}%</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <div class="stat-card green">
          <div class="stat-icon">
            <el-icon :size="28"><Wallet /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">¥{{ formatMoney(dashboard.monthData.collection) }}</div>
            <div class="stat-label">本月收款</div>
          </div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card red">
          <div class="stat-icon">
            <el-icon :size="28"><RefreshLeft /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">¥{{ formatMoney(dashboard.monthData.refund) }}</div>
            <div class="stat-label">本月退款</div>
          </div>
        </div>
      </el-col>
      <el-col :span="6">
        <el-card shadow="never" class="month-card">
          <div class="month-row">
            <div class="month-block">
              <div class="month-title">应收总额</div>
              <div class="month-amount" style="color: #e6a23c">¥{{ formatMoney(dashboard.monthData.receivableTotal) }}</div>
            </div>
            <el-divider direction="vertical" />
            <div class="month-block">
              <div class="month-title">坏账总额</div>
              <div class="month-amount" style="color: #f56c6c">¥{{ formatMoney(dashboard.monthData.badDebtTotal) }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="12" class="section-row">
      <el-col :span="4" v-for="item in keyIndicatorList" :key="item.label">
        <div class="mini-card" :style="{ borderTop: `3px solid ${item.color}` }">
          <div class="mini-value" :style="{ color: item.color }">{{ item.value }}</div>
          <div class="mini-label">{{ item.label }}</div>
        </div>
      </el-col>
    </el-row>

    <el-row :gutter="16" class="section-row">
      <el-col :span="8">
        <el-card shadow="never">
          <template #header>
            <span class="chart-title">近7日营收趋势</span>
          </template>
          <div ref="revenueChartRef" class="mini-chart"></div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="never">
          <template #header>
            <span class="chart-title">近7日入住率趋势</span>
          </template>
          <div ref="occupancyChartRef" class="mini-chart"></div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="never">
          <template #header>
            <span class="chart-title">近7日现金流趋势</span>
          </template>
          <div ref="cashFlowChartRef" class="mini-chart"></div>
        </el-card>
      </el-col>
    </el-row>

    <el-card shadow="never" class="section-row">
      <template #header>
        <div class="card-header">
          <span class="title">预警提示</span>
        </div>
      </template>
      <template v-if="dashboard.warnings.length > 0">
        <el-alert
          v-for="(warn, index) in dashboard.warnings"
          :key="index"
          :title="warn.type"
          :description="warn.message"
          :type="warn.level === 'danger' ? 'error' : 'warning'"
          show-icon
          :closable="false"
          style="margin-bottom: 10px"
        />
      </template>
      <el-result v-else icon="success" title="暂无预警" sub-title="当前财务状况正常" />
    </el-card>

    <el-row :gutter="16" class="section-row">
      <el-col :span="6">
        <div class="stat-card cyan">
          <div class="stat-icon">
            <el-icon :size="28"><Wallet /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">¥{{ formatMoney(dashboard.todayData.creditRecovery) }}</div>
            <div class="stat-label">今日回款金额</div>
          </div>
        </div>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import { Wallet, Document, RefreshLeft, List } from '@element-plus/icons-vue'
import * as echarts from 'echarts'
import api from '@/api'

const loading = ref(false)
const revenueChartRef = ref(null)
const occupancyChartRef = ref(null)
const cashFlowChartRef = ref(null)

const dashboard = reactive({
  todayData: { revenue: 0, collection: 0, refund: 0, newReceivable: 0, creditRecovery: 0 },
  monthData: { revenue: 0, target: 0, completionRate: 0, collection: 0, refund: 0, receivableTotal: 0, badDebtTotal: 0 },
  keyIndicators: { revPAR: 0, adr: 0, occupancyRate: 0, grossProfitRate: 0, badDebtRate: 0, turnoverRate: 0 },
  trends: {
    revenueTrend: [],
    occupancyTrend: [],
    cashFlowTrend: []
  },
  warnings: []
})

const keyIndicatorList = computed(() => [
  { label: 'RevPAR', value: `¥${formatMoney(dashboard.keyIndicators.revPAR)}`, color: '#409eff' },
  { label: 'ADR', value: `¥${formatMoney(dashboard.keyIndicators.adr)}`, color: '#67c23a' },
  { label: '入住率', value: `${(dashboard.keyIndicators.occupancyRate || 0).toFixed(1)}%`, color: '#e6a23c' },
  { label: '毛利率', value: dashboard.keyIndicators.grossProfitRate != null ? `${dashboard.keyIndicators.grossProfitRate.toFixed(1)}%` : '--', color: '#909399' },
  { label: '坏账率', value: `${(dashboard.keyIndicators.badDebtRate || 0).toFixed(1)}%`, color: '#f56c6c' },
  { label: '周转率', value: `${(dashboard.keyIndicators.turnoverRate || 0).toFixed(2)}`, color: '#b37feb' }
])

const formatMoney = (val) => {
  if (val === null || val === undefined) return '0.00'
  return Number(val).toFixed(2)
}

const getProgressColor = (percentage) => {
  if (percentage >= 80) return '#67c23a'
  if (percentage >= 50) return '#409eff'
  if (percentage >= 30) return '#e6a23c'
  return '#f56c6c'
}

const initChart = (chartRef, trendData, color, yFormatter) => {
  if (!chartRef) return null
  const chart = echarts.init(chartRef)
  const dates = trendData.map(item => item.date)
  const values = trendData.map(item => item.value)
  chart.setOption({
    grid: { top: 20, right: 20, bottom: 30, left: 50 },
    xAxis: { type: 'category', data: dates, axisLabel: { fontSize: 11 }, boundaryGap: false },
    yAxis: { type: 'value', axisLabel: { fontSize: 11, formatter: yFormatter || '{value}' } },
    tooltip: { trigger: 'axis' },
    series: [{
      type: 'line',
      data: values,
      smooth: true,
      symbol: 'circle',
      symbolSize: 6,
      lineStyle: { color, width: 2 },
      itemStyle: { color },
      areaStyle: { color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
        { offset: 0, color: color + '40' },
        { offset: 1, color: color + '05' }
      ])}
    }]
  })
  return chart
}

const initAllCharts = () => {
  const revenueChart = initChart(revenueChartRef.value, dashboard.trends.revenueTrend, '#409eff', val => `¥${val}`)
  const occupancyChart = initChart(occupancyChartRef.value, dashboard.trends.occupancyTrend, '#e6a23c', val => `${val}%`)
  const cashFlowChart = initChart(cashFlowChartRef.value, dashboard.trends.cashFlowTrend, '#67c23a', val => `¥${val}`)

  const handleResize = () => {
    revenueChart?.resize()
    occupancyChart?.resize()
    cashFlowChart?.resize()
  }
  window.addEventListener('resize', handleResize)
}

const loadData = async () => {
  loading.value = true
  try {
    const res = await api.finance.advanced.dashboard()
    if (res.code === 200) {
      const data = res.data || {}
      if (data.todayData) {
        Object.assign(dashboard.todayData, data.todayData)
      }
      if (data.monthData) {
        Object.assign(dashboard.monthData, data.monthData)
      }
      if (data.keyIndicators) {
        Object.assign(dashboard.keyIndicators, data.keyIndicators)
      }
      if (data.trends) {
        dashboard.trends.revenueTrend = data.trends.revenueTrend || []
        dashboard.trends.occupancyTrend = data.trends.occupancyTrend || []
        dashboard.trends.cashFlowTrend = data.trends.cashFlowTrend || []
      }
      dashboard.warnings = data.warnings || []
      await nextTick()
      initAllCharts()
    }
  } catch {
    ElMessage.error('加载数据失败')
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.finance-dashboard {
  padding: 20px;
}

.section-row {
  margin-bottom: 16px;
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

.stat-card {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 20px;
  border-radius: 8px;
  background: #f5f7fa;
}

.stat-card .stat-icon {
  width: 56px;
  height: 56px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  flex-shrink: 0;
}

.stat-card.blue .stat-icon {
  background: linear-gradient(135deg, #409eff 0%, #66b1ff 100%);
}

.stat-card.green .stat-icon {
  background: linear-gradient(135deg, #67c23a 0%, #85ce61 100%);
}

.stat-card.red .stat-icon {
  background: linear-gradient(135deg, #f56c6c 0%, #f78989 100%);
}

.stat-card.orange .stat-icon {
  background: linear-gradient(135deg, #e6a23c 0%, #ebb563 100%);
}

.stat-card.cyan .stat-icon {
  background: linear-gradient(135deg, #00b8d4 0%, #26c6da 100%);
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

.month-card {
  height: 100%;
}

.month-card :deep(.el-card__body) {
  padding: 16px 20px;
}

.month-title {
  font-size: 13px;
  color: #909399;
  margin-bottom: 6px;
}

.month-values {
  display: flex;
  align-items: baseline;
  gap: 0;
}

.month-amount {
  font-size: 20px;
  font-weight: 700;
  color: #303133;
}

.month-target {
  font-size: 14px;
  color: #909399;
}

.month-rate {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
}

.month-row {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 16px;
}

.month-block {
  flex: 1;
  text-align: center;
}

.month-block .month-amount {
  font-size: 18px;
}

.mini-card {
  background: #f5f7fa;
  border-radius: 8px;
  padding: 16px 12px;
  text-align: center;
}

.mini-value {
  font-size: 18px;
  font-weight: 700;
  line-height: 1.3;
}

.mini-label {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
}

.chart-title {
  font-size: 14px;
  font-weight: 600;
  color: #303133;
}

.mini-chart {
  width: 100%;
  height: 200px;
}
</style>
