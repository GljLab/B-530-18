<template>
  <div class="customer-detail-container" v-loading="pageLoading">
    <div class="detail-header">
      <div class="header-left">
        <el-button @click="router.push('/customer')">
          <el-icon><ArrowLeft /></el-icon>返回
        </el-button>
        <el-avatar :size="64" :src="customer?.avatar" class="customer-avatar">
          <el-icon :size="32"><User /></el-icon>
        </el-avatar>
        <div class="customer-summary">
          <div class="customer-name-row">
            <span class="customer-name">{{ customer?.name || '' }}</span>
            <el-tag :type="customerTypeTagType(customer?.customerType)" size="default">
              {{ customerTypeLabel(customer?.customerType) }}
            </el-tag>
            <el-icon v-if="customer?.importance === 3" class="crown-icon" :size="22" color="#e6a23c">
              <GoldMedal />
            </el-icon>
            <el-tag :type="statusTagType(customer?.status)" size="default">
              {{ statusLabel(customer?.status) }}
            </el-tag>
          </div>
        </div>
      </div>
      <div class="header-actions">
        <el-button v-if="hasPermission('customer:edit')" type="primary" @click="handleEdit">
          <el-icon><Edit /></el-icon>编辑
        </el-button>
        <el-button
          v-if="customer?.status === 1 && hasPermission('customer:freeze')"
          type="warning"
          @click="openFreezeDialog"
        >
          <el-icon><Lock /></el-icon>冻结
        </el-button>
        <el-button
          v-if="customer?.status === 2 && hasPermission('customer:unfreeze')"
          type="success"
          @click="openUnfreezeDialog"
        >
          <el-icon><Unlock /></el-icon>解冻
        </el-button>
        <el-button
          v-if="hasPermission('customer:blacklist:submit') && customer?.status !== 3"
          type="danger"
          @click="openBlacklistDialog"
        >
          <el-icon><Warning /></el-icon>加入黑名单
        </el-button>
        <el-button v-if="hasPermission('customer:export')" @click="openExportDialog">
          <el-icon><Download /></el-icon>导出
        </el-button>
        <el-button v-if="hasPermission('customer:delete')" type="danger" @click="handleDelete">
          <el-icon><Delete /></el-icon>删除
        </el-button>
      </div>
    </div>

    <el-alert
      v-if="customer?.status === 3"
      title="该客户已被加入黑名单"
      type="error"
      :closable="false"
      show-icon
      class="blacklist-alert"
    >
      <template v-if="blacklistInfo">
        <div>黑名单类型：{{ blacklistInfo.blacklistType === 1 ? '临时' : '永久' }}</div>
        <div v-if="blacklistInfo.blacklistType === 1 && blacklistInfo.expireTime">
          到期时间：{{ blacklistInfo.expireTime }}
        </div>
        <div v-if="blacklistInfo.reason">拉黑原因：{{ blacklistReasonLabel(blacklistInfo.reason) }}</div>
        <div v-if="blacklistInfo.detailDescription">详细说明：{{ blacklistInfo.detailDescription }}</div>
      </template>
    </el-alert>

    <el-row :gutter="16" class="stats-row">
      <el-col :span="6">
        <el-card shadow="never" class="stat-card">
          <div class="stat-value">{{ customer?.totalOrders ?? 0 }}</div>
          <div class="stat-label">总订单数</div>
        </el-card>
      </el-col>
      <el-col v-if="hasPermission('customer:finance:view')" :span="6">
        <el-card shadow="never" class="stat-card">
          <div class="stat-value">¥{{ customer?.totalSpent ?? 0 }}</div>
          <div class="stat-label">总消费额</div>
        </el-card>
      </el-col>
      <el-col v-if="hasPermission('customer:finance:view')" :span="6">
        <el-card shadow="never" class="stat-card">
          <div class="stat-value">¥{{ customer?.avgSpent ?? 0 }}</div>
          <div class="stat-label">平均消费</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="never" class="stat-card">
          <div class="stat-value">{{ customer?.lifecycleDays ?? 0 }}天</div>
          <div class="stat-label">客户生命周期</div>
        </el-card>
      </el-col>
    </el-row>

    <el-card shadow="never" class="tabs-card">
      <el-tabs v-model="activeTab">
        <el-tab-pane label="基本信息" name="basic">
          <div v-if="customer" class="info-sections">
            <div class="info-section">
              <h4 class="section-title">个人信息</h4>
              <el-descriptions :column="2" border>
                <el-descriptions-item label="姓名">{{ customer.name }}</el-descriptions-item>
                <el-descriptions-item label="性别">{{ genderLabel(customer.gender) }}</el-descriptions-item>
                <el-descriptions-item label="客户类型">{{ customerTypeLabel(customer.customerType) }}</el-descriptions-item>
                <el-descriptions-item label="客户来源">{{ customerSourceLabel(customer.customerSource) }}</el-descriptions-item>
                <el-descriptions-item label="重要程度">{{ importanceLabel(customer.importance) }}</el-descriptions-item>
                <el-descriptions-item label="状态">{{ statusLabel(customer.status) }}</el-descriptions-item>
                <el-descriptions-item label="生日">{{ customer.birthday || '-' }}</el-descriptions-item>
                <el-descriptions-item label="备注" :span="2">{{ customer.remark || '-' }}</el-descriptions-item>
              </el-descriptions>
            </div>

            <div class="info-section">
              <h4 class="section-title">
                标签
                <el-button
                  v-if="hasPermission('customer:tag:assign')"
                  type="primary"
                  link
                  size="small"
                  @click="openTagDialog"
                >
                  添加标签
                </el-button>
              </h4>
              <div class="tag-list">
                <el-tag
                  v-for="tag in customerTags"
                  :key="tag.id"
                  closable
                  :closable="hasPermission('customer:tag:assign')"
                  @close="handleRemoveTag(tag.id)"
                  class="customer-tag"
                >
                  {{ tag.tagName }}
                </el-tag>
                <span v-if="!customerTags.length" class="empty-text">暂无标签</span>
              </div>
            </div>

            <div class="info-section">
              <h4 class="section-title">证件信息</h4>
              <el-descriptions :column="2" border>
                <el-descriptions-item label="证件类型">{{ idTypeLabel(customer.idType) }}</el-descriptions-item>
                <el-descriptions-item label="证件号码">{{ customer.idNumber || '-' }}</el-descriptions-item>
              </el-descriptions>
            </div>

            <div class="info-section">
              <h4 class="section-title">联系信息</h4>
              <el-descriptions :column="2" border>
                <el-descriptions-item label="手机号">{{ customer.phone || '-' }}</el-descriptions-item>
                <el-descriptions-item label="邮箱">{{ customer.email || '-' }}</el-descriptions-item>
                <el-descriptions-item label="微信号">{{ customer.wechat || '-' }}</el-descriptions-item>
                <el-descriptions-item label="QQ号">{{ customer.qq || '-' }}</el-descriptions-item>
              </el-descriptions>
            </div>

            <div class="info-section">
              <h4 class="section-title">地址信息</h4>
              <div v-if="addresses.length" class="address-list">
                <div v-for="addr in addresses" :key="addr.id" class="address-card">
                  <div class="address-card-body">
                    <div class="address-header">
                      <el-tag :type="addressTypeTagType(addr.addressType)" size="small">
                        {{ addressTypeLabel(addr.addressType) }}
                      </el-tag>
                      <el-tag v-if="addr.isDefault" type="success" size="small" class="default-tag">默认</el-tag>
                    </div>
                    <div class="address-detail">{{ addr.fullAddress || addr.address }}</div>
                    <div v-if="addr.postalCode" class="address-postal">邮编：{{ addr.postalCode }}</div>
                  </div>
                  <div class="address-card-actions">
                    <el-button type="primary" link size="small" @click="handleEditAddress(addr)">
                      <el-icon><Edit /></el-icon>编辑
                    </el-button>
                    <el-button type="danger" link size="small" @click="handleDeleteAddress(addr.id)">
                      <el-icon><Delete /></el-icon>删除
                    </el-button>
                  </div>
                </div>
              </div>
              <el-empty v-else description="暂无地址信息" :image-size="60" />
            </div>
          </div>
        </el-tab-pane>

        <el-tab-pane label="偏好设置" name="preference">
          <div class="preference-header">
            <el-button
              v-if="hasPermission('customer:preference:manage')"
              type="primary"
              size="small"
              @click="openPreferenceDialog"
            >
              <el-icon><Edit /></el-icon>编辑
            </el-button>
          </div>
          <div class="info-sections">
            <div class="info-section">
              <h4 class="section-title">房间偏好</h4>
              <el-descriptions :column="2" border>
                <el-descriptions-item label="偏好房型">
                  <div class="preference-tag-list">
                    <el-tag v-for="rt in preferredRoomTypeList" :key="rt" size="small" class="pref-tag">{{ rt }}</el-tag>
                    <span v-if="!preferredRoomTypeList.length" class="empty-text">-</span>
                  </div>
                </el-descriptions-item>
                <el-descriptions-item label="楼层偏好">{{ floorLabel(preference?.preferredFloor) }}</el-descriptions-item>
                <el-descriptions-item label="朝向偏好">
                  <div class="preference-tag-list">
                    <el-tag v-for="o in preferredOrientationList" :key="o" size="small" class="pref-tag">{{ o }}</el-tag>
                    <span v-if="!preferredOrientationList.length" class="empty-text">-</span>
                  </div>
                </el-descriptions-item>
                <el-descriptions-item label="床型偏好">{{ bedTypeLabel(preference?.preferredBedType) }}</el-descriptions-item>
                <el-descriptions-item label="景观偏好">
                  <div class="preference-tag-list">
                    <el-tag v-for="v in preferredViewList" :key="v" size="small" class="pref-tag">{{ v }}</el-tag>
                    <span v-if="!preferredViewList.length" class="empty-text">-</span>
                  </div>
                </el-descriptions-item>
              </el-descriptions>
            </div>

            <div class="info-section">
              <h4 class="section-title">特殊需求</h4>
              <div class="checkbox-display">
                <el-checkbox
                  v-for="opt in specialNeedsOptions"
                  :key="opt.value"
                  :model-value="specialNeedsSet.has(opt.value)"
                  disabled
                >
                  {{ opt.label }}
                </el-checkbox>
              </div>
              <div v-if="specialNeedsSet.size === 0" class="empty-text">暂无特殊需求</div>
            </div>

            <div class="info-section">
              <h4 class="section-title">服务偏好</h4>
              <div class="checkbox-display">
                <el-checkbox
                  v-for="opt in servicePreferenceOptions"
                  :key="opt.value"
                  :model-value="servicePreferenceSet.has(opt.value)"
                  disabled
                >
                  {{ opt.label }}
                </el-checkbox>
              </div>
              <div v-if="servicePreferenceSet.size === 0" class="empty-text">暂无服务偏好</div>
            </div>

            <div class="info-section">
              <h4 class="section-title">饮食偏好</h4>
              <el-descriptions :column="2" border>
                <el-descriptions-item label="素食">
                  <el-tag :type="preference?.dietVegetarian ? 'success' : 'info'" size="small">
                    {{ preference?.dietVegetarian ? '是' : '否' }}
                  </el-tag>
                </el-descriptions-item>
                <el-descriptions-item label="清真">
                  <el-tag :type="preference?.dietHalal ? 'success' : 'info'" size="small">
                    {{ preference?.dietHalal ? '是' : '否' }}
                  </el-tag>
                </el-descriptions-item>
                <el-descriptions-item label="海鲜过敏">
                  <el-tag :type="preference?.dietSeafoodAllergy ? 'danger' : 'info'" size="small">
                    {{ preference?.dietSeafoodAllergy ? '是' : '否' }}
                  </el-tag>
                </el-descriptions-item>
                <el-descriptions-item label="不吃辣">
                  <el-tag :type="preference?.dietNoSpicy ? 'success' : 'info'" size="small">
                    {{ preference?.dietNoSpicy ? '是' : '否' }}
                  </el-tag>
                </el-descriptions-item>
                <el-descriptions-item label="其他过敏" :span="2">
                  {{ preference?.dietOtherAllergy || '-' }}
                </el-descriptions-item>
              </el-descriptions>
            </div>
          </div>
          <el-empty v-if="!preference" description="暂无偏好设置" :image-size="60" />
        </el-tab-pane>

        <el-tab-pane label="备注记录" name="notes">
          <div class="notes-header">
            <el-button
              v-if="hasPermission('customer:note:add')"
              type="primary"
              size="small"
              @click="openNoteDialog"
            >
              <el-icon><Plus /></el-icon>添加备注
            </el-button>
          </div>
          <el-timeline v-if="notes.length">
            <el-timeline-item
              v-for="note in sortedNotes"
              :key="note.id"
              :timestamp="note.createTime"
              placement="top"
            >
              <el-card shadow="never" class="note-card">
                <div class="note-card-header">
                  <div class="note-meta">
                    <span class="note-operator">{{ note.operatorName }}</span>
                    <el-tag
                      :type="note.importance === 3 ? 'danger' : note.importance === 2 ? 'warning' : 'info'"
                      size="small"
                    >
                      {{ note.importance === 3 ? '紧急' : note.importance === 2 ? '重要' : '普通' }}
                    </el-tag>
                    <el-tag v-if="note.isPinned" type="warning" size="small">置顶</el-tag>
                  </div>
                  <div class="note-actions">
                    <el-button
                      v-if="note.isPinned"
                      type="warning"
                      link
                      size="small"
                      @click="handleUnpinNote(note.id)"
                    >
                      取消置顶
                    </el-button>
                    <el-button
                      v-else
                      type="primary"
                      link
                      size="small"
                      @click="handlePinNote(note.id)"
                    >
                      置顶
                    </el-button>
                    <el-button
                      type="danger"
                      link
                      size="small"
                      @click="handleDeleteNote(note.id)"
                    >
                      删除
                    </el-button>
                  </div>
                </div>
                <div class="note-content">{{ note.content }}</div>
                <div v-if="getNoteAttachments(note).length" class="note-attachments">
                  <div
                    v-for="(att, idx) in getNoteAttachments(note)"
                    :key="idx"
                    class="attachment-item"
                  >
                    <el-icon class="att-icon"><Paperclip /></el-icon>
                    <span class="att-name">{{ att.name || att.fileName || '附件' + (idx + 1) }}</span>
                    <el-button
                      type="primary"
                      link
                      size="small"
                      @click="viewAttachment(att)"
                    >
                      查看
                    </el-button>
                  </div>
                </div>
              </el-card>
            </el-timeline-item>
          </el-timeline>
          <el-empty v-else description="暂无备注记录" :image-size="60" />
        </el-tab-pane>

        <el-tab-pane label="操作日志" name="logs">
          <el-timeline v-if="operationLogs.length">
            <el-timeline-item
              v-for="log in operationLogs"
              :key="log.id"
              :timestamp="log.createTime"
              placement="top"
            >
              <el-card shadow="never" class="log-card">
                <div class="log-content">
                  <el-tag size="small" class="log-type-tag">{{ operationTypeLabel(log.operationType) }}</el-tag>
                  <span class="log-operator">{{ log.operatorName }}</span>
                  <span v-if="log.remark" class="log-remark">{{ log.remark }}</span>
                </div>
              </el-card>
            </el-timeline-item>
          </el-timeline>
          <el-empty v-else description="暂无操作日志" :image-size="60" />
        </el-tab-pane>

        <el-tab-pane label="入住记录" name="stay">
          <el-empty description="暂无数据，客户尚未完成入住" :image-size="100" />
        </el-tab-pane>

        <el-tab-pane label="消费记录" name="payment">
          <el-empty description="暂无数据，客户尚未产生消费" :image-size="100" />
        </el-tab-pane>
      </el-tabs>
    </el-card>

    <el-dialog
      v-model="freezeDialogVisible"
      :title="freezeDialogType === 'freeze' ? '冻结客户' : '解冻客户'"
      width="480px"
      destroy-on-close
    >
      <el-alert
        v-if="freezeDialogType === 'freeze'"
        title="冻结后该客户将无法预订和入住"
        type="warning"
        :closable="false"
        show-icon
        class="freeze-warning"
      />
      <el-form ref="freezeFormRef" :model="freezeForm" :rules="freezeRules" label-width="80px">
        <el-form-item label="原因" prop="reason">
          <el-input
            v-model="freezeForm.reason"
            type="textarea"
            :rows="4"
            placeholder="请输入原因"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="freezeDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="freezeSaving" @click="handleFreezeSubmit">确认</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="tagDialogVisible" title="添加标签" width="480px" destroy-on-close>
      <el-select
        v-model="selectedTagIds"
        multiple
        filterable
        placeholder="请选择标签"
        style="width: 100%"
      >
        <el-option
          v-for="tag in allTags"
          :key="tag.id"
          :label="tag.tagName"
          :value="tag.id"
        />
      </el-select>
      <template #footer>
        <el-button @click="tagDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="tagSaving" @click="handleAddTags">确认</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="preferenceDialogVisible" title="编辑偏好设置" width="640px" destroy-on-close>
      <el-form ref="preferenceFormRef" :model="preferenceForm" label-width="100px">
        <h4 class="section-title">房间偏好</h4>
        <el-form-item label="偏好房型">
          <el-select
            v-model="preferenceForm.preferredRoomTypeList"
            multiple
            filterable
            placeholder="请选择偏好房型"
            style="width: 100%"
          >
            <el-option
              v-for="rt in roomTypeOptions"
              :key="rt.id"
              :label="rt.typeName"
              :value="rt.typeName"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="楼层偏好">
          <el-select v-model="preferenceForm.preferredFloor" placeholder="请选择" clearable>
            <el-option label="高楼层" value="high" />
            <el-option label="低楼层" value="low" />
            <el-option label="中间" value="mid" />
          </el-select>
        </el-form-item>
        <el-form-item label="朝向偏好">
          <el-select
            v-model="preferenceForm.preferredOrientationList"
            multiple
            placeholder="请选择朝向偏好"
            style="width: 100%"
          >
            <el-option
              v-for="o in orientationOptions"
              :key="o.value"
              :label="o.label"
              :value="o.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="床型偏好">
          <el-select v-model="preferenceForm.preferredBedType" placeholder="请选择" clearable>
            <el-option label="单人床" value="single" />
            <el-option label="大床" value="king" />
            <el-option label="双床" value="twin" />
          </el-select>
        </el-form-item>
        <el-form-item label="景观偏好">
          <el-select
            v-model="preferenceForm.preferredViewList"
            multiple
            placeholder="请选择景观偏好"
            style="width: 100%"
          >
            <el-option
              v-for="v in viewOptions"
              :key="v.value"
              :label="v.label"
              :value="v.value"
            />
          </el-select>
        </el-form-item>

        <h4 class="section-title">特殊需求</h4>
        <el-form-item label="">
          <el-checkbox-group v-model="preferenceForm.specialNeedsList">
            <el-checkbox
              v-for="opt in specialNeedsOptions"
              :key="opt.value"
              :value="opt.value"
            >
              {{ opt.label }}
            </el-checkbox>
          </el-checkbox-group>
        </el-form-item>

        <h4 class="section-title">服务偏好</h4>
        <el-form-item label="">
          <el-checkbox-group v-model="preferenceForm.servicePreferenceList">
            <el-checkbox
              v-for="opt in servicePreferenceOptions"
              :key="opt.value"
              :value="opt.value"
            >
              {{ opt.label }}
            </el-checkbox>
          </el-checkbox-group>
        </el-form-item>

        <h4 class="section-title">饮食偏好</h4>
        <el-form-item label="素食">
          <el-switch v-model="preferenceForm.dietVegetarian" :active-value="1" :inactive-value="0" />
        </el-form-item>
        <el-form-item label="清真">
          <el-switch v-model="preferenceForm.dietHalal" :active-value="1" :inactive-value="0" />
        </el-form-item>
        <el-form-item label="海鲜过敏">
          <el-switch v-model="preferenceForm.dietSeafoodAllergy" :active-value="1" :inactive-value="0" />
        </el-form-item>
        <el-form-item label="不吃辣">
          <el-switch v-model="preferenceForm.dietNoSpicy" :active-value="1" :inactive-value="0" />
        </el-form-item>
        <el-form-item label="其他过敏">
          <el-input v-model="preferenceForm.dietOtherAllergy" placeholder="请输入其他过敏信息" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="preferenceDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="preferenceSaving" @click="handleSavePreference">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="noteDialogVisible" title="添加备注" width="560px" destroy-on-close>
      <el-form ref="noteFormRef" :model="noteForm" :rules="noteRules" label-width="80px">
        <el-form-item label="内容" prop="content">
          <el-input
            v-model="noteForm.content"
            type="textarea"
            :rows="4"
            placeholder="请输入备注内容"
          />
        </el-form-item>
        <el-form-item label="重要程度" prop="importance">
          <el-select v-model="noteForm.importance" placeholder="请选择">
            <el-option label="普通" :value="1" />
            <el-option label="重要" :value="2" />
            <el-option label="紧急" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item label="附件">
          <el-upload
            v-model:file-list="noteForm.attachments"
            :action="uploadUrl"
            :headers="uploadHeaders"
            :auto-upload="true"
            :limit="3"
            :on-success="handleNoteUploadSuccess"
            :on-error="handleNoteUploadError"
            :on-remove="handleNoteRemoveFile"
            name="file"
          >
            <el-button size="small">选择文件</el-button>
          </el-upload>
        </el-form-item>
        <el-form-item label="@角色">
          <el-input v-model="noteForm.mentionRoles" placeholder="@角色（预留）" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="noteDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="noteSaving" @click="handleAddNote">确认</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="blacklistDialogVisible" title="加入黑名单" width="560px" destroy-on-close>
      <el-alert
        title="加入黑名单后该客户将无法预订和入住"
        type="warning"
        :closable="false"
        show-icon
        class="freeze-warning"
      />
      <el-form ref="blacklistFormRef" :model="blacklistForm" :rules="blacklistRules" label-width="100px">
        <el-form-item label="拉黑原因" prop="reason">
          <el-select v-model="blacklistForm.reason" placeholder="请选择原因">
            <el-option label="恶意破坏酒店设施" :value="1" />
            <el-option label="逃单/拖欠费用" :value="2" />
            <el-option label="骚扰其他客人或员工" :value="3" />
            <el-option label="违反法律法规" :value="4" />
            <el-option label="其他" :value="5" />
          </el-select>
        </el-form-item>
        <el-form-item label="详细说明" prop="detailDescription">
          <el-input
            v-model="blacklistForm.detailDescription"
            type="textarea"
            :rows="4"
            placeholder="请输入详细说明"
          />
        </el-form-item>
        <el-form-item label="证据材料">
          <el-upload
            v-model:file-list="blacklistForm.evidenceMaterials"
            :auto-upload="false"
            :limit="5"
          >
            <el-button size="small">选择文件</el-button>
          </el-upload>
        </el-form-item>
        <el-form-item label="黑名单类型" prop="blacklistType">
          <el-radio-group v-model="blacklistForm.blacklistType">
            <el-radio :value="1">临时</el-radio>
            <el-radio :value="2">永久</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item v-if="blacklistForm.blacklistType === 1" label="到期时间" prop="expireTime">
          <el-date-picker
            v-model="blacklistForm.expireTime"
            type="datetime"
            placeholder="请选择到期时间"
            style="width: 100%"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="blacklistDialogVisible = false">取消</el-button>
        <el-button type="danger" :loading="blacklistSaving" @click="handleBlacklistSubmit">确认提交</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="exportDialogVisible" title="导出客户数据" width="560px" destroy-on-close>
      <el-form label-width="100px">
        <el-form-item label="导出范围">
          <el-radio-group v-model="exportForm.range">
            <el-radio value="current">当前筛选</el-radio>
            <el-radio value="all">全部</el-radio>
            <el-radio value="custom">自定义</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="导出字段">
          <el-checkbox-group v-model="exportForm.fields">
            <el-checkbox value="basic">基础信息</el-checkbox>
            <el-checkbox value="idCard">证件</el-checkbox>
            <el-checkbox value="contact">联系方式</el-checkbox>
            <el-checkbox value="category">分类</el-checkbox>
            <el-checkbox value="statistics">统计</el-checkbox>
            <el-checkbox value="tags">标签</el-checkbox>
            <el-checkbox value="preference">偏好</el-checkbox>
          </el-checkbox-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="exportDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="exporting" @click="handleExport">导出</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ArrowLeft, User, Edit, Delete, Lock, Unlock, GoldMedal, Warning, Download, Plus, Paperclip } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import api from '@/api'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const id = route.params.id
