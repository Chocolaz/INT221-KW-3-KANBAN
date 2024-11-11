<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import TaskModal from './TaskModal.vue'
import AddModal from './AddModal.vue'
import DeleteModal from './DeleteModal.vue'
import StatusModal from './ToastTask.vue'
import EditModal from './EditModal.vue'
import FilterModal from './FilterModal.vue'
import FetchUtils from '../lib/fetchUtils'
import { statusStyle } from '../lib/statusStyles'
import { checkOwnership, checkAccessRight } from '../lib/utils'

const tasks = ref([])
const statuses = ref([])
const selectedTask = ref(null)
const showAddModal = ref(false)
const showDeleteModal = ref(false)
const showSuccessModal = ref(false)
const statusCode = ref(null)
const taskIdToDelete = ref(null)
const showEditModal = ref(false)
const taskToEdit = ref(null)
const operationType = ref('')
const taskTitleToDelete = ref(null)
const taskIndexToDelete = ref(null)
const selectedStatuses = ref(statuses.value.map((status) => status.statusName))

const sortOrder = ref(0)
const showFilterModal = ref(false)

const route = useRoute()
const router = useRouter()

const boardId = route.params.boardId
const boardData = ref(null)
const collaborators = ref([])
const currentUser = ref(localStorage.getItem('username'))
const canOperation = ref(false)

const tooltipMessage =
  'You need to be board owner or has write access to perform this action.'

const fetchCollaborators = async () => {
  try {
    collaborators.value = await FetchUtils.getCollab(boardId)
    console.log(collaborators.value)
  } catch (error) {
    console.error('Error fetching collaborator details:', error)
  }
}

const fetchBoardDetails = async () => {
  try {
    boardData.value = await FetchUtils.getBoards(boardId)

    await fetchCollaborators()

    console.log('boardId:', boardId)

    checkBoardAccess()
  } catch (error) {
    console.error('Error fetching board details:', error)
  }
}

const checkBoardAccess = () => {
  const isOwner = checkOwnership(boardData.value, currentUser.value)
  const hasWriteAccess = checkAccessRight(
    collaborators.value,
    currentUser.value
  )

  console.log('checkBoardAccess collaborators:', collaborators.value)
  console.log('checkBoardData:', boardData.value)

  canOperation.value = isOwner || hasWriteAccess
}

const formatLocalDate = (dateString) => {
  const date = new Date(dateString)
  return date.toLocaleString('en-GB')
}

const timezone = computed(
  () => Intl.DateTimeFormat().resolvedOptions().timeZone
)

const fetchTasks = async () => {
  try {
    if (!boardId) {
      throw new Error('Board ID is undefined')
    }

    const data = await FetchUtils.fetchData('tasks', boardId)
    tasks.value = data

    const taskId = route.params.taskId
    if (taskId && !tasks.value.some((task) => task.taskId === taskId)) {
      router.push({ name: 'boardView' })
    }
  } catch (error) {
    console.error('Error fetching tasks:', error)
  }
}

const fetchStatuses = async () => {
  try {
    if (!boardId) {
      throw new Error('Board ID is undefined')
    }

    const data = await FetchUtils.fetchData('statuses', boardId)
    statuses.value = data
  } catch (error) {
    console.error('Error fetching statuses:', error)
  }
}

const getStatusLabel = (statusName, statuses) => {
  const status = statuses.find((s) => s.name === statusName)
  return status ? status.name : 'No Status'
}

const sortedTasks = computed(() => {
  let sorted = [...tasks.value]

  if (sortOrder.value === 1) {
    sorted.sort((a, b) =>
      (a.statusName || '').localeCompare(b.statusName || '')
    )
  } else if (sortOrder.value === 2) {
    sorted.sort((a, b) =>
      (b.statusName || '').localeCompare(a.statusName || '')
    )
  } else {
    sorted.sort((a, b) => new Date(a.createdOn) - new Date(b.createdOn))
  }

  return sorted
})

const filteredTasks = computed(() => {
  if (selectedStatuses.value.length > 0) {
    return sortedTasks.value.filter((task) =>
      selectedStatuses.value.includes(task.statusName)
    )
  } else {
    return sortedTasks.value
  }
})

const openModal = async (taskId) => {
  if (!taskId || !boardId) {
    console.error('Task ID or Board ID is invalid or missing.')
    return
  }
  try {
    const data = await FetchUtils.fetchData('tasks', boardId, taskId)
    if (data) {
      selectedTask.value = data
      console.log(selectedTask.value)
    }
  } catch (error) {
    console.error('Error fetching task details:', error)
    if (error.status === 404) {
      alert('Task not found.')
    } else {
      alert('Error fetching task.')
    }
  }
}

