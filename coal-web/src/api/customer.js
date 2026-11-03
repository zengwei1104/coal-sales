import request from '../utils/request'

// 分页查询客户列表
export function getCustomerPage(params) {
  return request.get('/customers', { params })
}

// 查询单个客户（编辑回显）
export function getCustomer(id) {
  return request.get(`/customers/${id}`)
}

// 新增客户
export function addCustomer(data) {
  return request.post('/customers', data)
}

// 修改客户
export function updateCustomer(id, data) {
  return request.put(`/customers/${id}`, data)
}

// 删除客户
export function deleteCustomer(id) {
  return request.delete(`/customers/${id}`)
}

// 查询客户应收账款
export function getReceivable(id) {
  return request.get(`/customers/${id}/receivable`)
}
