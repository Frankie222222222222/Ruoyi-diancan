import request from '@/utils/request'

/**
 * 支付流水列表
 */
export function listPayment(query) {
  return request({ url: '/takeout/payment/list', method: 'get', params: query })
}

/**
 * 发起支付(Mock 直接成功,真实渠道返回 prepayInfo 给前端)
 * @param {number|string} paymentId
 */
export function createPayment(paymentId) {
  return request({ url: `/takeout/payment/create/${paymentId}`, method: 'post' })
}

/**
 * 主动查账
 * @param {number|string} paymentId
 */
export function queryPayment(paymentId) {
  return request({ url: `/takeout/payment/query/${paymentId}`, method: 'get' })
}

/**
 * 退款
 */
export function refundPayment(paymentId, reason) {
  return request({ url: `/takeout/payment/refund/${paymentId}`, method: 'put', params: { reason } })
}
