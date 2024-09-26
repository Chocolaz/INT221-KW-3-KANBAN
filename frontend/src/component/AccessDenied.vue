<template>
  <div class="access-denied">
    <h1>Access denied</h1>
    <p>You do not have permission to view this page.</p>
  </div>
</template>

<script setup>
// You can add any necessary script logic here (e.g., to redirect users after a delay)
</script>

<style scoped>
.access-denied {
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  height: 100vh;
  text-align: center;
  background-color: #f8f9fa;
}

h1 {
  font-size: 2rem;
  color: #ff6b6b;
}

p {
  font-size: 1.25rem;
  color: #333;
}
</style>
