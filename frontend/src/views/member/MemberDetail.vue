<template>
  <div class="member-detail-container" v-loading="loading">
    <div class="detail-header">
      <div class="member-info">
        <div class="member-avatar" :style="{ background: memberInfo.levelColor + '20' }">
          <span style="font-size: 32px">{{ memberInfo.levelIcon || '👤' }}</span>
        </div>
        <div class="member-main">
          <div class="member-name-row">
            <span class="member-name">{{ memberInfo.customerName }}</span>
            <el-tag
              v-if="memberInfo.status === 1"
              size="small"
              type="success"
              effect="dark"
              style="margin-left: 12px"
            >正常</el-tag>
            <el-tag
              v-else
              size="small"
              type="danger"
              effect="dark"
              style="margin-left: 12px"
            >已冻结</el-tag>
          </div>
          <div class="member-no">会员卡号：{{ memberInfo.memberNo }}</div>
          <div class="member-level">
            <span class="level-badge" :style="{ background: memberInfo.levelColor + '20', color: memberInfo.levelColor }">
              {{ memberInfo.levelIcon }} {{ memberInfo.levelName }}
            </span>
          </div>
        </div>
        <div class="member-stats">
          <div class="stat-item">
            <div class="stat-value">{{ formatNumber(memberInfo.currentPoints) }}</div>
            <div class="stat-label">当前积分</div>
          </div>
          <div class="stat-item">
            <div class="stat-value">¥{{ formatNumber(memberInfo.totalSpent) }}</div>
            <div class="stat-label">累计消费</div>
          </div>
          <div class="stat-item">
            <div class="stat-value">{{ memberInfo.stayCount || 0 }}</div>
            <div class="stat-label">入住次数</div>
          </div>
        </div>
      </div>
      <div class="header-actions">
        <el-button
          v-if="hasPermission('member:point:add') && memberInfo.status === 1"
          type="success"
          @click="handleAddPoints"
        >
          <el-icon><Coin /></el-icon>发放积分
        </el-button>
        <el-button
          v-if="hasPermission('member:point:use') && memberInfo.status === 1 && memberInfo.currentPoints > 0"
          type="primary"
          @click="handleUsePoints"
        >
          <el-icon><Coin /></el-icon>积分抵扣
        </el-button>
        <el-button
          v-if="hasPermission('member:level:adjust')"
          type="warning"
          @click="handleAdjustLevel"
        >
          <el-icon><Medal /></el-icon>调整等级
        </el-button>
        <el-button
          v-if="hasPermission('member:freeze') && memberInfo.status === 1"
          type="danger"
          @click="handleFreeze"
        >
          <el-icon><Lock /></el-icon>冻结会员
        </el-button>
        <el-button
          v-if="hasPermission('member:unfreeze') && memberInfo.status === 0"
          type="success"
          @click="handleUnfreeze"
        >
          <el-icon><Unlock /></el-icon>解冻会员
        </el-button>
        <el-button @click="handleBack">
          <el-icon><ArrowLeft /></el-icon>返回
        </el-button>
      </div>
    </div>

    <el-tabs v-model="activeTab" type="border-card">
      <el-tab-pane label="基础信息" name="basic">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-card shadow="never" class="info-card">
              <template #header>
                <span class="card-title">基本信息</span>
              </template>
              <el-descriptions :column="2" border size="small">
                <el-descriptions-item label="姓名">{{ memberInfo.customerName }}</el-descriptions-item>
                <el-descriptions-item label="手机号">{{ memberInfo.phone || '-' }}</el-descriptions-item>
                <el-descriptions-item label="性别">{{ genderText }}</el-descriptions-item>
                <el-descriptions-item label="证件类型">{{ idTypeText }}</el-descriptions-item>
                <el-descriptions-item label="证件号码" :span="2">{{ memberInfo.customer?.idNumber || '-' }}</el-descriptions-item>
              </el-descriptions>
            </el-card>
          </el-col>
          <el-col :span="12">
            <el-card shadow="never" class="info-card">
              <template #header>
                <span class="card-title">会员信息</span>
              </template>
              <el-descriptions :column="2" border size="small">
                <el-descriptions-item label="会员卡号">{{ memberInfo.memberNo }}</el-descriptions-item>
                <el-descriptions-item label="当前等级">
                  <span class="level-badge" :style="{ background: memberInfo.levelColor + '20', color: memberInfo.levelColor }">
                    {{ memberInfo.levelName }}
                  </span>
                </el-descriptions-item>
                <el-descriptions-item label="注册时间">{{ formatDateTime(memberInfo.registerTime) }}</el-descriptions-item>
                <el-descriptions-item label="注册来源">{{ registerSourceText }}</el-descriptions-item>
                <el-descriptions-item label="累计积分">{{ formatNumber(memberInfo.totalPoints) }}</el-descriptions-item>
                <el-descriptions-item label="当前积分">{{ formatNumber(memberInfo.currentPoints) }}</el-descriptions-item>
                <el-descriptions-item label="累计消费">¥{{ formatNumber(memberInfo.totalSpent) }}</el-descriptions-item>
                <el-descriptions-item label="本年消费">¥{{ formatNumber(memberInfo.yearlySpent) }}</el-descriptions-item>
                <el-descriptions-item label="入住次数">{{ memberInfo.stayCount || 0 }}</el-descriptions-item>
                <el-descriptions-item label="上次入住">
                  {{ memberInfo.lastStayTime ? formatDateTime(memberInfo.lastStayTime) : '-' }}
                </el-descriptions-item>
                <el-descriptions-item label="推荐人" :span="2">
                  {{ memberInfo.referrerNo || '-' }}
                </el-descriptions-item>
              </el-descriptions>
            </el-card>
          </el-col>
        </el-row>
      </el-tab-pane>

      <el-tab-pane label="等级权益" name="benefits">
        <el-card shadow="never" class="benefits-card" v-if="levelInfo">
          <template #header>
            <div class="benefits-header">
              <span class="level-icon-big" :style="{ color: levelInfo.levelColor }">
                {{ levelInfo.levelIcon }}
              </span>
              <div>
                <div class="level-name-big" :style="{ color: levelInfo.levelColor }">
                  {{ levelInfo.levelName }}
                </div>
                <div class="level-desc">当前等级享有的所有权益</div>
              </div>
            </div>
          </template>

          <el-row :gutter="20">
            <el-col :span="8">
              <div class="benefit-item">
                <div class="benefit-icon" style="background: #ecf5ff; color: #409eff">
                  <el-icon><Money /></el-icon>
                </div>
                <div class="benefit-content">
                  <div class="benefit-title">房费折扣</div>
                  <div class="benefit-value">{{ levelInfo.roomDiscount }}%</div>
                </div>
              </div>
            </el-col>
            <el-col :span="8">
              <div class="benefit-item">
                <div class="benefit-icon" style="background: #fdf6ec; color: #e6a23c">
                  <el-icon><Dish /></el-icon>
                </div>
                <div class="benefit-content">
                  <div class="benefit-title">餐饮折扣</div>
                  <div class="benefit-value">{{ levelInfo.diningDiscount }}%</div>
                </div>
              </div>
            </el-col>
            <el-col :span="8">
              <div class="benefit-item">
                <div class="benefit-icon" style="background: #f0f9eb; color: #67c23a">
                  <el-icon><Coin /></el-icon>
                </div>
                <div class="benefit-content">
                  <div class="benefit-title">积分倍率</div>
                  <div class="benefit-value">{{ levelInfo.pointRate }}倍</div>
                </div>
              </div>
            </el-col>
          </el-row>

          <el-divider />

          <div class="benefits-section">
            <div class="section-title">押金减免</div>
            <div class="section-content">
              <el-progress :percentage="Number(levelInfo.depositReduction)" status="success" />
              <span style="margin-left: 16px; color: #67c23a; font-weight: 600">
                减免 {{ levelInfo.depositReduction }}%
              </span>
            </div>
          </div>

          <el-divider />

          <div class="benefits-section">
            <div class="section-title">专属服务</div>
            <div class="benefits-tags">
              <el-tag
                v-for="(service, idx) in levelInfo.serviceList"
                :key="idx"
                type="success"
                effect="light"
                size="large"
                class="benefit-tag"
              >
                {{ service }}
              </el-tag>
              <span v-if="!levelInfo.serviceList || levelInfo.serviceList.length === 0" class="empty-text">
                暂无专属服务
              </span>
            </div>
          </div>

          <el-divider />

          <div class="benefits-section">
            <div class="section-title">其他权益</div>
            <div class="benefits-tags">
              <el-tag
                v-for="(benefit, idx) in levelInfo.otherBenefitList"
                :key="idx"
                type="warning"
                effect="light"
                size="large"
                class="benefit-tag"
              >
                {{ benefit }}
              </el-tag>
              <span v-if="!levelInfo.otherBenefitList || levelInfo.otherBenefitList.length === 0" class="empty-text">
                暂无其他权益
              </span>
            </div>
          </div>

          <el-divider />

          <div class="upgrade-info">
            <div class="upgrade-title">升级保级</div>
            <el-descriptions :column="2" border size="small">
              <el-descriptions-item label="升级条件">
                {{ levelInfo.upgradeType === 1 ? '累计消费' : '累计积分' }}
                <span style="color: #e6a23c; font-weight: 600">{{ levelInfo.upgradeCondition }}</span>
                {{ levelInfo.upgradeType === 1 ? '元' : '分' }}
              </el-descriptions-item>
              <el-descriptions-item label="保级条件">
                年消费 <span style="color: #67c23a; font-weight: 600">{{ levelInfo.keepCondition }}</span> 元
              </el-descriptions-item>
            </el-descriptions>
          </div>
        </el-card>
      </el-tab-pane>

      <el-tab-pane label="积分明细" name="points">
        <el-card shadow="never">
          <template #header>
            <div style="display: flex; justify-content: space-between; align-items: center">
              <span>积分记录</span>
              <div>
                <span style="margin-right: 16px">
                  当前积分：<span style="color: #e6a23c; font-weight: 600; font-size: 16px">
                    {{ formatNumber(memberInfo.currentPoints) }}
                  </span>
                </span>
              </div>
            </div>
          </template>
          <el-table :data="pointLogs" stripe border style="width: 100%" v-loading="pointLogsLoading">
            <el-table-column prop="createTime" label="时间" width="170">
              <template #default="{ row }">
                {{ formatDateTime(row.createTime) }}
              </template>
            </el-table-column>
            <el-table-column label="类型" width="100">
              <template #default="{ row }">
                <el-tag v-if="row.pointType === 1" type="success" size="small">获取</el-tag>
                <el-tag v-else type="warning" size="small">使用</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="points" label="积分" width="120" align="right">
              <template #default="{ row }">
                <span v-if="row.pointType === 1" style="color: #67c23a">+{{ formatNumber(row.points) }}</span>
                <span v-else style="color: #e6a23c">-{{ formatNumber(row.points) }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="balanceAfter" label="变动后余额" width="120" align="right" />
            <el-table-column label="原因" width="140">
              <template #default="{ row }">
                {{ reasonTypeText(row.reasonType) }}
              </template>
            </el-table-column>
            <el-table-column prop="detail" label="说明" min-width="200" />
            <el-table-column prop="operatorName" label="操作人" width="100" />
          </el-table>
          <div class="pagination">
            <el-pagination
              v-model:current-page="pointLogsPage.pageNum"
              v-model:page-size="pointLogsPage.pageSize"
              :total="pointLogsPage.total"
              layout="total, prev, pager, next"
              @current-change="handlePointLogsPageChange"
            />
          </div>
        </el-card>
      </el-tab-pane>

      <el-tab-pane label="等级变更记录" name="levelLogs">
        <el-card shadow="never">
          <el-timeline v-if="levelLogs.length > 0">
            <el-timeline-item
              v-for="log in levelLogs"
              :key="log.id"
              :timestamp="formatDateTime(log.createTime)"
              placement="top"
            >
              <el-card shadow="never" class="level-log-card">
                <div class="log-header">
                  <span class="log-type">
                    <el-tag v-if="log.changeType === 1" type="success">升级</el-tag>
                    <el-tag v-else-if="log.changeType === 2" type="warning">降级</el-tag>
                    <el-tag v-else type="primary">手动调整</el-tag>
                  </span>
                  <span class="log-operator">操作人：{{ log.operatorName || '系统' }}</span>
                </div>
                <div class="log-content">
                  <span v-if="log.oldLevelName">{{ log.oldLevelName }}</span>
                  <span v-if="log.oldLevelName" class="arrow">→</span>
                  <span class="new-level">{{ log.newLevelName }}</span>
                </div>
                <div class="log-reason">原因：{{ log.reason || '-' }}</div>
              </el-card>
            </el-timeline-item>
          </el-timeline>
          <el-empty v-else description="暂无等级变更记录" />
        </el-card>
      </el-tab-pane>

      <el-tab-pane label="入住历史" name="stays">
        <el-card shadow="never">
          <el-empty description="入住历史功能开发中" />
        </el-card>
      </el-tab-pane>
    </el-tabs>

    <el-dialog
      v-model="usePointsDialogVisible"
      title="积分抵扣"
      width="520px"
      destroy-on-close
    >
      <el-form ref="usePointsFormRef" :model="usePointsForm" :rules="usePointsRules" label-width="110px">
        <el-form-item label="会员姓名">
          <span>{{ memberInfo.customerName }}</span>
        </el-form-item>
        <el-form-item label="当前可用积分">
          <span style="color: #e6a23c; font-weight: 600; font-size: 16px">{{ formatNumber(memberInfo.currentPoints) }}</span>
        </el-form-item>
        <el-form-item label="订单金额" prop="orderAmount">
          <el-input-number v-model="usePointsForm.orderAmount" :min="0" :precision="2" style="width: 100%" placeholder="请输入订单金额" />
        </el-form-item>
        <el-form-item label="使用积分" prop="points">
          <el-slider
            v-model="usePointsForm.points"
            :max="memberInfo.currentPoints || 0"
            :step="1"
            show-input
            :show-input-controls="false"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="可抵扣金额">
          <span style="color: #67c23a; font-weight: 600; font-size: 16px">¥{{ usePointsDeduction }}</span>
        </el-form-item>
        <el-form-item label="抵扣后应付">
          <span style="font-weight: 600; font-size: 16px" :style="{ color: usePointsForm.orderAmount - usePointsDeduction > 0 ? '#f56c6c' : '#67c23a' }">
            ¥{{ Math.max(0, usePointsForm.orderAmount - usePointsDeduction).toFixed(2) }}
          </span>
        </el-form-item>
        <el-alert v-if="usePointsErrorMsg" :title="usePointsErrorMsg" type="error" show-icon :closable="false" style="margin-bottom: 12px" />
      </el-form>
      <template #footer>
        <el-button @click="usePointsDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="usePointsDialogSaving" :disabled="!!usePointsErrorMsg" @click="handleUsePointsSubmit">确认抵扣</el-button>
      </template>
    </el-dialog>

    <el-dialog
      v-model="pointsDialogVisible"
      title="发放积分"
      width="480px"
      destroy-on-close
    >
      <el-form ref="pointsFormRef" :model="pointsForm" :rules="pointsRules" label-width="100px">
        <el-form-item label="会员姓名">
          <span>{{ memberInfo.customerName }}</span>
        </el-form-item>
        <el-form-item label="当前积分">
          <span style="color: #e6a23c; font-weight: 600">{{ formatNumber(memberInfo.currentPoints) }}</span>
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
        <el-form-item label="当前等级">
          <span class="level-badge" :style="{ background: memberInfo.levelColor + '20', color: memberInfo.levelColor }">
            {{ memberInfo.levelName }}
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
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Coin, Medal, Lock, Unlock, ArrowLeft, Money, Dish
} from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import api from '@/api'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const hasPermission = (p) => userStore.hasPermission(p)

