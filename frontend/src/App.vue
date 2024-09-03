<template>
  <div id="app">
    <Navbar v-if="showNavbar" />
    <RouterView />
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import Navbar from './v3/navbar.vue'
const route = useRoute()

const showNavbar = computed(() => {
  return route.name !== 'loginView'
})
</script>

<style>
#app {
  font-family: Avenir, Helvetica, Arial, sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  text-align: center;
  color: #2c3e50;
  margin-top: 0px;
  background-color: #ffff;
}
</style>

