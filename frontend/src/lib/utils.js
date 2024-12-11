import { computed } from 'vue'
import { useRoute } from 'vue-router'

export const useRouteChecks = () => {
  const route = useRoute()

  const isTaskList = computed(() => route.name === 'taskView')
  const isStatusList = computed(() => route.name === 'statusView')
  const isManageCollab = computed(() => route.name === 'manageCollab')

  return { isTaskList, isStatusList, isManageCollab }
}

export const checkOwnership = (boardData, currentUser) => {
  if (!currentUser) {
    return false
  }

  if (boardData && boardData.data && boardData.data.owner) {
    const ownerName = boardData.data.owner.name.trim()
    const currentUserName = currentUser.trim()

    return ownerName === currentUserName
  } else {
    return false
  }
}

export const checkAccessRight = (collaborators, currentUser) => {
  if (!currentUser) {
    return false
  }

  if (collaborators && collaborators.length > 0) {
    const hasWriteAccess = collaborators.some((collab) => {
      return (
        collab.name.trim() === currentUser.trim() &&
        collab.access_right === 'WRITE'
      )
    })

    if (hasWriteAccess) {
      return true
    }
  }

  return false
}
