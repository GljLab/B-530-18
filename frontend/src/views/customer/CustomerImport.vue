<template>
  <div class="customer-import-container">
    <el-card shadow="never" class="upload-card">
      <template #header>
        <span class="card-title">批量导入客户</span>
      </template>
      <div class="upload-actions">
        <el-button type="primary" @click="handleDownloadTemplate">
          <el-icon><Download /></el-icon>下载模板
        </el-button>
      </div>
      <el-upload
        ref="uploadRef"
        class="upload-area"
        drag
        :auto-upload="false"
        :limit="1"
        accept=".xlsx,.xls"
        :on-change="handleFileChange"
        :on-exceed="handleExceed"
        :on-remove="handleRemove"
      >
        <el-icon class="el-icon--upload"><UploadFilled /></el-icon>
        <div class="el-upload__text">
          将文件拖到此处，或<em>点击上传</em>
        </div>
        <template #tip>
          <div class="el-upload__tip">仅支持 .xlsx / .xls 文件</div>
        </template>
      </el-upload>
      <div v-if="selectedFile" class="confirm-row">
        <el-button type="primary" :loading="importLoading" @click="handleImport">
          开始导入
        </el-button>
      </div>
    </el-card>

    <el-card v-if="importResult" shadow="never" class="result-card">
      <template #header>
        <span class="card-title">导入结果</span>
      </template>
      <div class="statistic-row">
        <el-statistic title="成功" :value="importResult.successCount">
          <template #suffix>条</template>
        </el-statistic>
        <el-statistic title="失败" :value="importResult.failCount">
          <template #suffix>条</template>
        </el-statistic>
      </div>
      <el-table
        v-if="importResult.failCount > 0"
        :data="importResult.failList"
        stripe
        border
        style="width: 100%; margin-top: 20px"
      >
        <el-table-column prop="row" label="行号" width="100" align="center" />
        <el-table-column prop="reason" label="原因" min-width="300" />
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import { Download, UploadFilled } from '@element-plus/icons-vue'
import api from '@/api'

const uploadRef = ref(null)
const selectedFile = ref(null)
const importLoading = ref(false)
const importResult = ref(null)

const handleDownloadTemplate = async () => {
  try {
    const blob = await api.customer.downloadImportTemplate()
    const url = window.URL.createObjectURL(new Blob([blob]))
    const link = document.createElement('a')
    link.href = url
    link.setAttribute('download', '客户导入模板.xlsx')
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    window.URL.revokeObjectURL(url)
  } catch {
    ElMessage.error('下载模板失败')
  }
}

const handleFileChange = (uploadFile) => {
  selectedFile.value = uploadFile
  importResult.value = null
}

const handleExceed = () => {
  ElMessage.warning('只能上传一个文件，请先移除已选文件')
}

const handleRemove = () => {
  selectedFile.value = null
}

const handleImport = async () => {
  if (!selectedFile.value) {
    ElMessage.warning('请先选择文件')
    return
  }
  importLoading.value = true
  importResult.value = null
  try {
    const formData = new FormData()
    formData.append('file', selectedFile.value.raw)
    const res = await api.customer.importCustomers(formData)
    if (res.code === 200) {
      importResult.value = {
        successCount: res.data?.successCount ?? 0,
        failCount: res.data?.failCount ?? 0,
        failList: res.data?.failList ?? []
      }
      if (importResult.value.failCount === 0) {
        ElMessage.success(`导入成功，共${importResult.value.successCount}条`)
      } else {
        ElMessage.warning(`部分导入失败，成功${importResult.value.successCount}条，失败${importResult.value.failCount}条`)
      }
    } else {
      ElMessage.error(res.message || '导入失败')
    }
  } catch {
    ElMessage.error('导入失败')
  } finally {
    importLoading.value = false
  }
}
</script>

<style scoped>
.customer-import-container {
  padding: 10px;
}

.upload-card {
  border-radius: 12px;
  border: none;
  margin-bottom: 12px;
}

.card-title {
  font-size: 16px;
  font-weight: 600;
}

.upload-actions {
  margin-bottom: 16px;
}

.upload-area {
  width: 100%;
}

.confirm-row {
  margin-top: 16px;
}

.result-card {
  border-radius: 12px;
  border: none;
}

.statistic-row {
  display: flex;
  gap: 60px;
}
</style>
