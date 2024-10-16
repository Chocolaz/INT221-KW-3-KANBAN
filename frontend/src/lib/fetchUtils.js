import { useRouter } from 'vue-router'

const baseUrl = import.meta.env.VITE_API_URL
const baseUrl3 = import.meta.env.VITE_API_URL3

const router = useRouter()

const handleResponse = async (response) => {
  const errorBody = await response.text()

  if (!response.ok) {
    console.error(`HTTP error! Status: ${response.status}`, errorBody)

    if (response.status === 401) {
      localStorage.removeItem('isAuthenticated')
      localStorage.removeItem('token')
      router.push('/login')
    }
    throw new Error(`HTTP error! Status: ${response.status}`)
  }

  if (errorBody) {
    try {
      const responseData = JSON.parse(errorBody)
      return { success: true, data: responseData, statusCode: response.status }
    } catch (e) {
      console.warn('Response is not valid JSON. Raw text:', errorBody)
      return { success: true, data: errorBody, statusCode: response.status }
    }
  } else {
    return { success: true, data: {}, statusCode: response.status }
  }
}

const getToken = () => {
  return localStorage.getItem('access_token')
}

const validateBoardId = (boardId) => {
  if (!boardId) {
    throw new Error('Board ID is required')
  }
}

const buildUrl = (url, boardId, taskId = null) => {
  return taskId
    ? `${baseUrl3}/boards/${boardId}/${url}/${taskId}`
    : `${baseUrl3}/boards/${boardId}/${url}`
}

const fetchWithAuth = async (url, options = {}) => {
  const token = getToken()
  const headers = {
    ...options.headers
  }

  if (token) {
    headers.Authorization = `Bearer ${token}`
  }

  const response = await fetch(url, { ...options, headers })
  return handleResponse(response)
}

const fetchData = async (url, boardId, taskId = null) => {
  try {
    validateBoardId(boardId)
    const fullUrl = buildUrl(url, boardId, taskId)
    const responseData = await fetchWithAuth(fullUrl)
    if (taskId) {
      responseData.data.taskId = taskId
    }
    return responseData.data
  } catch (error) {
    console.error('Error fetching data:', error)
    throw error
  }
}

const postData = async (url, boardId, data) => {
  try {
    validateBoardId(boardId)
    const fullUrl = buildUrl(url, boardId)
    const responseData = await fetchWithAuth(fullUrl, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(data)
    })
    console.log('Post status code:', responseData.statusCode)
    return responseData
  } catch (error) {
    console.error('Error posting data:', error)
    throw error
  }
}

const putData = async (url, boardId, data) => {
  try {
    validateBoardId(boardId)
    const fullUrl = buildUrl(url, boardId)
    const responseData = await fetchWithAuth(fullUrl, {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(data)
    })
    console.log('Put status code:', responseData.statusCode)
    return responseData
  } catch (error) {
    console.error('Error updating data:', error)
    throw error
  }
}

const deleteData = async (url, boardId, newId = null) => {
  try {
    validateBoardId(boardId)
    const fullUrl = newId
      ? `${baseUrl3}/boards/${boardId}/${url}/${newId}`
      : buildUrl(url, boardId)
    const responseData = await fetchWithAuth(fullUrl, { method: 'DELETE' })
    console.log(
      `Delete${newId ? ' and transfer' : ''} status code:`,
      responseData.statusCode
    )
    return responseData
  } catch (error) {
    console.error(
      `Error ${newId ? 'deleting and transferring' : 'deleting'} data:`,
      error.message
    )
    throw error
  }
}

const getBoards = async (boardId = null) => {
  try {
    const url = boardId ? `${baseUrl3}/boards/${boardId}` : `${baseUrl3}/boards`
    const responseData = await fetchWithAuth(url)
    console.log('Boards fetched:', responseData)

    return boardId ? responseData : responseData.data || responseData
  } catch (error) {
    console.error('Error retrieving boards:', error)
    throw error
  }
}

const addBoard = async (boardData) => {
  try {
    const responseData = await fetchWithAuth(`${baseUrl3}/boards`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(boardData)
    })
    console.log('Add board status code:', responseData.statusCode)
    return responseData
  } catch (error) {
    console.error('Error adding board:', error)
    throw error
  }
}

const visibilityBoard = async (boardId, visibility) => {
  try {
    validateBoardId(boardId)
    const fullUrl = `${baseUrl3}/boards/${boardId}`
    const requestBody = { visibility }
    console.log('Request Body:', requestBody)
    const responseData = await fetchWithAuth(fullUrl, {
      method: 'PATCH',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(requestBody)
    })
    console.log('Visibility status code:', responseData.statusCode)
    return responseData
  } catch (error) {
    if (error.message.includes('403')) {
      alert('You do not have permission to change board visibility mode.')
    } else {
      alert('There is a problem. Please try again later.')
    }
    console.error('Error updating board visibility:', error)
    throw error
  }
}

const getCollab = async (boardId) => {
  try {
    validateBoardId(boardId)
    const fullUrl = `${baseUrl3}/boards/${boardId}/collabs`
    const responseData = await fetchWithAuth(fullUrl)
    console.log('Collab details fetched:', responseData.data)
    return responseData.data || responseData
  } catch (error) {
    console.error('Error fetching collaboration details:', error)
    throw error
  }
}

const addCollab = async (boardId, data) => {
  try {
    validateBoardId(boardId)
    const fullUrl = `${baseUrl3}/boards/${boardId}/collabs`
    const responseData = await fetchWithAuth(fullUrl, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(data)
    })
    console.log('Add collaboration status code:', responseData.statusCode)
    return responseData
  } catch (error) {
    console.error('Error adding collaboration:', error)
    throw error
  }
}

export default {
  fetchData,
  postData,
  putData,
  deleteData,
  getBoards,
  addBoard,
  getAllBoards: getBoards,
  visibilityBoard,
  getCollab,
  addCollab
}
