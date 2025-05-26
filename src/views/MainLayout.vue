<template>
  <div class="school-website" >
<!--按钮  -->
      <LoginButton :isLoggedIn="isLoggedIn" 
      @logout="handleLogout"
       />
<!-- 顶部校徽和标题  -->
    <header class="header">
      <div class="logo-container">
        <img src="https://via.placeholder.com/80x80" alt="校徽" class="logo">
        <h1>XX大学纤维研究中心</h1>
      </div>
    </header>

    <!-- 导航栏 -->
    <nav class="nav-bar">
      <NavButton v-for="button in navButtons" :key="button.id" :title="button.title"
        :active="activeButton === button.id" @click="changeView(button.id)" />
    </nav>

    <!-- 轮播图 -->
    <div class="carousel-container">
      <Carousel :slides="slides" />
    </div>

    <!-- 内容区域 -->
    <main class="main-content">
      
     <router-view></router-view>
    </main> 

    <!-- 页脚 -->
    <footer class="footer">
      <p>© 2023 XX大学纤维研究中心 版权所有</p>
      <p>地址：XX省XX市XX区XX路XX号</p>
    </footer>
  
  </div>
</template>

<script>
import { ref } from 'vue'
import { useRouter } from 'vue-router' // 添加路由导入
import NavButton from '../components/NavButton.vue'
import Carousel from '@/components/Carousel.vue'
import GuideView from './GuideView.vue'
import FiberView from './FiberView.vue'
import AnalysisView from './AnalysisView.vue'
import MoreView from './MoreView.vue'
import LoginButton from '../components/LoginButton.vue'
import emitter from '@/eventBus'
import HistoryView from './HistoryView.vue'

export default {
  components: {
    NavButton,
    Carousel,
    GuideView,
    FiberView,
    AnalysisView,
    MoreView,
    LoginButton,
    HistoryView,
  },
 
  setup() {
     const router = useRouter() // 获取路由实例
    const isLoggedIn = ref(localStorage.getItem('loggedIn') === 'true')

    const navButtons = [
      { id: 'guide', title: '操作指南' },
      { id: 'fiber', title: '纤维识别' },
      { id: 'analysis', title: '图像分析' },
      { id: 'more', title: '更多' }
    ]
    
    emitter.on('login-success',()=>{
      isLoggedIn.value = true
      localStorage.setItem('loggedIn','true')
    })

    const activeButton = ref('guide')

    // const handleLoginSuccess = () => {
    //    isLoggedIn.value = true
    //   localStorage.setItem('loggedIn', 'true')

    // }
    const handleLogout = () => {
      isLoggedIn.value = false
      
      localStorage.removeItem('loggedIn')
      router.push('/guide') // 退出后跳转到首页
      
    }
    const handleAnalysisComplete = () => {
      if (this.$route.name === 'history') {
        this.$refs.HistoryView?.fetchRecords()
      }
      this.$bus.emit('refresh-history')
    }
    const slides = [
      {
        id: 1,
        title: '校园风光',
        image: 'https://via.placeholder.com/1200x400?text=校园风光1',
        description: '美丽的校园环境'
      },
      {
        id: 2,
        title: '科研成果',
        image: 'https://via.placeholder.com/1200x400?text=科研成果展示',
        description: '最新的纤维研究突破'
      },
      {
        id: 3,
        title: '学术活动',
        image: 'https://via.placeholder.com/1200x400?text=学术讲座现场',
        description: '丰富的学术交流活动'
      }
    ]

    const changeView = (buttonId) => {
      activeButton.value = buttonId
       router.push({ name: buttonId })
    }
    router.afterEach((to) => {
      const routeName = to.name.toLowerCase()
      if (navButtons.some(btn => btn.id === routeName)) {
        activeButton.value = routeName
      }
    })

    return {
      navButtons,
      activeButton,
      slides,
      changeView,
      handleLogout,
      isLoggedIn,
      handleAnalysisComplete,
    }
  }
}
</script>

<style scoped>
.school-website {
  font-family: 'Microsoft YaHei', sans-serif;
  max-width: 1200px;
  margin: 0 auto;
  color: #333;
}

.header {
  background-color: #1a5fb4;
  color: white;
  padding: 20px 0;
  text-align: center;
}

.logo-container {
  display: flex;
  align-items: center;
  justify-content: center;
}

.logo {
  margin-right: 20px;
}

.nav-bar {
  display: flex;
  justify-content: space-around;
  background-color: #f5f5f5;
  padding: 15px 0;
  margin-bottom: 20px;
}

.carousel-container {
  margin-bottom: 30px;
}

.main-content {
  padding: 20px;
  min-height: 400px;
  background-color: white;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
}

.footer {
  background-color: #1a5fb4;
  color: white;
  text-align: center;
  padding: 20px 0;
  margin-top: 30px;
}

.footer p {
  margin: 5px 0;
}
</style>