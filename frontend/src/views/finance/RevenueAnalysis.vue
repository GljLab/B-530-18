<template>
  <div class="revenue-analysis">
    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <span class="title">营收分析</span>
        </div>
      </template>

      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="日期范围">
          <el-date-picker
            v-model="searchForm.dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            value-format="YYYY-MM-DD"
            style="width: 280px"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadData">查询</el-button>
          <el-button type="success" @click="handleExport">导出</el-button>
        </el-form-item>
      </el-form>

      <el-row :gutter="16" class="summary-row">
        <el-col :span="6">
          <div class="stat-card blue">
            <div class="stat-icon">
              <el-icon :size="28"><Wallet /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">¥{{ formatMoney(indicators.totalRevenue) }}</div>
              <div class="stat-label">总营收</div>
            </div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="stat-card green">
            <div class="stat-icon">
              <el-icon :size="28"><TrendCharts /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">¥{{ formatMoney(indicators.revPAR) }}</div>
              <div class="stat-label">RevPAR</div>
            </div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="stat-card orange">
            <div class="stat-icon">
              <el-icon :size="28"><DataAnalysis /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">¥{{ formatMoney(indicators.adr) }}</div>
              <div class="stat-label">ADR</div>
            </div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="stat-card purple">
            <div class="stat-icon">
              <el-icon :size="28"><Odometer /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ indicators.occupancyRate !== null && indicators.occupancyRate !== undefined ? indicators.occupancyRate + '%' : '--' }}</div>
              <div class="stat-label">入住率</div>
            </div>
          </div>
        </el-col>
      </el-row>

      <el-row :gutter="16" style="margin-top: 20px">
        <el-col :span="8">
          <el-card shadow="never" class="chart-card">
            <template #header>
              <span class="chart-title">营收构成</span>
            </template>
            <div ref="compositionChartRef" class="chart-container"></div>
          </el-card>
        </el-col>
        <el-col :span="16">
          <el-card shadow="never" class="chart-card">
            <template #header>
              <span class="chart-title">近30日营收趋势</span>
            </template>
            <div ref="dailyChartRef" class="chart-container"></div>
          </el-card>
        </el-col>
      </el-row>

      <el-row :gutter="16" style="margin-top: 20px">
        <el-col :span="16">
          <el-card shadow="never" class="chart-card">
            <template #header>
              <span class="chart-title">月度营收趋势</span>
            </template>
            <div ref="monthlyChartRef" class="chart-container"></div>
          </el-card>
        </el-col>
        <el-col :span="8">
          <el-card shadow="never" class="chart-card">
            <template #header>
              <span class="chart-title">同比环比增长率</span>
            </template>
            <div class="yoy-mom-section">
              <div class="compare-item">
                <div class="compare-label">同比增长率</div>
                <div class="compare-value">{{ yoyMomGrowth.yoyRate ?? '--' }}</div>
              </div>
              <div class="compare-divider"></div>
              <div class="compare-item">
                <div class="compare-label">环比增长率</div>
                <div class="compare-value">{{ yoyMomGrowth.momRate ?? '--' }}</div>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>

      <el-card shadow="never" style="margin-top: 20px">
        <template #header>
          <span class="chart-title">房型营收分析</span>
        </template>
        <el-table :data="roomTypeAnalysis" v-loading="loading" border stripe style="width: 100%">
          <el-table-column prop="roomTypeName" label="房型名称" width="160" />
          <el-table-column label="营收金额" width="180" align="right">
            <template #default="{ row }">
              <span class="price">¥{{ formatMoney(row.revenue) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="占比" min-width="200">
            <template #default="{ row }">
              <div class="percent-cell">
                <el-progress
                  :percentage="row.percentage"
                  :stroke-width="14"
                  :color="getContributionColor(row.percentage)"
                />
                <span class="percent-text">{{ row.percentage.toFixed(1) }}%</span>
              </div>
            </template>
          </el-table-column>
          <el-table-column label="平均房价(ADR)" width="160" align="right">
            <template #default="{ row }">
              <span>¥{{ formatMoney(row.adr) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="贡献度排名" width="120" align="center">
            <template #default="{ row }">
              <el-tag :type="getRankTagType(row.rank)" size="small">第{{ row.rank }}名</el-tag>
            </template>
          </el-table-column>
        </el-table>
      </el-card>

      <el-card shadow="never" style="margin-top: 20px">
        <template #header>
          <span class="chart-title">关键指标</span>
        </template>
        <el-row :gutter="16">
          <el-col :span="6">
            <div class="indicator-card">
              <div class="indicator-value">¥{{ formatMoney(keyIndicators.revPAR) }}</div>
              <div class="indicator-label">RevPAR</div>
              <div class="indicator-desc">每间可售房收入 = 客房总收入 ÷ 可售客房数</div>
            </div>
          </el-col>
          <el-col :span="6">
            <div class="indicator-card">
              <div class="indicator-value">¥{{ formatMoney(keyIndicators.adr) }}</div>
              <div class="indicator-label">ADR</div>
              <div class="indicator-desc">平均房价 = 客房总收入 ÷ 实际售出客房数</div>
            </div>
          </el-col>
          <el-col :span="6">
            <div class="indicator-card">
              <div class="indicator-value">{{ keyIndicators.occupancyRate !== null && keyIndicators.occupancyRate !== undefined ? keyIndicators.occupancyRate + '%' : '--' }}</div>
              <div class="indicator-label">入住率</div>
              <div class="indicator-desc">入住率 = 实际售出客房数 ÷ 可售客房数 × 100%</div>
            </div>
          </el-col>
          <el-col :span="6">
            <div class="indicator-card">
              <div class="indicator-value">{{ keyIndicators.avgStayDays !== null && keyIndicators.avgStayDays !== undefined ? keyIndicators.avgStayDays : '--' }}</div>
              <div class="indicator-label">平均住宿天数</div>
              <div class="indicator-desc">平均住宿天数 = 总住宿天数 ÷ 入住间次数</div>
            </div>
          </el-col>
        </el-row>
      </el-card>

      <el-card shadow="never" style="margin-top: 20px">
        <template #header>
          <span class="chart-title">成本利润分析（字段预留）</span>
        </template>
        <el-row :gutter="16" class="cost-row">
          <el-col :span="4">
            <div class="cost-card">
              <div class="cost-value">--</div>
              <div class="cost-label">渠道佣金</div>
            </div>
          </el-col>
          <el-col :span="4">
            <div class="cost-card">
              <div class="cost-value">--</div>
              <div class="cost-label">人工成本</div>
            </div>
          </el-col>
          <el-col :span="4">
            <div class="cost-card">
              <div class="cost-value">--</div>
              <div class="cost-label">能耗成本</div>
            </div>
          </el-col>
          <el-col :span="4">
            <div class="cost-card">
              <div class="cost-value">--</div>
              <div class="cost-label">物料成本</div>
            </div>
          </el-col>
          <el-col :span="4">
            <div class="cost-card">
              <div class="cost-value">--</div>
              <div class="cost-label">维护成本</div>
            </div>
          </el-col>
          <el-col :span="4">
            <div class="cost-card">
              <div class="cost-value">--</div>
              <div class="cost-label">盈亏平衡点入住率</div>
            </div>
          </el-col>
        </el-row>
        <el-row :gutter="16" style="margin-top: 16px">
          <el-col :span="6">
            <div class="profit-card">
              <div class="profit-value">--</div>
              <div class="profit-label">毛利润</div>
            </div>
          </el-col>
          <el-col :span="6">
            <div class="profit-card">
              <div class="profit-value">--</div>
              <div class="profit-label">毛利率</div>
            </div>
          </el-col>
          <el-col :span="6">
            <div class="profit-card">
              <div class="profit-value">--</div>
              <div class="profit-label">净利润</div>
            </div>
          </el-col>
          <el-col :span="6">
            <div class="profit-card">
              <div class="profit-value">--</div>
              <div class="profit-label">净利率</div>
            </div>
          </el-col>
        </el-row>
      </el-card>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import { Wallet, TrendCharts, DataAnalysis, Odometer } from '@element-plus/icons-vue'
import * as echarts from 'echarts'
import api from '@/api'

const loading = ref(false)

const searchForm = reactive({
  dateRange: null
})

const indicators = ref({
  totalRevenue: 0,
  revPAR: 0,
  adr: 0,
  occupancyRate: null
})

const composition = ref([])
const dailyTrend = ref([])
const monthlyTrend = ref([])
const yoyMomGrowth = ref({
  yoyRate: null,
  momRate: null
})
const roomTypeAnalysis = ref([])
const keyIndicators = ref({
  revPAR: 0,
  adr: 0,
  occupancyRate: null,
  avgStayDays: null
})

const compositionChartRef = ref(null)
const dailyChartRef = ref(null)
const monthlyChartRef = ref(null)
let compositionChart = null
let dailyChart = null
let monthlyChart = null

const formatMoney = (val) => {
  if (val === null || val === undefined) return '0.00'
  return Number(val).toFixed(2)
}

const getContributionColor = (percentage) => {
  if (percentage >= 40) return '#409eff'
  if (percentage >= 25) return '#67c23a'
  if (percentage >= 10) return '#e6a23c'
  return '#909399'
}

const getRankTagType = (rank) => {
  if (rank === 1) return 'danger'
  if (rank === 2) return 'warning'
  if (rank === 3) return 'success'
  return 'info'
}

const initCompositionChart = () => {
  if (!compositionChartRef.value) return
  if (compositionChart) compositionChart.dispose()
  compositionChart = echarts.init(compositionChartRef.value)
  const option = {
    tooltip: {
      trigger: 'item',
      formatter: (params) => {
        return `${params.name}: ¥${formatMoney(params.value)} (${params.percent}%)`
      }
    },
    legend: {
      orient: 'horizontal',
      bottom: 0,
      data: composition.value.map(item => item.name)
    },
    series: [
      {
        name: '营收构成',
        type: 'pie',
        radius: ['45%', '70%'],
        center: ['50%', '45%'],
        avoidLabelOverlap: false,
        label: {
          show: true,
          formatter: '{b}\n{d}%'
        },
        emphasis: {
          label: {
            show: true,
            fontSize: 16,
            fontWeight: 'bold'
          }
        },
        data: composition.value,
        color: ['#409eff', '#67c23a', '#e6a23c']
      }
    ]
  }
  compositionChart.setOption(option)
}

const initDailyChart = () => {
  if (!dailyChartRef.value) return
  if (dailyChart) dailyChart.dispose()
  dailyChart = echarts.init(dailyChartRef.value)
  const dates = dailyTrend.value.map(item => item.date)
  const amounts = dailyTrend.value.map(item => item.amount)
  const option = {
    tooltip: {
      trigger: 'axis',
      formatter: (params) => {
        const p = params[0]
        return `${p.axisValue}<br/>营收金额: ¥${formatMoney(p.value)}`
      }
    },
    grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
    xAxis: {
      type: 'category',
      data: dates,
      axisLabel: { fontSize: 12 },
      boundaryGap: false
    },
    yAxis: {
      type: 'value',
      axisLabel: {
        formatter: (val) => '¥' + (val >= 10000 ? (val / 10000).toFixed(1) + '万' : val)
      }
    },
    series: [
      {
        name: '营收金额',
        type: 'line',
        data: amounts,
        smooth: true,
        lineStyle: { width: 3, color: '#409eff' },
        itemStyle: { color: '#409eff' },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(64,158,255,0.3)' },
            { offset: 1, color: 'rgba(64,158,255,0.05)' }
          ])
        }
      }
    ]
  }
  dailyChart.setOption(option)
}

const initMonthlyChart = () => {
  if (!monthlyChartRef.value) return
  if (monthlyChart) monthlyChart.dispose()
  monthlyChart = echarts.init(monthlyChartRef.value)
  const months = monthlyTrend.value.map(item => item.month)
  const amounts = monthlyTrend.value.map(item => item.amount)
  const option = {
    tooltip: {
      trigger: 'axis',
      formatter: (params) => {
        const p = params[0]
        return `${p.axisValue}<br/>营收金额: ¥${formatMoney(p.value)}`
      }
    },
    grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
    xAxis: {
      type: 'category',
      data: months,
      axisLabel: { fontSize: 12 }
    },
    yAxis: {
      type: 'value',
      axisLabel: {
        formatter: (val) => '¥' + (val >= 10000 ? (val / 10000).toFixed(1) + '万' : val)
      }
    },
    series: [
      {
        name: '营收金额',
        type: 'bar',
        data: amounts,
        barWidth: '50%',
        itemStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: '#409eff' },
            { offset: 1, color: '#66b1ff' }
          ]),
          borderRadius: [4, 4, 0, 0]
        }
      }
    ]
  }
  monthlyChart.setOption(option)
}