const memberId = computed(() => route.params.id)

const loading = ref(false)
const memberInfo = ref({})
const levelInfo = ref(null)
const levelOptions = ref([])
const activeTab = ref('basic')

const pointLogs = ref([])
const pointLogsLoading = ref(false)
const pointLogsPage = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0
})

const levelLogs = ref([])

const usePointsDialogVisible = ref(false)
const usePointsDialogSaving = ref(false)
const usePointsFormRef = ref(null)
const usePointsForm = reactive({
  orderAmount: 0,
  points: 0
})
const usePointsRules = {
  orderAmount: [{ required: true, message: '请输入订单金额', trigger: 'blur' }],
  points: [{ required: true, message: '请输入使用积分', trigger: 'blur' }]
}
const usePointsConsumeRule = ref(null)

const usePointsDeduction = computed(() => {
  if (!usePointsConsumeRule.value || usePointsForm.points <= 0) return 0
  const rule = usePointsConsumeRule.value
  const ratio = Number(rule.exchangeAmount) / Number(rule.exchangePoints)
  let deduction = usePointsForm.points * ratio
  if (rule.deductionCap && rule.deductionCap > 0 && usePointsForm.orderAmount > 0) {
    const maxDeduction = usePointsForm.orderAmount * Number(rule.deductionCap) / 100
    deduction = Math.min(deduction, maxDeduction)
  }
  return deduction.toFixed(2)
})

