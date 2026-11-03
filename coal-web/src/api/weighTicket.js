import request from '../utils/request'

// 分页查询磅单（status/orderId/plateNo 筛选）
export function getWeighTicketPage(params) {
  return request.get('/weigh-tickets', { params })
}

// 开单（录皮重）POST /weigh-tickets
export function addWeighTicket(data) {
  return request.post('/weigh-tickets', data)
}

// 录皮重 PUT /weigh-tickets/{id}/tare
export function addTareWeight(id, data) {
  return request.put(`/weigh-tickets/${id}/tare`, data)
}

// 录毛重 PUT /weigh-tickets/{id}/gross
export function addGrossWeight(id, data) {
  return request.put(`/weigh-tickets/${id}/gross`, data)
}

// 作废 POST /weigh-tickets/{id}/cancel
export function cancelWeighTicket(id, data) {
  return request.post(`/weigh-tickets/${id}/cancel`, data)
}

// 可发运磅单列表
export function getAvailableWeighTickets() {
  return request.get('/weigh-tickets/available')
}

// 某合同下已完成且未结算的磅单（生成结算单用）
export function getSettleableWeighTickets(contractId) {
  return request.get('/weigh-tickets/settleable', { params: { contractId } })
}
