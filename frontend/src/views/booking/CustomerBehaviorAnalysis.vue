<template>
  <div class="customer-behavior-container">
    <el-card class="filter-card" shadow="never">
      <el-row :gutter="16" align="middle">
        <el-col :xs="24" :sm="10" :md="8">
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
          <el-button type="primary" @click="handleQuery" :loading="loading">查询</el-button>
        </el-col>
      </el-row>
    </el-card>

    <div class="section-title">入住时长分析</div>
    <el-row :gutter="20" class="section-row">
      <el-col :xs="24" :sm="6">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-content">
            <div class="stat-icon" style="background: linear-gradient(135deg, #667eea 0%, #764ba2 100%)">
              <el-icon :size="28"><Clock /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stayData.avgDays }}</div>
              <div class="stat-label">平均入住天数</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="18">
        <el-card class="chart-card" shadow="hover">
          <div ref="durationChartRef" class="chart-container"></div>
        </el-card>
      </el-col>
    </el-row>

    <div class="section-title">房型偏好分析</div>
    <el-row :gutter="20" class="section-row">
      <el-col :span="24">
        <el-card class="chart-card" shadow="hover">
          <div ref="roomTypePieRef" class="chart-container"></div>
        </el-card>
      </el-col>
    </el-row>

    <div class="section-title">重复入住分析</div>
    <el-row :gutter="20" class="section-row">
      <el-col :xs="12" :sm="6">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-content">
            <div class="stat-icon" style="background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%)">
              <el-icon :size="28"><UserFilled /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ repeatData.repeatGuestCount }}</div>
              <div class="stat-label">重复入住客数</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="12" :sm="6">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-content">
            <div class="stat-icon" style="background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%)">
              <el-icon :size="28"><RefreshRight /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ repeatData.repeatRate }}%</div>
              <div class="stat-label">重复入住率</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="12">
        <el-card class="table-card" shadow="hover">
          <template #header>
            <span>高频重复入住客户</span>
          </template>
          <el-table :data="repeatData.topGuests" stripe style="width: 100%">
            <el-table-column prop="customerName" label="客户姓名" min-width="120" />
            <el-table-column prop="phone" label="手机号" min-width="130" />
            <el-table-column prop="visitCount" label="入住次数" width="100" align="center" />
            <el-table-column prop="lastVisitDate" label="最近入住" width="120" align="center" />
          </el-table>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount, nextTick } from 'vue'
import { Clock, UserFilled, RefreshRight } from '@element-plus/icons-vue'
import * as echarts from 'echarts'
import api from '@/api'

const loading = ref(false)
const queryForm = ref({ dateRange: [] })

const stayData = ref({ avgDays: 0, distribution: [] })
const roomTypePreference = ref([])
const repeatData = ref({ repeatGuestCount: 0, repeatRate: 0, topGuests: [] })

const durationChartRef = ref(null)
const roomTypePieRef = ref(null)
let durationChart = null
let roomTypePieChart = null

const loadData = async () => {
  const params = {}
  if (queryForm.value.dateRange && queryForm.value.dateRange.length === 2) {
    params.startDate = queryForm.value.dateRange[0]
    params.endDate = queryForm.value.dateRange[1]
  }

  try {
    loading.value = true
    const res = await api.analytics.customerBehavior(params)
    if (res.code === 200 && res.data) {
      stayData.value = res.data.stay || { avgDays: 0, distribution: [] }
      roomTypePreference.value = res.data.roomTypePreference || []
      repeatData.value = res.data.repeat || { repeatGuestCount: 0, repeatRate: 0, topGuests: [] }
      await nextTick()
      renderDurationChart()
      renderRoomTypePie()
    }
  } catch {
  } finally {
    loading.value = false
  }
}

const renderDurationChart = () => {
  if (!durationChartRef.value) return
  if (!durationChart) {
    durationChart = echarts.init(durationChartRef.value)
  }
  const labels = ['1天', '2天', '3天', '4-5天', '6-7天', '7天以上']
  const data = stayData.value.distribution.length > 0
    ? stayData.value.distribution.map(d => d.count)
    : [0, 0, 0, 0, 0, 0]

  durationChart.setOption({
    tooltip: {
      trigger: 'axis',
      axisPointer: { type: 'shadow' },
      formatter: '{b}<br/>人数: {c}'
    },
    grid: { left: 60, right: 30, top: 30, bottom: 40 },
    xAxis: {
      type: 'category',
      data: labels,
      axisLabel: { color: '#718096' }
    },
    yAxis: {
      type: 'value',
      name: '人数',
      axisLabel: { color: '#718096' },
      splitLine: { lineStyle: { type: 'dashed' } }
    },
    series: [{
      type: 'bar',
      data: data,
      barWidth: '50%',
      itemStyle: {
        borderRadius: [6, 6, 0, 0],
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: '#4facfe' },
          { offset: 1, color: '#00f2fe' }
        ])
      },
      label: { show: true, position: 'top', color: '#4a5568', formatter: '{c}' }
    }]
  })
}

const renderRoomTypePie = () => {
  if (!roomTypePieRef.value) return
  if (!roomTypePieChart) {
    roomTypePieChart = echarts.init(roomTypePieRef.value)
  }
  const data = roomTypePreference.value.map(d => ({
    name: d.typeName || d.name,
    value: d.bookingCount || d.count || 0
  }))

  roomTypePieChart.setOption({
    tooltip: { trigger: 'item', formatter: '{b}: {c} ({d}%)' },
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

const handleResize = () => {
  durationChart?.resize()
  roomTypePieChart?.resize()
}

onMounted(() => {
  loadData()
  window.addEventListener('resize', handleResize)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize)
  durationChart?.dispose()
  roomTypePieChart?.dispose()
})
</script>

<style scoped>
.customer-behavior-container {
  padding: 10px;
}

.filter-card {
  margin-bottom: 20px;
  border-radius: 12px;
  border: none;
}

.section-title {
  font-size: 18px;
  font-weight: 600;
  color: #2d3748;
  margin: 24px 0 16px;
  padding-left: 12px;
  border-left: 4px solid #667eea;
}

.section-row {
  margin-bottom: 10px;
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
  border-radius: 12px;
  border: none;
}

.chart-container {
  width: 100%;
  height: 350px;
}
</style>
