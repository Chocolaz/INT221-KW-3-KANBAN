<script setup>
import { defineProps, defineEmits, ref, onMounted, computed, watch } from 'vue'
import fetchUtils from '../lib/fetchUtils'
import Toast from './Toast.vue'
import { useRoute } from 'vue-router'

const props = defineProps({
  isOpen: Boolean,
  statusIdToTransfer: Number
})

const emit = defineEmits(['closeModal', 'statusTransfered'])

const route = useRoute()
const boardId = ref(route.params.boardId)
const showToast = ref(false)
const statusCode = ref(0)
const operationType = ref(null)
const existingStatuses = ref([])
const selectedStatusId = ref('')

const resetSelectedStatus = () => {
  selectedStatusId.value = ''
}

const closeModal = () => {
  emit('closeModal')
  resetSelectedStatus()
}

const fetchExistingStatuses = async () => {
  try {
    if (!boardId.value) {
      throw new Error('Board ID is required')
    }

    const response = await fetchUtils.fetchData('statuses', boardId.value)
    existingStatuses.value = response
  } catch (error) {
    console.error('Error fetching existing statuses:', error)
  }
}

onMounted(() => {
  fetchExistingStatuses()
})

watch(
  () => props.isOpen,
  (newValue) => {
    if (newValue) {
      fetchExistingStatuses()
    }
  }
)

const transferStatus = async () => {
  operationType.value = 'transfer'
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
      selectedStatusId.value,
      boardId.value
    )

    statusCode.value = response.statusCode
    if (response.success) {
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
      showToast.value = true
      closeModal()
    }
    if (error.message.includes('500')) {
      statusCode.value = 500
      showToast.value = true
      closeModal()
    }
  }
}

const filteredStatuses = computed(() => {
  return existingStatuses.value.filter(
    (status) => status.id !== props.statusIdToTransfer
  )
})

const defaultStatusName = computed(() => {
  const defaultStatus = existingStatuses.value.find(
    (status) => status.id === props.statusIdToTransfer
  )
  return defaultStatus ? defaultStatus.name : 'Unknown Status'
})
</script>

<template>
  <div
    v-if="isOpen"
    class="fixed inset-0 z-50 flex items-center justify-center bg-black bg-opacity-50"
  >
    <div class="bg-white rounded-lg shadow-lg p-6 max-w-sm w-full">
      <h2 class="text-lg font-semibold mb-4">Transfer and Delete</h2>
      <p class="text-left mb-4">
        There are tasks in
        <span class="font-semibold">{{ defaultStatusName }}</span> status. In
        order to delete this status, the system must transfer tasks in this
        status to an existing status.
      </p>
      <select
        v-model="selectedStatusId"
        class="mb-4 block w-full py-2 px-3 border border-gray-300 bg-white rounded-md shadow-sm focus:outline-none focus:border-blue-500 sm:text-sm"
      >
        <option value="" disabled selected>
          Please select a status to transfer tasks
        </option>
        <option
          v-for="status in filteredStatuses"
          :key="status.id"
          :value="status.id"
          class="py-2"
        >
          {{ status.name }}
        </option>
      </select>

      <div class="flex justify-end space-x-2">
        <button
          type="button"
          class="px-4 py-2 bg-gray-300 text-gray-800 rounded-md"
          @click="closeModal"
        >
          Cancel
        </button>
        <button
          type="button"
          class="px-4 py-2 bg-red-500 text-white hover:bg-red-600 rounded-md"
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
