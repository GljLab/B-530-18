<template>
  <div class="blacklist-management-container">
    <el-card shadow="never" class="search-card">
      <div class="search-row">
        <el-select
          v-model="queryParams.reason"
          placeholder="原因"
          clearable
          style="width: 160px"
        >
          <el-option v-for="r in reasonOptions" :key="r.value" :label="r.label" :value="r.value" />
        </el-select>
        <el-select
          v-model="queryParams.blacklistType"
          placeholder="类型"
          clearable
          style="width: 120px"
        >
          <el-option v-for="t in typeOptions" :key="t.value" :label="t.label" :value="t.value" />
        </el-select>
        <el-select
          v-model="queryParams.status"
          placeholder="状态"
          clearable
          style="width: 140px"
        >
          <el-option v-for="s in statusOptions" :key="s.value" :label="s.label" :value="s.value" />
        </el-select>
        <el-button type="primary" @click="handleSearch">
          <el-icon><Search /></el-icon>搜索
        </el-button>
        <el-button @click="handleReset">重置</el-button>
        <div style="flex: 1" />
        <el-button v-if="hasPermission('customer:blacklist:export')" @click="handleExport">
          <el-icon><Download /></el-icon>导出
        </el-button>
      </div>
    </el-card>

    <el-card shadow="never" class="table-card">
      <el-table
        :data="tableData"
        stripe
        border
        v-loading="tableLoading"
        style="width: 100%"
      >
        <el-table-column prop="customerName" label="姓名" min-width="100" />
        <el-table-column prop="customerPhone" label="手机号" width="130" align="center" />
        <el-table-column prop="customerIdNumber" label="证件号" width="180" align="center" />
        <el-table-column label="原因" width="110" align="center">
          <template #default="{ row }">
            {{ reasonLabel(row.reason) }}
          </template>
        </el-table-column>
        <el-table-column label="类型" width="90" align="center">
          <template #default="{ row }">
            <el-tag size="small" :type="row.blacklistType === 1 ? 'warning' : 'danger'">
              {{ row.blacklistType === 1 ? '临时' : '永久' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="加入时间" width="170" align="center">
          <template #default="{ row }">
            {{ formatTime(row.applyTime) }}
          </template>
        </el-table-column>
        <el-table-column prop="expireTime" label="到期时间" width="170" align="center" />
        <el-table-column label="状态" width="120" align="center">
          <template #default="{ row }">
            <el-tag size="small" :type="statusTagType(row.status)">
              {{ statusLabel(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="100" align="center" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="handleViewDetail(row)">查看详情</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div class="pagination-wrap">
        <el-pagination
          v-model:current-page="queryParams.pageNum"
          v-model:page-size="queryParams.pageSize"
          :page-sizes="[10, 20, 50, 100]"
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
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Search, Download } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import api from '@/api'

const router = useRouter()
const userStore = useUserStore()
const hasPermission = (p) => userStore.hasPermission(p)

const reasonOptions = [
  { value: 1, label: '拖欠房费' },
  { value: 2, label: '破坏设施' },
  { value: 3, label: '扰乱秩序' },
  { value: 4, label: '无理投诉' },
  { value: 5, label: '违法行为' }
]

const typeOptions = [
  { value: 1, label: '临时' },
  { value: 2, label: '永久' }
]

const statusOptions = [
  { value: 1, label: '待审批' },
  { value: 2, label: '审批通过生效' },
  { value: 3, label: '审批拒绝' },
  { value: 4, label: '已解除' },
  { value: 5, label: '待解除' }
]

const reasonLabel = (val) => {
  const map = { 1: '拖欠房费', 2: '破坏设施', 3: '扰乱秩序', 4: '无理投诉', 5: '违法行为' }
  return map[val] || '-'
}

const statusLabel = (val) => {
  const map = { 1: '待审批', 2: '审批通过生效', 3: '审批拒绝', 4: '已解除', 5: '待解除' }
  return map[val] || '-'
}

const statusTagType = (val) => {
  const map = { 1: 'warning', 2: 'danger', 3: 'info', 4: 'success', 5: 'warning' }
  return map[val] || 'info'
}

const formatTime = (val) => {
  if (!val) return '-'
  return val.replace('T', ' ').substring(0, 19)
}

const tableData = ref([])
const total = ref(0)
const tableLoading = ref(false)

const queryParams = reactive({
  reason: null,
  blacklistType: null,
  status: null,
  pageNum: 1,
  pageSize: 10
})

const loadData = async () => {
  tableLoading.value = true
  try {
    const params = {
      pageNum: queryParams.pageNum,
      pageSize: queryParams.pageSize,
      reason: queryParams.reason || undefined,
      blacklistType: queryParams.blacklistType || undefined,
      status: queryParams.status || undefined
    }
    const res = await api.customer.getBlacklistPage(params)
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

const handleSearch = () => {
  queryParams.pageNum = 1
  loadData()
}

const handleReset = () => {
  queryParams.reason = null
  queryParams.blacklistType = null
  queryParams.status = null
  queryParams.pageNum = 1
  loadData()
}

const handleExport = async () => {
  try {
    const params = {
      reason: queryParams.reason || undefined,
      blacklistType: queryParams.blacklistType || undefined,
      status: queryParams.status || undefined
    }
    const blob = await api.customer.exportBlacklist(params)
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = '黑名单列表.xlsx'
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    window.URL.revokeObjectURL(url)
    ElMessage.success('导出成功')
  } catch {
    ElMessage.error('导出失败')
  }
}

const handleViewDetail = (row) => {
  router.push(`/customer/blacklist/detail/${row.id}`)
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.blacklist-management-container {
  padding: 10px;
}

.search-card {
  border-radius: 12px;
  border: none;
  margin-bottom: 12px;
}

.search-row {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}

.table-card {
  border-radius: 12px;
  border: none;
}

.pagination-wrap {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}
</style>
