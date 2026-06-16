<template>
  <div class="customer-create">
    <el-card shadow="never" class="form-card">
      <template #header>
        <div class="card-header">
          <span class="card-title">新建客户</span>
        </div>
      </template>

      <el-steps :active="activeStep" finish-status="success" align-center class="step-bar">
        <el-step title="基本信息" />
        <el-step title="证件信息" />
        <el-step title="联系信息" />
        <el-step title="客户分类" />
      </el-steps>

      <el-alert
        v-if="showDuplicateWarning && !dismissedDuplicate"
        type="warning"
        :closable="false"
        class="duplicate-alert"
      >
        <template #title>
          <span>检测到相似客户</span>
        </template>
        <div class="duplicate-list">
          <div v-for="item in duplicateList" :key="item.id" class="duplicate-item">
            <span class="dup-name">{{ item.name }}</span>
            <span class="dup-phone">{{ item.phone }}</span>
            <span class="dup-id">{{ item.idNumber }}</span>
            <span class="dup-score">相似度: {{ formatSimilarity(item.similarity) }}</span>
            <el-button type="primary" link @click="router.push(`/customer/detail/${item.id}`)">查看详情</el-button>
          </div>
        </div>
        <el-button size="small" @click="dismissedDuplicate = true" style="margin-top: 8px">仍要创建</el-button>
      </el-alert>

      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="110px"
        label-position="right"
        class="step-form"
      >
        <div v-show="activeStep === 0">
          <el-row :gutter="24">
            <el-col :span="12">
              <el-form-item label="姓名" prop="name">
                <el-input v-model="form.name" placeholder="请输入客户姓名" maxlength="50" @blur="checkDuplicate" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="性别" prop="gender">
                <el-radio-group v-model="form.gender">
                  <el-radio :value="0">未知</el-radio>
                  <el-radio :value="1">男</el-radio>
                  <el-radio :value="2">女</el-radio>
                </el-radio-group>
              </el-form-item>
            </el-col>
          </el-row>
          <el-row :gutter="24">
            <el-col :span="12">
              <el-form-item label="出生日期" prop="birthDate">
                <el-date-picker
                  v-model="form.birthDate"
                  type="date"
                  placeholder="请选择出生日期"
                  style="width: 100%"
                  value-format="YYYY-MM-DD"
                />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="国籍" prop="nationality">
                <el-select v-model="form.nationality" placeholder="请选择国籍" style="width: 100%">
                  <el-option
                    v-for="item in nationalityOptions"
                    :key="item"
                    :label="item"
                    :value="item"
                  />
                </el-select>
              </el-form-item>
            </el-col>
          </el-row>
        </div>

        <div v-show="activeStep === 1">
          <el-row :gutter="24">
            <el-col :span="12">
              <el-form-item label="证件类型" prop="idType">
                <el-select v-model="form.idType" placeholder="请选择证件类型" style="width: 100%" @change="handleIdTypeChange">
                  <el-option
                    v-for="item in idTypeOptions"
                    :key="item.value"
                    :label="item.label"
                    :value="item.value"
                  />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="证件号码" prop="idNumber">
                <el-input v-model="form.idNumber" placeholder="请输入证件号码" @blur="onIdNumberBlur" />
              </el-form-item>
            </el-col>
          </el-row>
          <el-row :gutter="24">
            <el-col :span="12">
              <el-form-item label="证件有效期" prop="idExpiryDate">
                <el-date-picker
                  v-model="form.idExpiryDate"
                  type="date"
                  placeholder="请选择证件有效期"
                  style="width: 100%"
                  value-format="YYYY-MM-DD"
                />
              </el-form-item>
            </el-col>
          </el-row>
          <el-row :gutter="24">
            <el-col :span="12">
              <el-form-item label="证件正面照" prop="idPhotoFront">
                <el-upload
                  :action="uploadAction"
                  :headers="uploadHeaders"
                  :limit="1"
                  :on-success="handleFrontSuccess"
                  :before-upload="beforeUpload"
                  :on-remove="handleFrontRemove"
                  list-type="picture-card"
                  accept="image/*"
                >
                  <el-icon><Plus /></el-icon>
                </el-upload>
                <el-image
                  v-if="form.idPhotoFront && !frontFileList.length"
                  :src="form.idPhotoFront"
                  :preview-src-list="[form.idPhotoFront]"
                  fit="cover"
                  style="width: 100px; height: 100px; margin-top: 8px"
                />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="证件反面照" prop="idPhotoBack">
                <el-upload
                  :action="uploadAction"
                  :headers="uploadHeaders"
                  :limit="1"
                  :on-success="handleBackSuccess"
                  :before-upload="beforeUpload"
                  :on-remove="handleBackRemove"
                  list-type="picture-card"
                  accept="image/*"
                >
                  <el-icon><Plus /></el-icon>
                </el-upload>
                <el-image
                  v-if="form.idPhotoBack && !backFileList.length"
                  :src="form.idPhotoBack"
                  :preview-src-list="[form.idPhotoBack]"
                  fit="cover"
                  style="width: 100px; height: 100px; margin-top: 8px"
                />
              </el-form-item>
            </el-col>
          </el-row>
        </div>

        <div v-show="activeStep === 2">
          <el-row :gutter="24">
            <el-col :span="12">
              <el-form-item label="手机号码" prop="phone">
                <el-input v-model="form.phone" placeholder="请输入手机号码" @blur="checkPhoneUnique" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="备用电话" prop="backupPhone">
                <el-input v-model="form.backupPhone" placeholder="请输入备用电话" />
              </el-form-item>
            </el-col>
          </el-row>
          <el-row :gutter="24">
            <el-col :span="12">
              <el-form-item label="电子邮箱" prop="email">
                <el-input v-model="form.email" placeholder="请输入电子邮箱" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="微信号" prop="wechat">
                <el-input v-model="form.wechat" placeholder="请输入微信号" />
              </el-form-item>
            </el-col>
          </el-row>
          <el-divider content-position="left">紧急联系人</el-divider>
          <el-row :gutter="24">
            <el-col :span="8">
              <el-form-item label="联系人姓名" prop="emergencyContactName">
                <el-input v-model="form.emergencyContactName" placeholder="请输入联系人姓名" />
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item label="与本人关系" prop="emergencyContactRelation">
                <el-select v-model="form.emergencyContactRelation" placeholder="请选择关系" style="width: 100%">
                  <el-option
                    v-for="item in relationOptions"
                    :key="item"
                    :label="item"
                    :value="item"
                  />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item label="联系人电话" prop="emergencyContactPhone">
                <el-input v-model="form.emergencyContactPhone" placeholder="请输入联系人电话" />
              </el-form-item>
            </el-col>
          </el-row>
        </div>

        <div v-show="activeStep === 3">
          <el-row :gutter="24">
            <el-col :span="12">
              <el-form-item label="客户类型" prop="customerType">
                <el-select v-model="form.customerType" placeholder="请选择客户类型" style="width: 100%">
                  <el-option
                    v-for="item in customerTypeOptions"
                    :key="item.value"
                    :label="item.label"
                    :value="item.value"
                  />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="客户来源" prop="customerSource">
                <el-select v-model="form.customerSource" placeholder="请选择客户来源" style="width: 100%">
                  <el-option
                    v-for="item in customerSourceOptions"
                    :key="item.value"
                    :label="item.label"
                    :value="item.value"
                  />
                </el-select>
              </el-form-item>
            </el-col>
          </el-row>
          <el-row :gutter="24">
            <el-col :span="12">
              <el-form-item label="重要程度" prop="importance">
                <el-select v-model="form.importance" placeholder="请选择重要程度" style="width: 100%">
                  <el-option
                    v-for="item in importanceOptions"
                    :key="item.value"
                    :label="item.label"
                    :value="item.value"
                  />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="推荐人ID" prop="referrerId">
                <el-input v-model="form.referrerId" placeholder="请输入推荐人ID" />
              </el-form-item>
            </el-col>
          </el-row>
          <el-row :gutter="24">
            <el-col :span="12">
              <el-form-item label="标签">
                <el-select
                  v-model="selectedTagIds"
                  multiple
                  placeholder="请选择标签"
                  style="width: 100%"
                >
                  <el-option
                    v-for="tag in tagList"
                    :key="tag.id"
                    :label="tag.name"
                    :value="tag.id"
                  />
                </el-select>
              </el-form-item>
            </el-col>
          </el-row>
        </div>

        <el-form-item>
          <div class="form-actions">
            <el-button v-if="activeStep > 0" @click="handlePrev">上一步</el-button>
            <el-button v-if="activeStep < 3" type="primary" @click="handleNext">下一步</el-button>
            <el-button v-if="activeStep === 3" type="primary" :loading="submitting" @click="handleSave">
              保存
            </el-button>
            <el-button @click="handleCancel">取消</el-button>
          </div>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import api from '@/api'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()

