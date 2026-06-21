<template>
  <div class="checkin-detail" v-loading="loading">
    <div class="detail-header">
      <div class="header-left">
        <el-button @click="goBack" :icon="ArrowLeft">返回</el-button>
        <h2 class="checkin-no">
          入住单号：{{ checkIn.checkInNo }}
          <el-tag :type="getStatusTagType()" size="large" style="margin-left: 12px">
            {{ getStatusText() }}
          </el-tag>
        </h2>
      </div>
      <div class="header-actions">
        <el-button
          v-if="checkIn.status === 1 || checkIn.status === 3"
          type="success"
          @click="handleCheckout"
        >
          <el-icon><SwitchButton /></el-icon>
          办理退房
        </el-button>
        <el-button
          v-if="checkIn.status === 1 || checkIn.status === 3"
          type="warning"
          @click="openChangeRoomDialog"
        >
          <el-icon><Sort /></el-icon>
          换房
        </el-button>
        <el-button
          v-if="checkIn.status === 1 || checkIn.status === 3"
          type="info"
          @click="openExtendDialog"
        >
          <el-icon><Calendar /></el-icon>
          续住
        </el-button>
        <el-button
          v-if="checkIn.status === 2 && hasPermission('review:invitation:add')"
          type="primary"
          @click="handleInviteReview"
        >
          <el-icon><ChatDotRound /></el-icon>
          邀请评价
        </el-button>
      </div>
    </div>

    <el-row :gutter="16">
      <el-col :span="16">
        <el-card class="info-card">
          <template #header>
            <div class="card-header">
              <span class="card-title">入住信息</span>
            </div>
          </template>
          <el-descriptions :column="3" border>
            <el-descriptions-item label="客户姓名">{{ checkIn.customerName }}</el-descriptions-item>
            <el-descriptions-item label="联系电话">{{ checkIn.customerPhone }}</el-descriptions-item>
            <el-descriptions-item label="客户类型">
              {{ getCustomerTypeText(checkIn.customerType) }}
            </el-descriptions-item>
            <el-descriptions-item label="房型">{{ checkIn.roomTypeName }}</el-descriptions-item>
            <el-descriptions-item label="房号">{{ checkIn.roomNumber }}</el-descriptions-item>
            <el-descriptions-item label="入住人数">{{ checkIn.guestCount }}人</el-descriptions-item>
            <el-descriptions-item label="入住日期">{{ checkIn.checkInDate }}</el-descriptions-item>
            <el-descriptions-item label="预计退房">{{ checkIn.checkOutDate }}</el-descriptions-item>
            <el-descriptions-item label="已住天数">{{ stayedDays }}天</el-descriptions-item>
            <el-descriptions-item label="实际入住时间">
              {{ checkIn.actualCheckInTime }}
            </el-descriptions-item>
            <el-descriptions-item label="办理人">{{ checkIn.operatorName }}</el-descriptions-item>
            <el-descriptions-item label="预订来源">
              {{ getBookingSourceText(checkIn.bookingSource) }}
            </el-descriptions-item>
          </el-descriptions>
        </el-card>

        <el-card class="info-card" style="margin-top: 16px">
          <template #header>
            <div class="card-header">
              <span class="card-title">费用信息</span>
            </div>
          </template>
          <el-descriptions :column="3" border>
            <el-descriptions-item label="房费单价">¥{{ checkIn.roomPrice }}</el-descriptions-item>
            <el-descriptions-item label="房费小计">¥{{ checkIn.roomTotal }}</el-descriptions-item>
            <el-descriptions-item label="加床费">¥{{ checkIn.extraBedTotal || 0 }}</el-descriptions-item>
            <el-descriptions-item label="其他消费">¥{{ checkIn.otherFee || 0 }}</el-descriptions-item>
            <el-descriptions-item label="优惠折扣">¥{{ checkIn.discount || 0 }}</el-descriptions-item>
            <el-descriptions-item label="应付总额" class="highlight">
              <span style="color: #f56c6c; font-weight: bold">¥{{ checkIn.totalAmount }}</span>
            </el-descriptions-item>
            <el-descriptions-item label="已付金额">¥{{ checkIn.paidAmount || 0 }}</el-descriptions-item>
            <el-descriptions-item label="待结算金额" class="highlight">
              <span :style="{ color: checkIn.payableAmount > 0 ? '#f56c6c' : '#67c23a', fontWeight: 'bold' }">
                ¥{{ checkIn.payableAmount || 0 }}
              </span>
            </el-descriptions-item>
            <el-descriptions-item label="押金金额">¥{{ checkIn.depositAmount || 0 }}</el-descriptions-item>
          </el-descriptions>
        </el-card>

        <el-card class="info-card" style="margin-top: 16px">
          <template #header>
            <div class="card-header">
              <span class="card-title">入住人信息</span>
              <el-button type="primary" link size="small" @click="openGuestDialog">
                查看全部
              </el-button>
            </div>
          </template>
          <el-table :data="checkIn.guests" size="small">
            <el-table-column type="index" label="#" width="50" />
            <el-table-column label="姓名" prop="name" width="100">
              <template #default="{ row }">
                <span v-if="row.isMain === 1" style="color: #409eff; font-weight: bold">
                  {{ row.name }}（主）
                </span>
                <span v-else>{{ row.name }}</span>
              </template>
            </el-table-column>
            <el-table-column label="证件类型" width="100">
              <template #default="{ row }">{{ getIdTypeText(row.idType) }}</template>
            </el-table-column>
            <el-table-column label="证件号码" prop="idNumber" show-overflow-tooltip />
            <el-table-column label="联系电话" prop="phone" width="130" />
          </el-table>
        </el-card>

        <el-card class="info-card" style="margin-top: 16px">
          <template #header>
            <div class="card-header">
              <span class="card-title">房卡信息</span>
              <el-tag type="info" size="small">
                发放 {{ checkIn.keyCardCount }} 张 / 回收 {{ checkIn.keyCardReturned }} 张
              </el-tag>
            </div>
          </template>
          <el-table :data="checkIn.keyCards" size="small">
            <el-table-column type="index" label="#" width="50" />
            <el-table-column label="房卡号" prop="cardNo" width="150" />
            <el-table-column label="类型" width="100">
              <template #default="{ row }">{{ getCardTypeText(row.cardType) }}</template>
            </el-table-column>
            <el-table-column label="发放时间" prop="issueTime" width="170" />
            <el-table-column label="有效期至" prop="expireTime" width="170" />
            <el-table-column label="状态" width="100">
              <template #default="{ row }">
                <el-tag :type="getCardStatusType(row.status)" size="small">
                  {{ getCardStatusText(row.status) }}
                </el-tag>
              </template>
            </el-table-column>
          </el-table>
        </el-card>

        <el-card class="info-card" style="margin-top: 16px">
          <template #header>
            <div class="card-header">
              <span class="card-title">消费记录</span>
              <el-button
                v-if="checkIn.status === 1"
                type="primary"
                link
                size="small"
                @click="openConsumptionDialog"
              >
                添加消费
              </el-button>
            </div>
          </template>
          <el-table :data="checkIn.consumptions" size="small" empty-text="暂无消费记录">
            <el-table-column type="index" label="#" width="50" />
            <el-table-column label="消费时间" prop="consumptionTime" width="170" />
            <el-table-column label="消费类型" width="100">
              <template #default="{ row }">
                {{ getConsumptionTypeText(row.consumptionType) }}
              </template>
            </el-table-column>
            <el-table-column label="项目名称" prop="itemName" />
            <el-table-column label="单价" prop="unitPrice" width="100" align="right" />
            <el-table-column label="数量" prop="quantity" width="80" align="center" />
            <el-table-column label="金额" prop="totalAmount" width="100" align="right">
              <template #default="{ row }">¥{{ row.totalAmount }}</template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>

      <el-col :span="8">
        <el-card class="side-card">
          <template #header>
            <span class="card-title">押金信息</span>
          </template>
          <div class="deposit-info">
            <div class="deposit-amount">
              <span class="label">押金金额</span>
              <span class="value">¥{{ checkIn.depositAmount || 0 }}</span>
            </div>
            <div class="deposit-method">
              <span class="label">押金方式</span>
              <span class="value">{{ getDepositMethodText(checkIn.depositMethod) }}</span>
            </div>
            <div class="deposit-voucher">
              <span class="label">凭证号</span>
              <span class="value">{{ checkIn.depositVoucherNo || '-' }}</span>
            </div>
            <el-button
              v-if="checkIn.status === 1"
              type="primary"
              size="small"
              style="width: 100%; margin-top: 12px"
              @click="openDepositDialog"
            >
              修改押金
            </el-button>
          </div>
        </el-card>

        <el-card class="side-card" style="margin-top: 16px">
          <template #header>
            <span class="card-title">特殊要求</span>
          </template>
          <p style="margin: 0; color: #606266; line-height: 1.6">
            {{ checkIn.specialRequirements || '暂无特殊要求' }}
          </p>
        </el-card>

        <el-card class="side-card" style="margin-top: 16px">
          <template #header>
            <span class="card-title">关联预订</span>
          </template>
          <div v-if="checkIn.bookingNo">
            <div class="booking-info">
              <span class="label">预订单号</span>
              <el-button type="primary" link style="padding: 0">
                {{ checkIn.bookingNo }}
              </el-button>
            </div>
          </div>
          <div v-else>
            <span style="color: #909399">无关联预订单（散客）</span>
          </div>
        </el-card>

        <el-card class="side-card" style="margin-top: 16px">
          <template #header>
            <span class="card-title">操作记录</span>
          </template>
          <div class="timeline">
            <el-timeline>
              <el-timeline-item
                v-for="log in checkIn.operationLogs"
                :key="log.id"
                :timestamp="log.createTime"
                placement="top"
                :type="getLogType(log.operationType)"
              >
                <div class="log-item">
                  <div class="log-desc">{{ log.operationDesc }}</div>
                  <div class="log-operator">操作人：{{ log.operatorName || '系统' }}</div>
                </div>
              </el-timeline-item>
            </el-timeline>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-dialog v-model="guestDialogVisible" title="入住人信息" width="700px">
      <el-table :data="checkIn.guests" border>
        <el-table-column type="index" label="#" width="50" />
        <el-table-column label="姓名" prop="name" width="100">
          <template #default="{ row }">
            <span v-if="row.isMain === 1" style="color: #409eff; font-weight: bold">
              {{ row.name }}（主入住人）
            </span>
            <span v-else>{{ row.name }}</span>
          </template>
        </el-table-column>
        <el-table-column label="性别" width="60">
          <template #default="{ row }">{{ row.gender === 1 ? '男' : '女' }}</template>
        </el-table-column>
        <el-table-column label="证件类型" width="100">
          <template #default="{ row }">{{ getIdTypeText(row.idType) }}</template>
        </el-table-column>
        <el-table-column label="证件号码" prop="idNumber" />
        <el-table-column label="联系电话" prop="phone" width="130" />
      </el-table>
    </el-dialog>

    <el-dialog v-model="consumptionDialogVisible" title="添加消费" width="500px">
      <el-form :model="consumptionForm" label-width="100px">
        <el-form-item label="消费类型">
          <el-select v-model="consumptionForm.consumptionType" placeholder="请选择消费类型" style="width: 100%">
            <el-option label="餐饮" :value="2" />
            <el-option label="迷你吧" :value="3" />
            <el-option label="洗衣" :value="4" />
            <el-option label="通讯" :value="5" />
            <el-option label="接送" :value="6" />
            <el-option label="其他" :value="7" />
          </el-select>
        </el-form-item>
        <el-form-item label="项目名称">
          <el-input v-model="consumptionForm.itemName" placeholder="请输入消费项目名称" />
        </el-form-item>
        <el-form-item label="单价">
          <el-input-number
            v-model="consumptionForm.unitPrice"
            :min="0"
            :precision="2"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="数量">
          <el-input-number
            v-model="consumptionForm.quantity"
            :min="1"
            :precision="2"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="consumptionForm.remark" type="textarea" :rows="2" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="consumptionDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmAddConsumption" :loading="submitting">
          确认添加
        </el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="depositDialogVisible" title="修改押金" width="500px">
      <el-form :model="depositForm" label-width="100px">
        <el-form-item label="押金金额">
          <el-input-number
            v-model="depositForm.depositAmount"
            :min="0"
            :precision="2"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="押金方式">
          <el-select v-model="depositForm.depositMethod" style="width: 100%">
            <el-option label="现金押金" :value="1" />
            <el-option label="信用卡预授权" :value="2" />
            <el-option label="免押金" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item label="凭证号">
          <el-input v-model="depositForm.depositVoucherNo" placeholder="请输入凭证号/预授权号" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="depositDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmUpdateDeposit" :loading="submitting">
          确认修改
        </el-button>
      </template>
    </el-dialog>

    <el-dialog
      v-model="changeRoomDialogVisible"
      title="办理换房"
      width="600px"
      @close="resetChangeRoomForm"
    >
      <el-form :model="changeRoomForm" label-width="100px">
        <el-form-item label="原房间">
          <span>{{ checkIn.roomNumber }} - {{ checkIn.roomTypeName }}</span>
        </el-form-item>
        <el-form-item label="新房型">
          <el-select
            v-model="changeRoomForm.roomTypeId"
            placeholder="请选择房型"
            style="width: 100%"
            @change="loadAvailableRoomsForChange"
          >
            <el-option
              v-for="rt in roomTypeList"
              :key="rt.id"
              :label="rt.typeName"
              :value="rt.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="新房间">
          <el-select
            v-model="changeRoomForm.newRoomId"
            placeholder="请选择房间"
            style="width: 100%"
            :loading="loadingRooms"
          >
            <el-option
              v-for="room in availableRooms"
              :key="room.id"
              :label="room.roomNumber + ' - ' + (room.orientation || '')"
              :value="room.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="换房原因">
          <el-select v-model="changeRoomForm.changeReason" placeholder="请选择原因" style="width: 100%">
            <el-option label="客户要求" :value="1" />
            <el-option label="房间问题" :value="2" />
            <el-option label="升级房型" :value="3" />
            <el-option label="降级房型" :value="4" />
            <el-option label="其他" :value="5" />
          </el-select>
        </el-form-item>
        <el-form-item label="详细说明">
          <el-input
            v-model="changeRoomForm.changeDetail"
            type="textarea"
            :rows="3"
            placeholder="请输入详细说明"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="changeRoomDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmChangeRoom" :loading="submitting">确认换房</el-button>
      </template>
    </el-dialog>

    <el-dialog
      v-model="extendDialogVisible"
      title="办理续住"
      width="500px"
      @close="resetExtendForm"
    >
      <el-form :model="extendForm" label-width="120px">
        <el-form-item label="原退房日期">
          <span>{{ checkIn.checkOutDate }}</span>
        </el-form-item>
        <el-form-item label="新退房日期" required>
          <el-date-picker
            v-model="extendForm.newCheckOutDate"
            type="date"
            placeholder="请选择新退房日期"
            style="width: 100%"
            @change="calculateExtendDays"
          />
        </el-form-item>
        <el-form-item label="续住天数">
          <span style="color: #409eff; font-weight: bold">{{ extendForm.extendDays }} 天</span>
        </el-form-item>
        <el-form-item label="续住费用">
          <span style="color: #f56c6c; font-weight: bold">
            ¥{{ extendForm.extendAmount || 0 }}
          </span>
        </el-form-item>
        <el-form-item label="续住原因">
          <el-input
            v-model="extendForm.reason"
            type="textarea"
            :rows="2"
            placeholder="请输入续住原因（可选）"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="extendDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmExtend" :loading="submitting">确认续住</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="inviteReviewDialogVisible" title="邀请评价" width="500px">
      <div v-if="invitationCreated">
        <el-alert type="success" :closable="false" style="margin-bottom: 16px">
          <template #title>评价邀请创建成功</template>
          <div style="margin-top: 8px">
            <div style="margin-bottom: 8px; color: #606266">评价链接：</div>
            <div style="display: flex; align-items: center; gap: 8px">
              <el-input :value="reviewLink" readonly style="flex: 1" />
              <el-button type="primary" @click="handleCopyReviewLink">复制链接</el-button>
            </div>
            <div style="margin-top: 8px; color: #909399; font-size: 12px">
              链接有效期：7天
            </div>
          </div>
        </el-alert>
      </div>
      <div v-else>
        <el-alert type="info" :closable="false">
          <template #title>创建评价邀请</template>
          <div style="margin-top: 8px; color: #606266">
            点击确认后，系统将为该入住单创建评价邀请记录，客人可通过评价链接提交入住体验反馈。
          </div>
        </el-alert>
      </div>
      <template #footer>
        <el-button @click="inviteReviewDialogVisible = false">关闭</el-button>
        <el-button v-if="!invitationCreated" type="primary" @click="confirmCreateInvitation" :loading="creatingInvitation">
          确认创建
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  ArrowLeft, SwitchButton, Sort, Calendar, ChatDotRound
} from '@element-plus/icons-vue'
import api from '@/api'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()
const { hasPermission } = userStore

