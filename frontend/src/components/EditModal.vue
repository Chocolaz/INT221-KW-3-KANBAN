<script setup>
import {
  ref,
  computed,
  defineProps,
  defineEmits,
  onMounted,
  onUnmounted
} from 'vue'
import { useRoute } from 'vue-router'
import FetchUtils from '../lib/fetchUtils'
import { statusStyle } from '../lib/statusStyles'

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

const editedTask = ref({ ...props.task })
const statuses = ref([])
const isDropdownOpen = ref(false)
const hoverStatus = ref(null)

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

    if (response?.success) {
      props.onTaskUpdated(response.data)
      props.closeModal()
      emit('editSuccess', response.statusCode, 'edit')
      console.log('Task updated successfully.', response.statusCode)
    } else {
      console.error('Failed to update task')
      alert('Failed to edit task. Please try again.')
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

const toggleDropdown = () => {
  isDropdownOpen.value = !isDropdownOpen.value
}

const selectStatus = (status) => {
  editedTask.value.statusName = status
  isDropdownOpen.value = false
}

const closeDropdown = (event) => {
  if (!event.target.closest('.status-dropdown')) {
    isDropdownOpen.value = false
  }
}

onMounted(() => {
  fetchStatuses()
  document.addEventListener('click', closeDropdown)
})

onUnmounted(() => {
  document.removeEventListener('click', closeDropdown)
})
</script>

<template>
  <div
    class="fixed inset-0 flex items-center justify-center bg-black bg-opacity-50 z-50"
    @click.self="props.closeModal"
  >
    <div
      class="bg-white shadow-lg rounded-lg w-full max-w-lg mt-14 animate-fade-in-up"
    >
      <div class="p-4">
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
            <label
              for="title"
              class="block text-sm text-gray-700 text-start font-semibold"
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
              class="block text-sm text-gray-700 text-start font-semibold"
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
              class="block text-sm font-semibold text-start text-gray-700"
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

          <div class="mb-4 status-dropdown">
            <label
              for="status"
              class="block text-sm text-gray-700 text-start font-semibold"
              >Status</label
            >
            <div class="relative">
              <div
                class="block p-2 border border-gray-300 rounded-md shadow-sm cursor-pointer text-xs w-36 hover:opacity-80 transition-opacity duration-200"
                :style="statusStyle(editedTask.statusName)"
                @click="toggleDropdown"
              >
                {{ editedTask.statusName || 'Select Status' }}
              </div>

              <transition name="fade">
                <div
                  v-if="isDropdownOpen"
                  class="absolute top-full mt-1 w-36 bg-white border border-gray-300 rounded-md shadow-lg max-h-40 overflow-y-auto h-20 z-20"
                >
                  <ul class="list-none p-0 m-0">
                    <li
                      v-for="status in statuses"
                      :key="status.id"
                      class="p-2 cursor-pointer text-xs transition-colors duration-200"
                      :style="statusStyle(status.name)"
                      @click="selectStatus(status.name)"
                      @mouseenter="hoverStatus = status.name"
                      @mouseleave="hoverStatus = null"
                      :class="{ 'opacity-80': hoverStatus === status.name }"
                    >
                      {{ status.name }}
                    </li>
                  </ul>
                </div>
              </transition>
            </div>
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

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.2s;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translate3d(0, 20px, 0);
  }
  to {
    opacity: 1;
    transform: translate3d(0, 0, 0);
  }
}

.animate-fade-in-up {
  animation: fadeInUp 0.3s ease-out;
}
</style>