const activeStep = ref(0)
const formRef = ref(null)
const submitting = ref(false)
const frontFileList = ref([])
const backFileList = ref([])
const duplicateList = ref([])
const showDuplicateWarning = ref(false)
const dismissedDuplicate = ref(false)
const tagList = ref([])
const selectedTagIds = ref([])

const nationalityOptions = [
  '中国', '美国', '英国', '日本', '韩国',
  '法国', '德国', '澳大利亚', '加拿大', '新加坡', '其他'
]

const idTypeOptions = [
  { value: 1, label: '身份证' },
  { value: 2, label: '护照' },
  { value: 3, label: '港澳通行证' },
  { value: 4, label: '台胞证' },
  { value: 5, label: '驾驶证' }
]

const customerTypeOptions = [
  { value: 1, label: '散客' },
  { value: 2, label: '企业客户' },
  { value: 3, label: '协议客户' },
  { value: 4, label: 'VIP客户' }
]

const customerSourceOptions = [
  { value: 1, label: '官网' },
  { value: 2, label: 'OTA平台' },
  { value: 3, label: '企业协议' },
  { value: 4, label: '电话预订' },
  { value: 5, label: '前台登记' },
  { value: 6, label: '老客户推荐' }
]

const importanceOptions = [
  { value: 1, label: '普通' },
  { value: 2, label: '重要' },
  { value: 3, label: 'VIP' }
]

