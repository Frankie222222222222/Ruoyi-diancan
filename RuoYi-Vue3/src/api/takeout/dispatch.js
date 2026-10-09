import request from '@/utils/request'

// Query dispatch list
export function listDispatch(query) {
  return request({ url: '/takeout/dispatch/list', method: 'get', params: query })
}

// Get dispatch detail
export function getDispatch(dispatchId) {
  return request({ url: `/takeout/dispatch/${dispatchId}`, method: 'get' })
}

// Create dispatch (admin)
export function createDispatch(data) {
  return request({ url: '/takeout/dispatch/create', method: 'post', data })
}

// Rider claim order
export function claimOrder(orderId, riderId) {
  return request({ url: `/takeout/dispatch/claim/${orderId}/${riderId}`, method: 'post' })
}

// Rider accept dispatch
export function acceptDispatch(dispatchId, riderId) {
  return request({ url: `/takeout/dispatch/accept/${dispatchId}/${riderId}`, method: 'put' })
}

// Rider pickup
export function pickupDispatch(dispatchId) {
  return request({ url: `/takeout/dispatch/pickup/${dispatchId}`, method: 'put' })
}

// Complete delivery
export function completeDispatch(dispatchId) {
  return request({ url: `/takeout/dispatch/complete/${dispatchId}`, method: 'put' })
}

// Cancel dispatch
export function cancelDispatch(dispatchId, reason) {
    return request({ url: `/takeout/dispatch/cancel/${dispatchId}`, method: 'put', params: { reason } })
}

// Reassign dispatch to new rider
export function reassignDispatch(dispatchId, newRiderId, reason) {
    return request({ url: `/takeout/dispatch/reassign/${dispatchId}/${newRiderId}`, method: 'put', params: { reason } })
}

// Get active dispatch by order
export function getActiveDispatchByOrder(orderId) {
  return request({ url: `/takeout/dispatch/active/${orderId}`, method: 'get' })
}

// Get dispatches by rider
export function listDispatchByRider(riderId) {
  return request({ url: `/takeout/dispatch/rider/${riderId}`, method: 'get' })
}

// Delete dispatches
export function delDispatch(ids) {
  return request({ url: `/takeout/dispatch/${ids}`, method: 'delete' })
}
