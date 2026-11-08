import request from '../utils/request'

// 分页查询用户（仅管理员）
export function getUserPage(params) {
  return request.get('/users', { params })
}

// 新增用户
export function addUser(data) {
  return request.post('/users', data)
}

// 修改用户（不含登录名、密码）
export function updateUser(id, data) {
  return request.put(`/users/${id}`, data)
}

// 删除用户（逻辑删除，仅管理员）
export function deleteUser(id) {
  return request.delete(`/users/${id}`)
}

// 修改密码（后端限制：只能改自己的）
export function updateUserPassword(id, data) {
  return request.put(`/users/${id}/password`, data)
}
