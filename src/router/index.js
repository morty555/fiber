import { createRouter, createWebHistory } from 'vue-router'
import MainLayout from '@/views/MainLayout.vue'
import LoginPage from '../views/LoginPage.vue'
import GuideView from '@/views/GuideView.vue'
import AnalysisView from '@/views/AnalysisView.vue'
import MoreView from '@/views/MoreView.vue'
import FiberView from '@/views/FiberView.vue'
import RegisterPage from '@/views/RegisterPage.vue'
import HistoryView from '@/views/HistoryView.vue'
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
        component: AnalysisView,
        children:[
           {
            path: 'history',
            name: 'history',
            component: HistoryView,
           }
        ]
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
  {
    path:'/register',
    name:'register',
    component: RegisterPage
  },
]

const router = createRouter({
  history: createWebHistory(process.env.BASE_URL),
  routes
})

export default router