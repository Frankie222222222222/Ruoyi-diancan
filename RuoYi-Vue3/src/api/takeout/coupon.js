import request from '@/utils/request'

// Query coupon list
export function listCoupon(query) {
  return request({ url: '/takeout/coupon/list', method: 'get', params: query })
}

// Get coupon detail
export function getCoupon(couponId) {
  return request({ url: `/takeout/coupon/${couponId}`, method: 'get' })
}

// Add coupon
export function addCoupon(data) {
  return request({ url: '/takeout/coupon', method: 'post', data })
}

// Update coupon
export function updateCoupon(data) {
  return request({ url: '/takeout/coupon', method: 'put', data })
}

// Delete coupons
export function delCoupon(ids) {
  return request({ url: `/takeout/coupon/${ids}`, method: 'delete' })
}

// User receive coupon
export function receiveCoupon(couponId, userId) {
  return request({ url: `/takeout/coupon/receive/${couponId}/${userId}`, method: 'post' })
}

// User's coupons
export function getUserCoupons(userId, status) {
  return request({ url: `/takeout/coupon/userCoupon/${userId}`, method: 'get', params: { status } })
}

// Find best coupon for an order
export function getBestCouponForOrder(userId, merchantId, orderAmount) {
  return request({ url: '/takeout/coupon/bestForOrder', method: 'get', params: { userId, merchantId, orderAmount } })
}

// Use coupon when placing order
export function useCoupon(userCouponId, orderId) {
  return request({ url: `/takeout/coupon/use/${userCouponId}/${orderId}`, method: 'put' })
}
