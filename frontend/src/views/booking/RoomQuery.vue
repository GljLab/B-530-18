<template>
  <div class="room-query-container">
    <el-card shadow="never" class="query-card">
      <el-form ref="queryFormRef" :model="queryParams" :rules="queryRules" label-width="90px" size="default">
        <el-row :gutter="16">
          <el-col :span="8">
            <el-form-item label="入住日期" prop="checkInDate">
              <el-date-picker
                v-model="queryParams.checkInDate"
                type="date"
                placeholder="请选择入住日期"
                :disabled-date="disabledCheckInDate"
                style="width: 100%"
                @change="handleCheckInDateChange"
              />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="退房日期" prop="checkOutDate">
              <el-date-picker
                v-model="queryParams.checkOutDate"
                type="date"
                placeholder="请选择退房日期"
                :disabled-date="disabledCheckOutDate"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="房型">
              <el-select v-model="queryParams.roomTypeId" placeholder="请选择房型" clearable style="width: 100%">
                <el-option v-for="rt in roomTypes" :key="rt.id" :label="rt.typeName" :value="rt.id" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="8">
            <el-form-item label="楼栋">
              <el-select v-model="queryParams.buildingId" placeholder="请选择楼栋" clearable style="width: 100%" @change="handleBuildingChange">
                <el-option v-for="b in buildings" :key="b.id" :label="b.buildingName" :value="b.id" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="楼层">
              <el-select v-model="queryParams.floorId" placeholder="请选择楼层" clearable style="width: 100%">
                <el-option v-for="f in filteredFloors" :key="f.id" :label="f.floorName" :value="f.id" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="朝向">
              <el-select v-model="queryParams.orientation" placeholder="请选择朝向" clearable style="width: 100%">
                <el-option v-for="o in orientationOptions" :key="o" :label="o" :value="o" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="8">
            <el-form-item label="景观">
              <el-select v-model="queryParams.viewType" placeholder="请选择景观" clearable style="width: 100%">
                <el-option v-for="v in viewTypeOptions" :key="v" :label="v" :value="v" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="16">
            <el-form-item>
              <el-button type="primary" @click="handleQuery">
                <el-icon><Search /></el-icon>查询
              </el-button>
              <el-button @click="handleReset">
                <el-icon><Refresh /></el-icon>重置
              </el-button>
              <el-tag type="info" v-if="stayDays > 0" class="stay-days-tag">
                共 {{ stayDays }} 晚
              </el-tag>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
    </el-card>

    <div class="main-content">
      <div class="result-area">
        <div v-if="resultLoading" class="loading-container">
          <el-icon class="is-loading" :size="40"><Loading /></el-icon>
          <p class="loading-text">正在查询可用房源...</p>
        </div>
        <template v-else>
          <div v-if="groupedResults.length === 0" class="empty-container">
            <el-empty description="暂无符合条件的房源，请调整查询条件">
              <el-button type="primary" @click="handleReset">重置条件</el-button>
            </el-empty>
          </div>
          <template v-else>
            <div class="result-header">
              <span class="result-count">
                共找到 <strong>{{ totalAvailableRooms }}</strong> 间可用房间，<strong>{{ groupedResults.length }}</strong> 种房型
              </span>
            </div>
            <div class="room-type-list">
              <el-card
                v-for="(group, index) in paginatedGroups"
                :key="group.roomTypeId"
                shadow="hover"
                class="room-type-card"
              >
                <div class="room-type-header">
                  <div class="room-type-image">
                    <img v-if="group.mainImage" :src="group.mainImage" :alt="group.typeName" />
                    <el-icon v-else :size="80" class="placeholder-icon"><Picture /></el-icon>
                  </div>
                  <div class="room-type-info">
                    <h3 class="room-type-name">{{ group.typeName }}</h3>
                    <div class="room-type-meta">
                      <el-tag size="small" type="info">{{ group.area }}m²</el-tag>
                      <el-tag size="small">{{ bedTypeLabel(group.bedType) }}</el-tag>
                      <el-tag size="small" type="warning">可住{{ group.maxOccupancy }}人</el-tag>
                    </div>
                    <div class="room-type-facilities" v-if="group.facilities && group.facilities.length">
                      <el-tag
                        v-for="f in group.facilities.slice(0, 5)"
                        :key="f"
                        size="small"
                        type="success"
                        effect="plain"
                      >{{ f }}</el-tag>
                    </div>
                    <div class="room-type-availability">
                      <span class="available-count">
                        <el-icon><House /></el-icon>
                        可用 <strong>{{ group.availableRooms.length }}</strong> 间
                      </span>
                      <span class="price-info">
                        <span class="price-label">平日价</span>
                        <span class="price">¥{{ group.weekdayPrice }}</span>
                        <span class="price-unit">/晚</span>
                        <span class="price-label weekend">周末价</span>
                        <span class="price weekend">¥{{ group.weekendPrice }}</span>
                        <span class="price-unit">/晚</span>
                      </span>
                    </div>
                  </div>
                  <div class="room-type-actions">
                    <el-button
                      type="primary"
                      @click="toggleExpand(group.roomTypeId)"
                    >
                      <el-icon><CaretBottom v-if="!expandedIds.includes(group.roomTypeId)" /><CaretTop v-else /></el-icon>
                      {{ expandedIds.includes(group.roomTypeId) ? '收起' : '展开' }}房间
                    </el-button>
                  </div>
                </div>
                <div v-show="expandedIds.includes(group.roomTypeId)" class="room-list">
                  <el-table
                    :data="group.availableRooms"
                    border
                    size="small"
                    style="width: 100%"
                  >
                    <el-table-column prop="roomNumber" label="房号" width="100" align="center" />
                    <el-table-column prop="buildingName" label="楼栋" width="120" align="center" />
                    <el-table-column prop="floorName" label="楼层" width="100" align="center" />
                    <el-table-column prop="orientation" label="朝向" width="90" align="center">
                      <template #default="{ row }">{{ row.orientation || '-' }}</template>
                    </el-table-column>
                    <el-table-column prop="viewType" label="景观" width="90" align="center">
                      <template #default="{ row }">{{ row.viewType || '-' }}</template>
                    </el-table-column>
                    <el-table-column label="当日价格" width="120" align="center">
                      <template #default="{ row }">
                        <span class="room-price">¥{{ row.currentPrice || group.weekdayPrice }}</span>
                      </template>
                    </el-table-column>
                    <el-table-column label="操作" width="120" align="center" fixed="right">
                      <template #default="{ row }">
                        <el-button
                          type="success"
                          size="small"
                          :disabled="isRoomSelected(row.id)"
                          @click="addRoomToBooking(row, group)"
                        >
                          <el-icon><Plus /></el-icon>
                          {{ isRoomSelected(row.id) ? '已选择' : '选择' }}
                        </el-button>
                      </template>
                    </el-table-column>
                  </el-table>
                </div>
              </el-card>
            </div>
            <div class="pagination-wrap" v-if="totalGroups > pageSize">
              <el-pagination
                v-model:current-page="currentPage"
                v-model:page-size="pageSize"
                :page-sizes="[5, 10, 20]"
                :total="totalGroups"
                layout="total, sizes, prev, pager, next, jumper"
                @size-change="currentPage = 1"
              />
            </div>
          </template>
        </template>
      </div>

      <el-drawer
        v-model="bookingDrawerVisible"
        title="预订清单"
        direction="rtl"
        size="420px"
        :destroy-on-close="false"
      >
        <div class="booking-summary">
          <div class="summary-section">
            <h4 class="section-title">
              <el-icon><Calendar /></el-icon>
              入住信息
            </h4>
            <div class="summary-row">
              <span class="label">入住日期</span>
              <span class="value">{{ formatDate(queryParams.checkInDate) }}</span>
            </div>
            <div class="summary-row">
              <span class="label">退房日期</span>
              <span class="value">{{ formatDate(queryParams.checkOutDate) }}</span>
            </div>
            <div class="summary-row">
              <span class="label">入住天数</span>
              <span class="value"><strong>{{ stayDays }}</strong> 晚</span>
            </div>
          </div>

          <div class="summary-section">
            <h4 class="section-title">
              <el-icon><House /></el-icon>
              已选房间
              <el-tag size="small" type="primary" effect="dark">{{ selectedRooms.length }}间</el-tag>
            </h4>
            <div v-if="selectedRooms.length === 0" class="empty-rooms">
              <el-empty description="暂未选择房间" :image-size="80" />
            </div>
            <div v-else class="selected-room-list">
              <div
                v-for="item in selectedRooms"
                :key="item.roomId"
                class="selected-room-item"
              >
                <div class="room-info">
                  <div class="room-header">
                    <span class="room-number">{{ item.roomNumber }}</span>
                    <span class="room-type-name">{{ item.typeName }}</span>
                  </div>
                  <div class="room-detail">
                    <span>{{ item.buildingName }} · {{ item.floorName }}层</span>
                    <span v-if="item.orientation"> · {{ item.orientation }}</span>
                    <span v-if="item.viewType"> · {{ item.viewType }}</span>
                  </div>
                </div>
                <div class="room-price-section">
                  <div class="room-price">¥{{ item.price }}</div>
                  <div class="room-price-total">×{{ stayDays }} = ¥{{ item.price * stayDays }}</div>
                </div>
                <el-button
                  type="danger"
                  text
                  size="small"
                  @click="removeRoomFromBooking(item.roomId)"
                >
                  <el-icon><Delete /></el-icon>
                </el-button>
              </div>
            </div>
          </div>

          <div class="summary-section total-section">
            <div class="total-row">
              <span class="label">房费小计</span>
              <span class="total-price">¥{{ totalPrice }}</span>
            </div>
            <div class="total-row detail">
              <span class="label">{{ selectedRooms.length }}间 × {{ stayDays }}晚</span>
              <span class="value">¥{{ totalPrice }}</span>
            </div>
          </div>
        </div>

        <template #footer>
          <div class="drawer-footer">
            <el-button @click="clearSelectedRooms" :disabled="selectedRooms.length === 0">
              <el-icon><Delete /></el-icon>清空
            </el-button>
            <el-button
              type="primary"
              :disabled="selectedRooms.length === 0"
              @click="goToCreateBooking"
              v-if="hasPermission('booking:create')"
            >
              <el-icon><Right /></el-icon>创建预订
            </el-button>
          </div>
        </template>
      </el-drawer>

      <div
        v-if="!bookingDrawerVisible && selectedRooms.length > 0"
        class="floating-booking-btn"
        @click="bookingDrawerVisible = true"
      >
        <el-icon :size="24"><ShoppingCart /></el-icon>
        <el-badge :value="selectedRooms.length" class="cart-badge" />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  Search, Refresh, House, Picture, CaretBottom, CaretTop,
  Plus, Delete, Calendar, Right, ShoppingCart, Loading
} from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import api from '@/api'

