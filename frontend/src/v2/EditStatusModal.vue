<script setup>
import { defineProps, defineEmits, ref, watch, computed } from 'vue'
import fetchUtils from '../lib/fetchUtils'
import Toast from './Toast.vue'

const props = defineProps({
  isOpen: Boolean,
  statusData: Object,
  selectedStatusIdToEdit: Number,
  boardId: String 
})

const emit = defineEmits(['closeModal', 'statusEdited'])

const editedStatus = ref({ statusName: '', statusDescription: '' })
const initialStatus = ref({ statusName: '', statusDescription: '' })

const showToast = ref(false)
const statusCode = ref(0)
const operationType = ref(null)

watch(
  () => props.statusData,
  (newValue) => {
    if (newValue) {
      editedStatus.value = { ...newValue }
      initialStatus.value = { ...newValue }
    } else {
      editedStatus.value = { statusName: '', statusDescription: '' }
      initialStatus.value = { statusName: '', statusDescription: '' }
    }
  }
)

const isSaveDisabled = computed(() => {
  const statusName = editedStatus.value.statusName || ''
  const statusDescription = editedStatus.value.statusDescription || ''
  return (
    JSON.stringify(editedStatus.value) ===
      JSON.stringify(initialStatus.value) ||
    statusName.length > 50 ||
    statusDescription.length > 200
  )
})

const saveChanges = async () => {
  operationType.value = 'edit'
  try {
    if (props.selectedStatusIdToEdit === 1) {
      alert('The "No Status" status cannot be edited')
      emit('closeModal')
      throw new Error('The "No Status" status cannot be edited')
    }

    if (props.selectedStatusIdToEdit === 6) {
      alert('The "DONE" status cannot be edited')
      emit('closeModal')
      throw new Error('The "DONE" status cannot be edited') // Fixed the error message
    }

    const existingStatuses = await fetchUtils.fetchData(
      'statuses',
      props.boardId
    )
    const existingStatusNames = existingStatuses.map(
      (status) => status.statusName
    )

    if (editedStatus.value.statusName !== initialStatus.value.statusName) {
      if (existingStatusNames.includes(editedStatus.value.statusName)) {
        alert('Status name must be unique. Please enter a different name.')
        return
      }
    }

    const response = await fetchUtils.putData(
      `statuses/${props.selectedStatusIdToEdit}`,
      editedStatus.value,
      props.boardId // Ensure boardId is passed correctly
    )

    statusCode.value = response.statusCode
    if (response.success) {
      emit('closeModal')
      emit('statusEdited')
      showToast.value = true
    } else {
      console.error('Failed to update status:', response.data)
    }
  } catch (error) {
    console.error('Error updating status:', error.message)
    if (error.message.includes('404')) {
      statusCode.value = 404
      showToast.value = true
      emit('closeModal')
    }
  }
}
</script>

<template>
  <div v-if="isOpen" class="fixed inset-0 flex items-center justify-center">
    <div class="fixed inset-0 bg-black opacity-50"></div>
    <div class="bg-white rounded-lg p-6 max-w-md w-full relative z-10">
      <h2 class="text-lg font-semibold mb-4">Edit Status</h2>
      <div v-if="editedStatus">
        <form @submit.prevent="saveChanges" class="itbkk-modal-status">
          <div class="mb-4">
            <label for="statusName" class="block font-semibold mb-1 text-left"
              >Name:</label
            >
            <input
              v-model="editedStatus.statusName"
              type="text"
              id="statusName"
              name="statusName"
              maxlength="50"
              class="w-full border rounded-md p-2 font-medium itbkk-status-name"
            />
            <small
              v-if="
                editedStatus.statusName && editedStatus.statusName.length > 50
              "
              class="text-red-500"
            >
              Name must be at most 50 characters long.
            </small>

            <small
              v-if="
                editedStatus.statusDescription &&
                editedStatus.statusDescription.length > 200
              "
              class="text-red-500"
            >
              Description must be at most 200 characters long.
            </small>
          </div>
          <div class="mb-4">
            <label
              for="statusDescription"
              class="block font-semibold mb-1 text-left itbkk-status-description"
            >
              Description:
            </label>
            <textarea
              v-model="editedStatus.statusDescription"
              id="statusDescription"
              name="statusDescription"
              rows="4"
              maxlength="200"
              class="w-full border rounded-md p-2 font-medium"
            ></textarea>
            <small
              v-if="editedStatus.statusDescription.length > 200"
              class="text-red-500"
            >
              Description must be at most 200 characters long.
            </small>
          </div>
          <div class="flex justify-end">
            <button
              type="button"
              @click="emit('closeModal')"
              class="px-4 py-2 bg-gray-300 text-gray-800 rounded-md mr-2 itbkk-button-cancel"
            >
              Cancel
            </button>
            <button
              :disabled="isSaveDisabled"
              type="submit"
              class="px-4 py-2 rounded-md itbkk-button-confirm"
              :class="{
                'bg-gray-400 text-gray-600 cursor-not-allowed': isSaveDisabled,
                'bg-blue-500 text-white hover:bg-blue-600': !isSaveDisabled
              }"
            >
              Save
            </button>
          </div>
        </form>
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
.modal-container {
  max-width: 80%;
}
</style>
