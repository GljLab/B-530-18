<template>
  <div class="point-statistics-container">
    <el-card shadow="never" class="header-card">
      <div class="title">积分统计分析</div>
    </el-card>

    <el-row :gutter="16" class="overview-row">
      <el-col :span="4" v-for="item in overviewCards" :key="item.label">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-value" :style="{ color: item.color }">{{ item.value }}</div>
          <div class="stat-label">{{ item.label }}</div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="16" style="margin-top: 16px">
      <el-col :span="8">
        <el-card shadow="never">
          <template #header><span>积分获取来源分析</span></template>
          <div class="chart-container" ref="sourceChartRef"></div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="never">
          <template #header><span>积分使用去向分析</span></template>
          <div class="chart-container" ref="usageChartRef"></div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="never">
          <template #header><span>积分转化分析</span></template>
          <div class="conversion-content" v-loading="conversionLoading">
            <div class="conversion-item">
              <span class="conv-label">积分活跃会员数</span>
              <span class="conv-value">{{ conversionData.activeMemberCount || 0 }}</span>
            </div>
            <el-divider style="margin: 8px 0" />
            <div class="conversion-item">
              <span class="conv-label">积分活跃率</span>
              <span class="conv-value" style="color: #409eff">{{ conversionData.activeRate || 0 }}%</span>
            </div>
            <el-divider style="margin: 8px 0" />
            <div class="conversion-item">
              <span class="conv-label">平均积分使用频率</span>
              <span class="conv-value">{{ conversionData.avgUseFrequency || 0 }}次</span>
            </div>
            <el-divider style="margin: 8px 0" />
            <div class="conversion-item">
              <span class="conv-label">平均单次使用积分</span>
              <span class="conv-value">{{ conversionData.avgPointsPerUse || 0 }}</span>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-card shadow="never" style="margin-top: 16px">
      <template #header><span>积分趋势分析（近12个月）</span></template>
      <div class="trend-chart-container" ref="trendChartRef"></div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onUnmounted, nextTick } from 'vue'
import * as echarts from 'echarts'
import api from '@/api'

const overviewCards = ref([])
const sourceChartRef = ref(null)
const usageChartRef = ref(null)
const trendChartRef = ref(null)
const conversionData = ref({})
const conversionLoading = ref(false)

let sourceChart = null
let usageChart = null
let trendChart = null

const loadOverview = async () => {
  try {
    const res = await api.pointStatistics.overview()
    if (res.code === 200) {
      const d = res.data
      overviewCards.value = [
        { label: '积分总量', value: d.totalCurrentPoints || 0, color: '#409eff' },
        { label: '本月发放', value: d.monthEarned || 0, color: '#67c23a' },
        { label: '本月使用', value: d.monthUsed || 0, color: '#e6a23c' },
        { label: '使用率', value: (d.usageRate || 0) + '%', color: '#f56c6c' },
        { label: '积分负债', value: d.pointLiability || 0, color: '#909399' },
        { label: '负债说明', value: '潜在成本', color: '#909399' }
      ]
    }
  } catch (e) { console.error(e) }
}

const loadSourceAnalysis = async () => {
  try {
    const res = await api.pointStatistics.source()
    if (res.code === 200 && sourceChartRef.value) {
      sourceChart = echarts.init(sourceChartRef.value)
      sourceChart.setOption({
        tooltip: { trigger: 'item', formatter: '{b}: {c} ({d}%)' },
        series: [{
          type: 'pie', radius: ['40%', '70%'], label: { formatter: '{b}\n{d}%' },
          data: (res.data || []).map(item => ({ name: item.name, value: Number(item.value) || 0 }))
        }]
      })
    }
  } catch (e) { console.error(e) }
}

const loadUsageAnalysis = async () => {
  try {
    const res = await api.pointStatistics.usage()
    if (res.code === 200 && usageChartRef.value) {
      usageChart = echarts.init(usageChartRef.value)
      usageChart.setOption({
        tooltip: { trigger: 'item', formatter: '{b}: {c} ({d}%)' },
        series: [{
          type: 'pie', radius: ['40%', '70%'], label: { formatter: '{b}\n{d}%' },
          data: (res.data || []).map(item => ({ name: item.name, value: Number(item.value) || 0 }))
        }]
      })
    }
  } catch (e) { console.error(e) }
}

const loadTrend = async () => {
  try {
    const res = await api.pointStatistics.trend(12)
    if (res.code === 200 && trendChartRef.value) {
      const data = res.data || []
      trendChart = echarts.init(trendChartRef.value)
      trendChart.setOption({
        tooltip: { trigger: 'axis' },
        legend: { data: ['发放积分', '使用积分'] },
        xAxis: { type: 'category', data: data.map(d => d.month) },
        yAxis: { type: 'value' },
        series: [
          { name: '发放积分', type: 'line', smooth: true, data: data.map(d => Number(d.earned) || 0), itemStyle: { color: '#67c23a' } },
          { name: '使用积分', type: 'line', smooth: true, data: data.map(d => Number(d.used) || 0), itemStyle: { color: '#e6a23c' } }
        ]
      })
    }
  } catch (e) { console.error(e) }
}

const loadConversion = async () => {
  conversionLoading.value = true
  try {
    const res = await api.pointStatistics.conversion()
    if (res.code === 200) {
      conversionData.value = res.data || {}
    }
  } catch (e) { console.error(e) }
  conversionLoading.value = false
}

const handleResize = () => {
  sourceChart?.resize()
  usageChart?.resize()
  trendChart?.resize()
}

onMounted(async () => {
  await nextTick()
  loadOverview()
  loadSourceAnalysis()
  loadUsageAnalysis()
  loadTrend()
  loadConversion()
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  sourceChart?.dispose()
  usageChart?.dispose()
  trendChart?.dispose()
})
</script>

<style scoped>
.point-statistics-container { padding: 16px; }
.header-card { margin-bottom: 16px; }
.title { font-size: 18px; font-weight: 600; color: #303133; }
.overview-row { margin-bottom: 0; }
.stat-card { text-align: center; }
.stat-value { font-size: 24px; font-weight: 700; margin-bottom: 8px; }
.stat-label { font-size: 13px; color: #909399; }
.chart-container { height: 300px; }
.trend-chart-container { height: 350px; }
.conversion-content { padding: 10px; }
.conversion-item { display: flex; justify-content: space-between; align-items: center; padding: 6px 0; }
.conv-label { color: #606266; font-size: 14px; }
.conv-value { font-size: 18px; font-weight: 600; color: #303133; }
</style>
