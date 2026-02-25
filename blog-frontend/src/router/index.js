import { createRouter, createWebHistory } from 'vue-router'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      redirect: '/login'
    },
    {
      path: '/login',
      name: 'login',
      component: () => import('../views/Login.vue')
    },
    {
      path: '/register',
      name: 'register',
      component: () => import('../views/Register.vue')
    },
    {
      path: '/home',
      name: 'home',
      component: () => import('../views/Home.vue')
    },
    {
      path: '/blog/:id',
      name: 'blog-detail',
      component: () => import('../views/BlogDetail.vue')
    },
    {
      path: '/user',
      name: 'user-center',
      component: () => import('../views/UserCenter.vue')
    }
  ]
})

// ✨✨ 路由守卫：防止未登录访问 ✨✨
router.beforeEach((to, from, next) => {
  const user = localStorage.getItem('user')
  
  // 简单的白名单逻辑：除了登录和注册，其他页面都需要登录
  if (to.name !== 'login' && to.name !== 'register' && !user) {
    next('/login') // 没登录就踢回登录页
  } else {
    next() // 否则放行
  }
})

export default router