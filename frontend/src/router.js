import { createRouter, createWebHistory } from 'vue-router'
import TaskList from './components/TaskList.vue'
import StatusList from './v2/StatusList.vue'
import Login from './v3/Login.vue'
import BoardAdd from './v3/BoardAdd.vue'
import BoardList from './v3/BoardList.vue'

const routes = [
  {
    path: '/boards/:boardId/tasks',
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
  {
    path: '/board',
    name: 'boardView',
    component: BoardList
  },
  {
    path: '/board/add',
    name: 'boardAdd',
    component: BoardAdd
  },
  {
    path: '/',
    redirect: '/board'
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

// guard
router.beforeEach((to, from, next) => {
  const isAuthenticated = localStorage.getItem('isAuthenticated')

  if (to.name === 'loginView') {
    if (isAuthenticated) {
      next('/boards/someBoardId/tasks') // Redirect to a default path if authenticated
    } else {
      next()
    }
  } else if (to.meta.requiresAuth) {
    if (isAuthenticated) {
      next()
    } else {
      next('/login')
    }
  } else {
    next()
  }
})

export default router
