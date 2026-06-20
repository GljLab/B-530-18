<template>
  <div class="booking-create">
    <el-card shadow="never" class="form-card">
      <template #header>
        <div class="card-header">
          <span class="card-title">创建预订单</span>
        </div>
      </template>

      <el-steps :active="activeStep" finish-status="success" class="booking-steps">
        <el-step title="房间信息" icon="House" />
        <el-step title="客户信息" icon="User" />
        <el-step title="价格确认" icon="Money" />
        <el-step title="提交预订" icon="CircleCheck" />
      </el-steps>

      <div class="step-content">
        <div v-show="activeStep === 0" class="step-panel">
          <el-form
            ref="step1FormRef"
            :model="step1Form"
            :rules="step1Rules"
            label-width="110px"
            label-position="right"
          >
            <el-divider content-position="left">已选房间</el-divider>
            <el-alert
              v-if="selectedRoom"
              :title="`${selectedRoom.roomNumber} - ${selectedRoom.roomTypeName || '豪华大床房'}`"
              type="info"
              :closable="false"
              class="room-alert"
            >
              <template #default>
                <el-row :gutter="16">
                  <el-col :span="8">
                    <span class="room-label">楼层：</span>
                    <span>{{ selectedRoom.floorName || '3楼' }}</span>
                  </el-col>
                  <el-col :span="8">
                    <span class="room-label">床型：</span>
                    <span>{{ selectedRoom.bedType || '大床' }}</span>
                  </el-col>
                  <el-col :span="8">
                    <span class="room-label">面积：</span>
                    <span>{{ selectedRoom.area || '35' }}㎡</span>
                  </el-col>
                </el-row>
              </template>
            </el-alert>

            <el-divider content-position="left">入住信息</el-divider>
            <el-row :gutter="24">
              <el-col :span="12">
                <el-form-item label="入住日期" prop="checkinDate">
                  <el-date-picker
                    v-model="step1Form.checkinDate"
                    type="date"
                    placeholder="请选择入住日期"
                    style="width: 100%"
                    value-format="YYYY-MM-DD"
                    :disabled-date="disableCheckinDate"
                    @change="handleDateChange"
                  />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="退房日期" prop="checkoutDate">
                  <el-date-picker
                    v-model="step1Form.checkoutDate"
                    type="date"
                    placeholder="请选择退房日期"
                    style="width: 100%"
                    value-format="YYYY-MM-DD"
                    :disabled-date="disableCheckoutDate"
                    @change="handleDateChange"
                  />
                </el-form-item>
              </el-col>
            </el-row>
            <el-row :gutter="24">
              <el-col :span="8">
                <el-form-item label="入住人数" prop="guestCount">
                  <el-input-number
                    v-model="step1Form.guestCount"
                    :min="1"
                    :max="10"
                    style="width: 100%"
                  />
                </el-form-item>
              </el-col>
              <el-col :span="8">
                <el-form-item label="加床数量" prop="extraBedCount">
                  <el-input-number
                    v-model="step1Form.extraBedCount"
                    :min="0"
                    :max="3"
                    style="width: 100%"
                    @change="handleDateChange"
                  />
                </el-form-item>
              </el-col>
              <el-col :span="8">
                <el-form-item label="加床单价">
                  <el-input :model-value="`¥${roomTypeInfo.extraBedPrice || 80}.00/晚`" disabled />
                </el-form-item>
              </el-col>
            </el-row>
            <el-row :gutter="24">
              <el-col :span="12">
                <el-form-item label="预计到店时间" prop="arrivalTime">
                  <el-time-picker
                    v-model="step1Form.arrivalTime"
                    placeholder="请选择预计到店时间"
                    style="width: 100%"
                    value-format="HH:mm"
                  />
                </el-form-item>
              </el-col>
            </el-row>
            <el-form-item label="特殊要求" prop="specialRequests">
              <el-input
                v-model="step1Form.specialRequests"
                type="textarea"
                :rows="3"
                placeholder="请输入特殊要求（如高楼层、无烟房等）"
                maxlength="300"
                show-word-limit
              />
            </el-form-item>
          </el-form>
        </div>

        <div v-show="activeStep === 1" class="step-panel">
          <el-row :gutter="24">
            <el-col :span="16">
              <el-form
                ref="step2FormRef"
                :model="step2Form"
                :rules="step2Rules"
                label-width="110px"
                label-position="right"
              >
                <el-divider content-position="left">客户信息</el-divider>
                <el-form-item label="搜索客户">
                  <el-input
                    v-model="customerSearchKeyword"
                    placeholder="输入姓名或手机号搜索"
                    clearable
                    @input="handleCustomerSearch"
                  >
                    <template #prefix><el-icon><Search /></el-icon></template>
                  </el-input>
                </el-form-item>

                <div v-if="customerSearchResults.length > 0" class="customer-results">
                  <div
                    v-for="customer in customerSearchResults"
                    :key="customer.id"
                    class="customer-item"
                    :class="{ active: selectedCustomer?.id === customer.id }"
                    @click="handleSelectCustomer(customer)"
                  >
                    <div class="customer-info">
                      <span class="customer-name">{{ customer.name }}</span>
                      <span class="customer-phone">{{ customer.phone }}</span>
                    </div>
                    <el-tag v-if="customer.isBlacklist" type="danger" size="small">黑名单</el-tag>
                  </div>
                </div>

                <el-divider content-position="left">快速创建客户</el-divider>
                <el-row :gutter="24">
                  <el-col :span="12">
                    <el-form-item label="姓名" prop="newCustomer.name">
                      <el-input v-model="step2Form.newCustomer.name" placeholder="请输入姓名" maxlength="50" />
                    </el-form-item>
                  </el-col>
                  <el-col :span="12">
                    <el-form-item label="手机号" prop="newCustomer.phone">
                      <el-input v-model="step2Form.newCustomer.phone" placeholder="请输入手机号" maxlength="11" />
                    </el-form-item>
                  </el-col>
                </el-row>
                <el-row :gutter="24">
                  <el-col :span="12">
                    <el-form-item label="证件类型" prop="newCustomer.idType">
                      <el-select v-model="step2Form.newCustomer.idType" placeholder="请选择证件类型" style="width: 100%">
                        <el-option label="身份证" value="1" />
                        <el-option label="护照" value="2" />
                        <el-option label="港澳通行证" value="3" />
                        <el-option label="台胞证" value="4" />
                        <el-option label="其他" value="5" />
                      </el-select>
                    </el-form-item>
                  </el-col>
                  <el-col :span="12">
                    <el-form-item label="证件号" prop="newCustomer.idNumber">
                      <el-input v-model="step2Form.newCustomer.idNumber" placeholder="请输入证件号" maxlength="30" />
                    </el-form-item>
                  </el-col>
                </el-row>
                <el-form-item>
                  <el-button type="primary" :loading="creatingCustomer" @click="handleCreateCustomer">
                    <el-icon><Plus /></el-icon>创建并选择
                  </el-button>
                </el-form-item>

                <el-divider content-position="left">入住人信息</el-divider>
                <div v-for="(guest, index) in step2Form.guests" :key="index" class="guest-item">
                  <el-row :gutter="24">
                    <el-col :span="10">
                      <el-form-item
                        :label="`入住人${index + 1}姓名`"
                        :prop="`guests.${index}.name`"
                        :rules="guestNameRules"
                      >
                        <el-input v-model="guest.name" placeholder="请输入姓名" maxlength="50" />
                      </el-form-item>
                    </el-col>
                    <el-col :span="10">
                      <el-form-item
                        label="联系电话"
                        :prop="`guests.${index}.phone`"
                        :rules="guestPhoneRules"
                      >
                        <el-input v-model="guest.phone" placeholder="请输入联系电话" maxlength="11" />
                      </el-form-item>
                    </el-col>
                    <el-col :span="4">
                      <el-button
                        v-if="step2Form.guests.length > 1"
                        type="danger"
                        text
                        @click="removeGuest(index)"
                      >
                        <el-icon><Delete /></el-icon>删除
                      </el-button>
                    </el-col>
                  </el-row>
                </div>
                <el-form-item>
                  <el-button type="primary" text @click="addGuest">
                    <el-icon><Plus /></el-icon>添加入住人
                  </el-button>
                </el-form-item>
              </el-form>
            </el-col>

            <el-col :span="8">
              <el-alert
                v-if="isBlacklist"
                title="该客户在黑名单中，无法创建预订"
                type="error"
                :closable="false"
                show-icon
                class="blacklist-alert"
              />
              <el-card v-if="selectedCustomer" class="preference-card" :class="{ 'blacklist-card': isBlacklist }" shadow="hover">
                <template #header>
                  <div class="pref-header">
                    <span>客户偏好</span>
                    <el-tag v-if="isBlacklist" type="danger" size="small">黑名单</el-tag>
                  </div>
                </template>
                <div class="pref-section">
                  <div class="pref-label">基本信息</div>
                  <div class="pref-item">
                    <span class="pref-key">姓名：</span>
                    <span class="pref-value">{{ selectedCustomer.name }}</span>
                  </div>
                  <div class="pref-item">
                    <span class="pref-key">手机号：</span>
                    <span class="pref-value">{{ selectedCustomer.phone }}</span>
                  </div>
                  <div class="pref-item">
                    <span class="pref-key">会员等级：</span>
                    <el-tag size="small">{{ customerPreference.memberLevel || '普通会员' }}</el-tag>
                  </div>
                </div>
                <el-divider />
                <div v-if="customerMemberInfo" class="pref-section">
                  <div class="pref-label">会员信息</div>
                  <div class="pref-item">
                    <span class="pref-key">会员卡号：</span>
                    <span class="pref-value">{{ customerMemberInfo.memberNo || '-' }}</span>
                  </div>
                  <div class="pref-item">
                    <span class="pref-key">会员等级：</span>
                    <el-tag size="small" type="warning">{{ customerMemberInfo.levelName || customerMemberInfo.level || '-' }}</el-tag>
                  </div>
                  <div class="pref-item">
                    <span class="pref-key">可用积分：</span>
                    <span class="pref-value" style="color: #e6a23c; font-weight: 600">{{ customerMemberInfo.currentPoints ?? 0 }}</span>
                  </div>
                </div>
                <el-divider v-if="customerMemberInfo" />
                <div class="pref-section">
                  <div class="pref-label">入住偏好</div>
                  <div class="pref-tags">
                    <el-tag
                      v-for="(tag, idx) in customerPreference.roomTags"
                      :key="idx"
                      size="small"
                      style="margin-right: 6px; margin-bottom: 6px"
                    >
                      {{ tag }}
                    </el-tag>
                    <span v-if="!customerPreference.roomTags?.length" class="pref-empty">暂无偏好记录</span>
                  </div>
                </div>
                <el-divider />
                <div class="pref-section">
                  <div class="pref-label">特殊要求</div>
                  <div class="pref-text">
                    {{ customerPreference.specialNotes || '暂无特殊要求' }}
                  </div>
                </div>
              </el-card>
              <el-card v-else class="preference-card" shadow="never">
                <el-empty description="请先选择客户" :image-size="100" />
              </el-card>
            </el-col>
          </el-row>
        </div>

        <div v-show="activeStep === 2" class="step-panel">
          <el-form
            ref="step3FormRef"
            :model="step3Form"
            :rules="step3Rules"
            label-width="110px"
            label-position="right"
          >
            <el-divider content-position="left">价格明细</el-divider>
            <el-table :data="priceDetails" border style="width: 100%" class="price-table">
              <el-table-column prop="date" label="日期" width="120" align="center" />
              <el-table-column prop="weekday" label="星期" width="80" align="center" />
              <el-table-column prop="type" label="类型" width="100" align="center">
                <template #default="{ row }">
                  <el-tag v-if="row.type === 'room'" type="primary" size="small">房费</el-tag>
                  <el-tag v-else-if="row.type === 'extraBed'" type="warning" size="small">加床</el-tag>
                  <el-tag v-else type="info" size="small">其他</el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="description" label="说明" min-width="150" />
              <el-table-column prop="price" label="价格(元)" width="120" align="right">
                <template #default="{ row }">¥{{ row.price?.toFixed(2) || '0.00' }}</template>
              </el-table-column>
            </el-table>

            <div class="price-summary">
              <el-row :gutter="24">
                <el-col :span="8">
                  <div class="summary-item">
                    <span class="summary-label">房费小计：</span>
                    <span class="summary-value">¥{{ roomTotal.toFixed(2) }}</span>
                  </div>
                </el-col>
                <el-col :span="8">
                  <div class="summary-item">
                    <span class="summary-label">加床费小计：</span>
                    <span class="summary-value">¥{{ extraBedTotal.toFixed(2) }}</span>
                  </div>
                </el-col>
                <el-col :span="8">
                  <div class="summary-item">
                    <span class="summary-label">入住天数：</span>
                    <span class="summary-value">{{ stayDays }}晚</span>
                  </div>
                </el-col>
              </el-row>
            </div>

            <el-divider content-position="left">其他费用</el-divider>
            <el-row :gutter="24">
              <el-col :span="12">
                <el-form-item label="其他费用" prop="otherFee">
                  <el-input-number
                    v-model="step3Form.otherFee"
                    :min="0"
                    :precision="2"
                    :step="10"
                    style="width: 100%"
                    @change="calculateTotal"
                  />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="费用说明">
                  <el-input v-model="step3Form.otherFeeRemark" placeholder="请输入费用说明" maxlength="100" />
                </el-form-item>
              </el-col>
            </el-row>

            <el-divider content-position="left">优惠折扣</el-divider>
            <el-row :gutter="24">
              <el-col :span="12">
                <el-form-item label="优惠金额" prop="discount">
                  <el-input-number
                    v-model="step3Form.discount"
                    :min="0"
                    :precision="2"
                    :step="10"
                    style="width: 100%"
                    @change="calculateTotal"
                  />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="优惠说明">
                  <el-input v-model="step3Form.discountRemark" placeholder="请输入优惠说明" maxlength="100" />
                </el-form-item>
              </el-col>
            </el-row>

            <template v-if="customerMemberInfo">
              <el-divider content-position="left">积分抵扣</el-divider>
              <el-alert
                v-if="(roomTotal + extraBedTotal + step3Form.otherFee - step3Form.discount) < 100"
                title="订单金额不足100元，不可使用积分抵扣"
                type="warning"
                :closable="false"
                show-icon
                style="margin-bottom: 16px"
              />
              <el-row :gutter="24">
                <el-col :span="12">
                  <el-form-item label="使用积分">
                    <el-input-number
                      v-model="pointsUsed"
                      :min="0"
                      :max="maxPointsUsable"
                      :step="100"
                      :precision="0"
                      :disabled="(roomTotal + extraBedTotal + step3Form.otherFee - step3Form.discount) < 100"
                      style="width: 100%"
                    />
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="抵扣金额">
                    <span style="font-size: 18px; font-weight: 600; color: #67c23a">-¥{{ pointsDeductionAmount.toFixed(2) }}</span>
                  </el-form-item>
                </el-col>
              </el-row>
              <el-row :gutter="24">
                <el-col :span="24">
                  <div style="color: #909399; font-size: 13px; line-height: 1.8; padding-left: 110px">
                    <div>可用积分：<span style="color: #e6a23c; font-weight: 600">{{ customerMemberInfo.currentPoints ?? 0 }}</span> 积分（100积分=10元）</div>
                    <div>单次最多使用5000积分，最低消费100元，最多抵扣订单金额30%</div>
                  </div>
                </el-col>
              </el-row>
            </template>

            <el-divider />

            <div class="total-amount">
              <span class="total-label">应付总额：</span>
              <span class="total-value">¥{{ totalAmount.toFixed(2) }}</span>
            </div>

            <el-divider content-position="left">担保方式</el-divider>
            <el-form-item label="担保方式" prop="guaranteeType">
              <el-radio-group v-model="step3Form.guaranteeType">
                <el-radio value="1">
                  <el-tag size="small">到店支付</el-tag>
                </el-radio>
                <el-radio value="2">
                  <el-tag type="warning" size="small">信用卡担保</el-tag>
                </el-radio>
                <el-radio value="3">
                  <el-tag type="success" size="small">预付全款</el-tag>
                </el-radio>
              </el-radio-group>
            </el-form-item>

            <el-divider content-position="left">预订来源</el-divider>
            <el-row :gutter="24">
              <el-col :span="12">
                <el-form-item label="预订来源" prop="source">
                  <el-select v-model="step3Form.source" placeholder="请选择预订来源" style="width: 100%">
                    <el-option label="前台" value="1" />
                    <el-option label="官网" value="2" />
                    <el-option label="第三方平台" value="3" />
                    <el-option label="协议单位" value="4" />
                    <el-option label="旅行社" value="5" />
                    <el-option label="其他" value="6" />
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="来源备注">
                  <el-input v-model="step3Form.sourceRemark" placeholder="请输入来源备注" maxlength="100" />
                </el-form-item>
              </el-col>
            </el-row>
          </el-form>
        </div>

        <div v-show="activeStep === 3" class="step-panel">
          <el-alert
            title="请确认以下预订信息是否正确"
            type="warning"
            :closable="false"
            class="confirm-alert"
          />

          <el-row :gutter="24">
            <el-col :span="12">
              <el-card class="confirm-card" shadow="hover">
                <template #header>
                  <span class="confirm-card-title">
                    <el-icon><House /></el-icon> 房间信息
                  </span>
                </template>
                <div class="confirm-item">
                  <span class="confirm-label">房间：</span>
                  <span class="confirm-value">{{ selectedRoom?.roomNumber || '未选择' }} - {{ selectedRoom?.roomTypeName || '豪华大床房' }}</span>
                </div>
                <div class="confirm-item">
                  <span class="confirm-label">入住日期：</span>
                  <span class="confirm-value">{{ step1Form.checkinDate }}</span>
                </div>
                <div class="confirm-item">
                  <span class="confirm-label">退房日期：</span>
                  <span class="confirm-value">{{ step1Form.checkoutDate }}</span>
                </div>
                <div class="confirm-item">
                  <span class="confirm-label">入住天数：</span>
                  <span class="confirm-value">{{ stayDays }}晚</span>
                </div>
                <div class="confirm-item">
                  <span class="confirm-label">入住人数：</span>
                  <span class="confirm-value">{{ step1Form.guestCount }}人</span>
                </div>
                <div class="confirm-item">
                  <span class="confirm-label">加床数量：</span>
                  <span class="confirm-value">{{ step1Form.extraBedCount }}张</span>
                </div>
                <div class="confirm-item">
                  <span class="confirm-label">预计到店：</span>
                  <span class="confirm-value">{{ step1Form.arrivalTime || '未设置' }}</span>
                </div>
                <div class="confirm-item" v-if="step1Form.specialRequests">
                  <span class="confirm-label">特殊要求：</span>
                  <span class="confirm-value">{{ step1Form.specialRequests }}</span>
                </div>
              </el-card>
            </el-col>

            <el-col :span="12">
              <el-card class="confirm-card" shadow="hover">
                <template #header>
                  <span class="confirm-card-title">
                    <el-icon><User /></el-icon> 客户信息
                  </span>
                </template>
                <div class="confirm-item">
                  <span class="confirm-label">客户姓名：</span>
                  <span class="confirm-value">{{ selectedCustomer?.name || '未选择' }}</span>
                </div>
                <div class="confirm-item">
                  <span class="confirm-label">联系电话：</span>
                  <span class="confirm-value">{{ selectedCustomer?.phone || '未选择' }}</span>
                </div>
                <div class="confirm-item">
                  <span class="confirm-label">证件类型：</span>
                  <span class="confirm-value">{{ idTypeLabel }}</span>
                </div>
                <div class="confirm-item" v-if="selectedCustomer?.idNumber">
                  <span class="confirm-label">证件号码：</span>
                  <span class="confirm-value">{{ selectedCustomer.idNumber }}</span>
                </div>
                <el-divider />
                <div class="confirm-item">
                  <span class="confirm-label">入住人：</span>
                  <div class="guest-list">
                    <div v-for="(guest, idx) in step2Form.guests" :key="idx" class="guest-row">
                      {{ guest.name }} ({{ guest.phone }})
                    </div>
                  </div>
                </div>
              </el-card>
            </el-col>
          </el-row>

          <el-row :gutter="24" style="margin-top: 16px">
            <el-col :span="12">
              <el-card class="confirm-card" shadow="hover">
                <template #header>
                  <span class="confirm-card-title">
                    <el-icon><Money /></el-icon> 价格信息
                  </span>
                </template>
                <div class="confirm-item">
                  <span class="confirm-label">房费小计：</span>
                  <span class="confirm-value">¥{{ roomTotal.toFixed(2) }}</span>
                </div>
                <div class="confirm-item">
                  <span class="confirm-label">加床费：</span>
                  <span class="confirm-value">¥{{ extraBedTotal.toFixed(2) }}</span>
                </div>
                <div v-if="step3Form.otherFee > 0" class="confirm-item">
                  <span class="confirm-label">其他费用：</span>
                  <span class="confirm-value">¥{{ step3Form.otherFee.toFixed(2) }}</span>
                </div>
                <div v-if="step3Form.discount > 0" class="confirm-item">
                  <span class="confirm-label">优惠折扣：</span>
                  <span class="confirm-value discount">-¥{{ step3Form.discount.toFixed(2) }}</span>
                </div>
                <div v-if="pointsUsed > 0" class="confirm-item">
                  <span class="confirm-label">积分抵扣：</span>
                  <span class="confirm-value discount">-¥{{ pointsDeductionAmount.toFixed(2) }}（使用{{ pointsUsed }}积分）</span>
                </div>
                <el-divider />
                <div class="confirm-item total">
                  <span class="confirm-label">应付总额：</span>
                  <span class="confirm-value total-amount-value">¥{{ totalAmount.toFixed(2) }}</span>
                </div>
              </el-card>
            </el-col>

            <el-col :span="12">
              <el-card class="confirm-card" shadow="hover">
                <template #header>
                  <span class="confirm-card-title">
                    <el-icon><Document /></el-icon> 预订信息
                  </span>
                </template>
                <div class="confirm-item">
                  <span class="confirm-label">预订单号：</span>
                  <span class="confirm-value booking-no">{{ previewBookingNo }}</span>
                </div>
                <div class="confirm-item">
                  <span class="confirm-label">担保方式：</span>
                  <span class="confirm-value">{{ guaranteeTypeLabel }}</span>
                </div>
                <div class="confirm-item">
                  <span class="confirm-label">预订来源：</span>
                  <span class="confirm-value">{{ sourceLabel }}</span>
                </div>
                <div v-if="step3Form.sourceRemark" class="confirm-item">
                  <span class="confirm-label">来源备注：</span>
                  <span class="confirm-value">{{ step3Form.sourceRemark }}</span>
                </div>
                <div class="confirm-item">
                  <span class="confirm-label">创建时间：</span>
                  <span class="confirm-value">{{ currentDateTime }}</span>
                </div>
              </el-card>
            </el-col>
          </el-row>
        </div>
      </div>

      <div class="step-actions">
        <el-button v-if="activeStep > 0" @click="handlePrev">
          <el-icon><ArrowLeft /></el-icon>上一步
        </el-button>
        <el-button
          v-if="activeStep < 3"
          type="primary"
          @click="handleNext"
        >
          下一步<el-icon><ArrowRight /></el-icon>
        </el-button>
        <el-button
          v-if="activeStep === 3"
          type="primary"
          :loading="submitting"
          :disabled="isBlacklist"
          @click="handleSubmit"
        >
          <el-icon><Check /></el-icon>{{ isBlacklist ? '客户在黑名单，无法提交' : '提交预订' }}
        </el-button>
        <el-button @click="handleCancel">取消</el-button>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  Plus, Check, Search, Delete, ArrowLeft, ArrowRight,
  House, User, Money, CircleCheck, Document
} from '@element-plus/icons-vue'
import api from '@/api'

