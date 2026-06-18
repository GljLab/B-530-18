<template>
  <div class="bad-debt-manage">
    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <span class="title">坏账管理</span>
          <div>
            <el-button type="warning" @click="handleIdentify">识别坏账</el-button>
            <el-button @click="loadData">刷新</el-button>
          </div>
        </div>
      </template>

      <el-row :gutter="16" class="summary-row">
        <el-col :span="6">
          <div class="stat-card red">
            <div class="stat-icon">
              <el-icon :size="28"><Warning /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">¥{{ formatMoney(statistics.totalAmount) }}</div>
              <div class="stat-label">坏账总额</div>
            </div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="stat-card orange">
            <div class="stat-icon">
              <el-icon :size="28"><Document /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ statistics.totalCount || 0 }}</div>
              <div class="stat-label">坏账笔数</div>
            </div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="stat-card" :class="statistics.badDebtRate > 5 ? 'red' : 'blue'">
            <div class="stat-icon">
              <el-icon :size="28"><TrendCharts /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ statistics.badDebtRate || 0 }}%</div>
              <div class="stat-label">坏账率</div>
            </div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="stat-card green">
            <div class="stat-icon">
              <el-icon :size="28"><CircleCheck /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">¥{{ formatMoney(statistics.writeOffAmount) }}</div>
              <div class="stat-label">已核销金额</div>
            </div>
          </div>
        </el-col>
      </el-row>

      <el-alert
        v-if="statistics.badDebtRate > 5"
        title="风险预警：坏账率已超过5%阈值，请及时关注！"
        type="error"
        show-icon
        :closable="false"
        class="risk-alert"
      />
      <el-alert
        v-if="statistics.overdueAmount > statistics.overdueLimit"
        :title="`风险预警：逾期金额 ¥${formatMoney(statistics.overdueAmount)} 已超过限额 ¥${formatMoney(statistics.overdueLimit)}`"
        type="warning"
        show-icon
        :closable="false"
        class="risk-alert"
      />

      <el-form :inline="true" :model="filterForm" class="filter-form">
        <el-form-item label="关键词">
          <el-input v-model="filterForm.keyword" placeholder="客户姓名/单号" clearable style="width: 160px" />
        </el-form-item>
        <el-form-item label="坏账类型">
          <el-select v-model="filterForm.debtType" placeholder="全部" clearable style="width: 140px">
            <el-option label="挂账超期" :value="1" />
            <el-option label="逃单" :value="2" />
            <el-option label="拖欠费用" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="filterForm.status" placeholder="全部" clearable style="width: 140px">
            <el-option label="疑似" :value="0" />
            <el-option label="催收中" :value="1" />
            <el-option label="已核销" :value="2" />
            <el-option label="追偿中" :value="3" />
            <el-option label="部分收回" :value="4" />
          </el-select>
        </el-form-item>
        <el-form-item label="日期范围">
          <el-date-picker
            v-model="filterForm.dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            value-format="YYYY-MM-DD"
            style="width: 260px"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadData">查询</el-button>
          <el-button type="success" @click="handleExport">导出</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="tableData" v-loading="loading" border stripe style="width: 100%; margin-top: 20px">
        <el-table-column prop="debtNo" label="坏账编号" width="130" />
        <el-table-column prop="orderNo" label="关联单号" width="140" />
        <el-table-column prop="customerName" label="客户姓名" width="100" />
        <el-table-column label="坏账类型" width="110" align="center">
          <template #default="{ row }">
            <el-tag :type="debtTypeTag(row.debtType)" size="small">{{ debtTypeLabel(row.debtType) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="欠款金额" width="130" align="right">
          <template #default="{ row }">
            <span class="price">¥{{ formatMoney(row.debtAmount) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="已收回金额" width="130" align="right">
          <template #default="{ row }">
            <span>¥{{ formatMoney(row.recoveredAmount) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="剩余欠款" width="130" align="right">
          <template #default="{ row }">
            <span class="remaining">¥{{ formatMoney(row.remainingAmount) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="overdueDays" label="逾期天数" width="90" align="center" />
        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="statusTagType(row.status)" size="small">{{ statusLabel(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="debtDate" label="欠款时间" width="120" />
        <el-table-column label="操作" min-width="220" align="center" fixed="right">
          <template #default="{ row }">
            <el-button v-if="row.status === 0 || row.status === 1" type="warning" link size="small" @click="openCollectDialog(row)">催收</el-button>
            <el-button v-if="row.status === 1" type="success" link size="small" @click="openRecoverDialog(row)">部分收回</el-button>
            <el-button v-if="row.status !== 2" type="danger" link size="small" @click="openWriteoffDialog(row)">核销</el-button>
            <el-button v-if="row.status !== 2" type="info" link size="small" @click="openLegalDialog(row)">法律追偿</el-button>
            <el-button type="primary" link size="small" @click="openDetailDialog(row)">详情</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:current-page="pagination.currentPage"
        v-model:page-size="pagination.pageSize"
        :page-sizes="[10, 20, 50]"
        :total="pagination.total"
        layout="total, sizes, prev, pager, next, jumper"
        class="pagination"
        @current-change="loadData"
        @size-change="loadData"
      />
    </el-card>

    <el-card shadow="never" class="chart-card">
      <template #header>
        <div class="card-header">
          <span class="title">近12个月坏账趋势</span>
        </div>
      </template>
      <div ref="trendChartRef" class="chart-container"></div>
    </el-card>

    <el-dialog v-model="collectVisible" title="催收记录" width="500px" destroy-on-close>
      <el-form :model="collectForm" label-width="100px">
        <el-form-item label="催收方式">
          <el-select v-model="collectForm.actionMethod" placeholder="请选择" style="width: 100%">
            <el-option label="电话" :value="1" />
            <el-option label="短信" :value="2" />
            <el-option label="上门" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item label="催收结果">
          <el-input v-model="collectForm.actionResult" type="textarea" :rows="4" placeholder="请输入催收结果" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="collectVisible = false">取消</el-button>
        <el-button type="primary" @click="submitCollect">确认</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="recoverVisible" title="部分收回" width="500px" destroy-on-close>
      <el-form :model="recoverForm" label-width="100px">
        <el-form-item label="收回金额">
          <el-input-number v-model="recoverForm.recoveredAmount" :min="0.01" :precision="2" style="width: 100%" />
        </el-form-item>
        <el-form-item label="收款方式">
          <el-select v-model="recoverForm.paymentMethod" placeholder="请选择" style="width: 100%">
            <el-option label="现金" :value="1" />
            <el-option label="刷卡" :value="2" />
            <el-option label="移动支付" :value="3" />
            <el-option label="转账" :value="4" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="recoverVisible = false">取消</el-button>
        <el-button type="primary" @click="submitRecover">确认</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="writeoffVisible" title="坏账核销" width="500px" destroy-on-close>
      <el-form :model="writeoffForm" label-width="100px">
        <el-form-item label="核销原因">
          <el-input v-model="writeoffForm.writeOffReason" type="textarea" :rows="4" placeholder="请输入核销原因" />
        </el-form-item>
        <el-form-item label="核销凭证">
          <el-input v-model="writeoffForm.writeOffProof" placeholder="请输入核销凭证编号" />
        </el-form-item>
        <el-alert title="需财务经理或总经理审批" type="warning" show-icon :closable="false" />
      </el-form>
      <template #footer>
        <el-button @click="writeoffVisible = false">取消</el-button>
        <el-button type="primary" @click="submitWriteoff">确认核销</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="legalVisible" title="法律追偿" width="500px" destroy-on-close>
      <el-form :model="legalForm" label-width="100px">
        <el-form-item label="法律状态">
          <el-select v-model="legalForm.legalStatus" placeholder="请选择" style="width: 100%">
            <el-option label="未启动" :value="0" />
            <el-option label="律师函" :value="1" />
            <el-option label="诉讼中" :value="2" />
            <el-option label="执行中" :value="3" />
            <el-option label="已结案" :value="4" />
          </el-select>
        </el-form-item>
        <el-form-item label="法律信息">
          <el-input v-model="legalForm.legalInfo" type="textarea" :rows="4" placeholder="请输入法律追偿相关信息" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="legalVisible = false">取消</el-button>
        <el-button type="primary" @click="submitLegal">确认</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="detailVisible" title="坏账详情" width="700px" destroy-on-close>
      <div v-loading="detailLoading">
        <el-descriptions :column="2" border class="detail-descriptions">
          <el-descriptions-item label="坏账编号">{{ detailData.debtNo }}</el-descriptions-item>
          <el-descriptions-item label="关联单号">{{ detailData.orderNo }}</el-descriptions-item>
          <el-descriptions-item label="客户姓名">{{ detailData.customerName }}</el-descriptions-item>
          <el-descriptions-item label="坏账类型">{{ debtTypeLabel(detailData.debtType) }}</el-descriptions-item>
          <el-descriptions-item label="欠款金额">¥{{ formatMoney(detailData.debtAmount) }}</el-descriptions-item>
          <el-descriptions-item label="已收回金额">¥{{ formatMoney(detailData.recoveredAmount) }}</el-descriptions-item>
          <el-descriptions-item label="剩余欠款">¥{{ formatMoney(detailData.remainingAmount) }}</el-descriptions-item>
          <el-descriptions-item label="逾期天数">{{ detailData.overdueDays }}天</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="statusTagType(detailData.status)" size="small">{{ statusLabel(detailData.status) }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="欠款时间">{{ detailData.debtDate }}</el-descriptions-item>
        </el-descriptions>
        <div class="timeline-title">操作记录</div>
        <el-timeline>
          <el-timeline-item
            v-for="(item, index) in actionHistory"
            :key="index"
            :timestamp="item.actionTime"
            placement="top"
          >
            <div class="timeline-content">
              <div class="timeline-action">{{ item.actionType }}</div>
              <div v-if="item.actionResult" class="timeline-result">{{ item.actionResult }}</div>
              <div v-if="item.operator" class="timeline-operator">操作人：{{ item.operator }}</div>
            </div>
          </el-timeline-item>
        </el-timeline>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onUnmounted, nextTick } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Warning, Document, TrendCharts, CircleCheck } from '@element-plus/icons-vue'
import * as echarts from 'echarts'
import api from '@/api'

const loading = ref(false)
const detailLoading = ref(false)
const tableData = ref([])
const statistics = ref({
  totalAmount: 0,
  totalCount: 0,
  badDebtRate: 0,
  writeOffAmount: 0,
  overdueAmount: 0,
  overdueLimit: 0
})
const trendData = ref({ months: [], amounts: [], rates: [] })
const trendChartRef = ref(null)
let trendChart = null

const filterForm = reactive({
  keyword: '',
  debtType: null,
  status: null,
  dateRange: null
})

const pagination = reactive({
  currentPage: 1,
  pageSize: 10,
  total: 0
})

const currentRow = ref(null)
const collectVisible = ref(false)
const collectForm = reactive({
  actionMethod: null,
  actionResult: ''
})

const recoverVisible = ref(false)
const recoverForm = reactive({
  recoveredAmount: null,
  paymentMethod: null
})

const writeoffVisible = ref(false)
const writeoffForm = reactive({
  writeOffReason: '',
  writeOffProof: ''
})

const legalVisible = ref(false)
const legalForm = reactive({
  legalStatus: null,
  legalInfo: ''
})

const detailVisible = ref(false)
const detailData = ref({})
const actionHistory = ref([])

const formatMoney = (val) => {
  if (val === null || val === undefined) return '0.00'
  return Number(val).toFixed(2)
}

const debtTypeLabel = (type) => {
  const map = { 1: '挂账超期', 2: '逃单', 3: '拖欠费用' }
  return map[type] || '未知'
}

const debtTypeTag = (type) => {
  const map = { 1: 'warning', 2: 'danger', 3: 'info' }
  return map[type] || 'info'
}

const statusLabel = (status) => {
  const map = { 0: '疑似', 1: '催收中', 2: '已核销', 3: '追偿中', 4: '部分收回' }
  return map[status] || '未知'
}

const statusTagType = (status) => {
  const map = { 0: 'info', 1: 'warning', 2: 'success', 3: 'danger', 4: '' }
  return map[status] || 'info'
}

const paymentMethodLabel = (method) => {
  const map = { 1: '现金', 2: '刷卡', 3: '移动支付', 4: '转账' }
  return map[method] || '未知'
}

const loadStatistics = async () => {
  try {
    const res = await api.finance.badDebt.statistics()
    if (res.code === 200) {
      statistics.value = {
        totalAmount: res.data.totalAmount || 0,
        totalCount: res.data.totalCount || 0,
        badDebtRate: res.data.badDebtRate || 0,
        writeOffAmount: res.data.writeOffAmount || 0,
        overdueAmount: res.data.overdueAmount || 0,
        overdueLimit: res.data.overdueLimit || 0
      }
      trendData.value = {
        months: res.data.trendMonths || [],
        amounts: res.data.trendAmounts || [],
        rates: res.data.trendRates || []
      }
      await nextTick()
      renderTrendChart()
    }
  } catch {}
}

const loadData = async () => {
  loading.value = true
  try {
    const params = {
      page: pagination.currentPage,
      pageSize: pagination.pageSize,
      keyword: filterForm.keyword || undefined,
      debtType: filterForm.debtType !== null ? filterForm.debtType : undefined,
      status: filterForm.status !== null ? filterForm.status : undefined
    }
    if (filterForm.dateRange && filterForm.dateRange.length === 2) {
      params.startDate = filterForm.dateRange[0]
      params.endDate = filterForm.dateRange[1]
    }
    const res = await api.finance.badDebt.page(params)
    if (res.code === 200) {
      tableData.value = res.data.list || []
      pagination.total = res.data.total || 0
    }
  } catch {
    ElMessage.error('加载数据失败')
  } finally {
    loading.value = false
  }
}

const renderTrendChart = () => {
  if (!trendChartRef.value) return
  if (!trendChart) {
    trendChart = echarts.init(trendChartRef.value)
  }
  const months = trendData.value.months.length > 0
    ? trendData.value.months
    : Array.from({ length: 12 }, (_, i) => {
        const d = new Date()
        d.setMonth(d.getMonth() - 11 + i)
        return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}`
      })
  trendChart.setOption({
    tooltip: {
      trigger: 'axis',
      axisPointer: { type: 'cross' }
    },
    legend: {
      data: ['坏账金额', '坏账率'],
      bottom: 0
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '15%',
      top: '10%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: months,
      axisLabel: { rotate: 30 }
    },
    yAxis: [
      {
        type: 'value',
        name: '金额(元)',
        axisLabel: { formatter: '¥{value}' }
      },
      {
        type: 'value',
        name: '坏账率(%)',
        axisLabel: { formatter: '{value}%' },
        splitLine: { show: false }
      }
    ],
    series: [
      {
        name: '坏账金额',
        type: 'line',
        data: trendData.value.amounts,
        smooth: true,
        itemStyle: { color: '#f56c6c' },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(245,108,108,0.3)' },
            { offset: 1, color: 'rgba(245,108,108,0.05)' }
          ])
        }
      },
      {
        name: '坏账率',
        type: 'line',
        yAxisIndex: 1,
        data: trendData.value.rates,
        smooth: true,
        itemStyle: { color: '#e6a23c' }
      }
    ]
  })
}

const handleIdentify = async () => {
  try {
    await ElMessageBox.confirm('确认执行坏账识别？系统将自动识别符合条件的坏账记录。', '识别坏账', {
      type: 'warning'
    })
    const res = await api.finance.badDebt.identify()
    if (res.code === 200) {
      ElMessage.success(`识别完成，新增${res.data.count || 0}条坏账记录`)
      loadData()
      loadStatistics()
    }
  } catch {}
}

const openCollectDialog = (row) => {
  currentRow.value = row
  collectForm.actionMethod = null
  collectForm.actionResult = ''
  collectVisible.value = true
}

const submitCollect = async () => {
  if (!collectForm.actionMethod) {
    ElMessage.warning('请选择催收方式')
    return
  }
  try {
    const res = await api.finance.badDebt.collect(currentRow.value.id, {
      actionMethod: collectForm.actionMethod,
      actionResult: collectForm.actionResult
    })
    if (res.code === 200) {
      ElMessage.success('催收记录已提交')
      collectVisible.value = false
      loadData()
      loadStatistics()
    }
  } catch {
    ElMessage.error('操作失败')
  }
}

const openRecoverDialog = (row) => {
  currentRow.value = row
  recoverForm.recoveredAmount = null
  recoverForm.paymentMethod = null
  recoverVisible.value = true
}

const submitRecover = async () => {
  if (!recoverForm.recoveredAmount || recoverForm.recoveredAmount <= 0) {
    ElMessage.warning('请输入有效的收回金额')
    return
  }
  if (!recoverForm.paymentMethod) {
    ElMessage.warning('请选择收款方式')
    return
  }
  try {
    const res = await api.finance.badDebt.recover(currentRow.value.id, {
      recoveredAmount: recoverForm.recoveredAmount,
      paymentMethod: recoverForm.paymentMethod
    })
    if (res.code === 200) {
      ElMessage.success('部分收回已登记')
      recoverVisible.value = false
      loadData()
      loadStatistics()
    }
  } catch {
    ElMessage.error('操作失败')
  }
}

const openWriteoffDialog = (row) => {
  currentRow.value = row
  writeoffForm.writeOffReason = ''
  writeoffForm.writeOffProof = ''
  writeoffVisible.value = true
}

const submitWriteoff = async () => {
  if (!writeoffForm.writeOffReason) {
    ElMessage.warning('请输入核销原因')
    return
  }
  try {
    const res = await api.finance.badDebt.writeoff(currentRow.value.id, {
      writeOffReason: writeoffForm.writeOffReason,
      writeOffProof: writeoffForm.writeOffProof
    })
    if (res.code === 200) {
      ElMessage.success('核销申请已提交，等待审批')
      writeoffVisible.value = false
      loadData()
      loadStatistics()
    }
  } catch {
    ElMessage.error('操作失败')
  }
}

const openLegalDialog = (row) => {
  currentRow.value = row
  legalForm.legalStatus = null
  legalForm.legalInfo = ''
  legalVisible.value = true
}

const submitLegal = async () => {
  if (legalForm.legalStatus === null) {
    ElMessage.warning('请选择法律状态')
    return
  }
  try {
    const res = await api.finance.badDebt.legal(currentRow.value.id, {
      legalStatus: legalForm.legalStatus,
      legalInfo: legalForm.legalInfo
    })
    if (res.code === 200) {
      ElMessage.success('法律追偿已登记')
      legalVisible.value = false
      loadData()
      loadStatistics()
    }
  } catch {
    ElMessage.error('操作失败')
  }
}

const openDetailDialog = async (row) => {
  detailData.value = {}
  actionHistory.value = []
  detailVisible.value = true
  detailLoading.value = true
  try {
    const [infoRes, actionsRes] = await Promise.all([
      api.finance.badDebt.get(row.id),
      api.finance.badDebt.actions(row.id)
    ])
    if (infoRes.code === 200) {
      detailData.value = infoRes.data || {}
    }
    if (actionsRes.code === 200) {
      actionHistory.value = actionsRes.data || []
    }
  } catch {
    ElMessage.error('加载详情失败')
  } finally {
    detailLoading.value = false
  }
}

const handleExport = async () => {
  try {
    const params = {
      keyword: filterForm.keyword || undefined,
      debtType: filterForm.debtType !== null ? filterForm.debtType : undefined,
      status: filterForm.status !== null ? filterForm.status : undefined
    }
    if (filterForm.dateRange && filterForm.dateRange.length === 2) {
      params.startDate = filterForm.dateRange[0]
      params.endDate = filterForm.dateRange[1]
    }
    const res = await api.finance.badDebt.export(params)
    if (res.code === 200) {
      const list = res.data.list || tableData.value
      let csv = '\uFEFF坏账编号,关联单号,客户姓名,坏账类型,欠款金额,已收回金额,剩余欠款,逾期天数,状态,欠款时间\n'
      list.forEach(item => {
        csv += `${item.debtNo},${item.orderNo},${item.customerName},${debtTypeLabel(item.debtType)},${item.debtAmount},${item.recoveredAmount},${item.remainingAmount},${item.overdueDays},${statusLabel(item.status)},${item.debtDate}\n`
      })
      const blob = new Blob([csv], { type: 'text/csv;charset=utf-8;' })
      const link = document.createElement('a')
      link.href = URL.createObjectURL(blob)
      link.download = `坏账管理_${new Date().toISOString().slice(0, 10)}.csv`
      link.click()
      URL.revokeObjectURL(link.href)
      ElMessage.success('导出成功')
    }
  } catch {
    ElMessage.error('导出失败')
  }
}

const handleResize = () => {
  if (trendChart) trendChart.resize()
}

onMounted(() => {
  loadData()
  loadStatistics()
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  if (trendChart) {
    trendChart.dispose()
    trendChart = null
  }
})
</script>

<style scoped>
.bad-debt-manage {
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

.stat-card.red .stat-icon {
  background: linear-gradient(135deg, #f56c6c 0%, #f78989 100%);
}

.stat-card.green .stat-icon {
  background: linear-gradient(135deg, #67c23a 0%, #85ce61 100%);
}

.stat-card.blue .stat-icon {
  background: linear-gradient(135deg, #409eff 0%, #66b1ff 100%);
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

.risk-alert {
  margin-top: 16px;
}

.filter-form {
  margin-top: 16px;
  margin-bottom: 0;
}

.pagination {
  margin-top: 16px;
  justify-content: flex-end;
}

.chart-card {
  margin-top: 20px;
}

.chart-container {
  height: 350px;
}

.price {
  color: #f56c6c;
  font-weight: 600;
}

.remaining {
  color: #e6a23c;
  font-weight: 600;
}

.detail-descriptions {
  margin-bottom: 24px;
}

.timeline-title {
  font-size: 15px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 16px;
}

.timeline-content .timeline-action {
  font-weight: 600;
  color: #303133;
}

.timeline-content .timeline-result {
  font-size: 13px;
  color: #606266;
  margin-top: 4px;
}

.timeline-content .timeline-operator {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
}
</style>
