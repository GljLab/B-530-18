<template>
  <div class="layout-container">
    <!-- 侧边栏 -->
    <el-aside :width="isCollapse ? '64px' : '220px'" class="layout-aside">
      <div class="logo">
        <span v-if="!isCollapse">智慧酒店管理平台</span>
        <span v-else>酒</span>
      </div>
      
      <el-menu
        :default-active="activeMenu"
        :collapse="isCollapse"
        :collapse-transition="false"
        background-color="#304156"
        text-color="#bfcbd9"
        active-text-color="#409EFF"
        router
      >
        <el-menu-item index="/dashboard">
          <el-icon><HomeFilled /></el-icon>
          <template #title>首页</template>
        </el-menu-item>
        
        <el-sub-menu index="/system" v-if="hasSystemPermission">
          <template #title>
            <el-icon><Setting /></el-icon>
            <span>系统管理</span>
          </template>
          
          <el-menu-item index="/system/user" v-if="hasPermission('system:user:list')">
            <el-icon><User /></el-icon>
            <span>用户管理</span>
          </el-menu-item>
          
          <el-menu-item index="/system/role" v-if="hasPermission('system:role:list')">
            <el-icon><Avatar /></el-icon>
            <span>角色管理</span>
          </el-menu-item>
          
          <el-menu-item index="/system/menu" v-if="hasPermission('system:menu:list')">
            <el-icon><Menu /></el-icon>
            <span>菜单管理</span>
          </el-menu-item>
          
          <el-menu-item index="/system/dataPerm" v-if="hasPermission('system:dataPerm:list')">
            <el-icon><Lock /></el-icon>
            <span>数据权限</span>
          </el-menu-item>
        </el-sub-menu>
        <el-sub-menu index="/hotel" v-if="hasHotelPermission">
          <template #title>
            <el-icon><House /></el-icon>
            <span>酒店管理</span>
          </template>

          <el-menu-item index="/hotel/overview" v-if="hasPermission('hotel:info:list')">
            <el-icon><OfficeBuilding /></el-icon>
            <span>酒店概览</span>
          </el-menu-item>

          <el-menu-item index="/hotel/building" v-if="hasPermission('hotel:building:list')">
            <el-icon><School /></el-icon>
            <span>楼栋楼层</span>
          </el-menu-item>

          <el-menu-item index="/hotel/roomType" v-if="hasPermission('hotel:roomType:list')">
            <el-icon><Tickets /></el-icon>
            <span>房型管理</span>
          </el-menu-item>

          <el-menu-item index="/hotel/room" v-if="hasPermission('hotel:room:list')">
            <el-icon><Key /></el-icon>
            <span>房间管理</span>
          </el-menu-item>

          <el-menu-item index="/hotel/dashboard" v-if="hasPermission('hotel:dashboard:list')">
            <el-icon><DataAnalysis /></el-icon>
            <span>统计看板</span>
          </el-menu-item>
        </el-sub-menu>

        <el-sub-menu index="/maintenance" v-if="hasMaintenancePermission">
          <template #title>
            <el-icon><Tools /></el-icon>
            <span>维护管理</span>
          </template>

          <el-menu-item index="/maintenance/order" v-if="hasPermission('maintenance:order:list')">
            <el-icon><Document /></el-icon>
            <span>维护单管理</span>
          </el-menu-item>

          <el-menu-item index="/maintenance/order/create" v-if="hasPermission('maintenance:order:add')">
            <el-icon><Edit /></el-icon>
            <span>创建维护单</span>
          </el-menu-item>

          <el-menu-item index="/maintenance/changeLog" v-if="hasPermission('maintenance:changeLog:list')">
            <el-icon><Clock /></el-icon>
            <span>房间变更日志</span>
          </el-menu-item>

          <el-menu-item index="/maintenance/statistics" v-if="hasPermission('maintenance:statistics:list')">
            <el-icon><DataLine /></el-icon>
            <span>维护统计报表</span>
          </el-menu-item>
        </el-sub-menu>

        <el-sub-menu index="/customer" v-if="hasCustomerPermission">
          <template #title>
            <el-icon><UserFilled /></el-icon>
            <span>客户管理</span>
          </template>

          <el-menu-item index="/customer/list" v-if="hasPermission('customer:list')">
            <el-icon><User /></el-icon>
            <span>客户列表</span>
          </el-menu-item>

          <el-menu-item index="/customer/create" v-if="hasPermission('customer:add')">
            <el-icon><Plus /></el-icon>
            <span>新增客户</span>
          </el-menu-item>

          <el-menu-item index="/customer/tags" v-if="hasPermission('customer:tag:list')">
            <el-icon><PriceTag /></el-icon>
            <span>标签管理</span>
          </el-menu-item>

          <el-menu-item index="/customer/blacklist" v-if="hasPermission('customer:blacklist:list')">
            <el-icon><Warning /></el-icon>
            <span>黑名单管理</span>
          </el-menu-item>

          <el-menu-item index="/customer/blacklistApproval" v-if="hasPermission('customer:blacklist:approve')">
            <el-icon><Checked /></el-icon>
            <span>黑名单审批</span>
          </el-menu-item>

          <el-menu-item index="/customer/merge" v-if="hasPermission('customer:merge')">
            <el-icon><CopyDocument /></el-icon>
            <span>客户合并</span>
          </el-menu-item>

          <el-menu-item index="/customer/import" v-if="hasPermission('customer:import')">
            <el-icon><Upload /></el-icon>
            <span>批量导入</span>
          </el-menu-item>
        </el-sub-menu>

        <el-sub-menu index="/booking" v-if="hasBookingPermission">
          <template #title>
            <el-icon><Calendar /></el-icon>
            <span>预订管理</span>
          </template>

          <el-menu-item index="/booking/roomQuery" v-if="hasPermission('booking:roomQuery:list')">
            <el-icon><Search /></el-icon>
            <span>房源查询</span>
          </el-menu-item>

          <el-menu-item index="/booking/create" v-if="hasPermission('booking:create')">
            <el-icon><EditPen /></el-icon>
            <span>创建预订</span>
          </el-menu-item>

          <el-menu-item index="/booking/list" v-if="hasPermission('booking:list')">
            <el-icon><Document /></el-icon>
            <span>预订单管理</span>
          </el-menu-item>

          <el-menu-item index="/booking/statistics" v-if="hasPermission('booking:statistics:list')">
            <el-icon><DataLine /></el-icon>
            <span>预订统计</span>
          </el-menu-item>

          <el-menu-item index="/booking/calendar" v-if="hasPermission('booking:calendar:view')">
            <el-icon><Calendar /></el-icon>
            <span>预订日历</span>
          </el-menu-item>
        </el-sub-menu>

        <el-sub-menu index="/inventory" v-if="hasInventoryPermission">
          <template #title>
            <el-icon><Grid /></el-icon>
            <span>房量与规则</span>
          </template>

          <el-menu-item index="/inventory/pool" v-if="hasPermission('inventory:pool:list')">
            <el-icon><Calendar /></el-icon>
            <span>房量池管理</span>
          </el-menu-item>

          <el-menu-item index="/inventory/overbooking" v-if="hasPermission('inventory:overbooking:list')">
            <el-icon><Warning /></el-icon>
            <span>超售策略</span>
          </el-menu-item>

          <el-menu-item index="/inventory/monitor" v-if="hasPermission('inventory:monitor:list')">
            <el-icon><Monitor /></el-icon>
            <span>房量监控</span>
          </el-menu-item>

          <el-menu-item index="/inventory/rules" v-if="hasPermission('inventory:rule:list')">
            <el-icon><SetUp /></el-icon>
            <span>预订规则</span>
          </el-menu-item>
        </el-sub-menu>

        <el-sub-menu index="/channel" v-if="hasChannelPermission">
          <template #title>
            <el-icon><Share /></el-icon>
            <span>渠道管理</span>
          </template>

          <el-menu-item index="/channel/list" v-if="hasPermission('channel:list')">
            <el-icon><List /></el-icon>
            <span>渠道列表</span>
          </el-menu-item>

          <el-menu-item index="/channel/inventory" v-if="hasPermission('channel:inventory:list')">
            <el-icon><Grid /></el-icon>
            <span>渠道房量</span>
          </el-menu-item>

          <el-menu-item index="/channel/price" v-if="hasPermission('channel:price:list')">
            <el-icon><Money /></el-icon>
            <span>渠道价格</span>
          </el-menu-item>

          <el-menu-item index="/channel/statistics" v-if="hasPermission('channel:statistics:list')">
            <el-icon><DataLine /></el-icon>
            <span>渠道统计</span>
          </el-menu-item>
        </el-sub-menu>

        <el-sub-menu index="/analytics" v-if="hasAnalyticsPermission">
          <template #title>
            <el-icon><TrendCharts /></el-icon>
            <span>高级分析</span>
          </template>

          <el-menu-item index="/analytics/occupancy" v-if="hasPermission('analytics:occupancy:list')">
            <el-icon><DataAnalysis /></el-icon>
            <span>入住率分析</span>
          </el-menu-item>

          <el-menu-item index="/analytics/bookingCycle" v-if="hasPermission('analytics:cycle:list')">
            <el-icon><Timer /></el-icon>
            <span>预订周期</span>
          </el-menu-item>

          <el-menu-item index="/analytics/customerBehavior" v-if="hasPermission('analytics:behavior:list')">
            <el-icon><User /></el-icon>
            <span>客户行为</span>
          </el-menu-item>

          <el-menu-item index="/analytics/revenue" v-if="hasPermission('analytics:revenue:list')">
            <el-icon><Coin /></el-icon>
            <span>营收分析</span>
          </el-menu-item>
        </el-sub-menu>

        <el-sub-menu index="/visual" v-if="hasVisualPermission">
          <template #title>
            <el-icon><Calendar /></el-icon>
            <span>可视化日历</span>
          </template>

          <el-menu-item index="/visual/roomStatus" v-if="hasPermission('visual:roomStatus:list')">
            <el-icon><Calendar /></el-icon>
            <span>房态日历</span>
          </el-menu-item>

          <el-menu-item index="/visual/gantt" v-if="hasPermission('visual:gantt:list')">
            <el-icon><Histogram /></el-icon>
            <span>预订甘特图</span>
          </el-menu-item>

          <el-menu-item index="/visual/inventoryCompare" v-if="hasPermission('visual:compare:list')">
            <el-icon><DataLine /></el-icon>
            <span>房量对比</span>
          </el-menu-item>
        </el-sub-menu>

        <el-sub-menu index="/checkin" v-if="hasCheckinPermission">
          <template #title>
            <el-icon><Key /></el-icon>
            <span>入住管理</span>
          </template>

          <el-menu-item index="/checkin/list" v-if="hasPermission('checkin:list')">
            <el-icon><Document /></el-icon>
            <span>入住单管理</span>
          </el-menu-item>

          <el-menu-item index="/checkin/create" v-if="hasPermission('checkin:create')">
            <el-icon><Plus /></el-icon>
            <span>办理入住</span>
          </el-menu-item>

          <el-menu-item index="/checkin/walkin" v-if="hasPermission('checkin:walkin')">
            <el-icon><User /></el-icon>
            <span>散客入住</span>
          </el-menu-item>

          <el-menu-item index="/checkin/checkout" v-if="hasPermission('checkin:checkout')">
            <el-icon><SwitchButton /></el-icon>
            <span>办理退房</span>
          </el-menu-item>

          <el-menu-item index="/checkin/statistics" v-if="hasPermission('checkin:statistics:list')">
            <el-icon><DataLine /></el-icon>
            <span>入住统计</span>
          </el-menu-item>
        </el-sub-menu>

        <el-sub-menu index="/finance" v-if="hasFinancePermission">
          <template #title>
            <el-icon><Wallet /></el-icon>
            <span>财务管理</span>
          </template>

          <el-menu-item index="/finance/agreement" v-if="hasPermission('finance:agreement:query')">
            <el-icon><OfficeBuilding /></el-icon>
            <span>协议单位管理</span>
          </el-menu-item>

          <el-menu-item index="/finance/settlement" v-if="hasPermission('finance:settlement:query')">
            <el-icon><Wallet /></el-icon>
            <span>挂账结算</span>
          </el-menu-item>

          <el-menu-item index="/finance/refund" v-if="hasPermission('finance:refund:query')">
            <el-icon><Money /></el-icon>
            <span>退款审批</span>
          </el-menu-item>

          <el-menu-item index="/finance/daily" v-if="hasPermission('finance:daily:query')">
            <el-icon><Calendar /></el-icon>
            <span>日结对账</span>
          </el-menu-item>

          <el-menu-item index="/finance/shift" v-if="hasPermission('finance:shift:query')">
            <el-icon><SwitchButton /></el-icon>
            <span>交接班</span>
          </el-menu-item>

          <el-menu-item index="/finance/summary" v-if="hasPermission('finance:summary:query')">
            <el-icon><DataLine /></el-icon>
            <span>收款汇总</span>
          </el-menu-item>

          <el-menu-item index="/finance/receivable" v-if="hasPermission('finance:receivable:query')">
            <el-icon><Warning /></el-icon>
            <span>应收账款监控</span>
          </el-menu-item>

          <el-menu-item index="/finance/invoice" v-if="hasPermission('finance:invoice:query')">
            <el-icon><Ticket /></el-icon>
            <span>发票管理</span>
          </el-menu-item>

          <el-menu-item index="/finance/collectionDetail" v-if="hasPermission('finance:collection:query')">
            <el-icon><List /></el-icon>
            <span>收款明细报表</span>
          </el-menu-item>

          <el-menu-item index="/finance/collectionTrend" v-if="hasPermission('finance:collection:query')">
            <el-icon><TrendCharts /></el-icon>
            <span>收款趋势分析</span>
          </el-menu-item>

          <el-menu-item index="/finance/paymentMethod" v-if="hasPermission('finance:collection:query')">
            <el-icon><PieChart /></el-icon>
            <span>支付方式分析</span>
          </el-menu-item>

          <el-menu-item index="/finance/cashierStats" v-if="hasPermission('finance:cashier:query')">
            <el-icon><User /></el-icon>
            <span>收款人员统计</span>
          </el-menu-item>

          <el-menu-item index="/finance/badDebt" v-if="hasPermission('finance:badDebt:query')">
            <el-icon><Warning /></el-icon>
            <span>坏账管理</span>
          </el-menu-item>

          <el-menu-item index="/finance/revenueAnalysis" v-if="hasPermission('finance:revenue:query')">
            <el-icon><Coin /></el-icon>
            <span>营收分析</span>
          </el-menu-item>

          <el-menu-item index="/finance/receivableAnalysis" v-if="hasPermission('finance:receivable:query')">
            <el-icon><DataLine /></el-icon>
            <span>应收账款分析</span>
          </el-menu-item>

          <el-menu-item index="/finance/cashFlow" v-if="hasPermission('finance:cashflow:query')">
            <el-icon><Money /></el-icon>
            <span>现金流分析</span>
          </el-menu-item>

          <el-menu-item index="/finance/dashboard" v-if="hasPermission('finance:dashboard:query')">
            <el-icon><Monitor /></el-icon>
            <span>财务总览看板</span>
          </el-menu-item>

          <el-menu-item index="/finance/comparison" v-if="hasPermission('finance:comparison:query')">
            <el-icon><DataAnalysis /></el-icon>
            <span>对比分析</span>
          </el-menu-item>
        </el-sub-menu>

        <el-sub-menu index="/member" v-if="hasMemberPermission">
          <template #title>
            <el-icon><Stamp /></el-icon>
            <span>会员管理</span>
          </template>

          <el-menu-item index="/member/level" v-if="hasPermission('member:level:list')">
            <el-icon><Medal /></el-icon>
            <span>会员等级管理</span>
          </el-menu-item>

          <el-menu-item index="/member/list" v-if="hasPermission('member:list')">
            <el-icon><User /></el-icon>
            <span>会员列表</span>
          </el-menu-item>

          <el-menu-item index="/member/pointRule" v-if="hasPermission('member:pointRule:list')">
            <el-icon><SetUp /></el-icon>
            <span>积分规则管理</span>
          </el-menu-item>

          <el-menu-item index="/member/points" v-if="hasPermission('member:point:list')">
            <el-icon><Coin /></el-icon>
            <span>积分明细</span>
          </el-menu-item>

          <el-menu-item index="/member/statistics" v-if="hasPermission('member:statistics:list')">
            <el-icon><DataLine /></el-icon>
            <span>会员统计</span>
          </el-menu-item>

          <el-menu-item index="/member/pointStatistics" v-if="hasPermission('member:pointStatistics:list')">
            <el-icon><DataAnalysis /></el-icon>
            <span>积分统计</span>
          </el-menu-item>

          <el-menu-item index="/member/benefitLog" v-if="hasPermission('member:benefit:log:list')">
            <el-icon><Tickets /></el-icon>
            <span>权益使用记录</span>
          </el-menu-item>

          <el-menu-item index="/member/levelChangeStatistics" v-if="hasPermission('member:levelChange:statistics')">
            <el-icon><TrendCharts /></el-icon>
            <span>升降级统计</span>
          </el-menu-item>

          <el-menu-item index="/member/taskManage" v-if="hasPermission('member:task:list')">
            <el-icon><Timer /></el-icon>
            <span>定时任务管理</span>
          </el-menu-item>

          <el-menu-item index="/member/levelChangeLog" v-if="hasPermission('member:levelLog:list')">
            <el-icon><Tickets /></el-icon>
            <span>等级变更记录</span>
          </el-menu-item>
        </el-sub-menu>
      </el-menu>
    </el-aside>
    
    <!-- 主内容区 -->
    <el-container class="layout-main">
      <!-- 顶部导航 -->
      <el-header class="layout-header">
        <div class="header-left">
          <el-icon class="collapse-btn" @click="toggleCollapse">
            <Expand v-if="isCollapse" />
            <Fold v-else />
          </el-icon>
          <el-breadcrumb separator="/">
            <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
            <el-breadcrumb-item v-if="$route.meta.title && $route.path !== '/dashboard'">
              {{ $route.meta.title }}
            </el-breadcrumb-item>
          </el-breadcrumb>
        </div>
        
        <div class="header-right">
          <el-dropdown @command="handleCommand">
            <span class="user-info">
              <el-avatar :size="32" :src="userStore.user?.avatar">
                {{ userStore.user?.nickname?.charAt(0) || 'U' }}
              </el-avatar>
              <span class="nickname">{{ userStore.user?.nickname || '用户' }}</span>
              <el-icon><ArrowDown /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">
                  <el-icon><User /></el-icon>个人中心
                </el-dropdown-item>
                <el-dropdown-item divided command="logout">
                  <el-icon><SwitchButton /></el-icon>退出登录
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>
      
      <!-- 内容区 -->
      <el-main class="layout-content">
        <router-view v-slot="{ Component }">
          <transition name="fade" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </el-main>
    </el-container>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessageBox, ElMessage } from 'element-plus'
