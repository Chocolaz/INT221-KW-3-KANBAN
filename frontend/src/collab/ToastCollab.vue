<template>
  <div class="fixed top-10 right-4 z-50">
    <div
      v-if="showToast"
      class="rounded-md p-4 max-w-sm shadow-lg transition-all duration-300"
      :class="toastClasses"
    >
      <div class="flex items-center">
        <div class="flex-shrink-0">
          <!-- Success Icon -->
          <svg
            v-if="isSuccess"
            class="h-5 w-5 text-green-400"
            viewBox="0 0 20 20"
            fill="currentColor"
          >
            <path
              fill-rule="evenodd"
              d="M10 18a8 8 0 100-16 8 8 0 000 16zm3.707-9.293a1 1 0 00-1.414-1.414L9 10.586 7.707 9.293a1 1 0 00-1.414 1.414l2 2a1 1 0 001.414 0l4-4z"
              clip-rule="evenodd"
            />
          </svg>
          <!-- Error Icon -->
          <svg
            v-else
            class="h-5 w-5 text-red-400"
            viewBox="0 0 20 20"
            fill="currentColor"
          >
            <path
              fill-rule="evenodd"
              d="M10 18a8 8 0 100-16 8 8 0 000 16zM8.707 7.293a1 1 0 00-1.414 1.414L8.586 10l-1.293 1.293a1 1 0 101.414 1.414L10 11.414l1.293 1.293a1 1 0 001.414-1.414L11.414 10l1.293-1.293a1 1 0 00-1.414-1.414L10 8.586 8.707 7.293z"
              clip-rule="evenodd"
            />
          </svg>
        </div>
        <div class="ml-3">
          <p class="text-sm font-medium" :class="textColorClass">
            {{ message }}
          </p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()
const showToast = ref(false)
const message = ref('')
const status = ref(null)

const isSuccess = computed(() => status.value === 200)

const toastClasses = computed(() => ({
  'bg-green-50': isSuccess.value,
  'bg-red-50': !isSuccess.value
}))

const textColorClass = computed(() =>
  isSuccess.value ? 'text-green-800' : 'text-red-800'
)

const handleResponse = async (statusCode, closeModal, updateTable) => {
  status.value = statusCode

  switch (statusCode) {
    case 200:
      message.value = 'Successfully added'
      if (updateTable) await updateTable()
      if (closeModal) closeModal()
      break

    case 401:
      message.value = 'Please Log in'
      if (closeModal) closeModal()
      router.push('/login')
      break

    case 403:
      message.value = 'You do not have permission to add board collaborator.'
      if (closeModal) closeModal()
      break

    case 404:
      message.value = 'The user does not exists.'
      break

    case 409:
      message.value = 'The user is already the collaborator of this board.'
      break

    default:
      message.value = 'An unexpected error occurred.'
      console.log('Unexpected status code:', statusCode)
      break
  }

  showToast.value = true

  // Hide toast after 3 seconds
  setTimeout(() => {
    showToast.value = false
    message.value = ''
  }, 3000)
}

defineExpose({
  handleResponse
})
</script>
