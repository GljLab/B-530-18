<template>
  <div class="booking-detail-container">
    <div class="detail-header">
      <div class="header-left">
        <el-button @click="router.push('/booking/list')">
          <el-icon><ArrowLeft /></el-icon>返回列表
        </el-button>
        <span class="order-number">{{ bookingData?.bookingNo || '' }}</span>
        <el-tag v-if="bookingData" :type="statusTagType(bookingData.status)" size="large" effect="dark">
          {{ statusLabel(bookingData.status) }}
        </el-tag>
      </div>
      <div class="header-actions">
        <el-button
          v-if="bookingData?.status === 1 && hasPermission('booking:confirm')"
          type="success"
          @click="handleConfirm"
        >
          <el-icon><Check /></el-icon>确认
        </el-button>
        <el-button
          v-if="(bookingData?.status === 1 || bookingData?.status === 2) && hasPermission('booking:edit')"
          type="primary"
          @click="openEditDialog"
        >
          <el-icon><Edit /></el-icon>修改
        </el-button>
        <el-button
          v-if="(bookingData?.status === 1 || bookingData?.status === 2) && hasPermission('booking:cancel')"
          type="danger"
          @click="openCancelDialog"
        >
          <el-icon><Close /></el-icon>取消
        </el-button>
        <el-button
          v-if="(bookingData?.status === 2 || bookingData?.status === 3) && hasPermission('booking:checkin')"
          type="success"
          @click="openCheckinDialog"
        >
          <el-icon><Key /></el-icon>办理入住
        </el-button>
        <el-button
          v-if="bookingData?.status === 3 && hasPermission('booking:refund')"
          type="warning"
          @click="openRefundDialog"
        >
          <el-icon><Money /></el-icon>申请退款
        </el-button>
        <el-button
          v-if="(bookingData?.status === 1 || bookingData?.status === 2) && hasPermission('booking:changeRoom')"
          type="primary"
          @click="openChangeRoomDialog"
        >
          <el-icon><Switch /></el-icon>更换房间
        </el-button>
        <el-button
          v-if="bookingData?.status === 5"
          type="primary"
          @click="viewCheckinRecord"
        >
          <el-icon><Document /></el-icon>查看入住
        </el-button>
      </div>
    </div>

    <el-card v-if="bookingData" shadow="never" class="info-card">
      <template #header><span class="card-title">预订信息</span></template>
      <el-descriptions :column="2" border>
        <el-descriptions-item label="预订时间">
          <span class="field-readonly">{{ bookingData.createTime || '-' }}</span>
        </el-descriptions-item>
        <el-descriptions-item label="预订来源">
          <el-tag :type="sourceTagType(bookingData.source)" effect="light">
            {{ sourceLabel(bookingData.source) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="担保方式">
          <span class="field-readonly">{{ guaranteeLabel(bookingData.guaranteeType) }}</span>
        </el-descriptions-item>
        <el-descriptions-item label="已付金额">
          <span class="field-readonly price">¥{{ bookingData.paidAmount || 0 }}</span>
        </el-descriptions-item>
      </el-descriptions>
    </el-card>

    <el-card v-if="bookingData" shadow="never" class="info-card">
      <template #header><span class="card-title">房间信息</span></template>
      <el-descriptions :column="2" border>
        <el-descriptions-item label="房型">
          <span class="field-readonly">{{ bookingData.roomTypeName || '-' }}</span>
        </el-descriptions-item>
        <el-descriptions-item label="房号">
          <span class="field-readonly">{{ bookingData.roomNumber || '-' }}</span>
        </el-descriptions-item>
        <el-descriptions-item label="入住日期">
          <span class="field-readonly">{{ bookingData.checkinDate || '-' }}</span>
        </el-descriptions-item>
        <el-descriptions-item label="退房日期">
          <span class="field-readonly">{{ bookingData.checkoutDate || '-' }}</span>
        </el-descriptions-item>
        <el-descriptions-item label="入住天数">
          <span class="field-readonly">{{ bookingData.days || 0 }} 天</span>
        </el-descriptions-item>
        <el-descriptions-item label="房价">
          <span class="field-readonly price">¥{{ bookingData.roomPrice || 0 }}/晚</span>
        </el-descriptions-item>
      </el-descriptions>
    </el-card>

    <el-card v-if="bookingData" shadow="never" class="info-card">
      <template #header><span class="card-title">客户信息</span></template>
      <el-descriptions :column="2" border>
        <el-descriptions-item label="客户姓名">
          <span class="field-readonly">{{ bookingData.customerName || '-' }}</span>
        </el-descriptions-item>
        <el-descriptions-item label="手机号">
          <span class="field-readonly">{{ bookingData.customerPhone || '-' }}</span>
        </el-descriptions-item>
        <el-descriptions-item label="证件类型">
          <span class="field-readonly">{{ idTypeLabel(bookingData.customerIdType) }}</span>
        </el-descriptions-item>
        <el-descriptions-item label="证件号">
          <span class="field-readonly">{{ bookingData.customerIdNo || '-' }}</span>
        </el-descriptions-item>
        <el-descriptions-item label="客户偏好" :span="2">
          <span v-if="bookingData.customerPreference" class="remark-text">{{ bookingData.customerPreference }}</span>
          <span v-else class="text-muted">-</span>
        </el-descriptions-item>
      </el-descriptions>
    </el-card>

    <el-card v-if="bookingData" shadow="never" class="info-card">
      <template #header><span class="card-title">入住人信息</span></template>
      <el-descriptions :column="2" border>
        <el-descriptions-item label="入住人数">
          <span class="field-readonly">{{ bookingData.guestCount || 0 }} 人</span>
        </el-descriptions-item>
        <el-descriptions-item label="联系电话">
          <span class="field-readonly">{{ bookingData.contactPhone || '-' }}</span>
        </el-descriptions-item>
        <el-descriptions-item label="入住人姓名" :span="2">
          <div v-if="bookingData.guests && bookingData.guests.length" class="guest-list">
            <el-tag v-for="(guest, idx) in bookingData.guests" :key="idx" class="guest-tag">
              {{ guest.name }}
            </el-tag>
          </div>
          <span v-else class="text-muted">-</span>
        </el-descriptions-item>
      </el-descriptions>
    </el-card>

    <el-card v-if="bookingData" shadow="never" class="info-card">
      <template #header><span class="card-title">特殊要求</span></template>
      <el-descriptions :column="2" border>
        <el-descriptions-item label="加床数量">
          <span class="field-readonly">{{ bookingData.extraBedCount || 0 }} 张</span>
        </el-descriptions-item>
        <el-descriptions-item label="预计到店时间">
          <span class="field-readonly">{{ bookingData.expectedArrivalTime || '-' }}</span>
        </el-descriptions-item>
        <el-descriptions-item label="特殊要求备注" :span="2">
          <span v-if="bookingData.specialRequest" class="remark-text">{{ bookingData.specialRequest }}</span>
          <span v-else class="text-muted">-</span>
        </el-descriptions-item>
      </el-descriptions>
    </el-card>

    <el-card v-if="bookingData" shadow="never" class="info-card">
      <template #header><span class="card-title">价格明细</span></template>
      <el-table v-if="priceDetails.length" :data="priceDetails" border class="price-table">
        <el-table-column prop="date" label="日期" width="120" />
        <el-table-column prop="roomPrice" label="房费" width="100">
          <template #default="{ row }">¥{{ row.roomPrice || 0 }}</template>
        </el-table-column>
        <el-table-column prop="extraBedFee" label="加床费" width="100">
          <template #default="{ row }">¥{{ row.extraBedFee || 0 }}</template>
        </el-table-column>
        <el-table-column prop="otherFee" label="其他费用" width="100">
          <template #default="{ row }">¥{{ row.otherFee || 0 }}</template>
        </el-table-column>
        <el-table-column prop="subtotal" label="小计" width="100">
          <template #default="{ row }">¥{{ row.subtotal || 0 }}</template>
        </el-table-column>
      </el-table>
      <el-descriptions :column="2" border class="price-summary">
        <el-descriptions-item label="加床费合计">
          <span class="field-readonly">¥{{ bookingData.extraBedFee || 0 }}</span>
        </el-descriptions-item>
        <el-descriptions-item label="其他费用合计">
          <span class="field-readonly">¥{{ bookingData.otherFee || 0 }}</span>
        </el-descriptions-item>
        <el-descriptions-item label="优惠折扣">
          <span class="field-readonly discount">-¥{{ bookingData.discount || 0 }}</span>
        </el-descriptions-item>
        <el-descriptions-item label="应付总额">
          <span class="field-readonly total-price">¥{{ bookingData.totalAmount || 0 }}</span>
        </el-descriptions-item>
      </el-descriptions>
    </el-card>

    <el-card shadow="never" class="info-card">
      <template #header><span class="card-title">操作记录</span></template>
      <el-timeline v-if="operationLogs.length">
        <el-timeline-item
          v-for="log in operationLogs"
          :key="log.id"
          :timestamp="log.createTime"
          placement="top"
          :type="logTimelineType(log.type)"
        >
          <div class="log-content">
            <span class="log-operator">{{ log.operatorName || '系统' }}</span>
            <span class="log-type">{{ logTypeLabel(log.type) }}</span>
            <template v-if="log.type === 'status'">
              <el-tag v-if="log.oldStatus" :type="statusTagType(log.oldStatus)" size="small">
                {{ statusLabel(log.oldStatus) }}
              </el-tag>
              <span v-if="log.oldStatus" class="log-arrow">→</span>
              <el-tag :type="statusTagType(log.newStatus)" size="small">
                {{ statusLabel(log.newStatus) }}
              </el-tag>
            </template>
            <template v-else-if="log.type === 'change'">
              <span class="log-field">{{ log.fieldName }}:</span>
              <span class="log-old-value">{{ log.oldValue || '-' }}</span>
              <span class="log-arrow">→</span>
              <span class="log-new-value">{{ log.newValue || '-' }}</span>
            </template>
            <template v-else-if="log.type === 'cancel'">
              <span class="log-cancel-reason">取消原因：{{ log.cancelReason }}</span>
              <span v-if="log.penalty" class="log-penalty">违约金：¥{{ log.penalty }}</span>
            </template>
            <span v-if="log.remark" class="log-remark">{{ log.remark }}</span>
          </div>
        </el-timeline-item>
      </el-timeline>
      <el-empty v-else description="暂无操作记录" :image-size="80" />
    </el-card>

    <el-dialog
      v-model="editDialogVisible"
      title="修改预订"
      width="680px"
      destroy-on-close
    >
      <el-form :model="editForm" label-width="110px" ref="editFormRef">
        <el-form-item label="入住日期" required>
          <el-date-picker
            v-model="editForm.checkinDate"
            type="date"
            placeholder="选择入住日期"
            style="width: 100%"
            value-format="YYYY-MM-DD"
          />
        </el-form-item>
        <el-form-item label="退房日期" required>
          <el-date-picker
            v-model="editForm.checkoutDate"
            type="date"
            placeholder="选择退房日期"
            style="width: 100%"
            value-format="YYYY-MM-DD"
          />
        </el-form-item>
        <el-form-item label="入住人数" required>
          <el-input-number v-model="editForm.guestCount" :min="1" :max="10" style="width: 100%" />
        </el-form-item>
        <el-form-item label="入住人信息">
          <div class="guests-edit-container">
            <div v-for="(guest, idx) in editForm.guests" :key="idx" class="guest-edit-item">
              <el-input v-model="guest.name" placeholder="入住人姓名" style="width: 48%" />
              <el-input v-model="guest.idNo" placeholder="证件号" style="width: 48%" />
              <el-button type="danger" text @click="removeGuest(idx)">
                <el-icon><Delete /></el-icon>
              </el-button>
            </div>
            <el-button type="primary" text @click="addGuest">
              <el-icon><Plus /></el-icon>添加入住人
            </el-button>
          </div>
        </el-form-item>
        <el-form-item label="加床数量">
          <el-input-number v-model="editForm.extraBedCount" :min="0" :max="5" style="width: 100%" />
        </el-form-item>
        <el-form-item label="预计到店时间">
          <el-time-picker
            v-model="editForm.expectedArrivalTime"
            placeholder="选择预计到店时间"
            style="width: 100%"
            value-format="HH:mm:ss"
          />
        </el-form-item>
        <el-form-item label="特殊要求">
          <el-input
            v-model="editForm.specialRequest"
            type="textarea"
            :rows="3"
            placeholder="请输入特殊要求"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="editSaving" @click="handleEdit">确认修改</el-button>
      </template>
    </el-dialog>

    <el-dialog
      v-model="changeRoomDialogVisible"
      title="更换房间"
      width="720px"
      destroy-on-close
    >
      <el-form :model="changeRoomForm" label-width="100px">
        <el-form-item label="查询条件">
          <el-button type="primary" @click="queryAvailableRooms">
            <el-icon><Search /></el-icon>查询可用房间
          </el-button>
        </el-form-item>
      </el-form>
      <div v-if="availableRooms.length" class="room-list">
        <el-table :data="availableRooms" border @selection-change="handleRoomSelection">
          <el-table-column type="selection" width="50" />
          <el-table-column prop="roomNumber" label="房号" width="100" />
          <el-table-column prop="roomTypeName" label="房型" width="120" />
          <el-table-column prop="floorName" label="楼层" width="100" />
          <el-table-column prop="price" label="房价" width="100">
            <template #default="{ row }">¥{{ row.price || 0 }}/晚</template>
          </el-table-column>
          <el-table-column prop="priceDiff" label="差价" width="100">
            <template #default="{ row }">
              <span :class="row.priceDiff > 0 ? 'price-up' : row.priceDiff < 0 ? 'price-down' : ''">
                {{ row.priceDiff > 0 ? '+' : '' }}¥{{ row.priceDiff || 0 }}
              </span>
            </template>
          </el-table-column>
          <el-table-column prop="status" label="状态">
            <template #default="{ row }">
              <el-tag :type="roomStatusTagType(row.status)" effect="light">
                {{ roomStatusLabel(row.status) }}
              </el-tag>
            </template>
          </el-table-column>
        </el-table>
        <div v-if="selectedRoom" class="price-diff-info">
          <span>原房价：¥{{ bookingData?.roomPrice || 0 }}/晚</span>
          <span class="arrow">→</span>
          <span>新房价：¥{{ selectedRoom.price || 0 }}/晚</span>
          <span class="total-diff">
            总价差：{{ totalPriceDiff > 0 ? '+' : '' }}¥{{ totalPriceDiff }}
          </span>
        </div>
      </div>
      <el-empty v-else description="暂无可用房间" :image-size="60" />
      <template #footer>
        <el-button @click="changeRoomDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="changeRoomSaving" :disabled="!selectedRoom" @click="handleChangeRoom">
          确认换房
        </el-button>
      </template>
    </el-dialog>

    <el-dialog
      v-model="cancelDialogVisible"
      title="取消预订"
      width="520px"
      destroy-on-close
    >
      <el-form :model="cancelForm" label-width="100px" ref="cancelFormRef">
        <el-form-item label="取消原因" required>
          <el-select v-model="cancelForm.reason" placeholder="请选择取消原因" style="width: 100%">
            <el-option label="客户原因" :value="1" />
            <el-option label="酒店原因" :value="2" />
            <el-option label="不可抗力" :value="3" />
            <el-option label="其他原因" :value="4" />
          </el-select>
        </el-form-item>
        <el-form-item label="详细说明" required>
          <el-input
            v-model="cancelForm.remark"
            type="textarea"
            :rows="3"
            placeholder="请输入详细说明"
          />
        </el-form-item>
        <el-form-item label="收取违约金">
          <el-switch v-model="cancelForm.chargePenalty" />
        </el-form-item>
        <el-form-item v-if="cancelForm.chargePenalty" label="违约金金额">
          <el-input-number v-model="cancelForm.penaltyAmount" :min="0" :precision="2" style="width: 100%" />
        </el-form-item>
        <el-form-item label="退款金额">
          <span class="refund-amount">¥{{ calculatedRefundAmount }}</span>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="cancelDialogVisible = false">取消</el-button>
        <el-button type="danger" :loading="cancelSaving" @click="handleCancel">确认取消</el-button>
      </template>
    </el-dialog>

    <el-dialog
      v-model="checkinDialogVisible"
      title="办理入住"
      width="560px"
      destroy-on-close
    >
      <el-descriptions :column="1" border class="checkin-confirm">
        <el-descriptions-item label="预订单号">
          <span class="field-readonly">{{ bookingData?.bookingNo }}</span>
        </el-descriptions-item>
        <el-descriptions-item label="房型">
          <span class="field-readonly">{{ bookingData?.roomTypeName }}</span>
        </el-descriptions-item>
        <el-descriptions-item label="房号">
          <span class="field-readonly">{{ bookingData?.roomNumber }}</span>
        </el-descriptions-item>
        <el-descriptions-item label="入住日期">
          <span class="field-readonly">{{ bookingData?.checkinDate }}</span>
        </el-descriptions-item>
        <el-descriptions-item label="退房日期">
          <span class="field-readonly">{{ bookingData?.checkoutDate }}</span>
        </el-descriptions-item>
        <el-descriptions-item label="入住人数">
          <span class="field-readonly">{{ bookingData?.guestCount }} 人</span>
        </el-descriptions-item>
        <el-descriptions-item label="应付总额">
          <span class="field-readonly total-price">¥{{ bookingData?.totalAmount || 0 }}</span>
        </el-descriptions-item>
        <el-descriptions-item label="已付金额">
          <span class="field-readonly price">¥{{ bookingData?.paidAmount || 0 }}</span>
        </el-descriptions-item>
      </el-descriptions>
      <div class="checkin-tip">
        <el-icon class="tip-icon"><Warning /></el-icon>
        <span>请核对入住人信息无误后再办理入住</span>
      </div>
      <template #footer>
        <el-button @click="checkinDialogVisible = false">取消</el-button>
        <el-button type="success" :loading="checkinSaving" @click="handleCheckin">确认入住</el-button>
      </template>
    </el-dialog>

    <el-dialog
      v-model="refundDialogVisible"
      title="申请退款"
      width="520px"
      destroy-on-close
    >
      <el-form :model="refundForm" label-width="100px" ref="refundFormRef">
        <el-form-item label="退款原因" required>
          <el-select v-model="refundForm.reason" placeholder="请选择退款原因" style="width: 100%">
            <el-option label="客户取消" :value="1" />
            <el-option label="重复支付" :value="2" />
            <el-option label="金额错误" :value="3" />
            <el-option label="其他原因" :value="4" />
          </el-select>
        </el-form-item>
        <el-form-item label="退款金额" required>
          <el-input-number v-model="refundForm.amount" :min="0" :max="bookingData?.paidAmount || 0" :precision="2" style="width: 100%" />
        </el-form-item>
        <el-form-item label="退款说明" required>
          <el-input
            v-model="refundForm.remark"
            type="textarea"
            :rows="3"
            placeholder="请输入退款说明"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="refundDialogVisible = false">取消</el-button>
        <el-button type="warning" :loading="refundSaving" @click="handleRefund">提交申请</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  ArrowLeft,
  Check,
  Edit,
  Close,
  Key,
  Money,
  Switch,
  Document,
  Plus,
  Delete,
  Search,
  Warning
} from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import api from '@/api'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const hasPermission = (p) => userStore.hasPermission(p)

const statusLabel = (status) => {
  const map = { 1: '待确认', 2: '已确认', 3: '已支付', 4: '已取消', 5: '已入住', 6: '已完成' }
  return map[status] || '未知'
}

const statusTagType = (status) => {
  const map = { 1: 'warning', 2: 'primary', 3: 'success', 4: 'info', 5: 'success', 6: '' }
  return map[status] || 'info'
}

const sourceLabel = (source) => {
  const map = { 1: '前台', 2: '官网', 3: '携程', 4: '美团', 5: '飞猪', 6: '其他' }
  return map[source] || '未知'
}

const sourceTagType = (source) => {
  const map = { 1: 'primary', 2: 'success', 3: 'warning', 4: 'danger', 5: 'info', 6: '' }
  return map[source] || 'info'
}

const guaranteeLabel = (type) => {
  const map = { 1: '无担保', 2: '信用卡担保', 3: '支付宝担保', 4: '微信担保', 5: '预付担保' }
  return map[type] || '无担保'
}

const idTypeLabel = (type) => {
  const map = { 1: '身份证', 2: '护照', 3: '港澳通行证', 4: '台胞证', 5: '军官证' }
  return map[type] || '未知'
}

const logTypeLabel = (type) => {
  const map = { status: '状态变更', change: '信息变更', cancel: '取消记录', create: '创建订单' }
  return map[type] || '操作'
}

const logTimelineType = (type) => {
  const map = { status: 'primary', change: 'warning', cancel: 'danger', create: 'success' }
  return map[type] || ''
}

const roomStatusLabel = (status) => {
  const map = { 0: '空闲', 1: '已预订', 2: '已入住', 3: '维修中', 4: '清洁中' }
  return map[status] || '未知'
}

const roomStatusTagType = (status) => {
  const map = { 0: 'success', 1: 'warning', 2: 'primary', 3: 'danger', 4: 'info' }
  return map[status] || 'info'
}

const bookingData = ref(null)
const operationLogs = ref([])
const pageLoading = ref(false)

const priceDetails = computed(() => {
  return bookingData.value?.priceDetails || bookingData.value?.dailyPrices || []
})

const loadDetail = async () => {
  pageLoading.value = true
  try {
    const id = route.params.id
    const res = await api.booking.get(id)
    if (res.code === 200 && res.data) {
      bookingData.value = res.data
      operationLogs.value = res.data.operationLogs || res.data.logs || []
    } else {
      ElMessage.error(res.message || '获取预订单详情失败')
    }
  } catch {
    ElMessage.error('获取预订单详情失败')
  } finally {
    pageLoading.value = false
  }
}

const handleConfirm = async () => {
  try {
    await ElMessageBox.confirm('确认该预订？', '提示', {
      confirmButtonText: '确认',
      cancelButtonText: '取消',
      type: 'info'
    })
    const res = await api.booking.confirm(route.params.id)
    if (res.code === 200) {
      ElMessage.success('确认成功')
      await loadDetail()
    } else {
      ElMessage.error(res.message || '确认失败')
    }
  } catch {}
}

const editDialogVisible = ref(false)
const editSaving = ref(false)
const editFormRef = ref(null)
const editForm = reactive({
  checkinDate: '',
  checkoutDate: '',
  guestCount: 1,
  guests: [],
  extraBedCount: 0,
  expectedArrivalTime: '',
  specialRequest: ''
})

const openEditDialog = () => {
  editForm.checkinDate = bookingData.value?.checkinDate || ''
  editForm.checkoutDate = bookingData.value?.checkoutDate || ''
  editForm.guestCount = bookingData.value?.guestCount || 1
  editForm.guests = (bookingData.value?.guests || []).map(g => ({ ...g }))
  editForm.extraBedCount = bookingData.value?.extraBedCount || 0
  editForm.expectedArrivalTime = bookingData.value?.expectedArrivalTime || ''
  editForm.specialRequest = bookingData.value?.specialRequest || ''
  editDialogVisible.value = true
}

const addGuest = () => {
  editForm.guests.push({ name: '', idNo: '' })
}

const removeGuest = (idx) => {
  editForm.guests.splice(idx, 1)
}

const handleEdit = async () => {
  editSaving.value = true
  try {
    const res = await api.booking.update(route.params.id, {
      checkinDate: editForm.checkinDate,
      checkoutDate: editForm.checkoutDate,
      guestCount: editForm.guestCount,
      guests: editForm.guests,
      extraBedCount: editForm.extraBedCount,
      expectedArrivalTime: editForm.expectedArrivalTime,
      specialRequest: editForm.specialRequest
    })
    if (res.code === 200) {
      ElMessage.success('修改成功')
      editDialogVisible.value = false
      await loadDetail()
    } else {
      ElMessage.error(res.message || '修改失败')
    }
  } catch {
    ElMessage.error('修改失败')
  } finally {
    editSaving.value = false
  }
}

const changeRoomDialogVisible = ref(false)
const changeRoomSaving = ref(false)
const changeRoomForm = reactive({})
const availableRooms = ref([])
const selectedRoom = ref(null)

const totalPriceDiff = computed(() => {
  if (!selectedRoom.value || !bookingData.value) return 0
  const priceDiff = (selectedRoom.value.price || 0) - (bookingData.value.roomPrice || 0)
  return priceDiff * (bookingData.value.days || 0)
})

const openChangeRoomDialog = () => {
  availableRooms.value = []
  selectedRoom.value = null
  changeRoomDialogVisible.value = true
}

const queryAvailableRooms = async () => {
  try {
    const res = await api.booking.roomQuery({
      checkinDate: bookingData.value?.checkinDate,
      checkoutDate: bookingData.value?.checkoutDate,
      roomTypeId: bookingData.value?.roomTypeId
    })
    if (res.code === 200) {
      const rooms = res.data?.rows || res.data || []
      availableRooms.value = rooms.map(room => ({
        ...room,
        priceDiff: (room.price || 0) - (bookingData.value?.roomPrice || 0)
      }))
    } else {
      ElMessage.error(res.message || '查询失败')
    }
  } catch {
    ElMessage.error('查询失败')
  }
}

const handleRoomSelection = (selection) => {
  selectedRoom.value = selection[0] || null
}

const handleChangeRoom = async () => {
  if (!selectedRoom.value) {
    ElMessage.warning('请选择要更换的房间')
    return
  }
  changeRoomSaving.value = true
  try {
    const res = await api.booking.changeRoom(route.params.id, {
      newRoomId: selectedRoom.value.id,
      newRoomNumber: selectedRoom.value.roomNumber,
      priceDiff: totalPriceDiff.value
    })
    if (res.code === 200) {
      ElMessage.success('换房成功')
      changeRoomDialogVisible.value = false
      await loadDetail()
    } else {
      ElMessage.error(res.message || '换房失败')
    }
  } catch {
    ElMessage.error('换房失败')
  } finally {
    changeRoomSaving.value = false
  }
}

const cancelDialogVisible = ref(false)
const cancelSaving = ref(false)
const cancelFormRef = ref(null)
const cancelForm = reactive({
  reason: null,
  remark: '',
  chargePenalty: false,
  penaltyAmount: 0
})

const calculatedRefundAmount = computed(() => {
  const paid = bookingData.value?.paidAmount || 0
  const penalty = cancelForm.chargePenalty ? (cancelForm.penaltyAmount || 0) : 0
  return Math.max(0, paid - penalty)
})

const openCancelDialog = () => {
  cancelForm.reason = null
  cancelForm.remark = ''
  cancelForm.chargePenalty = false
  cancelForm.penaltyAmount = 0
  cancelDialogVisible.value = true
}

const handleCancel = async () => {
  if (!cancelForm.reason) {
    ElMessage.warning('请选择取消原因')
    return
  }
  if (!cancelForm.remark?.trim()) {
    ElMessage.warning('请输入详细说明')
    return
  }
  cancelSaving.value = true
  try {
    const res = await api.booking.cancel(route.params.id, {
      reason: cancelForm.reason,
      remark: cancelForm.remark,
      chargePenalty: cancelForm.chargePenalty,
      penaltyAmount: cancelForm.chargePenalty ? cancelForm.penaltyAmount : 0,
      refundAmount: calculatedRefundAmount.value
    })
    if (res.code === 200) {
      ElMessage.success('取消成功')
      cancelDialogVisible.value = false
      await loadDetail()
    } else {
      ElMessage.error(res.message || '取消失败')
    }
  } catch {
    ElMessage.error('取消失败')
  } finally {
    cancelSaving.value = false
  }
}

const checkinDialogVisible = ref(false)
const checkinSaving = ref(false)

const openCheckinDialog = () => {
  checkinDialogVisible.value = true
}

const handleCheckin = async () => {
  checkinSaving.value = true
  try {
    const res = await api.booking.checkin(route.params.id, {})
    if (res.code === 200) {
      ElMessage.success('办理入住成功')
      checkinDialogVisible.value = false
      await loadDetail()
    } else {
      ElMessage.error(res.message || '办理入住失败')
    }
  } catch {
    ElMessage.error('办理入住失败')
  } finally {
    checkinSaving.value = false
  }
}

const viewCheckinRecord = () => {
  if (bookingData.value?.checkinId) {
    router.push(`/checkin/detail/${bookingData.value.checkinId}`)
  } else {
    ElMessage.info('暂无入住记录')
  }
}

const refundDialogVisible = ref(false)
const refundSaving = ref(false)
const refundFormRef = ref(null)
const refundForm = reactive({
  reason: null,
  amount: 0,
  remark: ''
})

const openRefundDialog = () => {
  refundForm.reason = null
  refundForm.amount = bookingData.value?.paidAmount || 0
  refundForm.remark = ''
  refundDialogVisible.value = true
}

const handleRefund = async () => {
  if (!refundForm.reason) {
    ElMessage.warning('请选择退款原因')
    return
  }
  if (!refundForm.amount || refundForm.amount <= 0) {
    ElMessage.warning('请输入有效的退款金额')
    return
  }
  if (!refundForm.remark?.trim()) {
    ElMessage.warning('请输入退款说明')
    return
  }
  refundSaving.value = true
  try {
    const res = await api.booking.applyRefund(route.params.id, {
      reason: refundForm.reason,
      amount: refundForm.amount,
      remark: refundForm.remark
    })
    if (res.code === 200) {
      ElMessage.success('退款申请提交成功')
      refundDialogVisible.value = false
      await loadDetail()
    } else {
      ElMessage.error(res.message || '提交失败')
    }
  } catch {
    ElMessage.error('提交失败')
  } finally {
    refundSaving.value = false
  }
}

onMounted(() => {
  loadDetail()
})
</script>

<style scoped>
.booking-detail-container {
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

.order-number {
  font-size: 24px;
  font-weight: 700;
  color: #1a202c;
}

.header-actions {
  display: flex;
  gap: 10px;
}

.info-card {
  border-radius: 12px;
  border: none;
  margin-bottom: 16px;
}

.card-title {
  font-size: 16px;
  font-weight: 600;
  color: #1a202c;
}

.field-readonly {
  font-weight: 600;
  color: #2d3748;
}

.text-muted {
  color: #c0c4cc;
  font-size: 14px;
}

.price {
  color: #e6a23c;
  font-size: 16px;
}

.total-price {
  color: #f56c6c;
  font-size: 18px;
  font-weight: 700;
}

.discount {
  color: #67c23a;
  font-weight: 600;
}

.remark-text {
  font-size: 14px;
  color: #4a5568;
  line-height: 1.6;
  white-space: pre-wrap;
}

.guest-list {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.guest-tag {
  margin-right: 0;
}

.price-table {
  margin-bottom: 16px;
}

.price-summary {
  margin-top: 16px;
}

.log-content {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

.log-operator {
  font-weight: 600;
  color: #2d3748;
}

.log-type {
  color: #909399;
  font-size: 13px;
}

.log-arrow {
  color: #a0aec0;
  font-size: 16px;
}

.log-field {
  color: #606266;
  font-weight: 500;
}

.log-old-value {
  color: #909399;
  text-decoration: line-through;
}

.log-new-value {
  color: #409eff;
  font-weight: 500;
}

.log-cancel-reason {
  color: #f56c6c;
}

.log-penalty {
  color: #e6a23c;
}

.log-remark {
  color: #718096;
  font-size: 13px;
}

.guests-edit-container {
  width: 100%;
}

.guest-edit-item {
  display: flex;
  gap: 8px;
  margin-bottom: 8px;
  align-items: center;
}

.room-list {
  margin-top: 16px;
}

.price-diff-info {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-top: 16px;
  padding: 12px;
  background: #f5f7fa;
  border-radius: 8px;
  font-size: 14px;
}

.price-diff-info .arrow {
  color: #909399;
}

.total-diff {
  margin-left: auto;
  font-weight: 600;
  color: #409eff;
}

.price-up {
  color: #f56c6c;
  font-weight: 600;
}

.price-down {
  color: #67c23a;
  font-weight: 600;
}

.refund-amount {
  font-size: 18px;
  font-weight: 700;
  color: #67c23a;
}

.checkin-confirm {
  margin-bottom: 16px;
}

.checkin-tip {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px;
  background: #fdf6ec;
  border-radius: 8px;
  color: #e6a23c;
  font-size: 14px;
}

.tip-icon {
  font-size: 18px;
}
</style>
