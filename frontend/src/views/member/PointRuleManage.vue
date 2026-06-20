<template>
  <div class="point-rule-container">
    <el-card shadow="never" class="header-card">
      <div class="header-row">
        <div class="title">积分规则管理</div>
      </div>
    </el-card>

    <el-card shadow="never">
      <el-tabs v-model="activeTab">
        <el-tab-pane label="积分获取规则" name="earn">
          <div style="margin-bottom: 16px; display: flex; justify-content: flex-end">
            <el-button v-if="hasPermission('member:pointRule:earn:add')" type="primary" @click="handleAddEarn">
              <el-icon><Plus /></el-icon>新增获取规则
            </el-button>
          </div>
          <el-table :data="earnRules" v-loading="earnLoading" border style="width: 100%">
            <el-table-column prop="ruleName" label="规则名称" width="160" />
            <el-table-column label="规则类型" width="120">
              <template #default="{ row }">
                <el-tag size="small">{{ earnTypeMap[row.ruleType] || '未知' }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="规则参数" min-width="200">
              <template #default="{ row }">
                <span v-if="row.ruleType === 1">每消费 {{ row.pointValue }} 元获得 {{ row.pointAmount }} 积分</span>
                <span v-else>每次获得 {{ row.pointAmount }} 积分</span>
              </template>
            </el-table-column>
            <el-table-column label="积分倍率" width="100">
              <template #default="{ row }">{{ row.pointRate }}倍</template>
            </el-table-column>
            <el-table-column label="生效时间" width="200">
              <template #default="{ row }">
                <span v-if="row.startTime && row.endTime">{{ row.startTime?.substring(0, 10) }} ~ {{ row.endTime?.substring(0, 10) }}</span>
                <span v-else>永久有效</span>
              </template>
            </el-table-column>
            <el-table-column label="状态" width="80">
              <template #default="{ row }">
                <el-tag v-if="row.status === 1" type="success" size="small">启用</el-tag>
                <el-tag v-else type="info" size="small">禁用</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="200" fixed="right">
              <template #default="{ row }">
                <el-button v-if="hasPermission('member:pointRule:earn:edit')" type="primary" link size="small" @click="handleEditEarn(row)">编辑</el-button>
                <el-button v-if="hasPermission('member:pointRule:earn:edit')" :type="row.status === 1 ? 'warning' : 'success'" link size="small" @click="handleToggleEarnStatus(row)">{{ row.status === 1 ? '禁用' : '启用' }}</el-button>
                <el-button v-if="hasPermission('member:pointRule:earn:delete')" type="danger" link size="small" @click="handleDeleteEarn(row)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>

        <el-tab-pane label="积分消耗规则" name="consume">
          <div style="margin-bottom: 16px; display: flex; justify-content: flex-end">
            <el-button v-if="hasPermission('member:pointRule:consume:add')" type="primary" @click="handleAddConsume">
              <el-icon><Plus /></el-icon>新增消耗规则
            </el-button>
          </div>
          <el-table :data="consumeRules" v-loading="consumeLoading" border style="width: 100%">
            <el-table-column prop="ruleName" label="规则名称" width="160" />
            <el-table-column label="规则类型" width="120">
              <template #default="{ row }">
                <el-tag size="small">{{ consumeTypeMap[row.ruleType] || '未知' }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="规则参数" min-width="280">
              <template #default="{ row }">
                <span v-if="row.ruleType === 1">
                  {{ row.exchangePoints }}积分抵{{ row.exchangeAmount }}元，
                  单次最多{{ row.maxPointsPerUse }}积分，
                  最低消费{{ row.minOrderAmount }}元，
                  最多抵扣{{ row.deductionCap }}%
                </span>
                <span v-else>积分兑换权益（后续实现）</span>
              </template>
            </el-table-column>
            <el-table-column label="适用等级" width="120">
              <template #default="{ row }">
                <span v-if="!row.applicableLevels || row.applicableLevels === ''">全部等级</span>
                <span v-else>{{ row.applicableLevelList?.length || 0 }}个等级</span>
              </template>
            </el-table-column>
            <el-table-column label="生效时间" width="200">
              <template #default="{ row }">
                <span v-if="row.startTime && row.endTime">{{ row.startTime?.substring(0, 10) }} ~ {{ row.endTime?.substring(0, 10) }}</span>
                <span v-else>永久有效</span>
              </template>
            </el-table-column>
            <el-table-column label="状态" width="80">
              <template #default="{ row }">
                <el-tag v-if="row.status === 1" type="success" size="small">启用</el-tag>
                <el-tag v-else type="info" size="small">禁用</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="200" fixed="right">
              <template #default="{ row }">
                <el-button v-if="hasPermission('member:pointRule:consume:edit')" type="primary" link size="small" @click="handleEditConsume(row)">编辑</el-button>
                <el-button v-if="hasPermission('member:pointRule:consume:edit')" :type="row.status === 1 ? 'warning' : 'success'" link size="small" @click="handleToggleConsumeStatus(row)">{{ row.status === 1 ? '禁用' : '启用' }}</el-button>
                <el-button v-if="hasPermission('member:pointRule:consume:delete')" type="danger" link size="small" @click="handleDeleteConsume(row)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>
      </el-tabs>
    </el-card>

    <el-dialog v-model="earnDialogVisible" :title="earnForm.id ? '编辑积分获取规则' : '新增积分获取规则'" width="600px" destroy-on-close>
      <el-form ref="earnFormRef" :model="earnForm" :rules="earnFormRules" label-width="110px">
        <el-form-item label="规则名称" prop="ruleName">
          <el-input v-model="earnForm.ruleName" placeholder="请输入规则名称" />
        </el-form-item>
        <el-form-item label="规则类型" prop="ruleType">
          <el-select v-model="earnForm.ruleType" style="width: 100%" @change="handleEarnTypeChange">
            <el-option label="消费积分" :value="1" />
            <el-option label="签到积分" :value="2" />
            <el-option label="评价积分" :value="3" />
            <el-option label="推荐积分" :value="4" />
            <el-option label="生日积分" :value="5" />
            <el-option label="活动积分" :value="6" />
          </el-select>
        </el-form-item>
        <template v-if="earnForm.ruleType === 1">
          <el-form-item label="消费金额" prop="pointValue">
            <el-input-number v-model="earnForm.pointValue" :min="0.01" :precision="2" style="width: 50%" />
            <span style="margin-left: 8px; color: #909399">元</span>
          </el-form-item>
          <el-form-item label="获得积分" prop="pointAmount">
            <el-input-number v-model="earnForm.pointAmount" :min="0.01" :precision="2" style="width: 50%" />
            <span style="margin-left: 8px; color: #909399">积分</span>
          </el-form-item>
        </template>
        <template v-else>
          <el-form-item label="获得积分" prop="pointAmount">
            <el-input-number v-model="earnForm.pointAmount" :min="0.01" :precision="2" style="width: 50%" />
            <span style="margin-left: 8px; color: #909399">积分</span>
          </el-form-item>
        </template>
        <el-form-item label="积分倍率" prop="pointRate">
          <el-input-number v-model="earnForm.pointRate" :min="0.01" :precision="2" style="width: 50%" />
          <span style="margin-left: 8px; color: #909399">倍（根据会员等级调整）</span>
        </el-form-item>
        <el-form-item label="生效时间">
          <el-date-picker v-model="earnForm.timeRange" type="datetimerange" range-separator="至" start-placeholder="开始时间" end-placeholder="结束时间" style="width: 100%" />
        </el-form-item>
        <el-form-item label="启用状态">
          <el-switch v-model="earnForm.status" :active-value="1" :inactive-value="0" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="earnForm.remark" type="textarea" :rows="2" placeholder="请输入备注" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="earnDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="earnSaving" @click="handleSubmitEarn">确认</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="consumeDialogVisible" :title="consumeForm.id ? '编辑积分消耗规则' : '新增积分消耗规则'" width="650px" destroy-on-close>
      <el-form ref="consumeFormRef" :model="consumeForm" :rules="consumeFormRules" label-width="130px">
        <el-form-item label="规则名称" prop="ruleName">
          <el-input v-model="consumeForm.ruleName" placeholder="请输入规则名称" />
        </el-form-item>
        <el-form-item label="规则类型" prop="ruleType">
          <el-select v-model="consumeForm.ruleType" style="width: 100%">
            <el-option label="积分抵现" :value="1" />
            <el-option label="积分兑换" :value="2" />
          </el-select>
        </el-form-item>
        <template v-if="consumeForm.ruleType === 1">
          <el-form-item label="兑换比例" prop="exchangePoints">
            <div style="display: flex; align-items: center; gap: 8px">
              <el-input-number v-model="consumeForm.exchangePoints" :min="0.01" :precision="2" style="width: 140px" />
              <span>积分抵</span>
              <el-input-number v-model="consumeForm.exchangeAmount" :min="0.01" :precision="2" style="width: 140px" />
              <span>元</span>
            </div>
          </el-form-item>
          <el-form-item label="单次最多使用" prop="maxPointsPerUse">
            <el-input-number v-model="consumeForm.maxPointsPerUse" :min="0" :precision="2" style="width: 50%" />
            <span style="margin-left: 8px; color: #909399">积分</span>
          </el-form-item>
          <el-form-item label="最低消费要求" prop="minOrderAmount">
            <el-input-number v-model="consumeForm.minOrderAmount" :min="0" :precision="2" style="width: 50%" />
            <span style="margin-left: 8px; color: #909399">元</span>
          </el-form-item>
          <el-form-item label="抵扣上限" prop="deductionCap">
            <el-input-number v-model="consumeForm.deductionCap" :min="0" :max="100" :precision="2" style="width: 50%" />
            <span style="margin-left: 8px; color: #909399">%（最多抵扣订单金额的百分比）</span>
          </el-form-item>
        </template>
        <el-form-item label="适用会员等级">
          <el-select v-model="consumeForm.applicableLevelList" multiple placeholder="空表示全部等级" style="width: 100%">
            <el-option v-for="level in levelList" :key="level.id" :label="level.levelName" :value="level.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="生效时间">
          <el-date-picker v-model="consumeForm.timeRange" type="datetimerange" range-separator="至" start-placeholder="开始时间" end-placeholder="结束时间" style="width: 100%" />
        </el-form-item>
        <el-form-item label="启用状态">
          <el-switch v-model="consumeForm.status" :active-value="1" :inactive-value="0" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="consumeForm.remark" type="textarea" :rows="2" placeholder="请输入备注" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="consumeDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="consumeSaving" @click="handleSubmitConsume">确认</el-button>
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

const activeTab = ref('earn')
const earnRules = ref([])
const consumeRules = ref([])
const levelList = ref([])
const earnLoading = ref(false)
const consumeLoading = ref(false)

const earnTypeMap = { 1: '消费积分', 2: '签到积分', 3: '评价积分', 4: '推荐积分', 5: '生日积分', 6: '活动积分' }
const consumeTypeMap = { 1: '积分抵现', 2: '积分兑换' }

const earnDialogVisible = ref(false)
const earnSaving = ref(false)
const earnFormRef = ref(null)
const earnForm = reactive({
  id: null, ruleName: '', ruleType: 1, pointValue: 1, pointAmount: 1, pointRate: 1, timeRange: null, status: 1, remark: ''
})
const earnFormRules = {
  ruleName: [{ required: true, message: '请输入规则名称', trigger: 'blur' }],
  ruleType: [{ required: true, message: '请选择规则类型', trigger: 'change' }],
  pointValue: [{ required: true, message: '请输入消费金额', trigger: 'blur' }],
  pointAmount: [{ required: true, message: '请输入获得积分', trigger: 'blur' }],
  pointRate: [{ required: true, message: '请输入积分倍率', trigger: 'blur' }]
}

const consumeDialogVisible = ref(false)
const consumeSaving = ref(false)
const consumeFormRef = ref(null)
const consumeForm = reactive({
  id: null, ruleName: '', ruleType: 1, exchangePoints: 100, exchangeAmount: 10,
  maxPointsPerUse: 5000, minOrderAmount: 100, deductionCap: 30,
  applicableLevelList: [], timeRange: null, status: 1, remark: ''
})
const consumeFormRules = {
  ruleName: [{ required: true, message: '请输入规则名称', trigger: 'blur' }],
  ruleType: [{ required: true, message: '请选择规则类型', trigger: 'change' }],
  exchangePoints: [{ required: true, message: '请输入兑换积分数', trigger: 'blur' }],
  exchangeAmount: [{ required: true, message: '请输入兑换金额', trigger: 'blur' }]
}

const loadEarnRules = async () => {
  earnLoading.value = true
  try {
    const res = await api.pointRule.earnList()
    if (res.code === 200) earnRules.value = res.data || []
  } catch (e) { console.error(e) }
  earnLoading.value = false
}

const loadConsumeRules = async () => {
  consumeLoading.value = true
  try {
    const res = await api.pointRule.consumeList()
    if (res.code === 200) consumeRules.value = res.data || []
  } catch (e) { console.error(e) }
  consumeLoading.value = false
}

const loadLevelList = async () => {
  try {
    const res = await api.memberLevel.listEnabled()
    if (res.code === 200) levelList.value = res.data || []
  } catch (e) { console.error(e) }
}

const handleEarnTypeChange = (val) => {
  if (val !== 1) {
    earnForm.pointValue = 0
  } else {
    earnForm.pointValue = 1
  }
}

const handleAddEarn = () => {
  earnForm.id = null
  earnForm.ruleName = ''
  earnForm.ruleType = 1
  earnForm.pointValue = 1
  earnForm.pointAmount = 1
  earnForm.pointRate = 1
  earnForm.timeRange = null
  earnForm.status = 1
  earnForm.remark = ''
  earnDialogVisible.value = true
}

const handleEditEarn = (row) => {
  earnForm.id = row.id
  earnForm.ruleName = row.ruleName
  earnForm.ruleType = row.ruleType
  earnForm.pointValue = row.pointValue
  earnForm.pointAmount = row.pointAmount
  earnForm.pointRate = row.pointRate
  earnForm.timeRange = row.startTime && row.endTime ? [row.startTime, row.endTime] : null
  earnForm.status = row.status
  earnForm.remark = row.remark || ''
  earnDialogVisible.value = true
}

const handleSubmitEarn = async () => {
  if (!earnFormRef.value) return
  try { await earnFormRef.value.validate() } catch { return }
  earnSaving.value = true
  try {
    const data = { ...earnForm }
    if (data.timeRange && data.timeRange.length === 2) {
      data.startTime = data.timeRange[0]
      data.endTime = data.timeRange[1]
    } else {
      data.startTime = null
      data.endTime = null
    }
    delete data.timeRange
    let res
    if (data.id) {
      res = await api.pointRule.earnUpdate(data)
    } else {
      res = await api.pointRule.earnAdd(data)
    }
    if (res.code === 200) {
      ElMessage.success(data.id ? '修改成功' : '新增成功')
      earnDialogVisible.value = false
      loadEarnRules()
    }
  } catch (e) { console.error(e) }
  earnSaving.value = false
}

const handleToggleEarnStatus = (row) => {
  const newStatus = row.status === 1 ? 0 : 1
  const action = newStatus === 1 ? '启用' : '禁用'
  ElMessageBox.confirm(`确定要${action}规则"${row.ruleName}"吗？`, '提示', {
    confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning'
  }).then(async () => {
    try {
      const res = await api.pointRule.earnUpdateStatus(row.id, newStatus)
      if (res.code === 200) {
        ElMessage.success(`${action}成功`)
        loadEarnRules()
      }
    } catch (e) { console.error(e) }
  }).catch(() => {})
}

const handleDeleteEarn = (row) => {
  ElMessageBox.confirm(`确定要删除规则"${row.ruleName}"吗？`, '提示', {
    confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning'
  }).then(async () => {
    try {
      const res = await api.pointRule.earnDelete(row.id)
      if (res.code === 200) {
        ElMessage.success('删除成功')
        loadEarnRules()
      }
    } catch (e) { console.error(e) }
  }).catch(() => {})
}

const handleAddConsume = () => {
  consumeForm.id = null
  consumeForm.ruleName = ''
  consumeForm.ruleType = 1
  consumeForm.exchangePoints = 100
  consumeForm.exchangeAmount = 10
  consumeForm.maxPointsPerUse = 5000
  consumeForm.minOrderAmount = 100
  consumeForm.deductionCap = 30
  consumeForm.applicableLevelList = []
  consumeForm.timeRange = null
  consumeForm.status = 1
  consumeForm.remark = ''
  consumeDialogVisible.value = true
}

const handleEditConsume = (row) => {
  consumeForm.id = row.id
  consumeForm.ruleName = row.ruleName
  consumeForm.ruleType = row.ruleType
  consumeForm.exchangePoints = row.exchangePoints
  consumeForm.exchangeAmount = row.exchangeAmount
  consumeForm.maxPointsPerUse = row.maxPointsPerUse
  consumeForm.minOrderAmount = row.minOrderAmount
  consumeForm.deductionCap = row.deductionCap
  consumeForm.applicableLevelList = row.applicableLevelList || []
  consumeForm.timeRange = row.startTime && row.endTime ? [row.startTime, row.endTime] : null
  consumeForm.status = row.status
  consumeForm.remark = row.remark || ''
  consumeDialogVisible.value = true
}

const handleSubmitConsume = async () => {
  if (!consumeFormRef.value) return
  try { await consumeFormRef.value.validate() } catch { return }
  consumeSaving.value = true
  try {
    const data = { ...consumeForm }
    if (data.timeRange && data.timeRange.length === 2) {
      data.startTime = data.timeRange[0]
      data.endTime = data.timeRange[1]
    } else {
      data.startTime = null
      data.endTime = null
    }
    delete data.timeRange
    let res
    if (data.id) {
      res = await api.pointRule.consumeUpdate(data)
    } else {
      res = await api.pointRule.consumeAdd(data)
    }
    if (res.code === 200) {
      ElMessage.success(data.id ? '修改成功' : '新增成功')
      consumeDialogVisible.value = false
      loadConsumeRules()
    }
  } catch (e) { console.error(e) }
  consumeSaving.value = false
}

const handleToggleConsumeStatus = (row) => {
  const newStatus = row.status === 1 ? 0 : 1
  const action = newStatus === 1 ? '启用' : '禁用'
  ElMessageBox.confirm(`确定要${action}规则"${row.ruleName}"吗？`, '提示', {
    confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning'
  }).then(async () => {
    try {
      const res = await api.pointRule.consumeUpdateStatus(row.id, newStatus)
      if (res.code === 200) {
        ElMessage.success(`${action}成功`)
        loadConsumeRules()
      }
    } catch (e) { console.error(e) }
  }).catch(() => {})
}

const handleDeleteConsume = (row) => {
  ElMessageBox.confirm(`确定要删除规则"${row.ruleName}"吗？`, '提示', {
    confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning'
  }).then(async () => {
    try {
      const res = await api.pointRule.consumeDelete(row.id)
      if (res.code === 200) {
        ElMessage.success('删除成功')
        loadConsumeRules()
      }
    } catch (e) { console.error(e) }
  }).catch(() => {})
}

onMounted(() => {
  loadEarnRules()
  loadConsumeRules()
  loadLevelList()
})
</script>

<style scoped>
.point-rule-container { padding: 16px; }
.header-card { margin-bottom: 16px; }
.header-row { display: flex; justify-content: space-between; align-items: center; }
.title { font-size: 18px; font-weight: 600; color: #303133; }
</style>
