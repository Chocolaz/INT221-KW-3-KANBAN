<template>
  <div
    v-if="visible"
    class="tooltip flex items-center space-x-2 p-2 bg-black bg-opacity-70 text-white rounded shadow-lg"
    :style="{ top: position.top + 'px', left: position.left + 'px' }"
  >
    <LucideIcon class="w-4 h-4" />
    <span>{{ message }}</span>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { Info } from 'lucide-vue-next' // Import your desired icon

const props = defineProps({
  visible: {
    type: Boolean,
    required: true
  },
  message: {
    type: String,
    default: 'You need to be the board owner to perform this action.'
  }
})

const position = ref({ top: 0, left: 0 })

onMounted(() => {
  const tooltipElement = document.querySelector('.tooltip')
  if (tooltipElement) {
    const { top, left } = tooltipElement.getBoundingClientRect()
    position.value = { top: top + window.scrollY, left: left + window.scrollX }
  }
})
</script>

<style scoped>
.tooltip {
  position: absolute;
  z-index: 1000;
}
</style>
