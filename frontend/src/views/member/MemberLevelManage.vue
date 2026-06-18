<template>
  <div class="member-level-container">
    <el-card shadow="never" class="header-card">
      <div class="header-row">
        <div class="title">会员等级体系</div>
        <el-button v-if="hasPermission('member:level:add')" type="primary" @click="handleAdd">
          <el-icon><Plus /></el-icon>新增等级
        </el-button>
      </div>
    </el-card>

    <el-card shadow="never" class="levels-card">
      <div class="levels-grid">
        <div
          v-for="level in levelList"
          :key="level.id"
          class="level-card"
          :style="{ borderColor: level.levelColor + '40' }"
        >
          <div class="level-header" :style="{ background: 'linear-gradient(135deg, ' + level.levelColor + '20, ' + level.levelColor + '05)' }">
            <div class="level-icon" :style="{ color: level.levelColor }">
              {{ level.levelIcon }}
            </div>
            <div class="level-name" :style="{ color: level.levelColor }">
              {{ level.levelName }}
            </div>
            <div class="level-code">{{ level.levelCode }}</div>
          </div>
          
          <div class="level-body">
            <div class="stat-row">
              <div class="stat-item">
                <span class="stat-label">会员数量</span>
                <span class="stat-value">{{ level.memberCount || 0 }}</span>
              </div>
              <div class="stat-item">
                <span class="stat-label">等级排序</span>
                <span class="stat-value">{{ level.sortOrder }}</span>
              </div>
            </div>

            <div class="divider"></div>

            <div class="info-item">
              <span class="info-label">升级条件</span>
              <span class="info-value">
                {{ level.upgradeType === 1 ? '消费满' : '积分达' }}
                {{ level.upgradeCondition }}
                {{ level.upgradeType === 1 ? '元' : '分' }}
              </span>
            </div>
            <div class="info-item">
              <span class="info-label">保级条件</span>
              <span class="info-value">年消费 {{ level.keepCondition }} 元</span>
            </div>
            <div class="info-item">
              <span class="info-label">房费折扣</span>
              <span class="info-value highlight">{{ level.roomDiscount }}%</span>
            </div>
            <div class="info-item">
              <span class="info-label">积分倍率</span>
              <span class="info-value highlight">{{ level.pointRate }}倍</span>
            </div>
            <div class="info-item">
              <span class="info-label">押金减免</span>
              <span class="info-value highlight">{{ level.depositReduction }}%</span>
            </div>

            <div v-if="level.serviceList && level.serviceList.length > 0" class="benefits-section">
              <div class="benefits-title">专属服务</div>
              <div class="benefits-list">
                <el-tag v-for="(service, idx) in level.serviceList" :key="idx" size="small" type="success" effect="light">
                  {{ service }}
                </el-tag>
              </div>
            </div>

            <div v-if="level.otherBenefitList && level.otherBenefitList.length > 0" class="benefits-section">
              <div class="benefits-title">其他权益</div>
              <div class="benefits-list">
                <el-tag v-for="(benefit, idx) in level.otherBenefitList" :key="idx" size="small" type="warning" effect="light">
                  {{ benefit }}
                </el-tag>
              </div>
            </div>
          </div>

          <div class="level-footer">
            <el-tag v-if="level.status === 1" type="success" size="small">启用</el-tag>
            <el-tag v-else type="info" size="small">禁用</el-tag>
            <div class="actions">
              <el-button
                v-if="hasPermission('member:level:edit')"
                type="primary"
                link
                size="small"
                @click.stop="handleEdit(level)"
              >编辑</el-button>
              <el-button
                v-if="hasPermission('member:level:delete')"
                type="danger"
                link
                size="small"
                @click.stop="handleDelete(level)"
              >删除</el-button>
            </div>
          </div>
        </div>
      </div>
    </el-card>

    <el-dialog
      v-model="dialogVisible"
      :title="dialogForm.id ? '编辑会员等级' : '新增会员等级'"
      width="680px"
      destroy-on-close
    >
      <el-form ref="formRef" :model="dialogForm" :rules="formRules" label-width="100px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="等级名称" prop="levelName">
              <el-input v-model="dialogForm.levelName" placeholder="请输入等级名称" maxlength="20" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="等级编码" prop="levelCode">
              <el-input v-model="dialogForm.levelCode" placeholder="请输入等级编码" maxlength="30" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="等级图标" prop="levelIcon">
              <el-input v-model="dialogForm.levelIcon" placeholder="可输入emoji或图标地址" maxlength="100" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="等级颜色" prop="levelColor">
              <el-color-picker v-model="dialogForm.levelColor" show-alpha />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="等级排序" prop="sortOrder">
              <el-input-number v-model="dialogForm.sortOrder" :min="1" :max="100" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="启用状态" prop="status">
              <el-switch v-model="dialogForm.status" :active-value="1" :inactive-value="0" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="升级条件类型" prop="upgradeType">
              <el-select v-model="dialogForm.upgradeType" style="width: 100%">
                <el-option label="按消费金额" :value="1" />
                <el-option label="按积分" :value="2" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="升级条件值" prop="upgradeCondition">
              <el-input-number v-model="dialogForm.upgradeCondition" :min="0" :precision="2" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="保级条件" prop="keepCondition">
          <el-input-number v-model="dialogForm.keepCondition" :min="0" :precision="2" style="width: 50%" />
          <span style="margin-left: 10px; color: #909399">元/年</span>
        </el-form-item>

        <el-divider>折扣与积分</el-divider>

        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="房费折扣" prop="roomDiscount">
              <el-input-number v-model="dialogForm.roomDiscount" :min="0" :max="100" :precision="2" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="餐饮折扣" prop="diningDiscount">
              <el-input-number v-model="dialogForm.diningDiscount" :min="0" :max="100" :precision="2" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="积分倍率" prop="pointRate">
              <el-input-number v-model="dialogForm.pointRate" :min="0" :precision="2" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="押金减免" prop="depositReduction">
          <el-input-number v-model="dialogForm.depositReduction" :min="0" :max="100" :precision="2" style="width: 30%" />
          <span style="margin-left: 10px; color: #909399">%</span>
        </el-form-item>

        <el-divider>专属服务</el-divider>

        <el-form-item label="专属服务">
          <el-checkbox-group v-model="dialogForm.serviceList">
            <el-checkbox label="免费升级房型" />
            <el-checkbox label="延迟退房" />
            <el-checkbox label="提前入住" />
            <el-checkbox label="免费早餐" />
            <el-checkbox label="免费接送机" />
            <el-checkbox label="专属客服" />
            <el-checkbox label="生日礼遇" />
          </el-checkbox-group>
        </el-form-item>

        <el-divider>其他权益</el-divider>

        <el-form-item label="其他权益">
          <el-checkbox-group v-model="dialogForm.otherBenefitList">
            <el-checkbox label="房间优选" />
            <el-checkbox label="积分兑换优先" />
            <el-checkbox label="预订优先" />
          </el-checkbox-group>
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

