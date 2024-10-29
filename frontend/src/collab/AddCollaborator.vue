<template>
  <div
    class="fixed inset-0 flex items-center justify-center bg-black bg-opacity-50"
  >
    <div class="bg-white p-6 rounded-lg shadow-lg">
      <h3 class="text-xl mb-4">Add Collaborator</h3>
      <input
        type="email"
        v-model="collabEmail"
        placeholder="Email"
        class="border border-gray-300 p-2 w-full mb-4"
      />
      <select
        v-model="accessRight"
        class="border border-gray-300 p-2 w-full mb-4"
      >
        <option value="READ">READ</option>
        <option value="WRITE">WRITE</option>
      </select>
      <div class="flex justify-between">
        <button @click="close" class="bg-gray-300 py-2 px-4 rounded">
          Cancel
        </button>
        <button
          @click="addCollaborator"
          class="bg-blue-600 text-white py-2 px-4 rounded disabled:opacity-50"
          :disabled="!isEmailValid || collabEmail === ownerEmail"
        >
          Add
        </button>
      </div>
      <ToastCollab
        :status="toastStatus"
        :message="toastMessage"
        @hideComplete="resetToast"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import fetchUtils from '../lib/fetchUtils'
import ToastCollab from './ToastCollab.vue'

const emit = defineEmits(['close', 'collab-added'])
const route = useRoute()
const router = useRouter()

const collabEmail = ref('')
const accessRight = ref('READ')
const ownerEmail = ref(localStorage.getItem('email') || '')
const boardId = ref(route.params.boardId)
const toastMessage = ref('')
const toastStatus = ref(0)

const isEmailValid = computed(() => {
  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
  return (
    collabEmail.value &&
    collabEmail.value.length <= 50 &&
    emailRegex.test(collabEmail.value)
  )
})

const resetToast = () => {
  toastStatus.value = 0
  toastMessage.value = ''
}

const handleError = (status) => {
  switch (status) {
    case 401:
      localStorage.clear()
      toastMessage.value = 'Please login'
      setTimeout(() => {
        router.push({ name: 'loginView' })
      }, 3000)
      break
    case 403:
      toastMessage.value =
        'You do not have permission to add board collaborator.'
      setTimeout(() => emit('close'), 3000)
      break
    case 404:
      toastMessage.value = 'The user does not exist.'
      break
    case 409:
      toastMessage.value = 'The user is already the collaborator of this board.'
      break
    default:
      toastMessage.value = 'There is a problem. Please try again later.'
      setTimeout(() => emit('close'), 3000)
  }
  toastStatus.value = status
}

const addCollaborator = async () => {
  try {
    await fetchUtils.addCollab(boardId.value, {
      email: collabEmail.value,
      access_right: accessRight.value
    })
    emit('collab-added')
    emit('close')
  } catch (error) {
    console.error('Error adding collaborator:', error)
    const statusMatch = error.message.match(/Status: (\d+)/)
    const status = statusMatch ? parseInt(statusMatch[1]) : 500
    handleError(status)
  }
}

const close = () => {
  emit('close')
}
</script>
