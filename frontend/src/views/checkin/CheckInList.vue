<template>
  <div class="checkin-list">
    <div class="stats-cards">
      <el-row :gutter="16">
        <el-col :span="6">
          <el-card class="stat-card today-checkin">
            <div class="stat-content">
              <div class="stat-icon">
                <el-icon :size="32"><User /></el-icon>
              </div>
              <div class="stat-info">
                <div class="stat-value">{{ todayStats.todayCheckIn || 0 }}</div>
                <div class="stat-label">今日入住</div>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="stat-card today-checkout">
            <div class="stat-content">
              <div class="stat-icon">
                <el-icon :size="32"><SwitchButton /></el-icon>
              </div>
              <div class="stat-info">
                <div class="stat-value">{{ todayStats.todayCheckOut || 0 }}</div>
                <div class="stat-label">今日退房</div>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="stat-card in-house">
            <div class="stat-content">
              <div class="stat-icon">
                <el-icon :size="32"><House /></el-icon>
              </div>
              <div class="stat-info">
                <div class="stat-value">{{ todayStats.currentInHouse || 0 }}</div>
                <div class="stat-label">当前在住</div>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="stat-card overdue">
            <div class="stat-content">
              <div class="stat-icon">
                <el-icon :size="32"><Warning /></el-icon>
              </div>
              <div class="stat-info">
                <div class="stat-value">{{ todayStats.overdueCount || 0 }}</div>
                <div class="stat-label">超期未退</div>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <el-card class="filter-card">
      <div class="filter-section">
        <el-space wrap :size="16">
          <el-radio-group v-model="quickFilter" @change="handleQuickFilter">
            <el-radio-button value="all">全部</el-radio-button>
            <el-radio-button value="todayCheckIn">今日入住</el-radio-button>
            <el-radio-button value="todayCheckOut">今日退房</el-radio-button>
            <el-radio-button value="inHouse">在住中</el-radio-button>
            <el-radio-button value="overdue">超期未退</el-radio-button>
          </el-radio-group>
        </el-space>
      </div>

      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="关键字">
          <el-input
            v-model="searchForm.keyword"
            placeholder="入住单号/客户姓名/手机号/房号"
            clearable
            style="width: 280px"
            @keyup.enter="handleSearch"
          />
        </el-form-item>
        <el-form-item label="房型">
          <el-select v-model="searchForm.roomTypeId" placeholder="全部房型" clearable style="width: 160px">
            <el-option
              v-for="rt in roomTypeList"
              :key="rt.id"
              :label="rt.typeName"
              :value="rt.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="入住状态">
          <el-select v-model="searchForm.status" placeholder="全部状态" clearable style="width: 140px">
            <el-option label="在住" :value="1" />
            <el-option label="已退房" :value="2" />
            <el-option label="超期未退" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item label="入住日期">
          <el-date-picker
            v-model="dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            style="width: 260px"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">
            <el-icon><Search /></el-icon>
            查询
          </el-button>
          <el-button @click="handleReset">
            <el-icon><Refresh /></el-icon>
            重置
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card>
      <template #header>
        <div class="card-header">
          <span>入住单列表</span>
          <div class="header-actions">
            <el-button type="primary" @click="goToWalkIn">
              <el-icon><User /></el-icon>
              散客入住
            </el-button>
            <el-button type="success" @click="refreshData">
              <el-icon><Refresh /></el-icon>
              刷新
            </el-button>
          </div>
        </div>
      </template>

      <el-table
        :data="tableData"
        v-loading="loading"
        stripe
        border
        style="width: 100%"
        @row-dblclick="goToDetail"
      >
        <el-table-column prop="checkInNo" label="入住单号" width="200" show-overflow-tooltip />
        <el-table-column prop="customerName" label="客户姓名" width="100" />
        <el-table-column prop="customerPhone" label="手机号" width="130" />
        <el-table-column prop="roomTypeName" label="房型" width="100" />
        <el-table-column prop="roomNumber" label="房号" width="80" align="center" />
        <el-table-column prop="checkInDate" label="入住日期" width="110" />
        <el-table-column prop="checkOutDate" label="预计退房" width="110" />
        <el-table-column prop="stayedDays" label="已住天数" width="90" align="center">
          <template #default="{ row }">
            {{ calculateStayedDays(row) }}
          </template>
        </el-table-column>
        <el-table-column prop="depositAmount" label="押金" width="100" align="right">
          <template #default="{ row }">
            ¥{{ row.depositAmount || 0 }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="getStatusTagType(row)" size="small">
              {{ getStatusText(row) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="operatorName" label="办理人" width="100" />
        <el-table-column label="操作" width="280" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="goToDetail(row)">详情</el-button>
            <el-button
              v-if="row.status === 1 || row.status === 3"
              type="success"
              link
              @click="handleCheckout(row)"
            >
              退房
            </el-button>
            <el-button
              v-if="row.status === 1 || row.status === 3"
              type="warning"
              link
              @click="openChangeRoomDialog(row)"
            >
              换房
            </el-button>
            <el-button
              v-if="row.status === 1 || row.status === 3"
              type="info"
              link
              @click="openExtendDialog(row)"
            >
              续住
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination">
        <el-pagination
          v-model:current-page="pagination.pageNum"
          v-model:page-size="pagination.pageSize"
          :page-sizes="[20, 50, 100]"
          :total="pagination.total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handlePageChange"
        />
      </div>
    </el-card>

    <el-dialog
      v-model="changeRoomDialogVisible"
      title="办理换房"
      width="600px"
      @close="resetChangeRoomForm"
    >
      <el-form :model="changeRoomForm" label-width="100px">
        <el-form-item label="原房间">
          <span>{{ currentRoom?.roomNumber }} - {{ currentRoom?.roomTypeName }}</span>
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
              :label="room.roomNumber + ' - ' + (room.orientation || '') + ' ' + (room.viewType || '')"
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
          <span>{{ currentCheckIn?.checkOutDate }}</span>
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
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Search, Refresh, User, SwitchButton, House, Warning
} from '@element-plus/icons-vue'
import api from '@/api'

const router = useRouter()

const loading = ref(false)
const submitting = ref(false)
const loadingRooms = ref(false)
const quickFilter = ref('all')

const todayStats = ref({})

const searchForm = reactive({
  keyword: '',
  roomTypeId: null,
  status: null,
  startDate: null,
  endDate: null,
  floorId: null,
  sortField: '',
  sortOrder: ''
})

const dateRange = ref([])

const pagination = reactive({
  pageNum: 1,
  pageSize: 20,
  total: 0
})

const tableData = ref([])
const roomTypeList = ref([])
const availableRooms = ref([])

const changeRoomDialogVisible = ref(false)
const extendDialogVisible = ref(false)
const currentCheckIn = ref(null)
const currentRoom = ref(null)

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

const loadTodayStats = async () => {
  try {
    const res = await api.checkin.todayStats()
    if (res.code === 200) {
      todayStats.value = res.data
    }
  } catch (e) {
    console.error('获取今日统计失败', e)
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

const loadData = async () => {
  loading.value = true
  try {
    const params = {
      ...searchForm,
      pageNum: pagination.pageNum,
      pageSize: pagination.pageSize
    }
    if (dateRange.value && dateRange.value.length === 2) {
      params.startDate = dateRange.value[0]
      params.endDate = dateRange.value[1]
    }
    const res = await api.checkin.list(params)
    if (res.code === 200) {
      tableData.value = res.data.list
      pagination.total = res.data.total
    }
  } catch (e) {
    console.error('加载入住单列表失败', e)
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  pagination.pageNum = 1
  loadData()
}

const handleReset = () => {
  searchForm.keyword = ''
  searchForm.roomTypeId = null
  searchForm.status = null
  searchForm.floorId = null
  dateRange.value = []
  quickFilter.value = 'all'
  pagination.pageNum = 1
  loadData()
}

const handleQuickFilter = (val) => {
  const today = new Date().toISOString().split('T')[0]
  switch (val) {
    case 'todayCheckIn':
      dateRange.value = [today, today]
      searchForm.status = null
      break
    case 'todayCheckOut':
      searchForm.status = null
      dateRange.value = []
      break
    case 'inHouse':
      searchForm.status = 1
      dateRange.value = []
      break
    case 'overdue':
      searchForm.status = 3
      dateRange.value = []
      break
    default:
      searchForm.status = null
      dateRange.value = []
  }
  pagination.pageNum = 1
  loadData()
}

const handlePageChange = (val) => {
  pagination.pageNum = val
  loadData()
}

const handleSizeChange = (val) => {
  pagination.pageSize = val
  pagination.pageNum = 1
  loadData()
}

const refreshData = () => {
  loadTodayStats()
  loadData()
}

const goToDetail = (row) => {
  router.push(`/checkin/detail/${row.id}`)
}

const goToWalkIn = () => {
  router.push('/checkin/walkin')
}

const handleCheckout = (row) => {
  router.push({ path: '/checkin/checkout', query: { checkInId: row.id } })
}

const getStatusText = (row) => {
  if (row.isOverdue === 1) return '超期未退'
  const map = { 1: '在住', 2: '已退房', 3: '超期未退' }
  return map[row.status] || '未知'
}

const getStatusTagType = (row) => {
  if (row.isOverdue === 1 || row.status === 3) return 'danger'
  const map = { 1: 'success', 2: 'info' }
  return map[row.status] || ''
}

const calculateStayedDays = (row) => {
  if (row.status === 2 && row.stayedDays) {
    return row.stayedDays
  }
  const checkIn = new Date(row.checkInDate)
  const today = new Date()
  const diff = Math.floor((today - checkIn) / (1000 * 60 * 60 * 24))
  return diff < 0 ? 0 : diff
}

const openChangeRoomDialog = async (row) => {
  currentCheckIn.value = row
  currentRoom.value = row
  changeRoomForm.roomTypeId = row.roomTypeId
  changeRoomDialogVisible.value = true
  await loadAvailableRoomsForChange()
}

const loadAvailableRoomsForChange = async () => {
  if (!currentCheckIn.value?.id || !changeRoomForm.roomTypeId) return
  loadingRooms.value = true
  try {
    const res = await api.checkin.availableRoomsForChange(
      currentCheckIn.value.id,
      changeRoomForm.roomTypeId
    )
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
  currentCheckIn.value = null
  currentRoom.value = null
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
      checkInId: currentCheckIn.value.id,
      newRoomId: changeRoomForm.newRoomId,
      changeReason: changeRoomForm.changeReason,
      changeDetail: changeRoomForm.changeDetail
    })
    if (res.code === 200) {
      ElMessage.success('换房成功')
      changeRoomDialogVisible.value = false
      loadData()
    }
  } catch (e) {
    console.error('换房失败', e)
  } finally {
    submitting.value = false
  }
}

const openExtendDialog = (row) => {
  currentCheckIn.value = row
  extendDialogVisible.value = true
}

const calculateExtendDays = () => {
  if (!extendForm.newCheckOutDate || !currentCheckIn.value) {
    extendForm.extendDays = 0
    extendForm.extendAmount = 0
    return
  }
  const originalDate = new Date(currentCheckIn.value.checkOutDate)
  const newDate = new Date(extendForm.newCheckOutDate)
  const days = Math.floor((newDate - originalDate) / (1000 * 60 * 60 * 24))
  extendForm.extendDays = days > 0 ? days : 0
  extendForm.extendAmount = extendForm.extendDays * (currentCheckIn.value.roomPrice || 0)
}

const resetExtendForm = () => {
  extendForm.newCheckOutDate = null
  extendForm.extendDays = 0
  extendForm.extendAmount = 0
  extendForm.reason = ''
  currentCheckIn.value = null
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
      checkInId: currentCheckIn.value.id,
      newCheckOutDate: extendForm.newCheckOutDate,
      reason: extendForm.reason
    })
    if (res.code === 200) {
      ElMessage.success('续住成功')
      extendDialogVisible.value = false
      loadData()
    }
  } catch (e) {
    console.error('续住失败', e)
  } finally {
    submitting.value = false
  }
}

