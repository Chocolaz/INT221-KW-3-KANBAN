import { createRouter, createWebHistory } from 'vue-router'
import TaskList from './components/TaskList.vue'
import StatusList from './v2/StatusList.vue'
import Login from './v3/Login.vue'
import BoardAdd from './v3/BoardAdd.vue'
import BoardList from './v3/BoardList.vue'

const routes = [
  {
    path: '/task',
    name: 'taskView',
    component: TaskList,
    meta: { requiresAuth: true }
  },
  {
    path: '/task/:taskId',
    name: 'taskDetail',
    component: TaskList,
    meta: { requiresAuth: true }
  },
  {
    path: '/status',
    name: 'statusView',
    component: StatusList,
    meta: { requiresAuth: true }
  },
  {
    path: '/status/:statusId',
    name: 'statusDetail',
    component: StatusList,
    meta: { requiresAuth: true }
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
    //add board
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
    redirect: '/task'
  },
  {
    path: '/v3/boards/:boardId/tasks',
    name: 'TaskList',
    component: TaskList,
    props: (route) => ({ boardId: route.params.boardId })
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
      next('/task')
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
