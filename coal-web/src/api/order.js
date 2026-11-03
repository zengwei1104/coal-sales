import request from '../utils/request'

// 分页查询订单（支持 status/customerId/contractId/keyword）
export function getOrderPage(params) {
  return request.get('/orders', { params })
}

// 查询单个订单
export function getOrder(id) {
  return request.get(`/orders/${id}`)
}

// 新增订单
export function addOrder(data) {
  return request.post('/orders', data)
}

// 修改订单
export function updateOrder(id, data) {
  return request.put(`/orders/${id}`, data)
}

// 删除订单
export function deleteOrder(id) {
  return request.delete(`/orders/${id}`)
}

// 取消订单
export function cancelOrder(id) {
  return request.post(`/orders/${id}/cancel`)
}

// 可发运订单列表（磅单用）
export function getAvailableOrders() {
  return request.get('/orders/available')
}
