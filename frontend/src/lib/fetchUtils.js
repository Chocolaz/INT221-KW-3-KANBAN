import { useRouter } from 'vue-router'

const baseUrl = import.meta.env.VITE_API_URL
const baseUrl3 = import.meta.env.VITE_API_URL3

const router = useRouter()

const handleResponse = async (response) => {
  if (!response.ok) {
    if (response.status === 401) {
      // Reset authentication state and redirect to login page
      localStorage.removeItem('isAuthenticated')
      localStorage.removeItem('token')
      router.push('/login')
    }
    throw new Error(`HTTP error! Status: ${response.status}`)
  }
  const responseData = await response.json()
  return { success: true, data: responseData, statusCode: response.status }
}

const getToken = () => {
  const token = localStorage.getItem('token')
  if (!token) {
    throw new Error('No authentication token found')
  }
  return token
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

const fetchData = async (url, boardId, taskId = null) => {
  try {
    const token = getToken()
    validateBoardId(boardId)
    const fullUrl = buildUrl(url, boardId, taskId)
    const response = await fetch(fullUrl, {
      headers: {
        Authorization: `Bearer ${token}`
      }
    })
    const responseData = await handleResponse(response)
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
    const token = getToken()
    validateBoardId(boardId)
    const response = await fetch(buildUrl(url, boardId), {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        Authorization: `Bearer ${token}`
      },
      body: JSON.stringify(data)
    })
    const responseData = await handleResponse(response)
    console.log('Data posted successfully. Status code:', response.status)
    return responseData
  } catch (error) {
    console.error('Error posting data:', error)
    throw error
  }
}

const putData = async (url, boardId, data) => {
  try {
    const token = getToken()
    validateBoardId(boardId) 
    const fullUrl = buildUrl(url, boardId)
    const response = await fetch(fullUrl, {
      method: 'PUT',
      headers: {
        'Content-Type': 'application/json',
        Authorization: `Bearer ${token}`
      },
      body: JSON.stringify(data)
    })
    return await handleResponse(response)
  } catch (error) {
    console.error('Error updating data:', error)
    throw error
  }
}

const handleDeleteResponse = async (response) => {
  try {
    const text = await response.text()
    if (text.trim() === '') {
      // No content
      return { success: true, data: null, statusCode: response.status }
    }

    // Try to parse the response as JSON
    try {
      const responseData = JSON.parse(text)
      return { success: true, data: responseData, statusCode: response.status }
    } catch (parseError) {
      // If JSON parsing fails, return the raw text as data
      console.warn('Warning: Response is not valid JSON. Raw text:', text)
      return { success: true, data: text, statusCode: response.status }
    }
  } catch (error) {
    console.error('Error handling delete response:', error)
    throw new Error(`Error handling delete response: ${error.message}`)
  }
}

const deleteAndTransferData = async (url, newId, boardId) => {
  try {
    const token = getToken()
    validateBoardId(boardId)
    const fullUrl = `${baseUrl3}/boards/${boardId}/${url}/${newId}`
    const response = await fetch(fullUrl, {
      method: 'DELETE',
      headers: {
        Authorization: `Bearer ${token}`
      }
    })
    return await handleDeleteResponse(response)
  } catch (error) {
    console.error('Error deleting and transferring data:', error.message)
    throw error
  }
}
const deleteData = async (url, boardId) => {
  try {
    const token = getToken()
    validateBoardId(boardId)
    const response = await fetch(buildUrl(url, boardId), {
      method: 'DELETE',
      headers: {
        Authorization: `Bearer ${token}`
      }
    })
    console.log('Raw response:', response)
    return await handleDeleteResponse(response)
  } catch (error) {
    console.error('Error deleting data:', error.message)
    throw error
  }
}

const getBoards = async (boardId = null) => {
  try {
    const token = getToken()
    const url = boardId ? `${baseUrl3}/boards/${boardId}` : `${baseUrl3}/boards`
    const response = await fetch(url, {
      headers: {
        Authorization: `Bearer ${token}`
      }
    })
    const responseData = await handleResponse(response)
    console.log(
      `Boards retrieved successfully. Status code: ${response.status}`
    )
    return boardId ? responseData : responseData.data
  } catch (error) {
    console.error('Error retrieving boards:', error)
    throw error
  }
}

const addBoard = async (boardData) => {
  try {
    const token = getToken()
    const response = await fetch(`${baseUrl3}/boards`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        Authorization: `Bearer ${token}`
      },
      body: JSON.stringify(boardData)
    })
    const responseData = await handleResponse(response)
    console.log('Board added successfully. Status code:', response.status)
    return responseData
  } catch (error) {
    console.error('Error adding board:', error)
    throw error
  }
}

const getAllBoards = async () => {
  try {
    const token = getToken()
    const response = await fetch(`${baseUrl3}/boards`, {
      headers: {
        Authorization: `Bearer ${token}`
      }
    })
    const responseData = await handleResponse(response)
    console.log(
      'All boards retrieved successfully. Status code:',
      response.status
    )
    return responseData.data
  } catch (error) {
    console.error('Error retrieving all boards:', error)
    throw error
  }
}

export default {
  fetchData,
  postData,
  putData,
  deleteData,
  deleteAndTransferData,
  getBoards,
  addBoard,
  getAllBoards
}
