<template>
  <div class="level-distribution-analysis">
    <el-card shadow="never" class="overview-cards">
      <template #header>
        <span class="card-title">等级流动概览（本月）</span>
      </template>
      <el-row :gutter="16">
        <el-col :span="6">
          <div class="stat-card upgrade">
            <div class="stat-icon">
              <el-icon :size="28"><Top /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ levelFlow.monthUpgradeCount || 0 }}</div>
              <div class="stat-label">升级人数</div>
              <div class="stat-rate">升级率: {{ formatPercent(levelFlow.upgradeRate) }}%</div>
            </div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="stat-card downgrade">
            <div class="stat-icon">
              <el-icon :size="28"><Bottom /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ levelFlow.monthDowngradeCount || 0 }}</div>
              <div class="stat-label">降级人数</div>
              <div class="stat-rate">降级率: {{ formatPercent(levelFlow.downgradeRate) }}%</div>
            </div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="stat-card remain">
            <div class="stat-icon">
              <el-icon :size="28"><SwitchButton /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ levelFlow.monthRemainCount || 0 }}</div>
              <div class="stat-label">保持原等级</div>
              <div class="stat-rate">留存率: {{ formatPercent(levelFlow.remainRate) }}%</div>
            </div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="stat-card health">
            <div class="stat-icon">
              <el-icon :size="28"><Medal /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ levelHealth.length }}</div>
              <div class="stat-label">健康度评估</div>
              <div class="stat-rate">
                <el-tag :type="overallHealthType" size="small">{{ overallHealth }}</el-tag>
              </div>
            </div>
          </div>
        </el-col>
      </el-row>
    </el-card>

    <el-card shadow="never" class="comparison-card">
      <template #header>
        <span class="chart-title">各等级数据对比</span>
      </template>
      <el-table :data="levelComparison" stripe border style="width: 100%">
        <el-table-column label="等级" width="160">
          <template #default="{ row }">
            <span class="level-badge" :style="{ background: row.levelColor + '20', color: row.levelColor }">
              {{ row.levelIcon }} {{ row.levelName }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="memberCount" label="会员数量" width="100" align="center" />
        <el-table-column label="平均消费" width="120" align="center">
          <template #default="{ row }">
            ¥{{ formatNumber(row.avgSpent) }}
          </template>
        </el-table-column>
        <el-table-column label="平均积分" width="120" align="center">
          <template #default="{ row }">
            {{ formatNumber(row.avgPoints) }}
          </template>
        </el-table-column>
        <el-table-column label="平均入住次数" width="120" align="center">
          <template #default="{ row }">
            {{ row.avgStays || 0 }}
          </template>
        </el-table-column>
        <el-table-column label="复购率" width="100" align="center">
          <template #default="{ row }">
            <span style="color: #67c23a; font-weight: 600">{{ formatPercent(row.repeatRate) }}%</span>
          </template>
        </el-table-column>
        <el-table-column label="营收贡献" align="center">
          <template #default="{ row }">
            <span style="color: #e6a23c; font-weight: 600">¥{{ formatNumber(row.revenueContribution) }}</span>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-row :gutter="16" class="charts-row">
      <el-col :span="12">
        <el-card shadow="never">
          <template #header>
            <span class="chart-title">近12个月升降级趋势</span>
          </template>
          <div ref="flowTrendChartRef" class="line-chart"></div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card shadow="never">
          <template #header>
            <span class="chart-title">各等级会员分布</span>
          </template>
          <div ref="levelPieChartRef" class="pie-chart"></div>
        </el-card>
      </el-col>
    </el-row>

    <el-card shadow="never" class="health-card">
      <template #header>
        <span class="chart-title">等级健康度评估</span>
      </template>
      <el-table :data="levelHealth" stripe border style="width: 100%">
        <el-table-column label="等级" width="160">
          <template #default="{ row }">
            <span class="level-badge" :style="{ background: row.levelColor + '20', color: row.levelColor }">
              {{ row.levelIcon }} {{ row.levelName }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="totalCount" label="会员总数" width="100" align="center" />
        <el-table-column prop="activeCount" label="活跃会员" width="100" align="center">
          <template #default="{ row }">
            <span style="color: #67c23a">{{ row.activeCount }}</span>
          </template>
        </el-table-column>
        <el-table-column label="活跃率" min-width="180">
          <template #default="{ row }">
            <div class="progress-item">
              <el-progress
                :percentage="Number(row.activeRate).toFixed(1)"
                :stroke-width="12"
                :color="getHealthColor(row.activeRate)"
              />
              <span class="percentage-text">{{ formatPercent(row.activeRate) }}%</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="dormantCount" label="沉睡会员" width="100" align="center">
          <template #default="{ row }">
            <span style="color: #f56c6c">{{ row.dormantCount }}</span>
          </template>
        </el-table-column>
        <el-table-column label="沉睡率" width="100" align="center">
          <template #default="{ row }">
            <span style="color: #f56c6c">{{ formatPercent(row.dormantRate) }}%</span>
          </template>
        </el-table-column>
        <el-table-column label="健康度评级" width="120" align="center">
          <template #default="{ row }">
            <el-tag :type="getHealthTagType(row.healthLevel)" size="small">
              {{ row.healthLevel }}
            </el-tag>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick, onUnmounted, computed } from 'vue'
import * as echarts from 'echarts'
import { Top, Bottom, SwitchButton, Medal } from '@element-plus/icons-vue'
import api from '@/api'

const levelComparison = ref([])
const levelFlow = ref({})
const levelHealth = ref([])

const flowTrendChartRef = ref(null)
const levelPieChartRef = ref(null)

let flowTrendChart = null
let levelPieChart = null

const overallHealth = computed(() => {
  if (!levelHealth.value.length) return '健康'
  const avgRate = levelHealth.value.reduce((sum, item) => sum + Number(item.activeRate || 0), 0) / levelHealth.value.length
  if (avgRate >= 60) return '健康'
  if (avgRate >= 40) return '一般'
  return '需关注'
})

const overallHealthType = computed(() => {
  return getHealthTagType(overallHealth.value)
})

const formatNumber = (num) => {
  if (!num) return '0'
  return Number(num).toLocaleString('zh-CN')
}

const formatPercent = (num) => {
  if (!num) return '0'
  return Number(num).toFixed(2)
}

const getHealthColor = (rate) => {
  const r = Number(rate)
  if (r >= 60) return '#67c23a'
  if (r >= 40) return '#e6a23c'
  return '#f56c6c'
}

const getHealthTagType = (level) => {
  if (level === '健康') return 'success'
  if (level === '一般') return 'warning'
  return 'danger'
}

const loadData = async () => {
  try {
    const [compRes, flowRes, healthRes] = await Promise.all([
      api.memberAnalytics.getLevelComparison(),
      api.memberAnalytics.getLevelFlow(),
      api.memberAnalytics.getLevelHealth()
    ])

    if (compRes.code === 200) levelComparison.value = compRes.data || []
    if (flowRes.code === 200) levelFlow.value = flowRes.data || {}
    if (healthRes.code === 200) levelHealth.value = healthRes.data || []

    await nextTick()
    renderFlowTrendChart()
    renderLevelPieChart()
  } catch (e) {
    console.error(e)
  }
}

const renderFlowTrendChart = () => {
  if (!flowTrendChartRef.value) return
  if (!flowTrendChart) {
    flowTrendChart = echarts.init(flowTrendChartRef.value)
  }

  const trend = levelFlow.value.flowTrend || []
  const months = trend.map(item => item.month)
  const upgradeCounts = trend.map(item => item.upgradeCount || 0)
  const downgradeCounts = trend.map(item => item.downgradeCount || 0)

  flowTrendChart.setOption({
    tooltip: {
      trigger: 'axis'
    },
    legend: {
      data: ['升级人数', '降级人数'],
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
      data: months,
      boundaryGap: false,
      axisLabel: { color: '#909399', fontSize: 11, rotate: 30 }
    },
    yAxis: {
      type: 'value',
      name: '人数',
      nameTextStyle: { color: '#909399', fontSize: 12 },
      axisLabel: { color: '#909399', fontSize: 11 },
      splitLine: { lineStyle: { type: 'dashed', color: '#f0f0f0' } }
    },
    series: [
      {
        name: '升级人数',
        type: 'line',
        data: upgradeCounts,
        smooth: true,
        symbol: 'circle',
        symbolSize: 8,
        lineStyle: { width: 3, color: '#67c23a' },
        itemStyle: { color: '#67c23a', borderColor: '#fff', borderWidth: 2 },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(103, 194, 58, 0.3)' },
            { offset: 1, color: 'rgba(103, 194, 58, 0.05)' }
          ])
        }
      },
      {
        name: '降级人数',
        type: 'line',
        data: downgradeCounts,
        smooth: true,
        symbol: 'circle',
        symbolSize: 8,
        lineStyle: { width: 3, color: '#f56c6c' },
        itemStyle: { color: '#f56c6c', borderColor: '#fff', borderWidth: 2 },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(245, 108, 108, 0.3)' },
            { offset: 1, color: 'rgba(245, 108, 108, 0.05)' }
          ])
        }
      }
    ]
  })
}

