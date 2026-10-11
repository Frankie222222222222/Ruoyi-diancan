-- ============================================================
-- Takeout 菜单重构
-- 把"外卖管理"一级菜单（12个二级页面挤一起）拆成 7 个一级菜单
-- ============================================================

-- 0. 启动事务
SET autocommit = 0;
START TRANSACTION;

-- 1. 删除脏数据
-- menu_id=10022/10023 是重复的乱码"外卖管理"
-- menu_id=10046 是孤儿的"收货地址"(parent_id=0, visible=0)
DELETE FROM sys_role_menu WHERE menu_id IN (10022, 10023, 10046);
DELETE FROM sys_menu WHERE menu_id IN (10022, 10023, 10046);

-- 2. 删除原"外卖管理"一级菜单 (menu_id=2000) 和合并掉的 dishSales (menu_id=2011)
-- 注意：先删 2001-2012/10035-10041/10047 的 sys_role_menu 绑定
DELETE FROM sys_role_menu WHERE menu_id IN (2000, 2011);
DELETE FROM sys_role_menu WHERE menu_id IN (2001, 2002, 2003, 2004, 2005, 2006, 2007, 2008, 2009, 2010, 2012,
                                            10035, 10036, 10037, 10038, 10039, 10040, 10041, 10047);
DELETE FROM sys_menu WHERE menu_id IN (2000, 2011);

-- 3. 新建 7 个一级菜单（M = 目录）
-- 拿到 LAST_INSERT_ID 是第一个；后续用变量串联
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
VALUES ('工作台', 0, 1, 'dashboard', NULL, 1, 0, 'M', '0', '0', '', 'dashboard', 'admin', NOW(), '一级菜单');
SET @dashboardId = LAST_INSERT_ID();

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
VALUES ('订单中心', 0, 2, 'order', NULL, 1, 0, 'M', '0', '0', '', 'order', 'admin', NOW(), '一级菜单');
SET @orderId = LAST_INSERT_ID();

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
VALUES ('商品管理', 0, 3, 'product', NULL, 1, 0, 'M', '0', '0', '', 'product', 'admin', NOW(), '一级菜单');
SET @productId = LAST_INSERT_ID();

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
VALUES ('门店运营', 0, 4, 'shop', NULL, 1, 0, 'M', '0', '0', '', 'shop', 'admin', NOW(), '一级菜单');
SET @shopId = LAST_INSERT_ID();

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
VALUES ('骑手管理', 0, 5, 'rider', NULL, 1, 0, 'M', '0', '0', '', 'rider', 'admin', NOW(), '一级菜单');
SET @riderId = LAST_INSERT_ID();

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
VALUES ('用户与营销', 0, 6, 'marketing', NULL, 1, 0, 'M', '0', '0', '', 'user', 'admin', NOW(), '一级菜单');
SET @marketingId = LAST_INSERT_ID();

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
VALUES ('售后与反馈', 0, 7, 'aftersale', NULL, 1, 0, 'M', '0', '0', '', 'service', 'admin', NOW(), '一级菜单');
SET @aftersaleId = LAST_INSERT_ID();

-- 4. 迁移二级页面 (C = 菜单)
-- 4.1 工作台 -> 运营统计
UPDATE sys_menu SET parent_id=@dashboardId, order_num=1, path='statistics', component='takeout/statistics/index', icon='data-line', menu_type='C'
  WHERE menu_id=2012;

-- 4.2 订单中心 -> order/dispatch/kitchen
UPDATE sys_menu SET parent_id=@orderId, order_num=1, path='order', component='takeout/order/index', icon='list', menu_type='C'
  WHERE menu_id=2001;
UPDATE sys_menu SET parent_id=@orderId, order_num=2, path='dispatch', component='takeout/dispatch/index', icon='van', menu_type='C'
  WHERE menu_id=2006;
UPDATE sys_menu SET parent_id=@orderId, order_num=3, path='kitchen', component='takeout/kitchen/index', icon='dish', menu_type='C'
  WHERE menu_id=10035;

-- 4.3 商品管理 -> dish/dishCategory
UPDATE sys_menu SET parent_id=@productId, order_num=1, path='dish', component='takeout/dish/index', icon='food', menu_type='C'
  WHERE menu_id=2003;
UPDATE sys_menu SET parent_id=@productId, order_num=2, path='dishCategory', component='takeout/dishCategory/index', icon='collection', menu_type='C'
  WHERE menu_id=2004;

-- 4.4 门店运营 -> merchant/dineTable
UPDATE sys_menu SET parent_id=@shopId, order_num=1, path='merchant', component='takeout/merchant/index', icon='store', menu_type='C'
  WHERE menu_id=2002;
UPDATE sys_menu SET parent_id=@shopId, order_num=2, path='dineTable', component='takeout/dineTable/index', icon='table', menu_type='C'
  WHERE menu_id=10041;

