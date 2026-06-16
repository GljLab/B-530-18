<template>
  <div class="customer-edit">
    <el-page-header @back="router.back()" title="返回" content="编辑客户" />

    <div v-loading="pageLoading" class="form-container">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="120px" class="edit-form">

        <el-card shadow="never" class="section-card">
          <template #header><span class="section-title">基本信息</span></template>
          <el-row :gutter="20">
            <el-col :span="8">
              <el-form-item label="姓名" prop="name">
                <el-input v-model="form.name" placeholder="请输入姓名" maxlength="50" />
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item label="性别" prop="gender">
                <el-select v-model="form.gender" placeholder="请选择性别" style="width: 100%">
                  <el-option v-for="item in genderOptions" :key="item.value" :label="item.label" :value="item.value" />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item label="出生日期" prop="birthDate">
                <el-date-picker v-model="form.birthDate" type="date" placeholder="请选择出生日期" value-format="YYYY-MM-DD" style="width: 100%" />
              </el-form-item>
            </el-col>
          </el-row>
          <el-row :gutter="20">
            <el-col :span="8">
              <el-form-item label="国籍" prop="nationality">
                <el-select v-model="form.nationality" placeholder="请选择国籍" filterable style="width: 100%">
                  <el-option v-for="item in nationalityOptions" :key="item" :label="item" :value="item" />
                </el-select>
              </el-form-item>
            </el-col>
          </el-row>
        </el-card>

        <el-card shadow="never" class="section-card">
          <template #header><span class="section-title">证件信息</span></template>
          <el-row :gutter="20">
            <el-col :span="8">
              <el-form-item label="证件类型" prop="idType">
                <el-select v-model="form.idType" placeholder="请选择证件类型" style="width: 100%">
                  <el-option v-for="item in idTypeOptions" :key="item.value" :label="item.label" :value="item.value" />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item label="证件号码" prop="idNumber">
                <el-input v-model="form.idNumber" placeholder="请输入证件号码" @blur="checkIdNumberUnique" />
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item label="证件有效期" prop="idExpiryDate">
                <el-date-picker v-model="form.idExpiryDate" type="date" placeholder="请选择有效期" value-format="YYYY-MM-DD" style="width: 100%" />
              </el-form-item>
            </el-col>
          </el-row>
          <el-row :gutter="20">
            <el-col :span="8">
              <el-form-item label="证件正面照" prop="idPhotoFront">
                <el-upload
                  class="id-uploader"
                  :action="'/api/file/upload'"
                  :headers="uploadHeaders"
                  :show-file-list="false"
                  :on-success="(res) => handleUploadSuccess(res, 'idPhotoFront')"
                  accept="image/*"
                >
                  <el-image v-if="form.idPhotoFront" :src="form.idPhotoFront" fit="cover" class="upload-image" />
                  <el-icon v-else class="upload-icon"><Plus /></el-icon>
                </el-upload>
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item label="证件背面照" prop="idPhotoBack">
                <el-upload
                  class="id-uploader"
                  :action="'/api/file/upload'"
                  :headers="uploadHeaders"
                  :show-file-list="false"
                  :on-success="(res) => handleUploadSuccess(res, 'idPhotoBack')"
                  accept="image/*"
                >
                  <el-image v-if="form.idPhotoBack" :src="form.idPhotoBack" fit="cover" class="upload-image" />
                  <el-icon v-else class="upload-icon"><Plus /></el-icon>
                </el-upload>
              </el-form-item>
            </el-col>
          </el-row>
        </el-card>

        <el-card shadow="never" class="section-card">
          <template #header><span class="section-title">联系信息</span></template>
          <el-row :gutter="20">
            <el-col :span="8">
              <el-form-item label="手机号" prop="phone">
                <el-input v-model="form.phone" placeholder="请输入手机号" maxlength="11" @blur="checkPhoneUnique" />
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item label="备用手机号" prop="backupPhone">
                <el-input v-model="form.backupPhone" placeholder="请输入备用手机号" maxlength="11" />
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item label="邮箱" prop="email">
                <el-input v-model="form.email" placeholder="请输入邮箱" />
              </el-form-item>
            </el-col>
          </el-row>
          <el-row :gutter="20">
            <el-col :span="8">
              <el-form-item label="微信" prop="wechat">
                <el-input v-model="form.wechat" placeholder="请输入微信号" />
              </el-form-item>
            </el-col>
          </el-row>
          <el-divider content-position="left">紧急联系人</el-divider>
          <el-row :gutter="20">
            <el-col :span="8">
              <el-form-item label="联系人姓名" prop="emergencyContactName">
                <el-input v-model="form.emergencyContactName" placeholder="请输入紧急联系人姓名" />
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item label="与本人关系" prop="emergencyContactRelation">
                <el-select v-model="form.emergencyContactRelation" placeholder="请选择关系" style="width: 100%">
                  <el-option v-for="item in relationOptions" :key="item" :label="item" :value="item" />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item label="联系人电话" prop="emergencyContactPhone">
                <el-input v-model="form.emergencyContactPhone" placeholder="请输入紧急联系人电话" />
              </el-form-item>
            </el-col>
          </el-row>
        </el-card>

        <el-card shadow="never" class="section-card">
          <template #header><span class="section-title">分类信息</span></template>
          <el-row :gutter="20">
            <el-col :span="8">
              <el-form-item label="客户类型" prop="customerType">
                <el-select v-model="form.customerType" placeholder="请选择客户类型" style="width: 100%">
                  <el-option v-for="item in customerTypeOptions" :key="item.value" :label="item.label" :value="item.value" />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item label="客户来源" prop="customerSource">
                <el-select v-model="form.customerSource" placeholder="请选择客户来源" style="width: 100%">
                  <el-option v-for="item in customerSourceOptions" :key="item.value" :label="item.label" :value="item.value" />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item label="重要程度" prop="importance">
                <el-select v-model="form.importance" placeholder="请选择重要程度" style="width: 100%">
                  <el-option v-for="item in importanceOptions" :key="item.value" :label="item.label" :value="item.value" />
                </el-select>
              </el-form-item>
            </el-col>
          </el-row>
        </el-card>

        <el-card shadow="never" class="section-card">
          <template #header>
            <div class="address-header">
              <span class="section-title">地址管理</span>
              <el-button type="primary" size="small" :disabled="addresses.length >= 5" @click="openAddressDialog(null)">
                <el-icon><Plus /></el-icon>添加地址
              </el-button>
            </div>
          </template>
          <div v-if="addresses.length === 0" class="empty-address">
            <el-empty description="暂无地址信息" :image-size="80" />
          </div>
          <div v-else class="address-list">
            <el-card v-for="addr in addresses" :key="addr.id" shadow="hover" class="address-card" :class="{ 'is-default': addr.isDefault }">
              <div class="address-card-body">
                <div class="address-info">
                  <el-tag size="small" :type="addr.isDefault ? 'danger' : 'info'" class="address-type-tag">
                    {{ addressTypeLabel(addr.addressType) }}
                  </el-tag>
                  <el-tag v-if="addr.isDefault" size="small" type="danger" effect="dark" class="default-tag">默认</el-tag>
                  <span class="address-detail">{{ formatAddress(addr) }}</span>
                </div>
                <div class="address-actions">
                  <el-button v-if="!addr.isDefault" type="primary" link size="small" @click="handleSetDefault(addr)">设为默认</el-button>
                  <el-button type="primary" link size="small" @click="openAddressDialog(addr)">编辑</el-button>
                  <el-button type="danger" link size="small" @click="handleDeleteAddress(addr)">删除</el-button>
                </div>
              </div>
            </el-card>
          </div>
        </el-card>
      </el-form>

      <div class="form-footer">
        <el-button @click="router.back()">取消</el-button>
        <el-button type="primary" :loading="saving" @click="handleSave">保存</el-button>
      </div>
    </div>

    <el-dialog v-model="addressDialogVisible" :title="addressForm.id ? '编辑地址' : '添加地址'" width="600px" destroy-on-close>
      <el-form ref="addressFormRef" :model="addressForm" :rules="addressRules" label-width="100px">
        <el-form-item label="地址类型" prop="addressType">
          <el-select v-model="addressForm.addressType" placeholder="请选择地址类型" style="width: 100%">
            <el-option v-for="item in addressTypeOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="省份" prop="province">
          <el-input v-model="addressForm.province" placeholder="请输入省份" />
        </el-form-item>
        <el-form-item label="城市" prop="city">
          <el-input v-model="addressForm.city" placeholder="请输入城市" />
        </el-form-item>
        <el-form-item label="区/县" prop="district">
          <el-input v-model="addressForm.district" placeholder="请输入区/县" />
        </el-form-item>
        <el-form-item label="详细地址" prop="detail">
          <el-input v-model="addressForm.detail" type="textarea" :rows="2" placeholder="请输入详细地址" />
        </el-form-item>
        <el-form-item label="邮政编码" prop="postalCode">
          <el-input v-model="addressForm.postalCode" placeholder="请输入邮政编码" maxlength="6" />
        </el-form-item>
        <el-form-item label="设为默认" prop="isDefault">
          <el-switch v-model="addressForm.isDefault" :active-value="1" :inactive-value="0" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="addressDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="addressSaving" @click="handleSaveAddress">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import api from '@/api'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const customerId = route.params.id

