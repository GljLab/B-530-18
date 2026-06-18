<template>
  <div class="cash-flow-analysis">
    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <span class="title">现金流分析</span>
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
        <el-col :span="8">
          <div class="stat-card green">
            <div class="stat-icon">
              <el-icon :size="28"><Top /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">¥{{ formatMoney(flowData.totalInflow) }}</div>
              <div class="stat-label">现金流入</div>
            </div>
          </div>
        </el-col>
        <el-col :span="8">
          <div class="stat-card red">
            <div class="stat-icon">
              <el-icon :size="28"><Bottom /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">¥{{ formatMoney(flowData.totalOutflow) }}</div>
              <div class="stat-label">现金流出</div>
            </div>
          </div>
        </el-col>
        <el-col :span="8">
          <div class="stat-card" :class="flowData.netCashFlow >= 0 ? 'blue' : 'red'">
            <div class="stat-icon">
              <el-icon :size="28"><Wallet /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">¥{{ formatMoney(flowData.netCashFlow) }}</div>
              <div class="stat-label">净现金流</div>
            </div>
          </div>
        </el-col>
      </el-row>

      <el-row :gutter="16" class="detail-row">
        <el-col :span="12">
          <div class="detail-section">
            <div class="detail-title">流入明细</div>
            <div class="detail-items">
              <div class="detail-item">
                <span class="detail-label">房费收入</span>
                <span class="detail-value inflow">¥{{ formatMoney(flowData.inflowDetail.roomFee) }}</span>
              </div>
              <div class="detail-item">
                <span class="detail-label">押金收入</span>
                <span class="detail-value inflow">¥{{ formatMoney(flowData.inflowDetail.deposit) }}</span>
              </div>
              <div class="detail-item">
                <span class="detail-label">其他收款</span>
                <span class="detail-value inflow">¥{{ formatMoney(flowData.inflowDetail.other) }}</span>
              </div>
              <div class="detail-item">
                <span class="detail-label">挂账回款</span>
                <span class="detail-value inflow">¥{{ formatMoney(flowData.inflowDetail.creditRecovery) }}</span>
              </div>
            </div>
          </div>
        </el-col>
        <el-col :span="12">
          <div class="detail-section">
            <div class="detail-title">流出明细</div>
            <div class="detail-items">
              <div class="detail-item">
                <span class="detail-label">退款</span>
                <span class="detail-value outflow">¥{{ formatMoney(flowData.outflowDetail.refund) }}</span>
              </div>
              <div class="detail-item">
                <span class="detail-label">赔偿</span>
                <span class="detail-value outflow">¥{{ formatMoney(flowData.outflowDetail.compensation) }}</span>
              </div>
              <div class="detail-item">
                <span class="detail-label">其他支出(预留)</span>
                <span class="detail-value outflow">¥{{ formatMoney(flowData.outflowDetail.other) }}</span>
              </div>
            </div>
          </div>
        </el-col>
      </el-row>
    </el-card>

    <el-card shadow="never" style="margin-top: 16px">
      <template #header>
        <div class="card-header">
          <span class="title">近30日现金流趋势</span>
        </div>
      </template>
      <div ref="trendChartRef" class="trend-chart"></div>
    </el-card>

    <el-card shadow="never" style="margin-top: 16px">
      <template #header>
        <div class="card-header">
          <span class="title">现金流健康度</span>
        </div>
      </template>
      <div class="health-content">
        <div class="health-number" :class="healthColorClass">
          {{ (flowData.healthRatio * 100).toFixed(1) }}%
        </div>
        <div class="health-bar-wrapper">
          <div class="health-bar">
            <div class="health-bar-fill" :style="healthBarStyle"></div>
          </div>
          <div class="health-bar-labels">
            <span class="label-red">危险 (&lt;0)</span>
            <span class="label-yellow">一般 (0-30%)</span>
            <span class="label-green">健康 (&gt;30%)</span>
          </div>
        </div>
        <div class="health-desc" :class="healthColorClass">
          {{ flowData.healthStatus }}
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, onBeforeUnmount, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import { Wallet, Top, Bottom } from '@element-plus/icons-vue'
import * as echarts from 'echarts'
import api from '@/api'

