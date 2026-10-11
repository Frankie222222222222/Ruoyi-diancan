import request from '@/utils/request'

// Banner 列表
export function listBanner() {
  return request({ url: '/takeout/banner/list', method: 'get' })
}

// 新增/编辑
export function saveBanner(data) {
  return request({ url: '/takeout/banner', method: 'post', data })
}

// 删除
export function delBanner(ids) {
  return request({ url: `/takeout/banner/${ids}`, method: 'delete' })
}

// 公告
export function listAnnouncement() {
  return request({ url: '/takeout/announcement/list', method: 'get' })
}
export function saveAnnouncement(data) {
  return request({ url: '/takeout/announcement', method: 'post', data })
}
export function delAnnouncement(ids) {
  return request({ url: `/takeout/announcement/${ids}`, method: 'delete' })
}

// 帮助分类
export function listHelpCategory() {
  return request({ url: '/takeout/help/category/list', method: 'get' })
}
export function saveHelpCategory(data) {
  return request({ url: '/takeout/help/category', method: 'post', data })
}
// 帮助文章
export function listHelpArticle(categoryId) {
  return request({ url: '/takeout/help/article/list', method: 'get', params: { categoryId } })
}
export function saveHelpArticle(data) {
  return request({ url: '/takeout/help/article', method: 'post', data })
}
export function delHelpArticle(ids) {
  return request({ url: `/takeout/help/article/${ids}`, method: 'delete' })
}
