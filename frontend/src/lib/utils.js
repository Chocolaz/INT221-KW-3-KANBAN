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
    console.log('User is not authenticated.')
    return false
  }

  if (boardData && boardData.data && boardData.data.owner) {
    const ownerName = boardData.data.owner.name.trim()
    const currentUserName = currentUser.trim()

    console.log('Owner Name:', ownerName, 'Current User Name:', currentUserName)

    return ownerName === currentUserName
  } else {
    console.log('No owner found in boardData.')
    return false
  }
}

export const checkAccessRight = (collaborators, currentUser) => {
  if (!currentUser) {
    console.log('User is not authenticated or not provided.')
    return false
  }

  if (collaborators && collaborators.length > 0) {
    console.log('Current User:', currentUser.trim())

    const hasWriteAccess = collaborators.some((collab) => {
      console.log('Checking collaborator:', collab.name, collab.access_right)

      return (
        collab.name.trim() === currentUser.trim() &&
        collab.access_right === 'WRITE'
      )
    })

    if (hasWriteAccess) {
      console.log('User has WRITE access.')
      return true
    }
  }

  console.log('collaborators', collaborators)
  console.log('User does not have WRITE access.')
  return false
}
