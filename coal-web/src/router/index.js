import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/login/LoginView.vue')
  },
  {
    path: '/',
    component: () => import('../layout/Layout.vue'),
    redirect: '/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('../views/dashboard/HomeView.vue'),
        meta: { title: '工作台' }
      },
      // ---- 客户与合同 ----
      {
        path: 'biz/customer',
        name: 'Customer',
        component: () => import('../views/biz/customer/index.vue'),
        meta: { title: '客户管理' }
      },
      {
        path: 'biz/contract',
        name: 'Contract',
        component: () => import('../views/biz/contract/index.vue'),
        meta: { title: '合同管理' }
      },
      // ---- 销售订单 ----
      {
        path: 'order/list',
        name: 'SaleOrder',
        component: () => import('../views/order/list/index.vue'),
        meta: { title: '订单管理' }
      },
      // ---- 称重发运 ----
      {
        path: 'weigh/ticket',
        name: 'WeighTicket',
        component: () => import('../views/weigh/ticket/index.vue'),
        meta: { title: '磅单录入' }
      },
      // ---- 产品与库存 ----
      {
        path: 'stock/product',
        name: 'Product',
        component: () => import('../views/stock/product/index.vue'),
        meta: { title: '产品管理' }
      },
      {
        path: 'stock/query',
        name: 'StockQuery',
        component: () => import('../views/stock/query/index.vue'),
        meta: { title: '库存查询' }
      },
      {
        path: 'stock/record',
        name: 'StockRecord',
        component: () => import('../views/stock/record/index.vue'),
        meta: { title: '库存流水' }
      },
      {
        path: 'stock/check',
        name: 'StockCheck',
        component: () => import('../views/stock/check/index.vue'),
        meta: { title: '库存盘点' }
      },
      // ---- 结算与财务 ----
      {
        path: 'finance/settle',
        name: 'Settlement',
        component: () => import('../views/finance/settle/index.vue'),
        meta: { title: '结算管理' }
      },
      {
        path: 'finance/payment',
        name: 'Payment',
        component: () => import('../views/finance/payment/index.vue'),
        meta: { title: '回款管理' }
      },
      // ---- 其他模块页面后续按同样方式逐个补充 ----
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 登录守卫：未登录跳转登录页
router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')
  if (to.path === '/login') {
    next()
  } else if (!token) {
    next('/login')
  } else {
    next()
  }
})

export default router
