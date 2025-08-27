import api from './index'

// 获取考核领域
export const getAllField = () => {
  return api.get('/getAllField')
}

// 获取关键指标(根据考核领域)
export const getKeyIndicators = (fieldId) => {
  return api.get(`/getKeyIndicators?fieldId=${fieldId}`)
}

// 获取评价标准(根据关键指标)
export const getStandards = (indicatorId) => {
  return api.get(`/getStandards?indicatorId=${indicatorId}`)
}

// 上传文件
export const uploadFile = (data) => {
  return api.post('/uploadFile', data, {
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}


