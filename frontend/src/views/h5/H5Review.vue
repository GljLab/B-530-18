<template>
  <div class="h5-review-container" v-loading="pageLoading">
    <div v-if="pageError" class="error-page">
      <div class="error-icon">
        <el-icon size="64" color="#f56c6c"><Warning /></el-icon>
      </div>
      <h2 class="error-title">{{ pageError }}</h2>
      <p class="error-desc">请检查链接是否正确，或联系酒店工作人员</p>
    </div>

    <div v-else-if="pageData" class="review-page">
      <div class="hotel-header">
        <div class="hotel-logo">
          <div class="logo-placeholder">
            <el-icon size="32"><OfficeBuilding /></el-icon>
          </div>
        </div>
        <div class="hotel-name">{{ pageData.hotelInfo?.hotelName || '智慧酒店' }}</div>
      </div>

      <div class="stay-info-card">
        <div class="room-type">{{ pageData.invitation?.roomTypeName || '-' }}</div>
        <div class="stay-dates">
          <span>{{ pageData.invitation?.checkInDate || '-' }}</span>
          <span class="date-separator">至</span>
          <span>{{ pageData.invitation?.checkOutDate || '-' }}</span>
        </div>
        <div class="stay-days">入住 {{ stayDays }} 天</div>
      </div>

      <div class="overall-score-section">
        <div class="overall-label">综合评分</div>
        <div class="overall-score">{{ overallScore.toFixed(1) }}</div>
        <div class="overall-stars">
          <el-rate v-model="overallScoreInt" disabled :max="5" />
        </div>
      </div>

      <div class="section">
        <div class="section-title">评价指标</div>
        <div class="metric-list">
          <div v-for="metric in pageData.metrics" :key="metric.id" class="metric-item">
            <div class="metric-header">
              <span class="metric-name">
                {{ metric.metricName }}
                <span v-if="metric.isRequired === 1" class="required-mark">*</span>
              </span>
              <span class="metric-score">{{ getMetricScore(metric.id) }}分</span>
            </div>
            <el-rate
              v-model="metricScores[metric.id]"
              :max="metric.scoreMax"
              :min="metric.scoreMin"
              show-score
              size="large"
              @change="calculateOverallScore"
            />
          </div>
        </div>
      </div>

      <div class="section">
        <div class="section-title">
          评价标签
          <span class="tag-count">已选 {{ selectedTags.length }} 个</span>
        </div>
        <div class="tag-list">
          <div
            v-for="tag in pageData.tags"
            :key="tag.id"
            :class="['tag-card', { 'selected': selectedTags.includes(tag.id), 'good': tag.tagType === 1, 'bad': tag.tagType === 2 }]"
            @click="toggleTag(tag.id)"
          >
            {{ tag.tagText }}
          </div>
        </div>
      </div>

      <div class="section">
        <div class="section-title">评价内容</div>
        <div class="quick-comments">
          <el-button
            v-for="comment in pageData.quickComments"
            :key="comment.id"
            size="small"
            :type="comment.commentType === 1 ? 'success' : comment.commentType === 2 ? 'warning' : 'danger'"
            plain
            @click="fillQuickComment(comment.commentContent)"
          >{{ comment.commentContent }}</el-button>
        </div>
        <el-input
          v-model="reviewContent"
          type="textarea"
          :rows="4"
          placeholder="请分享您的入住体验（选填）"
          maxlength="500"
          show-word-limit
          class="review-textarea"
        />
      </div>

      <div class="section">
        <div class="section-title">
          上传照片
          <span class="img-count">{{ uploadedImages.length }}/6</span>
        </div>
        <div class="image-upload-area">
          <div
            v-for="(image, index) in uploadedImages"
            :key="index"
            class="image-item"
          >
            <img :src="image.url" alt="评价图片" />
            <div class="image-remove" @click="removeImage(index)">
              <el-icon><Close /></el-icon>
            </div>
          </div>
          <div
            v-if="uploadedImages.length < 6"
            class="upload-btn"
            @click="triggerFileInput"
          >
            <el-icon size="28"><Plus /></el-icon>
            <span>上传照片</span>
          </div>
          <input
            ref="fileInput"
            type="file"
            accept="image/jpeg,image/png"
            multiple
            style="display: none"
            @change="handleFileChange"
          />
        </div>
        <div class="upload-tips">
          支持JPG、PNG格式，单张不超过5MB，最多6张
        </div>
      </div>

      <div class="section anonymous-section">
        <el-checkbox v-model="isAnonymous">匿名评价</el-checkbox>
        <span class="anonymous-tip">（勾选后评价展示时不显示您的真实姓名）</span>
      </div>

      <div class="submit-footer">
        <el-button
          type="primary"
          size="large"
          class="submit-btn"
          :loading="submitting"
          @click="handleSubmit"
        >提交评价</el-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Warning, OfficeBuilding, Close, Plus } from '@element-plus/icons-vue'
