import request from '../utils/request'

// 查询全部煤种当前库存（含预警标记）
export function getStocks() {
  return request.get('/stocks')
}

// 分页查询库存流水
export function getStockRecordPage(params) {
  return request.get('/stock-records', { params })
}

// 采购入库 POST /stock-records/inbound
export function inboundStock(data) {
  return request.post('/stock-records/inbound', data)
}

// 盘点调整 POST /stock-records/check
export function checkStock(data) {
  return request.post('/stock-records/check', data)
}
