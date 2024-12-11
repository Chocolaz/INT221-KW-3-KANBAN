<script setup>
import { ref } from 'vue'
import fetchUtils from '../lib/fetchUtils'
import { useRouter } from 'vue-router'

const props = defineProps({
  boardId: String,
  collabId: String,
  collabName: String
})

const emit = defineEmits(['confirmed', 'close'])

const errorMessage = ref('')
const router = useRouter()

const handleError = (status) => {
  switch (status) {
    case 401:
      localStorage.clear()
      errorMessage.value = 'Please login.'
      setTimeout(() => {
        router.push({ name: 'loginView' })
      }, 3000)
      break
    case 403:
      errorMessage.value =
        'You do not have permission to remove this collaborator.'
      setTimeout(() => emit('close'), 3000)
      break
    case 404:
      errorMessage.value = `${props.collabName} is not a collaborator.`
      setTimeout(() => {
        emit('confirmed')
        emit('close')
      }, 3000)
      break
  }
}

const removeCollaborator = async () => {
  try {
    const response = await fetchUtils.removeCollab(
      props.boardId,
      props.collabId
    )

    if (response.statusCode === 200) {
      emit('confirmed')
      emit('close')
    }
  } catch (error) {
    console.error('Error removing collaborator:', error.message)
    const statusMatch = error.message.match(/Status: (\d+)/)
    const status = statusMatch ? parseInt(statusMatch[1]) : null
    if (status) {
      handleError(status)
    } else {
      errorMessage.value = 'There is a problem. Please try again later.'
    }
  }
}
</script>

<template>
  <div
    class="fixed inset-0 bg-gray-600 bg-opacity-50 flex justify-center items-center"
  >
    <div class="bg-white rounded-lg shadow-lg p-6 max-w-md">
      <p class="text-lg font-semibold mb-4">
        Are you sure you want to remove {{ collabName }} as a collaborator?
      </p>
      <div v-if="errorMessage" class="text-red-500 text-center mb-4">
        {{ errorMessage }}
      </div>
      <div class="flex justify-end gap-4">
        <button
          @click="removeCollaborator"
          class="bg-red-600 text-white px-4 py-2 rounded hover:bg-red-500"
        >
          Yes, Remove
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
