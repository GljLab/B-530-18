<template>
  <div class="member-value-analysis">
    <el-card shadow="never" class="overview-cards">
      <template #header>
        <span class="card-title">核心指标概览</span>
      </template>
      <el-row :gutter="16">
        <el-col :span="6">
          <div class="stat-card member">
            <div class="stat-icon">
              <el-icon :size="28"><UserFilled /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ memberVsNonMember.totalMembers || 0 }}</div>
              <div class="stat-label">会员总数</div>
            </div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="stat-card revenue">
            <div class="stat-icon">
              <el-icon :size="28"><Money /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">¥{{ formatNumber(revenueContribution.memberTotalRevenue) }}</div>
              <div class="stat-label">会员总营收</div>
            </div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="stat-card ratio">
            <div class="stat-icon">
              <el-icon :size="28"><PieChart /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ formatPercent(revenueContribution.revenueRatio) }}%</div>
              <div class="stat-label">营收占比</div>
            </div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="stat-card ltv">
            <div class="stat-icon">
              <el-icon :size="28"><TrendCharts /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">¥{{ formatNumber(ltvAnalysis.avgLTV) }}</div>
              <div class="stat-label">人均LTV</div>
            </div>
          </div>
        </el-col>
      </el-row>
    </el-card>

    <el-row :gutter="16" class="charts-row">
      <el-col :span="12">
        <el-card shadow="never">
          <template #header>
            <span class="chart-title">会员 vs 非会员对比</span>
          </template>
          <div ref="compareChartRef" class="bar-chart"></div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card shadow="never">
          <template #header>
            <span class="chart-title">近12个月会员营收占比趋势</span>
          </template>
          <div ref="revenueTrendChartRef" class="line-chart"></div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="16" class="charts-row">
      <el-col :span="10">
        <el-card shadow="never">
          <template #header>
            <span class="chart-title">各等级会员平均LTV</span>
          </template>
          <div ref="ltvByLevelChartRef" class="bar-chart"></div>
        </el-card>
      </el-col>
      <el-col :span="14">
        <el-card shadow="never">
          <template #header>
            <span class="chart-title">LTV分布</span>
          </template>
          <div ref="ltvDistChartRef" class="bar-chart"></div>
        </el-card>
      </el-col>
    </el-row>

    <el-card shadow="never" class="detail-card">
      <template #header>
        <span class="chart-title">会员与非会员详细对比</span>
      </template>
      <el-table :data="compareTableData" stripe border style="width: 100%">
        <el-table-column prop="metric" label="指标" width="180" />
        <el-table-column label="会员" align="center">
          <template #default="{ row }">
            <span style="color: #409eff; font-weight: 600">{{ row.memberValue }}</span>
          </template>
        </el-table-column>
        <el-table-column label="非会员" align="center">
          <template #default="{ row }">
            <span style="color: #909399">{{ row.nonMemberValue }}</span>
          </template>
        </el-table-column>
        <el-table-column label="差异" align="center">
          <template #default="{ row }">
            <span :style="{ color: row.diff > 0 ? '#67c23a' : '#f56c6c', fontWeight: 600 }">
              {{ row.diff > 0 ? '+' : '' }}{{ row.diff }}
              <span v-if="row.diffPercent">({{ row.diffPercent }}%)</span>
            </span>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick, onUnmounted, computed } from 'vue'
import * as echarts from 'echarts'
import { UserFilled, Money, PieChart, TrendCharts } from '@element-plus/icons-vue'
import api from '@/api'

const memberVsNonMember = ref({})
const revenueContribution = ref({})
const ltvAnalysis = ref({})

const compareChartRef = ref(null)
const revenueTrendChartRef = ref(null)
const ltvByLevelChartRef = ref(null)
const ltvDistChartRef = ref(null)

let compareChart = null
let revenueTrendChart = null
let ltvByLevelChart = null
let ltvDistChart = null

const compareTableData = computed(() => {
  const data = memberVsNonMember.value
  return [
    {
      metric: '平均每单消费金额',
      memberValue: '¥' + formatNumber(data.memberAvgPerOrder),
      nonMemberValue: '¥' + formatNumber(data.nonMemberAvgPerOrder),
      diff: '¥' + formatNumber(data.avgDiff),
      diffPercent: formatPercent(data.avgDiffPercent)
    },
    {
      metric: '平均入住次数',
      memberValue: data.memberAvgStays || 0,
      nonMemberValue: data.nonMemberAvgStays || 0,
      diff: data.avgStaysDiff || 0,
      diffPercent: ''
    },
    {
      metric: '复购率',
      memberValue: formatPercent(data.memberRepeatRate) + '%',
      nonMemberValue: formatPercent(data.nonMemberRepeatRate) + '%',
      diff: (data.repeatRateDiff > 0 ? '+' : '') + formatPercent(data.repeatRateDiff) + '%',
      diffPercent: ''
    }
  ]
})

