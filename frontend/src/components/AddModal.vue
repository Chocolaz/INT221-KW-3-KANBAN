<script setup>
import { ref, computed, onMounted, defineProps, defineEmits } from 'vue'
import FetchUtils from '../lib/fetchUtils'
import { useRoute } from 'vue-router'

const props = defineProps({
  closeModal: {
    type: Function,
    required: true
  }
})

const emit = defineEmits(['taskSaved', 'showStatusModal'])

const route = useRoute()

const taskDetails = ref({
  title: '',
  description: '',
  assignees: '',
  statusName: 'No Status'
})

const statuses = ref([])

const isSaveDisabled = computed(() => {
  const { title, description, assignees } = taskDetails.value
  return (
    !title.trim() ||
    title.length > 100 ||
    description.length > 500 ||
    assignees.length > 30
  )
})

async function handleSaveTask() {
  const boardId = route.params.boardId

  if (!boardId) {
    console.error('Board ID is undefined')
    return
  }

  try {
    const { success, data, statusCode } = await FetchUtils.postData(
      'tasks',
      boardId,
      taskDetails.value
    )

    if (success && statusCode === 201) {
      console.log('Task added successfully:', statusCode)
      emit('taskSaved', data)
      emit('showStatusModal', statusCode)
      resetTaskDetails()
      props.closeModal()
    } else {
      console.error('Failed to add task')
    }
  } catch (error) {
    console.error('Error saving task:', error)
  }
}

function cancelModal() {
  props.closeModal()
}

function resetTaskDetails() {
  taskDetails.value = {
    title: '',
    description: '',
    assignees: '',
    statusName: 'No Status'
  }
}

async function fetchStatuses() {
  const boardId = route.params.boardId

  if (!boardId) {
    console.error('Board ID is undefined')
    return
  }

  try {
    const data = await FetchUtils.fetchData('statuses', boardId)
    statuses.value = data
  } catch (error) {
    console.error('Error fetching statuses:', error)
  }
}

onMounted(() => {
  fetchStatuses()
})
</script>

<template>
  <div
    class="fixed inset-0 flex items-center justify-center bg-black bg-opacity-50 z-50"
    @click.self="cancelModal"
  >
    <div class="bg-white shadow-lg rounded-lg w-full max-w-lg mt-14">
      <div class="p-6">
        <div class="flex justify-between items-center mb-4">
          <h2 class="text-xl font-semibold text-red-600">Add Task</h2>
          <button
            class="text-2xl text-red-600 hover:text-red-800"
            @click="cancelModal"
          >
            &times;
          </button>
        </div>

        <form @submit.prevent="handleSaveTask">
          <div class="mb-4">
            <label for="title" class="block text-sm font-medium text-gray-700"
              >Title</label
            >
            <input
              type="text"
              id="title"
              v-model="taskDetails.title"
              class="mt-1 p-2 block w-full border border-gray-300 rounded-md shadow-sm focus:ring-red-500 focus:border-red-500"
              required
              maxlength="100"
              placeholder="Enter task title"
            />
            <small v-if="taskDetails.title.length > 90" class="text-red-600">
              {{ 100 - taskDetails.title.length }} characters left
            </small>
          </div>

          <div class="mb-4">
            <label
              for="description"
              class="block text-sm font-medium text-gray-700"
              >Description</label
            >
            <textarea
              id="description"
              v-model="taskDetails.description"
              class="mt-1 p-2 block w-full border border-gray-300 rounded-md shadow-sm focus:ring-red-500 focus:border-red-500"
              maxlength="500"
              placeholder="Enter task description"
            ></textarea>
            <small
              v-if="taskDetails.description.length > 450"
              class="text-red-600"
            >
              {{ 500 - taskDetails.description.length }} characters left
            </small>
          </div>

          <div class="mb-4">
            <label
              for="assignees"
              class="block text-sm font-medium text-gray-700"
              >Assignees</label
            >
            <input
              type="text"
              id="assignees"
              v-model="taskDetails.assignees"
              class="mt-1 p-2 block w-full border border-gray-300 rounded-md shadow-sm focus:ring-red-500 focus:border-red-500"
              maxlength="30"
              placeholder="Enter assignees"
            />
            <small
              v-if="taskDetails.assignees.length > 25"
              class="text-red-600"
            >
              {{ 30 - taskDetails.assignees.length }} characters left
            </small>
          </div>

          <div class="mb-4">
            <label for="status" class="block text-sm font-medium text-gray-700"
              >Status</label
            >
            <select
              id="status"
              v-model="taskDetails.statusName"
              class="mt-1 p-2 block border border-gray-300 w-60 mx-auto rounded-md shadow-sm focus:ring-red-500 focus:border-red-500"
            >
              <option v-if="statuses.length === 0" value="" disabled>
                Loading...
              </option>
              <option
                v-for="status in statuses"
                :key="status.id"
                :value="status.name"
              >
                {{ status.name }}
              </option>
            </select>
          </div>

          <div class="flex justify-end space-x-2">
            <button
              type="button"
              class="py-2 px-4 bg-gray-200 text-gray-700 rounded-md hover:bg-gray-300"
              @click="cancelModal"
            >
              Cancel
            </button>
            <button
              type="submit"
              :class="{ 'opacity-50 cursor-not-allowed': isSaveDisabled }"
              :disabled="isSaveDisabled"
              class="py-2 px-4 bg-red-600 text-white rounded-md hover:bg-red-700"
            >
              Save
            </button>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<style scoped></style>
