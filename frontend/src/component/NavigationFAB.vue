<template>
  <div>
    <div v-if="isStatusList">
      <div
        class="fab itbkk-manage-status fixed bottom-40 right-8 w-14 h-14 rounded-full flex items-center justify-center text-white text-2xl cursor-pointer"
        @click="backToHomePage"
      >
        <i class="fa fa-home"></i>
      </div>
    </div>

    <div v-if="isTaskList">
      <div
        class="fab fixed bottom-40 right-8 w-14 h-14 rounded-full flex items-center justify-center text-white text-2xl cursor-pointer"
        @click="goToStatusManagement"
      >
        <i class="fa fa-cog"></i>
      </div>
    </div>
  </div>
</template>

<script setup>
import { useRoute, useRouter } from 'vue-router'
import { useRouteChecks } from '../lib/utils'

const { isTaskList, isStatusList } = useRouteChecks()
const route = useRoute()
const router = useRouter()

const backToHomePage = () => {
  const boardId = route.params.boardId
  router.push({ name: 'taskView', params: { boardId } })
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