const usePointsErrorMsg = computed(() => {
  if (!usePointsConsumeRule.value) return '暂无可用的积分抵现规则'
  const rule = usePointsConsumeRule.value
  if (usePointsForm.points <= 0) return ''
  if (rule.maxPointsPerUse && usePointsForm.points > rule.maxPointsPerUse) {
    return `单次最多使用 ${rule.maxPointsPerUse} 积分`
  }
  if (rule.minOrderAmount && usePointsForm.orderAmount < Number(rule.minOrderAmount)) {
    return `订单金额需满 ${rule.minOrderAmount} 元才能使用积分`
  }
  if (usePointsForm.points > (memberInfo.value.currentPoints || 0)) {
    return '积分不足'
  }
  return ''
})

const pointsDialogVisible = ref(false)
const pointsDialogSaving = ref(false)
const pointsFormRef = ref(null)
const pointsForm = reactive({
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
  newLevelId: null,
  reason: ''
})

const levelRules = {
  newLevelId: [{ required: true, message: '请选择新等级', trigger: 'change' }],
  reason: [{ required: true, message: '请输入调整原因', trigger: 'blur' }]
}

const genderText = computed(() => {
  const gender = memberInfo.value.customer?.gender
  if (gender === 1) return '男'
  if (gender === 2) return '女'
  return '-'
})

