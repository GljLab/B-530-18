<template>
  <div class="checkin-statistics">
    <el-card class="overview-cards">
      <el-row :gutter="16">
        <el-col :span="4">
          <div class="stat-card total-rooms">
            <div class="stat-icon">
              <el-icon :size="28"><OfficeBuilding /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ overview.totalRooms || 0 }}</div>
              <div class="stat-label">总房间数</div>
            </div>
          </div>
        </el-col>
        <el-col :span="4">
          <div class="stat-card in-house-rooms">
            <div class="stat-icon">
              <el-icon :size="28"><House /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ overview.inHouseRooms || 0 }}</div>
              <div class="stat-label">在住房间数</div>
            </div>
          </div>
        </el-col>
        <el-col :span="4">
          <div class="stat-card occupancy-rate">
            <div class="stat-icon">
              <el-icon :size="28"><TrendCharts /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ occupancyRate }}%</div>
              <div class="stat-label">入住率</div>
            </div>
          </div>
        </el-col>
        <el-col :span="4">
          <div class="stat-card today-checkin">
            <div class="stat-icon">
              <el-icon :size="28"><User /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ overview.todayCheckIn || 0 }}</div>
              <div class="stat-label">今日入住</div>
            </div>
          </div>
        </el-col>
        <el-col :span="4">
          <div class="stat-card today-checkout">
            <div class="stat-icon">
              <el-icon :size="28"><SwitchButton /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ overview.todayCheckOut || 0 }}</div>
              <div class="stat-label">今日退房</div>
            </div>
          </div>
        </el-col>
        <el-col :span="4">
          <div class="stat-card overdue">
            <div class="stat-icon">
              <el-icon :size="28"><Warning /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ overview.overdueCount || 0 }}</div>
              <div class="stat-label">超期未退</div>
            </div>
          </div>
        </el-col>
      </el-row>
    </el-card>

    <el-row :gutter="16" class="charts-row">
      <el-col :span="14">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>近7日入住退房趋势</span>
              <el-radio-group v-model="trendDays" size="small" @change="loadTrendData">
                <el-radio-button :label="7">7天</el-radio-button>
                <el-radio-button :label="14">14天</el-radio-button>
                <el-radio-button :label="30">30天</el-radio-button>
              </el-radio-group>
            </div>
          </template>
          <div class="trend-chart">
            <div class="chart-legend">
              <span class="legend-item">
                <span class="legend-dot checkin-dot"></span>
                入住
              </span>
              <span class="legend-item">
                <span class="legend-dot checkout-dot"></span>
                退房
              </span>
            </div>
            <div class="chart-bars">
              <div class="bar-group" v-for="(item, index) in trendData" :key="index">
                <div class="bars-wrapper">
                  <div
                    class="bar checkin-bar"
                    :style="{ height: getBarHeight(item.checkInCount, maxTrendValue) + '%' }"
                    :title="`入住: ${item.checkInCount}`"
                  ></div>
                  <div
                    class="bar checkout-bar"
                    :style="{ height: getBarHeight(item.checkOutCount, maxTrendValue) + '%' }"
                    :title="`退房: ${item.checkOutCount}`"
                  ></div>
                </div>
                <div class="bar-label">{{ formatDate(item.date) }}</div>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="10">
        <el-card>
          <template #header>
            <span>客源分析</span>
          </template>
          <div class="source-chart">
            <div class="source-list">
              <div class="source-item" v-for="(item, index) in sourceData" :key="index">
                <div class="source-header">
                  <span class="source-name">{{ item.sourceName }}</span>
                  <span class="source-count">{{ item.count }} 单</span>
                </div>
                <div class="source-bar-wrapper">
                  <div
                    class="source-bar"
                    :style="{ width: getSourcePercent(item.count) + '%', background: getSourceColor(index) }"
                  ></div>
                </div>
                <div class="source-percent">{{ getSourcePercent(item.count).toFixed(1) }}%</div>
              </div>
            </div>
            <div class="source-total">
              <span>总计: {{ sourceTotal }} 单</span>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="16" class="charts-row">
      <el-col :span="12">
        <el-card>
          <template #header>
            <span>房型入住统计</span>
          </template>
          <div class="roomtype-table">
            <el-table :data="roomTypeData" stripe border style="width: 100%">
              <el-table-column prop="typeName" label="房型" width="120" />
              <el-table-column prop="totalRooms" label="房间数" width="80" align="center" />
              <el-table-column prop="inHouseRooms" label="在住数" width="80" align="center" />
              <el-table-column label="入住率" min-width="180">
                <template #default="{ row }">
                  <div class="rate-progress">
                    <el-progress
                      :percentage="getRoomTypeOccupancyRate(row)"
                      :stroke-width="12"
                      :color="getRateColor(getRoomTypeOccupancyRate(row))"
                    />
                    <span class="rate-text">{{ getRoomTypeOccupancyRate(row) }}%</span>
                  </div>
                </template>
              </el-table-column>
              <el-table-column prop="avgPrice" label="平均房价" width="100" align="right">
                <template #default="{ row }">
                  ¥{{ row.avgPrice || 0 }}
                </template>
              </el-table-column>
            </el-table>
          </div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card>
          <template #header>
            <span>楼层入住统计</span>
          </template>
          <div class="floor-table">
            <el-table :data="floorData" stripe border style="width: 100%">
              <el-table-column prop="floorName" label="楼层" width="100" align="center" />
              <el-table-column prop="totalRooms" label="房间数" width="80" align="center" />
              <el-table-column prop="inHouseRooms" label="在住数" width="80" align="center" />
              <el-table-column prop="vacantRooms" label="空房数" width="80" align="center" />
              <el-table-column label="入住率" min-width="180">
                <template #default="{ row }">
                  <div class="rate-progress">
                    <el-progress
                      :percentage="getFloorOccupancyRate(row)"
                      :stroke-width="12"
                      :color="getRateColor(getFloorOccupancyRate(row))"
                    />
                    <span class="rate-text">{{ getFloorOccupancyRate(row) }}%</span>
                  </div>
                </template>
              </el-table-column>
            </el-table>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-card class="stay-duration-card">
      <template #header>
        <div class="card-header">
          <span>平均住宿时长</span>
          <el-button type="primary" size="small" @click="refreshData">
            <el-icon><Refresh /></el-icon>
            刷新
          </el-button>
        </div>
      </template>
      <el-row :gutter="16">
        <el-col :span="6">
          <div class="duration-summary">
            <div class="duration-value">{{ averageStay.overallAvg || 0 }}</div>
            <div class="duration-unit">天</div>
            <div class="duration-label">总平均住宿时长</div>
          </div>
        </el-col>
        <el-col :span="10">
          <div class="duration-by-type">
            <div class="section-title">按房型统计</div>
            <div class="type-duration-list">
              <div class="type-duration-item" v-for="(item, index) in averageStay.byRoomType" :key="index">
                <span class="type-name">{{ item.typeName }}</span>
                <div class="type-duration-bar">
                  <div
                    class="type-bar-inner"
                    :style="{ width: getTypeDurationPercent(item.avgDays) + '%' }"
                  ></div>
                </div>
                <span class="type-days">{{ item.avgDays || 0 }}天</span>
              </div>
            </div>
          </div>
        </el-col>
        <el-col :span="8">
          <div class="duration-distribution">
            <div class="section-title">住宿时长分布</div>
            <div class="distribution-list">
              <div class="distribution-item" v-for="(item, index) in averageStay.distribution" :key="index">
                <span class="dist-range">{{ item.range }}</span>
                <div class="dist-bar-wrapper">
                  <div
                    class="dist-bar"
                    :style="{ width: getDistPercent(item.count) + '%' }"
                  ></div>
                </div>
                <span class="dist-count">{{ item.count }}单</span>
              </div>
            </div>
          </div>
        </el-col>
      </el-row>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import {
  OfficeBuilding, House, TrendCharts, User, SwitchButton, Warning, Refresh
} from '@element-plus/icons-vue'
import api from '@/api'

