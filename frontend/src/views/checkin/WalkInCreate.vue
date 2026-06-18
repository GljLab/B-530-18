<template>
  <div class="walkin-create">
    <el-card shadow="never" class="form-card">
      <template #header>
        <div class="card-header">
          <span class="card-title">散客入住</span>
        </div>
      </template>

      <el-steps :active="activeStep" finish-status="success" class="walkin-steps">
        <el-step title="查询房源" icon="Search" />
        <el-step title="客户信息" icon="User" />
        <el-step title="登记入住人" icon="Files" />
        <el-step title="收取押金" icon="Wallet" />
        <el-step title="完成入住" icon="CircleCheck" />
      </el-steps>

      <div class="step-content">
        <div v-show="activeStep === 0" class="step-panel">
          <el-form
            ref="step1FormRef"
            :model="step1Form"
            :rules="step1Rules"
            label-width="110px"
            label-position="right"
          >
            <el-divider content-position="left">查询条件</el-divider>
            <el-row :gutter="24">
              <el-col :span="8">
                <el-form-item label="入住日期" prop="checkinDate">
                  <el-date-picker
                    v-model="step1Form.checkinDate"
                    type="date"
                    placeholder="请选择入住日期"
                    style="width: 100%"
                    value-format="YYYY-MM-DD"
                    :disabled-date="disableCheckinDate"
                  />
                </el-form-item>
              </el-col>
              <el-col :span="8">
                <el-form-item label="退房日期" prop="checkoutDate">
                  <el-date-picker
                    v-model="step1Form.checkoutDate"
                    type="date"
                    placeholder="请选择退房日期"
                    style="width: 100%"
                    value-format="YYYY-MM-DD"
                    :disabled-date="disableCheckoutDate"
                  />
                </el-form-item>
              </el-col>
              <el-col :span="8">
                <el-form-item label="房型" prop="roomTypeId">
                  <el-select v-model="step1Form.roomTypeId" placeholder="请选择房型" style="width: 100%">
                    <el-option
                      v-for="item in roomTypeList"
                      :key="item.id"
                      :label="item.name"
                      :value="item.id"
                    />
                  </el-select>
                </el-form-item>
              </el-col>
            </el-row>
            <el-form-item>
              <el-button type="primary" :loading="queryLoading" @click="handleQueryRooms">
                <el-icon><Search /></el-icon>查询可用房间
              </el-button>
            </el-form-item>

            <el-divider content-position="left">可用房间</el-divider>
            <div v-loading="queryLoading" class="room-list">
              <el-empty v-if="availableRooms.length === 0 && !queryLoading" description="暂无可用房间" />
              <el-row :gutter="16">
                <el-col
                  v-for="room in availableRooms"
                  :key="room.id"
                  :span="6"
                  class="room-col"
                >
                  <div
                    class="room-card"
                    :class="{ active: selectedRoom?.id === room.id }"
                    @click="handleSelectRoom(room)"
                  >
                    <div class="room-number">{{ room.roomNumber }}</div>
                    <div class="room-type">{{ room.roomTypeName || '标准间' }}</div>
                    <div class="room-floor">{{ room.floorName || '' }}</div>
                    <div class="room-price">¥{{ room.price || 0 }}/晚</div>
                  </div>
                </el-col>
              </el-row>
            </div>

            <el-alert
              v-if="selectedRoom"
              :title="`已选房间：${selectedRoom.roomNumber} - ${selectedRoom.roomTypeName || '标准间'}`"
              type="success"
              :closable="false"
              class="selected-room-alert"
            >
              <template #default>
                <el-row :gutter="16">
                  <el-col :span="8">
                    <span class="room-label">楼层：</span>
                    <span>{{ selectedRoom.floorName || '-' }}</span>
                  </el-col>
                  <el-col :span="8">
                    <span class="room-label">床型：</span>
                    <span>{{ selectedRoom.bedType || '-' }}</span>
                  </el-col>
                  <el-col :span="8">
                    <span class="room-label">面积：</span>
                    <span>{{ selectedRoom.area || '-' }}㎡</span>
                  </el-col>
                </el-row>
              </template>
            </el-alert>
          </el-form>
        </div>

        <div v-show="activeStep === 1" class="step-panel">
          <el-form
            ref="step2FormRef"
            :model="step2Form"
            :rules="step2Rules"
            label-width="110px"
            label-position="right"
          >
            <el-divider content-position="left">客户信息</el-divider>
            <el-row :gutter="24">
              <el-col :span="12">
                <el-form-item label="客户姓名" prop="customerName">
                  <el-input v-model="step2Form.customerName" placeholder="请输入客户姓名" maxlength="50" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="手机号" prop="customerPhone">
                  <el-input v-model="step2Form.customerPhone" placeholder="请输入手机号" maxlength="11" />
                </el-form-item>
              </el-col>
            </el-row>
            <el-row :gutter="24">
              <el-col :span="12">
                <el-form-item label="证件类型" prop="idType">
                  <el-select v-model="step2Form.idType" placeholder="请选择证件类型" style="width: 100%">
                    <el-option label="身份证" value="1" />
                    <el-option label="护照" value="2" />
                    <el-option label="港澳通行证" value="3" />
                    <el-option label="台胞证" value="4" />
                    <el-option label="其他" value="5" />
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="证件号" prop="idNumber">
                  <el-input v-model="step2Form.idNumber" placeholder="请输入证件号" maxlength="30" />
                </el-form-item>
              </el-col>
            </el-row>
          </el-form>
        </div>

        <div v-show="activeStep === 2" class="step-panel">
          <el-form
            ref="step3FormRef"
            :model="step3Form"
            :rules="step3Rules"
            label-width="110px"
            label-position="right"
          >
            <el-divider content-position="left">主入住人</el-divider>
            <el-row :gutter="24">
              <el-col :span="12">
                <el-form-item label="姓名" prop="mainGuest.name">
                  <el-input v-model="step3Form.mainGuest.name" placeholder="请输入姓名" maxlength="50" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="手机号" prop="mainGuest.phone">
                  <el-input v-model="step3Form.mainGuest.phone" placeholder="请输入手机号" maxlength="11" />
                </el-form-item>
              </el-col>
            </el-row>
            <el-row :gutter="24">
              <el-col :span="12">
                <el-form-item label="证件类型" prop="mainGuest.idType">
                  <el-select v-model="step3Form.mainGuest.idType" placeholder="请选择证件类型" style="width: 100%">
                    <el-option label="身份证" value="1" />
                    <el-option label="护照" value="2" />
                    <el-option label="港澳通行证" value="3" />
                    <el-option label="台胞证" value="4" />
                    <el-option label="其他" value="5" />
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="证件号" prop="mainGuest.idNumber">
                  <el-input v-model="step3Form.mainGuest.idNumber" placeholder="请输入证件号" maxlength="30" />
                </el-form-item>
              </el-col>
            </el-row>

            <el-divider content-position="left">同住人</el-divider>
            <div v-for="(guest, index) in step3Form.roommates" :key="index" class="guest-item">
              <el-row :gutter="24">
                <el-col :span="10">
                  <el-form-item
                    :label="`同住人${index + 1}姓名`"
                    :prop="`roommates.${index}.name`"
                    :rules="guestNameRules"
                  >
                    <el-input v-model="guest.name" placeholder="请输入姓名" maxlength="50" />
                  </el-form-item>
                </el-col>
                <el-col :span="10">
                  <el-form-item
                    label="证件号"
                    :prop="`roommates.${index}.idNumber`"
                    :rules="guestIdRules"
                  >
                    <el-input v-model="guest.idNumber" placeholder="请输入证件号" maxlength="30" />
                  </el-form-item>
                </el-col>
                <el-col :span="4">
                  <el-button
                    type="danger"
                    text
                    @click="removeRoommate(index)"
                  >
                    <el-icon><Delete /></el-icon>删除
                  </el-button>
                </el-col>
              </el-row>
            </div>
            <el-form-item>
              <el-button type="primary" text @click="addRoommate">
                <el-icon><Plus /></el-icon>添加同住人
              </el-button>
            </el-form-item>
          </el-form>
        </div>

        <div v-show="activeStep === 3" class="step-panel">
          <el-form
            ref="step4FormRef"
            :model="step4Form"
            :rules="step4Rules"
            label-width="110px"
            label-position="right"
          >
            <el-divider content-position="left">押金信息</el-divider>
            <el-row :gutter="24">
              <el-col :span="12">
                <el-form-item label="押金金额" prop="depositAmount">
                  <el-input-number
                    v-model="step4Form.depositAmount"
                    :min="0"
                    :precision="2"
                    :step="100"
                    style="width: 100%"
                  />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="押金方式" prop="depositType">
                  <el-select v-model="step4Form.depositType" placeholder="请选择押金方式" style="width: 100%">
                    <el-option label="现金" value="1" />
                    <el-option label="微信支付" value="2" />
                    <el-option label="支付宝" value="3" />
                    <el-option label="银行卡" value="4" />
                    <el-option label="信用卡" value="5" />
                    <el-option label="其他" value="6" />
                  </el-select>
                </el-form-item>
              </el-col>
            </el-row>
            <el-row :gutter="24">
              <el-col :span="12">
                <el-form-item label="凭证号" prop="voucherNo">
                  <el-input v-model="step4Form.voucherNo" placeholder="请输入凭证号" maxlength="50" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="房卡数量" prop="keyCardCount">
                  <el-input-number
                    v-model="step4Form.keyCardCount"
                    :min="1"
                    :max="10"
                    style="width: 100%"
                  />
                </el-form-item>
              </el-col>
            </el-row>
            <el-form-item label="备注">
              <el-input
                v-model="step4Form.remark"
                type="textarea"
                :rows="3"
                placeholder="请输入备注信息"
                maxlength="200"
                show-word-limit
              />
            </el-form-item>
          </el-form>
        </div>

        <div v-show="activeStep === 4" class="step-panel">
          <el-alert
            title="请确认以下入住信息是否正确"
            type="warning"
            :closable="false"
            class="confirm-alert"
          />

          <el-row :gutter="24">
            <el-col :span="12">
              <el-card class="confirm-card" shadow="hover">
                <template #header>
                  <span class="confirm-card-title">
                    <el-icon><House /></el-icon> 房间信息
                  </span>
                </template>
                <div class="confirm-item">
                  <span class="confirm-label">房间号：</span>
                  <span class="confirm-value">{{ selectedRoom?.roomNumber || '未选择' }}</span>
                </div>
                <div class="confirm-item">
                  <span class="confirm-label">房型：</span>
                  <span class="confirm-value">{{ selectedRoom?.roomTypeName || '-' }}</span>
                </div>
                <div class="confirm-item">
                  <span class="confirm-label">入住日期：</span>
                  <span class="confirm-value">{{ step1Form.checkinDate }}</span>
                </div>
                <div class="confirm-item">
                  <span class="confirm-label">退房日期：</span>
                  <span class="confirm-value">{{ step1Form.checkoutDate }}</span>
                </div>
                <div class="confirm-item">
                  <span class="confirm-label">入住天数：</span>
                  <span class="confirm-value">{{ stayDays }}晚</span>
                </div>
                <div class="confirm-item">
                  <span class="confirm-label">房卡数量：</span>
                  <span class="confirm-value">{{ step4Form.keyCardCount }}张</span>
                </div>
              </el-card>
            </el-col>

            <el-col :span="12">
              <el-card class="confirm-card" shadow="hover">
                <template #header>
                  <span class="confirm-card-title">
                    <el-icon><User /></el-icon> 客户信息
                  </span>
                </template>
                <div class="confirm-item">
                  <span class="confirm-label">客户姓名：</span>
                  <span class="confirm-value">{{ step2Form.customerName || '-' }}</span>
                </div>
                <div class="confirm-item">
                  <span class="confirm-label">联系电话：</span>
                  <span class="confirm-value">{{ step2Form.customerPhone || '-' }}</span>
                </div>
                <div class="confirm-item">
                  <span class="confirm-label">证件类型：</span>
                  <span class="confirm-value">{{ idTypeLabel(step2Form.idType) }}</span>
                </div>
                <div class="confirm-item" v-if="step2Form.idNumber">
                  <span class="confirm-label">证件号码：</span>
                  <span class="confirm-value">{{ step2Form.idNumber }}</span>
                </div>
              </el-card>
            </el-col>
          </el-row>

          <el-row :gutter="24" style="margin-top: 16px">
            <el-col :span="12">
              <el-card class="confirm-card" shadow="hover">
                <template #header>
                  <span class="confirm-card-title">
                    <el-icon><Files /></el-icon> 入住人信息
                  </span>
                </template>
                <div class="confirm-item">
                  <span class="confirm-label">主入住人：</span>
                  <span class="confirm-value">{{ step3Form.mainGuest.name || '-' }}</span>
                </div>
                <div v-if="step3Form.mainGuest.idNumber" class="confirm-item">
                  <span class="confirm-label">证件号：</span>
                  <span class="confirm-value">{{ step3Form.mainGuest.idNumber }}</span>
                </div>
                <el-divider v-if="step3Form.roommates.length > 0" />
                <div v-if="step3Form.roommates.length > 0" class="confirm-item">
                  <span class="confirm-label">同住人：</span>
                  <div class="guest-list">
                    <div v-for="(guest, idx) in step3Form.roommates" :key="idx" class="guest-row">
                      {{ guest.name }}{{ guest.idNumber ? ` (${guest.idNumber})` : '' }}
                    </div>
                  </div>
                </div>
              </el-card>
            </el-col>

            <el-col :span="12">
              <el-card class="confirm-card" shadow="hover">
                <template #header>
                  <span class="confirm-card-title">
                    <el-icon><Wallet /></el-icon> 押金信息
                  </span>
                </template>
                <div class="confirm-item">
                  <span class="confirm-label">押金金额：</span>
                  <span class="confirm-value deposit-amount">¥{{ step4Form.depositAmount.toFixed(2) }}</span>
                </div>
                <div class="confirm-item">
                  <span class="confirm-label">押金方式：</span>
                  <span class="confirm-value">{{ depositTypeLabel }}</span>
                </div>
                <div v-if="step4Form.voucherNo" class="confirm-item">
                  <span class="confirm-label">凭证号：</span>
                  <span class="confirm-value">{{ step4Form.voucherNo }}</span>
                </div>
                <div v-if="step4Form.remark" class="confirm-item">
                  <span class="confirm-label">备注：</span>
                  <span class="confirm-value">{{ step4Form.remark }}</span>
                </div>
              </el-card>
            </el-col>
          </el-row>
        </div>
      </div>

      <div class="step-actions">
        <el-button v-if="activeStep > 0" @click="handlePrev">
          <el-icon><ArrowLeft /></el-icon>上一步
        </el-button>
        <el-button
          v-if="activeStep < 4"
          type="primary"
          @click="handleNext"
        >
          下一步<el-icon><ArrowRight /></el-icon>
        </el-button>
        <el-button
          v-if="activeStep === 4"
          type="primary"
          :loading="submitting"
          @click="handleSubmit"
        >
          <el-icon><Check /></el-icon>确认办理入住
        </el-button>
        <el-button @click="handleCancel">取消</el-button>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  Plus, Check, Search, Delete, ArrowLeft, ArrowRight,
  House, User, Wallet, CircleCheck, Files
} from '@element-plus/icons-vue'
import api from '@/api'

