export const statusStyle = (status) => {
    const statusUpperCase = status.toUpperCase()
    switch (statusUpperCase) {
      case 'TO DO':
        return { background: 'linear-gradient(to right, #FF9A9E, #F67C5E)' }
      case 'DOING':
        return { background: 'linear-gradient(to right, #FFE066, #F6E05E)' }
      case 'DONE':
        return { background: 'linear-gradient(to right, #AAF6BE, #68D391)' }
      case 'NO STATUS':
        return {
          backgroundColor: 'rgba(245, 245, 245, 0.8)',
          color: '#888',
          fontStyle: 'italic'
        }
      case 'WAITING':
        return { background: 'linear-gradient(to right, #D9A3FF, #B473FF)' }
      case 'IN PROGRESS':
        return { background: 'linear-gradient(to right, #FFB347, #FFA733)' }
      case 'REVIEWING':
        return { background: 'linear-gradient(to right, #FFB6C1, #FF69B4)' }
      case 'TESTING':
        return { background: 'linear-gradient(to right, #ADD8E6, #87CEEB)' }
      default:
        return { background: 'linear-gradient(to right, #A0CED9, #6CBEE6)' }
    }
  }
  