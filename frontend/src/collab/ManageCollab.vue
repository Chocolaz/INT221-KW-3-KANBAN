<template>
  <div class="p-6">
    <h2 class="text-3xl font-semibold mb-4 text-center">
      Manage Collaborators
    </h2>

    <div class="flex justify-center mb-4">
      <button
        @click="showAddCollabModal = true"
        class="bg-blue-600 text-white py-2 px-4 rounded-lg shadow hover:bg-blue-400 focus:outline-none focus:ring-2 focus:ring-blue-300"
        :disabled="!isBoardOwner"
      >
        Add Collaborator
      </button>
    </div>

    <p v-if="!isBoardOwner" class="text-red-500 text-center">
      You must be the board owner to add collaborators.
    </p>

    <div class="overflow-x-auto">
      <table class="min-w-full bg-white border border-gray-200">
        <thead>
          <tr>
            <th class="border px-6 py-4 text-left text-gray-600">No</th>
            <th class="border px-6 py-4 text-left text-gray-600">Name</th>
            <th class="border px-6 py-4 text-left text-gray-600">Email</th>
            <th class="border px-6 py-4 text-left text-gray-600">
              Access Right
            </th>
          </tr>
        </thead>
        <tbody>
          <tr v-if="collaborators.length === 0">
            <td colspan="4" class="text-gray-500 text-xl text-center py-6">
              No collaborators added
            </td>
          </tr>
          <tr
            v-for="(collab, index) in collaborators"
            :key="collab.oid"
            class="border-b hover:bg-gray-100"
          >
            <td class="border px-6 py-4 text-center">{{ index + 1 }}</td>
            <td class="border px-6 py-4 text-center">{{ collab.name }}</td>
            <td class="border px-6 py-4 text-center">{{ collab.email }}</td>
            <td class="border px-6 py-4 text-center">
              {{ collab.access_right }}
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- Modal for Add Collaborator -->
    <AddCollaborator
      v-if="showAddCollabModal"
      @close="showAddCollabModal = false"
      @collab-added="fetchCollaborators"
    />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import fetchUtils from '../lib/fetchUtils'
import AddCollaborator from './AddCollaborator.vue'

// Define props
const props = defineProps(['boardId'])

const collaborators = ref([])
const showAddCollabModal = ref(false)
const isBoardOwner = ref(false)

// Fetch collaborators for the current board
const fetchCollaborators = async () => {
  console.log('Board ID:', props.boardId) // Debugging: log the boardId

  if (!props.boardId) {
    console.error('Error fetching collaborators: Board ID is required')
    return
  }

  try {
    const response = await fetchUtils.getCollab(props.boardId) // Call to fetch collaborators
    collaborators.value = response.sort(
      (a, b) => new Date(a.added_on) - new Date(b.added_on)
    )

    // Check if user is board owner
    const boardResponse = await fetchUtils.getBoards() // Adjust according to your API
    const board = boardResponse.find((b) => b.id === props.boardId)
    if (board && board.owner.name === localStorage.getItem('username')) {
      isBoardOwner.value = true
    }
  } catch (error) {
    console.error('Error fetching collaborators:', error.message)
  }
}

// Fetch collaborators when the component is mounted
onMounted(fetchCollaborators)
</script>

<style scoped>
/* Add any additional styles here */
</style>
