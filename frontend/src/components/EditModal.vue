<script setup>
import { ref, computed, defineProps, defineEmits, onMounted } from 'vue'
import FetchUtils from '../lib/fetchUtils'

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
      updatedTask
    )

    if (response && response.success) {
      props.onTaskUpdated(response.data)
      props.closeModal()
      if (response.statusCode === 201) {
        emit('editSuccess', response.statusCode, 'edit')
        console.log('Task updated successfully.')
      }
    } else {
      console.error('Failed to update task')
      alert('Failed to edit task. Please try again.')
    }
  } catch (error) {
    console.error('Error updating task:', error)
    alert('Error updating task. Please try again.')
  }
}
const formatLocalDate = (dateString) => {
  const date = new Date(dateString)
  return date.toLocaleString('en-GB')
}

const timezone = computed(
  () => Intl.DateTimeFormat().resolvedOptions().timeZone
)

const fetchStatuses = async () => {
  try {
    const data = await FetchUtils.fetchData('statuses')
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
    class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50"
  >
    <div class="bg-white shadow-lg rounded-lg w-full max-w-lg p-6 relative">
      <h2 class="text-xl font-bold mb-4">Edit Task</h2>

      <form @submit.prevent="handleEditTask">
        <!-- Title -->
        <div class="mb-4">
          <label for="title" class="block font-semibold mb-1">Title:</label>
          <input
            type="text"
            id="title"
            v-model="editedTask.title"
            class="w-full px-3 py-2 border rounded-md"
            required
            maxlength="100"
          />
          <small
            v-if="editedTask.title.length > 100"
            class="text-red-500 text-sm"
          >
            Title must be at most 100 characters long.
          </small>
        </div>

        <!-- Description -->
        <div class="mb-4">
          <label for="description" class="block font-semibold mb-1"
            >Description:</label
          >
          <textarea
            id="description"
            v-model="editedTask.description"
            class="w-full px-3 py-2 border rounded-md"
            :placeholder="
              editedTask.description ? '' : 'No Description Provided'
            "
            maxlength="500"
          ></textarea>
          <small
            v-if="editedTask.description.length > 500"
            class="text-red-500 text-sm"
          >
            Description must be at most 500 characters long.
          </small>
        </div>

        <!-- Assignees -->
        <div class="mb-4">
          <label for="assignees" class="block font-semibold mb-1"
            >Assignees:</label
          >
          <input
            type="text"
            id="assignees"
            v-model="editedTask.assignees"
            class="w-full px-3 py-2 border rounded-md"
            :placeholder="editedTask.assignees ? '' : 'Unassigned'"
            maxlength="30"
          />
          <small
            v-if="editedTask.assignees.length > 30"
            class="text-red-500 text-sm"
          >
            Assignees must be at most 30 characters long.
          </small>
        </div>

        <!-- Status -->
        <div class="mb-4">
          <label for="status" class="block font-semibold mb-1">Status:</label>
          <select
            id="status"
            v-model="editedTask.statusName"
            class="w-full px-3 py-2 border rounded-md"
          >
            <option v-if="statuses.length === 0" value="" disabled>
              Loading...
            </option>
            <option
              v-else
              v-for="status in statuses"
              :key="status.statusId"
              :value="status.statusName"
            >
              {{ status.statusName }}
            </option>
          </select>
        </div>

        <!-- buttons -->
        <div class="flex justify-end space-x-4 mt-6">
          <button
            class="px-4 py-2 bg-green-500 text-white rounded-md hover:bg-green-600 disabled:opacity-50"
            type="submit"
            :disabled="isSaveDisabled"
          >
            Save
          </button>
          <button
            class="px-4 py-2 bg-red-500 text-white rounded-md hover:bg-red-600"
            type="button"
            @click="closeModal"
          >
            Cancel
          </button>
        </div>

        <!-- timezone, created date, updated date -->
        <div
          class="absolute top-4 right-4 p-2 border rounded-md bg-white shadow-md text-sm"
        >
          <div><strong>Timezone:</strong> {{ timezone }}</div>
          <div>
            <strong>Created Date:</strong>
            {{ formatLocalDate(task.createdOn) }}
          </div>
          <div>
            <strong>Updated Date:</strong>
            {{ formatLocalDate(task.updatedOn) }}
          </div>
        </div>
      </form>
    </div>
  </div>
</template>

<style scoped></style>
