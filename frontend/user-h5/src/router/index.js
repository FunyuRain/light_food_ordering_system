import { createRouter, createWebHashHistory } from 'vue-router'

const TOKEN_KEY = 'lf_user_token'

const routes = [
  {
    path: '/login',
    name: 'login',
    component: () => import('@/views/LoginView.vue'),
    meta: { title: '登录', guest: true }
  },
  {
    path: '/',
    component: () => import('@/layouts/UserLayout.vue'),
    redirect: '/menu',
    meta: { requiresAuth: true },
    children: [
      {
        path: 'menu',
        name: 'menu',
        component: () => import('@/views/MenuView.vue'),
        meta: { title: '点餐', tabbar: true }
      },
      {
        path: 'dishes/:id',
        name: 'dish-detail',
        component: () => import('@/views/DishDetailView.vue'),
        meta: { title: '菜品详情' }
      },
      {
        path: 'cart',
        name: 'cart',
        component: () => import('@/views/CartView.vue'),
        meta: { title: '购物车', tabbar: true }
      },
      {
        path: 'orders',
        name: 'orders',
        component: () => import('@/views/OrderListView.vue'),
        meta: { title: '我的订单', tabbar: true }
      },
      {
        path: 'orders/:id',
        name: 'order-detail',
        component: () => import('@/views/OrderDetailView.vue'),
        meta: { title: '订单详情' }
      },
      {
        path: 'address',
        name: 'address',
        component: () => import('@/views/AddressView.vue'),
        meta: { title: '收货地址', tabbar: true }
      },
      {
        path: 'checkout',
        name: 'checkout',
        component: () => import('@/views/CheckoutView.vue'),
        meta: { title: '确认订单' }
      }
    ]
  },
  { path: '/:pathMatch(.*)*', redirect: '/menu' }
]

const router = createRouter({
  history: createWebHashHistory(),
  routes,
  scrollBehavior() {
    return { top: 0 }
  }
})

router.beforeEach((to) => {
  document.title = `${to.meta.title || '轻食点单'} · Light Food`
  const token = localStorage.getItem(TOKEN_KEY)
  if (to.meta.requiresAuth && !token) {
    return { path: '/login', query: { redirect: to.fullPath } }
  }
  if (to.meta.guest && token) {
    return '/menu'
  }
  return true
})

export default router
