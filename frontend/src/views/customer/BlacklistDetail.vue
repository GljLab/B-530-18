<template>
  <div class="blacklist-detail-container" v-loading="loading">
    <div class="detail-header">
      <div class="header-left">
        <el-button @click="router.back()">
          <el-icon><ArrowLeft /></el-icon>返回
        </el-button>
        <h2 class="page-title">黑名单详情</h2>
        <el-tag :type="statusTagType(blacklist?.status)" size="default">
          {{ statusLabel(blacklist?.status) }}
        </el-tag>
      </div>
      <div class="header-actions">
        <el-button
          v-if="canApprove && (blacklist?.status === 1)"
          type="success"
          @click="openApproveDialog"
        >
          <el-icon><Check /></el-icon>审批通过
        </el-button>
        <el-button
          v-if="canApprove && (blacklist?.status === 1)"
          type="danger"
          @click="openRejectDialog"
        >
          <el-icon><Close /></el-icon>审批拒绝
        </el-button>
        <el-button
          v-if="canRemove && (blacklist?.status === 2)"
          type="warning"
          @click="openRemoveDialog"
        >
          <el-icon><Unlock /></el-icon>申请解除
        </el-button>
        <el-button
          v-if="canApprove && (blacklist?.status === 5)"
          type="success"
          @click="openApproveRemoveDialog"
        >
          <el-icon><Check /></el-icon>通过解除
        </el-button>
      </div>
    </div>

    <el-row :gutter="16">
      <el-col :span="12">
        <el-card shadow="never" class="info-card">
          <template #header>
            <span class="card-header-title">客户信息</span>
          </template>
          <el-descriptions :column="1" border size="default">
            <el-descriptions-item label="客户姓名">{{ blacklist?.customerName || '-' }}</el-descriptions-item>
            <el-descriptions-item label="手机号">{{ blacklist?.customerPhone || '-' }}</el-descriptions-item>
            <el-descriptions-item label="证件号">{{ blacklist?.customerIdNumber || '-' }}</el-descriptions-item>
          </el-descriptions>
        </el-card>
      </el-col>

      <el-col :span="12">
        <el-card shadow="never" class="info-card">
          <template #header>
            <span class="card-header-title">黑名单信息</span>
          </template>
          <el-descriptions :column="1" border size="default">
            <el-descriptions-item label="拉黑原因">{{ reasonLabel(blacklist?.reason) }}</el-descriptions-item>
            <el-descriptions-item label="黑名单类型">
              <el-tag :type="blacklist?.blacklistType === 2 ? 'danger' : 'warning'" size="small">
                {{ typeLabel(blacklist?.blacklistType) }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="当前状态">
              <el-tag :type="statusTagType(blacklist?.status)" size="small">
                {{ statusLabel(blacklist?.status) }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item v-if="blacklist?.blacklistType === 1" label="到期时间">
              {{ formatTime(blacklist?.expireTime) }}
            </el-descriptions-item>
          </el-descriptions>
        </el-card>
      </el-col>
    </el-row>

    <el-card shadow="never" class="detail-card">
      <template #header>
        <span class="card-header-title">详细描述</span>
      </template>
      <div class="detail-content">
        {{ blacklist?.detailDescription || '-' }}
      </div>
    </el-card>

    <el-card v-if="evidenceList.length" shadow="never" class="detail-card">
      <template #header>
        <span class="card-header-title">证据材料</span>
      </template>
      <div class="evidence-list">
        <div
          v-for="(item, idx) in evidenceList"
          :key="idx"
          class="evidence-item"
        >
          <el-icon class="evidence-icon"><Paperclip /></el-icon>
          <span class="evidence-name">{{ item.name || '证据材料' + (idx + 1) }}</span>
          <el-button type="primary" link size="small" @click="viewEvidence(item)">
            查看
          </el-button>
        </div>
      </div>
    </el-card>

    <el-card shadow="never" class="detail-card">
      <template #header>
        <span class="card-header-title">审批记录</span>
      </template>
      <el-timeline>
        <el-timeline-item
          :timestamp="formatTime(blacklist?.applyTime)"
          placement="top"
        >
          <el-card shadow="never" class="timeline-card">
            <div class="timeline-title">
              <el-tag size="small" type="info">提交申请</el-tag>
              <span class="timeline-operator">{{ blacklist?.applicantName || '未知' }}</span>
            </div>
            <div class="timeline-desc">提交加入黑名单申请</div>
          </el-card>
        </el-timeline-item>

        <el-timeline-item
          v-if="blacklist?.status === 2 || blacklist?.status === 3"
          :timestamp="formatTime(blacklist?.approveTime)"
          placement="top"
        >
          <el-card shadow="never" class="timeline-card">
            <div class="timeline-title">
              <el-tag size="small" :type="blacklist?.status === 2 ? 'success' : 'danger'">
                {{ blacklist?.status === 2 ? '审批通过' : '审批拒绝' }}
              </el-tag>
              <span class="timeline-operator">{{ blacklist?.approverName || '未知' }}</span>
            </div>
            <div v-if="blacklist?.approveOpinion" class="timeline-desc">
              审批意见：{{ blacklist.approveOpinion }}
            </div>
            <div v-if="blacklist?.rejectReason" class="timeline-desc">
              拒绝原因：{{ blacklist.rejectReason }}
            </div>
          </el-card>
        </el-timeline-item>

        <el-timeline-item
          v-if="blacklist?.status === 5 || blacklist?.status === 4"
          :timestamp="formatTime(blacklist?.removeApplyTime)"
          placement="top"
        >
          <el-card shadow="never" class="timeline-card">
            <div class="timeline-title">
              <el-tag size="small" type="warning">申请解除</el-tag>
              <span class="timeline-operator">{{ blacklist?.removeApplicantName || '未知' }}</span>
            </div>
            <div class="timeline-desc">解除原因：{{ blacklist?.removeReason || '-' }}</div>
          </el-card>
        </el-timeline-item>

        <el-timeline-item
          v-if="blacklist?.status === 4"
          :timestamp="formatTime(blacklist?.removeApproveTime)"
          placement="top"
        >
          <el-card shadow="never" class="timeline-card">
            <div class="timeline-title">
              <el-tag size="small" type="success">解除通过</el-tag>
              <span class="timeline-operator">{{ blacklist?.removeApproverName || '未知' }}</span>
            </div>
            <div v-if="blacklist?.removeApproveOpinion" class="timeline-desc">
              审批意见：{{ blacklist.removeApproveOpinion }}
            </div>
          </el-card>
        </el-timeline-item>
      </el-timeline>
    </el-card>

    <el-dialog
      v-model="approveDialogVisible"
      title="审批通过"
      width="480px"
      destroy-on-close
    >
      <el-form ref="approveFormRef" :model="approveForm" label-width="80px">
        <el-form-item label="审批意见">
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
        <el-button type="success" :loading="approveLoading" @click="handleApprove">确认通过</el-button>
      </template>
    </el-dialog>

    <el-dialog
      v-model="rejectDialogVisible"
      title="审批拒绝"
      width="480px"
      destroy-on-close
    >
      <el-form ref="rejectFormRef" :model="rejectForm" :rules="rejectRules" label-width="80px">
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
        <el-button type="danger" :loading="rejectLoading" @click="handleReject">确认拒绝</el-button>
      </template>
    </el-dialog>

    <el-dialog
      v-model="removeDialogVisible"
      title="申请解除黑名单"
      width="480px"
      destroy-on-close
    >
      <el-form ref="removeFormRef" :model="removeForm" :rules="removeRules" label-width="80px">
        <el-form-item label="解除原因" prop="reason">
          <el-input
            v-model="removeForm.reason"
            type="textarea"
            :rows="4"
            placeholder="请输入解除原因"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="removeDialogVisible = false">取消</el-button>
        <el-button type="warning" :loading="removeLoading" @click="handleRemoveSubmit">提交申请</el-button>
      </template>
    </el-dialog>

    <el-dialog
      v-model="approveRemoveDialogVisible"
      title="审批解除申请"
      width="480px"
      destroy-on-close
    >
      <el-form ref="approveRemoveFormRef" :model="approveRemoveForm" label-width="80px">
        <el-form-item label="审批意见">
          <el-input
            v-model="approveRemoveForm.opinion"
            type="textarea"
            :rows="4"
            placeholder="请输入审批意见（选填）"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="approveRemoveDialogVisible = false">取消</el-button>
        <el-button type="success" :loading="approveRemoveLoading" @click="handleApproveRemove">
          确认通过
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowLeft, Check, Close, Unlock, Paperclip } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import api from '@/api'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const id = route.params.id

const canApprove = computed(() => userStore.hasPermission('customer:blacklist:approve'))
const canRemove = computed(() => userStore.hasPermission('customer:blacklist:remove'))

const loading = ref(false)
const blacklist = ref(null)

const reasonLabel = (val) => {
  const map = { 1: '拖欠房费', 2: '破坏设施', 3: '扰乱秩序', 4: '无理投诉', 5: '违法行为' }
  return map[val] || '-'
}

const typeLabel = (val) => {
  const map = { 1: '临时', 2: '永久' }
  return map[val] || '-'
}

const statusLabel = (val) => {
  const map = { 1: '待审批', 2: '已生效', 3: '已拒绝', 4: '已解除', 5: '待解除审批' }
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

const evidenceList = computed(() => {
  if (!blacklist.value?.evidenceMaterials) return []
  try {
    const parsed = typeof blacklist.value.evidenceMaterials === 'string'
      ? JSON.parse(blacklist.value.evidenceMaterials)
      : blacklist.value.evidenceMaterials
    return Array.isArray(parsed) ? parsed : []
  } catch {
    return []
  }
})

const viewEvidence = (item) => {
  const url = item.url || item.fileUrl || item
  if (url) {
    window.open(url, '_blank')
  }
}

const loadDetail = async () => {
  loading.value = true
  try {
    const res = await api.customer.getBlacklistById(id)
    if (res.code === 200) {
      blacklist.value = res.data
    } else {
      ElMessage.error(res.message || '获取详情失败')
    }
  } catch {
    ElMessage.error('获取详情失败')
  } finally {
    loading.value = false
  }
}

const approveDialogVisible = ref(false)
const approveLoading = ref(false)
const approveFormRef = ref(null)
const approveForm = reactive({ opinion: '' })

const openApproveDialog = () => {
  approveForm.opinion = ''
  approveDialogVisible.value = true
}

const handleApprove = async () => {
  approveLoading.value = true
  try {
    const res = await api.customer.approveBlacklist(id, { opinion: approveForm.opinion })
    if (res.code === 200) {
      ElMessage.success('审批通过成功')
      approveDialogVisible.value = false
      await loadDetail()
    } else {
      ElMessage.error(res.message || '操作失败')
    }
  } catch {
    ElMessage.error('操作失败')
  } finally {
    approveLoading.value = false
  }
}

const rejectDialogVisible = ref(false)
const rejectLoading = ref(false)
const rejectFormRef = ref(null)
const rejectForm = reactive({ reason: '' })
const rejectRules = {
  reason: [{ required: true, message: '请输入拒绝原因', trigger: 'blur' }]
}

const openRejectDialog = () => {
  rejectForm.reason = ''
  rejectDialogVisible.value = true
}

const handleReject = async () => {
  const valid = await rejectFormRef.value.validate().catch(() => false)
  if (!valid) return

  rejectLoading.value = true
  try {
    const res = await api.customer.rejectBlacklist(id, { reason: rejectForm.reason })
    if (res.code === 200) {
      ElMessage.success('已拒绝该申请')
      rejectDialogVisible.value = false
      await loadDetail()
    } else {
      ElMessage.error(res.message || '操作失败')
    }
  } catch {
    ElMessage.error('操作失败')
  } finally {
    rejectLoading.value = false
  }
}

const removeDialogVisible = ref(false)
const removeLoading = ref(false)
const removeFormRef = ref(null)
const removeForm = reactive({ reason: '' })
const removeRules = {
  reason: [{ required: true, message: '请输入解除原因', trigger: 'blur' }]
}

const openRemoveDialog = () => {
  removeForm.reason = ''
  removeDialogVisible.value = true
}

const handleRemoveSubmit = async () => {
  const valid = await removeFormRef.value.validate().catch(() => false)
  if (!valid) return

  removeLoading.value = true
  try {
    const res = await api.customer.submitBlacklistRemove(id, { reason: removeForm.reason })
    if (res.code === 200) {
      ElMessage.success('解除申请已提交')
      removeDialogVisible.value = false
      await loadDetail()
    } else {
      ElMessage.error(res.message || '操作失败')
    }
  } catch {
    ElMessage.error('操作失败')
  } finally {
    removeLoading.value = false
  }
}

const approveRemoveDialogVisible = ref(false)
const approveRemoveLoading = ref(false)
const approveRemoveFormRef = ref(null)
const approveRemoveForm = reactive({ opinion: '' })

const openApproveRemoveDialog = () => {
  approveRemoveForm.opinion = ''
  approveRemoveDialogVisible.value = true
}

const handleApproveRemove = async () => {
  approveRemoveLoading.value = true
  try {
    const res = await api.customer.approveBlacklistRemove(id, { opinion: approveRemoveForm.opinion })
    if (res.code === 200) {
      ElMessage.success('解除审批通过')
      approveRemoveDialogVisible.value = false
      await loadDetail()
    } else {
      ElMessage.error(res.message || '操作失败')
    }
  } catch {
    ElMessage.error('操作失败')
  } finally {
    approveRemoveLoading.value = false
  }
}

onMounted(() => {
  loadDetail()
})
</script>

<style scoped>
.blacklist-detail-container {
  padding: 10px;
}

.detail-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  padding: 16px 20px;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.06);
}

.header-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.page-title {
  margin: 0;
  font-size: 20px;
  font-weight: 600;
  color: #1a202c;
}

.header-actions {
  display: flex;
  gap: 10px;
}

.info-card,
.detail-card {
  border-radius: 12px;
  border: none;
  margin-bottom: 16px;
}

.card-header-title {
  font-weight: 600;
  color: #1a202c;
}

.detail-content {
  font-size: 14px;
  color: #4a5568;
  line-height: 1.8;
  white-space: pre-wrap;
}

.evidence-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.evidence-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 12px;
  background: #f7f8fa;
  border-radius: 8px;
}

.evidence-icon {
  color: #909399;
  font-size: 16px;
}

.evidence-name {
  flex: 1;
  font-size: 14px;
  color: #606266;
}

.timeline-card {
  border-radius: 8px;
}

.timeline-title {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 6px;
}

.timeline-operator {
  font-size: 14px;
  font-weight: 500;
  color: #2d3748;
}

.timeline-desc {
  font-size: 13px;
  color: #718096;
  line-height: 1.6;
}
</style>
