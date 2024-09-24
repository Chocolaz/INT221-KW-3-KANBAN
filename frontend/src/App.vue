<template>
  <Navbar v-if="showNavbar" />

  <div v-if="isTaskList || isStatusList">
    <h1 class="text-lg font-semibold text-red-600 mt-3 text-center">
      {{ boardName }}
    </h1>
  </div>

  <div id="app">
    <RouterView />
  </div>

  <div v-if="isStatusList">
    <div
      class="fab fixed bottom-8 right-8 w-14 h-14 rounded-full flex items-center justify-center text-white text-2xl cursor-pointer"
      @click="backToHomePage"
    >
      <i class="fa fa-home"></i>
    </div>
  </div>

  <div v-if="isTaskList">
    <div
      class="fab fixed bottom-8 right-8 w-14 h-14 rounded-full flex items-center justify-center text-white text-2xl cursor-pointer"
      @click="goToStatusManagement"
    >
      <i class="fa fa-cog"></i>
    </div>
  </div>
</template>

<script setup>
import { computed, ref, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import Navbar from './v3/navbar.vue'
import fetchUtils from './lib/fetchUtils'

const route = useRoute()
const router = useRouter()
const boardId = ref(route.params.boardId)

const showNavbar = computed(() => route.name !== 'loginView')

const isTaskList = computed(() => route.name === 'taskView')
const isStatusList = computed(() => route.name === 'statusView')

const boardName = ref('')

const backToHomePage = () => {
  if (boardId.value) {
    router.push(`/boards/${boardId.value}/tasks`)
  } else {
    console.error('Board ID is not defined')
    router.push({ name: 'taskView' })
  }
}

const goToStatusManagement = () => {
  router.push({ name: 'statusView', params: { boardId: boardId.value } })
}

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

<style>
#app {
  font-family: Avenir, Helvetica, Arial, sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  text-align: center;
  color: #2c3e50;
  margin-top: 0px;
  background-color: #ffff;
}

.fab {
  background: linear-gradient(45deg, #ff6b6b, #f06543);
  transition: all 0.3s ease;
  box-shadow: 0 4px 15px rgba(255, 107, 107, 0.2);
}

.fab:hover {
  transform: scale(1.1) rotate(360deg);
}
</style>
