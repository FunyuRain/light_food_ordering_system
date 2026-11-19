import { createRouter, createWebHashHistory } from 'vue-router'

const TOKEN_KEY = 'lf_admin_token'

const routes = [
  {
    path: '/login',
    name: 'login',
    component: () => import('@/views/LoginView.vue'),
    meta: { title: '管理员登录', guest: true }
  },
  {
    path: '/',
    component: () => import('@/layouts/AdminLayout.vue'),
    redirect: '/orders',
    meta: { requiresAuth: true },
    children: [
      {
        path: 'orders',
        name: 'orders',
        component: () => import('@/views/OrderView.vue'),
        meta: { title: '订单管理' }
      },
      {
        path: 'dishes',
        name: 'dishes',
        component: () => import('@/views/DishView.vue'),
        meta: { title: '菜品管理' }
      },
      {
        path: 'categories',
        name: 'categories',
        component: () => import('@/views/CategoryView.vue'),
        meta: { title: '分类管理' }
      }
    ]
  },
  { path: '/:pathMatch(.*)*', redirect: '/orders' }
]

const router = createRouter({
  history: createWebHashHistory(),
  routes
})

router.beforeEach((to) => {
  document.title = `${to.meta.title || '轻食管理台'} · Light Food`
  const token = localStorage.getItem(TOKEN_KEY)
  if (to.meta.requiresAuth && !token) {
    return { path: '/login', query: { redirect: to.fullPath } }
  }
  if (to.meta.guest && token) {
    return '/orders'
  }
  return true
})

export default router
