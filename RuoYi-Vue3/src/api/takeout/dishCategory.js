import request from '@/utils/request'

// 查询菜品分类列表
export function listCategory(query) {
  return request({
    url: '/takeout/dishCategory/list',
    method: 'get',
    params: query
  })
}

// 查询菜品分类下拉选项（只查启用的）
export function optionselectCategory() {
  return request({
    url: '/takeout/dishCategory/optionselect',
    method: 'get'
  })
}

// 查询菜品分类详情
export function getCategory(categoryId) {
  return request({
    url: '/takeout/dishCategory/' + categoryId,
    method: 'get'
  })
}

// 新增菜品分类
export function addCategory(data) {
  return request({
    url: '/takeout/dishCategory',
    method: 'post',
    data: data
  })
}

// 修改菜品分类
export function updateCategory(data) {
  return request({
    url: '/takeout/dishCategory',
    method: 'put',
    data: data
  })
}

// 删除菜品分类
export function delCategory(ids) {
  return request({
    url: '/takeout/dishCategory/' + ids,
    method: 'delete'
  })
}
