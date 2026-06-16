<template>
  <div class="refund-manage">
    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <span class="title">退款管理</span>
        </div>
      </template>

      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="退款状态">
          <el-select v-model="searchForm.status" placeholder="全部" clearable style="width: 140px">
            <el-option label="待审批" :value="1" />
            <el-option label="已审批" :value="2" />
            <el-option label="已退款" :value="3" />
            <el-option label="已拒绝" :value="4" />
          </el-select>
        </el-form-item>
        <el-form-item label="预订单号">
          <el-input v-model="searchForm.bookingNo" placeholder="请输入预订单号" clearable style="width: 200px" />
        </el-form-item>
        <el-form-item label="客户姓名">
          <el-input v-model="searchForm.customerName" placeholder="请输入客户姓名" clearable style="width: 160px" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadRefunds">查询</el-button>
          <el-button @click="resetSearch">重置</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="tableData" v-loading="loading" border stripe style="width: 100%">
        <el-table-column prop="refundNo" label="退款单号" width="200" />
        <el-table-column prop="bookingNo" label="预订单号" width="200" />
        <el-table-column prop="customerName" label="客户姓名" width="120" />
        <el-table-column prop="paidAmount" label="已付金额" width="120">
          <template #default="{ row }">
            <span class="price">¥{{ row.paidAmount?.toFixed(2) || '0.00' }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="deductionAmount" label="扣款金额" width="120">
          <template #default="{ row }">
            <span class="price-up">¥{{ row.deductionAmount?.toFixed(2) || '0.00' }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="actualRefundAmount" label="退款金额" width="120">
          <template #default="{ row }">
            <span class="price">¥{{ row.actualRefundAmount?.toFixed(2) || '0.00' }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="refundReason" label="退款原因" width="140" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="statusType(row.status)" size="small">{{ statusLabel(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="applicantName" label="申请人" width="100" />
        <el-table-column prop="createTime" label="申请时间" width="180" />
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="viewDetail(row)">详情</el-button>
            <template v-if="row.status === 1">
              <el-button type="success" link size="small" @click="openApprove(row)">审批</el-button>
            </template>
            <template v-if="row.status === 2">
              <el-button type="warning" link size="small" @click="openProcess(row)">执行退款</el-button>
            </template>
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
        @size-change="loadRefunds"
        @current-change="loadRefunds"
      />
    </el-card>

    <el-dialog v-model="detailDialogVisible" title="退款详情" width="600px" destroy-on-close>
      <el-descriptions v-if="currentRefund" :column="2" border>
        <el-descriptions-item label="退款单号">{{ currentRefund.refundNo }}</el-descriptions-item>
        <el-descriptions-item label="预订单号">{{ currentRefund.bookingNo }}</el-descriptions-item>
        <el-descriptions-item label="客户姓名">{{ currentRefund.customerName }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="statusType(currentRefund.status)" size="small">
            {{ statusLabel(currentRefund.status) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="已付金额">
          <span class="price">¥{{ currentRefund.paidAmount?.toFixed(2) || '0.00' }}</span>
        </el-descriptions-item>
        <el-descriptions-item label="可退金额">
          <span class="price">¥{{ currentRefund.refundableAmount?.toFixed(2) || '0.00' }}</span>
        </el-descriptions-item>
        <el-descriptions-item label="扣款金额">
          <span class="price-up">¥{{ currentRefund.deductionAmount?.toFixed(2) || '0.00' }}</span>
        </el-descriptions-item>
        <el-descriptions-item label="实际退款">
          <span class="price">¥{{ currentRefund.actualRefundAmount?.toFixed(2) || '0.00' }}</span>
        </el-descriptions-item>
        <el-descriptions-item label="退款原因">{{ currentRefund.refundReason }}</el-descriptions-item>
        <el-descriptions-item label="退款方式">{{ currentRefund.refundMethod || '-' }}</el-descriptions-item>
        <el-descriptions-item label="申请人">{{ currentRefund.applicantName }}</el-descriptions-item>
        <el-descriptions-item label="申请时间">{{ currentRefund.createTime }}</el-descriptions-item>
        <el-descriptions-item label="审批人">{{ currentRefund.approverName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="审批时间">{{ currentRefund.approveTime || '-' }}</el-descriptions-item>
        <el-descriptions-item label="审批备注" :span="2">{{ currentRefund.approveRemark || '-' }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>

    <el-dialog v-model="approveDialogVisible" title="审批退款" width="480px" destroy-on-close>
      <el-form ref="approveFormRef" :model="approveForm" :rules="approveRules" label-width="100px">
        <el-form-item label="退款单号">
          <span>{{ currentRefund?.refundNo }}</span>
        </el-form-item>
        <el-form-item label="退款金额">
          <span class="price">¥{{ currentRefund?.actualRefundAmount?.toFixed(2) || '0.00' }}</span>
        </el-form-item>
        <el-form-item label="审批结果" prop="approved">
          <el-radio-group v-model="approveForm.approved">
            <el-radio :value="true">同意</el-radio>
            <el-radio :value="false">拒绝</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="审批备注" prop="approveRemark">
          <el-input v-model="approveForm.approveRemark" type="textarea" :rows="3" placeholder="请输入审批备注" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="approveDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="approveSaving" @click="handleApprove">确认</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import api from '@/api'

const loading = ref(false)
const tableData = ref([])
const searchForm = reactive({
  status: null,
  bookingNo: '',
  customerName: ''
})
const pagination = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0
})

const detailDialogVisible = ref(false)
const approveDialogVisible = ref(false)
const currentRefund = ref(null)
const approveFormRef = ref(null)
const approveSaving = ref(false)
const approveForm = reactive({
  id: null,
  approved: true,
  approveRemark: ''
})
const approveRules = {
  approveRemark: [{ required: true, message: '请输入审批备注', trigger: 'blur' }]
}

const statusLabel = (status) => {
  const map = { 1: '待审批', 2: '已审批', 3: '已退款', 4: '已拒绝' }
  return map[status] || '未知'
}

const statusType = (status) => {
  const map = { 1: 'warning', 2: 'primary', 3: 'success', 4: 'danger' }
  return map[status] || 'info'
}

const loadRefunds = async () => {
  loading.value = true
  try {
    const res = await api.refund.page({
      pageNum: pagination.pageNum,
      pageSize: pagination.pageSize,
      ...searchForm
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

const resetSearch = () => {
  searchForm.status = null
  searchForm.bookingNo = ''
  searchForm.customerName = ''
  pagination.pageNum = 1
  loadRefunds()
}

const viewDetail = async (row) => {
  try {
    const res = await api.refund.get(row.id)
    if (res.code === 200) {
      currentRefund.value = res.data
      detailDialogVisible.value = true
    }
  } catch {
    ElMessage.error('获取详情失败')
  }
}

const openApprove = (row) => {
  currentRefund.value = { ...row }
  approveForm.id = row.id
  approveForm.approved = true
  approveForm.approveRemark = ''
  approveDialogVisible.value = true
}

const handleApprove = async () => {
  const valid = await approveFormRef.value.validate().catch(() => false)
  if (!valid) return
  approveSaving.value = true
  try {
    const res = await api.refund.approve(approveForm.id, {
      approved: approveForm.approved,
      approveRemark: approveForm.approveRemark
    })
    if (res.code === 200) {
      ElMessage.success('审批成功')
      approveDialogVisible.value = false
      await loadRefunds()
    } else {
      ElMessage.error(res.message || '审批失败')
    }
  } catch {
    ElMessage.error('审批失败')
  } finally {
    approveSaving.value = false
  }
}

const openProcess = async (row) => {
  try {
    const res = await api.refund.process(row.id)
    if (res.code === 200) {
      ElMessage.success('退款执行成功')
      await loadRefunds()
    } else {
      ElMessage.error(res.message || '执行失败')
    }
  } catch {
    ElMessage.error('执行失败')
  }
}

onMounted(() => {
  loadRefunds()
})
</script>

<style scoped>
.refund-manage {
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

.price-up {
  color: #f56c6c;
  font-weight: 600;
}
</style>