const hasPermission = (p) => userStore.hasPermission(p)

const genderLabel = (v) => {
  const map = { 0: '未知', 1: '男', 2: '女' }
  return map[v] ?? '-'
}

const customerTypeLabel = (v) => {
  const map = { 1: '散客', 2: '企业客户', 3: '协议客户', 4: 'VIP客户' }
  return map[v] ?? '-'
}

const customerTypeTagType = (v) => {
  const map = { 1: 'info', 2: '', 3: 'warning', 4: 'danger' }
  return map[v] ?? 'info'
}

const customerSourceLabel = (v) => {
  const map = { 1: '官网', 2: 'OTA平台', 3: '企业协议', 4: '电话预订', 5: '前台登记', 6: '老客户推荐' }
  return map[v] ?? '-'
}

const importanceLabel = (v) => {
  const map = { 1: '普通', 2: '重要', 3: 'VIP' }
  return map[v] ?? '-'
}

const statusLabel = (v) => {
  const map = { 1: '正常', 2: '冻结', 3: '黑名单' }
  return map[v] ?? '-'
}

const statusTagType = (v) => {
  const map = { 1: 'success', 2: 'danger', 3: 'danger' }
  return map[v] ?? 'info'
}

const idTypeLabel = (v) => {
  const map = { 1: '身份证', 2: '护照', 3: '港澳通行证', 4: '台胞证', 5: '驾驶证' }
  return map[v] ?? '-'
}

