-- =============================================================
-- 外卖业务 · 菜单 v2 (7 个新模块菜单)
-- 在 takeout_menu_fix.sql 基础上追加
-- 父菜单: 2044 (外卖管理)
-- 子菜单: 骑手管理 / 评价管理 / 优惠券管理 / 投诉管理 / C端用户 / 数据统计 / 派单管理
-- =============================================================
SET NAMES utf8mb4;

-- 1) 清掉旧版本(如有), 防重复插入
DELETE FROM sys_role_menu WHERE menu_id IN (
  SELECT menu_id FROM sys_menu WHERE path IN (
    'rider','rating','coupon','complaint','takeoutUser','statistics','dispatch'
  ) OR perms IN (
    'takeout:rider:list','takeout:rating:list','takeout:coupon:list',
    'takeout:complaint:list','takeout:user:list','takeout:statistics:list','takeout:dispatch:list'
  )
);
DELETE FROM sys_menu WHERE path IN (
  'rider','rating','coupon','complaint','takeoutUser','statistics','dispatch'
);
DELETE FROM sys_menu WHERE perms IN (
  'takeout:rider:list','takeout:rating:list','takeout:coupon:list',
  'takeout:complaint:list','takeout:user:list','takeout:statistics:list','takeout:dispatch:list'
);

-- 2) 新增子菜单 (parent=2044 外卖管理)
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES
(2090, '派单管理',  2044, 1, 'dispatch',  'takeout/dispatch/index',  1, 0, 'C', '0', '0', 'takeout:dispatch:list',  'guide',     'admin', NOW(), '订单派单/抢单管理'),
(2091, '骑手管理',  2044, 2, 'rider',     'takeout/rider/index',     1, 0, 'C', '0', '0', 'takeout:rider:list',     'bicycle',   'admin', NOW(), '骑手信息管理'),
(2092, 'C端用户',   2044, 3, 'takeoutUser','takeout/user/index',     1, 0, 'C', '0', '0', 'takeout:user:list',      'user',      'admin', NOW(), 'C端外卖用户管理'),
(2093, '评价管理',  2044, 4, 'rating',    'takeout/rating/index',    1, 0, 'C', '0', '0', 'takeout:rating:list',    'star',      'admin', NOW(), '订单评价管理'),
(2094, '优惠券管理',2044, 5, 'coupon',    'takeout/coupon/index',    1, 0, 'C', '0', '0', 'takeout:coupon:list',    'money',     'admin', NOW(), '优惠券管理'),
(2095, '投诉管理',  2044, 6, 'complaint', 'takeout/complaint/index', 1, 0, 'C', '0', '0', 'takeout:complaint:list', 'message',   'admin', NOW(), '投诉退款工单'),
(2096, '数据统计',  2044, 7, 'statistics','takeout/statistics/index',1, 0, 'C', '0', '0', 'takeout:statistics:list','chart',     'admin', NOW(), '外卖数据统计');

-- 3) 派单管理按钮 (2090 下)
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES
(2150, '派单查询', 2090, 1, '#', '', 1, 0, 'F', '0', '0', 'takeout:dispatch:query',  '#', 'admin', NOW(), ''),
(2151, '新增派单', 2090, 2, '#', '', 1, 0, 'F', '0', '0', 'takeout:dispatch:add',    '#', 'admin', NOW(), ''),
(2152, '删除派单', 2090, 3, '#', '', 1, 0, 'F', '0', '0', 'takeout:dispatch:remove', '#', 'admin', NOW(), '');

-- 4) 骑手管理按钮 (2091 下)
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES
(2160, '骑手查询', 2091, 1, '#', '', 1, 0, 'F', '0', '0', 'takeout:rider:query',  '#', 'admin', NOW(), ''),
(2161, '骑手新增', 2091, 2, '#', '', 1, 0, 'F', '0', '0', 'takeout:rider:add',    '#', 'admin', NOW(), ''),
(2162, '骑手修改', 2091, 3, '#', '', 1, 0, 'F', '0', '0', 'takeout:rider:edit',   '#', 'admin', NOW(), ''),
(2163, '骑手删除', 2091, 4, '#', '', 1, 0, 'F', '0', '0', 'takeout:rider:remove', '#', 'admin', NOW(), ''),
(2164, '修改状态', 2091, 5, '#', '', 1, 0, 'F', '0', '0', 'takeout:rider:status', '#', 'admin', NOW(), '');

