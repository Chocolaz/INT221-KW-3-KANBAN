<template>
  <nav class="navbar">
    <div class="navbar-content">
      <div class="logo-container">
        <router-link to="/board">
          <img src="../assets/KANBANLOGO.png" alt="KANBAN Logo" class="logo" />
        </router-link>
      </div>

      <div class="user-actions">
        <div class="user-info">
          <i class="fa fa-user user-avatar" aria-hidden="true"></i>
          <span class="username itbkk-fullname">{{ username }}</span>
        </div>
        <button @click="toggleDropdown" class="menu-btn" aria-label="User Menu">
          <svg
            xmlns="http://www.w3.org/2000/svg"
            viewBox="0 0 24 24"
            fill="none"
            stroke="currentColor"
            stroke-width="2"
            stroke-linecap="round"
            stroke-linejoin="round"
            class="menu-icon"
          >
            <circle cx="12" cy="12" r="1"></circle>
            <circle cx="12" cy="5" r="1"></circle>
            <circle cx="12" cy="19" r="1"></circle>
          </svg>
        </button>
        <transition name="dropdown">
          <div v-if="showDropdown" class="dropdown">
            <button @click="navigate" class="dropdown-item">
              {{ isManageStatus ? 'Task List' : 'Manage Status' }}
            </button>
            <button @click="logout" class="dropdown-item logout itbkk-sign-out">
              Logout
            </button>
          </div>
        </transition>
      </div>
    </div>
  </nav>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'

const username = localStorage.getItem('username')
const showDropdown = ref(false)

const router = useRouter()
const route = useRoute()

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

<style scoped>
.navbar {
  background-color: #ffffff;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
  padding: 0.75rem 1.5rem;
  position: sticky;
  top: 0;
  z-index: 1000;
  transition: all 0.3s ease;
}

.navbar-content {
  max-width: 1200px;
  height: 40px;
  margin: 0 auto;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.logo-container {
  display: flex;
  align-items: center;
}

.logo {
  height: 50px;
  width: auto;
  transition: transform 0.3s ease;
}

.logo:hover {
  transform: scale(1.05);
}

.user-actions {
  display: flex;
  align-items: center;
  gap: 1rem;
  position: relative;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.user-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  object-fit: cover;
  transition: transform 0.3s ease;
  margin-top: 20px;
  color: #e74c3c;
}

.user-avatar:hover {
  transform: scale(1.1);
}

.username {
  font-size: 1rem;
  font-weight: 500;
  color: #333;
}

.menu-btn {
  background: none;
  border: none;
  cursor: pointer;
  padding: 0.5rem;
  border-radius: 50%;
  transition: background-color 0.3s ease, transform 0.3s ease;
}

.menu-btn:hover {
  background-color: #f0f0f0;
  transform: rotate(90deg);
}

.menu-icon {
  width: 24px;
  height: 24px;
  color: #666;
  transition: color 0.3s ease;
}

.menu-btn:hover .menu-icon {
  color: #333;
}

.dropdown {
  position: absolute;
  top: 100%;
  right: 0;
  background-color: #ffffff;
  border-radius: 4px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
  overflow: hidden;
  min-width: 150px;
}

.dropdown-item {
  display: block;
  width: 100%;
  padding: 0.75rem 1rem;
  text-align: left;
  background: none;
  border: none;
  font-size: 0.9rem;
  color: #333;
  cursor: pointer;
  transition: background-color 0.3s ease, transform 0.3s ease;
}

.dropdown-item:hover {
  background-color: #f0f0f0;
  transform: translateX(5px);
}

.dropdown-item.logout {
  color: #e74c3c;
}

.dropdown-item.logout:hover {
  background-color: #fbe9e7;
}

/* Dropdown animation */
.dropdown-enter-active,
.dropdown-leave-active {
  transition: opacity 0.3s, transform 0.3s;
}

.dropdown-enter-from,
.dropdown-leave-to {
  opacity: 0;
  transform: translateY(-10px);
}

@media (max-width: 768px) {
  .navbar-content {
    flex-direction: row;
  }

  .user-actions {
    flex-grow: 1;
    justify-content: flex-end;
  }

  .username {
    display: none;
  }
}
</style>
