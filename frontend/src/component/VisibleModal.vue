<script setup>
import { defineEmits } from 'vue'
import { useRoute } from 'vue-router'
import fetchUtils from '../lib/fetchUtils'

const props = defineProps({
  message: String,
  isPublic: Boolean,
  isPrivate: Boolean
})

const emit = defineEmits(['close', 'confirm'])
const route = useRoute()

const confirmAction = async () => {
  const boardId = route.params.boardId
  let newVisibility

  if (props.isPrivate) {
    newVisibility = 'public'
  } else if (props.isPublic) {
    newVisibility = 'private'
  } else {
    console.error('Invalid visibility state')
  }

  try {
    await fetchUtils.visibilityBoard(boardId, newVisibility)
    emit('confirm')
    closeModal()
  } catch (error) {
    console.error('Error updating board visibility:', error)
    closeModal()
  }
}

const closeModal = () => {
  emit('close')
}
</script>

<template>
  <div
    class="modal-overlay fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50"
    @click.self="closeModal"
  >
    <div
      class="modal-content bg-white rounded-lg shadow-lg p-6 max-w-sm w-full text-center"
    >
      <h1 class="text-red-600 text-lg font-semibold"> Board visibility change?</h1>
      <span>
        <p class="text-gray-700 text-base mb-4"> 
            Do you want to change board visibility to 
            <h3 class="text-slate-600 font-semibold">{{ message }}</h3>
        </p>
      </span>
      <div class="flex justify-center space-x-4">
        <button
          class="bg-blue-500 text-white py-2 px-4 rounded hover:bg-blue-600 transition"
          @click="confirmAction"
        >
          Confirm
        </button>
        <button
          class="bg-gray-300 text-gray-700 py-2 px-4 rounded hover:bg-gray-400 transition"
          @click="closeModal"
        >
          Cancel
        </button>
      </div>
    </div>
  </div>
</template>
