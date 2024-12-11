<script setup>
import { ref, onMounted } from 'vue'
import fetchUtils from '../lib/fetchUtils'
import AddCollaborator from './AddCollaborator.vue'
import ModalAccess from './ModalAccess.vue'
import RemoveCollabModal from './RemoveCollabModal.vue'

const props = defineProps(['boardId'])

const collaborators = ref([])
const showAddCollabModal = ref(false)
const showModalAccess = ref(false)
const showRemoveModal = ref(false)
const selectedCollabId = ref(null)
const selectedCollabName = ref('')
const selectedNewAccessRight = ref('')
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

const openModalAccess = (collabId, collabName, newAccessRight) => {
  selectedCollabId.value = collabId
  selectedCollabName.value = collabName
  selectedNewAccessRight.value = newAccessRight
  showModalAccess.value = true
}

const openModalRemove = (collabId, collabName) => {
  selectedCollabId.value = collabId
  selectedCollabName.value = collabName
  showRemoveModal.value = true
}

onMounted(fetchCollaborators)
</script>

<template>
  <div class="flex justify-center items-center mt-10 ">
    <div class="w-full max-w-5xl mx-auto rounded-xl ">
      <h2 class="text-2xl font-bold text-black text-center mb-6">
        Manage Collaborators
      </h2>

      <div class="flex justify-center mb-4">
        <button
          @click="showAddCollabModal = true"
          :class="{
            'bg-red-500 text-white hover:bg-red-400': isBoardOwner,
            'bg-gray-300 text-gray-500 cursor-not-allowed': !isBoardOwner
          }"
          class="py-2 px-4 rounded shadow focus:outline-none focus:ring-2 focus:ring-red-300"
          :disabled="!isBoardOwner"
        >
          <i class="fas fa-plus-circle mr-2"></i>Add Collaborator
        </button>
      </div>

      <p v-if="!isBoardOwner" class="text-red-400 text-center mb-4">
        You must be the board owner to manage collaborators.
      </p>

      <div class="rounded-lg border-2 border-red-400 shadow-md overflow-hidden">
        <table class="table-fixed min-w-full bg-white">
          <thead>
            <tr class="bg-red-400 text-white">
              <th class="w-1/12 px-4 py-3 text-center">No</th>
              <th class="w-2/12 px-4 py-3 text-center">Name</th>
              <th class="w-3/12 px-4 py-3 text-center">Email</th>
              <th class="w-3/12 px-4 py-3 text-center">Access Right</th>
              <th v-if="isBoardOwner" class="w-2/12 px-4 py-3 text-center">
                Manage Access
              </th>
            </tr>
          </thead>
          <tbody>
            <tr v-if="collaborators.length === 0">
              <td colspan="5" class="py-10 text-center text-gray-500 italic text-lg">
                No collaborators added
              </td>
            </tr>
            <tr
              v-for="(collab, index) in collaborators"
              :key="collab.oid"
              class="hover:bg-red-50 border-b"
            >
              <td class="px-4 py-4 text-center">{{ index + 1 }}</td>
              <td class="px-4 py-4 text-center">{{ collab.name }}</td>
              <td class="px-4 py-4 text-center">{{ collab.email }}</td>
              <td class="px-4 py-4 text-center">{{ collab.access_right }}</td>
              <td v-if="isBoardOwner" class="px-4 py-4 text-center">
                <div class="flex justify-center gap-2">
                  <select
                    :value="collab.access_right"
                    @change="openModalAccess(collab.oid, collab.name, $event.target.value)"
                    class="border border-gray-300 rounded p-2 focus:ring-red-300"
                  >
                    <option value="READ">READ</option>
                    <option value="WRITE">WRITE</option>
                  </select>
                  <button
                    @click="openModalRemove(collab.oid, collab.name)"
                    class="bg-red-500 text-white px-3 py-1 rounded hover:bg-red-400"
                  >
                    <i class="fas fa-trash-alt"></i> Remove
                  </button>
                </div>
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
      <ModalAccess
        v-if="showModalAccess"
        :board-id="boardId"
        :collab-id="selectedCollabId"
        :name="selectedCollabName"
        :new-right="selectedNewAccessRight"
        @confirmed="fetchCollaborators"
        @close="showModalAccess = false"
      />
      <RemoveCollabModal
        v-if="showRemoveModal"
        :board-id="boardId"
        :collab-id="selectedCollabId"
        :collab-name="selectedCollabName"
        @confirmed="fetchCollaborators"
        @close="showRemoveModal = false"
      />
    </div>
  </div>
</template>