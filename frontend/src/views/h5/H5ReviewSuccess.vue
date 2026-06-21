<template>
  <div class="h5-success-container" v-loading="pageLoading">
    <div v-if="successData" class="success-page">
      <div class="success-icon">
        <el-icon size="72" color="#67c23a"><CircleCheckFilled /></el-icon>
      </div>
      
      <h1 class="success-title">感谢您的宝贵评价！</h1>
      
      <div v-if="successData.isMember && successData.earnedPoints > 0" class="points-card">
        <div class="points-icon">
          <el-icon size="32" color="#e6a23c"><GoldMedal /></el-icon>
        </div>
        <div class="points-info">
          <div class="points-text">已赠送</div>
          <div class="points-value">{{ successData.earnedPoints }} 积分</div>
          <div class="points-desc">到您的会员账户</div>
        </div>
      </div>
      
      <div class="review-summary" v-if="successData.review">
        <div class="summary-title">您的评价</div>
        <div class="summary-score">
          <span class="score-label">综合评分：</span>
          <span class="score-value">{{ successData.review.overallScore }}</span>
          <span class="score-unit">分</span>
        </div>
        <el-rate v-model="successData.review.overallScore" disabled :max="5" class="summary-stars" />
      </div>
      
      <div class="coupon-section">
        <div class="coupon-placeholder">
          <el-icon size="48" color="#c0c4cc"><Discount /></el-icon>
          <p>优惠券领取入口（敬请期待）</p>
        </div>
      </div>
      
      <div class="action-buttons">
        <el-button type="primary" size="large" class="home-btn" @click="goHome">
          <el-icon><HomeFilled /></el-icon>
          返回首页
        </el-button>
      </div>
      
      <div class="footer-tip">
        <p>您的评价对我们非常重要，我们会持续改进服务质量</p>
        <p>期待您的再次光临！</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { CircleCheckFilled, GoldMedal, Discount, HomeFilled } from '@element-plus/icons-vue'
import api from '@/api'

const route = useRoute()

const pageLoading = ref(true)
const successData = ref(null)

const goHome = () => {
  window.location.href = '/'
}

const loadSuccessData = async () => {
  const reviewId = route.query.id
  
  if (!reviewId) {
    ElMessage.error('参数错误')
    pageLoading.value = false
    return
  }
  
  try {
    const res = await api.reviewH5.getSuccessData(reviewId)
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
  background: linear-gradient(135deg, #67c23a 0%, #85ce61 100%);
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
  margin-bottom: 24px;
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
  margin: 0 0 32px 0;
}

.points-card {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 20px;
  background: linear-gradient(135deg, #fff7e6 0%, #fffaeb 100%);
  border-radius: 12px;
  margin-bottom: 24px;
  border: 1px solid #faecd8;
}

.points-icon {
  width: 56px;
  height: 56px;
  background: #fff;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 2px 8px rgba(230, 162, 60, 0.2);
}

.points-info {
  text-align: left;
  flex: 1;
}

.points-text {
  font-size: 13px;
  color: #909399;
  margin-bottom: 2px;
}

.points-value {
  font-size: 28px;
  font-weight: bold;
  color: #e6a23c;
  line-height: 1.2;
}

.points-desc {
  font-size: 12px;
  color: #c0c4cc;
  margin-top: 2px;
}

.review-summary {
  padding: 20px;
  background: #f5f7fa;
  border-radius: 12px;
  margin-bottom: 24px;
}

.summary-title {
  font-size: 14px;
  color: #909399;
  margin-bottom: 12px;
}

.summary-score {
  margin-bottom: 8px;
}

.score-label {
  font-size: 14px;
  color: #606266;
}

.score-value {
  font-size: 32px;
  font-weight: bold;
  color: #f56c6c;
}

.score-unit {
  font-size: 14px;
  color: #909399;
  margin-left: 4px;
}

.summary-stars {
  justify-content: center;
}

.coupon-section {
  margin-bottom: 32px;
}

.coupon-placeholder {
  padding: 32px 20px;
  border: 2px dashed #dcdfe6;
  border-radius: 12px;
  color: #c0c4cc;
}

.coupon-placeholder p {
  margin: 12px 0 0 0;
  font-size: 14px;
}

.action-buttons {
  margin-bottom: 24px;
}

.home-btn {
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