const addressTypeLabel = (v) => {
  const map = { 1: '家庭地址', 2: '公司地址', 3: '发票邮寄地址' }
  return map[v] ?? '-'
}

const addressTypeTagType = (v) => {
  const map = { 1: '', 2: 'warning', 3: 'info' }
  return map[v] ?? 'info'
}

const floorLabel = (v) => {
  const map = { high: '高楼层', low: '低楼层', mid: '中间' }
  return map[v] ?? '-'
}

const bedTypeLabel = (v) => {
  const map = { single: '单人床', king: '大床', twin: '双床' }
  return map[v] ?? '-'
}

const operationTypeLabel = (v) => {
  const map = {
    1: '创建客户', 2: '修改信息', 3: '冻结', 4: '解冻', 5: '添加标签',
    6: '移除标签', 7: '修改偏好', 8: '添加备注', 9: '加入黑名单',
    10: '解除黑名单', 11: '客户合并', 12: '导入客户', 13: '导出客户'
  }
  return map[v] ?? '未知操作'
}

const blacklistReasonLabel = (v) => {
  const map = { 1: '恶意破坏酒店设施', 2: '逃单/拖欠费用', 3: '骚扰其他客人或员工', 4: '违反法律法规', 5: '其他' }
  return map[v] ?? '-'
}

