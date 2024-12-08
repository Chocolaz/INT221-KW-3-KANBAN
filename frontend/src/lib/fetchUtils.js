import { useRouter } from 'vue-router'

const baseUrl3 = import.meta.env.VITE_API_URL3

const router = useRouter()

const handleResponse = async (response) => {
  const contentType = response.headers.get('Content-Type') || ''
  let errorBody = ''

  try {
    if (contentType.includes('application/json')) {
      errorBody = await response.json()
    } else {
      errorBody = await response.text()
    }
  } catch (e) {
    console.warn('Error reading response body:', e.message)
  }

  if (!response.ok) {
    console.error(`HTTP error! Status: ${response.status}`, errorBody)

    if (response.status === 401) {
      localStorage.removeItem('isAuthenticated')
      localStorage.removeItem('token')
      router.push('/login')
    }

    throw new Error(
      `HTTP error! Status: ${response.status}, Body: ${
        typeof errorBody === 'string' ? errorBody : JSON.stringify(errorBody)
      }`
    )
  }

  return {
    success: true,
    data: typeof errorBody === 'string' ? errorBody : errorBody,
    statusCode: response.status
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

  if (!response.ok) {
    let errorBody = {}
    try {
      errorBody = await response.json()
    } catch (error) {
      errorBody = { message: 'No JSON response body' }
    }
    console.error('Error response body:', errorBody)

    throw new Error(errorBody.error || 'HTTP error! Status: ' + response.status)
  }

  if (options.rawResponse) {
    return response
  }

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

const updateCollabAccess = async (boardId, collabId, accessRight) => {
  try {
    validateBoardId(boardId)
    if (!collabId) throw new Error('Collaborator ID is required')

    const fullUrl = `${baseUrl3}/boards/${boardId}/collabs/${collabId}`
    const requestBody = { access_right: accessRight }
    console.log(`Updating access for ${collabId} to ${accessRight}`)

    const responseData = await fetchWithAuth(fullUrl, {
      method: 'PATCH',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(requestBody)
    })

    console.log(`Update access status code:`, responseData.statusCode)
    return responseData
  } catch (error) {
    console.error('Error updating collaborator access:', error.message)
    throw error
  }
}

const removeCollab = async (boardId, collabId) => {
  try {
    validateBoardId(boardId)
    if (!collabId) throw new Error('Collaborator ID is required')

    const fullUrl = `${baseUrl3}/boards/${boardId}/collabs/${collabId}`
    console.log(`Removing collaborator with ID ${collabId}`)

    const responseData = await fetchWithAuth(fullUrl, {
      method: 'DELETE',
      headers: { 'Content-Type': 'application/json' }
    })

    console.log(`Remove collaborator status code:`, responseData.statusCode)
    return responseData
  } catch (error) {
    console.error('Error removing collaborator:', error.message)
    throw error
  }
}

const fetchAttachments = async (attachments) => {
  try {
    if (Array.isArray(attachments)) {
      return await Promise.all(
        attachments.map(async (attachment) => {
          const fullUrl = `${baseUrl3}/attachments/${attachment.attachmentId}`
          const response = await fetchWithAuth(fullUrl, { rawResponse: true })
          const blob = await response.blob()
          const blobUrl = URL.createObjectURL(blob)
          return { ...attachment, blobUrl }
        })
      )
    }

    const fullUrl = `${baseUrl3}/attachments/${attachments.attachmentId}`
    const response = await fetchWithAuth(fullUrl, { rawResponse: true })
    const blob = await response.blob()
    const blobUrl = URL.createObjectURL(blob)
    return { ...attachments, blobUrl }
  } catch (error) {
    console.error('Error fetching attachments:', error)
    throw error
  }
}

const postTaskWithAttachment = async (boardId, newTaskDTO, files) => {
  try {
    validateBoardId(boardId)
    const fullUrl = `${baseUrl3}/boards/${boardId}/tasks`

    const formData = new FormData()

    formData.append(
      'newTaskDTO',
      new Blob([JSON.stringify(newTaskDTO)], { type: 'application/json' })
    )

    if (files && files.length > 0) {
      files.forEach((file) => {
        formData.append(`addAttachments`, file)
      })
    }

    const responseData = await fetchWithAuth(fullUrl, {
      method: 'POST',
      body: formData
    })

    console.log(
      'Post task with attachment status code:',
      responseData.statusCode
    )
    return responseData
  } catch (error) {
    console.error('Error posting task with attachment:', error)
    throw error
  }
}

const updateTaskWithAttachment = async (
  boardId,
  taskId,
  updateTaskDTO,
  deleteAttachments = [],
  files = []
) => {
  try {
    validateBoardId(boardId)
    if (!taskId) {
      throw new Error('Task ID is required for updating a task.')
    }

    let fullUrl = `${baseUrl3}/boards/${boardId}/tasks/${taskId}`
    if (deleteAttachments.length > 0) {
      const deleteParams = deleteAttachments
        .map((attachmentId) => `deleteAttachments=${attachmentId}`)
        .join('&')
      fullUrl += `?${deleteParams}`
    }

    const formData = new FormData()

    formData.append(
      'updateTaskDTO',
      new Blob([JSON.stringify(updateTaskDTO)], { type: 'application/json' })
    )

    if (files && files.length > 0) {
      files.forEach((file) => {
        formData.append('addAttachments', file)
      })
    }

    console.log('Final URL:', fullUrl)

    const responseData = await fetchWithAuth(fullUrl, {
      method: 'PUT',
      body: formData
    })

    console.log(
      'Update task with attachment status code:',
      responseData.statusCode
    )
    return responseData
  } catch (error) {
    console.error('Error updating task with attachment:', error)
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
  addCollab,
  updateCollabAccess,
  removeCollab,
  fetchAttachments,
  postTaskWithAttachment,
  updateTaskWithAttachment
}