const router = useRouter()
const userStore = useUserStore()
const hasPermission = (p) => userStore.hasPermission(p)

const orientationOptions = ['东', '南', '西', '北', '东南', '东北', '西南', '西北']
const viewTypeOptions = ['江景', '海景', '山景', '园景', '城景']

const today = new Date()
const tomorrow = new Date(today.getTime() + 24 * 60 * 60 * 1000)

const queryFormRef = ref(null)
const queryParams = reactive({
  checkInDate: today,
  checkOutDate: tomorrow,
  roomTypeId: null,
  buildingId: null,
  floorId: null,
  orientation: '',
  viewType: '',
  pageNum: 1,
  pageSize: 50
})

const queryRules = {
  checkInDate: [{ required: true, message: '请选择入住日期', trigger: 'change' }],
  checkOutDate: [{ required: true, message: '请选择退房日期', trigger: 'change' }]
}

const buildings = ref([])
const allFloors = ref([])
const roomTypes = ref([])
const resultLoading = ref(false)
const queryResults = ref([])
const expandedIds = ref([])

const currentPage = ref(1)
const pageSize = ref(5)

const selectedRooms = ref([])
const bookingDrawerVisible = ref(false)

const filteredFloors = computed(() => {
  if (!queryParams.buildingId) return allFloors.value
  return allFloors.value.filter(f => f.buildingId === queryParams.buildingId)
})