const router = useRouter()

const activeStep = ref(0)
const submitting = ref(false)
const queryLoading = ref(false)

const step1FormRef = ref(null)
const step2FormRef = ref(null)
const step3FormRef = ref(null)
const step4FormRef = ref(null)

const selectedRoom = ref(null)
const roomTypeList = ref([])
const availableRooms = ref([])

const step1Form = reactive({
  checkinDate: '',
  checkoutDate: '',
  roomTypeId: ''
})

const step2Form = reactive({
  customerName: '',
  customerPhone: '',
  idType: '1',
  idNumber: ''
})

const step3Form = reactive({
  mainGuest: {
    name: '',
    phone: '',
    idType: '1',
    idNumber: ''
  },
  roommates: []
})

const step4Form = reactive({
  depositAmount: 200,
  depositType: '1',
  voucherNo: '',
  keyCardCount: 1,
  remark: ''
})

const step1Rules = {
  checkinDate: [{ required: true, message: '请选择入住日期', trigger: 'change' }],
  checkoutDate: [{ required: true, message: '请选择退房日期', trigger: 'change' }],
  roomTypeId: [{ required: true, message: '请选择房型', trigger: 'change' }]
}

const step2Rules = {
  customerName: [{ required: true, message: '请输入客户姓名', trigger: 'blur' }],
  customerPhone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ],
  idType: [{ required: false, message: '请选择证件类型', trigger: 'change' }],
  idNumber: [{ required: false, message: '请输入证件号', trigger: 'blur' }]
}

