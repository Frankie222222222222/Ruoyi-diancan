-- =============================================================
-- 修复外卖模块 "白屏/404"
-- 2026-10-11
--
-- 根因（已查明）：
--   1. 菜单 2044 父级 "外卖业务" path='takeoutV2'，下挂 2090~2096 + 10041
--      8 个子菜单 path 是裸名（dispatch/rider/...）。
--   2. 前端 src/store/modules/permission.js 的 filterChildren() 在
--      rewriteRoutes 模式下执行：
--         el.path = lastRouter.path + '/' + el.path
--      把子菜单 path 拼成 "takeoutV2/dispatch" 这种多级路径。
--   3. vue-router 4 中，子路由 path 不能带 "/" 开头的多级段，
--      实际注册的 URL 变成 "/takeoutV2/takeoutV2/dispatch"，
--      没匹配到任何已 addRoute 的路由 → 命中 catch-all "/:pathMatch(.*)*"
--      → 渲染 views/error/404 → 用户看到 404 页面（截图一致）。
--   4. 菜单 10046/10047/10051 三个 "收货地址" 重复且都挂在顶级
--      (parent_id=0)，造成侧栏显示 3 个相同菜单，且无法收纳到
--      "外卖管理"目录下。
--   5. 10041 "堂食桌台" 挂在 2044 (V2) 下，访问同样会走 404 路径。
--   6. 10035 "后厨订单" 缺一个"查询"按钮权限，前端 v-hasPermi
--      看不到搜索/重置按钮（次要问题）。
--
-- 策略：
--   - 停用 V2 整棵子树 (2044 + 2090-2096 + 10041 中挪走的)
--   - 10041 dineTable 改挂到 2000 (外卖管理) 下
--   - address 三选一：保留 10047，停用另两个，并把 10047 挂到 2000
--   - 给后厨订单补"查询"按钮
--   - 给 admin + common 角色补授权新挂载的菜单/按钮
--
-- 不动前端 .vue / .js；不动 Controller；不用 DELETE（用 UPDATE/INSERT IGNORE）。
-- =============================================================
SET NAMES utf8mb4;
USE ry-vue;

-- ============================================================
-- 1. 停用 V2 整棵树（父菜单 + 7 个子菜单 + dineTable）
--    保留 10041（堂食桌台），但把它挪到 2000 下
-- ============================================================

-- 1.1 停用 2044 父菜单 "外卖业务"
UPDATE sys_menu SET status = '1' WHERE menu_id = 2044;

-- 1.2 停用 7 个 V2 子菜单（2000 系列有对应项，无须保留）
UPDATE sys_menu SET status = '1' WHERE menu_id IN (2090, 2091, 2092, 2093, 2094, 2095, 2096);

-- 1.3 堂食桌台 10041 从 V2 挪到 V1（2000）下
UPDATE sys_menu
   SET parent_id = 2000,
       order_num = 13
 WHERE menu_id = 10041;

-- ============================================================
-- 2. 处理 address 三重复菜单
--    保留 10047，停用 10046/10051；10047 挂到 2000 下
-- ============================================================

-- 2.1 停用两条重复
UPDATE sys_menu SET status = '1' WHERE menu_id IN (10046, 10051);

-- 2.2 10047 改父级为 2000
UPDATE sys_menu
   SET parent_id = 2000,
       order_num = 14
 WHERE menu_id = 10047;

-- ============================================================
-- 3. 补两个"查询"按钮，让前端 v-hasPermi 不再隐藏搜索/重置
-- ============================================================

-- 3.1 地址查询按钮（parent=10047 收货地址）
INSERT IGNORE INTO sys_menu
  (menu_name, parent_id, order_num, path, component,
   is_frame, is_cache, menu_type, visible, status, perms, icon,
   create_by, create_time, remark)
VALUES
  ('地址查询', 10047, 1, '#', '', 1, 0, 'F', '0', '0', 'takeout:address:query', '#',
   'admin', NOW(), '地址管理 - 列表/搜索/重置按钮');

-- 3.2 后厨查询按钮（parent=10035 后厨订单）
INSERT IGNORE INTO sys_menu
  (menu_name, parent_id, order_num, path, component,
   is_frame, is_cache, menu_type, visible, status, perms, icon,
   create_by, create_time, remark)
VALUES
  ('后厨查询', 10035, 1, '#', '', 1, 0, 'F', '0', '0', 'takeout:kitchen:query', '#',
   'admin', NOW(), '后厨订单 - 列表/搜索/重置按钮');

-- 3.3 商家查询按钮（parent=2002 商家管理，也补一下保持一致）
INSERT IGNORE INTO sys_menu
  (menu_name, parent_id, order_num, path, component,
   is_frame, is_cache, menu_type, visible, status, perms, icon,
   create_by, create_time, remark)
VALUES
  ('商家查询', 2002, 1, '#', '', 1, 0, 'F', '0', '0', 'takeout:merchant:query', '#',
   'admin', NOW(), '商家管理 - 列表/搜索/重置按钮');

