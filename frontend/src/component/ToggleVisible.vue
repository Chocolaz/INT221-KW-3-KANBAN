<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import VisibleModal from './VisibleModal.vue'
import fetchUtils from '../lib/fetchUtils'
import { checkOwnership } from '../lib/utils.js'

const route = useRoute()
const boardId = route.params.boardId
const isPublic = ref(false)
const isPrivate = ref(false)
const isModalVisible = ref(false)
const modalMessage = ref('')
const isOwner = ref(false)

const fetchBoardData = async () => {
  try {
    const boardData = await fetchUtils.getBoards(boardId)
    isPublic.value = boardData.data.visibility === 'public'
    isPrivate.value = boardData.data.visibility === 'private'

    const currentUser = localStorage.getItem('username')?.trim()
    isOwner.value = checkOwnership(boardData, currentUser)

    console.log(
      'isPublic:',
      isPublic.value,
      'isPrivate:',
      isPrivate.value,
      'isOwner:',
      isOwner.value
    )
  } catch (error) {
    console.error('Error fetching board data:', error)
  }
}

const showModal = () => {
  if (isPublic.value) {
    modalMessage.value = 'Make board private?'
  } else if (isPrivate.value) {
    modalMessage.value = 'Make board public?'
  }

  isModalVisible.value = true
}

onMounted(() => {
  fetchBoardData()
})
</script>

<template>
  <div>
    <button
      class="toggle-button fixed bottom-24 right-8 w-14 h-14 rounded-full flex items-center justify-center text-white text-2xl"
      :disabled="!isOwner"
      @click="isOwner ? showModal() : null"
      v-tooltip="
        !isOwner ? 'You need to be board owner to perform this action.' : ''
      "
    >
      <i :class="isPublic ? 'fa fa-globe' : 'fa fa-lock'"></i>
    </button>

    <VisibleModal
      v-if="isModalVisible"
      :message="modalMessage"
      :isPublic="isPublic"
      :isPrivate="isPrivate"
      @close="isModalVisible = false"
      @confirm="fetchBoardData"
    />
  </div>
</template>

<style scoped>
.toggle-button {
  background: linear-gradient(45deg, #6a5acd, #ff6b6b);
  transition: all 0.3s ease;
  box-shadow: 0 4px 15px rgba(106, 92, 205, 0.2);
}

.toggle-button:hover {
  transform: scale(1.1) rotate(360deg);
}

.toggle-button:disabled {
  cursor: not-allowed;
  opacity: 0.6;
}
</style>
