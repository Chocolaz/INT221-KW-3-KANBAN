<!-- Toast.vue -->
<template>
  <transition name="fade">
    <div
      v-if="visible"
      class="fixed bottom-4 right-4 max-w-xs bg-red-500 text-white p-3 rounded-md shadow-md"
    >
      <p v-html="message"></p>
    </div>
  </transition>
</template>

<script setup>
import { ref, watch } from 'vue'

const props = defineProps({
  message: { type: String, required: true },
  duration: { type: Number, default: 7000 }
})

const visible = ref(false)

watch(
  () => props.message,
  (newValue) => {
    if (newValue) {
      visible.value = true
      setTimeout(() => {
        visible.value = false
      }, props.duration)
    }
  }
)
</script>

<style scoped>
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.5s;
}
.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>
