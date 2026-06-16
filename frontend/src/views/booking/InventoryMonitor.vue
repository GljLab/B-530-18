<template>
  <div class="inventory-monitor-container">
    <div class="page-header">
      <h2 class="page-title">房量监控</h2>
    </div>

    <el-card shadow="hover" class="filter-card">
      <div class="filter-row">
        <el-date-picker
          v-model="dateRange"
          type="daterange"
          range-separator="至"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
          value-format="YYYY-MM-DD"
          style="width: 300px"
        />
        <el-button type="primary" @click="handleQuery">
          <el-icon><Search /></el-icon>查询
        </el-button>
        <el-button @click="handleExport">
          <el-icon><Download /></el-icon>导出
        </el-button>
      </div>
    </el-card>

    <el-card shadow="hover" class="table-card">
      <el-table :data="tableData" stripe border v-loading="tableLoading" style="width: 100%">
        <el-table-column prop="date" label="日期" width="120" align="center" />
        <el-table-column prop="roomTypeName" label="房型" min-width="140" />
        <el-table-column prop="totalRooms" label="总房量" width="100" align="center" />
        <el-table-column prop="availableRooms" label="可售房量" width="100" align="center" />
        <el-table-column prop="reservedRooms" label="已预订" width="100" align="center" />
        <el-table-column label="剩余" width="100" align="center">
          <template #default="{ row }">
            <span :class="getRemainClass(row)">{{ row.availableRooms - row.reservedRooms }}</span>
          </template>
        </el-table-column>
        <el-table-column label="预订率(%)" width="120" align="center">
          <template #default="{ row }">
            <span :class="getRateClass(row)" class="rate-value">
              {{ calcBookingRate(row) }}%
            </span>
          </template>
        </el-table-column>
        <el-table-column label="超售状态" width="140" align="center">
          <template #default="{ row }">
            <el-tag :type="getStatusTagType(row)" size="small">
              {{ getStatusLabel(row) }}
            </el-tag>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrap">
        <el-pagination
          v-model:current-page="queryParams.pageNum"
          v-model:page-size="queryParams.pageSize"
          :page-sizes="[20, 50, 100]"
          :total="total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="loadData"
          @current-change="loadData"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Search, Download } from '@element-plus/icons-vue'
import api from '@/api'

const dateRange = ref([])
const tableData = ref([])
const total = ref(0)
const tableLoading = ref(false)

const queryParams = reactive({
  pageNum: 1,
  pageSize: 20
})

const calcBookingRate = (row) => {
  if (!row.totalRooms || row.totalRooms === 0) return 0
  return ((row.reservedRooms / row.totalRooms) * 100).toFixed(1)
}

const getRateClass = (row) => {
  const rate = parseFloat(calcBookingRate(row))
  if (rate > 100) return 'rate-red'
  if (rate >= 80) return 'rate-orange'
  if (rate >= 50) return 'rate-yellow'
  return 'rate-green'
}

const getRemainClass = (row) => {
  const remain = row.availableRooms - row.reservedRooms
  if (remain <= 0) return 'remain-danger'
  if (remain <= row.totalRooms * 0.2) return 'remain-warning'
  return ''
}

const getStatusLabel = (row) => {
  const rate = parseFloat(calcBookingRate(row))
  const overbooked = row.reservedRooms - row.totalRooms
  if (overbooked > 0) return `已超售${overbooked}间`
  if (rate >= 80) return '接近满房'
  return '正常'
}

const getStatusTagType = (row) => {
  const rate = parseFloat(calcBookingRate(row))
  const overbooked = row.reservedRooms - row.totalRooms
  if (overbooked > 0) return 'danger'
  if (rate >= 80) return 'warning'
  return 'success'
}

const loadData = async () => {
  tableLoading.value = true
  try {
    const params = { ...queryParams }
    if (dateRange.value && dateRange.value.length === 2) {
      params.startDate = dateRange.value[0]
      params.endDate = dateRange.value[1]
    }
    const res = await api.inventory.monitor(params)
    if (res.code === 200) {
      tableData.value = res.data?.records || res.data?.list || []
      total.value = res.data?.total || 0
    }
  } catch {
    tableData.value = []
    total.value = 0
  } finally {
    tableLoading.value = false
  }
}

const handleQuery = () => {
  queryParams.pageNum = 1
  loadData()
}

const handleExport = async () => {
  if (!dateRange.value || dateRange.value.length !== 2) {
    ElMessage.warning('请选择日期范围')
    return
  }
  try {
    const params = {
      startDate: dateRange.value[0],
      endDate: dateRange.value[1]
    }
    const res = await api.inventory.monitorExport(params)
    const blob = new Blob([res], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' })
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = `房量监控_${dateRange.value[0]}_${dateRange.value[1]}.xlsx`
    link.click()
    window.URL.revokeObjectURL(url)
    ElMessage.success('导出成功')
  } catch {
    ElMessage.error('导出失败')
  }
}

onMounted(() => {
  const now = new Date()
  const start = new Date(now)
  start.setDate(start.getDate() - 7)
  const fmt = (d) => `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`
  dateRange.value = [fmt(start), fmt(now)]
  loadData()
})
</script>

<style scoped>
.inventory-monitor-container {
  padding: 16px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.page-title {
  font-size: 20px;
  font-weight: 600;
  color: #2d3748;
  margin: 0;
}

.filter-card {
  margin-bottom: 16px;
}

.filter-row {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  align-items: center;
}

.table-card {
  margin-bottom: 16px;
}

.pagination-wrap {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}

.rate-value {
  font-weight: 600;
}

.rate-green {
  color: #67c23a;
}

.rate-yellow {
  color: #e6a23c;
}

.rate-orange {
  color: #f56c6c;
}

.rate-red {
  color: #c45656;
  font-weight: 700;
}

.remain-danger {
  color: #f56c6c;
  font-weight: 700;
}

.remain-warning {
  color: #e6a23c;
}
</style>
