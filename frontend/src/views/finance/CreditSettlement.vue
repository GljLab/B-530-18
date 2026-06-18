<template>
  <div class="credit-settlement">
    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <span class="card-title">挂账结算</span>
        </div>
      </template>

      <el-form :model="queryForm" inline>
        <el-form-item label="协议单位">
          <el-select
            v-model="queryForm.agreementUnitId"
            placeholder="请选择协议单位"
            clearable
            filterable
            style="width: 240px"
          >
            <el-option
              v-for="item in agreementUnitList"
              :key="item.id"
              :label="item.name"
              :value="item.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="结算周期">
          <el-date-picker
            v-model="queryForm.dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            value-format="YYYY-MM-DD"
            style="width: 320px"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="billsLoading" @click="handleQueryBills">
            查询挂账账单
          </el-button>
          <el-button
            type="success"
            :disabled="selectedBills.length === 0"
            @click="handleOpenCreateDialog"
          >
            创建结算单
          </el-button>
        </el-form-item>
      </el-form>

      <el-table
        ref="billsTableRef"
        v-loading="billsLoading"
        :data="billsList"
        border
        @selection-change="handleSelectionChange"
        style="width: 100%"
      >
        <el-table-column type="selection" width="50" align="center" />
        <el-table-column prop="billNo" label="账单号" min-width="140" />
        <el-table-column prop="customerName" label="客户姓名" min-width="100" />
        <el-table-column prop="roomNumber" label="房号" min-width="80" />
        <el-table-column prop="checkInDate" label="入住日期" min-width="110" />
        <el-table-column prop="checkOutDate" label="退房日期" min-width="110" />
        <el-table-column prop="roomFee" label="房费" min-width="100" align="right">
          <template #default="{ row }">
            ¥{{ formatMoney(row.roomFee) }}
          </template>
        </el-table-column>
        <el-table-column prop="extraFee" label="额外费用" min-width="100" align="right">
          <template #default="{ row }">
            ¥{{ formatMoney(row.extraFee) }}
          </template>
        </el-table-column>
        <el-table-column prop="discountAmount" label="折扣金额" min-width="100" align="right">
          <template #default="{ row }">
            ¥{{ formatMoney(row.discountAmount) }}
          </template>
        </el-table-column>
        <el-table-column prop="totalAmount" label="总金额" min-width="110" align="right">
          <template #default="{ row }">
            <span class="money-text">¥{{ formatMoney(row.totalAmount) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" min-width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 0 ? 'warning' : 'success'" size="small">
              {{ row.status === 0 ? '待结算' : '已结算' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="billTime" label="挂账时间" min-width="160" />
      </el-table>

      <div v-if="selectedBills.length > 0" class="selected-summary">
        <el-tag type="info" size="large">
          已选 {{ selectedBills.length }} 条账单，合计金额：
          <span class="summary-money">¥{{ formatMoney(selectedTotalAmount) }}</span>
        </el-tag>
      </div>
    </el-card>

    <el-dialog
      v-model="createDialogVisible"
      title="创建结算单"
      width="600px"
      :close-on-click-modal="false"
    >
      <el-form
        ref="createFormRef"
        :model="createForm"
        :rules="createRules"
        label-width="110px"
        label-position="right"
      >
        <el-form-item label="协议单位名称">
          <el-input v-model="createForm.agreementUnitName" readonly />
        </el-form-item>
        <el-form-item label="结算周期">
          <el-input v-model="createForm.settlementPeriod" readonly />
        </el-form-item>
        <el-form-item label="账单数量">
          <el-input v-model="createForm.billCount" readonly />
        </el-form-item>
        <el-form-item label="结算总额">
          <el-input :model-value="'¥' + formatMoney(createForm.totalAmount)" readonly>
            <template #prefix>
              <span style="color: #f56c6c; font-weight: 600">¥</span>
            </template>
          </el-input>
        </el-form-item>
        <el-form-item label="结算方式" prop="settlementMethod">
          <el-select v-model="createForm.settlementMethod" placeholder="请选择结算方式" style="width: 100%">
            <el-option label="银行转账" :value="1" />
            <el-option label="现金" :value="2" />
            <el-option label="支票" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item label="结算日期" prop="settlementDate">
          <el-date-picker
            v-model="createForm.settlementDate"
            type="date"
            placeholder="请选择结算日期"
            value-format="YYYY-MM-DD"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="结算凭证号" prop="voucherNo">
          <el-input v-model="createForm.voucherNo" placeholder="请输入结算凭证号" maxlength="50" />
        </el-form-item>
        <el-form-item label="发票号" prop="invoiceNo">
          <el-input v-model="createForm.invoiceNo" placeholder="请输入发票号" maxlength="50" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input
            v-model="createForm.remark"
            type="textarea"
            :rows="3"
            placeholder="请输入备注"
            maxlength="200"
            show-word-limit
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="createDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="createLoading" @click="handleCreateSettlement">
          确认提交
        </el-button>
      </template>
    </el-dialog>

    <el-card shadow="never" style="margin-top: 16px">
      <template #header>
        <div class="card-header">
          <span class="card-title">结算记录</span>
        </div>
      </template>

      <el-table v-loading="recordsLoading" :data="recordsList" border style="width: 100%">
        <el-table-column prop="settlementNo" label="结算单号" min-width="160" />
        <el-table-column prop="agreementUnitName" label="协议单位" min-width="140" />
        <el-table-column prop="settlementPeriod" label="结算周期" min-width="200" />
        <el-table-column prop="billCount" label="账单数量" min-width="90" align="center" />
        <el-table-column prop="totalAmount" label="结算总额" min-width="120" align="right">
          <template #default="{ row }">
            <span class="money-text">¥{{ formatMoney(row.totalAmount) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="settlementMethod" label="结算方式" min-width="100" align="center">
          <template #default="{ row }">
            {{ settlementMethodLabel(row.settlementMethod) }}
          </template>
        </el-table-column>
        <el-table-column prop="settlementDate" label="结算日期" min-width="110" />
        <el-table-column prop="status" label="状态" min-width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 0 ? 'warning' : 'success'" size="small">
              {{ row.status === 0 ? '待确认' : '已完成' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" min-width="90" align="center" fixed="right">
          <template #default="{ row }">
            <el-button
              v-if="row.status === 0"
              type="primary"
              link
              size="small"
              @click="handleConfirmSettlement(row)"
            >
              确认
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:current-page="recordsPagination.pageNum"
        v-model:page-size="recordsPagination.pageSize"
        :total="recordsPagination.total"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next, jumper"
        class="pagination"
        @size-change="loadRecords"
        @current-change="loadRecords"
      />
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import api from '@/api'

const queryForm = reactive({
  agreementUnitId: '',
  dateRange: null
})

const agreementUnitList = ref([])
const billsList = ref([])
const billsLoading = ref(false)
const selectedBills = ref([])
const billsTableRef = ref(null)

const createDialogVisible = ref(false)
const createLoading = ref(false)
const createFormRef = ref(null)

const createForm = reactive({
  agreementUnitId: '',
  agreementUnitName: '',
  settlementPeriod: '',
  billCount: 0,
  totalAmount: 0,
  settlementMethod: null,
  settlementDate: '',
  voucherNo: '',
  invoiceNo: '',
  remark: ''
})

const createRules = {
  settlementMethod: [{ required: true, message: '请选择结算方式', trigger: 'change' }],
  settlementDate: [{ required: true, message: '请选择结算日期', trigger: 'change' }]
}

const recordsList = ref([])
const recordsLoading = ref(false)
const recordsPagination = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0
})

const selectedTotalAmount = computed(() => {
  return selectedBills.value.reduce((sum, bill) => sum + (Number(bill.totalAmount) || 0), 0)
})

const formatMoney = (value) => {
  const num = Number(value) || 0
  return num.toFixed(2)
}

const settlementMethodLabel = (method) => {
  const map = { 1: '银行转账', 2: '现金', 3: '支票' }
  return map[method] || '-'
}

const loadAgreementUnits = async () => {
  try {
    const res = await api.finance.agreementUnit.list()
    if (res.code === 200) {
      agreementUnitList.value = res.data?.records || res.data?.list || res.data || []
    }
  } catch {
    agreementUnitList.value = []
  }
}

const handleQueryBills = async () => {
  if (!queryForm.agreementUnitId) {
    ElMessage.warning('请选择协议单位')
    return
  }
  if (!queryForm.dateRange || queryForm.dateRange.length < 2) {
    ElMessage.warning('请选择结算周期')
    return
  }
  billsLoading.value = true
  try {
    const params = {
      agreementUnitId: queryForm.agreementUnitId,
      startDate: queryForm.dateRange[0],
      endDate: queryForm.dateRange[1]
    }
    const res = await api.finance.creditSettlement.bills(params)
    if (res.code === 200) {
      billsList.value = res.data?.records || res.data?.list || res.data || []
    } else {
      ElMessage.error(res.message || '查询失败')
    }
  } catch {
    ElMessage.error('查询失败，请重试')
  } finally {
    billsLoading.value = false
  }
}

const handleSelectionChange = (selection) => {
  selectedBills.value = selection
}

const handleOpenCreateDialog = () => {
  const unit = agreementUnitList.value.find(u => u.id === queryForm.agreementUnitId)
  createForm.agreementUnitId = queryForm.agreementUnitId
  createForm.agreementUnitName = unit?.name || ''
  createForm.settlementPeriod = queryForm.dateRange ? `${queryForm.dateRange[0]} 至 ${queryForm.dateRange[1]}` : ''
  createForm.billCount = selectedBills.value.length
  createForm.totalAmount = selectedTotalAmount.value
  createForm.settlementMethod = null
  createForm.settlementDate = ''
  createForm.voucherNo = ''
  createForm.invoiceNo = ''
  createForm.remark = ''
  createDialogVisible.value = true
}

const handleCreateSettlement = async () => {
  const valid = await createFormRef.value.validate().catch(() => false)
  if (!valid) return
  createLoading.value = true
  try {
    const payload = {
      agreementUnitId: createForm.agreementUnitId,
      agreementUnitName: createForm.agreementUnitName,
      startDate: queryForm.dateRange?.[0],
      endDate: queryForm.dateRange?.[1],
      billIds: selectedBills.value.map(b => b.id || b.billId),
      billCount: createForm.billCount,
      totalAmount: createForm.totalAmount,
      settlementMethod: createForm.settlementMethod,
      settlementDate: createForm.settlementDate,
      voucherNo: createForm.voucherNo,
      invoiceNo: createForm.invoiceNo,
      remark: createForm.remark
    }
    const res = await api.finance.creditSettlement.create(payload)
    if (res.code === 200) {
      ElMessage.success('结算单创建成功')
      createDialogVisible.value = false
      handleQueryBills()
      loadRecords()
    } else {
      ElMessage.error(res.message || '创建失败')
    }
  } catch {
    ElMessage.error('创建失败，请重试')
  } finally {
    createLoading.value = false
  }
}

const loadRecords = async () => {
  recordsLoading.value = true
  try {
    const params = {
      pageNum: recordsPagination.pageNum,
      pageSize: recordsPagination.pageSize
    }
    const res = await api.finance.creditSettlement.page(params)
    if (res.code === 200) {
      const data = res.data
      recordsList.value = data?.records || data?.list || []
      recordsPagination.total = data?.total || 0
    }
  } catch {
    recordsList.value = []
  } finally {
    recordsLoading.value = false
  }
}

const handleConfirmSettlement = async (row) => {
  try {
    await ElMessageBox.confirm('确认该结算单已完成结算？', '确认结算', {
      confirmButtonText: '确认',
      cancelButtonText: '取消',
      type: 'warning'
    })
    const res = await api.finance.creditSettlement.confirm(row.id)
    if (res.code === 200) {
      ElMessage.success('确认成功')
      loadRecords()
      if (queryForm.agreementUnitId && queryForm.dateRange) {
        handleQueryBills()
      }
    } else {
      ElMessage.error(res.message || '确认失败')
    }
  } catch {
    //
  }
}

onMounted(() => {
  loadAgreementUnits()
  loadRecords()
})
</script>

<style scoped lang="scss">
.credit-settlement {
  padding: 16px;
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.card-title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.money-text {
  color: #f56c6c;
  font-weight: 600;
}

.selected-summary {
  margin-top: 16px;
  padding: 12px 16px;
  background: #f5f7fa;
  border-radius: 4px;
}

.summary-money {
  color: #f56c6c;
  font-weight: 600;
  font-size: 16px;
}

.pagination {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}
</style>
