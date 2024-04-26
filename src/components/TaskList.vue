<script setup>
import { onMounted, ref } from 'vue';
import Table from './Table.vue';
import { getItems} from '@/libs/fetchUtils.js'
import { TaskManagement } from '../libs/TaskManagement.js'
console.log(`${import.meta.env.VITE_API_ENDPOINT}`)
const allTask = ref(new TaskManagement())

onMounted(async () => {
    const items = await getItems(`${import.meta.env.VITE_API_ENDPOINT}`)
    allTask.value.getDtoAllTasks(items)
    console.log(allTask.value.getTasks())
})

</script>
 
<template>
<div>
    <Table :tasks="allTask.getTasks()"/>
</div>
</template>
 
<style scoped>

</style>