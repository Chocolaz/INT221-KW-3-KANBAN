<script setup>
import { ref, computed, defineProps, defineEmits, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import FetchUtils from '../lib/fetchUtils'

const route = useRoute()
const boardId = route.params.boardId

const props = defineProps({
  task: {
    type: Object,
    required: true
  },
  closeModal: {
    type: Function,
    required: true
  },
  onTaskUpdated: {
    type: Function,
    required: true
  }
})
const emit = defineEmits(['editSuccess'])
const editedTask = ref({
  title: '',
  description: '',
  assignees: '',
  statusName: ''
})
const statuses = ref([])

if (props.task) {
  editedTask.value = { ...props.task }
}

const initialTask = JSON.parse(JSON.stringify(props.task))

const isSaveDisabled = computed(() => {
  return (
    JSON.stringify(editedTask.value) === JSON.stringify(initialTask) ||
    editedTask.value.title.length > 100 ||
    editedTask.value.description.length > 500 ||
    editedTask.value.assignees.length > 30
  )
})

const handleEditTask = async () => {
  try {
    const updatedTask = {
      title: editedTask.value.title,
      description: editedTask.value.description,
      assignees: editedTask.value.assignees,
      statusName: editedTask.value.statusName,
      updatedOn: new Date().toISOString()
    }

    const response = await FetchUtils.putData(
      `tasks/${props.task.taskId}`,
      boardId,
      updatedTask
    )

    if (response) {
      if (response.success) {
        props.onTaskUpdated(response.data)
        props.closeModal()

        emit('editSuccess', response.statusCode, 'edit')
        console.log('Task updated successfully.', response.statusCode)
      } else {
        console.error('Failed to update task')
        alert('Failed to edit task. Please try again.')
      }
    }
  } catch (error) {
    console.error('Error updating task:', error)
    alert('Error updating task. Please try again.')
  }
}

const fetchStatuses = async () => {
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
    @click.self="props.closeModal"
  >
    <div class="bg-white shadow-lg rounded-lg w-full max-w-lg mt-14">
      <div class="p-6">
        <div class="flex justify-between items-center mb-4">
          <h2 class="text-xl font-semibold text-red-600">Edit Task</h2>
          <button
            class="text-2xl text-red-600 hover:text-red-800"
            @click="props.closeModal"
          >
            &times;
          </button>
        </div>

        <form @submit.prevent="handleEditTask">
          <div class="mb-4">
            <label for="title" class="block text-sm font-medium text-gray-700"
              >Title</label
            >
            <input
              type="text"
              id="title"
              v-model="editedTask.title"
              class="mt-1 p-2 block w-full border border-gray-300 rounded-md shadow-sm focus:ring-red-500 focus:border-red-500"
              required
              maxlength="100"
              placeholder="Enter task title"
            />
            <small v-if="editedTask.title.length > 90" class="text-red-600">
              {{ 100 - editedTask.title.length }} characters left
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
              v-model="editedTask.description"
              class="mt-1 p-2 block w-full border border-gray-300 rounded-md shadow-sm focus:ring-red-500 focus:border-red-500"
              maxlength="500"
              placeholder="Enter task description"
            ></textarea>
            <small
              v-if="editedTask.description.length > 450"
              class="text-red-600"
            >
              {{ 500 - editedTask.description.length }} characters left
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
              v-model="editedTask.assignees"
              class="mt-1 p-2 block w-full border border-gray-300 rounded-md shadow-sm focus:ring-red-500 focus:border-red-500"
              maxlength="30"
              placeholder="Enter assignees"
            />
            <small v-if="editedTask.assignees.length > 25" class="text-red-600">
              {{ 30 - editedTask.assignees.length }} characters left
            </small>
          </div>

          <div class="mb-4">
            <label for="status" class="block text-sm font-medium text-gray-700"
              >Status</label
            >
            <select
              id="status"
              v-model="editedTask.statusName"
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
              @click="props.closeModal"
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

<style scoped>
textarea {
  resize: none;
}
</style>