const route = useRoute()
const router = useRouter()

const loading = ref(false)
const submitting = ref(false)
const loadingRooms = ref(false)
const checkInId = route.params.id

const checkIn = ref({
  guests: [],
  keyCards: [],
  consumptions: [],
  operationLogs: []
})

const roomTypeList = ref([])
const availableRooms = ref([])

const guestDialogVisible = ref(false)
const consumptionDialogVisible = ref(false)
const depositDialogVisible = ref(false)
const changeRoomDialogVisible = ref(false)
const extendDialogVisible = ref(false)

const consumptionForm = reactive({
  consumptionType: null,
  itemName: '',
  unitPrice: 0,
  quantity: 1,
  remark: ''
})

const depositForm = reactive({
  depositAmount: 0,
  depositMethod: 1,
  depositVoucherNo: ''
})

const changeRoomForm = reactive({
  newRoomId: null,
  roomTypeId: null,
  changeReason: null,
  changeDetail: ''
})

const extendForm = reactive({
  newCheckOutDate: null,
  extendDays: 0,
  extendAmount: 0,
  reason: ''
})

const inviteReviewDialogVisible = ref(false)
const creatingInvitation = ref(false)
const invitationCreated = ref(false)
const reviewLink = ref('')

const stayedDays = computed(() => {
  if (checkIn.value.status === 2 && checkIn.value.stayedDays) {
    return checkIn.value.stayedDays
  }
  const checkInDate = new Date(checkIn.value.checkInDate)
  const today = new Date()
  const diff = Math.floor((today - checkInDate) / (1000 * 60 * 60 * 24))
  return diff < 0 ? 0 : diff
})

