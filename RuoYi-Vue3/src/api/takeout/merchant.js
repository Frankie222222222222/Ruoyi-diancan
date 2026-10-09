import request from '@/utils/request'

// 查询商家列表
export function listMerchant(query) {
  return request({
    url: '/takeout/merchant/list',
    method: 'get',
    params: query
  })
}

// 查询商家详情
export function getMerchant(merchantId) {
  return request({
    url: '/takeout/merchant/' + merchantId,
    method: 'get'
  })
}

// 新增商家
export function addMerchant(data) {
  return request({
    url: '/takeout/merchant',
    method: 'post',
    data: data
  })
}

// 修改商家
export function updateMerchant(data) {
  return request({
    url: '/takeout/merchant',
    method: 'put',
    data: data
  })
}

// 修改营业状态
export function changeMerchantStatus(data) {
  return request({
    url: '/takeout/merchant/changeStatus',
    method: 'put',
    data: data
  })
}

// 删除商家
export function delMerchant(merchantIds) {
  return request({
    url: '/takeout/merchant/' + merchantIds,
    method: 'delete'
  })
}

// 商家统计概览
export function statMerchant(merchantId) {
  return request({
    url: '/takeout/merchant/stat/' + merchantId,
    method: 'get'
  })
}

// 商家审核
export function auditMerchant(data) {
  return request({
    url: '/takeout/merchant/audit',
    method: 'put',
    data: data
  })
}
