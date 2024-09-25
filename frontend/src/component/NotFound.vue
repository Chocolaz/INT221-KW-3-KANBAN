<template>
  <div class="flex items-center justify-center h-screen bg-gray-100">
    <div class="text-center">
      <h1 class="text-9xl font-bold text-gray-700">404</h1>
      <p class="text-2xl font-semibold text-gray-600 mt-4">Page Not Found</p>
      <p class="mt-2 text-gray-500">
        Oops! The page you're looking for doesn't exist.
      </p>
      <button
        @click="goHome"
        class="mt-6 px-4 py-2 text-white bg-blue-600 rounded-lg hover:bg-blue-700"
      >
        Go Home
      </button>
    </div>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'

const router = useRouter()

function goHome() {
  router.push('/')
}
</script>
