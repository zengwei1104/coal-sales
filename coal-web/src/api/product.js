import request from '../utils/request'

// 分页查询煤种商品
export function getProductPage(params) {
  return request.get('/products', { params })
}

// 查询单个商品
export function getProduct(id) {
  return request.get(`/products/${id}`)
}

// 新增商品
export function addProduct(data) {
  return request.post('/products', data)
}

// 修改商品
export function updateProduct(id, data) {
  return request.put(`/products/${id}`, data)
}

// 删除商品
export function deleteProduct(id) {
  return request.delete(`/products/${id}`)
}
