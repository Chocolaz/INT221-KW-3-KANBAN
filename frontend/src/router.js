import { createRouter, createWebHistory } from 'vue-router'
import TaskList from './v1/TaskList.vue'
import TestAttach from './v1/TestAttach.vue'
import StatusList from './v2/StatusList.vue'
import Login from './v3/Login.vue'
import BoardList from './v3/BoardList.vue'
import NotFound from './component/NotFound.vue'
import AccessDenied from './component/AccessDenied.vue'
import ManageCollab from './collab/ManageCollab.vue'

import {
  refreshAccessToken,
  removeTokens,
  getRefreshToken,
  checkBoardAccess,
  checkTokenValidity
} from './lib/authService'

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
    props: (route) => ({
      boardId: route.params.boardId,
      statusId: route.params.statusId
    })
  },
  {
    path: '/boards/:boardId/collab',
    name: 'manageCollab',
    component: ManageCollab,
    props: (route) => ({ boardId: route.params.boardId })
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
    path: '/',
    redirect: '/login'
  },
  {
    path: '/:pathMatch(.*)*',
    redirect: '/board'
  },
  {
    path: '/test',
    component: TestAttach
  }
]

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes
})

router.beforeEach(async (to, from, next) => {
  const { isAccessTokenValid, isRefreshTokenValid } = checkTokenValidity()

  let isAuthenticated = isAccessTokenValid

  if (to.params.boardId) {
    try {
      const { hasAccess, notFound, isPublic } = await checkBoardAccess(
        to.params.boardId
      )

      if (notFound) {
        return next({ name: 'notFound' })
      }

      if (isPublic) {
        return next()
      }

      if (!hasAccess) {
        return next({ name: 'accessDenied' })
      }
    } catch (error) {
      return next({ name: 'notFound' })
    }
  }

  if (to.meta.requiresAuth && !isAuthenticated) {
    const refreshToken = getRefreshToken()

    if (refreshToken && isRefreshTokenValid) {
      try {
        await refreshAccessToken()
        isAuthenticated = true
      } catch (error) {
        removeTokens()
        return next({ name: 'loginView', query: { redirect: to.fullPath } })
      }
    } else {
      removeTokens()
      return next({ name: 'loginView', query: { redirect: to.fullPath } })
    }
  }

  if (to.name === 'loginView' && isAuthenticated) {
    return next({ name: 'boardView' })
  }

  next()
})

export default router
