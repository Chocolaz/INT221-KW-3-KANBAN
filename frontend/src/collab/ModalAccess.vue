<template>
  <div
    class="fixed inset-0 bg-gray-600 bg-opacity-50 flex justify-center items-center"
  >
    <div class="bg-white rounded-lg shadow-lg p-6 max-w-md">
      <p class="text-lg font-semibold mb-4">
        Do you want to change the access right of {{ name }} to {{ newRight }}?
      </p>
      <div v-if="errorMessage" class="text-red-500 text-center mb-4">
        {{ errorMessage }}
      </div>
      <div class="flex justify-end gap-4">
        <button
          @click="confirmAccessRightChange"
          class="bg-red-600 text-white px-4 py-2 rounded hover:bg-red-500"
        >
          Confirm
        </button>
        <button
          @click="$emit('close')"
          class="bg-gray-400 text-white px-4 py-2 rounded hover:bg-gray-500"
        >
          Cancel
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import fetchUtils from '../lib/fetchUtils'

const props = defineProps({
  boardId: String,
  collabId: String,
  name: String,
  newRight: String
})

const emit = defineEmits(['confirmed', 'close'])

const errorMessage = ref('')

const confirmAccessRightChange = async () => {
  try {
    const response = await fetchUtils.updateCollabAccess(
      props.boardId,
      props.collabId,
      props.newRight
    )

    if (response.success && response.statusCode === 200) {
      emit('confirmed')
      emit('close')
    } else {
      errorMessage.value = 'There is a problem. Please try again later.'
    }
  } catch (error) {
    if (error.response?.statusCode === 403) {
      errorMessage.value =
        'You do not have permission to change collaborator access right.'
    }
    if (error.response?.statusCode === 401) {
      errorMessage.value = 'Please login.'
      localStorage.clear()
      setTimeout(() => {
        router.push({ name: 'loginView' })
      }, 3000)
    } else {
      errorMessage.value = 'There is a problem. Please try again later.'
    }
  }
}
</script>
