<template>
  <div class="school-website" >
<!--按钮  -->
      <LoginButton :isLoggedIn="isLoggedIn" 
      @logout="handleLogout"
       />
<!-- 顶部校徽和标题  -->
    <header class="header">
      <div class="logo-container">
        <!-- <img src="https://via.placeholder.com/80x80" alt="校徽" class="logo"> -->
        <h1>华南理工大学纤维分析平台</h1> 
      </div>
    </header>

    <!-- 导航栏 -->
    <nav class="nav-bar">
      <NavButton v-for="button in navButtons" :key="button.id" :title="button.title"
        :active="activeButton === button.id" @click="changeView(button.id)" />
    </nav>

    <!-- 轮播图 -->
      <div class="carousel-container" v-if="isGuidePage">
        <Carousel :slides="slides" />
      </div>


    <!-- 内容区域 -->
    <main class="main-content">
      
     <router-view @analysis-complete="handleAnalysisComplete" ></router-view>
    </main> 

    <!-- 页脚 -->
    <footer class="footer">
      <p>© 2023 华南理工大学纤维研究中心 版权所有</p>
      <p>地址：广东省广州市番禺区外环东路382号</p>
    </footer>
  
  </div>
</template>

<script>
import { ref } from 'vue'
import { computed } from 'vue'
import { useRouter ,useRoute} from 'vue-router'
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
     const route = useRoute()
    const isLoggedIn = ref(localStorage.getItem('loggedIn') === 'true')
    const isGuidePage = computed(() => route.name === 'guide')

    const navButtons = [
      { id: 'guide', title: '操作指南' },
      { id: 'fiber', title: '纤维识别' },
      { id: 'function', title: '其他功能' },
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
        image: 'https://srp-fiber.oss-cn-beijing.aliyuncs.com/237d-c37259b60325aabc33879e0abc314d88.jpg',
        description: '美丽的校园环境'
      },
      {
        id: 2,
        title: '科研竞赛',
        image: 'https://srp-fiber.oss-cn-beijing.aliyuncs.com/20240513141836714.jpg',
        description: '浓厚的竞赛氛围'
      },
      {
        id: 3,
        title: '学术活动',
        image: 'https://srp-fiber.oss-cn-beijing.aliyuncs.com/13e6283a-8998-430c-ada9-62dbc5bdb1b5.jpg',
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
      isGuidePage,

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