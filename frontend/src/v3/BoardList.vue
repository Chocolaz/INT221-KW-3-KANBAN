<template>
  <div class="flex justify-center items-center mt-10">
    <div class="w-full max-w-5xl mx-auto rounded-xl">
      <h2 class="text-2xl font-bold text-black text-center mb-6">Board List</h2>

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
      <p
        v-if="personalBoards.length >= 1"
        class="text-red-500 text-center mb-4"
      >
        You can only create up to 1 personal board.
      </p>

      <!-- Personal Boards List -->
      <h3 class="text-xl font-semibold mb-2 text-center">Personal Boards</h3>
      <div
        class="rounded-lg border-2 border-red-400 shadow-md overflow-hidden mb-6"
      >
        <table class="table-fixed min-w-full bg-white">
          <thead>
            <tr class="bg-red-400 text-white">
              <th class="w-1/12 px-4 py-3 text-center">No</th>
              <th class="w-5/12 px-4 py-3 text-center">Name</th>
              <th class="w-3/12 px-4 py-3 text-center">Visibility</th>
              <th class="w-3/12 px-4 py-3 text-center">Owner</th>
            </tr>
          </thead>
          <tbody>
            <tr v-if="personalBoards.length === 0">
              <td
                colspan="4"
                class="py-10 text-center text-gray-500 italic text-lg"
              >
                No personal board
              </td>
            </tr>
            <tr
              v-for="(board, index) in personalBoards"
              :key="board.id"
              class="hover:bg-red-50 border-b"
            >
              <td class="px-4 py-4 text-center">{{ index + 1 }}</td>
              <td class="px-4 py-4 text-center">
                <a
                  @click.prevent="viewBoardTasks(board.id)"
                  class="text-blue-500 hover:underline cursor-pointer"
                >
                  {{ board.name }}
                </a>
              </td>
              <td class="px-4 py-4 text-center">{{ board.visibility }}</td>
              <td class="px-4 py-4 text-center">{{ board.owner.name }}</td>
            </tr>
          </tbody>
        </table>
      </div>

      <!-- Collab Boards List -->
      <h3 class="text-xl font-semibold mb-2 text-center">Collab Boards</h3>
      <div class="rounded-lg border-2 border-red-400 shadow-md overflow-hidden">
        <table class="table-fixed min-w-full bg-white">
          <thead>
            <tr class="bg-red-400 text-white">
              <th class="w-1/12 px-4 py-3 text-center">No</th>
              <th class="w-5/12 px-4 py-3 text-center">Name</th>
              <th class="w-3/12 px-4 py-3 text-center">Owner</th>
              <th class="w-3/12 px-4 py-3 text-center">Access Right</th>
              <th class="w-1/12 px-4 py-3 text-center">Action</th>
            </tr>
          </thead>
          <tbody>
            <tr v-if="collabBoards.length === 0">
              <td
                colspan="5"
                class="py-10 text-center text-gray-500 italic text-lg"
              >
                No collaboration boards
              </td>
            </tr>
            <tr
              v-for="(board, index) in collabBoards"
              :key="board.id"
              class="hover:bg-red-50 border-b"
            >
              <td class="px-4 py-4 text-center">{{ index + 1 }}</td>
              <td class="px-4 py-4 text-center">
                <a
                  @click.prevent="viewBoardTasks(board.id)"
                  class="text-blue-500 hover:underline cursor-pointer"
                >
                  {{ board.name }}
                </a>
              </td>
              <td class="px-4 py-4 text-center">{{ board.owner.name }}</td>
              <td class="px-4 py-4 text-center">{{ board.accessRight }}</td>
              <td class="px-4 py-4 text-center">
                <button
                  class="bg-red-600 text-white py-1 px-3 rounded-lg shadow hover:bg-red-400 focus:outline-none focus:ring-2 focus:ring-red-300"
                  @click="showLeaveModal(board.name, board.id)"
                >
                  Leave
                </button>
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

      <!-- Modal for Leaving Board -->
      <LeaveBoardModal
        v-if="isLeaveModalVisible"
        :boardName="boardToLeave"
        :isVisible="isLeaveModalVisible"
        :onLeave="handleLeave"
        @close="isLeaveModalVisible = false"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import fetchUtils from '../lib/fetchUtils'
import AddBoard from './AddBoard.vue'
import LeaveBoardModal from './LeaveBoardModal.vue'

const personalBoards = ref([])
const collabBoards = ref([])
const showModal = ref(false)
const isLeaveModalVisible = ref(false)
const boardToLeave = ref('')
const boardToLeaveId = ref(null)
const router = useRouter()
const username = localStorage.getItem('username')

const fetchBoards = async () => {
  try {
    const response = await fetchUtils.getBoards()

    const personal = response.filter((board) => board.owner.name === username)
    const collab = response.filter((board) => board.owner.name !== username)

    personal.sort((a, b) => new Date(a.created_on) - new Date(b.created_on))

    for (const board of collab) {
      try {
        const collabDetails = await fetchUtils.getCollab(board.id)
        if (collabDetails.length > 0) {
          const { access_right, added_on, oid } = collabDetails[0]
          board.accessRight = access_right
          board.addedOn = new Date(added_on)
          board.oid = oid
        }
      } catch (error) {
        console.error(
          `Error fetching collab details for board ${board.id}:`,
          error
        )
      }
    }

    collab.sort((a, b) => a.addedOn - b.addedOn)

    personalBoards.value = personal
    collabBoards.value = collab
  } catch (error) {
    console.error('Error fetching boards:', error.message)
  }
}

const viewBoardTasks = (boardId) => {
  router.push({ name: 'taskView', params: { boardId } })
}

const showLeaveModal = (boardName, boardId) => {
  boardToLeave.value = boardName
  boardToLeaveId.value = boardId
  isLeaveModalVisible.value = true
}

const handleLeave = async () => {
  try {
    const board = collabBoards.value.find((b) => b.id === boardToLeaveId.value)
    if (board && board.oid) {
      const collabId = board.oid
      await fetchUtils.removeCollab(boardToLeaveId.value, collabId)
      await fetchBoards()
    } else {
      console.error('Collaborator ID not found')
    }
  } catch (error) {
    console.error('Error leaving board:', error.message)
  } finally {
    isLeaveModalVisible.value = false
  }
}

onMounted(fetchBoards)
</script>
