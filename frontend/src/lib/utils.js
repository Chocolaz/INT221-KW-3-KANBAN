import { computed } from 'vue'
import { useRoute } from 'vue-router'

export const useRouteChecks = () => {
  const route = useRoute()

  const isTaskList = computed(() => route.name === 'taskView')
  const isStatusList = computed(() => route.name === 'statusView')
  return { isTaskList, isStatusList }
}

export const checkOwnership = (boardData, currentUser) => {
  if (!currentUser) {
    console.log('User is not authenticated.')
    return false // Return false for non-authenticated users
  }

  if (boardData && boardData.data && boardData.data.owner) {
    const ownerName = boardData.data.owner.name.trim()
    const currentUserName = currentUser.trim()

    console.log(
      '**',
      'Owner Name:',
      ownerName,
      '**',
      '**',
      'Current User Name:',
      currentUserName,
      '**'
    )

    return ownerName === currentUserName
  } else {
    console.log('No owner found in boardData.')
    return false
  }
}
