<template>
  <div class="complaint-manage-container">
    <el-card shadow="never" class="search-card">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="关键词">
          <el-input
            v-model="searchForm.keyword"
            placeholder="客人姓名/手机号/投诉单号/入住单号"
            clearable
            style="width: 260px"
            @keyup.enter="handleSearch"
          />
        </el-form-item>
        <el-form-item label="投诉类型">
          <el-select v-model="searchForm.complaintType" placeholder="全部类型" clearable style="width: 160px">
            <el-option
              v-for="item in complaintTypeOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="投诉时间">
          <el-date-picker
            v-model="searchForm.dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            value-format="YYYY-MM-DD"
            style="width: 280px"
          />
        </el-form-item>
        <el-form-item v-if="hasPermission('complaint:myTask')">
          <el-checkbox v-model="searchForm.myTask">
            <el-icon><User /></el-icon>我的投诉任务
          </el-checkbox>
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
      <el-tabs v-model="activeTab" @tab-change="handleTabChange">
        <el-tab-pane label="待处理" name="pending" />
        <el-tab-pane label="处理中" name="processing" />
        <el-tab-pane label="已处理" name="handled" />
        <el-tab-pane label="已驳回" name="rejected" />
      </el-tabs>

      <el-table :data="tableData" stripe border v-loading="tableLoading" style="width: 100%">
        <el-table-column prop="complaintNo" label="投诉单号" width="160" />
        <el-table-column prop="complaintTime" label="投诉时间" width="170" align="center" />
        <el-table-column prop="customerName" label="客人姓名" width="100" />
        <el-table-column prop="customerPhone" label="手机号" width="130" />
        <el-table-column prop="checkInNo" label="入住单号" width="140" />
        <el-table-column prop="roomTypeName" label="房型" width="110" />
        <el-table-column label="投诉类型" width="120" align="center">
          <template #default="{ row }">
            <el-tag :type="getComplaintTypeTagType(row.complaintType)" size="small">
              {{ getComplaintTypeLabel(row.complaintType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="getStatusTagType(row.complaintStatus)" size="small">
              {{ getStatusLabel(row.complaintStatus) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="assignUserName" label="责任人" width="100">
          <template #default="{ row }">
            {{ row.assignUserName || '-' }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="280" fixed="right" align="center">
          <template #default="{ row }">
            <el-button size="small" type="primary" link @click="handleViewDetail(row)">
              查看详情
            </el-button>
            <el-button
              v-if="row.complaintStatus === 1 && hasPermission('complaint:accept')"
              size="small"
              type="success"
              link
              @click="handleAccept(row)"
            >受理</el-button>
            <el-button
              v-if="row.complaintStatus === 1 && hasPermission('complaint:reject')"
              size="small"
              type="danger"
              link
              @click="handleReject(row)"
            >驳回</el-button>
            <el-button
              v-if="row.complaintStatus === 2 && hasPermission('complaint:handle') && isMyTask(row)"
              size="small"
              type="warning"
              link
              @click="handleProcess(row)"
            >处理</el-button>
            <el-button
              v-if="row.complaintStatus === 3 && hasPermission('complaint:visit')"
              size="small"
              type="info"
              link
              @click="handleVisit(row)"
            >回访</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrapper">
        <el-pagination
          v-model:current-page="pagination.pageNum"
          v-model:page-size="pagination.pageSize"
          :total="pagination.total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <el-drawer
      v-model="detailVisible"
      title="投诉详情"
      size="600px"
      :destroy-on-close="true"
    >
      <div v-if="currentDetail" class="detail-container">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="投诉单号">{{ currentDetail.complaintNo }}</el-descriptions-item>
          <el-descriptions-item label="投诉时间">{{ currentDetail.complaintTime }}</el-descriptions-item>
          <el-descriptions-item label="投诉状态">
            <el-tag :type="getStatusTagType(currentDetail.complaintStatus)">
              {{ getStatusLabel(currentDetail.complaintStatus) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="投诉类型">
            <el-tag :type="getComplaintTypeTagType(currentDetail.complaintType)">
              {{ getComplaintTypeLabel(currentDetail.complaintType) }}
            </el-tag>
          </el-descriptions-item>
        </el-descriptions>

        <div class="detail-section">
          <div class="section-title">客人信息</div>
          <el-descriptions :column="2" border size="small">
            <el-descriptions-item label="姓名">{{ currentDetail.customerName }}</el-descriptions-item>
            <el-descriptions-item label="手机号">{{ currentDetail.customerPhone }}</el-descriptions-item>
            <el-descriptions-item label="邮箱" :span="2">{{ currentDetail.customerEmail || '-' }}</el-descriptions-item>
          </el-descriptions>
        </div>

        <div class="detail-section">
          <div class="section-title">入住信息</div>
          <el-descriptions :column="2" border size="small">
            <el-descriptions-item label="入住单号">{{ currentDetail.checkInNo }}</el-descriptions-item>
            <el-descriptions-item label="房型">{{ currentDetail.roomTypeName }}</el-descriptions-item>
            <el-descriptions-item label="入住日期" :span="2">
              {{ currentDetail.checkInDate }} ~ {{ currentDetail.checkOutDate }}
            </el-descriptions-item>
          </el-descriptions>
        </div>

        <div class="detail-section">
          <div class="section-title">投诉内容</div>
          <div class="content-text">{{ currentDetail.complaintContent }}</div>
        </div>

        <div v-if="currentDetail.expectedSolution" class="detail-section">
          <div class="section-title">期望解决方案</div>
          <div class="content-text">{{ currentDetail.expectedSolution }}</div>
        </div>

        <div v-if="currentDetail.images && currentDetail.images.length > 0" class="detail-section">
          <div class="section-title">证据图片</div>
          <div class="image-list">
            <el-image
              v-for="(img, idx) in currentDetail.images"
              :key="idx"
              :src="img.imageUrl"
              :preview-src-list="currentDetail.images.map(i => i.imageUrl)"
              fit="cover"
              class="preview-image"
              style="width: 100px; height: 100px; margin-right: 8px; margin-bottom: 8px; border-radius: 4px;"
            />
          </div>
        </div>

        <div v-if="currentDetail.complaintStatus >= 2" class="detail-section">
          <div class="section-title">受理信息</div>
          <el-descriptions :column="2" border size="small">
            <el-descriptions-item label="受理人">{{ currentDetail.acceptUserName }}</el-descriptions-item>
            <el-descriptions-item label="受理时间">{{ currentDetail.acceptTime }}</el-descriptions-item>
            <el-descriptions-item label="责任人">{{ currentDetail.assignUserName }}</el-descriptions-item>
            <el-descriptions-item label="受理意见" :span="2">{{ currentDetail.acceptRemark }}</el-descriptions-item>
          </el-descriptions>
        </div>

        <div v-if="currentDetail.complaintStatus === 4" class="detail-section">
          <div class="section-title">驳回信息</div>
          <el-descriptions :column="2" border size="small">
            <el-descriptions-item label="驳回原因">{{ getRejectReasonLabel(currentDetail.rejectReason) }}</el-descriptions-item>
            <el-descriptions-item label="驳回人">{{ currentDetail.rejectUserName }}</el-descriptions-item>
            <el-descriptions-item label="驳回时间" :span="2">{{ currentDetail.rejectTime }}</el-descriptions-item>
            <el-descriptions-item label="详细说明" :span="2">{{ currentDetail.rejectRemark }}</el-descriptions-item>
          </el-descriptions>
        </div>

        <div v-if="currentDetail.complaintStatus === 3" class="detail-section">
          <div class="section-title">处理信息</div>
          <el-descriptions :column="2" border size="small">
            <el-descriptions-item label="处理结果">{{ getHandleResultLabel(currentDetail.handleResult) }}</el-descriptions-item>
            <el-descriptions-item label="处理人">{{ currentDetail.handleUserName }}</el-descriptions-item>
            <el-descriptions-item label="处理时间" :span="2">{{ currentDetail.handleTime }}</el-descriptions-item>
            <el-descriptions-item label="处理方案" :span="2">{{ currentDetail.handleSolution }}</el-descriptions-item>
            <el-descriptions-item label="补偿方案" :span="2">{{ currentDetail.compensationPlan || '-' }}</el-descriptions-item>
            <el-descriptions-item label="处理备注" :span="2">{{ currentDetail.handleRemark || '-' }}</el-descriptions-item>
          </el-descriptions>
        </div>

        <div v-if="currentDetail.visits && currentDetail.visits.length > 0" class="detail-section">
          <div class="section-title">回访历史</div>
          <el-timeline>
            <el-timeline-item
              v-for="(visit, idx) in currentDetail.visits"
              :key="idx"
              :timestamp="visit.visitTime"
              :type="getSatisfactionType(visit.satisfaction)"
            >
              <el-card shadow="never" size="small">
                <div class="visit-item">
                  <div class="visit-header">
                    <span class="visit-user">{{ visit.visitUserName }}</span>
                    <span class="visit-method">{{ getVisitMethodLabel(visit.visitMethod) }}</span>
                    <el-tag :type="getSatisfactionTagType(visit.satisfaction)" size="small">
                      {{ getSatisfactionLabel(visit.satisfaction) }}
                    </el-tag>
                  </div>
                  <div class="visit-remark">{{ visit.visitRemark }}</div>
                  <div v-if="visit.needReprocess === 1" class="visit-reprocess">
                    <el-tag type="danger" size="small">需再处理</el-tag>
                  </div>
                </div>
              </el-card>
            </el-timeline-item>
          </el-timeline>
        </div>

        <div v-if="currentDetail.complaintStatus === 3 && hasPermission('complaint:visit')" class="detail-footer">
          <el-button type="primary" @click="handleVisit(currentDetail)">
            <el-icon><ChatDotRound /></el-icon>添加回访
          </el-button>
        </div>
      </div>
    </el-drawer>

    <el-dialog
      v-model="acceptVisible"
      title="受理投诉"
      width="500px"
      :destroy-on-close="true"
    >
      <el-form :model="acceptForm" label-width="90px" ref="acceptFormRef">
        <el-form-item label="分配责任人" prop="assignUserId">
          <el-select v-model="acceptForm.assignUserId" placeholder="请选择责任人" style="width: 100%">
            <el-option
              v-for="user in staffList"
              :key="user.id"
              :label="user.nickname || user.username"
              :value="user.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="受理意见" prop="acceptRemark">
          <el-input
            v-model="acceptForm.acceptRemark"
            type="textarea"
            :rows="4"
            placeholder="请填写受理意见（最多200字）"
            maxlength="200"
            show-word-limit
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="acceptVisible = false">取消</el-button>
        <el-button type="primary" :loading="acceptLoading" @click="submitAccept">确认受理</el-button>
      </template>
    </el-dialog>

    <el-dialog
      v-model="rejectVisible"
      title="驳回投诉"
      width="500px"
      :destroy-on-close="true"
    >
      <el-form :model="rejectForm" label-width="90px" ref="rejectFormRef">
        <el-form-item label="驳回原因" prop="rejectReason">
          <el-select v-model="rejectForm.rejectReason" placeholder="请选择驳回原因" style="width: 100%">
            <el-option label="投诉不成立" :value="1" />
            <el-option label="信息不完整" :value="2" />
            <el-option label="超出受理范围" :value="3" />
            <el-option label="其他" :value="4" />
          </el-select>
        </el-form-item>
        <el-form-item label="详细说明" prop="rejectRemark">
          <el-input
            v-model="rejectForm.rejectRemark"
            type="textarea"
            :rows="4"
            placeholder="请填写详细说明（最多200字）"
            maxlength="200"
            show-word-limit
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="rejectVisible = false">取消</el-button>
        <el-button type="danger" :loading="rejectLoading" @click="submitReject">确认驳回</el-button>
      </template>
    </el-dialog>

    <el-dialog
      v-model="handleVisible"
      title="处理投诉"
      width="600px"
      :destroy-on-close="true"
    >
      <el-form :model="handleForm" label-width="100px" ref="handleFormRef">
        <el-form-item label="处理方案" prop="handleSolution">
          <el-input
            v-model="handleForm.handleSolution"
            type="textarea"
            :rows="4"
            placeholder="请详细描述采取的处理措施（最多500字）"
            maxlength="500"
            show-word-limit
          />
        </el-form-item>
        <el-form-item label="处理结果" prop="handleResult">
          <el-select v-model="handleForm.handleResult" placeholder="请选择处理结果" style="width: 100%">
            <el-option label="已解决：问题已处理，客人满意" :value="1" />
            <el-option label="部分解决：问题部分处理" :value="2" />
            <el-option label="无法解决：客观原因无法解决" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item label="补偿方案">
          <el-input
            v-model="handleForm.compensationPlan"
            placeholder="如：赠送优惠券、退还部分费用、免费升级房型（最多100字）"
            maxlength="100"
            show-word-limit
          />
        </el-form-item>
        <el-form-item label="处理备注">
          <el-input
            v-model="handleForm.handleRemark"
            type="textarea"
            :rows="2"
            placeholder="其他说明（最多200字）"
            maxlength="200"
            show-word-limit
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="handleVisible = false">取消</el-button>
        <el-button type="primary" :loading="handleLoading" @click="submitHandle">提交处理结果</el-button>
      </template>
    </el-dialog>

    <el-dialog
      v-model="visitVisible"
      title="投诉回访"
      width="500px"
      :destroy-on-close="true"
    >
      <el-form :model="visitForm" label-width="100px" ref="visitFormRef">
        <el-form-item label="回访时间">
          <el-date-picker
            v-model="visitForm.visitTime"
            type="datetime"
            placeholder="选择回访时间"
            value-format="YYYY-MM-DDTHH:mm:ss"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="回访方式" prop="visitMethod">
          <el-radio-group v-model="visitForm.visitMethod">
            <el-radio :value="1">电话</el-radio>
            <el-radio :value="2">短信</el-radio>
            <el-radio :value="3">邮件</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="客人满意度" prop="satisfaction">
          <el-radio-group v-model="visitForm.satisfaction">
            <el-radio :value="1">满意</el-radio>
            <el-radio :value="2">基本满意</el-radio>
            <el-radio :value="3">不满意</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="回访备注" prop="visitRemark">
          <el-input
            v-model="visitForm.visitRemark"
            type="textarea"
            :rows="4"
            placeholder="请填写回访备注（最多200字）"
            maxlength="200"
            show-word-limit
          />
        </el-form-item>
        <el-form-item v-if="visitForm.satisfaction === 3" label="需再处理">
          <el-switch v-model="visitForm.needReprocess" />
          <span class="form-tip">客人不满意时，可标记为需再处理</span>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="visitVisible = false">取消</el-button>
        <el-button type="primary" :loading="visitLoading" @click="submitVisit">提交回访</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh, ChatDotRound, User } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import api from '@/api'

const userStore = useUserStore()

const hasPermission = (permission) => userStore.hasPermission(permission)

const activeTab = ref('pending')
const tableLoading = ref(false)
const tableData = ref([])
const searchForm = reactive({
  keyword: '',
  complaintType: null,
  dateRange: [],
  myTask: false
})
const pagination = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0
})

const detailVisible = ref(false)
const currentDetail = ref(null)

const acceptVisible = ref(false)
const acceptLoading = ref(false)
const acceptFormRef = ref(null)
const acceptForm = reactive({
  assignUserId: null,
  acceptRemark: ''
})
const staffList = ref([])

const rejectVisible = ref(false)
const rejectLoading = ref(false)
const rejectFormRef = ref(null)
const rejectForm = reactive({
  rejectReason: null,
  rejectRemark: ''
})

const handleVisible = ref(false)
const handleLoading = ref(false)
const handleFormRef = ref(null)
const handleForm = reactive({
  handleSolution: '',
  handleResult: null,
  compensationPlan: '',
  handleRemark: ''
})

const visitVisible = ref(false)
const visitLoading = ref(false)
const visitFormRef = ref(null)
const visitForm = reactive({
  visitTime: null,
  visitMethod: null,
  satisfaction: null,
  visitRemark: '',
  needReprocess: 0
})

const currentRowId = ref(null)

const complaintTypeOptions = [
  { value: 1, label: '服务质量问题' },
  { value: 2, label: '设施设备问题' },
  { value: 3, label: '卫生问题' },
  { value: 4, label: '安全问题' },
  { value: 5, label: '价格收费问题' },
  { value: 6, label: '员工态度问题' },
  { value: 7, label: '噪音问题' },
  { value: 8, label: '其他' }
]

const getStatusMap = () => ({
  1: { label: '待处理', type: 'warning' },
  2: { label: '处理中', type: 'primary' },
  3: { label: '已处理', type: 'success' },
  4: { label: '已驳回', type: 'info' }
})

const getStatusLabel = (status) => getStatusMap()[status]?.label || '-'
const getStatusTagType = (status) => getStatusMap()[status]?.type || ''

const getComplaintTypeLabel = (type) => complaintTypeOptions.find(t => t.value === type)?.label || '-'
const getComplaintTypeTagType = (type) => {
  const map = { 1: '', 2: 'warning', 3: 'danger', 4: 'danger', 5: 'warning', 6: 'warning', 7: 'info', 8: '' }
  return map[type] || ''
}

const getRejectReasonLabel = (reason) => {
  const map = { 1: '投诉不成立', 2: '信息不完整', 3: '超出受理范围', 4: '其他' }
  return map[reason] || '-'
}

const getHandleResultLabel = (result) => {
  const map = { 1: '已解决', 2: '部分解决', 3: '无法解决' }
  return map[result] || '-'
}

const getVisitMethodLabel = (method) => {
  const map = { 1: '电话', 2: '短信', 3: '邮件' }
  return map[method] || '-'
}

const getSatisfactionLabel = (s) => {
  const map = { 1: '满意', 2: '基本满意', 3: '不满意' }
  return map[s] || '-'
}

const getSatisfactionTagType = (s) => {
  const map = { 1: 'success', 2: 'warning', 3: 'danger' }
  return map[s] || ''
}

const getSatisfactionType = (s) => {
  const map = { 1: 'success', 2: 'warning', 3: 'danger' }
  return map[s] || 'primary'
}

const isMyTask = (row) => {
  if (!row.assignUserId) return true
  return String(row.assignUserId) === String(userStore.user?.id)
}

const tabToStatus = {
  pending: 1,
  processing: 2,
  handled: 3,
  rejected: 4
}

const loadTableData = async () => {
  tableLoading.value = true
  try {
    const params = {
      complaintStatus: tabToStatus[activeTab.value],
      keyword: searchForm.keyword || undefined,
      complaintType: searchForm.complaintType || undefined,
      startDate: searchForm.dateRange?.[0] || undefined,
      endDate: searchForm.dateRange?.[1] || undefined,
      myTask: searchForm.myTask || undefined,
      pageNum: pagination.pageNum,
      pageSize: pagination.pageSize
    }
    const res = await api.complaint.getPage(params)
    if (res.code === 200) {
      tableData.value = res.data.list || []
      pagination.total = res.data.total || 0
    } else {
      ElMessage.error(res.message || '获取投诉列表失败')
    }
  } catch (error) {
    console.error('获取投诉列表失败:', error)
    tableData.value = []
    pagination.total = 0
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
  searchForm.complaintType = null
  searchForm.dateRange = []
  searchForm.myTask = false
  pagination.pageNum = 1
  loadTableData()
}

const handleTabChange = () => {
  pagination.pageNum = 1
  loadTableData()
}

const handleSizeChange = (size) => {
  pagination.pageSize = size
  loadTableData()
}

const handleCurrentChange = (page) => {
  pagination.pageNum = page
  loadTableData()
}

const handleViewDetail = async (row) => {
  try {
    const res = await api.complaint.getDetail(row.id)
    if (res.code === 200) {
      currentDetail.value = res.data
      detailVisible.value = true
    }
  } catch (e) {}
}

const loadStaffList = async () => {
  try {
    const res = await api.complaint.getStaffList()
    if (res.code === 200) {
      staffList.value = res.data || []
    }
  } catch (e) {}
}

const handleAccept = (row) => {
  currentRowId.value = row.id
  acceptForm.assignUserId = null
  acceptForm.acceptRemark = ''
  loadStaffList()
  acceptVisible.value = true
}

const submitAccept = async () => {
  if (!acceptForm.assignUserId) {
    ElMessage.warning('请选择责任人')
    return
  }
  if (!acceptForm.acceptRemark || acceptForm.acceptRemark.trim() === '') {
    ElMessage.warning('请填写受理意见')
    return
  }
  acceptLoading.value = true
  try {
    const res = await api.complaint.accept(currentRowId.value, {
      assignUserId: acceptForm.assignUserId,
      acceptRemark: acceptForm.acceptRemark
    })
    if (res.code === 200) {
      ElMessage.success('受理成功')
      acceptVisible.value = false
      loadTableData()
    }
  } finally {
    acceptLoading.value = false
  }
}

const handleReject = (row) => {
  currentRowId.value = row.id
  rejectForm.rejectReason = null
  rejectForm.rejectRemark = ''
  rejectVisible.value = true
}

const submitReject = async () => {
  if (!rejectForm.rejectReason) {
    ElMessage.warning('请选择驳回原因')
    return
  }
  if (!rejectForm.rejectRemark || rejectForm.rejectRemark.trim() === '') {
    ElMessage.warning('请填写详细说明')
    return
  }
  rejectLoading.value = true
  try {
    const res = await api.complaint.reject(currentRowId.value, {
      rejectReason: rejectForm.rejectReason,
      rejectRemark: rejectForm.rejectRemark
    })
    if (res.code === 200) {
      ElMessage.success('驳回成功')
      rejectVisible.value = false
      loadTableData()
    }
  } finally {
    rejectLoading.value = false
  }
}

const handleProcess = (row) => {
  currentRowId.value = row.id
  handleForm.handleSolution = ''
  handleForm.handleResult = null
  handleForm.compensationPlan = ''
  handleForm.handleRemark = ''
  handleVisible.value = true
}

const submitHandle = async () => {
  if (!handleForm.handleSolution || handleForm.handleSolution.trim() === '') {
    ElMessage.warning('请填写处理方案')
    return
  }
  if (!handleForm.handleResult) {
    ElMessage.warning('请选择处理结果')
    return
  }
  handleLoading.value = true
  try {
    const res = await api.complaint.handle(currentRowId.value, {
      handleSolution: handleForm.handleSolution,
      handleResult: handleForm.handleResult,
      compensationPlan: handleForm.compensationPlan,
      handleRemark: handleForm.handleRemark
    })
    if (res.code === 200) {
      ElMessage.success('处理结果已提交')
      handleVisible.value = false
      loadTableData()
    }
  } finally {
    handleLoading.value = false
  }
}

const handleVisit = (row) => {
  currentRowId.value = row.id
  visitForm.visitTime = new Date().toISOString().slice(0, 19)
  visitForm.visitMethod = null
  visitForm.satisfaction = null
  visitForm.visitRemark = ''
  visitForm.needReprocess = 0
  detailVisible.value = false
  visitVisible.value = true
}

const submitVisit = async () => {
  if (!visitForm.visitMethod) {
    ElMessage.warning('请选择回访方式')
    return
  }
  if (!visitForm.satisfaction) {
    ElMessage.warning('请选择客人满意度')
    return
  }
  if (!visitForm.visitRemark || visitForm.visitRemark.trim() === '') {
    ElMessage.warning('请填写回访备注')
    return
  }
  visitLoading.value = true
  try {
    const res = await api.complaint.visit(currentRowId.value, {
      visitTime: visitForm.visitTime,
      visitMethod: visitForm.visitMethod,
      satisfaction: visitForm.satisfaction,
      visitRemark: visitForm.visitRemark,
      needReprocess: visitForm.satisfaction === 3 ? visitForm.needReprocess : 0
    })
    if (res.code === 200) {
      ElMessage.success('回访记录已保存')
      visitVisible.value = false
      loadTableData()
    }
  } finally {
    visitLoading.value = false
  }
}

onMounted(() => {
  loadTableData()
})
</script>

<style scoped>
.complaint-manage-container {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.search-card :deep(.el-card__body) {
  padding: 16px 20px 0;
}

.search-form {
  display: flex;
  flex-wrap: wrap;
}

.table-card :deep(.el-card__body) {
  padding: 16px 20px;
}

.pagination-wrapper {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}

.detail-container {
  padding: 0 8px;
}

.detail-section {
  margin-top: 20px;
}

.section-title {
  font-size: 15px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 12px;
  padding-left: 8px;
  border-left: 3px solid #409eff;
}

.content-text {
  padding: 12px 16px;
  background: #f5f7fa;
  border-radius: 6px;
  line-height: 1.8;
  color: #606266;
}

.image-list {
  display: flex;
  flex-wrap: wrap;
}

.detail-footer {
  margin-top: 24px;
  padding-top: 16px;
  border-top: 1px solid #ebeef5;
  text-align: right;
}

.visit-item {
  padding: 4px 0;
}

.visit-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 8px;
}

.visit-user {
  font-weight: 600;
  color: #303133;
}

.visit-method {
  color: #909399;
  font-size: 13px;
}

.visit-remark {
  color: #606266;
  line-height: 1.6;
}

.visit-reprocess {
  margin-top: 8px;
}

.form-tip {
  margin-left: 8px;
  font-size: 12px;
  color: #909399;
}
</style>
