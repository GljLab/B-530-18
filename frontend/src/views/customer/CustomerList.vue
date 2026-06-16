<template>
  <div class="customer-list-container">
    <el-card shadow="never" class="search-card">
      <div class="search-row">
        <el-input
          v-model="queryParams.keyword"
          placeholder="姓名/手机号/证件号"
          clearable
          style="width: 240px"
          @keyup.enter="handleSearch"
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
        <el-button type="primary" @click="handleSearch">
          <el-icon><Search /></el-icon>搜索
        </el-button>
        <el-button @click="handleReset">重置</el-button>
        <el-button type="text" @click="advancedFilterVisible = !advancedFilterVisible">
          {{ advancedFilterVisible ? '收起筛选' : '高级筛选' }}
          <el-icon>
            <ArrowUp v-if="advancedFilterVisible" />
            <ArrowDown v-else />
          </el-icon>
        </el-button>
        <div style="flex: 1" />
        <el-button v-if="hasPermission('customer:add')" type="primary" @click="handleCreate">
          <el-icon><Plus /></el-icon>新建客户
        </el-button>
        <el-button v-if="hasPermission('customer:export')" @click="openExportDialog">
          <el-icon><Download /></el-icon>导出
        </el-button>
        <el-button v-if="hasPermission('customer:dedup')" @click="router.push('/customer/merge')">
          <el-icon><CopyDocument /></el-icon>查重
        </el-button>
      </div>
      <div v-if="advancedFilterVisible" class="advanced-filter">
        <el-select
          v-model="queryParams.customerType"
          placeholder="客户类型"
          multiple
          clearable
          collapse-tags
          style="width: 220px"
        >
          <el-option v-for="ct in customerTypeOptions" :key="ct.value" :label="ct.label" :value="ct.value" />
        </el-select>
        <el-select
          v-model="queryParams.customerSource"
          placeholder="客户来源"
          multiple
          clearable
          collapse-tags
          style="width: 220px"
        >
          <el-option v-for="cs in customerSourceOptions" :key="cs.value" :label="cs.label" :value="cs.value" />
        </el-select>
        <el-select
          v-model="queryParams.status"
          placeholder="状态"
          clearable
          style="width: 140px"
        >
          <el-option v-for="s in statusOptions" :key="s.value" :label="s.label" :value="s.value" />
        </el-select>
        <el-select
          v-model="queryParams.importance"
          placeholder="重要程度"
          multiple
          clearable
          collapse-tags
          style="width: 200px"
        >
          <el-option v-for="imp in importanceOptions" :key="imp.value" :label="imp.label" :value="imp.value" />
        </el-select>
        <el-date-picker
          v-model="queryParams.registerDateRange"
          type="daterange"
          range-separator="至"
          start-placeholder="注册开始日期"
          end-placeholder="注册结束日期"
          value-format="YYYY-MM-DD"
          style="width: 280px"
        />
        <el-select
          v-model="queryParams.tagIds"
          placeholder="按标签筛选"
          multiple
          clearable
          collapse-tags
          style="width: 240px"
        >
          <el-option v-for="tag in tagList" :key="tag.id" :label="tag.name" :value="tag.id" />
        </el-select>
        <el-radio-group v-model="queryParams.tagLogic" size="small" style="margin-left: 4px">
          <el-radio-button value="OR">OR</el-radio-button>
          <el-radio-button value="AND">AND</el-radio-button>
        </el-radio-group>
      </div>
    </el-card>

    <el-card shadow="never" class="table-card">
      <el-table
        :data="tableData"
        stripe
        border
        v-loading="tableLoading"
        style="width: 100%"
        @sort-change="handleSortChange"
      >
        <el-table-column label="头像" width="70" align="center">
          <template #default="{ row }">
            <el-avatar :size="36" :src="row.avatar" />
          </template>
        </el-table-column>
        <el-table-column prop="name" label="姓名" min-width="100">
          <template #default="{ row }">
            <el-icon v-if="row.status === 3" class="blacklist-warning"><Warning /></el-icon>
            <el-icon v-if="row.hasImportantNote" class="important-note-bell"><Bell /></el-icon>
            <span>{{ row.name }}</span>
            <el-icon v-if="row.importance === 3" class="vip-crown"><GoldMedal /></el-icon>
          </template>
        </el-table-column>
        <el-table-column prop="phone" label="手机号" width="130" align="center" />
        <el-table-column label="客户类型" width="110" align="center">
          <template #default="{ row }">
            <el-tag size="small" :type="customerTypeTagType(row.customerType)">
              {{ customerTypeLabel(row.customerType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="客户来源" width="110" align="center">
          <template #default="{ row }">
            {{ customerSourceLabel(row.customerSource) }}
          </template>
        </el-table-column>
        <el-table-column label="标签" min-width="160" align="center">
          <template #default="{ row }">
            <template v-if="row.tags && row.tags.length > 0">
              <el-tag
                v-for="(tag, idx) in row.tags.slice(0, 3)"
                :key="tag.id || tag.tagId"
                size="small"
                :type="tagColorTypes[idx % tagColorTypes.length]"
                style="margin: 2px"
              >{{ tag.name || tag.tagName }}</el-tag>
              <el-tag v-if="row.tags.length > 3" size="small" type="info" style="margin: 2px">
                +{{ row.tags.length - 3 }}
              </el-tag>
            </template>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column label="累计消费" width="120" align="right" sortable="custom" prop="totalSpent">
          <template #default="{ row }">
            <span v-if="hasPermission('customer:finance:view')" class="amount-text">
              ¥{{ formatAmount(row.totalSpent) }}
            </span>
            <span v-else>***</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="90" align="center">
          <template #default="{ row }">
            <el-tag size="small" :type="statusTagType(row.status)">
              {{ statusLabel(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="注册时间" width="170" align="center" sortable="custom" prop="registerTime" />
        <el-table-column label="操作" width="200" align="center" fixed="right">
          <template #default="{ row }">
            <el-button
              v-if="hasPermission('customer:query')"
              type="primary"
              link
              size="small"
              @click="handleViewDetail(row)"
            >查看详情</el-button>
            <el-button
              v-if="hasPermission('customer:edit')"
              type="primary"
              link
              size="small"
              @click="handleEdit(row)"
            >编辑</el-button>
            <el-button
              v-if="row.status === 1 && hasPermission('customer:freeze')"
              type="danger"
              link
              size="small"
              @click="openFreezeDialog(row)"
            >冻结</el-button>
            <el-button
              v-if="row.status === 2 && hasPermission('customer:unfreeze')"
              type="success"
              link
              size="small"
              @click="openUnfreezeDialog(row)"
            >解冻</el-button>
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

    <el-dialog
      v-model="freezeDialogVisible"
      :title="freezeForm.action === 'freeze' ? '冻结客户' : '解冻客户'"
      width="480px"
      destroy-on-close
    >
      <el-alert
        v-if="freezeForm.action === 'freeze'"
        title="冻结后该客户将无法进行预订和入住操作，请谨慎操作！"
        type="warning"
        :closable="false"
        show-icon
        style="margin-bottom: 16px"
      />
      <el-form ref="freezeFormRef" :model="freezeForm" :rules="freezeRules" label-width="80px">
        <el-form-item label="客户姓名">
          <span>{{ freezeForm.customerName }}</span>
        </el-form-item>
        <el-form-item label="原因" prop="reason">
          <el-input
            v-model="freezeForm.reason"
            type="textarea"
            :rows="4"
            :placeholder="freezeForm.action === 'freeze' ? '请输入冻结原因' : '请输入解冻原因'"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="freezeDialogVisible = false">取消</el-button>
        <el-button
          :type="freezeForm.action === 'freeze' ? 'danger' : 'success'"
          :loading="freezeSaving"
          @click="handleFreezeSubmit"
        >确认{{ freezeForm.action === 'freeze' ? '冻结' : '解冻' }}</el-button>
      </template>
    </el-dialog>

    <el-dialog
      v-model="exportDialogVisible"
      title="导出客户数据"
      width="520px"
      destroy-on-close
    >
      <el-form label-width="100px">
        <el-form-item label="导出范围">
          <el-radio-group v-model="exportForm.range">
            <el-radio value="filter">当前筛选</el-radio>
            <el-radio value="all">全部</el-radio>
            <el-radio value="custom">自定义</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item v-if="exportForm.range === 'custom'" label="自定义范围">
          <el-date-picker
            v-model="exportForm.customRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            value-format="YYYY-MM-DD"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="导出字段">
          <el-checkbox-group v-model="exportForm.fields">
            <el-checkbox value="basic" label="基础信息" />
            <el-checkbox value="idcard" label="证件" />
            <el-checkbox value="contact" label="联系方式" />
            <el-checkbox value="category" label="分类" />
            <el-checkbox value="statistics" label="统计" />
            <el-checkbox value="tags" label="标签" />
            <el-checkbox value="preference" label="偏好" />
          </el-checkbox-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="exportDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="exportLoading" @click="handleExport">确认导出</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Plus, ArrowUp, ArrowDown, GoldMedal, Warning, Bell, Download, CopyDocument } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import api from '@/api'

const router = useRouter()
const userStore = useUserStore()
const hasPermission = (p) => userStore.hasPermission(p)

const customerTypeOptions = [
  { value: 1, label: '散客' },
  { value: 2, label: '企业客户' },
  { value: 3, label: '协议客户' },
  { value: 4, label: 'VIP客户' }
]

const customerSourceOptions = [
  { value: 1, label: '官网' },
  { value: 2, label: 'OTA平台' },
  { value: 3, label: '企业协议' },
  { value: 4, label: '电话预订' },
  { value: 5, label: '前台登记' },
  { value: 6, label: '老客户推荐' }
]

const statusOptions = [
  { value: 1, label: '正常' },
  { value: 2, label: '冻结' },
  { value: 3, label: '黑名单' }
]

const importanceOptions = [
  { value: 1, label: '普通' },
  { value: 2, label: '重要' },
  { value: 3, label: 'VIP' }
]

const customerTypeLabel = (val) => {
  const map = { 1: '散客', 2: '企业客户', 3: '协议客户', 4: 'VIP客户' }
  return map[val] || '-'
}

const customerTypeTagType = (val) => {
  const map = { 1: 'info', 2: '', 3: 'warning', 4: 'danger' }
  return map[val] || 'info'
}

const customerSourceLabel = (val) => {
  const map = { 1: '官网', 2: 'OTA平台', 3: '企业协议', 4: '电话预订', 5: '前台登记', 6: '老客户推荐' }
  return map[val] || '-'
}

const statusTagType = (val) => {
  const map = { 1: 'success', 2: 'danger', 3: 'danger' }
  return map[val] || 'info'
}

const statusLabel = (val) => {
  const map = { 1: '正常', 2: '冻结', 3: '黑名单' }
  return map[val] || '-'
}

const tagColorTypes = ['', 'success', 'warning', 'danger', 'info']

const formatAmount = (val) => {
  if (val === null || val === undefined) return '0.00'
  return Number(val).toFixed(2).replace(/\B(?=(\d{3})+(?!\d))/g, ',')
}

const advancedFilterVisible = ref(false)
const tableData = ref([])
const total = ref(0)
const tableLoading = ref(false)
const tagList = ref([])

const queryParams = reactive({
  keyword: '',
  customerType: [],
  customerSource: [],
  status: null,
  importance: [],
  registerDateRange: null,
  tagIds: [],
  tagLogic: 'OR',
  pageNum: 1,
  pageSize: 10,
  orderBy: '',
  orderDir: ''
})

const loadData = async () => {
  tableLoading.value = true
  try {
    const params = {
      pageNum: queryParams.pageNum,
      pageSize: queryParams.pageSize,
      keyword: queryParams.keyword || undefined,
      status: queryParams.status || undefined,
      orderBy: queryParams.orderBy || undefined,
      orderDir: queryParams.orderDir || undefined
    }
    if (queryParams.customerType.length > 0) {
      params.customerType = queryParams.customerType.join(',')
    }
    if (queryParams.customerSource.length > 0) {
      params.customerSource = queryParams.customerSource.join(',')
    }
    if (queryParams.importance.length > 0) {
      params.importance = queryParams.importance.join(',')
    }
    if (queryParams.registerDateRange && queryParams.registerDateRange.length === 2) {
      params.createTimeStart = queryParams.registerDateRange[0]
      params.createTimeEnd = queryParams.registerDateRange[1]
    }
    if (queryParams.tagIds.length > 0) {
      params.tagIds = queryParams.tagIds.join(',')
      params.tagLogic = queryParams.tagLogic
    }
    const res = await api.customer.getPage(params)
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
  queryParams.keyword = ''
  queryParams.customerType = []
  queryParams.customerSource = []
  queryParams.status = null
  queryParams.importance = []
  queryParams.registerDateRange = null
  queryParams.tagIds = []
  queryParams.tagLogic = 'OR'
  queryParams.pageNum = 1
  queryParams.orderBy = ''
  queryParams.orderDir = ''
  loadData()
}

const handleSortChange = ({ prop, order }) => {
  queryParams.orderBy = prop || ''
  queryParams.orderDir = order === 'ascending' ? 'asc' : order === 'descending' ? 'desc' : ''
  loadData()
}

const handleCreate = () => {
  router.push('/customer/create')
}

const handleViewDetail = (row) => {
  router.push(`/customer/detail/${row.id}`)
}

const handleEdit = (row) => {
  router.push(`/customer/edit/${row.id}`)
}

const freezeDialogVisible = ref(false)
const freezeSaving = ref(false)
const freezeFormRef = ref(null)
const freezeForm = reactive({
  customerId: null,
  customerName: '',
  action: 'freeze',
  reason: ''
})

const freezeRules = {
  reason: [{ required: true, message: '请输入原因', trigger: 'blur' }]
}

const openFreezeDialog = (row) => {
  freezeForm.customerId = row.id
  freezeForm.customerName = row.name
  freezeForm.action = 'freeze'
  freezeForm.reason = ''
  freezeDialogVisible.value = true
}

const openUnfreezeDialog = (row) => {
  freezeForm.customerId = row.id
  freezeForm.customerName = row.name
  freezeForm.action = 'unfreeze'
  freezeForm.reason = ''
  freezeDialogVisible.value = true
}

const handleFreezeSubmit = async () => {
  const valid = await freezeFormRef.value.validate().catch(() => false)
  if (!valid) return
  freezeSaving.value = true
  try {
    const payload = { reason: freezeForm.reason }
    const res = freezeForm.action === 'freeze'
      ? await api.customer.freeze(freezeForm.customerId, payload)
      : await api.customer.unfreeze(freezeForm.customerId, payload)
    if (res.code === 200) {
      ElMessage.success(freezeForm.action === 'freeze' ? '冻结成功' : '解冻成功')
      freezeDialogVisible.value = false
      loadData()
    } else {
      ElMessage.error(res.message || '操作失败')
    }
  } catch {
    ElMessage.error('操作失败')
  } finally {
    freezeSaving.value = false
  }
}

const exportDialogVisible = ref(false)
const exportLoading = ref(false)
const exportForm = reactive({
  range: 'filter',
  customRange: null,
  fields: ['basic', 'contact', 'category']
})

const openExportDialog = () => {
  exportForm.range = 'filter'
  exportForm.customRange = null
  exportForm.fields = ['basic', 'contact', 'category']
  exportDialogVisible.value = true
}

const handleExport = async () => {
  exportLoading.value = true
  try {
    const data = { fields: exportForm.fields }
    if (exportForm.range === 'filter') {
      data.status = queryParams.status || undefined
      if (queryParams.customerType.length > 0) data.customerType = queryParams.customerType.join(',')
      if (queryParams.customerSource.length > 0) data.customerSource = queryParams.customerSource.join(',')
      if (queryParams.importance.length > 0) data.importance = queryParams.importance.join(',')
      if (queryParams.tagIds.length > 0) {
        data.tagIds = queryParams.tagIds.join(',')
        data.tagLogic = queryParams.tagLogic
      }
      if (queryParams.keyword) data.keyword = queryParams.keyword
    } else if (exportForm.range === 'custom' && exportForm.customRange) {
      data.createTimeStart = exportForm.customRange[0]
      data.createTimeEnd = exportForm.customRange[1]
    }
    const res = await api.customer.exportCustomers(data)
    const blob = new Blob([res], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' })
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = `客户数据_${new Date().toISOString().slice(0, 10)}.xlsx`
    link.click()
    window.URL.revokeObjectURL(url)
    ElMessage.success('导出成功')
    exportDialogVisible.value = false
  } catch {
    ElMessage.error('导出失败')
  } finally {
    exportLoading.value = false
  }
}

const loadTagList = async () => {
  try {
    const res = await api.customer.getTagList()
    if (res.code === 200) {
      tagList.value = res.data || []
    }
  } catch {
    tagList.value = []
  }
}

onMounted(() => {
  loadData()
  loadTagList()
})
</script>

<style scoped>
.customer-list-container {
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

.advanced-filter {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
  margin-top: 12px;
  padding-top: 12px;
  border-top: 1px solid #ebeef5;
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

.vip-crown {
  color: #e6a23c;
  margin-left: 4px;
  vertical-align: middle;
}

.blacklist-warning {
  color: #f56c6c;
  margin-right: 4px;
  vertical-align: middle;
}

.important-note-bell {
  color: #f56c6c;
  margin-right: 2px;
  vertical-align: middle;
  font-size: 14px;
}

.amount-text {
  font-weight: 600;
  color: #409eff;
}
</style>
