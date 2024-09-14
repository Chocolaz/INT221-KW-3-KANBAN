<script setup>
import { ref, onMounted } from 'vue'
import fetchUtils from '../lib/fetchUtils'
import { useRoute, useRouter } from 'vue-router'
import AddStatusModal from '../v2/AddStatusModal.vue'
import EditStatusModal from '../v2/EditStatusModal.vue'
import DeleteStatusModal from '../v2/DeleteStatusModal.vue'
import TransferStatusModal from '../v2/TransferStatusModal.vue'

// State management
const statuses = ref([])
const tasks = ref([])
const isAddOpen = ref(false)
const isEditOpen = ref(false)
const isDeleteOpen = ref(false)
const isTransferOpen = ref(false)
const selectedStatus = ref(null)
const selectedStatusIdToEdit = ref(null)
const selectedStatusIdToDelete = ref(null)
const selectedStatusIdToTransfer = ref(null)

// Route and router
const route = useRoute()
const router = useRouter()

// Fetch data
async function fetchData() {
  try {
    const boardId = route.params.boardId
    if (!boardId) throw new Error('Board ID is not available')

    statuses.value = await fetchUtils.fetchData('statuses', boardId)
    tasks.value = await fetchUtils.fetchData('tasks', boardId)

    const statusId = route.params.statusId
    if (statusId) {
      const status = statuses.value.find(
        (s) => s.statusId === parseInt(statusId)
      )
      if (status) {
        openEditModal(status)
      } else {
        router.push({ name: 'taskView' })
      }
    }
  } catch (error) {
    console.error('Error fetching data:', error)
  }
}

// Navigation
const backToHomePage = () => {
  const boardId = route.params.boardId
  if (boardId) {
    router.push(`/boards/${boardId}/tasks`)
  } else {
    console.error('Board ID is not defined')
    router.push({ name: 'taskView' })
  }
}

// Modal handlers
const openAddModal = () => (isAddOpen.value = true)
const openEditModal = (status) => {
  selectedStatus.value = { ...status }
  selectedStatusIdToEdit.value = status.statusId
  isEditOpen.value = true
}
const openDeleteModal = (status) => {
  selectedStatusIdToDelete.value = status.statusId
  isDeleteOpen.value = true
}
const openTransferModal = (status) => {
  selectedStatusIdToTransfer.value = status.statusId
  isTransferOpen.value = true
}

const closeModal = () => {
  isAddOpen.value = false
  isEditOpen.value = false
  isDeleteOpen.value = false
  isTransferOpen.value = false
}

const handleStatusAdded = () => fetchData()
const handleStatusEdited = () => fetchData()
const handleDelete = () => fetchData()
const handleTransfer = () => fetchData()

// Check if status is in use
const checkTasksBeforeDelete = (status) => {
  const statusInUse = tasks.value.some(
    (task) => task.statusName === status.name
  )
  statusInUse ? openTransferModal(status) : openDeleteModal(status)
}

// Fetch data on mount
onMounted(fetchData)

// Status Style function
const statusStyle = (statusName) => {
  const statusUpperCase = statusName.toUpperCase()
  const styles = {
    'TO DO': { background: 'linear-gradient(to right, #FF9A9E, #F67C5E)' },
    DOING: { background: 'linear-gradient(to right, #FFE066, #F6E05E)' },
    DONE: { background: 'linear-gradient(to right, #AAF6BE, #68D391)' },
    'NO STATUS': {
      backgroundColor: 'rgba(245, 245, 245, 0.8)',
      color: '#888',
      fontStyle: 'italic'
    },
    WAITING: { background: 'linear-gradient(to right, #D9A3FF, #B473FF)' },
    'IN PROGRESS': {
      background: 'linear-gradient(to right, #FFB347, #FFA733)'
    },
    REVIEWING: { background: 'linear-gradient(to right, #FFB6C1, #FF69B4)' },
    TESTING: { background: 'linear-gradient(to right, #ADD8E6, #87CEEB)' }
  }
  return (
    styles[statusUpperCase] || {
      background: 'linear-gradient(to right, #A0CED9, #6CBEE6)'
    }
  )
}
</script>

<template>
  <div id="app">
    <div class="table-container">
      <table class="table header-table">
        <thead>
          <tr>
            <th class="itbkk-button-add" style="text-align: center">
              <button @click="openAddModal" class="icon-button add-button">
                <i class="fas fa-plus-circle"></i>
              </button>
            </th>
            <th>Name</th>
            <th>Description</th>
            <th style="width: 100px">
              <i class="fas fa-ellipsis-h"></i>
            </th>
          </tr>
        </thead>
      </table>
      <div class="body-container">
        <table class="table body-table">
          <tbody>
            <tr
              v-for="(status, index) in statuses"
              :key="status.id"
              class="itbkk-item"
            >
              <td class="border px-4 py-2" style="text-align: center">
                {{ index + 1 }}
              </td>
              <td class="itbkk-status-name" :style="statusStyle(status.name)">
                {{ status.name }}
              </td>
              <td class="itbkk-status-description">
                <span
                  v-if="status.description"
                  v-html="status.description"
                ></span>
                <span v-else class="no-description"
                  >No description provided</span
                >
              </td>
              <td class="border px-4 py-2" style="width: 100px">
                <div class="action-buttons">
                  <button
                    class="icon-button edit-button"
                    @click="openEditModal(status)"
                  >
                    <i class="fas fa-edit"></i>
                  </button>
                  <button
                    class="icon-button delete-button"
                    @click="checkTasksBeforeDelete(status)"
                  >
                    <i class="fas fa-trash-alt"></i>
                  </button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <!-- Modals -->
    <AddStatusModal
      :isAddOpen="isAddOpen"
      @closeModal="closeModal"
      @statusAdded="handleStatusAdded"
    />
    <EditStatusModal
      :isOpen="isEditOpen"
      @closeModal="closeModal"
      @statusEdited="handleStatusEdited"
      :statusData="selectedStatus"
      :selectedStatusIdToEdit="selectedStatusIdToEdit"
    />
    <DeleteStatusModal
      :isOpen="isDeleteOpen"
      @closeModal="closeModal"
      @statusDeleted="handleDelete"
      :statusIdToDelete="selectedStatusIdToDelete"
    />
    <TransferStatusModal
      :isOpen="isTransferOpen"
      @closeModal="closeModal"
      @statusTransfered="handleTransfer"
      :statusIdToTransfer="selectedStatusIdToTransfer"
    />

    <div class="fab" @click="backToHomePage">
      <i class="fa fa-home"></i>
    </div>
  </div>
