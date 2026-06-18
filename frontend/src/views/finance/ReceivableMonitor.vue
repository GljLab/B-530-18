<template>
  <div class="receivable-monitor">
    <el-row :gutter="16" class="summary-row">
      <el-col :span="8">
        <el-card shadow="never" class="summary-card">
          <div class="summary-label">应收账款总额</div>
          <div class="summary-value total-red">¥{{ formatMoney(summary.totalReceivable) }}</div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="never" class="summary-card">
          <div class="summary-label">预警单位数</div>
          <div class="summary-value total-yellow">{{ summary.warningCount }}</div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="never" class="summary-card">
          <div class="summary-label">超期未结算单位数</div>
          <div class="summary-value total-red">{{ summary.overdueCount }}</div>
        </el-card>
      </el-col>
    </el-row>

    <el-card shadow="never" class="table-card">
      <template #header>
        <div class="card-header">
          <span class="title">协议单位应收状态</span>
          <el-button type="primary" :loading="loading" @click="loadData">
            <el-icon><Refresh /></el-icon>刷新
          </el-button>
        </div>
      </template>

      <el-table
        :data="tableData"
        v-loading="loading"
        border
        stripe
        style="width: 100%"
        :row-class-name="rowClassName"
      >
        <el-table-column prop="unitName" label="单位名称" min-width="160" show-overflow-tooltip />
        <el-table-column label="当前欠款" width="140" align="right">
          <template #default="{ row }">
            <span class="debt-red">{{ formatMoney(row.currentDebt) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="信用额度" width="140" align="right">
          <template #default="{ row }">
            {{ formatMoney(row.creditLimit) }}
          </template>
        </el-table-column>
        <el-table-column label="额度使用率" width="180" align="center">
          <template #default="{ row }">
            <el-progress
              :percentage="calcUsageRate(row)"
              :color="usageColor(calcUsageRate(row))"
              :stroke-width="14"
              :text-inside="true"
            />
          </template>
        </el-table-column>
        <el-table-column prop="earliestDebtTime" label="最早欠款时间" width="180" />
        <el-table-column label="预警级别" width="120" align="center">
          <template #default="{ row }">
            <span v-if="row.warningLevel === 0" class="level-normal">正常</span>
            <span v-else-if="row.warningLevel === 1" class="level-attention">注意</span>
            <span v-else-if="row.warningLevel === 2" class="level-warning">警告</span>
            <span v-else-if="row.warningLevel === 3" class="level-overdue">超期</span>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onBeforeUnmount } from 'vue'
import { ElMessage } from 'element-plus'
import { Refresh } from '@element-plus/icons-vue'
import api from '@/api'

const loading = ref(false)
const tableData = ref([])
const summary = reactive({
  totalReceivable: 0,
  warningCount: 0,
  overdueCount: 0
})

let autoRefreshTimer = null
const AUTO_REFRESH_INTERVAL = 60000

const formatMoney = (amount) => {
  if (amount === null || amount === undefined) return '0.00'
  return Number(amount).toFixed(2).replace(/\B(?=(\d{3})+(?!\d))/g, ',')
}

const calcUsageRate = (row) => {
  if (!row.creditLimit || row.creditLimit <= 0) return 0
  const rate = (row.currentDebt || 0) / row.creditLimit * 100
  return Math.min(Math.round(rate * 100) / 100, 100)
}

const usageColor = (percentage) => {
  if (percentage > 95) return '#f56c6c'
  if (percentage >= 80) return '#e6a23c'
  return '#409eff'
}

const rowClassName = ({ row }) => {
  if (row.warningLevel >= 2) return 'danger-row'
  if (row.warningLevel === 1) return 'warning-row'
  return ''
}

const loadData = async () => {
  loading.value = true
  try {
    const res = await api.finance.statistics.receivableMonitor()
    if (res.code === 200) {
      const units = Array.isArray(res.data) ? res.data : (res.data?.units || [])
      tableData.value = units
      summary.totalReceivable = units.reduce((sum, u) => sum + (Number(u.currentDebt) || 0), 0)
      summary.warningCount = units.filter(u => u.warningLevel > 0).length
      summary.overdueCount = units.filter(u => u.warningLevel === 3).length
    }
  } catch {
    ElMessage.error('加载应收监控数据失败')
  } finally {
    loading.value = false
  }
}

const startAutoRefresh = () => {
  stopAutoRefresh()
  autoRefreshTimer = setInterval(() => {
    loadData()
  }, AUTO_REFRESH_INTERVAL)
}

const stopAutoRefresh = () => {
  if (autoRefreshTimer) {
    clearInterval(autoRefreshTimer)
    autoRefreshTimer = null
  }
}

onMounted(() => {
  loadData()
  startAutoRefresh()
})

onBeforeUnmount(() => {
  stopAutoRefresh()
})
</script>

<style scoped>
.receivable-monitor {
  padding: 10px;
}

.summary-row {
  margin-bottom: 16px;
}

.summary-card {
  border-radius: 12px;
  border: none;
  text-align: center;
}

.summary-label {
  font-size: 14px;
  color: #909399;
  margin-bottom: 8px;
}

.summary-value {
  font-size: 28px;
  font-weight: 700;
}

.total-red {
  color: #f56c6c;
}

.total-yellow {
  color: #e6a23c;
}

.table-card {
  border-radius: 12px;
  border: none;
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

.debt-red {
  color: #f56c6c;
  font-weight: 600;
}

.level-normal {
  color: #67c23a;
  font-weight: 600;
}

.level-attention {
  color: #e6a23c;
  font-weight: 600;
}

.level-warning {
  color: #f56c6c;
  font-weight: 600;
}

.level-overdue {
  color: #f56c6c;
  font-weight: 700;
  animation: blink 1s ease-in-out infinite;
}

@keyframes blink {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.3; }
}

:deep(.el-table .warning-row) {
  background-color: #fdf6ec !important;
}

:deep(.el-table .danger-row) {
  background-color: #fef0f0 !important;
}
</style>
