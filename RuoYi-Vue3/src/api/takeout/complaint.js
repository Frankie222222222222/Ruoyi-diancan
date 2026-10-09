import request from '@/utils/request'

// Query complaint list
export function listComplaint(query) {
  return request({ url: '/takeout/complaint/list', method: 'get', params: query })
}

// Get complaint detail
export function getComplaint(complaintId) {
  return request({ url: `/takeout/complaint/${complaintId}`, method: 'get' })
}

// Submit complaint (C-end)
export function submitComplaint(data) {
  return request({ url: '/takeout/complaint', method: 'post', data })
}

// Process complaint
export function processComplaint(complaintId, status, approvedAmount, handleRemark) {
  return request({ url: `/takeout/complaint/process/${complaintId}`, method: 'put', params: { status, approvedAmount, handleRemark } })
}

// User appeal
export function appealComplaint(complaintId, content) {
  return request({ url: `/takeout/complaint/appeal/${complaintId}`, method: 'put', params: { content } })
}

// Delete complaints
export function delComplaint(ids) {
  return request({ url: `/takeout/complaint/${ids}`, method: 'delete' })
}
