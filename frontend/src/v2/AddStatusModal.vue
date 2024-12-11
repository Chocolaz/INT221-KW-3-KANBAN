<script setup>
import { defineProps, defineEmits, ref, computed } from 'vue'
import { useRoute } from 'vue-router'
import fetchUtils from '../lib/fetchUtils'
import Toast from './Toast.vue'

const props = defineProps({
  isAddOpen: Boolean
})

const emit = defineEmits(['closeModal', 'statusAdded'])

const route = useRoute()
const boardId = route.params.boardId

const statusName = ref('')
const statusDescription = ref('')
const statusCode = ref(0)
const operationType = ref(null)
const showToast = ref(false)

const closeModal = () => {
  statusName.value = ''
  statusDescription.value = ''
  emit('closeModal')
}

const isSaveDisabled = computed(() => {
  return (
    !statusName.value.trim() ||
    statusName.value.length > 50 ||
    statusDescription.value.length > 200
  )
})

const addStatus = async () => {
  operationType.value = 'add'
  try {
    const existingStatuses = await fetchUtils.fetchData('statuses', boardId)
    const existingStatusNames = existingStatuses.map((status) => status.name)

    if (existingStatusNames.includes(statusName.value)) {
      alert('Status name must be unique. Please enter a different name.')
      return
    }

    const newStatus = {
      name: statusName.value,
      description: statusDescription.value
    }
    const response = await fetchUtils.postData('statuses', boardId, newStatus)
    statusCode.value = response.statusCode
    if (response.success) {
      closeModal()
      emit('statusAdded')
      showToast.value = true
    }
  } catch (error) {
    console.error('Error adding status:', error)
    alert('An error has occurred, the status could not be added.')
    closeModal()
  }
}
</script>

<template>
  <div class="fixed inset-0 flex items-center justify-center" v-if="isAddOpen">
    <div class="fixed inset-0 bg-black opacity-50"></div>
    <div
      class="bg-white rounded-lg p-6 max-w-md w-full relative z-10 animate-fade-in-up"
    >
      <h2 class="text-lg font-semibold mb-4 text-red-600">Add Status</h2>
      <form @submit.prevent="addStatus" class="itbkk-modal-status">
        <div class="mb-4">
          <label for="statusName" class="block font-semibold mb-1 text-left"
            >Name</label
          >
          <input
            type="text"
            id="statusName"
            v-model.trim="statusName"
            maxlength="50"
            class="w-full border rounded-md p-2 font-medium"
            placeholder="Enter status name"
          />
          <small v-if="statusName.length > 50" class="text-red-500"
            >Name must be at most 50 characters long.</small
          >
        </div>
        <div class="mb-4">
          <label
            for="statusDescription"
            class="block font-semibold mb-1 text-left"
            >Description</label
          >
          <textarea
            id="statusDescription"
            v-model.trim="statusDescription"
            maxlength="200"
            class="w-full border rounded-md p-2 font-medium"
            rows="4"
            placeholder="Enter status description"
          ></textarea>
          <small v-if="statusDescription.length > 200" class="text-red-500"
            >Description must be at most 200 characters long.</small
          >
        </div>
        <div class="flex justify-end">
          <button
            type="button"
            class="px-4 py-2 bg-gray-300 text-gray-800 rounded-md mr-2 itbkk-button-cancel"
            @click="closeModal"
          >
            Cancel
          </button>
          <button
            type="submit"
            :class="[
              'px-4 py-2 rounded-md itbkk-button-confirm',
              isSaveDisabled
                ? 'bg-gray-400 text-gray-600 cursor-not-allowed'
                : 'bg-blue-500 text-white hover:bg-blue-600'
            ]"
            :disabled="isSaveDisabled"
          >
            Add Status
          </button>
        </div>
      </form>
    </div>
  </div>
  <Toast
    :show="showToast"
    :statusCode="statusCode"
    :operationType="operationType"
    @close="showToast = false"
  />
</template>
