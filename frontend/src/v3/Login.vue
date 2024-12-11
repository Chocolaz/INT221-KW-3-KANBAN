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

const isSignInDisabled = computed(() => {
  return username.value.trim() === '' || password.value.trim() === ''
})

const login = async () => {
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
      showError.value = false

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
  }

  if (showError.value) {
    setTimeout(() => {
      showError.value = false
    }, 3000)
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
  <div class="login-container">
    <div class="login-box">
      <div class="login-header">
        <h1 class="title">Welcome Back</h1>
        <p class="subtitle">Please sign in to your account</p>
      </div>
      <form @submit.prevent="login" class="login-form">
        <div class="input-group">
          <input
            type="text"
            v-model="username"
            class="itbkk-username"
            maxlength="50"
            required
          />
          <label>Username</label>
          <span class="highlight"></span>
        </div>
        <div class="input-group">
          <input
            type="password"
            v-model="password"
            class="itbkk-password"
            maxlength="14"
            required
          />
          <label>Password</label>
          <span class="highlight"></span>
        </div>
        <button
          type="submit"
          class="login-btn itbkk-button-signin"
          :class="{ disabled: isSignInDisabled }"
          :disabled="isSignInDisabled"
        >
          <span>Sign In</span>
          <svg viewBox="0 0 24 24">
            <path
              d="M4,12V20A2,2 0 0,0 6,22H18A2,2 0 0,0 20,20V12A2,2 0 0,0 18,10H6A2,2 0 0,0 4,12M6,12H18V20H6V12M12,2A2,2 0 0,1 14,4V8H10V4A2,2 0 0,1 12,2Z"
            />
          </svg>
        </button>
      </form>
    </div>
    <div v-if="showError" class="error-toast">
      <p>Error</p>
      <p class="itbkk-message">{{ errorMessage }}</p>
    </div>
  </div>
</template>

<style scoped>
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100vh;
  background: linear-gradient(-45deg, #e7886a, #e47ba3, #67abc4, #68c7b1);
  background-size: 400% 400%;
  animation: gradient 15s ease infinite;
}

.login-box {
  background: rgba(255, 255, 255, 0.1);
  border-radius: 20px;
  padding: 40px;
  backdrop-filter: blur(20px);
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
  width: 400px;
  opacity: 0;
  transform: translateY(-50px);
  animation: fadeInUp 0.5s ease-out forwards;
}

@keyframes fadeInUp {
  0% {
    opacity: 0;
    transform: translateY(-50px);
  }
  100% {
    opacity: 1;
    transform: translateY(0);
  }
}

.login-header {
  text-align: center;
  margin-bottom: 30px;
}

.title {
  color: #ffffff;
  font-size: 2.5em;
  text-shadow: 2px 2px 4px rgba(0, 0, 0, 0.1);
  animation: fadeIn 0.5s ease-out 0.2s forwards;
  opacity: 0;
}

@keyframes fadeIn {
  0% {
    opacity: 0;
  }
  100% {
    opacity: 1;
  }
}

.subtitle {
  color: #ffffff;
  font-size: 1.2em;
  margin-top: 10px;
  text-shadow: 1px 1px 2px rgba(0, 0, 0, 0.1);
  animation: fadeIn 0.5s ease-out 0.4s forwards;
  opacity: 0;
}

.login-form {
  display: flex;
  flex-direction: column;
}

.input-group {
  position: relative;
  margin-bottom: 30px;
  opacity: 0;
  transform: translateX(-20px);
  animation: fadeInLeft 0.5s ease-out forwards;
}

@keyframes fadeInLeft {
  0% {
    opacity: 0;
    transform: translateX(-20px);
  }
  100% {
    opacity: 1;
    transform: translateX(0);
  }
}

input {
  font-size: 16px;
  padding: 10px 10px 10px 5px;
  display: block;
  width: 100%;
  border: none;
  border-bottom: 1px solid #ffffff;
  background: transparent;
  color: #ffffff;
}

input:focus {
  outline: none;
}

label {
  color: #ffffff;
  font-size: 16px;
  font-weight: normal;
  position: absolute;
  pointer-events: none;
  left: 5px;
  top: 10px;
  transition: 0.2s ease all;
}

input:focus ~ label,
input:valid ~ label {
  top: -20px;
  font-size: 14px;
  color: #ffffff;
}

.highlight {
  position: absolute;
  height: 60%;
  width: 100px;
  top: 25%;
  left: 0;
  pointer-events: none;
  opacity: 0.5;
}

input:focus ~ .highlight {
  animation: inputHighlighter 0.3s ease;
}

@keyframes inputHighlighter {
  from {
    background: #ffffff;
  }
  to {
    width: 0;
    background: transparent;
  }
}

.login-btn {
  background: #ffffff;
  color: #667eea;
  border: none;
  padding: 12px 24px;
  font-size: 16px;
  border-radius: 25px;
  cursor: pointer;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transform: translateY(20px);
  animation: fadeInUp 0.5s ease-out 0.6s forwards;
}

.login-btn:hover {
  background: #667eea;
  color: #ffffff;
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.2);
}

.login-btn span {
  margin-right: 10px;
}

.login-btn svg {
  width: 20px;
  height: 20px;
  fill: currentColor;
}

.error-toast {
  position: fixed;
  top: 20px;
  right: 20px;
  background-color: #ffcccb;
  color: #d8000c;
  padding: 15px 20px;
  border-radius: 5px;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
  z-index: 1000;
  animation: slideIn 0.3s ease-out;
}

.error-toast p {
  margin: 0;
}

.error-toast p:first-child {
  font-weight: bold;
  margin-bottom: 5px;
}

@keyframes slideIn {
  from {
    transform: translateX(100%);
    opacity: 0;
  }
  to {
    transform: translateX(0);
    opacity: 1;
  }
}

.login-btn:disabled {
  background: #cccccc;
  color: #666666;
}

.login-btn:disabled:hover {
  background: #cccccc;
  color: #666666;
  box-shadow: none;
}

@keyframes gradient {
  0% {
    background-position: 0% 50%;
  }
  50% {
    background-position: 100% 50%;
  }
  100% {
    background-position: 0% 50%;
  }
}
</style>