const specialNeedsOptions = [
  { value: 'no_smoke', label: '无烟房' },
  { value: 'quiet', label: '安静房间' },
  { value: 'away_elevator', label: '远离电梯' },
  { value: 'near_elevator', label: '靠近电梯' },
  { value: 'extra_bed', label: '需要加床' },
  { value: 'baby_cot', label: '需要婴儿床' },
  { value: 'pollen_allergy', label: '花粉过敏' },
  { value: 'humidifier', label: '需要加湿器' },
  { value: 'extra_pillow', label: '多枕头' },
  { value: 'low_floor', label: '需要低楼层' }
]

const orientationOptions = [
  { value: '东向', label: '东向' },
  { value: '南向', label: '南向' },
  { value: '西向', label: '西向' },
  { value: '北向', label: '北向' },
  { value: '东南向', label: '东南向' },
  { value: '西南向', label: '西南向' },
  { value: '东北向', label: '东北向' },
  { value: '西北向', label: '西北向' }
]

const viewOptions = [
  { value: '海景', label: '海景' },
  { value: '山景', label: '山景' },
  { value: '城景', label: '城景' },
  { value: '园景', label: '园景' },
  { value: '湖景', label: '湖景' },
  { value: '街景', label: '街景' }
]

const servicePreferenceOptions = [
  { value: 'do_not_disturb', label: '免打扰' },
  { value: 'early_newspaper', label: '要早报' },
  { value: 'laundry', label: '洗衣服务' },
  { value: 'wake_up_call', label: '叫醒服务' },
  { value: 'early_checkin', label: '提前check-in' },
  { value: 'late_checkout', label: '延迟checkout' }
]