const formatNumber = (num) => {
  if (!num) return '0'
  return Number(num).toLocaleString('zh-CN')
}

const formatPercent = (num) => {
  if (!num) return '0'
  return Number(num).toFixed(2)
}

const loadData = async () => {
  try {
    const [vsRes, revenueRes, ltvRes] = await Promise.all([
      api.memberAnalytics.getVsNonMember(),
      api.memberAnalytics.getRevenueContribution(),
      api.memberAnalytics.getLTVAnalysis()
    ])

    if (vsRes.code === 200) memberVsNonMember.value = vsRes.data || {}
    if (revenueRes.code === 200) revenueContribution.value = revenueRes.data || {}
    if (ltvRes.code === 200) ltvAnalysis.value = ltvRes.data || {}

    await nextTick()
    renderCompareChart()
    renderRevenueTrendChart()
    renderLTVByLevelChart()
    renderLTVDistChart()
  } catch (e) {
    console.error(e)
  }
}

const renderCompareChart = () => {
  if (!compareChartRef.value) return
  if (!compareChart) {
    compareChart = echarts.init(compareChartRef.value)
  }

  const data = memberVsNonMember.value

  compareChart.setOption({
    tooltip: {
      trigger: 'axis',
      axisPointer: { type: 'shadow' }
    },
    legend: {
      data: ['会员', '非会员'],
      bottom: 0
    },
    grid: {
      left: 60,
      right: 30,
      top: 30,
      bottom: 50
    },
    xAxis: {
      type: 'category',
      data: ['平均消费金额(元)', '平均入住次数', '复购率(%)'],
      axisLabel: {
        color: '#606266',
        fontSize: 12
      }
    },
    yAxis: {
      type: 'value',
      axisLabel: { color: '#909399', fontSize: 11 },
      splitLine: { lineStyle: { type: 'dashed', color: '#f0f0f0' } }
    },
    series: [
      {
        name: '会员',
        type: 'bar',
        data: [
          data.memberAvgPerOrder || 0,
          data.memberAvgStays || 0,
          data.memberRepeatRate || 0
        ],
        itemStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: '#667eea' },
            { offset: 1, color: '#764ba2' }
          ]),
          borderRadius: [4, 4, 0, 0]
        },
        barWidth: 30
      },
      {
        name: '非会员',
        type: 'bar',
        data: [
          data.nonMemberAvgPerOrder || 0,
          data.nonMemberAvgStays || 0,
          data.nonMemberRepeatRate || 0
        ],
        itemStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: '#909399' },
            { offset: 1, color: '#606266' }
          ]),
          borderRadius: [4, 4, 0, 0]
        },
        barWidth: 30
      }
    ]
  })
}

const renderRevenueTrendChart = () => {
  if (!revenueTrendChartRef.value) return
  if (!revenueTrendChart) {
    revenueTrendChart = echarts.init(revenueTrendChartRef.value)
  }

  const trend = revenueContribution.value.monthlyTrend || []
  const months = trend.map(item => item.month)
  const ratios = trend.map(item => item.ratio || 0)

  revenueTrendChart.setOption({
    tooltip: {
      trigger: 'axis',
      formatter: '{b}<br/>会员营收占比: {c}%'
    },
    grid: {
      left: 60,
      right: 30,
      top: 30,
      bottom: 40
    },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: months,
      axisLabel: { color: '#909399', fontSize: 11, rotate: 30 }
    },
    yAxis: {
      type: 'value',
      name: '占比(%)',
      nameTextStyle: { color: '#909399', fontSize: 12 },
      axisLabel: { color: '#909399', fontSize: 11, formatter: '{value}%' },
      splitLine: { lineStyle: { type: 'dashed', color: '#f0f0f0' } }
    },
    series: [{
      type: 'line',
      data: ratios,
      smooth: true,
      symbol: 'circle',
      symbolSize: 8,
      lineStyle: {
        width: 3,
        color: new echarts.graphic.LinearGradient(0, 0, 1, 0, [
          { offset: 0, color: '#667eea' },
          { offset: 1, color: '#f093fb' }
        ])
      },
      itemStyle: { color: '#667eea', borderColor: '#fff', borderWidth: 2 },
      areaStyle: {
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: 'rgba(102, 126, 234, 0.3)' },
          { offset: 1, color: 'rgba(240, 147, 251, 0.05)' }
        ])
      },
      label: {
        show: true,
        position: 'top',
        color: '#606266',
        fontSize: 11,
        formatter: '{c}%'
      }
    }]
  })
}