import api from '@/api'

const route = useRoute()
const router = useRouter()

const pageLoading = ref(true)
const pageError = ref('')
const pageData = ref(null)

const metricScores = reactive({})
const selectedTags = ref([])
const reviewContent = ref('')
const uploadedImages = ref([])
const isAnonymous = ref(0)
const submitting = ref(false)
const fileInput = ref(null)

const overallScore = ref(0)
const overallScoreInt = computed(() => Math.round(overallScore.value))

const stayDays = computed(() => {
  if (!pageData.value?.invitation?.checkInDate || !pageData.value?.invitation?.checkOutDate) return 0
  const start = new Date(pageData.value.invitation.checkInDate)
  const end = new Date(pageData.value.invitation.checkOutDate)
  return Math.ceil((end - start) / (1000 * 60 * 60 * 24))
})

const getMetricScore = (metricId) => {
  return metricScores[metricId] || 0
}

const calculateOverallScore = () => {
  if (!pageData.value || !pageData.value.metrics) return
  
  let totalScore = 0
  let totalWeight = 0
  
  for (const metric of pageData.value.metrics) {
    const score = metricScores[metric.id] || 0
    const weight = metric.weight || 0
    totalScore += score * weight
    totalWeight += weight
  }
  
  if (totalWeight > 0) {
    overallScore.value = totalScore / totalWeight
  }
}

const toggleTag = (tagId) => {
  const index = selectedTags.value.indexOf(tagId)
  if (index > -1) {
    selectedTags.value.splice(index, 1)
  } else {
    selectedTags.value.push(tagId)
  }
}

const fillQuickComment = (content) => {
  reviewContent.value = content
}

const triggerFileInput = () => {
  if (fileInput.value) {
    fileInput.value.click()
  }
}

const handleFileChange = async (event) => {
  const files = event.target.files
  if (!files || files.length === 0) return
  
  const remainingSlots = 6 - uploadedImages.value.length
  const filesToProcess = Array.from(files).slice(0, remainingSlots)
  
  for (const file of filesToProcess) {
    if (file.size > 5 * 1024 * 1024) {
      ElMessage.error(`图片${file.name}超过5MB限制`)
      continue
    }
    
    if (!['image/jpeg', 'image/png'].includes(file.type)) {
      ElMessage.error(`图片${file.name}格式不支持，请上传JPG或PNG格式`)
      continue
    }
    
    try {
      const formData = new FormData()
      formData.append('file', file)
      
      const res = await api.hotel.uploadFile(formData)
      if (res.code === 200) {
        uploadedImages.value.push({
          url: res.data,
          name: file.name
        })
      }
    } catch (error) {
      console.error('图片上传失败:', error)
      ElMessage.error(`图片${file.name}上传失败`)
    }
  }
  
  if (fileInput.value) {
    fileInput.value.value = ''
  }
}

const removeImage = (index) => {
  uploadedImages.value.splice(index, 1)
}