const activeTab = ref('basic')
const customer = ref(null)
const addresses = ref([])
const customerTags = ref([])
const allTags = ref([])
const preference = ref(null)
const notes = ref([])
const operationLogs = ref([])
const blacklistInfo = ref(null)
const pageLoading = ref(false)
const roomTypeOptions = ref([])

const freezeDialogVisible = ref(false)
const freezeDialogType = ref('freeze')
const freezeSaving = ref(false)
const freezeFormRef = ref(null)
const freezeForm = reactive({ reason: '' })
const freezeRules = {
  reason: [{ required: true, message: '请输入原因', trigger: 'blur' }]
}

const tagDialogVisible = ref(false)
const tagSaving = ref(false)
const selectedTagIds = ref([])

const preferenceDialogVisible = ref(false)
const preferenceSaving = ref(false)
const preferenceFormRef = ref(null)
const preferenceForm = reactive({
  preferredRoomTypeList: [],
  preferredFloor: '',
  preferredOrientationList: [],
  preferredBedType: '',
  preferredViewList: [],
  specialNeedsList: [],
  servicePreferenceList: [],
  dietVegetarian: 0,
  dietHalal: 0,
  dietSeafoodAllergy: 0,
  dietNoSpicy: 0,
  dietOtherAllergy: ''
})

const noteDialogVisible = ref(false)
const noteSaving = ref(false)
const noteFormRef = ref(null)
const noteForm = reactive({
  content: '',
  importance: 1,
  attachments: [],
  mentionRoles: ''
})
const noteUploadedUrls = ref([])
const noteRules = {
  content: [{ required: true, message: '请输入备注内容', trigger: 'blur' }],
  importance: [{ required: true, message: '请选择重要程度', trigger: 'change' }]
}

const blacklistDialogVisible = ref(false)
const blacklistSaving = ref(false)
const blacklistFormRef = ref(null)
const blacklistForm = reactive({
  reason: null,
  detailDescription: '',
  evidenceMaterials: [],
  blacklistType: 2,
  expireTime: null
})
const blacklistRules = {
  reason: [{ required: true, message: '请选择拉黑原因', trigger: 'change' }],
  detailDescription: [{ required: true, message: '请输入详细说明', trigger: 'blur' }],
  blacklistType: [{ required: true, message: '请选择黑名单类型', trigger: 'change' }]
}