const idTypeText = computed(() => {
  const idType = memberInfo.value.customer?.idType
  if (idType === 1) return '身份证'
  if (idType === 2) return '护照'
  if (idType === 3) return '其他'
  return '-'
})

const registerSourceText = computed(() => {
  const source = memberInfo.value.registerSource
  if (source === 1) return '前台注册'
  if (source === 2) return '官网注册'
  if (source === 3) return 'APP注册'
  return '-'
})

const loadMemberInfo = async () => {
  loading.value = true
  try {
    const res = await api.member.getById(memberId.value)
    if (res.code === 200) {
      memberInfo.value = res.data || {}
      levelInfo.value = res.data?.memberLevel || null
    }
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

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

const loadPointLogs = async () => {
  pointLogsLoading.value = true
  try {
    const res = await api.member.pointLogPage(memberId.value, {
      pageNum: pointLogsPage.pageNum,
      pageSize: pointLogsPage.pageSize
    })
    if (res.code === 200) {
      pointLogs.value = res.data.list || []
      pointLogsPage.total = res.data.total || 0
    }
  } catch (e) {
    console.error(e)
  } finally {
    pointLogsLoading.value = false
  }
}

const loadLevelLogs = async () => {
  try {
    const res = await api.member.levelLogs(memberId.value)
    if (res.code === 200) {
      levelLogs.value = res.data || []
    }
  } catch (e) {
    console.error(e)
  }
}

const handlePointLogsPageChange = (page) => {
  pointLogsPage.pageNum = page
  loadPointLogs()
}

const handleAddPoints = () => {
  pointsForm.points = 0
  pointsForm.reasonType = 2
  pointsForm.detail = ''
  pointsDialogVisible.value = true
}

const handleUsePoints = async () => {
  usePointsForm.orderAmount = 0
  usePointsForm.points = 0
  try {
    const res = await api.pointRule.consumeEnabled()
    if (res.code === 200 && res.data) {
      const cashRule = res.data.find(r => r.ruleType === 1)
      if (cashRule) {
        usePointsConsumeRule.value = cashRule
      }
    }
  } catch (e) {
    console.error(e)
  }
  usePointsDialogVisible.value = true
}

const handleUsePointsSubmit = async () => {
  if (!usePointsFormRef.value) return
  try {
    await usePointsFormRef.value.validate()
  } catch {
    return
  }

  if (usePointsErrorMsg.value) {
    ElMessage.warning(usePointsErrorMsg.value)
    return
  }

  usePointsDialogSaving.value = true
  try {
    const res = await api.member.usePointsWithRule({
      memberId: memberId.value,
      points: usePointsForm.points,
      orderAmount: usePointsForm.orderAmount
    })
    if (res.code === 200) {
      ElMessage.success(`积分抵扣成功，抵扣金额：¥${usePointsDeduction.value}`)
      usePointsDialogVisible.value = false
      loadMemberInfo()
      loadPointLogs()
    }
  } catch (e) {
    console.error(e)
  } finally {
    usePointsDialogSaving.value = false
  }
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
      memberId: memberId.value,
      points: pointsForm.points,
      reasonType: pointsForm.reasonType,
      reason: reasonMap[pointsForm.reasonType],
      detail: pointsForm.detail
    })
    if (res.code === 200) {
      ElMessage.success('积分发放成功')
      pointsDialogVisible.value = false
      loadMemberInfo()
      loadPointLogs()
    }
  } catch (e) {
    console.error(e)
  } finally {
    pointsDialogSaving.value = false
  }
}