const loading = ref(false)
const trendDays = ref(7)

const overview = ref({})
const trendData = ref([])
const roomTypeData = ref([])
const floorData = ref([])
const sourceData = ref([])
const customerTypeData = ref([])
const averageStay = reactive({
  overallAvg: 0,
  byRoomType: [],
  distribution: []
})

const occupancyRate = computed(() => {
  if (!overview.value.totalRooms) return 0
  return ((overview.value.inHouseRooms / overview.value.totalRooms) * 100).toFixed(1)
})

const maxTrendValue = computed(() => {
  let max = 0
  trendData.value.forEach(item => {
    max = Math.max(max, item.checkInCount || 0, item.checkOutCount || 0)
  })
  return max || 1
})

const sourceTotal = computed(() => {
  return sourceData.value.reduce((sum, item) => sum + (item.count || 0), 0)
})

const maxTypeDuration = computed(() => {
  let max = 0
  averageStay.byRoomType.forEach(item => {
    max = Math.max(max, item.avgDays || 0)
  })
  return max || 1
})

const maxDistCount = computed(() => {
  let max = 0
  averageStay.distribution.forEach(item => {
    max = Math.max(max, item.count || 0)
  })
  return max || 1
})

const loadOverview = async () => {
  try {
    const res = await api.checkin.statistics.overview()
    if (res.code === 200) {
      overview.value = res.data
    }
  } catch (e) {
    console.error('加载概览数据失败', e)
  }
}