const exportDialogVisible = ref(false)
const exporting = ref(false)
const exportForm = reactive({
  range: 'current',
  fields: ['basic', 'contact', 'category']
})

const specialNeedsSet = computed(() => {
  if (!preference.value?.specialNeeds) return new Set()
  return new Set(preference.value.specialNeeds.split(',').filter(Boolean))
})

const servicePreferenceSet = computed(() => {
  if (!preference.value?.servicePreference) return new Set()
  return new Set(preference.value.servicePreference.split(',').filter(Boolean))
})

const preferredRoomTypeList = computed(() => {
  if (!preference.value?.preferredRoomType) return []
  return preference.value.preferredRoomType.split(',').filter(Boolean)
})

const preferredOrientationList = computed(() => {
  if (!preference.value?.preferredOrientation) return []
  return preference.value.preferredOrientation.split(',').filter(Boolean)
})

const preferredViewList = computed(() => {
  if (!preference.value?.preferredView) return []
  return preference.value.preferredView.split(',').filter(Boolean)
})

const uploadUrl = '/api/file/upload'
const uploadHeaders = computed(() => ({
  Authorization: `Bearer ${localStorage.getItem('token') || ''}`
}))

const sortedNotes = computed(() => {
  return [...notes.value].sort((a, b) => {
    if (a.isPinned !== b.isPinned) return b.isPinned - a.isPinned
    return new Date(b.createTime) - new Date(a.createTime)
  })
})

const loadCustomer = async () => {
  pageLoading.value = true
  try {
    const res = await api.customer.getById(id)
    if (res.code === 200 && res.data) {
      customer.value = res.data
      if (res.data.status === 3) {
        loadBlacklistInfo()
      }
    } else {
      ElMessage.error(res.message || '获取客户详情失败')
    }
  } catch {
    ElMessage.error('获取客户详情失败')
  } finally {
    pageLoading.value = false
  }
}

const loadAddresses = async () => {
  try {
    const res = await api.customer.getAddresses(id)
    if (res.code === 200) {
      addresses.value = res.data || []
    }
  } catch {
    addresses.value = []
  }
}

const loadCustomerTags = async () => {
  try {
    const res = await api.customer.getCustomerTags(id)
    if (res.code === 200) {
      customerTags.value = res.data || []
    }
  } catch {
    customerTags.value = []
  }
}

const loadAllTags = async () => {
  try {
    const res = await api.customer.getTagList()
    if (res.code === 200) {
      allTags.value = res.data || []
    }
  } catch {
    allTags.value = []
  }
}

const loadRoomTypes = async () => {
  try {
    const res = await api.hotel.getRoomTypes()
    if (res.code === 200) {
      roomTypeOptions.value = res.data || []
    }
  } catch {
    roomTypeOptions.value = []
  }
}

const loadPreference = async () => {
  try {
    const res = await api.customer.getPreference(id)
    if (res.code === 200) {
      preference.value = res.data || null
    }
  } catch {
    preference.value = null
  }
}

const loadNotes = async () => {
  try {
    const res = await api.customer.getNotes(id)
    if (res.code === 200) {
      notes.value = res.data || []
    }
  } catch {
    notes.value = []
  }
}

const loadOperationLogs = async () => {
  try {
    const res = await api.customer.getOperationLogs(id)
    if (res.code === 200) {
      operationLogs.value = res.data || []
    }
  } catch {
    operationLogs.value = []
  }
}

const loadBlacklistInfo = async () => {
  try {
    const res = await api.customer.checkBlacklist(id)
    if (res.code === 200) {
      blacklistInfo.value = res.data || null
    }
  } catch {
    blacklistInfo.value = null
  }
}

const handleEdit = () => {
  router.push(`/customer/edit/${id}`)
}

const openFreezeDialog = () => {
  freezeDialogType.value = 'freeze'
  freezeForm.reason = ''
  freezeDialogVisible.value = true
}

const openUnfreezeDialog = () => {
  freezeDialogType.value = 'unfreeze'
  freezeForm.reason = ''
  freezeDialogVisible.value = true
}

const handleFreezeSubmit = async () => {
  const valid = await freezeFormRef.value.validate().catch(() => false)
  if (!valid) return

  freezeSaving.value = true
  try {
    const action = freezeDialogType.value === 'freeze' ? api.customer.freeze : api.customer.unfreeze
    const res = await action(id, { reason: freezeForm.reason })
    if (res.code === 200) {
      ElMessage.success(freezeDialogType.value === 'freeze' ? '冻结成功' : '解冻成功')
      freezeDialogVisible.value = false
      await loadCustomer()
    } else {
      ElMessage.error(res.message || '操作失败')
    }
  } catch {
    ElMessage.error('操作失败')
  } finally {
    freezeSaving.value = false
  }
}

const handleDelete = async () => {
  try {
    await ElMessageBox.confirm(`确认删除客户「${customer.value?.name}」？删除后不可恢复！`, '警告', {
      confirmButtonText: '确认删除',
      cancelButtonText: '取消',
      type: 'warning'
    })
    const res = await api.customer.delete(id)
    if (res.code === 200) {
      ElMessage.success('删除成功')
      router.push('/customer')
    } else {
      ElMessage.error(res.message || '删除失败')
    }
  } catch {}
}

const handleEditAddress = (addr) => {
  router.push({ path: `/customer/edit/${id}`, query: { editAddress: addr.id } })
}

const handleDeleteAddress = async (addrId) => {
  try {
    await ElMessageBox.confirm('确认删除该地址？', '警告', {
      confirmButtonText: '确认删除',
      cancelButtonText: '取消',
      type: 'warning'
    })
    const res = await api.customer.deleteAddress(addrId)
    if (res.code === 200) {
      ElMessage.success('删除成功')
      await loadAddresses()
    } else {
      ElMessage.error(res.message || '删除失败')
    }
  } catch {}
}

const openTagDialog = async () => {
  selectedTagIds.value = []
  await loadAllTags()
  tagDialogVisible.value = true
}

const handleAddTags = async () => {
  if (!selectedTagIds.value.length) {
    ElMessage.warning('请选择至少一个标签')
    return
  }
  tagSaving.value = true
  try {
    const res = await api.customer.addTagsToCustomer(id, selectedTagIds.value)
    if (res.code === 200) {
      ElMessage.success('添加标签成功')
      tagDialogVisible.value = false
      await loadCustomerTags()
    } else {
      ElMessage.error(res.message || '添加标签失败')
    }
  } catch {
    ElMessage.error('添加标签失败')
  } finally {
    tagSaving.value = false
  }
}

