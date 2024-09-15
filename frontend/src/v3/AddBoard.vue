<template>
  <div
    class="fixed inset-0 bg-black bg-opacity-70 flex justify-center items-center z-50"
    @click.self="closeModal"
  >
    <div class="bg-white p-6 rounded-lg w-80 max-w-md text-center shadow-lg">
      <h2 class="text-2xl font-semibold mb-4">Add New Board</h2>
      <form @submit.prevent="submitBoard">
        <input
          v-model="newBoardName"
          placeholder="Board Name"
          required
          class="w-full p-2 mb-4 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
        />
        <div class="flex justify-between">
          <button
            type="submit"
            class="bg-red-500 text-white py-2 px-4 rounded-lg hover:bg-red-700 focus:outline-none focus:ring-2 focus:ring-blue-500"
          >
                <i class="fas fa-plus-circle"></i>
            Add Board
          </button>
          <button
            class="bg-gray-500 text-white py-2 px-4 rounded-lg hover:bg-gray-700 focus:outline-none focus:ring-2 focus:ring-red-500"
            @click="closeModal"
          >
            Close
          </button>
        </div>
      </form>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import fetchUtils from '../lib/fetchUtils'

const newBoardName = ref('')

const emit = defineEmits(['close', 'board-added'])

const closeModal = () => {
  emit('close')
}

const submitBoard = async () => {
  try {
    const boardData = { boardName: newBoardName.value }
    const response = await fetchUtils.addBoard(boardData)
    console.log('Board added:', response)
    emit('board-added')
    closeModal()
  } catch (error) {
    console.error('Error adding board:', error)
  }
}
</script>