const loadDetail = async () => {
  loading.value = true
  try {
    const res = await api.checkin.get(checkInId)
    if (res.code === 200) {
      checkIn.value = res.data
    }
  } catch (e) {
    console.error('加载入住单详情失败', e)
  } finally {
    loading.value = false
  }
}

const loadRoomTypes = async () => {
  try {
    const res = await api.hotel.getRoomTypes()
    if (res.code === 200) {
      roomTypeList.value = res.data
    }
  } catch (e) {
    console.error('加载房型列表失败', e)
  }
}

const goBack = () => {
  router.back()
}

const handleCheckout = () => {
  router.push({ path: '/checkin/checkout', query: { checkInId: checkInId } })
}

const getStatusText = () => {
  if (checkIn.value.isOverdue === 1) return '超期未退'
  const map = { 1: '在住', 2: '已退房', 3: '超期未退' }
  return map[checkIn.value.status] || '未知'
}

const getStatusTagType = () => {
  if (checkIn.value.isOverdue === 1 || checkIn.value.status === 3) return 'danger'
  const map = { 1: 'success', 2: 'info' }
  return map[checkIn.value.status] || ''
}

const getCustomerTypeText = (type) => {
  const map = { 1: '散客', 2: '会员', 3: '协议客户', 4: '团队' }
  return map[type] || '未知'
}

