<template>
  <div class="h5-success-container" v-loading="pageLoading">
    <div v-if="successData" class="success-page">
      <div class="success-icon">
        <el-icon size="72" color="#409eff"><CircleCheckFilled /></el-icon>
      </div>

      <h1 class="success-title">您的投诉已提交</h1>
      <p class="success-subtitle">我们会尽快处理</p>

      <div class="complaint-no-card">
        <div class="complaint-no-label">投诉单号</div>
        <div class="complaint-no-value">{{ successData.complaintNo }}</div>
      </div>

      <div class="notice-card">
        <el-icon size="20" color="#e6a23c"><WarningFilled /></el-icon>
        <span class="notice-text">客服将在24小时内与您联系，请保持电话畅通</span>
      </div>

      <div class="action-buttons">
        <el-button type="primary" size="large" class="back-btn" @click="goBack">
          <el-icon><ArrowLeft /></el-icon>
          返回
        </el-button>
      </div>

      <div class="footer-tip">
        <p>感谢您的反馈，我们会持续改进服务质量</p>
        <p>期待为您提供更好的入住体验！</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { CircleCheckFilled, WarningFilled, ArrowLeft } from '@element-plus/icons-vue'
import api from '@/api'

const route = useRoute()

const pageLoading = ref(true)
const successData = ref(null)

const goBack = () => {
  if (window.history.length > 1) {
    window.history.back()
  } else {
    window.location.href = '/'
  }
}

const loadSuccessData = async () => {
  const complaintId = route.query.id

  if (!complaintId) {
    ElMessage.error('参数错误')
    pageLoading.value = false
    return
  }

  try {
    const res = await api.complaintH5.getSuccessData(complaintId)
    if (res.code === 200) {
      successData.value = res.data
    } else {
      ElMessage.error(res.message || '加载失败')
    }
  } catch (error) {
    console.error('加载成功页面数据失败:', error)
    ElMessage.error('加载失败，请刷新重试')
  } finally {
    pageLoading.value = false
  }
}

onMounted(() => {
  loadSuccessData()
})
</script>

<style scoped>
.h5-success-container {
  min-height: 100vh;
  background: linear-gradient(135deg, #409eff 0%, #66b1ff 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
}

.success-page {
  max-width: 480px;
  width: 100%;
  background: #fff;
  border-radius: 20px;
  padding: 40px 24px;
  text-align: center;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
}

.success-icon {
  margin-bottom: 20px;
  animation: bounceIn 0.6s ease-out;
}

@keyframes bounceIn {
  0% {
    opacity: 0;
    transform: scale(0.3);
  }
  50% {
    transform: scale(1.05);
  }
  70% {
    transform: scale(0.9);
  }
  100% {
    opacity: 1;
    transform: scale(1);
  }
}

.success-title {
  font-size: 24px;
  font-weight: 600;
  color: #303133;
  margin: 0 0 8px 0;
}

.success-subtitle {
  font-size: 14px;
  color: #909399;
  margin: 0 0 28px 0;
}

.complaint-no-card {
  background: linear-gradient(135deg, #ecf5ff 0%, #f0f7ff 100%);
  border-radius: 12px;
  padding: 20px;
  margin-bottom: 20px;
  border: 1px solid #d9ecff;
}

.complaint-no-label {
  font-size: 13px;
  color: #909399;
  margin-bottom: 8px;
}

.complaint-no-value {
  font-size: 22px;
  font-weight: bold;
  color: #409eff;
  letter-spacing: 1px;
}

.notice-card {
  display: flex;
  align-items: flex-start;
  gap: 8px;
  padding: 16px;
  background: #fdf6ec;
  border-radius: 12px;
  margin-bottom: 32px;
  text-align: left;
}

.notice-text {
  font-size: 14px;
  color: #e6a23c;
  line-height: 1.6;
  flex: 1;
}

.action-buttons {
  margin-bottom: 28px;
}

.back-btn {
  width: 100%;
  height: 48px;
  font-size: 16px;
  border-radius: 24px;
}

.footer-tip {
  font-size: 13px;
  color: #909399;
  line-height: 1.8;
}

.footer-tip p {
  margin: 0;
}
</style>
