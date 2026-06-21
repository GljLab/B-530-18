<template>
  <div class="task-manage" v-loading="pageLoading">
    <el-card shadow="never" style="margin-bottom: 16px">
      <template #header>
        <div class="card-header">
          <span>定时任务列表</span>
        </div>
      </template>
      <el-table :data="taskList" stripe border style="width: 100%">
        <el-table-column prop="taskName" label="任务名称" width="180" />
        <el-table-column prop="description" label="任务说明" min-width="280" />
        <el-table-column prop="cronExpression" label="执行频率" width="200">
          <template #default="{ row }">
            <el-tag type="info" size="small">{{ row.cronExpression }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag v-if="row.status === 1" type="success" size="small">启用</el-tag>
            <el-tag v-else type="danger" size="small">禁用</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" align="center" fixed="right">
          <template #default="{ row }">
            <el-button
              type="primary"
              size="small"
              :icon="VideoPlay"
              :loading="triggeringTask === row.taskType"
              :disabled="!hasTriggerPermission"
              @click="handleTriggerTask(row)"
            >
              手动执行
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <span>任务执行日志</span>
        </div>
      </template>

      <el-form :inline="true" :model="logForm" style="margin-bottom: 16px">
        <el-form-item label="任务类型">
          <el-select v-model="logForm.taskType" placeholder="全部" clearable style="width: 160px">
            <el-option label="升级检查" :value="1" />
            <el-option label="保级降级检查" :value="2" />
            <el-option label="年度消费重置" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item label="执行结果">
          <el-select v-model="logForm.executeResult" placeholder="全部" clearable style="width: 140px">
            <el-option label="成功" :value="1" />
            <el-option label="失败" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item label="执行时间">
          <el-date-picker
            v-model="logForm.dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            value-format="YYYY-MM-DD"
            style="width: 280px"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="loadTaskLogs">查询</el-button>
          <el-button :icon="Refresh" @click="resetLogForm">重置</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="taskLogList" stripe border style="width: 100%" v-loading="logsLoading">
        <el-table-column prop="taskName" label="任务名称" width="200" />
        <el-table-column label="任务类型" width="140">
          <template #default="{ row }">
            <el-tag v-if="row.taskType === 1" type="primary" size="small">升级检查</el-tag>
            <el-tag v-else-if="row.taskType === 2" type="warning" size="small">保级降级</el-tag>
            <el-tag v-else type="info" size="small">年度重置</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="executeTime" label="执行时间" width="180">
          <template #default="{ row }">
            {{ formatDateTime(row.executeTime) }}
          </template>
        </el-table-column>
        <el-table-column label="执行结果" width="100" align="center">
          <template #default="{ row }">
            <el-tag v-if="row.executeResult === 1" type="success" size="small">成功</el-tag>
            <el-tag v-else type="danger" size="small">失败</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="processCount" label="处理会员数" width="120" align="center" />
        <el-table-column prop="upgradeCount" label="升级人数" width="100" align="center">
          <template #default="{ row }">
            <span v-if="row.upgradeCount > 0" style="color: #67c23a; font-weight: 600">
              {{ row.upgradeCount }}
            </span>
            <span v-else>0</span>
          </template>
        </el-table-column>
        <el-table-column prop="downgradeCount" label="降级人数" width="100" align="center">
          <template #default="{ row }">
            <span v-if="row.downgradeCount > 0" style="color: #e6a23c; font-weight: 600">
              {{ row.downgradeCount }}
            </span>
            <span v-else>0</span>
          </template>
        </el-table-column>
        <el-table-column prop="resetCount" label="重置人数" width="100" align="center" />
        <el-table-column label="触发方式" width="100">
          <template #default="{ row }">
            <el-tag v-if="row.triggerType === 1" type="info" size="small">系统定时</el-tag>
            <el-tag v-else type="warning" size="small">手动触发</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="operatorName" label="操作人" width="100">
          <template #default="{ row }">
            {{ row.operatorName || '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="errorMsg" label="异常信息" min-width="200" show-overflow-tooltip>
          <template #default="{ row }">
            <span v-if="row.errorMsg" style="color: #f56c6c">{{ row.errorMsg }}</span>
            <span v-else style="color: #909399">-</span>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination">
        <el-pagination
          v-model:current-page="logPage.pageNum"
          v-model:page-size="logPage.pageSize"
          :page-sizes="[10, 20, 50]"
          :total="logPage.total"
          layout="total, sizes, prev, pager, next"
          @size-change="handleLogSizeChange"
          @current-change="handleLogPageChange"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { VideoPlay, Search, Refresh } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import api from '@/api'

const userStore = useUserStore()
const hasTriggerPermission = userStore.hasPermission('member:task:trigger')

const pageLoading = ref(false)
const logsLoading = ref(false)
const triggeringTask = ref(null)

const taskList = ref([])

const logForm = reactive({
  taskType: null,
  executeResult: null,
  dateRange: []
})

const taskLogList = ref([])
const logPage = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0
})

const loadTaskList = async () => {
  pageLoading.value = true
  try {
    const res = await api.memberLevelTask.list()
    if (res.code === 200) {
      taskList.value = res.data || []
    }
  } catch (e) {
    console.error(e)
  } finally {
    pageLoading.value = false
  }
}

const loadTaskLogs = async () => {
  logsLoading.value = true
  try {
    const params = {
      pageNum: logPage.pageNum,
      pageSize: logPage.pageSize,
      taskType: logForm.taskType || undefined,
      executeResult: logForm.executeResult || undefined,
      startTime: logForm.dateRange?.[0] || undefined,
      endTime: logForm.dateRange?.[1] || undefined
    }
    const res = await api.memberLevelTask.logPage(params)
    if (res.code === 200) {
      taskLogList.value = res.data?.list || []
      logPage.total = res.data?.total || 0
    }
  } catch (e) {
    console.error(e)
  } finally {
    logsLoading.value = false
  }
}

const resetLogForm = () => {
  logForm.taskType = null
  logForm.executeResult = null
  logForm.dateRange = []
  logPage.pageNum = 1
  loadTaskLogs()
}

const handleTriggerTask = async (task) => {
  try {
    await ElMessageBox.confirm(
      `确定要手动执行任务【${task.taskName}】吗？`,
      '提示',
      {
        confirmButtonText: '确定执行',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
  } catch {
    return
  }

  triggeringTask.value = task.taskType
  try {
    const res = await api.memberLevelTask.trigger(task.taskType)
    if (res.code === 200) {
      ElMessage.success('任务执行成功')
      loadTaskLogs()
    }
  } catch (e) {
    console.error(e)
  } finally {
    triggeringTask.value = null
  }
}

const handleLogPageChange = (page) => {
  logPage.pageNum = page
  loadTaskLogs()
}

const handleLogSizeChange = (size) => {
  logPage.pageSize = size
  logPage.pageNum = 1
  loadTaskLogs()
}

const formatDateTime = (datetime) => {
  if (!datetime) return ''
  return datetime.replace('T', ' ').substring(0, 19)
}

onMounted(() => {
  loadTaskList()
  loadTaskLogs()
})
</script>

<style scoped>
.task-manage {
  padding: 16px;
}

.card-header {
  font-weight: 600;
  color: #303133;
}

.pagination {
  margin-top: 16px;
  text-align: right;
}
</style>
