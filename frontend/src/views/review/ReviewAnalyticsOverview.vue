<template>
  <div class="review-analytics-container">
    <el-card shadow="never" class="overview-card">
      <template #header>
        <div class="card-header">
          <span class="card-title">评价数据总览</span>
          <el-radio-group v-model="timeRange" size="default" @change="handleTimeRangeChange">
            <el-radio-button :value="3">近3个月</el-radio-button>
            <el-radio-button :value="6">近6个月</el-radio-button>
            <el-radio-button :value="12">近12个月</el-radio-button>
          </el-radio-group>
        </div>
      </template>

      <el-row :gutter="20" class="stats-row">
        <el-col :xs="12" :sm="8" :md="4">
          <div class="stat-item total-count">
            <div class="stat-icon">
              <el-icon><ChatDotRound /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-label">评价总数</div>
              <div class="stat-value">{{ statistics.totalCount || 0 }}</div>
              <div class="stat-unit">条</div>
            </div>
          </div>
        </el-col>
        <el-col :xs="12" :sm="8" :md="4">
          <div class="stat-item this-month">
            <div class="stat-icon">
              <el-icon><Calendar /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-label">本月新增</div>
              <div class="stat-value">{{ statistics.thisMonthCount || 0 }}</div>
              <div class="stat-unit">条</div>
            </div>
          </div>
        </el-col>
        <el-col :xs="12" :sm="8" :md="4">
          <div class="stat-item avg-score">
            <div class="stat-icon">
              <el-icon><Star /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-label">综合评分</div>
              <div class="stat-value big">{{ statistics.avgScore || '0.0' }}</div>
              <div class="stat-unit">分</div>
            </div>
          </div>
        </el-col>
        <el-col :xs="12" :sm="8" :md="4">
          <div class="stat-item good-rate">
            <div class="stat-icon">
              <el-icon><CircleCheckFilled /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-label">好评率</div>
              <div class="stat-value">{{ statistics.goodRate || 0 }}</div>
              <div class="stat-unit">%</div>
            </div>
          </div>
        </el-col>
        <el-col :xs="12" :sm="8" :md="4">
          <div class="stat-item bad-rate">
            <div class="stat-icon">
              <el-icon><CircleCloseFilled /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-label">差评率</div>
              <div class="stat-value">{{ statistics.badRate || 0 }}</div>
              <div class="stat-unit">%</div>
            </div>
          </div>
        </el-col>
        <el-col :xs="12" :sm="8" :md="4">
          <div class="stat-item reply-rate">
            <div class="stat-icon">
              <el-icon><ChatLineSquare /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-label">回复率</div>
              <div class="stat-value">{{ statistics.replyRate || 0 }}</div>
              <div class="stat-unit">%</div>
            </div>
          </div>
        </el-col>
      </el-row>
    </el-card>

    <el-row :gutter="20">
      <el-col :span="24" :lg="8">
        <el-card shadow="never" class="chart-card">
          <template #header>
            <div class="card-header">
              <span class="card-title">评分分布</span>
            </div>
          </template>
          <div class="distribution-list">
            <div
              v-for="item in statistics.scoreDistribution || []"
              :key="item.score"
              class="distribution-item"
              @click="handleScoreClick(item.score)"
            >
              <div class="dist-score">
                <span>{{ item.score }}</span>
                <el-icon><Star /></el-icon>
              </div>
              <div class="dist-bar-wrapper">
                <div class="dist-bar" :style="{ width: item.percent + '%' }" :class="'score-' + item.score"></div>
              </div>
              <div class="dist-count">
                {{ item.count }}条 ({{ item.percent }}%)
              </div>
            </div>
          </div>
          <div ref="scoreChartRef" class="score-chart"></div>
        </el-card>
      </el-col>
      <el-col :span="24" :lg="16">
        <el-card shadow="never" class="chart-card">
          <template #header>
            <div class="card-header">
              <span class="card-title">评价数量趋势</span>
            </div>
          </template>
          <div ref="quantityChartRef" class="chart"></div>
        </el-card>
      </el-col>
    </el-row>

    <el-card shadow="never" class="chart-card">
      <template #header>
        <div class="card-header">
          <span class="card-title">评分趋势分析</span>
          <el-tag v-if="scoreTrendData.trend === 'rising'" type="success" effect="light">
            <el-icon><Top /></el-icon> 评分上升
          </el-tag>
          <el-tag v-else-if="scoreTrendData.trend === 'falling'" type="danger" effect="light">
            <el-icon><Bottom /></el-icon> 评分下降
          </el-tag>
          <el-tag v-else type="info" effect="light">
            <el-icon><Minus /></el-icon> 评分稳定
          </el-tag>
        </div>
      </template>
      <div ref="scoreTrendChartRef" class="chart"></div>
    </el-card>

    <el-dialog v-model="reviewListVisible" :title="selectedScore + '星评价列表'" width="80%" top="5vh">
      <div v-loading="reviewListLoading" class="review-list-dialog">
        <el-table :data="reviewList" stripe>
          <el-table-column prop="customerName" label="客户" width="120">
            <template #default="{ row }">
              {{ row.isAnonymous === 1 ? '匿名用户' : row.customerName }}
            </template>
          </el-table-column>
          <el-table-column prop="roomTypeName" label="房型" width="120" />
          <el-table-column prop="overallScore" label="评分" width="100">
            <template #default="{ row }">
              <el-rate :model-value="Number(row.overallScore)" disabled size="small" />
            </template>
          </el-table-column>
          <el-table-column prop="reviewContent" label="评价内容" show-overflow-tooltip />
          <el-table-column prop="reviewTime" label="评价时间" width="180" />
        </el-table>
        <el-pagination
          v-model:current-page="reviewPagination.pageNum"
          v-model:page-size="reviewPagination.pageSize"
          :page-sizes="[10, 15, 20]"
          :total="reviewPagination.total"
          layout="total, sizes, prev, pager, next, jumper"
          background
          @size-change="fetchReviewList"
          @current-change="fetchReviewList"
          class="pagination"
        />
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onBeforeUnmount, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  ChatDotRound, Star, CircleCheckFilled, CircleCloseFilled, ChatLineSquare,
  Calendar, Top, Bottom, Minus
} from '@element-plus/icons-vue'
import * as echarts from 'echarts'
import api from '@/api'

