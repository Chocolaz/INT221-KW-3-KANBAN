const baseUrl = 'http://localhost:8080/itb-kk/v1'

const fetchData = async (url) => {
  try {
    const response = await fetch(`${baseUrl}/${url}`)
    if (!response.ok) {
      throw new Error(`HTTP error! Status: ${response.status}`)
    }
    return await response.json()
  } catch (error) {
    console.error('Error fetching data:', error)
    throw error
  }
}

const postData = async (url, data) => {
  try {
    const response = await fetch(`${baseUrl}/${url}`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify(data)
    })
    if (!response.ok) {
      throw new Error(`HTTP error! Status: ${response.status}`)
    }
    const responseData = await response.json()
    if (response.status === 201) {
      console.log('Data posted successfully! Status code:', response.status)
      return { success: true, data: responseData, statusCode: response.status }
    } else {
      // handle other successful responses if needed
      return { success: true, data: responseData, statusCode: response.status }
    }
  } catch (error) {
    console.error('Error posting data:', error)
    throw error
  }
}

const putData = async (url, data) => {
  try {
    const response = await fetch(`${baseUrl}/${url}`, {
      method: 'PUT',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify(data)
    })
    if (!response.ok) {
      throw new Error(`HTTP error! Status: ${response.status}`)
    }
    return await response.json()
  } catch (error) {
    console.error('Error updating data:', error)
    throw error
  }
}

const deleteData = async (url) => {
  try {
    const response = await fetch(`${baseUrl}/${url}`, {
      method: 'DELETE'
    })
    if (!response.ok) {
      throw new Error(`HTTP error! Status: ${response.status}`)
    }
    const responseData = await response.json()
    // Assuming you want to handle 204 status code as successful
    if (response.status === 200) {
      console.log('Data deleted successfully! Status code:', response.status)
      return { success: true, data: responseData, statusCode: response.status }
    } else {
      // handle other successful responses if needed
      return { success: true, data: responseData, statusCode: response.status }
    }
  } catch (error) {
    console.error('Error deleting data:', error)
    throw error
  }
}

export default { fetchData, postData, putData, deleteData }