const relationOptions = ['父母', '配偶', '朋友', '同事', '其他']

const uploadAction = '/api/file/upload'
const uploadHeaders = computed(() => ({
  Authorization: `Bearer ${userStore.token || localStorage.getItem('token') || ''}`
}))

const form = reactive({
  name: '',
  gender: 0,
  birthDate: '',
  nationality: '',
  idType: null,
  idNumber: '',
  idExpiryDate: '',
  idPhotoFront: '',
  idPhotoBack: '',
  phone: '',
  backupPhone: '',
  email: '',
  wechat: '',
  emergencyContactName: '',
  emergencyContactRelation: '',
  emergencyContactPhone: '',
  customerType: null,
  customerSource: null,
  importance: null,
  referrerId: ''
})

const validatePhone = (rule, value, callback) => {
  if (!value) {
    callback(new Error('请输入手机号码'))
  } else if (!/^1[3-9]\d{9}$/.test(value)) {
    callback(new Error('请输入正确的11位手机号码'))
  } else {
    callback()
  }
}

const validateEmail = (rule, value, callback) => {
  if (!value) {
    callback()
  } else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(value)) {
    callback(new Error('请输入正确的邮箱格式'))
  } else {
    callback()
  }
}

const validateIdNumber = (rule, value, callback) => {
  if (!value) {
    if (form.idType) {
      callback(new Error('请输入证件号码'))
    } else {
      callback()
    }
    return
  }
  if (form.idType === 1) {
    if (!/^\d{15}$|^\d{18}$/.test(value)) {
      callback(new Error('身份证号应为15或18位数字'))
    } else {
      callback()
    }
  } else if (form.idType === 2) {
    if (!/^[a-zA-Z0-9]{5,20}$/.test(value)) {
      callback(new Error('护照号应为5-20位字母数字'))
    } else {
      callback()
    }
  } else {
    callback()
  }
}

const rules = reactive({
  name: [{ required: true, message: '请输入客户姓名', trigger: 'blur' }],
  phone: [{ required: true, validator: validatePhone, trigger: 'blur' }],
  email: [{ validator: validateEmail, trigger: 'blur' }],
  idNumber: [{ validator: validateIdNumber, trigger: 'blur' }]
})

const handleIdTypeChange = () => {
  form.idNumber = ''
  formRef.value?.clearValidate('idNumber')
}

const checkPhoneUnique = async () => {
  if (!form.phone || !/^1[3-9]\d{9}$/.test(form.phone)) return
  try {
    const res = await api.customer.getPage({ phone: form.phone, pageNum: 1, pageSize: 1 })
    if (res.code === 200 && res.data?.records?.length > 0) {
      ElMessage.warning('该手机号码已存在')
    }
  } catch {}
}

const checkIdNumberUnique = async () => {
  if (!form.idNumber || !form.idType) return
  if (form.idType === 1 && !/^\d{15}$|^\d{18}$/.test(form.idNumber)) return
  if (form.idType === 2 && !/^[a-zA-Z0-9]{5,20}$/.test(form.idNumber)) return
  try {
    const res = await api.customer.getPage({ idNumber: form.idNumber, pageNum: 1, pageSize: 1 })
    if (res.code === 200 && res.data?.records?.length > 0) {
      ElMessage.warning('该证件号码已存在')
    }
  } catch {}
}

