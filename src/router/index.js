import { createRouter, createWebHistory } from 'vue-router'
import MainLayout from '@/views/MainLayout.vue'
import LoginPage from '../views/LoginPage.vue'
import GuideView from '@/views/GuideView.vue'
import AnalysisView from '@/views/AnalysisView.vue'
import MoreView from '@/views/MoreView.vue'
import FiberView from '@/views/FiberView.vue'
const routes = [
  {
    path: '/',
    component: MainLayout,
    children: [
      {
        path: '/',
        redirect: '/guide' // 默认重定向到指南页
      },
      {
        path: 'guide',
        name: 'guide',
        component: GuideView
      },
      {
        path: 'fiber',
        name: 'fiber',
        component: FiberView
      },
      {
        path: 'function',
        name: 'function',
        component: AnalysisView
      },
      {
        path: 'more',
        name: 'more',
        component: MoreView
      }
    ]
  }
  ,
  {
    path: '/login',
    name: 'login',
    component: LoginPage,

  },
]

const router = createRouter({
  history: createWebHistory(process.env.BASE_URL),
  routes
})

export default router