const getBookingSourceText = (source) => {
  const map = {
    1: '前台', 2: '官网', 3: '第三方平台',
    4: '协议单位', 5: '旅行社', 6: '其他'
  }
  return map[source] || '未知'
}

const getIdTypeText = (type) => {
  const map = {
    1: '身份证', 2: '护照', 3: '港澳通行证',
    4: '台胞证', 5: '军官证', 6: '其他'
  }
  return map[type] || '未知'
}

const getCardTypeText = (type) => {
  const map = { 1: '普通房卡', 2: '电子房卡', 3: '临时卡' }
  return map[type] || '未知'
}

const getCardStatusText = (status) => {
  const map = { 1: '使用中', 2: '已归还', 3: '已挂失', 4: '已注销' }
  return map[status] || '未知'
}

const getCardStatusType = (status) => {
  const map = { 1: 'success', 2: 'info', 3: 'warning', 4: 'danger' }
  return map[status] || ''
}

const getConsumptionTypeText = (type) => {
  const map = {
    1: '房费', 2: '餐饮', 3: '迷你吧',
    4: '洗衣', 5: '通讯', 6: '接送', 7: '其他'
  }
  return map[type] || '未知'
}

const getDepositMethodText = (method) => {
  const map = { 1: '现金押金', 2: '信用卡预授权', 3: '免押金' }
  return map[method] || '未知'
}

