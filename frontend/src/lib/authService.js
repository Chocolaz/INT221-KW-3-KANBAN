import jwtDecode from 'vue-jwt-decode'
import fetchUtils from '@/lib/fetchUtils'

const baseUrl = import.meta.env.VITE_API_URL2 // For token requests
const baseUrl3 = import.meta.env.VITE_API_URL3 // For board requests

// Store tokens
export function storeTokens(accessToken, refreshToken) {
  localStorage.setItem('access_token', accessToken)
  localStorage.setItem('refresh_token', refreshToken)
}

// Get tokens
export function getAccessToken() {
  return localStorage.getItem('access_token')
}

export function getRefreshToken() {
  return localStorage.getItem('refresh_token')
}

// Remove tokens
export function removeTokens() {
  localStorage.removeItem('access_token')
  localStorage.removeItem('refresh_token')
  localStorage.removeItem('username')
}

// Refresh access token
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

export const isTokenValid = (token, tokenType = 'access') => {
  if (!token) return false

  try {
    const decodedToken = jwtDecode.decode(token)
    const currentTime = Math.floor(Date.now() / 1000)
    const remainingTime = decodedToken.exp - currentTime

    if (remainingTime > 0) {
      console.log(`${tokenType} token will expire in ${remainingTime} seconds`)
      return true
    } else {
      console.log(`${tokenType} token has expired`)
      return false
    }
  } catch (error) {
    console.error(`Error decoding ${tokenType} token:`, error)
    return false
  }
}

// Calculate token expiration time in seconds
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
    console.error('Error decoding token:', error)
    return null
  }
}

export const checkTokenValidity = () => {
  const accessToken = getAccessToken()
  const refreshToken = getRefreshToken()

  const isAccessTokenValid = isTokenValid(accessToken, 'access')
  const accessTokenExpiration = accessToken
    ? getTokenExpirationTime(accessToken)
    : null

  // Validate the refresh token and log expiration time
  const isRefreshTokenValid = isTokenValid(refreshToken, 'refresh')
  const refreshTokenExpiration = refreshToken
    ? getTokenExpirationTime(refreshToken)
    : null

  return {
    isAccessTokenValid,
    isRefreshTokenValid,
    accessTokenExpiration,
    refreshTokenExpiration
  }
}

// Check board access
export async function checkBoardAccess(boardId) {
  try {
    const response = await fetchUtils.getBoards(boardId)

    if (response.statusCode === 403) {
      console.log(response.statusCode)
      return { hasAccess: false, notFound: false }
    }
    if (response.statusCode === 404) {
      console.log(response.statusCode)
      return { hasAccess: false, notFound: true }
    }

    const boardData = response.data
    const currentUser = localStorage.getItem('username')
    const boardOwner = boardData.owner.username || boardData.owner.name

    // Allow access to public boards even if the user is not authenticated
    if (boardData.visibility === 'public') {
      return { hasAccess: true, notFound: false }
    }

    // If the board is not public, check if the current user is the owner
    if (boardOwner === currentUser) {
      return { hasAccess: true, notFound: false }
    }

    // Fetch collaborators and check if the current user has READ or WRITE access
    const collaborators = await fetchUtils.getCollab(boardId)
    const hasAccess = collaborators.some(
      (collab) =>
        collab.name === currentUser &&
        (collab.access_right === 'READ' || collab.access_right === 'WRITE')
    )

    if (hasAccess) {
      console.log('Collaborator access granted:', hasAccess)
      return { hasAccess: true, notFound: false }
    }

    console.log('Access denied to user:', currentUser)
    return { hasAccess: false, notFound: false }
  } catch (error) {
    console.error('Error fetching board data:', error)
    return { hasAccess: false, notFound: error.message.includes('404') }
  }
}
