import request from '../utils/request'

// 分页查询字典项（可按 dictType 筛选）
export function getDictPage(params) {
  return request.get('/dicts', { params })
}

// 按类型取全部键值（下拉回显、唯一性校验用）
export function getDictByType(type) {
  return request.get(`/dicts/type/${type}`)
}

// 新增字典项
export function addDict(data) {
  return request.post('/dicts', data)
}

// 修改字典项
export function updateDict(id, data) {
  return request.put(`/dicts/${id}`, data)
}

// 删除字典项（逻辑删除）
export function deleteDict(id) {
  return request.delete(`/dicts/${id}`)
}
