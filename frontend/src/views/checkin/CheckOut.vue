<template>
  <div class="checkout-page" v-loading="loading">
    <div class="page-header">
      <div class="header-left">
        <el-button @click="goBack" :icon="ArrowLeft">返回</el-button>
        <h2 class="page-title">办理退房</h2>
      </div>
    </div>

    <el-card v-if="!hasCheckInId" class="select-checkin-card">
      <template #header>
        <span>选择入住单</span>
      </template>
      <el-form :inline="true" :model="searchForm" style="margin-bottom: 16px">
        <el-form-item label="入住单号">
          <el-input v-model="searchForm.keyword" placeholder="请输入入住单号/客户姓名/手机号/房号" style="width: 300px" />
        </el-form-item>
        <el-form-item label="入住状态">
          <el-select v-model="searchForm.status" placeholder="全部" style="width: 150px" clearable>
            <el-option label="在住" :value="1" />
            <el-option label="超期未退" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="searchCheckIns" :icon="Search">查询</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="checkInList" v-loading="loadingList" style="width: 100%">
        <el-table-column prop="checkInNo" label="入住单号" width="200" />
        <el-table-column prop="customerName" label="客户姓名" width="100" />
        <el-table-column prop="customerPhone" label="手机号" width="130" />
        <el-table-column prop="roomTypeName" label="房型" width="100" />
        <el-table-column prop="roomNumber" label="房号" width="80" />
        <el-table-column prop="checkInDate" label="入住日期" width="110" />
        <el-table-column prop="checkOutDate" label="预计退房" width="110" />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag v-if="row.isOverdue === 1" type="danger" size="small">超期未退</el-tag>
            <el-tag v-else type="success" size="small">在住</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="已住天数" width="100">
          <template #default="{ row }">
            {{ calculateStayedDays(row.checkInDate) }}天
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120">
          <template #default="{ row }">
            <el-button type="primary" link @click="selectCheckIn(row)">
              办理退房
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <div style="margin-top: 16px; display: flex; justify-content: flex-end">
        <el-pagination
          v-model:current-page="searchForm.pageNum"
          v-model:page-size="searchForm.pageSize"
          :page-sizes="[10, 20, 50]"
          :total="total"
          layout="total, sizes, prev, pager, next"
          @size-change="searchCheckIns"
          @current-change="searchCheckIns"
        />
      </div>
    </el-card>

    <el-row v-if="hasCheckInId" :gutter="16">
      <el-col :span="16">
        <el-card class="info-card">
          <template #header>
            <div class="card-header">
              <span class="card-title">入住信息</span>
            </div>
          </template>
          <el-descriptions :column="3" border>
            <el-descriptions-item label="入住单号">{{ checkIn.checkInNo }}</el-descriptions-item>
            <el-descriptions-item label="客户姓名">{{ checkIn.customerName }}</el-descriptions-item>
            <el-descriptions-item label="房号">{{ checkIn.roomNumber }}</el-descriptions-item>
            <el-descriptions-item label="房型">{{ checkIn.roomTypeName }}</el-descriptions-item>
            <el-descriptions-item label="入住日期">{{ checkIn.checkInDate }}</el-descriptions-item>
            <el-descriptions-item label="已住天数">
              <span style="color: #409eff; font-weight: bold">{{ stayedDays }}天</span>
            </el-descriptions-item>
          </el-descriptions>
        </el-card>

        <el-card class="info-card" style="margin-top: 16px">
          <template #header>
            <div class="card-header">
              <span class="card-title">费用明细</span>
            </div>
          </template>
          <el-descriptions :column="3" border>
            <el-descriptions-item label="房费">¥{{ checkoutForm.roomFee || 0 }}</el-descriptions-item>
            <el-descriptions-item label="加床费">¥{{ checkoutForm.extraBedFee || 0 }}</el-descriptions-item>
            <el-descriptions-item label="其他消费">¥{{ checkoutForm.otherFee || 0 }}</el-descriptions-item>
            <el-descriptions-item label="损坏赔偿">¥{{ checkoutForm.damageFee || 0 }}</el-descriptions-item>
            <el-descriptions-item label="优惠折扣">¥{{ checkoutForm.discount || 0 }}</el-descriptions-item>
            <el-descriptions-item label="应付总额" class="highlight">
              <span style="color: #f56c6c; font-weight: bold">¥{{ totalAmount }}</span>
            </el-descriptions-item>
            <el-descriptions-item label="已付金额">¥{{ checkoutForm.paidAmount || 0 }}</el-descriptions-item>
            <el-descriptions-item label="押金抵扣">¥{{ depositDeduction }}</el-descriptions-item>
            <el-descriptions-item label="待结算金额" class="highlight">
              <span :style="{ color: payableAmount > 0 ? '#f56c6c' : '#67c23a', fontWeight: 'bold' }">
                ¥{{ payableAmount }}
              </span>
            </el-descriptions-item>
          </el-descriptions>
        </el-card>

        <el-card class="info-card" style="margin-top: 16px">
          <template #header>
            <div class="card-header">
              <span class="card-title">房间检查</span>
              <el-switch v-model="roomInspectionEnabled" active-text="已检查" inactive-text="未检查" />
            </div>
          </template>
          <el-form :model="inspectionForm" label-width="100px" v-if="roomInspectionEnabled">
            <el-form-item label="检查结果">
              <el-radio-group v-model="inspectionForm.result">
                <el-radio :value="1">正常</el-radio>
                <el-radio :value="2">有损坏</el-radio>
              </el-radio-group>
            </el-form-item>
            <el-form-item label="损坏物品" v-if="inspectionForm.result === 2">
              <el-input v-model="inspectionForm.damagedItems" placeholder="请输入损坏物品名称" />
            </el-form-item>
            <el-form-item label="损坏描述" v-if="inspectionForm.result === 2">
              <el-input
                v-model="inspectionForm.damageDescription"
                type="textarea"
                :rows="3"
                placeholder="请输入损坏情况描述"
              />
            </el-form-item>
            <el-form-item label="赔偿金额" v-if="inspectionForm.result === 2">
              <el-input-number
                v-model="inspectionForm.compensationAmount"
                :min="0"
                :precision="2"
                style="width: 200px"
                @change="handleCompensationChange"
              />
              <span style="margin-left: 8px; color: #909399">元</span>
            </el-form-item>
          </el-form>
          <div v-else style="color: #909399; text-align: center; padding: 20px 0">
            暂未进行房间检查
          </div>
        </el-card>
      </el-col>

      <el-col :span="8">
        <el-card class="side-card">
          <template #header>
            <span class="card-title">押金信息</span>
          </template>
          <div class="deposit-info">
            <div class="deposit-item">
              <span class="label">押金金额</span>
              <span class="value">¥{{ checkIn.depositAmount || 0 }}</span>
            </div>
            <div class="deposit-item">
              <span class="label">押金方式</span>
              <span class="value">{{ getDepositMethodText(checkIn.depositMethod) }}</span>
            </div>
            <el-divider style="margin: 12px 0" />
            <div class="deposit-item">
              <span class="label">押金抵扣</span>
              <el-switch v-model="useDepositDeduction" />
            </div>
            <div class="deposit-item" v-if="useDepositDeduction">
              <span class="label">抵扣金额</span>
              <span class="value highlight">¥{{ depositDeduction }}</span>
            </div>
            <div class="deposit-item">
              <span class="label">押金退还</span>
              <span class="value" :class="{ 'refund': depositRefund > 0 }">
                ¥{{ depositRefund }}
              </span>
            </div>
          </div>
        </el-card>

        <el-card class="side-card" style="margin-top: 16px">
          <template #header>
            <span class="card-title">房卡回收</span>
          </template>
          <div class="keycard-info">
            <div class="keycard-item">
              <span class="label">发放数量</span>
              <span class="value">{{ checkIn.keyCardCount || 0 }} 张</span>
            </div>
            <div class="keycard-item">
              <span class="label">回收数量</span>
              <el-input-number
                v-model="checkoutForm.returnedKeyCards"
                :min="0"
                :max="checkIn.keyCardCount || 0"
                size="small"
                style="width: 120px"
              />
            </div>
            <div class="keycard-item">
              <span class="label">遗失数量</span>
              <span class="value" style="color: #f56c6c">
                {{ (checkIn.keyCardCount || 0) - (checkoutForm.returnedKeyCards || 0) }} 张
              </span>
            </div>
          </div>
        </el-card>

        <el-card class="side-card" style="margin-top: 16px">
          <template #header>
            <span class="card-title">支付方式</span>
          </template>
          <el-radio-group v-model="checkoutForm.paymentMethod" style="width: 100%">
            <el-radio :value="1" style="display: block; margin: 0 0 12px 0">现金</el-radio>
            <el-radio :value="2" style="display: block; margin: 0 0 12px 0">刷卡</el-radio>
            <el-radio :value="3" style="display: block; margin: 0 0 12px 0">移动支付</el-radio>
            <el-radio :value="4" style="display: block; margin: 0 0 12px 0">押金抵扣</el-radio>
            <el-radio :value="5" style="display: block; margin: 0 0 12px 0">协议挂账</el-radio>
          </el-radio-group>
        </el-card>

        <el-card class="side-card" style="margin-top: 16px">
          <template #header>
            <span class="card-title">结算信息</span>
          </template>
          <div class="payment-summary">
            <div class="summary-row total">
              <span class="label">应付总额</span>
              <span class="value">¥{{ totalAmount }}</span>
            </div>
            <div class="summary-row">
              <span class="label">已付金额</span>
              <span class="value">¥{{ checkoutForm.paidAmount || 0 }}</span>
            </div>
            <div class="summary-row">
              <span class="label">押金抵扣</span>
              <span class="value">-¥{{ depositDeduction }}</span>
            </div>
            <el-divider style="margin: 8px 0" />
            <div class="summary-row payable">
              <span class="label">待结算金额</span>
              <span class="value">¥{{ payableAmount }}</span>
            </div>
            <div class="summary-row refund" v-if="depositRefund > 0">
              <span class="label">应退押金</span>
              <span class="value">¥{{ depositRefund }}</span>
            </div>
          </div>
        </el-card>

        <div class="action-section">
          <el-button type="primary" size="large" style="width: 100%" :loading="submitting" @click="handleCheckout">
            确认退房
          </el-button>
        </div>
      </el-col>
    </el-row>

    <el-dialog v-model="successDialogVisible" title="退房成功" width="500px" :close-on-click-modal="false">
      <div class="success-content">
        <div class="success-icon">
          <el-icon :size="64" color="#67c23a"><CircleCheckFilled /></el-icon>
        </div>
        <div class="success-title">退房办理成功</div>
        <div class="success-info">
          <div class="info-row">
            <span class="label">退房单号：</span>
            <span class="value highlight">{{ checkoutResult.checkOutNo }}</span>
          </div>
          <div class="info-row">
            <span class="label">入住单号：</span>
            <span class="value">{{ checkIn.checkInNo }}</span>
          </div>
          <div class="info-row">
            <span class="label">房号：</span>
            <span class="value">{{ checkIn.roomNumber }}</span>
          </div>
          <div class="info-row">
            <span class="label">实收金额：</span>
            <span class="value">¥{{ checkoutResult.actualPaid || 0 }}</span>
          </div>
          <div class="info-row" v-if="depositRefund > 0">
            <span class="label">退还押金：</span>
            <span class="value" style="color: #67c23a">¥{{ depositRefund }}</span>
          </div>
        </div>
      </div>
      <template #footer>
        <el-button @click="goToList">返回列表</el-button>
        <el-button type="primary" @click="successDialogVisible = false">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ArrowLeft, CircleCheckFilled, Search } from '@element-plus/icons-vue'
