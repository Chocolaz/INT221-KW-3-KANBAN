<script setup>
import { ref, defineProps, defineEmits, watch } from 'vue'

const props = defineProps({
  statuses: Array,
  selectedStatuses: Array
})

const emit = defineEmits(['applyFilter', 'close'])

const showSelectedStatuses = ref([...props.selectedStatuses])
const selectAll = ref(false)

// Watch for changes to selectedStatuses and update selectAll
watch(
  () => props.selectedStatuses,
  (newValue) => {
    selectAll.value = newValue.length === props.statuses.length
  },
  { immediate: true }
)

const applyFilter = () => {
  emit('applyFilter', showSelectedStatuses.value)
}

const selectAllChanged = () => {
  if (selectAll.value) {
    showSelectedStatuses.value = props.statuses.map((status) => status.name)
  } else {
    showSelectedStatuses.value = []
  }
}

// Watch for changes to showSelectedStatuses and update selectAll
watch(
  () => showSelectedStatuses.value,
  () => {
    selectAll.value =
      showSelectedStatuses.value.length === props.statuses.length
  }
)
</script>

<template>
  <div
    class="fixed inset-0 bg-gray-900 bg-opacity-50 flex items-center justify-center z-50"
  >
    <div
      class="bg-white rounded-lg shadow-lg p-6 w-full max-w-md mt-16 animate-fade-in-up"
    >
      <h2 class="text-2xl font-semibold mb-4">Select Statuses to Filter</h2>
      <div class="space-y-4">
        <!-- Select All Checkbox -->
        <div class="flex items-center space-x-2">
          <input
            type="checkbox"
            v-model="selectAll"
            @change="selectAllChanged"
            class="form-checkbox h-5 w-5 text-indigo-600 transition duration-150 ease-in-out"
          />
          <label class="text-sm font-medium">Select All</label>
        </div>
        <!-- Statuses Checkboxes -->
        <div
          v-for="status in statuses"
          :key="status.name"
          class="flex items-center space-x-2"
        >
          <input
            type="checkbox"
            :value="status.name"
            v-model="showSelectedStatuses"
            @change="checkboxChanged"
            class="form-checkbox h-5 w-5 text-indigo-600 transition duration-150 ease-in-out"
          />
          <label class="text-sm">{{ status.name }}</label>
        </div>
      </div>
      <!-- Validation Message -->
      <div
        v-if="showSelectedStatuses.length === 0 && !selectAll"
        class="text-red-600 mt-2 text-sm"
      >
        You must select at least one.
      </div>
      <!-- Buttons -->
      <div class="flex justify-end gap-4 mt-4">
        <button
          @click="applyFilter"
          :disabled="showSelectedStatuses.length === 0 && !selectAll"
          :class="{
            'bg-gray-400 cursor-not-allowed':
              showSelectedStatuses.length === 0 && !selectAll,
            'bg-green-500 hover:bg-green-600':
              showSelectedStatuses.length > 0 || selectAll
          }"
          class="px-4 py-2 text-white rounded-md focus:outline-none focus:ring-2 focus:ring-green-500"
        >
          Apply Filter
        </button>
        <button
          @click="$emit('close')"
          class="bg-red-500 hover:bg-red-600 px-4 py-2 text-white rounded-md focus:outline-none focus:ring-2 focus:ring-red-500"
        >
          Close
        </button>
      </div>
    </div>
  </div>
</template>

<style scoped>
</style>
