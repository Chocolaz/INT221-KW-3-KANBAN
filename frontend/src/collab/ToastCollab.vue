<template>
  <Transition name="fade">
    <div
      v-if="isVisible"
      :class="['fixed bottom-10 left-4 p-4 rounded shadow-lg', statusClass]"
    >
      <p>{{ message }}</p>
    </div>
  </Transition>
</template>

<script setup>
import { ref, watch, computed } from 'vue'

const props = defineProps({
  status: {
    type: Number,
    required: true
  },
  message: {
    type: String,
    required: true
  }
})

const emit = defineEmits(['hideComplete'])
const isVisible = ref(false)

const statusClass = computed(() => {
  switch (props.status) {
    case 200:
      return 'bg-green-600 text-white'
    case 401:
    case 403:
    case 404:
    case 409:
      return 'bg-red-600 text-white'
    default:
      return 'bg-gray-600 text-white'
  }
})

watch(
  () => props.status,
  (newStatus) => {
    if (newStatus !== 0) {
      isVisible.value = true
      setTimeout(() => {
        isVisible.value = false
        emit('hideComplete')
      }, 3000)
    }
  }
)
</script>

<style scoped>
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.5s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

.fade-enter-to,
.fade-leave-from {
  opacity: 1;
}
</style>
