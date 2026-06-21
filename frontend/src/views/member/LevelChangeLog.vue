<template>
  <div class="level-change-log" v-loading="loading">
    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <span>等级变更记录</span>
        </div>
      </template>

      <el-form :inline="true" :model="queryForm" style="margin-bottom: 16px">
        <el-form-item label="会员卡号">
          <el-input
            v-model="queryForm.memberNo"
            placeholder="请输入会员卡号"
            clearable
            style="width: 200px"
          />
        </el-form-item>
        <el-form-item label="变更类型">
          <el-select v-model="queryForm.changeType" placeholder="全部" clearable style="width: 140px">
            <el-option label="升级" :value="1" />
            <el-option label="降级" :value="2" />
            <el-option label="手动调整" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item label="触发方式">
          <el-select v-model="queryForm.triggerType" placeholder="全部" clearable style="width: 140px">
            <el-option label="系统自动" :value="1" />
            <el-option label="管理员手动" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item label="变更时间">
          <el-date-picker
            v-model="queryForm.dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            value-format="YYYY-MM-DD"
            style="width: 280px"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="loadLogs">查询</el-button>
          <el-button :icon="Refresh" @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="logList" stripe border style="width: 100%" v-loading="loading">
        <el-table-column prop="createTime" label="变更时间" width="180">
          <template #default="{ row }">
            {{ formatDateTime(row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column prop="memberNo" label="会员卡号" width="180" />
        <el-table-column label="变更类型" width="100" align="center">
          <template #default="{ row }">
            <el-tag v-if="row.changeType === 1" type="success" size="small">升级</el-tag>
            <el-tag v-else-if="row.changeType === 2" type="warning" size="small">降级</el-tag>
            <el-tag v-else type="primary" size="small">手动调整</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="等级变更" width="260">
          <template #default="{ row }">
            <div class="level-change-row">
              <span v-if="row.oldLevelName" class="old-level">{{ row.oldLevelName }}</span>
              <span v-if="row.oldLevelName" class="arrow">
                <el-icon><ArrowRightBold /></el-icon>
              </span>
              <span class="new-level">{{ row.newLevelName }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="reason" label="变更原因" min-width="280" show-overflow-tooltip />
        <el-table-column label="触发方式" width="110" align="center">
          <template #default="{ row }">
            <el-tag v-if="row.triggerType === 1" type="info" size="small">系统自动</el-tag>
            <el-tag v-else-if="row.triggerType === 2" type="warning" size="small">管理员</el-tag>
            <span v-else style="color: #909399">-</span>
          </template>
        </el-table-column>
        <el-table-column prop="operatorName" label="操作人" width="110">
          <template #default="{ row }">
            {{ row.operatorName || (row.triggerType === 1 ? '系统' : '-') }}
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination">
        <el-pagination
          v-model:current-page="page.pageNum"
          v-model:page-size="page.pageSize"
          :page-sizes="[10, 20, 50]"
          :total="page.total"
          layout="total, sizes, prev, pager, next"
          @size-change="handleSizeChange"
          @current-change="handlePageChange"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { Search, Refresh, ArrowRightBold } from '@element-plus/icons-vue'
import api from '@/api'

const loading = ref(false)
const logList = ref([])

const queryForm = reactive({
  memberNo: '',
  changeType: null,
  triggerType: null,
  dateRange: []
})

const page = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0
})

const loadLogs = async () => {
  loading.value = true
  try {
    const params = {
      pageNum: page.pageNum,
      pageSize: page.pageSize,
      memberNo: queryForm.memberNo || undefined,
      changeType: queryForm.changeType || undefined,
      triggerType: queryForm.triggerType || undefined,
      startTime: queryForm.dateRange?.[0] || undefined,
      endTime: queryForm.dateRange?.[1] || undefined
    }
    const res = await api.memberLevelChange.logPage(params)
    if (res.code === 200) {
      logList.value = res.data?.list || []
      page.total = res.data?.total || 0
    }
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

const resetQuery = () => {
  queryForm.memberNo = ''
  queryForm.changeType = null
  queryForm.triggerType = null
  queryForm.dateRange = []
  page.pageNum = 1
  loadLogs()
}

const handlePageChange = (p) => {
  page.pageNum = p
  loadLogs()
}

const handleSizeChange = (size) => {
  page.pageSize = size
  page.pageNum = 1
  loadLogs()
}

const formatDateTime = (datetime) => {
  if (!datetime) return ''
  return datetime.replace('T', ' ').substring(0, 19)
}

onMounted(() => {
  loadLogs()
})
</script>

<style scoped>
.level-change-log {
  padding: 16px;
}

.card-header {
  font-weight: 600;
  color: #303133;
}

.level-change-row {
  display: flex;
  align-items: center;
  gap: 8px;
}

.old-level {
  color: #909399;
}

.arrow {
  color: #c0c4cc;
  display: inline-flex;
  align-items: center;
}

.new-level {
  font-weight: 600;
  color: #409eff;
}

.pagination {
  margin-top: 16px;
  text-align: right;
}
</style>