const pageLoading = ref(false)
const saving = ref(false)
const formRef = ref(null)

const genderOptions = [
  { value: 0, label: '未知' },
  { value: 1, label: '男' },
  { value: 2, label: '女' }
]

const nationalityOptions = ['中国', '美国', '英国', '日本', '韩国', '法国', '德国', '澳大利亚', '加拿大', '新加坡', '其他']

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

const addressTypeOptions = [
  { value: 1, label: '家庭地址' },
  { value: 2, label: '公司地址' },
  { value: 3, label: '发票邮寄地址' }
]

const uploadHeaders = computed(() => ({
  Authorization: `Bearer ${userStore.token}`
}))

const form = reactive({
  id: null,
  name: '',
  gender: 0,
  birthDate: '',
  nationality: '',
  idType: 1,
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
  customerType: 1,
  customerSource: 1,
  importance: 1
})

const addresses = ref([])

const validatePhone = (rule, value, callback) => {
  if (!value) return callback()
  if (!/^1[3-9]\d{9}$/.test(value)) {
    callback(new Error('请输入正确的11位手机号'))
  } else {
    callback()
  }
}

const validateEmail = (rule, value, callback) => {
  if (!value) return callback()
  if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(value)) {
    callback(new Error('请输入正确的邮箱格式'))
  } else {
    callback()
  }
}

