-- ==========================================================
-- 外卖业务菜单清理 + 重建
-- 新层级：商家菜管理 > 商家管理 / 菜品分类 / 菜品管理
-- 删除：分类（id=2004）
-- ==========================================================
SET NAMES utf8mb4;

-- 1) 删掉旧 takeout 菜单
DELETE FROM sys_role_menu WHERE menu_id IN (
    SELECT menu_id FROM sys_menu WHERE path = 'takeout' OR path = 'merchant' OR path = 'category' OR path = 'dishCategory' OR path = 'dish'
    OR perms LIKE 'takeout:%'
);
DELETE FROM sys_menu WHERE path = 'takeout' OR path = 'merchant' OR path = 'category' OR path = 'dishCategory' OR path = 'dish';
DELETE FROM sys_menu WHERE perms LIKE 'takeout:%';

-- 2) 重建菜单
-- 2.1 父菜单：商家菜管理
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
VALUES (2000, '商家菜管理', 0, 5, 'merchantDish', NULL, 1, 0, 'M', '0', '0', '', 'shopping', 'admin', NOW(), '商家菜管理目录');

-- 2.2 商家管理（parent=2000）
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
VALUES (2001, '商家管理', 2000, 1, 'merchant', 'takeout/merchant/index', 1, 0, 'C', '0', '0', 'takeout:merchant:list', 'shop', 'admin', NOW(), '商家管理菜单');

-- 2.3 菜品分类（parent=2000）
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
VALUES (2002, '菜品分类', 2000, 2, 'dishCategory', 'takeout/dishCategory/index', 1, 0, 'C', '0', '0', 'takeout:dishCategory:list', 'list', 'admin', NOW(), '菜品分类菜单');

-- 2.4 菜品管理（parent=2000）
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
VALUES (2003, '菜品管理', 2000, 3, 'dish', 'takeout/dish/index', 1, 0, 'C', '0', '0', 'takeout:dish:list', 'food', 'admin', NOW(), '菜品管理菜单');

-- 3) 商家管理按钮权限
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES
(2010, '商家查询',   2001, 1, '#', '', 1, 0, 'F', '0', '0', 'takeout:merchant:query',  '#', 'admin', NOW(), ''),
(2011, '商家新增',   2001, 2, '#', '', 1, 0, 'F', '0', '0', 'takeout:merchant:add',    '#', 'admin', NOW(), ''),
(2012, '商家修改',   2001, 3, '#', '', 1, 0, 'F', '0', '0', 'takeout:merchant:edit',   '#', 'admin', NOW(), ''),
(2013, '商家删除',   2001, 4, '#', '', 1, 0, 'F', '0', '0', 'takeout:merchant:remove', '#', 'admin', NOW(), ''),
(2014, '修改状态',   2001, 5, '#', '', 1, 0, 'F', '0', '0', 'takeout:merchant:status', '#', 'admin', NOW(), ''),
(2015, '商家审核',   2001, 6, '#', '', 1, 0, 'F', '0', '0', 'takeout:merchant:audit',  '#', 'admin', NOW(), '');

-- 4) 菜品分类按钮权限
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES
(2020, '分类查询',   2002, 1, '#', '', 1, 0, 'F', '0', '0', 'takeout:dishCategory:query',  '#', 'admin', NOW(), ''),
(2021, '分类新增',   2002, 2, '#', '', 1, 0, 'F', '0', '0', 'takeout:dishCategory:add',    '#', 'admin', NOW(), ''),
(2022, '分类修改',   2002, 3, '#', '', 1, 0, 'F', '0', '0', 'takeout:dishCategory:edit',   '#', 'admin', NOW(), ''),
(2023, '分类删除',   2002, 4, '#', '', 1, 0, 'F', '0', '0', 'takeout:dishCategory:remove', '#', 'admin', NOW(), '');

-- 5) 菜品管理按钮权限
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES
(2030, '菜品查询',   2003, 1, '#', '', 1, 0, 'F', '0', '0', 'takeout:dish:query',   '#', 'admin', NOW(), ''),
(2031, '菜品新增',   2003, 2, '#', '', 1, 0, 'F', '0', '0', 'takeout:dish:add',     '#', 'admin', NOW(), ''),
(2032, '菜品修改',   2003, 3, '#', '', 1, 0, 'F', '0', '0', 'takeout:dish:edit',    '#', 'admin', NOW(), ''),
(2033, '菜品删除',   2003, 4, '#', '', 1, 0, 'F', '0', '0', 'takeout:dish:remove',  '#', 'admin', NOW(), '');

-- 6) 给 admin 角色分配所有 takeout 菜单权限
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 1, menu_id FROM sys_menu
WHERE path IN ('merchantDish', 'merchant', 'dishCategory', 'dish') OR perms LIKE 'takeout:%'
ON DUPLICATE KEY UPDATE role_id = role_id;

-- 7) 验证
SELECT menu_id, parent_id, menu_name, path, component, perms
FROM sys_menu
WHERE path IN ('merchantDish', 'merchant', 'dishCategory', 'dish') OR perms LIKE 'takeout:%'
ORDER BY parent_id, order_num;
