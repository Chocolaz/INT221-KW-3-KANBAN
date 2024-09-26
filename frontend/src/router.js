import { createRouter, createWebHistory } from 'vue-router'
import jwtDecode from 'vue-jwt-decode'

import TaskList from './v1/TaskList.vue'
import StatusList from './v2/StatusList.vue'
import Login from './v3/Login.vue'
import BoardAdd from './v3/BoardAdd.vue'
import BoardList from './v3/BoardList.vue'
import NotFound from './component/NotFound.vue'
import AccessDenied from './component/AccessDenied.vue'
import fetchUtils from '@/lib/fetchUtils'

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

const checkBoardAccess = async (boardId) => {
  try {
    const response = await fetchUtils.getBoards(boardId)

    if (response.statusCode === 403) {
      return { hasAccess: false, notFound: false }
    }

    if (response.statusCode === 404) {
      return { hasAccess: false, notFound: true }
    }

    const boardData = response.data
    const currentUser = localStorage.getItem('username')

    const boardOwner = boardData.owner.username || boardData.owner.name

    if (boardData.visibility === 'public' || boardOwner === currentUser) {
      return { hasAccess: true, notFound: false }
    }

    console.log('Access denied to user:', currentUser)
    return { hasAccess: false, notFound: false }
  } catch (error) {
    console.error('Error fetching board data:', error)
    return { hasAccess: false, notFound: error.message.includes('404') }
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
    path: '/access-denied',
    name: 'accessDenied',
    component: AccessDenied
  },
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

router.beforeEach(async (to, from, next) => {
  const token = localStorage.getItem('token')
  const isAuthenticated = isTokenValid(token)

  console.log('Navigating to:', to.fullPath)

  if (to.meta.requiresAuth && !isAuthenticated) {
    next({ name: 'loginView', query: { redirect: to.fullPath } })
  } else if (to.name === 'loginView' && isAuthenticated) {
    next({ name: 'boardView' })
  } else if (to.params.boardId) {
    try {
      const { hasAccess, notFound } = await checkBoardAccess(to.params.boardId)

      console.log('Check Board Access Result:', { hasAccess, notFound })

      if (notFound) {
        console.log('Board not found, redirecting to NotFound page')
        next({ name: 'notFound' })
      } else if (!hasAccess) {
        console.log('Access denied, redirecting to AccessDenied page')
        next({ name: 'accessDenied' })
      } else {
        next()
      }
    } catch (error) {
      console.error('Error in router guard:', error)
      next({ name: 'notFound' })
    }
  } else {
    next()
  }
})

export default router
