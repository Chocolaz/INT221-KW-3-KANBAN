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
        @input="validateEmail"
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
          class="bg-blue-600 text-white py-2 px-4 rounded"
          :disabled="!isEmailValid || collabEmail === ownerEmail"
        >
          Add
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, defineEmits, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import fetchUtils from '../lib/fetchUtils'

const emit = defineEmits()
const collabEmail = ref('')
const accessRight = ref('READ')
const ownerEmail = ref('')
const boardId = ref('')

const route = useRoute()

onMounted(() => {
  ownerEmail.value = localStorage.getItem('email') || ''
  boardId.value = route.params.boardId
})

const isEmailValid = computed(() => {
  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
  return (
    collabEmail.value &&
    collabEmail.value.length <= 50 &&
    emailRegex.test(collabEmail.value)
  )
})

const close = () => {
  emit('close')
}

const addCollaborator = async () => {
  try {
    await fetchUtils.addCollab(boardId.value, {
      email: collabEmail.value,
      access_right: accessRight.value
    })
    emit('collab-added')
    close()
  } catch (error) {
    console.error('Error adding collaborator:', error.message)
  }
}
</script>

<style scoped></style>
