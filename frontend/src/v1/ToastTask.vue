<script>
export default {
  props: {
    showModal: {
      type: Boolean,
      required: true
    },
    statusCode: {
      type: Number
    },
    operationType: {
      type: String,
      required: true
    },
    closeModal: {
      type: Function,
      required: true
    }
  },
  data() {
    return {
      timer: null
    }
  },
  computed: {
  toastClass() {
    if (
      this.statusCode === 201 ||
      (this.statusCode === 200 && this.operationType === 'add')
    ) {
      return 'bg-green-100 border-l-4 border-green-500'
    }
    if (this.statusCode === 200 && this.operationType === 'delete') {
      return 'bg-red-100 border-l-4 border-red-500'
    }
    if (this.statusCode === 200 && this.operationType === 'edit') {
      return 'bg-yellow-100 border-l-4 border-yellow-500'
    }
    return 'bg-red-100 border-l-4 border-red-500'
  },
  titleClass() {
    if (
      this.statusCode === 201 ||
      (this.statusCode === 200 && this.operationType === 'add')
    ) {
      return 'text-green-700'
    }
    if (this.statusCode === 200 && this.operationType === 'delete') {
      return 'text-red-700'
    }
    if (this.statusCode === 200 && this.operationType === 'edit') {
      return 'text-yellow-700'
    }
    return 'text-red-700'
  },
  toastTitle() {
    if (
      this.statusCode === 201 ||
      (this.statusCode === 200 && this.operationType === 'add')
    ) {
      return 'ADD TASK SUCCESS!'
    }
    if (this.statusCode === 200 && this.operationType === 'delete') {
      return 'DELETE SUCCESS!'
    }
    if (this.statusCode === 200 && this.operationType === 'edit') {
      return 'EDIT SUCCESS!'
    }
    if (this.statusCode === 404) return 'EXISTED?'
    return 'ERROR!'
  },
  toastMessage() {
    if (
      this.statusCode === 201 ||
      (this.statusCode === 200 && this.operationType === 'add')
    ) {
      return 'The task has been successfully added.'
    }
    if (this.statusCode === 200 && this.operationType === 'delete') {
      return 'The task has been deleted.'
    }
    if (this.statusCode === 200 && this.operationType === 'edit') {
      return 'The task has been successfully edited.'
    }
    if (this.statusCode === 404) {
      return 'An error has occurred, the task does not exist.'
    }
    return `Something went wrong. Status code: ${this.statusCode}`
  }
},
  watch: {
    showModal(newVal) {
      if (newVal) {
        this.startCloseTimer()
      } else {
        this.clearCloseTimer()
      }
    }
  },
  methods: {
    startCloseTimer() {
      this.clearCloseTimer()
      this.timer = setTimeout(this.closeModal, 1000)
    },
    clearCloseTimer() {
      if (this.timer) {
        clearTimeout(this.timer)
        this.timer = null
      }
    }
  },
  unmounted() {
    this.clearCloseTimer()
  }
}
</script>

<template>
  <transition
  enter-active-class="transition ease-out duration-300"
  enter-from-class="transform opacity-0 scale-95"
  enter-to-class="transform opacity-100 scale-100"
  leave-active-class="transition ease-in duration-200"
  leave-from-class="transform opacity-100 scale-100"
  leave-to-class="transform opacity-0 scale-95"
>

    <div class="fixed top-16 right-5 z-50" v-if="showModal">
      <div
        :class="[ 
          'flex items-start max-w-xs w-full shadow-lg rounded-lg',
          toastClass
        ]"
      >
        <div class="p-2 flex-grow">
          <h2 :class="['text-sm font-bold mb-1', titleClass]">
            {{ toastTitle }}
          </h2>
          <p class="text-xs">{{ toastMessage }}</p>
        </div>
        <button
          @click="closeModal"
          class="p-2 flex-shrink-0 text-gray-400 hover:text-gray-500 focus:outline-none focus:text-gray-500 transition ease-in-out duration-150"
        >
          &times;
        </button>
      </div>
    </div>
  </transition>
</template>

