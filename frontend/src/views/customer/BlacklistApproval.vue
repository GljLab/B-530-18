<template>
  <div class="blacklist-approval-container">
    <el-card shadow="never" class="tabs-card">
      <el-tabs v-model="activeTab" @tab-change="handleTabChange">
        <el-tab-pane label="待审批申请" name="pending">
          <el-table
            :data="pendingList"
            stripe
            border
            v-loading="pendingLoading"
            style="width: 100%"
          >
            <el-table-column prop="customerName" label="客户姓名" min-width="100" />
            <el-table-column label="原因" min-width="120">
              <template #default="{ row }">
                {{ reasonMap[row.reason] || '-' }}
              </template>
            </el-table-column>
            <el-table-column label="类型" width="100" align="center">
              <template #default="{ row }">
                <el-tag size="small" :type="row.blacklistType === 2 ? 'danger' : 'warning'">
                  {{ typeMap[row.blacklistType] || '-' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="applicantName" label="申请人" width="110" align="center" />
            <el-table-column prop="applyTime" label="申请时间" width="170" align="center" />
            <el-table-column label="操作" width="180" align="center" fixed="right">
              <template #default="{ row }">
                <el-button type="success" link size="small" @click="openApproveDialog(row, 'add')">审批通过</el-button>
                <el-button type="danger" link size="small" @click="openRejectDialog(row, 'add')">审批拒绝</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>

        <el-tab-pane label="待解除申请" name="pendingRemove">
          <el-table
            :data="pendingRemoveList"
            stripe
            border
            v-loading="pendingRemoveLoading"
            style="width: 100%"
          >
            <el-table-column prop="customerName" label="客户姓名" min-width="100" />
            <el-table-column label="原因" min-width="120">
              <template #default="{ row }">
                {{ reasonMap[row.reason] || '-' }}
              </template>
            </el-table-column>
            <el-table-column label="类型" width="100" align="center">
              <template #default="{ row }">
                <el-tag size="small" :type="row.blacklistType === 2 ? 'danger' : 'warning'">
                  {{ typeMap[row.blacklistType] || '-' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="removeApplicantName" label="解除申请人" width="120" align="center" />
            <el-table-column prop="removeApplyTime" label="解除申请时间" width="170" align="center" />
            <el-table-column label="操作" width="180" align="center" fixed="right">
              <template #default="{ row }">
                <el-button type="success" link size="small" @click="openApproveDialog(row, 'remove')">通过</el-button>
                <el-button type="danger" link size="small" @click="openRejectDialog(row, 'remove')">拒绝</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>
      </el-tabs>
    </el-card>

    <el-dialog
      v-model="approveDialogVisible"
      title="审批通过"
      width="480px"
      destroy-on-close
    >
      <el-form ref="approveFormRef" :model="approveForm" label-width="80px">
        <el-form-item label="客户姓名">
          <span>{{ approveForm.customerName }}</span>
        </el-form-item>
        <el-form-item label="审批意见" prop="opinion">
          <el-input
            v-model="approveForm.opinion"
            type="textarea"
            :rows="4"
            placeholder="请输入审批意见（选填）"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="approveDialogVisible = false">取消</el-button>
        <el-button type="success" :loading="approveSaving" @click="handleApproveSubmit">确认通过</el-button>
      </template>
    </el-dialog>

    <el-dialog
      v-model="rejectDialogVisible"
      title="审批拒绝"
      width="480px"
      destroy-on-close
    >
      <el-form ref="rejectFormRef" :model="rejectForm" :rules="rejectRules" label-width="80px">
        <el-form-item label="客户姓名">
          <span>{{ rejectForm.customerName }}</span>
        </el-form-item>
        <el-form-item label="拒绝原因" prop="reason">
          <el-input
            v-model="rejectForm.reason"
            type="textarea"
            :rows="4"
            placeholder="请输入拒绝原因"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="rejectDialogVisible = false">取消</el-button>
        <el-button type="danger" :loading="rejectSaving" @click="handleRejectSubmit">确认拒绝</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import api from '@/api'

const reasonMap = {
  1: '拖欠房费',
  2: '破坏设施',
  3: '扰乱秩序',
  4: '无理投诉',
  5: '违法行为'
}

const typeMap = {
  1: '临时',
  2: '永久'
}

const activeTab = ref('pending')

const pendingList = ref([])
const pendingLoading = ref(false)

const pendingRemoveList = ref([])
const pendingRemoveLoading = ref(false)

const loadPending = async () => {
  pendingLoading.value = true
  try {
    const res = await api.customer.getBlacklistPending()
    if (res.code === 200) {
      pendingList.value = res.data || []
    }
  } catch {
    pendingList.value = []
  } finally {
    pendingLoading.value = false
  }
}

const loadPendingRemove = async () => {
  pendingRemoveLoading.value = true
  try {
    const res = await api.customer.getBlacklistPendingRemove()
    if (res.code === 200) {
      pendingRemoveList.value = res.data || []
    }
  } catch {
    pendingRemoveList.value = []
  } finally {
    pendingRemoveLoading.value = false
  }
}

const handleTabChange = (tab) => {
  if (tab === 'pending') {
    loadPending()
  } else {
    loadPendingRemove()
  }
}

const approveDialogVisible = ref(false)
const approveSaving = ref(false)
const approveFormRef = ref(null)
const approveForm = reactive({
  id: null,
  customerName: '',
  actionType: 'add',
  opinion: ''
})

const openApproveDialog = (row, actionType) => {
  approveForm.id = row.id
  approveForm.customerName = row.customerName
  approveForm.actionType = actionType
  approveForm.opinion = ''
  approveDialogVisible.value = true
}

const handleApproveSubmit = async () => {
  approveSaving.value = true
  try {
    const payload = { opinion: approveForm.opinion }
    const res = approveForm.actionType === 'add'
      ? await api.customer.approveBlacklist(approveForm.id, payload)
      : await api.customer.approveBlacklistRemove(approveForm.id, payload)
    if (res.code === 200) {
      ElMessage.success('审批通过成功')
      approveDialogVisible.value = false
      if (approveForm.actionType === 'add') {
        loadPending()
      } else {
        loadPendingRemove()
      }
    } else {
      ElMessage.error(res.message || '操作失败')
    }
  } catch {
    ElMessage.error('操作失败')
  } finally {
    approveSaving.value = false
  }
}

const rejectDialogVisible = ref(false)
const rejectSaving = ref(false)
const rejectFormRef = ref(null)
const rejectForm = reactive({
  id: null,
  customerName: '',
  reason: ''
})

const rejectRules = {
  reason: [{ required: true, message: '请输入拒绝原因', trigger: 'blur' }]
}

const openRejectDialog = (row) => {
  rejectForm.id = row.id
  rejectForm.customerName = row.customerName
  rejectForm.reason = ''
  rejectDialogVisible.value = true
}

const handleRejectSubmit = async () => {
  const valid = await rejectFormRef.value.validate().catch(() => false)
  if (!valid) return
  rejectSaving.value = true
  try {
    const payload = { reason: rejectForm.reason }
    const res = await api.customer.rejectBlacklist(rejectForm.id, payload)
    if (res.code === 200) {
      ElMessage.success('已拒绝该申请')
      rejectDialogVisible.value = false
      loadPending()
      loadPendingRemove()
    } else {
      ElMessage.error(res.message || '操作失败')
    }
  } catch {
    ElMessage.error('操作失败')
  } finally {
    rejectSaving.value = false
  }
}

onMounted(() => {
  loadPending()
})
</script>

<style scoped>
.blacklist-approval-container {
  padding: 10px;
}

.tabs-card {
  border-radius: 12px;
  border: none;
}
</style>