const validateIdNumber = (rule, value, callback) => {
  if (!value) return callback()
  const type = form.idType
  if (type === 1) {
    if (!/^\d{17}[\dXx]$/.test(value)) {
      callback(new Error('请输入正确的18位身份证号'))
    } else {
      callback()
    }
  } else if (type === 2) {
    if (!/^[A-Za-z0-9]{5,20}$/.test(value)) {
      callback(new Error('请输入正确的护照号码'))
    } else {
      callback()
    }
  } else {
    if (!/^[A-Za-z0-9]{5,20}$/.test(value)) {
      callback(new Error('请输入正确的证件号码'))
    } else {
      callback()
    }
  }
}

const rules = {
  name: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { validator: validatePhone, trigger: 'blur' }
  ],
  email: [{ validator: validateEmail, trigger: 'blur' }],
  idNumber: [{ validator: validateIdNumber, trigger: 'blur' }]
}

const checkPhoneUnique = async () => {
  if (!form.phone || !/^1[3-9]\d{9}$/.test(form.phone)) return
  try {
    const res = await api.customer.getPage({ phone: form.phone, pageNum: 1, pageSize: 1 })
    if (res.code === 200 && res.data?.records?.length > 0) {
      const existing = res.data.records[0]
      if (existing.id !== form.id) {
        ElMessage.warning('该手机号已被其他客户使用')
      }
    }
  } catch {}
}

const checkIdNumberUnique = async () => {
  if (!form.idNumber) return
  try {
    const res = await api.customer.getPage({ idNumber: form.idNumber, pageNum: 1, pageSize: 1 })
    if (res.code === 200 && res.data?.records?.length > 0) {
      const existing = res.data.records[0]
      if (existing.id !== form.id) {
        ElMessage.warning('该证件号已被其他客户使用')
      }
    }
  } catch {}
}

const handleUploadSuccess = (res, field) => {
  if (res.code === 200) {
    form[field] = res.data?.url || res.data
  } else {
    ElMessage.error(res.message || '上传失败')
  }
}

const addressTypeLabel = (type) => {
  const item = addressTypeOptions.find(o => o.value === type)
  return item ? item.label : '未知'
}

const formatAddress = (addr) => {
  const parts = [addr.province, addr.city, addr.district, addr.detail].filter(Boolean)
  return parts.join(' ')
}

const addressDialogVisible = ref(false)
const addressSaving = ref(false)
const addressFormRef = ref(null)

const defaultAddressForm = () => ({
  id: null,
  customerId: customerId,
  addressType: 1,
  province: '',
  city: '',
  district: '',
  detail: '',
  postalCode: '',
  isDefault: 0
})

const addressForm = reactive(defaultAddressForm())

const addressRules = {
  addressType: [{ required: true, message: '请选择地址类型', trigger: 'change' }],
  province: [{ required: true, message: '请输入省份', trigger: 'blur' }],
  city: [{ required: true, message: '请输入城市', trigger: 'blur' }],
  detail: [{ required: true, message: '请输入详细地址', trigger: 'blur' }]
}