import api from '@/api'

const route = useRoute()
const router = useRouter()

const loading = ref(false)
const loadingList = ref(false)
const submitting = ref(false)
const successDialogVisible = ref(false)
const roomInspectionEnabled = ref(false)
const useDepositDeduction = ref(true)

const checkInId = ref(route.query.checkInId ? Number(route.query.checkInId) : null)
const hasCheckInId = computed(() => !!checkInId.value)

const checkInList = ref([])
const total = ref(0)

const searchForm = reactive({
  keyword: '',
  status: null,
  pageNum: 1,
  pageSize: 10
})

const checkIn = ref({
  guests: [],
  keyCards: [],
  consumptions: []
})

const checkoutResult = ref({})

const checkoutForm = reactive({
  checkInId: null,
  roomFee: 0,
  extraBedFee: 0,
  otherFee: 0,
  damageFee: 0,
  discount: 0,
  paidAmount: 0,
  paymentMethod: 1,
  voucherNo: '',
  depositMethod: 1,
  returnedKeyCards: 0,
  remark: ''
})

const inspectionForm = reactive({
  result: 1,
  damagedItems: '',
  damageDescription: '',
  compensationAmount: 0
})

const stayedDays = computed(() => {
  if (checkIn.value.stayedDays) {
    return checkIn.value.stayedDays
  }
  const checkInDate = new Date(checkIn.value.checkInDate)
  const today = new Date()
  const diff = Math.floor((today - checkInDate) / (1000 * 60 * 60 * 24))
  return diff < 0 ? 0 : diff
})

