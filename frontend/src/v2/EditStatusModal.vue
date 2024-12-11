<script setup>
import { ref, watch, computed } from 'vue'
import fetchUtils from '../lib/fetchUtils'
import Toast from './Toast.vue'
import { useRoute } from 'vue-router'

const route = useRoute()
const boardId = route.params.boardId

const props = defineProps({
  isOpen: Boolean,
  statusData: Object,
  selectedStatusIdToEdit: Number
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
      editedStatus.value = {
        statusName: newValue.name || '',
        statusDescription: newValue.description || ''
      }
      initialStatus.value = { ...editedStatus.value }
    }
  },
  { immediate: true }
)

const isSaveDisabled = computed(() => {
  const { statusName, statusDescription } = editedStatus.value
  return (
    JSON.stringify(editedStatus.value) ===
      JSON.stringify(initialStatus.value) ||
    statusName.length > 50 ||
    statusDescription.length > 200 ||
    statusName === 'TO DO' ||
    statusName === 'DONE'
  )
})

const restrictedStatuses = ['NO STATUS', 'DONE']

const saveChanges = async () => {
  operationType.value = 'edit'
  try {
    if (!boardId) throw new Error('Board ID is required')

    const originalStatusName = props.statusData.name?.toUpperCase() || ''

    if (restrictedStatuses.includes(originalStatusName)) {
      const statusErrorMessage = `The "${props.statusData.name}" status cannot be edited.`
      alert(statusErrorMessage)
      emit('closeModal')
      return
    }

    const rawStatuses = await fetchUtils.fetchData('statuses', boardId)
    console.log('Raw statuses:', rawStatuses)

    const existingStatusNames = rawStatuses
      .filter((status) => status.name)
      .map((status) => status.name.toUpperCase())

    console.log('Existing status names:', existingStatusNames)

    const editedStatusName = editedStatus.value.statusName?.toUpperCase() || ''
    const initialStatusName =
      initialStatus.value.statusName?.toUpperCase() || ''

    if (
      editedStatusName !== initialStatusName &&
      existingStatusNames.includes(editedStatusName)
    ) {
      alert('Status name must be unique. Please enter a different name.')
      return
    }

    const response = await fetchUtils.putData(
      `statuses/${props.selectedStatusIdToEdit}`,
      boardId,
      editedStatus.value
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
  <div
    v-if="isOpen"
    class="fixed inset-0 flex items-center justify-center z-50 bg-black bg-opacity-50"
  >
    <div
      class="bg-white rounded-lg p-6 max-w-md w-full relative border-2 border-red-500 animate-[fadeInUp_0.3s_ease-out]"
    >
      <h2 class="text-lg font-semibold mb-4 text-red-600">Edit Status</h2>
      <div v-if="editedStatus">
        <form @submit.prevent="saveChanges">
          <div class="mb-4">
            <label for="statusName" class="block font-semibold mb-1 text-left"
              >Name:</label
            >
            <input
              v-model="editedStatus.statusName"
              type="text"
              id="statusName"
              maxlength="50"
              class="w-full border rounded-md p-2 font-medium"
            />
          </div>
          <div class="mb-4">
            <label
              for="statusDescription"
              class="block font-semibold mb-1 text-left"
              >Description:</label
            >
            <textarea
              v-model="editedStatus.statusDescription"
              id="statusDescription"
              rows="4"
              maxlength="200"
              class="w-full border rounded-md p-2 font-medium"
              placeholder="No description provided"
            ></textarea>
          </div>
          <div class="flex justify-end">
            <button
              type="button"
              @click="emit('closeModal')"
              class="px-4 py-2 bg-gray-300 text-gray-800 rounded-md mr-2"
            >
              Cancel
            </button>
            <button
              :disabled="isSaveDisabled"
              type="submit"
              class="px-4 py-2 rounded-md"
              :class="
                isSaveDisabled
                  ? 'bg-gray-400 text-gray-600 cursor-not-allowed'
                  : 'bg-blue-500 text-white hover:bg-blue-600'
              "
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