const step3Rules = {
  'mainGuest.name': [{ required: true, message: '请输入主入住人姓名', trigger: 'blur' }],
  'mainGuest.phone': [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ],
  'mainGuest.idType': [{ required: false, message: '请选择证件类型', trigger: 'change' }],
  'mainGuest.idNumber': [{ required: false, message: '请输入证件号', trigger: 'blur' }]
}

const step4Rules = {
  depositAmount: [{ required: true, message: '请输入押金金额', trigger: 'change' }],
  depositType: [{ required: true, message: '请选择押金方式', trigger: 'change' }],
  keyCardCount: [{ required: true, message: '请输入房卡数量', trigger: 'change' }]
}

const guestNameRules = [{ required: true, message: '请输入姓名', trigger: 'blur' }]
const guestIdRules = [{ required: false, message: '请输入证件号', trigger: 'blur' }]

const stayDays = computed(() => {
  if (!step1Form.checkinDate || !step1Form.checkoutDate) return 0
  const start = new Date(step1Form.checkinDate)
  const end = new Date(step1Form.checkoutDate)
  return Math.max(0, Math.ceil((end - start) / (1000 * 60 * 60 * 24)))
})

const depositTypeLabel = computed(() => {
  const map = { '1': '现金', '2': '微信支付', '3': '支付宝', '4': '银行卡', '5': '信用卡', '6': '其他' }
  return map[step4Form.depositType] || '未选择'
})