const totalAmount = computed(() => {
  const roomFee = Number(checkoutForm.roomFee) || 0
  const extraBedFee = Number(checkoutForm.extraBedFee) || 0
  const otherFee = Number(checkoutForm.otherFee) || 0
  const damageFee = Number(checkoutForm.damageFee) || 0
  const discount = Number(checkoutForm.discount) || 0
  const total = roomFee + extraBedFee + otherFee + damageFee - discount
  return total < 0 ? 0 : total.toFixed(2)
})

const depositDeduction = computed(() => {
  if (!useDepositDeduction.value) return 0
  const deposit = Number(checkIn.value.depositAmount) || 0
  const payable = Number(totalAmount.value) - Number(checkoutForm.paidAmount || 0)
  if (payable <= 0) return 0
  return deposit >= payable ? payable.toFixed(2) : deposit.toFixed(2)
})

const payableAmount = computed(() => {
  const total = Number(totalAmount.value) || 0
  const paid = Number(checkoutForm.paidAmount) || 0
  const depositDeduct = Number(depositDeduction.value) || 0
  const payable = total - paid - depositDeduct
  return payable.toFixed(2)
})

const depositRefund = computed(() => {
  const deposit = Number(checkIn.value.depositAmount) || 0
  const deduct = Number(depositDeduction.value) || 0
  const refund = deposit - deduct
  return refund > 0 ? refund.toFixed(2) : 0
})