const handleTaskClick = (taskId) => {
  if (taskId) {
    openModal(taskId)
  } else {
    console.error('Invalid taskId:', taskId)
  }
}
const handleAddTask = () => {
  showAddModal.value = true
  operationType.value = 'add'
}
const handleTaskSaved = (savedTask) => {
  tasks.value.push(savedTask)
  showAddModal.value = false
  tasks.value.sort((a, b) => new Date(a.createdOn) - new Date(b.createdOn))
  fetchTasks()
}
const cancelAdd = () => {
  showAddModal.value = false
}
const closeModal = () => {
  selectedTask.value = null
}
const openDeleteModal = (taskId) => {
  const task = tasks.value.find((task) => task.taskId === taskId)
  if (task) {
    taskIdToDelete.value = taskId
    taskTitleToDelete.value = task.title
    taskIndexToDelete.value = tasks.value.indexOf(task) + 1
    operationType.value = 'delete'
    showDeleteModal.value = true
  }
}
const handleTaskDeleted = (deletedTaskId, receivedStatusCode) => {
  console.log('Received deletion status code:', receivedStatusCode)
  statusCode.value = receivedStatusCode
  tasks.value = tasks.value.filter((task) => task.taskId !== deletedTaskId)
  closeDeleteModal()
  showSuccessModal.value = true
}
const closeDeleteModal = () => {
  showDeleteModal.value = false
}
const closeSuccessModal = () => {
  showSuccessModal.value = false
}
const handleShowStatusModal = (status) => {
  if (status === 201 || status === 200) {
    showSuccessModal.value = true
    statusCode.value = status
  }
}
const openEditModal = async (taskId) => {
  try {
    const data = await FetchUtils.fetchData('tasks', boardId, taskId)
    taskToEdit.value = data
    console.log(taskToEdit.value)
    if (taskToEdit.value) {
      operationType.value = 'edit'
      showEditModal.value = true
    }
  } catch (error) {
    console.error('Error fetching task details:', error)
    alert('Failed to edit task. Please try again.')
  }
}

const closeEditModal = () => {
  showEditModal.value = false
}
const onTaskUpdated = (updatedTask, status) => {
  const taskIndex = tasks.value.findIndex(
    (task) => task.taskId === updatedTask.taskId
  )
  if (taskIndex !== -1) {
    tasks.value[taskIndex] = updatedTask
  }
  handleEditSuccess(status)
}

const handleEditSuccess = (status) => {
  console.log('Received status code after edit:', status)
  statusCode.value = status
  showSuccessModal.value = true
}

const sortTasksByStatus = () => {
  sortOrder.value = (sortOrder.value + 1) % 3
}

const openFilterModal = () => {
  showFilterModal.value = true
}

const closeFilterModal = () => {
  showFilterModal.value = false
}

const applyFilter = (selectedStatusesValue) => {
  console.log('Selected statuses:', selectedStatusesValue)
  selectedStatuses.value = selectedStatusesValue
  closeFilterModal()
}

// Fetch board details and tasks on mount
onMounted(async () => {
  try {
    await fetchBoardDetails() // Fetch board to get permissions
    await fetchTasks()
    await fetchStatuses()
    const taskId = route.params.taskId
    if (taskId) {
      await openModal(taskId)
    }
  } catch (error) {
    console.error('Error during onMounted:', error)
  }
})
</script>

