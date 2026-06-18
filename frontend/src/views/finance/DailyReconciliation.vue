<template>
  <div class="daily-reconciliation">
    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <span class="title">日结对账</span>
        </div>
      </template>

      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="对账日期">
          <el-date-picker
            v-model="searchForm.reconcileDate"
            type="date"
            placeholder="选择对账日期"
            value-format="YYYY-MM-DD"
            style="width: 180px"
          />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="全部" clearable style="width: 140px">
            <el-option label="全部" :value="null" />
            <el-option label="未对账" :value="0" />
            <el-option label="已完成" :value="1" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadData">查询</el-button>
          <el-button @click="resetSearch">重置</el-button>
          <el-button type="success" @click="handleAutoCalculate">自动生成</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="tableData" v-loading="loading" border stripe style="width: 100%">
        <el-table-column prop="reconcileDate" label="对账日期" width="120" />
        <el-table-column prop="cashTotal" label="现金总额" width="120">
          <template #default="{ row }">
            <span class="price">¥{{ formatMoney(row.cashTotal) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="cardTotal" label="刷卡总额" width="120">
          <template #default="{ row }">
            <span class="price">¥{{ formatMoney(row.cardTotal) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="mobileTotal" label="移动支付总额" width="130">
          <template #default="{ row }">
            <span class="price">¥{{ formatMoney(row.mobileTotal) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="creditAmount" label="挂账金额" width="120">
          <template #default="{ row }">
            <span class="price-warn">¥{{ formatMoney(row.creditAmount) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="prepaidAmount" label="预付金额" width="120">
          <template #default="{ row }">
            <span class="price">¥{{ formatMoney(row.prepaidAmount) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="refundAmount" label="退款金额" width="120">
          <template #default="{ row }">
            <span class="price-up">¥{{ formatMoney(row.refundAmount) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="receivableTotal" label="应收合计" width="120">
          <template #default="{ row }">
            <span class="price">¥{{ formatMoney(row.receivableTotal) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="actualTotal" label="实际合计" width="120">
          <template #default="{ row }">
            <span class="price">¥{{ formatMoney(row.actualTotal) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="difference" label="差异" width="120">
          <template #default="{ row }">
            <span :class="row.difference !== 0 ? 'price-up' : 'price-success'">¥{{ formatMoney(row.difference) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 0 ? 'warning' : 'success'" size="small">
              {{ row.status === 0 ? '未对账' : '已完成' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="140" fixed="right">
          <template #default="{ row }">
            <el-button v-if="row.status === 0" type="primary" link size="small" @click="openReconcile(row)">对账</el-button>
            <el-button type="success" link size="small" @click="viewDetail(row)">查看</el-button>
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
        @size-change="loadData"
        @current-change="loadData"
      />
    </el-card>

    <el-dialog v-model="reconcileDialogVisible" title="日结对账" width="900px" destroy-on-close>
      <div v-if="currentRecord" class="reconcile-content">
        <el-row :gutter="24">
          <el-col :span="12">
            <div class="section-title">应收金额</div>
            <div class="amount-section">
              <div class="amount-group">
                <div class="group-header">现金</div>
                <div class="amount-row">
                  <span class="amount-label">房费</span>
                  <span class="amount-value">¥{{ formatMoney(currentRecord.cashRoomAmount) }}</span>
                </div>
                <div class="amount-row">
                  <span class="amount-label">押金</span>
                  <span class="amount-value">¥{{ formatMoney(currentRecord.cashDepositAmount) }}</span>
                </div>
                <div class="amount-row">
                  <span class="amount-label">其他</span>
                  <span class="amount-value">¥{{ formatMoney(currentRecord.cashOtherAmount) }}</span>
                </div>
                <div class="amount-row subtotal">
                  <span class="amount-label">合计</span>
                  <span class="amount-value">¥{{ formatMoney(currentRecord.cashTotal) }}</span>
                </div>
              </div>

              <div class="amount-group">
                <div class="group-header">刷卡</div>
                <div class="amount-row">
                  <span class="amount-label">房费</span>
                  <span class="amount-value">¥{{ formatMoney(currentRecord.cardRoomAmount) }}</span>
                </div>
                <div class="amount-row">
                  <span class="amount-label">押金</span>
                  <span class="amount-value">¥{{ formatMoney(currentRecord.cardDepositAmount) }}</span>
                </div>
                <div class="amount-row">
                  <span class="amount-label">其他</span>
                  <span class="amount-value">¥{{ formatMoney(currentRecord.cardOtherAmount) }}</span>
                </div>
                <div class="amount-row subtotal">
                  <span class="amount-label">合计</span>
                  <span class="amount-value">¥{{ formatMoney(currentRecord.cardTotal) }}</span>
                </div>
              </div>

              <div class="amount-group">
                <div class="group-header">移动支付</div>
                <div class="amount-row">
                  <span class="amount-label">支付宝</span>
                  <span class="amount-value">¥{{ formatMoney(currentRecord.alipayAmount) }}</span>
                </div>
                <div class="amount-row">
                  <span class="amount-label">微信</span>
                  <span class="amount-value">¥{{ formatMoney(currentRecord.wechatAmount) }}</span>
                </div>
                <div class="amount-row subtotal">
                  <span class="amount-label">合计</span>
                  <span class="amount-value">¥{{ formatMoney(currentRecord.mobileTotal) }}</span>
                </div>
              </div>

              <div class="amount-row">
                <span class="amount-label">挂账金额</span>
                <span class="amount-value price-warn">¥{{ formatMoney(currentRecord.creditAmount) }}</span>
              </div>
              <div class="amount-row">
                <span class="amount-label">预付金额</span>
                <span class="amount-value">¥{{ formatMoney(currentRecord.prepaidAmount) }}</span>
              </div>
              <div class="amount-row">
                <span class="amount-label">退款金额</span>
                <span class="amount-value price-up">¥{{ formatMoney(currentRecord.refundAmount) }}</span>
              </div>

              <div class="amount-row total-row">
                <span class="amount-label">应收合计</span>
                <span class="amount-value total-amount">¥{{ formatMoney(currentRecord.receivableTotal) }}</span>
              </div>
            </div>
          </el-col>

          <el-col :span="12">
            <div class="section-title">实际收款</div>
            <div class="actual-section">
              <el-form :model="reconcileForm" label-width="120px">
                <el-form-item label="实际现金">
                  <el-input-number
                    v-model="reconcileForm.actualCash"
                    :precision="2"
                    :min="0"
                    :controls="false"
                    style="width: 200px"
                  />
                </el-form-item>
                <el-form-item label="实际刷卡">
                  <el-input-number
                    v-model="reconcileForm.actualCard"
                    :precision="2"
                    :min="0"
                    :controls="false"
                    style="width: 200px"
                  />
                </el-form-item>
                <el-form-item label="实际移动支付">
                  <el-input-number
                    v-model="reconcileForm.actualMobile"
                    :precision="2"
                    :min="0"
                    :controls="false"
                    style="width: 200px"
                  />
                </el-form-item>
                <el-form-item label="实际合计">
                  <span class="total-amount">¥{{ formatMoney(actualTotal) }}</span>
                </el-form-item>
              </el-form>
            </div>
          </el-col>
        </el-row>

        <el-divider />

        <div class="difference-section">
          <el-row :gutter="24" align="middle">
            <el-col :span="8">
              <div class="diff-item">
                <span class="diff-label">差异金额</span>
                <span :class="difference !== 0 ? 'price-up' : 'price-success'" class="diff-value">
                  ¥{{ formatMoney(difference) }}
                </span>
              </div>
            </el-col>
          </el-row>
          <template v-if="difference !== 0">
            <el-form :model="reconcileForm" label-width="120px" style="margin-top: 16px">
              <el-form-item label="差异原因">
                <el-input
                  v-model="reconcileForm.differenceReason"
                  type="textarea"
                  :rows="3"
                  placeholder="请输入差异原因"
                />
              </el-form-item>
              <el-form-item label="差异证明材料">
                <el-upload
                  :action="'/api/file/upload'"
                  :headers="uploadHeaders"
                  :on-success="handleUploadSuccess"
                  :on-remove="handleUploadRemove"
                  :file-list="reconcileForm.differenceProof"
                  list-type="text"
                >
                  <el-button type="primary" size="small">上传文件</el-button>
                </el-upload>
              </el-form-item>
            </el-form>
          </template>
        </div>
      </div>

      <template #footer>
        <el-button @click="reconcileDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="confirmSaving" @click="handleConfirm">确认日结</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="detailDialogVisible" title="日结详情" width="900px" destroy-on-close>
      <div v-if="currentRecord" class="detail-content">
        <el-row :gutter="24">
          <el-col :span="12">
            <div class="section-title">应收金额</div>
            <div class="amount-section">
              <div class="amount-group">
                <div class="group-header">现金</div>
                <div class="amount-row">
                  <span class="amount-label">房费</span>
                  <span class="amount-value">¥{{ formatMoney(currentRecord.cashRoomAmount) }}</span>
                </div>
                <div class="amount-row">
                  <span class="amount-label">押金</span>
                  <span class="amount-value">¥{{ formatMoney(currentRecord.cashDepositAmount) }}</span>
                </div>
                <div class="amount-row">
                  <span class="amount-label">其他</span>
                  <span class="amount-value">¥{{ formatMoney(currentRecord.cashOtherAmount) }}</span>
                </div>
                <div class="amount-row subtotal">
                  <span class="amount-label">合计</span>
                  <span class="amount-value">¥{{ formatMoney(currentRecord.cashTotal) }}</span>
                </div>
              </div>

              <div class="amount-group">
                <div class="group-header">刷卡</div>
                <div class="amount-row">
                  <span class="amount-label">房费</span>
                  <span class="amount-value">¥{{ formatMoney(currentRecord.cardRoomAmount) }}</span>
                </div>
                <div class="amount-row">
                  <span class="amount-label">押金</span>
                  <span class="amount-value">¥{{ formatMoney(currentRecord.cardDepositAmount) }}</span>
                </div>
                <div class="amount-row">
                  <span class="amount-label">其他</span>
                  <span class="amount-value">¥{{ formatMoney(currentRecord.cardOtherAmount) }}</span>
                </div>
                <div class="amount-row subtotal">
                  <span class="amount-label">合计</span>
                  <span class="amount-value">¥{{ formatMoney(currentRecord.cardTotal) }}</span>
                </div>
              </div>

              <div class="amount-group">
                <div class="group-header">移动支付</div>
                <div class="amount-row">
                  <span class="amount-label">支付宝</span>
                  <span class="amount-value">¥{{ formatMoney(currentRecord.alipayAmount) }}</span>
                </div>
                <div class="amount-row">
                  <span class="amount-label">微信</span>
                  <span class="amount-value">¥{{ formatMoney(currentRecord.wechatAmount) }}</span>
                </div>
                <div class="amount-row subtotal">
                  <span class="amount-label">合计</span>
                  <span class="amount-value">¥{{ formatMoney(currentRecord.mobileTotal) }}</span>
                </div>
              </div>

              <div class="amount-row">
                <span class="amount-label">挂账金额</span>
                <span class="amount-value price-warn">¥{{ formatMoney(currentRecord.creditAmount) }}</span>
              </div>
              <div class="amount-row">
                <span class="amount-label">预付金额</span>
                <span class="amount-value">¥{{ formatMoney(currentRecord.prepaidAmount) }}</span>
              </div>
              <div class="amount-row">
                <span class="amount-label">退款金额</span>
                <span class="amount-value price-up">¥{{ formatMoney(currentRecord.refundAmount) }}</span>
              </div>
              <div class="amount-row total-row">
                <span class="amount-label">应收合计</span>
                <span class="amount-value total-amount">¥{{ formatMoney(currentRecord.receivableTotal) }}</span>
              </div>
            </div>
          </el-col>

          <el-col :span="12">
            <div class="section-title">实际收款</div>
            <div class="amount-section">
              <div class="amount-row">
                <span class="amount-label">实际现金</span>
                <span class="amount-value">¥{{ formatMoney(currentRecord.actualCash) }}</span>
              </div>
              <div class="amount-row">
                <span class="amount-label">实际刷卡</span>
                <span class="amount-value">¥{{ formatMoney(currentRecord.actualCard) }}</span>
              </div>
              <div class="amount-row">
                <span class="amount-label">实际移动支付</span>
                <span class="amount-value">¥{{ formatMoney(currentRecord.actualMobile) }}</span>
              </div>
              <div class="amount-row total-row">
                <span class="amount-label">实际合计</span>
                <span class="amount-value total-amount">¥{{ formatMoney(currentRecord.actualTotal) }}</span>
              </div>
            </div>

            <el-divider />

            <div class="diff-item">
              <span class="diff-label">差异金额</span>
              <span :class="currentRecord.difference !== 0 ? 'price-up' : 'price-success'" class="diff-value">
                ¥{{ formatMoney(currentRecord.difference) }}
              </span>
            </div>
            <template v-if="currentRecord.difference !== 0">
              <div class="amount-row" style="margin-top: 12px">
                <span class="amount-label">差异原因</span>
                <span class="amount-value">{{ currentRecord.differenceReason || '-' }}</span>
              </div>
              <div v-if="currentRecord.differenceProofUrls && currentRecord.differenceProofUrls.length" class="amount-row" style="margin-top: 8px">
                <span class="amount-label">差异证明</span>
                <div>
                  <a
                    v-for="(url, idx) in currentRecord.differenceProofUrls"
                    :key="idx"
                    :href="url"
                    target="_blank"
                    class="proof-link"
                  >附件{{ idx + 1 }}</a>
                </div>
              </div>
            </template>
          </el-col>
        </el-row>

        <el-divider />

        <el-descriptions :column="3" border>
          <el-descriptions-item label="对账日期">{{ currentRecord.reconcileDate }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="currentRecord.status === 0 ? 'warning' : 'success'" size="small">
              {{ currentRecord.status === 0 ? '未对账' : '已完成' }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="确认人">{{ currentRecord.confirmBy || '-' }}</el-descriptions-item>
          <el-descriptions-item label="确认时间">{{ currentRecord.confirmTime || '-' }}</el-descriptions-item>
        </el-descriptions>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import api from '@/api'

const loading = ref(false)
const tableData = ref([])
const searchForm = reactive({
  reconcileDate: new Date().toISOString().slice(0, 10),
  status: null
})
const pagination = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0
})

const reconcileDialogVisible = ref(false)
const detailDialogVisible = ref(false)
const currentRecord = ref(null)
const confirmSaving = ref(false)
const reconcileForm = reactive({
  actualCash: 0,
  actualCard: 0,
  actualMobile: 0,
  differenceReason: '',
  differenceProof: []
})

const uploadHeaders = computed(() => {
  const token = localStorage.getItem('token')
  return token ? { Authorization: `Bearer ${token}` } : {}
})

const actualTotal = computed(() => {
  return (reconcileForm.actualCash || 0) + (reconcileForm.actualCard || 0) + (reconcileForm.actualMobile || 0)
})

const difference = computed(() => {
  return actualTotal.value - (currentRecord.value?.receivableTotal || 0)
})

const formatMoney = (val) => {
  if (val === null || val === undefined) return '0.00'
  return Number(val).toFixed(2)
}

const loadData = async () => {
  loading.value = true
  try {
    const res = await api.finance.dailyReconciliation.page({
      pageNum: pagination.pageNum,
      pageSize: pagination.pageSize,
      ...searchForm
    })
    if (res.code === 200) {
      tableData.value = res.data.records
      pagination.total = res.data.total
    }
  } catch {
    ElMessage.error('加载数据失败')
  } finally {
    loading.value = false
  }
}

const resetSearch = () => {
  searchForm.reconcileDate = new Date().toISOString().slice(0, 10)
  searchForm.status = null
  pagination.pageNum = 1
  loadData()
}

const handleAutoCalculate = async () => {
  if (!searchForm.reconcileDate) {
    ElMessage.warning('请选择对账日期')
    return
  }
  try {
    const res = await api.finance.dailyReconciliation.autoCalculate({ reconcileDate: searchForm.reconcileDate })
    if (res.code === 200) {
      ElMessage.success('自动生成成功')
      await loadData()
    } else {
      ElMessage.error(res.message || '自动生成失败')
    }
  } catch {
    ElMessage.error('自动生成失败')
  }
}

const openReconcile = async (row) => {
  try {
    const res = await api.finance.dailyReconciliation.getByDate(row.reconcileDate)
    if (res.code === 200) {
      currentRecord.value = res.data
      reconcileForm.actualCash = res.data.cashTotal || 0
      reconcileForm.actualCard = res.data.cardTotal || 0
      reconcileForm.actualMobile = res.data.mobileTotal || 0
      reconcileForm.differenceReason = ''
      reconcileForm.differenceProof = []
      reconcileDialogVisible.value = true
    }
  } catch {
    ElMessage.error('获取对账数据失败')
  }
}

const handleUploadSuccess = (response) => {
  if (response.code === 200) {
    reconcileForm.differenceProof.push({ name: response.data.fileName || response.data, url: response.data.url || response.data })
  }
}

const handleUploadRemove = (file) => {
  const idx = reconcileForm.differenceProof.findIndex(f => f.url === file.url)
  if (idx > -1) reconcileForm.differenceProof.splice(idx, 1)
}

const handleConfirm = async () => {
  if (!currentRecord.value) return
  confirmSaving.value = true
  try {
    const data = {
      actualCash: reconcileForm.actualCash,
      actualCard: reconcileForm.actualCard,
      actualMobile: reconcileForm.actualMobile,
      differenceReason: reconcileForm.differenceReason,
      differenceProofUrls: reconcileForm.differenceProof.map(f => f.url)
    }
    const res = await api.finance.dailyReconciliation.confirm(currentRecord.value.id, data)
    if (res.code === 200) {
      ElMessage.success('日结确认成功')
      reconcileDialogVisible.value = false
      await loadData()
    } else {
      ElMessage.error(res.message || '确认失败')
    }
  } catch {
    ElMessage.error('确认失败')
  } finally {
    confirmSaving.value = false
  }
}

const viewDetail = async (row) => {
  try {
    const res = await api.finance.dailyReconciliation.getByDate(row.reconcileDate)
    if (res.code === 200) {
      currentRecord.value = res.data
      detailDialogVisible.value = true
    }
  } catch {
    ElMessage.error('获取详情失败')
  }
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.daily-reconciliation {
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

.price-warn {
  color: #e6a23c;
  font-weight: 600;
}

.price-success {
  color: #67c23a;
  font-weight: 600;
}

.section-title {
  font-size: 15px;
  font-weight: 600;
  margin-bottom: 16px;
  padding-bottom: 8px;
  border-bottom: 2px solid #409eff;
}

.amount-section {
  padding: 0 8px;
}

.amount-group {
  margin-bottom: 16px;
  padding: 12px;
  background: #f5f7fa;
  border-radius: 6px;
}

.group-header {
  font-weight: 600;
  font-size: 14px;
  margin-bottom: 8px;
  color: #303133;
}

.amount-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 4px 0;
}

.amount-row.subtotal {
  border-top: 1px dashed #dcdfe6;
  margin-top: 6px;
  padding-top: 8px;
  font-weight: 600;
}

.amount-row.total-row {
  margin-top: 12px;
  padding: 10px 12px;
  background: #ecf5ff;
  border-radius: 6px;
  border: 1px solid #b3d8ff;
}

.amount-label {
  color: #606266;
  font-size: 13px;
}

.amount-value {
  font-size: 13px;
  color: #303133;
}

.total-amount {
  font-size: 18px;
  font-weight: 700;
  color: #409eff;
}

.actual-section {
  padding: 0 8px;
}

.difference-section {
  padding: 0 8px;
}

.diff-item {
  display: flex;
  align-items: center;
  gap: 16px;
}

.diff-label {
  font-size: 14px;
  font-weight: 600;
  color: #303133;
}

.diff-value {
  font-size: 20px;
  font-weight: 700;
}

.proof-link {
  color: #409eff;
  margin-right: 8px;
  text-decoration: underline;
}

.detail-content .amount-section {
  padding: 0;
}
</style>
