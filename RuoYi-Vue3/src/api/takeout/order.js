import request from '@/utils/request'

// Query order list
export function listOrder(query) {
  return request({ url: '/takeout/order/list', method: 'get', params: query })
}

// Get order detail by id
export function getOrder(id) {
  return request({ url: `/takeout/order/${id}`, method: 'get' })
}

// Get status dict
export function getOrderStatusDict() {
  return request({ url: '/takeout/order/dict/status', method: 'get' })
}

// Check orderNo unique
export function checkOrderNoUnique(query) {
  return request({ url: '/takeout/order/checkOrderNoUnique', method: 'get', params: query })
}

// Add new order
export function addOrder(data) {
  return request({ url: '/takeout/order', method: 'post', data })
}

// Update order
export function updateOrder(data) {
  return request({ url: '/takeout/order', method: 'put', data })
}

// Delete order(s)
export function delOrder(ids) {
  return request({ url: `/takeout/order/${ids}`, method: 'delete' })
}

// Change order status
export function changeOrderStatus(id, status) {
  return request({ url: `/takeout/order/changeStatus/${id}/${status}`, method: 'put' })
}

// Cancel order
export function cancelOrder(id, reason) {
  return request({ url: `/takeout/order/cancel/${id}`, method: 'put', params: { reason } })
}

// Export orders
export function exportOrder(query) {
  return request({ url: '/takeout/order/export', method: 'get', params: query, responseType: 'blob' })
}

// 列出某商家下在售菜品（订单弹窗选用）
export function listDishByMerchant(merchantId) {
  return request({ url: `/takeout/dish/listDishByMerchant/${merchantId}`, method: 'get' })
}