const openAddressDialog = (addr) => {
  if (addr) {
    Object.assign(addressForm, {
      id: addr.id,
      customerId: customerId,
      addressType: addr.addressType,
      province: addr.province,
      city: addr.city,
      district: addr.district,
      detail: addr.detail,
      postalCode: addr.postalCode,
      isDefault: addr.isDefault
    })
  } else {
    Object.assign(addressForm, defaultAddressForm())
  }
  addressDialogVisible.value = true
}

const handleSaveAddress = async () => {
  const valid = await addressFormRef.value.validate().catch(() => false)
  if (!valid) return

  addressSaving.value = true
  try {
    let res
    if (addressForm.id) {
      res = await api.customer.updateAddress({ ...addressForm })
    } else {
      res = await api.customer.addAddress({ ...addressForm })
    }
    if (res.code === 200) {
      ElMessage.success(addressForm.id ? '地址更新成功' : '地址添加成功')
      addressDialogVisible.value = false
      await loadAddresses()
    } else {
      ElMessage.error(res.message || '操作失败')
    }
  } catch {
    ElMessage.error('操作失败')
  } finally {
    addressSaving.value = false
  }
}

const handleDeleteAddress = async (addr) => {
  try {
    await ElMessageBox.confirm('确定删除该地址吗？', '提示', { type: 'warning' })
    const res = await api.customer.deleteAddress(addr.id)
    if (res.code === 200) {
      ElMessage.success('删除成功')
      await loadAddresses()
    } else {
      ElMessage.error(res.message || '删除失败')
    }
  } catch {}
}

const handleSetDefault = async (addr) => {
  try {
    const res = await api.customer.setDefaultAddress(addr.id)
    if (res.code === 200) {
      ElMessage.success('设置成功')
      await loadAddresses()
    } else {
      ElMessage.error(res.message || '设置失败')
    }
  } catch {
    ElMessage.error('设置失败')
  }
}

const loadAddresses = async () => {
  try {
    const res = await api.customer.getAddresses(customerId)
    if (res.code === 200) {
      addresses.value = res.data || []
    }
  } catch {}
}

const loadCustomer = async () => {
  pageLoading.value = true
  try {
    const res = await api.customer.getById(customerId)
    if (res.code === 200 && res.data) {
      const data = res.data
      Object.keys(form).forEach(key => {
        if (data[key] !== undefined && data[key] !== null) {
          form[key] = data[key]
        }
      })
      if (data.addresses) {
        addresses.value = data.addresses
      } else {
        await loadAddresses()
      }
    } else {
      ElMessage.error('客户信息加载失败')
      router.back()
    }
  } catch {
    ElMessage.error('客户信息加载失败')
    router.back()
  } finally {
    pageLoading.value = false
  }
}

const handleSave = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  saving.value = true
  try {
    const res = await api.customer.update({ ...form })
    if (res.code === 200) {
      ElMessage.success('保存成功')
      router.back()
    } else {
      ElMessage.error(res.message || '保存失败')
    }
  } catch {
    ElMessage.error('保存失败')
  } finally {
    saving.value = false
  }
}

onMounted(() => {
  loadCustomer()
})
</script>

<style scoped>
.customer-edit {
  padding: 20px;
}

.form-container {
  margin-top: 20px;
}

.section-card {
  margin-bottom: 20px;
}

.section-title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.address-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.address-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.address-card {
  border: 1px solid #ebeef5;
  transition: border-color 0.3s;
}

.address-card.is-default {
  border-color: #f56c6c;
}

.address-card-body {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.address-info {
  display: flex;
  align-items: center;
  gap: 8px;
  flex: 1;
  flex-wrap: wrap;
}

.address-type-tag {
  flex-shrink: 0;
}

.default-tag {
  flex-shrink: 0;
}

.address-detail {
  color: #606266;
  font-size: 14px;
}

.address-actions {
  display: flex;
  gap: 4px;
  flex-shrink: 0;
}

.empty-address {
  padding: 20px 0;
}

.id-uploader :deep(.el-upload) {
  border: 1px dashed #d9d9d9;
  border-radius: 6px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  width: 120px;
  height: 120px;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: border-color 0.3s;
}

.id-uploader :deep(.el-upload:hover) {
  border-color: #409eff;
}

.upload-image {
  width: 120px;
  height: 120px;
}

.upload-icon {
  font-size: 28px;
  color: #8c939d;
}

.form-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  padding: 20px 0;
}
</style>
