<template>
  <div class="p-6">
    <h2 class="text-3xl font-semibold mb-4 text-center">Board List</h2>

    <!-- Create Board Button -->
    <div class="flex justify-center mb-4">
      <button
        @click="showModal = true"
        class="bg-rose-600 text-white py-2 px-4 rounded-lg shadow hover:bg-rose-400 focus:outline-none focus:ring-2 focus:ring-rose-300"
        :disabled="boards.length >= 1"
      >
        Create Board
      </button>
    </div>

    <!-- Message when the board limit is reached -->
    <p v-if="boards.length >= 1" class="text-red-500 text-center">
      You can only create up to 1 boards.
    </p>

    <div class="table-container">
      <table class="table header-table">
        <thead>
          <tr>
            <th>No</th>
            <th>Name</th>
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
            <td class="border px-6 py-4 text-center">{{ index + 1 }}</td>
            <td class="border px-6 py-4 text-center">
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
    </div>

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
    console.log('Fetching boards...')
    const response = await fetchUtils.getBoards()
    console.log('Fetched boards response:', response)

    boards.value = Array.isArray(response) ? response : []

    if (boards.value.length > 0) {
      router.push({ name: 'taskView', params: { boardId: boards.value[0].id } })
    }
  } catch (error) {
    console.error('Error fetching boards:', error.message)
  }
}

const viewBoardTasks = (boardId) => {
  router.push({ name: 'taskView', params: { boardId } })
}

onMounted(fetchBoards)
</script>

<style scoped>
tbody tr:nth-child(even) {
  background-color: #ffe3e3;
}

tbody tr:nth-child(odd) {
  background-color: #ffffff;
}

tbody tr:hover {
  background-color: #ffccd5;
  transition: background-color 0.3s ease;
}

.table-container {
  margin: 0 auto;
  width: 700px;
  border-radius: 10px;
  font-size: 16px;
  color: #343a40;
  background: #ffffff;
  border: 2px solid #ff6b6b;
  box-shadow: 0 8px 32px 0 rgba(255, 107, 107, 0.2);
  overflow: hidden;
  display: flex;
  justify-content: center;
  align-items: center;
}

.table-container {
  margin-top: 50px;
}

.table {
  border-collapse: separate;
  border-spacing: 0;
  width: 100%;
  table-layout: fixed;
  background-color: #ffffff;
  overflow: hidden;
}

.table th,
.table td {
  text-align: center;
}

.table th {
  background-color: #ff6b6b;
  font-weight: bold;
  color: #ffffff;
  font-size: 17px;
}
</style>
