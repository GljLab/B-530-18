<template>
  <div class="invoice-manage">
    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <span class="title">发票管理</span>
          <el-button type="primary" @click="openApplyDialog">申请开票</el-button>
        </div>
      </template>

      <el-tabs v-model="activeTab" @tab-change="handleTabChange">
        <el-tab-pane label="待开票" name="0" />
        <el-tab-pane label="已开票" name="1" />
        <el-tab-pane label="待邮寄" name="2" />
        <el-tab-pane label="已完成" name="3" />
      </el-tabs>

      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="关键字">
          <el-input v-model="searchForm.keyword" placeholder="发票编号/客户名称" clearable style="width: 200px" />
        </el-form-item>
        <el-form-item label="发票类型">
          <el-select v-model="searchForm.invoiceType" placeholder="全部" clearable style="width: 160px">
            <el-option label="普通发票(个人)" :value="1" />
            <el-option label="普通发票(企业)" :value="2" />
            <el-option label="专用发票" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item label="日期范围">
          <el-date-picker
            v-model="searchForm.dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            value-format="YYYY-MM-DD"
            style="width: 280px"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
          <el-button type="success" @click="handleExport">导出</el-button>
        </el-form-item>
      </el-form>

      <el-row :gutter="16" class="summary-row">
        <el-col :span="6">
          <div class="stat-card blue">
            <div class="stat-icon">
              <el-icon :size="28"><Wallet /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">¥{{ formatMoney(statistics.totalAmount) }}</div>
              <div class="stat-label">开票总额</div>
            </div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="stat-card green">
            <div class="stat-icon">
              <el-icon :size="28"><Document /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ statistics.totalCount || 0 }}</div>
              <div class="stat-label">开票笔数</div>
            </div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="stat-card orange">
            <div class="stat-icon">
              <el-icon :size="28"><Clock /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ statistics.pendingCount || 0 }}</div>
              <div class="stat-label">待开票数</div>
            </div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="stat-card red">
            <div class="stat-icon">
              <el-icon :size="28"><Van /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ statistics.pendingMailCount || 0 }}</div>
              <div class="stat-label">待邮寄数</div>
            </div>
          </div>
        </el-col>
      </el-row>

      <el-table :data="tableData" v-loading="loading" border stripe style="width: 100%; margin-top: 20px">
        <el-table-column prop="invoiceNo" label="发票编号" width="180" />
        <el-table-column prop="relatedOrderNo" label="关联单号" width="180" />
        <el-table-column prop="customerName" label="客户名称" width="120" />
        <el-table-column label="发票类型" width="140" align="center">
          <template #default="{ row }">
            <el-tag :type="invoiceTypeTag(row.invoiceType)" size="small">{{ invoiceTypeMap[row.invoiceType] || '-' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="invoiceTitle" label="发票抬头" min-width="140" />
        <el-table-column label="开票金额" width="130" align="right">
          <template #default="{ row }">
            <span class="price">¥{{ formatMoney(row.invoiceAmount) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="开票项目" width="100" align="center">
          <template #default="{ row }">
            {{ invoiceItemMap[row.invoiceItem] || '-' }}
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="statusType(row.status)" size="small">{{ statusLabel(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="applyTime" label="申请时间" width="170" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="openDetailDialog(row)">详情</el-button>
            <el-button v-if="row.status === 0" type="warning" link size="small" @click="openProcessDialog(row)">开票</el-button>
            <el-tag v-if="row.status === 1 && row.isElectronic" type="success" size="small">已发送</el-tag>
            <el-button v-if="row.status === 1 && !row.isElectronic" type="info" link size="small" @click="openMailDialog(row)">邮寄</el-button>
            <el-button v-if="row.status === 2" type="info" link size="small" @click="openMailDialog(row)">邮寄</el-button>
            <el-button v-if="row.status === 3" type="success" link size="small" @click="handleComplete(row)">完成</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:current-page="pagination.pageNum"
        v-model:page-size="pagination.pageSize"
        :total="pagination.total"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next, jumper"
        class="pagination"
        @size-change="loadList"
        @current-change="loadList"
      />
    </el-card>

    <el-dialog v-model="applyDialogVisible" title="申请开票" width="650px" destroy-on-close>
      <el-form ref="applyFormRef" :model="applyForm" :rules="applyRules" label-width="110px">
        <el-form-item label="关联单类型" prop="relatedOrderType">
          <el-radio-group v-model="applyForm.relatedOrderType">
            <el-radio :value="1">入住单</el-radio>
            <el-radio :value="2">预订单</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="关联单号" prop="relatedOrderNo">
          <el-input v-model="applyForm.relatedOrderNo" placeholder="请输入关联单号" />
        </el-form-item>
        <el-form-item label="客户名称" prop="customerName">
          <el-input v-model="applyForm.customerName" placeholder="请输入客户名称" />
        </el-form-item>
        <el-form-item label="发票类型" prop="invoiceType">
          <el-select v-model="applyForm.invoiceType" placeholder="请选择发票类型" style="width: 100%">
            <el-option label="普通发票(个人)" :value="1" />
            <el-option label="普通发票(企业)" :value="2" />
            <el-option label="专用发票" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item label="发票抬头" prop="invoiceTitle">
          <el-input v-model="applyForm.invoiceTitle" placeholder="请输入发票抬头" />
        </el-form-item>
        <el-form-item v-if="applyForm.invoiceType === 2 || applyForm.invoiceType === 3" label="税号" prop="taxNo">
          <el-input v-model="applyForm.taxNo" placeholder="请输入纳税人识别号" />
        </el-form-item>
        <el-form-item v-if="applyForm.invoiceType === 3" label="公司地址" prop="companyAddress">
          <el-input v-model="applyForm.companyAddress" placeholder="请输入公司地址" />
        </el-form-item>
        <el-form-item v-if="applyForm.invoiceType === 3" label="银行账号" prop="bankAccount">
          <el-input v-model="applyForm.bankAccount" placeholder="请输入银行账号" />
        </el-form-item>
        <el-form-item label="开票金额" prop="invoiceAmount">
          <el-input-number v-model="applyForm.invoiceAmount" :min="0.01" :precision="2" :step="1" style="width: 100%" />
        </el-form-item>
        <el-form-item label="订单金额">
          <el-input v-model="applyForm.orderAmount" disabled placeholder="自动获取" />
        </el-form-item>
        <el-form-item label="开票项目" prop="invoiceItem">
          <el-select v-model="applyForm.invoiceItem" placeholder="请选择开票项目" style="width: 100%">
            <el-option label="住宿费" :value="1" />
            <el-option label="餐饮费" :value="2" />
            <el-option label="其他" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item label="联系电话" prop="contactPhone">
          <el-input v-model="applyForm.contactPhone" placeholder="请输入联系电话" />
        </el-form-item>
        <el-form-item label="联系邮箱" prop="contactEmail">
          <el-input v-model="applyForm.contactEmail" placeholder="请输入联系邮箱" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="applyDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="applySaving" @click="handleApplySubmit">提交申请</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="processDialogVisible" title="开票处理" width="550px" destroy-on-close>
      <el-form ref="processFormRef" :model="processForm" :rules="processRules" label-width="100px">
        <el-form-item label="发票代码" prop="invoiceCode">
          <el-input v-model="processForm.invoiceCode" placeholder="请输入发票代码" />
        </el-form-item>
        <el-form-item label="发票号码" prop="invoiceNumber">
          <el-input v-model="processForm.invoiceNumber" placeholder="请输入发票号码" />
        </el-form-item>
        <el-form-item label="发票PDF">
          <el-upload
            v-model:file-list="processForm.invoicePdf"
            action="/api/file/upload"
            :limit="1"
            :headers="uploadHeaders"
            accept=".pdf"
          >
            <el-button type="primary" size="small">上传文件</el-button>
          </el-upload>
        </el-form-item>
        <el-form-item label="电子发票">
          <el-checkbox v-model="processForm.isElectronic">电子发票</el-checkbox>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="processDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="processSaving" @click="handleProcessSubmit">确认开票</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="mailDialogVisible" title="邮寄信息" width="550px" destroy-on-close>
      <el-form ref="mailFormRef" :model="mailForm" :rules="mailRules" label-width="100px">
        <el-form-item label="收件人" prop="mailRecipient">
          <el-input v-model="mailForm.mailRecipient" placeholder="请输入收件人" />
        </el-form-item>
        <el-form-item label="邮寄地址" prop="mailAddress">
          <el-input v-model="mailForm.mailAddress" placeholder="请输入邮寄地址" />
        </el-form-item>
        <el-form-item label="快递公司" prop="mailCompany">
          <el-input v-model="mailForm.mailCompany" placeholder="请输入快递公司" />
        </el-form-item>
        <el-form-item label="快递单号" prop="mailTrackingNo">
          <el-input v-model="mailForm.mailTrackingNo" placeholder="请输入快递单号" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="mailDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="mailSaving" @click="handleMailSubmit">确认邮寄</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="detailDialogVisible" title="发票详情" width="700px" destroy-on-close>
      <el-descriptions v-if="currentRow" :column="2" border class="detail-desc">
        <el-descriptions-item label="发票编号">{{ currentRow.invoiceNo }}</el-descriptions-item>
        <el-descriptions-item label="关联单号">{{ currentRow.relatedOrderNo }}</el-descriptions-item>
        <el-descriptions-item label="客户名称">{{ currentRow.customerName }}</el-descriptions-item>
        <el-descriptions-item label="发票类型">
          <el-tag :type="invoiceTypeTag(currentRow.invoiceType)" size="small">{{ invoiceTypeMap[currentRow.invoiceType] }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="发票抬头" :span="2">{{ currentRow.invoiceTitle }}</el-descriptions-item>
        <el-descriptions-item label="开票金额">
          <span class="price">¥{{ formatMoney(currentRow.invoiceAmount) }}</span>
        </el-descriptions-item>
        <el-descriptions-item label="订单金额">
          <span class="price">¥{{ formatMoney(currentRow.orderAmount) }}</span>
        </el-descriptions-item>
        <el-descriptions-item label="开票项目">{{ invoiceItemMap[currentRow.invoiceItem] || '-' }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="statusType(currentRow.status)" size="small">{{ statusLabel(currentRow.status) }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="发票代码">{{ currentRow.invoiceCode || '-' }}</el-descriptions-item>
        <el-descriptions-item label="发票号码">{{ currentRow.invoiceNumber || '-' }}</el-descriptions-item>
        <el-descriptions-item label="电子发票">{{ currentRow.isElectronic ? '是' : '否' }}</el-descriptions-item>
        <el-descriptions-item label="申请时间">{{ currentRow.applyTime }}</el-descriptions-item>
        <el-descriptions-item label="税号">{{ currentRow.taxNo || '-' }}</el-descriptions-item>
        <el-descriptions-item label="联系电话">{{ currentRow.contactPhone || '-' }}</el-descriptions-item>
        <el-descriptions-item label="联系邮箱" :span="2">{{ currentRow.contactEmail || '-' }}</el-descriptions-item>
        <el-descriptions-item v-if="currentRow.mailTrackingNo" label="快递公司">{{ currentRow.mailCompany || '-' }}</el-descriptions-item>
        <el-descriptions-item v-if="currentRow.mailTrackingNo" label="快递单号">{{ currentRow.mailTrackingNo }}</el-descriptions-item>
        <el-descriptions-item v-if="currentRow.mailRecipient" label="收件人">{{ currentRow.mailRecipient }}</el-descriptions-item>
        <el-descriptions-item v-if="currentRow.mailAddress" label="邮寄地址">{{ currentRow.mailAddress }}</el-descriptions-item>
      </el-descriptions>
      <el-divider v-if="currentRow && currentRow.actionHistory && currentRow.actionHistory.length" />
      <div v-if="currentRow && currentRow.actionHistory && currentRow.actionHistory.length" class="action-history">
        <h4>操作记录</h4>
        <el-timeline>
          <el-timeline-item
            v-for="(item, idx) in currentRow.actionHistory"
            :key="idx"
            :timestamp="item.actionTime"
            placement="top"
          >
            {{ item.actionDesc }}
          </el-timeline-item>
        </el-timeline>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Wallet, Document, Clock, Van } from '@element-plus/icons-vue'
import api from '@/api'

const invoiceTypeMap = { 1: '普通发票(个人)', 2: '普通发票(企业)', 3: '专用发票' }
const invoiceItemMap = { 1: '住宿费', 2: '餐饮费', 3: '其他' }

const invoiceTypeTag = (type) => {
  const map = { 1: '', 2: 'warning', 3: 'danger' }
  return map[type] ?? 'info'
}

const statusLabel = (status) => {
  const map = { 0: '待开票', 1: '已开票', 2: '待邮寄', 3: '已邮寄', 4: '已完成' }
  return map[status] ?? '未知'
}

const statusType = (status) => {
  const map = { 0: 'warning', 1: '', 2: 'info', 3: '', 4: 'success' }
  return map[status] ?? 'info'
}

const formatMoney = (val) => {
  if (val === null || val === undefined) return '0.00'
  return Number(val).toFixed(2)
}

const loading = ref(false)
const tableData = ref([])
const activeTab = ref('0')
const searchForm = reactive({
  keyword: '',
  invoiceType: null,
  dateRange: null
})
const pagination = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0
})
const statistics = ref({
  totalAmount: 0,
  totalCount: 0,
  pendingCount: 0,
  pendingMailCount: 0
})
const currentRow = ref(null)

const uploadHeaders = computed(() => {
  const token = localStorage.getItem('token')
  return token ? { Authorization: `Bearer ${token}` } : {}
})

const loadList = async () => {
  loading.value = true
  try {
    const params = {
      pageNum: pagination.pageNum,
      pageSize: pagination.pageSize,
      status: parseInt(activeTab.value),
      keyword: searchForm.keyword || undefined,
      invoiceType: searchForm.invoiceType ?? undefined
    }
    if (searchForm.dateRange && searchForm.dateRange.length === 2) {
      params.startDate = searchForm.dateRange[0]
      params.endDate = searchForm.dateRange[1]
    }
    const res = await api.finance.invoice.page(params)
    if (res.code === 200) {
      tableData.value = res.data.list
      pagination.total = res.data.total
    }
  } catch {
    ElMessage.error('加载失败')
  } finally {
    loading.value = false
  }
}

const loadStatistics = async () => {
  try {
    const params = {}
    if (searchForm.dateRange && searchForm.dateRange.length === 2) {
      params.startDate = searchForm.dateRange[0]
      params.endDate = searchForm.dateRange[1]
    }
    const res = await api.finance.invoice.statistics(params)
    if (res.code === 200) {
      statistics.value = {
        totalAmount: res.data.totalAmount || 0,
        totalCount: res.data.totalCount || 0,
        pendingCount: res.data.pendingCount || 0,
        pendingMailCount: res.data.pendingMailCount || 0
      }
    }
  } catch {
  }
}

const handleSearch = () => {
  pagination.pageNum = 1
  loadList()
  loadStatistics()
}

const handleReset = () => {
  searchForm.keyword = ''
  searchForm.invoiceType = null
  searchForm.dateRange = null
  pagination.pageNum = 1
  loadList()
  loadStatistics()
}

const handleTabChange = () => {
  pagination.pageNum = 1
  loadList()
}

const handleExport = async () => {
  try {
    const params = {
      status: parseInt(activeTab.value),
      keyword: searchForm.keyword || undefined,
      invoiceType: searchForm.invoiceType ?? undefined
    }
    if (searchForm.dateRange && searchForm.dateRange.length === 2) {
      params.startDate = searchForm.dateRange[0]
      params.endDate = searchForm.dateRange[1]
    }
    const res = await api.finance.invoice.export(params)
    if (res.code === 200) {
      let csv = '\uFEFF发票编号,关联单号,客户名称,发票类型,发票抬头,开票金额,开票项目,状态,申请时间\n'
      const list = res.data || []
      list.forEach(item => {
        csv += `${item.invoiceNo},${item.relatedOrderNo},${item.customerName},${invoiceTypeMap[item.invoiceType] || ''},${item.invoiceTitle},${item.invoiceAmount},${invoiceItemMap[item.invoiceItem] || ''},${statusLabel(item.status)},${item.applyTime}\n`
      })
      const blob = new Blob([csv], { type: 'text/csv;charset=utf-8;' })
      const link = document.createElement('a')
      link.href = URL.createObjectURL(blob)
      link.download = `发票管理_${new Date().toISOString().slice(0, 10)}.csv`
      link.click()
      URL.revokeObjectURL(link.href)
      ElMessage.success('导出成功')
    }
  } catch {
    ElMessage.error('导出失败')
  }
}

const applyDialogVisible = ref(false)
const applySaving = ref(false)
const applyFormRef = ref(null)
const applyForm = reactive({
  relatedOrderType: 1,
  relatedOrderNo: '',
  customerName: '',
  invoiceType: null,
  invoiceTitle: '',
  taxNo: '',
  companyAddress: '',
  bankAccount: '',
  invoiceAmount: null,
  orderAmount: '',
  invoiceItem: null,
  contactPhone: '',
  contactEmail: ''
})

const applyRules = {
  relatedOrderType: [{ required: true, message: '请选择关联单类型', trigger: 'change' }],
  relatedOrderNo: [{ required: true, message: '请输入关联单号', trigger: 'blur' }],
  customerName: [{ required: true, message: '请输入客户名称', trigger: 'blur' }],
  invoiceType: [{ required: true, message: '请选择发票类型', trigger: 'change' }],
  invoiceTitle: [{ required: true, message: '请输入发票抬头', trigger: 'blur' }],
  invoiceAmount: [{ required: true, message: '请输入开票金额', trigger: 'blur' }],
  invoiceItem: [{ required: true, message: '请选择开票项目', trigger: 'change' }]
}

const openApplyDialog = () => {
  applyForm.relatedOrderType = 1
  applyForm.relatedOrderNo = ''
  applyForm.customerName = ''
  applyForm.invoiceType = null
  applyForm.invoiceTitle = ''
  applyForm.taxNo = ''
  applyForm.companyAddress = ''
  applyForm.bankAccount = ''
  applyForm.invoiceAmount = null
  applyForm.orderAmount = ''
  applyForm.invoiceItem = null
  applyForm.contactPhone = ''
  applyForm.contactEmail = ''
  applyDialogVisible.value = true
}

const handleApplySubmit = async () => {
  const valid = await applyFormRef.value.validate().catch(() => false)
  if (!valid) return
  applySaving.value = true
  try {
    const payload = {
      relatedOrderType: applyForm.relatedOrderType,
      relatedOrderNo: applyForm.relatedOrderNo,
      customerName: applyForm.customerName,
      invoiceType: applyForm.invoiceType,
      invoiceTitle: applyForm.invoiceTitle,
      taxNo: applyForm.taxNo || undefined,
      companyAddress: applyForm.companyAddress || undefined,
      bankAccount: applyForm.bankAccount || undefined,
      invoiceAmount: applyForm.invoiceAmount,
      invoiceItem: applyForm.invoiceItem,
      contactPhone: applyForm.contactPhone || undefined,
      contactEmail: applyForm.contactEmail || undefined
    }
    const res = await api.finance.invoice.add(payload)
    if (res.code === 200) {
      ElMessage.success('开票申请提交成功')
      applyDialogVisible.value = false
      loadList()
      loadStatistics()
    } else {
      ElMessage.error(res.message || '提交失败')
    }
  } catch {
    ElMessage.error('提交失败')
  } finally {
    applySaving.value = false
  }
}

const processDialogVisible = ref(false)
const processSaving = ref(false)
const processFormRef = ref(null)
const processForm = reactive({
  id: null,
  invoiceCode: '',
  invoiceNumber: '',
  invoicePdf: [],
  isElectronic: false
})

const processRules = {
  invoiceCode: [{ required: true, message: '请输入发票代码', trigger: 'blur' }],
  invoiceNumber: [{ required: true, message: '请输入发票号码', trigger: 'blur' }]
}

const openProcessDialog = (row) => {
  processForm.id = row.id
  processForm.invoiceCode = ''
  processForm.invoiceNumber = ''
  processForm.invoicePdf = []
  processForm.isElectronic = false
  processDialogVisible.value = true
}

const handleProcessSubmit = async () => {
  const valid = await processFormRef.value.validate().catch(() => false)
  if (!valid) return
  processSaving.value = true
  try {
    const payload = {
      invoiceCode: processForm.invoiceCode,
      invoiceNumber: processForm.invoiceNumber,
      isElectronic: processForm.isElectronic
    }
    const res = await api.finance.invoice.process(processForm.id, payload)
    if (res.code === 200) {
      ElMessage.success('开票成功')
      processDialogVisible.value = false
      loadList()
      loadStatistics()
    } else {
      ElMessage.error(res.message || '开票失败')
    }
  } catch {
    ElMessage.error('开票失败')
  } finally {
    processSaving.value = false
  }
}

const mailDialogVisible = ref(false)
const mailSaving = ref(false)
const mailFormRef = ref(null)
const mailForm = reactive({
  id: null,
  mailRecipient: '',
  mailAddress: '',
  mailCompany: '',
  mailTrackingNo: ''
})

const mailRules = {
  mailRecipient: [{ required: true, message: '请输入收件人', trigger: 'blur' }],
  mailAddress: [{ required: true, message: '请输入邮寄地址', trigger: 'blur' }],
  mailCompany: [{ required: true, message: '请输入快递公司', trigger: 'blur' }],
  mailTrackingNo: [{ required: true, message: '请输入快递单号', trigger: 'blur' }]
}

const openMailDialog = (row) => {
  mailForm.id = row.id
  mailForm.mailRecipient = ''
  mailForm.mailAddress = ''
  mailForm.mailCompany = ''
  mailForm.mailTrackingNo = ''
  mailDialogVisible.value = true
}

const handleMailSubmit = async () => {
  const valid = await mailFormRef.value.validate().catch(() => false)
  if (!valid) return
  mailSaving.value = true
  try {
    const payload = {
      mailRecipient: mailForm.mailRecipient,
      mailAddress: mailForm.mailAddress,
      mailCompany: mailForm.mailCompany,
      mailTrackingNo: mailForm.mailTrackingNo
    }
    const res = await api.finance.invoice.mail(mailForm.id, payload)
    if (res.code === 200) {
      ElMessage.success('邮寄信息提交成功')
      mailDialogVisible.value = false
      loadList()
      loadStatistics()
    } else {
      ElMessage.error(res.message || '提交失败')
    }
  } catch {
    ElMessage.error('提交失败')
  } finally {
    mailSaving.value = false
  }
}

const handleComplete = async (row) => {
  try {
    const res = await api.finance.invoice.process(row.id, { status: 4 })
    if (res.code === 200) {
      ElMessage.success('操作成功')
      loadList()
      loadStatistics()
    } else {
      ElMessage.error(res.message || '操作失败')
    }
  } catch {
    ElMessage.error('操作失败')
  }
}

const detailDialogVisible = ref(false)

const openDetailDialog = async (row) => {
  try {
    const res = await api.finance.invoice.get(row.id)
    if (res.code === 200) {
      currentRow.value = res.data
      detailDialogVisible.value = true
    }
  } catch {
    ElMessage.error('获取详情失败')
  }
}

onMounted(() => {
  loadList()
  loadStatistics()
})
</script>

<style scoped>
.invoice-manage {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.title {
  font-size: 16px;
  font-weight: 600;
}

.search-form {
  margin-bottom: 16px;
}

.summary-row {
  margin-top: 8px;
}

.stat-card {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 20px;
  border-radius: 8px;
  background: #f5f7fa;
}

.stat-card .stat-icon {
  width: 56px;
  height: 56px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  flex-shrink: 0;
}

.stat-card.blue .stat-icon {
  background: linear-gradient(135deg, #409eff 0%, #66b1ff 100%);
}

.stat-card.green .stat-icon {
  background: linear-gradient(135deg, #67c23a 0%, #85ce61 100%);
}

.stat-card.orange .stat-icon {
  background: linear-gradient(135deg, #e6a23c 0%, #ebb563 100%);
}

.stat-card.red .stat-icon {
  background: linear-gradient(135deg, #f56c6c 0%, #f78989 100%);
}

.stat-info .stat-value {
  font-size: 22px;
  font-weight: 700;
  color: #303133;
  line-height: 1.3;
}

.stat-info .stat-label {
  font-size: 13px;
  color: #909399;
  margin-top: 4px;
}

.stat-card.red .stat-value {
  color: #f56c6c;
}

.pagination {
  margin-top: 20px;
  justify-content: flex-end;
  display: flex;
}

.price {
  color: #409eff;
  font-weight: 600;
}

.detail-desc {
  margin-bottom: 16px;
}

.action-history h4 {
  font-size: 14px;
  font-weight: 600;
  margin-bottom: 12px;
  color: #303133;
}
</style>
