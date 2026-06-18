<template>
  <div class="payment-method-analysis">
    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <span class="title">支付方式分析</span>
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
        </el-form-item>
      </el-form>

      <el-row :gutter="16" class="summary-row">
        <el-col :span="6">
          <div class="stat-card blue">
            <div class="stat-icon">
              <el-icon :size="28"><Wallet /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ statsData.totalCount || 0 }}</div>
              <div class="stat-label">总收款笔数</div>
            </div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="stat-card green">
            <div class="stat-icon">
              <el-icon :size="28"><Money /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">¥{{ formatMoney(statsData.totalAmount) }}</div>
              <div class="stat-label">总收款金额</div>
            </div>
          </div>
        </el-col>
        <el-col :span="4">
          <div class="mini-stat-card">
            <div class="mini-label">现金笔数</div>
            <div class="mini-value">{{ methodCountMap['现金'] || 0 }}</div>
          </div>
        </el-col>
        <el-col :span="4">
          <div class="mini-stat-card">
            <div class="mini-label">刷卡笔数</div>
            <div class="mini-value">{{ methodCountMap['刷卡'] || 0 }}</div>
          </div>
        </el-col>
        <el-col :span="4">
          <div class="mini-stat-card">
            <div class="mini-label">移动支付笔数</div>
            <div class="mini-value">{{ methodCountMap['移动支付'] || 0 }}</div>
          </div>
        </el-col>
      </el-row>

      <el-row :gutter="16" style="margin-top: 20px">
        <el-col :span="12">
          <el-card shadow="never" class="chart-card">
            <template #header>
              <span class="chart-title">各支付方式金额占比</span>
            </template>
            <div ref="pieChartRef" class="chart-container"></div>
          </el-card>
        </el-col>
        <el-col :span="12">
          <el-card shadow="never" class="chart-card">
            <template #header>
              <span class="chart-title">各支付方式金额对比</span>
            </template>
            <div ref="barChartRef" class="chart-container"></div>
          </el-card>
        </el-col>
      </el-row>

      <el-card shadow="never" style="margin-top: 20px">
        <template #header>
          <span class="chart-title">支付方式明细</span>
        </template>
        <el-table :data="methodDetails" v-loading="loading" border stripe style="width: 100%">
          <el-table-column prop="method" label="支付方式" width="160" />
          <el-table-column prop="count" label="使用频率" width="120" align="center" />
          <el-table-column label="金额占比" width="200">
            <template #default="{ row }">
              <div class="percent-cell">
                <el-progress
                  :percentage="row.percentage || 0"
                  :stroke-width="14"
                  :color="getProgressColor(row.percentage || 0)"
                />
                <span class="percent-text">{{ (row.percentage || 0).toFixed(1) }}%</span>
              </div>
            </template>
          </el-table-column>
          <el-table-column label="平均单笔金额" align="right">
            <template #default="{ row }">
              <span class="price">¥{{ formatMoney(row.avgAmount) }}</span>
            </template>
          </el-table-column>
        </el-table>
      </el-card>

      <el-row :gutter="16" style="margin-top: 20px">
        <el-col :span="24">
          <el-card shadow="never" class="chart-card">
            <template #header>
              <span class="chart-title">现金使用率趋势（近30日）</span>
            </template>
            <div ref="cashTrendChartRef" class="chart-container"></div>
          </el-card>
        </el-col>
      </el-row>

      <el-card shadow="never" style="margin-top: 20px">
        <template #header>
          <span class="chart-title">客户类型/房型分析</span>
        </template>
        <el-table :data="customerTypeAnalysis" border stripe style="width: 100%">
          <el-table-column prop="type" label="类型" width="160" />
          <el-table-column prop="category" label="分类" width="160" />
          <el-table-column prop="count" label="使用次数" width="120" align="center" />
          <el-table-column prop="amount" label="金额" align="right">
            <template #default="{ row }">
              <span class="price">¥{{ formatMoney(row.amount) }}</span>
            </template>
          </el-table-column>
        </el-table>
      </el-card>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import { Wallet, Money } from '@element-plus/icons-vue'
import * as echarts from 'echarts'
import api from '@/api'

const loading = ref(false)

const searchForm = reactive({
  dateRange: null
})

const statsData = ref({
  totalCount: 0,
  totalAmount: 0
})

const methodDetails = ref([])
const pieData = ref([])
const cashTrend = ref([])
const customerTypeAnalysis = ref([
  { type: '--', category: '--', count: '--', amount: '--' },
  { type: '--', category: '--', count: '--', amount: '--' },
  { type: '--', category: '--', count: '--', amount: '--' }
])

const pieChartRef = ref(null)
const barChartRef = ref(null)
const cashTrendChartRef = ref(null)
let pieChart = null
let barChart = null
let cashTrendChart = null

const methodCountMap = computed(() => {
  const map = {}
  methodDetails.value.forEach(item => {
    map[item.method] = item.count
  })
  return map
})

const formatMoney = (val) => {
  if (val === null || val === undefined || val === '--') return '0.00'
  return Number(val).toFixed(2)
}

const getProgressColor = (percentage) => {
  if (percentage >= 40) return '#409eff'
  if (percentage >= 25) return '#67c23a'
  if (percentage >= 10) return '#e6a23c'
  return '#909399'
}

