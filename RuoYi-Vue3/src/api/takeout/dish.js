import request from '@/utils/request'
import { parseStrEmpty } from "@/utils/ruoyi"

// 查询菜品列表
export function listDish(query) {
  return request({
    url: '/takeout/dish/list',
    method: 'get',
    params: query
  })
}

// 查询菜品详情
export function getDish(dishId) {
  return request({
    url: '/takeout/dish/' + parseStrEmpty(dishId),
    method: 'get'
  })
}

// 新增菜品
export function addDish(data) {
  return request({
    url: '/takeout/dish',
    method: 'post',
    data: data
  })
}

// 修改菜品
export function updateDish(data) {
  return request({
    url: '/takeout/dish',
    method: 'put',
    data: data
  })
}

// 删除菜品
export function delDish(dishIds) {
  return request({
    url: '/takeout/dish/' + dishIds,
    method: 'delete'
  })
}

// 修改菜品上下架状态
export function changeDishStatus(dishId, status) {
  const data = { dishId, status }
  return request({
    url: '/takeout/dish/changeStatus/' + dishId + '/' + status,
    method: 'put'
  })
}

// 调整菜品库存（delta 可正可负）
export function adjustDishStock(dishId, delta) {
  return request({
    url: '/takeout/dish/adjustStock/' + dishId + '/' + delta,
    method: 'put'
  })
}

// 销量 Top N
export function topSales(query) {
  return request({
    url: '/takeout/dish/topSales',
    method: 'get',
    params: query
  })
}

// 列出某商家下在售菜品
export function listDishByMerchant(merchantId) {
  return request({
    url: '/takeout/dish/listDishByMerchant/' + merchantId,
    method: 'get'
  })
}

// 列出引用过某菜品的所有订单
export function listOrdersByDishId(dishId) {
  return request({
    url: '/takeout/dish/orders/' + dishId,
    method: 'get'
  })
}
