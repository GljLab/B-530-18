<template>
  <div class="member-list-container">
    <el-card shadow="never" class="stats-card">
      <div class="stats-grid">
        <div class="stat-item">
          <div class="stat-icon" style="background: #ecf5ff; color: #409eff">
            <el-icon><User /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ statistics.totalMembers || 0 }}</div>
            <div class="stat-label">会员总数</div>
          </div>
        </div>
        <div class="stat-item">
          <div class="stat-icon" style="background: #f0f9eb; color: #67c23a">
            <el-icon><Plus /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ statistics.newMembersThisMonth || 0 }}</div>
            <div class="stat-label">本月新增</div>
          </div>
        </div>
        <div class="stat-item">
          <div class="stat-icon" style="background: #fdf6ec; color: #e6a23c">
            <el-icon><TrendCharts /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ statistics.activeMembersThisMonth || 0 }}</div>
            <div class="stat-label">本月活跃</div>
          </div>
        </div>
      </div>
    </el-card>

    <el-card shadow="never" class="search-card">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="关键词">
          <el-input
            v-model="searchForm.keyword"
            placeholder="会员卡号/姓名/手机号"
            clearable
            style="width: 240px"
            @keyup.enter="handleSearch"
          />
        </el-form-item>
        <el-form-item label="会员等级">
          <el-select v-model="searchForm.levelIds" multiple placeholder="请选择" style="width: 200px">
            <el-option
              v-for="level in levelOptions"
              :key="level.id"
              :label="level.levelName"
              :value="level.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" multiple placeholder="请选择" style="width: 160px">
            <el-option label="正常" :value="1" />
            <el-option label="已冻结" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item label="注册时间">
          <el-date-picker
            v-model="searchForm.registerTimeRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            value-format="YYYY-MM-DD"
            style="width: 260px"
          />
        </el-form-item>
        <el-form-item label="积分范围">
          <el-input-number v-model="searchForm.pointsMin" :min="0" placeholder="最小" style="width: 100px" />
          <span style="margin: 0 8px">-</span>
          <el-input-number v-model="searchForm.pointsMax" :min="0" placeholder="最大" style="width: 100px" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">
            <el-icon><Search /></el-icon>搜索
          </el-button>
          <el-button @click="handleReset">
            <el-icon><Refresh /></el-icon>重置
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card shadow="never" class="table-card">
      <div class="table-header">
        <div class="table-title">会员列表</div>
        <div class="table-actions">
          <el-button
            v-if="hasPermission('member:add')"
            type="primary"
            @click="handleRegister"
          >
            <el-icon><Plus /></el-icon>注册会员
          </el-button>
          <el-button
            v-if="hasPermission('member:point:add') && selectedMembers.length > 0"
            type="warning"
            @click="handleBatchAddPoints"
          >
            <el-icon><Coin /></el-icon>批量发放积分
          </el-button>
          <el-button
            v-if="hasPermission('member:export')"
            @click="handleExport"
          >
            <el-icon><Download /></el-icon>导出
          </el-button>
        </div>
      </div>

      <el-table
        ref="tableRef"
        :data="tableData"
        stripe
        border
        v-loading="tableLoading"
        style="width: 100%"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="50" />
        <el-table-column prop="memberNo" label="会员卡号" width="180" />
        <el-table-column prop="customerName" label="姓名" width="100" />
        <el-table-column prop="phone" label="手机号" width="130" />
        <el-table-column label="当前等级" width="140">
          <template #default="{ row }">
            <span class="level-badge" :style="{ background: row.levelColor + '20', color: row.levelColor }">
              {{ row.levelIcon }} {{ row.levelName }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="currentPoints" label="当前积分" width="110" align="right">
          <template #default="{ row }">
            <span class="points-value">{{ formatNumber(row.currentPoints) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="totalSpent" label="累计消费" width="120" align="right">
          <template #default="{ row }">
            <span>¥{{ formatNumber(row.totalSpent) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="registerTime" label="注册时间" width="170">
          <template #default="{ row }">
            {{ formatDateTime(row.registerTime) }}
          </template>
        </el-table-column>
        <el-table-column prop="lastStayTime" label="上次入住" width="170">
          <template #default="{ row }">
            {{ row.lastStayTime ? formatDateTime(row.lastStayTime) : '-' }}
          </template>
        </el-table-column>
        <el-table-column label="状态" width="90" align="center">
          <template #default="{ row }">
            <el-tag v-if="row.status === 1" type="success" size="small">正常</el-tag>
            <el-tag v-else type="danger" size="small">已冻结</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="240" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="handleDetail(row)">
              详情
            </el-button>
            <el-button
              v-if="hasPermission('member:point:add')"
              type="success"
              link
              size="small"
              @click="handleAddPoints(row)"
            >发放积分</el-button>
            <el-button
              v-if="hasPermission('member:level:adjust')"
              type="warning"
              link
              size="small"
              @click="handleAdjustLevel(row)"
            >调整等级</el-button>
            <el-button
              v-if="hasPermission('member:freeze') && row.status === 1"
              type="danger"
              link
              size="small"
              @click="handleFreeze(row)"
            >冻结</el-button>
            <el-button
              v-if="hasPermission('member:unfreeze') && row.status === 0"
              type="success"
              link
              size="small"
              @click="handleUnfreeze(row)"
            >解冻</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination">
        <el-pagination
          v-model:current-page="pagination.pageNum"
          v-model:page-size="pagination.pageSize"
          :total="pagination.total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handlePageChange"
        />
      </div>
    </el-card>

    <el-dialog
      v-model="pointsDialogVisible"
      title="发放积分"
      width="480px"
      destroy-on-close
    >
      <el-form ref="pointsFormRef" :model="pointsForm" :rules="pointsRules" label-width="100px">
        <el-form-item label="会员姓名">
          <span>{{ pointsForm.memberName }}</span>
        </el-form-item>
        <el-form-item label="会员卡号">
          <span>{{ pointsForm.memberNo }}</span>
        </el-form-item>
        <el-form-item label="当前积分">
          <span class="points-value">{{ formatNumber(pointsForm.currentPoints) }}</span>
        </el-form-item>
        <el-form-item label="发放积分" prop="points">
          <el-input-number v-model="pointsForm.points" :min="0" :precision="2" style="width: 100%" />
        </el-form-item>
        <el-form-item label="发放原因" prop="reasonType">
          <el-select v-model="pointsForm.reasonType" style="width: 100%">
            <el-option label="活动赠送" :value="2" />
            <el-option label="客诉补偿" :value="3" />
            <el-option label="生日礼遇" :value="4" />
            <el-option label="推荐奖励" :value="5" />
            <el-option label="其他" :value="7" />
          </el-select>
        </el-form-item>
        <el-form-item label="详细说明" prop="detail">
          <el-input
            v-model="pointsForm.detail"
            type="textarea"
            :rows="3"
            placeholder="请输入详细说明"
            maxlength="500"
            show-word-limit
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="pointsDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="pointsDialogSaving" @click="handlePointsSubmit">确认发放</el-button>
      </template>
    </el-dialog>

    <el-dialog
      v-model="levelDialogVisible"
      title="调整等级"
      width="480px"
      destroy-on-close
    >
      <el-form ref="levelFormRef" :model="levelForm" :rules="levelRules" label-width="100px">
        <el-form-item label="会员姓名">
          <span>{{ levelForm.memberName }}</span>
        </el-form-item>
        <el-form-item label="当前等级">
          <span class="level-badge" :style="{ background: levelForm.currentLevelColor + '20', color: levelForm.currentLevelColor }">
            {{ levelForm.currentLevelName }}
          </span>
        </el-form-item>
        <el-form-item label="新等级" prop="newLevelId">
          <el-select v-model="levelForm.newLevelId" style="width: 100%">
            <el-option
              v-for="level in levelOptions"
              :key="level.id"
              :label="level.levelName"
              :value="level.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="调整原因" prop="reason">
          <el-input
            v-model="levelForm.reason"
            type="textarea"
            :rows="3"
            placeholder="请输入调整原因"
            maxlength="500"
            show-word-limit
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="levelDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="levelDialogSaving" @click="handleLevelSubmit">确认调整</el-button>
      </template>
    </el-dialog>

    <el-dialog
      v-model="registerDialogVisible"
      title="注册会员"
      width="560px"
      destroy-on-close
    >
      <el-form ref="registerFormRef" :model="registerForm" :rules="registerRules" label-width="100px">
        <el-form-item label="注册方式" prop="registerType">
          <el-radio-group v-model="registerForm.registerType">
            <el-radio :value="1">已有客户升级</el-radio>
            <el-radio :value="2">新客户注册</el-radio>
          </el-radio-group>
        </el-form-item>

        <template v-if="registerForm.registerType === 1">
          <el-form-item label="客户姓名" prop="customerId">
            <el-select
              v-model="registerForm.customerId"
              filterable
              remote
              placeholder="请输入客户姓名搜索"
              style="width: 100%"
              :remote-method="searchCustomer"
              :loading="customerSearchLoading"
            >
              <el-option
                v-for="customer in customerOptions"
                :key="customer.id"
                :label="customer.name + ' - ' + customer.phone"
                :value="customer.id"
              />
            </el-select>
          </el-form-item>
        </template>

        <template v-if="registerForm.registerType === 2">
          <el-form-item label="客户姓名" prop="name">
            <el-input v-model="registerForm.name" placeholder="请输入客户姓名" />
          </el-form-item>
          <el-form-item label="手机号" prop="phone">
            <el-input v-model="registerForm.phone" placeholder="请输入手机号" />
          </el-form-item>
          <el-form-item label="证件类型" prop="idType">
            <el-select v-model="registerForm.idType" style="width: 100%">
              <el-option label="身份证" :value="1" />
              <el-option label="护照" :value="2" />
              <el-option label="其他" :value="3" />
            </el-select>
          </el-form-item>
          <el-form-item label="证件号码" prop="idNumber">
            <el-input v-model="registerForm.idNumber" placeholder="请输入证件号码" />
          </el-form-item>
        </template>

        <el-form-item label="注册来源" prop="registerSource">
          <el-select v-model="registerForm.registerSource" style="width: 100%">
            <el-option label="前台注册" :value="1" />
            <el-option label="官网注册" :value="2" />
            <el-option label="APP注册" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item label="推荐人卡号">
          <el-input v-model="registerForm.referrerNo" placeholder="请输入推荐人会员卡号（选填）" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="registerDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="registerDialogSaving" @click="handleRegisterSubmit">确认注册</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { User, Plus, TrendCharts, Search, Refresh, Coin, Download } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import api from '@/api'

const router = useRouter()
const userStore = useUserStore()
const hasPermission = (p) => userStore.hasPermission(p)

const tableRef = ref(null)
const tableData = ref([])
const tableLoading = ref(false)
const levelOptions = ref([])
const selectedMembers = ref([])
const statistics = ref({})

const searchForm = reactive({
  keyword: '',
  levelIds: [],
  status: [],
  registerTimeRange: [],
  pointsMin: null,
  pointsMax: null
})

const pagination = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0
})

const pointsDialogVisible = ref(false)
const pointsDialogSaving = ref(false)
const pointsFormRef = ref(null)
const pointsForm = reactive({
  memberId: null,
  memberName: '',
  memberNo: '',
  currentPoints: 0,
  points: 0,
  reasonType: 2,
  detail: ''
})

const pointsRules = {
  points: [{ required: true, message: '请输入发放积分数量', trigger: 'blur' }],
  reasonType: [{ required: true, message: '请选择发放原因', trigger: 'change' }]
}

const levelDialogVisible = ref(false)
const levelDialogSaving = ref(false)
const levelFormRef = ref(null)
const levelForm = reactive({
  memberId: null,
  memberName: '',
  currentLevelId: null,
  currentLevelName: '',
  currentLevelColor: '',
  newLevelId: null,
  reason: ''
})

const levelRules = {
  newLevelId: [{ required: true, message: '请选择新等级', trigger: 'change' }],
  reason: [{ required: true, message: '请输入调整原因', trigger: 'blur' }]
}

const registerDialogVisible = ref(false)
const registerDialogSaving = ref(false)
const registerFormRef = ref(null)
const registerForm = reactive({
  registerType: 1,
  customerId: null,
  name: '',
  phone: '',
  idType: 1,
  idNumber: '',
  registerSource: 1,
  referrerNo: ''
})

const registerRules = {
  registerType: [{ required: true, message: '请选择注册方式', trigger: 'change' }],
  customerId: [{ required: true, message: '请选择客户', trigger: 'change' }],
  name: [{ required: true, message: '请输入客户姓名', trigger: 'blur' }],
  phone: [{ required: true, message: '请输入手机号', trigger: 'blur' }]
}

const customerOptions = ref([])
const customerSearchLoading = ref(false)

const loadLevelOptions = async () => {
  try {
    const res = await api.memberLevel.listEnabled()
    if (res.code === 200) {
      levelOptions.value = res.data || []
    }
  } catch (e) {
    console.error(e)
  }
}

const loadStatistics = async () => {
  try {
    const res = await api.member.statistics()
    if (res.code === 200) {
      statistics.value = res.data || {}
    }
  } catch (e) {
    console.error(e)
  }
}

const loadTableData = async () => {
  tableLoading.value = true
  try {
    const params = {
      pageNum: pagination.pageNum,
      pageSize: pagination.pageSize,
      keyword: searchForm.keyword || undefined,
      levelIds: searchForm.levelIds.length > 0 ? searchForm.levelIds : undefined,
      status: searchForm.status.length > 0 ? searchForm.status : undefined,
      pointsMin: searchForm.pointsMin,
      pointsMax: searchForm.pointsMax
    }
    if (searchForm.registerTimeRange && searchForm.registerTimeRange.length === 2) {
      params.registerTimeStart = searchForm.registerTimeRange[0]
      params.registerTimeEnd = searchForm.registerTimeRange[1]
    }

    const res = await api.member.page(params)
    if (res.code === 200) {
      tableData.value = res.data.list || []
      pagination.total = res.data.total || 0
    }
  } catch (e) {
    console.error(e)
  } finally {
    tableLoading.value = false
  }
}

const handleSearch = () => {
  pagination.pageNum = 1
  loadTableData()
}

const handleReset = () => {
  searchForm.keyword = ''
  searchForm.levelIds = []
  searchForm.status = []
  searchForm.registerTimeRange = []
  searchForm.pointsMin = null
  searchForm.pointsMax = null
  pagination.pageNum = 1
  loadTableData()
}

const handlePageChange = (page) => {
  pagination.pageNum = page
  loadTableData()
}

const handleSizeChange = (size) => {
  pagination.pageSize = size
  pagination.pageNum = 1
  loadTableData()
}

const handleSelectionChange = (selection) => {
  selectedMembers.value = selection
}

const handleDetail = (row) => {
  router.push(`/member/detail/${row.id}`)
}

const handleAddPoints = (row) => {
  pointsForm.memberId = row.id
  pointsForm.memberName = row.customerName
  pointsForm.memberNo = row.memberNo
  pointsForm.currentPoints = row.currentPoints
  pointsForm.points = 0
  pointsForm.reasonType = 2
  pointsForm.detail = ''
  pointsDialogVisible.value = true
}

const handlePointsSubmit = async () => {
  if (!pointsFormRef.value) return
  try {
    await pointsFormRef.value.validate()
  } catch {
    return
  }

  pointsDialogSaving.value = true
  try {
    const reasonMap = {
      2: '活动赠送',
      3: '客诉补偿',
      4: '生日礼遇',
      5: '推荐奖励',
      7: '其他'
    }
    const res = await api.member.addPoints({
      memberId: pointsForm.memberId,
      points: pointsForm.points,
      reasonType: pointsForm.reasonType,
      reason: reasonMap[pointsForm.reasonType],
      detail: pointsForm.detail
    })
    if (res.code === 200) {
      ElMessage.success('积分发放成功')
      pointsDialogVisible.value = false
      loadTableData()
      loadStatistics()
    }
  } catch (e) {
    console.error(e)
  } finally {
    pointsDialogSaving.value = false
  }
}

const handleAdjustLevel = (row) => {
  levelForm.memberId = row.id
  levelForm.memberName = row.customerName
  levelForm.currentLevelId = row.levelId
  levelForm.currentLevelName = row.levelName
  levelForm.currentLevelColor = row.levelColor
  levelForm.newLevelId = row.levelId
  levelForm.reason = ''
  levelDialogVisible.value = true
}

const handleLevelSubmit = async () => {
  if (!levelFormRef.value) return
  try {
    await levelFormRef.value.validate()
  } catch {
    return
  }

  levelDialogSaving.value = true
  try {
    const res = await api.member.adjustLevel({
      memberId: levelForm.memberId,
      newLevelId: levelForm.newLevelId,
      reason: levelForm.reason
    })
    if (res.code === 200) {
      ElMessage.success('等级调整成功')
      levelDialogVisible.value = false
      loadTableData()
    }
  } catch (e) {
    console.error(e)
  } finally {
    levelDialogSaving.value = false
  }
}

const handleFreeze = (row) => {
  ElMessageBox.prompt(`确定要冻结会员"${row.customerName}"吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning',
    inputPlaceholder: '请输入冻结原因',
    inputValidator: (value) => {
      if (!value || value.trim() === '') {
        return '请输入冻结原因'
      }
      return true
    }
  }).then(async ({ value }) => {
    try {
      const res = await api.member.freeze(row.id, { reason: value })
      if (res.code === 200) {
        ElMessage.success('冻结成功')
        loadTableData()
      }
    } catch (e) {
      console.error(e)
    }
  }).catch(() => {})
}

const handleUnfreeze = (row) => {
  ElMessageBox.prompt(`确定要解冻会员"${row.customerName}"吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'success',
    inputPlaceholder: '请输入解冻原因',
    inputValidator: (value) => {
      if (!value || value.trim() === '') {
        return '请输入解冻原因'
      }
      return true
    }
  }).then(async ({ value }) => {
    try {
      const res = await api.member.unfreeze(row.id, { reason: value })
      if (res.code === 200) {
        ElMessage.success('解冻成功')
        loadTableData()
      }
    } catch (e) {
      console.error(e)
    }
  }).catch(() => {})
}

const handleRegister = () => {
  registerForm.registerType = 1
  registerForm.customerId = null
  registerForm.name = ''
  registerForm.phone = ''
  registerForm.idType = 1
  registerForm.idNumber = ''
  registerForm.registerSource = 1
  registerForm.referrerNo = ''
  registerDialogVisible.value = true
}

const searchCustomer = async (query) => {
  if (!query) {
    customerOptions.value = []
    return
  }
  customerSearchLoading.value = true
  try {
    const res = await api.customer.getPage({ keyword: query, pageNum: 1, pageSize: 20 })
    if (res.code === 200) {
      customerOptions.value = res.data.list || []
    }
  } catch (e) {
    console.error(e)
  } finally {
    customerSearchLoading.value = false
  }
}

const handleRegisterSubmit = async () => {
  if (!registerFormRef.value) return
  try {
    await registerFormRef.value.validate()
  } catch {
    return
  }

  registerDialogSaving.value = true
  try {
    let res
    if (registerForm.registerType === 1) {
      let referrerId = null
      if (registerForm.referrerNo) {
        const referrerRes = await api.member.getByMemberNo(registerForm.referrerNo)
        if (referrerRes.code === 200 && referrerRes.data) {
          referrerId = referrerRes.data.id
        } else {
          ElMessage.warning('推荐人卡号不存在，将忽略推荐人信息')
        }
      }
      res = await api.member.registerFromCustomer({
        customerId: registerForm.customerId,
        registerSource: registerForm.registerSource,
        referrerId
      })
    } else {
      let referrerId = null
      if (registerForm.referrerNo) {
        const referrerRes = await api.member.getByMemberNo(registerForm.referrerNo)
        if (referrerRes.code === 200 && referrerRes.data) {
          referrerId = referrerRes.data.id
        } else {
          ElMessage.warning('推荐人卡号不存在，将忽略推荐人信息')
        }
      }
      res = await api.member.registerNew({
        name: registerForm.name,
        phone: registerForm.phone,
        idType: registerForm.idType,
        idNumber: registerForm.idNumber,
        registerSource: registerForm.registerSource,
        referrerId
      })
    }

    if (res.code === 200) {
      ElMessage.success('会员注册成功')
      registerDialogVisible.value = false
      loadTableData()
      loadStatistics()
    }
  } catch (e) {
    console.error(e)
  } finally {
    registerDialogSaving.value = false
  }
}

const handleBatchAddPoints = () => {
  ElMessageBox.prompt(`确定要为选中的 ${selectedMembers.value.length} 位会员发放积分吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning',
    inputPlaceholder: '请输入积分数量',
    inputType: 'number',
    inputValidator: (value) => {
      if (!value || Number(value) <= 0) {
        return '请输入大于0的积分数量'
      }
      return true
    }
  }).then(async ({ value }) => {
    try {
      const memberIds = selectedMembers.value.map(m => m.id)
      const res = await api.member.batchAddPoints({
        memberIds,
        points: Number(value),
        reasonType: 2,
        reason: '批量发放'
      })
      if (res.code === 200) {
        ElMessage.success('批量发放积分成功')
        loadTableData()
      }
    } catch (e) {
      console.error(e)
    }
  }).catch(() => {})
}

const handleExport = () => {
  ElMessage.info('导出功能开发中')
}

const formatNumber = (num) => {
  if (num === null || num === undefined) return '0'
  return Number(num).toLocaleString()
}

const formatDateTime = (datetime) => {
  if (!datetime) return ''
  return datetime.replace('T', ' ').substring(0, 19)
}

onMounted(() => {
  loadLevelOptions()
  loadStatistics()
  loadTableData()
})
</script>

<style scoped>
.member-list-container {
  padding: 16px;
}

.stats-card {
  margin-bottom: 16px;
}

.stats-grid {
  display: flex;
  gap: 24px;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 16px;
  flex: 1;
  padding: 8px;
}

.stat-icon {
  width: 56px;
  height: 56px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28px;
}

.stat-info {
  flex: 1;
}

.stat-value {
  font-size: 24px;
  font-weight: 600;
  color: #303133;
  line-height: 1.2;
}

.stat-label {
  font-size: 13px;
  color: #909399;
  margin-top: 4px;
}

.search-card {
  margin-bottom: 16px;
}

.search-form {
  margin: 0;
}

.table-card {
  padding: 20px;
}

.table-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.table-title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.table-actions {
  display: flex;
  gap: 8px;
}

.level-badge {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 4px 10px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 500;
}

.points-value {
  color: #e6a23c;
  font-weight: 600;
}

.pagination {
  margin-top: 16px;
  text-align: right;
}
</style>