const initPieChart = () => {
  if (!pieChartRef.value) return
  if (pieChart) pieChart.dispose()
  pieChart = echarts.init(pieChartRef.value)
  const option = {
    tooltip: {
      trigger: 'item',
      formatter: '{b}: ¥{c} ({d}%)'
    },
    legend: {
      orient: 'vertical',
      right: '5%',
      top: 'center'
    },
    series: [
      {
        name: '支付方式',
        type: 'pie',
        radius: ['40%', '70%'],
        center: ['40%', '50%'],
        avoidLabelOverlap: false,
        itemStyle: {
          borderRadius: 6,
          borderColor: '#fff',
          borderWidth: 2
        },
        label: {
          show: true,
          formatter: '{b}\n{d}%'
        },
        data: pieData.value.length > 0
          ? pieData.value
          : [
              { name: '现金', value: 0 },
              { name: '刷卡', value: 0 },
              { name: '移动支付', value: 0 }
            ],
        color: ['#67c23a', '#409eff', '#e6a23c']
      }
    ]
  }
  pieChart.setOption(option)
}

const initBarChart = () => {
  if (!barChartRef.value) return
  if (barChart) barChart.dispose()
  barChart = echarts.init(barChartRef.value)
  const names = methodDetails.value.map(item => item.method)
  const amounts = methodDetails.value.map(item => item.amount)
  const option = {
    tooltip: {
      trigger: 'axis',
      formatter: (params) => {
        const p = params[0]
        return `${p.name}<br/>金额: ¥${formatMoney(p.value)}`
      }
    },
    grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
    xAxis: {
      type: 'category',
      data: names.length > 0 ? names : ['现金', '刷卡', '移动支付']
    },
    yAxis: {
      type: 'value',
      axisLabel: {
        formatter: (val) => '¥' + (val >= 10000 ? (val / 10000).toFixed(1) + '万' : val)
      }
    },
    series: [
      {
        name: '金额',
        type: 'bar',
        data: amounts.length > 0 ? amounts : [0, 0, 0],
        barWidth: '40%',
        itemStyle: {
          borderRadius: [4, 4, 0, 0],
          color: (params) => {
            const colors = ['#67c23a', '#409eff', '#e6a23c']
            return colors[params.dataIndex % colors.length]
          }
        }
      }
    ]
  }
  barChart.setOption(option)
}

const initCashTrendChart = () => {
  if (!cashTrendChartRef.value) return
  if (cashTrendChart) cashTrendChart.dispose()
  cashTrendChart = echarts.init(cashTrendChartRef.value)
  const dates = cashTrend.value.map(item => item.date)
  const rates = cashTrend.value.map(item => item.cashRate)
  const option = {
    tooltip: {
      trigger: 'axis',
      formatter: (params) => {
        const p = params[0]
        return `${p.axisValue}<br/>现金使用率: ${p.value}%`
      }
    },
    grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
    xAxis: {
      type: 'category',
      data: dates,
      boundaryGap: false
    },
    yAxis: {
      type: 'value',
      axisLabel: {
        formatter: '{value}%'
      },
      max: 100
    },
    series: [
      {
        name: '现金使用率',
        type: 'line',
        data: rates,
        smooth: true,
        lineStyle: { width: 3, color: '#67c23a' },
        itemStyle: { color: '#67c23a' },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(103,194,58,0.3)' },
            { offset: 1, color: 'rgba(103,194,58,0.05)' }
          ])
        }
      }
    ]
  }
  cashTrendChart.setOption(option)
}

const loadData = async () => {
  if (!searchForm.dateRange || searchForm.dateRange.length < 2) {
    ElMessage.warning('请选择日期范围后再查询')
    return
  }
  loading.value = true
  try {
    const params = {}
    if (searchForm.dateRange && searchForm.dateRange.length === 2) {
      params.startDate = searchForm.dateRange[0]
      params.endDate = searchForm.dateRange[1]
    }
    const res = await api.finance.advanced.paymentMethodAnalysis(params)
    if (res.code === 200) {
      const data = res.data || {}
      statsData.value = {
        totalCount: data.totalCount || 0,
        totalAmount: data.totalAmount || 0
      }
      methodDetails.value = data.methodDetails || []
      pieData.value = data.pieData || []
      cashTrend.value = data.cashTrend || []
      if (data.customerTypeAnalysis && data.customerTypeAnalysis.length > 0) {
        customerTypeAnalysis.value = data.customerTypeAnalysis
      }
      await nextTick()
      initPieChart()
      initBarChart()
      initCashTrendChart()
    }
  } catch {
    ElMessage.error('加载数据失败')
  } finally {
    loading.value = false
  }
}

onMounted(() => {
})
</script>

<style scoped>
.payment-method-analysis {
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

.mini-stat-card {
  padding: 16px;
  border-radius: 8px;
  background: #f5f7fa;
  text-align: center;
}

.mini-label {
  font-size: 12px;
  color: #909399;
  margin-bottom: 6px;
}

.mini-value {
  font-size: 20px;
  font-weight: 700;
  color: #303133;
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
</style>
