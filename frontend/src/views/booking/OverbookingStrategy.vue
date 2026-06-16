<template>
  <div class="overbooking-strategy-container">
    <div class="page-header">
      <h2 class="page-title">超售策略配置</h2>
    </div>

    <el-card shadow="hover" class="global-switch-card">
      <div class="global-switch-row">
        <span class="switch-label">全局超售开关</span>
        <el-switch
          v-model="globalEnabled"
          active-text="开启"
          inactive-text="关闭"
          @change="handleGlobalSwitch"
        />
      </div>
    </el-card>

    <el-card shadow="hover" class="table-card">
      <div class="table-toolbar">
        <el-button type="primary" :loading="saveLoading" @click="handleSaveAll">保存全部</el-button>
      </div>

      <el-table :data="strategyList" stripe border v-loading="tableLoading" style="width: 100%">
        <el-table-column prop="typeName" label="房型名称" min-width="140" />
        <el-table-column prop="totalRooms" label="总房量" width="100" align="center" />
        <el-table-column label="超售开关" width="120" align="center">
          <template #default="{ row }">
            <el-switch v-model="row.overbookingEnabled" />
          </template>
        </el-table-column>
        <el-table-column label="超售比例" width="180" align="center">
          <template #default="{ row }">
            <el-input-number
              v-model="row.overbookingRatio"
              :min="0"
              :max="50"
              :step="5"
              :precision="0"
              :disabled="!row.overbookingEnabled"
              size="small"
              style="width: 120px"
            />
            <span style="margin-left: 4px">%</span>
          </template>
        </el-table-column>
        <el-table-column label="最大可售" width="120" align="center">
          <template #default="{ row }">
            <span :class="{ 'highlight-value': row.overbookingEnabled && row.overbookingRatio > 0 }">
              {{ calcMaxSalable(row) }}
            </span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="100" align="center">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="handleEditRow(row)">编辑</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="editDialogVisible" title="编辑超售策略" width="480px" destroy-on-close>
      <el-form ref="editFormRef" :model="editForm" :rules="editRules" label-width="100px">
        <el-form-item label="房型名称">
          <span>{{ editForm.typeName }}</span>
        </el-form-item>
        <el-form-item label="超售开关" prop="overbookingEnabled">
          <el-switch v-model="editForm.overbookingEnabled" />
        </el-form-item>
        <el-form-item label="超售比例" prop="overbookingRatio">
          <el-input-number
            v-model="editForm.overbookingRatio"
            :min="0"
            :max="50"
            :step="5"
            :precision="0"
            :disabled="!editForm.overbookingEnabled"
            style="width: 100%"
          />
          <span style="margin-left: 4px">%</span>
        </el-form-item>
        <el-form-item label="最大可售">
          <span class="highlight-value">{{ calcMaxSalable(editForm) }}</span>
          <span style="margin-left: 8px; color: #909399; font-size: 12px">= 可售 × (1 + 比例)</span>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="editSaving" @click="handleEditSave">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import api from '@/api'

const globalEnabled = ref(false)
const strategyList = ref([])
const tableLoading = ref(false)
const saveLoading = ref(false)

const editDialogVisible = ref(false)
const editSaving = ref(false)
const editFormRef = ref(null)
const editForm = reactive({
  roomTypeId: null,
  typeName: '',
  totalRooms: 0,
  availableRooms: 0,
  overbookingEnabled: false,
  overbookingRatio: 0
})
const editRules = {}

const calcMaxSalable = (row) => {
  const available = row.availableRooms ?? row.totalRooms ?? 0
  const ratio = row.overbookingEnabled ? (row.overbookingRatio || 0) / 100 : 0
  return Math.floor(available * (1 + ratio))
}

const loadGlobalSwitch = async () => {
  try {
    const res = await api.overbooking.global()
    if (res.code === 200 && res.data) {
      globalEnabled.value = !!res.data.enabled
    }
  } catch {}
}

const handleGlobalSwitch = async (val) => {
  try {
    const res = await api.overbooking.updateGlobal({ enabled: val ? 1 : 0 })
    if (res.code === 200) {
      ElMessage.success(val ? '全局超售已开启' : '全局超售已关闭')
    } else {
      globalEnabled.value = !val
      ElMessage.error(res.message || '操作失败')
    }
  } catch {
    globalEnabled.value = !val
    ElMessage.error('操作失败')
  }
}

const loadStrategies = async () => {
  tableLoading.value = true
  try {
    const res = await api.overbooking.list()
    if (res.code === 200) {
      strategyList.value = (res.data || []).map(item => ({
        ...item,
        overbookingEnabled: !!item.enabled,
        overbookingRatio: item.overbookingRatio != null ? Math.round(item.overbookingRatio * 100) : 0
      }))
    }
  } catch {
    strategyList.value = []
  } finally {
    tableLoading.value = false
  }
}

const handleEditRow = (row) => {
  editForm.roomTypeId = row.roomTypeId
  editForm.typeName = row.typeName
  editForm.totalRooms = row.totalRooms
  editForm.availableRooms = row.availableRooms ?? row.totalRooms
  editForm.overbookingEnabled = row.overbookingEnabled
  editForm.overbookingRatio = row.overbookingRatio
  editDialogVisible.value = true
}

const handleEditSave = async () => {
  editSaving.value = true
  try {
    const res = await api.overbooking.save({
      roomTypeId: editForm.roomTypeId,
      enabled: editForm.overbookingEnabled ? 1 : 0,
      overbookingRatio: (editForm.overbookingRatio || 0) / 100
    })
    if (res.code === 200) {
      ElMessage.success('保存成功')
      editDialogVisible.value = false
      await loadStrategies()
    } else {
      ElMessage.error(res.message || '保存失败')
    }
  } catch {
    ElMessage.error('保存失败')
  } finally {
    editSaving.value = false
  }
}

const handleSaveAll = async () => {
  saveLoading.value = true
  try {
    for (const row of strategyList.value) {
      const res = await api.overbooking.save({
        roomTypeId: row.roomTypeId,
        enabled: row.overbookingEnabled ? 1 : 0,
        overbookingRatio: (row.overbookingRatio || 0) / 100
      })
      if (res.code !== 200) {
        ElMessage.error(res.message || '保存失败')
        return
      }
    }
    ElMessage.success('保存成功')
    await loadStrategies()
  } catch {
    ElMessage.error('保存失败')
  } finally {
    saveLoading.value = false
  }
}

onMounted(() => {
  loadGlobalSwitch()
  loadStrategies()
})
</script>

<style scoped>
.overbooking-strategy-container {
  padding: 16px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.page-title {
  font-size: 20px;
  font-weight: 600;
  color: #2d3748;
  margin: 0;
}

.global-switch-card {
  margin-bottom: 16px;
}

.global-switch-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.switch-label {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.table-card {
  margin-bottom: 16px;
}

.table-toolbar {
  display: flex;
  justify-content: flex-end;
  margin-bottom: 16px;
}

.highlight-value {
  color: #e6a23c;
  font-weight: 700;
  font-size: 15px;
}
</style>
