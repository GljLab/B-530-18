<template>
  <div class="collection-detail">
    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <span class="title">收款明细报表</span>
        </div>
      </template>

      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="日期范围" required>
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
        <el-form-item label="收款类型">
          <el-select v-model="searchForm.paymentType" placeholder="全部" clearable style="width: 160px">
            <el-option label="房费" :value="1" />
            <el-option label="押金" :value="2" />
            <el-option label="加床费" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item label="支付方式">
          <el-select v-model="searchForm.paymentMethod" placeholder="全部" clearable style="width: 160px">
            <el-option label="现金" :value="1" />
            <el-option label="刷卡" :value="2" />
            <el-option label="移动支付" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button type="success" @click="handleExport">导出</el-button>
        </el-form-item>
      </el-form>

      <el-row :gutter="16" class="summary-row">
        <el-col :span="5">
          <div class="stat-card blue">
            <div class="stat-icon">
              <el-icon :size="28"><Wallet /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">¥{{ formatMoney(summaryData.totalAmount) }}</div>
              <div class="stat-label">总收款金额</div>
            </div>
          </div>
        </el-col>
        <el-col :span="5">
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
        <el-col :span="5">
          <div class="stat-card orange">
            <div class="stat-icon">
              <el-icon :size="28"><Money /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">¥{{ formatMoney(summaryData.cashAmount) }}</div>
              <div class="stat-label">现金</div>
            </div>
          </div>
        </el-col>
        <el-col :span="5">
          <div class="stat-card purple">
            <div class="stat-icon">
              <el-icon :size="28"><CreditCard /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">¥{{ formatMoney(summaryData.cardAmount) }}</div>
              <div class="stat-label">刷卡</div>
            </div>
          </div>
        </el-col>
        <el-col :span="4">
          <div class="stat-card cyan">
            <div class="stat-icon">
              <el-icon :size="28"><Cellphone /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">¥{{ formatMoney(summaryData.mobileAmount) }}</div>
              <div class="stat-label">移动支付</div>
            </div>
          </div>
        </el-col>
      </el-row>

      <el-table :data="tableData" v-loading="loading" border stripe style="width: 100%; margin-top: 20px">
        <el-table-column prop="paymentTime" label="收款时间" width="170" />
        <el-table-column prop="orderNo" label="订单号" width="180" />
        <el-table-column prop="customerName" label="客户姓名" width="120" />
        <el-table-column label="收款类型" width="120" align="center">
          <template #default="{ row }">
            <el-tag :type="paymentTypeTag(row.paymentType)" size="small">{{ paymentTypeMap[row.paymentType] || '-' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="支付方式" width="120" align="center">
          <template #default="{ row }">
            <el-tag :type="paymentMethodTag(row.paymentMethod)" size="small">{{ paymentMethodMap[row.paymentMethod] || '-' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="金额" width="150" align="right">
          <template #default="{ row }">
            <span class="price">¥{{ formatMoney(row.amount) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="operatorName" label="收款人" width="100" />
      </el-table>

      <el-pagination
        v-model:current-page="pagination.pageNum"
        v-model:page-size="pagination.pageSize"
        :total="pagination.total"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next, jumper"
        class="pagination"
        @size-change="loadData"
        @current-change="loadData"
      />
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Wallet, Document, Money, CreditCard, Cellphone } from '@element-plus/icons-vue'
import api from '@/api'

const paymentTypeMap = { 1: '房费', 2: '押金', 3: '加床费' }
const paymentMethodMap = { 1: '现金', 2: '刷卡', 3: '移动支付' }

const paymentTypeTag = (type) => {
  const map = { 1: '', 2: 'warning', 3: 'info' }
  return map[type] ?? 'info'
}

const paymentMethodTag = (method) => {
  const map = { 1: '', 2: 'success', 3: 'warning' }
  return map[method] ?? 'info'
}

const formatMoney = (val) => {
  if (val === null || val === undefined) return '0.00'
  return Number(val).toFixed(2)
}

const loading = ref(false)
const tableData = ref([])
const searchForm = reactive({
  dateRange: null,
  paymentType: null,
  paymentMethod: null
})
const pagination = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0
})
const summaryData = ref({
  totalAmount: 0,
  totalCount: 0,
  cashAmount: 0,
  cardAmount: 0,
  mobileAmount: 0
})

const loadData = async () => {
  if (!searchForm.dateRange || searchForm.dateRange.length < 2) {
    ElMessage.warning('请选择日期范围后再查询')
    return
  }
  loading.value = true
  try {
    const params = {
      startDate: searchForm.dateRange[0],
      endDate: searchForm.dateRange[1],
      pageNum: pagination.pageNum,
      pageSize: pagination.pageSize,
      paymentType: searchForm.paymentType ?? undefined,
      paymentMethod: searchForm.paymentMethod ?? undefined
    }
    const res = await api.finance.advanced.collectionDetail(params)
    if (res.code === 200) {
      const data = res.data || {}
      tableData.value = data.list || []
      pagination.total = data.total || 0
      summaryData.value = {
        totalAmount: data.totalAmount || 0,
        totalCount: data.totalCount || 0,
        cashAmount: data.cashAmount || 0,
        cardAmount: data.cardAmount || 0,
        mobileAmount: data.mobileAmount || 0
      }
    }
  } catch {
    ElMessage.error('加载数据失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  pagination.pageNum = 1
  loadData()
}

const handleExport = async () => {
  if (!searchForm.dateRange || searchForm.dateRange.length < 2) {
    ElMessage.warning('请选择日期范围后再导出')
    return
  }
  try {
    const params = {
      startDate: searchForm.dateRange[0],
      endDate: searchForm.dateRange[1],
      paymentType: searchForm.paymentType ?? undefined,
      paymentMethod: searchForm.paymentMethod ?? undefined,
      pageNum: 1,
      pageSize: 99999
    }
    const res = await api.finance.advanced.collectionDetail(params)
    if (res.code === 200) {
      const data = res.data || {}
      const list = data.list || []
      let csv = '\uFEFF收款时间,订单号,客户姓名,收款类型,支付方式,金额,收款人\n'
      list.forEach(item => {
        csv += `${item.paymentTime},${item.orderNo},${item.customerName},${paymentTypeMap[item.paymentType] || ''},${paymentMethodMap[item.paymentMethod] || ''},${item.amount},${item.operatorName}\n`
      })
      csv += `\n总计,,,,-,${data.totalAmount || 0},\n`
      const blob = new Blob([csv], { type: 'text/csv;charset=utf-8;' })
      const link = document.createElement('a')
      link.href = URL.createObjectURL(blob)
      link.download = `收款明细_${new Date().toISOString().slice(0, 10)}.csv`
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
.collection-detail {
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
  background: linear-gradient(135deg, #9b59b6 0%, #b07cc6 100%);
}

.stat-card.cyan .stat-icon {
  background: linear-gradient(135deg, #00bcd4 0%, #26c6da 100%);
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

.pagination {
  margin-top: 20px;
  justify-content: flex-end;
  display: flex;
}

.price {
  color: #409eff;
  font-weight: 600;
}
</style>