const idTypeLabel = (type) => {
  const map = { '1': '身份证', '2': '护照', '3': '港澳通行证', '4': '台胞证', '5': '其他' }
  return map[type] || '未设置'
}

const disableCheckinDate = (date) => {
  return date < new Date(new Date().setHours(0, 0, 0, 0) - 1)
}

const disableCheckoutDate = (date) => {
  if (!step1Form.checkinDate) return true
  return date <= new Date(step1Form.checkinDate)
}

const formatDate = (date) => {
  return date.toISOString().slice(0, 10)
}

const initDefaultDates = () => {
  const today = new Date()
  const tomorrow = new Date(today)
  tomorrow.setDate(tomorrow.getDate() + 1)
  step1Form.checkinDate = formatDate(today)
  step1Form.checkoutDate = formatDate(tomorrow)
}

const loadRoomTypes = async () => {
  try {
    const res = await api.hotel.getRoomTypes()
    if (res.code === 200) {
      roomTypeList.value = res.data?.records || res.data?.list || res.data || []
    }
  } catch {
    roomTypeList.value = []
  }
}

const handleQueryRooms = async () => {
  if (!step1Form.checkinDate || !step1Form.checkoutDate || !step1Form.roomTypeId) {
    ElMessage.warning('请完善查询条件')
    return
  }
  queryLoading.value = true
  try {
    const params = {
      checkinDate: step1Form.checkinDate,
      checkoutDate: step1Form.checkoutDate,
      roomTypeId: step1Form.roomTypeId
    }
    const res = await api.checkin.availableRoomsForWalkIn(params)
    if (res.code === 200) {
      availableRooms.value = res.data?.records || res.data?.list || res.data || []
    } else {
      ElMessage.error(res.message || '查询失败')
    }
  } catch {
    ElMessage.error('查询失败，请重试')
  } finally {
    queryLoading.value = false
  }
}

