<template>
  <div class="customer-merge-container">
    <el-card shadow="never" class="scan-card">
      <div class="scan-row">
        <el-button type="primary" :loading="scanLoading" @click="handleScan">
          <el-icon><Search /></el-icon>查重工具
        </el-button>
        <div style="flex: 1" />
        <span class="scan-hint">手动查重：</span>
        <el-input
          v-model="manualCheck.name"
          placeholder="姓名"
          clearable
          style="width: 160px"
        />
        <el-select
          v-model="manualCheck.idType"
          placeholder="证件类型"
          clearable
          style="width: 140px"
        >
          <el-option v-for="item in idTypeOptions" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>
        <el-input
          v-model="manualCheck.idNumber"
          placeholder="证件号码"
          clearable
          style="width: 180px"
        />
        <el-button type="warning" :loading="manualLoading" @click="handleManualCheck">查重</el-button>
      </div>
    </el-card>

    <el-card shadow="never" class="table-card">
      <el-table
        :data="duplicateList"
        stripe
        border
        v-loading="tableLoading"
        style="width: 100%"
      >
        <el-table-column label="客户A姓名" min-width="100">
          <template #default="{ row }">
            {{ row.customerA?.name || '-' }}
          </template>
        </el-table-column>
        <el-table-column label="客户A手机" width="130" align="center">
          <template #default="{ row }">
            {{ row.customerA?.phone || '-' }}
          </template>
        </el-table-column>
        <el-table-column label="客户B姓名" min-width="100">
          <template #default="{ row }">
            {{ row.customerB?.name || '-' }}
          </template>
        </el-table-column>
        <el-table-column label="客户B手机" width="130" align="center">
          <template #default="{ row }">
            {{ row.customerB?.phone || '-' }}
          </template>
        </el-table-column>
        <el-table-column label="相似度" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="getSimilarityType(row.similarity)" size="small">
              {{ (row.similarity * 100).toFixed(0) }}%
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="reason" label="原因" min-width="120" />
        <el-table-column label="操作" width="120" align="center" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="openMergeDialog(row)">选择合并</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div v-if="duplicateList.length === 0 && !tableLoading" class="empty-hint">
        暂无重复客户数据，请点击"查重工具"进行扫描
      </div>
    </el-card>

    <el-dialog
      v-model="mergeDialogVisible"
      title="合并客户"
      width="960px"
      destroy-on-close
      :close-on-click-modal="false"
    >
      <el-alert
        title="合并操作不可逆，请仔细核对信息后确认"
        type="warning"
        :closable="false"
        show-icon
        style="margin-bottom: 16px"
      />
      <div v-loading="mergeDialogLoading" class="merge-compare">
        <div class="compare-side source-side">
          <div class="side-header">客户A（来源）</div>
          <div class="side-id">ID: {{ sourceCustomer?.id }}</div>
          <div v-for="field in mergeFields" :key="field.key" class="field-row">
            <div class="field-label">{{ field.label }}</div>
            <div class="field-value">
              <el-radio
                :value="field.key"
                v-model="fieldSelection[field.key]"
                @change="handleFieldSelect(field.key, 'source')"
              >
                {{ formatFieldValue(field, sourceCustomer) }}
              </el-radio>
            </div>
          </div>
        </div>
        <div class="compare-divider" />
        <div class="compare-side target-side">
          <div class="side-header">客户B（目标）</div>
          <div class="side-id">ID: {{ targetCustomer?.id }}</div>
          <div v-for="field in mergeFields" :key="field.key" class="field-row">
            <div class="field-label">{{ field.label }}</div>
            <div class="field-value">
              <el-radio
                :value="field.key"
                v-model="fieldSelection[field.key]"
                @change="handleFieldSelect(field.key, 'target')"
              >
                {{ formatFieldValue(field, targetCustomer) }}
              </el-radio>
            </div>
          </div>
        </div>
      </div>

      <el-divider />

      <div class="merge-options">
        <el-checkbox v-model="mergeTags">合并标签</el-checkbox>
        <el-checkbox v-model="mergePreferences" style="margin-left: 24px">合并偏好</el-checkbox>
      </div>

      <div class="keep-id-section">
        <span class="keep-id-label">保留客户ID：</span>
        <el-radio-group v-model="keepId">
          <el-radio value="source">客户A（ID: {{ sourceCustomer?.id }}）</el-radio>
          <el-radio value="target">客户B（ID: {{ targetCustomer?.id }}）</el-radio>
        </el-radio-group>
      </div>

      <template #footer>
        <el-button @click="mergeDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="mergeSaving" @click="handleMergeConfirm">确认合并</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search } from '@element-plus/icons-vue'
