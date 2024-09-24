<template>
  <div>
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
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'

const route = useRoute()
const router = useRouter()

const isTaskList = computed(() => route.name === 'taskView')
const isStatusList = computed(() => route.name === 'statusView')

const backToHomePage = () => {
  const boardId = route.params.boardId
  if (boardId) {
    router.push(`/boards/${boardId}/tasks`)
  } else {
    console.error('Board ID is not defined')
    router.push({ name: 'taskView' })
  }
}

const goToStatusManagement = () => {
  const boardId = route.params.boardId
  router.push({ name: 'statusView', params: { boardId } })
}
</script>

<style scoped>
.fab {
  background: linear-gradient(45deg, #ff6b6b, #f06543);
  transition: all 0.3s ease;
  box-shadow: 0 4px 15px rgba(255, 107, 107, 0.2);
}

.fab:hover {
  transform: scale(1.1) rotate(360deg);
}
</style>
