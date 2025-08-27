import api from './index'

// 用户登录
export const login = (data) => {
  return api.post('/user/login', data)
}

// 用户登出
export const logout = () => {
  return api.post('/user/logout')
}

// 修改密码
export const updatePassword = (data) => {
  return api.put('/user/updatePassword', data)
}

// 提交记录
export const submitRecord = (data) => {
  return api.post('/submitRecord', data)
}

// 保存草稿
export const saveDraft = (data) => {
  return api.post('/saveDraft', data)
}

// 修改记录
export const updateRecord = (data) => {
  return api.put('/updateRecord', data)
}

// 查看记录详情
export const getRecordDetail = (id) => {
  return api.get(`/getRecordDetail?id=${id}`)
}

// 查询全部自己记录
export const getAllRecords = (params) => {
  return api.get('/getAllRecords', { params })
}


