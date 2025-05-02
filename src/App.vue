<template>
  <div>
   <router-view></router-view>
  </div>
</template>
<script>
import MainLayout from './views/MainLayout.vue';
export default {
  name: 'Main',
  components: {
    MainLayout
  }
}
</script>
