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
import {
  getAccessToken,
  refreshAccessToken,
  removeTokens
} from './lib/authService'

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
  const accessToken = localStorage.getItem('access_token')
  let isAuthenticated = isTokenValid(accessToken)

  // Check if authentication is required and user is not authenticated
  if (to.meta.requiresAuth && !isAuthenticated) {
    // User is not authenticated, attempt to refresh token
    try {
      const refreshResponse = await refreshAccessToken()
      if (refreshResponse.access_token) {
        localStorage.setItem('access_token', refreshResponse.access_token)
        isAuthenticated = true // User is now authenticated
      } else {
        // No access token received, remove tokens and redirect to login
        removeTokens()
        return next({ name: 'loginView', query: { redirect: to.fullPath } })
      }
    } catch (error) {
      // Error refreshing token; remove tokens and redirect to login
      console.error('Error refreshing token:', error)
      removeTokens()
      return next({ name: 'loginView', query: { redirect: to.fullPath } })
    }
  }

  // If already authenticated and trying to access the login page, redirect to the board view
  if (to.name === 'loginView' && isAuthenticated) {
    return next({ name: 'boardView' })
  }

  // Check board access if boardId is present in the route
  if (to.params.boardId) {
    try {
      const { hasAccess, notFound } = await checkBoardAccess(to.params.boardId)
      if (notFound) {
        console.log('Board not found, redirecting to NotFound page')
        return next({ name: 'notFound' })
      } else if (!hasAccess) {
        console.log('Access denied, redirecting to AccessDenied page')
        return next({ name: 'accessDenied' })
      }
    } catch (error) {
      console.error('Error checking board access:', error)
      return next({ name: 'notFound' })
    }
  }

  next()
})

export default router