const loadDetail = async () => {
  if (!checkInId.value) {
    return
  }
  loading.value = true
  try {
    const res = await api.checkin.get(checkInId.value)
    if (res.code === 200) {
      checkIn.value = res.data
      checkoutForm.checkInId = checkInId.value
      checkoutForm.roomFee = res.data.roomTotal || 0
      checkoutForm.extraBedFee = res.data.extraBedTotal || 0
      checkoutForm.otherFee = res.data.otherFee || 0
      checkoutForm.discount = res.data.discount || 0
      checkoutForm.paidAmount = res.data.paidAmount || 0
      checkoutForm.returnedKeyCards = res.data.keyCardCount || 0
    }
  } catch (e) {
    console.error('加载入住单详情失败', e)
  } finally {
    loading.value = false
  }
}

const searchCheckIns = async () => {
  loadingList.value = true
  try {
    const params = {
      keyword: searchForm.keyword || undefined,
      status: searchForm.status || undefined,
      isOverdue: searchForm.status === 3 ? 1 : undefined,
      pageNum: searchForm.pageNum,
      pageSize: searchForm.pageSize
    }
    const res = await api.checkin.list(params)
    if (res.code === 200) {
      checkInList.value = res.data.records || []
      total.value = res.data.total || 0
    }
  } catch (e) {
    console.error('查询入住单失败', e)
  } finally {
    loadingList.value = false
  }
}

const selectCheckIn = (row) => {
  checkInId.value = row.id
  router.replace({ query: { checkInId: row.id } })
  loadDetail()
}

const calculateStayedDays = (checkInDate) => {
  if (!checkInDate) return 0
  const start = new Date(checkInDate)
  const today = new Date()
  const diff = Math.floor((today - start) / (1000 * 60 * 60 * 24))
  return diff < 0 ? 0 : diff
}

const handleCompensationChange = (value) => {
  checkoutForm.damageFee = value
}

