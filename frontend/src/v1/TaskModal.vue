<template>
  <div
    class="fixed inset-0 bg-gray-600 bg-opacity-50 overflow-y-auto w-full z-50 flex items-center justify-center"
  >
    <div
      class="relative text-base bg-white rounded-lg shadow-xl max-w-4xl w-full m-4 mt-20 p-6 animate-fade-in-up"
    >
      <div class="absolute top-4 right-4">
        <button
          @click="closeModal"
          class="text-gray-400 hover:text-gray-600 transition duration-150 ease-in-out"
        >
          <svg
            class="h-6 w-6"
            fill="none"
            viewBox="0 0 24 24"
            stroke="currentColor"
          >
            <path
              stroke-linecap="round"
              stroke-linejoin="round"
              stroke-width="2"
              d="M6 18L18 6M6 6l12 12"
            />
          </svg>
        </button>
      </div>

      <h2 class="text-2xl font-bold text-primary mb-4 pr-8 itbkk-title">
        {{ task.title }}
      </h2>

      <div class="grid grid-cols-1 md:grid-cols-3 gap-6">
        <div class="md:col-span-2">
          <div class="bg-gray-50 rounded-lg p-4 mb-6 h-48">
            <h3 class="text-base font-semibold text-gray-700 mb-2">
              Description
            </h3>
            <p class="text-gray-600 itbkk-description text-start">
              {{ task.description || 'No description provided' }}
            </p>
          </div>

          <div class="bg-gray-50 rounded-lg p-4">
            <h3 class="text-base font-semibold text-gray-700 mb-2">
              Assignees
            </h3>
            <p class="text-gray-600 itbkk-assignees text-start mb-5">
              {{ task.assignees || 'Unassigned' }}
            </p>
          </div>
        </div>
        <div v-if="task.attachments && task.attachments.length > 0">
          <div class="bg-gray-50 rounded-lg p-4 mb-6">
            <h3 class="text-base font-semibold text-gray-700 mb-2">
              Attachments
            </h3>
            <div
              v-for="attachment in task.attachments"
              :key="attachment.attachmentId"
              class="mb-4"
            >
              <div class="flex items-center">
                <!-- Thumbnail of the attachment -->
                <img
                  :src="getThumbnailUrl(attachment.file)"
                  alt="Attachment Thumbnail"
                  class="w-16 h-16 object-cover rounded-md mr-4"
                />
                <!-- File Name -->
                <span class="text-gray-700">{{ attachment.file }}</span>
              </div>
            </div>
          </div>
        </div>
        <div v-else>
          <div class="bg-gray-50 rounded-lg p-4 mb-6">
            <h3 class="text-base font-semibold text-gray-700 mb-2">
              Attachments
            </h3>
            <p class="text-gray-600">No attachments available.</p>
          </div>
        </div>

        <div>
          <div class="bg-gray-50 rounded-lg p-4 mb-6">
            <h3 class="text-base font-semibold text-gray-700 mb-2">Status</h3>
            <span
              class="px-3 py-1 rounded-full text-sm font-semibold itbkk-status"
              :style="statusStyle(task.statusName)"
            >
              {{ task.statusName || 'Unassigned' }}
            </span>
          </div>

          <div class="bg-gray-50 rounded-lg p-4 space-y-4">
            <div>
              <h4 class="text-sm font-medium text-gray-500">Timezone</h4>
              <p class="text-gray-700 itbkk-timezone">{{ timezone }}</p>
            </div>
            <div>
              <h4 class="text-sm font-medium text-gray-500">Created</h4>
              <p class="text-gray-700 itbkk-created-on">{{ createdDate }}</p>
            </div>
            <div>
              <h4 class="text-sm font-medium text-gray-500">Updated</h4>
              <p class="text-gray-700 itbkk-updated-on">{{ updatedDate }}</p>
            </div>
          </div>
        </div>
      </div>

      <div class="flex justify-end space-x-4">
        <button
          @click="closeModal"
          class="px-4 py-2 bg-gray-200 text-gray-800 rounded hover:bg-gray-300 transition duration-150 ease-in-out itbkk-button"
        >
          Close
        </button>
        <button
          @click="closeModal"
          class="px-4 py-2 bg-blue-500 text-white rounded hover:bg-blue-600 transition duration-150 ease-in-out itbkk-button"
        >
          Done
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { defineProps } from 'vue'
import { statusStyle } from '@/lib/statusStyles'

const props = defineProps({
  task: {
    type: Object,
    required: true
  },
  timezone: {
    type: String,
    required: true
  },
  createdDate: {
    type: String,
    required: true
  },
  updatedDate: {
    type: String,
    required: true
  },
  closeModal: {
    type: Function,
    required: true
  }
})

const getThumbnailUrl = (file) => {
  return `http://localhost:8080/uploads/${file}`
}
</script>

<style scoped>
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

.text-primary {
  color: #ff6b6b;
}
</style>
