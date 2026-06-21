<template>
  <div class="benefit-usage-statistics">
    <el-card shadow="never" class="overview-cards">
      <template #header>
        <span class="card-title">权益使用概览</span>
      </template>
      <el-row :gutter="16">
        <el-col :span="6">
          <div class="stat-card discount">
            <div class="stat-icon">
              <el-icon :size="28"><Discount /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ usageRates.discount?.useCount || 0 }}</div>
              <div class="stat-label">折扣使用次数</div>
              <div class="stat-rate">使用率: {{ formatPercent(usageRates.discount?.usageRate) }}%</div>
            </div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="stat-card upgrade">
            <div class="stat-icon">
              <el-icon :size="28"><Top /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ usageRates.upgrade?.useCount || 0 }}</div>
              <div class="stat-label">免费升级次数</div>
              <div class="stat-rate">使用率: {{ formatPercent(usageRates.upgrade?.usageRate) }}%</div>
            </div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="stat-card late">
            <div class="stat-icon">
              <el-icon :size="28"><Clock /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ usageRates.lateCheckout?.useCount || 0 }}</div>
              <div class="stat-label">延迟退房次数</div>
              <div class="stat-rate">使用率: {{ formatPercent(usageRates.lateCheckout?.usageRate) }}%</div>
            </div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="stat-card deposit">
            <div class="stat-icon">
              <el-icon :size="28"><Wallet /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ usageRates.deposit?.useCount || 0 }}</div>
              <div class="stat-label">押金减免次数</div>
              <div class="stat-rate">使用率: {{ formatPercent(usageRates.deposit?.usageRate) }}%</div>
            </div>
          </div>
        </el-col>
      </el-row>
    </el-card>

    <el-card shadow="never" class="cost-cards">
      <template #header>
        <span class="card-title">权益成本分析</span>
      </template>
      <el-row :gutter="16">
        <el-col :span="6">
          <div class="cost-item">
            <div class="cost-label">折扣成本</div>
            <div class="cost-value discount">¥{{ formatNumber(costAnalysis.discountCost) }}</div>
            <div class="cost-sub">占会员营收: {{ formatPercent(costAnalysis.discountCostRatio) }}%</div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="cost-item">
            <div class="cost-label">升级成本</div>
            <div class="cost-value upgrade">
              <span v-if="costAnalysis.upgradeCostReserved" style="color: #909399">预留字段</span>
              <span v-else>¥{{ formatNumber(costAnalysis.upgradeCost) }}</span>
            </div>
            <div class="cost-sub">估算房型差价</div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="cost-item">
            <div class="cost-label">总权益成本</div>
            <div class="cost-value total">¥{{ formatNumber(costAnalysis.totalBenefitCost) }}</div>
            <div class="cost-sub">成本率: {{ formatPercent(costAnalysis.benefitCostRate) }}%</div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="cost-item">
            <div class="cost-label">ROI 投资回报</div>
            <div class="cost-value roi" :class="{ positive: costAnalysis.roi > 1 }">
              {{ costAnalysis.roi || 0 }}
            </div>
            <div class="cost-sub">额外营收带来的回报倍数</div>
          </div>
        </el-col>
      </el-row>
    </el-card>

    <el-row :gutter="16" class="charts-row">
      <el-col :span="12">
        <el-card shadow="never">
          <template #header>
            <span class="chart-title">权益使用率对比</span>
          </template>
          <div ref="usageRateChartRef" class="bar-chart"></div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card shadow="never">
          <template #header>
            <span class="chart-title">成本效益对比</span>
          </template>
          <div ref="costBenefitChartRef" class="bar-chart"></div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="16" class="detail-row">
      <el-col :span="12">
        <el-card shadow="never" class="detail-card">
          <template #header>
            <span class="chart-title">折扣优惠明细</span>
          </template>
          <el-table :data="discountDetail" stripe border style="width: 100%">
            <el-table-column label="指标" width="150" />
            <el-table-column label="数值" align="right">
              <template #default="{ row }">
                <span :style="{ color: row.highlight ? '#f56c6c' : '#303133', fontWeight: row.highlight ? 600 : 400 }">
                  {{ row.value }}
                </span>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card shadow="never" class="detail-card">
          <template #header>
            <span class="chart-title">押金减免明细</span>
          </template>
          <el-table :data="depositDetail" stripe border style="width: 100%">
            <el-table-column label="指标" width="150" />
            <el-table-column label="数值" align="right">
              <template #default="{ row }">
                <span :style="{ color: row.highlight ? '#f56c6c' : '#303133', fontWeight: row.highlight ? 600 : 400 }">
                  {{ row.value }}
                </span>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
    </el-row>

    <el-card shadow="never" class="summary-card">
      <template #header>
        <span class="chart-title">会员体系成本效益总览</span>
      </template>
      <el-descriptions :column="3" border size="default">
        <el-descriptions-item label="会员总营收">
          <span style="color: #67c23a; font-weight: 600; font-size: 16px">¥{{ formatNumber(costAnalysis.memberTotalRevenue) }}</span>
        </el-descriptions-item>
        <el-descriptions-item label="权益总成本">
          <span style="color: #f56c6c; font-weight: 600; font-size: 16px">¥{{ formatNumber(costAnalysis.totalBenefitCost) }}</span>
        </el-descriptions-item>
        <el-descriptions-item label="成本率">
          <el-tag :type="costAnalysis.benefitCostRate > 10 ? 'danger' : 'success'" size="large">
            {{ formatPercent(costAnalysis.benefitCostRate) }}%
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="额外带来营收">
          <span style="color: #409eff; font-weight: 600">¥{{ formatNumber(costAnalysis.extraRevenueTotal) }}</span>
        </el-descriptions-item>
        <el-descriptions-item label="ROI">
          <span :style="{ color: costAnalysis.roi > 1 ? '#67c23a' : '#f56c6c', fontWeight: 600, fontSize: '16px' }">
            {{ costAnalysis.roi || 0 }} 倍
          </span>
        </el-descriptions-item>
        <el-descriptions-item label="会员订单数">
          {{ usageRates.memberOrderCount || 0 }}
        </el-descriptions-item>
      </el-descriptions>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick, onUnmounted, computed } from 'vue'
