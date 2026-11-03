import request from '../utils/request'

// ===== 工作台聚合接口：复用各模块已有分页/列表接口，前端聚合展示 =====

// 客户总数（分页 size=1 取 total）
export function getCustomerTotal() {
  return request.get('/customers', { params: { current: 1, size: 1 } })
}

// 合同分页（工作台取总数）
export function getContractPage(params) {
  return request.get('/contracts', { params })
}

// 订单分页（工作台按状态取分布）
export function getOrderPage(params) {
  return request.get('/orders', { params })
}

// 库存列表（含预警标记）
export function getStocks() {
  return request.get('/stocks')
}

// 磅单分页（工作台取总数）
export function getWeighTicketPage(params) {
  return request.get('/weigh-tickets', { params })
}

// 结算单分页（工作台取总数）
export function getSettlementPage(params) {
  return request.get('/settlements', { params })
}

// 回款分页（工作台取总数）
export function getPaymentPage(params) {
  return request.get('/payments', { params })
}
