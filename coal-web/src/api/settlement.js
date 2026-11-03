import request from '../utils/request'

// 分页查询结算单（status/customerId 筛选）
export function getSettlementPage(params) {
  return request.get('/settlements', { params })
}

// 结算单详情（含明细）
export function getSettlementDetail(id) {
  return request.get(`/settlements/${id}`)
}

// 生成结算单（contractId + ticketIds）
export function createSettlement(data) {
  return request.post('/settlements', data)
}

// 审核结算单
export function auditSettlement(id, data) {
  return request.post(`/settlements/${id}/audit`, data)
}

// 作废结算单
export function cancelSettlement(id) {
  return request.post(`/settlements/${id}/cancel`)
}

// 某客户可核销结算单（含剩余未收金额）
export function getWriteoffableSettlements(customerId) {
  return request.get('/settlements/writeoffable', { params: { customerId } })
}
