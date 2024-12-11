<script setup>
import { ref, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'

const showDropdown = ref(false)
const router = useRouter()
const route = useRoute()

const username = localStorage.getItem('username')
const isAuthenticated = !!username

const displayUsername = computed(() => {
  return isAuthenticated ? username : 'Guest'
})

const isManageStatus = computed(() => route.name === 'statusView')

const toggleDropdown = () => {
  showDropdown.value = !showDropdown.value
}

const navigate = () => {
  if (isManageStatus.value) {
    router.push({ name: 'taskView' })
  } else {
    router.push({ name: 'statusView' })
  }
  showDropdown.value = false
}

const logout = () => {
  localStorage.clear()
  showDropdown.value = false
  router.push({ name: 'loginView' })
}
</script>

<template>
  <nav
    class="bg-white shadow-md sticky top-0 z-10 py-3 px-6 transition-all duration-300"
  >
    <div class="max-w-screen-xl h-10 mx-auto flex justify-between items-center">
      <div class="flex items-center">
        <router-link to="/board">
          <img
            src="../assets/KANBANLOGO.png"
            alt="KANBAN Logo"
            class="h-12 w-auto transition-transform duration-300 hover:scale-105"
          />
        </router-link>
      </div>

      <div class="flex items-center gap-4 relative">
        <div class="flex items-center gap-2">
          <i
            class="fa fa-user w-10 h-10 rounded-full object-cover text-red-500 transition-transform duration-300 mt-5 hover:scale-110"
          ></i>
          <span class="text-lg font-medium text-gray-800">{{
            displayUsername
          }}</span>
        </div>
        <button
          @click="toggleDropdown"
          class="bg-none border-none cursor-pointer p-2 rounded-full transition-all duration-300 ease-in-out transform hover:bg-gray-100 hover:rotate-90"
          aria-label="User Menu"
        >
          <svg
            xmlns="http://www.w3.org/2000/svg"
            viewBox="0 0 24 24"
            fill="none"
            stroke="currentColor"
            stroke-width="2"
            stroke-linecap="round"
            stroke-linejoin="round"
            class="w-6 h-6 text-gray-500 transition-colors duration-300 hover:text-gray-800"
          >
            <circle cx="12" cy="12" r="1"></circle>
            <circle cx="12" cy="5" r="1"></circle>
            <circle cx="12" cy="19" r="1"></circle>
          </svg>
        </button>
        <transition name="dropdown">
          <div
            v-if="showDropdown"
            class="absolute top-full right-0 bg-white rounded-md shadow-lg overflow-hidden min-w-[150px]"
          >
            <button
              @click="navigate"
              class="w-full p-3 text-left text-gray-800 text-sm transition-all duration-300 ease-in-out hover:bg-green-100 transform hover:translate-x-1"
            >
              {{ isManageStatus ? 'Task List' : 'Manage Status' }}
            </button>
            <button
              @click="logout"
              class="w-full p-3 text-left text-red-500 text-sm transition-all duration-300 ease-in-out hover:bg-red-100 transform hover:translate-x-1"
            >
              Logout
            </button>
          </div>
        </transition>
      </div>
    </div>
  </nav>
</template>
