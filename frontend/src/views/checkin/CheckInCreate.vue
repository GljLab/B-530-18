<template>
  <div class="checkin-create">
    <div class="page-header">
      <el-button @click="goBack" :icon="ArrowLeft">返回</el-button>
      <h2>办理入住</h2>
    </div>

    <el-card v-if="!bookingId" class="select-booking-card">
      <template #header>
        <span>选择预订单</span>
      </template>
      <el-form :inline="true" :model="searchForm">
        <el-form-item label="预订单号">
          <el-input v-model="searchForm.bookingNo" placeholder="请输入预订单号" />
        </el-form-item>
        <el-form-item label="客户姓名">
          <el-input v-model="searchForm.customerName" placeholder="请输入客户姓名" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="searchBookings" :icon="Search">查询</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="bookingList" v-loading="loadingBookings" style="margin-top: 16px">
        <el-table-column prop="bookingNo" label="预订单号" width="200" />
        <el-table-column prop="customerName" label="客户姓名" width="100" />
        <el-table-column prop="customerPhone" label="手机号" width="130" />
        <el-table-column prop="roomTypeName" label="房型" width="100" />
        <el-table-column prop="checkInDate" label="入住日期" width="110" />
        <el-table-column prop="checkOutDate" label="退房日期" width="110" />
        <el-table-column prop="totalAmount" label="订单金额" width="100">
          <template #default="{ row }">¥{{ row.totalAmount }}</template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getBookingStatusType(row.status)" size="small">
              {{ getBookingStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="100">
          <template #default="{ row }">
            <el-button
              type="primary"
              link
              :disabled="row.status !== 1 && row.status !== 2 && row.status !== 3"
              @click="selectBooking(row)"
            >
              办理入住
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-card v-if="selectedBooking" class="checkin-form-card">
      <el-steps :active="activeStep" finish-status="success" align-center>
        <el-step title="核对信息" />
        <el-step title="登记入住人" />
        <el-step title="分配房间" />
        <el-step title="收取押金" />
        <el-step title="完成入住" />
      </el-steps>

      <div class="step-content">
        <div v-if="activeStep === 0" class="step-0">
          <h3>预订信息确认</h3>
          <el-descriptions :column="2" border>
            <el-descriptions-item label="预订单号">
              {{ selectedBooking.bookingNo }}
            </el-descriptions-item>
            <el-descriptions-item label="预订状态">
              <el-tag :type="getBookingStatusType(selectedBooking.status)" size="small">
                {{ getBookingStatusText(selectedBooking.status) }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="客户姓名">
              {{ selectedBooking.customerName }}
            </el-descriptions-item>
            <el-descriptions-item label="联系电话">
              {{ selectedBooking.customerPhone }}
            </el-descriptions-item>
            <el-descriptions-item label="房型">
              {{ selectedBooking.roomTypeName }}
            </el-descriptions-item>
            <el-descriptions-item label="房号">
              {{ selectedBooking.roomNumber || '未分配' }}
            </el-descriptions-item>
            <el-descriptions-item label="入住日期">
              {{ selectedBooking.checkInDate }}
            </el-descriptions-item>
            <el-descriptions-item label="退房日期">
              {{ selectedBooking.checkOutDate }}
            </el-descriptions-item>
            <el-descriptions-item label="房费单价">
              ¥{{ selectedBooking.roomPrice }}
            </el-descriptions-item>
            <el-descriptions-item label="入住天数">
              {{ selectedBooking.days }} 天
            </el-descriptions-item>
            <el-descriptions-item label="订单总额" :span="2">
              <span style="color: #f56c6c; font-size: 18px; font-weight: bold">
                ¥{{ selectedBooking.totalAmount }}
              </span>
            </el-descriptions-item>
          </el-descriptions>

          <div class="special-requirements">
            <h4>特殊要求</h4>
            <p>{{ selectedBooking.specialRequirements || '无' }}</p>
          </div>

          <div v-if="bookingMemberInfo" class="member-info-section">
            <h4>关联会员</h4>
            <p>卡号 {{ bookingMemberInfo.memberNo }}，等级 {{ bookingMemberInfo.levelName }}，可用积分 {{ bookingMemberInfo.currentPoints }}</p>
          </div>

          <div class="step-actions">
            <el-button type="primary" @click="nextStep">下一步</el-button>
          </div>
        </div>

        <div v-if="activeStep === 1" class="step-1">
          <h3>入住人信息登记</h3>
          <el-alert
            title="主入住人信息必填（公安要求）"
            type="warning"
            :closable="false"
            style="margin-bottom: 16px"
          />

          <div class="guest-list">
            <div
              v-for="(guest, index) in guests"
              :key="index"
              class="guest-item"
            >
              <div class="guest-header">
                <span class="guest-title">
                  {{ guest.isMain === 1 ? '主入住人' : '同住人 ' + index }}
                </span>
                <el-button
                  v-if="guest.isMain !== 1"
                  type="danger"
                  link
                  size="small"
                  @click="removeGuest(index)"
                >
                  删除
                </el-button>
              </div>
              <el-form :model="guest" label-width="100px">
                <el-row :gutter="16">
                  <el-col :span="8">
                    <el-form-item label="姓名" required>
                      <el-input v-model="guest.name" placeholder="请输入姓名" />
                    </el-form-item>
                  </el-col>
                  <el-col :span="8">
                    <el-form-item label="证件类型">
                      <el-select v-model="guest.idType" placeholder="请选择">
                        <el-option label="身份证" :value="1" />
                        <el-option label="护照" :value="2" />
                        <el-option label="港澳通行证" :value="3" />
                        <el-option label="台胞证" :value="4" />
                        <el-option label="军官证" :value="5" />
                        <el-option label="其他" :value="6" />
                      </el-select>
                    </el-form-item>
                  </el-col>
                  <el-col :span="8">
                    <el-form-item label="证件号码" required>
                      <el-input v-model="guest.idNumber" placeholder="请输入证件号码" />
                    </el-form-item>
                  </el-col>
                  <el-col :span="8">
                    <el-form-item label="手机号">
                      <el-input v-model="guest.phone" placeholder="请输入手机号" />
                    </el-form-item>
                  </el-col>
                </el-row>
              </el-form>
            </div>
          </div>

          <el-button type="primary" plain @click="addGuest">
            <el-icon><Plus /></el-icon>
            添加同住人
          </el-button>

          <div class="step-actions">
            <el-button @click="prevStep">上一步</el-button>
            <el-button type="primary" @click="nextStep">下一步</el-button>
          </div>
        </div>

        <div v-if="activeStep === 2" class="step-2">
          <h3>分配房间</h3>

          <el-alert
            v-if="upgradeEligibility?.eligible && upgradeEligibility?.roomTypes?.length > 0"
            title="会员升级权益"
            type="success"
            :closable="false"
            show-icon
            style="margin-bottom: 16px"
          >
            <template #default>
              <div class="upgrade-alert-content">
                <span>该会员可免费升级房型，当前可升级房型：</span>
                <el-select
                  v-model="selectedUpgradeRoomTypeId"
                  placeholder="选择升级房型"
                  style="width: 200px; margin: 0 12px"
                  @change="handleUpgradeRoomTypeChange"
                >
                  <el-option
                    v-for="rt in upgradeEligibility.roomTypes"
                    :key="rt.id"
                    :label="`${rt.typeName}（差价：+¥${rt.priceDiff.toFixed(2)}）`"
                    :value="rt.id"
                  />
                </el-select>
                <el-button
                  type="success"
                  size="small"
                  :disabled="!selectedUpgradeRoomTypeId"
                  @click="showUpgradeConfirm"
                >
                  确认升级
                </el-button>
                <el-button
                  type="info"
                  size="small"
                  text
                  @click="skipUpgrade"
                >
                  暂不升级
                </el-button>
              </div>
            </template>
          </el-alert>

          <div v-if="isUpgraded" class="upgrade-info-bar">
            <el-icon color="#67c23a"><CircleCheckFilled /></el-icon>
            <span>已升级至：<strong>{{ upgradedRoomTypeName }}</strong>（原房型：{{ selectedBooking.roomTypeName }}，房费保持不变）</span>
            <el-button type="danger" text size="small" @click="cancelUpgrade">取消升级</el-button>
          </div>

          <div class="room-select-section">
            <div class="room-filter">
              <span>房型：{{ isUpgraded ? upgradedRoomTypeName : selectedBooking.roomTypeName }}</span>
              <span v-if="isUpgraded" style="color: #67c23a; font-size: 12px">（升级房型）</span>
              <el-button type="primary" @click="loadAvailableRooms">
                <el-icon><Refresh /></el-icon>
                刷新可用房间
              </el-button>
            </div>

            <div class="room-list" v-loading="loadingRooms">
              <div
                v-for="room in availableRooms"
                :key="room.id"
                class="room-card"
                :class="{ active: selectedRoomId === room.id, preferred: room.isPreferred }"
                @click="selectRoom(room.id)"
              >
                <div class="room-header">
                  <div class="room-number">{{ room.roomNumber }}</div>
                  <el-tag
                    v-if="room.isPreferred"
                    size="small"
                    type="warning"
                    effect="light"
                    class="preferred-tag"
                  >
                    <el-icon><Star /></el-icon>
                    会员优选
                  </el-tag>
                </div>
                <div class="room-info">
                  <div class="room-item">
                    <span class="label">朝向：</span>
                    <span>{{ room.orientation || '未知' }}</span>
                  </div>
                  <div class="room-item">
                    <span class="label">景观：</span>
                    <span>{{ room.viewType || '无' }}</span>
                  </div>
                  <div class="room-item">
                    <span class="label">楼层：</span>
                    <span>{{ room.floorName || '' }}楼</span>
                  </div>
                </div>
                <div v-if="selectedRoomId === room.id" class="room-selected">
                  <el-icon color="#67c23a"><CircleCheckFilled /></el-icon>
                </div>
              </div>
            </div>

            <el-empty v-if="!loadingRooms && availableRooms.length === 0" description="暂无可用房间" />
          </div>

          <div class="step-actions">
            <el-button @click="prevStep">上一步</el-button>
            <el-button type="primary" :disabled="!selectedRoomId" @click="nextStep">
              下一步
            </el-button>
          </div>
        </div>

        <div v-if="activeStep === 3" class="step-3">
          <h3>收取押金</h3>

          <el-card v-if="depositReductionInfo?.hasReduction" class="deposit-reduction-card" shadow="hover">
            <template #header>
              <div class="deposit-reduction-header">
                <el-icon color="#67c23a"><Wallet /></el-icon>
                <span>会员押金减免</span>
              </div>
            </template>
            <div class="deposit-reduction-content">
              <div class="deposit-item">
                <span class="deposit-label">标准押金：</span>
                <span class="deposit-original">¥{{ depositReductionInfo.standardDeposit.toFixed(2) }}</span>
              </div>
              <div class="deposit-item">
                <span class="deposit-label">减免金额：</span>
                <span class="deposit-reduction">-¥{{ depositReductionInfo.reductionAmount.toFixed(2) }}</span>
                <el-tag type="success" size="small" style="margin-left: 8px">
                  {{ depositReductionInfo.reductionRemark }}
                </el-tag>
              </div>
              <div class="deposit-item total">
                <span class="deposit-label">实际押金：</span>
                <span class="deposit-actual">¥{{ depositReductionInfo.actualDeposit.toFixed(2) }}</span>
              </div>
            </div>
          </el-card>

          <el-form :model="depositForm" label-width="120px" style="max-width: 600px; margin-top: 16px">
            <el-form-item label="押金金额" required>
              <el-input-number
                v-model="depositForm.depositAmount"
                :min="0"
                :precision="2"
                :step="50"
                style="width: 200px"
                :disabled="depositReductionInfo?.hasReduction"
                @change="calculateDepositReduction"
              />
              <span style="margin-left: 8px; color: #909399">元</span>
            </el-form-item>
            <el-form-item label="押金方式" required>
              <el-radio-group v-model="depositForm.depositMethod">
                <el-radio :value="1">现金押金</el-radio>
                <el-radio :value="2">信用卡预授权</el-radio>
                <el-radio :value="3">免押金</el-radio>
              </el-radio-group>
            </el-form-item>
            <el-form-item label="凭证号">
              <el-input
                v-model="depositForm.depositVoucherNo"
                placeholder="请输入收款凭证号/预授权号"
                style="width: 300px"
              />
            </el-form-item>
            <el-form-item label="房卡数量">
              <el-input-number
                v-model="depositForm.keyCardCount"
                :min="1"
                :max="5"
                style="width: 150px"
              />
              <span style="margin-left: 8px; color: #909399">张</span>
            </el-form-item>
            <el-form-item label="备注">
              <el-input
                v-model="depositForm.remark"
                type="textarea"
                :rows="2"
                placeholder="请输入备注信息"
              />
            </el-form-item>
          </el-form>

          <div class="step-actions">
            <el-button @click="prevStep">上一步</el-button>
            <el-button type="primary" @click="nextStep">下一步</el-button>
          </div>
        </div>

        <div v-if="activeStep === 4" class="step-4">
          <h3>确认入住信息</h3>
          <el-row :gutter="16">
            <el-col :span="12">
              <el-card>
                <template #header><span class="card-title">房间信息</span></template>
                <div class="info-item">
                  <span class="label">房型：</span>
                  <span class="value">{{ selectedBooking.roomTypeName }}</span>
                </div>
                <div class="info-item">
                  <span class="label">房号：</span>
                  <span class="value highlight">{{ selectedRoom?.roomNumber }}</span>
                </div>
                <div class="info-item">
                  <span class="label">入住日期：</span>
                  <span class="value">{{ selectedBooking.checkInDate }}</span>
                </div>
                <div class="info-item">
                  <span class="label">退房日期：</span>
                  <span class="value">{{ selectedBooking.checkOutDate }}</span>
                </div>
                <div class="info-item">
                  <span class="label">房费单价：</span>
                  <span class="value">¥{{ selectedBooking.roomPrice }}</span>
                </div>
              </el-card>
            </el-col>
            <el-col :span="12">
              <el-card>
                <template #header><span class="card-title">客户信息</span></template>
                <div class="info-item">
                  <span class="label">姓名：</span>
                  <span class="value">{{ selectedBooking.customerName }}</span>
                </div>
                <div class="info-item">
                  <span class="label">电话：</span>
                  <span class="value">{{ selectedBooking.customerPhone }}</span>
                </div>
                <div class="info-item">
                  <span class="label">入住人数：</span>
                  <span class="value">{{ guests.length }}人</span>
                </div>
                <div v-if="bookingMemberInfo" class="info-item">
                  <span class="label">关联会员：</span>
                  <span class="value">卡号 {{ bookingMemberInfo.memberNo }}，等级 {{ bookingMemberInfo.levelName }}，可用积分 {{ bookingMemberInfo.currentPoints }}</span>
                </div>
              </el-card>
            </el-col>
          </el-row>

          <el-row :gutter="16" style="margin-top: 16px">
            <el-col :span="12">
              <el-card>
                <template #header><span class="card-title">入住人信息</span></template>
                <div v-for="(guest, index) in guests" :key="index" class="guest-info">
                  <span class="guest-name">
                    {{ guest.isMain === 1 ? '主：' : '' }}{{ guest.name }}
                  </span>
                  <span class="guest-id">
                    {{ getIdTypeText(guest.idType) }}: {{ guest.idNumber }}
                  </span>
                </div>
              </el-card>
            </el-col>
            <el-col :span="12">
              <el-card>
                <template #header><span class="card-title">押金信息</span></template>
                <div class="info-item" v-if="depositReductionInfo?.hasReduction">
                  <span class="label">标准押金：</span>
                  <span class="value" style="text-decoration: line-through; color: #909399">
                    ¥{{ depositReductionInfo.standardDeposit.toFixed(2) }}
                  </span>
                </div>
                <div class="info-item" v-if="depositReductionInfo?.hasReduction">
                  <span class="label">减免金额：</span>
                  <span class="value" style="color: #67c23a">
                    -¥{{ depositReductionInfo.reductionAmount.toFixed(2) }}
                    <el-tag type="success" size="small" style="margin-left: 8px">
                      {{ depositReductionInfo.reductionRemark }}
                    </el-tag>
                  </span>
                </div>
                <div class="info-item">
                  <span class="label">押金金额：</span>
                  <span class="value highlight">¥{{ depositForm.depositAmount }}</span>
                </div>
                <div class="info-item">
                  <span class="label">押金方式：</span>
                  <span class="value">{{ getDepositMethodText(depositForm.depositMethod) }}</span>
                </div>
                <div class="info-item">
                  <span class="label">房卡数量：</span>
                  <span class="value">{{ depositForm.keyCardCount }}张</span>
                </div>
                <div class="info-item" v-if="isUpgraded">
                  <span class="label">房型升级：</span>
                  <span class="value" style="color: #67c23a">
                    已升级至 {{ upgradedRoomTypeName }}
                  </span>
                </div>
              </el-card>
            </el-col>
          </el-row>

          <div class="total-section">
            <div class="total-item">
              <span class="label">应付总额：</span>
              <span class="value">¥{{ selectedBooking.totalAmount }}</span>
            </div>
            <div class="total-item">
              <span class="label">已付金额：</span>
              <span class="value">¥{{ selectedBooking.paidAmount || 0 }}</span>
            </div>
            <div class="total-item highlight">
              <span class="label">待结算金额：</span>
              <span class="value">¥{{ (selectedBooking.totalAmount - (selectedBooking.paidAmount || 0)).toFixed(2) }}</span>
            </div>
          </div>

          <div class="step-actions">
            <el-button @click="prevStep">上一步</el-button>
            <el-button type="primary" size="large" @click="submitCheckIn" :loading="submitting">
              确认办理入住
            </el-button>
          </div>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  ArrowLeft, Search, Plus, Refresh, CircleCheckFilled,
  Star, Wallet
} from '@element-plus/icons-vue'
import api from '@/api'

const route = useRoute()
const router = useRouter()

const bookingId = ref(route.query.bookingId ? Number(route.query.bookingId) : null)
const activeStep = ref(0)
const submitting = ref(false)
const loadingBookings = ref(false)
const loadingRooms = ref(false)

const searchForm = reactive({
  bookingNo: '',
  customerName: ''
})

const bookingList = ref([])
const selectedBooking = ref(null)
const selectedRoomId = ref(null)
const selectedRoom = ref(null)
const availableRooms = ref([])

const guests = ref([
  { isMain: 1, name: '', idType: 1, idNumber: '', phone: '' }
])

const depositForm = reactive({
  depositAmount: 200,
  depositMethod: 1,
  depositVoucherNo: '',
  keyCardCount: 1,
  remark: ''
})

const goBack = () => {
  if (bookingId.value) {
    router.push('/checkin/list')
  } else {
    router.back()
  }
}

const searchBookings = async () => {
  loadingBookings.value = true
  try {
    const res = await api.booking.page({
      ...searchForm,
      pageNum: 1,
      pageSize: 20
    })
    if (res.code === 200) {
      bookingList.value = res.data.records
    }
  } catch (e) {
    console.error('查询预订单失败', e)
  } finally {
    loadingBookings.value = false
  }
}

const bookingMemberInfo = ref(null)
const depositReductionInfo = ref(null)
const upgradeEligibility = ref(null)
const selectedUpgradeRoomTypeId = ref(null)
const isUpgraded = ref(false)
const upgradedRoomTypeName = ref('')
const originalRoomTypeId = ref(null)
const upgradeSkipped = ref(false)

const calculateDepositReduction = async () => {
  if (!bookingMemberInfo.value?.id || depositForm.depositAmount <= 0) {
    depositReductionInfo.value = null
    return
  }
  try {
    const res = await api.memberBenefit.calculateDeposit({
      memberId: bookingMemberInfo.value.id,
      standardDeposit: depositForm.depositAmount
    })
    if (res.code === 200 && res.data) {
      depositReductionInfo.value = res.data
      if (res.data.hasReduction) {
        depositForm.depositAmount = res.data.actualDeposit
      }
    }
  } catch (e) {
    console.error('计算押金减免失败', e)
  }
}

const loadUpgradeEligibility = async () => {
  if (!bookingMemberInfo.value?.id || upgradeSkipped.value || isUpgraded.value) {
    upgradeEligibility.value = null
    return
  }
  try {
    const res = await api.memberBenefit.getUpgradeRoomTypes({
      memberId: bookingMemberInfo.value.id,
      currentRoomTypeId: selectedBooking.value.roomTypeId,
      checkInDate: selectedBooking.value.checkInDate,
      checkOutDate: selectedBooking.value.checkOutDate
    })
    if (res.code === 200 && res.data) {
      upgradeEligibility.value = res.data
    }
  } catch (e) {
    console.error('加载升级房型失败', e)
  }
}

const handleUpgradeRoomTypeChange = () => {
}

const showUpgradeConfirm = async () => {
  if (!selectedUpgradeRoomTypeId.value || !bookingId.value) return
  try {
    const res = await api.memberBenefit.applyRoomUpgrade(
      bookingId.value,
      { newRoomTypeId: selectedUpgradeRoomTypeId.value }
    )
    if (res.code === 200 && res.data) {
      isUpgraded.value = true
      upgradedRoomTypeName.value = res.data.newRoomTypeName
      originalRoomTypeId.value = selectedBooking.value.roomTypeId
      const upgradedType = upgradeEligibility.value.roomTypes.find(
        rt => rt.id === selectedUpgradeRoomTypeId.value
      )
      if (upgradedType) {
        upgradedRoomTypeName.value = upgradedType.typeName
      }
      ElMessage.success(`已成功升级至 ${upgradedRoomTypeName.value}`)
      upgradeEligibility.value = null
      selectedUpgradeRoomTypeId.value = null
      loadAvailableRooms()
    }
  } catch (e) {
    console.error('升级房型失败', e)
  }
}

const skipUpgrade = () => {
  upgradeSkipped.value = true
  upgradeEligibility.value = null
}

const cancelUpgrade = () => {
  isUpgraded.value = false
  upgradedRoomTypeName.value = ''
  originalRoomTypeId.value = null
  upgradeSkipped.value = false
  loadUpgradeEligibility()
  loadAvailableRooms()
}

const selectBooking = async (booking) => {
  selectedBooking.value = booking
  bookingId.value = booking.id
  guests.value = [{
    isMain: 1,
    name: booking.customerName,
    idType: 1,
    idNumber: '',
    phone: booking.customerPhone
  }]
  if (booking.roomId) {
    selectedRoomId.value = booking.roomId
  }
  depositReductionInfo.value = null
  upgradeEligibility.value = null
  isUpgraded.value = false
  upgradedRoomTypeName.value = ''
  upgradeSkipped.value = false

  if (booking.memberId) {
    try {
      const res = await api.member.getById(booking.memberId)
      if (res.code === 200) {
        bookingMemberInfo.value = res.data || null
      }
    } catch (e) {
      console.error('加载会员信息失败', e)
    }
  } else {
    bookingMemberInfo.value = null
  }
}

const getBookingStatusText = (status) => {
  const map = {
    1: '待确认', 2: '已确认', 3: '已支付',
    4: '已入住', 5: '已完成', 6: '已取消'
  }
  return map[status] || '未知'
}

const getBookingStatusType = (status) => {
  const map = {
    1: 'warning', 2: 'primary', 3: 'success',
    4: 'success', 5: 'info', 6: 'danger'
  }
  return map[status] || ''
}

const getIdTypeText = (type) => {
  const map = {
    1: '身份证', 2: '护照', 3: '港澳通行证',
    4: '台胞证', 5: '军官证', 6: '其他'
  }
  return map[type] || '未知'
}

const getDepositMethodText = (method) => {
  const map = { 1: '现金押金', 2: '信用卡预授权', 3: '免押金' }
  return map[method] || '未知'
}

const addGuest = () => {
  guests.value.push({
    isMain: 0,
    name: '',
    idType: 1,
    idNumber: '',
    phone: ''
  })
}

const removeGuest = (index) => {
  guests.value.splice(index, 1)
}

const selectRoom = (roomId) => {
  selectedRoomId.value = roomId
  selectedRoom.value = availableRooms.value.find(r => r.id === roomId)
}

const loadAvailableRooms = async () => {
  if (!selectedBooking.value) return
  loadingRooms.value = true
  try {
    const queryRoomTypeId = isUpgraded.value && originalRoomTypeId.value
      ? selectedUpgradeRoomTypeId.value
      : selectedBooking.value.roomTypeId

    const res = await api.booking.roomQuery({
      roomTypeId: queryRoomTypeId,
      checkInDate: selectedBooking.value.checkInDate,
      checkOutDate: selectedBooking.value.checkOutDate
    })
    if (res.code === 200) {
      let rooms = res.data || []

      if (bookingMemberInfo.value?.id && rooms.length > 0) {
        try {
          const prefRes = await api.memberBenefit.getPreferredRooms(rooms)
          if (prefRes.code === 200 && prefRes.data) {
            rooms = prefRes.data
          }
        } catch (e) {
          console.error('获取会员优选房间失败', e)
        }
      }

      availableRooms.value = rooms

      if (selectedBooking.value.roomId && !isUpgraded.value) {
        const hasRoom = availableRooms.value.find(r => r.id === selectedBooking.value.roomId)
        if (!hasRoom) {
          availableRooms.value.unshift({
            id: selectedBooking.value.roomId,
            roomNumber: selectedBooking.value.roomNumber,
            roomTypeId: selectedBooking.value.roomTypeId
          })
        }
        if (!selectedRoomId.value) {
          selectRoom(selectedBooking.value.roomId)
        }
      } else if (rooms.length > 0 && !selectedRoomId.value) {
        selectRoom(rooms[0].id)
      }
    }
  } catch (e) {
    console.error('加载可用房间失败', e)
  } finally {
    loadingRooms.value = false
  }
}

const validateStep = () => {
  if (activeStep.value === 1) {
    const mainGuest = guests.value.find(g => g.isMain === 1)
    if (!mainGuest || !mainGuest.name) {
      ElMessage.warning('请填写主入住人姓名')
      return false
    }
    if (!mainGuest.idType) {
      ElMessage.warning('请选择主入住人证件类型')
      return false
    }
    if (!mainGuest.idNumber) {
      ElMessage.warning('请填写主入住人证件号码')
      return false
    }
    for (let i = 0; i < guests.value.length; i++) {
      const guest = guests.value[i]
      if (guest.name && guest.idType && !guest.idNumber) {
        ElMessage.warning(`请填写第${i + 1}位入住人的证件号码`)
        return false
      }
    }
  }
  if (activeStep.value === 2) {
    if (!selectedRoomId.value) {
      ElMessage.warning('请选择房间')
      return false
    }
  }
  return true
}

const nextStep = async () => {
  if (!validateStep()) return
  if (activeStep.value < 4) {
    activeStep.value++
    if (activeStep.value === 2 && availableRooms.value.length === 0) {
      await loadAvailableRooms()
      if (bookingMemberInfo.value?.id) {
        await loadUpgradeEligibility()
      }
    }
    if (activeStep.value === 3 && bookingMemberInfo.value?.id) {
      await calculateDepositReduction()
    }
  }
}

const prevStep = () => {
  if (activeStep.value > 0) {
    activeStep.value--
  }
}

const submitCheckIn = async () => {
  if (!selectedRoomId.value) {
    ElMessage.warning('请选择房间')
    return
  }
  const mainGuest = guests.value.find(g => g.isMain === 1)
  if (!mainGuest || !mainGuest.name || !mainGuest.idNumber) {
    ElMessage.warning('请完善主入住人信息')
    return
  }

  submitting.value = true
  try {
    const res = await api.checkin.checkInFromBooking({
      bookingId: bookingId.value,
      roomId: selectedRoomId.value,
      guests: guests.value,
      depositAmount: depositForm.depositAmount,
      depositMethod: depositForm.depositMethod,
      depositVoucherNo: depositForm.depositVoucherNo,
      keyCardCount: depositForm.keyCardCount,
      specialRequirements: selectedBooking.value.specialRequirements,
      remark: depositForm.remark
    })
    if (res.code === 200) {
      ElMessage.success('办理入住成功')
      router.push('/checkin/list')
    }
  } catch (e) {
    console.error('办理入住失败', e)
  } finally {
    submitting.value = false
  }
}

onMounted(() => {
  if (bookingId.value) {
    loadBookingDetail()
  }
})

const loadBookingDetail = async () => {
  try {
    const res = await api.booking.get(bookingId.value)
    if (res.code === 200) {
      selectBooking(res.data)
    }
  } catch (e) {
    console.error('加载预订详情失败', e)
  }
}
</script>

<style scoped lang="scss">
.checkin-create {
  .page-header {
    display: flex;
    align-items: center;
    gap: 16px;
    margin-bottom: 16px;
    padding: 16px 20px;
    background: #fff;
    border-radius: 8px;

    h2 {
      margin: 0;
      font-size: 20px;
      font-weight: bold;
    }
  }

  .checkin-form-card {
    :deep(.el-steps) {
      padding: 30px 0 40px;
    }
  }

  .step-content {
    padding: 20px 40px;

    h3 {
      margin: 0 0 20px;
      font-size: 18px;
      color: #303133;
    }

    .step-actions {
      display: flex;
      justify-content: center;
      gap: 16px;
      margin-top: 30px;
      padding-top: 20px;
      border-top: 1px solid #ebeef5;
    }
  }

  .special-requirements {
    margin-top: 20px;
    padding: 16px;
    background: #f5f7fa;
    border-radius: 4px;

    h4 {
      margin: 0 0 8px;
      font-size: 14px;
      color: #303133;
    }

    p {
      margin: 0;
      color: #606266;
    }
  }

  .member-info-section {
    margin-top: 20px;
    padding: 16px;
    background: #fdf6ec;
    border: 1px solid #faecd8;
    border-radius: 4px;

    h4 {
      margin: 0 0 8px;
      font-size: 14px;
      color: #e6a23c;
    }

    p {
      margin: 0;
      color: #606266;
      font-size: 14px;
    }
  }

  .guest-list {
    .guest-item {
      margin-bottom: 16px;
      padding: 16px;
      background: #f5f7fa;
      border-radius: 4px;

      .guest-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
        margin-bottom: 12px;

        .guest-title {
          font-weight: bold;
          color: #409eff;
        }
      }
    }
  }

  .room-select-section {
    .room-filter {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 16px;
    }

    .room-list {
      display: grid;
      grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
      gap: 16px;

      .room-card {
        position: relative;
        padding: 16px;
        border: 2px solid #ebeef5;
        border-radius: 8px;
        cursor: pointer;
        transition: all 0.3s;

        &:hover {
          border-color: #409eff;
        }

        &.active {
          border-color: #67c23a;
          background: #f0f9eb;
        }

        .room-number {
          font-size: 20px;
          font-weight: bold;
          color: #303133;
          margin-bottom: 8px;
        }

        .room-info {
          .room-item {
            font-size: 13px;
            color: #606266;
            margin-bottom: 4px;

            .label {
              color: #909399;
            }
          }
        }

        .room-selected {
          position: absolute;
          top: 8px;
          right: 8px;
        }
      }
    }
  }

  .step-4 {
    .card-title {
      font-weight: bold;
    }

    .info-item {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 8px 0;
      border-bottom: 1px dashed #ebeef5;

      &:last-child {
        border-bottom: none;
      }

      .label {
        color: #909399;
      }

      .value {
        color: #303133;
        font-weight: 500;

        &.highlight {
          color: #409eff;
          font-weight: bold;
        }
      }
    }

    .guest-info {
      display: flex;
      justify-content: space-between;
      padding: 8px 0;
      border-bottom: 1px dashed #ebeef5;

      &:last-child {
        border-bottom: none;
      }

      .guest-name {
        font-weight: 500;
      }

      .guest-id {
        color: #909399;
        font-size: 13px;
      }
    }

    .total-section {
      margin-top: 24px;
      padding: 20px;
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      border-radius: 8px;
      color: #fff;

      .total-item {
        display: flex;
        justify-content: space-between;
        align-items: center;
        margin-bottom: 12px;

        &:last-child {
          margin-bottom: 0;
          padding-top: 12px;
          border-top: 1px solid rgba(255, 255, 255, 0.3);
        }

        &.highlight .value {
          font-size: 24px;
          font-weight: bold;
        }

        .label {
          font-size: 14px;
          opacity: 0.9;
        }

        .value {
          font-size: 16px;
          font-weight: 500;
        }
      }
    }
  }

  .upgrade-alert-content {
    display: flex;
    align-items: center;
    flex-wrap: wrap;
    gap: 8px;
  }

  .upgrade-info-bar {
    display: flex;
    align-items: center;
    gap: 8px;
    padding: 12px 16px;
    background: #f0f9eb;
    border: 1px solid #c2e7b0;
    border-radius: 4px;
    margin-bottom: 16px;

    span {
      flex: 1;
      color: #67c23a;
    }
  }

  .room-card {
    &.preferred {
      border-color: #e6a23c;
      background: linear-gradient(135deg, #fdf6ec 0%, #ffffff 100%);
    }

    .room-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 8px;
    }

    .preferred-tag {
      display: flex;
      align-items: center;
      gap: 2px;
    }
  }

  .deposit-reduction-card {
    border-left: 4px solid #67c23a;

    .deposit-reduction-header {
      display: flex;
      align-items: center;
      gap: 8px;
      font-weight: 600;
      color: #67c23a;
    }

    .deposit-reduction-content {
      .deposit-item {
        display: flex;
        justify-content: space-between;
        align-items: center;
        padding: 8px 0;

        &.total {
          padding-top: 12px;
          margin-top: 8px;
          border-top: 1px dashed #ebeef5;
        }

        .deposit-label {
          color: #606266;
        }

        .deposit-original {
          text-decoration: line-through;
          color: #909399;
        }

        .deposit-reduction {
          color: #67c23a;
          font-weight: 600;
        }

        .deposit-actual {
          color: #f56c6c;
          font-size: 18px;
          font-weight: 700;
        }
      }
    }
  }
}
</style>