-- 5) C端用户按钮 (2092 下)
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES
(2170, '用户查询', 2092, 1, '#', '', 1, 0, 'F', '0', '0', 'takeout:user:query',  '#', 'admin', NOW(), ''),
(2171, '用户修改', 2092, 2, '#', '', 1, 0, 'F', '0', '0', 'takeout:user:edit',   '#', 'admin', NOW(), ''),
(2172, '用户删除', 2092, 3, '#', '', 1, 0, 'F', '0', '0', 'takeout:user:remove', '#', 'admin', NOW(), '');

-- 6) 评价管理按钮 (2093 下)
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES
(2180, '评价查询', 2093, 1, '#', '', 1, 0, 'F', '0', '0', 'takeout:rating:query',  '#', 'admin', NOW(), ''),
(2181, '评价回复', 2093, 2, '#', '', 1, 0, 'F', '0', '0', 'takeout:rating:reply',  '#', 'admin', NOW(), ''),
(2182, '评价删除', 2093, 3, '#', '', 1, 0, 'F', '0', '0', 'takeout:rating:remove', '#', 'admin', NOW(), '');

-- 7) 优惠券按钮 (2094 下)
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES
(2190, '券查询',   2094, 1, '#', '', 1, 0, 'F', '0', '0', 'takeout:coupon:query',  '#', 'admin', NOW(), ''),
(2191, '券新增',   2094, 2, '#', '', 1, 0, 'F', '0', '0', 'takeout:coupon:add',    '#', 'admin', NOW(), ''),
(2192, '券修改',   2094, 3, '#', '', 1, 0, 'F', '0', '0', 'takeout:coupon:edit',   '#', 'admin', NOW(), ''),
(2193, '券删除',   2094, 4, '#', '', 1, 0, 'F', '0', '0', 'takeout:coupon:remove', '#', 'admin', NOW(), '');

-- 8) 投诉工单按钮 (2095 下)
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES
(2200, '工单查询', 2095, 1, '#', '', 1, 0, 'F', '0', '0', 'takeout:complaint:query',  '#', 'admin', NOW(), ''),
(2201, '处理工单', 2095, 2, '#', '', 1, 0, 'F', '0', '0', 'takeout:complaint:process','#', 'admin', NOW(), ''),
(2202, '工单删除', 2095, 3, '#', '', 1, 0, 'F', '0', '0', 'takeout:complaint:remove', '#', 'admin', NOW(), '');

-- 9) 授权给 admin 角色(role_id=1)
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 1, menu_id FROM sys_menu
WHERE path IN ('dispatch','rider','takeoutUser','rating','coupon','complaint','statistics')
   OR perms IN (
     'takeout:dispatch:query','takeout:dispatch:add','takeout:dispatch:remove',
     'takeout:rider:query','takeout:rider:add','takeout:rider:edit','takeout:rider:remove','takeout:rider:status',
     'takeout:user:query','takeout:user:edit','takeout:user:remove',
     'takeout:rating:query','takeout:rating:reply','takeout:rating:remove',
     'takeout:coupon:query','takeout:coupon:add','takeout:coupon:edit','takeout:coupon:remove',
     'takeout:complaint:query','takeout:complaint:process','takeout:complaint:remove',
     'takeout:dispatch:list','takeout:rider:list','takeout:user:list','takeout:rating:list',
     'takeout:coupon:list','takeout:complaint:list','takeout:statistics:list'
   )
ON DUPLICATE KEY UPDATE role_id = role_id;

-- 10) 验证
SELECT menu_id, parent_id, menu_name, path, component, menu_type, perms
FROM sys_menu
WHERE path IN ('dispatch','rider','takeoutUser','rating','coupon','complaint','statistics')
ORDER BY menu_id;