const renderLTVByLevelChart = () => {
  if (!ltvByLevelChartRef.value) return
  if (!ltvByLevelChart) {
    ltvByLevelChart = echarts.init(ltvByLevelChartRef.value)
  }

  const ltvByLevel = ltvAnalysis.value.ltvByLevel || []
  const names = ltvByLevel.map(item => item.levelName)
  const values = ltvByLevel.map(item => item.avgLTV || 0)
  const colors = ltvByLevel.map(item => item.levelColor)

  ltvByLevelChart.setOption({
    tooltip: {
      trigger: 'axis',
      axisPointer: { type: 'shadow' },
      formatter: '{b}<br/>平均LTV: ¥{c}'
    },
    grid: {
      left: 80,
      right: 30,
      top: 30,
      bottom: 30
    },
    xAxis: {
      type: 'value',
      axisLabel: { color: '#909399', fontSize: 11 },
      splitLine: { lineStyle: { type: 'dashed', color: '#f0f0f0' } }
    },
    yAxis: {
      type: 'category',
      data: names,
      axisLabel: { color: '#606266', fontSize: 12 }
    },
    series: [{
      type: 'bar',
      data: values.map((value, index) => ({
        value,
        itemStyle: {
          color: colors[index] || '#409eff',
          borderRadius: [0, 4, 4, 0]
        }
      })),
      barWidth: 24,
      label: {
        show: true,
        position: 'right',
        color: '#606266',
        fontSize: 11,
        formatter: '¥{c}'
      }
    }]
  })
}

const renderLTVDistChart = () => {
  if (!ltvDistChartRef.value) return
  if (!ltvDistChart) {
    ltvDistChart = echarts.init(ltvDistChartRef.value)
  }

  const distribution = ltvAnalysis.value.ltvDistribution || []
  const ranges = distribution.map(item => item.range)
  const counts = distribution.map(item => item.count)

  ltvDistChart.setOption({
    tooltip: {
      trigger: 'axis',
      axisPointer: { type: 'shadow' },
      formatter: function(params) {
        const data = params[0]
        const item = distribution[data.dataIndex]
        return `${item.range}<br/>会员数: ${item.count} 人<br/>占比: ${formatPercent(item.percentage)}%`
      }
    },
    grid: {
      left: 60,
      right: 30,
      top: 30,
      bottom: 30
    },
    xAxis: {
      type: 'category',
      data: ranges,
      axisLabel: { color: '#606266', fontSize: 11 }
    },
    yAxis: {
      type: 'value',
      name: '会员数',
      nameTextStyle: { color: '#909399', fontSize: 12 },
      axisLabel: { color: '#909399', fontSize: 11 },
      splitLine: { lineStyle: { type: 'dashed', color: '#f0f0f0' } }
    },
    series: [{
      type: 'bar',
      data: counts,
      itemStyle: {
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: '#43e97b' },
          { offset: 1, color: '#38f9d7' }
        ]),
        borderRadius: [4, 4, 0, 0]
      },
      barWidth: 40,
      label: {
        show: true,
        position: 'top',
        color: '#606266',
        fontSize: 12,
        fontWeight: 600
      }
    }]
  })
}

const handleResize = () => {
  compareChart?.resize()
  revenueTrendChart?.resize()
  ltvByLevelChart?.resize()
  ltvDistChart?.resize()
}

onMounted(() => {
  loadData()
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  compareChart?.dispose()
  revenueTrendChart?.dispose()
  ltvByLevelChart?.dispose()
  ltvDistChart?.dispose()
})
</script>

<style scoped>
.member-value-analysis {
  padding: 16px;
}

.overview-cards {
  margin-bottom: 16px;
}

.card-title {
  font-weight: 600;
  color: #303133;
  font-size: 15px;
}

.stat-card {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 20px;
  border-radius: 10px;
  color: #fff;
}

.stat-card.member {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.stat-card.revenue {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
}

.stat-card.ratio {
  background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
}

.stat-card.ltv {
  background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%);
}

.stat-icon {
  width: 56px;
  height: 56px;
  border-radius: 12px;
  background: rgba(255, 255, 255, 0.2);
  display: flex;
  align-items: center;
  justify-content: center;
}

.stat-info {
  flex: 1;
}

.stat-value {
  font-size: 24px;
  font-weight: 700;
  line-height: 1.2;
}

.stat-label {
  font-size: 13px;
  opacity: 0.9;
  margin-top: 4px;
}

.charts-row {
  margin-bottom: 16px;
}

.chart-title {
  font-weight: 600;
  color: #303133;
  font-size: 14px;
}

.bar-chart,
.line-chart {
  height: 320px;
  width: 100%;
}

.detail-card {
  margin-bottom: 16px;
}
</style>
