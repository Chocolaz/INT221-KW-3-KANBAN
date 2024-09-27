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

export function accessServiceWithToken(token) {
  return fetch(`${baseUrl3}/boards`, {
    method: 'GET',
    headers: {
      Authorization: `Bearer ${token}`
    }
  })
}

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