<template>
  <div>
    <div id="app">
      <div class="table-container relative">
        <table class="table header-table">
          <thead>
            <tr>
              <th class="itbkk-button-add" style="text-align: center">
                <div class="relative inline-block z-10 tooltip">
                  <button
                    @click="canOperation ? handleAddTask() : null"
                    class="icon-button add-button"
                    :disabled="!canOperation"
                  >
                    <i class="fas fa-plus-circle"></i>
                  </button>
                  <span
                    v-if="!canOperation"
                    class="tooltiptext invisible w-48 bg-red-500 text-white text-center rounded-md absolute bottom-[60%] left-1/2 text-[8px] ml-[-90px] opacity-0 transition-opacity duration-500"
                  >
                    {{ tooltipMessage }}</span
                  >
                </div>
              </th>
              <th>
                Title
                <button
                  @click="openFilterModal"
                  class="icon-button filter-button"
                >
                  <i class="fas fa-filter"></i>
                </button>
              </th>
              <th>Assignees</th>
              <th>Attachment</th>
              <th style="position: relative">
                Status
                <button
                  @click="sortTasksByStatus"
                  class="icon-button sort-button"
                >
                  <i
                    :class="[
                      'fas',
                      sortOrder === 0
                        ? 'fa-sort'
                        : sortOrder === 1
                        ? 'fa-sort-up'
                        : 'fa-sort-down'
                    ]"
                  ></i>
                </button>
              </th>
              <th style="width: 100px; text-align: center">
                <i
                  class="fas fa-ellipsis-h"
                  style="
                    width: 25px;
                    height: 25px;
                    display: block;
                    margin: 0 auto;
                    margin-top: 10px;
                  "
                ></i>
              </th>
            </tr>
          </thead>
        </table>
        <div class="body-container relative">
          <table class="table body-table w-full">
            <tbody>
              <tr v-if="filteredTasks.length === 0" class="h-full">
                <td colspan="5" class="relative">
                  <div
                    class="absolute inset-0 flex items-center justify-center text-xl italic text-gray-500 py-4 bg-gray-100 border border-gray-300 rounded-lg shadow-md transition duration-300 ease-in-out transform hover:scale-105"
                  >
                    No Task Provided
                  </div>
                </td>
              </tr>
              <tr
                v-for="(task, index) in filteredTasks"
                :key="task.taskId"
                class="itbkk-item"
              >
                <td class="border px-4 py-2" style="text-align: center">
                  {{ index + 1 }}
                </td>
                <td class="itbkk-title" @click="handleTaskClick(task.taskId)">
                  {{ task.title }}
                </td>
                <td
                  class="border px-4 py-2 itbkk-assignees"
                  :style="{
                    fontStyle: task.assignees ? 'normal' : 'italic',
                    color: task.assignees ? 'black' : 'grey'
                  }"
                >
                  {{ task.assignees || 'Unassigned' }}
                </td>
                <td
                  class="border px-4 py-2 itbkk-attachment"
                  :style="{
                    fontStyle:
                      task.attachmentCount === '-' ? 'italic' : 'normal',
                    color: task.attachmentCount === '-' ? 'grey' : 'black'
                  }"
                >
                  {{ task.attachmentCount }}
                </td>
                <td
                  class="border px-4 py-2 itbkk-status"
                  :data-status="task.statusName"
                  :style="statusStyle(task.statusName)"
                >
                  {{ getStatusLabel(task.statusName, statuses) }}
                </td>
                <td class="border px-4 py-2" style="width: 100px">
                  <div class="action-buttons">
                    <button class="itbkk-button-action">
                      <div class="relative inline-block z-10 tooltip">
                        <button
                          class="icon-button edit-button"
                          @click="
                            canOperation ? openEditModal(task.taskId) : null
                          "
                        >
                          <i class="fas fa-edit"></i>
                        </button>
                        <span
                          v-if="!canOperation"
                          class="tooltiptext invisible w-[103px] bg-red-500 text-white text-center rounded-md absolute bottom-[30%] left-1/2 text-[8px] ml-[-125px] opacity-0 transition-opacity duration-500"
                        >
                          {{ tooltipMessage }}
                        </span>
                      </div>

                      <div class="relative inline-block z-10 tooltip">
                        <button
                          class="icon-button delete-button"
                          @click="
                            canOperation ? openDeleteModal(task.taskId) : null
                          "
                        >
                          <i class="fas fa-trash-alt"></i>
                        </button>
                        <span
                          v-if="!canOperation"
                          class="tooltiptext invisible w-[103px] bg-red-500 text-white text-center rounded-md absolute bottom-[-10%] left-1/2 text-[8px] ml-[-120px] opacity-0 transition-opacity duration-500"
                        >
                          {{ tooltipMessage }}
                        </span>
                      </div>
                    </button>
                  </div>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </div>

    <task-modal
      v-if="selectedTask"
      :task="selectedTask"
      :timezone="timezone"
      :createdDate="formatLocalDate(selectedTask.createdOn)"
      :updatedDate="formatLocalDate(selectedTask.updatedOn)"
      :closeModal="closeModal"
    />

    <add-modal
      v-if="showAddModal"
      @taskSaved="handleTaskSaved"
      @showStatusModal="handleShowStatusModal"
      :closeModal="cancelAdd"
    />

    <status-modal
      :showModal="showSuccessModal"
      :statusCode="statusCode"
      :closeModal="closeSuccessModal"
      :operationType="operationType"
    />

    <delete-modal
      v-if="showDeleteModal"
      :closeModal="closeDeleteModal"
      :taskId="taskIdToDelete"
      :taskTitle="taskTitleToDelete"
      :taskIndex="taskIndexToDelete"
      @deleted="handleTaskDeleted"
    />

    <edit-modal
      v-if="showEditModal"
      :task="taskToEdit"
      :closeModal="closeEditModal"
      :onTaskUpdated="onTaskUpdated"
      @editSuccess="handleEditSuccess"
    />

    <filter-modal
      v-if="showFilterModal"
      :statuses="statuses"
      :selectedStatuses="selectedStatuses"
      @applyFilter="applyFilter"
      @close="closeFilterModal"
    ></filter-modal>
  </div>