const validateSubmit = () => {
  if (!pageData.value || !pageData.value.metrics) {
    ElMessage.error('页面数据加载失败，请刷新重试')
    return false
  }
  
  const requiredMetrics = pageData.value.metrics.filter(m => m.isRequired === 1)
  for (const metric of requiredMetrics) {
    if (!metricScores[metric.id] || metricScores[metric.id] < metric.scoreMin) {
      ElMessage.error(`请对必评指标"${metric.metricName}"评分`)
      return false
    }
  }
  
  if (reviewContent.value && reviewContent.value.length > 500) {
    ElMessage.error('评价内容不能超过500字')
    return false
  }
  
  if (uploadedImages.value.length > 6) {
    ElMessage.error('最多只能上传6张图片')
    return false
  }
  
  return true
}

const handleSubmit = async () => {
  if (!validateSubmit()) return
  
  submitting.value = true
  try {
    const metricScoresList = pageData.value.metrics
      .filter(m => metricScores[m.id] && metricScores[m.id] > 0)
      .map(m => ({
        metricId: m.id,
        metricName: m.metricName,
        score: metricScores[m.id],
        weight: m.weight
      }))
    
    const submitData = {
      checkInNo: pageData.value?.invitation?.checkInNo || '',
      code: pageData.value?.invitation?.reviewCode || '',
      metricScores: metricScoresList,
      selectedTags: selectedTags.value,
      reviewContent: reviewContent.value,
      images: uploadedImages.value.map((img, index) => ({
        imageUrl: img.url,
        imageName: img.name,
        sortOrder: index
      })),
      isAnonymous: isAnonymous.value ? 1 : 0
    }
    
    const res = await api.reviewH5.submit(submitData)
    if (res.code === 200) {
      ElMessage.success('评价提交成功')
      router.push({
        path: '/h5/review/success',
        query: { id: res.data.id }
      })
    }
  } catch (error) {
    console.error('提交评价失败:', error)
  } finally {
    submitting.value = false
  }
}

const loadPageData = async () => {
  const order = route.query.order
  const code = route.query.code
  
  if (!order || !code) {
    pageError.value = '链接已失效'
    pageLoading.value = false
    return
  }
  
  try {
    const res = await api.reviewH5.getData(order, code)
    if (res.code === 200) {
      pageData.value = res.data
      
      const metrics = res.data.metrics || []
      for (const metric of metrics) {
        metricScores[metric.id] = 0
      }
    } else {
      pageError.value = res.message || '链接已失效'
    }
  } catch (error) {
    console.error('加载页面数据失败:', error)
    if (error.response && error.response.data && error.response.data.message) {
      pageError.value = error.response.data.message
    } else {
      pageError.value = '链接已失效'
    }
  } finally {
    pageLoading.value = false
  }
}

onMounted(() => {
  loadPageData()
})
</script>

<style scoped>
.h5-review-container {
  min-height: 100vh;
  background: #f5f7fa;
  padding-bottom: 80px;
}

.error-page {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 100vh;
  padding: 40px 20px;
  text-align: center;
}

.error-icon {
  margin-bottom: 24px;
}

.error-title {
  font-size: 20px;
  color: #303133;
  margin: 0 0 12px 0;
}

.error-desc {
  font-size: 14px;
  color: #909399;
  margin: 0;
}

.review-page {
  max-width: 750px;
  margin: 0 auto;
}