const handleSelectRoom = (room) => {
  selectedRoom.value = room
}

const addRoommate = () => {
  step3Form.roommates.push({ name: '', idNumber: '' })
}

const removeRoommate = (index) => {
  step3Form.roommates.splice(index, 1)
}

const validateStep = async (step) => {
  if (step === 0) {
    if (!selectedRoom.value) {
      ElMessage.warning('请选择房间')
      return false
    }
    return await step1FormRef.value.validate().catch(() => false)
  }
  if (step === 1) {
    return await step2FormRef.value.validate().catch(() => false)
  }
  if (step === 2) {
    const valid = await step3FormRef.value.validate().catch(() => false)
    if (!valid) return false
    for (let i = 0; i < step3Form.roommates.length; i++) {
      const guest = step3Form.roommates[i]
      if (!guest.name) {
        ElMessage.warning(`请完善第${i + 1}位同住人姓名`)
        return false
      }
    }
    return true
  }
  if (step === 3) {
    return await step4FormRef.value.validate().catch(() => false)
  }
  return true
}

const handlePrev = () => {
  if (activeStep.value > 0) {
    activeStep.value--
  }
}

const handleNext = async () => {
  const valid = await validateStep(activeStep.value)
  if (!valid) return
  if (activeStep.value < 4) {
    activeStep.value++
  }
}

