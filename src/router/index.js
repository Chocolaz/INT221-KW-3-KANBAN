import { createRouter, createWebHistory } from 'vue-router'
import Task from '../views/Task.vue'


const history = createWebHistory()

const routes = [
  {path: "/task" , 
  name : "task" , 
  component: Task},
  {
    path: '/',
    redirect: '/task'
  }
]

const router = createRouter({
  history,
  routes
})

export default router