.hotel-header {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 20px 16px;
  background: linear-gradient(135deg, #409eff 0%, #66b1ff 100%);
  color: #fff;
}

.hotel-logo {
  width: 56px;
  height: 56px;
  border-radius: 12px;
  overflow: hidden;
  background: rgba(255, 255, 255, 0.2);
  display: flex;
  align-items: center;
  justify-content: center;
}

.hotel-logo img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.logo-placeholder {
  color: #fff;
}

.hotel-name {
  font-size: 20px;
  font-weight: 600;
}

.stay-info-card {
  margin: -20px 16px 16px;
  padding: 20px;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  position: relative;
  z-index: 10;
}

.room-type {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 12px;
}

.stay-dates {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 15px;
  color: #606266;
  margin-bottom: 8px;
}

.date-separator {
  color: #c0c4cc;
}

.stay-days {
  font-size: 14px;
  color: #909399;
}

.overall-score-section {
  text-align: center;
  padding: 24px 16px;
  background: #fff;
  margin-bottom: 12px;
}

.overall-label {
  font-size: 14px;
  color: #909399;
  margin-bottom: 8px;
}

.overall-score {
  font-size: 48px;
  font-weight: bold;
  color: #f56c6c;
  line-height: 1.2;
  margin-bottom: 8px;
}

.overall-stars {
  display: flex;
  justify-content: center;
}

.section {
  padding: 20px 16px;
  background: #fff;
  margin-bottom: 12px;
}

.section-title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 16px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.tag-count {
  font-size: 13px;
  color: #909399;
  font-weight: normal;
}

.metric-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.metric-item {
  padding-bottom: 16px;
  border-bottom: 1px solid #f0f2f5;
}

.metric-item:last-child {
  padding-bottom: 0;
  border-bottom: none;
}

.metric-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.metric-name {
  font-size: 15px;
  color: #303133;
}

.required-mark {
  color: #f56c6c;
  margin-left: 2px;
}

.metric-score {
  font-size: 14px;
  color: #f56c6c;
  font-weight: 600;
}

.tag-list {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.tag-card {
  padding: 8px 16px;
  border-radius: 20px;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.2s;
  border: 1px solid #dcdfe6;
  background: #fff;
  color: #606266;
}

.tag-card.good {
  border-color: #c2e7b0;
  background: #f0f9eb;
  color: #67c23a;
}

.tag-card.bad {
  border-color: #fbc4c4;
  background: #fef0f0;
  color: #f56c6c;
}

.tag-card.selected {
  transform: scale(1.05);
}

.tag-card.selected.good {
  background: #67c23a;
  color: #fff;
  border-color: #67c23a;
}

.tag-card.selected.bad {
  background: #f56c6c;
  color: #fff;
  border-color: #f56c6c;
}

.quick-comments {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 16px;
}

.review-textarea {
  width: 100%;
}

.img-count {
  font-size: 13px;
  color: #909399;
  font-weight: normal;
}

.image-upload-area {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-bottom: 12px;
}

.image-item {
  width: 100px;
  height: 100px;
  position: relative;
  border-radius: 8px;
  overflow: hidden;
}

.image-item img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.image-remove {
  position: absolute;
  top: 4px;
  right: 4px;
  width: 24px;
  height: 24px;
  background: rgba(0, 0, 0, 0.6);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  cursor: pointer;
}

.upload-btn {
  width: 100px;
  height: 100px;
  border: 2px dashed #dcdfe6;
  border-radius: 8px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 4px;
  color: #909399;
  font-size: 12px;
  cursor: pointer;
  transition: all 0.2s;
}

.upload-btn:hover {
  border-color: #409eff;
  color: #409eff;
}

.upload-tips {
  font-size: 12px;
  color: #c0c4cc;
}

.anonymous-section {
  display: flex;
  align-items: center;
  gap: 8px;
}

.anonymous-tip {
  font-size: 13px;
  color: #909399;
}

.submit-footer {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  padding: 12px 16px;
  background: #fff;
  box-shadow: 0 -2px 12px rgba(0, 0, 0, 0.1);
  z-index: 100;
}

.submit-btn {
  width: 100%;
  height: 48px;
  font-size: 16px;
  border-radius: 24px;
}

@media (min-width: 750px) {
  .submit-footer {
    max-width: 750px;
    margin: 0 auto;
    left: 50%;
    transform: translateX(-50%);
  }
}
</style>