const stayDays = computed(() => {
  if (!queryParams.checkInDate || !queryParams.checkOutDate) return 0
  const diff = queryParams.checkOutDate.getTime() - queryParams.checkInDate.getTime()
  return Math.max(0, Math.floor(diff / (1000 * 60 * 60 * 24)))
})

const groupedResults = computed(() => {
  const groups = []
  const map = new Map()
  
  for (const room of queryResults.value) {
    const key = room.roomTypeId
    if (!map.has(key)) {
      const roomType = roomTypes.value.find(rt => rt.id === room.roomTypeId) || {}
      map.set(key, {
        roomTypeId: room.roomTypeId,
        typeName: room.roomTypeName || roomType.typeName || '未知房型',
        area: roomType.area || room.area || 0,
        bedType: roomType.bedType || room.bedType || 'single',
        maxOccupancy: roomType.maxOccupancy || room.maxOccupancy || 2,
        weekdayPrice: roomType.weekdayPrice || room.price || 0,
        weekendPrice: roomType.weekendPrice || room.weekendPrice || roomType.weekdayPrice || 0,
        mainImage: roomType.mainImage || null,
        facilities: roomType.facilities || [],
        availableRooms: []
      })
    }
    const group = map.get(key)
    group.availableRooms.push({
      id: room.id,
      roomNumber: room.roomNumber,
      buildingId: room.buildingId,
      buildingName: room.buildingName,
      floorId: room.floorId,
      floorName: room.floorName,
      orientation: room.orientation,
      viewType: room.viewType,
      currentPrice: room.currentPrice || group.weekdayPrice
    })
  }
  
  groups.push(...map.values())
  groups.sort((a, b) => a.weekdayPrice - b.weekdayPrice)
  return groups
})

