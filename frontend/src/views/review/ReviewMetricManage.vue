<template>
  <div class="review-metric-container">
    <el-card shadow="never" class="search-card">
      <div class="search-row">
        <div class="weight-info">
          <span class="weight-label">当前权重总和：</span>
          <span :class="['weight-value', { 'warning': totalWeight !== 100 }]">{{ totalWeight }}%</span>
          <el-alert
            v-if="totalWeight !== 100"
            type="warning"
            :closable="false"
            show-icon
            size="small"
            class="weight-alert"
          >
            启用指标的权重总和应为100%，当前为{{ totalWeight }}%
          </el-alert>
        </div>
        <div style="flex: 1" />
        <el-button v-if="hasPermission('review:metric:query')" type="success" plain @click="handlePreview">
          <el-icon><View /></el-icon>预览
        </el-button>
        <el-button v-if="hasPermission('review:metric:add')" type="primary" @click="handleAdd">
          <el-icon><Plus /></el-icon>新增指标
        </el-button>
      </div>
    </el-card>

    <el-card shadow="never" class="table-card">
      <el-table :data="metricList" stripe border v-loading="tableLoading" style="width: 100%">
        <el-table-column prop="metricName" label="指标名称" min-width="140">
          <template #default="{ row }">
            <span>
              {{ row.metricName }}
              <el-tag v-if="row.isRequired === 1" type="danger" size="small" class="required-tag">必评</el-tag>
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="metricDesc" label="指标说明" min-width="200" show-overflow-tooltip />
        <el-table-column label="评分范围" width="100" align="center">
          <template #default="{ row }">
            {{ row.scoreMin }}-{{ row.scoreMax }}分
          </template>
        </el-table-column>
        <el-table-column prop="weight" label="权重(%)" width="100" align="center" />
        <el-table-column prop="sortOrder" label="显示排序" width="100" align="center" />
        <el-table-column prop="status" label="启用状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">
              {{ row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180" align="center" />
        <el-table-column label="操作" width="240" align="center" fixed="right">
          <template #default="{ row }">
            <el-button
              v-if="hasPermission('review:metric:edit')"
              type="primary"
              link
              size="small"
              @click="handleEdit(row)"
            >编辑</el-button>
            <el-button
              v-if="hasPermission('review:metric:status')"
              :type="row.status === 1 ? 'warning' : 'success'"
              link
              size="small"
              @click="handleToggleStatus(row)"
            >{{ row.status === 1 ? '禁用' : '启用' }}</el-button>
            <el-button
              v-if="hasPermission('review:metric:delete')"
              type="danger"
              link
              size="small"
              @click="handleDelete(row)"
            >删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog
      v-model="dialogVisible"
      :title="dialogForm.id ? '编辑评价指标' : '新增评价指标'"
      width="560px"
      destroy-on-close
    >
      <el-form ref="formRef" :model="dialogForm" :rules="formRules" label-width="100px">
        <el-form-item label="指标名称" prop="metricName">
          <el-input v-model="dialogForm.metricName" placeholder="请输入指标名称" maxlength="50" show-word-limit />
        </el-form-item>
        <el-form-item label="指标说明" prop="metricDesc">
          <el-input
            v-model="dialogForm.metricDesc"
            type="textarea"
            :rows="3"
            placeholder="请输入指标说明（选填）"
            maxlength="500"
            show-word-limit
          />
        </el-form-item>
        <el-form-item label="评分范围">
          <el-input disabled>
            <template #prefix>1-5分（5分制星级评分）</template>
          </el-input>
        </el-form-item>
        <el-form-item label="权重(%)" prop="weight">
          <el-input-number v-model="dialogForm.weight" :min="0" :max="100" :step="5" />
          <span style="margin-left: 8px; color: #909399; font-size: 12px">综合评分中的权重百分比</span>
        </el-form-item>
        <el-form-item label="是否必评" prop="isRequired">
          <el-switch v-model="dialogForm.isRequired" :active-value="1" :inactive-value="0" />
          <span style="margin-left: 8px; color: #909399; font-size: 12px">客人必须评分才能提交</span>
        </el-form-item>
        <el-form-item label="显示排序" prop="sortOrder">
          <el-input-number v-model="dialogForm.sortOrder" :min="0" :step="1" />
          <span style="margin-left: 8px; color: #909399; font-size: 12px">数字越小越靠前</span>
        </el-form-item>
        <el-form-item label="启用状态" prop="status">
          <el-switch v-model="dialogForm.status" :active-value="1" :inactive-value="0" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="dialogSaving" @click="handleSubmit">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="previewVisible" title="评价指标预览" width="600px" destroy-on-close>
      <div class="preview-container">
        <h3 style="margin: 0 0 16px; text-align: center">请为本次入住打分</h3>
        <div
          v-for="metric in enabledMetrics"
          :key="metric.id"
          class="preview-metric-item"
        >
          <div class="preview-metric-name">
            {{ metric.metricName }}
            <span v-if="metric.isRequired === 1" class="preview-required">*</span>
            <span class="preview-weight">(权重{{ metric.weight }}%)</span>
          </div>
          <div class="preview-metric-desc" v-if="metric.metricDesc">{{ metric.metricDesc }}</div>
          <el-rate v-model="previewScores[metric.id]" :max="5" allow-half />
        </div>
        <div class="preview-tip">
          <el-icon><InfoFilled /></el-icon>
          综合评分 = Σ(单项评分 × 权重%)，带<span class="preview-required">*</span>为必评项
        </div>
        <div class="preview-composite" v-if="compositeScore > 0">
          综合评分：<span class="composite-score">{{ compositeScore.toFixed(1) }}</span> 分
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, View, InfoFilled } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import api from '@/api'

const userStore = useUserStore()
const hasPermission = (p) => userStore.hasPermission(p)

const metricList = ref([])
const enabledMetrics = ref([])
const totalWeight = ref(0)
const tableLoading = ref(false)

const dialogVisible = ref(false)
const dialogSaving = ref(false)
const formRef = ref(null)
const dialogForm = reactive({
  id: null,
  metricName: '',
  metricDesc: '',
  scoreMin: 1,
  scoreMax: 5,
  weight: 0,
  isRequired: 0,
  sortOrder: 0,
  status: 1
})

const formRules = {
  metricName: [{ required: true, message: '请输入指标名称', trigger: 'blur' }],
  weight: [{ required: true, message: '请输入权重', trigger: 'blur' }],
  sortOrder: [{ required: true, message: '请输入显示排序', trigger: 'blur' }]
}

const previewVisible = ref(false)
const previewScores = reactive({})

const compositeScore = computed(() => {
  let total = 0
  enabledMetrics.value.forEach(m => {
    const score = previewScores[m.id] || 0
    total += score * (m.weight / 100)
  })
  return total
})

const loadMetricList = async () => {
  tableLoading.value = true
  try {
    const res = await api.reviewMetric.list()
    if (res.code === 200) {
      metricList.value = res.data || []
    }
  } catch {
    metricList.value = []
  } finally {
    tableLoading.value = false
  }
}

const loadTotalWeight = async () => {
  try {
    const res = await api.reviewMetric.getTotalWeight()
    if (res.code === 200) {
      totalWeight.value = res.data?.totalWeight || 0
    }
  } catch {
    totalWeight.value = 0
  }
}

const handleAdd = () => {
  dialogForm.id = null
  dialogForm.metricName = ''
  dialogForm.metricDesc = ''
  dialogForm.scoreMin = 1
  dialogForm.scoreMax = 5
  dialogForm.weight = 0
  dialogForm.isRequired = 0
  dialogForm.sortOrder = 0
  dialogForm.status = 1
  dialogVisible.value = true
}

const handleEdit = (row) => {
  dialogForm.id = row.id
  dialogForm.metricName = row.metricName
  dialogForm.metricDesc = row.metricDesc || ''
  dialogForm.scoreMin = row.scoreMin
  dialogForm.scoreMax = row.scoreMax
  dialogForm.weight = row.weight
  dialogForm.isRequired = row.isRequired
  dialogForm.sortOrder = row.sortOrder
  dialogForm.status = row.status
  dialogVisible.value = true
}

const handleSubmit = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  dialogSaving.value = true
  try {
    const payload = {
      metricName: dialogForm.metricName,
      metricDesc: dialogForm.metricDesc,
      scoreMin: dialogForm.scoreMin,
      scoreMax: dialogForm.scoreMax,
      weight: dialogForm.weight,
      isRequired: dialogForm.isRequired,
      sortOrder: dialogForm.sortOrder,
      status: dialogForm.status
    }
    const res = dialogForm.id
      ? await api.reviewMetric.update({ ...payload, id: dialogForm.id })
      : await api.reviewMetric.add(payload)
    if (res.code === 200) {
      ElMessage.success(dialogForm.id ? '编辑成功' : '新增成功')
      dialogVisible.value = false
      loadMetricList()
      loadTotalWeight()
    } else {
      ElMessage.error(res.message || '操作失败')
    }
  } catch {
    ElMessage.error('操作失败')
  } finally {
    dialogSaving.value = false
  }
}

