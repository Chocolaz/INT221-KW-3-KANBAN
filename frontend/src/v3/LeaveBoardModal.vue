<script setup>
import { defineProps } from 'vue'

const props = defineProps({
  boardName: String,
  isVisible: {
    type: Boolean,
    required: true
  },
  onLeave: Function
})

const confirmLeave = () => {
  props.onLeave()
}
</script>

<template>
  <div
    v-if="isVisible"
    class="fixed inset-0 flex items-center justify-center bg-black bg-opacity-50 z-50"
  >
    <div class="bg-white rounded-lg shadow-lg p-6 animate-fade-in-up">
      <h3 class="text-lg font-semibold mb-4">Leave Board</h3>
      <p>Are you sure you want to leave the board "{{ boardName }}"?</p>
      <div class="flex justify-end mt-4">
        <button
          @click="confirmLeave"
          class="bg-red-600 text-white py-2 px-4 rounded-lg mr-2"
        >
          Leave
        </button>
        <button
          @click="$emit('close')"
          class="bg-gray-300 text-black py-2 px-4 rounded-lg"
        >
          Cancel
        </button>
      </div>
    </div>
  </div>
</template>
