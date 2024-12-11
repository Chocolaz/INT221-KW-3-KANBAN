<script setup>
import { useRoute, useRouter } from 'vue-router'
import { useRouteChecks } from '../lib/utils'

const { isTaskList, isManageCollab, isStatusList } = useRouteChecks()
const route = useRoute()
const router = useRouter()

const goToManageCollab = () => {
  const boardId = route.params.boardId
  router.push({ name: 'manageCollab', params: { boardId } })
}

const goToTaskList = () => {
  const boardId = route.params.boardId
  router.push({ name: 'taskView', params: { boardId } })
}
</script>

<template>
  <div>
    <div v-if="isTaskList || isStatusList">
      <div
        class="fab fixed bottom-8 right-8 w-14 h-14 rounded-full flex items-center justify-center text-white text-2xl cursor-pointer"
        @click="goToManageCollab"
      >
        <i class="fa fa-users"></i>
      </div>
    </div>

    <div v-if="isManageCollab">
      <div
        class="fab fixed bottom-8 right-8 w-14 h-14 rounded-full flex items-center justify-center text-white text-2xl cursor-pointer"
        @click="goToTaskList"
      >
        <i class="fa fa-tasks"></i>
      </div>
    </div>
  </div>
</template>

<style scoped>
.fab {
  background: linear-gradient(
    45deg,
    #ff4e50,
    #fc6a73,
    #ff9966
  );
  transition: all 0.3s ease;
  box-shadow: 0 4px 15px rgba(252, 106, 115, 0.2); 
}

.fab:hover {
  transform: scale(1.1) rotate(360deg);
}
</style>
