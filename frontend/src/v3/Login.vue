<script setup>
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { storeTokens } from '../lib/authService'
import VueJwtDecode from 'vue-jwt-decode'

const baseUrl = import.meta.env.VITE_API_URL2
const router = useRouter()

const username = ref('')
const password = ref('')
const showError = ref(false)
const errorMessage = ref('')
const isLoading = ref(false)

const isSignInDisabled = computed(() => {
  return username.value.trim() === '' || password.value.trim() === ''
})

const login = async () => {
  if (isSignInDisabled.value) return

  isLoading.value = true
  showError.value = false

  try {
    const response = await fetch(`${baseUrl}/login`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({
        userName: username.value,
        password: password.value
      })
    })

    if (response.ok) {
      const data = await response.json()
      storeTokens(data.access_token, data.refresh_token)
      decodeAndLogToken(data.access_token)
      router.push('/board')
    } else {
      handleLoginError(response)
    }
  } catch (error) {
    console.error('Error during login:', error)
    showError.value = true
    errorMessage.value = 'There is a problem. Please try again later.'
  } finally {
    isLoading.value = false
  }
}

const handleLoginError = (response) => {
  showError.value = true
  if (response.status === 401 || response.status === 400) {
    errorMessage.value = 'Username or Password is incorrect.'
  } else {
    errorMessage.value = 'There is a problem. Please try again later.'
  }
}

const decodeAndLogToken = (token) => {
  try {
    const decodedToken = VueJwtDecode.decode(token)
    localStorage.setItem('username', decodedToken.name)
    localStorage.setItem('email', decodedToken.email)
  } catch (error) {
    console.error('Error decoding token:', error)
  }
}
</script>

<template>
  <div
    class="min-h-screen bg-gradient-to-br from-white via-red-50 to-white flex items-center justify-center px-4 py-8 relative overflow-hidden"
  >
    <!-- Animated Background -->
    <div class="absolute inset-0 pointer-events-none overflow-hidden">
      <div
        class="absolute top-[-10%] left-[-10%] w-96 h-96 bg-red-100/30 rounded-full animate-blob"
      ></div>
      <div
        class="absolute bottom-[-10%] right-[-10%] w-96 h-96 bg-red-200/30 rounded-full animate-blob animation-delay-2000"
      ></div>

      <!-- Grid Background -->
      <div class="absolute inset-0 bg-grid-red-100/30 opacity-30"></div>
    </div>

    <div
      class="w-full max-w-md bg-white/90 rounded-3xl shadow-2xl border border-red-100/50 relative z-10 transform transition-all duration-300 hover:scale-[1.02]"
    >
      <div class="p-10 space-y-6">
        <div class="text-center mb-8">
          <div class="flex justify-center">
            <img
              src="../assets/KANBANLOGO.png"
              alt="KANBAN Logo"
              class="h-20 w-auto transition-transform duration-300 hover:scale-105 mb-3"
            />
          </div>

          <p class="text-gray-500 text-lg animate-fade-in">
            Streamline Your Workflow
          </p>
        </div>

        <form @submit.prevent="login" class="space-y-6">
          <div class="relative group">
            <input
              type="text"
              v-model="username"
              placeholder="Username"
              maxlength="50"
              class="w-full px-4 py-3 bg-red-50 text-gray-700 placeholder-gray-400 rounded-xl border border-red-100 focus:outline-none focus:ring-2 focus:ring-red-300 focus:border-transparent transition duration-300 group-focus-within:shadow-lg animate-fade-in-up"
            />
          </div>

          <div class="relative group">
            <input
              type="password"
              v-model="password"
              placeholder="Password"
              maxlength="14"
              class="w-full px-4 py-3 bg-red-50 text-gray-700 placeholder-gray-400 rounded-xl border border-red-100 focus:outline-none focus:ring-2 focus:ring-red-300 focus:border-transparent transition duration-300 group-focus-within:shadow-lg animate-fade-in-up"
            />
          </div>

          <div
            v-if="showError"
            class="bg-red-100 text-red-800 p-3 rounded-lg text-center animate-slide-fade"
          >
            {{ errorMessage }}
          </div>

          <button
            type="submit"
            :disabled="isSignInDisabled || isLoading"
            class="w-full bg-gradient-to-r from-red-500 to-red-600 text-white font-bold py-3 rounded-xl hover:from-red-700 hover:to-red-900 transition duration-300 disabled:opacity-50 disabled:cursor-not-allowed active:scale-95 transform animate-fade-in-up flex items-center justify-center"
          >
            <span v-if="isLoading" class="animate-pulse">Signing In...</span>
            <span v-else>Sign In</span>
          </button>
        </form>
      </div>
    </div>
  </div>
</template>

<style scoped>
/* Additional custom styles if needed */
.animate-blob {
  animation: blob 7s infinite;
}

@keyframes blob {
  0%,
  100% {
    transform: translate(0, 0) scale(1);
  }
  50% {
    transform: translate(50px, 50px) scale(1.2);
  }
}

.animation-delay-2000 {
  animation-delay: 2s;
}
</style>