const totalGroups = computed(() => groupedResults.value.length)

const paginatedGroups = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  const end = start + pageSize.value
  return groupedResults.value.slice(start, end)
})

const totalAvailableRooms = computed(() => {
  return groupedResults.value.reduce((sum, g) => sum + g.availableRooms.length, 0)
})

const totalPrice = computed(() => {
  return selectedRooms.value.reduce((sum, item) => sum + item.price * stayDays.value, 0)
})

const bedTypeLabel = (type) => {
  const map = { single: '单人床', king: '大床', twin: '双床' }
  return map[type] || type || '-'
}

const disabledCheckInDate = (date) => {
  const today = new Date()
  today.setHours(0, 0, 0, 0)
  return date < today
}

const disabledCheckOutDate = (date) => {
  if (!queryParams.checkInDate) {
    const today = new Date()
    today.setHours(0, 0, 0, 0)
    return date <= today
  }
  return date <= queryParams.checkInDate
}

const formatDate = (date) => {
  if (!date) return '-'
  const d = new Date(date)
  const year = d.getFullYear()
  const month = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
}

const handleCheckInDateChange = () => {
  if (queryParams.checkOutDate && queryParams.checkOutDate <= queryParams.checkInDate) {
    const nextDay = new Date(queryParams.checkInDate.getTime() + 24 * 60 * 60 * 1000)
    queryParams.checkOutDate = nextDay
  }
}

const handleBuildingChange = () => {
  queryParams.floorId = null
}

const loadBuildings = async () => {
  try {
    const res = await api.hotel.getBuildings()
    if (res.code === 200) {
      buildings.value = res.data || []
      const floors = []
      for (const b of buildings.value) {
        if (b.floors && b.floors.length) {
          for (const f of b.floors) {
            floors.push({ ...f, buildingId: b.id })
          }
        }
      }
      allFloors.value = floors
    }
  } catch {
    buildings.value = []
    allFloors.value = []
  }
}

const loadRoomTypes = async () => {
  try {
    const res = await api.hotel.getRoomTypes()
    if (res.code === 200) {
      roomTypes.value = res.data || []
    }
  } catch {
    roomTypes.value = []
  }
}

const handleQuery = async () => {
  const valid = await queryFormRef.value.validate().catch(() => false)
  if (!valid) return
  
  if (stayDays.value <= 0) {
    ElMessage.warning('退房日期必须晚于入住日期')
    return
  }

  resultLoading.value = true
  expandedIds.value = []
  currentPage.value = 1
  
  try {
    const params = {
      checkInDate: formatDate(queryParams.checkInDate),
      checkOutDate: formatDate(queryParams.checkOutDate)
    }
    if (queryParams.roomTypeId) params.roomTypeId = queryParams.roomTypeId
    if (queryParams.buildingId) params.buildingId = queryParams.buildingId
    if (queryParams.floorId) params.floorId = queryParams.floorId
    if (queryParams.orientation) params.orientation = queryParams.orientation
    if (queryParams.viewType) params.viewType = queryParams.viewType
    
    const res = await api.booking.roomQuery(params)
    if (res.code === 200) {
      queryResults.value = res.data?.list || res.data || []
      if (queryResults.value.length === 0) {
        ElMessage.info('暂无符合条件的可用房间')
      } else {
        ElMessage.success(`查询成功，找到 ${queryResults.value.length} 间可用房间`)
      }
    } else {
      ElMessage.error(res.message || '查询失败')
      queryResults.value = []
    }
  } catch {
    ElMessage.error('查询失败')
    queryResults.value = []
  } finally {
    resultLoading.value = false
  }
}

