<template>
  <div class="login-container">
    <div class="login-box">
      <h2>用户登录</h2>
      <form @submit.prevent="handleSubmit">
        <div class="form-group">
          <label for="username">用户名</label>
          <input type="text" id="username"
           v-model="username" required 
           placeholder="请输入用户名" />
        </div>
        <div class="form-group">
          <label for="password">密码</label>
          <input 
          type="password" 
          id="password" 
          v-model="password" required 
          placeholder="请输入密码" />
        </div>
        <button type="submit" class="login-btn">登录</button>
      </form>
      <p class="register-link">
        还没有账号? <a href="/register">立即注册</a>
      </p>
    </div>
  </div>
</template>

<script>
import axios from 'axios'
import emitter from '@/eventBus';
import { API_BASE_URL } from '@/config'

export default {
  name: 'LoginPage',
  data() {
    return {
      username: '',
      password: ''
    }
  },
  methods: {
async handleSubmit() {
  try {
    const response = await axios.post(`${API_BASE_URL}/login`, {
      username: this.username,
      password: this.password
    });

    const result = response.data;

    if (result.code === 1) {
      alert('登录成功!');
       emitter.emit('login-success'); 
      localStorage.setItem('jwtToken', result.data.token);
      localStorage.setItem('loggedIn', 'true');
      this.$router.push('/guide');
    } else {
      alert('登录失败: ' + (result.message || result.msg || '未知错误'));
    }
  } catch (error) {
    if (error.response && error.response.data && error.response.data.message) {
      alert('请求错误: ' + error.response.data.message);
    } else {
      alert('请求失败，请检查网络或服务器。');
    }
    console.error('登录出错:', error);
  }
}


  }
}
</script>


<style scoped>
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background-color: #f5f5f5;
}

.login-box {
  width: 100%;
  max-width: 400px;
  padding: 2rem;
  background: white;
  border-radius: 8px;
  box-shadow: 0 0 20px rgba(0, 0, 0, 0.1);
}

h2 {
  text-align: center;
  margin-bottom: 1.5rem;
  color: #333;
}

.form-group {
  margin-bottom: 1.5rem;
}

label {
  display: block;
  margin-bottom: 0.5rem;
  font-weight: 500;
}

input {
  width: 100%;
  padding: 0.75rem;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 1rem;
}

.login-btn {
  width: 100%;
  padding: 0.75rem;
  background-color: #4CAF50;
  color: white;
  border: none;
  border-radius: 4px;
  font-size: 1rem;
  cursor: pointer;
  transition: background-color 0.3s;
}

.login-btn:hover {
  background-color: #45a049;
}

.register-link {
  text-align: center;
  margin-top: 1.5rem;
  color: #666;
}

.register-link a {
  color: #4CAF50;
  text-decoration: none;
}

.register-link a:hover {
  text-decoration: underline;
}
</style>