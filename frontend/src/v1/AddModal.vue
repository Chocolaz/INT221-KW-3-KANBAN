<script setup>
import {
  ref,
  computed,
  onMounted,
  onUnmounted,
  defineProps,
  defineEmits
} from 'vue'
import FetchUtils from '../lib/fetchUtils'
import { useRoute } from 'vue-router'
import { statusStyle } from '../lib/statusStyles'

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
const isDropdownOpen = ref(false)
const hoverStatus = ref(null)

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

const toggleDropdown = () => {
  isDropdownOpen.value = !isDropdownOpen.value
}

const selectStatus = (status) => {
  taskDetails.value.statusName = status
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
    @click.self="cancelModal"
  >
    <div
      class="itbkk-modal-task bg-white shadow-lg rounded-lg w-full max-w-lg mt-14 animate-fade-in-up"
    >
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
            <label
              for="title"
              class="block text-sm font-semibold text-start text-gray-700"
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
              class="block text-sm font-semibold text-start text-gray-700"
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
              class="block text-sm font-semibold text-start text-gray-700"
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

          <div class="mb-4 status-dropdown">
            <label
              for="status"
              class="block text-sm font-semibold text-start text-gray-700"
              >Status</label
            >
            <div class="relative">
              <div
                class="block p-2 border border-gray-300 rounded-md shadow-sm cursor-pointer text-xs w-36 hover:opacity-80 transition-opacity duration-200"
                :style="statusStyle(taskDetails.statusName)"
                @click="toggleDropdown"
              >
                {{ taskDetails.statusName || 'Select Status' }}
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
