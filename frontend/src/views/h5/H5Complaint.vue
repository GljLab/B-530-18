<template>
  <div class="h5-complaint-container" v-loading="pageLoading">
    <div v-if="pageError" class="error-page">
      <div class="error-icon">
        <el-icon size="64" color="#f56c6c"><Warning /></el-icon>
      </div>
      <h2 class="error-title">{{ pageError }}</h2>
      <p class="error-desc">请检查链接是否正确，或联系酒店工作人员</p>
    </div>

    <div v-else-if="pageData" class="complaint-page">
      <div class="hotel-header">
        <div class="hotel-logo">
          <div class="logo-placeholder">
            <el-icon size="32"><OfficeBuilding /></el-icon>
          </div>
        </div>
        <div class="hotel-name">{{ pageData.hotelInfo?.hotelName || '智慧酒店' }}</div>
      </div>

      <div class="page-title">客户投诉</div>

      <div class="stay-info-card">
        <div class="stay-info-row">
          <span class="stay-label">入住单号：</span>
          <span class="stay-value">{{ pageData.checkIn?.checkInNo || '-' }}</span>
        </div>
        <div class="stay-info-row">
          <span class="stay-label">房型：</span>
          <span class="stay-value">{{ pageData.checkIn?.roomTypeName || '-' }}</span>
        </div>
        <div class="stay-info-row">
          <span class="stay-label">入住日期：</span>
          <span class="stay-value">{{ pageData.checkIn?.checkInDate || '-' }} 至 {{ pageData.checkIn?.checkOutDate || '-' }}</span>
        </div>
      </div>

      <div class="section">
        <div class="section-title"><span class="required">*</span>投诉类型</div>
        <el-select v-model="formData.complaintType" placeholder="请选择投诉类型" class="full-width">
          <el-option
            v-for="item in complaintTypeOptions"
            :key="item.value"
            :label="item.label"
            :value="item.value"
          />
        </el-select>
      </div>

      <div class="section">
        <div class="section-title">
          <span class="required">*</span>投诉内容
          <span class="word-count">{{ formData.complaintContent.length }}/500</span>
        </div>
        <el-input
          v-model="formData.complaintContent"
          type="textarea"
          :rows="5"
          placeholder="请详细描述您遇到的问题"
          maxlength="500"
          class="complaint-textarea"
        />
      </div>

      <div class="section">
        <div class="section-title">
          证据图片（选填）
          <span class="img-count">{{ uploadedImages.length }}/6</span>
        </div>
        <div class="image-upload-area">
          <div
            v-for="(image, index) in uploadedImages"
            :key="index"
            class="image-item"
          >
            <img :src="image.imageUrl" :alt="image.imageName || '投诉图片'" />
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

      <div class="section">
        <div class="section-title">
          期望解决方案（选填）
          <span class="word-count">{{ formData.expectedSolution.length }}/100</span>
        </div>
        <el-input
          v-model="formData.expectedSolution"
          placeholder="如：希望退款、希望赔偿、希望道歉等"
          maxlength="100"
          class="full-width"
        />
      </div>

      <div class="section">
        <div class="section-title"><span class="required">*</span>联系方式</div>
        <div class="contact-form">
          <el-form-item label="手机号">
            <el-input
              v-model="formData.customerPhone"
              placeholder="请输入手机号"
              maxlength="11"
            />
          </el-form-item>
          <el-form-item label="邮箱">
            <el-input
              v-model="formData.customerEmail"
              placeholder="选填"
            />
          </el-form-item>
        </div>
      </div>

      <div class="submit-footer">
        <el-button
          type="primary"
          size="large"
          class="submit-btn"
          :loading="submitting"
          @click="handleSubmit"
        >提交投诉</el-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Warning, OfficeBuilding, Close, Plus } from '@element-plus/icons-vue'
import api from '@/api'

const route = useRoute()
const router = useRouter()

const pageLoading = ref(true)
const pageError = ref('')
const pageData = ref(null)
const submitting = ref(false)
const fileInput = ref(null)
const uploadedImages = ref([])

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

