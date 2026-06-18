<template>
  <div class="agreement-unit-container">
    <el-card shadow="never" class="search-card">
      <div class="search-row">
        <el-input
          v-model="queryParams.unitName"
          placeholder="单位名称"
          clearable
          style="width: 200px"
          @keyup.enter="handleSearch"
        />
        <el-select v-model="queryParams.status" placeholder="状态" clearable style="width: 120px">
          <el-option label="全部" :value="null" />
          <el-option label="启用" :value="1" />
          <el-option label="禁用" :value="0" />
        </el-select>
        <el-select v-model="queryParams.cooperationType" placeholder="合作类型" clearable style="width: 140px">
          <el-option label="全部" :value="null" />
          <el-option label="长期协议" :value="1" />
          <el-option label="临时协议" :value="2" />
        </el-select>
        <el-button type="primary" @click="handleSearch">
          <el-icon><Search /></el-icon>搜索
        </el-button>
        <div style="flex: 1" />
        <el-button type="primary" @click="handleAdd">
          <el-icon><Plus /></el-icon>新增协议单位
        </el-button>
      </div>
    </el-card>

    <el-card shadow="never" class="table-card">
      <el-table :data="tableData" v-loading="tableLoading" border stripe style="width: 100%">
        <el-table-column prop="unitName" label="单位名称" min-width="160" show-overflow-tooltip />
        <el-table-column prop="creditCode" label="统一社会信用代码" width="200" show-overflow-tooltip />
        <el-table-column label="合作类型" width="110" align="center">
          <template #default="{ row }">
            {{ row.cooperationType === 1 ? '长期协议' : row.cooperationType === 2 ? '临时协议' : '-' }}
          </template>
        </el-table-column>
        <el-table-column label="结算周期" width="110" align="center">
          <template #default="{ row }">
            {{ settlementCycleLabel(row.settlementCycle) }}
          </template>
        </el-table-column>
        <el-table-column label="信用额度" width="130" align="right">
          <template #default="{ row }">
            {{ formatMoney(row.creditLimit) }}
          </template>
        </el-table-column>
        <el-table-column label="当前欠款" width="130" align="right">
          <template #default="{ row }">
            <span :class="{ 'debt-red': row.currentDebt > 0 }">{{ formatMoney(row.currentDebt) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="额度使用率" width="160" align="center">
          <template #default="{ row }">
            <el-progress
              :percentage="calcUsageRate(row)"
              :color="usageColor(calcUsageRate(row))"
              :stroke-width="14"
              :text-inside="true"
            />
          </template>
        </el-table-column>
        <el-table-column label="协议折扣" width="100" align="center">
          <template #default="{ row }">
            {{ formatDiscount(row.discountRate) }}
          </template>
        </el-table-column>
        <el-table-column label="状态" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">
              {{ row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" align="center" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="handleEdit(row)">编辑</el-button>
            <el-button
              :type="row.status === 1 ? 'danger' : 'success'"
              link
              size="small"
              @click="handleToggleStatus(row)"
            >{{ row.status === 1 ? '禁用' : '启用' }}</el-button>
            <el-button type="danger" link size="small" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div class="pagination-wrap">
        <el-pagination
          v-model:current-page="queryParams.pageNum"
          v-model:page-size="queryParams.pageSize"
          :page-sizes="[10, 20, 50, 100]"
          :total="total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="loadData"
          @current-change="loadData"
        />
      </div>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="800px" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="120px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="单位名称" prop="unitName">
              <el-input v-model="form.unitName" placeholder="请输入单位名称" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="统一社会信用代码" prop="creditCode">
              <el-input v-model="form.creditCode" placeholder="请输入统一社会信用代码" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="单位地址" prop="address">
              <el-input v-model="form.address" placeholder="请输入单位地址" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="联系电话" prop="phone">
              <el-input v-model="form.phone" placeholder="请输入联系电话" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="联系人姓名" prop="contactName">
              <el-input v-model="form.contactName" placeholder="请输入联系人姓名" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="联系人职位" prop="contactPosition">
              <el-input v-model="form.contactPosition" placeholder="请输入联系人职位" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="联系人手机号" prop="contactPhone">
              <el-input v-model="form.contactPhone" placeholder="请输入联系人手机号" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="联系人邮箱" prop="contactEmail">
              <el-input v-model="form.contactEmail" placeholder="请输入联系人邮箱" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="合作类型" prop="cooperationType">
              <el-select v-model="form.cooperationType" placeholder="请选择合作类型" style="width: 100%">
                <el-option label="长期协议" :value="1" />
                <el-option label="临时协议" :value="2" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="签约日期" prop="signDate">
              <el-date-picker
                v-model="form.signDate"
                type="date"
                placeholder="请选择签约日期"
                value-format="YYYY-MM-DD"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="协议有效期" prop="validUntil">
              <el-date-picker
                v-model="form.validUntil"
                type="date"
                placeholder="请选择协议有效期"
                value-format="YYYY-MM-DD"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="结算周期" prop="settlementCycle">
              <el-select v-model="form.settlementCycle" placeholder="请选择结算周期" style="width: 100%">
                <el-option label="月结" :value="1" />
                <el-option label="季度结" :value="2" />
                <el-option label="半年结" :value="3" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="信用额度" prop="creditLimit">
              <el-input-number v-model="form.creditLimit" :min="0.01" :precision="2" :step="1000" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="协议折扣" prop="discountRate">
              <el-input-number v-model="form.discountRate" :min="0.01" :max="1" :step="0.05" :precision="2" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="备注" prop="remark">
              <el-input v-model="form.remark" type="textarea" :rows="3" placeholder="请输入备注" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="协议附件" prop="attachment">
              <el-upload
                :auto-upload="false"
                :limit="1"
                accept=".pdf"
                :on-change="handleFileChange"
                :on-remove="handleFileRemove"
                :file-list="fileList"
              >
                <el-button size="small">选择文件</el-button>
                <template #tip>
                  <div class="upload-tip">仅支持PDF格式，单个文件</div>
                </template>
              </el-upload>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Plus } from '@element-plus/icons-vue'
import api from '@/api'

const formatMoney = (amount) => {
  if (amount === null || amount === undefined) return '¥0.00'
  return '¥' + Number(amount).toFixed(2).replace(/\B(?=(\d{3})+(?!\d))/g, ',')
}

const formatDiscount = (rate) => {
  if (!rate) return '-'
  return (rate * 10).toFixed(rate * 10 % 1 === 0 ? 0 : 1) + '折'
}

const settlementCycleLabel = (val) => {
  const map = { 1: '月结', 2: '季度结', 3: '半年结' }
  return map[val] || '-'
}

const calcUsageRate = (row) => {
  if (!row.creditLimit || row.creditLimit <= 0) return 0
  const rate = (row.currentDebt || 0) / row.creditLimit * 100
  return Math.min(Math.round(rate * 100) / 100, 100)
}

const usageColor = (percentage) => {
  if (percentage > 95) return '#f56c6c'
  if (percentage > 80) return '#e6a23c'
  return '#409eff'
}

const tableData = ref([])
const total = ref(0)
const tableLoading = ref(false)
const dialogVisible = ref(false)
const submitLoading = ref(false)
const formRef = ref(null)
const fileList = ref([])

const queryParams = reactive({
  unitName: '',
  status: null,
  cooperationType: null,
  pageNum: 1,
  pageSize: 10
})

const form = reactive({
  id: null,
  unitName: '',
  creditCode: '',
  address: '',
  phone: '',
  contactName: '',
  contactPosition: '',
  contactPhone: '',
  contactEmail: '',
  cooperationType: null,
  signDate: '',
  validUntil: '',
  settlementCycle: null,
  creditLimit: null,
  discountRate: null,
  remark: '',
  attachment: null
})

const dialogTitle = computed(() => form.id ? '编辑协议单位' : '新增协议单位')

const rules = {
  unitName: [{ required: true, message: '请输入单位名称', trigger: 'blur' }],
  creditLimit: [{ required: true, message: '请输入信用额度', trigger: 'blur' }]
}

const loadData = async () => {
  tableLoading.value = true
  try {
    const params = {
      pageNum: queryParams.pageNum,
      pageSize: queryParams.pageSize,
      unitName: queryParams.unitName || undefined,
      status: queryParams.status !== null ? queryParams.status : undefined,
      cooperationType: queryParams.cooperationType !== null ? queryParams.cooperationType : undefined
    }
    const res = await api.finance.agreementUnit.page(params)
    if (res.code === 200) {
      tableData.value = res.data?.records || res.data?.list || []
      total.value = res.data?.total || 0
    }
  } catch {
    tableData.value = []
    total.value = 0
  } finally {
    tableLoading.value = false
  }
}

const handleSearch = () => {
  queryParams.pageNum = 1
  loadData()
}

const resetForm = () => {
  form.id = null
  form.unitName = ''
  form.creditCode = ''
  form.address = ''
  form.phone = ''
  form.contactName = ''
  form.contactPosition = ''
  form.contactPhone = ''
  form.contactEmail = ''
  form.cooperationType = null
  form.signDate = ''
  form.validUntil = ''
  form.settlementCycle = null
  form.creditLimit = null
  form.discountRate = null
  form.remark = ''
  form.attachment = null
  fileList.value = []
}

const handleAdd = () => {
  resetForm()
  dialogVisible.value = true
}

const handleEdit = async (row) => {
  resetForm()
  try {
    const res = await api.finance.agreementUnit.get(row.id)
    if (res.code === 200 && res.data) {
      Object.assign(form, {
        id: res.data.id,
        unitName: res.data.unitName || '',
        creditCode: res.data.creditCode || '',
        address: res.data.address || '',
        phone: res.data.phone || '',
        contactName: res.data.contactName || '',
        contactPosition: res.data.contactPosition || '',
        contactPhone: res.data.contactPhone || '',
        contactEmail: res.data.contactEmail || '',
        cooperationType: res.data.cooperationType,
        signDate: res.data.signDate || '',
        validUntil: res.data.validUntil || '',
        settlementCycle: res.data.settlementCycle,
        creditLimit: res.data.creditLimit,
        discountRate: res.data.discountRate,
        remark: res.data.remark || '',
        attachment: res.data.attachment || null
      })
      if (res.data.attachmentName) {
        fileList.value = [{ name: res.data.attachmentName, url: res.data.attachment }]
      }
    }
    dialogVisible.value = true
  } catch {
    ElMessage.error('加载协议单位信息失败')
  }
}

const handleToggleStatus = async (row) => {
  const newStatus = row.status === 1 ? 0 : 1
  const actionText = newStatus === 1 ? '启用' : '禁用'
  try {
    await ElMessageBox.confirm(`确定${actionText}协议单位 "${row.unitName}" 吗？`, '提示', { type: 'warning' })
    const res = await api.finance.agreementUnit.updateStatus(row.id, { status: newStatus })
    if (res.code === 200) {
      ElMessage.success(`${actionText}成功`)
      loadData()
    } else {
      ElMessage.error(res.message || '操作失败')
    }
  } catch (e) {
    if (e !== 'cancel') {
      ElMessage.error('操作失败')
    }
  }
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(`确定删除协议单位 "${row.unitName}" 吗？`, '提示', { type: 'warning' })
    const res = await api.finance.agreementUnit.delete(row.id)
    if (res.code === 200) {
      ElMessage.success('删除成功')
      loadData()
    } else {
      ElMessage.error(res.message || '删除失败')
    }
  } catch (e) {
    if (e !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

const handleFileChange = (file) => {
  form.attachment = file.raw
  fileList.value = [file]
}

const handleFileRemove = () => {
  form.attachment = null
  fileList.value = []
}

const handleSubmit = async () => {
  if (!formRef.value) return
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  submitLoading.value = true
  try {
    const submitData = { ...form }
    if (submitData.attachment && submitData.attachment instanceof File) {
      const formData = new FormData()
      Object.keys(submitData).forEach((key) => {
        if (key === 'attachment') {
          formData.append('file', submitData.attachment)
        } else if (submitData[key] !== null && submitData[key] !== undefined) {
          formData.append(key, submitData[key])
        }
      })
      if (form.id) {
        const res = await api.finance.agreementUnit.update(formData)
        if (res.code === 200) {
          ElMessage.success('修改成功')
        } else {
          ElMessage.error(res.message || '修改失败')
          return
        }
      } else {
        const res = await api.finance.agreementUnit.add(formData)
        if (res.code === 200) {
          ElMessage.success('新增成功')
        } else {
          ElMessage.error(res.message || '新增失败')
          return
        }
      }
    } else {
      delete submitData.attachment
      if (form.id) {
        const res = await api.finance.agreementUnit.update(submitData)
        if (res.code === 200) {
          ElMessage.success('修改成功')
        } else {
          ElMessage.error(res.message || '修改失败')
          return
        }
      } else {
        const res = await api.finance.agreementUnit.add(submitData)
        if (res.code === 200) {
          ElMessage.success('新增成功')
        } else {
          ElMessage.error(res.message || '新增失败')
          return
        }
      }
    }
    dialogVisible.value = false
    loadData()
  } catch {
    ElMessage.error('操作失败')
  } finally {
    submitLoading.value = false
  }
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.agreement-unit-container {
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
  flex-wrap: wrap;
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

.debt-red {
  color: #f56c6c;
  font-weight: 600;
}

.upload-tip {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
}
</style>
