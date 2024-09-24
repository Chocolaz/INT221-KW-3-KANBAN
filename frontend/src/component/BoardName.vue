<template>
  <h1
    class="itbkk-board-name text-lg font-semibold text-red-600 mt-3 text-center"
  >
    {{ boardName }}
  </h1>
</template>

<script setup>
import { ref, watch, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import fetchUtils from '../lib/fetchUtils'

const route = useRoute()
const boardId = ref(route.params.boardId)
const boardName = ref('')

// Fetch board name by ID
const getBoardNameById = async (id) => {
  try {
    const boardData = await fetchUtils.getBoards(id)
    boardName.value = boardData.data.name
  } catch (error) {
    console.error('Failed to fetch board name:', error)
  }
}

watch(
  () => route.params.boardId,
  (newBoardId) => {
    boardId.value = newBoardId
    if (newBoardId) {
      getBoardNameById(newBoardId)
    } else {
      boardName.value = ''
    }
  }
)

onMounted(() => {
  if (boardId.value) {
    getBoardNameById(boardId.value)
  }
})
</script>
