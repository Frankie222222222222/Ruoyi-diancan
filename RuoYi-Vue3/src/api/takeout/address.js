import request from '@/utils/request'

// 收货地址列表
export function listAddress(query) {
  return request({ url: '/takeout/address/list', method: 'get', params: query })
}

// 收货地址详情
export function getAddress(addressId) {
  return request({ url: `/takeout/address/${addressId}`, method: 'get' })
}

// 新增/编辑(upsert)
export function saveAddress(data) {
  return request({ url: '/takeout/address', method: 'post', data })
}

// 设为默认
export function setDefaultAddress(addressId) {
  return request({ url: `/takeout/address/default/${addressId}`, method: 'put' })
}

// 删除
export function delAddress(ids) {
  return request({ url: `/takeout/address/${ids}`, method: 'delete' })
}