import {
  HomeFilled, Setting, User, Avatar, Menu, Lock,
  Expand, Fold, ArrowDown, SwitchButton,
  House, OfficeBuilding, School, Tickets, Key,
  Tools, Document, Edit, Clock, DataLine,
  UserFilled, Plus, PriceTag, Warning, Checked, CopyDocument, Upload,
  Calendar, Search, EditPen, Grid, Monitor, SetUp, Share, List, Money,
  TrendCharts, Timer, Coin, Histogram, Wallet, Ticket, PieChart,
  Stamp, Medal, DataAnalysis
} from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const isCollapse = ref(false)

const activeMenu = computed(() => route.path)

const hasPermission = (permission) => {
  return userStore.hasPermission(permission)
}

const hasSystemPermission = computed(() => {
  return hasPermission('system:user:list') ||
         hasPermission('system:role:list') ||
         hasPermission('system:menu:list') ||
         hasPermission('system:dataPerm:list')
})

const hasHotelPermission = computed(() => {
  return hasPermission('hotel:info:list') ||
         hasPermission('hotel:building:list') ||
         hasPermission('hotel:roomType:list') ||
         hasPermission('hotel:room:list') ||
         hasPermission('hotel:dashboard:list')
})

const hasMaintenancePermission = computed(() => {
  return hasPermission('maintenance:order:list') ||
         hasPermission('maintenance:order:add') ||
         hasPermission('maintenance:changeLog:list') ||
         hasPermission('maintenance:statistics:list')
})

