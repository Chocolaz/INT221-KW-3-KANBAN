import { computed } from 'vue'
import { useRoute } from 'vue-router'

export const useRouteChecks = () => {
  const route = useRoute()

  const isTaskList = computed(() => route.name === 'taskView')
  const isStatusList = computed(() => route.name === 'statusView')
  console.log('isTaskList:', isTaskList.value)
  console.log('isStatusList:', isStatusList.value)

  return { isTaskList, isStatusList }
}
