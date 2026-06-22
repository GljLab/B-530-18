<template>
  <div class="metric-analytics-container">
    <el-row :gutter="20">
      <el-col :span="24" :lg="8">
        <el-card shadow="never" class="chart-card">
          <template #header>
            <div class="card-header">
              <span class="card-title">指标雷达图</span>
            </div>
          </template>
          <div class="strength-weakness">
            <el-tag type="success" effect="light">
              <el-icon><Top /></el-icon> 最强：{{ radarData.strongestMetric || '-' }}
            </el-tag>
            <el-tag type="danger" effect="light">
              <el-icon><Bottom /></el-icon> 最弱：{{ radarData.weakestMetric || '-' }}
            </el-tag>
          </div>
          <div ref="radarChartRef" class="radar-chart"></div>
        </el-card>
      </el-col>
      <el-col :span="24" :lg="16">
        <el-card shadow="never" class="chart-card">
          <template #header>
            <div class="card-header">
              <span class="card-title">指标评分对比</span>
            </div>
          </template>
          <el-table :data="radarData.metrics || []" stripe>
            <el-table-column prop="rank" label="排名" width="70" align="center">
              <template #default="{ row }">
                <el-tag v-if="row.rank === 1" type="warning" effect="dark">1</el-tag>
                <el-tag v-else-if="row.rank === 2" type="info" effect="light">2</el-tag>
                <el-tag v-else-if="row.rank === 3" type="info" effect="plain">3</el-tag>
                <span v-else>{{ row.rank }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="metricName" label="指标名称" min-width="120">
              <template #default="{ row, $index }">
                <span class="highest" v-if="$index === 0">
                  <el-icon><Medal /></el-icon> {{ row.metricName }}
                </span>
                <span class="lowest" v-else-if="$index === radarData.metrics.length - 1 && radarData.metrics.length > 1">
                  <el-icon><Warning /></el-icon> {{ row.metricName }}
                </span>
                <span v-else>{{ row.metricName }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="avgScore" label="平均分" width="100" align="center">
              <template #default="{ row }">
                <span class="score-high" v-if="row.avgScore >= 4.5">{{ row.avgScore }}</span>
                <span class="score-low" v-else-if="row.avgScore < 3.5">{{ row.avgScore }}</span>
                <span v-else>{{ row.avgScore }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="maxScore" label="最高" width="80" align="center" />
            <el-table-column prop="minScore" label="最低" width="80" align="center" />
            <el-table-column prop="stdDev" label="标准差" width="100" align="center" />
            <el-table-column prop="count" label="评价数" width="90" align="center" />
          </el-table>
        </el-card>
      </el-col>
    </el-row>

    <el-card shadow="never" class="chart-card">
      <template #header>
        <div class="card-header">
          <span class="card-title">指标改进趋势</span>
          <el-radio-group v-model="periodType" size="default" @change="handlePeriodChange">
            <el-radio-button value="month">本月vs上月</el-radio-button>
            <el-radio-button value="quarter">本季度vs上季度</el-radio-button>
          </el-radio-group>
        </div>
      </template>

      <div v-if="comparisonData.improvedMetrics && comparisonData.improvedMetrics.length > 0" class="summary-tags">
        <el-tag type="success" effect="light" class="summary-tag">
          <el-icon><Top /></el-icon>
          进步明显：{{ comparisonData.improvedMetrics.join('、') }}
        </el-tag>
        <el-tag v-if="comparisonData.declinedMetrics && comparisonData.declinedMetrics.length > 0" type="danger" effect="light" class="summary-tag">
          <el-icon><Bottom /></el-icon>
          退步明显：{{ comparisonData.declinedMetrics.join('、') }}
        </el-tag>
      </div>

      <el-table :data="comparisonData.comparisonData || []" stripe>
        <el-table-column prop="metricName" label="指标名称" min-width="120" />
        <el-table-column prop="previousScore" label="上期评分" width="120" align="center" />
        <el-table-column prop="currentScore" label="本期评分" width="120" align="center" />
        <el-table-column label="变化幅度" width="120" align="center">
          <template #default="{ row }">
            <span :class="getChangeClass(row.change)">
              {{ row.change > 0 ? '+' : '' }}{{ row.change }}分
            </span>
          </template>
        </el-table-column>
        <el-table-column label="变化趋势" width="100" align="center">
          <template #default="{ row }">
            <el-icon v-if="row.trend === 'rising'" class="trend-up"><Top /></el-icon>
            <el-icon v-else-if="row.trend === 'falling'" class="trend-down"><Bottom /></el-icon>
            <el-icon v-else class="trend-stable"><Minus /></el-icon>
          </template>
        </el-table-column>
        <el-table-column label="可视化" min-width="200">
          <template #default="{ row }">
            <div class="trend-bar-wrapper">
              <div class="trend-bar previous" :style="{ width: getBarWidth(row.previousScore) + '%' }">
                <span class="bar-label">上期</span>
              </div>
            </div>
            <div class="trend-bar-wrapper">
              <div class="trend-bar current" :style="{ width: getBarWidth(row.currentScore) + '%' }">
                <span class="bar-label">本期</span>
              </div>
            </div>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onBeforeUnmount, nextTick } from 'vue'
import {
  Top, Bottom, Minus, Medal, Warning
} from '@element-plus/icons-vue'
import * as echarts from 'echarts'
import api from '@/api'

const periodType = ref('month')
const radarData = ref({})
const comparisonData = ref({})

const radarChartRef = ref(null)
let radarChart = null

const handlePeriodChange = () => {
  fetchMetricComparison()
}

const getChangeClass = (change) => {
  if (change > 0) return 'change-up'
  if (change < 0) return 'change-down'
  return 'change-stable'
}

const getBarWidth = (score) => {
  return (score / 5) * 100
}

const fetchMetricRadar = async () => {
  try {
    const res = await api.reviewAnalytics.getMetricRadar()
    radarData.value = res.data || {}
    await nextTick()
    renderRadarChart()
  } catch (e) {
    console.error(e)
  }
}

const fetchMetricComparison = async () => {
  try {
    const res = await api.reviewAnalytics.getMetricComparison(periodType.value)
    comparisonData.value = res.data || {}
  } catch (e) {
    console.error(e)
  }
}

const renderRadarChart = () => {
  if (!radarChartRef.value) return
  if (radarChart) radarChart.dispose()
  
  radarChart = echarts.init(radarChartRef.value)
  
  const metrics = radarData.value.metrics || []
  const indicator = metrics.map(m => ({
    name: m.metricName,
    max: 5
  }))
  const values = metrics.map(m => m.avgScore || 0)
  
  radarChart.setOption({
    tooltip: {
      trigger: 'item'
    },
    radar: {
      indicator: indicator,
      shape: 'polygon',
      splitNumber: 5,
      axisName: {
        color: '#6b7280',
        fontSize: 12
      },
      splitLine: {
        lineStyle: {
          color: 'rgba(0, 0, 0, 0.1)'
        }
      },
      splitArea: {
        show: true,
        areaStyle: {
          color: ['rgba(59, 130, 246, 0.02)', 'rgba(59, 130, 246, 0.05)']
        }
      }
    },
    series: [{
      type: 'radar',
      data: [{
        value: values,
        name: '平均评分',
        symbol: 'circle',
        symbolSize: 8,
        lineStyle: {
          width: 2,
          color: '#3b82f6'
        },
        itemStyle: {
          color: '#3b82f6'
        },
        areaStyle: {
          color: new echarts.graphic.RadialGradient(0.5, 0.5, 1, [
            { offset: 0, color: 'rgba(59, 130, 246, 0.4)' },
            { offset: 1, color: 'rgba(59, 130, 246, 0.1)' }
          ])
        }
      }]
    }]
  })
}

const handleResize = () => {
  radarChart?.resize()
}

onMounted(() => {
  fetchMetricRadar()
  fetchMetricComparison()
  window.addEventListener('resize', handleResize)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize)
  radarChart?.dispose()
})
</script>

<style lang="scss" scoped>
.metric-analytics-container {
  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }

  .card-title {
    font-size: 16px;
    font-weight: 600;
    color: #1f2937;
  }

  .chart-card {
    margin-bottom: 20px;

    .strength-weakness {
      display: flex;
      gap: 10px;
      margin-bottom: 16px;
      flex-wrap: wrap;
    }

    .radar-chart {
      height: 300px;
    }

    .summary-tags {
      margin-bottom: 16px;
      display: flex;
      gap: 10px;
      flex-wrap: wrap;

      .summary-tag {
        display: flex;
        align-items: center;
        gap: 4px;
      }
    }

    .highest {
      color: #d97706;
      font-weight: 600;
      display: flex;
      align-items: center;
      gap: 4px;

      .el-icon {
        color: #f59e0b;
      }
    }

    .lowest {
      color: #dc2626;
      display: flex;
      align-items: center;
      gap: 4px;

      .el-icon {
        color: #ef4444;
      }
    }

    .score-high {
      color: #059669;
      font-weight: 600;
    }

    .score-low {
      color: #dc2626;
    }

    .change-up {
      color: #059669;
      font-weight: 600;
    }

    .change-down {
      color: #dc2626;
      font-weight: 600;
    }

    .change-stable {
      color: #6b7280;
    }

    .trend-up {
      color: #10b981;
      font-size: 18px;
    }

    .trend-down {
      color: #ef4444;
      font-size: 18px;
    }

    .trend-stable {
      color: #6b7280;
      font-size: 18px;
    }

    .trend-bar-wrapper {
      margin-bottom: 4px;
      height: 20px;
      position: relative;

      &:last-child {
        margin-bottom: 0;
      }

      .trend-bar {
        height: 20px;
        border-radius: 4px;
        position: relative;
        min-width: 60px;
        transition: width 0.5s;

        &.previous {
          background: linear-gradient(90deg, #9ca3af, #6b7280);
        }

        &.current {
          background: linear-gradient(90deg, #60a5fa, #3b82f6);
        }

        .bar-label {
          position: absolute;
          left: 8px;
          top: 50%;
          transform: translateY(-50%);
          font-size: 11px;
          color: #fff;
          font-weight: 500;
        }
      }
    }
  }
}
</style>
