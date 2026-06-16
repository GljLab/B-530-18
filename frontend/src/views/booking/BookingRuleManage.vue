<template>
  <div class="booking-rule-container">
    <div class="page-header">
      <h2 class="page-title">预订规则管理</h2>
    </div>

    <el-card shadow="hover" class="filter-card">
      <div class="filter-row">
        <el-input v-model="queryParams.name" placeholder="规则名称" clearable style="width: 180px" @keyup.enter="handleSearch" />
        <el-select v-model="queryParams.ruleType" placeholder="规则类型" clearable style="width: 180px">
          <el-option v-for="opt in ruleTypeOptions" :key="opt.value" :label="opt.label" :value="opt.value" />
        </el-select>
        <el-select v-model="queryParams.enabled" placeholder="启用状态" clearable style="width: 140px">
          <el-option label="已启用" :value="1" />
          <el-option label="已禁用" :value="0" />
        </el-select>
        <el-button type="primary" @click="handleSearch">
          <el-icon><Search /></el-icon>查询
        </el-button>
        <el-button @click="handleReset">
          <el-icon><Refresh /></el-icon>重置
        </el-button>
        <el-button type="primary" @click="openCreateDialog">
          <el-icon><Plus /></el-icon>新增规则
        </el-button>
      </div>
    </el-card>

    <el-card shadow="hover" class="table-card">
      <el-table :data="tableData" stripe border v-loading="tableLoading" style="width: 100%">
        <el-table-column prop="ruleName" label="规则名称" min-width="140" />
        <el-table-column label="规则类型" width="140" align="center">
          <template #default="{ row }">
            {{ getRuleTypeLabel(row.ruleType) }}
          </template>
        </el-table-column>
        <el-table-column prop="priority" label="优先级" width="100" align="center" />
        <el-table-column label="启用状态" width="100" align="center">
          <template #default="{ row }">
            <el-switch
              :model-value="!!row.enabled"
              @change="(val) => handleToggle(row, val)"
            />
          </template>
        </el-table-column>
        <el-table-column label="适用房型" width="160">
          <template #default="{ row }">
            <span>{{ formatRoomTypes(row.applyRoomTypes) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="适用日期" width="200">
          <template #default="{ row }">
            <span>{{ row.applyDateStart && row.applyDateEnd ? `${row.applyDateStart} 至 ${row.applyDateEnd}` : '全部' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="160" align="center" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="openEditDialog(row)">编辑</el-button>
            <el-button type="danger" link size="small" @click="handleDelete(row)">删除</el-button>
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

    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑规则' : '新增规则'" width="640px" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="formRules" label-width="120px">
        <el-form-item label="规则名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入规则名称" maxlength="50" />
        </el-form-item>
        <el-form-item label="规则类型" prop="ruleType">
          <el-select v-model="form.ruleType" placeholder="请选择规则类型" style="width: 100%">
            <el-option v-for="opt in ruleTypeOptions" :key="opt.value" :label="opt.label" :value="opt.value" />
          </el-select>
        </el-form-item>

        <el-form-item v-if="form.ruleType === 1" label="最短入住天数" prop="minDays">
          <el-input-number v-model="form.minDays" :min="1" :max="30" style="width: 100%" />
        </el-form-item>
        <el-form-item v-if="form.ruleType === 2" label="最长入住天数" prop="maxDays">
          <el-input-number v-model="form.maxDays" :min="1" :max="365" style="width: 100%" />
        </el-form-item>
        <el-form-item v-if="form.ruleType === 3" label="提前预订天数" prop="advanceDays">
          <el-input-number v-model="form.advanceDays" :min="0" :max="365" style="width: 100%" />
        </el-form-item>
        <el-form-item v-if="form.ruleType === 4" label="最晚预订时间" prop="latestTime">
          <el-time-picker v-model="form.latestTime" placeholder="请选择时间" value-format="HH:mm" style="width: 100%" />
        </el-form-item>
        <el-form-item v-if="form.ruleType === 5" label="限制日期" prop="restrictedDates">
          <el-date-picker
            v-model="form.restrictedDates"
            type="dates"
            placeholder="请选择日期"
            value-format="YYYY-MM-DD"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item v-if="form.ruleType === 6" label="限制房型" prop="restrictedRoomTypes">
          <el-select v-model="form.restrictedRoomTypes" multiple placeholder="请选择房型" style="width: 100%">
            <el-option v-for="rt in roomTypes" :key="rt.id" :label="rt.typeName" :value="rt.id" />
          </el-select>
        </el-form-item>
        <el-form-item v-if="form.ruleType === 7" label="允许客户类型" prop="allowedCustomerTypes">
          <el-select v-model="form.allowedCustomerTypes" multiple placeholder="请选择客户类型" style="width: 100%">
            <el-option v-for="ct in customerTypeOptions" :key="ct" :label="ct" :value="ct" />
          </el-select>
        </el-form-item>

        <el-form-item label="优先级" prop="priority">
          <el-input-number v-model="form.priority" :min="1" :max="100" style="width: 100%" />
        </el-form-item>
        <el-form-item label="适用房型" prop="applicableRoomTypes">
          <el-select v-model="form.applicableRoomTypes" multiple placeholder="请选择适用房型" style="width: 100%">
            <el-option v-for="rt in roomTypes" :key="rt.id" :label="rt.typeName" :value="rt.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="适用日期范围" prop="applicableDateRange">
          <el-date-picker
            v-model="form.applicableDateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            value-format="YYYY-MM-DD"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="适用预订来源" prop="applicableSources">
          <el-select v-model="form.applicableSources" multiple placeholder="请选择预订来源" style="width: 100%">
            <el-option v-for="src in sourceOptions" :key="src.value" :label="src.label" :value="src.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="规则描述" prop="description">
          <el-input v-model="form.description" type="textarea" :rows="3" placeholder="请输入规则描述" maxlength="300" show-word-limit />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="handleSave">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh, Plus } from '@element-plus/icons-vue'
import api from '@/api'

const ruleTypeOptions = [
  { value: 1, label: '最短入住天数' },
  { value: 2, label: '最长入住天数' },
  { value: 3, label: '提前预订天数' },
  { value: 4, label: '最晚预订时间' },
  { value: 5, label: '连住限制' },
  { value: 6, label: '房型限制' },
  { value: 7, label: '客户类型限制' }
]

const customerTypeOptions = ['散客', '团队', '会员', '协议客户']

const sourceOptions = [
  { value: 'online', label: '线上预订' },
  { value: 'offline', label: '线下预订' },
  { value: 'phone', label: '电话预订' },
  { value: 'walkin', label: '散客' },
  { value: 'ota', label: 'OTA平台' },
  { value: 'member', label: '会员预订' }
]

const roomTypes = ref([])
const tableData = ref([])
const total = ref(0)
const tableLoading = ref(false)

const queryParams = reactive({
  name: '',
  ruleType: null,
  enabled: null,
  pageNum: 1,
  pageSize: 20
})

const dialogVisible = ref(false)
const isEdit = ref(false)
const saving = ref(false)
const formRef = ref(null)

const form = reactive({
  id: null,
  name: '',
  ruleType: null,
  minDays: 1,
  maxDays: 30,
  advanceDays: 0,
  latestTime: '',
  restrictedDates: [],
  restrictedRoomTypes: [],
  allowedCustomerTypes: [],
  priority: 10,
  applicableRoomTypes: [],
  applicableDateRange: [],
  applicableSources: [],
  description: ''
})

const formRules = {
  name: [{ required: true, message: '请输入规则名称', trigger: 'blur' }],
  ruleType: [{ required: true, message: '请选择规则类型', trigger: 'change' }],
  priority: [{ required: true, message: '请输入优先级', trigger: 'blur' }]
}

const getRuleTypeLabel = (type) => {
  const opt = ruleTypeOptions.find(o => o.value === type)
  return opt ? opt.label : '-'
}

const formatRoomTypes = (ids) => {
  if (!ids) return '全部'
  const idArr = typeof ids === 'string' ? ids.split(',') : (Array.isArray(ids) ? ids : [])
  if (idArr.length === 0) return '全部'
  const names = idArr.map(id => {
    const rt = roomTypes.value.find(r => String(r.id) === String(id))
    return rt ? rt.typeName : id
  })
  return names.join('、')
}

const formatDateRange = (range) => {
  if (!range || !Array.isArray(range) || range.length !== 2) return '全部'
  return `${range[0]} 至 ${range[1]}`
}

const loadRoomTypes = async () => {
  try {
    const res = await api.hotel.getRoomTypes()
    if (res.code === 200) {
      roomTypes.value = res.data || []
    }
  } catch {
    roomTypes.value = []
  }
}

const loadData = async () => {
  tableLoading.value = true
  try {
    const res = await api.bookingRule.page(queryParams)
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
  queryParams.name = ''
  queryParams.ruleType = null
  queryParams.enabled = null
  queryParams.pageNum = 1
  loadData()
}

const resetForm = () => {
  form.id = null
  form.name = ''
  form.ruleType = null
  form.minDays = 1
  form.maxDays = 30
  form.advanceDays = 0
  form.latestTime = ''
  form.restrictedDates = []
  form.restrictedRoomTypes = []
  form.allowedCustomerTypes = []
  form.priority = 10
  form.applicableRoomTypes = []
  form.applicableDateRange = []
  form.applicableSources = []
  form.description = ''
}

const openCreateDialog = () => {
  isEdit.value = false
  resetForm()
  dialogVisible.value = true
}

const openEditDialog = (row) => {
  isEdit.value = true
  form.id = row.id
  form.name = row.ruleName || ''
  form.ruleType = row.ruleType
  const params = parseRuleParamsStr(row.ruleParams)
  form.minDays = params.minDays ?? 1
  form.maxDays = params.maxDays ?? 30
  form.advanceDays = params.advanceDays ?? 0
  form.latestTime = params.latestTime || ''
  form.restrictedDates = params.restrictedDates ? params.restrictedDates.split(',') : []
  form.restrictedRoomTypes = params.restrictedRoomTypes ? params.restrictedRoomTypes.split(',').map(Number) : []
  form.allowedCustomerTypes = params.allowedCustomerTypes ? params.allowedCustomerTypes.split(',') : []
  form.priority = row.priority ?? 10
  form.applicableRoomTypes = row.applyRoomTypes ? row.applyRoomTypes.split(',').map(Number) : []
  form.applicableDateRange = row.applyDateStart && row.applyDateEnd ? [row.applyDateStart, row.applyDateEnd] : []
  form.applicableSources = row.applySources ? row.applySources.split(',') : []
  form.description = row.description || ''
  dialogVisible.value = true
}

const parseRuleParamsStr = (ruleParams) => {
  if (!ruleParams) return {}
  try {
    return JSON.parse(ruleParams)
  } catch {
    return {}
  }
}

const buildPayload = () => {
  const payload = {
    ruleName: form.name,
    ruleType: form.ruleType,
    priority: form.priority,
    applyRoomTypes: Array.isArray(form.applicableRoomTypes) ? form.applicableRoomTypes.join(',') : '',
    applySources: Array.isArray(form.applicableSources) ? form.applicableSources.join(',') : '',
    description: form.description,
    enabled: 1
  }

  const ruleParams = {}
  if (form.ruleType === 1) ruleParams.minDays = form.minDays
  if (form.ruleType === 2) ruleParams.maxDays = form.maxDays
  if (form.ruleType === 3) ruleParams.advanceDays = form.advanceDays
  if (form.ruleType === 4) ruleParams.latestTime = form.latestTime
  if (form.ruleType === 5) ruleParams.restrictedDates = Array.isArray(form.restrictedDates) ? form.restrictedDates.join(',') : ''
  if (form.ruleType === 6) ruleParams.restrictedRoomTypes = Array.isArray(form.restrictedRoomTypes) ? form.restrictedRoomTypes.join(',') : ''
  if (form.ruleType === 7) ruleParams.allowedCustomerTypes = Array.isArray(form.allowedCustomerTypes) ? form.allowedCustomerTypes.join(',') : ''
  payload.ruleParams = JSON.stringify(ruleParams)

  if (Array.isArray(form.applicableDateRange) && form.applicableDateRange.length === 2) {
    payload.applyDateStart = form.applicableDateRange[0]
    payload.applyDateEnd = form.applicableDateRange[1]
  }

  if (isEdit.value) payload.id = form.id

  return payload
}

const handleSave = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  saving.value = true
  try {
    const payload = buildPayload()
    const res = isEdit.value
      ? await api.bookingRule.update(payload)
      : await api.bookingRule.add(payload)
    if (res.code === 200) {
      ElMessage.success(isEdit.value ? '修改成功' : '新增成功')
      dialogVisible.value = false
      await loadData()
    } else {
      ElMessage.error(res.message || '操作失败')
    }
  } catch {
    ElMessage.error('操作失败')
  } finally {
    saving.value = false
  }
}

const handleToggle = async (row, val) => {
  try {
    const res = await api.bookingRule.toggle(row.id, { enabled: val ? 1 : 0 })
    if (res.code === 200) {
      ElMessage.success(val ? '已启用' : '已禁用')
      await loadData()
    } else {
      ElMessage.error(res.message || '操作失败')
    }
  } catch {
    ElMessage.error('操作失败')
  }
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(`确认删除规则「${row.ruleName}」？`, '提示', { type: 'warning' })
    const res = await api.bookingRule.delete(row.id)
    if (res.code === 200) {
      ElMessage.success('删除成功')
      await loadData()
    } else {
      ElMessage.error(res.message || '删除失败')
    }
  } catch {}
}

onMounted(async () => {
  await loadRoomTypes()
  await loadData()
})
</script>

<style scoped>
.booking-rule-container {
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
</style>