const renderLevelPieChart = () => {
  if (!levelPieChartRef.value) return
  if (!levelPieChart) {
    levelPieChart = echarts.init(levelPieChartRef.value)
  }

  const data = levelComparison.value.map(item => ({
    name: item.levelName,
    value: item.memberCount || 0,
    itemStyle: { color: item.levelColor }
  }))

  levelPieChart.setOption({
    tooltip: {
      trigger: 'item',
      formatter: '{b}: {c} 人 ({d}%)'
    },
    legend: {
      bottom: 0,
      left: 'center',
      textStyle: { color: '#606266', fontSize: 12 }
    },
    series: [{
      type: 'pie',
      radius: ['40%', '65%'],
      center: ['50%', '45%'],
      avoidLabelOverlap: true,
      itemStyle: {
        borderRadius: 8,
        borderColor: '#fff',
        borderWidth: 2
      },
      label: {
        show: true,
        formatter: '{b}\n{d}%',
        color: '#606266',
        fontSize: 12
      },
      labelLine: {
        show: true,
        length: 15,
        length2: 10
      },
      data: data
    }]
  })
}

const handleResize = () => {
  flowTrendChart?.resize()
  levelPieChart?.resize()
}

onMounted(() => {
  loadData()
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  flowTrendChart?.dispose()
  levelPieChart?.dispose()
})
</script>

