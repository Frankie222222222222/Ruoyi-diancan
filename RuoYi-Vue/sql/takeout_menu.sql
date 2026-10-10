-- ============================================================
-- 外卖管理模块菜单 / 权限 / 路由
-- 适用：RuoYi-Vue3（前端 view 路径 takeout/*）
-- 数据库：ry-vue
-- 起点 menuId = 2000（已有最大 1060，安全隔离）
-- 重复执行会先清掉旧数据再插入，幂等
-- 重要：本文件必须以 UTF-8（无 BOM）保存。Windows PowerShell 调用 mysql
--       时若用 Get-Content | mysql 会出现 GBK 乱码导致解析失败，
--       推荐：python 子进程 + --default-character-set=utf8mb4
-- 表 sys_menu 真实列序：
--   menu_id, menu_name, parent_id, order_num, path, component,
--   query, route_name, is_frame, is_cache, menu_type, visible, status,
--   perms, icon, create_by, create_time, update_by, update_time, remark
-- ============================================================

-- 1. 清理旧「外卖」菜单 + 角色关联
DELETE FROM sys_role_menu
 WHERE menu_id IN (SELECT menu_id FROM sys_menu
                    WHERE menu_id >= 2000
                       OR menu_name = '外卖管理'
                       OR path = 'takeout'
                       OR path LIKE 'takeout/%');

DELETE FROM sys_menu
 WHERE menu_id >= 2000
    OR menu_name = '外卖管理'
    OR path = 'takeout'
    OR path LIKE 'takeout/%';

-- ============================================================
-- 2. 顶级目录：外卖管理
-- ============================================================
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES
(2000, '外卖管理', 0, 5, 'takeout', null, null, '', '1', '0', 'M', '0', '0', '', 'shopping', 'admin', NOW(), '外卖管理目录');

-- ============================================================
-- 3. 子菜单（C 类型）— menuId 2001 ~ 2012
-- ============================================================
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES
(2001, '订单管理',   2000,  1, 'order',        'takeout/order/index',        null, '', '1', '0', 'C', '0', '0', 'takeout:order:list',        'list',      'admin', NOW(), '外卖订单管理'),
(2002, '商家管理',   2000,  2, 'merchant',     'takeout/merchant/index',     null, '', '1', '0', 'C', '0', '0', 'takeout:merchant:list',     'shop',      'admin', NOW(), '入驻商家管理'),
(2003, '菜品管理',   2000,  3, 'dish',         'takeout/dish/index',         null, '', '1', '0', 'C', '0', '0', 'takeout:dish:list',         'dish',      'admin', NOW(), '菜品信息维护'),
(2004, '菜品分类',   2000,  4, 'dishCategory', 'takeout/dishCategory/index', null, '', '1', '0', 'C', '0', '0', 'takeout:dishCategory:list', 'tree',      'admin', NOW(), '菜品分类管理'),
(2005, '骑手管理',   2000,  5, 'rider',        'takeout/rider/index',        null, '', '1', '0', 'C', '0', '0', 'takeout:rider:list',        'peoples',   'admin', NOW(), '骑手信息'),
(2006, '配送调度',   2000,  6, 'dispatch',     'takeout/dispatch/index',     null, '', '1', '0', 'C', '0', '0', 'takeout:dispatch:list',     'log',       'admin', NOW(), '订单配送调度'),
(2007, '用户管理',   2000,  7, 'user',         'takeout/user/index',         null, '', '1', '0', 'C', '0', '0', 'takeout:user:list',         'user',      'admin', NOW(), 'C端用户'),
(2008, '优惠券管理', 2000,  8, 'coupon',       'takeout/coupon/index',       null, '', '1', '0', 'C', '0', '0', 'takeout:coupon:list',       'money',     'admin', NOW(), '优惠券模板'),
(2009, '评价管理',   2000,  9, 'rating',       'takeout/rating/index',       null, '', '1', '0', 'C', '0', '0', 'takeout:rating:list',       'star',      'admin', NOW(), '订单评价'),
(2010, '投诉管理',   2000, 10, 'complaint',    'takeout/complaint/index',    null, '', '1', '0', 'C', '0', '0', 'takeout:complaint:list',    'warn',      'admin', NOW(), '用户投诉处理'),
(2011, '销量统计',   2000, 11, 'dishSales',    'takeout/dishSales/index',    null, '', '1', '0', 'C', '0', '0', 'takeout:dishSales:list',    'chart',     'admin', NOW(), '菜品销量统计'),
(2012, '运营统计',   2000, 12, 'statistics',   'takeout/statistics/index',   null, '', '1', '0', 'C', '0', '0', 'takeout:statistics:list',   'dashboard', 'admin', NOW(), '外卖运营统计');

-- ============================================================
-- 4. 按钮权限（F 类型）— 5 个 / 子菜单：查询/新增/修改/删除/导出
--    统计类只给「查询+导出」共 2 个
--    menuId: 3001~3054
-- ============================================================
-- 2001 订单管理
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES
(3001, '订单查询', 2001, 1, '', null, null, '', '1', '0', 'F', '0', '0', 'takeout:order:query',  '#', 'admin', NOW(), ''),
(3002, '订单新增', 2001, 2, '', null, null, '', '1', '0', 'F', '0', '0', 'takeout:order:add',    '#', 'admin', NOW(), ''),
(3003, '订单修改', 2001, 3, '', null, null, '', '1', '0', 'F', '0', '0', 'takeout:order:edit',   '#', 'admin', NOW(), ''),
(3004, '订单删除', 2001, 4, '', null, null, '', '1', '0', 'F', '0', '0', 'takeout:order:remove', '#', 'admin', NOW(), ''),
(3005, '订单导出', 2001, 5, '', null, null, '', '1', '0', 'F', '0', '0', 'takeout:order:export', '#', 'admin', NOW(), '');

-- 2002 商家管理
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES
(3006, '商家查询', 2002, 1, '', null, null, '', '1', '0', 'F', '0', '0', 'takeout:merchant:query',  '#', 'admin', NOW(), ''),
(3007, '商家新增', 2002, 2, '', null, null, '', '1', '0', 'F', '0', '0', 'takeout:merchant:add',    '#', 'admin', NOW(), ''),
(3008, '商家修改', 2002, 3, '', null, null, '', '1', '0', 'F', '0', '0', 'takeout:merchant:edit',   '#', 'admin', NOW(), ''),
(3009, '商家删除', 2002, 4, '', null, null, '', '1', '0', 'F', '0', '0', 'takeout:merchant:remove', '#', 'admin', NOW(), ''),
(3010, '商家导出', 2002, 5, '', null, null, '', '1', '0', 'F', '0', '0', 'takeout:merchant:export', '#', 'admin', NOW(), '');

-- 2003 菜品管理
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES
(3011, '菜品查询', 2003, 1, '', null, null, '', '1', '0', 'F', '0', '0', 'takeout:dish:query',  '#', 'admin', NOW(), ''),
(3012, '菜品新增', 2003, 2, '', null, null, '', '1', '0', 'F', '0', '0', 'takeout:dish:add',    '#', 'admin', NOW(), ''),
(3013, '菜品修改', 2003, 3, '', null, null, '', '1', '0', 'F', '0', '0', 'takeout:dish:edit',   '#', 'admin', NOW(), ''),
(3014, '菜品删除', 2003, 4, '', null, null, '', '1', '0', 'F', '0', '0', 'takeout:dish:remove', '#', 'admin', NOW(), ''),
(3015, '菜品导出', 2003, 5, '', null, null, '', '1', '0', 'F', '0', '0', 'takeout:dish:export', '#', 'admin', NOW(), '');

-- 2004 菜品分类
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES
(3016, '菜品分类查询', 2004, 1, '', null, null, '', '1', '0', 'F', '0', '0', 'takeout:dishCategory:query',  '#', 'admin', NOW(), ''),
(3017, '菜品分类新增', 2004, 2, '', null, null, '', '1', '0', 'F', '0', '0', 'takeout:dishCategory:add',    '#', 'admin', NOW(), ''),
(3018, '菜品分类修改', 2004, 3, '', null, null, '', '1', '0', 'F', '0', '0', 'takeout:dishCategory:edit',   '#', 'admin', NOW(), ''),
(3019, '菜品分类删除', 2004, 4, '', null, null, '', '1', '0', 'F', '0', '0', 'takeout:dishCategory:remove', '#', 'admin', NOW(), ''),
(3020, '菜品分类导出', 2004, 5, '', null, null, '', '1', '0', 'F', '0', '0', 'takeout:dishCategory:export', '#', 'admin', NOW(), '');

-- 2005 骑手管理
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES
(3021, '骑手查询', 2005, 1, '', null, null, '', '1', '0', 'F', '0', '0', 'takeout:rider:query',  '#', 'admin', NOW(), ''),
(3022, '骑手新增', 2005, 2, '', null, null, '', '1', '0', 'F', '0', '0', 'takeout:rider:add',    '#', 'admin', NOW(), ''),
(3023, '骑手修改', 2005, 3, '', null, null, '', '1', '0', 'F', '0', '0', 'takeout:rider:edit',   '#', 'admin', NOW(), ''),
(3024, '骑手删除', 2005, 4, '', null, null, '', '1', '0', 'F', '0', '0', 'takeout:rider:remove', '#', 'admin', NOW(), ''),
(3025, '骑手导出', 2005, 5, '', null, null, '', '1', '0', 'F', '0', '0', 'takeout:rider:export', '#', 'admin', NOW(), '');

-- 2006 配送调度
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES
(3026, '配送查询', 2006, 1, '', null, null, '', '1', '0', 'F', '0', '0', 'takeout:dispatch:query',  '#', 'admin', NOW(), ''),
(3027, '配送新增', 2006, 2, '', null, null, '', '1', '0', 'F', '0', '0', 'takeout:dispatch:add',    '#', 'admin', NOW(), ''),
(3028, '配送修改', 2006, 3, '', null, null, '', '1', '0', 'F', '0', '0', 'takeout:dispatch:edit',   '#', 'admin', NOW(), ''),
(3029, '配送删除', 2006, 4, '', null, null, '', '1', '0', 'F', '0', '0', 'takeout:dispatch:remove', '#', 'admin', NOW(), ''),
(3030, '配送导出', 2006, 5, '', null, null, '', '1', '0', 'F', '0', '0', 'takeout:dispatch:export', '#', 'admin', NOW(), '');

-- 2007 用户管理
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES
(3031, '用户查询', 2007, 1, '', null, null, '', '1', '0', 'F', '0', '0', 'takeout:user:query',  '#', 'admin', NOW(), ''),
(3032, '用户新增', 2007, 2, '', null, null, '', '1', '0', 'F', '0', '0', 'takeout:user:add',    '#', 'admin', NOW(), ''),
(3033, '用户修改', 2007, 3, '', null, null, '', '1', '0', 'F', '0', '0', 'takeout:user:edit',   '#', 'admin', NOW(), ''),
(3034, '用户删除', 2007, 4, '', null, null, '', '1', '0', 'F', '0', '0', 'takeout:user:remove', '#', 'admin', NOW(), ''),
(3035, '用户导出', 2007, 5, '', null, null, '', '1', '0', 'F', '0', '0', 'takeout:user:export', '#', 'admin', NOW(), '');

-- 2008 优惠券管理
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES
(3036, '优惠券查询', 2008, 1, '', null, null, '', '1', '0', 'F', '0', '0', 'takeout:coupon:query',  '#', 'admin', NOW(), ''),
(3037, '优惠券新增', 2008, 2, '', null, null, '', '1', '0', 'F', '0', '0', 'takeout:coupon:add',    '#', 'admin', NOW(), ''),
(3038, '优惠券修改', 2008, 3, '', null, null, '', '1', '0', 'F', '0', '0', 'takeout:coupon:edit',   '#', 'admin', NOW(), ''),
(3039, '优惠券删除', 2008, 4, '', null, null, '', '1', '0', 'F', '0', '0', 'takeout:coupon:remove', '#', 'admin', NOW(), ''),
(3040, '优惠券导出', 2008, 5, '', null, null, '', '1', '0', 'F', '0', '0', 'takeout:coupon:export', '#', 'admin', NOW(), '');

-- 2009 评价管理
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES
(3041, '评价查询', 2009, 1, '', null, null, '', '1', '0', 'F', '0', '0', 'takeout:rating:query',  '#', 'admin', NOW(), ''),
(3042, '评价新增', 2009, 2, '', null, null, '', '1', '0', 'F', '0', '0', 'takeout:rating:add',    '#', 'admin', NOW(), ''),
(3043, '评价修改', 2009, 3, '', null, null, '', '1', '0', 'F', '0', '0', 'takeout:rating:edit',   '#', 'admin', NOW(), ''),
(3044, '评价删除', 2009, 4, '', null, null, '', '1', '0', 'F', '0', '0', 'takeout:rating:remove', '#', 'admin', NOW(), ''),
(3045, '评价导出', 2009, 5, '', null, null, '', '1', '0', 'F', '0', '0', 'takeout:rating:export', '#', 'admin', NOW(), '');

-- 2010 投诉管理
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES
(3046, '投诉查询', 2010, 1, '', null, null, '', '1', '0', 'F', '0', '0', 'takeout:complaint:query',  '#', 'admin', NOW(), ''),
(3047, '投诉新增', 2010, 2, '', null, null, '', '1', '0', 'F', '0', '0', 'takeout:complaint:add',    '#', 'admin', NOW(), ''),
(3048, '投诉修改', 2010, 3, '', null, null, '', '1', '0', 'F', '0', '0', 'takeout:complaint:edit',   '#', 'admin', NOW(), ''),
(3049, '投诉删除', 2010, 4, '', null, null, '', '1', '0', 'F', '0', '0', 'takeout:complaint:remove', '#', 'admin', NOW(), ''),
(3050, '投诉导出', 2010, 5, '', null, null, '', '1', '0', 'F', '0', '0', 'takeout:complaint:export', '#', 'admin', NOW(), '');

-- 2011 销量统计
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES
(3051, '销量查询', 2011, 1, '', null, null, '', '1', '0', 'F', '0', '0', 'takeout:dishSales:query',  '#', 'admin', NOW(), ''),
(3052, '销量导出', 2011, 5, '', null, null, '', '1', '0', 'F', '0', '0', 'takeout:dishSales:export', '#', 'admin', NOW(), '');

-- 2012 运营统计
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES
(3053, '运营查询', 2012, 1, '', null, null, '', '1', '0', 'F', '0', '0', 'takeout:statistics:query',  '#', 'admin', NOW(), ''),
(3054, '运营导出', 2012, 5, '', null, null, '', '1', '0', 'F', '0', '0', 'takeout:statistics:export', '#', 'admin', NOW(), '');

-- ============================================================
-- 5. 角色授权：超级管理员(1) + 管理员(2)
-- ============================================================
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 1, menu_id FROM sys_menu WHERE menu_id >= 2000;

INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 2, menu_id FROM sys_menu WHERE menu_id >= 2000;

-- ============================================================
-- 6. 验证
-- ============================================================
SELECT menu_id, menu_name, parent_id, order_num, path, component, perms, menu_type
  FROM sys_menu
 WHERE menu_id >= 2000
 ORDER BY menu_id;
