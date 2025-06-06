import { createRouter, createWebHistory } from 'vue-router'
import MainLayout from '@/views/MainLayout.vue'
import LoginPage from '../views/LoginPage.vue'
import GuideView from '@/views/GuideView.vue'
import AnalysisView from '@/views/AnalysisView.vue'
import MoreView from '@/views/MoreView.vue'
import FiberView from '@/views/FiberView.vue'
import RegisterPage from '@/views/RegisterPage.vue'
import HistoryView from '@/views/HistoryView.vue'
import ImageView from '@/views/ImageView.vue'
import FiberDataView from '@/views/FiberDataView.vue'
import DataGraphView from '@/views/DataGraphView.vue'
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
 
    {
     path: '/history',
     name: 'history',
      component: HistoryView,
      meta: {
        requiresAuth: true,
      }
  },
  {
    path: '/image',
    name: 'image',
    component: ImageView,
    meta: {
      requiresAuth: true,
    }
    },
    {
      path: '/fiberdata',
      name: 'fiberdata',
      component: FiberDataView,
      meta: {
        requiresAuth: true,
     }
    },
       {
      path: '/graph',
      name: 'graph',
      component: DataGraphView,
      meta: {
        requiresAuth: true,
      }
        }
]

const router = createRouter({
  history: createWebHistory(process.env.BASE_URL),
  routes
})
router.beforeEach((to, from, next) => {
  const isLoggedIn = localStorage.getItem('loggedIn') === 'true'
  
  if (to.meta.requiresAuth) {
    if (!isLoggedIn) {
      alert('请先登录')
      next('/login')
    } else {
      next()
    }
  } else {
    next()
  }
})
export default router