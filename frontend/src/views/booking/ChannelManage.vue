<template>
  <div class="channel-manage-container">
    <div class="page-header">
      <h2 class="page-title">渠道管理</h2>
    </div>

    <el-card shadow="hover" class="search-card">
      <el-form :model="searchForm" inline>
        <el-form-item label="渠道名称">
          <el-input v-model="searchForm.channelName" placeholder="请输入渠道名称" clearable />
        </el-form-item>
        <el-form-item label="渠道类型">
          <el-select v-model="searchForm.channelType" placeholder="全部" clearable>
            <el-option v-for="t in channelTypes" :key="t.value" :label="t.label" :value="t.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="合作状态">
          <el-select v-model="searchForm.cooperationStatus" placeholder="全部" clearable>
            <el-option label="合作中" :value="1" />
            <el-option label="已停用" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
      <div class="action-bar">
        <el-button type="primary" @click="openDialog(null)">新增渠道</el-button>
      </div>
    </el-card>

    <el-card shadow="hover" class="table-card">
      <el-table :data="tableData" v-loading="loading" stripe>
        <el-table-column prop="channelName" label="渠道名称" min-width="120" />
        <el-table-column prop="channelCode" label="渠道代码" min-width="100" />
        <el-table-column label="渠道类型" min-width="100">
          <template #default="{ row }">
            {{ getChannelTypeLabel(row.channelType) }}
          </template>
        </el-table-column>
        <el-table-column label="合作状态" min-width="90">
          <template #default="{ row }">
            <el-tag :type="row.cooperationStatus === 1 ? 'success' : 'danger'">
              {{ row.cooperationStatus === 1 ? '合作中' : '已停用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="佣金比例(%)" min-width="100">
          <template #default="{ row }">
            {{ row.commissionRate != null ? (row.commissionRate * 100).toFixed(2) : '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="settlementCycle" label="结算周期" min-width="90" />
        <el-table-column prop="contactPerson" label="联系人" min-width="90" />
        <el-table-column prop="contactPhone" label="联系电话" min-width="120" />
        <el-table-column label="操作" min-width="200" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openDialog(row)">编辑</el-button>
            <el-button link :type="row.cooperationStatus === 1 ? 'warning' : 'success'" @click="toggleStatus(row)">
              {{ row.cooperationStatus === 1 ? '禁用' : '启用' }}
            </el-button>
            <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div class="pagination-wrapper">
        <el-pagination
          v-model:current-page="pagination.pageNum"
          v-model:page-size="pagination.pageSize"
          :total="pagination.total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="loadData"
          @current-change="loadData"
        />
      </div>
    </el-card>

    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑渠道' : '新增渠道'"
      width="560px"
      destroy-on-close
      :close-on-click-modal="false"
    >
      <el-form ref="formRef" :model="form" :rules="formRules" label-width="100px">
        <el-form-item label="渠道名称" prop="channelName">
          <el-input v-model="form.channelName" placeholder="请输入渠道名称" />
        </el-form-item>
        <el-form-item label="渠道代码" prop="channelCode">
          <el-input v-model="form.channelCode" placeholder="请输入渠道代码" />
        </el-form-item>
        <el-form-item label="渠道类型" prop="channelType">
          <el-select v-model="form.channelType" placeholder="请选择渠道类型" style="width: 100%">
            <el-option v-for="t in channelTypes" :key="t.value" :label="t.label" :value="t.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="佣金比例" prop="commissionRate">
          <el-input-number v-model="form.commissionRate" :step="0.01" :min="0" :max="1" :precision="2" style="width: 100%" />
        </el-form-item>
        <el-form-item label="结算周期" prop="settlementCycle">
          <el-select v-model="form.settlementCycle" placeholder="请选择结算周期" style="width: 100%">
            <el-option label="月结" value="月结" />
            <el-option label="半月结" value="半月结" />
            <el-option label="周结" value="周结" />
            <el-option label="日结" value="日结" />
          </el-select>
        </el-form-item>
        <el-form-item label="联系人" prop="contactPerson">
          <el-input v-model="form.contactPerson" placeholder="请输入联系人" />
        </el-form-item>
        <el-form-item label="联系电话" prop="contactPhone">
          <el-input v-model="form.contactPhone" placeholder="请输入联系电话" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="form.remark" type="textarea" :rows="3" placeholder="请输入备注" />
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
import api from '@/api'

const channelTypes = [
  { value: 1, label: 'OTA平台' },
  { value: 2, label: '官方渠道' },
  { value: 3, label: '企业协议' },
  { value: 4, label: '旅行社' },
  { value: 5, label: '其他' }
]

const getChannelTypeLabel = (type) => {
  const item = channelTypes.find(t => t.value === type)
  return item ? item.label : '未知'
}

const loading = ref(false)
const tableData = ref([])
const pagination = reactive({ pageNum: 1, pageSize: 10, total: 0 })
const searchForm = reactive({ channelName: '', channelType: null, cooperationStatus: null })

const dialogVisible = ref(false)
const isEdit = ref(false)
const saving = ref(false)
const formRef = ref(null)
const form = reactive({
  id: null,
  channelName: '',
  channelCode: '',
  channelType: null,
  commissionRate: 0,
  settlementCycle: '',
  contactPerson: '',
  contactPhone: '',
  remark: ''
})
const formRules = {
  channelName: [{ required: true, message: '请输入渠道名称', trigger: 'blur' }],
  channelCode: [{ required: true, message: '请输入渠道代码', trigger: 'blur' }],
  channelType: [{ required: true, message: '请选择渠道类型', trigger: 'change' }],
  commissionRate: [{ required: true, message: '请输入佣金比例', trigger: 'blur' }],
  settlementCycle: [{ required: true, message: '请选择结算周期', trigger: 'change' }]
}

const loadData = async () => {
  loading.value = true
  try {
    const res = await api.channel.page({
      pageNum: pagination.pageNum,
      pageSize: pagination.pageSize,
      channelName: searchForm.channelName || undefined,
      channelType: searchForm.channelType,
      cooperationStatus: searchForm.cooperationStatus
    })
    if (res.code === 200) {
      tableData.value = res.data?.records || []
      pagination.total = res.data?.total || 0
    }
  } catch {
    tableData.value = []
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  pagination.pageNum = 1
  loadData()
}

const handleReset = () => {
  searchForm.channelName = ''
  searchForm.channelType = null
  searchForm.cooperationStatus = null
  handleSearch()
}

const openDialog = (row) => {
  if (row) {
    isEdit.value = true
    form.id = row.id
    form.channelName = row.channelName
    form.channelCode = row.channelCode
    form.channelType = row.channelType
    form.commissionRate = row.commissionRate
    form.settlementCycle = row.settlementCycle
    form.contactPerson = row.contactPerson
    form.contactPhone = row.contactPhone
    form.remark = row.remark || ''
  } else {
    isEdit.value = false
    form.id = null
    form.channelName = ''
    form.channelCode = ''
    form.channelType = null
    form.commissionRate = 0
    form.settlementCycle = ''
    form.contactPerson = ''
    form.contactPhone = ''
    form.remark = ''
  }
  dialogVisible.value = true
}

const handleSave = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  saving.value = true
  try {
    const payload = { ...form }
    const res = isEdit.value
      ? await api.channel.update(payload)
      : await api.channel.add(payload)
    if (res.code === 200) {
      ElMessage.success(isEdit.value ? '编辑成功' : '新增成功')
      dialogVisible.value = false
      loadData()
    } else {
      ElMessage.error(res.message || '操作失败')
    }
  } catch {
    ElMessage.error('操作失败')
  } finally {
    saving.value = false
  }
}

const toggleStatus = async (row) => {
  const action = row.cooperationStatus === 1 ? '禁用' : '启用'
  try {
    await ElMessageBox.confirm(`确认${action}渠道【${row.channelName}】？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    const res = await api.channel.update({
      id: row.id,
      cooperationStatus: row.cooperationStatus === 1 ? 0 : 1
    })
    if (res.code === 200) {
      ElMessage.success(`${action}成功`)
      loadData()
    } else {
      ElMessage.error(res.message || '操作失败')
    }
  } catch {}
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(`确认删除渠道【${row.channelName}】？此操作不可恢复`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    const res = await api.channel.delete(row.id)
    if (res.code === 200) {
      ElMessage.success('删除成功')
      loadData()
    } else {
      ElMessage.error(res.message || '删除失败')
    }
  } catch {}
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.channel-manage-container {
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

.search-card {
  border-radius: 12px;
  margin-bottom: 16px;
}

.action-bar {
  margin-top: 12px;
}

.table-card {
  border-radius: 12px;
}

.pagination-wrapper {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}
</style>
