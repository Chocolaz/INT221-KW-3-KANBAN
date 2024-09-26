<template>
  <div
    v-if="visible"
    class="tooltip"
    :style="{ top: position.top + 'px', left: position.left + 'px' }"
  >
    {{ message }}
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'

const props = defineProps({
  visible: {
    type: Boolean,
    required: true
  },
  message: {
    type: String,
    default: 'You need to be board owner to perform this action.'
  }
})

const position = ref({ top: 0, left: 0 })

onMounted(() => {
  const tooltipElement = document.querySelector('.tooltip')
  if (tooltipElement) {
    const { top, left } = tooltipElement.getBoundingClientRect()
    position.value = { top, left }
  }
})
</script>

<style scoped>
.tooltip {
  position: absolute;
  background-color: rgba(0, 0, 0, 0.7);
  color: #fff;
  padding: 8px;
  border-radius: 4px;
  z-index: 1000;
}
</style>