const loading = ref(false)
const trendChartRef = ref(null)
let chartInstance = null

const flowData = ref({
  totalInflow: 0,
  totalOutflow: 0,
  netCashFlow: 0,
  inflowDetail: { roomFee: 0, deposit: 0, other: 0, creditRecovery: 0 },
  outflowDetail: { refund: 0, compensation: 0, other: 0 },
  trend: [],
  healthRatio: 0,
  healthStatus: ''
})

const searchForm = reactive({
  dateRange: null
})

const formatMoney = (val) => {
  if (val === null || val === undefined) return '0.00'
  return Number(val).toFixed(2)
}

const healthColorClass = computed(() => {
  const ratio = flowData.value.healthRatio
  if (ratio > 0.3) return 'health-green'
  if (ratio >= 0) return 'health-yellow'
  return 'health-red'
})

const healthBarStyle = computed(() => {
  const ratio = flowData.value.healthRatio
  const percent = Math.min(Math.max(ratio * 100, -100), 100)
  const width = Math.abs(percent)
  const color = ratio > 0.3 ? '#67c23a' : ratio >= 0 ? '#e6a23c' : '#f56c6c'
  return {
    width: `${width}%`,
    background: color
  }
})

const initChart = () => {
  if (!trendChartRef.value) return
  chartInstance = echarts.init(trendChartRef.value)
}

const updateChart = () => {
  if (!chartInstance) return
  const trend = flowData.value.trend || []
  const dates = trend.map(item => item.date)
  const inflowData = trend.map(item => item.inflow)
  const outflowData = trend.map(item => item.outflow)
  const netFlowData = trend.map(item => item.netFlow)

  chartInstance.setOption({
    tooltip: {
      trigger: 'axis',
      axisPointer: { type: 'cross' },
      formatter: (params) => {
        let html = `<div style="font-weight:600;margin-bottom:4px">${params[0].axisValue}</div>`
        params.forEach(p => {
          html += `<div style="display:flex;align-items:center;gap:6px;margin:2px 0">
            <span style="display:inline-block;width:10px;height:10px;border-radius:50%;background:${p.color}"></span>
            <span>${p.seriesName}：¥${formatMoney(p.value)}</span>
          </div>`
        })
        return html
      }
    },
    legend: {
      data: ['流入', '流出', '净流入'],
      bottom: 0
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '12%',
      top: '8%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: dates,
      axisLabel: {
        formatter: (val) => val.slice(5)
      }
    },
    yAxis: {
      type: 'value',
      axisLabel: {
        formatter: (val) => `¥${(val / 1000).toFixed(0)}k`
      }
    },
    series: [
      {
        name: '流入',
        type: 'line',
        smooth: true,
        symbol: 'none',
        data: inflowData,
        lineStyle: { color: '#67c23a', width: 2 },
        itemStyle: { color: '#67c23a' },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(103,194,58,0.3)' },
            { offset: 1, color: 'rgba(103,194,58,0.02)' }
          ])
        }
      },
      {
        name: '流出',
        type: 'line',
        smooth: true,
        symbol: 'none',
        data: outflowData,
        lineStyle: { color: '#f56c6c', width: 2 },
        itemStyle: { color: '#f56c6c' },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(245,108,108,0.3)' },
            { offset: 1, color: 'rgba(245,108,108,0.02)' }
          ])
        }
      },
      {
        name: '净流入',
        type: 'line',
        smooth: true,
        symbol: 'none',
        data: netFlowData,
        lineStyle: { color: '#409eff', width: 2 },
        itemStyle: { color: '#409eff' },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(64,158,255,0.3)' },
            { offset: 1, color: 'rgba(64,158,255,0.02)' }
          ])
        }
      }
    ]
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
    const res = await api.finance.advanced.cashFlowAnalysis(params)
    if (res.code === 200) {
      const data = res.data || {}
      flowData.value = {
        totalInflow: data.totalInflow || 0,
        totalOutflow: data.totalOutflow || 0,
        netCashFlow: data.netCashFlow || 0,
        inflowDetail: data.inflowDetail || { roomFee: 0, deposit: 0, other: 0, creditRecovery: 0 },
        outflowDetail: data.outflowDetail || { refund: 0, compensation: 0, other: 0 },
        trend: data.trend || [],
        healthRatio: data.healthRatio || 0,
        healthStatus: data.healthStatus || ''
      }
      await nextTick()
      updateChart()
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
    const res = await api.finance.advanced.cashFlowAnalysis(params)
    if (res.code === 200) {
      const data = res.data || {}
      let csv = '\uFEFF类别,项目,金额\n'
      csv += `流入,房费收入,${data.inflowDetail?.roomFee || 0}\n`
      csv += `流入,押金收入,${data.inflowDetail?.deposit || 0}\n`
      csv += `流入,其他收款,${data.inflowDetail?.other || 0}\n`
      csv += `流入,挂账回款,${data.inflowDetail?.creditRecovery || 0}\n`
      csv += `流入合计,,${data.totalInflow || 0}\n`
      csv += `流出,退款,${data.outflowDetail?.refund || 0}\n`
      csv += `流出,赔偿,${data.outflowDetail?.compensation || 0}\n`
      csv += `流出,其他支出,${data.outflowDetail?.other || 0}\n`
      csv += `流出合计,,${data.totalOutflow || 0}\n`
      csv += `净现金流,,${data.netCashFlow || 0}\n`
      csv += `健康度,,${((data.healthRatio || 0) * 100).toFixed(1)}%\n`
      const blob = new Blob([csv], { type: 'text/csv;charset=utf-8;' })
      const link = document.createElement('a')
      link.href = URL.createObjectURL(blob)
      link.download = `现金流分析_${new Date().toISOString().slice(0, 10)}.csv`
      link.click()
      URL.revokeObjectURL(link.href)
      ElMessage.success('导出成功')
    }
  } catch {
    ElMessage.error('导出失败')
  }
}