onMounted(() => {
  loadTodayStats()
  loadRoomTypes()
  loadData()
})
</script>

<style scoped lang="scss">
.checkin-list {
  .stats-cards {
    margin-bottom: 16px;

    .stat-card {
      .stat-content {
        display: flex;
        align-items: center;
        gap: 16px;
      }

      .stat-icon {
        width: 60px;
        height: 60px;
        border-radius: 10px;
        display: flex;
        align-items: center;
        justify-content: center;
        color: #fff;
      }

      &.today-checkin .stat-icon {
        background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      }

      &.today-checkout .stat-icon {
        background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
      }

      &.in-house .stat-icon {
        background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
      }

      &.overdue .stat-icon {
        background: linear-gradient(135deg, #fa709a 0%, #fee140 100%);
      }

      .stat-info {
        .stat-value {
          font-size: 24px;
          font-weight: bold;
          color: #303133;
          line-height: 1.2;
        }

        .stat-label {
          font-size: 14px;
          color: #909399;
          margin-top: 4px;
        }
      }
    }
  }

  .filter-card {
    margin-bottom: 16px;

    .filter-section {
      margin-bottom: 16px;
      padding-bottom: 16px;
      border-bottom: 1px solid #ebeef5;
    }

    .search-form {
      margin: 0;
    }
  }

  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }

  .pagination {
    margin-top: 20px;
    display: flex;
    justify-content: flex-end;
  }
}
</style>