const loadData = async () => {
  if (!searchForm.dateRange || searchForm.dateRange.length < 2) {
    ElMessage.warning('请选择日期范围后再查询')
    return
  }
  loading.value = true
  try {
    const params = {
      startDate: searchForm.dateRange[0],
      endDate: searchForm.dateRange[1]
    }
    const res = await api.finance.advanced.revenueAnalysis(params)
    if (res.code === 200) {
      const data = res.data || {}
      indicators.value = {
        totalRevenue: data.totalRevenue || 0,
        revPAR: data.revPAR || 0,
        adr: data.adr || 0,
        occupancyRate: data.occupancyRate ?? null
      }
      composition.value = data.composition || [
        { name: '房费', value: 0 },
        { name: '加床费', value: 0 },
        { name: '其他服务', value: 0 }
      ]
      dailyTrend.value = data.dailyTrend || []
      monthlyTrend.value = data.monthlyTrend || []
      yoyMomGrowth.value = {
        yoyRate: data.yoyMomGrowth?.yoyRate ?? null,
        momRate: data.yoyMomGrowth?.momRate ?? null
      }
      roomTypeAnalysis.value = data.roomTypeAnalysis || []
      keyIndicators.value = {
        revPAR: data.keyIndicators?.revPAR || 0,
        adr: data.keyIndicators?.adr || 0,
        occupancyRate: data.keyIndicators?.occupancyRate ?? null,
        avgStayDays: data.keyIndicators?.avgStayDays ?? null
      }
      await nextTick()
      initCompositionChart()
      initDailyChart()
      initMonthlyChart()
    }
  } catch {
    ElMessage.error('加载数据失败')
  } finally {
    loading.value = false
  }
}

