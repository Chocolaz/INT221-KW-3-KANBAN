<script>
export default {
  props: {
    showModal: {
      type: Boolean,
      required: true
    },
    statusCode: {
      type: Number
    },
    operationType: {
      type: String,
      required: true
    },
    closeModal: {
      type: Function,
      required: true
    }
  },
  updated() {
    console.log('Received status code:', this.statusCode)
    console.log('operationType:', this.operationType)
  }
}
</script>

<template>
  <div class="modal-wrapper" v-if="showModal">
    <div class="modal">
      <div class="modal-content">
        <div v-if="statusCode === 201 && operationType === 'add'">
          <h2 class="modal-title">ADD TASK SUCCESS!</h2>
          <p>The task has been successfully added.</p>
        </div>
        <div v-if="statusCode === 200 && operationType === 'delete'">
          <h2 class="modal-title" style="color: #f67c5e">DELETE SUCCESS!</h2>
          <p class="itbkk-message">The task has been deleted.</p>
        </div>
        <div v-else-if="statusCode === 200 && operationType === 'edit'">
          <h2 class="modal-title" style="color: #f28c28">EDIT SUCCESS!</h2>
          <p>The task has been successfully edited.</p>
        </div>
        <div v-else-if="statusCode === 404">
          <h2 class="modal-title" style="color: #f67c5e">EXISTED?</h2>
          <p>An error has occurred, the task does not exist</p>
        </div>
        <!-- <div v-else>
          <h2 class="modal-title" style="color: red">ERROR!</h2>
          <p>Something went wrong. Status code: {{ statusCode }}</p>
        </div> -->
        <div class="modal-buttons">
          <button
            class="itbkk-button button-close"
            :style="{
              backgroundColor: statusCode === 200 ? '#f67c5e' : '#68d391'
            }"
            @click="closeModal"
          >
            Close
          </button>
        </div>
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
  box-shadow: 0 0 20px rgba(0, 0, 0, 0.2);
  border-radius: 8px;
  width: 400px;
}

.modal-content {
  padding: 30px;
}

.modal-title {
  margin-top: 0;
  font-size: 24px;
  font-weight: bold;
  color: #22a052;
}

.modal-buttons {
  display: flex;
  justify-content: flex-end;
}

.itbkk-button {
  padding: 10px 20px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  color: #fff;
}

.button-close {
  background-color: #68d391;
}

.button-close:hover {
  opacity: 0.8;
}
</style>
