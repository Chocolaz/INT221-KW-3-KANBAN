<template>
  <div v-if="isTaskList || isStatusList">
    <h1
      class="itbkk-board-name text-lg font-semibold text-red-600 mt-3 text-center"
    >
      {{ boardName }}
    </h1>
  </div>
</template>

<script setup>
import { computed, ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import fetchUtils from '../lib/fetchUtils'

const route = useRoute()
const boardId = ref(route.params.boardId)
const boardName = ref('')

const isTaskList = computed(() => route.name === 'taskView')
const isStatusList = computed(() => route.name === 'statusView')

const getBoardNameById = async (id) => {
  const boardData = await fetchUtils.getBoards(id)
  return boardData.data.name
}

watch(
  () => route.params.boardId,
  async (newBoardId) => {
    boardId.value = newBoardId
    if (newBoardId) {
      boardName.value = await getBoardNameById(newBoardId)
    }
  }
)
</script>

<style scoped>
</style>
