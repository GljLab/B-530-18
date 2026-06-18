<template>
  <div class="refund-approval">
    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <span class="title">退款审批</span>
        </div>
      </template>

      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="关键字">
          <el-input v-model="searchForm.keyword" placeholder="客户姓名/手机号/单号" clearable style="width: 200px" />
        </el-form-item>
        <el-form-item label="退款类型">
          <el-select v-model="searchForm.refundType" placeholder="全部" clearable style="width: 160px">
            <el-option label="预订取消退款" :value="1" />
            <el-option label="多收费用退款" :value="2" />
            <el-option label="客诉补偿退款" :value="3" />
            <el-option label="房间质量问题退款" :value="4" />
            <el-option label="其他" :value="5" />
          </el-select>
        </el-form-item>
        <el-form-item label="退款状态">
          <el-select v-model="searchForm.status" placeholder="全部" clearable style="width: 140px">
            <el-option label="待审批" :value="0" />
            <el-option label="已通过" :value="1" />
            <el-option label="已拒绝" :value="2" />
            <el-option label="已退款" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button type="success" @click="openApplyDialog">申请退款</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="tableData" v-loading="loading" border stripe style="width: 100%">
        <el-table-column prop="refundNo" label="退款单号" width="180" />
        <el-table-column prop="relatedOrderNo" label="关联单号" width="180" />
        <el-table-column prop="customerName" label="客户姓名" width="100" />
        <el-table-column label="退款类型" width="140">
          <template #default="{ row }">
            {{ refundTypeMap[row.refundType] || '-' }}
          </template>
        </el-table-column>
        <el-table-column label="退款金额" width="120">
          <template #default="{ row }">
            <span class="price">¥{{ formatMoney(row.refundAmount) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="审批金额" width="120">
          <template #default="{ row }">
            <span class="price">¥{{ formatMoney(row.approvedAmount) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="applicantName" label="申请人" width="100" />
        <el-table-column prop="applyTime" label="申请时间" width="170" />
        <el-table-column prop="approverName" label="审批人" width="100" />
        <el-table-column prop="approveTime" label="审批时间" width="170" />
        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="statusType(row.status)" size="small">{{ statusLabel(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="openDetailDialog(row)">查看</el-button>
            <el-button v-if="row.status === 0" type="warning" link size="small" @click="openApproveDialog(row)">审批</el-button>
            <el-button v-if="row.status === 1" type="success" link size="small" @click="openExecuteDialog(row)">执行退款</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:current-page="pagination.pageNum"
        v-model:page-size="pagination.pageSize"
        :total="pagination.total"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next, jumper"
        class="pagination"
        @size-change="loadList"
        @current-change="loadList"
      />
    </el-card>

    <el-dialog v-model="applyDialogVisible" title="申请退款" width="600px" destroy-on-close>
      <el-form ref="applyFormRef" :model="applyForm" :rules="applyRules" label-width="100px">
        <el-form-item label="关联单类型" prop="relatedOrderType">
          <el-select v-model="applyForm.relatedOrderType" placeholder="请选择" style="width: 100%">
            <el-option label="预订单" :value="1" />
            <el-option label="入住单" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item label="关联单号" prop="relatedOrderNo">
          <el-input v-model="applyForm.relatedOrderNo" placeholder="请输入关联单号" />
        </el-form-item>
        <el-form-item label="客户姓名" prop="customerName">
          <el-input v-model="applyForm.customerName" placeholder="请输入客户姓名" />
        </el-form-item>
        <el-form-item label="客户手机号" prop="customerPhone">
          <el-input v-model="applyForm.customerPhone" placeholder="请输入客户手机号" />
        </el-form-item>
        <el-form-item label="退款类型" prop="refundType">
          <el-select v-model="applyForm.refundType" placeholder="请选择退款类型" style="width: 100%">
            <el-option label="预订取消退款" :value="1" />
            <el-option label="多收费用退款" :value="2" />
            <el-option label="客诉补偿退款" :value="3" />
            <el-option label="房间质量问题退款" :value="4" />
            <el-option label="其他" :value="5" />
          </el-select>
        </el-form-item>
        <el-form-item label="退款金额" prop="refundAmount">
          <el-input-number v-model="applyForm.refundAmount" :min="0.01" :precision="2" :step="1" style="width: 100%" />
        </el-form-item>
        <el-form-item label="退款原因" prop="refundReason">
          <el-input v-model="applyForm.refundReason" type="textarea" :rows="3" placeholder="请输入退款原因" />
        </el-form-item>
        <el-form-item label="证明材料">
          <el-upload
            v-model:file-list="applyForm.evidenceFiles"
            action="/api/file/upload"
            list-type="picture-card"
            accept="image/*"
            :headers="uploadHeaders"
            multiple
          >
            <el-icon><Plus /></el-icon>
          </el-upload>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="applyDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="applySaving" @click="handleApplySubmit">提交申请</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="approveDialogVisible" title="退款审批" width="600px" destroy-on-close>
      <el-descriptions v-if="currentRow" :column="2" border class="detail-desc">
        <el-descriptions-item label="退款单号">{{ currentRow.refundNo }}</el-descriptions-item>
        <el-descriptions-item label="关联单号">{{ currentRow.relatedOrderNo }}</el-descriptions-item>
        <el-descriptions-item label="客户姓名">{{ currentRow.customerName }}</el-descriptions-item>
        <el-descriptions-item label="退款类型">{{ refundTypeMap[currentRow.refundType] }}</el-descriptions-item>
        <el-descriptions-item label="退款金额">
          <span class="price">¥{{ formatMoney(currentRow.refundAmount) }}</span>
        </el-descriptions-item>
        <el-descriptions-item label="退款原因" :span="2">{{ currentRow.refundReason }}</el-descriptions-item>
      </el-descriptions>

      <el-divider />

      <el-form ref="approveFormRef" :model="approveForm" :rules="approveRules" label-width="100px">
        <el-form-item label="审批操作" prop="action">
          <el-radio-group v-model="approveForm.action">
            <el-radio value="approve">通过</el-radio>
            <el-radio value="reject">拒绝</el-radio>
            <el-radio value="modify">修改金额</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item v-if="approveForm.action === 'modify'" label="修改金额" prop="modifiedAmount">
          <el-input-number v-model="approveForm.modifiedAmount" :min="0.01" :precision="2" :step="1" style="width: 200px" />
        </el-form-item>
        <el-form-item label="审批意见" prop="approveRemark">
          <el-input v-model="approveForm.approveRemark" type="textarea" :rows="3" placeholder="请输入审批意见" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="approveDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="approveSaving" @click="handleApproveSubmit">确认</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="executeDialogVisible" title="执行退款" width="600px" destroy-on-close>
      <el-descriptions v-if="currentRow" :column="2" border class="detail-desc">
        <el-descriptions-item label="退款单号">{{ currentRow.refundNo }}</el-descriptions-item>
        <el-descriptions-item label="关联单号">{{ currentRow.relatedOrderNo }}</el-descriptions-item>
        <el-descriptions-item label="客户姓名">{{ currentRow.customerName }}</el-descriptions-item>
        <el-descriptions-item label="退款类型">{{ refundTypeMap[currentRow.refundType] }}</el-descriptions-item>
        <el-descriptions-item label="审批金额">
          <span class="price">¥{{ formatMoney(currentRow.approvedAmount) }}</span>
        </el-descriptions-item>
        <el-descriptions-item label="退款原因" :span="2">{{ currentRow.refundReason }}</el-descriptions-item>
      </el-descriptions>

      <el-divider />

      <el-form ref="executeFormRef" :model="executeForm" :rules="executeRules" label-width="100px">
        <el-form-item label="退款方式" prop="refundMethod">
          <el-select v-model="executeForm.refundMethod" placeholder="请选择退款方式" style="width: 100%">
            <el-option label="原路退回" :value="1" />
            <el-option label="现金退款" :value="2" />
            <el-option label="转账退款" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item label="退款凭证号" prop="voucherNo">
          <el-input v-model="executeForm.voucherNo" placeholder="请输入退款凭证号" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="executeDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="executeSaving" @click="handleExecuteSubmit">确认退款</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="detailDialogVisible" title="退款详情" width="700px" destroy-on-close>
      <el-descriptions v-if="currentRow" :column="2" border class="detail-desc">
        <el-descriptions-item label="退款单号">{{ currentRow.refundNo }}</el-descriptions-item>
        <el-descriptions-item label="关联单号">{{ currentRow.relatedOrderNo }}</el-descriptions-item>
        <el-descriptions-item label="客户姓名">{{ currentRow.customerName }}</el-descriptions-item>
        <el-descriptions-item label="退款类型">{{ refundTypeMap[currentRow.refundType] }}</el-descriptions-item>
        <el-descriptions-item label="退款金额">
          <span class="price">¥{{ formatMoney(currentRow.refundAmount) }}</span>
        </el-descriptions-item>
        <el-descriptions-item label="审批金额">
          <span class="price">¥{{ formatMoney(currentRow.approvedAmount) }}</span>
        </el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="statusType(currentRow.status)" size="small">{{ statusLabel(currentRow.status) }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="退款原因" :span="2">{{ currentRow.refundReason }}</el-descriptions-item>
        <el-descriptions-item label="申请人">{{ currentRow.applicantName }}</el-descriptions-item>
        <el-descriptions-item label="申请时间">{{ currentRow.applyTime }}</el-descriptions-item>
        <el-descriptions-item label="审批人">{{ currentRow.approverName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="审批时间">{{ currentRow.approveTime || '-' }}</el-descriptions-item>
        <el-descriptions-item label="退款方式">{{ refundMethodMap[currentRow.refundMethod] || '-' }}</el-descriptions-item>
        <el-descriptions-item label="退款凭证号">{{ currentRow.voucherNo || '-' }}</el-descriptions-item>
        <el-descriptions-item label="证明材料" :span="2">
          <template v-if="currentRow.evidenceUrls && currentRow.evidenceUrls.length">
            <el-image
              v-for="(url, idx) in currentRow.evidenceUrls"
              :key="idx"
              :src="url"
              :preview-src-list="currentRow.evidenceUrls"
              :initial-index="idx"
              fit="cover"
              style="width: 80px; height: 80px; margin-right: 8px"
            />
          </template>
          <span v-else>-</span>
        </el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import api from '@/api'

const refundTypeMap = {
  1: '预订取消退款',
  2: '多收费用退款',
  3: '客诉补偿退款',
  4: '房间质量问题退款',
  5: '其他'
}

const refundMethodMap = {
  1: '原路退回',
  2: '现金退款',
  3: '转账退款'
}

const statusLabel = (status) => {
  const map = { 0: '待审批', 1: '已通过', 2: '已拒绝', 3: '已退款' }
  return map[status] ?? '未知'
}

const statusType = (status) => {
  const map = { 0: 'warning', 1: '', 2: 'danger', 3: 'success' }
  return map[status] ?? 'info'
}

const formatMoney = (val) => {
  if (val == null) return '0.00'
  return Number(val).toFixed(2)
}

const loading = ref(false)
const tableData = ref([])
const searchForm = reactive({
  keyword: '',
  refundType: null,
  status: null
})
const pagination = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0
})

const currentRow = ref(null)

const loadList = async () => {
  loading.value = true
  try {
    const res = await api.finance.refundApply.page({
      pageNum: pagination.pageNum,
      pageSize: pagination.pageSize,
      keyword: searchForm.keyword || undefined,
      refundType: searchForm.refundType ?? undefined,
      status: searchForm.status ?? undefined
    })
    if (res.code === 200) {
      tableData.value = res.data.records
      pagination.total = res.data.total
    }
  } catch {
    ElMessage.error('加载失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  pagination.pageNum = 1
  loadList()
}

const uploadHeaders = computed(() => {
  const token = localStorage.getItem('token')
  return token ? { Authorization: `Bearer ${token}` } : {}
})

const applyDialogVisible = ref(false)
const applySaving = ref(false)
const applyFormRef = ref(null)
const applyForm = reactive({
  relatedOrderType: null,
  relatedOrderNo: '',
  customerName: '',
  customerPhone: '',
  refundType: null,
  refundAmount: null,
  refundReason: '',
  evidenceFiles: []
})

const applyRules = {
  relatedOrderType: [{ required: true, message: '请选择关联单类型', trigger: 'change' }],
  relatedOrderNo: [{ required: true, message: '请输入关联单号', trigger: 'blur' }],
  customerName: [{ required: true, message: '请输入客户姓名', trigger: 'blur' }],
  refundType: [{ required: true, message: '请选择退款类型', trigger: 'change' }],
  refundAmount: [{ required: true, message: '请输入退款金额', trigger: 'blur' }],
  refundReason: [{ required: true, message: '请输入退款原因', trigger: 'blur' }]
}

const openApplyDialog = () => {
  applyForm.relatedOrderType = null
  applyForm.relatedOrderNo = ''
  applyForm.customerName = ''
  applyForm.customerPhone = ''
  applyForm.refundType = null
  applyForm.refundAmount = null
  applyForm.refundReason = ''
  applyForm.evidenceFiles = []
  applyDialogVisible.value = true
}

const handleApplySubmit = async () => {
  const valid = await applyFormRef.value.validate().catch(() => false)
  if (!valid) return
  applySaving.value = true
  try {
    const payload = {
      relatedOrderType: applyForm.relatedOrderType,
      relatedOrderNo: applyForm.relatedOrderNo,
      customerName: applyForm.customerName,
      customerPhone: applyForm.customerPhone,
      refundType: applyForm.refundType,
      refundAmount: applyForm.refundAmount,
      refundReason: applyForm.refundReason,
      evidenceUrls: applyForm.evidenceFiles.map(f => f.response?.data?.url || f.url).filter(Boolean)
    }
    const res = await api.finance.refundApply.apply(payload)
    if (res.code === 200) {
      ElMessage.success('退款申请提交成功')
      applyDialogVisible.value = false
      loadList()
    } else {
      ElMessage.error(res.message || '提交失败')
    }
  } catch {
    ElMessage.error('提交失败')
  } finally {
    applySaving.value = false
  }
}

const approveDialogVisible = ref(false)
const approveSaving = ref(false)
const approveFormRef = ref(null)
const approveForm = reactive({
  id: null,
  action: 'approve',
  approveRemark: '',
  modifiedAmount: null
})

const approveRules = {
  action: [{ required: true, message: '请选择审批操作', trigger: 'change' }],
  approveRemark: [{ required: true, message: '请输入审批意见', trigger: 'blur' }],
  modifiedAmount: [{
    validator: (rule, value, callback) => {
      if (approveForm.action === 'modify' && (!value || value <= 0)) {
        callback(new Error('请输入修改金额'))
      } else {
        callback()
      }
    },
    trigger: 'blur'
  }]
}

const openApproveDialog = (row) => {
  currentRow.value = { ...row }
  approveForm.id = row.id
  approveForm.action = 'approve'
  approveForm.approveRemark = ''
  approveForm.modifiedAmount = null
  approveDialogVisible.value = true
}

const handleApproveSubmit = async () => {
  const valid = await approveFormRef.value.validate().catch(() => false)
  if (!valid) return
  approveSaving.value = true
  try {
    const payload = {
      action: approveForm.action,
      approveRemark: approveForm.approveRemark
    }
    if (approveForm.action === 'modify') {
      payload.approvedAmount = approveForm.modifiedAmount
    }
    const res = await api.finance.refundApply.approve(approveForm.id, payload)
    if (res.code === 200) {
      ElMessage.success('审批成功')
      approveDialogVisible.value = false
      loadList()
    } else {
      ElMessage.error(res.message || '审批失败')
    }
  } catch {
    ElMessage.error('审批失败')
  } finally {
    approveSaving.value = false
  }
}

const executeDialogVisible = ref(false)
const executeSaving = ref(false)
const executeFormRef = ref(null)
const executeForm = reactive({
  id: null,
  refundMethod: null,
  voucherNo: ''
})

const executeRules = {
  refundMethod: [{ required: true, message: '请选择退款方式', trigger: 'change' }],
  voucherNo: [{ required: true, message: '请输入退款凭证号', trigger: 'blur' }]
}

const openExecuteDialog = (row) => {
  currentRow.value = { ...row }
  executeForm.id = row.id
  executeForm.refundMethod = null
  executeForm.voucherNo = ''
  executeDialogVisible.value = true
}

const handleExecuteSubmit = async () => {
  const valid = await executeFormRef.value.validate().catch(() => false)
  if (!valid) return
  executeSaving.value = true
  try {
    const res = await api.finance.refundApply.execute(executeForm.id, {
      refundMethod: executeForm.refundMethod,
      voucherNo: executeForm.voucherNo
    })
    if (res.code === 200) {
      ElMessage.success('退款执行成功')
      executeDialogVisible.value = false
      loadList()
    } else {
      ElMessage.error(res.message || '执行失败')
    }
  } catch {
    ElMessage.error('执行失败')
  } finally {
    executeSaving.value = false
  }
}

const detailDialogVisible = ref(false)

const openDetailDialog = async (row) => {
  try {
    const res = await api.finance.refundApply.get(row.id)
    if (res.code === 200) {
      currentRow.value = res.data
      detailDialogVisible.value = true
    }
  } catch {
    ElMessage.error('获取详情失败')
  }
}

onMounted(() => {
  loadList()
})
</script>

<style scoped>
.refund-approval {
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

.pagination {
  margin-top: 20px;
  justify-content: flex-end;
  display: flex;
}

.price {
  color: #409eff;
  font-weight: 600;
}

.detail-desc {
  margin-bottom: 16px;
}
</style>