const router = useRouter()

const timeRange = ref(12)
const statistics = ref({})
const scoreTrendData = ref({})

const scoreChartRef = ref(null)
const quantityChartRef = ref(null)
const scoreTrendChartRef = ref(null)

let scoreChart = null
let quantityChart = null
let scoreTrendChart = null

const reviewListVisible = ref(false)
const selectedScore = ref(null)
const reviewList = ref([])
const reviewListLoading = ref(false)
const reviewPagination = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0
})

const handleTimeRangeChange = () => {
  fetchQuantityTrend()
  fetchScoreTrend()
}

const handleScoreClick = (score) => {
  selectedScore.value = score
  reviewPagination.pageNum = 1
  fetchReviewList()
  reviewListVisible.value = true
}

const fetchOverview = async () => {
  try {
    const res = await api.reviewAnalytics.getOverview()
    statistics.value = res.data || {}
    await nextTick()
    renderScoreChart()
  } catch (e) {
    console.error(e)
  }
}

const fetchQuantityTrend = async () => {
  try {
    const res = await api.reviewAnalytics.getQuantityTrend(timeRange.value)
    await nextTick()
    renderQuantityChart(res.data?.trendData || [])
  } catch (e) {
    console.error(e)
  }
}

const fetchScoreTrend = async () => {
  try {
    const res = await api.reviewAnalytics.getScoreTrend(timeRange.value)
    scoreTrendData.value = res.data || {}
    await nextTick()
    renderScoreTrendChart(res.data?.trendData || [])
  } catch (e) {
    console.error(e)
  }
}

const fetchReviewList = async () => {
  if (!selectedScore.value) return
  reviewListLoading.value = true
  try {
    const res = await api.reviewAnalytics.getReviewsByScore(
      selectedScore.value,
      reviewPagination.pageNum,
      reviewPagination.pageSize
    )
    reviewList.value = res.data?.list || []
    reviewPagination.total = res.data?.total || 0
  } catch (e) {
    console.error(e)
  } finally {
    reviewListLoading.value = false
  }
}

