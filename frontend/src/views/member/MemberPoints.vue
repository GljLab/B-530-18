<template>
  <div class="member-points-container" v-loading="loading">
    <div class="page-header">
      <el-button @click="goBack" :icon="ArrowLeft">返回</el-button>
      <h2 class="page-title">积分明细</h2>
    </div>

    <el-row :gutter="16" v-if="memberInfo">
      <el-col :span="8">
        <el-card shadow="hover" class="summary-card">
          <div class="summary-value" style="color: #409eff">{{ formatNumber(summary.currentPoints) }}</div>
          <div class="summary-label">当前可用积分</div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="hover" class="summary-card">
          <div class="summary-value" style="color: #67c23a">{{ formatNumber(summary.totalEarned) }}</div>
          <div class="summary-label">累计获得积分</div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="hover" class="summary-card">
          <div class="summary-value" style="color: #e6a23c">{{ formatNumber(summary.totalUsed) }}</div>
          <div class="summary-label">累计使用积分</div>
        </el-card>
      </el-col>
    </el-row>

    <el-card shadow="never" style="margin-top: 16px">
      <template #header>
        <div style="display: flex; justify-content: space-between; align-items: center">
          <span>积分明细</span>
          <div style="display: flex; gap: 12px; align-items: center">
            <el-date-picker v-model="dateRange" type="daterange" range-separator="至" start-placeholder="开始日期" end-placeholder="结束日期" value-format="YYYY-MM-DD" style="width: 280px" @change="handleSearch" />
            <el-select v-model="filterType" placeholder="全部类型" clearable style="width: 120px" @change="handleSearch">
              <el-option label="获取" :value="1" />
              <el-option label="使用" :value="2" />
            </el-select>
          </div>
        </div>
      </template>
      <el-table :data="pointLogs" border style="width: 100%">
        <el-table-column label="时间" width="170">
          <template #default="{ row }">{{ formatTime(row.createTime) }}</template>
        </el-table-column>
        <el-table-column label="类型" width="80">
          <template #default="{ row }">
            <el-tag v-if="row.pointType === 1" type="success" size="small">获取</el-tag>
            <el-tag v-else type="warning" size="small">使用</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="方式" width="110">
          <template #default="{ row }">{{ reasonTypeMap[row.reasonType] || '其他' }}</template>
        </el-table-column>
        <el-table-column label="积分变动" width="120">
          <template #default="{ row }">
            <span :style="{ color: row.pointType === 1 ? '#67c23a' : '#f56c6c', fontWeight: 'bold' }">
              {{ row.pointType === 1 ? '+' : '-' }}{{ row.points }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="reason" label="说明" min-width="160" />
        <el-table-column label="关联订单" width="110">
          <template #default="{ row }">
            <span v-if="row.relatedOrderType === 1">入住单</span>
            <span v-else-if="row.relatedOrderType === 2">预订单</span>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column label="积分余额" width="120">
          <template #default="{ row }">{{ row.balanceAfter }}</template>
        </el-table-column>
        <el-table-column prop="operatorName" label="操作人" width="100" />
      </el-table>

      <div style="margin-top: 16px; display: flex; justify-content: flex-end">
        <el-pagination
          v-model:current-page="pageNum"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 50]"
          :total="total"
          layout="total, sizes, prev, pager, next"
          @size-change="loadPointLogs"
          @current-change="loadPointLogs"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ArrowLeft } from '@element-plus/icons-vue'
import api from '@/api'

const route = useRoute()
const router = useRouter()

const loading = ref(false)
const memberInfo = ref(null)
const summary = ref({ currentPoints: 0, totalEarned: 0, totalUsed: 0 })
const pointLogs = ref([])
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)
const filterType = ref(null)
const dateRange = ref(null)

const reasonTypeMap = { 1: '消费赠送', 2: '活动赠送', 3: '客诉补偿', 4: '生日礼遇', 5: '推荐奖励', 6: '积分兑换', 7: '其他' }

const memberId = ref(null)

const goBack = () => {
  router.back()
}

const formatNumber = (val) => {
  if (val === null || val === undefined) return '0'
  return Number(val).toLocaleString()
}

const formatTime = (time) => {
  if (!time) return '-'
  return time.substring(0, 16).replace('T', ' ')
}

const loadMemberInfo = async () => {
  if (!memberId.value) return
  try {
    const res = await api.member.getById(memberId.value)
    if (res.code === 200) memberInfo.value = res.data
  } catch (e) { console.error(e) }
}

const loadSummary = async () => {
  if (!memberId.value) return
  try {
    const res = await api.member.pointSummary(memberId.value)
    if (res.code === 200) summary.value = res.data
  } catch (e) { console.error(e) }
}

const loadPointLogs = async () => {
  if (!memberId.value) return
  loading.value = true
  try {
    const params = { pageNum: pageNum.value, pageSize: pageSize.value }
    if (filterType.value) params.pointType = filterType.value
    if (dateRange.value && dateRange.value.length === 2) {
      params.startTime = dateRange.value[0]
      params.endTime = dateRange.value[1]
    }
    const res = await api.member.pointLogPageFiltered(memberId.value, params)
    if (res.code === 200) {
      pointLogs.value = res.data.records || []
      total.value = res.data.total || 0
    }
  } catch (e) { console.error(e) }
  loading.value = false
}

const handleSearch = () => {
  pageNum.value = 1
  loadPointLogs()
}

onMounted(() => {
  memberId.value = route.query.memberId || route.params.id
  if (memberId.value) {
    loadMemberInfo()
    loadSummary()
    loadPointLogs()
  }
})
</script>

<style scoped>
.member-points-container { padding: 16px; }
.page-header { display: flex; align-items: center; gap: 12px; margin-bottom: 16px; }
.page-title { font-size: 18px; font-weight: 600; color: #303133; margin: 0; }
.summary-card { text-align: center; padding: 10px 0; }
.summary-value { font-size: 32px; font-weight: 700; margin-bottom: 8px; }
.summary-label { font-size: 14px; color: #909399; }
</style>
