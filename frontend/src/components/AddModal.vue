<script>
import FetchUtils from '../lib/fetchUtils'

export default {
  props: {
    closeModal: {
      type: Function,
      required: true
    }
  },
  data() {
    return {
      taskDetails: {
        title: '',
        description: '',
        assignees: '',
        statusName: 'No Status'
      },
      statuses: []
    }
  },
  computed: {
    isSaveDisabled() {
      return (
        !this.taskDetails.title.trim() ||
        this.taskDetails.title.length > 100 ||
        this.taskDetails.description.length > 500 ||
        this.taskDetails.assignees.length > 30
      )
    }
  },
  methods: {
    async handleSaveTask() {
      try {
        const { success, data, statusCode } = await FetchUtils.postData(
          'tasks',
          this.taskDetails
        )
        if (success && statusCode === 201) {
          console.log('The task has been successfully added', statusCode)
          this.$emit('taskSaved', data)
          this.$emit('showStatusModal', statusCode)
          this.taskDetails = {
            title: '',
            description: '',
            assignees: '',
            statusName: 'No Status'
          }
          this.closeModal()
        } else {
          console.error('Something wrong while adding the task')
        }
      } catch (error) {
        console.error('Error saving task:', error)
      }
    },
    cancelModal() {
      this.closeModal()
    }
  },
  async created() {
    try {
      const boardId = this.$route.params.boardId // Retrieve boardId from route params

      if (!boardId) {
        throw new Error('Board ID is undefined')
      }

      // Fetch statuses using boardId
      const data = await FetchUtils.fetchData('statuses', boardId)
      this.statuses = data
    } catch (error) {
      console.error('Error fetching statuses:', error)
    }
  }
}
</script>

<template>
  <div class="modal-wrapper" @click.self="cancelModal">
    <div class="modal" :class="{ 'modal-open': true }">
      <div class="modal-content">
        <h2 class="modal-title">Add Task</h2>
        <button class="close-button" @click="cancelModal">&times;</button>

        <form @submit.prevent="handleSaveTask">
          <div class="form-group">
            <label for="title">Title:</label>
            <input
              type="text"
              id="title"
              v-model="taskDetails.title"
              class="itbkk-title"
              required
              maxlength="100"
              placeholder="Enter task title"
            />
            <small v-if="taskDetails.title.length > 90" class="error">
              {{ 100 - taskDetails.title.length }} characters left
            </small>
          </div>
          <div class="form-group">
            <label for="description">Description:</label>
            <textarea
              id="description"
              v-model="taskDetails.description"
              class="itbkk-description"
              maxlength="500"
              placeholder="Enter task description"
            ></textarea>
            <small v-if="taskDetails.description.length > 450" class="error">
              {{ 500 - taskDetails.description.length }} characters left
            </small>
          </div>
          <div class="form-group">
            <label for="assignees">Assignees:</label>
            <input
              type="text"
              id="assignees"
              v-model="taskDetails.assignees"
              class="itbkk-assignees"
              maxlength="30"
              placeholder="Enter assignees"
            />
            <small v-if="taskDetails.assignees.length > 25" class="error">
              {{ 30 - taskDetails.assignees.length }} characters left
            </small>
          </div>
          <div class="form-group">
            <label for="status">Status:</label>
            <select
              id="status"
              v-model="taskDetails.statusName"
              class="itbkk-status"
            >
              <option v-if="statuses.length === 0" value="" disabled>
                Loading...
              </option>
              <option
                v-else
                v-for="status in statuses"
                :key="status.statusId"
                :value="status.statusName"
              >
                {{ status.statusName }}
              </option>
            </select>
          </div>

          <div class="modal-buttons">
            <button
              class="itbkk-button itbkk-button-cancel"
              type="button"
              @click="cancelModal"
            >
              Cancel
            </button>
            <button
              class="itbkk-button itbkk-button-confirm"
              type="submit"
              :class="{ disabled: isSaveDisabled }"
              :disabled="isSaveDisabled"
            >
              Save
            </button>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<style scoped>
.modal-wrapper {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(0, 0, 0, 0.5);
  z-index: 1000;
  display: flex;
  justify-content: center;
  align-items: center;
}

.modal {
  background-color: white;
  box-shadow: 0 0 10px rgba(0, 0, 0, 0.15);
  border-radius: 8px;
  width: 80%;
  max-width: 500px;
  font-size: 13px;
}

.modal-content {
  padding: 20px;
  position: relative;
}

.modal-title {
  margin-top: 0;
  margin-bottom: 15px;
  font-size: 20px;
  font-weight: bold;
  color: #e74c3c;
}

.close-button {
  position: absolute;
  top: 8px;
  right: 8px;
  font-size: 20px;
  background: none;
  border: none;
  color: #ff6b6b;
  cursor: pointer;
  transition: color 0.3s ease;
}

.close-button:hover {
  color: #f06543;
}

.form-group {
  margin-bottom: 15px;
}

label {
  display: block;
  font-weight: bold;
  margin-bottom: 3px;
  color: #343a40;
}

input[type='text'],
textarea,
select {
  width: 100%;
  padding: 8px;
  border-radius: 4px;
  border: 1px solid #ccc;
  font-size: 14px;
  transition: border-color 0.3s ease, box-shadow 0.3s ease;
}

input[type='text']:focus,
textarea:focus,
select:focus {
  outline: none;
  border-color: #ff6b6b;
  box-shadow: 0 0 0 2px rgba(255, 107, 107, 0.2);
}

textarea {
  height: 100px;
  resize: vertical;
}

.itbkk-status {
  width: 100px;
}

.modal-buttons {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
}

.itbkk-button {
  padding: 8px 16px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
  font-weight: bold;
  transition: background-color 0.3s ease, transform 0.1s ease;
}

.itbkk-button:active {
  transform: scale(0.95);
}

.itbkk-button-confirm {
  background-color: #ff6b6b;
  color: #fff;
}

.itbkk-button-cancel {
  background-color: #f8f9fa;
  color: #343a40;
}

.itbkk-button-confirm:hover {
  background-color: #f06543;
}

.itbkk-button-cancel:hover {
  background-color: #e9ecef;
}

.error {
  color: #ff6b6b;
  font-size: 11px;
  margin-top: 5px;
}

.disabled {
  background-color: #ced4da;
  cursor: not-allowed;
}

@keyframes shake {
  0%,
  100% {
    transform: translateX(0);
  }
  10%,
  30%,
  50%,
  70%,
  90% {
    transform: translateX(-5px);
  }
  20%,
  40%,
  60%,
  80% {
    transform: translateX(5px);
  }
}

.error {
  animation: shake 0.82s cubic-bezier(0.36, 0.07, 0.19, 0.97) both;
}
</style>
