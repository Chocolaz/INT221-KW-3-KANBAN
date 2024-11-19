<script setup>
import { ref, computed, defineEmits } from 'vue'

const emit = defineEmits(['filesSelected'])

const MAX_FILES = 10
const MAX_FILE_SIZE = 20 * 1024 * 1024
const MAX_TOTAL_FILE_SIZE = 20 * 1024 * 1024

const fileInput = ref(null)
const fileList = ref([])
const isDragging = ref(false)
const errorMessage = ref(null)

function formatFileSize(bytes) {
  const k = 1024
  const sizes = ['Bytes', 'KB', 'MB', 'GB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i]
}

function getFileIcon(type) {
  if (type.includes('pdf')) return '📄'
  if (type.includes('word')) return '📝'
  if (type.includes('sheet') || type.includes('excel')) return '📊'
  return '📎'
}

function isImageFile(type) {
  return type.startsWith('image/')
}

function createThumbnail(file) {
  return new Promise((resolve) => {
    if (!isImageFile(file.type)) {
      resolve(null)
      return
    }
    const reader = new FileReader()
    reader.onload = (e) => resolve(e.target.result)
    reader.readAsDataURL(file)
  })
}

const canAddMoreFiles = computed(() => fileList.value.length < MAX_FILES)

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

async function addFiles(files) {
  const validFiles = []
  const notAddedFiles = []
  const duplicateFiles = []
  const oversizedFiles = []
  const fileCountExceededFiles = []
  const totalSizeExceededFiles = []

  for (const file of files) {
    if (fileList.value.some((item) => item.file.name === file.name)) {
      duplicateFiles.push(file.name)
      continue
    }

    if (file.size > MAX_FILE_SIZE) {
      oversizedFiles.push(file.name)
      continue
    }

    const totalSize =
      fileList.value.reduce((total, item) => total + item.file.size, 0) +
      file.size

    if (fileList.value.length + validFiles.length >= MAX_FILES) {
      fileCountExceededFiles.push(file.name)
      continue
    }

    if (totalSize > MAX_TOTAL_FILE_SIZE) {
      totalSizeExceededFiles.push(file.name)
      continue
    }

    validFiles.push({
      file,
      thumbnail: await createThumbnail(file)
    })
  }

  const filesToAdd = validFiles.slice(0, MAX_FILES - fileList.value.length)
  fileList.value = [...fileList.value, ...filesToAdd]

  let errorMessageText = ''

  if (duplicateFiles.length > 0) {
    errorMessageText += `File(s) with the same filename cannot be added or updated: <p>${duplicateFiles.join(
      ', '
    )}</p> Please delete the existing file(s) first to update.`
  }

  if (oversizedFiles.length > 0) {
    errorMessageText += `<br>Each file cannot be larger than ${
      MAX_FILE_SIZE / (1024 * 1024)
    } MB: <p>${oversizedFiles.join(', ')}</p>`
  }

  if (fileCountExceededFiles.length > 0) {
    errorMessageText += `<br>Each task can have at most ${MAX_FILES} files: <p>${fileCountExceededFiles.join(
      ', '
    )}</p>`
  }

  if (totalSizeExceededFiles.length > 0) {
    errorMessageText += `<br>Total file size must not exceed ${
      MAX_TOTAL_FILE_SIZE / (1024 * 1024)
    } MB: <p>${totalSizeExceededFiles.join(', ')}</p>`
  }

  if (notAddedFiles.length > 0 && errorMessageText === '') {
    errorMessageText += `<br>The following files were not added: <p>${notAddedFiles.join(
      ', '
    )}</p>`
  }

  if (errorMessageText) {
    displayError(errorMessageText)
  }

  emit(
    'filesSelected',
    fileList.value.map((item) => item.file)
  )
}

function removeFile(index) {
  fileList.value.splice(index, 1)
  emit(
    'filesSelected',
    fileList.value.map((item) => item.file)
  )
}

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

    <!-- Error Message -->
    <transition name="fade">
      <div
        v-if="errorMessage"
        class="text-xs text-red-500 mt-2 animate-fade-in"
        v-html="errorMessage"
      ></div>
    </transition>

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

<style scoped></style>
