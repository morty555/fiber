import { createRouter, createWebHistory } from 'vue-router'
import App from '@/App.vue' // 主页
import LoginPage from '../views/LoginPage.vue'

const routes = [
  {
    path: '/',
    name: 'home',
    component: App
  },
  {
    path: '/login',
    name: 'login',
    component: LoginPage
  },

]

const router = createRouter({
  history: createWebHistory(process.env.BASE_URL),
  routes
})

export default router