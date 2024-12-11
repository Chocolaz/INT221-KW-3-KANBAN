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
import { statusStyle, getFileIcon } from '../lib/statusStyles'
import FileAttachmentInput from './FileAttachmentInput.vue'

const route = useRoute()
const boardId = route.params.boardId
const selectedFiles = ref([])

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
const fetchedAttachments = ref([])
const errorMessage = ref('') 
const failedFiles = ref([])

const initialTask = JSON.parse(JSON.stringify(props.task))

const isSaveDisabled = computed(() => {
  const isTaskUnchanged =
    JSON.stringify(editedTask.value) === JSON.stringify(initialTask)
  const isFileSelected = selectedFiles.value.length > 0
  const hasDeletedAttachments = (props.task.deleteAttachments || []).length > 0

  return (
    (isTaskUnchanged && !isFileSelected && !hasDeletedAttachments) ||
    editedTask.value.title.length > 100 ||
    editedTask.value.description.length > 500 ||
    editedTask.value.assignees.length > 30
  )
})

const fetchAttachmentsForTask = async () => {
  try {
    const attachments = props.task.attachments || []
    fetchedAttachments.value = await FetchUtils.fetchAttachments(attachments)
  } catch (error) {
    console.error('Error fetching attachments:', error)
  }
}

function handleFilesSelected(files) {
  selectedFiles.value = files
}

const handleEditTask = async () => {
  try {
    const updatedTask = {
      title: editedTask.value.title,
      description: editedTask.value.description,
      assignees: editedTask.value.assignees,
      statusName: editedTask.value.statusName,
      updatedOn: new Date().toISOString()
    }

    const deleteAttachments = props.task.deleteAttachments || []

    const response = await FetchUtils.updateTaskWithAttachment(
      boardId,
      props.task.taskId,
      updatedTask,
      deleteAttachments,
      selectedFiles.value || []
    )

    if (response?.success) {
      props.onTaskUpdated(response.data)
      props.closeModal()
      emit('editSuccess', response.statusCode, 'edit')
      console.log('Task updated successfully.', response.statusCode)
    } else {
      console.error('Failed to update task')
      errorMessage.value = 'Failed to edit task. Please try again.'
      failedFiles.value = selectedFiles.value.map((file) => file.name)
    }
  } catch (error) {
    console.error('Error updating task:', error)
    errorMessage.value = error.response?.data?.message || error.message
    failedFiles.value = selectedFiles.value.map((file) => file.name)
    setTimeout(() => {
      errorMessage.value = ''
      failedFiles.value = []
    }, 7000)
  }
}