const formData = reactive({
  complaintType: null,
  complaintContent: '',
  expectedSolution: '',
  customerPhone: '',
  customerEmail: ''
})

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

      const res = await api.reviewH5.uploadImage(formData)
      if (res.code === 200) {
        uploadedImages.value.push({
          imageUrl: res.data.url,
          imageName: file.name
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

const validatePhone = (phone) => {
  return /^1[3-9]\d{9}$/.test(phone)
}

const validateSubmit = () => {
  if (!formData.complaintType) {
    ElMessage.error('请选择投诉类型')
    return false
  }
  if (!formData.complaintContent || formData.complaintContent.trim() === '') {
    ElMessage.error('请填写投诉内容')
    return false
  }
  if (formData.complaintContent.length > 500) {
    ElMessage.error('投诉内容不能超过500字')
    return false
  }
  if (uploadedImages.value.length > 6) {
    ElMessage.error('最多只能上传6张图片')
    return false
  }
  if (!formData.customerPhone) {
    ElMessage.error('请填写手机号')
    return false
  }
  if (!validatePhone(formData.customerPhone)) {
    ElMessage.error('手机号格式不正确')
    return false
  }
  return true
}

const handleSubmit = async () => {
  if (!validateSubmit()) return

  submitting.value = true
  try {
    const submitData = {
      checkInNo: pageData.value?.checkIn?.checkInNo || '',
      code: pageData.value?.code || '',
      complaintType: formData.complaintType,
      complaintContent: formData.complaintContent,
      expectedSolution: formData.expectedSolution,
      customerPhone: formData.customerPhone,
      customerEmail: formData.customerEmail,
      customerName: pageData.value?.checkIn?.customerName || '',
      images: uploadedImages.value.map((img, index) => ({
        imageUrl: img.imageUrl,
        imageName: img.imageName,
        sortOrder: index
      }))
    }

    const res = await api.complaintH5.submit(submitData)
    if (res.code === 200) {
      ElMessage.success('投诉提交成功')
      router.push({
        path: '/h5/complaint/success',
        query: { id: res.data.id }
      })
    }
  } catch (error) {
    console.error('提交投诉失败:', error)
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
    const res = await api.complaintH5.getData(order, code)
    if (res.code === 200) {
      pageData.value = res.data
      if (res.data.checkIn?.customerPhone) {
        formData.customerPhone = res.data.checkIn.customerPhone
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
.h5-complaint-container {
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
  margin-bottom: 20px;
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

.complaint-page {
  max-width: 480px;
  margin: 0 auto;
}

.hotel-header {
  background: linear-gradient(135deg, #409eff 0%, #66b1ff 100%);
  padding: 20px;
  display: flex;
  align-items: center;
  gap: 12px;
  color: #fff;
}

.hotel-logo {
  width: 48px;
  height: 48px;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.logo-placeholder {
  color: #fff;
}

.hotel-name {
  font-size: 18px;
  font-weight: 600;
}

.page-title {
  font-size: 22px;
  font-weight: 600;
  color: #303133;
  padding: 20px 20px 12px;
  text-align: center;
}

.stay-info-card {
  background: #fff;
  margin: 0 16px 16px;
  padding: 16px;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

.stay-info-row {
  display: flex;
  padding: 6px 0;
  font-size: 14px;
}

.stay-label {
  color: #909399;
  min-width: 80px;
}

.stay-value {
  color: #303133;
  flex: 1;
}

.section {
  background: #fff;
  margin: 0 16px 16px;
  padding: 16px;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

.section-title {
  font-size: 15px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 12px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.required {
  color: #f56c6c;
  margin-right: 4px;
}

.word-count {
  font-size: 12px;
  color: #909399;
  font-weight: normal;
}

.img-count {
  font-size: 12px;
  color: #909399;
  font-weight: normal;
}

.full-width {
  width: 100%;
}

.complaint-textarea {
  width: 100%;
}

.image-upload-area {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}

.image-item {
  width: 90px;
  height: 90px;
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
  top: 2px;
  right: 2px;
  width: 20px;
  height: 20px;
  background: rgba(0, 0, 0, 0.5);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  cursor: pointer;
  font-size: 12px;
}

.upload-btn {
  width: 90px;
  height: 90px;
  border: 1px dashed #dcdfe6;
  border-radius: 8px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #909399;
  cursor: pointer;
  font-size: 12px;
  gap: 4px;
}

.upload-btn:hover {
  border-color: #409eff;
  color: #409eff;
}

.upload-tips {
  font-size: 12px;
  color: #909399;
  margin-top: 8px;
}

.contact-form {
  padding: 0;
}

.contact-form :deep(.el-form-item) {
  margin-bottom: 16px;
}

.contact-form :deep(.el-form-item:last-child) {
  margin-bottom: 0;
}

.submit-footer {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  max-width: 480px;
  margin: 0 auto;
  padding: 12px 16px;
  background: #fff;
  box-shadow: 0 -2px 8px rgba(0, 0, 0, 0.06);
}

.submit-btn {
  width: 100%;
  height: 48px;
  font-size: 16px;
  border-radius: 24px;
}
</style>