const handleResize = () => {
  chartInstance?.resize()
}

onMounted(() => {
  initChart()
  window.addEventListener('resize', handleResize)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize)
  chartInstance?.dispose()
  chartInstance = null
})
</script>

<style scoped>
.cash-flow-analysis {
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

.stat-card.red .stat-value {
  color: #f56c6c;
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

.detail-row {
  margin-top: 16px;
}

.detail-section {
  background: #f5f7fa;
  border-radius: 8px;
  padding: 16px 20px;
}

.detail-title {
  font-size: 14px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 12px;
  padding-bottom: 8px;
  border-bottom: 1px solid #e4e7ed;
}

.detail-items {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.detail-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.detail-label {
  font-size: 13px;
  color: #606266;
}

.detail-value {
  font-size: 14px;
  font-weight: 600;
}

.detail-value.inflow {
  color: #67c23a;
}

.detail-value.outflow {
  color: #f56c6c;
}

.trend-chart {
  width: 100%;
  height: 400px;
}

.health-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 20px;
  padding: 24px 0;
}

.health-number {
  font-size: 48px;
  font-weight: 700;
  line-height: 1.2;
}

.health-green {
  color: #67c23a;
}

.health-yellow {
  color: #e6a23c;
}

.health-red {
  color: #f56c6c;
}

.health-bar-wrapper {
  width: 100%;
  max-width: 500px;
}

.health-bar {
  height: 20px;
  background: #ebeef5;
  border-radius: 10px;
  overflow: hidden;
}

.health-bar-fill {
  height: 100%;
  border-radius: 10px;
  transition: width 0.6s ease, background 0.3s ease;
}

.health-bar-labels {
  display: flex;
  justify-content: space-between;
  margin-top: 8px;
  font-size: 12px;
}

.label-red {
  color: #f56c6c;
}

.label-yellow {
  color: #e6a23c;
}

.label-green {
  color: #67c23a;
}

.health-desc {
  font-size: 16px;
  font-weight: 500;
}
</style>
