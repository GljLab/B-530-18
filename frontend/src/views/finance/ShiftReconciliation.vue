<template>
  <div class="shift-reconciliation">
    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <span class="title">交接班管理</span>
        </div>
      </template>

      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="交接班日期">
          <el-date-picker
            v-model="searchForm.shiftDate"
            type="date"
            placeholder="选择日期"
            value-format="YYYY-MM-DD"
            style="width: 180px"
          />
        </el-form-item>
        <el-form-item label="班次">
          <el-select v-model="searchForm.shiftType" placeholder="全部" clearable style="width: 140px">
            <el-option label="全部" :value="null" />
            <el-option label="早班" :value="1" />
            <el-option label="中班" :value="2" />
            <el-option label="晚班" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="全部" clearable style="width: 140px">
            <el-option label="全部" :value="null" />
            <el-option label="待确认" :value="0" />
            <el-option label="已完成" :value="1" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadData">查询</el-button>
          <el-button type="success" @click="openCreateDialog">创建交接班</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="tableData" v-loading="loading" border stripe style="width: 100%">
        <el-table-column prop="shiftDate" label="交接班日期" width="120" />
        <el-table-column prop="shiftType" label="班次" width="80">
          <template #default="{ row }">
            {{ shiftTypeLabel(row.shiftType) }}
          </template>
        </el-table-column>
        <el-table-column prop="handoverUserName" label="交班人" width="100" />
        <el-table-column prop="takeoverUserName" label="接班人" width="100" />
        <el-table-column prop="cashTotal" label="现金收款" width="110">
          <template #default="{ row }">
            <span class="price">¥{{ formatMoney(row.cashTotal) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="cardTotal" label="刷卡收款" width="110">
          <template #default="{ row }">
            <span class="price">¥{{ formatMoney(row.cardTotal) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="mobileTotal" label="移动支付收款" width="130">
          <template #default="{ row }">
            <span class="price">¥{{ formatMoney(row.mobileTotal) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="creditTotal" label="挂账金额" width="110">
          <template #default="{ row }">
            <span class="price-warn">¥{{ formatMoney(row.creditTotal) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="receivableTotal" label="应收合计" width="110">
          <template #default="{ row }">
            <span class="price">¥{{ formatMoney(row.receivableTotal) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="actualTotal" label="实际合计" width="110">
          <template #default="{ row }">
            <span class="price">¥{{ formatMoney(row.actualTotal) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="difference" label="差异" width="110">
          <template #default="{ row }">
            <span :class="row.difference !== 0 ? 'price-up' : 'price-success'">¥{{ formatMoney(row.difference) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="row.status === 0 ? 'warning' : 'success'" size="small">
              {{ row.status === 0 ? '待确认' : '已完成' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="140" fixed="right">
          <template #default="{ row }">
            <el-button v-if="row.status === 0" type="primary" link size="small" @click="openTakeoverDialog(row)">接班确认</el-button>
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

    <el-dialog v-model="createDialogVisible" title="创建交接班" width="700px" destroy-on-close>
      <el-form ref="createFormRef" :model="createForm" :rules="createRules" label-width="140px">
        <el-form-item label="班次" prop="shiftType">
          <el-select v-model="createForm.shiftType" placeholder="请选择班次" style="width: 100%">
            <el-option label="早班" :value="1" />
            <el-option label="中班" :value="2" />
            <el-option label="晚班" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item label="交班人">
          <el-input v-model="createForm.handoverUserName" readonly />
        </el-form-item>

        <el-divider content-position="left">本班次收款统计</el-divider>

        <el-form-item label="现金收款" prop="cashTotal">
          <el-input-number v-model="createForm.cashTotal" :precision="2" :min="0" :controls="false" style="width: 100%" />
        </el-form-item>
        <el-form-item label="刷卡收款" prop="cardTotal">
          <el-input-number v-model="createForm.cardTotal" :precision="2" :min="0" :controls="false" style="width: 100%" />
        </el-form-item>
        <el-form-item label="移动支付收款" prop="mobileTotal">
          <el-input-number v-model="createForm.mobileTotal" :precision="2" :min="0" :controls="false" style="width: 100%" />
        </el-form-item>
        <el-form-item label="挂账金额" prop="creditTotal">
          <el-input-number v-model="createForm.creditTotal" :precision="2" :min="0" :controls="false" style="width: 100%" />
        </el-form-item>
        <el-form-item label="应收合计">
          <span class="total-amount">¥{{ formatMoney(createReceivableTotal) }}</span>
        </el-form-item>

        <el-divider content-position="left">实际收款</el-divider>

        <el-form-item label="实际现金" prop="actualCash">
          <el-input-number v-model="createForm.actualCash" :precision="2" :min="0" :controls="false" style="width: 100%" />
        </el-form-item>
        <el-form-item label="实际刷卡" prop="actualCard">
          <el-input-number v-model="createForm.actualCard" :precision="2" :min="0" :controls="false" style="width: 100%" />
        </el-form-item>
        <el-form-item label="实际移动支付" prop="actualMobile">
          <el-input-number v-model="createForm.actualMobile" :precision="2" :min="0" :controls="false" style="width: 100%" />
        </el-form-item>
        <el-form-item label="实际合计">
          <span class="total-amount">¥{{ formatMoney(createActualTotal) }}</span>
        </el-form-item>

        <el-divider />

        <el-form-item label="差异金额">
          <span :class="createDifference !== 0 ? 'price-up' : 'price-success'" class="diff-value">
            ¥{{ formatMoney(createDifference) }}
          </span>
        </el-form-item>
        <el-form-item v-if="createDifference !== 0" label="差异原因" prop="differenceReason">
          <el-input v-model="createForm.differenceReason" type="textarea" :rows="3" placeholder="请输入差异原因" />
        </el-form-item>
        <el-form-item label="交班备注">
          <el-input v-model="createForm.remark" type="textarea" :rows="3" placeholder="请输入交班备注" />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="createDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="createSaving" @click="handleCreate">提交</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="takeoverDialogVisible" title="接班确认" width="600px" destroy-on-close>
      <div v-if="takeoverRecord" class="takeover-info">
        <el-descriptions :column="2" border style="margin-bottom: 20px">
          <el-descriptions-item label="交接班日期">{{ takeoverRecord.shiftDate }}</el-descriptions-item>
          <el-descriptions-item label="班次">{{ shiftTypeLabel(takeoverRecord.shiftType) }}</el-descriptions-item>
          <el-descriptions-item label="交班人">{{ takeoverRecord.handoverUserName }}</el-descriptions-item>
          <el-descriptions-item label="应收合计">
            <span class="price">¥{{ formatMoney(takeoverRecord.receivableTotal) }}</span>
          </el-descriptions-item>
          <el-descriptions-item label="实际合计">
            <span class="price">¥{{ formatMoney(takeoverRecord.actualTotal) }}</span>
          </el-descriptions-item>
          <el-descriptions-item label="差异金额">
            <span :class="takeoverRecord.difference !== 0 ? 'price-up' : 'price-success'">
              ¥{{ formatMoney(takeoverRecord.difference) }}
            </span>
          </el-descriptions-item>
        </el-descriptions>

        <el-divider content-position="left">收款明细</el-divider>

        <el-descriptions :column="2" border style="margin-bottom: 20px">
          <el-descriptions-item label="现金收款">¥{{ formatMoney(takeoverRecord.cashTotal) }}</el-descriptions-item>
          <el-descriptions-item label="刷卡收款">¥{{ formatMoney(takeoverRecord.cardTotal) }}</el-descriptions-item>
          <el-descriptions-item label="移动支付收款">¥{{ formatMoney(takeoverRecord.mobileTotal) }}</el-descriptions-item>
          <el-descriptions-item label="挂账金额">¥{{ formatMoney(takeoverRecord.creditTotal) }}</el-descriptions-item>
        </el-descriptions>

        <el-divider content-position="left">实际收款</el-divider>

        <el-descriptions :column="3" border style="margin-bottom: 20px">
          <el-descriptions-item label="实际现金">¥{{ formatMoney(takeoverRecord.actualCash) }}</el-descriptions-item>
          <el-descriptions-item label="实际刷卡">¥{{ formatMoney(takeoverRecord.actualCard) }}</el-descriptions-item>
          <el-descriptions-item label="实际移动支付">¥{{ formatMoney(takeoverRecord.actualMobile) }}</el-descriptions-item>
        </el-descriptions>

        <div v-if="takeoverRecord.difference !== 0" style="margin-bottom: 16px">
          <el-descriptions :column="1" border>
            <el-descriptions-item label="差异原因">{{ takeoverRecord.differenceReason || '-' }}</el-descriptions-item>
          </el-descriptions>
        </div>

        <div v-if="takeoverRecord.remark" style="margin-bottom: 16px">
          <el-descriptions :column="1" border>
            <el-descriptions-item label="交班备注">{{ takeoverRecord.remark }}</el-descriptions-item>
          </el-descriptions>
        </div>

        <el-divider content-position="left">接班信息</el-divider>

        <el-form :model="takeoverForm" label-width="100px">
          <el-form-item label="接班人">
            <el-select v-model="takeoverForm.takeoverUserId" filterable placeholder="请选择接班人" style="width: 100%">
              <el-option
                v-for="user in userList"
                :key="user.id"
                :label="user.realName || user.username"
                :value="user.id"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="接班备注">
            <el-input v-model="takeoverForm.remark" type="textarea" :rows="3" placeholder="请输入接班备注" />
          </el-form-item>
        </el-form>
      </div>

      <template #footer>
        <el-button @click="takeoverDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="takeoverSaving" @click="handleTakeover">确认接班</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="detailDialogVisible" title="交接班详情" width="700px" destroy-on-close>
      <div v-if="detailRecord" class="detail-content">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="交接班日期">{{ detailRecord.shiftDate }}</el-descriptions-item>
          <el-descriptions-item label="班次">{{ shiftTypeLabel(detailRecord.shiftType) }}</el-descriptions-item>
          <el-descriptions-item label="交班人">{{ detailRecord.handoverUserName }}</el-descriptions-item>
          <el-descriptions-item label="接班人">{{ detailRecord.takeoverUserName || '-' }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="detailRecord.status === 0 ? 'warning' : 'success'" size="small">
              {{ detailRecord.status === 0 ? '待确认' : '已完成' }}
            </el-tag>
          </el-descriptions-item>
        </el-descriptions>

        <el-divider content-position="left">本班次收款统计</el-divider>

        <el-descriptions :column="2" border>
          <el-descriptions-item label="现金收款">
            <span class="price">¥{{ formatMoney(detailRecord.cashTotal) }}</span>
          </el-descriptions-item>
          <el-descriptions-item label="刷卡收款">
            <span class="price">¥{{ formatMoney(detailRecord.cardTotal) }}</span>
          </el-descriptions-item>
          <el-descriptions-item label="移动支付收款">
            <span class="price">¥{{ formatMoney(detailRecord.mobileTotal) }}</span>
          </el-descriptions-item>
          <el-descriptions-item label="挂账金额">
            <span class="price-warn">¥{{ formatMoney(detailRecord.creditTotal) }}</span>
          </el-descriptions-item>
          <el-descriptions-item label="应收合计">
            <span class="total-amount">¥{{ formatMoney(detailRecord.receivableTotal) }}</span>
          </el-descriptions-item>
        </el-descriptions>

        <el-divider content-position="left">实际收款</el-divider>

        <el-descriptions :column="3" border>
          <el-descriptions-item label="实际现金">¥{{ formatMoney(detailRecord.actualCash) }}</el-descriptions-item>
          <el-descriptions-item label="实际刷卡">¥{{ formatMoney(detailRecord.actualCard) }}</el-descriptions-item>
          <el-descriptions-item label="实际移动支付">¥{{ formatMoney(detailRecord.actualMobile) }}</el-descriptions-item>
        </el-descriptions>

        <el-divider />

        <div class="detail-diff-row">
          <span class="diff-label">差异金额</span>
          <span :class="detailRecord.difference !== 0 ? 'price-up' : 'price-success'" class="diff-value">
            ¥{{ formatMoney(detailRecord.difference) }}
          </span>
        </div>

        <template v-if="detailRecord.difference !== 0">
          <el-descriptions :column="1" border style="margin-top: 12px">
            <el-descriptions-item label="差异原因">{{ detailRecord.differenceReason || '-' }}</el-descriptions-item>
          </el-descriptions>
        </template>

        <template v-if="detailRecord.remark">
          <el-descriptions :column="1" border style="margin-top: 12px">
            <el-descriptions-item label="交班备注">{{ detailRecord.remark }}</el-descriptions-item>
          </el-descriptions>
        </template>

        <template v-if="detailRecord.takeoverRemark">
          <el-descriptions :column="1" border style="margin-top: 12px">
            <el-descriptions-item label="接班备注">{{ detailRecord.takeoverRemark }}</el-descriptions-item>
          </el-descriptions>
        </template>
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
  shiftDate: new Date().toISOString().slice(0, 10),
  shiftType: null,
  status: null
})
const pagination = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0
})

const createDialogVisible = ref(false)
const createSaving = ref(false)
const createFormRef = ref(null)
const createForm = reactive({
  shiftType: null,
  handoverUserName: '',
  cashTotal: 0,
  cardTotal: 0,
  mobileTotal: 0,
  creditTotal: 0,
  actualCash: 0,
  actualCard: 0,
  actualMobile: 0,
  differenceReason: '',
  remark: ''
})

const createRules = {
  shiftType: [{ required: true, message: '请选择班次', trigger: 'change' }]
}

const takeoverDialogVisible = ref(false)
const takeoverSaving = ref(false)
const takeoverRecord = ref(null)
const userList = ref([])
const takeoverForm = reactive({
  takeoverUserId: null,
  remark: ''
})

const detailDialogVisible = ref(false)
const detailRecord = ref(null)

const createReceivableTotal = computed(() => {
  return (createForm.cashTotal || 0) + (createForm.cardTotal || 0) + (createForm.mobileTotal || 0) + (createForm.creditTotal || 0)
})

const createActualTotal = computed(() => {
  return (createForm.actualCash || 0) + (createForm.actualCard || 0) + (createForm.actualMobile || 0)
})

const createDifference = computed(() => {
  return createActualTotal.value - createReceivableTotal.value
})

const shiftTypeLabel = (type) => {
  const map = { 1: '早班', 2: '中班', 3: '晚班' }
  return map[type] || '-'
}

const formatMoney = (val) => {
  if (val === null || val === undefined) return '0.00'
  return Number(val).toFixed(2)
}

const loadData = async () => {
  loading.value = true
  try {
    const res = await api.finance.shiftReconciliation.page({
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

const loadUserList = async () => {
  try {
    const res = await api.user.list({ pageNum: 1, pageSize: 200, status: 1 })
    if (res.code === 200) {
      userList.value = res.data.records || res.data || []
    }
  } catch {
    userList.value = []
  }
}

const openCreateDialog = async () => {
  createForm.shiftType = null
  createForm.cashTotal = 0
  createForm.cardTotal = 0
  createForm.mobileTotal = 0
  createForm.creditTotal = 0
  createForm.actualCash = 0
  createForm.actualCard = 0
  createForm.actualMobile = 0
  createForm.differenceReason = ''
  createForm.remark = ''
  try {
    const res = await api.auth.getUserInfo()
    if (res.code === 200) {
      createForm.handoverUserName = res.data.realName || res.data.username || ''
    }
  } catch {
    createForm.handoverUserName = ''
  }
  createDialogVisible.value = true
}

const handleCreate = async () => {
  if (!createFormRef.value) return
  await createFormRef.value.validate()
  if (createDifference.value !== 0 && !createForm.differenceReason) {
    ElMessage.warning('存在差异，请填写差异原因')
    return
  }
  createSaving.value = true
  try {
    const data = {
      shiftType: createForm.shiftType,
      cashTotal: createForm.cashTotal,
      cardTotal: createForm.cardTotal,
      mobileTotal: createForm.mobileTotal,
      creditTotal: createForm.creditTotal,
      receivableTotal: createReceivableTotal.value,
      actualCash: createForm.actualCash,
      actualCard: createForm.actualCard,
      actualMobile: createForm.actualMobile,
      actualTotal: createActualTotal.value,
      difference: createDifference.value,
      differenceReason: createForm.differenceReason,
      remark: createForm.remark
    }
    const res = await api.finance.shiftReconciliation.create(data)
    if (res.code === 200) {
      ElMessage.success('创建成功')
      createDialogVisible.value = false
      await loadData()
    } else {
      ElMessage.error(res.message || '创建失败')
    }
  } catch {
    ElMessage.error('创建失败')
  } finally {
    createSaving.value = false
  }
}

const openTakeoverDialog = async (row) => {
  takeoverForm.takeoverUserId = null
  takeoverForm.remark = ''
  try {
    const res = await api.finance.shiftReconciliation.get(row.id)
    if (res.code === 200) {
      takeoverRecord.value = res.data
      takeoverDialogVisible.value = true
    }
  } catch {
    ElMessage.error('获取交接班数据失败')
  }
}

const handleTakeover = async () => {
  if (!takeoverForm.takeoverUserId) {
    ElMessage.warning('请选择接班人')
    return
  }
  takeoverSaving.value = true
  try {
    const res = await api.finance.shiftReconciliation.takeover(takeoverRecord.value.id, {
      takeoverUserId: takeoverForm.takeoverUserId,
      remark: takeoverForm.remark
    })
    if (res.code === 200) {
      ElMessage.success('接班确认成功')
      takeoverDialogVisible.value = false
      await loadData()
    } else {
      ElMessage.error(res.message || '接班确认失败')
    }
  } catch {
    ElMessage.error('接班确认失败')
  } finally {
    takeoverSaving.value = false
  }
}

const viewDetail = async (row) => {
  try {
    const res = await api.finance.shiftReconciliation.get(row.id)
    if (res.code === 200) {
      detailRecord.value = res.data
      detailDialogVisible.value = true
    }
  } catch {
    ElMessage.error('获取详情失败')
  }
}

onMounted(() => {
  loadData()
  loadUserList()
})
</script>

<style scoped>
.shift-reconciliation {
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

.total-amount {
  font-size: 18px;
  font-weight: 700;
  color: #409eff;
}

.diff-value {
  font-size: 18px;
  font-weight: 700;
}

.diff-label {
  font-size: 14px;
  font-weight: 600;
  color: #303133;
}

.detail-diff-row {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 8px 0;
}

.takeover-info {
  padding: 0 4px;
}

.detail-content {
  padding: 0 4px;
}
</style>
