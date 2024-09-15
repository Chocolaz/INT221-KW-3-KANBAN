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
      const boardId = this.$route.params.boardId

      if (!boardId) {
        throw new Error('Board ID is undefined')
      }

      const data = await FetchUtils.fetchData('statuses', boardId)
      this.statuses = data
    } catch (error) {
      console.error('Error fetching statuses:', error)
    }
  }
}
</script>

<template>
  <div
    class="fixed inset-0 flex items-center justify-center bg-black bg-opacity-50 z-50"
    @click.self="cancelModal"
  >
    <div class="bg-white shadow-lg rounded-lg w-full max-w-lg mt-14">
      <div class="p-6">
        <div class="flex justify-between items-center mb-4">
          <h2 class="text-xl font-semibold text-red-600">Add Task</h2>
          <button
            class="text-2xl text-red-600 hover:text-red-800"
            @click="cancelModal"
          >
            &times;
          </button>
        </div>

        <form @submit.prevent="handleSaveTask">
          <div class="mb-4">
            <label for="title" class="block text-sm font-medium text-gray-700"
              >Title</label
            >
            <input
              type="text"
              id="title"
              v-model="taskDetails.title"
              class="mt-1 p-2 block w-full border border-gray-300 rounded-md shadow-sm focus:ring-red-500 focus:border-red-500"
              required
              maxlength="100"
              placeholder="Enter task title"
            />
            <small v-if="taskDetails.title.length > 90" class="text-red-600">
              {{ 100 - taskDetails.title.length }} characters left
            </small>
          </div>

          <div class="mb-4">
            <label
              for="description"
              class="block text-sm font-medium text-gray-700"
              >Description</label
            >
            <textarea
              id="description"
              v-model="taskDetails.description"
              class="mt-1 p-2 block w-full border border-gray-300 rounded-md shadow-sm focus:ring-red-500 focus:border-red-500"
              maxlength="500"
              placeholder="Enter task description"
            ></textarea>
            <small
              v-if="taskDetails.description.length > 450"
              class="text-red-600"
            >
              {{ 500 - taskDetails.description.length }} characters left
            </small>
          </div>

          <div class="mb-4">
            <label
              for="assignees"
              class="block text-sm font-medium text-gray-700"
              >Assignees</label
            >
            <input
              type="text"
              id="assignees"
              v-model="taskDetails.assignees"
              class="mt-1 p-2 block w-full border border-gray-300 rounded-md shadow-sm focus:ring-red-500 focus:border-red-500"
              maxlength="30"
              placeholder="Enter assignees"
            />
            <small
              v-if="taskDetails.assignees.length > 25"
              class="text-red-600"
            >
              {{ 30 - taskDetails.assignees.length }} characters left
            </small>
          </div>

          <div class="mb-4">
            <label for="status" class="block text-sm font-medium text-gray-700"
              >Status</label
            >
            <select
              id="status"
              v-model="taskDetails.statusName"
              class="mt-1 p-2 block border border-gray-300 w-60 mx-auto rounded-md shadow-sm focus:ring-red-500 focus:border-red-500"
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

          <div class="flex justify-end space-x-2">
            <button
              type="button"
              class="py-2 px-4 bg-gray-200 text-gray-700 rounded-md hover:bg-gray-300"
              @click="cancelModal"
            >
              Cancel
            </button>
            <button
              type="submit"
              :class="{ 'opacity-50 cursor-not-allowed': isSaveDisabled }"
              :disabled="isSaveDisabled"
              class="py-2 px-4 bg-red-600 text-white rounded-md hover:bg-red-700"
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
</style>
