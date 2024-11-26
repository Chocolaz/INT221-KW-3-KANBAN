<template>
  <Navbar v-if="showNavbar" />

  <BoardName v-if="isTaskList || isStatusList" />

  <div id="app">
    <RouterView />
  </div>

  <NavigationFAB />

  <ToggleVisible v-if="isTaskList || isStatusList" />

  <CollabFAB v-if="isTaskList || isManageCollab || isStatusList" />
</template>

<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import Navbar from './v3/navbar.vue'
import BoardName from './component/BoardName.vue'
import NavigationFAB from './component/NavigationFAB.vue'
import ToggleVisible from './component/ToggleVisible.vue'
import CollabFAB from './component/CollabFAB.vue'

import { useRouteChecks } from './lib/utils'

const { isTaskList, isStatusList, isManageCollab } = useRouteChecks()
const route = useRoute()

const showNavbar = computed(() => route.name !== 'loginView')
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
