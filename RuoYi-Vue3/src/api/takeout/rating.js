import request from '@/utils/request'

// Query rating list
export function listRating(query) {
  return request({ url: '/takeout/rating/list', method: 'get', params: query })
}

// Get rating detail
export function getRating(ratingId) {
  return request({ url: `/takeout/rating/${ratingId}`, method: 'get' })
}

// Get rating by order
export function getRatingByOrder(orderId) {
  return request({ url: `/takeout/rating/byOrder/${orderId}`, method: 'get' })
}

// Submit rating
export function submitRating(data) {
  return request({ url: '/takeout/rating', method: 'post', data })
}

// Merchant reply
export function replyRating(ratingId, reply) {
  return request({ url: `/takeout/rating/reply/${ratingId}`, method: 'put', params: { reply } })
}

// Delete ratings
export function delRating(ids) {
  return request({ url: `/takeout/rating/${ids}`, method: 'delete' })
}
