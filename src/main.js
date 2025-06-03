
function clearLoginStateIfDev() {
  if (import.meta.env && import.meta.env.MODE === 'development') {
    localStorage.removeItem('loggedIn')
  }
}
clearLoginStateIfDev()//顶层代码思想


import { createApp } from 'vue'
import App from './App.vue'
import router from './router'
import axios from "axios"
const app = createApp(App)
app.use(router)
app.mount('#app')
app.use(axios)
