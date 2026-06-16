<template>
  <div class="booking-cycle-container">
    <el-card class="filter-card" shadow="never">
      <el-row :gutter="16" align="middle">
        <el-col :xs="24" :sm="8" :md="6">
          <el-date-picker
            v-model="queryForm.dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            style="width: 100%"
          />
        </el-col>
        <el-col :xs="8" :sm="4" :md="3">
          <el-select v-model="queryForm.roomTypeId" placeholder="房型" clearable style="width: 100%">
            <el-option v-for="rt in roomTypes" :key="rt.typeId" :label="rt.typeName" :value="rt.typeId" />
          </el-select>
        </el-col>
        <el-col :xs="8" :sm="4" :md="3">
          <el-select v-model="queryForm.channelId" placeholder="渠道" clearable style="width: 100%">
            <el-option v-for="ch in channels" :key="ch.channelId" :label="ch.channelName" :value="ch.channelId" />
          </el-select>
        </el-col>
        <el-col :xs="8" :sm="4" :md="3">
          <el-select v-model="queryForm.customerType" placeholder="客户类型" clearable style="width: 100%">
            <el-option label="散客" value="individual" />
            <el-option label="团队" value="group" />
            <el-option label="协议客户" value="contract" />
            <el-option label="会员" value="member" />
          </el-select>
        </el-col>
        <el-col :xs="12" :sm="4" :md="3">
          <el-button type="primary" @click="handleQuery" :loading="loading">查询</el-button>
        </el-col>
      </el-row>
    </el-card>

    <el-row :gutter="20" class="stat-row">
      <el-col :xs="8">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-content">
            <div class="stat-icon" style="background: linear-gradient(135deg, #667eea 0%, #764ba2 100%)">
              <el-icon :size="28"><Timer /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ summary.avgAdvanceDays }}</div>
              <div class="stat-label">平均提前天数</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="8">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-content">
            <div class="stat-icon" style="background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%)">
              <el-icon :size="28"><Lightning /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ summary.instantRate }}%</div>
              <div class="stat-label">即时预订率</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="8">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-content">
            <div class="stat-icon" style="background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%)">
              <el-icon :size="28"><CircleClose /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ summary.cancelRate }}%</div>
              <div class="stat-label">取消率</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-card class="chart-card" shadow="hover">
      <template #header>
        <span>提前预订天数分布</span>
      </template>
      <div ref="barChartRef" class="chart-container"></div>
    </el-card>

    <el-card class="table-card" shadow="hover">
      <template #header>
        <span>平均提前天数明细</span>
      </template>
      <el-table :data="tableData" stripe style="width: 100%">
        <el-table-column prop="roomTypeName" label="房型" min-width="120" />
        <el-table-column prop="channelName" label="渠道" min-width="120" />
        <el-table-column prop="customerType" label="客户类型" min-width="120">
          <template #default="{ row }">
            {{ customerTypeMap[row.customerType] || row.customerType }}
          </template>
        </el-table-column>
        <el-table-column prop="avgAdvanceDays" label="平均提前天数" width="140" align="center" />
        <el-table-column prop="bookingCount" label="预订数" width="100" align="center" />
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount, nextTick } from 'vue'
import { Timer, Lightning, CircleClose } from '@element-plus/icons-vue'
import * as echarts from 'echarts'
import api from '@/api'

const customerTypeMap = { individual: '散客', group: '团队', contract: '协议客户', member: '会员' }

const loading = ref(false)
const queryForm = ref({
  dateRange: [],
  roomTypeId: null,
  channelId: null,
  customerType: null
})

const roomTypes = ref([])
const channels = ref([])
const summary = ref({ avgAdvanceDays: 0, instantRate: 0, cancelRate: 0 })
const distribution = ref([])
const tableData = ref([])

const barChartRef = ref(null)
let barChart = null

const loadOptions = async () => {
  try {
    const [rtRes, chRes] = await Promise.all([
      api.hotel.getRoomTypes(),
      api.channel.list()
    ])
    if (rtRes.code === 200) roomTypes.value = rtRes.data || []
    if (chRes.code === 200) channels.value = chRes.data || []
  } catch {}
}

const loadData = async () => {
  const params = {}
  if (queryForm.value.dateRange && queryForm.value.dateRange.length === 2) {
    params.startDate = queryForm.value.dateRange[0]
    params.endDate = queryForm.value.dateRange[1]
  }
  if (queryForm.value.roomTypeId) params.roomTypeId = queryForm.value.roomTypeId
  if (queryForm.value.channelId) params.channelId = queryForm.value.channelId
  if (queryForm.value.customerType) params.customerType = queryForm.value.customerType

  try {
    loading.value = true
    const res = await api.analytics.bookingCycle(params)
    if (res.code === 200 && res.data) {
      summary.value = res.data.summary || { avgAdvanceDays: 0, instantRate: 0, cancelRate: 0 }
      distribution.value = res.data.distribution || []
      tableData.value = res.data.details || []
      await nextTick()
      renderBarChart()
    }
  } catch {
  } finally {
    loading.value = false
  }
}

const renderBarChart = () => {
  if (!barChartRef.value) return
  if (!barChart) {
    barChart = echarts.init(barChartRef.value)
  }
  const labels = ['1天内', '1-3天', '3-7天', '7-14天', '14-30天', '30天以上']
  const data = distribution.value.length > 0
    ? distribution.value.map(d => d.count)
    : [0, 0, 0, 0, 0, 0]

  barChart.setOption({
    tooltip: {
      trigger: 'axis',
      axisPointer: { type: 'shadow' },
      formatter: '{b}<br/>预订数: {c}'
    },
    grid: { left: 60, right: 30, top: 30, bottom: 40 },
    xAxis: {
      type: 'category',
      data: labels,
      axisLabel: { color: '#718096' }
    },
    yAxis: {
      type: 'value',
      name: '预订数',
      axisLabel: { color: '#718096' },
      splitLine: { lineStyle: { type: 'dashed' } }
    },
    series: [{
      type: 'bar',
      data: data,
      barWidth: '50%',
      itemStyle: {
        borderRadius: [6, 6, 0, 0],
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: '#667eea' },
          { offset: 1, color: '#764ba2' }
        ])
      },
      label: { show: true, position: 'top', color: '#4a5568', formatter: '{c}' }
    }]
  })
}

const handleQuery = () => {
  loadData()
}

const handleResize = () => {
  barChart?.resize()
}

onMounted(() => {
  loadOptions()
  loadData()
  window.addEventListener('resize', handleResize)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize)
  barChart?.dispose()
})
</script>

<style scoped>
.booking-cycle-container {
  padding: 10px;
}

.filter-card {
  margin-bottom: 20px;
  border-radius: 12px;
  border: none;
}

.stat-row {
  margin-bottom: 20px;
}

.stat-card {
  border-radius: 12px;
  border: none;
  transition: transform 0.3s;
}

.stat-card:hover {
  transform: translateY(-5px);
}

.stat-content {
  display: flex;
  align-items: center;
  gap: 15px;
  padding: 10px;
}

.stat-icon {
  width: 60px;
  height: 60px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
}

.stat-info {
  flex: 1;
}

.stat-value {
  font-size: 28px;
  font-weight: 700;
  color: #2d3748;
}

.stat-label {
  font-size: 14px;
  color: #718096;
}

.chart-card,
.table-card {
  margin-bottom: 20px;
  border-radius: 12px;
  border: none;
}

.chart-container {
  width: 100%;
  height: 400px;
}
</style>
