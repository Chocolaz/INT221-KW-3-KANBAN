<template>
  <section class="page_404">
    <div class="container">
      <div class="row">
        <div class="col-sm-12">
          <div class="col-sm-10 col-sm-offset-1 text-center">
            <h1 class="text-5xl font-bold text-red-700 animate-bounce">404</h1>

            <p class="mt-2 text-gray-500">
              Oops! The page you're looking for doesn't exist.
            </p>
            <div class="four_zero_four_bg">
              <div class="contant_box_404">
              </div>
            </div>

            <button
              @click="goHome"
              class="px-6 py-3 text-white bg-red-600 rounded-lg hover:bg-red-700 transition duration-300 transform hover:scale-105"
            >
              Go Home
            </button>
          </div>
        </div>
      </div>
    </div>
  </section>
</template>

<script setup>
import { useRouter } from 'vue-router'

const router = useRouter()

function goHome() {
  router.push('/')
}
</script>

<style scoped>
.page_404 {
  padding: 40px 0;
  background: #fff;
  display: flex;
  justify-content: center;
}

.page_404 img {
  width: 100%;
}

.four_zero_four_bg {
  background-image: url(https://cdn.dribbble.com/users/285475/screenshots/2083086/dribbble_1.gif);
  height: 300px;
  background-position: center;
}

.four_zero_four_bg h1 {
  font-size: 80px;
}

.four_zero_four_bg h3 {
  font-size: 80px;
}

.link_404 {
  color: #fff !important;
  padding: 10px 20px;
  background: #39ac31;
  margin: 10px 0;
  display: inline-block;
}
.contant_box_404 {
  margin-top: -15px;
}

@keyframes fadeIn {
  0% {
    opacity: 0;
  }
  100% {
    opacity: 1;
  }
}

@keyframes bounce {
  0%,
  20%,
  50%,
  80%,
  100% {
    transform: translateY(0);
  }
  40% {
    transform: translateY(-30px);
  }
  60% {
    transform: translateY(-15px);
  }
}

@keyframes slideIn {
  from {
    transform: translateY(-100px);
    opacity: 0;
  }
  to {
    transform: translateY(0);
    opacity: 1;
  }
}

.animate-fadeIn {
  animation: fadeIn 0.6s ease-in;
}

.animate-bounce {
  animation: bounce 1s infinite;
}

.animate-slideIn {
  animation: slideIn 0.5s ease-out;
}
</style>
