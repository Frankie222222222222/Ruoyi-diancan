/**
 * utils/role.js - 角色常量与分流路由
 */
export const ROLE = {
  USER:    'user',
  KITCHEN: 'kitchen',
  RIDER:   'rider',
  ADMIN:   'admin',
};

export const ROLE_LABEL = {
  user:    '顾客',
  kitchen: '后厨',
  rider:   '骑手',
  admin:   '管理员',
};

export const ROLE_COLOR = {
  user:    '#909399',
  kitchen: '#E6A23C',
  rider:   '#409EFF',
  admin:   '#F56C6C',
};

/**
 * 根据角色返回首页 tabBar 路径
 * 注意:必须在 pages.json 里先注册这些 tabBar 页面
 */
export function homePageByRole(role) {
  switch (role) {
    case ROLE.KITCHEN: return '/pages/kitchen-home/kitchen-home';
    case ROLE.RIDER:   return '/pages/rider-home/rider-home';
    case ROLE.ADMIN:   return '/pages/admin-home/admin-home';
    case ROLE.USER:
    default:           return '/pages/home/home';
  }
}

export function isStaff(role) {
  return role === ROLE.KITCHEN || role === ROLE.RIDER || role === ROLE.ADMIN;
}
