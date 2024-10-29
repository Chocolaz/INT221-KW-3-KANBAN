<template>
  <div class="p-6 bg-gray-50">
    <h2 class="text-3xl font-semibold mb-4 text-center text-red-600">
      Manage Collaborators
    </h2>
    <div class="flex justify-center mb-4">
      <button
        @click="showAddCollabModal = true"
        :class="{
          'bg-red-600 text-white hover:bg-red-500': isBoardOwner,
          'bg-gray-400 text-gray-200': !isBoardOwner
        }"
        class="py-2 px-4 rounded-lg shadow focus:outline-none focus:ring-2 focus:ring-red-300"
        :disabled="!isBoardOwner"
      >
        Add Collaborator
      </button>
    </div>
    <p v-if="!isBoardOwner" class="text-red-500 text-center">
      You must be the board owner to add collaborators.
    </p>
    <div class="overflow-x-auto">
      <table
        class="min-w-full bg-white border border-gray-200 rounded-lg shadow"
      >
        <thead>
          <tr class="bg-red-100">
            <th class="border px-6 py-4 text-left text-gray-600">No</th>
            <th class="border px-6 py-4 text-left text-gray-600">Name</th>
            <th class="border px-6 py-4 text-left text-gray-600">Email</th>
            <th class="border px-6 py-4 text-left text-gray-600">
              Access Right
            </th>
            <th
              v-if="isBoardOwner"
              class="border px-6 py-4 text-left text-gray-600"
            >
              Manage Access
            </th>
          </tr>
        </thead>
        <tbody>
          <tr v-if="collaborators.length === 0">
            <td colspan="5" class="text-gray-500 text-xl text-center py-6">
              No collaborators added
            </td>
          </tr>
          <tr
            v-for="(collab, index) in collaborators"
            :key="collab.oid"
            class="border-b hover:bg-red-50"
          >
            <td class="border px-6 py-4 text-center">{{ index + 1 }}</td>
            <td class="border px-6 py-4 text-center">{{ collab.name }}</td>
            <td class="border px-6 py-4 text-center">{{ collab.email }}</td>
            <td class="border px-6 py-4 text-center">
              {{ collab.access_right }}
            </td>
            <td v-if="isBoardOwner" class="border px-6 py-4 text-center">
              <select
                v-model="collab.access_right"
                @change="updateAccessRight(collab.oid, collab.access_right)"
                class="border border-gray-300 p-2 rounded"
              >
                <option value="READ">READ</option>
                <option value="WRITE">WRITE</option>
              </select>
            </td>
          </tr>
        </tbody>
      </table>
    </div>
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

const props = defineProps(['boardId'])

const collaborators = ref([])
const showAddCollabModal = ref(false)
const isBoardOwner = ref(false)

const fetchCollaborators = async () => {
  try {
    const response = await fetchUtils.getCollab(props.boardId)
    collaborators.value = response.sort(
      (a, b) => new Date(a.added_on) - new Date(b.added_on)
    )

    const boardResponse = await fetchUtils.getBoards()
    const board = boardResponse.find((b) => b.id === props.boardId)
    if (board && board.owner.name === localStorage.getItem('username')) {
      isBoardOwner.value = true
    }
  } catch (error) {
    console.error('Error fetching collaborators:', error.message)
  }
}

const updateAccessRight = async (collabId, newAccessRight) => {
  try {
    await fetchUtils.updateCollabAccess(props.boardId, collabId, newAccessRight)
    console.log(
      `Updated access right for collaborator ${collabId} to ${newAccessRight}`
    )
  } catch (error) {
    console.error(
      `Error updating access right for collaborator ${collabId}:`,
      error
    )
  }
}

onMounted(fetchCollaborators)
</script>
