-- ============================================================
-- 用户管理 - 新增 takeout:user:add 按钮菜单
-- 背景:
--   - 用户管理 2092 已经存在(parent_id = 用户与营销)
--   - 现有按钮菜单 takeout:user:list / query / edit / remove 缺 add
--   - admin(角色 id=1)授权 add 权限
-- 日期: 2026-10-12
-- ============================================================

SET NAMES utf8mb4;
USE `ry-vue`;

SET autocommit = 0;
START TRANSACTION;

-- 拿 2092 父菜单 id(用户管理)
SELECT @userMgmtId := menu_id FROM sys_menu WHERE menu_id = 2092;
SELECT IF(@userMgmtId IS NULL, 'ERROR: 父菜单 2092 不存在,请先跑 menu_final_v4.sql', 'OK: 父菜单已就绪') AS status;

-- 新增 takeout:user:add 按钮菜单(menu_type='F' 按钮)
-- order_num=4 排在 edit(query=1, edit=2, remove=3 之后, add 放最后)
INSERT INTO `sys_menu`
  (`menu_name`, `parent_id`, `order_num`, `path`, `component`, `is_frame`, `is_cache`,
   `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `remark`)
SELECT '用户新增', @userMgmtId, 4, '#', '', 1, 0,
       'F', '0', '0', 'takeout:user:add', '#', 'admin', NOW(), '管理员代建用户'
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'takeout:user:add');

-- 取 add 菜单 id
SELECT @userAddId := menu_id FROM sys_menu WHERE perms = 'takeout:user:add';

-- 授权给 admin 角色(role_id=1)
INSERT IGNORE INTO `sys_role_menu` (`role_id`, `menu_id`)
VALUES (1, @userAddId);

COMMIT;

-- ============================================================
-- 验证
-- ============================================================
SELECT '==== 用户管理 2092 下的按钮菜单 ====' AS info;
SELECT menu_id, menu_name, parent_id, order_num, perms, menu_type
FROM sys_menu
WHERE parent_id = 2092
ORDER BY order_num;

SELECT '==== admin(角色1) 已授权的 takeout:user:* 权限 ====' AS info;
SELECT m.menu_id, m.menu_name, m.perms
FROM sys_menu m
INNER JOIN sys_role_menu rm ON rm.menu_id = m.menu_id
WHERE rm.role_id = 1
  AND m.perms LIKE 'takeout:user:%'
ORDER BY m.order_num;
