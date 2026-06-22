<template>
  <div class="review-audit-container">
    <el-card shadow="never" class="search-card">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="关键词">
          <el-input
            v-model="searchForm.keyword"
            placeholder="客人姓名/手机号"
            clearable
            style="width: 220px"
            @keyup.enter="handleSearch"
          />
        </el-form-item>
        <el-form-item label="评分范围">
          <el-select v-model="searchForm.scoreFilter" placeholder="全部星级" clearable style="width: 140px">
            <el-option label="5星" :value="5" />
            <el-option label="4星" :value="4" />
            <el-option label="3星" :value="3" />
            <el-option label="2星" :value="2" />
            <el-option label="1星" :value="1" />
          </el-select>
        </el-form-item>
        <el-form-item label="评价时间">
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
        <el-tab-pane label="待审核" name="pending">
          <span slot="label">
            <el-badge :value="pendingCount" :hidden="pendingCount === 0" class="tab-badge">
              待审核
            </el-badge>
          </span>
        </el-tab-pane>
        <el-tab-pane label="已通过" name="approved" />
        <el-tab-pane label="已拒绝" name="rejected" />
        <el-tab-pane label="已隐藏" name="hidden" />
      </el-tabs>

      <el-table :data="tableData" stripe border v-loading="tableLoading" style="width: 100%">
        <el-table-column prop="reviewTime" label="评价时间" width="170" align="center" />
        <el-table-column prop="customerName" label="客人姓名" width="100">
          <template #default="{ row }">
            <span v-if="row.isAnonymous === 1">
              <el-tag size="small" type="info">匿名</el-tag>
            </span>
            <span v-else>{{ row.customerName }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="customerPhone" label="手机号" width="130" />
        <el-table-column prop="roomTypeName" label="房型" width="110" />
        <el-table-column label="入住日期" width="200" align="center">
          <template #default="{ row }">
            {{ row.checkInDate }} ~ {{ row.checkOutDate }}
          </template>
        </el-table-column>
        <el-table-column label="综合评分" width="140" align="center">
          <template #default="{ row }">
            <div class="score-cell">
              <el-rate
                :model-value="Number(row.overallScore)"
                disabled
                size="small"
                :max="5"
              />
              <span class="score-text">{{ row.overallScore }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="评价内容" min-width="200" show-overflow-tooltip>
          <template #default="{ row }">
            <span>{{ truncateText(row.reviewContent, 30) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="图片" width="70" align="center">
          <template #default="{ row }">
            <el-tag v-if="row.images && row.images.length > 0" type="success" size="small" effect="plain">有</el-tag>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column label="审核状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="getStatusTagType(row.reviewStatus)" size="small">
              {{ getStatusText(row.reviewStatus) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="300" align="center" fixed="right">
          <template #default="{ row }">
            <el-button
              type="primary"
              link
              size="small"
              @click="handleViewDetail(row)"
            >查看详情</el-button>

            <template v-if="row.reviewStatus === 0">
              <el-button
                v-if="hasPermission('review:audit:approve')"
                type="success"
                link
                size="small"
                @click="handleApprove(row)"
              >通过</el-button>
              <el-button
                v-if="hasPermission('review:audit:reject')"
                type="danger"
                link
                size="small"
                @click="handleReject(row)"
              >拒绝</el-button>
              <el-button
                v-if="hasPermission('review:audit:hide')"
                type="warning"
                link
                size="small"
                @click="handleHide(row)"
              >隐藏</el-button>
            </template>

            <template v-else-if="row.reviewStatus === 2 || row.reviewStatus === 3">
              <el-button
                v-if="hasPermission('review:audit:viewReason')"
                type="info"
                link
                size="small"
                @click="handleViewReason(row)"
              >查看原因</el-button>
            </template>

            <template v-if="row.reviewStatus === 3">
              <el-button
                v-if="hasPermission('review:audit:approve')"
                type="success"
                link
                size="small"
                @click="handleReapprove(row)"
              >重新通过</el-button>
            </template>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:current-page="pagination.pageNum"
        v-model:page-size="pagination.pageSize"
        :page-sizes="[10, 20, 50, 100]"
        :total="pagination.total"
        layout="total, sizes, prev, pager, next, jumper"
        background
        @size-change="fetchList"
        @current-change="fetchList"
        class="pagination"
      />
    </el-card>

    <el-drawer
      v-model="detailDrawerVisible"
      title="评价详情"
      direction="rtl"
      size="620px"
      destroy-on-close
    >
      <div v-if="currentDetail" class="review-detail">
        <div class="detail-section">
          <div class="section-title">
            <el-icon><User /></el-icon>客人信息
          </div>
          <el-descriptions :column="2" size="small" border>
            <el-descriptions-item label="姓名">
              <span v-if="currentDetail.review.isAnonymous === 1">匿名用户</span>
              <span v-else>{{ currentDetail.review.customerName }}</span>
            </el-descriptions-item>
            <el-descriptions-item label="手机号">
              {{ currentDetail.review.isAnonymous === 1 ? '***' : currentDetail.review.customerPhone }}
            </el-descriptions-item>
            <el-descriptions-item label="会员等级" :span="2">
              <el-tag v-if="currentDetail.member" type="warning" size="small">
                {{ currentDetail.member.levelName }}
              </el-tag>
              <span v-else>非会员</span>
            </el-descriptions-item>
          </el-descriptions>
        </div>

        <div class="detail-section">
          <div class="section-title">
            <el-icon><Calendar /></el-icon>入住信息
          </div>
          <el-descriptions :column="2" size="small" border>
            <el-descriptions-item label="房型">{{ currentDetail.review.roomTypeName }}</el-descriptions-item>
            <el-descriptions-item label="入住单号">{{ currentDetail.review.checkInNo }}</el-descriptions-item>
            <el-descriptions-item label="入住日期" :span="2">
              {{ currentDetail.review.checkInDate }} 至 {{ currentDetail.review.checkOutDate }}
            </el-descriptions-item>
          </el-descriptions>
        </div>

        <div class="detail-section">
          <div class="section-title">
            <el-icon><Star /></el-icon>评分信息
          </div>
          <div class="overall-score-box">
            <div class="overall-score-big">{{ currentDetail.review.overallScore }}</div>
            <div class="overall-score-label">
              <el-rate :model-value="Number(currentDetail.review.overallScore)" disabled :max="5" />
              <div class="score-subtitle">综合评分</div>
            </div>
          </div>
          <el-descriptions :column="2" size="small" border class="metric-scores">
            <el-descriptions-item
              v-for="ms in currentDetail.review.metricScores"
              :key="ms.id"
              :label="ms.metricName"
            >
              <el-rate :model-value="ms.score" disabled size="small" :max="5" />
              <span class="metric-score-num">{{ ms.score }}分</span>
            </el-descriptions-item>
          </el-descriptions>
        </div>

        <div v-if="currentDetail.tags && currentDetail.tags.length > 0" class="detail-section">
          <div class="section-title">
            <el-icon><PriceTag /></el-icon>评价标签
          </div>
          <div class="tag-list">
            <el-tag
              v-for="tag in currentDetail.tags"
              :key="tag.id"
              :type="tag.tagType === 1 ? 'success' : 'danger'"
              effect="light"
              style="margin-right: 8px; margin-bottom: 8px;"
            >
              {{ tag.tagText }}
            </el-tag>
          </div>
        </div>

        <div class="detail-section">
          <div class="section-title">
            <el-icon><EditPen /></el-icon>评价内容
          </div>
          <div class="content-box">
            <p v-if="currentDetail.review.reviewContent">{{ currentDetail.review.reviewContent }}</p>
            <span v-else class="text-muted">无文字评价</span>
          </div>
        </div>

        <div v-if="currentDetail.review.images && currentDetail.review.images.length > 0" class="detail-section">
          <div class="section-title">
            <el-icon><Picture /></el-icon>评价图片
          </div>
          <el-image-viewer
            v-if="imageViewerVisible"
            :url-list="imageList"
            :initial-index="imageViewerIndex"
            @close="imageViewerVisible = false"
          />
          <div class="image-list">
            <el-image
              v-for="(img, idx) in currentDetail.review.images"
              :key="img.id"
              :src="img.imageUrl"
              :preview-src-list="imageList"
              :initial-index="idx"
              fit="cover"
              class="review-image-thumb"
              @click="openImageViewer(idx)"
              preview-teleported
            />
          </div>
        </div>

        <div class="detail-section">
          <div class="section-title">
            <el-icon><InfoFilled /></el-icon>其他信息
          </div>
          <el-descriptions :column="2" size="small" border>
            <el-descriptions-item label="是否匿名">
              {{ currentDetail.review.isAnonymous === 1 ? '是' : '否' }}
            </el-descriptions-item>
            <el-descriptions-item label="评价时间">
              {{ currentDetail.review.reviewTime }}
            </el-descriptions-item>
            <el-descriptions-item label="审核状态">
              <el-tag :type="getStatusTagType(currentDetail.review.reviewStatus)" size="small">
                {{ getStatusText(currentDetail.review.reviewStatus) }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item v-if="currentDetail.review.auditTime" label="审核时间">
              {{ currentDetail.review.auditTime }}
            </el-descriptions-item>
            <el-descriptions-item v-if="currentDetail.review.auditorName" label="审核人">
              {{ currentDetail.review.auditorName }}
            </el-descriptions-item>
            <el-descriptions-item
              v-if="currentDetail.review.reviewStatus === 2 || currentDetail.review.reviewStatus === 3"
              label="原因"
              :span="2"
            >
              <span class="reason-text">{{ currentDetail.review.auditRemark }}</span>
            </el-descriptions-item>
          </el-descriptions>
        </div>

        <div v-if="currentDetail.review.replyContent" class="detail-section">
          <div class="section-title">
            <el-icon><ChatLineSquare /></el-icon>酒店回复
          </div>
          <div class="reply-box">
            <div class="reply-header">
              <strong>酒店回复</strong>
              <span class="reply-time">{{ currentDetail.review.replyTime }}</span>
              <span class="reply-by" v-if="currentDetail.review.repliedName">
                · {{ currentDetail.review.repliedName }}
              </span>
            </div>
            <div class="reply-content">{{ currentDetail.review.replyContent }}</div>
          </div>
        </div>

        <div class="detail-actions">
          <template v-if="currentDetail.review.reviewStatus === 0">
            <el-button
              v-if="hasPermission('review:audit:approve')"
              type="success"
              @click="handleApprove(currentDetail.review)"
            >通过审核</el-button>
            <el-button
              v-if="hasPermission('review:audit:reject')"
              type="danger"
              @click="handleReject(currentDetail.review)"
            >拒绝审核</el-button>
            <el-button
              v-if="hasPermission('review:audit:hide')"
              type="warning"
              @click="handleHide(currentDetail.review)"
            >隐藏评价</el-button>
          </template>
          <template v-else-if="currentDetail.review.reviewStatus === 3">
            <el-button
              v-if="hasPermission('review:audit:approve')"
              type="success"
              @click="handleReapprove(currentDetail.review)"
            >重新审核通过</el-button>
          </template>

          <template v-if="currentDetail.review.reviewStatus === 1">
            <el-button
              v-if="!currentDetail.review.replyContent && hasPermission('review:reply:add')"
              type="primary"
              @click="handleReply(currentDetail.review)"
            >回复评价</el-button>
            <el-button
              v-else-if="currentDetail.review.replyContent && hasPermission('review:reply:edit')"
              type="warning"
              @click="handleEditReply(currentDetail.review)"
            >编辑回复</el-button>
          </template>
        </div>
      </div>
    </el-drawer>

    <el-dialog v-model="rejectDialogVisible" title="拒绝审核" width="480px" destroy-on-close>
      <el-form :model="rejectForm" :rules="rejectRules" ref="rejectFormRef" label-width="100px">
        <el-form-item label="拒绝原因" prop="reasonType">
          <el-select v-model="rejectForm.reasonType" placeholder="请选择原因类型" style="width: 100%">
            <el-option label="广告信息" value="1" />
            <el-option label="恶意差评" value="2" />
            <el-option label="违规内容" value="3" />
            <el-option label="其他" value="4" />
          </el-select>
        </el-form-item>
        <el-form-item label="详细说明" prop="remark">
          <el-input
            v-model="rejectForm.remark"
            type="textarea"
            :rows="4"
            placeholder="请填写详细说明（必填）"
            maxlength="200"
            show-word-limit
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="rejectDialogVisible = false">取消</el-button>
        <el-button type="danger" @click="confirmReject">确认拒绝</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="hideDialogVisible" title="隐藏评价" width="480px" destroy-on-close>
      <el-form :model="hideForm" :rules="hideRules" ref="hideFormRef" label-width="100px">
        <el-form-item label="隐藏原因" prop="remark">
          <el-input
            v-model="hideForm.remark"
            type="textarea"
            :rows="4"
            placeholder="请填写隐藏原因（必填）"
            maxlength="200"
            show-word-limit
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="hideDialogVisible = false">取消</el-button>
        <el-button type="warning" @click="confirmHide">确认隐藏</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="replyDialogVisible" :title="isEditReply ? '编辑回复' : '回复评价'" width="520px" destroy-on-close>
      <el-form :model="replyForm" :rules="replyRules" ref="replyFormRef" label-width="80px">
        <el-form-item label="回复内容" prop="replyContent">
          <el-input
            v-model="replyForm.replyContent"
            type="textarea"
            :rows="5"
            placeholder="请输入回复内容（最多300字）"
            maxlength="300"
            show-word-limit
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="replyDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmReply">
          {{ isEditReply ? '更新回复' : '提交回复' }}
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh, User, Calendar, Star, PriceTag, EditPen, Picture, InfoFilled, ChatLineSquare } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import api from '@/api'

const userStore = useUserStore()
const hasPermission = (perm) => userStore.hasPermission(perm)

const activeTab = ref('pending')
const tableLoading = ref(false)
const pendingCount = ref(0)

const statusMap = {
  pending: 0,
  approved: 1,
  rejected: 2,
  hidden: 3
}

const searchForm = reactive({
  keyword: '',
  scoreFilter: null,
  dateRange: []
})

const pagination = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0
})

const tableData = ref([])

const detailDrawerVisible = ref(false)
const currentDetail = ref(null)
const imageViewerVisible = ref(false)
const imageViewerIndex = ref(0)
const imageList = computed(() => {
  if (!currentDetail.value || !currentDetail.value.review.images) return []
  return currentDetail.value.review.images.map(img => img.imageUrl)
})

const rejectDialogVisible = ref(false)
const rejectFormRef = ref(null)
const rejectForm = reactive({
  reasonType: '',
  remark: ''
})
const rejectRules = {
  reasonType: [{ required: true, message: '请选择拒绝原因', trigger: 'change' }],
  remark: [{ required: true, message: '请填写详细说明', trigger: 'blur' }]
}
const currentRejectId = ref(null)

const hideDialogVisible = ref(false)
const hideFormRef = ref(null)
const hideForm = reactive({ remark: '' })
const hideRules = {
  remark: [{ required: true, message: '请填写隐藏原因', trigger: 'blur' }]
}
const currentHideId = ref(null)

const replyDialogVisible = ref(false)
const isEditReply = ref(false)
const replyFormRef = ref(null)
const replyForm = reactive({ replyContent: '' })
const replyRules = {
  replyContent: [
    { required: true, message: '请填写回复内容', trigger: 'blur' },
    { max: 300, message: '回复内容不能超过300字', trigger: 'blur' }
  ]
}
const currentReplyId = ref(null)

const getStatusText = (status) => {
  const map = { 0: '待审核', 1: '已通过', 2: '已拒绝', 3: '已隐藏' }
  return map[status] || '未知'
}

const getStatusTagType = (status) => {
  const map = { 0: 'warning', 1: 'success', 2: 'danger', 3: 'info' }
  return map[status] || ''
}

const truncateText = (text, maxLen) => {
  if (!text) return '-'
  if (text.length <= maxLen) return text
  return text.substring(0, maxLen) + '...'
}

const openImageViewer = (idx) => {
  imageViewerIndex.value = idx
  imageViewerVisible.value = true
}

const handleTabChange = () => {
  pagination.pageNum = 1
  fetchList()
}

const handleSearch = () => {
  pagination.pageNum = 1
  fetchList()
}

const handleReset = () => {
  searchForm.keyword = ''
  searchForm.scoreFilter = null
  searchForm.dateRange = []
  pagination.pageNum = 1
  fetchList()
}

const fetchList = async () => {
  tableLoading.value = true
  try {
    const params = {
      reviewStatus: statusMap[activeTab.value],
      pageNum: pagination.pageNum,
      pageSize: pagination.pageSize
    }
    if (searchForm.keyword) params.keyword = searchForm.keyword
    if (searchForm.scoreFilter) params.scoreFilter = searchForm.scoreFilter
    if (searchForm.dateRange && searchForm.dateRange.length === 2) {
      params.startDate = searchForm.dateRange[0]
      params.endDate = searchForm.dateRange[1]
    }
    const res = await api.reviewAudit.getPage(params)
    tableData.value = res.data.list || []
    pagination.total = res.data.total || 0

    if (activeTab.value === 'pending') {
      pendingCount.value = res.data.total || 0
    }
  } catch (e) {
    console.error(e)
  } finally {
    tableLoading.value = false
  }
}

const handleViewDetail = async (row) => {
  try {
    const res = await api.reviewAudit.getDetail(row.id)
    currentDetail.value = res.data
    detailDrawerVisible.value = true
  } catch (e) {
    console.error(e)
  }
}

const handleApprove = async (row) => {
  try {
    await ElMessageBox.confirm('确认通过该评价的审核？通过后评价将公开展示。', '确认通过', {
      type: 'success',
      confirmButtonText: '确认通过',
      cancelButtonText: '取消'
    })
    await api.reviewAudit.approve(row.id)
    ElMessage.success('审核通过成功')
    fetchList()
    if (detailDrawerVisible.value) handleViewDetail(row)
  } catch (e) {
    if (e !== 'cancel') console.error(e)
  }
}

const handleReject = (row) => {
  currentRejectId.value = row.id
  rejectForm.reasonType = ''
  rejectForm.remark = ''
  rejectDialogVisible.value = true
}

const confirmReject = async () => {
  if (!rejectFormRef.value) return
  await rejectFormRef.value.validate(async (valid) => {
    if (!valid) return
    try {
      await api.reviewAudit.reject(currentRejectId.value, {
        reasonType: rejectForm.reasonType,
        remark: rejectForm.remark
      })
      ElMessage.success('审核拒绝成功')
      rejectDialogVisible.value = false
      fetchList()
      if (detailDrawerVisible.value && currentDetail.value) {
        handleViewDetail(currentDetail.value.review)
      }
    } catch (e) {
      console.error(e)
    }
  })
}

const handleHide = (row) => {
  currentHideId.value = row.id
  hideForm.remark = ''
  hideDialogVisible.value = true
}

const confirmHide = async () => {
  if (!hideFormRef.value) return
  await hideFormRef.value.validate(async (valid) => {
    if (!valid) return
    try {
      await api.reviewAudit.hide(currentHideId.value, { remark: hideForm.remark })
      ElMessage.success('评价已隐藏')
      hideDialogVisible.value = false
      fetchList()
      if (detailDrawerVisible.value && currentDetail.value) {
        handleViewDetail(currentDetail.value.review)
      }
    } catch (e) {
      console.error(e)
    }
  })
}

const handleViewReason = (row) => {
  ElMessageBox.alert(row.auditRemark || '无', '审核原因', {
    confirmButtonText: '确定',
    type: 'info'
  })
}

const handleReapprove = async (row) => {
  try {
    await ElMessageBox.confirm('确认重新审核通过该评价？通过后评价将公开展示。', '确认通过', {
      type: 'success',
      confirmButtonText: '确认通过',
      cancelButtonText: '取消'
    })
    await api.reviewAudit.reapprove(row.id)
    ElMessage.success('重新审核通过成功')
    fetchList()
    if (detailDrawerVisible.value) handleViewDetail(row)
  } catch (e) {
    if (e !== 'cancel') console.error(e)
  }
}

const handleReply = (row) => {
  currentReplyId.value = row.id
  isEditReply.value = false
  replyForm.replyContent = ''
  replyDialogVisible.value = true
}

const handleEditReply = (row) => {
  currentReplyId.value = row.id
  isEditReply.value = true
  replyForm.replyContent = row.replyContent || ''
  replyDialogVisible.value = true
}

const confirmReply = async () => {
  if (!replyFormRef.value) return
  await replyFormRef.value.validate(async (valid) => {
    if (!valid) return
    try {
      if (isEditReply.value) {
        await api.reviewAudit.editReply(currentReplyId.value, { replyContent: replyForm.replyContent })
        ElMessage.success('回复已更新')
      } else {
        await api.reviewAudit.reply(currentReplyId.value, { replyContent: replyForm.replyContent })
        ElMessage.success('回复提交成功')
      }
      replyDialogVisible.value = false
      fetchList()
      if (detailDrawerVisible.value && currentDetail.value) {
        handleViewDetail(currentDetail.value.review)
      }
    } catch (e) {
      console.error(e)
    }
  })
}

onMounted(() => {
  fetchList()
})
</script>

<style lang="scss" scoped>
.review-audit-container {
  .search-card {
    margin-bottom: 16px;
  }

  .table-card {
    :deep(.el-tabs) {
      margin-bottom: 16px;
    }
  }

  .score-cell {
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 6px;

    .score-text {
      font-weight: bold;
      color: #f59e0b;
    }
  }

  .pagination {
    margin-top: 16px;
    display: flex;
    justify-content: flex-end;
  }
}

.review-detail {
  padding: 0 12px;

  .detail-section {
    margin-bottom: 24px;
  }

  .section-title {
    font-size: 15px;
    font-weight: 600;
    margin-bottom: 12px;
    color: #1f2937;
    display: flex;
    align-items: center;
    gap: 6px;
    padding-left: 8px;
    border-left: 3px solid #409eff;
  }

  .overall-score-box {
    display: flex;
    align-items: center;
    gap: 20px;
    padding: 16px;
    background: linear-gradient(135deg, #fef3c7, #fde68a);
    border-radius: 8px;
    margin-bottom: 16px;

    .overall-score-big {
      font-size: 48px;
      font-weight: bold;
      color: #d97706;
      line-height: 1;
    }

    .overall-score-label {
      .score-subtitle {
        font-size: 13px;
        color: #78716c;
        margin-top: 4px;
      }
    }
  }

  .metric-scores {
    margin-top: 8px;

    .metric-score-num {
      margin-left: 6px;
      font-size: 13px;
      color: #6b7280;
    }
  }

  .content-box {
    padding: 12px 16px;
    background: #f9fafb;
    border-radius: 6px;
    line-height: 1.7;

    p {
      margin: 0;
      white-space: pre-wrap;
    }

    .text-muted {
      color: #9ca3af;
    }
  }

  .image-list {
    display: flex;
    gap: 10px;
    flex-wrap: wrap;
  }

  .review-image-thumb {
    width: 100px;
    height: 100px;
    border-radius: 6px;
    cursor: pointer;
    border: 1px solid #e5e7eb;
  }

  .reason-text {
    color: #dc2626;
    font-size: 13px;
  }

  .reply-box {
    padding: 14px 16px;
    background: #eff6ff;
    border-left: 4px solid #3b82f6;
    border-radius: 6px;

    .reply-header {
      margin-bottom: 8px;
      font-size: 13px;
      display: flex;
      align-items: center;
      gap: 10px;
      color: #1e40af;

      .reply-time, .reply-by {
        color: #6b7280;
        font-weight: normal;
      }
    }

    .reply-content {
      line-height: 1.7;
      white-space: pre-wrap;
      color: #1f2937;
    }
  }

  .detail-actions {
    display: flex;
    gap: 10px;
    flex-wrap: wrap;
    padding-top: 16px;
    border-top: 1px solid #e5e7eb;
  }
}
</style>
