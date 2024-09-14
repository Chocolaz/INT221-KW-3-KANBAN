<script setup>
import { defineProps, defineEmits, ref, onMounted, computed } from 'vue'
import fetchUtils from '../lib/fetchUtils'
import Toast from './Toast.vue'

const props = defineProps({
  isOpen: Boolean,
  statusIdToTransfer: Number
})

const emit = defineEmits(['closeModal', 'statusTransfered'])

import { useRoute } from 'vue-router'

const route = useRoute()
const boardId = ref(route.params.boardId)
const showToast = ref(false)
const statusCode = ref(0)
const operationType = ref(null)
const existingStatuses = ref([])
const selectedStatusId = ref(null)

const resetSelectedStatus = () => {
  selectedStatusId.value = null
}

const closeModal = () => {
  emit('closeModal')
  resetSelectedStatus()
}

const fetchExistingStatuses = async () => {
  try {
    // Ensure boardId is provided
    if (!boardId.value) {
      throw new Error('Board ID is required')
    }

    // Pass boardId to fetchData
    const response = await fetchUtils.fetchData('statuses', boardId.value)
    existingStatuses.value = response

    const defaultStatus = existingStatuses.value.find(
      (status) => status.statusId === props.statusIdToTransfer
    )
    selectedStatusId.value = defaultStatus ? defaultStatus.statusId : null
  } catch (error) {
    console.error('Error fetching existing statuses:', error)
  }
}

onMounted(() => {
  fetchExistingStatuses()
})

const transferStatus = async () => {
  operationType.value = 'transfer'
  console.log('OperationType', operationType.value)
  try {
    if (typeof props.statusIdToTransfer === 'undefined') {
      throw new Error('Status ID to transfer is not defined')
    }

    if (!selectedStatusId.value) {
      throw new Error('Please select a status to transfer tasks')
    }

    if (props.statusIdToTransfer === 1) {
      alert('The "No Status" status cannot be transferred')
      closeModal()
      throw new Error('The "No Status" status cannot be transferred')
    }

    const response = await fetchUtils.deleteAndTransferData(
      `statuses/${props.statusIdToTransfer}`,
      selectedStatusId.value
    )
    statusCode.value = response.statusCode
    if (response.success) {
      console.log(
        'Status transferred and deleted successfully!',
        statusCode.value
      )
      emit('statusTransfered')
      showToast.value = true
      closeModal()
    } else {
      throw new Error(response.data.message)
    }
  } catch (error) {
    console.error('Error transferring status:', error.message)
    if (error.message.includes('404')) {
      statusCode.value = 404
      console.log(statusCode.value)
      showToast.value = true
      closeModal()
    }
    if (error.message.includes('500')) {
      statusCode.value = 500
      console.log(statusCode.value)
      showToast.value = true
      closeModal()
    }
  }
}

const filteredStatuses = computed(() => {
  return existingStatuses.value.filter(
    (status) => status.statusId !== props.statusIdToTransfer
  )
})

const defaultStatusName = computed(() => {
  const defaultStatus = existingStatuses.value.find(
    (status) => status.statusId === props.statusIdToTransfer
  )
  return defaultStatus ? defaultStatus.statusName : ''
})
</script>

<template>
  <div class="modal-mask" v-if="isOpen">
    <div class="itbkk-message">
      <h2 class="text-lg font-semibold mb-4">Transfer and Delete</h2>
      <p class="text-left mb-4">
        There are tasks in
        <span class="font-semibold">{{ defaultStatusName }}</span> status. In
        order to delete this status, the system must transfer tasks in this
        status to existing status.
      </p>
      <select
        v-model="selectedStatusId"
        class="mb-4 block w-full py-2 px-3 border border-gray-300 bg-white rounded-md shadow-sm focus:outline-none focus:border-blue-500 sm:text-sm"
      >
        <option value="null" selected disabled hidden>
          Select to transfer
        </option>
        <option
          v-for="status in filteredStatuses"
          :key="status.statusId"
          :value="status.statusId"
          class="py-2"
        >
          {{ status.statusName }}
        </option>
      </select>

      <div class="flex justify-end">
        <button
          type="button"
          class="px-4 py-2 bg-gray-300 text-gray-800 rounded-md mr-2 itbkk-button-cancel"
          @click="closeModal"
        >
          Cancel
        </button>
        <button
          type="button"
          class="px-4 py-2 bg-red-500 text-white hover:bg-red-600 rounded-md itbkk-button-confirm"
          @click="transferStatus"
        >
          Transfer and Delete
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
.modal-mask {
  position: fixed;
  z-index: 9998;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(0, 0, 0, 0.5);
  display: flex;
  justify-content: center;
  align-items: center;
}

.itbkk-message {
  width: 400px;
  padding: 20px 30px;
  background-color: #fff;
  border-radius: 2px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.33);
}
</style>