const getLogType = (type) => {
  const map = {
    1: 'primary', 2: 'info', 3: 'warning', 4: 'success',
    5: '', 6: '', 7: '', 8: '', 9: 'danger', 10: ''
  }
  return map[type] || ''
}

const openGuestDialog = () => {
  guestDialogVisible.value = true
}

const openConsumptionDialog = () => {
  consumptionForm.consumptionType = null
  consumptionForm.itemName = ''
  consumptionForm.unitPrice = 0
  consumptionForm.quantity = 1
  consumptionForm.remark = ''
  consumptionDialogVisible.value = true
}

const confirmAddConsumption = async () => {
  if (!consumptionForm.consumptionType) {
    ElMessage.warning('请选择消费类型')
    return
  }
  if (!consumptionForm.itemName) {
    ElMessage.warning('请输入消费项目名称')
    return
  }
  submitting.value = true
  try {
    const res = await api.checkin.addConsumption({
      checkInId: checkInId,
      ...consumptionForm
    })
    if (res.code === 200) {
      ElMessage.success('添加消费成功')
      consumptionDialogVisible.value = false
      loadDetail()
    }
  } catch (e) {
    console.error('添加消费失败', e)
  } finally {
    submitting.value = false
  }
}

const openDepositDialog = () => {
  depositForm.depositAmount = checkIn.value.depositAmount || 0
  depositForm.depositMethod = checkIn.value.depositMethod || 1
  depositForm.depositVoucherNo = checkIn.value.depositVoucherNo || ''
  depositDialogVisible.value = true
}

