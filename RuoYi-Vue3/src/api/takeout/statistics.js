import request from '@/utils/request'

// Dashboard data
export function getDashboard() {
  return request({ url: '/takeout/statistics/dashboard', method: 'get' })
}

// Order trend (last N days, default 7)
export function getOrderTrend(days) {
  return request({ url: '/takeout/statistics/trend', method: 'get', params: { days } })
}

// Top merchants by revenue
export function getTopMerchants(limit) {
  return request({ url: '/takeout/statistics/topMerchants', method: 'get', params: { limit } })
}

// Top dishes by sales
export function getTopDishes(limit) {
  return request({ url: '/takeout/statistics/topDishes', method: 'get', params: { limit } })
}

// Top riders by delivery count
export function getTopRiders(limit) {
  return request({ url: '/takeout/statistics/topRiders', method: 'get', params: { limit } })
}

// Health check
export function pingStatistics() {
  return request({ url: '/takeout/statistics/ping', method: 'get' })
}
