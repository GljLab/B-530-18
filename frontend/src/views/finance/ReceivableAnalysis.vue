<template>
  <div class="receivable-analysis">
    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <span class="title">应收账款分析</span>
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
              <div class="stat-value">¥{{ formatMoney(summaryData.totalReceivable) }}</div>
              <div class="stat-label">应收账款总额</div>
            </div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="stat-card red">
            <div class="stat-icon">
              <el-icon :size="28"><WarningFilled /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">¥{{ formatMoney(summaryData.overdueAmount) }}</div>
              <div class="stat-label">逾期金额</div>
            </div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="stat-card orange">
            <div class="stat-icon">
              <el-icon :size="28"><Document /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ summaryData.overdueCount || 0 }}</div>
              <div class="stat-label">逾期笔数</div>
            </div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="stat-card green">
            <div class="stat-icon">
              <el-icon :size="28"><TrendCharts /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ (summaryData.turnoverRate || 0).toFixed(2) }}%</div>
              <div class="stat-label">应收账款周转率</div>
            </div>
          </div>
        </el-col>
      </el-row>

      <div class="section-title">账龄分析</div>
      <div ref="agingChartRef" class="chart-container"></div>

      <div class="section-title">协议单位分析</div>
      <el-table :data="unitAnalysis" v-loading="loading" border stripe style="width: 100%">
        <el-table-column prop="unitName" label="单位名称" min-width="160" />
        <el-table-column label="应收金额" width="160" align="right">
          <template #default="{ row }">
            <span class="price">¥{{ formatMoney(row.receivableAmount) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="aging" label="账龄" width="120" align="center" />
        <el-table-column label="信用额度" width="160" align="right">
          <template #default="{ row }">
            ¥{{ formatMoney(row.creditLimit) }}
          </template>
        </el-table-column>
        <el-table-column label="使用率" width="200">
          <template #default="{ row }">
            <div class="percent-cell">
              <el-progress
                :percentage="row.usageRate"
                :stroke-width="14"
                :color="getUsageColor(row.usageRate)"
              />
              <span class="percent-text">{{ row.usageRate.toFixed(1) }}%</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="风险等级" width="120" align="center">
          <template #default="{ row }">
            <el-tag :type="riskTagType(row.riskLevel)" size="small">{{ row.riskLevel }}</el-tag>
          </template>
        </el-table-column>
      </el-table>

      <div class="section-title">周转率指标</div>
      <el-row :gutter="16" class="turnover-row">
        <el-col :span="6">
          <div class="indicator-card">
            <div class="indicator-value blue-text">{{ (turnoverIndicators.turnoverRate || 0).toFixed(2) }}%</div>
            <div class="indicator-label">应收账款周转率</div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="indicator-card">
            <div class="indicator-value green-text">{{ turnoverIndicators.avgCollectionDays || 0 }}天</div>
            <div class="indicator-label">平均回款天数</div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="indicator-card">
            <div class="indicator-value">¥{{ formatMoney(turnoverIndicators.beginningReceivable) }}</div>
            <div class="indicator-label">期初应收</div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="indicator-card">
            <div class="indicator-value">¥{{ formatMoney(turnoverIndicators.endingReceivable) }}</div>
            <div class="indicator-label">期末应收</div>
          </div>
        </el-col>
      </el-row>

      <div class="section-title">逾期应收</div>
      <el-row :gutter="16" class="overdue-row">
        <el-col :span="8">
          <div class="overdue-card">
            <div class="overdue-value red-text">¥{{ formatMoney(overdueStats.overdueAmount) }}</div>
            <div class="overdue-label">逾期金额</div>
          </div>
        </el-col>
        <el-col :span="8">
          <div class="overdue-card">
            <div class="overdue-value orange-text">{{ overdueStats.overdueCount || 0 }}笔</div>
            <div class="overdue-label">逾期笔数</div>
          </div>
        </el-col>
        <el-col :span="8">
          <div class="overdue-card" :class="{ 'warning-bg': overdueStats.overdueRate > 10 }">
            <div class="overdue-value" :class="overdueStats.overdueRate > 10 ? 'red-text' : ''">{{ (overdueStats.overdueRate || 0).toFixed(2) }}%</div>
            <div class="overdue-label">逾期率{{ overdueStats.overdueRate > 10 ? '（预警）' : '' }}</div>
          </div>
        </el-col>
      </el-row>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onBeforeUnmount, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import { Wallet, WarningFilled, Document, TrendCharts } from '@element-plus/icons-vue'
import * as echarts from 'echarts'
import api from '@/api'

const loading = ref(false)
const agingChartRef = ref(null)
let agingChart = null

const summaryData = ref({
  totalReceivable: 0,
  overdueAmount: 0,
  overdueCount: 0,
  turnoverRate: 0
})

const agingAnalysis = ref([])
const unitAnalysis = ref([])
const turnoverIndicators = ref({
  turnoverRate: 0,
  avgCollectionDays: 0,
  beginningReceivable: 0,
  endingReceivable: 0
})
const overdueStats = ref({
  overdueAmount: 0,
  overdueCount: 0,
  overdueRate: 0
})

const searchForm = reactive({
  dateRange: null
})

const formatMoney = (val) => {
  if (val === null || val === undefined) return '0.00'
  return Number(val).toFixed(2)
}

const getUsageColor = (rate) => {
  if (rate >= 80) return '#f56c6c'
  if (rate >= 60) return '#e6a23c'
  return '#67c23a'
}

const riskTagType = (level) => {
  const map = { '低': 'success', '中': 'warning', '高': 'danger', '极高': 'danger' }
  return map[level] || 'info'
}

const initAgingChart = () => {
  if (!agingChartRef.value) return
  if (agingChart) {
    agingChart.dispose()
  }
  agingChart = echarts.init(agingChartRef.value)
  const buckets = agingAnalysis.value.map(item => item.bucket)
  const amounts = agingAnalysis.value.map(item => item.amount)
  agingChart.setOption({
    tooltip: {
      trigger: 'axis',
      formatter: (params) => {
        const p = params[0]
        return `${p.name}<br/>应收金额：¥${formatMoney(p.value)}`
      }
    },
    grid: { left: 60, right: 30, top: 30, bottom: 40 },
    xAxis: {
      type: 'category',
      data: buckets,
      axisLabel: { color: '#606266' }
    },
    yAxis: {
      type: 'value',
      axisLabel: {
        color: '#606266',
        formatter: (val) => '¥' + (val >= 10000 ? (val / 10000).toFixed(1) + '万' : val)
      }
    },
    series: [{
      type: 'bar',
      data: amounts,
      barWidth: '40%',
      itemStyle: {
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: '#409eff' },
          { offset: 1, color: '#66b1ff' }
        ]),
        borderRadius: [4, 4, 0, 0]
      }
    }]
  })
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
    const res = await api.finance.advanced.receivableAnalysis(params)
    if (res.code === 200) {
      const data = res.data || {}
      summaryData.value = {
        totalReceivable: data.totalReceivable || 0,
        overdueAmount: data.overdueAmount || 0,
        overdueCount: data.overdueCount || 0,
        turnoverRate: data.turnoverRate || 0
      }
      agingAnalysis.value = data.agingAnalysis || []
      unitAnalysis.value = (data.unitAnalysis || []).sort((a, b) => {
        const order = { '极高': 0, '高': 1, '中': 2, '低': 3 }
        return (order[a.riskLevel] ?? 4) - (order[b.riskLevel] ?? 4)
      })
      turnoverIndicators.value = data.turnoverIndicators || {
        turnoverRate: 0,
        avgCollectionDays: 0,
        beginningReceivable: 0,
        endingReceivable: 0
      }
      overdueStats.value = data.overdueStats || {
        overdueAmount: 0,
        overdueCount: 0,
        overdueRate: 0
      }
      await nextTick()
      initAgingChart()
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
    const res = await api.finance.advanced.receivableAnalysis(params)
    if (res.code === 200) {
      const data = res.data || {}
      const units = (data.unitAnalysis || []).sort((a, b) => {
        const order = { '极高': 0, '高': 1, '中': 2, '低': 3 }
        return (order[a.riskLevel] ?? 4) - (order[b.riskLevel] ?? 4)
      })
      let csv = '\uFEFF单位名称,应收金额,账龄,信用额度,使用率,风险等级\n'
      units.forEach(item => {
        csv += `${item.unitName},${item.receivableAmount},${item.aging},${item.creditLimit},${item.usageRate}%,${item.riskLevel}\n`
      })
      const blob = new Blob([csv], { type: 'text/csv;charset=utf-8;' })
      const link = document.createElement('a')
      link.href = URL.createObjectURL(blob)
      link.download = `应收账款分析_${new Date().toISOString().slice(0, 10)}.csv`
      link.click()
      URL.revokeObjectURL(link.href)
      ElMessage.success('导出成功')
    }
  } catch {
    ElMessage.error('导出失败')
  }
}