const handleReset = () => {
  queryParams.checkInDate = today
  queryParams.checkOutDate = tomorrow
  queryParams.roomTypeId = null
  queryParams.buildingId = null
  queryParams.floorId = null
  queryParams.orientation = ''
  queryParams.viewType = ''
  queryFormRef.value?.resetFields()
  queryResults.value = []
  expandedIds.value = []
  currentPage.value = 1
}

const toggleExpand = (roomTypeId) => {
  const index = expandedIds.value.indexOf(roomTypeId)
  if (index > -1) {
    expandedIds.value.splice(index, 1)
  } else {
    expandedIds.value.push(roomTypeId)
  }
}

const isRoomSelected = (roomId) => {
  return selectedRooms.value.some(item => item.roomId === roomId)
}

const addRoomToBooking = (room, group) => {
  if (isRoomSelected(room.id)) {
    ElMessage.warning('该房间已在预订清单中')
    return
  }
  
  selectedRooms.value.push({
    roomId: room.id,
    roomNumber: room.roomNumber,
    typeName: group.typeName,
    roomTypeId: group.roomTypeId,
    buildingId: room.buildingId,
    buildingName: room.buildingName,
    floorId: room.floorId,
    floorName: room.floorName,
    orientation: room.orientation,
    viewType: room.viewType,
    price: room.currentPrice || group.weekdayPrice
  })
  
  ElMessage.success(`已添加 ${room.roomNumber} 到预订清单`)
  bookingDrawerVisible.value = true
}

const removeRoomFromBooking = (roomId) => {
  const index = selectedRooms.value.findIndex(item => item.roomId === roomId)
  if (index > -1) {
    selectedRooms.value.splice(index, 1)
    ElMessage.info('已从预订清单移除')
  }
}

const clearSelectedRooms = () => {
  if (selectedRooms.value.length === 0) return
  selectedRooms.value = []
  ElMessage.info('已清空预订清单')
}

const goToCreateBooking = () => {
  if (selectedRooms.value.length === 0) {
    ElMessage.warning('请先选择房间')
    return
  }
  
  const bookingData = {
    checkInDate: formatDate(queryParams.checkInDate),
    checkOutDate: formatDate(queryParams.checkOutDate),
    days: stayDays.value,
    rooms: selectedRooms.value.map(item => ({
      roomId: item.roomId,
      roomNumber: item.roomNumber,
      roomTypeId: item.roomTypeId,
      typeName: item.typeName,
      price: item.price
    })),
    totalPrice: totalPrice.value
  }
  
  sessionStorage.setItem('pendingBooking', JSON.stringify(bookingData))
  router.push('/booking/create')
}

onMounted(async () => {
  await Promise.all([loadBuildings(), loadRoomTypes()])
})
</script>

<style scoped>
.room-query-container {
  padding: 16px;
}

.query-card {
  margin-bottom: 16px;
}

.stay-days-tag {
  margin-left: 12px;
}

.main-content {
  position: relative;
  min-height: 400px;
}

.result-area {
  margin-right: 0;
  transition: margin-right 0.3s ease;
}

.loading-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 0;
  color: #909399;
}

.loading-text {
  margin-top: 16px;
}

.empty-container {
  padding: 40px 0;
}

.result-header {
  margin-bottom: 16px;
}

.result-count {
  color: #606266;
  font-size: 14px;
}

.result-count strong {
  color: #409eff;
  margin: 0 4px;
}

.room-type-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.room-type-card {
  transition: all 0.3s ease;
}

.room-type-card:hover {
  transform: translateY(-2px);
}

.room-type-header {
  display: flex;
  gap: 20px;
}

.room-type-image {
  width: 160px;
  height: 120px;
  border-radius: 8px;
  overflow: hidden;
  background: #f5f7fa;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.room-type-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.placeholder-icon {
  color: #c0c4cc;
}

