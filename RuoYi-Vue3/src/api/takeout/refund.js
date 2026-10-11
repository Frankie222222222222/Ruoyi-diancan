import request from '@/utils/request'

// 退款列表
export function listRefund(query) {
  return request({ url: '/takeout/refund/list', method: 'get', params: query })
}

// 详情
export function getRefund(complaintId) {
  return request({ url: `/takeout/refund/${complaintId}`, method: 'get' })
}

// 审核通过
export function approveRefund(complaintId, handleRemark) {
  return request({ url: `/takeout/refund/approve/${complaintId}`, method: 'put', params: { handleRemark } })
}

// 审核驳回
export function rejectRefund(complaintId, handleRemark) {
  return request({ url: `/takeout/refund/reject/${complaintId}`, method: 'put', params: { handleRemark } })
}
