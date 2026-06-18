<template>
  <div class="cashier-statistics">
    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <span class="title">收款人员统计</span>
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

      <el-form :inline="true" class="sort-form">
        <el-form-item label="排序方式">
          <el-radio-group v-model="sortMode" @change="handleSortChange">
            <el-radio value="amount">按金额排名</el-radio>
            <el-radio value="count">按笔数排名</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>

      <el-table :data="pagedData" v-loading="loading" border stripe style="width: 100%; margin-top: 20px">
        <el-table-column label="排名" width="80" align="center">
          <template #default="{ $index }">
            <span :class="getRankClass($index + (pagination.currentPage - 1) * pagination.pageSize)">{{ $index + (pagination.currentPage - 1) * pagination.pageSize + 1 }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="cashierName" label="收款人员" width="120" />
        <el-table-column prop="count" label="收款笔数" width="110" align="center" />
        <el-table-column label="总金额" width="160" align="right">
          <template #default="{ row }">
            <span class="price">¥{{ formatMoney(row.totalAmount) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="平均单笔金额" width="160" align="right">
          <template #default="{ row }">
            <span>¥{{ formatMoney(row.avgAmount) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="现金占比" width="120" align="center">
          <template #default="{ row }">
            <span>{{ row.cashRate }}%</span>
          </template>
        </el-table-column>
        <el-table-column label="刷卡占比" width="120" align="center">
          <template #default="{ row }">
            <span>{{ row.cardRate }}%</span>
          </template>
        </el-table-column>
        <el-table-column label="移动支付占比" width="130" align="center">
          <template #default="{ row }">
            <span>{{ row.mobileRate }}%</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120" align="center" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="openDetail(row)">查看详情</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:current-page="pagination.currentPage"
        v-model:page-size="pagination.pageSize"
        :page-sizes="[10, 20, 50]"
        :total="tableData.length"
        layout="total, sizes, prev, pager, next, jumper"
        class="pagination"
      />
    </el-card>

    <el-dialog v-model="detailVisible" :title="`${detailCashier} - 收款详情`" width="800px" destroy-on-close>
      <div class="detail-header">
        <span>收款人员：{{ detailCashier }}</span>
        <span v-if="searchForm.dateRange && searchForm.dateRange.length === 2" style="margin-left: 24px">
          日期范围：{{ searchForm.dateRange[0] }} 至 {{ searchForm.dateRange[1] }}
        </span>
      </div>
      <el-table :data="detailData" v-loading="detailLoading" border stripe style="width: 100%; margin-top: 16px" max-height="400">
        <el-table-column prop="time" label="时间" width="180" />
        <el-table-column prop="orderNo" label="订单号" width="160" />
        <el-table-column prop="customer" label="客户" width="120" />
        <el-table-column prop="paymentMethod" label="支付方式" width="120" align="center" />
        <el-table-column label="金额" width="140" align="right">
          <template #default="{ row }">
            <span class="price">¥{{ formatMoney(row.amount) }}</span>
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import api from '@/api'

const loading = ref(false)
const detailLoading = ref(false)
const tableData = ref([])
const allData = ref({ list: [], rankedByAmount: [], rankedByCount: [] })
const sortMode = ref('amount')
const detailVisible = ref(false)
const detailCashier = ref('')
const detailCashierId = ref(null)
const detailData = ref([])

const searchForm = reactive({
  dateRange: null
})

const pagination = reactive({
  currentPage: 1,
  pageSize: 10
})

const pagedData = computed(() => {
  const start = (pagination.currentPage - 1) * pagination.pageSize
  return tableData.value.slice(start, start + pagination.pageSize)
})

const formatMoney = (val) => {
  if (val === null || val === undefined) return '0.00'
  return Number(val).toFixed(2)
}

const getRankClass = (index) => {
  if (index === 0) return 'rank-gold'
  if (index === 1) return 'rank-silver'
  if (index === 2) return 'rank-bronze'
  return ''
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
    const res = await api.finance.advanced.cashierStatistics(params)
    if (res.code === 200) {
      allData.value = res.data || { list: [], rankedByAmount: [], rankedByCount: [] }
      applySortMode()
    }
  } catch {
    ElMessage.error('加载数据失败')
  } finally {
    loading.value = false
  }
}

const applySortMode = () => {
  if (sortMode.value === 'amount') {
    tableData.value = [...(allData.value.rankedByAmount || [])]
  } else {
    tableData.value = [...(allData.value.rankedByCount || [])]
  }
  pagination.currentPage = 1
}

const handleSortChange = () => {
  applySortMode()
}

const openDetail = async (row) => {
  detailCashierId.value = row.cashierId
  detailCashier.value = row.cashierName
  detailVisible.value = true
  detailLoading.value = true
  try {
    const params = {}
    if (searchForm.dateRange && searchForm.dateRange.length === 2) {
      params.startDate = searchForm.dateRange[0]
      params.endDate = searchForm.dateRange[1]
    }
    const res = await api.finance.advanced.cashierDetail(row.cashierId, params)
    if (res.code === 200) {
      detailData.value = res.data || []
    }
  } catch {
    ElMessage.error('加载详情失败')
  } finally {
    detailLoading.value = false
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
    const res = await api.finance.advanced.cashierStatistics(params)
    if (res.code === 200) {
      const data = res.data || {}
      const list = data.rankedByAmount || []
      let csv = '\uFEFF排名,收款人员,收款笔数,总金额,平均单笔金额,现金占比,刷卡占比,移动支付占比\n'
      list.forEach((item, index) => {
        csv += `${index + 1},${item.cashierName},${item.count},${item.totalAmount},${item.avgAmount},${item.cashRate}%,${item.cardRate}%,${item.mobileRate}%\n`
      })
      const blob = new Blob([csv], { type: 'text/csv;charset=utf-8;' })
      const link = document.createElement('a')
      link.href = URL.createObjectURL(blob)
      link.download = `收款人员统计_${new Date().toISOString().slice(0, 10)}.csv`
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
.cashier-statistics {
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
  margin-bottom: 8px;
}

.sort-form {
  margin-bottom: 0;
}

.pagination {
  margin-top: 16px;
  justify-content: flex-end;
}

.price {
  color: #409eff;
  font-weight: 600;
}

.rank-gold {
  display: inline-block;
  width: 24px;
  height: 24px;
  line-height: 24px;
  border-radius: 50%;
  background: linear-gradient(135deg, #ffd700 0%, #ffb800 100%);
  color: #fff;
  font-weight: 700;
  font-size: 12px;
}

.rank-silver {
  display: inline-block;
  width: 24px;
  height: 24px;
  line-height: 24px;
  border-radius: 50%;
  background: linear-gradient(135deg, #c0c0c0 0%, #a8a8a8 100%);
  color: #fff;
  font-weight: 700;
  font-size: 12px;
}

.rank-bronze {
  display: inline-block;
  width: 24px;
  height: 24px;
  line-height: 24px;
  border-radius: 50%;
  background: linear-gradient(135deg, #cd7f32 0%, #b87333 100%);
  color: #fff;
  font-weight: 700;
  font-size: 12px;
}

.detail-header {
  font-size: 14px;
  color: #606266;
}
</style>
