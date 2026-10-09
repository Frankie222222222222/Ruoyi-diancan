import request from '@/utils/request'

// Query user list
export function listUser(query) {
  return request({ url: '/takeout/user/list', method: 'get', params: query })
}

// Get user detail
export function getUser(userId) {
  return request({ url: `/takeout/user/${userId}`, method: 'get' })
}

// Register new user
export function registerUser(data) {
  return request({ url: '/takeout/user/register', method: 'post', data })
}

// User login (by phone)
export function loginUser(data) {
  return request({ url: '/takeout/user/login', method: 'post', data })
}

// Update user profile
export function updateUser(data) {
  return request({ url: '/takeout/user', method: 'put', data })
}

// Delete users
export function delUser(ids) {
  return request({ url: `/takeout/user/${ids}`, method: 'delete' })
}

// Increment user stats (internal)
export function incrementUserStats(userId, amount) {
  return request({ url: `/takeout/user/incrementStats/${userId}`, method: 'post', params: { amount } })
}