const handleAdjustLevel = () => {
  levelForm.newLevelId = memberInfo.value.levelId
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
      memberId: memberId.value,
      newLevelId: levelForm.newLevelId,
      reason: levelForm.reason
    })
    if (res.code === 200) {
      ElMessage.success('等级调整成功')
      levelDialogVisible.value = false
      loadMemberInfo()
      loadLevelLogs()
    }
  } catch (e) {
    console.error(e)
  } finally {
    levelDialogSaving.value = false
  }
}

const handleFreeze = () => {
  ElMessageBox.prompt('确定要冻结该会员吗？', '提示', {
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
      const res = await api.member.freeze(memberId.value, { reason: value })
      if (res.code === 200) {
        ElMessage.success('冻结成功')
        loadMemberInfo()
      }
    } catch (e) {
      console.error(e)
    }
  }).catch(() => {})
}

const handleUnfreeze = () => {
  ElMessageBox.prompt('确定要解冻该会员吗？', '提示', {
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
      const res = await api.member.unfreeze(memberId.value, { reason: value })
      if (res.code === 200) {
        ElMessage.success('解冻成功')
        loadMemberInfo()
      }
    } catch (e) {
      console.error(e)
    }
  }).catch(() => {})
}

const handleBack = () => {
  router.back()
}

