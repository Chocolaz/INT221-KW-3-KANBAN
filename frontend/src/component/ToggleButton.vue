<template>
  <button
    class="toggle-button fixed bottom-24 right-8 w-14 h-14 rounded-full flex items-center justify-center text-white text-2xl cursor-pointer"
    @click="toggle"
  >
    <i :class="isToggled ? 'fa fa-globe' : 'fa fa-lock'"></i>
  </button>
</template>

<script setup>
import { ref } from 'vue'

const isToggled = ref(false)

const toggle = () => {
  isToggled.value = !isToggled.value
  console.log('Toggle is now:', isToggled.value)
}
</script>

<style scoped>
.toggle-button {
  background: linear-gradient(45deg, #6a5acd, #ff6b6b);
  transition: all 0.3s ease;
  box-shadow: 0 4px 15px rgba(106, 92, 205, 0.2);
}

.toggle-button:hover {
  transform: scale(1.1) rotate(360deg);
}
</style>
