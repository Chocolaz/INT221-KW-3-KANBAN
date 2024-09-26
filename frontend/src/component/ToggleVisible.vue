<template>
  <div>
    <button
      class="toggle-button fixed bottom-24 right-8 w-14 h-14 rounded-full flex items-center justify-center text-white text-2xl cursor-pointer"
      @click="showModal"
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

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import VisibleModal from './VisibleModal.vue'
import fetchUtils from '../lib/fetchUtils'

const route = useRoute()
const boardId = route.params.boardId
const isPublic = ref(false)
const isPrivate = ref(false)
const isModalVisible = ref(false)
const modalMessage = ref('')

// Fetch board data
const fetchBoardData = async () => {
  try {
    const boardData = await fetchUtils.getBoards(boardId)
    isPublic.value = boardData.data.visibility === 'public'
    isPrivate.value = boardData.data.visibility === 'private'
    console.log('isPublic:', isPublic.value, 'isPrivate:', isPrivate.value)
  } catch (error) {
    console.error('Error fetching board data:', error)
  }
}

// Show modal
const showModal = () => {
  if (isPublic.value) {
    modalMessage.value = 'private?'
  } else if (isPrivate.value) {
    modalMessage.value = '"public?'
  }

  isModalVisible.value = true
}

onMounted(fetchBoardData)
</script>

<style scoped>
.toggle-button {
  background: linear-gradient(45deg, #6a5acd, #ff6b6b);
  transition: all 0.3s ease;
  box-shadow: 0 4px 15px rgba(106, 92, 205, 0.2);
}

.toggle-button:hover {
  transform: scale(1.1) rotate(360deg);
}
</style>
