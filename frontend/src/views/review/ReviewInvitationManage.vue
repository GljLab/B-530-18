<template>
  <div class="review-invitation-container">
    <el-card shadow="never" class="search-card">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="关键词">
          <el-input
            v-model="searchForm.keyword"
            placeholder="客人姓名/手机号/入住单号"
            clearable
            style="width: 240px"
            @keyup.enter="handleSearch"
          />
        </el-form-item>
        <el-form-item label="评价状态">
          <el-select v-model="searchForm.reviewStatus" placeholder="全部" clearable style="width: 140px">
            <el-option label="待评价" :value="0" />
            <el-option label="已评价" :value="1" />
          </el-select>
        </el-form-item>
        <el-form-item label="日期范围">
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
      <el-table :data="tableData" stripe border v-loading="tableLoading" style="width: 100%">
        <el-table-column prop="checkInNo" label="入住单号" min-width="160" fixed="left" />
        <el-table-column prop="customerName" label="客人姓名" width="100" />
        <el-table-column prop="customerPhone" label="手机号" width="130" />
        <el-table-column prop="roomTypeName" label="房型" width="120" />
        <el-table-column label="入住日期" width="220" align="center">
          <template #default="{ row }">
            {{ row.checkInDate }} 至 {{ row.checkOutDate }}
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="邀请时间" width="180" align="center" />
        <el-table-column label="评价状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.reviewStatus === 1 ? 'success' : 'warning'" size="small">
              {{ row.reviewStatus === 1 ? '已评价' : '待评价' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="是否发送" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.isSent === 1 ? 'success' : 'info'" size="small">
              {{ row.isSent === 1 ? '已发送' : '未发送' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="sendTime" label="发送时间" width="180" align="center">
          <template #default="{ row }">
            <span v-if="row.sendTime">{{ row.sendTime }}</span>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="300" align="center" fixed="right">
          <template #default="{ row }">
            <el-button
              v-if="hasPermission('review:invitation:copy')"
              type="primary"
              link
              size="small"
              @click="handleCopyLink(row)"
            >复制链接</el-button>
            <el-button
              v-if="hasPermission('review:invitation:send') && row.isSent !== 1"
              type="success"
              link
              size="small"
              @click="handleSendInvitation(row)"
            >发送邀请</el-button>
            <el-button
              v-if="hasPermission('review:invitation:viewReview') && row.reviewStatus === 1"
              type="warning"
              link
              size="small"
              @click="handleViewReview(row)"
            >查看评价</el-button>
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

    <el-dialog v-model="sendDialogVisible" title="发送评价邀请" width="420px" destroy-on-close>
      <el-form :model="sendForm" :rules="sendFormRules" ref="sendFormRef" label-width="80px">
        <el-form-item label="发送方式" prop="sendMethod">
          <el-radio-group v-model="sendForm.sendMethod">
            <el-radio :value="1">短信发送</el-radio>
            <el-radio :value="2">邮件发送</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="发送内容">
          <el-input
            type="textarea"
            :rows="3"
            disabled
            :model-value="getSendContent()"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="sendDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmSend">确认发送</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="reviewDialogVisible" title="查看评价详情" width="640px" destroy-on-close>
      <div v-if="currentReview" class="review-detail">
        <div class="review-header">
          <div class="review-score">
            <span class="score-label">综合评分：</span>
            <span class="score-value">{{ currentReview.overallScore }}分</span>
          </div>
          <div class="review-time">评价时间：{{ currentReview.reviewTime }}</div>
        </div>
        <div class="review-info">
          <div><strong>客人：</strong>{{ currentReview.isAnonymous === 1 ? '匿名用户' : currentReview.customerName }}</div>
          <div><strong>房型：</strong>{{ currentReview.roomTypeName }}</div>
          <div><strong>入住日期：</strong>{{ currentReview.checkInDate }} 至 {{ currentReview.checkOutDate }}</div>
        </div>
        <div class="review-section" v-if="metricScores.length > 0">
          <h4>指标评分</h4>
          <div class="metric-score-list">
            <div v-for="score in metricScores" :key="score.id" class="metric-score-item">
              <span class="metric-name">{{ score.metricName }}</span>
              <div class="stars">
                <el-rate v-model="score.score" disabled show-score />
              </div>
            </div>
          </div>
        </div>
        <div class="review-section" v-if="currentReview.selectedTags">
          <h4>评价标签</h4>
          <div class="tag-list">
            <el-tag
              v-for="tag in selectedTagObjects"
              :key="tag.id"
              :type="tag.tagType === 1 ? 'success' : 'danger'"
              size="small"
              style="margin-right: 8px; margin-bottom: 8px"
            >{{ tag.tagText }}</el-tag>
          </div>
        </div>
        <div class="review-section" v-if="currentReview.reviewContent">
          <h4>评价内容</h4>
          <p class="review-content">{{ currentReview.reviewContent }}</p>
        </div>
        <div class="review-section" v-if="reviewImages.length > 0">
          <h4>评价图片</h4>
          <div class="image-list">
            <el-image
              v-for="(img, index) in reviewImages"
              :key="index"
              :src="img.imageUrl"
              :preview-src-list="reviewImages.map(i => i.imageUrl)"
              fit="cover"
              class="review-image"
            />
          </div>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh } from '@element-plus/icons-vue'
import api from '@/api'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()

const hasPermission = (permission) => {
  return userStore.hasPermission(permission)
}

const searchForm = reactive({
  keyword: '',
  reviewStatus: null,
  dateRange: []
})

const pagination = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0
})

const tableData = ref([])
const tableLoading = ref(false)

const sendDialogVisible = ref(false)
const sendFormRef = ref(null)
const sendForm = reactive({
  id: null,
  sendMethod: 1
})
const sendFormRules = {
  sendMethod: [{ required: true, message: '请选择发送方式', trigger: 'change' }]
}

const reviewDialogVisible = ref(false)
const currentReview = ref(null)
const metricScores = ref([])
const reviewImages = ref([])
const allTags = ref([])
const selectedTagObjects = ref([])

const fetchList = async () => {
  tableLoading.value = true
  try {
    const params = {
      pageNum: pagination.pageNum,
      pageSize: pagination.pageSize,
      keyword: searchForm.keyword,
      reviewStatus: searchForm.reviewStatus
    }
    if (searchForm.dateRange && searchForm.dateRange.length === 2) {
      params.startDate = searchForm.dateRange[0]
      params.endDate = searchForm.dateRange[1]
    }
    const res = await api.reviewInvitation.page(params)
    if (res.code === 200) {
      tableData.value = res.data.list || []
      pagination.total = res.data.total || 0
    }
  } catch (error) {
    console.error('获取评价邀请列表失败:', error)
  } finally {
    tableLoading.value = false
  }
}

const handleSearch = () => {
  pagination.pageNum = 1
  fetchList()
}

const handleReset = () => {
  searchForm.keyword = ''
  searchForm.reviewStatus = null
  searchForm.dateRange = []
  pagination.pageNum = 1
  fetchList()
}

const handleCopyLink = async (row) => {
  try {
    const res = await api.reviewInvitation.getLink(row.id)
    if (res.code === 200) {
      const fullLink = `${window.location.origin}${res.data}`
      await navigator.clipboard.writeText(fullLink)
      ElMessage.success('链接已复制，可用于测试')
    }
  } catch (error) {
    console.error('复制链接失败:', error)
    ElMessage.error('复制链接失败')
  }
}

const handleSendInvitation = (row) => {
  sendForm.id = row.id
  sendForm.sendMethod = 1
  sendDialogVisible.value = true
}

const getSendContent = () => {
  const row = tableData.value.find(r => r.id === sendForm.id)
  if (!row) return ''
  const baseUrl = window.location.origin
  return `【酒店评价邀请】尊敬的${row.customerName}，感谢您入住我们酒店。请点击链接评价您的入住体验：${baseUrl}${row.reviewLink}`
}

const confirmSend = async () => {
  if (!sendFormRef.value) return
  await sendFormRef.value.validate(async (valid) => {
    if (valid) {
      try {
        const res = await api.reviewInvitation.send(sendForm.id, sendForm.sendMethod)
        if (res.code === 200) {
          ElMessage.success('评价邀请已发送（模拟）')
          sendDialogVisible.value = false
          fetchList()
        }
      } catch (error) {
        console.error('发送邀请失败:', error)
      }
    }
  })
}

const handleViewReview = async (row) => {
  try {
    const res = await api.reviewSubmission.getByCheckInId(row.checkInId)
    if (res.code === 200 && res.data) {
      currentReview.value = res.data
      metricScores.value = res.data.metricScores || []
      reviewImages.value = res.data.images || []
      
      if (currentReview.value.selectedTags) {
        const tagIds = currentReview.value.selectedTags.split(',').map(Number)
        const tagRes = await api.reviewTag.list({ status: 1 })
        if (tagRes.code === 200) {
          allTags.value = tagRes.data
          selectedTagObjects.value = allTags.value.filter(t => tagIds.includes(t.id))
        }
      }
      
      reviewDialogVisible.value = true
    }
  } catch (error) {
    console.error('获取评价详情失败:', error)
  }
}

onMounted(() => {
  fetchList()
})
</script>

<style scoped>
.review-invitation-container {
  padding: 16px;
}

.search-card {
  margin-bottom: 16px;
}

.search-form {
  margin: 0;
}

.table-card {
  margin-bottom: 16px;
}

.pagination {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}

.review-detail {
  padding: 10px;
}

.review-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding-bottom: 16px;
  border-bottom: 1px solid #eee;
}

.score-label {
  font-size: 16px;
  color: #606266;
}

.score-value {
  font-size: 28px;
  font-weight: bold;
  color: #f56c6c;
}

.review-time {
  font-size: 14px;
  color: #909399;
}

.review-info {
  margin-bottom: 20px;
  line-height: 2;
  font-size: 14px;
  color: #606266;
}

.review-section {
  margin-bottom: 20px;
}

.review-section h4 {
  margin: 0 0 12px 0;
  font-size: 15px;
  color: #303133;
  font-weight: 600;
}

.metric-score-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.metric-score-item {
  display: flex;
  align-items: center;
  gap: 12px;
}

.metric-name {
  width: 100px;
  font-size: 14px;
  color: #606266;
}

.review-content {
  font-size: 14px;
  line-height: 1.8;
  color: #606266;
  margin: 0;
  padding: 12px;
  background: #f5f7fa;
  border-radius: 4px;
}

.tag-list {
  display: flex;
  flex-wrap: wrap;
}

.image-list {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.review-image {
  width: 100px;
  height: 100px;
  border-radius: 4px;
  cursor: pointer;
}
</style>
