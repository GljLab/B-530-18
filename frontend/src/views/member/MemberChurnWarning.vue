<template>
  <div class="member-churn-warning">
    <el-card shadow="never" class="overview-cards">
      <template #header>
        <span class="card-title">流失风险概览</span>
      </template>
      <el-row :gutter="16">
        <el-col :span="6">
          <div class="stat-card active">
            <div class="stat-icon">
              <el-icon :size="28"><UserFilled /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ riskSummary.activeCount || 0 }}</div>
              <div class="stat-label">活跃会员</div>
              <div class="stat-rate">
                {{ formatPercent(riskSummary.totalMembers > 0 ? (riskSummary.activeCount / riskSummary.totalMembers * 100) : 0) }}%
              </div>
            </div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="stat-card low">
            <div class="stat-icon">
              <el-icon :size="28"><Warning /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ riskSummary.lowRiskCount || 0 }}</div>
              <div class="stat-label">低风险流失</div>
              <div class="stat-rate">{{ formatPercent(riskSummary.lowRiskRate) }}%</div>
            </div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="stat-card medium">
            <div class="stat-icon">
              <el-icon :size="28"><BellFilled /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ riskSummary.mediumRiskCount || 0 }}</div>
              <div class="stat-label">中风险流失</div>
              <div class="stat-rate">{{ formatPercent(riskSummary.mediumRiskRate) }}%</div>
            </div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="stat-card high">
            <div class="stat-icon">
              <el-icon :size="28"><CircleCloseFilled /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ riskSummary.highRiskCount || 0 }}</div>
              <div class="stat-label">高风险流失</div>
              <div class="stat-rate">{{ formatPercent(riskSummary.highRiskRate) }}%</div>
            </div>
          </div>
        </el-col>
      </el-row>
    </el-card>

    <el-row :gutter="16" class="charts-row">
      <el-col :span="14">
        <el-card shadow="never">
          <template #header>
            <span class="chart-title">近12个月流失趋势</span>
          </template>
          <div ref="churnTrendChartRef" class="line-chart"></div>
        </el-card>
      </el-col>
      <el-col :span="10">
        <el-card shadow="never">
          <template #header>
            <span class="chart-title">各等级流失分布</span>
          </template>
          <div ref="churnByLevelChartRef" class="bar-chart"></div>
        </el-card>
      </el-col>
    </el-row>

    <el-card shadow="never" class="list-card">
      <template #header>
        <div class="card-header">
          <span class="chart-title">流失预警列表</span>
          <div class="batch-actions">
            <el-button size="small" type="primary" plain :disabled="selectedMembers.length === 0">
              发送挽留短信
            </el-button>
            <el-button size="small" type="success" plain :disabled="selectedMembers.length === 0">
              发放优惠券
            </el-button>
            <el-button size="small" type="warning" plain :disabled="selectedMembers.length === 0">
              赠送积分
            </el-button>
          </div>
        </div>
      </template>

      <div class="filter-section">
        <el-form :inline="true" size="small">
          <el-form-item label="风险等级">
            <el-select v-model="filterForm.riskLevel" placeholder="全部" clearable style="width: 120px">
              <el-option label="活跃" value="active" />
              <el-option label="低风险" value="low" />
              <el-option label="中风险" value="medium" />
              <el-option label="高风险" value="high" />
            </el-select>
          </el-form-item>
          <el-form-item label="会员等级">
            <el-select v-model="filterForm.levelIds" multiple placeholder="全部" clearable style="width: 200px">
              <el-option
                v-for="level in levelOptions"
                :key="level.id"
                :label="level.levelName"
                :value="level.id"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="上次入住">
            <el-date-picker
              v-model="filterForm.lastStayRange"
              type="daterange"
              range-separator="至"
              start-placeholder="开始日期"
              end-placeholder="结束日期"
              value-format="YYYY-MM-DD"
              style="width: 240px"
            />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" size="small" @click="loadChurnList">查询</el-button>
            <el-button size="small" @click="resetFilter">重置</el-button>
          </el-form-item>
        </el-form>
      </div>

      <el-table
        :data="churnList"
        stripe
        border
        style="width: 100%"
        v-loading="loading"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="50" />
        <el-table-column prop="memberNo" label="会员卡号" width="160" />
        <el-table-column prop="customerName" label="姓名" width="100" />
        <el-table-column prop="phone" label="手机号" width="130" />
        <el-table-column prop="levelName" label="会员等级" width="120">
          <template #default="{ row }">
            <el-tag size="small">{{ row.levelName }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="lastStayTime" label="上次入住时间" width="170">
          <template #default="{ row }">
            {{ row.lastStayTime ? formatDate(row.lastStayTime) : '从未入住' }}
          </template>
        </el-table-column>
        <el-table-column label="流失风险" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="getRiskTagType(row.riskLevel)" size="small">
              {{ getRiskLabel(row.riskLevel) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="累计消费" width="120" align="right">
          <template #default="{ row }">
            <span style="color: #e6a23c; font-weight: 600">¥{{ formatNumber(row.totalSpent) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="stayCount" label="入住次数" width="90" align="center" />
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small">详情</el-button>
            <el-button type="success" link size="small">挽留</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination">
        <el-pagination
          v-model:current-page="pagination.pageNum"
          v-model:page-size="pagination.pageSize"
          :total="pagination.total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handlePageChange"
        />
      </div>
    </el-card>

    <el-card shadow="never" class="level-table-card">
      <template #header>
        <span class="chart-title">按等级流失分析</span>
      </template>
      <el-table :data="churnByLevelList" stripe border style="width: 100%">
        <el-table-column label="等级" width="160">
          <template #default="{ row }">
            <span class="level-badge" :style="{ background: row.levelColor + '20', color: row.levelColor }">
              {{ row.levelIcon }} {{ row.levelName }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="totalMembers" label="会员总数" width="120" align="center" />
        <el-table-column prop="churnCount" label="流失会员数" width="120" align="center">
          <template #default="{ row }">
            <span style="color: #f56c6c; font-weight: 600">{{ row.churnCount }}</span>
          </template>
        </el-table-column>
        <el-table-column label="流失率" min-width="200">
          <template #default="{ row }">
            <div class="progress-item">
              <el-progress
                :percentage="Number(row.churnRate).toFixed(1)"
                :stroke-width="12"
                color="#f56c6c"
              />
              <span class="percentage-text">{{ formatPercent(row.churnRate) }}%</span>
            </div>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick, onUnmounted, reactive } from 'vue'
import * as echarts from 'echarts'
import { UserFilled, Warning, BellFilled, CircleCloseFilled } from '@element-plus/icons-vue'
import api from '@/api'

const riskSummary = ref({})
const churnList = ref([])
const churnByLevelList = ref([])
const levelOptions = ref([])
const selectedMembers = ref([])
const loading = ref(false)

const filterForm = reactive({
  riskLevel: '',
  levelIds: [],
  lastStayRange: []
})

const pagination = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0
})

const churnTrendChartRef = ref(null)
const churnByLevelChartRef = ref(null)

let churnTrendChart = null
let churnByLevelChart = null

const formatNumber = (num) => {
  if (!num) return '0'
  return Number(num).toLocaleString('zh-CN')
}

const formatPercent = (num) => {
  if (!num) return '0'
  return Number(num).toFixed(2)
}

const formatDate = (date) => {
  if (!date) return ''
  const d = new Date(date)
  const year = d.getFullYear()
  const month = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
}

const getRiskTagType = (level) => {
  switch (level) {
    case 'active': return 'success'
    case 'low': return 'info'
    case 'medium': return 'warning'
    case 'high': return 'danger'
    default: return 'info'
  }
}

const getRiskLabel = (level) => {
  switch (level) {
    case 'active': return '活跃'
    case 'low': return '低风险'
    case 'medium': return '中风险'
    case 'high': return '高风险'
    default: return '未知'
  }
}

const handleSelectionChange = (val) => {
  selectedMembers.value = val
}

const handleSizeChange = (size) => {
  pagination.pageSize = size
  pagination.pageNum = 1
  loadChurnList()
}

const handlePageChange = (page) => {
  pagination.pageNum = page
  loadChurnList()
}

const resetFilter = () => {
  filterForm.riskLevel = ''
  filterForm.levelIds = []
  filterForm.lastStayRange = []
  pagination.pageNum = 1
  loadChurnList()
}

const loadSummaryData = async () => {
  try {
    const [summaryRes, trendRes, levelRes] = await Promise.all([
      api.memberAnalytics.getChurnSummary(),
      api.memberAnalytics.getChurnTrend(),
      api.memberAnalytics.getChurnByLevel()
    ])

    if (summaryRes.code === 200) riskSummary.value = summaryRes.data || {}
    if (trendRes.code === 200) {
      churnTrendData = trendRes.data || []
      renderChurnTrendChart()
    }
    if (levelRes.code === 200) {
      churnByLevelList.value = levelRes.data || []
      levelOptions.value = levelRes.data || []
      renderChurnByLevelChart()
    }
  } catch (e) {
    console.error(e)
  }
}

let churnTrendData = []

const loadChurnList = async () => {
  loading.value = true
  try {
    const params = {
      pageNum: pagination.pageNum,
      pageSize: pagination.pageSize
    }
    if (filterForm.riskLevel) params.riskLevel = filterForm.riskLevel
    if (filterForm.levelIds?.length) params.levelIds = filterForm.levelIds
    if (filterForm.lastStayRange?.length === 2) {
      params.lastStayStart = filterForm.lastStayRange[0]
      params.lastStayEnd = filterForm.lastStayRange[1]
    }

    const res = await api.memberAnalytics.getChurnList(params)
    if (res.code === 200) {
      churnList.value = res.data?.list || []
      pagination.total = res.data?.total || 0
    }
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

const renderChurnTrendChart = () => {
  if (!churnTrendChartRef.value) return
  if (!churnTrendChart) {
    churnTrendChart = echarts.init(churnTrendChartRef.value)
  }

  const trend = churnTrendData || []
  const months = trend.map(item => item.month)
  const counts = trend.map(item => item.churnCount || 0)
  const rates = trend.map(item => item.churnRate || 0)

  churnTrendChart.setOption({
    tooltip: {
      trigger: 'axis'
    },
    legend: {
      data: ['流失人数', '流失率(%)'],
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
      data: months,
      boundaryGap: false,
      axisLabel: { color: '#909399', fontSize: 11, rotate: 30 }
    },
    yAxis: [
      {
        type: 'value',
        name: '人数',
        nameTextStyle: { color: '#909399', fontSize: 12 },
        axisLabel: { color: '#909399', fontSize: 11 },
        splitLine: { lineStyle: { type: 'dashed', color: '#f0f0f0' } }
      },
      {
        type: 'value',
        name: '流失率(%)',
        nameTextStyle: { color: '#909399', fontSize: 12 },
        axisLabel: { color: '#909399', fontSize: 11, formatter: '{value}%' },
        splitLine: { show: false }
      }
    ],
    series: [
      {
        name: '流失人数',
        type: 'bar',
        data: counts,
        itemStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: '#f093fb' },
            { offset: 1, color: '#f5576c' }
          ]),
          borderRadius: [4, 4, 0, 0]
        },
        barWidth: 20
      },
      {
        name: '流失率(%)',
        type: 'line',
        yAxisIndex: 1,
        data: rates,
        smooth: true,
        symbol: 'circle',
        symbolSize: 8,
        lineStyle: { width: 3, color: '#667eea' },
        itemStyle: { color: '#667eea', borderColor: '#fff', borderWidth: 2 }
      }
    ]
  })
}

const renderChurnByLevelChart = () => {
  if (!churnByLevelChartRef.value) return
  if (!churnByLevelChart) {
    churnByLevelChart = echarts.init(churnByLevelChartRef.value)
  }

  const data = churnByLevelList.value || []
  const names = data.map(item => item.levelName)
  const rates = data.map(item => item.churnRate || 0)
  const colors = data.map(item => item.levelColor)

  churnByLevelChart.setOption({
    tooltip: {
      trigger: 'axis',
      axisPointer: { type: 'shadow' },
      formatter: '{b}<br/>流失率: {c}%'
    },
    grid: {
      left: 80,
      right: 30,
      top: 20,
      bottom: 30
    },
    xAxis: {
      type: 'value',
      name: '流失率(%)',
      nameTextStyle: { color: '#909399', fontSize: 12 },
      axisLabel: { color: '#909399', fontSize: 11, formatter: '{value}%' },
      splitLine: { lineStyle: { type: 'dashed', color: '#f0f0f0' } }
    },
    yAxis: {
      type: 'category',
      data: names,
      axisLabel: { color: '#606266', fontSize: 12 }
    },
    series: [{
      type: 'bar',
      data: rates.map((value, index) => ({
        value,
        itemStyle: {
          color: colors[index] || '#f56c6c',
          borderRadius: [0, 4, 4, 0]
        }
      })),
      barWidth: 24,
      label: {
        show: true,
        position: 'right',
        color: '#606266',
        fontSize: 11,
        formatter: '{c}%'
      }
    }]
  })
}

const handleResize = () => {
  churnTrendChart?.resize()
  churnByLevelChart?.resize()
}

onMounted(async () => {
  await loadSummaryData()
  await nextTick()
  loadChurnList()
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  churnTrendChart?.dispose()
  churnByLevelChart?.dispose()
})
</script>

<style scoped>
.member-churn-warning {
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

.stat-card.active {
  background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%);
}

.stat-card.low {
  background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
}

.stat-card.medium {
  background: linear-gradient(135deg, #fa709a 0%, #fee140 100%);
}

.stat-card.high {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
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

.stat-rate {
  font-size: 12px;
  opacity: 0.85;
  margin-top: 4px;
}

.charts-row {
  margin-bottom: 16px;
}

.line-chart,
.bar-chart {
  height: 320px;
  width: 100%;
}

.list-card {
  margin-bottom: 16px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.batch-actions {
  display: flex;
  gap: 8px;
}

.filter-section {
  margin-bottom: 16px;
  padding: 16px;
  background: #f5f7fa;
  border-radius: 8px;
}

.pagination {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}

.level-table-card {
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