const renderScoreChart = () => {
  if (!scoreChartRef.value) return
  if (scoreChart) scoreChart.dispose()
  
  scoreChart = echarts.init(scoreChartRef.value)
  
  const distribution = statistics.value.scoreDistribution || []
  const scores = distribution.map(d => d.score + '星')
  const counts = distribution.map(d => d.count)
  const colors = ['#ef4444', '#f97316', '#eab308', '#84cc16', '#22c55e']
  
  scoreChart.setOption({
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
      type: 'category',
      data: scores,
      axisLabel: { color: '#6b7280' }
    },
    yAxis: {
      type: 'value',
      axisLabel: { color: '#6b7280' }
    },
    series: [{
      type: 'bar',
      data: counts,
      itemStyle: {
        color: (params) => colors[params.dataIndex],
        borderRadius: [4, 4, 0, 0]
      },
      barWidth: '50%',
      label: {
        show: true,
        position: 'top',
        formatter: '{c}条'
      }
    }]
  })
}

const renderQuantityChart = (data) => {
  if (!quantityChartRef.value) return
  if (quantityChart) quantityChart.dispose()
  
  quantityChart = echarts.init(quantityChartRef.value)
  
  const months = data.map(d => d.month)
  const counts = data.map(d => d.count)
  
  quantityChart.setOption({
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
      name: '评价数量',
      data: counts,
      smooth: true,
      symbol: 'circle',
      symbolSize: 8,
      lineStyle: {
        width: 3,
        color: '#3b82f6'
      },
      itemStyle: {
        color: '#3b82f6',
        borderWidth: 2,
        borderColor: '#fff'
      },
      areaStyle: {
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: 'rgba(59, 130, 246, 0.3)' },
          { offset: 1, color: 'rgba(59, 130, 246, 0.05)' }
        ])
      }
    }]
  })
}

const renderScoreTrendChart = (data) => {
  if (!scoreTrendChartRef.value) return
  if (scoreTrendChart) scoreTrendChart.dispose()
  
  scoreTrendChart = echarts.init(scoreTrendChartRef.value)
  
  const months = data.map(d => d.month)
  const avgScores = data.map(d => d.avgScore)
  const goodRates = data.map(d => d.goodRate)
  
  scoreTrendChart.setOption({
    tooltip: {
      trigger: 'axis',
      axisPointer: { type: 'cross' }
    },
    legend: {
      data: ['平均评分', '好评率'],
      top: 0
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      top: '15%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: months,
      boundaryGap: false,
      axisLabel: { color: '#6b7280', rotate: 30 }
    },
    yAxis: [
      {
        type: 'value',
        name: '评分',
        min: 0,
        max: 5,
        interval: 1,
        axisLabel: { color: '#6b7280' }
      },
      {
        type: 'value',
        name: '好评率(%)',
        min: 0,
        max: 100,
        interval: 20,
        axisLabel: {
          color: '#6b7280',
          formatter: '{value}%'
        }
      }
    ],
    series: [
      {
        type: 'line',
        name: '平均评分',
        data: avgScores,
        smooth: true,
        symbol: 'circle',
        symbolSize: 8,
        lineStyle: {
          width: 3,
          color: '#f59e0b'
        },
        itemStyle: {
          color: '#f59e0b',
          borderWidth: 2,
          borderColor: '#fff'
        },
        yAxisIndex: 0
      },
      {
        type: 'line',
        name: '好评率',
        data: goodRates,
        smooth: true,
        symbol: 'circle',
        symbolSize: 8,
        lineStyle: {
          width: 3,
          color: '#10b981'
        },
        itemStyle: {
          color: '#10b981',
          borderWidth: 2,
          borderColor: '#fff'
        },
        yAxisIndex: 1
      }
    ]
  })
}

const handleResize = () => {
  scoreChart?.resize()
  quantityChart?.resize()
  scoreTrendChart?.resize()
}

