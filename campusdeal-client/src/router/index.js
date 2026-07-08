import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/',
    name: 'Index',
    component: () => import('@/views/Index.vue')
  },
  {
    path: '/products',
    name: 'Products',
    component: () => import('@/views/Products.vue')
  },
  {
    path: '/products/:id',
    name: 'ProductDetail',
    component: () => import('@/views/ProductDetail.vue')
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue')
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/views/Register.vue')
  },
  {
    path: '/publish',
    name: 'Publish',
    component: () => import('@/views/Publish.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/cart',
    name: 'Cart',
    component: () => import('@/views/Cart.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/orders',
    name: 'Orders',
    component: () => import('@/views/Orders.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/orders/:id',
    name: 'OrderDetail',
    component: () => import('@/views/OrderDetail.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/review/:orderId',
    name: 'ReviewPage',
    component: () => import('@/views/ReviewPage.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/review-detail',
    name: 'ReviewDetail',
    component: () => import('@/views/ReviewDetail.vue')
  },
  {
    path: '/favorites',
    name: 'Favorites',
    component: () => import('@/views/Favorites.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/messages',
    name: 'Messages',
    component: () => import('@/views/Messages.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/chat/:conversationId',
    name: 'Chat',
    component: () => import('@/views/Chat.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/admin-chat',
    name: 'AdminChat',
    component: () => import('@/views/AdminChat.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/profile',
    name: 'Profile',
    component: () => import('@/views/Profile.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/settings',
    name: 'Settings',
    component: () => import('@/views/Settings.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/settings/profile',
    name: 'SettingsProfile',
    component: () => import('@/views/SettingsProfile.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/settings/account',
    name: 'SettingsAccount',
    component: () => import('@/views/SettingsAccount.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/settings/address',
    name: 'SettingsAddress',
    component: () => import('@/views/SettingsAddress.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/my-products',
    name: 'MyProducts',
    component: () => import('@/views/MyProducts.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/credit-detail',
    name: 'CreditDetail',
    component: () => import('@/views/CreditDetail.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/admin',
    component: () => import('@/views/admin/AdminLayout.vue'),
    meta: { requiresAdmin: true },
    children: [
      {
        path: '',
        redirect: '/admin/dashboard'
      },
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/admin/Dashboard.vue')
      },
      {
        path: 'products',
        name: 'ProductManage',
        component: () => import('@/views/admin/ProductManage.vue')
      },
      {
        path: 'orders',
        name: 'OrderManage',
        component: () => import('@/views/admin/OrderManage.vue')
      },
      {
        path: 'users',
        name: 'UserManage',
        component: () => import('@/views/admin/UserManage.vue')
      },
      {
        path: 'announcements',
        name: 'AnnouncementManage',
        component: () => import('@/views/admin/AnnouncementManage.vue')
      },
      {
        path: 'logs',
        name: 'SystemLog',
        component: () => import('@/views/admin/SystemLog.vue')
      },
      {
        path: 'messages',
        name: 'MessageManage',
        component: () => import('@/views/admin/MessageManage.vue')
      },
      {
        path: 'messages/system/:userId',
        name: 'SystemChat',
        component: () => import('@/views/admin/SystemChat.vue')
      },
      {
        path: 'credits',
        name: 'CreditManage',
        component: () => import('@/views/admin/CreditManage.vue')
      },
      {
        path: 'basic-config',
        name: 'BasicConfig',
        component: () => import('@/views/admin/BasicConfig.vue')
      },
      {
        path: 'credit-rules',
        name: 'CreditRuleManage',
        component: () => import('@/views/admin/CreditRuleManage.vue')
      },
      {
        path: 'order-rules',
        name: 'OrderRuleManage',
        component: () => import('@/views/admin/OrderRuleManage.vue')
      },
      {
        path: 'sensitive-words',
        name: 'SensitiveWordManage',
        component: () => import('@/views/admin/SensitiveWordManage.vue')
      },
      {
        path: 'banners',
        name: 'BannerManage',
        component: () => import('@/views/admin/BannerManage.vue')
      },
      {
        path: 'orders/:id',
        name: 'AdminOrderDetail',
        component: () => import('@/views/admin/OrderDetail.vue')
      },
      {
        path: 'credit-records',
        name: 'CreditRecords',
        component: () => import('@/views/admin/CreditRecords.vue')
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior() {
    return { top: 0 }
  }
})

router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')
  const adminToken = localStorage.getItem('adminToken')
  const adminRole = localStorage.getItem('adminRole')

  // 需要普通用户认证的路由
  if (to.meta.requiresAuth && !token) {
    next('/login?redirect=' + encodeURIComponent(to.fullPath))
    return
  }

  // 已登录用户进入需认证页面时，校验账号是否被封禁（后端对封禁账号返回 403）
  if (to.meta.requiresAuth && token) {
    fetch('/api/v1/auth/me', {
      headers: { Authorization: 'Bearer ' + token }
    }).then(res => {
      if (res.status === 403) {
        localStorage.removeItem('token')
        localStorage.removeItem('refreshToken')
        localStorage.removeItem('userInfo')
        localStorage.removeItem('userRole')
        next('/login?banned=1')
      } else {
        next()
      }
    }).catch(() => {
      next()
    })
    return
  }

  // 需要管理员认证的路由
  if (to.meta.requiresAdmin) {
    if (!adminToken || adminRole !== 'admin') {
      if (adminToken) {
        localStorage.removeItem('adminToken')
        localStorage.removeItem('adminInfo')
        localStorage.removeItem('adminRole')
      }
      next('/login?loginType=admin')
      return
    }
    next()
    return
  }

  // 登录/注册页面：根据已登录态智能跳转
  if (to.path === '/login' || to.path === '/register') {
    const loginType = to.query.loginType
    if (loginType === 'admin') {
      // 访问管理员登录页：如果管理员已登录则跳转后台
      if (adminToken && adminRole === 'admin') {
        next('/admin/dashboard')
        return
      }
      // 管理员未登录，允许进入管理员登录页（即使普通用户已登录也不拦截）
      next()
      return
    }
    // 访问普通登录/注册页：如果普通用户已登录则跳转首页
    if (token) {
      next('/')
      return
    }
    next()
    return
  }

  next()
})

export default router