</template>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Poppins:wght@300;400;600&family=Montserrat:wght@700&display=swap');

.tooltip:hover .tooltiptext {
  visibility: visible;
  opacity: 1;
  word-break: break-word;
  white-space: normal;
}

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
  width: 900px;
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
  max-height: calc(35px * 10);
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
  padding: 8px; /* Reduced padding */
  text-align: left;
  height: 30px; /* Reduced height */
  word-wrap: break-word;
  word-break: break-all;
  white-space: normal;
}

.table th {
  background-color: #ff6b6b;
  font-weight: bold;
  color: #ffffff;
  font-size: 14px; /* Reduced font size */
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

.itbkk-status {
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

.action-button {
  border: none;
  border-radius: 10px;
  background: #f06543;
  color: #ffffff;
  padding: 10px;
  transition: all 0.3s ease;
}

.action-button:active {
  transform: scale(0.95);
}

.itbkk-item {
  transition: all 0.3s ease;
  transform: translateY(20px);
  opacity: 0;
  animation: fadeIn 0.5s forwards;
}

@keyframes fadeIn {
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.itbkk-item:hover {
  transform: scale(1.02);
  box-shadow: 0 5px 15px rgba(255, 107, 107, 0.2);
}

.itbkk-button-add button,
.itbkk-button-action button,
.itbkk-button-sort,
.itbkk-filter-status {
  transition: transform 0.2s ease;
}

.itbkk-button-add button:active,
.itbkk-button-action button:active,
.itbkk-button-sort:active,
.itbkk-filter-status:active {
  transform: scale(0.95);
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

.add-button:hover {
  animation: pulse 1s infinite;
}

.filter-button {
  margin-left: 10px;
}

.filter-button:hover {
  animation: shake 0.82s cubic-bezier(0.36, 0.07, 0.19, 0.97) both;
}

.sort-button:hover {
  animation: flip 0.6s ease;
}

.edit-button {
  color: #f06542;
}
.delete-button {
  color: #d33131;
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

@keyframes shake {
  10%,
  90% {
    transform: translate3d(-1px, 0, 0);
  }
  20%,
  80% {
    transform: translate3d(2px, 0, 0);
  }
  30%,
  50%,
  70% {
    transform: translate3d(-2px, 0, 0);
  }
  40%,
  60% {
    transform: translate3d(2px, 0, 0);
  }
}

@keyframes flip {
  0% {
    transform: perspective(400px) rotateY(0);
  }
  100% {
    transform: perspective(400px) rotateY(180deg);
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

.modal {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(5px);
  border-radius: 15px;
  border: 2px solid #ff6b6b;
  box-shadow: 0 8px 32px 0 rgba(255, 107, 107, 0.2);
}

@media (min-width: 769px) {
  .body-container {
    height: calc(40px * 10);
  }
}
@media (max-width: 768px) {
  .task-list-container {
    padding: 0.5rem;
  }

  .table-container {
    border-radius: 0;
    border-left: none;
    border-right: none;
  }

  .table th,
  .table td {
    padding: 0.5rem;
  }

  .column-assignees,
  .column-status {
    display: none;
  }

  .column-title {
    width: 60%;
  }

  .column-actions {
    width: 80px;
  }

  .fab {
    bottom: 20px;
    right: 20px;
    width: 40px;
    height: 40px;
    font-size: 16px;
  }
}

@media (max-width: 480px) {
  .column-index {
    display: none;
  }

  .column-title {
    width: 70%;
  }

  .column-actions {
    width: 60px;
  }

  .add-button img,
  .filter-button img,
  .sort-button img,
  .action-button img {
    width: 16px;
    height: 16px;
  }
}
</style>
