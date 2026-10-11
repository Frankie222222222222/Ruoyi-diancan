import request from '@/utils/request'

// 后厨可见订单
export function listKitchenOrders() {
  return request({ url: '/takeout/kitchen/list', method: 'get' })
}

// 后厨接单
export function kitchenAccept(orderId, kitchenId) {
  return request({ url: `/takeout/kitchen/accept/${orderId}`, method: 'put', params: { kitchenId } })
}

// 后厨出餐
export function kitchenReady(orderId, kitchenId) {
  return request({ url: `/takeout/kitchen/ready/${orderId}`, method: 'put', params: { kitchenId } })
}

// 看板统计
export function kitchenDashboard() {
  return request({ url: '/takeout/kitchen/dashboard', method: 'get' })
}
