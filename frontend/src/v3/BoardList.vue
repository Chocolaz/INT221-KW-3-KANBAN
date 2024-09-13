<template>
  <div class="p-6">
    <h2 class="text-3xl font-semibold mb-4">Board List</h2>

    <!-- Create Board Button -->
    <button
      @click="showModal = true"
      class="bg-green-500 text-white py-2 px-4 rounded-lg shadow hover:bg-green-600 focus:outline-none focus:ring-2 focus:ring-green-500"
      :disabled="boards.length >= 4"
    >
      Create Board
    </button>

    <!-- Message when the board limit is reached -->
    <p v-if="boards.length >= 4" class="text-red-500 mt-2">
      You can only create up to 4 boards.
    </p>

    <table class="w-full mt-4 border-collapse">
      <thead>
        <tr class="bg-gray-200 text-left">
          <th class="border px-4 py-2">No</th>
          <th class="border px-4 py-2">Name</th>
        </tr>
      </thead>
      <tbody>
        <!-- Check if there are no boards -->
        <tr v-if="boards.length === 0">
          <td colspan="2" class="text-gray-500 text-xl text-center py-6">
            No personal board
          </td>
        </tr>
        <!-- Render boards if available -->
        <tr
          v-for="(board, index) in boards"
          :key="board.id"
          class="border-b hover:bg-gray-100"
        >
          <td class="border px-4 py-2">{{ index + 1 }}</td>
          <td class="border px-4 py-2">
            <a
              @click.prevent="viewBoardTasks(board.id)"
              class="text-blue-500 hover:underline cursor-pointer"
            >
              {{ board.name }}
            </a>
          </td>
        </tr>
      </tbody>
    </table>

    <!-- Modal for Add Board -->
    <AddBoard
      v-if="showModal"
      @close="showModal = false"
      @board-added="fetchBoards"
    />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import fetchUtils from '../lib/fetchUtils'
import AddBoard from './AddBoard.vue'

const boards = ref([])
const showModal = ref(false)
const router = useRouter()

const fetchBoards = async () => {
  try {
    const response = await fetchUtils.getBoards()
    boards.value = response
  } catch (error) {
    console.error('Error fetching boards:', error)
  }
}

const viewBoardTasks = (boardId) => {
  router.push({ name: 'TaskList', params: { boardId } })
}

onMounted(fetchBoards)
</script>