const confirmUpdateDeposit = async () => {
  submitting.value = true
  try {
    const res = await api.checkin.updateDeposit({
      checkInId: checkInId,
      ...depositForm
    })
    if (res.code === 200) {
      ElMessage.success('押金修改成功')
      depositDialogVisible.value = false
      loadDetail()
    }
  } catch (e) {
    console.error('修改押金失败', e)
  } finally {
    submitting.value = false
  }
}

const openChangeRoomDialog = async () => {
  changeRoomForm.roomTypeId = checkIn.value.roomTypeId
  changeRoomDialogVisible.value = true
  await loadAvailableRoomsForChange()
}

const loadAvailableRoomsForChange = async () => {
  if (!changeRoomForm.roomTypeId) return
  loadingRooms.value = true
  try {
    const res = await api.checkin.availableRoomsForChange(checkInId, changeRoomForm.roomTypeId)
    if (res.code === 200) {
      availableRooms.value = res.data
    }
  } catch (e) {
    console.error('加载可用房间失败', e)
  } finally {
    loadingRooms.value = false
  }
}

const resetChangeRoomForm = () => {
  changeRoomForm.newRoomId = null
  changeRoomForm.roomTypeId = null
  changeRoomForm.changeReason = null
  changeRoomForm.changeDetail = ''
  availableRooms.value = []
}

const confirmChangeRoom = async () => {
  if (!changeRoomForm.newRoomId) {
    ElMessage.warning('请选择新房间')
    return
  }
  if (!changeRoomForm.changeReason) {
    ElMessage.warning('请选择换房原因')
    return
  }
  submitting.value = true
  try {
    const res = await api.checkin.changeRoom({
      checkInId: checkInId,
      newRoomId: changeRoomForm.newRoomId,
      changeReason: changeRoomForm.changeReason,
      changeDetail: changeRoomForm.changeDetail
    })
    if (res.code === 200) {
      ElMessage.success('换房成功')
      changeRoomDialogVisible.value = false
      loadDetail()
    }
  } catch (e) {
    console.error('换房失败', e)
  } finally {
    submitting.value = false
  }
}

const openExtendDialog = () => {
  extendForm.newCheckOutDate = null
  extendForm.extendDays = 0
  extendForm.extendAmount = 0
  extendForm.reason = ''
  extendDialogVisible.value = true
}

