<template>
  <div
    class="fixed inset-0 bg-black/30 backdrop-blur-sm z-50 flex items-center justify-center p-4"
  >
    <div
      class="bg-white rounded-xl shadow-2xl w-full max-w-4xl max-h-[85vh] flex flex-col mt-14 animate-modal"
      @click.stop
    >
      <!-- Header -->
      <div
        class="p-6 border-b border-gray-100 flex items-start justify-between"
      >
        <h2 class="text-2xl font-semibold text-gray-900 pr-8">
          {{ task.title }}
        </h2>
        <button
          @click="closeModal"
          class="text-gray-400 hover:text-gray-600 transition-colors p-1 hover:bg-gray-100 rounded-full"
        >
          <i class="fas fa-times w-5 h-5"></i>
        </button>
      </div>

      <!-- Scrollable Content -->
      <div class="flex-1 overflow-y-auto">
        <div class="p-6">
          <div class="grid grid-cols-1 md:grid-cols-3 gap-6">
            <!-- Main Content - Now spans 2 columns -->
            <div class="md:col-span-2 space-y-6">
              <!-- Description -->
              <div class="bg-gray-50 rounded-xl p-6">
                <h3
                  class="text-sm font-semibold text-gray-900 mb-3 flex items-center gap-2"
                >
                  <i class="fas fa-align-justify w-5 h-5 text-gray-500"></i>
                  Description
                </h3>
                <p class="text-gray-600 leading-relaxed text-sm">
                  {{ task.description || 'No description provided' }}
                </p>
              </div>

              <!-- Assignees -->
              <div class="bg-gray-50 rounded-xl p-6">
                <h3
                  class="text-sm font-semibold text-gray-900 mb-3 flex items-center gap-2"
                >
                  <i class="fas fa-user-friends w-5 h-5 text-gray-500"></i>
                  Assignees
                </h3>
                <div class="flex flex-wrap gap-2">
                  <template v-if="task.assignees">
                    <span
                      v-for="(assignee, index) in task.assignees.split(',')"
                      :key="index"
                      class="px-3 py-1 bg-gray-200 text-gray-700 rounded-full text-sm"
                    >
                      {{ assignee.trim() }}
                    </span>
                  </template>
                  <span v-else class="text-gray-500 text-sm">Unassigned</span>
                </div>
              </div>

              <!-- Attachments Section -->
              <div class="bg-gray-50 rounded-xl p-6">
                <h3
                  class="text-sm font-semibold text-gray-900 mb-3 flex items-center gap-2"
                >
                  <i class="fas fa-paperclip w-5 h-5 text-gray-500"></i>
                  Attachments
                </h3>
                <div class="flex flex-wrap gap-4">
                  <template v-if="attachments.length">
                    <div
                      v-for="attachment in attachments"
                      :key="attachment.attachmentId"
                      class="flex items-center bg-gray-200 rounded-lg overflow-hidden group transition-colors hover:bg-gray-300 cursor-pointer w-60"
                      @click="handleAttachmentClick(attachment)"
                    >
                      <div
                        class="w-16 h-16 flex items-center justify-center overflow-hidden bg-gray-100 rounded-lg text-3xl"
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
                  <span v-else class="text-gray-500 text-sm">No attachments</span>
                </div>
              </div>
            </div>

            <!-- Sidebar -->
            <div class="space-y-6">
              <!-- Status -->
              <div class="bg-gray-50 rounded-xl p-6">
                <h3
                  class="text-base font-semibold text-gray-900 mb-3 flex items-center gap-2"
                >
                  <i class="fas fa-check-circle w-5 h-5 text-gray-500"></i>
                  Status
                </h3>
                <span
                  class="px-3 py-1 rounded-full text-sm font-semibold itbkk-status"
                  :style="statusStyle(task.statusName)"
                >
                  {{ task.statusName || 'Unassigned' }}
                </span>
              </div>

              <!-- Metadata -->
              <div class="bg-gray-50 rounded-xl p-6 space-y-4">
                <div>
                  <div
                    class="flex items-center gap-2 text-xs text-gray-500 mb-1"
                  >
                    <i class="fas fa-globe w-4 h-4"></i>
                    Timezone
                  </div>
                  <p class="text-gray-900 text-sm">{{ timezone }}</p>
                </div>
                <div>
                  <div
                    class="flex items-center gap-2 text-xs text-gray-500 mb-1"
                  >
                    <i class="fas fa-calendar-alt w-4 h-4"></i>
                    Created
                  </div>
                  <p class="text-gray-900 text-sm">{{ createdDate }}</p>
                </div>
                <div>
                  <div
                    class="flex items-center gap-2 text-xs text-gray-500 mb-1"
                  >
                    <i class="fas fa-calendar-check w-4 h-4"></i>
                    Updated
                  </div>
                  <p class="text-gray-900 text-sm">{{ updatedDate }}</p>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- Footer -->
      <div class="border-t border-gray-100 p-4 flex justify-end gap-3">
        <button
          @click="closeModal"
          class="px-4 py-2 text-gray-700 bg-gray-100 hover:bg-gray-200 rounded-lg transition-colors"
        >
          Cancel
        </button>
        <button
          @click="closeModal"
          class="px-4 py-2 text-white bg-red-500 hover:bg-red-600 rounded-lg transition-colors"
        >
          Done
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { defineProps } from 'vue'
import fetchUtils from '@/lib/fetchUtils'
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

const attachments = ref([])

onMounted(async () => {
  attachments.value = await fetchUtils.fetchAttachments(props.task.attachments)
})

const handleAttachmentClick = (attachment) => {
  const fileType = attachment.file.split('.').pop().toLowerCase()
  const supportedFileTypes = ['pdf', 'jpeg', 'jpg', 'png', 'txt']

  if (supportedFileTypes.includes(fileType)) {
    window.open(attachment.blobUrl, '_blank')
  } else {
    const link = document.createElement('a')
    link.href = attachment.blobUrl
    link.download = attachment.file
    link.click()
  }
}

const getFileIcon = (fileName) => {
  const fileType = fileName.toLowerCase()

  if (fileType.includes('pdf')) return '📄'
  if (fileType.includes('doc') || fileType.includes('word')) return '📝'
  if (
    fileType.includes('xls') ||
    fileType.includes('sheet') ||
    fileType.includes('excel')
  )
    return '📊'
  if (fileType.includes('ppt')) return '📈'
  if (fileType.includes('txt')) return '📜'

  return '📎'
}
</script>

<style scoped>
.animate-modal {
  animation: modal-in 0.3s ease-out;
}

@keyframes modal-in {
  from {
    opacity: 0;
    transform: scale(0.95) translateY(10px);
  }
  to {
    opacity: 1;
    transform: scale(1) translateY(0);
  }
}

.overflow-y-auto {
  scrollbar-width: thin;
  scrollbar-color: #e5e7eb transparent;
}

.overflow-y-auto::-webkit-scrollbar {
  width: 6px;
}

.overflow-y-auto::-webkit-scrollbar-track {
  background: transparent;
}

.overflow-y-auto::-webkit-scrollbar-thumb {
  background-color: #e5e7eb;
  border-radius: 3px;
}

.overflow-y-auto::-webkit-scrollbar-thumb:hover {
  background-color: #d1d5db;
}
</style>