const hasCustomerPermission = computed(() => {
  return hasPermission('customer:list') ||
         hasPermission('customer:add') ||
         hasPermission('customer:tag:list') ||
         hasPermission('customer:blacklist:list') ||
         hasPermission('customer:blacklist:approve') ||
         hasPermission('customer:merge') ||
         hasPermission('customer:import')
})

const hasBookingPermission = computed(() => {
  return hasPermission('booking:roomQuery:list') ||
         hasPermission('booking:create') ||
         hasPermission('booking:list') ||
         hasPermission('booking:statistics:list') ||
         hasPermission('booking:calendar:view')
})

const hasInventoryPermission = computed(() => {
  return hasPermission('inventory:pool:list') ||
         hasPermission('inventory:overbooking:list') ||
         hasPermission('inventory:monitor:list') ||
         hasPermission('inventory:rule:list')
})

const hasChannelPermission = computed(() => {
  return hasPermission('channel:list') ||
         hasPermission('channel:inventory:list') ||
         hasPermission('channel:price:list') ||
         hasPermission('channel:statistics:list')
})

const hasAnalyticsPermission = computed(() => {
  return hasPermission('analytics:occupancy:list') ||
         hasPermission('analytics:cycle:list') ||
         hasPermission('analytics:behavior:list') ||
         hasPermission('analytics:revenue:list')
})