</template>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Poppins:wght@300;400;600&family=Montserrat:wght@700&display=swap');

body {
  font-family: 'Poppins', sans-serif;
  background-color: #ffffff;
  color: #343a40;
  line-height: 1.6;
}

h1,
h2,
h3 {
  font-family: 'Montserrat', sans-serif;
}

#app {
  max-width: 1000px;
  margin: 0 auto;
  margin-top: 15px;
  display: flex;
  justify-content: center;
}

.table-container {
  margin: 0 auto;
  width: 1000px;
  border-radius: 10px;
  font-size: 16px;
  color: #343a40;
  background: #ffffff;
  border: 2px solid #ff6b6b;
  box-shadow: 0 8px 32px 0 rgba(255, 107, 107, 0.2);
  overflow: hidden;
}

.header-table {
  width: 100%;
  table-layout: fixed;
}

.body-container {
  max-height: calc(70px * 10);
  overflow-y: auto;
}

.body-table {
  width: 100%;
  table-layout: fixed;
}

.table {
  border-collapse: separate;
  border-spacing: 0;
  width: 100%;
  table-layout: fixed;
  background-color: #ffffff;
  overflow: hidden;
}

.table th,
.table td {
  border: none;
  padding: 8px;
  text-align: left;
  height: 30px;
  word-wrap: break-word;
  word-break: break-all;
  white-space: normal;
}

.table th {
  background-color: #ff6b6b;
  font-weight: bold;
  color: #ffffff;
  font-size: 14px;
}

tbody tr:nth-child(even) {
  background-color: #ffe3e3;
}

tbody tr:nth-child(odd) {
  background-color: #ffffff;
}

tbody tr:hover {
  background-color: #ffccd5;
  transition: background-color 0.3s ease;
}

.itbkk-status-name {
  padding: 5px 8px;
  border-radius: 15px;
  text-transform: uppercase;
  font-weight: 600;
  letter-spacing: 1px;
  font-size: 0.7em;
}

.action-buttons {
  text-align: center;
}

.icon-button {
  border: none;
  background: none;
  padding: 5px;
  font-size: 1.2em;
  color: #ffffff;
  cursor: pointer;
  transition: transform 0.3s ease, color 0.3s ease;
}

.icon-button:hover {
  color: #353b41;
  transform: scale(1.1);
}

.icon-button:active {
  transform: scale(0.9);
}

.add-button {
  font-size: 1.5em;
}

.edit-button {
  color: #f06542;
}
.delete-button {
  color: #d33131;
}

.add-button:hover {
  animation: pulse 1s infinite;
}

.edit-button:hover {
  animation: rotate 0.5s ease;
}

.delete-button:hover {
  animation: wobble 0.5s ease;
}

@keyframes pulse {
  0% {
    transform: scale(1);
  }
  50% {
    transform: scale(1.1);
  }
  100% {
    transform: scale(1);
  }
}

@keyframes rotate {
  0% {
    transform: rotate(0deg);
  }
  100% {
    transform: rotate(360deg);
  }
}

@keyframes wobble {
  0% {
    transform: translateX(0%);
  }
  15% {
    transform: translateX(-25%) rotate(-5deg);
  }
  30% {
    transform: translateX(20%) rotate(3deg);
  }
  45% {
    transform: translateX(-15%) rotate(-3deg);
  }
  60% {
    transform: translateX(10%) rotate(2deg);
  }
  75% {
    transform: translateX(-5%) rotate(-1deg);
  }
  100% {
    transform: translateX(0%);
  }
}

.no-description {
  font-style: italic;
  color: #808080;
}

::-webkit-scrollbar {
  width: 10px;
}

::-webkit-scrollbar-track {
  background: #f7f7f7;
}

::-webkit-scrollbar-thumb {
  background: linear-gradient(45deg, #ff6b6b, #f06543);
  border-radius: 5px;
}

@media (min-width: 769px) {
  .body-container {
    height: calc(40px * 10);
  }
}

@media (max-width: 768px) {
  .table-container {
    width: 95%;
    border-radius: 0;
    border-left: none;
    border-right: none;
  }

  .table th,
  .table td {
    padding: 0.5rem;
  }

  .icon-button {
    font-size: 1em;
  }
}

.fab {
  position: fixed;
  bottom: 30px;
  right: 30px;
  width: 60px;
  height: 60px;
  background: linear-gradient(45deg, #ff6b6b, #f06543);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 24px;
  box-shadow: 0 4px 15px rgba(255, 107, 107, 0.2);
  cursor: pointer;
  transition: all 0.3s ease;
}

.fab:hover {
  transform: scale(1.1) rotate(360deg);
}
</style>
