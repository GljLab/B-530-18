import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/stores/user'

// 静态路由
const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue'),
    meta: { title: '登录', requiresAuth: false }
  },
  {
    path: '/',
    name: 'Layout',
    component: () => import('@/views/Layout.vue'),
    redirect: '/dashboard',
    meta: { requiresAuth: true },
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/Dashboard.vue'),
        meta: { title: '首页', icon: 'HomeFilled' }
      },
      {
        path: 'profile',
        name: 'Profile',
        component: () => import('@/views/Profile.vue'),
        meta: { title: '个人中心', icon: 'User' }
      },
      {
        path: 'system/user',
        name: 'SystemUser',
        component: () => import('@/views/system/User.vue'),
        meta: { title: '用户管理', icon: 'User', permission: 'system:user:list' }
      },
      {
        path: 'system/role',
        name: 'SystemRole',
        component: () => import('@/views/system/Role.vue'),
        meta: { title: '角色管理', icon: 'Avatar', permission: 'system:role:list' }
      },
      {
        path: 'system/menu',
        name: 'SystemMenu',
        component: () => import('@/views/system/Menu.vue'),
        meta: { title: '菜单管理', icon: 'Menu', permission: 'system:menu:list' }
      },
      {
        path: 'system/dataPerm',
        name: 'SystemDataPerm',
        component: () => import('@/views/system/DataPermission.vue'),
        meta: { title: '数据权限', icon: 'Lock', permission: 'system:dataPerm:list' }
      },
      {
        path: 'hotel/overview',
        name: 'HotelOverview',
        component: () => import('@/views/hotel/HotelOverview.vue'),
        meta: { title: '酒店概览', icon: 'OfficeBuilding', permission: 'hotel:info:list' }
      },
      {
        path: 'hotel/building',
        name: 'BuildingFloor',
        component: () => import('@/views/hotel/BuildingFloor.vue'),
        meta: { title: '楼栋楼层', icon: 'School', permission: 'hotel:building:list' }
      },
      {
        path: 'hotel/roomType',
        name: 'RoomType',
        component: () => import('@/views/hotel/RoomType.vue'),
        meta: { title: '房型管理', icon: 'Tickets', permission: 'hotel:roomType:list' }
      },
      {
        path: 'hotel/room',
        name: 'RoomManage',
        component: () => import('@/views/hotel/RoomManage.vue'),
        meta: { title: '房间管理', icon: 'Key', permission: 'hotel:room:list' }
      },
      {
        path: 'hotel/room/:id',
        name: 'RoomDetail',
        component: () => import('@/views/hotel/RoomDetail.vue'),
        meta: { title: '房间详情', icon: 'Key', permission: 'hotel:room:query' }
      },
      {
        path: 'hotel/floorPermission',
        name: 'FloorPermission',
        component: () => import('@/views/hotel/FloorPermission.vue'),
        meta: { title: '楼层权限配置', icon: 'Key', permission: 'hotel:floorPermission:list' }
      },
      {
        path: 'hotel/batchOperation',
        name: 'BatchOperation',
        component: () => import('@/views/hotel/BatchOperation.vue'),
        meta: { title: '批量操作日志', icon: 'List', permission: 'hotel:batchOperation:list' }
      },
      {
        path: 'hotel/dashboard',
        name: 'HotelDashboard',
        component: () => import('@/views/hotel/HotelDashboard.vue'),
        meta: { title: '统计看板', icon: 'DataAnalysis', permission: 'hotel:dashboard:list' }
      },
      {
        path: 'maintenance/order',
        name: 'MaintenanceOrderList',
        component: () => import('@/views/maintenance/MaintenanceOrderList.vue'),
        meta: { title: '维护单管理', icon: 'Document', permission: 'maintenance:order:list' }
      },
      {
        path: 'maintenance/order/create',
        name: 'MaintenanceOrderCreate',
        component: () => import('@/views/maintenance/MaintenanceOrderCreate.vue'),
        meta: { title: '创建维护单', icon: 'Edit', permission: 'maintenance:order:add' }
      },
      {
        path: 'maintenance/order/:id',
        name: 'MaintenanceOrderDetail',
        component: () => import('@/views/maintenance/MaintenanceOrderDetail.vue'),
        meta: { title: '维护单详情', icon: 'Document', permission: 'maintenance:order:query' }
      },
      {
        path: 'maintenance/changeLog',
        name: 'RoomChangeLog',
        component: () => import('@/views/maintenance/RoomChangeLog.vue'),
        meta: { title: '房间变更日志', icon: 'Clock', permission: 'maintenance:changeLog:list' }
      },
      {
        path: 'maintenance/statistics',
        name: 'MaintenanceStatistics',
        component: () => import('@/views/maintenance/MaintenanceStatistics.vue'),
        meta: { title: '维护统计报表', icon: 'DataLine', permission: 'maintenance:statistics:list' }
      },
      {
        path: 'customer/list',
        name: 'CustomerList',
        component: () => import('@/views/customer/CustomerList.vue'),
        meta: { title: '客户列表', icon: 'User', permission: 'customer:list' }
      },
      {
        path: 'customer/create',
        name: 'CustomerCreate',
        component: () => import('@/views/customer/CustomerCreate.vue'),
        meta: { title: '新增客户', icon: 'Plus', permission: 'customer:add' }
      },
      {
        path: 'customer/detail/:id',
        name: 'CustomerDetail',
        component: () => import('@/views/customer/CustomerDetail.vue'),
        meta: { title: '客户详情', icon: 'View', permission: 'customer:query' }
      },
      {
        path: 'customer/edit/:id',
        name: 'CustomerEdit',
        component: () => import('@/views/customer/CustomerEdit.vue'),
        meta: { title: '编辑客户', icon: 'Edit', permission: 'customer:edit' }
      },
      {
        path: 'customer/tags',
        name: 'TagManagement',
        component: () => import('@/views/customer/TagManagement.vue'),
        meta: { title: '标签管理', icon: 'PriceTag', permission: 'customer:tag:list' }
      },
      {
        path: 'customer/blacklist',
        name: 'BlacklistManagement',
        component: () => import('@/views/customer/BlacklistManagement.vue'),
        meta: { title: '黑名单管理', icon: 'Warning', permission: 'customer:blacklist:list' }
      },
      {
        path: 'customer/blacklist/detail/:id',
        name: 'BlacklistDetail',
        component: () => import('@/views/customer/BlacklistDetail.vue'),
        meta: { title: '黑名单详情', icon: 'Warning', permission: 'customer:blacklist:list', hidden: true }
      },
      {
        path: 'customer/blacklistApproval',
        name: 'BlacklistApproval',
        component: () => import('@/views/customer/BlacklistApproval.vue'),
        meta: { title: '黑名单审批', icon: 'Checked', permission: 'customer:blacklist:approve' }
      },
      {
        path: 'customer/merge',
        name: 'CustomerMerge',
        component: () => import('@/views/customer/CustomerMerge.vue'),
        meta: { title: '客户合并', icon: 'CopyDocument', permission: 'customer:merge' }
      },
      {
        path: 'customer/import',
        name: 'CustomerImport',
        component: () => import('@/views/customer/CustomerImport.vue'),
        meta: { title: '批量导入', icon: 'Upload', permission: 'customer:import' }
      },
      {
        path: 'booking/roomQuery',
        name: 'BookingRoomQuery',
        component: () => import('@/views/booking/RoomQuery.vue'),
        meta: { title: '房源查询', icon: 'Search', permission: 'booking:roomQuery:list' }
      },
      {
        path: 'booking/create',
        name: 'BookingCreate',
        component: () => import('@/views/booking/BookingCreate.vue'),
        meta: { title: '创建预订', icon: 'Plus', permission: 'booking:create' }
      },
      {
        path: 'booking/list',
        name: 'BookingList',
        component: () => import('@/views/booking/BookingList.vue'),
        meta: { title: '预订单列表', icon: 'List', permission: 'booking:list' }
      },
      {
        path: 'booking/detail/:id',
        name: 'BookingDetail',
        component: () => import('@/views/booking/BookingDetail.vue'),
        meta: { title: '预订单详情', icon: 'Document', permission: 'booking:query' }
      },
      {
        path: 'booking/statistics',
        name: 'BookingStatistics',
        component: () => import('@/views/booking/BookingStatistics.vue'),
        meta: { title: '预订统计', icon: 'DataLine', permission: 'booking:statistics:list' }
      },
      {
        path: 'booking/calendar',
        name: 'BookingCalendar',
        component: () => import('@/views/booking/BookingCalendar.vue'),
        meta: { title: '预订日历', icon: 'Calendar', permission: 'booking:calendar:view' }
      },
      {
        path: 'booking/refund',
        name: 'RefundManage',
        component: () => import('@/views/booking/RefundManage.vue'),
        meta: { title: '退款管理', icon: 'Money', permission: 'booking:refund:approve' }
      },
      {
        path: 'inventory/pool',
        name: 'InventoryPoolManage',
        component: () => import('@/views/booking/InventoryPoolManage.vue'),
        meta: { title: '房量池管理', icon: 'Calendar', permission: 'inventory:pool:list' }
      },
      {
        path: 'inventory/overbooking',
        name: 'OverbookingStrategy',
        component: () => import('@/views/booking/OverbookingStrategy.vue'),
        meta: { title: '超售策略', icon: 'Warning', permission: 'inventory:overbooking:list' }
      },
      {
        path: 'inventory/monitor',
        name: 'InventoryMonitor',
        component: () => import('@/views/booking/InventoryMonitor.vue'),
        meta: { title: '房量监控', icon: 'Monitor', permission: 'inventory:monitor:list' }
      },
      {
        path: 'inventory/rules',
        name: 'BookingRuleManage',
        component: () => import('@/views/booking/BookingRuleManage.vue'),
        meta: { title: '预订规则', icon: 'SetUp', permission: 'inventory:rule:list' }
      },
      {
        path: 'channel/list',
        name: 'ChannelManage',
        component: () => import('@/views/booking/ChannelManage.vue'),
        meta: { title: '渠道列表', icon: 'List', permission: 'channel:list' }
      },
      {
        path: 'channel/inventory',
        name: 'ChannelInventory',
        component: () => import('@/views/booking/ChannelInventory.vue'),
        meta: { title: '渠道房量', icon: 'Grid', permission: 'channel:inventory:list' }
      },
      {
        path: 'channel/price',
        name: 'ChannelPrice',
        component: () => import('@/views/booking/ChannelPrice.vue'),
        meta: { title: '渠道价格', icon: 'Money', permission: 'channel:price:list' }
      },
      {
        path: 'channel/statistics',
        name: 'ChannelStats',
        component: () => import('@/views/booking/ChannelStats.vue'),
        meta: { title: '渠道统计', icon: 'DataLine', permission: 'channel:statistics:list' }
      },
      {
        path: 'analytics/occupancy',
        name: 'OccupancyAnalysis',
        component: () => import('@/views/booking/OccupancyAnalysis.vue'),
        meta: { title: '入住率分析', icon: 'DataAnalysis', permission: 'analytics:occupancy:list' }
      },
      {
        path: 'analytics/bookingCycle',
        name: 'BookingCycleAnalysis',
        component: () => import('@/views/booking/BookingCycleAnalysis.vue'),
        meta: { title: '预订周期', icon: 'Timer', permission: 'analytics:cycle:list' }
      },
      {
        path: 'analytics/customerBehavior',
        name: 'CustomerBehaviorAnalysis',
        component: () => import('@/views/booking/CustomerBehaviorAnalysis.vue'),
        meta: { title: '客户行为', icon: 'User', permission: 'analytics:behavior:list' }
      },
      {
        path: 'analytics/revenue',
        name: 'RevenueAnalysis',
        component: () => import('@/views/booking/RevenueAnalysis.vue'),
        meta: { title: '营收分析', icon: 'Coin', permission: 'analytics:revenue:list' }
      },
      {
        path: 'visual/roomStatus',
        name: 'RoomStatusCalendar',
        component: () => import('@/views/booking/RoomStatusCalendar.vue'),
        meta: { title: '房态日历', icon: 'Calendar', permission: 'visual:roomStatus:list' }
      },
      {
        path: 'visual/gantt',
        name: 'BookingGantt',
        component: () => import('@/views/booking/BookingGantt.vue'),
        meta: { title: '预订甘特图', icon: 'Histogram', permission: 'visual:gantt:list' }
      },
      {
        path: 'visual/inventoryCompare',
        name: 'InventoryCompareCalendar',
        component: () => import('@/views/booking/InventoryCompareCalendar.vue'),
        meta: { title: '房量对比', icon: 'DataLine', permission: 'visual:compare:list' }
      },
      {
        path: 'checkin/list',
        name: 'CheckInList',
        component: () => import('@/views/checkin/CheckInList.vue'),
        meta: { title: '入住单管理', icon: 'Document', permission: 'checkin:list' }
      },
      {
        path: 'checkin/create',
        name: 'CheckInCreate',
        component: () => import('@/views/checkin/CheckInCreate.vue'),
        meta: { title: '办理入住', icon: 'Plus', permission: 'checkin:create' }
      },
      {
        path: 'checkin/walkin',
        name: 'WalkInCreate',
        component: () => import('@/views/checkin/WalkInCreate.vue'),
        meta: { title: '散客入住', icon: 'User', permission: 'checkin:walkin' }
      },
      {
        path: 'checkin/checkout',
        name: 'CheckOutPage',
        component: () => import('@/views/checkin/CheckOut.vue'),
        meta: { title: '办理退房', icon: 'SwitchButton', permission: 'checkin:checkout' }
      },
      {
        path: 'checkin/detail/:id',
        name: 'CheckInDetail',
        component: () => import('@/views/checkin/CheckInDetail.vue'),
        meta: { title: '入住单详情', icon: 'Document', permission: 'checkin:query', hidden: true }
      },
      {
        path: 'checkin/statistics',
        name: 'CheckInStatistics',
        component: () => import('@/views/checkin/CheckInStatistics.vue'),
        meta: { title: '入住统计', icon: 'DataLine', permission: 'checkin:statistics:list' }
      }
    ]
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: () => import('@/views/404.vue'),
    meta: { title: '404', requiresAuth: false }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫
router.beforeEach(async (to, from, next) => {
  // 设置页面标题
  document.title = to.meta.title ? `${to.meta.title} - 智慧酒店管理平台` : '智慧酒店管理平台'
  
  const userStore = useUserStore()
  const token = userStore.token
  
  // 不需要认证的页面
  if (to.meta.requiresAuth === false) {
    if (token && to.path === '/login') {
      next('/')
    } else {
      next()
    }
    return
  }
  
  // 需要认证的页面
  if (!token) {
    next('/login')
    return
  }
  
  // 检查用户信息是否已加载
  if (!userStore.user) {
    try {
      await userStore.getUserInfo()
    } catch (error) {
      userStore.logout()
      next('/login')
      return
    }
  }
  
  // 检查权限
  if (to.meta.permission) {
    const hasPermission = userStore.hasPermission(to.meta.permission)
    if (!hasPermission) {
      next('/dashboard')
      return
    }
  }
  
  next()
})

export default router