import api from '@/api'

const idTypeOptions = [
  { value: 1, label: '身份证' },
  { value: 2, label: '护照' },
  { value: 3, label: '港澳通行证' },
  { value: 4, label: '台胞证' },
  { value: 5, label: '驾驶证' }
]

const idTypeLabel = (val) => {
  const map = { 1: '身份证', 2: '护照', 3: '港澳通行证', 4: '台胞证', 5: '驾驶证' }
  return map[val] || '-'
}

const genderLabel = (val) => {
  const map = { 0: '未知', 1: '男', 2: '女' }
  return map[val] || '-'
}

const mergeFields = [
  { key: 'name', label: '姓名' },
  { key: 'gender', label: '性别' },
  { key: 'birthDate', label: '出生日期' },
  { key: 'nationality', label: '国籍' },
  { key: 'phone', label: '手机号码' },
  { key: 'backupPhone', label: '备用电话' },
  { key: 'email', label: '电子邮箱' },
  { key: 'wechat', label: '微信号' },
  { key: 'idType', label: '证件类型' },
  { key: 'idNumber', label: '证件号码' },
  { key: 'customerType', label: '客户类型' },
  { key: 'customerSource', label: '客户来源' },
  { key: 'importance', label: '重要程度' }
]

const scanLoading = ref(false)
const manualLoading = ref(false)
const tableLoading = ref(false)
const duplicateList = ref([])

const manualCheck = reactive({
  name: '',
  idType: null,
  idNumber: ''
})

const handleScan = async () => {
  scanLoading.value = true
  tableLoading.value = true
  try {
    const res = await api.customer.scanDuplicates()
    if (res.code === 200) {
      duplicateList.value = res.data || []
      ElMessage.success(`扫描完成，发现 ${duplicateList.value.length} 对重复客户`)
    } else {
      ElMessage.error(res.message || '扫描失败')
    }
  } catch {
    ElMessage.error('扫描失败')
  } finally {
    scanLoading.value = false
    tableLoading.value = false
  }
}

const handleManualCheck = async () => {
  if (!manualCheck.name && !manualCheck.idNumber) {
    ElMessage.warning('请至少输入姓名或证件号码')
    return
  }
  manualLoading.value = true
  tableLoading.value = true
  try {
    const data = {
      name: manualCheck.name || undefined,
      idNumber: manualCheck.idNumber || undefined,
      idType: manualCheck.idType || undefined
    }
    const res = await api.customer.checkDuplicate(data)
    if (res.code === 200) {
      duplicateList.value = res.data || []
      if (duplicateList.value.length === 0) {
        ElMessage.info('未发现重复客户')
      } else {
        ElMessage.success(`发现 ${duplicateList.value.length} 对重复客户`)
      }
    } else {
      ElMessage.error(res.message || '查重失败')
    }
  } catch {
    ElMessage.error('查重失败')
  } finally {
    manualLoading.value = false
    tableLoading.value = false
  }
}

const getSimilarityType = (similarity) => {
  if (similarity >= 0.9) return 'danger'
  if (similarity >= 0.7) return 'warning'
  return 'info'
}

const mergeDialogVisible = ref(false)
const mergeDialogLoading = ref(false)
const mergeSaving = ref(false)
const sourceCustomer = ref(null)
const targetCustomer = ref(null)
const fieldSelection = reactive({})
const mergeTags = ref(false)
const mergePreferences = ref(false)
const keepId = ref('source')

const formatFieldValue = (field, customer) => {
  if (!customer) return '-'
  const val = customer[field.key]
  if (val === null || val === undefined || val === '') return '-'
  if (field.key === 'idType') return idTypeLabel(val)
  if (field.key === 'gender') return genderLabel(val)
  return val
}

