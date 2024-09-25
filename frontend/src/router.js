import { createRouter, createWebHistory } from 'vue-router'
import jwtDecode from 'vue-jwt-decode'

import TaskList from './v1/TaskList.vue'
import StatusList from './v2/StatusList.vue'
import Login from './v3/Login.vue'
import BoardAdd from './v3/BoardAdd.vue'
import BoardList from './v3/BoardList.vue'
import NotFound from './component/NotFound.vue'

const isTokenValid = (token) => {
  if (!token) return false

  try {
    const decodedToken = jwtDecode.decode(token)
    const currentTime = Math.floor(Date.now() / 1000)
    return decodedToken.exp > currentTime
  } catch (error) {
    console.error('Error decoding token:', error)
    return false
  }
}

const routes = [
  {
    path: '/boards/:boardId',
    name: 'taskView',
    component: TaskList,
    meta: { requiresAuth: true },
    props: (route) => ({ boardId: route.params.boardId })
  },
  {
    path: '/boards/:boardId/tasks/:taskId',
    name: 'taskDetail',
    component: TaskList,
    meta: { requiresAuth: true },
    props: (route) => ({
      boardId: route.params.boardId,
      taskId: route.params.taskId
    })
  },
  {
    path: '/boards/:boardId/status',
    name: 'statusView',
    component: StatusList,
    meta: { requiresAuth: true },
    props: (route) => ({ boardId: route.params.boardId })
  },
  {
    path: '/boards/:boardId/status/:statusId',
    name: 'statusDetail',
    component: StatusList,
    meta: { requiresAuth: true },
    props: (route) => ({
      boardId: route.params.boardId,
      statusId: route.params.statusId
    })
  },
  {
    path: '/login',
    name: 'loginView',
    component: Login
  },
  { path: '/notfound', name: 'notFound', component: NotFound },
  {
    path: '/board',
    name: 'boardView',
    component: BoardList,
    meta: { requiresAuth: true }
  },
  {
    path: '/board/add',
    name: 'boardAdd',
    component: BoardAdd,
    meta: { requiresAuth: true }
  },
  {
    path: '/',
    redirect: '/login'
  },
  {
    path: '/:pathMatch(.*)*',
    redirect: '/board'
  }
]

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes
})

router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')
  const isAuthenticated = isTokenValid(token)

  console.log('Navigating to:', to.fullPath)
  console.log('Is Authenticated:', isAuthenticated)

  if (to.meta.requiresAuth && !isAuthenticated) {
    console.log('Redirecting to login')
    next({ name: 'loginView', query: { redirect: to.fullPath } })
  } else if (to.name === 'loginView' && isAuthenticated) {
    console.log('Redirecting to boards')
    next({ name: 'boardView' })
  } else {
    next()
  }
})

export default router