-- 4.5 骑手管理 -> rider
UPDATE sys_menu SET parent_id=@riderId, order_num=1, path='rider', component='takeout/rider/index', icon='avatar', menu_type='C'
  WHERE menu_id=2005;

-- 4.6 用户与营销 -> user/address/coupon
UPDATE sys_menu SET parent_id=@marketingId, order_num=1, path='user', component='takeout/user/index', icon='peoples', menu_type='C'
  WHERE menu_id=2007;
UPDATE sys_menu SET parent_id=@marketingId, order_num=2, path='address', component='takeout/address/index', icon='location', menu_type='C'
  WHERE menu_id=10047;
UPDATE sys_menu SET parent_id=@marketingId, order_num=3, path='coupon', component='takeout/coupon/index', icon='discount', menu_type='C'
  WHERE menu_id=2008;

-- 4.7 售后与反馈 -> rating/complaint
UPDATE sys_menu SET parent_id=@aftersaleId, order_num=1, path='rating', component='takeout/rating/index', icon='star', menu_type='C'
  WHERE menu_id=2009;
UPDATE sys_menu SET parent_id=@aftersaleId, order_num=2, path='complaint', component='takeout/complaint/index', icon='warning', menu_type='C'
  WHERE menu_id=2010;

-- 5. 新增"打印扫码设置"占位页 (门店运营下)
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
VALUES ('打印扫码设置', @shopId, 3, 'print', 'takeout/print/index', 1, 0, 'C', '0', '0', 'takeout:print:list', 'printer', 'admin', NOW(), '占位页');
SET @printId = LAST_INSERT_ID();

-- 6. 补齐已有 vue 页面但没菜单的条目
-- 6.1 骑手位置
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
VALUES ('骑手位置', @riderId, 2, 'riderLocation', 'takeout/riderLocation/index', 1, 0, 'C', '0', '0', 'takeout:rider:list', 'location', 'admin', NOW(), '');

-- 6.2 Banner管理
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
VALUES ('Banner管理', @marketingId, 4, 'banner', 'takeout/ops/banner', 1, 0, 'C', '0', '0', 'takeout:ops:list', 'picture', 'admin', NOW(), '');

-- 6.3 公告管理
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
VALUES ('公告管理', @marketingId, 5, 'announcement', 'takeout/ops/announcement', 1, 0, 'C', '0', '0', 'takeout:ops:list', 'message', 'admin', NOW(), '');

-- 6.4 帮助中心
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
VALUES ('帮助中心', @marketingId, 6, 'help', 'takeout/ops/help', 1, 0, 'C', '0', '0', 'takeout:ops:list', 'documentation', 'admin', NOW(), '');

-- 7. 把所有新菜单授权给 admin (role_id=1)
INSERT IGNORE INTO sys_role_menu (role_id, menu_id)
SELECT 1, menu_id FROM sys_menu
WHERE menu_id IN (@dashboardId, @orderId, @productId, @shopId, @riderId, @marketingId, @aftersaleId);

-- 已迁移的旧菜单重新授权
INSERT IGNORE INTO sys_role_menu (role_id, menu_id)
SELECT 1, menu_id FROM sys_menu
WHERE menu_id IN (2001, 2006, 10035, 2003, 2004, 2002, 10041, 2005, 2007, 10047, 2008, 2009, 2010, 2012, @printId);

-- 新加的 (ops/riderLocation) 授权
INSERT IGNORE INTO sys_role_menu (role_id, menu_id)
SELECT 1, menu_id FROM sys_menu
WHERE menu_name IN ('打印扫码设置','骑手位置','Banner管理','公告管理','帮助中心');

-- 8. 按钮菜单自动跟着二级页面走（parent_id 不变），重新授权给 admin
INSERT IGNORE INTO sys_role_menu (role_id, menu_id)
SELECT 1, m.menu_id FROM sys_menu m
WHERE m.menu_type = 'F'
  AND m.parent_id IN (2001, 2002, 2003, 2004, 2005, 2006, 2007, 2008, 2009, 2010, 2012, 10035, 10041, 10047)
  AND m.menu_id NOT IN (SELECT menu_id FROM sys_role_menu WHERE role_id=1);

COMMIT;

-- 9. 验证
SELECT '一级菜单:' AS info;
SELECT menu_id, menu_name, parent_id, order_num, path, icon FROM sys_menu
WHERE parent_id=0 AND menu_type='M' ORDER BY order_num;

SELECT '工作台子菜单:' AS info;
SELECT menu_id, menu_name, order_num, path, component FROM sys_menu
WHERE parent_id=@dashboardId ORDER BY order_num;

SELECT '订单中心子菜单:' AS info;
SELECT menu_id, menu_name, order_num, path FROM sys_menu
WHERE parent_id=@orderId ORDER BY order_num;