const router = useRouter()
const route = useRoute()

const activeStep = ref(0)
const submitting = ref(false)
const creatingCustomer = ref(false)
const calculatingPrice = ref(false)

const step1FormRef = ref(null)
const step2FormRef = ref(null)
const step3FormRef = ref(null)

const selectedRoom = ref(null)
const selectedCustomer = ref(null)
const isBlacklist = ref(false)
const customerSearchKeyword = ref('')
const customerSearchResults = ref([])
const customerSearchTimer = ref(null)
const customerMemberInfo = ref(null)
const pointsUsed = ref(0)

const roomTypeInfo = reactive({
  basePrice: 298,
  weekendPrice: 358,
  holidayPrice: 398,
  extraBedPrice: 80
})

const customerPreference = reactive({
  memberLevel: '普通会员',
  roomTags: [],
  specialNotes: ''
})

const priceDetails = ref([])

const step1Form = reactive({
  checkinDate: '',
  checkoutDate: '',
  guestCount: 1,
  extraBedCount: 0,
  arrivalTime: '14:00',
  specialRequests: ''
})

const step2Form = reactive({
  newCustomer: {
    name: '',
    phone: '',
    idType: '1',
    idNumber: ''
  },
  guests: [
    { name: '', phone: '' }
  ]
})

