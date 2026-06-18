<template>
  <div class="collection-trend">
    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <span class="title">收款趋势分析</span>
          <div class="days-selector">
            <el-button
              v-for="d in daysOptions"
              :key="d.value"
              :type="selectedDays === d.value ? 'primary' : ''"
              size="small"
              @click="changeDays(d.value)"
            >
              {{ d.label }}
            </el-button>
          </div>
        </div>
      </template>

      <el-row :gutter="16" class="summary-row">
        <el-col :span="6">
          <div class="stat-card blue">
            <div class="stat-icon">
              <el-icon :size="28"><Wallet /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">¥{{ formatMoney(statsData.totalAmount) }}</div>
              <div class="stat-label">本期收款总额</div>
            </div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="stat-card green">
            <div class="stat-icon">
              <el-icon :size="28"><Document /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ statsData.totalCount || 0 }}</div>
              <div class="stat-label">本期收款笔数</div>
            </div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="stat-card orange">
            <div class="stat-icon">
              <el-icon :size="28"><TrendCharts /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">¥{{ formatMoney(statsData.avgDaily) }}</div>
              <div class="stat-label">日均收款</div>
            </div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="stat-card purple">
            <div class="stat-icon">
              <el-icon :size="28"><DataAnalysis /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ statsData.momGrowth ?? '--' }}</div>
              <div class="stat-label">环比增长率</div>
            </div>
          </div>
        </el-col>
      </el-row>

      <el-row :gutter="16" style="margin-top: 20px">
        <el-col :span="24">
          <el-card shadow="never" class="chart-card">
            <template #header>
              <span class="chart-title">收款趋势</span>
            </template>
            <div ref="trendChartRef" class="chart-container"></div>
          </el-card>
        </el-col>
      </el-row>

      <el-row :gutter="16" style="margin-top: 20px">
        <el-col :span="24">
          <el-card shadow="never" class="chart-card">
            <template #header>
              <span class="chart-title">支付方式趋势</span>
            </template>
            <div ref="methodChartRef" class="chart-container"></div>
          </el-card>
        </el-col>
      </el-row>

      <el-row :gutter="16" style="margin-top: 20px">
        <el-col :span="12">
          <el-card shadow="never" class="chart-card">
            <template #header>
              <span class="chart-title">周对比</span>
            </template>
            <div class="week-comparison">
              <div class="week-item">
                <div class="week-label">本周收款</div>
                <div class="week-value">¥{{ formatMoney(weekData.thisWeek) }}</div>
              </div>
              <div class="week-divider">
                <el-icon :size="20"><Right /></el-icon>
              </div>
              <div class="week-item">
                <div class="week-label">上周收款</div>
                <div class="week-value">¥{{ formatMoney(weekData.lastWeek) }}</div>
              </div>
              <div class="week-growth" :class="weekData.growthRate >= 0 ? 'up' : 'down'">
                <span>{{ weekData.growthRate !== null && weekData.growthRate !== undefined ? (weekData.growthRate >= 0 ? '+' : '') + weekData.growthRate + '%' : '--' }}</span>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="12">
          <el-card shadow="never" class="chart-card">
            <template #header>
              <span class="chart-title">同比环比分析</span>
            </template>
            <div class="yoy-mom-section">
              <div class="compare-item">
                <div class="compare-label">同比增长率</div>
                <div class="compare-value">{{ yoyMomData.yoyGrowth ?? '--' }}</div>
              </div>
              <div class="compare-divider"></div>
              <div class="compare-item">
                <div class="compare-label">环比增长率</div>
                <div class="compare-value">{{ yoyMomData.momGrowth ?? '--' }}</div>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import { Wallet, Document, TrendCharts, DataAnalysis, Right } from '@element-plus/icons-vue'
import * as echarts from 'echarts'
import api from '@/api'

const loading = ref(false)
const selectedDays = ref(7)
const daysOptions = [
  { label: '近7日', value: 7 },
  { label: '近15日', value: 15 },
  { label: '近30日', value: 30 }
]

const statsData = ref({
  totalAmount: 0,
  totalCount: 0,
  avgDaily: 0,
  momGrowth: null
})

const trendData = ref([])
const weekData = ref({
  thisWeek: 0,
  lastWeek: 0,
  growthRate: null
})
const yoyMomData = ref({
  yoyGrowth: null,
  momGrowth: null
})

const trendChartRef = ref(null)
const methodChartRef = ref(null)
let trendChart = null
let methodChart = null

const formatMoney = (val) => {
  if (val === null || val === undefined) return '0.00'
  return Number(val).toFixed(2)
}

const changeDays = (days) => {
  selectedDays.value = days
  loadData()
}