const checkDuplicate = async () => {
  if (!form.name && !form.idNumber) return
  dismissedDuplicate.value = false
  try {
    const res = await api.customer.checkDuplicate({
      name: form.name || undefined,
      idNumber: form.idNumber || undefined,
      idType: form.idType || undefined,
      excludeId: null
    })
    if (res.code === 200 && res.data?.length > 0) {
      duplicateList.value = res.data
      showDuplicateWarning.value = true
    } else {
      duplicateList.value = []
      showDuplicateWarning.value = false
    }
  } catch {
    duplicateList.value = []
    showDuplicateWarning.value = false
  }
}

const onIdNumberBlur = () => {
  checkIdNumberUnique()
  checkDuplicate()
}

const formatSimilarity = (val) => {
  if (val == null) return ''
  return val > 1 ? val + '%' : (val * 100).toFixed(0) + '%'
}

const loadTags = async () => {
  try {
    const res = await api.customer.getTagList()
    if (res.code === 200) {
      tagList.value = res.data || []
    }
  } catch {}
}

onMounted(() => {
  loadTags()
})

const beforeUpload = (file) => {
  const isImage = file.type.startsWith('image/')
  const isLt5M = file.size / 1024 / 1024 < 5
  if (!isImage) {
    ElMessage.error('只能上传图片文件')
    return false
  }
  if (!isLt5M) {
    ElMessage.error('图片大小不能超过5MB')
    return false
  }
  return true
}

const handleFrontSuccess = (response) => {
  if (response.code === 200 && response.data) {
    form.idPhotoFront = response.data.url
  } else {
    ElMessage.error(response.message || '上传失败')
  }
}

const handleFrontRemove = () => {
  form.idPhotoFront = ''
}

const handleBackSuccess = (response) => {
  if (response.code === 200 && response.data) {
    form.idPhotoBack = response.data.url
  } else {
    ElMessage.error(response.message || '上传失败')
  }
}

const handleBackRemove = () => {
  form.idPhotoBack = ''
}

const stepFields = [
  ['name', 'gender', 'birthDate', 'nationality'],
  ['idType', 'idNumber', 'idExpiryDate', 'idPhotoFront', 'idPhotoBack'],
  ['phone', 'backupPhone', 'email', 'wechat', 'emergencyContactName', 'emergencyContactRelation', 'emergencyContactPhone'],
  ['customerType', 'customerSource', 'importance', 'referrerId']
]

const handlePrev = () => {
  if (activeStep.value > 0) {
    activeStep.value--
  }
}

const handleNext = async () => {
  const fields = stepFields[activeStep.value]
  try {
    await formRef.value.validateField(fields)
    activeStep.value++
  } catch {}
}

const handleSave = async () => {
  try {
    await formRef.value.validate()
  } catch {
    return
  }
  submitting.value = true
  try {
    const payload = { ...form }
    if (!payload.idType) {
      delete payload.idNumber
      delete payload.idExpiryDate
      delete payload.idPhotoFront
      delete payload.idPhotoBack
    }
    const res = await api.customer.add(payload)
    if (res.code === 200) {
      const createdId = res.data?.id || res.data
      if (selectedTagIds.value.length > 0 && createdId) {
        try {
          await api.customer.addTagsToCustomer(createdId, selectedTagIds.value)
        } catch {}
      }
      ElMessage.success('创建客户成功')
      router.push('/customer/list')
    } else {
      ElMessage.error(res.message || '创建失败')
    }
  } catch {
    ElMessage.error('创建失败，请重试')
  } finally {
    submitting.value = false
  }
}

const handleCancel = () => {
  router.push('/customer/list')
}
</script>

<style scoped lang="scss">
.customer-create {
  padding: 16px;
}

.form-card {
  max-width: 960px;
  margin: 0 auto;
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.card-title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.step-bar {
  margin-bottom: 24px;
}

.duplicate-alert {
  margin-bottom: 16px;
}

.duplicate-list {
  margin-top: 8px;
}

.duplicate-item {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 6px 0;
  border-bottom: 1px solid #ebeef5;

  &:last-child {
    border-bottom: none;
  }
}

.dup-name {
  font-weight: 600;
  min-width: 80px;
}

.dup-phone,
.dup-id {
  color: #606266;
  min-width: 120px;
}

.dup-score {
  color: #e6a23c;
  font-weight: 600;
  min-width: 80px;
}

.step-form {
  margin-top: 16px;
}

.form-actions {
  display: flex;
  justify-content: center;
  gap: 12px;
  padding-top: 12px;
}

:deep(.el-radio) {
  margin-right: 16px;
}

:deep(.el-upload--picture-card) {
  width: 100px;
  height: 100px;
  line-height: 100px;
}

:deep(.el-upload-list--picture-card .el-upload-list__item) {
  width: 100px;
  height: 100px;
}
</style>