const handleRemoveTag = async (tagId) => {
  try {
    await ElMessageBox.confirm('确认移除该标签？', '提示', {
      confirmButtonText: '确认',
      cancelButtonText: '取消',
      type: 'warning'
    })
    const res = await api.customer.removeTagFromCustomer(id, tagId)
    if (res.code === 200) {
      ElMessage.success('移除标签成功')
      await loadCustomerTags()
    } else {
      ElMessage.error(res.message || '移除标签失败')
    }
  } catch {}
}

const openPreferenceDialog = async () => {
  await loadRoomTypes()
  const p = preference.value || {}
  preferenceForm.preferredRoomTypeList = p.preferredRoomType ? p.preferredRoomType.split(',').filter(Boolean) : []
  preferenceForm.preferredFloor = p.preferredFloor || ''
  preferenceForm.preferredOrientationList = p.preferredOrientation ? p.preferredOrientation.split(',').filter(Boolean) : []
  preferenceForm.preferredBedType = p.preferredBedType || ''
  preferenceForm.preferredViewList = p.preferredView ? p.preferredView.split(',').filter(Boolean) : []
  preferenceForm.specialNeedsList = p.specialNeeds ? p.specialNeeds.split(',').filter(Boolean) : []
  preferenceForm.servicePreferenceList = p.servicePreference ? p.servicePreference.split(',').filter(Boolean) : []
  preferenceForm.dietVegetarian = p.dietVegetarian || 0
  preferenceForm.dietHalal = p.dietHalal || 0
  preferenceForm.dietSeafoodAllergy = p.dietSeafoodAllergy || 0
  preferenceForm.dietNoSpicy = p.dietNoSpicy || 0
  preferenceForm.dietOtherAllergy = p.dietOtherAllergy || ''
  preferenceDialogVisible.value = true
}

const handleSavePreference = async () => {
  preferenceSaving.value = true
  try {
    const data = {
      customerId: Number(id),
      preferredRoomType: preferenceForm.preferredRoomTypeList.join(','),
      preferredFloor: preferenceForm.preferredFloor,
      preferredOrientation: preferenceForm.preferredOrientationList.join(','),
      preferredBedType: preferenceForm.preferredBedType,
      preferredView: preferenceForm.preferredViewList.join(','),
      specialNeeds: preferenceForm.specialNeedsList.join(','),
      servicePreference: preferenceForm.servicePreferenceList.join(','),
      dietVegetarian: preferenceForm.dietVegetarian,
      dietHalal: preferenceForm.dietHalal,
      dietSeafoodAllergy: preferenceForm.dietSeafoodAllergy,
      dietNoSpicy: preferenceForm.dietNoSpicy,
      dietOtherAllergy: preferenceForm.dietOtherAllergy
    }
    const res = await api.customer.savePreference(data)
    if (res.code === 200) {
      ElMessage.success('保存偏好成功')
      preferenceDialogVisible.value = false
      await loadPreference()
    } else {
      ElMessage.error(res.message || '保存偏好失败')
    }
  } catch {
    ElMessage.error('保存偏好失败')
  } finally {
    preferenceSaving.value = false
  }
}

const openNoteDialog = () => {
  noteForm.content = ''
  noteForm.importance = 1
  noteForm.attachments = []
  noteForm.mentionRoles = ''
  noteUploadedUrls.value = []
  noteDialogVisible.value = true
}

const getNoteAttachments = (note) => {
  if (!note || !note.attachments) return []
  try {
    const parsed = typeof note.attachments === 'string' 
      ? JSON.parse(note.attachments) 
      : note.attachments
    return Array.isArray(parsed) ? parsed : []
  } catch {
    return []
  }
}

const viewAttachment = (att) => {
  const url = att.url || att.fileUrl || att
  if (url) {
    window.open(url, '_blank')
  }
}

const handleNoteUploadSuccess = (response, uploadFile) => {
  if (response.code === 200 && response.data) {
    const url = typeof response.data === 'string' ? response.data : response.data.url
    const name = uploadFile.name
    noteUploadedUrls.value.push({ url, name })
    uploadFile.url = url
  } else {
    ElMessage.error(response.message || '上传失败')
  }
}

const handleNoteUploadError = () => {
  ElMessage.error('附件上传失败，请重试')
}

const handleNoteRemoveFile = (uploadFile) => {
  const url = uploadFile.url
  if (url) {
    const idx = noteUploadedUrls.value.findIndex(a => a.url === url)
    if (idx > -1) noteUploadedUrls.value.splice(idx, 1)
  }
}

const handleAddNote = async () => {
  const valid = await noteFormRef.value.validate().catch(() => false)
  if (!valid) return

  noteSaving.value = true
  try {
    const data = {
      customerId: Number(id),
      content: noteForm.content,
      importance: noteForm.importance,
      attachments: JSON.stringify(noteUploadedUrls.value),
      mentionRoles: noteForm.mentionRoles
    }
    const res = await api.customer.addNote(data)
    if (res.code === 200) {
      ElMessage.success('添加备注成功')
      noteDialogVisible.value = false
      await loadNotes()
    } else {
      ElMessage.error(res.message || '添加备注失败')
    }
  } catch {
    ElMessage.error('添加备注失败')
  } finally {
    noteSaving.value = false
  }
}

const handleDeleteNote = async (noteId) => {
  try {
    await ElMessageBox.confirm('确认删除该备注？', '警告', {
      confirmButtonText: '确认删除',
      cancelButtonText: '取消',
      type: 'warning'
    })
    const res = await api.customer.deleteNote(noteId)
    if (res.code === 200) {
      ElMessage.success('删除成功')
      await loadNotes()
    } else {
      ElMessage.error(res.message || '删除失败')
    }
  } catch {}
}

const handlePinNote = async (noteId) => {
  try {
    const res = await api.customer.pinNote(noteId)
    if (res.code === 200) {
      ElMessage.success('置顶成功')
      await loadNotes()
    } else {
      ElMessage.error(res.message || '置顶失败')
    }
  } catch {
    ElMessage.error('置顶失败')
  }
}

