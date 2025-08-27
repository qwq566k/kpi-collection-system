import api from './index'

// 成果审核-条件查询
export const queryRecords = (data) => {
  return api.post('/admin/queryRecords', data)
}

// 成果审核-通过记录
export const approveRecord = (id) => {
  return api.put(`/approveRecord?id=${id}`)
}

// 成果审核-退回记录
export const rejectRecord = (data) => {
  return api.put('/rejectRecord', data)
}

// 成果统计-提交量统计
export const getSubmissionStats = () => {
  return api.get('/getSubmissionStats')
}

// 成果统计-考核领域统计
export const getAreaStats = () => {
  return api.get('/getAreaStats')
}

// 成果统计-负责人统计
export const getSubmitterStats = () => {
  return api.get('/getSubmitterStats')
}

// 成果导出-条件查询
export const exportQuery = (data) => {
  return api.post('/exportQuery', data)
}

// 成果导出-导出Excel
export const exportExcel = (data) => {
  return api.post('/exportExcel', data, {
    responseType: 'blob' // 设置响应类型为blob以处理文件流
  })
}

// 用户管理-条件查询
export const queryUsers = (data) => {
  return api.post('/queryUsers', data)
}

// 用户管理-添加用户
export const addUser = (data) => {
  return api.post('/addUser', data)
}

// 用户管理-批量导入
export const importUsers = (data) => {
  return api.post('/importUsers', data)
}

// 用户管理-编辑用户
export const updateUser = (data) => {
  return api.put('/updateUser', data)
}

// 用户管理-删除用户
export const deleteUser = (ids) => {
  return api.delete(`/deleteUser?ids=${ids}`)
}


