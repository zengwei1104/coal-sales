import request from '../utils/request'

// ===== 统计报表接口（后端前缀 /api/reports） =====

// 销售趋势（按结算审核日聚合）
export function getSalesTrend(params) {
  return request.get('/reports/sales-trend', { params })
}

// 煤炭销量排行（按煤种商品聚合）
export function getCoalSalesRank(params) {
  return request.get('/reports/coal-rank', { params })
}

// 客户销售额排行
export function getCustomerRank(params) {
  return request.get('/reports/customer-rank', { params })
}

// 回款率统计
export function getPaymentRate(params) {
  return request.get('/reports/payment-rate', { params })
}

// 库存预警
export function getStockWarning() {
  return request.get('/reports/stock-warning')
}