const reasonTypeText = (type) => {
  const map = {
    1: '消费赠送',
    2: '活动赠送',
    3: '客诉补偿',
    4: '生日礼遇',
    5: '推荐奖励',
    6: '积分兑换',
    7: '其他'
  }
  return map[type] || '其他'
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
  loadMemberInfo()
  loadPointLogs()
  loadLevelLogs()
})
</script>

<style scoped>
.member-detail-container {
  padding: 16px;
}

.detail-header {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 12px;
  padding: 24px;
  margin-bottom: 16px;
  color: #fff;
}

.member-info {
  display: flex;
  align-items: center;
  gap: 20px;
}

.member-avatar {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(255, 255, 255, 0.2);
}

.member-main {
  flex: 1;
}

.member-name-row {
  display: flex;
  align-items: center;
  margin-bottom: 8px;
}

.member-name {
  font-size: 24px;
  font-weight: 600;
}

.member-no {
  font-size: 14px;
  opacity: 0.9;
  margin-bottom: 8px;
}

.member-level {
  margin-top: 8px;
}

.level-badge {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 4px 12px;
  border-radius: 12px;
  font-size: 13px;
  font-weight: 500;
}

.member-stats {
  display: flex;
  gap: 40px;
}

.stat-item {
  text-align: center;
}

