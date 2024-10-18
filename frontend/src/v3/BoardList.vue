<template>
  <div class="p-6">
    <h2 class="text-3xl font-semibold mb-4 text-center">Board List</h2>

    <!-- Create Board Button -->
    <div class="flex justify-center mb-4">
      <button
        @click="showModal = true"
        class="bg-rose-600 text-white py-2 px-4 rounded-lg shadow hover:bg-rose-400 focus:outline-none focus:ring-2 focus:ring-rose-300"
        :disabled="personalBoards.length >= 1"
      >
        Create Board
      </button>
    </div>

    <!-- Message when the board limit is reached -->
    <p v-if="personalBoards.length >= 1" class="text-red-500 text-center">
      You can only create up to 1 personal board.
    </p>

    <!-- Personal Boards List -->
    <h3 class="text-2xl font-semibold mb-2">Personal Boards</h3>
    <div class="overflow-x-auto mb-6">
      <table class="min-w-full bg-white border border-gray-200">
        <thead>
          <tr>
            <th class="border px-6 py-4 text-left text-gray-600">No</th>
            <th class="border px-6 py-4 text-left text-gray-600">Name</th>
            <th class="border px-6 py-4 text-left text-gray-600">Visibility</th>
            <th class="border px-6 py-4 text-left text-gray-600">Owner</th>
          </tr>
        </thead>
        <tbody>
          <!-- Check if there are no personal boards -->
          <tr v-if="personalBoards.length === 0">
            <td colspan="4" class="text-gray-500 text-xl text-center py-6">
              No personal board
            </td>
          </tr>
          <!-- Render personal boards if available -->
          <tr
            v-for="(board, index) in personalBoards"
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
            <td class="border px-6 py-4 text-center">{{ board.visibility }}</td>
            <td class="border px-6 py-4 text-center">{{ board.owner.name }}</td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- Collab Boards List -->
    <h3 class="text-2xl font-semibold mb-2">Collab Boards</h3>
    <div class="overflow-x-auto">
      <table class="min-w-full bg-white border border-gray-200">
        <thead>
          <tr>
            <th class="border px-6 py-4 text-left text-gray-600">No</th>
            <th class="border px-6 py-4 text-left text-gray-600">Name</th>
            <th class="border px-6 py-4 text-left text-gray-600">Owner</th>
          </tr>
        </thead>
        <tbody>
          <tr v-if="collabBoards.length === 0">
            <td colspan="3" class="text-gray-500 text-xl text-center py-6">
              No collaboration boards
            </td>
          </tr>
          <!-- Render collaboration boards if available -->
          <tr
            v-for="(board, index) in collabBoards"
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
            <td class="border px-6 py-4 text-center">{{ board.owner.name }}</td>
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

const personalBoards = ref([])
const collabBoards = ref([])
const showModal = ref(false)
const router = useRouter()
const username = localStorage.getItem('username')

const fetchBoards = async () => {
  try {
    const response = await fetchUtils.getBoards()

    // Filter boards based on visibility
    const personal = response.filter((board) => board.owner.name === username)
    const collab = response.filter(
      (board) => board.visibility === 'public' || board.owner.name === username
    )

    // Sort personal boards by created_on in ascending order (if created_on exists)
    personal.sort((a, b) => new Date(a.created_on) - new Date(b.created_on))

    personalBoards.value = personal
    collabBoards.value = collab
  } catch (error) {
    console.error('Error fetching boards:', error.message)
  }
}

const viewBoardTasks = (boardId) => {
  router.push({ name: 'taskView', params: { boardId } })
}

onMounted(fetchBoards)
</script>

<style scoped></style>
