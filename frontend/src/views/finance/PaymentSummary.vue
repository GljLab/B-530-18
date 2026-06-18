<template>
  <div class="payment-summary">
    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <span class="title">收款汇总</span>
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
              <div class="stat-value">¥{{ formatMoney(summaryData.grandTotal) }}</div>
              <div class="stat-label">总收款金额</div>
            </div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="stat-card green">
            <div class="stat-icon">
              <el-icon :size="28"><Document /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ summaryData.totalCount || 0 }}</div>
              <div class="stat-label">总收款笔数</div>
            </div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="stat-card" :class="summaryData.refundTotal > 0 ? 'red' : 'green'">
            <div class="stat-icon">
              <el-icon :size="28"><RefreshLeft /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">¥{{ formatMoney(summaryData.refundTotal) }}</div>
              <div class="stat-label">退款总额</div>
            </div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="stat-card orange">
            <div class="stat-icon">
              <el-icon :size="28"><List /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ summaryData.refundCount || 0 }}</div>
              <div class="stat-label">退款笔数</div>
            </div>
          </div>
        </el-col>
      </el-row>

      <el-table :data="tableDataWithPercent" v-loading="loading" border stripe style="width: 100%; margin-top: 20px">
        <el-table-column prop="method" label="支付方式" width="160" />
        <el-table-column label="收款金额" width="180" align="right">
          <template #default="{ row }">
            <span class="price">¥{{ formatMoney(row.amount) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="count" label="收款笔数" width="120" align="center" />
        <el-table-column label="占比" min-width="240">
          <template #default="{ row }">
            <div class="percent-cell">
              <el-progress
                :percentage="row.percentage"
                :stroke-width="14"
                :color="getProgressColor(row.percentage)"
              />
              <span class="percent-text">{{ row.percentage.toFixed(1) }}%</span>
            </div>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Wallet, Document, RefreshLeft, List } from '@element-plus/icons-vue'
import api from '@/api'

const loading = ref(false)
const summaryData = ref({
  grandTotal: 0,
  totalCount: 0,
  refundTotal: 0,
  refundCount: 0
})
const tableData = ref([])

const searchForm = reactive({
  dateRange: null
})

const tableDataWithPercent = computed(() => {
  const totalAmount = tableData.value.reduce((sum, item) => sum + (item.amount || 0), 0)
  return tableData.value.map(item => ({
    ...item,
    percentage: totalAmount > 0 ? (item.amount / totalAmount) * 100 : 0
  }))
})

const formatMoney = (val) => {
  if (val === null || val === undefined) return '0.00'
  return Number(val).toFixed(2)
}

const getProgressColor = (percentage) => {
  if (percentage >= 40) return '#409eff'
  if (percentage >= 25) return '#67c23a'
  if (percentage >= 10) return '#e6a23c'
  return '#909399'
}

const loadData = async () => {
  loading.value = true
  try {
    const params = {}
    if (searchForm.dateRange && searchForm.dateRange.length === 2) {
      params.startDate = searchForm.dateRange[0]
      params.endDate = searchForm.dateRange[1]
    }
    const res = await api.finance.statistics.paymentSummary(params)
    if (res.code === 200) {
      const data = res.data || {}
      summaryData.value = {
        grandTotal: data.grandTotal || 0,
        totalCount: data.totalCount || 0,
        refundTotal: data.refundTotal || 0,
        refundCount: data.refundCount || 0
      }
      tableData.value = data.paymentDetails || []
    }
  } catch {
    ElMessage.error('加载数据失败')
  } finally {
    loading.value = false
  }
}

const handleExport = async () => {
  try {
    const params = {}
    if (searchForm.dateRange && searchForm.dateRange.length === 2) {
      params.startDate = searchForm.dateRange[0]
      params.endDate = searchForm.dateRange[1]
    }
    const res = await api.finance.statistics.paymentSummary(params)
    if (res.code === 200) {
      const data = res.data || {}
      const details = data.paymentDetails || []
      let csv = '\uFEFF支付方式,收款金额,收款笔数\n'
      details.forEach(item => {
        csv += `${item.method},${item.amount},${item.count}\n`
      })
      csv += `总计,${data.grandTotal || 0},${data.totalCount || 0}\n`
      const blob = new Blob([csv], { type: 'text/csv;charset=utf-8;' })
      const link = document.createElement('a')
      link.href = URL.createObjectURL(blob)
      link.download = `收款汇总_${new Date().toISOString().slice(0, 10)}.csv`
      link.click()
      URL.revokeObjectURL(link.href)
      ElMessage.success('导出成功')
    }
  } catch {
    ElMessage.error('导出失败')
  }
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.payment-summary {
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
</style>