const handleSubmit = async () => {
  submitting.value = true
  try {
    const payload = {
      roomId: selectedRoom.value?.id,
      roomNumber: selectedRoom.value?.roomNumber,
      roomTypeId: step1Form.roomTypeId,
      checkinDate: step1Form.checkinDate,
      checkoutDate: step1Form.checkoutDate,
      days: stayDays.value,
      customerName: step2Form.customerName,
      customerPhone: step2Form.customerPhone,
      idType: step2Form.idType,
      idNumber: step2Form.idNumber,
      mainGuest: {
        name: step3Form.mainGuest.name,
        phone: step3Form.mainGuest.phone,
        idType: step3Form.mainGuest.idType,
        idNumber: step3Form.mainGuest.idNumber
      },
      roommates: step3Form.roommates,
      depositAmount: step4Form.depositAmount,
      depositType: step4Form.depositType,
      voucherNo: step4Form.voucherNo,
      keyCardCount: step4Form.keyCardCount,
      remark: step4Form.remark
    }
    const res = await api.checkin.walkInCheckIn(payload)
    if (res.code === 200) {
      ElMessage.success('办理入住成功')
      router.push('/checkin/list')
    } else {
      ElMessage.error(res.message || '办理失败')
    }
  } catch {
    ElMessage.error('办理失败，请重试')
  } finally {
    submitting.value = false
  }
}

const handleCancel = () => {
  router.push('/checkin/list')
}

onMounted(() => {
  initDefaultDates()
  loadRoomTypes()
})
</script>

<style scoped lang="scss">
.walkin-create {
  padding: 16px;
}

.form-card {
  max-width: 1200px;
  margin: 0 auto;
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

.walkin-steps {
  margin: 24px 0 32px;
}

.step-content {
  min-height: 500px;
}

.step-panel {
  padding: 8px 0;
}

.room-label {
  color: #909399;
  margin-right: 4px;
}

.room-list {
  min-height: 200px;
}

.room-col {
  margin-bottom: 16px;
}

.room-card {
  border: 2px solid #e4e7ed;
  border-radius: 8px;
  padding: 16px;
  cursor: pointer;
  transition: all 0.2s;
  background-color: #fff;

  &:hover {
    border-color: #409eff;
    box-shadow: 0 2px 12px 0 rgba(64, 158, 255, 0.2);
  }

  &.active {
    border-color: #409eff;
    background-color: #ecf5ff;
  }
}

.room-number {
  font-size: 20px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 8px;
}

.room-type {
  font-size: 14px;
  color: #606266;
  margin-bottom: 4px;
}

.room-floor {
  font-size: 13px;
  color: #909399;
  margin-bottom: 8px;
}

.room-price {
  font-size: 16px;
  font-weight: 600;
  color: #f56c6c;
}

.selected-room-alert {
  margin-top: 16px;
}

.guest-item {
  padding: 8px 0;
  border-bottom: 1px dashed #e4e7ed;

  &:last-child {
    border-bottom: none;
  }
}

.confirm-alert {
  margin-bottom: 16px;
}

.confirm-card {
  height: 100%;
}

.confirm-card-title {
  display: flex;
  align-items: center;
  gap: 6px;
  font-weight: 500;
}

.confirm-item {
  display: flex;
  margin-bottom: 10px;
  line-height: 1.6;
}

.confirm-label {
  color: #909399;
  min-width: 90px;
  flex-shrink: 0;
}

.confirm-value {
  color: #303133;
  flex: 1;

  &.deposit-amount {
    font-size: 18px;
    font-weight: 600;
    color: #f56c6c;
  }
}

.guest-list {
  flex: 1;
}

.guest-row {
  margin-bottom: 4px;
}

.step-actions {
  display: flex;
  justify-content: center;
  gap: 12px;
  padding-top: 24px;
  border-top: 1px solid #ebeef5;
  margin-top: 24px;
}
</style>
