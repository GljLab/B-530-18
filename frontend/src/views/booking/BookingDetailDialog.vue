<template>
  <el-dialog
    v-model="visible"
    :title="dialogTitle"
    width="900px"
    destroy-on-close
    class="booking-detail-dialog"
    @close="handleClose"
  >
    <div v-loading="loading" class="detail-content">
      <div v-if="bookingData" class="detail-header">
        <div class="header-left">
          <span class="booking-no">{{ bookingData.bookingNo || '' }}</span>
          <el-tag :type="statusTagType(bookingData.status)" size="large" effect="dark">
            {{ statusLabel(bookingData.status) }}
          </el-tag>
        </div>
        <div class="header-right">
          <span class="create-time">创建时间：{{ bookingData.createTime || '-' }}</span>
          <span class="creator">创建人：{{ bookingData.createUserName || '-' }}</span>
        </div>
      </div>

      <el-tabs v-model="activeTab" v-if="bookingData" class="detail-tabs">
        <el-tab-pane label="基本信息" name="basic">
          <el-descriptions :column="2" border size="default">
            <el-descriptions-item label="预订单号">
              <span class="field-value">{{ bookingData.bookingNo || '-' }}</span>
            </el-descriptions-item>
            <el-descriptions-item label="预订状态">
              <el-tag :type="statusTagType(bookingData.status)" size="small">
                {{ statusLabel(bookingData.status) }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="创建时间">
              <span class="field-value">{{ bookingData.createTime || '-' }}</span>
            </el-descriptions-item>
            <el-descriptions-item label="创建人">
              <span class="field-value">{{ bookingData.createUserName || '-' }}</span>
            </el-descriptions-item>
          </el-descriptions>

          <el-divider content-position="left">客户信息</el-divider>
          <el-descriptions :column="2" border size="default">
            <el-descriptions-item label="客户姓名">
              <span class="field-value">{{ bookingData.customerName || '-' }}</span>
            </el-descriptions-item>
            <el-descriptions-item label="联系电话">
              <span class="field-value">{{ bookingData.customerPhone || '-' }}</span>
            </el-descriptions-item>
            <el-descriptions-item label="证件类型">
              <span class="field-value">{{ idTypeLabel(bookingData.customerIdType) }}</span>
            </el-descriptions-item>
            <el-descriptions-item label="证件号码">
              <span class="field-value">{{ bookingData.customerIdNo || '-' }}</span>
            </el-descriptions-item>
          </el-descriptions>

          <el-divider content-position="left">房间信息</el-divider>
          <el-descriptions :column="2" border size="default">
            <el-descriptions-item label="房型">
              <span class="field-value">{{ bookingData.roomTypeName || '-' }}</span>
            </el-descriptions-item>
            <el-descriptions-item label="房号">
              <span class="field-value">{{ bookingData.roomNumber || '-' }}</span>
            </el-descriptions-item>
            <el-descriptions-item label="楼栋">
              <span class="field-value">{{ bookingData.buildingName || '-' }}</span>
            </el-descriptions-item>
            <el-descriptions-item label="楼层">
              <span class="field-value">{{ bookingData.floorName || '-' }}</span>
            </el-descriptions-item>
          </el-descriptions>

          <el-divider content-position="left">入住信息</el-divider>
          <el-descriptions :column="2" border size="default">
            <el-descriptions-item label="入住日期">
              <span class="field-value">{{ bookingData.checkInDate || bookingData.checkinDate || '-' }}</span>
            </el-descriptions-item>
            <el-descriptions-item label="退房日期">
              <span class="field-value">{{ bookingData.checkOutDate || bookingData.checkoutDate || '-' }}</span>
            </el-descriptions-item>
            <el-descriptions-item label="入住天数">
              <span class="field-value">{{ bookingData.days || 0 }} 晚</span>
            </el-descriptions-item>
            <el-descriptions-item label="入住人数">
              <span class="field-value">{{ bookingData.guestCount || 0 }} 人</span>
            </el-descriptions-item>
            <el-descriptions-item label="入住人姓名">
              <span class="field-value">{{ bookingData.guestNames || '-' }}</span>
            </el-descriptions-item>
            <el-descriptions-item label="联系电话">
              <span class="field-value">{{ bookingData.guestPhone || '-' }}</span>
            </el-descriptions-item>
          </el-descriptions>

          <el-divider content-position="left">费用信息</el-divider>
          <el-descriptions :column="2" border size="default">
            <el-descriptions-item label="房费">
              <span class="field-value price">¥{{ formatNumber(bookingData.roomTotal) }}</span>
            </el-descriptions-item>
            <el-descriptions-item label="加床费">
              <span class="field-value price">¥{{ formatNumber(bookingData.extraBedTotal) }}</span>
            </el-descriptions-item>
            <el-descriptions-item label="其他费用">
              <span class="field-value price">¥{{ formatNumber(bookingData.otherFee) }}</span>
            </el-descriptions-item>
            <el-descriptions-item label="优惠">
              <span class="field-value discount">-¥{{ formatNumber(bookingData.discount) }}</span>
            </el-descriptions-item>
            <el-descriptions-item label="应付总额">
              <span class="field-value total-price">¥{{ formatNumber(bookingData.totalAmount) }}</span>
            </el-descriptions-item>
            <el-descriptions-item label="已付金额">
              <span class="field-value price">¥{{ formatNumber(bookingData.paidAmount) }}</span>
            </el-descriptions-item>
          </el-descriptions>

          <el-divider content-position="left">其他信息</el-divider>
          <el-descriptions :column="2" border size="default">
            <el-descriptions-item label="担保方式">
              <span class="field-value">{{ guaranteeLabel(bookingData.guaranteeType) }}</span>
            </el-descriptions-item>
            <el-descriptions-item label="预订来源">
              <el-tag :type="sourceTagType(bookingData.bookingSource || bookingData.source)" size="small">
                {{ sourceLabel(bookingData.bookingSource || bookingData.source) }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="特殊要求" :span="2">
              <span class="field-value">{{ bookingData.specialRequirements || bookingData.specialRequest || '-' }}</span>
            </el-descriptions-item>
            <el-descriptions-item label="预计到店时间" :span="2">
              <span class="field-value">{{ bookingData.expectedArrivalTime || '-' }}</span>
            </el-descriptions-item>
          </el-descriptions>
        </el-tab-pane>

        <el-tab-pane label="价格明细" name="price">
          <el-table :data="priceDetails" border stripe style="width: 100%">
            <el-table-column prop="stayDate" label="日期" width="140" align="center">
              <template #default="{ row }">
                {{ row.stayDate || row.date || '-' }}
              </template>
            </el-table-column>
            <el-table-column label="星期" width="80" align="center">
              <template #default="{ row }">
                {{ getWeekday(row.stayDate || row.date) }}
              </template>
            </el-table-column>
            <el-table-column prop="dayType" label="日期类型" width="100" align="center">
              <template #default="{ row }">
                <el-tag v-if="row.dayType === '周末'" type="warning" size="small">周末</el-tag>
                <el-tag v-else-if="row.dayType === '节假日'" type="danger" size="small">节假日</el-tag>
                <el-tag v-else type="primary" size="small">平日</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="price" label="售价(元)" width="120" align="right">
              <template #default="{ row }">
                ¥{{ formatNumber(row.price) }}
              </template>
            </el-table-column>
            <el-table-column v-if="canSeeCost" prop="costPrice" label="成本(元)" width="120" align="right">
              <template #default="{ row }">
                <span class="cost-price">¥{{ formatNumber(row.costPrice) }}</span>
              </template>
            </el-table-column>
          </el-table>

          <el-descriptions :column="canSeeCost ? 5 : 4" border size="default" class="price-summary">
            <el-descriptions-item label="房费小计">
              <span class="summary-value">¥{{ calculateRoomTotal() }}</span>
            </el-descriptions-item>
            <el-descriptions-item label="加床费">
              <span class="summary-value">¥{{ formatNumber(bookingData.extraBedTotal) }}</span>
            </el-descriptions-item>
            <el-descriptions-item label="其他费用">
              <span class="summary-value">¥{{ formatNumber(bookingData.otherFee) }}</span>
            </el-descriptions-item>
            <el-descriptions-item label="优惠">
              <span class="summary-value discount">-¥{{ formatNumber(bookingData.discount) }}</span>
            </el-descriptions-item>
            <el-descriptions-item v-if="canSeeCost" label="总成本">
              <span class="summary-value cost-price">¥{{ calculateCostTotal() }}</span>
            </el-descriptions-item>
            <el-descriptions-item label="总计" :span="canSeeCost ? 5 : 4">
              <span class="summary-total">¥{{ formatNumber(bookingData.totalAmount) }}</span>
            </el-descriptions-item>
          </el-descriptions>
        </el-tab-pane>

        <el-tab-pane label="状态流转日志" name="status">
          <el-timeline v-if="statusLogs.length > 0">
            <el-timeline-item
              v-for="(log, index) in statusLogs"
              :key="log.id || index"
              :timestamp="log.createTime"
              placement="top"
              :type="getStatusTimelineType(log.newStatus)"
            >
              <div class="log-item">
                <div class="log-header">
                  <span class="log-operator">{{ log.operatorName || '系统' }}</span>
                  <span class="log-time">{{ log.createTime }}</span>
                </div>
                <div class="log-content">
                  <span v-if="log.oldStatus" class="status-old">
                    <el-tag :type="statusTagType(log.oldStatus)" size="small">
                      {{ statusLabel(log.oldStatus) }}
                    </el-tag>
                  </span>
                  <span v-if="log.oldStatus" class="log-arrow">→</span>
                  <span class="status-new">
                    <el-tag :type="statusTagType(log.newStatus)" size="small" effect="dark">
                      {{ statusLabel(log.newStatus) }}
                    </el-tag>
                  </span>
                </div>
                <div v-if="log.remark" class="log-remark">
                  <el-icon><ChatDotRound /></el-icon>
                  <span>{{ log.remark }}</span>
                </div>
              </div>
            </el-timeline-item>
          </el-timeline>
          <el-empty v-else description="暂无状态流转记录" :image-size="80" />
        </el-tab-pane>

        <el-tab-pane label="变更日志" name="change">
          <el-table v-if="changeLogs.length > 0" :data="changeLogs" border stripe style="width: 100%">
            <el-table-column prop="createTime" label="变更时间" width="170" align="center" />
            <el-table-column prop="changeType" label="变更类型" width="100" align="center">
              <template #default="{ row }">
                <el-tag :type="getChangeTagType(row.changeType)" size="small">
                  {{ row.changeType || '修改' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="fieldName" label="变更字段" width="140" align="center" />
            <el-table-column prop="oldValue" label="原值">
              <template #default="{ row }">
                <span class="old-value">{{ row.oldValue || '-' }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="newValue" label="新值">
              <template #default="{ row }">
                <span class="new-value">{{ row.newValue || '-' }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="operatorName" label="操作人" width="100" align="center" />
            <el-table-column prop="remark" label="备注" min-width="150">
              <template #default="{ row }">
                <span>{{ row.remark || '-' }}</span>
              </template>
            </el-table-column>
          </el-table>
          <el-empty v-else description="暂无变更记录" :image-size="80" />
        </el-tab-pane>
      </el-tabs>

      <el-empty v-if="!bookingData && !loading" description="暂无数据" :image-size="80" />
    </div>

    <template #footer>
      <el-button @click="handleClose">关闭</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { ChatDotRound } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import api from '@/api'

const userStore = useUserStore()
const canSeeCost = computed(() => userStore.canSeeCost)

const props = defineProps({
  modelValue: {
    type: Boolean,
    default: false
  },
  bookingId: {
    type: [Number, String],
    default: null
  },
  defaultTab: {
    type: String,
    default: 'basic'
  }
})

const emit = defineEmits(['update:modelValue', 'closed'])

const visible = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val)
})

const loading = ref(false)
const activeTab = ref(props.defaultTab)
const bookingData = ref(null)
const priceDetails = ref([])
const statusLogs = ref([])
const changeLogs = ref([])

const dialogTitle = computed(() => {
  if (bookingData.value?.bookingNo) {
    return `预订详情 - ${bookingData.value.bookingNo}`
  }
  return '预订详情'
})

const statusLabel = (status) => {
  const map = { 1: '待确认', 2: '已确认', 3: '已支付', 4: '已取消', 5: '已入住', 6: '已完成', 9: '已退房' }
  return map[status] || '未知'
}

const statusTagType = (status) => {
  const map = { 1: 'warning', 2: 'primary', 3: 'success', 4: 'info', 5: 'success', 6: '', 9: 'info' }
  return map[status] || 'info'
}

const sourceLabel = (source) => {
  const map = { '1': '前台', '2': '官网', '3': '第三方平台', '4': '协议单位', '5': '旅行社', '6': '其他' }
  return map[source] || source || '未知'
}

const sourceTagType = (source) => {
  const map = { '1': 'primary', '2': 'success', '3': 'warning', '4': 'danger', '5': 'info', '6': '' }
  return map[source] || 'info'
}

const guaranteeLabel = (type) => {
  const map = { '1': '到店支付', '2': '信用卡担保', '3': '预付全款' }
  return map[type] || type || '未选择'
}

const idTypeLabel = (type) => {
  const map = { '1': '身份证', '2': '护照', '3': '港澳通行证', '4': '台胞证', '5': '其他' }
  return map[type] || '未设置'
}

const formatNumber = (num) => {
  if (num === null || num === undefined || num === '') return '0.00'
  return Number(num).toFixed(2)
}

const getWeekday = (dateStr) => {
  if (!dateStr) return '-'
  const weekdays = ['周日', '周一', '周二', '周三', '周四', '周五', '周六']
  const date = new Date(dateStr)
  if (isNaN(date.getTime())) return '-'
  return weekdays[date.getDay()]
}

const getStatusTimelineType = (status) => {
  const map = { 1: 'warning', 2: 'primary', 3: 'success', 4: 'info', 5: 'success', 6: '', 9: 'info' }
  return map[status] || ''
}

const getChangeTagType = (type) => {
  const map = { '修改': 'warning', '换房': 'primary', '取消': 'danger' }
  return map[type] || 'info'
}

const calculateRoomTotal = () => {
  if (!priceDetails.value || priceDetails.value.length === 0) return '0.00'
  const total = priceDetails.value.reduce((sum, item) => {
    return sum + (Number(item.price) || 0)
  }, 0)
  return total.toFixed(2)
}

const calculateCostTotal = () => {
  if (!priceDetails.value || priceDetails.value.length === 0) return '0.00'
  const total = priceDetails.value.reduce((sum, item) => {
    return sum + (Number(item.costPrice) || 0)
  }, 0)
  return total.toFixed(2)
}

const loadDetail = async () => {
  if (!props.bookingId) return
  loading.value = true
  try {
    const res = await api.booking.get(props.bookingId)
    if (res.code === 200 && res.data) {
      const data = res.data
      bookingData.value = data.booking || data
      priceDetails.value = data.details || data.bookingDetails || data.dailyPrices || []
      statusLogs.value = data.statusLogs || data.status_logs || []
      changeLogs.value = data.changeLogs || data.change_logs || []
    } else {
      ElMessage.error(res.message || '获取预订详情失败')
    }
  } catch {
    ElMessage.error('获取预订详情失败')
  } finally {
    loading.value = false
  }
}

const handleClose = () => {
  visible.value = false
  activeTab.value = 'basic'
  bookingData.value = null
  priceDetails.value = []
  statusLogs.value = []
  changeLogs.value = []
  emit('closed')
}

watch(() => props.bookingId, (newId) => {
  if (newId && visible.value) {
    loadDetail()
  }
})

watch(visible, (val) => {
  if (val) {
    activeTab.value = props.defaultTab
    if (props.bookingId) {
      loadDetail()
    }
  }
})
</script>

<style scoped>
.booking-detail-dialog :deep(.el-dialog__body) {
  padding: 16px 20px;
  max-height: 70vh;
  overflow-y: auto;
}

.detail-content {
  min-height: 400px;
}

.detail-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 8px;
  margin-bottom: 20px;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.booking-no {
  font-size: 20px;
  font-weight: 700;
  color: #fff;
}

.header-right {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 4px;
  color: rgba(255, 255, 255, 0.9);
  font-size: 13px;
}

.detail-tabs {
  margin-top: 16px;
}

.detail-tabs :deep(.el-tabs__content) {
  padding: 16px 8px;
}

.field-value {
  color: #303133;
  font-weight: 500;
}

.price {
  color: #e6a23c;
  font-weight: 600;
}

.total-price {
  color: #f56c6c;
  font-weight: 700;
  font-size: 16px;
}

.discount {
  color: #67c23a;
  font-weight: 600;
}

.price-summary {
  margin-top: 16px;
}

.summary-value {
  font-weight: 600;
  color: #303133;
}

.summary-value.discount {
  color: #67c23a;
}

.summary-total {
  font-size: 18px;
  font-weight: 700;
  color: #f56c6c;
}

.cost-price {
  color: #909399;
  font-weight: 500;
}

.summary-value.cost-price {
  color: #909399;
}

.log-item {
  background: #f8f9fa;
  padding: 12px 16px;
  border-radius: 8px;
}

.log-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.log-operator {
  font-weight: 600;
  color: #303133;
}

.log-time {
  font-size: 12px;
  color: #909399;
}

.log-content {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

.log-arrow {
  color: #909399;
  font-size: 16px;
}

.log-remark {
  margin-top: 8px;
  padding-top: 8px;
  border-top: 1px dashed #e4e7ed;
  color: #606266;
  font-size: 13px;
  display: flex;
  align-items: center;
  gap: 4px;
}

.old-value {
  color: #909399;
  text-decoration: line-through;
}

.new-value {
  color: #409eff;
  font-weight: 500;
}

:deep(.el-descriptions__label) {
  width: 110px;
  font-weight: 500;
}

:deep(.el-divider__text) {
  font-weight: 600;
  color: #303133;
}
</style>
