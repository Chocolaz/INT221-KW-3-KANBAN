<script setup>
import { ref, computed, defineEmits } from 'vue'

const emit = defineEmits(['filesSelected'])

const MAX_FILES = 10
const MAX_FILE_SIZE = 20 * 1024 * 1024
const MAX_COMBINED_SIZE = 20 * 1024 * 1024

const fileInput = ref(null)
const fileList = ref([])
const isDragging = ref(false)
const errorMessage = ref(null)

// Function to format file size
function formatFileSize(bytes) {
  if (bytes === 0) return '0 Bytes'
  const k = 1024
  const sizes = ['Bytes', 'KB', 'MB', 'GB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i]
}

const combinedFileSize = computed(() => {
  return fileList.value.reduce((total, item) => total + item.file.size, 0)
})

function checkCombinedFileSize(files) {
  const newFilesSize = files.reduce((total, file) => total + file.size, 0)
  return combinedFileSize.value + newFilesSize <= MAX_COMBINED_SIZE
}

// Function to get file icon based on type
function getFileIcon(type) {
  if (type.includes('pdf')) return '📄'
  if (type.includes('word')) return '📝'
  if (type.includes('sheet') || type.includes('excel')) return '📊'
  return '📎'
}

function isImageFile(type) {
  return type.startsWith('image/')
}

// Function to create thumbnail for image files
function createThumbnail(file) {
  return new Promise((resolve) => {
    if (!isImageFile(file.type)) {
      resolve(null)
      return
    }

    const reader = new FileReader()
    reader.onload = (e) => {
      resolve(e.target.result)
    }
    reader.readAsDataURL(file)
  })
}

// Check if file list exceeds limits
const canAddMoreFiles = computed(() => fileList.value.length < MAX_FILES)

// Handle file selection with validation
async function handleFileChange(event) {
  const files = event?.target?.files ? Array.from(event.target.files) : []
  await addFiles(files)
}

function displayError(message) {
  errorMessage.value = message
  setTimeout(() => {
    errorMessage.value = null
  }, 7000)
}

// Add selected files to the list with validation
async function addFiles(files) {
  const validFiles = []
  for (const file of files) {
    if (file.size > MAX_FILE_SIZE) {
      displayError(`File "${file.name}" exceeds the 20 MB size limit.`)
      continue
    }
    if (fileList.value.some((item) => item.file.name === file.name)) {
      displayError(`File "${file.name}" already exists.`)
      continue
    }
    if (fileList.value.length + validFiles.length >= MAX_FILES) {
      displayError(`You can only upload up to ${MAX_FILES} files.`)
      break
    }

    if (!checkCombinedFileSize(files)) {
      displayError(`Total file size must not exceed 20 MB.`)
      break
    }

    validFiles.push({
      file,
      thumbnail: await createThumbnail(file)
    })
  }

  fileList.value = [...fileList.value, ...validFiles]
  emit(
    'filesSelected',
    fileList.value.map((item) => item.file)
  )
}

// Remove file from the list
function removeFile(index) {
  fileList.value.splice(index, 1)
  errorMessage.value = ''
  emit(
    'filesSelected',
    fileList.value.map((item) => item.file)
  )
}

// Handle drag and drop events
function handleDragEnter(e) {
  e.preventDefault()
  isDragging.value = true
}

function handleDragLeave(e) {
  e.preventDefault()
  isDragging.value = false
}

async function handleDrop(e) {
  e.preventDefault()
  isDragging.value = false
  const files = Array.from(e.dataTransfer.files)
  await addFiles(files)
}
</script>

<template>
  <div class="w-full">
    <!-- Label Row -->
    <div class="flex items-center justify-between mb-1">
      <label for="attachments" class="text-sm font-bold text-gray-700">
        Upload Attachments
      </label>
      <!-- Error Message -->
      <transition name="fade">
        <div v-if="errorMessage" class="text-xs text-red-500">
          {{ errorMessage }}
        </div>
      </transition>
    </div>

    <!-- Upload Area -->
    <div
      class="relative"
      @dragenter="handleDragEnter"
      @dragleave="handleDragLeave"
      @dragover.prevent
      @drop="handleDrop"
    >
      <div
        class="h-20 border-2 border-dashed rounded-lg transition-all duration-200 flex items-center justify-center gap-3 px-4 cursor-pointer group"
        :class="{
          'border-blue-400 bg-blue-50': isDragging,
          'border-gray-300 hover:border-gray-400 hover:bg-gray-50': !isDragging
        }"
        @click="canAddMoreFiles ? $refs.fileInput.click() : ''"
      >
        <!-- Upload Icon -->
        <div
          class="w-8 h-8 rounded-full bg-gray-100 flex items-center justify-center group-hover:bg-gray-200 transition-colors"
        >
          <svg
            class="w-4 h-4 text-gray-500"
            fill="none"
            stroke="currentColor"
            viewBox="0 0 24 24"
          >
            <path
              stroke-linecap="round"
              stroke-linejoin="round"
              stroke-width="2"
              d="M7 16a4 4 0 01-.88-7.903A5 5 0 1115.9 6L16 6a5 5 0 011 9.9M15 13l-3-3m0 0l-3 3m3-3v12"
            />
          </svg>
        </div>

        <!-- Upload Text -->
        <div class="text-center">
          <p class="text-sm text-gray-600">
            Drop files here or
            <span
              class="text-blue-500 hover:text-blue-600"
              :class="{ 'cursor-not-allowed': !canAddMoreFiles }"
            >
              browse
            </span>
          </p>
          <p v-if="!canAddMoreFiles" class="text-xs text-red-500">
            Maximum file limit reached.
          </p>
        </div>

        <input
          type="file"
          id="attachments"
          ref="fileInput"
          multiple
          @change="handleFileChange"
          class="hidden"
        />
      </div>
    </div>

    <!-- File List -->
    <div v-if="fileList.length > 0" class="mt-3">
      <div class="text-xs text-gray-500 mb-2">
        {{ fileList.length }} file(s) selected
      </div>

      <ul class="grid grid-cols-2 gap-2">
        <li
          v-for="(item, index) in fileList"
          :key="index"
          class="group relative flex bg-white rounded-md border border-gray-200 hover:border-gray-300 transition-all duration-200 overflow-hidden"
        >
          <!-- Thumbnail/Icon Container -->
          <div
            class="w-12 h-12 flex-shrink-0 flex items-center justify-center bg-gray-50 border-r border-gray-200"
          >
            <img
              v-if="isImageFile(item.file.type) && item.thumbnail"
              :src="item.thumbnail"
              :alt="item.file.name"
              class="w-full h-full object-cover"
            />
            <span v-else class="text-lg" role="img" aria-label="file type">
              {{ getFileIcon(item.file.type) }}
            </span>
          </div>

          <!-- File Info -->
          <div class="flex-1 p-2 flex flex-col justify-center min-w-0">
            <p class="text-xs font-medium text-gray-700 truncate">
              {{ item.file.name }}
            </p>
            <p class="text-[10px] text-gray-500">
              {{ formatFileSize(item.file.size) }}
            </p>
          </div>

          <!-- Remove Button -->
          <button
            type="button"
            @click.stop="removeFile(index)"
            class="absolute top-1 right-1 p-1 text-gray-400 hover:text-red-500 rounded-full hover:bg-gray-100 transition-colors"
            aria-label="Remove file"
          >
            <svg
              class="w-3 h-3"
              fill="none"
              stroke="currentColor"
              viewBox="0 0 24 24"
            >
              <path
                stroke-linecap="round"
                stroke-linejoin="round"
                stroke-width="2"
                d="M6 18L18 6M6 6l12 12"
              />
            </svg>
          </button>
        </li>
      </ul>
    </div>
  </div>
</template>

<style scoped>
.animate-fade-in {
  animation: fadeIn 0.2s ease-in-out;
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(-10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.5s ease;
}
.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>
