import Vue from 'vue'
import VueRouter from 'vue-router'

Vue.use(VueRouter)

const routes = [
  { path: '/', redirect: '/login' },
  { path: '/home', component: () => import('../views/Home.vue') },
  { path: '/search', component: () => import('../views/Search.vue') },
  { path: '/movie/:id', component: () => import('../views/MovieDetail.vue') },
  { path: '/collection', component: () => import('../views/Collection.vue'), meta: { requiresAuth: true } },
  { path: '/collections/manage', component: () => import('../views/CollectionManager.vue'), meta: { requiresAuth: true } },
  { path: '/tags', component: () => import('../views/TagManager.vue'), meta: { requiresAuth: true } },
  { path: '/dislike', component: () => import('../views/Dislike.vue'), meta: { requiresAuth: true } },
  { path: '/login', component: () => import('../views/Login.vue') },
  { path: '/register', component: () => import('../views/Register.vue') },
  { path: '/admin', component: () => import('../views/Admin.vue'), meta: { requiresAdmin: true } },
  { path: '/admin/behavior', component: () => import('../views/admin/BehaviorLogs.vue'), meta: { requiresAdmin: true } },
  { path: '/user/profile', component: () => import('../views/UserProfile.vue'), meta: { requiresAuth: true } },
  { path: '/user/change-password', component: () => import('../views/ChangePassword.vue'), meta: { requiresAuth: true } },
  { path: '/notes', component: () => import('../views/Notes.vue'), meta: { requiresAuth: true } },
  { path: '/user/preference', component: () => import('../views/UserPreference.vue'), meta: { requiresAuth: true } },
  // 新增：推荐算法分析页面
  { path: '/recommend/analysis', component: () => import('../views/RecommendAnalysis.vue'), meta: { requiresAuth: true } }
]

const router = new VueRouter({ mode: 'history', routes })

router.beforeEach((to, from, next) => {
  const user = JSON.parse(localStorage.getItem('user') || 'null')
  
  // 需要登录但未登录
  if (to.meta.requiresAuth && !user) {
    // 保存目标路径，登录后跳转
    localStorage.setItem('redirectPath', to.fullPath)
    next('/login')
    return
  }
  
  // 需要管理员权限
  if (to.meta.requiresAdmin && (!user || user.role !== 'ADMIN')) {
    next('/login')
    return
  }
  
  next()
})

export default router