const handleDelete = (row) => {
  ElMessageBox.confirm('确定删除该评价指标？删除后无法恢复。', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      const res = await api.reviewMetric.delete(row.id)
      if (res.code === 200) {
        ElMessage.success('删除成功')
        loadMetricList()
        loadTotalWeight()
      } else {
        ElMessage.error(res.message || '删除失败')
      }
    } catch {
      ElMessage.error('删除失败')
    }
  }).catch(() => {})
}

const handleToggleStatus = (row) => {
  const action = row.status === 1 ? '禁用' : '启用'
  ElMessageBox.confirm(`确定${action}该评价指标？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      const newStatus = row.status === 1 ? 0 : 1
      const res = await api.reviewMetric.updateStatus(row.id, newStatus)
      if (res.code === 200) {
        ElMessage.success(`${action}成功`)
        loadMetricList()
        loadTotalWeight()
      } else {
        ElMessage.error(res.message || `${action}失败`)
      }
    } catch {
      ElMessage.error(`${action}失败`)
    }
  }).catch(() => {})
}

const handlePreview = async () => {
  try {
    const res = await api.reviewMetric.listEnabled()
    if (res.code === 200) {
      enabledMetrics.value = res.data || []
      enabledMetrics.value.forEach(m => {
        previewScores[m.id] = 0
      })
      previewVisible.value = true
    }
  } catch {
    enabledMetrics.value = []
  }
}

onMounted(() => {
  loadMetricList()
  loadTotalWeight()
})
</script>

<style scoped>
.review-metric-container {
  padding: 10px;
}

.search-card {
  border-radius: 12px;
  border: none;
  margin-bottom: 12px;
}

.search-row {
  display: flex;
  align-items: center;
  gap: 10px;
}

.weight-info {
  display: flex;
  align-items: center;
  gap: 8px;
}

.weight-label {
  font-size: 14px;
  color: #606266;
}

.weight-value {
  font-size: 18px;
  font-weight: 600;
  color: #67c23a;
}

.weight-value.warning {
  color: #e6a23c;
}

.weight-alert {
  margin-left: 12px;
}

.table-card {
  border-radius: 12px;
  border: none;
}

.required-tag {
  margin-left: 6px;
}

.preview-container {
  padding: 10px;
}

.preview-metric-item {
  padding: 12px 0;
  border-bottom: 1px solid #f0f0f0;
}

.preview-metric-item:last-child {
  border-bottom: none;
}

.preview-metric-name {
  font-size: 14px;
  color: #303133;
  margin-bottom: 6px;
  font-weight: 500;
}

.preview-required {
  color: #f56c6c;
  margin-left: 2px;
}

.preview-weight {
  color: #909399;
  font-size: 12px;
  margin-left: 6px;
  font-weight: normal;
}

.preview-metric-desc {
  font-size: 12px;
  color: #909399;
  margin-bottom: 8px;
}

.preview-tip {
  margin-top: 16px;
  padding: 10px;
  background: #f5f7fa;
  border-radius: 6px;
  font-size: 12px;
  color: #909399;
  display: flex;
  align-items: center;
  gap: 4px;
}

.preview-composite {
  margin-top: 16px;
  padding: 14px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 8px;
  text-align: center;
  color: #fff;
  font-size: 16px;
}

.composite-score {
  font-size: 28px;
  font-weight: bold;
  margin: 0 4px;
}
</style>
