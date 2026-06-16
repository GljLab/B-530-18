<template>
  <div class="booking-list-container">
    <el-row :gutter="16" class="stats-row">
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card stat-new">
          <div class="stat-content">
            <div class="stat-value">{{ todayStats.newBookings || 0 }}</div>
            <div class="stat-label">今日新增预订</div>
          </div>
          <el-icon :size="40" class="stat-icon"><Calendar /></el-icon>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card stat-pending">
          <div class="stat-content">
            <div class="stat-value">{{ todayStats.pendingCheckin || 0 }}</div>
            <div class="stat-label">今日待入住</div>
          </div>
          <el-icon :size="40" class="stat-icon"><Clock /></el-icon>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card stat-checkedin">
          <div class="stat-content">
            <div class="stat-value">{{ todayStats.checkedIn || 0 }}</div>
            <div class="stat-label">今日已入住</div>
          </div>
          <el-icon :size="40" class="stat-icon"><User /></el-icon>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card stat-cancelled">
          <div class="stat-content">
            <div class="stat-value">{{ todayStats.cancelled || 0 }}</div>
            <div class="stat-label">今日取消</div>
          </div>
          <el-icon :size="40" class="stat-icon"><CircleClose /></el-icon>
        </el-card>
      </el-col>
    </el-row>

    <el-card shadow="never" class="filter-card">
      <div class="filter-row">
        <el-input v-model="queryParams.keyword" placeholder="预订单号/客户姓名/手机号" clearable style="width: 220px" @keyup.enter="handleSearch">
          <template #prefix><el-icon><Search /></el-icon></template>
        </el-input>
        <el-select v-model="queryParams.roomTypeId" placeholder="房型" clearable style="width: 140px" @change="handleSearch">
          <el-option v-for="rt in roomTypes" :key="rt.id" :label="rt.typeName" :value="rt.id" />
        </el-select>
        <el-select v-model="queryParams.status" placeholder="预订状态" clearable style="width: 140px" @change="handleSearch">
          <el-option v-for="s in statusOptions" :key="s.value" :label="s.label" :value="s.value" />
        </el-select>
        <el-select v-model="queryParams.source" placeholder="预订来源" clearable style="width: 140px" @change="handleSearch">
          <el-option v-for="s in sourceOptions" :key="s.value" :label="s.label" :value="s.value" />
        </el-select>
        <el-date-picker
          v-model="queryParams.checkinDateRange"
          type="daterange"
          range-separator="至"
          start-placeholder="入住开始日期"
          end-placeholder="入住结束日期"
          value-format="YYYY-MM-DD"
          style="width: 280px"
          @change="handleSearch"
        />
        <el-date-picker
          v-model="queryParams.bookingTimeRange"
          type="datetimerange"
          range-separator="至"
          start-placeholder="预订开始时间"
          end-placeholder="预订结束时间"
          value-format="YYYY-MM-DD HH:mm:ss"
          style="width: 320px"
          @change="handleSearch"
        />
        <el-select v-model="queryParams.sortBy" placeholder="排序" clearable style="width: 160px" @change="handleSearch">
          <el-option label="按预订时间降序" value="bookingTime_desc" />
          <el-option label="按预订时间升序" value="bookingTime_asc" />
          <el-option label="按入住日期降序" value="checkinDate_desc" />
          <el-option label="按入住日期升序" value="checkinDate_asc" />
          <el-option label="按金额降序" value="amount_desc" />
          <el-option label="按金额升序" value="amount_asc" />
        </el-select>
        <el-button type="primary" @click="handleSearch">
          <el-icon><Search /></el-icon>搜索
        </el-button>
        <el-button @click="handleReset">
          <el-icon><Refresh /></el-icon>重置
        </el-button>
      </div>
    </el-card>

    <el-card shadow="never" class="table-card">
      <div class="table-toolbar">
        <div class="table-toolbar-left">
          <el-button v-if="hasPermission('booking:add')" type="primary" @click="handleAddBooking">
            <el-icon><Plus /></el-icon>新增预订
          </el-button>
          <el-button
            v-if="hasPermission('booking:batch:confirm')"
            :disabled="selectedRows.length === 0 || !canBatchConfirm"
            @click="handleBatchConfirm"
          >
            <el-icon><CircleCheck /></el-icon>批量确认
          </el-button>
          <el-button v-if="hasPermission('booking:export')" @click="openExportDialog">
            <el-icon><Download /></el-icon>导出
          </el-button>
        </div>
        <div v-if="selectedRows.length > 0" class="batch-toolbar">
          <el-tag type="info" class="batch-count-tag">
            已选择 <strong>{{ selectedRows.length }}</strong> 条预订单
          </el-tag>
          <el-button size="small" @click="clearSelection">取消选择</el-button>
        </div>
      </div>

      <el-table
        :data="tableData"
        stripe
        border
        v-loading="tableLoading"
        style="width: 100%"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="50" align="center" />
        <el-table-column prop="bookingNo" label="预订单号" width="180" align="center">
          <template #default="{ row }">
            <el-link type="primary" @click="openDetailDialog(row)">
              {{ row.bookingNo }}
            </el-link>
          </template>
        </el-table-column>
        <el-table-column prop="customerName" label="客户姓名" width="110" align="center" />
        <el-table-column prop="phone" label="手机号" width="130" align="center" />
        <el-table-column prop="roomTypeName" label="房型" min-width="140" />
        <el-table-column prop="roomNumber" label="房号" width="90" align="center">
          <template #default="{ row }">{{ row.roomNumber || '-' }}</template>
        </el-table-column>
        <el-table-column prop="checkinDate" label="入住日期" width="120" align="center" />
        <el-table-column prop="checkoutDate" label="退房日期" width="120" align="center" />
        <el-table-column prop="days" label="天数" width="80" align="center" />
        <el-table-column prop="totalAmount" label="应付金额" width="110" align="center">
          <template #default="{ row }">¥{{ row.totalAmount?.toFixed(2) || '0.00' }}</template>
        </el-table-column>
        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="statusTagType(row.status)" size="small">{{ statusLabel(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="bookingTime" label="预订时间" width="170" align="center" />
        <el-table-column prop="sourceName" label="预订来源" width="110" align="center" />
        <el-table-column label="操作" width="420" align="center" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="openDetailDialog(row)">详情</el-button>
            <template v-if="row.status === 1">
              <el-button v-if="hasPermission('booking:confirm')" type="primary" link size="small" @click="handleConfirm(row)">确认</el-button>
              <el-button v-if="hasPermission('booking:edit')" type="primary" link size="small" @click="openEditDialog(row)">修改</el-button>
              <el-button v-if="hasPermission('booking:changeRoom')" type="warning" link size="small" @click="openChangeRoomDialog(row)">换房</el-button>
              <el-button v-if="hasPermission('booking:cancel')" type="danger" link size="small" @click="handleCancel(row)">取消</el-button>
            </template>
            <template v-else-if="row.status === 2">
              <el-button v-if="hasPermission('booking:checkin')" type="success" link size="small" @click="openCheckinDialog(row)">入住</el-button>
              <el-button v-if="hasPermission('booking:edit')" type="primary" link size="small" @click="openEditDialog(row)">修改</el-button>
              <el-button v-if="hasPermission('booking:changeRoom')" type="warning" link size="small" @click="openChangeRoomDialog(row)">换房</el-button>
              <el-button v-if="hasPermission('booking:cancel')" type="danger" link size="small" @click="handleCancel(row)">取消</el-button>
            </template>
            <template v-else-if="row.status === 3">
              <el-button v-if="hasPermission('booking:checkin')" type="success" link size="small" @click="openCheckinDialog(row)">入住</el-button>
              <el-button v-if="hasPermission('booking:refund')" type="warning" link size="small" @click="openRefundDialog(row)">退款</el-button>
            </template>
            <template v-else-if="row.status === 4">
              <el-button type="primary" link size="small" @click="handleViewCheckin(row)">查看入住</el-button>
            </template>
            <template v-else-if="row.status === 5">
              <el-button type="primary" link size="small" @click="handleViewCheckin(row)">查看入住</el-button>
            </template>
            <template v-else-if="row.status === 6">
              <el-button type="danger" link size="small" @click="handleViewCancel(row)">查看取消</el-button>
            </template>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrap">
        <el-pagination
          v-model:current-page="queryParams.pageNum"
          v-model:page-size="queryParams.pageSize"
          :page-sizes="[20, 50, 100]"
          :total="total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="loadBookings"
          @current-change="loadBookings"
        />
      </div>
    </el-card>

    <el-dialog v-model="cancelDialogVisible" title="取消预订" width="560px" destroy-on-close>
      <el-alert
        title="取消规则说明"
        type="info"
        :closable="false"
        show-icon
        class="cancel-rules-alert"
      >
        <div class="cancel-rules-list">
          <div v-for="(rule, idx) in cancelRulesConfig" :key="idx" class="cancel-rule-item">
            <span class="rule-condition">{{ rule.condition }}：</span>
            <span class="rule-penalty">{{ rule.penalty }}</span>
          </div>
        </div>
      </el-alert>

      <el-divider />

      <el-form ref="cancelFormRef" :model="cancelForm" :rules="cancelRules" label-width="100px">
        <el-form-item label="预订单号">
          <span class="form-value">{{ cancelForm.bookingNo }}</span>
        </el-form-item>
        <el-form-item label="应付总额">
          <span class="form-value price">¥{{ cancelForm.totalAmount?.toFixed(2) || '0.00' }}</span>
        </el-form-item>
        <el-form-item v-if="cancelPenaltyInfo" label="当前规则">
          <el-tag :type="cancelPenaltyInfo.penalty > 0 ? 'warning' : 'success'" size="small">
            {{ cancelPenaltyInfo.rule.condition }} - {{ cancelPenaltyInfo.rule.penalty }}
          </el-tag>
        </el-form-item>
        <el-form-item v-if="cancelPenaltyInfo" label="违约金">
          <span class="form-value" :class="cancelPenaltyInfo.penalty > 0 ? 'price-up' : 'price-down'">
            ¥{{ cancelPenaltyInfo.penalty?.toFixed(2) || '0.00' }}
          </span>
        </el-form-item>
        <el-form-item v-if="cancelPenaltyInfo" label="可退金额">
          <span class="form-value price">
            ¥{{ Math.max(0, (cancelForm.totalAmount || 0) - (cancelPenaltyInfo.penalty || 0)).toFixed(2) }}
          </span>
        </el-form-item>
        <el-form-item label="取消原因" prop="reason">
          <el-select v-model="cancelForm.reason" placeholder="请选择取消原因" style="width: 100%">
            <el-option label="客户取消" value="客户取消" />
            <el-option label="房源不可用" value="房源不可用" />
            <el-option label="价格变动" value="价格变动" />
            <el-option label="其他原因" value="其他原因" />
          </el-select>
        </el-form-item>
        <el-form-item label="收取违约金">
          <el-switch v-model="cancelForm.chargePenalty" />
        </el-form-item>
        <el-form-item v-if="cancelForm.chargePenalty" label="违约金金额">
          <el-input-number
            v-model="cancelForm.penaltyAmount"
            :min="0"
            :precision="2"
            :step="10"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="详细说明" prop="remark">
          <el-input v-model="cancelForm.remark" type="textarea" :rows="3" placeholder="请输入详细说明" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="cancelDialogVisible = false">取消</el-button>
        <el-button type="danger" :loading="cancelSaving" @click="handleConfirmCancel">确认取消</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="refundDialogVisible" title="申请退款" width="520px" destroy-on-close>
      <el-form ref="refundFormRef" :model="refundForm" :rules="refundRules" label-width="120px">
        <el-form-item label="预订单号">
          <span class="form-value">{{ refundForm.bookingNo }}</span>
        </el-form-item>
        <el-form-item label="已付金额">
          <span class="form-value price">¥{{ refundForm.paidAmount?.toFixed(2) || '0.00' }}</span>
        </el-form-item>
        <el-form-item label="违约扣款">
          <span class="form-value price-up">¥{{ refundForm.cancelPenalty?.toFixed(2) || '0.00' }}</span>
        </el-form-item>
        <el-form-item label="应退金额">
          <span class="form-value price">¥{{ Math.max(0, (refundForm.paidAmount || 0) - (refundForm.cancelPenalty || 0)).toFixed(2) }}</span>
        </el-form-item>
        <el-divider />
        <el-form-item label="退款原因" prop="refundReason">
          <el-select v-model="refundForm.refundReason" placeholder="请选择退款原因" style="width: 100%">
            <el-option v-for="reason in refundReasons" :key="reason.value" :label="reason.label" :value="reason.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="退款金额" prop="refundAmount">
          <el-input-number
            v-model="refundForm.refundAmount"
            :min="0.01"
            :max="refundForm.paidAmount"
            :precision="2"
            :step="10"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="退款说明" prop="refundRemark">
          <el-input v-model="refundForm.refundRemark" type="textarea" :rows="3" placeholder="请输入退款说明" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="refundDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="refundSaving" @click="handleRefund">提交申请</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="checkinDialogVisible" title="办理入住" width="480px" destroy-on-close>
      <el-form ref="checkinFormRef" :model="checkinForm" :rules="checkinRules" label-width="100px">
        <el-form-item label="预订单号">
          <span class="form-value">{{ checkinForm.bookingNo }}</span>
        </el-form-item>
        <el-form-item v-if="currentCheckinBooking" label="客户姓名">
          <span class="form-value">{{ currentCheckinBooking.customerName || '-' }}</span>
        </el-form-item>
        <el-form-item v-if="currentCheckinBooking" label="房间信息">
          <span class="form-value">
            {{ currentCheckinBooking.roomTypeName || '-' }} - {{ currentCheckinBooking.roomNo || '-' }}
          </span>
        </el-form-item>
        <el-form-item v-if="currentCheckinBooking" label="入住日期">
          <span class="form-value">
            {{ currentCheckinBooking.checkinDate || '-' }} 至 {{ currentCheckinBooking.checkoutDate || '-' }}
          </span>
        </el-form-item>
        <el-form-item label="入住时间" prop="checkInTime">
          <el-date-picker
            v-model="checkinForm.checkInTime"
            type="datetime"
            placeholder="请选择入住时间"
            format="YYYY-MM-DD HH:mm"
            value-format="YYYY-MM-DDTHH:mm"
            style="width: 100%"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="checkinDialogVisible = false">取消</el-button>
        <el-button type="success" :loading="checkinSaving" @click="handleCheckin">确认入住</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="exportDialogVisible" title="导出预订单" width="560px" destroy-on-close>
      <el-form label-width="100px">
        <el-form-item label="导出范围">
          <el-radio-group v-model="exportForm.scope">
            <el-radio value="current">当前页 ({{ tableData.length }}条)</el-radio>
            <el-radio value="all">全部 ({{ total }}条)</el-radio>
            <el-radio value="selected">已选择 ({{ selectedRows.length }}条)</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="导出字段">
          <el-checkbox-group v-model="exportForm.exportFields">
            <el-checkbox v-for="f in exportFieldOptions" :key="f.value" :value="f.value">{{ f.label }}</el-checkbox>
          </el-checkbox-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="exportDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="exportSaving" @click="handleExport">确认导出</el-button>
      </template>
    </el-dialog>

    <el-dialog
      v-model="editDialogVisible"
      title="修改预订"
      width="680px"
      destroy-on-close
    >
      <el-form
        ref="editFormRef"
        :model="editForm"
        :rules="editRules"
        label-width="110px"
        label-position="right"
      >
        <el-alert
          v-if="currentEditBooking"
          :title="`修改预订单：${currentEditBooking.bookingNo}`"
          type="info"
          :closable="false"
          class="edit-alert"
        />
        <el-divider content-position="left">入住信息</el-divider>
        <el-row :gutter="24">
          <el-col :span="12">
            <el-form-item label="入住日期" prop="checkInDate">
              <el-date-picker
                v-model="editForm.checkInDate"
                type="date"
                placeholder="请选择入住日期"
                style="width: 100%"
                value-format="YYYY-MM-DD"
                @change="handleEditDateChange"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="退房日期" prop="checkOutDate">
              <el-date-picker
                v-model="editForm.checkOutDate"
                type="date"
                placeholder="请选择退房日期"
                style="width: 100%"
                value-format="YYYY-MM-DD"
                @change="handleEditDateChange"
              />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="24">
          <el-col :span="8">
            <el-form-item label="入住人数" prop="guestCount">
              <el-input-number
                v-model="editForm.guestCount"
                :min="1"
                :max="10"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="加床数量" prop="extraBedCount">
              <el-input-number
                v-model="editForm.extraBedCount"
                :min="0"
                :max="3"
                style="width: 100%"
                @change="handleEditDateChange"
              />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="入住天数">
              <el-input :model-value="`${editStayDays}晚`" disabled />
            </el-form-item>
          </el-col>
        </el-row>
        <el-divider content-position="left">入住人信息</el-divider>
        <el-row :gutter="24">
          <el-col :span="12">
            <el-form-item label="入住人姓名" prop="guestNames">
              <el-input
                v-model="editForm.guestNames"
                placeholder="请输入入住人姓名"
                maxlength="50"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="联系电话" prop="guestPhone">
              <el-input
                v-model="editForm.guestPhone"
                placeholder="请输入联系电话"
                maxlength="11"
              />
            </el-form-item>
          </el-col>
        </el-row>
        <el-divider content-position="left">其他信息</el-divider>
        <el-row :gutter="24">
          <el-col :span="12">
            <el-form-item label="预计到店时间">
              <el-time-picker
                v-model="editForm.expectedArrivalTime"
                placeholder="请选择预计到店时间"
                style="width: 100%"
                value-format="HH:mm"
              />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="特殊要求">
          <el-input
            v-model="editForm.specialRequirements"
            type="textarea"
            :rows="3"
            placeholder="请输入特殊要求（如高楼层、无烟房等）"
            maxlength="300"
            show-word-limit
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="editSaving" @click="handleEdit">
          确认修改
        </el-button>
      </template>
    </el-dialog>

    <el-dialog
      v-model="changeRoomDialogVisible"
      title="更换房间"
      width="800px"
      destroy-on-close
    >
      <div v-loading="roomQueryLoading">
        <el-alert
          v-if="currentChangeRoomBooking"
          :title="`为预订单「${currentChangeRoomBooking.bookingNo}」更换房间`"
          type="warning"
          :closable="false"
          class="change-room-alert"
        >
          <template #default>
            <span>原房间：{{ currentChangeRoomBooking.roomNumber || '-' }} ({{ currentChangeRoomBooking.roomTypeName || '-' }})</span>
            <span style="margin-left: 20px;">原房价：¥{{ (currentChangeRoomBooking.roomPrice || 0).toFixed(2) }}/晚</span>
          </template>
        </el-alert>

        <el-row :gutter="16" style="margin-top: 16px;">
          <el-col :span="24">
            <el-button type="primary" @click="queryAvailableRoomsForChange">
              <el-icon><Refresh /></el-icon>刷新可用房间
            </el-button>
          </el-col>
        </el-row>

        <div v-if="availableRooms.length > 0" style="margin-top: 16px;">
          <el-table
            :data="availableRooms"
            border
            stripe
            @selection-change="handleRoomSelectionChange"
            style="width: 100%"
          >
            <el-table-column type="selection" width="50" align="center" />
            <el-table-column prop="roomNumber" label="房号" width="100" align="center" />
            <el-table-column prop="roomTypeName" label="房型" min-width="140" />
            <el-table-column prop="floorName" label="楼层" width="100" align="center" />
            <el-table-column prop="bedType" label="床型" width="100" align="center" />
            <el-table-column label="房价(元/晚)" width="120" align="right">
              <template #default="{ row }">
                ¥{{ (row.basePrice || row.price || 0).toFixed(2) }}
              </template>
            </el-table-column>
            <el-table-column label="单日差价" width="120" align="right">
              <template #default="{ row }">
                <span :class="row.priceDiff > 0 ? 'price-up' : row.priceDiff < 0 ? 'price-down' : ''">
                  {{ row.priceDiff > 0 ? '+' : '' }}{{ row.priceDiff.toFixed(2) }}
                </span>
              </template>
            </el-table-column>
            <el-table-column label="状态" width="100" align="center">
              <template #default="{ row }">
                <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">
                  {{ row.status === 1 ? '空闲' : '不可用' }}
                </el-tag>
              </template>
            </el-table-column>
          </el-table>

          <div v-if="selectedNewRoom" class="price-diff-info">
            <el-row :gutter="16">
              <el-col :span="6">
                <span class="info-label">原房价：</span>
                <span class="info-value">¥{{ (currentChangeRoomBooking?.roomPrice || 0).toFixed(2) }}/晚</span>
              </el-col>
              <el-col :span="2">
                <span class="info-arrow">→</span>
              </el-col>
              <el-col :span="6">
                <span class="info-label">新房价：</span>
                <span class="info-value">¥{{ (selectedNewRoom.basePrice || selectedNewRoom.price || 0).toFixed(2) }}/晚</span>
              </el-col>
              <el-col :span="10" class="total-diff-col">
                <span class="info-label">总价差：</span>
                <span class="info-value" :class="calculateTotalPriceDiff > 0 ? 'price-up' : calculateTotalPriceDiff < 0 ? 'price-down' : ''">
                  {{ calculateTotalPriceDiff > 0 ? '+' : '' }}¥{{ calculateTotalPriceDiff.toFixed(2) }}
                </span>
                <el-tag size="small" type="info" style="margin-left: 8px;">
                  {{ currentChangeRoomBooking?.days || 0 }}晚
                </el-tag>
              </el-col>
            </el-row>
          </div>
        </div>

        <el-empty v-else-if="!roomQueryLoading" description="暂无可用房间" :image-size="80" />
      </div>
      <template #footer>
        <el-button @click="changeRoomDialogVisible = false">取消</el-button>
        <el-button
          type="primary"
          :loading="changeRoomSaving"
          :disabled="!selectedNewRoom"
          @click="handleChangeRoom"
        >
          确认换房
        </el-button>
      </template>
    </el-dialog>

    <BookingDetailDialog
      v-model="detailDialogVisible"
      :booking-id="currentBookingId"
      :default-tab="detailDefaultTab"
      @closed="handleDetailClosed"
    />
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Plus, Search, Refresh, Download, Calendar, Clock, User, CircleClose, CircleCheck
} from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import api from '@/api'
import BookingDetailDialog from './BookingDetailDialog.vue'

const userStore = useUserStore()
const hasPermission = (p) => userStore.hasPermission(p)

const statusOptions = [
  { value: 1, label: '待确认' },
  { value: 2, label: '已确认' },
  { value: 3, label: '已支付' },
  { value: 4, label: '已入住' },
  { value: 5, label: '已完成' },
  { value: 6, label: '已取消' }
]

const sourceOptions = [
  { value: 'online', label: '线上预订' },
  { value: 'offline', label: '线下预订' },
  { value: 'phone', label: '电话预订' },
  { value: 'walkin', label: '散客' },
  { value: 'ota', label: 'OTA平台' },
  { value: 'member', label: '会员预订' }
]

const exportFieldOptions = [
  { value: 'bookingNo', label: '预订单号' },
  { value: 'customerName', label: '客户姓名' },
  { value: 'phone', label: '手机号' },
  { value: 'roomTypeName', label: '房型' },
  { value: 'roomNumber', label: '房号' },
  { value: 'checkinDate', label: '入住日期' },
  { value: 'checkoutDate', label: '退房日期' },
  { value: 'days', label: '天数' },
  { value: 'totalAmount', label: '应付金额' },
  { value: 'status', label: '状态' },
  { value: 'bookingTime', label: '预订时间' },
  { value: 'sourceName', label: '预订来源' }
]

const todayStats = reactive({
  newBookings: 0,
  pendingCheckin: 0,
  checkedIn: 0,
  cancelled: 0
})

const roomTypes = ref([])
const tableData = ref([])
const total = ref(0)
const tableLoading = ref(false)
const selectedRows = ref([])

const queryParams = reactive({
  keyword: '',
  roomTypeId: null,
  status: null,
  source: null,
  checkinDateRange: [],
  bookingTimeRange: [],
  sortBy: 'bookingTime_desc',
  pageNum: 1,
  pageSize: 20
})

const canBatchConfirm = computed(() => {
  return selectedRows.value.every(row => row.status === 1)
})

const statusTagType = (status) => {
  const map = { 1: 'warning', 2: 'primary', 3: 'success', 4: 'info', 5: 'success', 6: 'danger' }
  return map[status] || 'info'
}

const statusLabel = (status) => {
  const map = { 1: '待确认', 2: '已确认', 3: '已支付', 4: '已入住', 5: '已完成', 6: '已取消' }
  return map[status] || '未知'
}

const loadTodayStats = async () => {
  try {
    const res = await api.booking.stats.today()
    if (res.code === 200 && res.data) {
      todayStats.newBookings = res.data.newBookings || 0
      todayStats.pendingCheckin = res.data.pendingCheckin || 0
      todayStats.checkedIn = res.data.checkedIn || 0
      todayStats.cancelled = res.data.cancelled || 0
    }
  } catch {}
}

const loadRoomTypes = async () => {
  try {
    const res = await api.hotel.getRoomTypes()
    if (res.code === 200) {
      roomTypes.value = res.data || []
    }
  } catch {
    roomTypes.value = []
  }
}

const loadBookings = async () => {
  tableLoading.value = true
  try {
    const params = { ...queryParams }
    if (params.checkinDateRange && params.checkinDateRange.length === 2) {
      params.checkinStartDate = params.checkinDateRange[0]
      params.checkinEndDate = params.checkinDateRange[1]
    }
    delete params.checkinDateRange
    if (params.bookingTimeRange && params.bookingTimeRange.length === 2) {
      params.bookingStartTime = params.bookingTimeRange[0]
      params.bookingEndTime = params.bookingTimeRange[1]
    }
    delete params.bookingTimeRange
    if (params.sortBy) {
      const [sortField, sortOrder] = params.sortBy.split('_')
      params.sortField = sortField
      params.sortOrder = sortOrder
    }
    delete params.sortBy
    const res = await api.booking.page(params)
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
  loadBookings()
}

const handleReset = () => {
  queryParams.keyword = ''
  queryParams.roomTypeId = null
  queryParams.status = null
  queryParams.source = null
  queryParams.checkinDateRange = []
  queryParams.bookingTimeRange = []
  queryParams.sortBy = 'bookingTime_desc'
  queryParams.pageNum = 1
  loadBookings()
}

const handleSelectionChange = (rows) => {
  selectedRows.value = rows
}

const clearSelection = () => {
  selectedRows.value = []
}

const handleAddBooking = () => {
  ElMessage.info('新增预订功能待开发')
}

const handleConfirm = async (row) => {
  try {
    await ElMessageBox.confirm(`确认预订「${row.bookingNo}」？`, '提示', { type: 'warning' })
    const res = await api.booking.confirm(row.id)
    if (res.code === 200) {
      ElMessage.success('确认成功')
      await loadBookings()
      await loadTodayStats()
    } else {
      ElMessage.error(res.message || '确认失败')
    }
  } catch {}
}

const handleBatchConfirm = async () => {
  const invalidRows = selectedRows.value.filter(row => row.status !== 1)
  if (invalidRows.length > 0) {
    ElMessage.warning('只能批量确认只能选择待确认状态的预订单')
    return
  }
  try {
    await ElMessageBox.confirm(`确认选中的 ${selectedRows.value.length} 条预订单？`, '提示', { type: 'warning' })
    const ids = selectedRows.value.map(r => r.id)
    const res = await api.booking.batchConfirm({ ids })
    if (res.code === 200) {
      ElMessage.success('批量确认成功')
      clearSelection()
      await loadBookings()
      await loadTodayStats()
    } else {
      ElMessage.error(res.message || '批量确认失败')
    }
  } catch {}
}

const editDialogVisible = ref(false)
const editSaving = ref(false)
const editFormRef = ref(null)
const currentEditBooking = ref(null)
const editForm = reactive({
  checkInDate: '',
  checkOutDate: '',
  guestCount: 1,
  guestNames: '',
  guestPhone: '',
  extraBedCount: 0,
  specialRequirements: '',
  expectedArrivalTime: ''
})

const editRules = {
  checkInDate: [{ required: true, message: '请选择入住日期', trigger: 'change' }],
  checkOutDate: [{ required: true, message: '请选择退房日期', trigger: 'change' }],
  guestCount: [{ required: true, message: '请输入入住人数', trigger: 'change' }],
  guestNames: [{ required: true, message: '请输入入住人姓名', trigger: 'blur' }],
  guestPhone: [
    { required: true, message: '请输入联系电话', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ]
}

const editStayDays = computed(() => {
  if (!editForm.checkInDate || !editForm.checkOutDate) return 0
  const start = new Date(editForm.checkInDate)
  const end = new Date(editForm.checkOutDate)
  return Math.max(0, Math.ceil((end - start) / (1000 * 60 * 60 * 24)))
})

const openEditDialog = async (row) => {
  currentEditBooking.value = { ...row }
  try {
    const res = await api.booking.get(row.id)
    if (res.code === 200 && res.data) {
      const booking = res.data.booking || res.data
      editForm.checkInDate = booking.checkInDate || booking.checkinDate || ''
      editForm.checkOutDate = booking.checkOutDate || booking.checkoutDate || ''
      editForm.guestCount = booking.guestCount || 1
      editForm.guestNames = booking.guestNames || ''
      editForm.guestPhone = booking.guestPhone || ''
      editForm.extraBedCount = booking.extraBedCount || 0
      editForm.specialRequirements = booking.specialRequirements || booking.specialRequest || ''
      editForm.expectedArrivalTime = booking.expectedArrivalTime || ''
    } else {
      editForm.checkInDate = row.checkInDate || row.checkinDate || ''
      editForm.checkOutDate = row.checkOutDate || row.checkoutDate || ''
      editForm.guestCount = row.guestCount || 1
      editForm.guestNames = row.customerName || ''
      editForm.guestPhone = row.phone || ''
      editForm.extraBedCount = row.extraBedCount || 0
      editForm.specialRequirements = ''
      editForm.expectedArrivalTime = ''
    }
  } catch {
    editForm.checkInDate = row.checkInDate || row.checkinDate || ''
    editForm.checkOutDate = row.checkOutDate || row.checkoutDate || ''
    editForm.guestCount = row.guestCount || 1
    editForm.guestNames = row.customerName || ''
    editForm.guestPhone = row.phone || ''
    editForm.extraBedCount = row.extraBedCount || 0
    editForm.specialRequirements = ''
    editForm.expectedArrivalTime = ''
  }
  editDialogVisible.value = true
}

const handleEditDateChange = async () => {
  if (editForm.checkInDate && editForm.checkOutDate && editStayDays.value > 0 && currentEditBooking.value) {
    try {
      const res = await api.booking.calculatePrice({
        roomTypeId: currentEditBooking.value.roomTypeId,
        checkinDate: editForm.checkInDate,
        checkoutDate: editForm.checkOutDate,
        extraBedCount: editForm.extraBedCount
      })
      if (res.code === 200 && res.data) {
        ElMessage.success(`价格已重新计算，总价：¥${res.data.totalPrice || 0}`)
      }
    } catch {}
  }
}

const handleEdit = async () => {
  if (!currentEditBooking.value) return
  const valid = await editFormRef.value.validate().catch(() => false)
  if (!valid) return

  if (editStayDays.value <= 0) {
    ElMessage.warning('退房日期必须晚于入住日期')
    return
  }

  const changedFields = {}
  const booking = currentEditBooking.value

  if (editForm.checkInDate !== (booking.checkInDate || booking.checkinDate)) {
    changedFields.checkInDate = editForm.checkInDate
  }
  if (editForm.checkOutDate !== (booking.checkOutDate || booking.checkoutDate)) {
    changedFields.checkOutDate = editForm.checkOutDate
  }
  if (editForm.guestCount !== booking.guestCount) {
    changedFields.guestCount = editForm.guestCount
  }
  if (editForm.guestNames !== booking.guestNames) {
    changedFields.guestNames = editForm.guestNames
  }
  if (editForm.guestPhone !== booking.guestPhone) {
    changedFields.guestPhone = editForm.guestPhone
  }
  if (editForm.extraBedCount !== booking.extraBedCount) {
    changedFields.extraBedCount = editForm.extraBedCount
  }
  if (editForm.specialRequirements !== (booking.specialRequirements || booking.specialRequest)) {
    changedFields.specialRequirements = editForm.specialRequirements
  }
  if (editForm.expectedArrivalTime !== booking.expectedArrivalTime) {
    changedFields.expectedArrivalTime = editForm.expectedArrivalTime
  }

  if (Object.keys(changedFields).length === 0) {
    ElMessage.info('没有检测到修改内容')
    return
  }

  editSaving.value = true
  try {
    const res = await api.booking.update(currentEditBooking.value.id, {
      changedFields
    })
    if (res.code === 200) {
      ElMessage.success('修改成功')
      editDialogVisible.value = false
      await loadBookings()
    } else {
      ElMessage.error(res.message || '修改失败')
    }
  } catch {
    ElMessage.error('修改失败，请重试')
  } finally {
    editSaving.value = false
  }
}

const cancelDialogVisible = ref(false)
const cancelSaving = ref(false)
const cancelFormRef = ref(null)
const cancelForm = reactive({
  id: null,
  bookingNo: '',
  checkInDate: '',
  totalAmount: 0,
  reason: '',
  remark: '',
  chargePenalty: false,
  penaltyAmount: 0
})
const cancelRules = {
  reason: [{ required: true, message: '请选择取消原因', trigger: 'change' }]
}

const cancelRulesConfig = [
  { condition: '提前3天及以上', penalty: '全额退款', description: '提前3天以上取消预订，不收取违约金' },
  { condition: '提前1-2天', penalty: '扣首晚房费', description: '提前1-2天取消预订，收取首晚房费作为违约金' },
  { condition: '当日取消', penalty: '不予退款', description: '入住当日取消预订，不予退款' }
]

const cancelPenaltyInfo = computed(() => {
  if (!cancelForm.checkInDate) return null
  const today = new Date()
  today.setHours(0, 0, 0, 0)
  const checkIn = new Date(cancelForm.checkInDate)
  checkIn.setHours(0, 0, 0, 0)
  const diffDays = Math.ceil((checkIn - today) / (1000 * 60 * 60 * 24))
  
  if (diffDays >= 3) {
    return { rule: cancelRulesConfig[0], penalty: 0 }
  } else if (diffDays >= 1 && diffDays <= 2) {
    const firstNightPrice = cancelForm.totalAmount / (currentCancelBooking?.days || 1)
    return { rule: cancelRulesConfig[1], penalty: Math.round(firstNightPrice * 100) / 100 }
  } else {
    return { rule: cancelRulesConfig[2], penalty: cancelForm.totalAmount }
  }
})

const currentCancelBooking = ref(null)

const handleCancel = (row) => {
  currentCancelBooking.value = { ...row }
  cancelForm.id = row.id
  cancelForm.bookingNo = row.bookingNo
  cancelForm.checkInDate = row.checkInDate || row.checkinDate || ''
  cancelForm.totalAmount = row.totalAmount || 0
  cancelForm.reason = ''
  cancelForm.remark = ''
  cancelForm.chargePenalty = cancelPenaltyInfo.value?.penalty > 0
  cancelForm.penaltyAmount = cancelPenaltyInfo.value?.penalty || 0
  cancelDialogVisible.value = true
}

const handleConfirmCancel = async () => {
  const valid = await cancelFormRef.value.validate().catch(() => false)
  if (!valid) return
  cancelSaving.value = true
  try {
    const res = await api.booking.cancel(cancelForm.id, {
      reason: cancelForm.reason,
      cancelDetail: cancelForm.remark,
      chargePenalty: cancelForm.chargePenalty,
      penaltyAmount: cancelForm.chargePenalty ? cancelForm.penaltyAmount : 0
    })
    if (res.code === 200) {
      ElMessage.success('取消成功')
      cancelDialogVisible.value = false
      await loadBookings()
      await loadTodayStats()
    } else {
      ElMessage.error(res.message || '取消失败')
    }
  } catch {
    ElMessage.error('取消失败')
  } finally {
    cancelSaving.value = false
  }
}

const checkinDialogVisible = ref(false)
const checkinSaving = ref(false)
const checkinFormRef = ref(null)
const currentCheckinBooking = ref(null)
const checkinForm = reactive({
  id: null,
  bookingNo: '',
  checkInTime: ''
})

const checkinRules = {
  checkInTime: [{ required: true, message: '请选择入住时间', trigger: 'change' }]
}

const openCheckinDialog = (row) => {
  currentCheckinBooking.value = { ...row }
  checkinForm.id = row.id
  checkinForm.bookingNo = row.bookingNo
  const now = new Date()
  checkinForm.checkInTime = new Date(now.getTime() - now.getTimezoneOffset() * 60000).toISOString().slice(0, 16)
  checkinDialogVisible.value = true
}

const handleCheckin = async () => {
  const valid = await checkinFormRef.value.validate().catch(() => false)
  if (!valid) return
  checkinSaving.value = true
  try {
    const res = await api.booking.checkin(checkinForm.id, {
      checkInTime: checkinForm.checkInTime
    })
    if (res.code === 200) {
      ElMessage.success('办理入住成功')
      checkinDialogVisible.value = false
      await loadBookings()
      await loadTodayStats()
    } else {
      ElMessage.error(res.message || '办理入住失败')
    }
  } catch {
    ElMessage.error('办理入住失败，请重试')
  } finally {
    checkinSaving.value = false
  }
}

const refundDialogVisible = ref(false)
const refundSaving = ref(false)
const refundFormRef = ref(null)
const currentRefundBooking = ref(null)
const refundForm = reactive({
  bookingId: null,
  bookingNo: '',
  paidAmount: 0,
  cancelPenalty: 0,
  refundReason: '',
  refundAmount: 0,
  refundRemark: ''
})

const refundRules = {
  refundReason: [{ required: true, message: '请选择退款原因', trigger: 'change' }],
  refundAmount: [{ required: true, message: '请输入退款金额', trigger: 'blur' }],
  refundRemark: [{ required: true, message: '请输入退款说明', trigger: 'blur' }]
}

const refundReasons = [
  { value: '1', label: '客户取消' },
  { value: '2', label: '重复支付' },
  { value: '3', label: '金额错误' },
  { value: '4', label: '其他原因' }
]

const refundStatusLabel = (status) => {
  const map = { 1: '待审批', 2: '已审批', 3: '已退款', 4: '已拒绝' }
  return map[status] || '未知'
}

const openRefundDialog = async (row) => {
  currentRefundBooking.value = { ...row }
  refundForm.bookingId = row.id
  refundForm.bookingNo = row.bookingNo
  refundForm.paidAmount = row.paidAmount || 0
  refundForm.cancelPenalty = row.cancelPenalty || 0
  refundForm.refundAmount = Math.max(0, (row.paidAmount || 0) - (row.cancelPenalty || 0))
  refundForm.refundReason = ''
  refundForm.refundRemark = ''
  refundDialogVisible.value = true
}

const handleRefund = async () => {
  const valid = await refundFormRef.value.validate().catch(() => false)
  if (!valid) return
  if (refundForm.refundAmount <= 0) {
    ElMessage.warning('退款金额必须大于0')
    return
  }
  if (refundForm.refundAmount > refundForm.paidAmount) {
    ElMessage.warning('退款金额不能大于已付金额')
    return
  }
  refundSaving.value = true
  try {
    const res = await api.booking.applyRefund(refundForm.bookingId, {
      refundReason: refundForm.refundReason,
      refundAmount: refundForm.refundAmount,
      refundRemark: refundForm.refundRemark
    })
    if (res.code === 200) {
      ElMessage.success('退款申请提交成功，等待审批')
      refundDialogVisible.value = false
      await loadBookings()
    } else {
      ElMessage.error(res.message || '提交失败')
    }
  } catch {
    ElMessage.error('提交失败，请重试')
  } finally {
    refundSaving.value = false
  }
}

const handleViewCheckin = (row) => {
  openDetailDialog(row, 'statusLogs')
}

const handleViewCancel = (row) => {
  openDetailDialog(row, 'changeLogs')
}

const exportDialogVisible = ref(false)
const exportSaving = ref(false)
const exportForm = reactive({
  scope: 'current',
  exportFields: ['bookingNo', 'customerName', 'phone', 'roomTypeName', 'checkinDate', 'checkoutDate', 'totalAmount', 'status']
})

const detailDialogVisible = ref(false)
const currentBookingId = ref(null)
const detailDefaultTab = ref('basic')

const openDetailDialog = (row, tab = 'basic') => {
  currentBookingId.value = row.id
  detailDefaultTab.value = tab
  detailDialogVisible.value = true
}

const handleDetailClosed = () => {
  currentBookingId.value = null
}

const changeRoomDialogVisible = ref(false)
const changeRoomSaving = ref(false)
const currentChangeRoomBooking = ref(null)
const availableRooms = ref([])
const selectedNewRoom = ref(null)
const roomQueryLoading = ref(false)

const openChangeRoomDialog = (row) => {
  currentChangeRoomBooking.value = { ...row }
  availableRooms.value = []
  selectedNewRoom.value = null
  changeRoomDialogVisible.value = true
  queryAvailableRoomsForChange()
}

const queryAvailableRoomsForChange = async () => {
  if (!currentChangeRoomBooking.value) return
  roomQueryLoading.value = true
  try {
    const booking = currentChangeRoomBooking.value
    const res = await api.booking.roomQuery({
      checkinDate: booking.checkInDate || booking.checkinDate,
      checkoutDate: booking.checkOutDate || booking.checkoutDate,
      roomTypeId: booking.roomTypeId
    })
    if (res.code === 200) {
      const rooms = res.data || []
      availableRooms.value = rooms
        .filter(r => r.id !== booking.roomId)
        .map(r => ({
          ...r,
          priceDiff: (r.basePrice || r.price || 0) - (booking.roomPrice || 0)
        }))
    } else {
      ElMessage.error(res.message || '查询可用房间失败')
    }
  } catch {
    ElMessage.error('查询可用房间失败')
  } finally {
    roomQueryLoading.value = false
  }
}

const handleRoomSelectionChange = (selection) => {
  selectedNewRoom.value = selection[0] || null
}

const calculateTotalPriceDiff = computed(() => {
  if (!selectedNewRoom.value || !currentChangeRoomBooking.value) return 0
  const days = currentChangeRoomBooking.value.days || 1
  return selectedNewRoom.value.priceDiff * days
})

const handleChangeRoom = async () => {
  if (!selectedNewRoom.value || !currentChangeRoomBooking.value) {
    ElMessage.warning('请选择要更换的房间')
    return
  }
  changeRoomSaving.value = true
  try {
    const res = await api.booking.changeRoom(currentChangeRoomBooking.value.id, {
      newRoomId: selectedNewRoom.value.id
    })
    if (res.code === 200) {
      const data = res.data || {}
      let msg = '换房成功'
      if (data.priceDiff !== undefined && data.priceDiff !== 0) {
        const diff = Number(data.priceDiff)
        if (diff > 0) {
          msg += `，需补差价：¥${diff.toFixed(2)}`
        } else {
          msg += `，退还差价：¥${Math.abs(diff).toFixed(2)}`
        }
      }
      ElMessage.success(msg)
      changeRoomDialogVisible.value = false
      await loadBookings()
    } else {
      ElMessage.error(res.message || '换房失败')
    }
  } catch {
    ElMessage.error('换房失败，请重试')
  } finally {
    changeRoomSaving.value = false
  }
}

const openExportDialog = () => {
  exportForm.scope = 'current'
  exportDialogVisible.value = true
}

const handleExport = async () => {
  if (exportForm.scope === 'selected' && (!selectedRows.value || selectedRows.value.length === 0)) {
    ElMessage.warning('请先选择要导出的预订单')
    return
  }
  exportSaving.value = true
  try {
    const params = {
      ...queryParams,
      scope: exportForm.scope,
      exportFields: exportForm.exportFields
    }
    if (exportForm.scope === 'current') {
      params.pageNum = queryParams.pageNum
      params.pageSize = queryParams.pageSize
    } else if (exportForm.scope === 'selected') {
      params.ids = selectedRows.value.map(r => r.id)
    }
    const res = await api.booking.export(params)
    const blob = new Blob([res], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' })
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = `预订单列表_${new Date().toISOString().slice(0, 10)}.xlsx`
    link.click()
    window.URL.revokeObjectURL(url)
    ElMessage.success('导出成功')
    exportDialogVisible.value = false
  } catch {
    ElMessage.error('导出失败')
  } finally {
    exportSaving.value = false
  }
}

onMounted(() => {
  loadTodayStats()
  loadRoomTypes()
  loadBookings()
})
</script>

<style scoped>
.booking-list-container {
  padding: 16px;
}

.stats-row {
  margin-bottom: 16px;
}

.stat-card {
  border-radius: 8px;
  overflow: hidden;
}

.stat-card :deep(.el-card__body) {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 20px;
}

.stat-content {
  flex: 1;
}

.stat-value {
  font-size: 28px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 4px;
}

.stat-label {
  font-size: 14px;
  color: #909399;
}

.stat-icon {
  opacity: 0.8;
}

.stat-new .stat-value,
.stat-new .stat-icon {
  color: #409eff;
}

.stat-pending .stat-value,
.stat-pending .stat-icon {
  color: #e6a23c;
}

.stat-checkedin .stat-value,
.stat-checkedin .stat-icon {
  color: #67c23a;
}

.stat-cancelled .stat-value,
.stat-cancelled .stat-icon {
  color: #f56c6c;
}

.filter-card {
  margin-bottom: 16px;
}

.filter-row {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  align-items: center;
}

.table-card {
  margin-bottom: 16px;
}

.table-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.table-toolbar-left {
  display: flex;
  gap: 8px;
}

.batch-toolbar {
  display: flex;
  align-items: center;
  gap: 8px;
}

.batch-count-tag {
  margin-right: 8px;
}

.pagination-wrap {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}

.edit-alert {
  margin-bottom: 16px;
}

.change-room-alert {
  margin-bottom: 16px;
}

.price-diff-info {
  margin-top: 16px;
  padding: 16px;
  background: #f8f9fa;
  border-radius: 8px;
}

.info-label {
  color: #909399;
  margin-right: 4px;
}

.info-value {
  font-weight: 600;
  color: #303133;
}

.info-arrow {
  color: #909399;
  font-size: 18px;
}

.total-diff-col {
  text-align: right;
}

.price-up {
  color: #f56c6c;
  font-weight: 600;
}

.price-down {
  color: #67c23a;
  font-weight: 600;
}

.cancel-rules-alert {
  margin-bottom: 16px;
}

.cancel-rules-list {
  margin-top: 8px;
}

.cancel-rule-item {
  margin-bottom: 4px;
  font-size: 13px;
}

.rule-condition {
  color: #606266;
}

.rule-penalty {
  color: #409eff;
  font-weight: 500;
}

.form-value {
  color: #303133;
  font-weight: 500;
}
</style>