.stat-value {
  font-size: 20px;
  font-weight: 600;
  margin-bottom: 4px;
}

.stat-label {
  font-size: 13px;
  opacity: 0.85;
}

.header-actions {
  display: flex;
  gap: 10px;
  margin-top: 16px;
  justify-content: flex-end;
}

.info-card {
  margin-bottom: 16px;
}

.card-title {
  font-weight: 600;
  color: #303133;
}

.benefits-card {
  max-width: 900px;
  margin: 0 auto;
}

.benefits-header {
  display: flex;
  align-items: center;
  gap: 16px;
}

.level-icon-big {
  font-size: 48px;
}

.level-name-big {
  font-size: 20px;
  font-weight: 600;
}

.level-desc {
  font-size: 13px;
  color: #909399;
  margin-top: 4px;
}

.benefit-item {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 16px;
  background: #fafafa;
  border-radius: 8px;
}

.benefit-icon {
  width: 48px;
  height: 48px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
}

.benefit-title {
  font-size: 13px;
  color: #909399;
}

.benefit-value {
  font-size: 20px;
  font-weight: 600;
  color: #303133;
}

.benefits-section {
  margin: 16px 0;
}

.section-title {
  font-size: 14px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 12px;
}

.section-content {
  display: flex;
  align-items: center;
}

.benefits-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.benefit-tag {
  padding: 8px 16px;
  font-size: 13px;
}

.empty-text {
  color: #909399;
  font-size: 13px;
}

.upgrade-info {
  margin-top: 16px;
}

.upgrade-title {
  font-size: 14px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 12px;
}

.pagination {
  margin-top: 16px;
  text-align: right;
}

.level-log-card {
  margin: 8px 0;
}

.log-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.log-operator {
  font-size: 12px;
  color: #909399;
}

.log-content {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 15px;
  margin-bottom: 8px;
}

.arrow {
  color: #909399;
}

.new-level {
  font-weight: 600;
  color: #409eff;
}

.log-reason {
  font-size: 13px;
  color: #606266;
}
</style>