const handleUnpinNote = async (noteId) => {
  try {
    const res = await api.customer.unpinNote(noteId)
    if (res.code === 200) {
      ElMessage.success('取消置顶成功')
      await loadNotes()
    } else {
      ElMessage.error(res.message || '取消置顶失败')
    }
  } catch {
    ElMessage.error('取消置顶失败')
  }
}

const openBlacklistDialog = () => {
  blacklistForm.reason = null
  blacklistForm.detailDescription = ''
  blacklistForm.evidenceMaterials = []
  blacklistForm.blacklistType = 2
  blacklistForm.expireTime = null
  blacklistDialogVisible.value = true
}

const handleBlacklistSubmit = async () => {
  const valid = await blacklistFormRef.value.validate().catch(() => false)
  if (!valid) return

  if (blacklistForm.blacklistType === 1 && !blacklistForm.expireTime) {
    ElMessage.warning('临时黑名单请选择到期时间')
    return
  }

  blacklistSaving.value = true
  try {
    const data = {
      customerId: Number(id),
      reason: blacklistForm.reason,
      detailDescription: blacklistForm.detailDescription,
      blacklistType: blacklistForm.blacklistType,
      expireTime: blacklistForm.blacklistType === 1 ? blacklistForm.expireTime : null
    }
    const res = await api.customer.submitBlacklist(data)
    if (res.code === 200) {
      ElMessage.success('提交黑名单申请成功')
      blacklistDialogVisible.value = false
      await loadCustomer()
    } else {
      ElMessage.error(res.message || '提交失败')
    }
  } catch {
    ElMessage.error('提交失败')
  } finally {
    blacklistSaving.value = false
  }
}

const openExportDialog = () => {
  exportForm.range = 'current'
  exportForm.fields = ['basic', 'contact', 'category']
  exportDialogVisible.value = true
}

const handleExport = async () => {
  if (!exportForm.fields.length) {
    ElMessage.warning('请至少选择一个导出字段')
    return
  }
  exporting.value = true
  try {
    const data = {
      customerId: Number(id),
      range: exportForm.range,
      fields: exportForm.fields
    }
    const res = await api.customer.exportCustomers(data)
    const blob = new Blob([res], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' })
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = `客户_${customer.value?.name || id}_导出.xlsx`
    link.click()
    window.URL.revokeObjectURL(url)
    exportDialogVisible.value = false
    ElMessage.success('导出成功')
  } catch {
    ElMessage.error('导出失败')
  } finally {
    exporting.value = false
  }
}

onMounted(() => {
  loadCustomer()
  loadAddresses()
  loadCustomerTags()
  loadPreference()
  loadNotes()
  loadOperationLogs()
})
</script>

<style scoped>
.customer-detail-container {
  padding: 10px;
}

.detail-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  padding: 16px 20px;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.06);
}

.header-left {
  display: flex;
  align-items: center;
  gap: 16px;
}

.customer-avatar {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  flex-shrink: 0;
}

.customer-summary {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.customer-name-row {
  display: flex;
  align-items: center;
  gap: 10px;
}

.customer-name {
  font-size: 24px;
  font-weight: 700;
  color: #1a202c;
}

.crown-icon {
  flex-shrink: 0;
}

.header-actions {
  display: flex;
  gap: 10px;
}

.blacklist-alert {
  margin-bottom: 16px;
}

.blacklist-alert div {
  font-size: 13px;
  line-height: 1.8;
}

.stats-row {
  margin-bottom: 16px;
}

.stat-card {
  border-radius: 12px;
  border: none;
  text-align: center;
}

.stat-value {
  font-size: 22px;
  font-weight: 700;
  color: #2d3748;
  margin-bottom: 4px;
}

.stat-label {
  font-size: 13px;
  color: #718096;
}

.tabs-card {
  border-radius: 12px;
  border: none;
}

.info-sections {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.info-section {
  margin: 0;
}

.section-title {
  font-size: 16px;
  font-weight: 600;
  color: #1a202c;
  margin: 0 0 12px 0;
  padding-left: 10px;
  border-left: 3px solid #409eff;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.tag-list {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.customer-tag {
  border-radius: 6px;
}

.empty-text {
  font-size: 13px;
  color: #909399;
}

.address-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(340px, 1fr));
  gap: 12px;
}

.address-card {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  padding: 14px 16px;
  background: #f7f8fa;
  border-radius: 10px;
  border: 1px solid #ebeef5;
}

.address-card-body {
  flex: 1;
}

.address-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
}

.default-tag {
  margin-left: 0;
}

.address-detail {
  font-size: 14px;
  color: #2d3748;
  line-height: 1.6;
}

.address-postal {
  font-size: 13px;
  color: #909399;
  margin-top: 4px;
}

.address-card-actions {
  display: flex;
  flex-direction: column;
  gap: 4px;
  flex-shrink: 0;
  margin-left: 12px;
}

.freeze-warning {
  margin-bottom: 16px;
}

.preference-header {
  display: flex;
  justify-content: flex-end;
  margin-bottom: 16px;
}

.checkbox-display {
  display: flex;
  flex-wrap: wrap;
  gap: 0 16px;
}

.preference-tag-list {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}

.pref-tag {
  margin: 0;
}

.notes-header {
  display: flex;
  justify-content: flex-end;
  margin-bottom: 16px;
}

.note-card {
  border-radius: 10px;
}

.note-card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.note-meta {
  display: flex;
  align-items: center;
  gap: 8px;
}

.note-operator {
  font-size: 14px;
  font-weight: 600;
  color: #2d3748;
}

.note-actions {
  display: flex;
  gap: 4px;
}

.note-content {
  font-size: 14px;
  color: #4a5568;
  line-height: 1.8;
}

.note-attachments {
  margin-top: 12px;
  padding-top: 12px;
  border-top: 1px solid #ebeef5;
}

.attachment-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 6px 0;
  font-size: 13px;
}

.att-icon {
  color: #909399;
  font-size: 16px;
}

.att-name {
  flex: 1;
  color: #606266;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.log-card {
  border-radius: 10px;
}

.log-content {
  display: flex;
  align-items: center;
  gap: 10px;
}

.log-type-tag {
  flex-shrink: 0;
}

.log-operator {
  font-size: 14px;
  color: #2d3748;
  font-weight: 500;
}

.log-remark {
  font-size: 13px;
  color: #718096;
}
</style>
