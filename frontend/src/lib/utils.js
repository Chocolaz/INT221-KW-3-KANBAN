import { computed } from 'vue'
import { useRoute } from 'vue-router'

export const useRouteChecks = () => {
  const route = useRoute()

  const isTaskList = computed(() => route.name === 'taskView')
  const isStatusList = computed(() => route.name === 'statusView')
  return { isTaskList, isStatusList }
}