const step3Form = reactive({
  otherFee: 0,
  otherFeeRemark: '',
  discount: 0,
  discountRemark: '',
  guaranteeType: '1',
  source: '1',
  sourceRemark: ''
})

const step1Rules = {
  checkinDate: [{ required: true, message: '请选择入住日期', trigger: 'change' }],
  checkoutDate: [{ required: true, message: '请选择退房日期', trigger: 'change' }],
  guestCount: [{ required: true, message: '请输入入住人数', trigger: 'change' }],
  extraBedCount: [{ required: true, message: '请输入加床数量', trigger: 'change' }],
  arrivalTime: [{ required: true, message: '请选择预计到店时间', trigger: 'change' }]
}

const step2Rules = {
  'newCustomer.name': [{ required: false, message: '请输入姓名', trigger: 'blur' }],
  'newCustomer.phone': [
    { required: false, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ],
  'newCustomer.idType': [{ required: false, message: '请选择证件类型', trigger: 'change' }],
  'newCustomer.idNumber': [{ required: false, message: '请输入证件号', trigger: 'blur' }]
}

const guestNameRules = [{ required: true, message: '请输入入住人姓名', trigger: 'blur' }]
const guestPhoneRules = [
  { required: true, message: '请输入联系电话', trigger: 'blur' },
  { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
]

const step3Rules = {
  guaranteeType: [{ required: true, message: '请选择担保方式', trigger: 'change' }],
  source: [{ required: true, message: '请选择预订来源', trigger: 'change' }]
}

const stayDays = computed(() => {
  if (!step1Form.checkinDate || !step1Form.checkoutDate) return 0
  const start = new Date(step1Form.checkinDate)
  const end = new Date(step1Form.checkoutDate)
  return Math.max(0, Math.ceil((end - start) / (1000 * 60 * 60 * 24)))
})

const roomTotal = computed(() => {
  return priceDetails.value
    .filter(p => p.type === 'room')
    .reduce((sum, p) => sum + (p.price || 0), 0)
})

const extraBedTotal = computed(() => {
  return priceDetails.value
    .filter(p => p.type === 'extraBed')
    .reduce((sum, p) => sum + (p.price || 0), 0)
})

const totalAmount = computed(() => {
  return roomTotal.value + extraBedTotal.value + step3Form.otherFee - step3Form.discount - pointsDeductionAmount.value
})

const pointsDeductionAmount = computed(() => {
  return pointsUsed.value * 0.1
})

const maxPointsUsable = computed(() => {
  if (!customerMemberInfo.value) return 0
  const availablePoints = customerMemberInfo.value.currentPoints ?? 0
  const baseAmount = roomTotal.value + extraBedTotal.value + step3Form.otherFee - step3Form.discount
  const maxDeduction = baseAmount * 0.30
  const maxPointsFromCap = Math.floor(maxDeduction / 0.1)
  return Math.min(availablePoints, 5000, maxPointsFromCap)
})

const previewBookingNo = computed(() => {
  const now = new Date()
  const dateStr = now.toISOString().slice(0, 10).replace(/-/g, '')
  const random = Math.floor(Math.random() * 10000).toString().padStart(4, '0')
  return `BK${dateStr}${random}`
})

const currentDateTime = computed(() => {
  return new Date().toLocaleString('zh-CN')
})

const idTypeLabel = computed(() => {
  const map = { '1': '身份证', '2': '护照', '3': '港澳通行证', '4': '台胞证', '5': '其他' }
  return map[selectedCustomer.value?.idType] || '未设置'
})

const guaranteeTypeLabel = computed(() => {
  const map = { '1': '到店支付', '2': '信用卡担保', '3': '预付全款' }
  return map[step3Form.guaranteeType] || '未选择'
})

const sourceLabel = computed(() => {
  const map = { '1': '前台', '2': '官网', '3': '第三方平台', '4': '协议单位', '5': '旅行社', '6': '其他' }
  return map[step3Form.source] || '未选择'
})

const disableCheckinDate = (date) => {
  return date < new Date(new Date().setHours(0, 0, 0, 0) - 1)
}

const disableCheckoutDate = (date) => {
  if (!step1Form.checkinDate) return true
  return date <= new Date(step1Form.checkinDate)
}

const initDefaultDates = () => {
  const pendingBooking = sessionStorage.getItem('pendingBooking')
  if (pendingBooking) {
    try {
      const bookingData = JSON.parse(pendingBooking)
      if (bookingData.checkInDate && bookingData.checkOutDate) {
        step1Form.checkinDate = bookingData.checkInDate
        step1Form.checkoutDate = bookingData.checkOutDate
        return
      }
    } catch {}
  }
  
  const today = new Date()
  const tomorrow = new Date(today)
  tomorrow.setDate(tomorrow.getDate() + 1)
  const dayAfter = new Date(today)
  dayAfter.setDate(dayAfter.getDate() + 2)
  step1Form.checkinDate = formatDate(tomorrow)
  step1Form.checkoutDate = formatDate(dayAfter)
}

const formatDate = (date) => {
  return date.toISOString().slice(0, 10)
}

const getWeekday = (dateStr) => {
  const weekdays = ['日', '一', '二', '三', '四', '五', '六']
  return `周${weekdays[new Date(dateStr).getDay()]}`
}

const handleDateChange = async () => {
  if (step1Form.checkinDate && step1Form.checkoutDate && stayDays.value > 0) {
    await calculatePrice()
  }
}

const calculatePrice = async () => {
  if (!selectedRoom.value?.roomTypeId && !selectedRoom.value?.id) return
  calculatingPrice.value = true
  try {
    const params = {
      roomTypeId: selectedRoom.value?.roomTypeId || 1,
      checkinDate: step1Form.checkinDate,
      checkoutDate: step1Form.checkoutDate,
      extraBedCount: step1Form.extraBedCount,
      extraBedPrice: roomTypeInfo.extraBedPrice
    }
    const res = await api.booking.calculatePrice(params)
    if (res.code === 200 && res.data) {
      priceDetails.value = res.data.details || []
      if (res.data.roomTypeInfo) {
        roomTypeInfo.basePrice = res.data.roomTypeInfo.basePrice ?? roomTypeInfo.basePrice
        roomTypeInfo.weekendPrice = res.data.roomTypeInfo.weekendPrice ?? roomTypeInfo.weekendPrice
        roomTypeInfo.holidayPrice = res.data.roomTypeInfo.holidayPrice ?? roomTypeInfo.holidayPrice
        roomTypeInfo.extraBedPrice = res.data.roomTypeInfo.extraBedPrice ?? roomTypeInfo.extraBedPrice
      }
    } else {
      generateMockPriceDetails()
    }
  } catch {
    generateMockPriceDetails()
  } finally {
    calculatingPrice.value = false
  }
}

const generateMockPriceDetails = () => {
  const details = []
  if (!step1Form.checkinDate || !step1Form.checkoutDate) return
  const start = new Date(step1Form.checkinDate)
  const end = new Date(step1Form.checkoutDate)
  const current = new Date(start)
  while (current < end) {
    const dateStr = formatDate(current)
    const day = current.getDay()
    const isWeekend = day === 0 || day === 6
    const price = isWeekend ? roomTypeInfo.weekendPrice : roomTypeInfo.basePrice
    details.push({
      date: dateStr,
      weekday: getWeekday(dateStr),
      type: 'room',
      description: isWeekend ? '周末房价' : '平日房价',
      price
    })
    if (step1Form.extraBedCount > 0) {
      details.push({
        date: dateStr,
        weekday: getWeekday(dateStr),
        type: 'extraBed',
        description: `加床${step1Form.extraBedCount}张`,
        price: roomTypeInfo.extraBedPrice * step1Form.extraBedCount
      })
    }
    current.setDate(current.getDate() + 1)
  }
  priceDetails.value = details
}

const calculateTotal = () => {}

const handleCustomerSearch = () => {
  if (!customerSearchKeyword.value || customerSearchKeyword.value.length < 1) {
    customerSearchResults.value = []
    return
  }
  if (customerSearchTimer.value) {
    clearTimeout(customerSearchTimer.value)
  }
  customerSearchTimer.value = setTimeout(async () => {
    try {
      const res = await api.customer.getPage({
        pageNum: 1,
        pageSize: 10,
        keyword: customerSearchKeyword.value
      })
      if (res.code === 200) {
        customerSearchResults.value = res.data?.records || res.data?.list || []
      }
    } catch {
      customerSearchResults.value = []
    }
  }, 300)
}

const handleSelectCustomer = async (customer) => {
  selectedCustomer.value = customer
  customerMemberInfo.value = null
  pointsUsed.value = 0
  try {
    const res = await api.customer.getPreference(customer.id)
    if (res.code === 200 && res.data) {
      Object.assign(customerPreference, res.data)
    }
  } catch {}
  try {
    const res = await api.customer.checkBlacklist(customer.id)
    isBlacklist.value = res.code === 200 && res.data === true
  } catch {
    isBlacklist.value = false
  }
  try {
    const res = await api.member.getByCustomerId(customer.id)
    if (res.code === 200 && res.data) {
      customerMemberInfo.value = res.data
    }
  } catch {}
  if (!step2Form.guests[0]?.name) {
    step2Form.guests[0].name = customer.name
  }
  if (!step2Form.guests[0]?.phone) {
    step2Form.guests[0].phone = customer.phone
  }
  if (customerPreference.specialNotes && !step1Form.specialRequests) {
    step1Form.specialRequests = customerPreference.specialNotes
  }
}

const handleCreateCustomer = async () => {
  const { name, phone, idType, idNumber } = step2Form.newCustomer
  if (!name || !phone) {
    ElMessage.warning('请填写姓名和手机号')
    return
  }
  if (!/^1[3-9]\d{9}$/.test(phone)) {
    ElMessage.warning('请输入正确的手机号')
    return
  }
  creatingCustomer.value = true
  try {
    const res = await api.customer.add({
      name,
      phone,
      idType,
      idNumber
    })
    if (res.code === 200 && res.data) {
      ElMessage.success('创建客户成功')
      const newCustomer = typeof res.data === 'object' ? res.data : { id: res.data, name, phone, idType, idNumber }
      await handleSelectCustomer(newCustomer)
      step2Form.newCustomer = { name: '', phone: '', idType: '1', idNumber: '' }
    } else {
      ElMessage.error(res.message || '创建失败')
    }
  } catch {
    ElMessage.error('创建失败，请重试')
  } finally {
    creatingCustomer.value = false
  }
}

const addGuest = () => {
  if (step2Form.guests.length >= step1Form.guestCount) {
    ElMessage.warning(`入住人数为${step1Form.guestCount}人，最多添加${step1Form.guestCount}个入住人`)
    return
  }
  step2Form.guests.push({ name: '', phone: '' })
}

const removeGuest = (index) => {
  step2Form.guests.splice(index, 1)
}

const validateStep = async (step) => {
  if (step === 0) {
    if (!selectedRoom.value) {
      ElMessage.warning('请先选择房间')
      return false
    }
    return await step1FormRef.value.validate().catch(() => false)
  }
  if (step === 1) {
    if (!selectedCustomer.value) {
      ElMessage.warning('请先选择或创建客户')
      return false
    }
    const valid = await step2FormRef.value.validate().catch(() => false)
    if (!valid) return false
    for (let i = 0; i < step2Form.guests.length; i++) {
      const guest = step2Form.guests[i]
      if (!guest.name || !guest.phone) {
        ElMessage.warning(`请完善第${i + 1}位入住人信息`)
        return false
      }
      if (!/^1[3-9]\d{9}$/.test(guest.phone)) {
        ElMessage.warning(`第${i + 1}位入住人手机号格式不正确`)
        return false
      }
    }
    return true
  }
  if (step === 2) {
    return await step3FormRef.value.validate().catch(() => false)
  }
  return true
}

const handlePrev = () => {
  if (activeStep.value > 0) {
    activeStep.value--
  }
}

const handleNext = async () => {
  const valid = await validateStep(activeStep.value)
  if (!valid) return
  if (activeStep.value < 3) {
    activeStep.value++
    if (activeStep.value === 2 && priceDetails.value.length === 0) {
      await calculatePrice()
    }
  }
}

const handleSubmit = async () => {
  const valid = await validateStep(2)
  if (!valid) return
  if (isBlacklist.value) {
    ElMessage.error('客户在黑名单中，无法提交预订')
    return
  }
  submitting.value = true
  try {
    const payload = {
      roomId: selectedRoom.value?.id,
      roomTypeId: selectedRoom.value?.roomTypeId || 1,
      customerId: selectedCustomer.value?.id,
      customerName: selectedCustomer.value?.name,
      customerPhone: selectedCustomer.value?.phone,
      checkInDate: step1Form.checkinDate,
      checkOutDate: step1Form.checkoutDate,
      days: stayDays.value,
      guestCount: step1Form.guestCount,
      extraBedCount: step1Form.extraBedCount,
      expectedArrivalTime: step1Form.checkinDate + 'T' + (step1Form.arrivalTime || '14:00') + ':00',
      specialRequirements: step1Form.specialRequirements,
      guestNames: step2Form.guests.map(g => g.name).join(','),
      guestPhone: step2Form.guests[0]?.phone || '',
      bookingSource: step3Form.source,
      sourceRemark: step3Form.sourceRemark,
      guaranteeType: step3Form.guaranteeType,
      roomTotal: roomTotal.value,
      extraBedTotal: extraBedTotal.value,
      otherFee: step3Form.otherFee,
      otherFeeRemark: step3Form.otherFeeRemark,
      discount: step3Form.discount,
      discountRemark: step3Form.discountRemark,
      totalAmount: totalAmount.value,
      pointsUsed: pointsUsed.value,
      pointsDeductionAmount: pointsDeductionAmount.value,
      memberId: customerMemberInfo.value?.id || null,
      memberNo: customerMemberInfo.value?.memberNo || null,
      details: priceDetails.value
    }
    const res = await api.booking.create(payload)
    if (res.code === 200) {
      ElMessage.success('创建预订单成功')
      sessionStorage.removeItem('pendingBooking')
      localStorage.removeItem('selectedBookingRoom')
      router.push('/booking/list')
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
  router.push('/booking/roomQuery')
}

const loadRoomTypeInfo = async () => {
  try {
    const roomTypeId = selectedRoom.value?.roomTypeId || 1
    const res = await api.hotel.getRoomType(roomTypeId)
    if (res.code === 200 && res.data) {
      roomTypeInfo.basePrice = res.data.basePrice || roomTypeInfo.basePrice
      roomTypeInfo.weekendPrice = res.data.weekendPrice || roomTypeInfo.weekendPrice
      roomTypeInfo.holidayPrice = res.data.holidayPrice || roomTypeInfo.holidayPrice
      roomTypeInfo.extraBedPrice = res.data.extraBedPrice || roomTypeInfo.extraBedPrice
    }
  } catch {}
}

const initSelectedRoom = () => {
  const pendingBooking = sessionStorage.getItem('pendingBooking')
  if (pendingBooking) {
    try {
      const bookingData = JSON.parse(pendingBooking)
      if (bookingData.rooms && bookingData.rooms.length > 0) {
        selectedRoom.value = {
          id: bookingData.rooms[0].roomId,
          roomNumber: bookingData.rooms[0].roomNumber,
          roomTypeId: bookingData.rooms[0].roomTypeId,
          roomTypeName: bookingData.rooms[0].typeName,
          price: bookingData.rooms[0].price
        }
        return
      }
    } catch {}
  }
  
  const roomData = route.query?.room || localStorage.getItem('selectedBookingRoom')
  if (roomData) {
    try {
      selectedRoom.value = typeof roomData === 'string' ? JSON.parse(roomData) : roomData
    } catch {}
  }
  if (!selectedRoom.value) {
    selectedRoom.value = {
      id: 1,
      roomNumber: '301',
      roomTypeId: 1,
      roomTypeName: '豪华大床房',
      floorName: '3楼',
      bedType: '大床',
      area: 35
    }
  }
}

onMounted(() => {
  initSelectedRoom()
  initDefaultDates()
  loadRoomTypeInfo()
  setTimeout(() => {
    calculatePrice()
  }, 500)
})
</script>

<style scoped lang="scss">
.booking-create {
  padding: 16px;
}

.form-card {
  max-width: 1200px;
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

.booking-steps {
  margin: 24px 0 32px;
}

.step-content {
  min-height: 500px;
}

.step-panel {
  padding: 8px 0;
}

.room-alert {
  margin-bottom: 16px;
}

.room-label {
  color: #909399;
  margin-right: 4px;
}

.customer-results {
  max-height: 200px;
  overflow-y: auto;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  margin-bottom: 16px;
}

.customer-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 16px;
  cursor: pointer;
  border-bottom: 1px solid #f0f0f0;
  transition: background-color 0.2s;

  &:last-child {
    border-bottom: none;
  }

  &:hover {
    background-color: #f5f7fa;
  }

  &.active {
    background-color: #ecf5ff;
  }
}

.customer-info {
  display: flex;
  gap: 16px;
}

.customer-name {
  font-weight: 500;
  color: #303133;
}

.customer-phone {
  color: #909399;
}

.guest-item {
  padding: 8px 0;
  border-bottom: 1px dashed #e4e7ed;

  &:last-child {
    border-bottom: none;
  }
}

.blacklist-alert {
  margin-bottom: 16px;
}

.blacklist-card {
  border: 1px solid #f56c6c;
  box-shadow: 0 2px 12px 0 rgba(245, 108, 108, 0.15);
}

.preference-card {
  position: sticky;
  top: 16px;
}

.pref-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.pref-section {
  margin-bottom: 8px;
}

.pref-label {
  font-weight: 500;
  color: #303133;
  margin-bottom: 8px;
}

.pref-item {
  margin-bottom: 6px;
  color: #606266;
}

.pref-key {
  color: #909399;
}

.pref-tags {
  display: flex;
  flex-wrap: wrap;
}

.pref-text {
  color: #606266;
  line-height: 1.6;
}

.pref-empty {
  color: #c0c4cc;
  font-size: 13px;
}

.price-table {
  margin-bottom: 16px;
}

.price-summary {
  background-color: #f8f9fa;
  padding: 16px;
  border-radius: 4px;
  margin-bottom: 16px;
}

.summary-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.summary-label {
  color: #606266;
}

.summary-value {
  font-weight: 500;
  color: #303133;
}

.total-amount {
  text-align: right;
  padding: 16px 24px;
  background: linear-gradient(90deg, #fff5f5 0%, #fff 100%);
  border-radius: 4px;
  margin-bottom: 16px;
}

.total-label {
  font-size: 16px;
  color: #606266;
  margin-right: 8px;
}

.total-value {
  font-size: 28px;
  font-weight: 700;
  color: #f56c6c;
}

.confirm-alert {
  margin-bottom: 16px;
}

.confirm-card {
  height: 100%;
}

.confirm-card-title {
  display: flex;
  align-items: center;
  gap: 6px;
  font-weight: 500;
}

.confirm-item {
  display: flex;
  margin-bottom: 10px;
  line-height: 1.6;

  &.total {
    padding-top: 8px;
  }
}

.confirm-label {
  color: #909399;
  min-width: 90px;
  flex-shrink: 0;
}

.confirm-value {
  color: #303133;
  flex: 1;

  &.discount {
    color: #67c23a;
  }

  &.total-amount-value {
    font-size: 20px;
    font-weight: 700;
    color: #f56c6c;
  }
}

.booking-no {
  font-family: 'Courier New', monospace;
  font-weight: 600;
  color: #409eff;
}

.guest-list {
  flex: 1;
}

.guest-row {
  margin-bottom: 4px;
}

.step-actions {
  display: flex;
  justify-content: center;
  gap: 12px;
  padding-top: 24px;
  border-top: 1px solid #ebeef5;
  margin-top: 24px;
}

:deep(.el-radio) {
  margin-right: 16px;
}
</style>