-- ============================================================
-- 4. 重新授权
--    admin (role_id=1) 默认 *:*:* 实际上已全部可见，
--    但保险起见把新增的 menu_id 也写进 sys_role_menu
--    common (role_id=2) 必须显式授权，否则 V1 子菜单看不到
-- ============================================================

-- 4.1 admin：10041 (堂食桌台) / 10047 (收货地址) 改父级后，
--     sys_role_menu 里旧记录是 parent_id=2044 时的授权，
--     admin 走 *:*:* 仍然可见，但显式同步一下无副作用
INSERT IGNORE INTO sys_role_menu (role_id, menu_id)
SELECT 1, menu_id FROM sys_menu
 WHERE menu_id IN (10041, 10047)
   AND menu_id NOT IN (SELECT menu_id FROM sys_role_menu WHERE role_id = 1);

-- 4.2 admin：新插入的 3 个查询按钮
INSERT IGNORE INTO sys_role_menu (role_id, menu_id)
SELECT 1, menu_id FROM sys_menu
 WHERE perms IN ('takeout:address:query','takeout:kitchen:query','takeout:merchant:query')
   AND menu_id NOT IN (SELECT menu_id FROM sys_role_menu WHERE role_id = 1);

-- 4.3 common (role_id=2) 之前被错误授权了 V2 父菜单 2044 + 7 个 V2 子菜单，
--     这些现在都 status=1，侧栏不会再显示，无需清理 sys_role_menu
--     （RuoYi 渲染侧栏时会过滤 status=0 的菜单）。
--     但 2044 现在停用，common 角色点不到任何外卖业务菜单（V2 已全停），
--     需要补：2000 (外卖管理) 父菜单 + 10041 (堂食桌台) + 10047 (收货地址)
--     + 3 个新查询按钮
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (2, 2000);
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (2, 10041);
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (2, 10047);
INSERT IGNORE INTO sys_role_menu (role_id, menu_id)
SELECT 2, menu_id FROM sys_menu
 WHERE perms IN ('takeout:address:query','takeout:kitchen:query','takeout:merchant:query');

-- ============================================================
-- 5. 清理一下"幽灵菜单"
--    10022 是另一个"外卖管理"父菜单（status=1 已停用），
--    但 common 角色可能曾被授权过，从 sys_role_menu 里摘掉
-- ============================================================
DELETE FROM sys_role_menu
 WHERE menu_id = 10022;

-- =============================================================
-- 验证（修复后跑一下）
-- =============================================================
SELECT '==== 1. 外卖管理 2000 子菜单（应包含后厨/堂食/地址 + 原 12 个） ====' AS info;
SELECT menu_id, parent_id, menu_name, path, component, menu_type, status
  FROM sys_menu
 WHERE parent_id = 2000 AND menu_type <> 'F'
 ORDER BY order_num;

SELECT '==== 2. V2 树残留（应全 status=1） ====' AS info;
SELECT menu_id, menu_name, path, parent_id, status
  FROM sys_menu
 WHERE path = 'takeoutV2' OR parent_id = 2044 OR menu_id BETWEEN 2090 AND 2096
 ORDER BY menu_id;

SELECT '==== 3. address 三选一后剩余 ====' AS info;
SELECT menu_id, parent_id, menu_name, path, component, status
  FROM sys_menu
 WHERE component = 'takeout/address/index' OR path = 'address'
 ORDER BY menu_id;

SELECT '==== 4. 堂食桌台归位 ====' AS info;
SELECT menu_id, parent_id, menu_name, path, status
  FROM sys_menu WHERE menu_id = 10041;

SELECT '==== 5. common (role_id=2) 现在的外卖可见菜单 ====' AS info;
SELECT m.menu_id, m.parent_id, m.menu_name, m.path, m.status
  FROM sys_menu m
  JOIN sys_role_menu rm ON m.menu_id = rm.menu_id
 WHERE rm.role_id = 2
   AND (m.perms LIKE 'takeout:%' OR m.path = 'takeout')
 ORDER BY m.parent_id, m.order_num;

SELECT '==== 6. 新插入的查询按钮 ====' AS info;
SELECT menu_id, parent_id, menu_name, perms, status
  FROM sys_menu
 WHERE perms IN ('takeout:address:query','takeout:kitchen:query','takeout:merchant:query');

-- =============================================================
-- ROLLBACK（出问题时反向执行）
-- =============================================================
-- UPDATE sys_menu SET status='0' WHERE menu_id=2044;
-- UPDATE sys_menu SET status='0' WHERE menu_id IN (2090,2091,2092,2093,2094,2095,2096);
-- UPDATE sys_menu SET parent_id=2044, order_num=10 WHERE menu_id=10041;
-- UPDATE sys_menu SET status='0' WHERE menu_id IN (10046,10051);
-- UPDATE sys_menu SET parent_id=0, order_num=5 WHERE menu_id=10047;
-- DELETE FROM sys_menu WHERE perms IN ('takeout:address:query','takeout:kitchen:query','takeout:merchant:query');
-- DELETE FROM sys_role_menu WHERE role_id IN (1,2) AND menu_id IN (10041,10047);
-- DELETE FROM sys_role_menu WHERE role_id=2 AND menu_id=2000;
