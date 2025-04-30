<template>
  <button 
    class="nav-button"
    :class="{ active: active }"
    @click="$emit('click')"
  >
    {{ title }}
  </button>
</template>

<script>
export default {
  props: {
    title: {
      type: String,
      required: true
    },
    active: {
      type: Boolean,
      default: false
    }
  },
  emits: ['click']
}
</script>

<style scoped>
.nav-button {
  padding: 10px 25px;
  font-size: 16px;
  background-color: transparent;
  border: none;
  cursor: pointer;
  transition: all 0.3s;
  border-radius: 4px;
}

.nav-button:hover {
  background-color: #e1e1e1;
}

.nav-button.active {
  background-color: #1a5fb4;
  color: white;
}
</style>