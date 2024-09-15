<script setup>
import { defineProps, defineEmits, ref } from 'vue'
import fetchUtils from '../lib/fetchUtils'
import Toast from './Toast.vue'
import { useRoute } from 'vue-router'

const ERROR_MESSAGES = {
  NO_STATUS: 'The "No Status" status cannot be deleted',
  DONE_STATUS: 'The "Done" status cannot be deleted',
  UNKNOWN: 'Unknown error occurred'
}

const route = useRoute()
const boardId = route.params.boardId

const props = defineProps({
  isOpen: Boolean,
  statusIdToDelete: Number
})

const emit = defineEmits(['closeModal', 'statusDeleted'])

const showToast = ref(false)
const statusCode = ref(0)
const operationType = ref(null)

const closeModal = () => {
  emit('closeModal')
}

const deleteStatus = async () => {
  operationType.value = 'delete'
  console.log('OperationType:', operationType.value)

  try {
    const { statusIdToDelete } = props

    if (statusIdToDelete === undefined) {
      throw new Error('Status ID to delete is not defined')
    }

    const rawStatuses = await fetchUtils.fetchData('statuses', boardId)
    const statusToDelete = rawStatuses.find(
      (status) => status.id === statusIdToDelete
    )

    if (!statusToDelete) {
      throw new Error('Status not found')
    }

    const statusName = statusToDelete.name.toUpperCase()

    if (statusName === 'NO STATUS') {
      showErrorAndClose(ERROR_MESSAGES.NO_STATUS)
      return
    }

    if (statusName === 'DONE') {
      showErrorAndClose(ERROR_MESSAGES.DONE_STATUS)
      return
    }

    const response = await fetchUtils.deleteData(
      `statuses/${statusIdToDelete}`,
      boardId
    )

    statusCode.value = response.statusCode
    if (response.success) {
      console.log('Status deleted successfully!', statusCode.value)
      emit('statusDeleted')
      showToast.value = true
      closeModal()
    } else {
      throw new Error(response.data?.message || ERROR_MESSAGES.UNKNOWN)
    }
  } catch (error) {
    console.error('Error deleting status:', error.message)
    handleErrors(error.message)
    showToast.value = true
    closeModal()
  }
}

const showErrorAndClose = (message) => {
  alert(message)
  closeModal()
}

const handleErrors = (message) => {
  if (message.includes('404')) {
    statusCode.value = 404
  } else if (message.includes('500')) {
    statusCode.value = 500
  } else {
    statusCode.value = 'unknown'
  }
  console.log(statusCode.value)
}
</script>

<template>
  <div
    class="fixed inset-0 flex items-center justify-center bg-black bg-opacity-50 z-50"
    v-if="isOpen"
  >
    <div
      class="bg-white p-6 rounded-lg shadow-lg w-full max-w-sm animate-fade-in-up"
    >
      <h2 class="text-lg font-semibold mb-4 text-red-600">Delete Status</h2>
      <p class="text-left mb-4">Are you sure you want to delete the status?</p>
      <div class="flex justify-end space-x-2">
        <button
          type="button"
          class="px-4 py-2 bg-gray-300 text-gray-800 rounded-md hover:bg-gray-400"
          @click="closeModal"
        >
          Cancel
        </button>
        <button
          type="button"
          class="px-4 py-2 bg-red-500 text-white rounded-md hover:bg-red-600"
          @click="deleteStatus"
        >
          Confirm
        </button>
      </div>
    </div>
  </div>
  <Toast
    :show="showToast"
    :statusCode="statusCode"
    :operationType="operationType"
    @close="showToast = false"
  />
</template>

<style scoped>
@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translate3d(0, 20px, 0);
  }
  to {
    opacity: 1;
    transform: translate3d(0, 0, 0);
  }
}

.animate-fade-in-up {
  animation: fadeInUp 0.3s ease-out;
}
</style>