.room-type-info {
  flex: 1;
  min-width: 0;
}

.room-type-name {
  font-size: 18px;
  font-weight: 600;
  margin: 0 0 8px 0;
  color: #303133;
}

.room-type-meta {
  display: flex;
  gap: 8px;
  margin-bottom: 8px;
}

.room-type-facilities {
  display: flex;
  gap: 6px;
  flex-wrap: wrap;
  margin-bottom: 12px;
}

.room-type-availability {
  display: flex;
  align-items: center;
  gap: 24px;
}

.available-count {
  display: flex;
  align-items: center;
  gap: 4px;
  color: #67c23a;
  font-size: 14px;
}

.available-count strong {
  font-size: 16px;
  margin: 0 2px;
}

.price-info {
  display: flex;
  align-items: baseline;
  gap: 8px;
}

.price-label {
  color: #909399;
  font-size: 12px;
}

.price-label.weekend {
  margin-left: 12px;
}

.price {
  font-size: 20px;
  font-weight: 600;
  color: #f56c6c;
}

.price.weekend {
  color: #e6a23c;
}

.price-unit {
  color: #909399;
  font-size: 12px;
}

.room-type-actions {
  display: flex;
  align-items: flex-start;
  flex-shrink: 0;
}

.room-list {
  margin-top: 16px;
  padding-top: 16px;
  border-top: 1px solid #ebeef5;
}

.room-price {
  font-size: 16px;
  font-weight: 600;
  color: #f56c6c;
}

.pagination-wrap {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}

.booking-summary {
  padding: 8px 0;
}

.summary-section {
  margin-bottom: 24px;
}

.section-title {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 15px;
  font-weight: 600;
  margin: 0 0 12px 0;
  color: #303133;
}

.section-title .el-tag {
  margin-left: auto;
}

.summary-row {
  display: flex;
  justify-content: space-between;
  padding: 6px 0;
  font-size: 14px;
}

.summary-row .label {
  color: #606266;
}

.summary-row .value {
  color: #303133;
}

.summary-row .value strong {
  color: #409eff;
  font-size: 16px;
}

.empty-rooms {
  padding: 20px 0;
}

.selected-room-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.selected-room-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
  background: #f5f7fa;
  border-radius: 8px;
}

.room-info {
  flex: 1;
  min-width: 0;
}

.room-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 4px;
}

.room-number {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.room-type-name {
  font-size: 13px;
  color: #909399;
}

.room-detail {
  font-size: 12px;
  color: #606266;
}

.room-price-section {
  text-align: right;
  flex-shrink: 0;
}

.room-price-section .room-price {
  font-size: 18px;
}

.room-price-total {
  font-size: 12px;
  color: #909399;
  margin-top: 2px;
}

.total-section {
  padding-top: 16px;
  border-top: 1px dashed #ebeef5;
}

.total-row {
  display: flex;
  justify-content: space-between;
  align-items: baseline;
}

.total-row .label {
  font-size: 14px;
  color: #606266;
}

.total-row .total-price {
  font-size: 24px;
  font-weight: 600;
  color: #f56c6c;
}

.total-row.detail {
  margin-top: 4px;
}

.total-row.detail .label,
.total-row.detail .value {
  font-size: 12px;
  color: #909399;
}

.drawer-footer {
  display: flex;
  gap: 12px;
  justify-content: flex-end;
}

.floating-booking-btn {
  position: fixed;
  right: 24px;
  bottom: 40px;
  width: 60px;
  height: 60px;
  border-radius: 50%;
  background: #409eff;
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 4px 16px rgba(64, 158, 255, 0.4);
  cursor: pointer;
  z-index: 1000;
  transition: all 0.3s ease;
}

.floating-booking-btn:hover {
  transform: scale(1.05);
  box-shadow: 0 6px 20px rgba(64, 158, 255, 0.5);
}

.cart-badge {
  position: absolute;
  top: -8px;
  right: -8px;
}

@media (max-width: 768px) {
  .room-type-header {
    flex-direction: column;
  }
  
  .room-type-image {
    width: 100%;
    height: 160px;
  }
  
  .room-type-availability {
    flex-wrap: wrap;
    gap: 12px;
  }
}
</style>