const handleExport = async () => {
  if (!searchForm.dateRange || searchForm.dateRange.length < 2) {
    ElMessage.warning('请选择日期范围后再导出')
    return
  }
  try {
    const params = {
      startDate: searchForm.dateRange[0],
      endDate: searchForm.dateRange[1]
    }
    const res = await api.finance.advanced.revenueAnalysis(params)
    if (res.code === 200) {
      const data = res.data || {}
      const details = data.roomTypeAnalysis || []
      let csv = '\uFEFF房型名称,营收金额,占比(%),平均房价(ADR),贡献度排名\n'
      details.forEach(item => {
        csv += `${item.roomTypeName},${item.revenue},${item.percentage?.toFixed(1) || 0},${item.adr},${item.rank}\n`
      })
      csv += `\n总营收,${data.totalRevenue || 0}\n`
      csv += `RevPAR,${data.revPAR || 0}\n`
      csv += `ADR,${data.adr || 0}\n`
      csv += `入住率,${data.occupancyRate ?? '--'}\n`
      const blob = new Blob([csv], { type: 'text/csv;charset=utf-8;' })
      const link = document.createElement('a')
      link.href = URL.createObjectURL(blob)
      link.download = `营收分析_${new Date().toISOString().slice(0, 10)}.csv`
      link.click()
      URL.revokeObjectURL(link.href)
      ElMessage.success('导出成功')
    }
  } catch {
    ElMessage.error('导出失败')
  }
}

