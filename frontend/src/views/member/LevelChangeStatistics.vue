<template>
  <div class="level-change-statistics" v-loading="loading">
    <el-row :gutter="16">
      <el-col :span="8">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <div class="stat-icon" style="background: linear-gradient(135deg, #67c23a, #85ce61)">
              <el-icon :size="32"><TrendCharts /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ statistics.upgradeThisMonth || 0 }}</div>
              <div class="stat-label">本月升级人数</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <div class="stat-icon" style="background: linear-gradient(135deg, #e6a23c, #f0c78a)">
              <el-icon :size="32"><TrendCharts /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ statistics.downgradeThisMonth || 0 }}</div>
              <div class="stat-label">本月降级人数</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <div class="stat-icon" style="background: linear-gradient(135deg, #409eff, #79bbff)">
              <el-icon :size="32"><DataLine /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ totalChange }}</div>
              <div class="stat-label">本月净变动</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="16" style="margin-top: 16px">
      <el-col :span="24">
        <el-card shadow="never">
          <template #header>
            <div class="card-header">
              <span>升降级趋势（近12个月）</span>
            </div>
          </template>
          <div ref="trendChartRef" style="width: 100%; height: 360px"></div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="16" style="margin-top: 16px">
      <el-col :span="24">
        <el-card shadow="never">
          <template #header>
            <div class="card-header">
              <span>按等级统计升降级情况</span>
            </div>
          </template>
          <el-table :data="statistics.byLevel || []" stripe border style="width: 100%">
            <el-table-column prop="levelName" label="会员等级" width="160">
              <template #default="{ row }">
                <span
                  class="level-badge"
                  :style="{
                    background: (row.levelColor || '#909399') + '20',
                    color: row.levelColor || '#909399'
                  }"
                >
                  {{ row.levelName }}
                </span>
              </template>
            </el-table-column>
            <el-table-column prop="upgradeCount" label="升级到此等级人数" width="200" align="center">
              <template #default="{ row }">
                <span style="color: #67c23a; font-weight: 600">{{ row.upgradeCount || 0 }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="downgradeCount" label="降级到此等级人数" width="200" align="center">
              <template #default="{ row }">
                <span style="color: #e6a23c; font-weight: 600">{{ row.downgradeCount || 0 }}</span>
              </template>
            </el-table-column>
            <el-table-column label="合计" align="center">
              <template #default="{ row }">
                {{ (row.upgradeCount || 0) + (row.downgradeCount || 0) }}
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, nextTick, watch } from 'vue'
import { TrendCharts, DataLine } from '@element-plus/icons-vue'
import * as echarts from 'echarts'
import api from '@/api'

const loading = ref(false)
const statistics = reactive({
  upgradeThisMonth: 0,
  downgradeThisMonth: 0,
  byLevel: [],
  trend: []
})
const trendChartRef = ref(null)
let trendChart = null

const totalChange = computed(() => {
  return (statistics.upgradeThisMonth || 0) - (statistics.downgradeThisMonth || 0)
})

const loadStatistics = async () => {
  loading.value = true
  try {
    const res = await api.memberLevelChange.statistics()
    if (res.code === 200 && res.data) {
      Object.assign(statistics, res.data)
      await nextTick()
      renderTrendChart()
    }
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

const renderTrendChart = () => {
  if (!trendChartRef.value) return
  if (!trendChart) {
    trendChart = echarts.init(trendChartRef.value)
  }
  const months = (statistics.trend || []).map(t => t.month)
  const upgradeData = (statistics.trend || []).map(t => t.upgradeCount || 0)
  const downgradeData = (statistics.trend || []).map(t => t.downgradeCount || 0)

  const option = {
    tooltip: {
      trigger: 'axis'
    },
    legend: {
      data: ['升级人数', '降级人数']
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: months
    },
    yAxis: {
      type: 'value',
      minInterval: 1
    },
    series: [
      {
        name: '升级人数',
        type: 'line',
        smooth: true,
        data: upgradeData,
        itemStyle: { color: '#67c23a' },
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
        smooth: true,
        data: downgradeData,
        itemStyle: { color: '#e6a23c' },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(230, 162, 60, 0.3)' },
            { offset: 1, color: 'rgba(230, 162, 60, 0.05)' }
          ])
        }
      }
    ]
  }
  trendChart.setOption(option)
}

const handleResize = () => {
  if (trendChart) {
    trendChart.resize()
  }
}

onMounted(() => {
  loadStatistics()
  window.addEventListener('resize', handleResize)
})
</script>

<style scoped>
.level-change-statistics {
  padding: 16px;
}

.stat-card {
  border-radius: 8px;
}

.stat-content {
  display: flex;
  align-items: center;
  gap: 16px;
}

.stat-icon {
  width: 64px;
  height: 64px;
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
  color: #303133;
  line-height: 1.2;
}

.stat-label {
  font-size: 13px;
  color: #909399;
  margin-top: 4px;
}

.card-header {
  font-weight: 600;
  color: #303133;
}

.level-badge {
  display: inline-flex;
  align-items: center;
  padding: 4px 12px;
  border-radius: 12px;
  font-size: 13px;
  font-weight: 500;
}
</style>