const deleteAttachment = (attachmentId) => {
  fetchedAttachments.value = fetchedAttachments.value.filter(
    (attachment) => attachment.attachmentId !== attachmentId
  )

  props.task.deleteAttachments = props.task.deleteAttachments || []

  if (!props.task.deleteAttachments.includes(attachmentId)) {
    props.task.deleteAttachments.push(attachmentId)
  }

  console.log('deleteAttachments updated:', props.task.deleteAttachments)
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
  fetchAttachmentsForTask()
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
      class="itbkk-modal-task bg-white shadow-lg rounded-lg w-full max-w-lg mt-14 animate-fade-in-up max-h-full"
    >
      <div
        class="flex justify-between items-center p-4 border-b border-gray-200"
      >
        <h2 class="text-xl font-semibold text-red-600">Edit Task</h2>
        <button
          class="text-2xl text-red-600 hover:text-red-800"
          @click="props.closeModal"
        >
          &times;
        </button>
      </div>

      <form @submit.prevent="handleEditTask">
        <div class="p-5 overflow-y-auto max-h-[350px]">
          <div class="mb-4">
            <label
              for="title"
              class="block text-sm font-semibold text-start text-gray-700"
            >
              Title
            </label>
            <input
              type="text"
              id="title"
              v-model="editedTask.title"
              class="mt-1 p-2 block w-full border border-gray-300 rounded-md shadow-sm focus:ring-red-500 focus:border-red-500 text-sm"
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
              class="block text-sm font-semibold text-start text-gray-700"
            >
              Description
            </label>
            <textarea
              id="description"
              v-model="editedTask.description"
              class="mt-1 p-2 block w-full border border-gray-300 rounded-md shadow-sm focus:ring-red-500 focus:border-red-500 text-sm resize-none"
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

          <div class="mb-4 flex space-x-4">
            <div class="flex-1">
              <label
                for="assignees"
                class="block text-sm font-semibold text-start text-gray-700"
              >
                Assignees
              </label>
              <input
                type="text"
                id="assignees"
                v-model="editedTask.assignees"
                class="mt-1 p-2 w-full border border-gray-300 rounded-md shadow-sm focus:ring-red-500 focus:border-red-500 text-sm"
                maxlength="30"
                placeholder="Enter assignees"
              />
              <small
                v-if="editedTask.assignees.length > 25"
                class="text-red-600"
              >
                {{ 30 - editedTask.assignees.length }} characters left
              </small>
            </div>

            <div class="status-dropdown w-36">
              <label
                for="status"
                class="block text-sm font-semibold text-start text-gray-700 mb-1"
              >
                Status
              </label>
              <div class="relative">
                <div
                  class="block p-2 border border-gray-300 rounded-md shadow-sm cursor-pointer text-xs hover:opacity-80 transition-opacity duration-200 h-9"
                  :style="statusStyle(editedTask.statusName)"
                  @click="toggleDropdown"
                >
                  {{ editedTask.statusName || 'Select Status' }}
                </div>

                <transition name="fade">
                  <div
                    v-if="isDropdownOpen"
                    class="absolute top-full mt-1 w-full bg-white border border-gray-300 rounded-md shadow-lg max-h-40 overflow-y-auto z-20"
                  >
                    <ul class="list-none p-0 m-0">
                      <li
                        v-for="status in statuses"
                        :key="status.id"
                        class="p-2 cursor-pointer text-xs transition-opacity duration-200"
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
          </div>

          <div v-if="fetchedAttachments.length > 0" class="mb-4">
            <div class="bg-gray-50 rounded-xl p-6">
              <h3
                class="text-sm font-semibold text-gray-900 mb-3 flex items-center gap-2"
              >
                <i class="fas fa-paperclip w-5 h-5 text-gray-500"></i>
                Attachments
              </h3>
              <div class="flex flex-wrap gap-4">
                <template v-if="fetchedAttachments.length">
                  <div
                    v-for="attachment in fetchedAttachments"
                    :key="attachment.attachmentId"
                    class="relative flex items-center bg-gray-200 rounded-lg overflow-hidden group transition-colors hover:bg-gray-300 cursor-pointer w-30"
                  >
                    <div class="absolute top-0 right-0 p-1">
                      <button
                        class="text-gray-600 hover:text-red-600"
                        @click.stop="deleteAttachment(attachment.attachmentId)"
                      >
                        &times;
                      </button>
                    </div>
                    <div
                      class="w-10 h-10 flex items-center justify-center overflow-hidden bg-gray-100 rounded-lg"
                    >
                      <template
                        v-if="
                          ['jpg', 'jpeg', 'png'].includes(
                            attachment.file.split('.').pop().toLowerCase()
                          )
                        "
                      >
                        <img
                          v-if="attachment.blobUrl"
                          :src="attachment.blobUrl"
                          :alt="attachment.file"
                          class="object-cover w-full h-full"
                        />
                      </template>
                      <template v-else>
                        <span>{{ getFileIcon(attachment.file) }}</span>
                      </template>
                    </div>
                    <span
                      class="text-xs text-gray-700 px-3 truncate max-w-[160px]"
                    >
                      {{ attachment.file }}
                    </span>
                  </div>
                </template>
              </div>
            </div>
          </div>

          <div class="mb-4">
            <FileAttachmentInput @filesSelected="handleFilesSelected" />
          </div>

          <transition
            enter-active-class="transition-opacity duration-500 ease-in-out"
            enter-from-class="opacity-0"
            enter-to-class="opacity-100"
            leave-active-class="transition-opacity duration-500 ease-in-out"
            leave-from-class="opacity-100"
            leave-to-class="opacity-0"
          >
            <div
              v-if="errorMessage || failedFiles.length > 0"
              class="flex items-start gap-3 text-red-700 bg-red-100 border border-red-300 p-4 rounded-lg shadow-lg"
            >
              <i class="fas fa-exclamation-circle text-red-500 text-2xl"></i>
              <div class="space-y-4">
                <div v-if="errorMessage" class="text-sm">
                  <p class="font-semibold">Error:</p>
                  <p>{{ errorMessage }}</p>
                  <p>
                    Please delete the attachment and add again to update the
                    file.
                  </p>
                </div>

                <div v-if="failedFiles.length > 0" class="text-sm">
                  <p>The following files are not added:</p>
                  <ul class="list-disc pl-6">
                    <li v-for="fileName in failedFiles" :key="fileName">
                      {{ fileName }}
                    </li>
                  </ul>
                </div>
              </div>
            </div>
          </transition>
        </div>

        <div class="flex justify-end items-center p-2 border-t border-gray-200">
          <div class="space-x-2">
            <button
              type="button"
              class="py-2 px-4 bg-gray-200 text-gray-700 rounded-md hover:bg-gray-300"
              @click="props.closeModal"
            >
              Cancel
            </button>
            <button
              type="submit"
              :disabled="isSaveDisabled"
              class="py-2 px-4 bg-red-600 text-white rounded-md hover:bg-red-700 disabled:bg-gray-400"
            >
              Save
            </button>
          </div>
        </div>
      </form>
    </div>
  </div>
</template>