const handleCheckout = async () => {
  if (payableAmount.value > 0 && !checkoutForm.paymentMethod) {
    ElMessage.warning('请选择支付方式')
    return
  }
  
  if (Number(checkoutForm.returnedKeyCards) > Number(checkIn.value.keyCardCount || 0)) {
    ElMessage.warning('回收房卡数量不能大于发放数量')
    return
  }

  try {
    await ElMessageBox.confirm(
      '确认办理退房吗？退房后将无法撤销。',
      '确认退房',
      {
        confirmButtonText: '确认退房',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
  } catch {
    return
  }

  submitting.value = true
  try {
    const data = {
      checkInId: checkInId.value,
      paymentMethod: checkoutForm.paymentMethod,
      paymentVoucherNo: checkoutForm.voucherNo || '',
      depositMethod: checkoutForm.depositMethod || 1,
      keyCardReturned: checkoutForm.returnedKeyCards,
      keyCardLost: (checkIn.value.keyCardCount || 0) - checkoutForm.returnedKeyCards,
      roomChecked: roomInspectionEnabled.value ? 1 : 0,
      roomCheckResult: roomInspectionEnabled.value ? inspectionForm.result : null,
      damageItems: inspectionForm.damagedItems || null,
      damageDescription: inspectionForm.damageDescription || null,
      damageCompensation: inspectionForm.compensationAmount || 0,
      remark: checkoutForm.remark
    }

    const res = await api.checkin.checkout(data)
    if (res.code === 200) {
      checkoutResult.value = res.data
      successDialogVisible.value = true
    }
  } catch (e) {
    console.error('办理退房失败', e)
  } finally {
    submitting.value = false
  }
}

const goBack = () => {
  if (hasCheckInId.value) {
    checkInId.value = null
    router.replace({ query: {} })
    searchCheckIns()
  } else {
    router.back()
  }
}

const goToList = () => {
  successDialogVisible.value = false
  router.push('/checkin/list')
}

const getDepositMethodText = (method) => {
  const map = { 1: '现金押金', 2: '信用卡预授权', 3: '免押金' }
  return map[method] || '未知'
}

onMounted(() => {
  if (hasCheckInId.value) {
    loadDetail()
  } else {
    searchCheckIns()
  }
})
</script>

<style scoped lang="scss">
.checkout-page {
  .select-checkin-card {
    margin-bottom: 16px;
  }

  .page-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 16px;
    padding: 16px 20px;
    background: #fff;
    border-radius: 8px;
    box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);

    .header-left {
      display: flex;
      align-items: center;
      gap: 16px;

      .page-title {
        margin: 0;
        font-size: 20px;
        font-weight: bold;
        color: #303133;
      }
    }
  }

  .info-card {
    .card-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
    }

    .card-title {
      font-weight: bold;
      font-size: 16px;
    }

    .highlight {
      :deep(.el-descriptions-item__content) {
        font-weight: bold;
      }
    }
  }

  .side-card {
    .card-title {
      font-weight: bold;
    }

    .deposit-info {
      .deposit-item {
        display: flex;
        justify-content: space-between;
        align-items: center;
        padding: 8px 0;

        .label {
          color: #909399;
          font-size: 14px;
        }

        .value {
          color: #303133;
          font-size: 14px;
          font-weight: 500;

          &.highlight {
            color: #409eff;
            font-weight: bold;
          }

          &.refund {
            color: #67c23a;
            font-weight: bold;
          }
        }
      }
    }

    .keycard-info {
      .keycard-item {
        display: flex;
        justify-content: space-between;
        align-items: center;
        padding: 8px 0;

        .label {
          color: #909399;
          font-size: 14px;
        }

        .value {
          color: #303133;
          font-size: 14px;
          font-weight: 500;
        }
      }
    }

    .payment-summary {
      .summary-row {
        display: flex;
        justify-content: space-between;
        align-items: center;
        padding: 6px 0;

        .label {
          color: #909399;
          font-size: 14px;
        }

        .value {
          color: #303133;
          font-size: 14px;
          font-weight: 500;
        }

        &.total {
          .value {
            font-size: 16px;
            font-weight: bold;
            color: #f56c6c;
          }
        }

        &.payable {
          .value {
            font-size: 18px;
            font-weight: bold;
            color: #f56c6c;
          }
        }

        &.refund {
          .value {
            color: #67c23a;
          }
        }
      }
    }
  }

  .action-section {
    margin-top: 20px;
    position: sticky;
    bottom: 0;
    padding-top: 16px;
    background: #f5f7fa;
  }
}

.success-content {
  text-align: center;
  padding: 20px 0;

  .success-icon {
    margin-bottom: 16px;
  }

  .success-title {
    font-size: 20px;
    font-weight: bold;
    color: #303133;
    margin-bottom: 20px;
  }

  .success-info {
    background: #f5f7fa;
    border-radius: 8px;
    padding: 16px;
    text-align: left;

    .info-row {
      display: flex;
      padding: 8px 0;

      .label {
        color: #909399;
        width: 80px;
      }

      .value {
        color: #303133;
        font-weight: 500;

        &.highlight {
          color: #409eff;
          font-weight: bold;
          font-size: 16px;
        }
      }
    }
  }
}
</style>
