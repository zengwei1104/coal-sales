import request from '../utils/request'

// 分页查询合同（支持 status、keyword 筛选）
export function getContractPage(params) {
  return request.get('/contracts', { params })
}

// 查询单个合同
export function getContract(id) {
  return request.get(`/contracts/${id}`)
}

// 新增合同
export function addContract(data) {
  return request.post('/contracts', data)
}

// 修改合同
export function updateContract(id, data) {
  return request.put(`/contracts/${id}`, data)
}

// 删除合同
export function deleteContract(id) {
  return request.delete(`/contracts/${id}`)
}

// 提交审核
export function submitContract(id) {
  return request.post(`/contracts/${id}/submit`)
}

// 审核合同（pass: true/false, opinion: 意见）
export function auditContract(id, data) {
  return request.post(`/contracts/${id}/audit`, data)
}
