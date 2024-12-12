import jwtDecode from 'vue-jwt-decode'
import fetchUtils from '@/lib/fetchUtils'

const baseUrl = import.meta.env.VITE_API_URL2
const baseUrl3 = import.meta.env.VITE_API_URL3

export function storeTokens(accessToken, refreshToken) {
  localStorage.setItem('access_token', accessToken)
  localStorage.setItem('refresh_token', refreshToken)
}

export function getAccessToken() {
  return localStorage.getItem('access_token')
}

export function getRefreshToken() {
  return localStorage.getItem('refresh_token')
}

export function removeTokens() {
  localStorage.removeItem('access_token')
  localStorage.removeItem('refresh_token')
  localStorage.removeItem('username')
}

// Fetches a new access token using the refresh token.
export async function refreshAccessToken() {
  const refreshToken = getRefreshToken()
  if (!refreshToken) {
    removeTokens()
    throw new Error('No refresh token found')
  }

  const response = await fetch(`${baseUrl}/token`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      Authorization: `Bearer ${refreshToken}`
    },
    body: JSON.stringify({})
  })

  if (response.status === 200) {
    const data = await response.json()
    storeTokens(data.access_token, refreshToken)
    return data.access_token
  } else if (response.status === 401) {
    removeTokens()
    throw new Error('Unauthorized: Redirecting to login page')
  } else {
    throw new Error('There is a problem. Please try again later.')
  }
}

// Checks if a token is valid (not expired) using jwtDecode.
export const isTokenValid = (token, tokenType = 'access') => {
  if (!token) return false

  try {
    const decodedToken = jwtDecode.decode(token)
    const currentTime = Math.floor(Date.now() / 1000)
    const remainingTime = decodedToken.exp - currentTime

    if (remainingTime > 0) {
      return true
    } else {
      return false
    }
  } catch (error) {
    return false
  }
}

// Returns the time remaining until a token expires (in seconds)
export const getTokenExpirationTime = (token) => {
  try {
    const decodedToken = jwtDecode.decode(token)
    const currentTime = Math.floor(Date.now() / 1000)
    const remainingTime = decodedToken.exp - currentTime

    if (remainingTime > 0) {
      return remainingTime
    } else {
      return 0
    }
  } catch (error) {
    return null
  }
}

// Checks the validity of both access and refresh tokens, their expiration times.
export const checkTokenValidity = () => {
  const accessToken = getAccessToken()
  const refreshToken = getRefreshToken()

  const isAccessTokenValid = isTokenValid(accessToken, 'access')
  const accessTokenExpiration = accessToken
    ? getTokenExpirationTime(accessToken)
    : null

  const isRefreshTokenValid = isTokenValid(refreshToken, 'refresh')
  const refreshTokenExpiration = refreshToken
    ? getTokenExpirationTime(refreshToken)
    : null

  // Returns an object with relevant information.
  return {
    isAccessTokenValid,
    isRefreshTokenValid,
    accessTokenExpiration,
    refreshTokenExpiration
  }
}

// Checks if the current user has access to a specific board.
export async function checkBoardAccess(boardId) {
  try {
    const response = await fetchUtils.getBoards(boardId)

    // Handles various scenarios: success, not found, public boards, ownership, and collaboration.

    //Access denied
    if (response.statusCode === 403) {
      return { hasAccess: false, notFound: false, isPublic: false }
    }

    //Not found
    if (response.statusCode === 404) {
      return { hasAccess: false, notFound: true, isPublic: false }
    }

    //Public visibility
    const boardData = response.data
    if (boardData.visibility === 'public') {
      return { hasAccess: true, notFound: false, isPublic: true }
    }

    //Ownership
    const currentUser = localStorage.getItem('username')
    const boardOwner = boardData.owner.username || boardData.owner.name

    if (boardOwner === currentUser) {
      return { hasAccess: true, notFound: false, isPublic: false }
    }

    //Collaborators
    const collaborators = await fetchUtils.getCollab(boardId)
    const hasAccess = collaborators.some(
      (collab) =>
        collab.name === currentUser &&
        (collab.access_right === 'READ' || collab.access_right === 'WRITE')
    )

    if (hasAccess) {
      return { hasAccess: true, notFound: false, isPublic: false }
    }

    //Default
    return { hasAccess: false, notFound: false, isPublic: false }
  } catch (error) {
    return {
      hasAccess: false,
      notFound: error.message.includes('404'),
      isPublic: false
    }
  }
}