const handleResize = () => {
  if (agingChart) {
    agingChart.resize()
  }
}

onMounted(() => {
  window.addEventListener('resize', handleResize)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize)
  if (agingChart) {
    agingChart.dispose()
    agingChart = null
  }
})
</script>

<style scoped>
.receivable-analysis {
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

.stat-card.red .stat-icon {
  background: linear-gradient(135deg, #f56c6c 0%, #f78989 100%);
}

.stat-card.orange .stat-icon {
  background: linear-gradient(135deg, #e6a23c 0%, #ebb563 100%);
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

.stat-card.red .stat-value {
  color: #f56c6c;
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

.section-title {
  font-size: 15px;
  font-weight: 600;
  color: #303133;
  margin-top: 24px;
  margin-bottom: 16px;
  padding-left: 10px;
  border-left: 3px solid #409eff;
}

.chart-container {
  width: 100%;
  height: 350px;
}

.turnover-row {
  margin-top: 0;
}

.indicator-card {
  background: #f5f7fa;
  border-radius: 8px;
  padding: 20px;
  text-align: center;
}

.indicator-card .indicator-value {
  font-size: 22px;
  font-weight: 700;
  color: #303133;
  line-height: 1.3;
}

.indicator-card .indicator-label {
  font-size: 13px;
  color: #909399;
  margin-top: 6px;
}

.blue-text {
  color: #409eff;
}

.green-text {
  color: #67c23a;
}

.overdue-row {
  margin-top: 0;
}

.overdue-card {
  background: #f5f7fa;
  border-radius: 8px;
  padding: 20px;
  text-align: center;
}

.overdue-card.warning-bg {
  background: #fef0f0;
  border: 1px solid #fbc4c4;
}

.overdue-card .overdue-value {
  font-size: 22px;
  font-weight: 700;
  color: #303133;
  line-height: 1.3;
}

.overdue-card .overdue-label {
  font-size: 13px;
  color: #909399;
  margin-top: 6px;
}

.red-text {
  color: #f56c6c;
}

.orange-text {
  color: #e6a23c;
}
</style>
