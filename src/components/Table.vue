<script setup>
import { computed } from 'vue';


    defineProps({
        tasks: Array
    })
     // Create a computed property to sort tasks by createdOn in ascending order
     const sortedTasks = computed(() => {
            return tasks.value.sort((a, b) => {
              return new Date(a.createdOn) - new Date(b.createdOn)
            })
          })
          
          // Function to get the display label for task status
          const getStatusLabel = (status) => {
            if (status === 'ToDo') {
              return 'To Do'
            }
            return status
          }
</script>
 

<template>
  <div class="table-container">
    <table class="table">
      <thead>
        <tr>
          <th class="border px-4 py-2">Title</th>
          <th class="border px-4 py-2">Assignees</th>
          <th class="border px-4 py-2">Status</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="(task,index) in tasks"  :key="index" class="itbkk-item">
          <td class="border px-4 py-2 itbkk-title">{{ task.title }}</td>
          <td class="border px-4 py-2 itbkk-assignees">
            {{ task.assignees || 'Unassigned' }}
          </td>
          <td
            class="border px-4 py-2 itbkk-status"
            :data-status="task.status"
          >
            {{ getStatusLabel(task.status) }}
          </td>
        </tr>
      </tbody>
    </table>
  </div>
</template>


<style scoped>
.table-container {
  margin: 0 auto;
  width: 80%;
  max-width: 1200px;
  overflow-x: auto;
}

.table {
  border-collapse: collapse;
  width: 100%;
  border-radius: 8px;
  overflow: hidden;
}

.table th,
.table td {
  border: 1px solid #e2e8f0;
  padding: 12px;
  text-align: left;
}

.table th {
  background-color: #edf2f7;
  font-weight: bold;
}

.itbkk-status {
  padding: 8px 12px;
  border-radius: 4px;
  text-transform: uppercase;
}

.itbkk-status[data-status='ToDo'] {
  background-color: #f67c5e;
  color: #333;
}

.itbkk-status[data-status='No Status'] {
  background-color: #d1d5db;
  color: #333;
}

.itbkk-status[data-status='Doing'] {
  background-color: #f6e05e;
  color: #333;
}

.itbkk-status[data-status='Done'] {
  background-color: #68d391;
  color: #333;
}
</style>