<template>
    <div class="p-8 space-y-8">
      <section>
        <h2 class="text-2xl font-semibold mb-4">Personal Boards</h2>
        <table class="table-auto w-full border-collapse border border-gray-300">
          <thead>
            <tr class="bg-gray-100">
              <th class="border border-gray-300 px-4 py-2">No</th>
              <th class="border border-gray-300 px-4 py-2">Name</th>
              <th class="border border-gray-300 px-4 py-2">Visibility</th>
            </tr>
          </thead>
          <tbody>
            <tr 
              v-for="(board, index) in personalBoards" 
              :key="board.id" 
              class="hover:bg-gray-50"
            >
              <td class="border border-gray-300 px-4 py-2 text-center">{{ index + 1 }}</td>
              <td class="border border-gray-300 px-4 py-2 itbkk-board-name">{{ board.name }}</td>
              <td class="border border-gray-300 px-4 py-2 text-center">{{ board.visibility }}</td>
            </tr>
          </tbody>
        </table>
      </section>
  
      <section>
        <h2 class="text-2xl font-semibold mb-4">Collab Boards</h2>
        <table class="table-auto w-full border-collapse border border-gray-300">
          <thead>
            <tr class="bg-gray-100">
              <th class="border border-gray-300 px-4 py-2">No</th>
              <th class="border border-gray-300 px-4 py-2">Name</th>
              <th class="border border-gray-300 px-4 py-2">Owner</th>
              <th class="border border-gray-300 px-4 py-2">Action</th>
            </tr>
          </thead>
          <tbody>
            <tr 
              v-for="(board, index) in collabBoards" 
              :key="board.id" 
              class="hover:bg-gray-50"
            >
              <td class="border border-gray-300 px-4 py-2 text-center">{{ index + 1 }}</td>
              <td class="border border-gray-300 px-4 py-2 itbkk-board-name">{{ board.name }}</td>
              <td class="border border-gray-300 px-4 py-2 text-center">{{ board.owner.name }}</td>
              <td class="border border-gray-300 px-4 py-2 text-center">
                <button
                  class="bg-red-500 hover:bg-red-600 text-white font-semibold px-4 py-2 rounded"
                  @click="leaveBoard(board.id)"
                >
                  Leave
                </button>
              </td>
            </tr>
          </tbody>
        </table>
      </section>
    </div>
  </template>
  
  <script lang="ts" setup>
  import { ref, onMounted } from 'vue';
  
  const personalBoards = ref([]);
  const collabBoards = ref([]);
  
  const fetchBoards = async () => {
    try {
      const response = await fetch('http://localhost:8080/v3/boards');
      if (!response.ok) {
        throw new Error(`Error: ${response.statusText}`);
      }
      const data = await response.json();
  
      personalBoards.value = data.filter((board: any) => board.visibility);
      collabBoards.value = data.filter((board: any) => board.owner);
    } catch (error) {
      console.error('Failed to fetch boards:', error);
    }
  };
  
  const leaveBoard = (boardId: string) => {
    console.log(`Leaving board with ID: ${boardId}`);
    collabBoards.value = collabBoards.value.filter(board => board.id !== boardId);
  };
  
  onMounted(fetchBoards);
  </script>
  
  <style scoped>
  .itbkk-board-name {
    @apply font-semibold text-blue-600 cursor-pointer;
  }
  
  .itbkk-board-name:hover {
    @apply underline;
  }
  </style>
  