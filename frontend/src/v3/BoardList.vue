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

const personalExpanded = ref(true)
const collabExpanded = ref(true)
const boardHoverStates = ref({
  personal: {},
  collab: {}
})

const fetchBoards = async () => {
  try {
    const response = await fetchUtils.getBoards()

    const personal = response.filter((board) => board.owner.name === username)
    const collab = []

    personal.sort((a, b) => new Date(a.created_on) - new Date(b.created_on))

    for (const board of response.filter(
      (board) => board.owner.name !== username
    )) {
      try {
        const collabDetails = await fetchUtils.getCollab(board.id)

        if (collabDetails.length > 0) {
          const { access_right, added_on, oid } = collabDetails[0]
          board.accessRight = access_right
          board.addedOn = new Date(added_on)
          board.oid = oid
          collab.push(board)
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

    personalBoards.value.forEach((board) => {
      boardHoverStates.value.personal[board.id] = false
    })
    collabBoards.value.forEach((board) => {
      boardHoverStates.value.collab[board.id] = false
    })
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

const toggleSection = (section) => {
  if (section === 'personal') {
    personalExpanded.value = !personalExpanded.value
  } else {
    collabExpanded.value = !collabExpanded.value
  }
}

const handleBoardHover = (type, boardId, isHovering) => {
  boardHoverStates.value[type][boardId] = isHovering
}

onMounted(fetchBoards)
</script>

<template>
  <div
    class="min-h-screen bg-white p-8 transition-all duration-300 ease-in-out"
  >
    <div
      class="w-full max-w-5xl h-[400px] mx-auto bg-white rounded-2xl shadow-2xl p-8 overflow-y-auto animate-fade-in-up"
    >
      <h2 class="text-3xl font-bold text-center text-red-600 mb-8">
        <i class="fas fa-folder-open mr-2"></i>Board Management
      </h2>

      <!-- Create Board Section -->
      <div class="flex justify-center mb-6">
        <button
          @click="showModal = true"
          :disabled="personalBoards.length >= 1"
          class="bg-red-600 text-white py-3 px-6 rounded-lg shadow-lg hover:bg-red-500 transition-all duration-300 transform hover:scale-105 active:scale-95 disabled:opacity-50 disabled:cursor-not-allowed flex items-center space-x-2"
        >
          <i class="fas fa-plus-circle"></i>
          <span>Create Board</span>
        </button>
      </div>

      <!-- Personal Boards Section -->
      <div class="mb-8">
        <div
          @click="toggleSection('personal')"
          class="flex justify-between items-center cursor-pointer py-2 px-4 border-2 rounded-lg hover:bg-red-100 transition-colors duration-300"
        >
          <h3 class="text-xl font-semibold text-red-600">
            <i class="fas fa-user mr-2"></i>Personal Boards
          </h3>
          <i
            class="fas fa-chevron-down h-6 w-6 text-red-600 transition-transform duration-300"
            :class="{ 'rotate-180': !personalExpanded }"
          ></i>
        </div>
        <transition
          name="slide-fade"
          enter-active-class="transition-all duration-300 ease-out"
          leave-active-class="transition-all duration-300 ease-in"
        >
          <div v-if="personalExpanded" class="mt-4 space-y-4">
            <div
              v-if="personalBoards.length === 0"
              class="text-center text-gray-500 italic py-4 bg-gray-100 rounded-lg"
            >
              <i class="fas fa-folder-open mr-2"></i>No personal boards
            </div>
            <div
              v-for="(board, index) in personalBoards"
              :key="board.id"
              class="bg-white border border-red-100 rounded-lg p-4 transition-transform duration-300 hover:shadow-lg hover:scale-105 flex justify-between items-center"
            >
              <a
                @click.prevent="viewBoardTasks(board.id)"
                class="text-gray-600 font-semibold hover:text-red-600 cursor-pointer"
              >
                <i class="fas fa-folder mr-2 text-red-500"></i>{{ board.name }}
              </a>
              <span class="text-sm text-gray-500">
                <i
                  :class="
                    board.visibility === 'public'
                      ? 'fas fa-globe'
                      : 'fas fa-lock'
                  "
                  class="mr-1"
                ></i>
                {{ board.visibility }}
              </span>
            </div>
          </div>
        </transition>
      </div>

      <!-- Collaboration Boards Section -->
      <div>
        <div
          @click="toggleSection('collab')"
          class="flex justify-between items-center cursor-pointer py-2 px-4 rounded-lg hover:bg-red-100 transition-colors duration-300 border-2"
        >
          <h3 class="text-xl font-semibold text-red-600">
            <i class="fas fa-users mr-2"></i>Collaboration Boards
          </h3>
          <i
            class="fas fa-chevron-down h-6 w-6 text-red-600 transition-transform duration-300"
            :class="{ 'rotate-180': !collabExpanded }"
          ></i>
        </div>
        <transition>
          <div v-if="collabExpanded" class="mt-4 space-y-4">
            <div
              v-if="collabBoards.length === 0"
              class="text-center text-gray-500 italic py-4 bg-gray-100 rounded-lg"
            >
              <i class="fas fa-folder-open mr-2"></i>No collaboration boards
            </div>
            <div
              v-for="(board, index) in collabBoards"
              :key="board.id"
              class="bg-white border border-red-100 rounded-lg p-4 flex justify-between items-center transition-transform duration-300 hover:shadow-lg hover:scale-105"
            >
              <div>
                <a
                  @click.prevent="viewBoardTasks(board.id)"
                  class="text-grey-500 font-semibold hover:text-red-600 cursor-pointer"
                >
                  <i class="text-red-500 fas fa-folder mr-2"></i
                  >{{ board.name }}
                </a>
                <div class="text-sm text-gray-500 mt-1">
                  <i class="fas fa-user mr-1"></i>Owner:
                  {{ board.owner.name }} |
                  <i class="fas fa-key mr-1"></i>Access: {{ board.accessRight }}
                </div>
              </div>
              <button
                @click="showLeaveModal(board.name, board.id)"
                class="bg-red-500 text-white px-3 py-1 rounded hover:bg-red-600 transition-colors flex items-center"
              >
                <i class="fas fa-sign-out-alt mr-2"></i>Leave
              </button>
            </div>
          </div>
        </transition>
      </div>
    </div>
    <AddBoard
      v-if="showModal"
      @close="showModal = false"
      @board-added="fetchBoards"
    />
    <LeaveBoardModal
      v-if="isLeaveModalVisible"
      :boardName="boardToLeave"
      :isVisible="isLeaveModalVisible"
      :onLeave="handleLeave"
      @close="isLeaveModalVisible = false"
    />
  </div>
</template>

<style scoped>
.slide-fade-enter-active,
.slide-fade-leave-active {
  transition: all 0.3s ease;
}
.slide-fade-enter-from,
.slide-fade-leave-to {
  opacity: 0;
  transform: translateY(-20px);
}

@keyframes fade-in {
  from {
    opacity: 0;
  }
  to {
    opacity: 1;
  }
}

.animate-fade-in {
  animation: fade-in 0.5s ease-out;
}
</style>
