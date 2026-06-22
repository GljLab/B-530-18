<template>
  <div class="room-type-analytics-container">
    <el-card shadow="never" class="chart-card">
      <template #header>
        <div class="card-header">
          <span class="card-title">房型评价对比</span>
          <div class="summary-tags">
            <el-tag v-if="roomTypeData.bestRoomType" type="success" effect="light">
              <el-icon><Medal /></el-icon> 评分最高：{{ roomTypeData.bestRoomType }}
            </el-tag>
            <el-tag v-if="roomTypeData.worstRoomType" type="danger" effect="light">
              <el-icon><Warning /></el-icon> 评分最低：{{ roomTypeData.worstRoomType }}
            </el-tag>
          </div>
        </div>
      </template>
      <el-table :data="roomTypeData.roomTypes || []" stripe>
        <el-table-column label="排名" width="70" align="center">
          <template #default="{ row }">
            <el-tag v-if="row.rank === 1" type="warning" effect="dark">1</el-tag>
            <el-tag v-else-if="row.rank === 2" type="info" effect="light">2</el-tag>
            <el-tag v-else-if="row.rank === 3" type="info" effect="plain">3</el-tag>
            <span v-else>{{ row.rank }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="roomTypeName" label="房型名称" min-width="150">
          <template #default="{ row }">
            <span class="highest" v-if="row.rank === 1">
              <el-icon><Medal /></el-icon> {{ row.roomTypeName }}
            </span>
            <span class="lowest" v-else-if="row.rank === roomTypeData.roomTypes?.length && roomTypeData.roomTypes?.length > 1 && row.reviewCount > 0">
              <el-icon><Warning /></el-icon> {{ row.roomTypeName }}
            </span>
            <span v-else>{{ row.roomTypeName }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="reviewCount" label="评价数量" width="100" align="center" />
        <el-table-column prop="avgScore" label="平均评分" width="120" align="center">
          <template #default="{ row }">
            <span class="score-high" v-if="row.avgScore >= 4.5">{{ row.avgScore }}</span>
            <span class="score-low" v-else-if="row.avgScore < 3.5 && row.reviewCount > 0">{{ row.avgScore }}</span>
            <span v-else>{{ row.avgScore || '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="goodRate" label="好评率" width="100" align="center">
          <template #default="{ row }">
            <el-tag v-if="row.goodRate >= 90" type="success" effect="plain">{{ row.goodRate }}%</el-tag>
            <el-tag v-else-if="row.goodRate >= 70" type="warning" effect="plain">{{ row.goodRate }}%</el-tag>
            <el-tag v-else type="danger" effect="plain">{{ row.goodRate }}%</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="badRate" label="差评率" width="100" align="center">
          <template #default="{ row }">
            <el-tag v-if="row.badRate <= 3" type="success" effect="plain">{{ row.badRate }}%</el-tag>
            <el-tag v-else-if="row.badRate <= 10" type="warning" effect="plain">{{ row.badRate }}%</el-tag>
            <el-tag v-else type="danger" effect="plain">{{ row.badRate }}%</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150" align="center" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="viewRoomTypeDetail(row)">
              查看详情
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-row :gutter="20" v-if="selectedRoomType">
      <el-col :span="24" :lg="8">
        <el-card shadow="never" class="chart-card">
          <template #header>
            <div class="card-header">
              <span class="card-title">{{ selectedRoomType.roomTypeName }} - 指标雷达图</span>
            </div>
          </template>
          <div ref="roomTypeRadarRef" class="radar-chart"></div>
        </el-card>
      </el-col>
      <el-col :span="24" :lg="16">
        <el-card shadow="never" class="chart-card">
          <template #header>
            <div class="card-header">
              <span class="card-title">指标对比分析</span>
            </div>
          </template>
          <el-table :data="roomTypeMetrics.metrics || []" stripe>
            <el-table-column prop="metricName" label="指标名称" min-width="120" />
            <el-table-column prop="roomTypeScore" label="房型评分" width="120" align="center">
              <template #default="{ row }">
                <span :class="row.difference > 0 ? 'score-high' : row.difference < 0 ? 'score-low' : ''">
                  {{ row.roomTypeScore || '-' }}
                </span>
              </template>
            </el-table-column>
            <el-table-column prop="overallScore" label="酒店平均" width="120" align="center" />
            <el-table-column label="差异" width="100" align="center">
              <template #default="{ row }">
                <span v-if="row.difference > 0" class="change-up">
                  <el-icon><Top /></el-icon> +{{ row.difference }}
                </span>
                <span v-else-if="row.difference < 0" class="change-down">
                  <el-icon><Bottom /></el-icon> {{ row.difference }}
                </span>
                <span v-else class="change-stable">-</span>
              </template>
            </el-table-column>
            <el-table-column label="对比" min-width="200">
              <template #default="{ row }">
                <div class="compare-bar-wrapper">
                  <div class="compare-bar overall" :style="{ width: getBarWidth(row.overallScore) + '%' }">
                    <span class="bar-label">平均</span>
                  </div>
                </div>
                <div class="compare-bar-wrapper">
                  <div class="compare-bar room-type" :style="{ width: getBarWidth(row.roomTypeScore) + '%' }">
                    <span class="bar-label">房型</span>
                  </div>
                </div>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
    </el-row>

    <el-card shadow="never" class="chart-card" v-if="selectedRoomType">
      <template #header>
        <div class="card-header">
          <span class="card-title">{{ selectedRoomType.roomTypeName }} - 评价列表</span>
        </div>
      </template>
      <div v-loading="reviewListLoading" class="review-list">
        <el-table :data="reviewList" stripe>
          <el-table-column prop="customerName" label="客户" width="120">
            <template #default="{ row }">
              {{ row.isAnonymous === 1 ? '匿名用户' : row.customerName }}
            </template>
          </el-table-column>
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
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onBeforeUnmount, nextTick } from 'vue'
import {
  Medal, Warning, Top, Bottom
} from '@element-plus/icons-vue'
import * as echarts from 'echarts'
import api from '@/api'

const roomTypeData = ref({})
const roomTypeMetrics = ref({})
const selectedRoomType = ref(null)

const roomTypeRadarRef = ref(null)
let roomTypeRadarChart = null

const reviewList = ref([])
const reviewListLoading = ref(false)
const reviewPagination = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0
})

const getBarWidth = (score) => {
  if (!score) return 0
  return (score / 5) * 100
}

const viewRoomTypeDetail = async (roomType) => {
  selectedRoomType.value = roomType
  reviewPagination.pageNum = 1
  
  try {
    const res = await api.reviewAnalytics.getRoomTypeMetrics(roomType.roomTypeId)
    roomTypeMetrics.value = res.data || {}
    await nextTick()
    renderRoomTypeRadarChart()
    fetchReviewList()
  } catch (e) {
    console.error(e)
  }
}

const fetchRoomTypeAnalysis = async () => {
  try {
    const res = await api.reviewAnalytics.getRoomTypeAnalysis()
    roomTypeData.value = res.data || {}
    
    if (roomTypeData.value.roomTypes && roomTypeData.value.roomTypes.length > 0) {
      viewRoomTypeDetail(roomTypeData.value.roomTypes[0])
    }
  } catch (e) {
    console.error(e)
  }
}

const fetchReviewList = async () => {
  if (!selectedRoomType.value) return
  reviewListLoading.value = true
  try {
    const res = await api.reviewAnalytics.getReviewsByRoomType(
      selectedRoomType.value.roomTypeId,
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

const renderRoomTypeRadarChart = () => {
  if (!roomTypeRadarRef.value) return
  if (roomTypeRadarChart) roomTypeRadarChart.dispose()
  
  roomTypeRadarChart = echarts.init(roomTypeRadarRef.value)
  
  const metrics = roomTypeMetrics.value.metrics || []
  const indicator = metrics.map(m => ({
    name: m.metricName,
    max: 5
  }))
  const roomTypeValues = metrics.map(m => m.roomTypeScore || 0)
  const overallValues = metrics.map(m => m.overallScore || 0)
  
  roomTypeRadarChart.setOption({
    tooltip: {
      trigger: 'item'
    },
    legend: {
      data: ['房型评分', '酒店平均'],
      top: 0
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
      data: [
        {
          value: roomTypeValues,
          name: '房型评分',
          symbol: 'circle',
          symbolSize: 6,
          lineStyle: {
            width: 2,
            color: '#3b82f6'
          },
          itemStyle: {
            color: '#3b82f6'
          },
          areaStyle: {
            color: 'rgba(59, 130, 246, 0.2)'
          }
        },
        {
          value: overallValues,
          name: '酒店平均',
          symbol: 'circle',
          symbolSize: 6,
          lineStyle: {
            width: 2,
            color: '#9ca3af'
          },
          itemStyle: {
            color: '#9ca3af'
          },
          areaStyle: {
            color: 'rgba(156, 163, 175, 0.1)'
          }
        }
      ]
    }]
  })
}

const handleResize = () => {
  roomTypeRadarChart?.resize()
}

onMounted(() => {
  fetchRoomTypeAnalysis()
  window.addEventListener('resize', handleResize)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize)
  roomTypeRadarChart?.dispose()
})
</script>

<style lang="scss" scoped>
.room-type-analytics-container {
  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    flex-wrap: wrap;
    gap: 10px;
  }

  .card-title {
    font-size: 16px;
    font-weight: 600;
    color: #1f2937;
  }

  .summary-tags {
    display: flex;
    gap: 10px;
  }

  .chart-card {
    margin-bottom: 20px;

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
      display: flex;
      align-items: center;
      justify-content: center;
      gap: 2px;
    }

    .change-down {
      color: #dc2626;
      font-weight: 600;
      display: flex;
      align-items: center;
      justify-content: center;
      gap: 2px;
    }

    .change-stable {
      color: #6b7280;
    }

    .radar-chart {
      height: 300px;
    }

    .compare-bar-wrapper {
      margin-bottom: 4px;
      height: 20px;
      position: relative;

      &:last-child {
        margin-bottom: 0;
      }

      .compare-bar {
        height: 20px;
        border-radius: 4px;
        position: relative;
        min-width: 60px;
        transition: width 0.5s;

        &.overall {
          background: linear-gradient(90deg, #9ca3af, #6b7280);
        }

        &.room-type {
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

    .review-list {
      .pagination {
        margin-top: 20px;
        display: flex;
        justify-content: center;
      }
    }
  }
}
</style>
