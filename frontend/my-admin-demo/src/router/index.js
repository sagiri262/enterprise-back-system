import { createRouter, createWebHistory } from 'vue-router'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'personal-home',
      component: () => import('../views/PersonalHome.vue'),
    },
    {
      path: '/admin',
      name: 'admin-dashboard',
      component: () => import('../views/AdminDashboard.vue'),
    },
    {
      path: '/blog-admin',
      name: 'blog-admin',
      component: () => import('../views/BlogAdmin.vue'),
    },
  ],
  scrollBehavior(to, from, savedPosition) {
    if (savedPosition) return savedPosition
    if (to.hash) return { el: to.hash, behavior: 'smooth' }
    return { top: 0 }
  },
})

export default router