const handleFieldSelect = (fieldKey, side) => {
  fieldSelection[fieldKey] = side
}

const openMergeDialog = async (row) => {
  mergeDialogLoading.value = true
  mergeDialogVisible.value = true
  try {
    const sourceId = row.customerA?.id || row.sourceId
    const targetId = row.customerB?.id || row.targetId
    const [sourceRes, targetRes] = await Promise.all([
      api.customer.getById(sourceId),
      api.customer.getById(targetId)
    ])
    sourceCustomer.value = sourceRes.code === 200 ? sourceRes.data : null
    targetCustomer.value = targetRes.code === 200 ? targetRes.data : null

    mergeFields.forEach((field) => {
      fieldSelection[field.key] = 'source'
    })
    mergeTags.value = false
    mergePreferences.value = false
    keepId.value = 'source'
  } catch {
    ElMessage.error('加载客户信息失败')
    mergeDialogVisible.value = false
  } finally {
    mergeDialogLoading.value = false
  }
}

const handleMergeConfirm = async () => {
  const selectedFields = {}
  mergeFields.forEach((field) => {
    selectedFields[field.key] = fieldSelection[field.key]
  })

  const keepSide = keepId.value
  const sourceId = sourceCustomer.value?.id
  const targetId = targetCustomer.value?.id

  try {
    await ElMessageBox.confirm(
      '合并操作不可逆！合并后，未被保留的客户记录将被删除，其关联数据将转移至保留的客户。是否确认合并？',
      '合并确认',
      {
        confirmButtonText: '确认合并',
        cancelButtonText: '取消',
        type: 'warning',
        confirmButtonClass: 'el-button--danger'
      }
    )
  } catch {
    return
  }

  mergeSaving.value = true
  try {
    const payload = {
      sourceId,
      targetId,
      fieldSelection: selectedFields,
      mergeTags: mergeTags.value,
      mergePreferences: mergePreferences.value,
      keepId: keepSide === 'source' ? sourceId : targetId
    }
    const res = await api.customer.mergeCustomers(payload)
    if (res.code === 200) {
      ElMessage.success('合并成功')
      mergeDialogVisible.value = false
      handleScan()
    } else {
      ElMessage.error(res.message || '合并失败')
    }
  } catch {
    ElMessage.error('合并失败')
  } finally {
    mergeSaving.value = false
  }
}
</script>

<style scoped>
.customer-merge-container {
  padding: 10px;
}

.scan-card {
  border-radius: 12px;
  border: none;
  margin-bottom: 12px;
}

.scan-row {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}

.scan-hint {
  font-size: 14px;
  color: #606266;
}

.table-card {
  border-radius: 12px;
  border: none;
}

.empty-hint {
  text-align: center;
  padding: 40px 0;
  color: #909399;
  font-size: 14px;
}

.merge-compare {
  display: flex;
  gap: 0;
}

.compare-side {
  flex: 1;
  min-width: 0;
}

.side-header {
  font-size: 15px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 4px;
}

.source-side .side-header {
  color: #409eff;
}

.target-side .side-header {
  color: #67c23a;
}

.side-id {
  font-size: 12px;
  color: #909399;
  margin-bottom: 12px;
}

.compare-divider {
  width: 1px;
  background: #dcdfe6;
  margin: 0 20px;
  align-self: stretch;
}

.field-row {
  display: flex;
  align-items: center;
  min-height: 36px;
  border-bottom: 1px solid #f2f3f5;
}

.field-label {
  width: 80px;
  flex-shrink: 0;
  font-size: 13px;
  color: #909399;
}

.field-value {
  flex: 1;
  min-width: 0;
  font-size: 13px;
  color: #303133;
}

.field-value :deep(.el-radio) {
  height: auto;
  white-space: normal;
  word-break: break-all;
}

.field-value :deep(.el-radio__label) {
  white-space: normal;
  word-break: break-all;
  line-height: 1.4;
}

.merge-options {
  display: flex;
  align-items: center;
  margin-bottom: 16px;
}

.keep-id-section {
  display: flex;
  align-items: center;
}

.keep-id-label {
  font-size: 14px;
  color: #303133;
  font-weight: 500;
  margin-right: 12px;
}
</style>