const hasVisualPermission = computed(() => {
  return hasPermission('visual:roomStatus:list') ||
         hasPermission('visual:gantt:list') ||
         hasPermission('visual:compare:list')
})

const hasCheckinPermission = computed(() => {
  return hasPermission('checkin:list') ||
         hasPermission('checkin:create') ||
         hasPermission('checkin:walkin') ||
         hasPermission('checkin:checkout') ||
         hasPermission('checkin:statistics:list')
})

const hasFinancePermission = computed(() => {
  return hasPermission('finance:agreement:query') ||
         hasPermission('finance:settlement:query') ||
         hasPermission('finance:refund:query') ||
         hasPermission('finance:daily:query') ||
         hasPermission('finance:shift:query') ||
         hasPermission('finance:summary:query') ||
         hasPermission('finance:receivable:query') ||
         hasPermission('finance:invoice:query') ||
         hasPermission('finance:collection:query') ||
         hasPermission('finance:cashier:query') ||
         hasPermission('finance:badDebt:query') ||
         hasPermission('finance:revenue:query') ||
         hasPermission('finance:cashflow:query') ||
         hasPermission('finance:dashboard:query') ||
         hasPermission('finance:comparison:query')
})

const hasMemberPermission = computed(() => {
  return hasPermission('member:level:list') ||
         hasPermission('member:list') ||
         hasPermission('member:pointRule:list') ||
         hasPermission('member:point:list') ||
         hasPermission('member:statistics:list') ||
         hasPermission('member:pointStatistics:list') ||
         hasPermission('member:benefit:log:list')
})