const initTrendChart = () => {
  if (!trendChartRef.value) return
  if (trendChart) trendChart.dispose()
  trendChart = echarts.init(trendChartRef.value)
  const dates = trendData.value.map(item => item.date)
  const amounts = trendData.value.map(item => item.totalAmount)
  const option = {
    tooltip: {
      trigger: 'axis',
      formatter: (params) => {
        const p = params[0]
        return `${p.axisValue}<br/>收款总额: ¥${formatMoney(p.value)}`
      }
    },
    grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
    xAxis: {
      type: 'category',
      data: dates,
      axisLabel: { fontSize: 12 },
      boundaryGap: false
    },
    yAxis: {
      type: 'value',
      axisLabel: {
        formatter: (val) => '¥' + (val >= 10000 ? (val / 10000).toFixed(1) + '万' : val)
      }
    },
    series: [
      {
        name: '收款总额',
        type: 'line',
        data: amounts,
        smooth: true,
        lineStyle: { width: 3, color: '#409eff' },
        itemStyle: { color: '#409eff' },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(64,158,255,0.3)' },
            { offset: 1, color: 'rgba(64,158,255,0.05)' }
          ])
        }
      }
    ]
  }
  trendChart.setOption(option)
}

const initMethodChart = () => {
  if (!methodChartRef.value) return
  if (methodChart) methodChart.dispose()
  methodChart = echarts.init(methodChartRef.value)
  const dates = trendData.value.map(item => item.date)
  const cashData = trendData.value.map(item => item.cashAmount)
  const cardData = trendData.value.map(item => item.cardAmount)
  const mobileData = trendData.value.map(item => item.mobileAmount)
  const option = {
    tooltip: {
      trigger: 'axis',
      formatter: (params) => {
        let str = params[0].axisValue + '<br/>'
        params.forEach(p => {
          str += `${p.marker}${p.seriesName}: ¥${formatMoney(p.value)}<br/>`
        })
        return str
      }
    },
    legend: {
      data: ['现金', '刷卡', '移动支付'],
      top: 0
    },
    grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
    xAxis: {
      type: 'category',
      data: dates,
      boundaryGap: false
    },
    yAxis: {
      type: 'value',
      axisLabel: {
        formatter: (val) => '¥' + (val >= 10000 ? (val / 10000).toFixed(1) + '万' : val)
      }
    },
    series: [
      {
        name: '现金',
        type: 'line',
        data: cashData,
        smooth: true,
        lineStyle: { width: 2 },
        itemStyle: { color: '#67c23a' }
      },
      {
        name: '刷卡',
        type: 'line',
        data: cardData,
        smooth: true,
        lineStyle: { width: 2 },
        itemStyle: { color: '#409eff' }
      },
      {
        name: '移动支付',
        type: 'line',
        data: mobileData,
        smooth: true,
        lineStyle: { width: 2 },
        itemStyle: { color: '#e6a23c' }
      }
    ]
  }
  methodChart.setOption(option)
}

const loadData = async () => {
  loading.value = true
  try {
    const res = await api.finance.advanced.collectionTrend({ days: selectedDays.value })
    if (res.code === 200) {
      const data = res.data || {}
      statsData.value = {
        totalAmount: data.totalAmount || 0,
        totalCount: data.totalCount || 0,
        avgDaily: data.avgDaily || 0,
        momGrowth: data.momGrowth ?? null
      }
      trendData.value = data.trendData || []
      weekData.value = {
        thisWeek: data.weekComparison?.thisWeek || 0,
        lastWeek: data.weekComparison?.lastWeek || 0,
        growthRate: data.weekComparison?.growthRate ?? null
      }
      yoyMomData.value = {
        yoyGrowth: data.yoyMom?.yoyGrowth ?? null,
        momGrowth: data.yoyMom?.momGrowth ?? null
      }
      await nextTick()
      initTrendChart()
      initMethodChart()
    }
  } catch {
    ElMessage.error('加载数据失败')
  } finally {
    loading.value = false
  }
}

onMounted(async () => {
  await loadData()
})
</script>

<style scoped>
.collection-trend {
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

.days-selector {
  display: flex;
  gap: 8px;
}

.summary-row {
  margin-top: 8px;
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

.stat-card.orange .stat-icon {
  background: linear-gradient(135deg, #e6a23c 0%, #ebb563 100%);
}

.stat-card.purple .stat-icon {
  background: linear-gradient(135deg, #9b59b6 0%, #b07cd8 100%);
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

.chart-card {
  min-height: 360px;
}

.chart-title {
  font-size: 14px;
  font-weight: 600;
}

.chart-container {
  width: 100%;
  height: 300px;
}

.week-comparison {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 24px;
  padding: 30px 0;
}

.week-item {
  text-align: center;
}

.week-label {
  font-size: 13px;
  color: #909399;
  margin-bottom: 8px;
}

.week-value {
  font-size: 24px;
  font-weight: 700;
  color: #303133;
}

.week-divider {
  color: #c0c4cc;
}

.week-growth {
  font-size: 20px;
  font-weight: 700;
}

.week-growth.up {
  color: #67c23a;
}

.week-growth.down {
  color: #f56c6c;
}

.yoy-mom-section {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 40px;
  padding: 30px 0;
}

.compare-item {
  text-align: center;
}

.compare-label {
  font-size: 13px;
  color: #909399;
  margin-bottom: 8px;
}

.compare-value {
  font-size: 24px;
  font-weight: 700;
  color: #303133;
}

.compare-divider {
  width: 1px;
  height: 40px;
  background: #dcdfe6;
}
</style>
