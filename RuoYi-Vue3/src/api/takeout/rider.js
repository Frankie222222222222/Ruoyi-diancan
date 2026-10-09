import request from '@/utils/request'

// Query rider list
export function listRider(query) {
  return request({ url: '/takeout/rider/list', method: 'get', params: query })
}

// Get rider detail
export function getRider(riderId) {
  return request({ url: `/takeout/rider/${riderId}`, method: 'get' })
}

// Add rider
export function addRider(data) {
  return request({ url: '/takeout/rider', method: 'post', data })
}

// Update rider
export function updateRider(data) {
  return request({ url: '/takeout/rider', method: 'put', data })
}

// Delete riders
export function delRider(ids) {
  return request({ url: `/takeout/rider/${ids}`, method: 'delete' })
}

// Change rider status
export function changeRiderStatus(riderId, status) {
  return request({ url: `/takeout/rider/changeStatus/${riderId}/${status}`, method: 'put' })
}

// Get available riders
export function getAvailableRiders(city) {
  return request({ url: '/takeout/rider/available', method: 'get', params: { city } })
}
