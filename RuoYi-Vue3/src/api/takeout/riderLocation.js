import request from '@/utils/request'

/**
 * 骑手上报一次位置(开发期可由后台管理员手动调用)
 * @param {Object} data { dispatchId, riderId, lng, lat, reportTime }
 */
export function reportRiderLocation(data) {
  return request({ url: '/takeout/riderLocation/report', method: 'post', data })
}

/**
 * 地图组件批量拉取所有 active 派单的骑手最新位置
 * 返回结构: [{ dispatchId, orderId, riderId, riderName, status, riderLng, riderLat, locationUpdateTime, orderNo, ... }]
 */
export function listActiveDispatchLocations() {
  return request({ url: '/takeout/riderLocation/active', method: 'get' })
}