const toggleCollapse = () => {
  isCollapse.value = !isCollapse.value
}

const handleCommand = async (command) => {
  if (command === 'profile') {
    router.push('/profile')
  } else if (command === 'logout') {
    try {
      await ElMessageBox.confirm('确定要退出登录吗？', '提示', {
        type: 'warning'
      })
      userStore.logout()
      router.push('/login')
      ElMessage.success('已退出登录')
    } catch {
      // 用户取消
    }
  }
}
</script>

<style scoped>
.layout-container {
  display: flex;
  min-height: 100vh;
}

.layout-aside {
  background-color: #304156;
  transition: width 0.3s;
  overflow: hidden;
}

.logo {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 18px;
  font-weight: bold;
  background-color: #263445;
}

.layout-main {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.layout-header {
  height: 60px;
  background: #fff;
  box-shadow: 0 1px 4px rgba(0, 21, 41, 0.08);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 15px;
}

.collapse-btn {
  font-size: 20px;
  cursor: pointer;
  color: #606266;
}

.collapse-btn:hover {
  color: #409EFF;
}

.header-right {
  display: flex;
  align-items: center;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  color: #606266;
}

.nickname {
  max-width: 100px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.layout-content {
  background-color: #f0f2f5;
  padding: 20px;
  overflow-y: auto;
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.2s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

:deep(.el-menu) {
  border-right: none;
}
</style>