import * as echarts from 'echarts'
import { Discount, Top, Clock, Wallet } from '@element-plus/icons-vue'
import api from '@/api'

const usageRates = ref({})
const costAnalysis = ref({})

const usageRateChartRef = ref(null)
const costBenefitChartRef = ref(null)

let usageRateChart = null
let costBenefitChart = null

const discountDetail = computed(() => {
  const d = usageRates.value.discount || {}
  return [
    { label: '使用次数', value: d.useCount || 0 },
    { label: '折扣总额', value: '¥' + formatNumber(d.totalAmount), highlight: true },
    { label: '使用率', value: formatPercent(d.usageRate) + '%' },
    { label: '会员订单数', value: usageRates.value.memberOrderCount || 0 }
  ]
})

const depositDetail = computed(() => {
  const d = usageRates.value.deposit || {}
  return [
    { label: '减免次数', value: d.useCount || 0 },
    { label: '减免总额', value: '¥' + formatNumber(d.totalReductionAmount), highlight: true },
    { label: '平均减免金额', value: '¥' + formatNumber(d.avgReductionAmount) },
    { label: '风险金额', value: '¥' + formatNumber(d.riskAmount) },
    { label: '实际损失', value: '¥' + formatNumber(d.actualLoss) }
  ]
})

const formatNumber = (num) => {
  if (!num) return '0'
  return Number(num).toLocaleString('zh-CN')
}

const formatPercent = (num) => {
  if (!num) return '0'
  return Number(num).toFixed(2)
}

const loadData = async () => {
  try {
    const [usageRes, costRes] = await Promise.all([
      api.memberAnalytics.getBenefitUsageRates(),
      api.memberAnalytics.getBenefitCost()
    ])

    if (usageRes.code === 200) usageRates.value = usageRes.data || {}
    if (costRes.code === 200) costAnalysis.value = costRes.data || {}

    await nextTick()
    renderUsageRateChart()
    renderCostBenefitChart()
  } catch (e) {
    console.error(e)
  }
}