const loadTrendData = async () => {
  try {
    const res = await api.checkin.statistics.trend(trendDays.value)
    if (res.code === 200) {
      trendData.value = res.data || []
    }
  } catch (e) {
    console.error('加载趋势数据失败', e)
  }
}

const loadRoomTypeData = async () => {
  try {
    const res = await api.checkin.statistics.roomType()
    if (res.code === 200) {
      roomTypeData.value = res.data || []
    }
  } catch (e) {
    console.error('加载房型统计失败', e)
  }
}

const loadFloorData = async () => {
  try {
    const res = await api.checkin.statistics.floor()
    if (res.code === 200) {
      floorData.value = res.data || []
    }
  } catch (e) {
    console.error('加载楼层统计失败', e)
  }
}

const loadSourceData = async () => {
  try {
    const res = await api.checkin.statistics.source()
    if (res.code === 200) {
      sourceData.value = res.data || []
    }
  } catch (e) {
    console.error('加载客源统计失败', e)
  }
}

const loadAverageStay = async () => {
  try {
    const res = await api.checkin.statistics.averageStay()
    if (res.code === 200) {
      const data = res.data || {}
      averageStay.overallAvg = data.overallAvg || 0
      averageStay.byRoomType = data.byRoomType || []
      averageStay.distribution = data.distribution || []
    }
  } catch (e) {
    console.error('加载平均住宿时长失败', e)
  }
}

const getBarHeight = (value, max) => {
  if (!max) return 0
  return (value / max) * 100
}

const formatDate = (dateStr) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  return `${date.getMonth() + 1}/${date.getDate()}`
}

const getSourcePercent = (count) => {
  if (!sourceTotal.value) return 0
  return (count / sourceTotal.value) * 100
}

const sourceColors = [
  '#409eff', '#67c23a', '#e6a23c', '#f56c6c',
  '#909399', '#8e44ad', '#16a085', '#d35400'
]

const getSourceColor = (index) => {
  return sourceColors[index % sourceColors.length]
}

const getRoomTypeOccupancyRate = (row) => {
  if (!row.totalRooms) return 0
  return Number(((row.inHouseRooms / row.totalRooms) * 100).toFixed(1))
}