onMounted(() => {
})
</script>

<style scoped>
.revenue-analysis {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.title {
  font-size: 16px;
  font-weight: 600;
}

.search-form {
  margin-bottom: 16px;
}

.summary-row {
  margin-top: 8px;
}

.stat-card {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 20px;
  border-radius: 8px;
  background: #f5f7fa;
}

.stat-card .stat-icon {
  width: 56px;
  height: 56px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  flex-shrink: 0;
}

.stat-card.blue .stat-icon {
  background: linear-gradient(135deg, #409eff 0%, #66b1ff 100%);
}

.stat-card.green .stat-icon {
  background: linear-gradient(135deg, #67c23a 0%, #85ce61 100%);
}

.stat-card.orange .stat-icon {
  background: linear-gradient(135deg, #e6a23c 0%, #ebb563 100%);
}

.stat-card.purple .stat-icon {
  background: linear-gradient(135deg, #9b59b6 0%, #b07cd8 100%);
}

.stat-info .stat-value {
  font-size: 22px;
  font-weight: 700;
  color: #303133;
  line-height: 1.3;
}

.stat-info .stat-label {
  font-size: 13px;
  color: #909399;
  margin-top: 4px;
}

.chart-card {
  min-height: 360px;
}

.chart-title {
  font-size: 14px;
  font-weight: 600;
}

.chart-container {
  width: 100%;
  height: 300px;
}

.yoy-mom-section {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 40px;
  padding: 30px 0;
}

.compare-item {
  text-align: center;
}

.compare-label {
  font-size: 13px;
  color: #909399;
  margin-bottom: 8px;
}

.compare-value {
  font-size: 24px;
  font-weight: 700;
  color: #303133;
}

.compare-divider {
  width: 1px;
  height: 40px;
  background: #dcdfe6;
}

.price {
  color: #409eff;
  font-weight: 600;
}

.percent-cell {
  display: flex;
  align-items: center;
  gap: 10px;
}

.percent-cell :deep(.el-progress) {
  flex: 1;
}

.percent-text {
  font-size: 13px;
  font-weight: 500;
  color: #606266;
  min-width: 50px;
  text-align: right;
}

.indicator-card {
  padding: 20px;
  border-radius: 8px;
  background: #f5f7fa;
  text-align: center;
}

.indicator-value {
  font-size: 22px;
  font-weight: 700;
  color: #303133;
  line-height: 1.3;
}

.indicator-label {
  font-size: 14px;
  font-weight: 600;
  color: #606266;
  margin-top: 6px;
}

.indicator-desc {
  font-size: 12px;
  color: #909399;
  margin-top: 8px;
  line-height: 1.5;
}

.cost-row {
  margin-top: 8px;
}

.cost-card {
  padding: 20px 12px;
  border-radius: 8px;
  background: #f5f7fa;
  text-align: center;
}

.cost-value {
  font-size: 20px;
  font-weight: 700;
  color: #c0c4cc;
}

.cost-label {
  font-size: 13px;
  color: #909399;
  margin-top: 6px;
}

.profit-card {
  padding: 20px;
  border-radius: 8px;
  background: #f5f7fa;
  text-align: center;
}

.profit-value {
  font-size: 20px;
  font-weight: 700;
  color: #c0c4cc;
}

.profit-label {
  font-size: 13px;
  color: #909399;
  margin-top: 6px;
}
</style>