onMounted(() => {
  fetchOverview()
  fetchQuantityTrend()
  fetchScoreTrend()
  window.addEventListener('resize', handleResize)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize)
  scoreChart?.dispose()
  quantityChart?.dispose()
  scoreTrendChart?.dispose()
})
</script>

<style lang="scss" scoped>
.review-analytics-container {
  .overview-card {
    margin-bottom: 20px;
  }

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

  .stats-row {
    .stat-item {
      display: flex;
      align-items: center;
      gap: 16px;
      padding: 16px;
      background: #f9fafb;
      border-radius: 12px;
      transition: transform 0.2s, box-shadow 0.2s;

      &:hover {
        transform: translateY(-2px);
        box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
      }

      .stat-icon {
        width: 56px;
        height: 56px;
        border-radius: 12px;
        display: flex;
        align-items: center;
        justify-content: center;
        font-size: 28px;
        color: #fff;
      }

      .stat-info {
        flex: 1;
        display: flex;
        align-items: baseline;
        flex-wrap: wrap;
        gap: 4px;
      }

      .stat-label {
        width: 100%;
        font-size: 13px;
        color: #6b7280;
        margin-bottom: 4px;
      }

      .stat-value {
        font-size: 28px;
        font-weight: bold;
        line-height: 1;

        &.big {
          font-size: 36px;
        }
      }

      .stat-unit {
        font-size: 14px;
        color: #6b7280;
      }

      &.total-count {
        .stat-icon {
          background: linear-gradient(135deg, #60a5fa, #3b82f6);
        }
        .stat-value {
          color: #2563eb;
        }
      }

      &.this-month {
        .stat-icon {
          background: linear-gradient(135deg, #a78bfa, #8b5cf6);
        }
        .stat-value {
          color: #7c3aed;
        }
      }

      &.avg-score {
        .stat-icon {
          background: linear-gradient(135deg, #fbbf24, #f59e0b);
        }
        .stat-value {
          color: #d97706;
        }
      }

      &.good-rate {
        .stat-icon {
          background: linear-gradient(135deg, #34d399, #10b981);
        }
        .stat-value {
          color: #059669;
        }
      }

      &.bad-rate {
        .stat-icon {
          background: linear-gradient(135deg, #f87171, #ef4444);
        }
        .stat-value {
          color: #dc2626;
        }
      }

      &.reply-rate {
        .stat-icon {
          background: linear-gradient(135deg, #38bdf8, #0ea5e9);
        }
        .stat-value {
          color: #0284c7;
        }
      }
    }
  }

  .chart-card {
    margin-bottom: 20px;

    .distribution-list {
      margin-bottom: 20px;

      .distribution-item {
        display: flex;
        align-items: center;
        gap: 12px;
        padding: 10px 0;
        cursor: pointer;
        border-radius: 6px;
        padding-left: 8px;
        padding-right: 8px;
        transition: background 0.2s;

        &:hover {
          background: #f9fafb;
        }

        .dist-score {
          width: 54px;
          font-size: 14px;
          font-weight: 600;
          color: #d97706;
          display: flex;
          align-items: center;
          gap: 2px;

          .el-icon {
            font-size: 14px;
            color: #fbbf24;
          }
        }

        .dist-bar-wrapper {
          flex: 1;
          height: 12px;
          background: #e5e7eb;
          border-radius: 6px;
          overflow: hidden;

          .dist-bar {
            height: 100%;
            border-radius: 6px;
            transition: width 0.5s;

            &.score-5 { background: #22c55e; }
            &.score-4 { background: #84cc16; }
            &.score-3 { background: #eab308; }
            &.score-2 { background: #f97316; }
            &.score-1 { background: #ef4444; }
          }
        }

        .dist-count {
          width: 100px;
          font-size: 12px;
          color: #6b7280;
          text-align: right;
        }
      }
    }

    .score-chart {
      height: 200px;
    }

    .chart {
      height: 300px;
    }
  }

  .review-list-dialog {
    .pagination {
      margin-top: 20px;
      display: flex;
      justify-content: center;
    }
  }
}
</style>
