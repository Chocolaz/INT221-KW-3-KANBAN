<script setup>
import FetchUtils from '../lib/fetchUtils'
import { defineProps, defineEmits } from 'vue'
import { useRoute } from 'vue-router'

const route = useRoute()
const boardId = route.params.boardId

const props = defineProps({
  closeModal: {
    type: Function,
    required: true
  },
  taskId: {
    type: Number,
    required: true
  },
  taskTitle: {
    type: String,
    required: true
  },
  taskIndex: {
    type: Number,
    required: true
  }
})

const emit = defineEmits(['deleted', 'showSuccessModal'])

const confirmDelete = async () => {
  try {
    const response = await FetchUtils.deleteData(
      `tasks/${props.taskId}`,
      boardId
    )
    const statusCode = response.statusCode

    emit('deleted', props.taskId, statusCode)

    props.closeModal()
  } catch (error) {
    alert('Failed to delete task.')
  }
}

const cancelModal = () => {
  props.closeModal()
}
</script>

<template>
  <div
    class="fixed inset-0 bg-black bg-opacity-50 flex justify-center items-center z-50"
  >
    <div class="bg-white shadow-lg rounded-lg w-96 p-6 animate-fade-in-up">
      <h2 class="text-xl font-bold mb-4">DELETE TASK</h2>
      <p class="text-gray-700 mb-6">
        Do you want to delete the task number "{{ taskIndex }}" <br />
        "{{ taskTitle }}"?
      </p>
      <div class="flex justify-between">
        <button
          class="bg-red-500 text-white py-2 px-4 rounded hover:bg-red-600"
          @click="confirmDelete"
        >
          Confirm
        </button>
        <button
          class="bg-gray-300 text-gray-700 py-2 px-4 rounded hover:bg-gray-400"
          @click="cancelModal"
        >
          Cancel
        </button>
      </div>
    </div>
  </div>
</template>
