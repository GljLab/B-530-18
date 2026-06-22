<template>
  <div class="tag-analytics-container">
    <el-row :gutter="20">
      <el-col :span="24" :lg="12">
        <el-card shadow="never" class="chart-card">
          <template #header>
            <div class="card-header">
              <span class="card-title">
                <el-icon><CircleCheckFilled /></el-icon> 好评标签排行
              </span>
            </div>
          </template>
          <div ref="positiveChartRef" class="bar-chart"></div>
        </el-card>
      </el-col>
      <el-col :span="24" :lg="12">
        <el-card shadow="never" class="chart-card">
          <template #header>
            <div class="card-header">
              <span class="card-title">
                <el-icon><CircleCloseFilled /></el-icon> 差评标签排行
              </span>
            </div>
          </template>
          <div ref="negativeChartRef" class="bar-chart"></div>
        </el-card>
      </el-col>
    </el-row>

    <el-card shadow="never" class="chart-card">
      <template #header>
        <div class="card-header">
          <span class="card-title">标签使用趋势</span>
          <div class="header-controls">
            <el-select v-model="selectedTagId" placeholder="选择标签" style="width: 200px" @change="handleTagChange">
              <el-option
                v-for="tag in allTags"
                :key="tag.tagId"
                :label="tag.tagText"
                :value="tag.tagId"
              />
            </el-select>
            <el-radio-group v-model="timeRange" size="default" @change="handleTimeRangeChange" style="margin-left: 10px">
              <el-radio-button :value="3">近3个月</el-radio-button>
              <el-radio-button :value="6">近6个月</el-radio-button>
              <el-radio-button :value="12">近12个月</el-radio-button>
            </el-radio-group>
          </div>
        </div>
      </template>

      <div v-if="tagTrendData.trend" class="trend-tag">
        <el-tag v-if="tagTrendData.trend === 'rising'" type="danger" effect="light">
          <el-icon><Top /></el-icon> 该标签提及量上升，需要关注
        </el-tag>
        <el-tag v-else-if="tagTrendData.trend === 'falling'" type="success" effect="light">
          <el-icon><Bottom /></el-icon> 该标签提及量下降，问题改善
        </el-tag>
        <el-tag v-else type="info" effect="light">
          <el-icon><Minus /></el-icon> 该标签提及量稳定
        </el-tag>
      </div>

      <div ref="tagTrendChartRef" class="trend-chart"></div>
    </el-card>

    <el-row :gutter="20">
      <el-col :span="24" :lg="12">
        <el-card shadow="never" class="chart-card">
          <template #header>
            <div class="card-header">
              <span class="card-title">好评标签详情</span>
            </div>
          </template>
          <el-table :data="tagData.positiveTags || []" stripe>
            <el-table-column label="排名" width="70" align="center" type="index">
              <template #default="{ $index }">
                <el-tag v-if="$index === 0" type="warning" effect="dark">1</el-tag>
                <el-tag v-else-if="$index === 1" type="info" effect="light">2</el-tag>
                <el-tag v-else-if="$index === 2" type="info" effect="plain">3</el-tag>
                <span v-else>{{ $index + 1 }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="tagText" label="标签名称" min-width="150" />
            <el-table-column prop="count" label="使用次数" width="100" align="center" />
            <el-table-column label="占比" width="100" align="center">
              <template #default="{ row }">
                {{ getTagPercent(row.count, 'positive') }}%
              </template>
            </el-table-column>
            <el-table-column label="操作" width="100" align="center">
              <template #default="{ row }">
                <el-button type="primary" size="small" link @click="viewTagTrend(row)">
                  查看趋势
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
      <el-col :span="24" :lg="12">
        <el-card shadow="never" class="chart-card">
          <template #header>
            <div class="card-header">
              <span class="card-title">差评标签详情</span>
            </div>
          </template>
          <el-table :data="tagData.negativeTags || []" stripe>
            <el-table-column label="排名" width="70" align="center" type="index">
              <template #default="{ $index }">
                <el-tag v-if="$index === 0" type="danger" effect="dark">1</el-tag>
                <el-tag v-else-if="$index === 1" type="danger" effect="light">2</el-tag>
                <el-tag v-else-if="$index === 2" type="danger" effect="plain">3</el-tag>
                <span v-else>{{ $index + 1 }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="tagText" label="标签名称" min-width="150" />
            <el-table-column prop="count" label="使用次数" width="100" align="center" />
            <el-table-column label="占比" width="100" align="center">
              <template #default="{ row }">
                {{ getTagPercent(row.count, 'negative') }}%
              </template>
            </el-table-column>
            <el-table-column label="操作" width="100" align="center">
              <template #default="{ row }">
                <el-button type="primary" size="small" link @click="viewTagTrend(row)">
                  查看趋势
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onBeforeUnmount, nextTick } from 'vue'
import {
  CircleCheckFilled, CircleCloseFilled, Top, Bottom, Minus
} from '@element-plus/icons-vue'
import * as echarts from 'echarts'
import api from '@/api'

const timeRange = ref(12)
const selectedTagId = ref(null)
const tagData = ref({})
const tagTrendData = ref({})

const positiveChartRef = ref(null)
const negativeChartRef = ref(null)
const tagTrendChartRef = ref(null)

let positiveChart = null
let negativeChart = null
let tagTrendChart = null

const allTags = computed(() => {
  const positive = tagData.value.positiveTags || []
  const negative = tagData.value.negativeTags || []
  return [...positive, ...negative]
})

const totalPositiveCount = computed(() => {
  return (tagData.value.positiveTags || []).reduce((sum, t) => sum + t.count, 0)
})

const totalNegativeCount = computed(() => {
  return (tagData.value.negativeTags || []).reduce((sum, t) => sum + t.count, 0)
})

const getTagPercent = (count, type) => {
  const total = type === 'positive' ? totalPositiveCount.value : totalNegativeCount.value
  if (total === 0) return 0
  return Math.round((count / total) * 100)
}

const handleTagChange = () => {
  fetchTagTrend()
}

const handleTimeRangeChange = () => {
  fetchTagTrend()
}

const viewTagTrend = (tag) => {
  selectedTagId.value = tag.tagId
  fetchTagTrend()
}

const fetchTagStatistics = async () => {
  try {
    const res = await api.reviewAnalytics.getTagStatistics()
    tagData.value = res.data || {}
    if (allTags.value.length > 0) {
      selectedTagId.value = allTags.value[0].tagId
    }
    await nextTick()
    renderPositiveChart()
    renderNegativeChart()
    if (selectedTagId.value) {
      fetchTagTrend()
    }
  } catch (e) {
    console.error(e)
  }
}

const fetchTagTrend = async () => {
  if (!selectedTagId.value) return
  try {
    const res = await api.reviewAnalytics.getTagTrend(selectedTagId.value, timeRange.value)
    tagTrendData.value = res.data || {}
    await nextTick()
    renderTagTrendChart(res.data?.trendData || [])
  } catch (e) {
    console.error(e)
  }
}

const renderPositiveChart = () => {
  if (!positiveChartRef.value) return
  if (positiveChart) positiveChart.dispose()
  
  positiveChart = echarts.init(positiveChartRef.value)
  
  const tags = tagData.value.positiveTags || []
  const names = tags.map(t => t.tagText).reverse()
  const counts = tags.map(t => t.count).reverse()
  
  positiveChart.setOption({
    tooltip: {
      trigger: 'axis',
      axisPointer: { type: 'shadow' }
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      containLabel: true
    },
    xAxis: {
      type: 'value',
      axisLabel: { color: '#6b7280' }
    },
    yAxis: {
      type: 'category',
      data: names,
      axisLabel: { color: '#6b7280' }
    },
    series: [{
      type: 'bar',
      data: counts,
      itemStyle: {
        color: new echarts.graphic.LinearGradient(0, 0, 1, 0, [
          { offset: 0, color: '#34d399' },
          { offset: 1, color: '#10b981' }
        ]),
        borderRadius: [0, 4, 4, 0]
      },
      barWidth: '60%',
      label: {
        show: true,
        position: 'right',
        formatter: '{c}次'
      }
    }]
  })
}

const renderNegativeChart = () => {
  if (!negativeChartRef.value) return
  if (negativeChart) negativeChart.dispose()
  
  negativeChart = echarts.init(negativeChartRef.value)
  
  const tags = tagData.value.negativeTags || []
  const names = tags.map(t => t.tagText).reverse()
  const counts = tags.map(t => t.count).reverse()
  
  negativeChart.setOption({
    tooltip: {
      trigger: 'axis',
      axisPointer: { type: 'shadow' }
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      containLabel: true
    },
    xAxis: {
      type: 'value',
      axisLabel: { color: '#6b7280' }
    },
    yAxis: {
      type: 'category',
      data: names,
      axisLabel: { color: '#6b7280' }
    },
    series: [{
      type: 'bar',
      data: counts,
      itemStyle: {
        color: new echarts.graphic.LinearGradient(0, 0, 1, 0, [
          { offset: 0, color: '#f87171' },
          { offset: 1, color: '#ef4444' }
        ]),
        borderRadius: [0, 4, 4, 0]
      },
      barWidth: '60%',
      label: {
        show: true,
        position: 'right',
        formatter: '{c}次'
      }
    }]
  })
}

const renderTagTrendChart = (data) => {
  if (!tagTrendChartRef.value) return
  if (tagTrendChart) tagTrendChart.dispose()
  
  tagTrendChart = echarts.init(tagTrendChartRef.value)
  
  const months = data.map(d => d.month)
  const counts = data.map(d => d.count)
  
  const selectedTag = allTags.value.find(t => t.tagId === selectedTagId.value)
  const isNegative = (tagData.value.negativeTags || []).some(t => t.tagId === selectedTagId.value)
  const lineColor = isNegative ? '#ef4444' : '#10b981'
  
  tagTrendChart.setOption({
    tooltip: {
      trigger: 'axis',
      axisPointer: { type: 'cross' }
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: months,
      boundaryGap: false,
      axisLabel: { color: '#6b7280', rotate: 30 }
    },
    yAxis: {
      type: 'value',
      axisLabel: { color: '#6b7280' }
    },
    series: [{
      type: 'line',
      name: selectedTag?.tagText || '使用次数',
      data: counts,
      smooth: true,
      symbol: 'circle',
      symbolSize: 8,
      lineStyle: {
        width: 3,
        color: lineColor
      },
      itemStyle: {
        color: lineColor,
        borderWidth: 2,
        borderColor: '#fff'
      },
      areaStyle: {
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: isNegative ? 'rgba(239, 68, 68, 0.3)' : 'rgba(16, 185, 129, 0.3)' },
          { offset: 1, color: isNegative ? 'rgba(239, 68, 68, 0.05)' : 'rgba(16, 185, 129, 0.05)' }
        ])
      }
    }]
  })
}

const handleResize = () => {
  positiveChart?.resize()
  negativeChart?.resize()
  tagTrendChart?.resize()
}

onMounted(() => {
  fetchTagStatistics()
  window.addEventListener('resize', handleResize)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize)
  positiveChart?.dispose()
  negativeChart?.dispose()
  tagTrendChart?.dispose()
})
</script>

<style lang="scss" scoped>
.tag-analytics-container {
  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }

  .card-title {
    font-size: 16px;
    font-weight: 600;
    color: #1f2937;
    display: flex;
    align-items: center;
    gap: 6px;
  }

  .header-controls {
    display: flex;
    align-items: center;
  }

  .chart-card {
    margin-bottom: 20px;

    .bar-chart {
      height: 350px;
    }

    .trend-chart {
      height: 300px;
    }

    .trend-tag {
      margin-bottom: 16px;
    }
  }
}
</style>