const calculateExtendDays = () => {
  if (!extendForm.newCheckOutDate) {
    extendForm.extendDays = 0
    extendForm.extendAmount = 0
    return
  }
  const originalDate = new Date(checkIn.value.checkOutDate)
  const newDate = new Date(extendForm.newCheckOutDate)
  const days = Math.floor((newDate - originalDate) / (1000 * 60 * 60 * 24))
  extendForm.extendDays = days > 0 ? days : 0
  extendForm.extendAmount = extendForm.extendDays * (checkIn.value.roomPrice || 0)
}

const resetExtendForm = () => {
  extendForm.newCheckOutDate = null
  extendForm.extendDays = 0
  extendForm.extendAmount = 0
  extendForm.reason = ''
}

const confirmExtend = async () => {
  if (!extendForm.newCheckOutDate) {
    ElMessage.warning('请选择新退房日期')
    return
  }
  if (extendForm.extendDays <= 0) {
    ElMessage.warning('续住天数必须大于0')
    return
  }
  submitting.value = true
  try {
    const res = await api.checkin.extendStay({
      checkInId: checkInId,
      newCheckOutDate: extendForm.newCheckOutDate,
      reason: extendForm.reason
    })
    if (res.code === 200) {
      ElMessage.success('续住成功')
      extendDialogVisible.value = false
      loadDetail()
    }
  } catch (e) {
    console.error('续住失败', e)
  } finally {
    submitting.value = false
  }
}

const handleInviteReview = async () => {
  if (checkIn.value.status !== 2) {
    ElMessage.warning('客人未退房，无法邀请评价')
    return
  }
  try {
    const existingInvitation = await api.reviewInvitation.getByCheckInId(checkInId)
    if (existingInvitation.code === 200 && existingInvitation.data) {
      ElMessage.info('已创建评价邀请')
      reviewLink.value = await api.reviewInvitation.getLink(existingInvitation.data.id)
      if (reviewLink.value.code === 200) {
        reviewLink.value = reviewLink.value.data
      }
      invitationCreated.value = true
      inviteReviewDialogVisible.value = true
      return
    }
  } catch (e) {
    console.error('检查评价邀请失败', e)
  }
  invitationCreated.value = false
  reviewLink.value = ''
  inviteReviewDialogVisible.value = true
}

const confirmCreateInvitation = async () => {
  creatingInvitation.value = true
  try {
    const res = await api.reviewInvitation.create({ checkInId: checkInId })
    if (res.code === 200) {
      const linkRes = await api.reviewInvitation.getLink(res.data.id)
      if (linkRes.code === 200) {
        reviewLink.value = linkRes.data
      }
      invitationCreated.value = true
      ElMessage.success('评价邀请创建成功')
    } else {
      ElMessage.error(res.message || '创建失败')
    }
  } catch (e) {
    console.error('创建评价邀请失败', e)
    ElMessage.error('创建失败')
  } finally {
    creatingInvitation.value = false
  }
}

const handleCopyReviewLink = async () => {
  try {
    await navigator.clipboard.writeText(reviewLink.value)
    ElMessage.success('链接已复制，可用于测试')
  } catch (e) {
    console.error('复制失败', e)
    ElMessage.error('复制失败')
  }
}

onMounted(() => {
  loadDetail()
  loadRoomTypes()
})
</script>

<style scoped lang="scss">
.checkin-detail {
  .detail-header {
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

      .checkin-no {
        margin: 0;
        font-size: 20px;
        font-weight: bold;
        color: #303133;
      }
    }
  }

  .info-card {
    .card-title {
      font-weight: bold;
      font-size: 16px;
    }
  }

  .side-card {
    .card-title {
      font-weight: bold;
    }

    .deposit-info {
      .deposit-amount,
      .deposit-method,
      .deposit-voucher {
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

      .deposit-amount .value {
        font-size: 20px;
        font-weight: bold;
        color: #409eff;
      }
    }

    .timeline {
      max-height: 400px;
      overflow-y: auto;

      .log-item {
        .log-desc {
          font-size: 14px;
          color: #303133;
        }

        .log-operator {
          font-size: 12px;
          color: #909399;
          margin-top: 4px;
        }
      }
    }
  }
}
</style>