const getFloorOccupancyRate = (row) => {
  if (!row.totalRooms) return 0
  return Number(((row.inHouseRooms / row.totalRooms) * 100).toFixed(1))
}

const getRateColor = (rate) => {
  if (rate >= 80) return '#67c23a'
  if (rate >= 60) return '#409eff'
  if (rate >= 40) return '#e6a23c'
  return '#f56c6c'
}

const getTypeDurationPercent = (days) => {
  if (!maxTypeDuration.value) return 0
  return (days / maxTypeDuration.value) * 100
}

const getDistPercent = (count) => {
  if (!maxDistCount.value) return 0
  return (count / maxDistCount.value) * 100
}

const refreshData = () => {
  loadOverview()
  loadTrendData()
  loadRoomTypeData()
  loadFloorData()
  loadSourceData()
  loadAverageStay()
  ElMessage.success('数据已刷新')
}

onMounted(() => {
  loadOverview()
  loadTrendData()
  loadRoomTypeData()
  loadFloorData()
  loadSourceData()
  loadAverageStay()
})
</script>

<style scoped lang="scss">
.checkin-statistics {
  .overview-cards {
    margin-bottom: 16px;

    .stat-card {
      display: flex;
      align-items: center;
      gap: 12px;
      padding: 8px 0;

      .stat-icon {
        width: 52px;
        height: 52px;
        border-radius: 10px;
        display: flex;
        align-items: center;
        justify-content: center;
        color: #fff;
        flex-shrink: 0;
      }

      &.total-rooms .stat-icon {
        background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      }

      &.in-house-rooms .stat-icon {
        background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
      }

      &.occupancy-rate .stat-icon {
        background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%);
      }

      &.today-checkin .stat-icon {
        background: linear-gradient(135deg, #fa709a 0%, #fee140 100%);
      }

      &.today-checkout .stat-icon {
        background: linear-gradient(135deg, #a8edea 0%, #fed6e3 100%);
      }

      &.overdue .stat-icon {
        background: linear-gradient(135deg, #ff9a9e 0%, #fecfef 100%);
      }

      .stat-info {
        .stat-value {
          font-size: 22px;
          font-weight: bold;
          color: #303133;
          line-height: 1.2;
        }

        .stat-label {
          font-size: 13px;
          color: #909399;
          margin-top: 4px;
        }
      }
    }
  }

  .charts-row {
    margin-bottom: 16px;
  }

  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }

  .trend-chart {
    .chart-legend {
      display: flex;
      gap: 20px;
      margin-bottom: 16px;
      justify-content: flex-end;

      .legend-item {
        display: flex;
        align-items: center;
        gap: 6px;
        font-size: 13px;
        color: #606266;

        .legend-dot {
          width: 12px;
          height: 12px;
          border-radius: 2px;
        }

        .checkin-dot {
          background: #409eff;
        }

        .checkout-dot {
          background: #67c23a;
        }
      }
    }

    .chart-bars {
      display: flex;
      align-items: flex-end;
      justify-content: space-around;
      height: 220px;
      padding: 0 10px;
      border-bottom: 1px solid #ebeef5;
      border-left: 1px solid #ebeef5;
      position: relative;

      .bar-group {
        display: flex;
        flex-direction: column;
        align-items: center;
        flex: 1;
        height: 100%;

        .bars-wrapper {
          flex: 1;
          display: flex;
          align-items: flex-end;
          justify-content: center;
          gap: 6px;
          width: 100%;

          .bar {
            width: 20px;
            border-radius: 4px 4px 0 0;
            transition: height 0.3s ease;
            min-height: 2px;

            &.checkin-bar {
              background: linear-gradient(180deg, #66b1ff 0%, #409eff 100%);
            }

            &.checkout-bar {
              background: linear-gradient(180deg, #85ce61 0%, #67c23a 100%);
            }
          }
        }

        .bar-label {
          margin-top: 8px;
          font-size: 12px;
          color: #909399;
        }
      }
    }
  }

  .source-chart {
    .source-list {
      max-height: 260px;
      overflow-y: auto;
    }

    .source-item {
      margin-bottom: 16px;

      &:last-child {
        margin-bottom: 0;
      }

      .source-header {
        display: flex;
        justify-content: space-between;
        margin-bottom: 6px;

        .source-name {
          font-size: 13px;
          color: #606266;
        }

        .source-count {
          font-size: 13px;
          font-weight: 500;
          color: #303133;
        }
      }

      .source-bar-wrapper {
        height: 14px;
        background: #f0f2f5;
        border-radius: 7px;
        overflow: hidden;

        .source-bar {
          height: 100%;
          border-radius: 7px;
          transition: width 0.5s ease;
        }
      }

      .source-percent {
        font-size: 12px;
        color: #909399;
        margin-top: 4px;
        text-align: right;
      }
    }

    .source-total {
      margin-top: 16px;
      padding-top: 12px;
      border-top: 1px solid #ebeef5;
      text-align: right;
      font-size: 14px;
      font-weight: 500;
      color: #303133;
    }
  }

  .rate-progress {
    display: flex;
    align-items: center;
    gap: 10px;

    :deep(.el-progress) {
      flex: 1;
    }

    .rate-text {
      font-size: 13px;
      font-weight: 500;
      color: #606266;
      min-width: 45px;
      text-align: right;
    }
  }

  .stay-duration-card {
    .duration-summary {
      text-align: center;
      padding: 20px 0;

      .duration-value {
        font-size: 48px;
        font-weight: bold;
        color: #409eff;
        line-height: 1;
        display: inline-block;
      }

      .duration-unit {
        display: inline-block;
        font-size: 16px;
        color: #909399;
        margin-left: 4px;
        margin-top: 20px;
      }

      .duration-label {
        font-size: 14px;
        color: #606266;
        margin-top: 8px;
      }
    }

    .section-title {
      font-size: 14px;
      font-weight: 500;
      color: #303133;
      margin-bottom: 16px;
      padding-bottom: 8px;
      border-bottom: 1px solid #ebeef5;
    }

    .duration-by-type {
      padding: 10px 0;

      .type-duration-list {
        .type-duration-item {
          display: flex;
          align-items: center;
          gap: 10px;
          margin-bottom: 12px;

          &:last-child {
            margin-bottom: 0;
          }

          .type-name {
            width: 80px;
            font-size: 13px;
            color: #606266;
            flex-shrink: 0;
          }

          .type-duration-bar {
            flex: 1;
            height: 12px;
            background: #f0f2f5;
            border-radius: 6px;
            overflow: hidden;

            .type-bar-inner {
              height: 100%;
              background: linear-gradient(90deg, #66b1ff 0%, #409eff 100%);
              border-radius: 6px;
              transition: width 0.5s ease;
            }
          }

          .type-days {
            width: 50px;
            text-align: right;
            font-size: 13px;
            font-weight: 500;
            color: #303133;
            flex-shrink: 0;
          }
        }
      }
    }

    .duration-distribution {
      padding: 10px 0;

      .distribution-list {
        .distribution-item {
          display: flex;
          align-items: center;
          gap: 10px;
          margin-bottom: 12px;

          &:last-child {
            margin-bottom: 0;
          }

          .dist-range {
            width: 70px;
            font-size: 13px;
            color: #606266;
            flex-shrink: 0;
          }

          .dist-bar-wrapper {
            flex: 1;
            height: 12px;
            background: #f0f2f5;
            border-radius: 6px;
            overflow: hidden;

            .dist-bar {
              height: 100%;
              background: linear-gradient(90deg, #85ce61 0%, #67c23a 100%);
              border-radius: 6px;
              transition: width 0.5s ease;
            }
          }

          .dist-count {
            width: 50px;
            text-align: right;
            font-size: 13px;
            color: #909399;
            flex-shrink: 0;
          }
        }
      }
    }
  }
}
</style>