const levelList = ref([])
const dialogVisible = ref(false)
const dialogSaving = ref(false)
const formRef = ref(null)

const dialogForm = reactive({
  id: null,
  levelName: '',
  levelCode: '',
  levelIcon: '',
  levelColor: '#409EFF',
  sortOrder: 1,
  upgradeType: 1,
  upgradeCondition: 0,
  keepCondition: 0,
  roomDiscount: 100,
  diningDiscount: 100,
  pointRate: 1,
  depositReduction: 0,
  serviceList: [],
  otherBenefitList: [],
  status: 1
})

const formRules = {
  levelName: [{ required: true, message: '请输入等级名称', trigger: 'blur' }],
  levelCode: [{ required: true, message: '请输入等级编码', trigger: 'blur' }],
  sortOrder: [{ required: true, message: '请输入等级排序', trigger: 'blur' }],
  upgradeCondition: [{ required: true, message: '请输入升级条件值', trigger: 'blur' }]
}

const loadLevelList = async () => {
  try {
    const res = await api.memberLevel.list()
    if (res.code === 200) {
      levelList.value = res.data || []
    }
  } catch (e) {
    console.error(e)
  }
}

const handleAdd = () => {
  dialogForm.id = null
  dialogForm.levelName = ''
  dialogForm.levelCode = ''
  dialogForm.levelIcon = '👤'
  dialogForm.levelColor = '#409EFF'
  dialogForm.sortOrder = 1
  dialogForm.upgradeType = 1
  dialogForm.upgradeCondition = 0
  dialogForm.keepCondition = 0
  dialogForm.roomDiscount = 100
  dialogForm.diningDiscount = 100
  dialogForm.pointRate = 1
  dialogForm.depositReduction = 0
  dialogForm.serviceList = []
  dialogForm.otherBenefitList = []
  dialogForm.status = 1
  dialogVisible.value = true
}