<style scoped>
.level-distribution-analysis {
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

.stat-card.upgrade {
  background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%);
}

.stat-card.downgrade {
  background: linear-gradient(135deg, #fa709a 0%, #fee140 100%);
}

.stat-card.remain {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.stat-card.health {
  background: linear-gradient(135deg, #30cfd0 0%, #330867 100%);
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
  font-size: 26px;
  font-weight: 700;
  line-height: 1.2;
}

.stat-label {
  font-size: 13px;
  opacity: 0.9;
  margin-top: 4px;
}

.stat-rate {
  font-size: 12px;
  opacity: 0.85;
  margin-top: 4px;
}

.comparison-card {
  margin-bottom: 16px;
}

.charts-row {
  margin-bottom: 16px;
}

.chart-title {
  font-weight: 600;
  color: #303133;
  font-size: 14px;
}

.line-chart,
.pie-chart {
  height: 320px;
  width: 100%;
}

.health-card {
  margin-bottom: 16px;
}

.level-badge {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 6px 14px;
  border-radius: 16px;
  font-size: 13px;
  font-weight: 500;
}

.progress-item {
  display: flex;
  align-items: center;
  gap: 12px;
}

.percentage-text {
  font-size: 13px;
  color: #606266;
  font-weight: 500;
  min-width: 60px;
  text-align: right;
}
</style>
