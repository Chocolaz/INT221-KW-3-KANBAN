<template>
  <div>
    <h2>{{ task.title }}</h2>
    <p>Status: {{ task.statusName }}</p>
    <div v-if="attachments.length">
      <h3>Attachments</h3>
      <div v-for="attachment in attachments" :key="attachment.attachmentId">
        <img
          v-if="attachment.blobUrl"
          :src="attachment.blobUrl"
          alt="Attachment Image"
          class="max-w-full h-auto"
        />
      </div>
    </div>
  </div>
</template>

<script>
import { ref, computed, onMounted } from 'vue'
import fetchUtils from '@/lib/fetchUtils'

export default {
  setup() {
    const task = ref({
      taskId: 10,
      title: 'Tassssssdsadssk Title',
      description: 'หหฟหกTask Description',
      assignees: 'Assignsdee Name',
      statusName: 'To Do',
      attachments: [
        {
          attachmentId: 4,
          file: 'b59ae87e-58fe-4cf5-a877-3627bfc9c510_Punktify.png',
          uploadedOn: '2024-11-10T16:38:05.000+00:00'
        },
        {
          attachmentId: 5,
          file: '7b80ace6-648c-4d1a-af5d-e2f3a8431234_INT210-2023-LAB-4-102.png',
          uploadedOn: '2024-11-10T16:46:15.000+00:00'
        }
      ]
    })

    const attachments = ref([])

    const formattedCreatedOn = computed(() => {
      return new Date(task.value.createdOn).toLocaleDateString()
    })

    onMounted(async () => {
      attachments.value = await fetchUtils.fetchAttachments(
        task.value.attachments
      )
    })

    return {
      task,
      attachments,
      formattedCreatedOn
    }
  }
}
</script>
