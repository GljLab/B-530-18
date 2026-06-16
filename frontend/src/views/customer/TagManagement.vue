<template>
  <div class="customer-list-container">
    <el-card shadow="never" class="search-card">
      <div class="search-row">
        <div style="flex: 1" />
        <el-button v-if="hasPermission('customer:tag:manage')" type="primary" @click="handleAdd">
          <el-icon><Plus /></el-icon>新增标签
        </el-button>
      </div>
    </el-card>

    <el-card shadow="never" class="table-card">
      <el-table :data="tagList" stripe border v-loading="tableLoading" style="width: 100%">
        <el-table-column prop="tagName" label="标签名称" min-width="160" />
        <el-table-column prop="customerCount" label="使用客户数" width="140" align="center" />
        <el-table-column prop="createTime" label="创建时间" width="180" align="center" />
        <el-table-column label="操作" width="160" align="center" fixed="right">
          <template #default="{ row }">
            <el-button
              v-if="hasPermission('customer:tag:manage')"
              type="primary"
              link
              size="small"
              @click="handleEdit(row)"
            >编辑</el-button>
            <el-button
              v-if="hasPermission('customer:tag:manage')"
              type="danger"
              link
              size="small"
              @click="handleDelete(row)"
            >删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-card shadow="never" class="table-card" style="margin-top: 12px">
      <template #header>
        <span>标签统计</span>
      </template>
      <el-table :data="statisticsData" stripe border style="width: 100%">
        <el-table-column prop="tagName" label="标签名称" min-width="160" />
        <el-table-column prop="customerCount" label="使用客户数" width="140" align="center" />
      </el-table>
    </el-card>

    <el-dialog
      v-model="dialogVisible"
      :title="dialogForm.id ? '编辑标签' : '新增标签'"
      width="480px"
      destroy-on-close
    >
      <el-form ref="formRef" :model="dialogForm" :rules="formRules" label-width="80px">
        <el-form-item label="标签名称" prop="tagName">
          <el-input v-model="dialogForm.tagName" placeholder="请输入标签名称" maxlength="30" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="dialogSaving" @click="handleSubmit">确认</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import api from '@/api'

const userStore = useUserStore()
const hasPermission = (p) => userStore.hasPermission(p)

const tagList = ref([])
const statisticsData = ref([])
const tableLoading = ref(false)

const dialogVisible = ref(false)
const dialogSaving = ref(false)
const formRef = ref(null)
const dialogForm = reactive({
  id: null,
  tagName: ''
})

const formRules = {
  tagName: [{ required: true, message: '请输入标签名称', trigger: 'blur' }]
}

const loadTagList = async () => {
  tableLoading.value = true
  try {
    const res = await api.customer.getTagList()
    if (res.code === 200) {
      tagList.value = res.data || []
    }
  } catch {
    tagList.value = []
  } finally {
    tableLoading.value = false
  }
}

const loadStatistics = async () => {
  try {
    const res = await api.customer.getTagStatistics()
    if (res.code === 200) {
      statisticsData.value = res.data || []
    }
  } catch {
    statisticsData.value = []
  }
}

const handleAdd = () => {
  dialogForm.id = null
  dialogForm.tagName = ''
  dialogVisible.value = true
}

const handleEdit = (row) => {
  dialogForm.id = row.id
  dialogForm.tagName = row.tagName
  dialogVisible.value = true
}

const handleSubmit = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  dialogSaving.value = true
  try {
    const payload = { tagName: dialogForm.tagName }
    const res = dialogForm.id
      ? await api.customer.updateTag({ ...payload, id: dialogForm.id })
      : await api.customer.addTag(payload)
    if (res.code === 200) {
      ElMessage.success(dialogForm.id ? '编辑成功' : '新增成功')
      dialogVisible.value = false
      loadTagList()
      loadStatistics()
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
  const warning = row.customerCount > 0
    ? `该标签下有 ${row.customerCount} 个客户正在使用，删除后将从这些客户中移除该标签，是否继续？`
    : '确定删除该标签？'
  ElMessageBox.confirm(warning, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      const res = await api.customer.deleteTag(row.id)
      if (res.code === 200) {
        ElMessage.success('删除成功')
        loadTagList()
        loadStatistics()
      } else {
        ElMessage.error(res.message || '删除失败')
      }
    } catch {
      ElMessage.error('删除失败')
    }
  }).catch(() => {})
}

onMounted(() => {
  loadTagList()
  loadStatistics()
})
</script>

<style scoped>
.customer-list-container {
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

.table-card {
  border-radius: 12px;
  border: none;
}

.pagination-wrap {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}
</style>
