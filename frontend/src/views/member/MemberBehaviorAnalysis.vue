<template>
  <div class="member-behavior-analysis">
    <el-card shadow="never" class="overview-cards">
      <template #header>
        <span class="card-title">行为概览</span>
      </template>
      <el-row :gutter="16">
        <el-col :span="6">
          <div class="stat-card frequency">
            <div class="stat-icon">
              <el-icon :size="28"><Clock /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ frequencyData.totalMembers || 0 }}</div>
              <div class="stat-label">会员总数</div>
            </div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="stat-card amount">
            <div class="stat-icon">
              <el-icon :size="28"><Wallet /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ loyalCount }}</div>
              <div class="stat-label">忠诚型会员</div>
            </div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="stat-card advance">
            <div class="stat-icon">
              <el-icon :size="28"><Calendar /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ bookingAdvance.avgAdvanceDays || 0 }}</div>
              <div class="stat-label">平均提前预订(天)</div>
            </div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="stat-card weekend">
            <div class="stat-icon">
              <el-icon :size="28"><Sunny /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ weekendRatio }}%</div>
              <div class="stat-label">周末入住占比</div>
            </div>
          </div>
        </el-col>
      </el-row>
    </el-card>

    <el-row :gutter="16" class="charts-row">
      <el-col :span="14">
        <el-card shadow="never">
          <template #header>
            <span class="chart-title">消费频次分布</span>
          </template>
          <div ref="frequencyChartRef" class="bar-chart"></div>
        </el-card>
      </el-col>
      <el-col :span="10">
        <el-card shadow="never">
          <template #header>
            <span class="chart-title">消费金额分布</span>
          </template>
          <div ref="amountPieChartRef" class="pie-chart"></div>
        </el-card>
      </el-col>
    </el-row>

    <el-card shadow="never" class="top-members-card">
      <template #header>
        <span class="chart-title">高价值会员 TOP 10%</span>
      </template>
      <el-table :data="topValueMembers" stripe border style="width: 100%">
        <el-table-column type="index" label="排名" width="70" align="center" />
        <el-table-column prop="memberNo" label="会员卡号" width="160" />
        <el-table-column prop="customerName" label="姓名" width="100" />
        <el-table-column prop="levelName" label="等级" width="120">
          <template #default="{ row }">
            <el-tag size="small">{{ row.levelName }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="累计消费" width="140" align="right">
          <template #default="{ row }">
            <span style="color: #e6a23c; font-weight: 600">¥{{ formatNumber(row.totalSpent) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="stayCount" label="入住次数" width="100" align="center" />
        <el-table-column label="当前积分" width="120" align="right">
          <template #default="{ row }">
            <span style="color: #67c23a">{{ formatNumber(row.currentPoints) }}</span>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-row :gutter="16" class="charts-row">
      <el-col :span="12">
        <el-card shadow="never">
          <template #header>
            <span class="chart-title">月份入住分布</span>
          </template>
          <div ref="monthChartRef" class="bar-chart"></div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card shadow="never">
          <template #header>
            <span class="chart-title">星期入住分布</span>
          </template>
          <div ref="weekChartRef" class="bar-chart"></div>
        </el-card>
      </el-col>
    </el-row>

    <el-card shadow="never" class="advance-card">
      <template #header>
        <span class="chart-title">预订提前期统计</span>
      </template>
      <el-row :gutter="16">
        <el-col :span="6">
          <div class="advance-item">
            <div class="advance-value" style="color: #f56c6c">{{ bookingAdvance.sameDayCount || 0 }}</div>
            <div class="advance-label">当天预订</div>
            <div class="advance-percent">
              {{ calcPercent(bookingAdvance.sameDayCount) }}%
            </div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="advance-item">
            <div class="advance-value" style="color: #e6a23c">{{ bookingAdvance.oneToThreeCount || 0 }}</div>
            <div class="advance-label">1-3天提前</div>
            <div class="advance-percent">
              {{ calcPercent(bookingAdvance.oneToThreeCount) }}%
            </div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="advance-item">
            <div class="advance-value" style="color: #409eff">{{ bookingAdvance.fourToSevenCount || 0 }}</div>
            <div class="advance-label">4-7天提前</div>
            <div class="advance-percent">
              {{ calcPercent(bookingAdvance.fourToSevenCount) }}%
            </div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="advance-item">
            <div class="advance-value" style="color: #67c23a">{{ bookingAdvance.moreThanSevenCount || 0 }}</div>
            <div class="advance-label">7天以上提前</div>
            <div class="advance-percent">
              {{ calcPercent(bookingAdvance.moreThanSevenCount) }}%
            </div>
          </div>
        </el-col>
      </el-row>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick, onUnmounted, computed } from 'vue'
import * as echarts from 'echarts'
import { Clock, Wallet, Calendar, Sunny } from '@element-plus/icons-vue'
import api from '@/api'

const frequencyData = ref({})
const amountData = ref({})
const timePreference = ref({})
const bookingAdvance = ref({})
const topValueMembers = ref([])

const frequencyChartRef = ref(null)
const amountPieChartRef = ref(null)
const monthChartRef = ref(null)
const weekChartRef = ref(null)

let frequencyChart = null
let amountPieChart = null
let monthChart = null
let weekChart = null

const loyalCount = computed(() => {
  const dist = frequencyData.value.distribution || []
  const loyal = dist.find(d => d.type.includes('忠诚'))
  return loyal ? loyal.count : 0
})

const weekendRatio = computed(() => {
  const weekPref = timePreference.value.weekPreference || []
  if (!weekPref.length) return '0'
  const weekendCount = weekPref.filter(d => d.isWeekend).reduce((sum, d) => sum + (d.count || 0), 0)
  const totalCount = weekPref.reduce((sum, d) => sum + (d.count || 0), 0)
  return totalCount > 0 ? ((weekendCount / totalCount) * 100).toFixed(1) : '0'
})

const formatNumber = (num) => {
  if (!num) return '0'
  return Number(num).toLocaleString('zh-CN')
}

const calcPercent = (count) => {
  const total = bookingAdvance.value.totalBookings || 0
  if (!total) return '0'
  return ((count || 0) / total * 100).toFixed(1)
}

const loadData = async () => {
  try {
    const [freqRes, amountRes, timeRes] = await Promise.all([
      api.memberAnalytics.getFrequencyAnalysis(),
      api.memberAnalytics.getAmountAnalysis(),
      api.memberAnalytics.getTimePreference()
    ])

    if (freqRes.code === 200) {
      frequencyData.value = freqRes.data || {}
    }
    if (amountRes.code === 200) {
      amountData.value = amountRes.data || {}
      topValueMembers.value = amountRes.data?.topValueMembers || []
    }
    if (timeRes.code === 200) {
      timePreference.value = timeRes.data || {}
      bookingAdvance.value = timeRes.data?.bookingAdvance || {}
    }

    await nextTick()
    renderFrequencyChart()
    renderAmountPieChart()
    renderMonthChart()
    renderWeekChart()
  } catch (e) {
    console.error(e)
  }
}

const renderFrequencyChart = () => {
  if (!frequencyChartRef.value) return
  if (!frequencyChart) {
    frequencyChart = echarts.init(frequencyChartRef.value)
  }

  const dist = frequencyData.value.distribution || []
  const types = dist.map(d => d.type)
  const counts = dist.map(d => d.count || 0)
  const totalSpent = dist.map(d => d.totalSpent || 0)

  frequencyChart.setOption({
    tooltip: {
      trigger: 'axis',
      axisPointer: { type: 'shadow' }
    },
    legend: {
      data: ['会员数量', '累计消费(元)'],
      bottom: 0
    },
    grid: {
      left: 60,
      right: 60,
      top: 30,
      bottom: 50
    },
    xAxis: {
      type: 'category',
      data: types,
      axisLabel: { color: '#606266', fontSize: 12 }
    },
    yAxis: [
      {
        type: 'value',
        name: '会员数',
        nameTextStyle: { color: '#909399', fontSize: 12 },
        axisLabel: { color: '#909399', fontSize: 11 },
        splitLine: { lineStyle: { type: 'dashed', color: '#f0f0f0' } }
      },
      {
        type: 'value',
        name: '消费(元)',
        nameTextStyle: { color: '#909399', fontSize: 12 },
        axisLabel: { color: '#909399', fontSize: 11 },
        splitLine: { show: false }
      }
    ],
    series: [
      {
        name: '会员数量',
        type: 'bar',
        data: counts,
        itemStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: '#667eea' },
            { offset: 1, color: '#764ba2' }
          ]),
          borderRadius: [4, 4, 0, 0]
        },
        barWidth: 30,
        label: {
          show: true,
          position: 'top',
          color: '#606266',
          fontSize: 11
        }
      },
      {
        name: '累计消费(元)',
        type: 'bar',
        yAxisIndex: 1,
        data: totalSpent,
        itemStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: '#f093fb' },
            { offset: 1, color: '#f5576c' }
          ]),
          borderRadius: [4, 4, 0, 0]
        },
        barWidth: 30
      }
    ]
  })
}