const renderUsageRateChart = () => {
  if (!usageRateChartRef.value) return
  if (!usageRateChart) {
    usageRateChart = echarts.init(usageRateChartRef.value)
  }

  const rates = usageRates.value
  const names = ['折扣优惠', '免费升级', '延迟退房', '押金减免']
  const values = [
    rates.discount?.usageRate || 0,
    rates.upgrade?.usageRate || 0,
    rates.lateCheckout?.usageRate || 0,
    rates.deposit?.usageRate || 0
  ]
  const colors = ['#667eea', '#67c23a', '#e6a23c', '#f56c6c']

  usageRateChart.setOption({
    tooltip: {
      trigger: 'axis',
      axisPointer: { type: 'shadow' },
      formatter: '{b}<br/>使用率: {c}%'
    },
    grid: {
      left: 60,
      right: 30,
      top: 30,
      bottom: 30
    },
    xAxis: {
      type: 'category',
      data: names,
      axisLabel: { color: '#606266', fontSize: 12 }
    },
    yAxis: {
      type: 'value',
      name: '使用率(%)',
      nameTextStyle: { color: '#909399', fontSize: 12 },
      axisLabel: { color: '#909399', fontSize: 11, formatter: '{value}%' },
      splitLine: { lineStyle: { type: 'dashed', color: '#f0f0f0' } }
    },
    series: [{
      type: 'bar',
      data: values.map((value, index) => ({
        value,
        itemStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: colors[index] },
            { offset: 1, color: colors[index] + '80' }
          ]),
          borderRadius: [6, 6, 0, 0]
        }
      })),
      barWidth: 50,
      label: {
        show: true,
        position: 'top',
        color: '#606266',
        fontSize: 12,
        fontWeight: 600,
        formatter: '{c}%'
      }
    }]
  })
}

const renderCostBenefitChart = () => {
  if (!costBenefitChartRef.value) return
  if (!costBenefitChart) {
    costBenefitChart = echarts.init(costBenefitChartRef.value)
  }

  const cost = costAnalysis.value
  const items = ['折扣成本', '升级成本', '总权益成本', '会员总营收', '额外营收']
  const values = [
    Number(cost.discountCost) || 0,
    Number(cost.upgradeCost) || 0,
    Number(cost.totalBenefitCost) || 0,
    Number(cost.memberTotalRevenue) || 0,
    Number(cost.extraRevenueTotal) || 0
  ]
  const colors = ['#f56c6c', '#e6a23c', '#909399', '#67c23a', '#409eff']

  costBenefitChart.setOption({
    tooltip: {
      trigger: 'axis',
      axisPointer: { type: 'shadow' },
      formatter: function(params) {
        return params[0].name + '<br/>金额: ¥' + Number(params[0].value).toLocaleString('zh-CN')
      }
    },
    grid: {
      left: 80,
      right: 30,
      top: 30,
      bottom: 30
    },
    xAxis: {
      type: 'value',
      axisLabel: { color: '#909399', fontSize: 11 },
      splitLine: { lineStyle: { type: 'dashed', color: '#f0f0f0' } }
    },
    yAxis: {
      type: 'category',
      data: items,
      axisLabel: { color: '#606266', fontSize: 12 }
    },
    series: [{
      type: 'bar',
      data: values.map((value, index) => ({
        value,
        itemStyle: {
          color: colors[index],
          borderRadius: [0, 4, 4, 0]
        }
      })),
      barWidth: 24,
      label: {
        show: true,
        position: 'right',
        color: '#606266',
        fontSize: 11,
        formatter: function(params) {
          return '¥' + Number(params.value).toLocaleString('zh-CN')
        }
      }
    }]
  })
}

const handleResize = () => {
  usageRateChart?.resize()
  costBenefitChart?.resize()
}

onMounted(() => {
  loadData()
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  usageRateChart?.dispose()
  costBenefitChart?.dispose()
})
</script>

<style scoped>
.benefit-usage-statistics {
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

.stat-card.discount {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.stat-card.upgrade {
  background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%);
}

.stat-card.late {
  background: linear-gradient(135deg, #fa709a 0%, #fee140 100%);
}

.stat-card.deposit {
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

.cost-cards {
  margin-bottom: 16px;
}

.cost-item {
  text-align: center;
  padding: 20px;
  border-radius: 8px;
  background: #f5f7fa;
}

.cost-label {
  font-size: 13px;
  color: #909399;
  margin-bottom: 8px;
}

.cost-value {
  font-size: 24px;
  font-weight: 700;
  line-height: 1.2;
}

.cost-value.discount { color: #f56c6c; }
.cost-value.upgrade { color: #e6a23c; }
.cost-value.total { color: #909399; }
.cost-value.roi { color: #409eff; }
.cost-value.roi.positive { color: #67c23a; }

.cost-sub {
  font-size: 12px;
  color: #909399;
  margin-top: 6px;
}

.charts-row {
  margin-bottom: 16px;
}

.chart-title {
  font-weight: 600;
  color: #303133;
  font-size: 14px;
}

.bar-chart {
  height: 320px;
  width: 100%;
}

.detail-row {
  margin-bottom: 16px;
}

.detail-card {
  height: 100%;
}

.summary-card {
  margin-bottom: 16px;
}
</style>
