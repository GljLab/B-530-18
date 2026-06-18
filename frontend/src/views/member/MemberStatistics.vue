<template>
  <div class="member-statistics">
    <el-card shadow="never" class="overview-cards">
      <el-row :gutter="16">
        <el-col :span="6">
          <div class="stat-card total">
            <div class="stat-icon">
              <el-icon :size="28"><User /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ statistics.totalMembers || 0 }}</div>
              <div class="stat-label">会员总数</div>
            </div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="stat-card new">
            <div class="stat-icon">
              <el-icon :size="28"><UserFilled /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ statistics.newMembersThisMonth || 0 }}</div>
              <div class="stat-label">本月新增会员</div>
            </div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="stat-card active">
            <div class="stat-icon">
              <el-icon :size="28"><TrendCharts /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ statistics.activeMembersThisMonth || 0 }}</div>
              <div class="stat-label">本月活跃会员</div>
            </div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="stat-card level">
            <div class="stat-icon">
              <el-icon :size="28"><Medal /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ levelCount || 0 }}</div>
              <div class="stat-label">会员等级数</div>
            </div>
          </div>
        </el-col>
      </el-row>
    </el-card>

    <el-row :gutter="16" class="charts-row">
      <el-col :span="10">
        <el-card shadow="never">
          <template #header>
            <span class="chart-title">等级分布</span>
          </template>
          <div ref="levelChartRef" class="pie-chart"></div>
        </el-card>
      </el-col>
      <el-col :span="14">
        <el-card shadow="never">
          <template #header>
            <span class="chart-title">近12个月会员增长趋势</span>
          </template>
          <div ref="growthChartRef" class="line-chart"></div>
        </el-card>
      </el-col>
    </el-row>

    <el-card shadow="never" class="level-detail-card">
      <template #header>
        <span class="chart-title">各等级会员详情</span>
      </template>
      <el-table :data="levelDistribution" stripe border style="width: 100%">
        <el-table-column label="等级" width="160">
          <template #default="{ row }">
            <span class="level-badge" :style="{ background: row.levelColor + '20', color: row.levelColor }">
              {{ row.levelIcon }} {{ row.levelName }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="count" label="会员数量" width="120" align="center" />
        <el-table-column label="占比" min-width="200">
          <template #default="{ row }">
            <div class="progress-item">
              <el-progress
                :percentage="Number(row.percentage).toFixed(1)"
                :stroke-width="12"
                :color="row.levelColor"
              />
              <span class="percentage-text">{{ Number(row.percentage).toFixed(1) }}%</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="房费折扣" width="120" align="center">
          <template #default="{ row }">
            <span style="color: #e6a23c; font-weight: 600">{{ row.roomDiscount || '-' }}%</span>
          </template>
        </el-table-column>
        <el-table-column label="积分倍率" width="120" align="center">
          <template #default="{ row }">
            <span style="color: #67c23a; font-weight: 600">{{ row.pointRate || '-' }}倍</span>
          </template>
        </el-table-column>
        <el-table-column label="升级条件" width="200" align="center">
          <template #default="{ row }">
            {{ row.upgradeType === 1 ? '消费满' : '积分达' }}
            <span style="color: #409eff; font-weight: 600">{{ row.upgradeCondition || 0 }}</span>
            {{ row.upgradeType === 1 ? '元' : '分' }}
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick, onUnmounted } from 'vue'
import * as echarts from 'echarts'
import { User, UserFilled, TrendCharts, Medal } from '@element-plus/icons-vue'
import api from '@/api'

const statistics = ref({})
const levelDistribution = ref([])
const growthTrend = ref([])
const levelCount = ref(0)

const levelChartRef = ref(null)
const growthChartRef = ref(null)

let levelChart = null
let growthChart = null

const loadStatistics = async () => {
  try {
    const res = await api.member.statistics()
    if (res.code === 200) {
      statistics.value = res.data || {}
      levelDistribution.value = res.data?.levelDistribution || []
      growthTrend.value = res.data?.growthTrend || []
      levelCount.value = levelDistribution.value.length

      await nextTick()
      renderLevelChart()
      renderGrowthChart()
    }
  } catch (e) {
    console.error(e)
  }
}

const renderLevelChart = () => {
  if (!levelChartRef.value) return
  if (!levelChart) {
    levelChart = echarts.init(levelChartRef.value)
  }

  const data = levelDistribution.value.map(item => ({
    name: item.levelName,
    value: item.count || 0,
    itemStyle: {
      color: item.levelColor
    }
  }))

  levelChart.setOption({
    tooltip: {
      trigger: 'item',
      formatter: '{b}: {c} 人 ({d}%)'
    },
    legend: {
      bottom: 10,
      left: 'center',
      textStyle: {
        color: '#606266',
        fontSize: 12
      }
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

const renderGrowthChart = () => {
  if (!growthChartRef.value) return
  if (!growthChart) {
    growthChart = echarts.init(growthChartRef.value)
  }

  const months = growthTrend.value.map(item => item.month)
  const counts = growthTrend.value.map(item => item.count)

  growthChart.setOption({
    tooltip: {
      trigger: 'axis',
      formatter: '{b}<br/>新增会员: {c} 人',
      axisPointer: {
        type: 'shadow'
      }
    },
    grid: {
      left: 60,
      right: 30,
      top: 30,
      bottom: 40
    },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: months,
      axisLabel: {
        color: '#909399',
        fontSize: 11,
        rotate: 30
      },
      axisLine: {
        lineStyle: {
          color: '#e4e7ed'
        }
      }
    },
    yAxis: {
      type: 'value',
      name: '人数',
      nameTextStyle: {
        color: '#909399',
        fontSize: 12
      },
      axisLabel: {
        color: '#909399',
        fontSize: 11
      },
      splitLine: {
        lineStyle: {
          type: 'dashed',
          color: '#f0f0f0'
        }
      }
    },
    series: [{
      type: 'line',
      data: counts,
      smooth: true,
      symbol: 'circle',
      symbolSize: 8,
      lineStyle: {
        width: 3,
        color: new echarts.graphic.LinearGradient(0, 0, 1, 0, [
          { offset: 0, color: '#667eea' },
          { offset: 1, color: '#764ba2' }
        ])
      },
      itemStyle: {
        color: '#667eea',
        borderColor: '#fff',
        borderWidth: 2
      },
      areaStyle: {
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: 'rgba(102, 126, 234, 0.3)' },
          { offset: 1, color: 'rgba(118, 75, 162, 0.05)' }
        ])
      },
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
  levelChart?.resize()
  growthChart?.resize()
}

onMounted(() => {
  loadStatistics()
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  levelChart?.dispose()
  growthChart?.dispose()
})
</script>

<style scoped>
.member-statistics {
  padding: 16px;
}

.overview-cards {
  margin-bottom: 16px;
}

.stat-card {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 20px;
  border-radius: 10px;
  color: #fff;
}

.stat-card.total {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.stat-card.new {
  background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%);
}

.stat-card.active {
  background: linear-gradient(135deg, #fa709a 0%, #fee140 100%);
}

.stat-card.level {
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

.charts-row {
  margin-bottom: 16px;
}

.chart-title {
  font-weight: 600;
  color: #303133;
  font-size: 14px;
}

.pie-chart {
  height: 320px;
  width: 100%;
}

.line-chart {
  height: 320px;
  width: 100%;
}

.level-detail-card {
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