const renderAmountPieChart = () => {
  if (!amountPieChartRef.value) return
  if (!amountPieChart) {
    amountPieChart = echarts.init(amountPieChartRef.value)
  }

  const dist = amountData.value.distribution || []
  const data = dist.map(d => ({
    name: d.level,
    value: d.count || 0,
    itemStyle: { color: d.color }
  }))

  amountPieChart.setOption({
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

const renderMonthChart = () => {
  if (!monthChartRef.value) return
  if (!monthChart) {
    monthChart = echarts.init(monthChartRef.value)
  }

  const monthPref = timePreference.value.monthPreference || []
  const months = monthPref.map(d => d.month)
  const counts = monthPref.map(d => d.count || 0)

  monthChart.setOption({
    tooltip: {
      trigger: 'axis',
      axisPointer: { type: 'shadow' },
      formatter: '{b}<br/>入住次数: {c}'
    },
    grid: {
      left: 50,
      right: 20,
      top: 30,
      bottom: 30
    },
    xAxis: {
      type: 'category',
      data: months,
      axisLabel: { color: '#606266', fontSize: 11 }
    },
    yAxis: {
      type: 'value',
      axisLabel: { color: '#909399', fontSize: 11 },
      splitLine: { lineStyle: { type: 'dashed', color: '#f0f0f0' } }
    },
    series: [{
      type: 'bar',
      data: counts,
      itemStyle: {
        color: function(params) {
          const colorList = ['#409eff', '#67c23a', '#e6a23c', '#f56c6c', '#909399', '#667eea']
          return colorList[params.dataIndex % colorList.length]
        },
        borderRadius: [4, 4, 0, 0]
      },
      barWidth: 24,
      label: {
        show: true,
        position: 'top',
        color: '#606266',
        fontSize: 10
      }
    }]
  })
}

const renderWeekChart = () => {
  if (!weekChartRef.value) return
  if (!weekChart) {
    weekChart = echarts.init(weekChartRef.value)
  }

  const weekPref = timePreference.value.weekPreference || []
  const days = weekPref.map(d => d.day)
  const counts = weekPref.map(d => d.count || 0)
  const isWeekend = weekPref.map(d => d.isWeekend)

  weekChart.setOption({
    tooltip: {
      trigger: 'axis',
      axisPointer: { type: 'shadow' }
    },
    grid: {
      left: 50,
      right: 20,
      top: 30,
      bottom: 30
    },
    xAxis: {
      type: 'category',
      data: days,
      axisLabel: { color: '#606266', fontSize: 12 }
    },
    yAxis: {
      type: 'value',
      axisLabel: { color: '#909399', fontSize: 11 },
      splitLine: { lineStyle: { type: 'dashed', color: '#f0f0f0' } }
    },
    series: [{
      type: 'bar',
      data: counts.map((value, index) => ({
        value,
        itemStyle: {
          color: isWeekend[index] ? '#f56c6c' : '#409eff',
          borderRadius: [4, 4, 0, 0]
        }
      })),
      barWidth: 40,
      label: {
        show: true,
        position: 'top',
        color: '#606266',
        fontSize: 11
      }
    }]
  })
}

const handleResize = () => {
  frequencyChart?.resize()
  amountPieChart?.resize()
  monthChart?.resize()
  weekChart?.resize()
}

onMounted(() => {
  loadData()
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  frequencyChart?.dispose()
  amountPieChart?.dispose()
  monthChart?.dispose()
  weekChart?.dispose()
})
</script>

<style scoped>
.member-behavior-analysis {
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

.stat-card.frequency {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.stat-card.amount {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
}

.stat-card.advance {
  background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
}

.stat-card.weekend {
  background: linear-gradient(135deg, #fa709a 0%, #fee140 100%);
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
.pie-chart {
  height: 320px;
  width: 100%;
}

.top-members-card {
  margin-bottom: 16px;
}

.advance-card {
  margin-bottom: 16px;
}

.advance-item {
  text-align: center;
  padding: 20px;
  border-radius: 8px;
  background: #f5f7fa;
}

.advance-value {
  font-size: 28px;
  font-weight: 700;
  line-height: 1.2;
}

.advance-label {
  font-size: 13px;
  color: #606266;
  margin-top: 6px;
}

.advance-percent {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
}
</style>
