<template>
  <button class="login-button" @click="handleClick">
    {{ isLoggedIn ? '退出' : '登录' }}
  </button>
</template>

<script>
export default {
  props: {
    isLoggedIn: Boolean
  },
  methods: {
    handleClick() {
      if (this.isLoggedIn) {
        this.$emit('logout')
      } else {
        this.$router.push('/login') // 使用vue-router跳转
      }
    }
  }
}
</script>

<style scoped>
.login-button {
  position: fixed;
  top: 20px;
  right: 20px;
  padding: 8px 16px;
  background-color: grey;
  color: white;
  border: none;
  border-radius: 4px;
  z-index: 1000;
  cursor: pointer;
  width: 100pt;
}

.login-button:hover {
  background-color: darkgray;
}
</style>