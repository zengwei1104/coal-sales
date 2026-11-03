import request from '../utils/request'

// 分页查询回款单（customerId 筛选）
export function getPaymentPage(params) {
  return request.get('/payments', { params })
}

// 登记回款 POST /payments
export function createPayment(data) {
  return request.post('/payments', data)
}

// 核销 POST /payments/{id}/write-off
export function writeOffPayment(id, data) {
  return request.post(`/payments/${id}/write-off`, data)
}

// 客户应收账款 GET /customers/{id}/receivable
export function getCustomerReceivable(id) {
  return request.get(`/customers/${id}/receivable`)
}
