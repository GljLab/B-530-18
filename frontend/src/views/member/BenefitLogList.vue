<template>
  <div class="benefit-log-list-container">
    <el-card shadow="never" class="search-card">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="会员信息">
          <el-input
            v-model="searchForm.keyword"
            placeholder="会员卡号/姓名/手机号"
            clearable
            style="width: 220px"
            @keyup.enter="handleSearch"
          />
        </el-form-item>
        <el-form-item label="权益类型">
          <el-select v-model="searchForm.benefitType" placeholder="全部" clearable style="width: 160px">
            <el-option label="房费折扣" :value="1" />
            <el-option label="免费升级房型" :value="2" />
            <el-option label="押金减免" :value="3" />
            <el-option label="延迟退房" :value="4" />
            <el-option label="积分发放" :value="5" />
            <el-option label="提前入住" :value="6" />
            <el-option label="免费早餐" :value="7" />
          </el-select>
        </el-form-item>
        <el-form-item label="关联单号">
          <el-input
            v-model="searchForm.relatedOrderNo"
            placeholder="预订/入住单号"
            clearable
            style="width: 180px"
            @keyup.enter="handleSearch"
          />
        </el-form-item>
        <el-form-item label="使用时间">
          <el-date-picker
            v-model="searchForm.timeRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            value-format="YYYY-MM-DD"
            style="width: 260px"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">
            <el-icon><Search /></el-icon>搜索
          </el-button>
          <el-button @click="handleReset">
            <el-icon><Refresh /></el-icon>重置
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card shadow="never" class="table-card">
      <div class="table-header">
        <div class="table-title">权益使用记录</div>
        <div class="table-actions">
          <el-button
            v-if="hasPermission('member:benefit:export')"
            @click="handleExport"
            :loading="exporting"
          >
            <el-icon><Download /></el-icon>导出
          </el-button>
        </div>
      </div>

      <el-table
        ref="tableRef"
        :data="tableData"
        stripe
        border
        v-loading="tableLoading"
        style="width: 100%"
      >
        <el-table-column prop="createTime" label="使用时间" width="170">
          <template #default="{ row }">
            {{ formatDateTime(row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column prop="memberNo" label="会员卡号" width="160" />
        <el-table-column prop="memberName" label="会员姓名" width="100" />
        <el-table-column prop="benefitTypeName" label="权益类型" width="120">
          <template #default="{ row }">
            <el-tag :type="getBenefitTagType(row.benefitType)" size="small">
              {{ row.benefitTypeName }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="关联单号" width="160">
          <template #default="{ row }">
            <span v-if="row.relatedOrderType === 1">预订：</span>
            <span v-else-if="row.relatedOrderType === 2">入住：</span>
            {{ row.relatedOrderNo || '-' }}
          </template>
        </el-table-column>
        <el-table-column label="权益内容" min-width="280">
          <template #default="{ row }">
            <div class="benefit-content">
              <template v-if="row.benefitType === 1 || row.benefitType === 3">
                <div class="content-row">
                  <span class="label">原价：</span>
                  <span class="original">¥{{ formatAmount(row.originalAmount) }}</span>
                </div>
                <div class="content-row">
                  <span class="label">优惠：</span>
                  <span class="benefit">-¥{{ formatAmount(row.benefitAmount) }}</span>
                </div>
                <div class="content-row">
                  <span class="label">实付：</span>
                  <span class="actual">¥{{ formatAmount(row.actualAmount) }}</span>
                </div>
              </template>
              <template v-else-if="row.benefitType === 2">
                <span class="upgrade-content">{{ row.benefitContent }}</span>
              </template>
              <template v-else-if="row.benefitType === 4">
                <span class="late-checkout-content">{{ row.benefitContent }}</span>
              </template>
              <template v-else-if="row.benefitType === 5">
                <div class="content-row">
                  <span class="label">消费金额：</span>
                  <span>¥{{ formatAmount(row.originalAmount) }}</span>
                </div>
                <div class="content-row">
                  <span class="label">获得积分：</span>
                  <span class="points">+{{ formatNumber(row.benefitAmount) }}</span>
                </div>
              </template>
              <template v-else>
                <span>{{ row.benefitContent || '-' }}</span>
              </template>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="operatorName" label="操作人" width="100" />
        <el-table-column prop="remark" label="备注" width="150" show-overflow-tooltip />
        <el-table-column label="操作" width="120" align="center">
          <template #default="{ row }">
            <el-button
              type="primary"
              link
              size="small"
              @click="handleViewDetail(row)"
            >
              详情
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination">
        <el-pagination
          v-model:current-page="page.pageNum"
          v-model:page-size="page.pageSize"
          :page-sizes="[10, 20, 50, 100]"
          :total="page.total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handlePageChange"
        />
      </div>
    </el-card>

    <el-dialog v-model="detailDialogVisible" title="权益使用详情" width="600px">
      <el-descriptions v-if="currentLog" :column="2" border>
        <el-descriptions-item label="使用时间" :span="2">
          {{ formatDateTime(currentLog.createTime) }}
        </el-descriptions-item>
        <el-descriptions-item label="会员卡号">{{ currentLog.memberNo }}</el-descriptions-item>
        <el-descriptions-item label="会员姓名">{{ currentLog.memberName }}</el-descriptions-item>
        <el-descriptions-item label="权益类型">
          <el-tag :type="getBenefitTagType(currentLog.benefitType)" size="small">
            {{ currentLog.benefitTypeName }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="关联单号">
          <span v-if="currentLog.relatedOrderType === 1">预订：</span>
          <span v-else-if="currentLog.relatedOrderType === 2">入住：</span>
          {{ currentLog.relatedOrderNo || '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="权益内容" :span="2">
          <span style="white-space: pre-wrap">{{ currentLog.benefitContent }}</span>
        </el-descriptions-item>
        <el-descriptions-item v-if="currentLog.originalAmount !== null && currentLog.originalAmount !== undefined" label="原始金额">
          ¥{{ formatAmount(currentLog.originalAmount) }}
        </el-descriptions-item>
        <el-descriptions-item v-if="currentLog.benefitAmount !== null && currentLog.benefitAmount !== undefined" label="权益金额">
          <span v-if="currentLog.benefitType === 1 || currentLog.benefitType === 3" style="color: #67c23a">
            -¥{{ formatAmount(currentLog.benefitAmount) }}
          </span>
          <span v-else-if="currentLog.benefitType === 5" style="color: #e6a23c">
            +{{ formatNumber(currentLog.benefitAmount) }} 积分
          </span>
          <span v-else>¥{{ formatAmount(currentLog.benefitAmount) }}</span>
        </el-descriptions-item>
        <el-descriptions-item v-if="currentLog.actualAmount !== null && currentLog.actualAmount !== undefined" label="实际金额">
          <span style="color: #f56c6c; font-weight: 600">¥{{ formatAmount(currentLog.actualAmount) }}</span>
        </el-descriptions-item>
        <el-descriptions-item label="操作人">{{ currentLog.operatorName || '系统' }}</el-descriptions-item>
        <el-descriptions-item label="备注" :span="2">
          {{ currentLog.remark || '-' }}
        </el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button @click="detailDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Search, Refresh, Download } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import api from '@/api'

const userStore = useUserStore()
const hasPermission = (p) => userStore.hasPermission(p)

const tableLoading = ref(false)
const exporting = ref(false)
const tableData = ref([])
const page = reactive({
  pageNum: 1,
  pageSize: 20,
  total: 0
})

const searchForm = reactive({
  keyword: '',
  benefitType: null,
  relatedOrderNo: '',
  timeRange: []
})

const detailDialogVisible = ref(false)
const currentLog = ref(null)

const getBenefitTagType = (type) => {
  const map = {
    1: 'primary',
    2: 'success',
    3: 'warning',
    4: 'info',
    5: 'warning',
    6: 'success',
    7: 'primary'
  }
  return map[type] || 'info'
}

const formatDateTime = (datetime) => {
  if (!datetime) return ''
  return datetime.replace('T', ' ').substring(0, 19)
}

const formatNumber = (num) => {
  if (num === null || num === undefined) return '0'
  return Number(num).toLocaleString()
}

const formatAmount = (amount) => {
  if (amount === null || amount === undefined) return '0.00'
  return Number(amount).toFixed(2)
}

const loadData = async () => {
  tableLoading.value = true
  try {
    const params = {
      keyword: searchForm.keyword || undefined,
      benefitType: searchForm.benefitType || undefined,
      relatedOrderNo: searchForm.relatedOrderNo || undefined,
      startTime: searchForm.timeRange?.[0] || undefined,
      endTime: searchForm.timeRange?.[1] || undefined,
      pageNum: page.pageNum,
      pageSize: page.pageSize
    }
    const res = await api.memberBenefit.getBenefitLogPage(params)
    if (res.code === 200) {
      tableData.value = res.data.list || []
      page.total = res.data.total || 0
    }
  } catch (e) {
    console.error('加载权益使用记录失败', e)
  } finally {
    tableLoading.value = false
  }
}

const handleSearch = () => {
  page.pageNum = 1
  loadData()
}

const handleReset = () => {
  searchForm.keyword = ''
  searchForm.benefitType = null
  searchForm.relatedOrderNo = ''
  searchForm.timeRange = []
  page.pageNum = 1
  loadData()
}

const handlePageChange = (p) => {
  page.pageNum = p
  loadData()
}

const handleSizeChange = (size) => {
  page.pageSize = size
  page.pageNum = 1
  loadData()
}

const handleExport = async () => {
  exporting.value = true
  try {
    const params = {
      keyword: searchForm.keyword || undefined,
      benefitType: searchForm.benefitType || undefined,
      relatedOrderNo: searchForm.relatedOrderNo || undefined,
      startTime: searchForm.timeRange?.[0] || undefined,
      endTime: searchForm.timeRange?.[1] || undefined
    }
    const res = await api.memberBenefit.exportBenefitLogs(params)
    if (res.code === 200) {
      const blob = new Blob([res.data], { type: 'application/vnd.ms-excel' })
      const url = window.URL.createObjectURL(blob)
      const link = document.createElement('a')
      link.href = url
      link.download = `权益使用记录_${new Date().toISOString().slice(0, 10)}.xlsx`
      document.body.appendChild(link)
      link.click()
      document.body.removeChild(link)
      window.URL.revokeObjectURL(url)
      ElMessage.success('导出成功')
    }
  } catch (e) {
    console.error('导出权益使用记录失败', e)
  } finally {
    exporting.value = false
  }
}

const handleViewDetail = (row) => {
  currentLog.value = row
  detailDialogVisible.value = true
}

onMounted(() => {
  loadData()
})
</script>

<style scoped lang="scss">
.benefit-log-list-container {
  padding: 16px;

  .search-card {
    margin-bottom: 16px;

    .search-form {
      display: flex;
      flex-wrap: wrap;
      gap: 16px;
    }
  }

  .table-card {
    .table-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 16px;

      .table-title {
        font-size: 16px;
        font-weight: 600;
        color: #303133;
      }
    }

    .benefit-content {
      .content-row {
        display: flex;
        align-items: center;
        margin-bottom: 4px;

        &:last-child {
          margin-bottom: 0;
        }

        .label {
          color: #909399;
          margin-right: 8px;
          min-width: 60px;
        }

        .original {
          text-decoration: line-through;
          color: #909399;
        }

        .benefit {
          color: #67c23a;
          font-weight: 600;
        }

        .actual {
          color: #f56c6c;
          font-weight: 600;
        }

        .points {
          color: #e6a23c;
          font-weight: 600;
        }
      }

      .upgrade-content,
      .late-checkout-content {
        color: #67c23a;
      }
    }

    .pagination {
      margin-top: 16px;
      display: flex;
      justify-content: flex-end;
    }
  }
}
</style>