const handleEdit = (level) => {
  dialogForm.id = level.id
  dialogForm.levelName = level.levelName
  dialogForm.levelCode = level.levelCode
  dialogForm.levelIcon = level.levelIcon || ''
  dialogForm.levelColor = level.levelColor || '#409EFF'
  dialogForm.sortOrder = level.sortOrder
  dialogForm.upgradeType = level.upgradeType
  dialogForm.upgradeCondition = level.upgradeCondition
  dialogForm.keepCondition = level.keepCondition
  dialogForm.roomDiscount = level.roomDiscount
  dialogForm.diningDiscount = level.diningDiscount
  dialogForm.pointRate = level.pointRate
  dialogForm.depositReduction = level.depositReduction
  dialogForm.serviceList = level.serviceList || []
  dialogForm.otherBenefitList = level.otherBenefitList || []
  dialogForm.status = level.status
  dialogVisible.value = true
}

const handleSubmit = async () => {
  if (!formRef.value) return
  try {
    await formRef.value.validate()
  } catch {
    return
  }

  dialogSaving.value = true
  try {
    let res
    if (dialogForm.id) {
      res = await api.memberLevel.update(dialogForm)
    } else {
      res = await api.memberLevel.add(dialogForm)
    }
    if (res.code === 200) {
      ElMessage.success(dialogForm.id ? '修改成功' : '新增成功')
      dialogVisible.value = false
      loadLevelList()
    }
  } catch (e) {
    console.error(e)
  } finally {
    dialogSaving.value = false
  }
}

const handleDelete = (level) => {
  ElMessageBox.confirm(`确定要删除会员等级"${level.levelName}"吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      const res = await api.memberLevel.delete(level.id)
      if (res.code === 200) {
        ElMessage.success('删除成功')
        loadLevelList()
      }
    } catch (e) {
      console.error(e)
    }
  }).catch(() => {})
}

onMounted(() => {
  loadLevelList()
})
</script>

<style scoped>
.member-level-container {
  padding: 16px;
}

.header-card {
  margin-bottom: 16px;
}

.header-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.title {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
}

.levels-card {
  padding: 20px;
}

.levels-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 20px;
}

.level-card {
  border: 2px solid #ebeef5;
  border-radius: 12px;
  overflow: hidden;
  transition: all 0.3s;
  background: #fff;
}

.level-card:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  transform: translateY(-2px);
}

.level-header {
  padding: 20px;
  text-align: center;
}

.level-icon {
  font-size: 48px;
  margin-bottom: 8px;
}

.level-name {
  font-size: 20px;
  font-weight: 600;
  margin-bottom: 4px;
}

.level-code {
  font-size: 12px;
  color: #909399;
}

.level-body {
  padding: 16px 20px;
}

.stat-row {
  display: flex;
  justify-content: space-around;
  margin-bottom: 12px;
}

.stat-item {
  text-align: center;
}

.stat-label {
  display: block;
  font-size: 12px;
  color: #909399;
  margin-bottom: 4px;
}

.stat-value {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
}

.divider {
  height: 1px;
  background: #f0f0f0;
  margin: 12px 0;
}

.info-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
  font-size: 13px;
}

.info-label {
  color: #606266;
}

.info-value {
  color: #303133;
}

.info-value.highlight {
  font-weight: 600;
  color: #e6a23c;
}

.benefits-section {
  margin-top: 12px;
}

.benefits-title {
  font-size: 12px;
  color: #909399;
  margin-bottom: 8px;
}

.benefits-list {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}

.level-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 20px;
  border-top: 1px solid #f0f0f0;
  background: #fafafa;
}

.actions {
  display: flex;
  gap: 8px;
}
</style>
