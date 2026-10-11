-- =============================================================
-- takeout_menu_audit.sql
-- 外卖菜单表健康检查脚本（read-only）
-- 2026-10-11
--
-- 用途：定期跑一次，发现菜单表是否又被改乱
--      （比如有人又把某个菜单的 path 改成 takeoutV2/*，
--       或者新建了顶级 address 重复菜单）
--
-- 跑法（任选其一）：
--   1) docker exec study_booking_mysql mysql -uroot -p123456 ry-vue < takeout_menu_audit.sql
--   2) Navicat / DBeaver 直接跑
--
-- 预期：所有 query 返回 0 行。如有命中，需排查。
-- =============================================================
SET NAMES utf8mb4;
USE ry-vue;

-- ============================================================
-- 检查 1: 是否有 path 含 'takeoutV2' 或 'takeout/' 等异常前缀
-- 期望：0 行（所有外卖菜单 path 应是裸名 order/dispatch/rider...）
-- ============================================================
SELECT '==== CHECK 1: 异常前缀的 takeout path ====' AS info;
SELECT menu_id, parent_id, menu_name, path, component, status
  FROM sys_menu
 WHERE (path LIKE 'takeoutV2%' OR path LIKE 'takeout/%' OR path LIKE 'takeout-%')
   AND menu_type <> 'F'
 ORDER BY parent_id, order_num;

-- ============================================================
-- 检查 2: 是否有顶级（M 类型 path='takeout' 或 'takeoutV2'）的"外卖"父菜单超过 1 个
-- 期望：每组 path 只出现 1 次
-- ============================================================
SELECT '==== CHECK 2: 顶级外卖父菜单去重 ====' AS info;
SELECT path, COUNT(*) AS cnt, GROUP_CONCAT(menu_id) AS menu_ids, GROUP_CONCAT(menu_name) AS names
  FROM sys_menu
 WHERE menu_type = 'M' AND path IN ('takeout','takeoutV2','takeout-v2')
 GROUP BY path
HAVING COUNT(*) > 1;

-- ============================================================
-- 检查 3: component 字段对应的前端 .vue 文件是否存在
--         列出所有外卖相关 C 类型菜单
-- 注：file_exists 是个字符串标记，1=存在 0=缺失
-- 由于 MySQL 没文件系统函数，只能在 WHERE 里写注释提示；
-- 真正判断请运行：
--   ls -1 src/views/takeout/*/index.vue  (Linux/Mac)
--   Get-ChildItem -Recurse -Filter *.vue src\views\takeout (Windows)
-- 期望：每个 menu_id 都在 views/takeout/<component> 对应目录下有 index.vue
-- ============================================================
SELECT '==== CHECK 3: 外卖 C 菜单的 component 路径（手工对比 views/ 目录）====' AS info;
SELECT m.menu_id, m.parent_id, m.menu_name, m.path, m.component, m.status,
       CONCAT('views/', REPLACE(m.component, 'index', ''), 'index.vue') AS expected_file
  FROM sys_menu m
 WHERE m.menu_type = 'C'
   AND m.component LIKE 'takeout/%'
   AND m.status = '0'
 ORDER BY m.parent_id, m.order_num;

-- ============================================================
-- 检查 4: 顶级菜单中是否有 component='takeout/xxx/index' 的孤儿
--         （parent_id=0 但 component 却是 takeout 前缀）
-- 期望：0 行
-- ============================================================
SELECT '==== CHECK 4: 顶级 takeout 孤儿菜单 ====' AS info;
SELECT menu_id, menu_name, parent_id, path, component
  FROM sys_menu
 WHERE parent_id = 0
   AND component LIKE 'takeout/%'
   AND menu_type <> 'M'
   AND status = '0'
 ORDER BY menu_id;

-- ============================================================
-- 检查 5: 同一 component 字段出现多次（重复菜单）
-- 期望：每个 component 只出现 1 次（status=0 的话）
-- ============================================================
SELECT '==== CHECK 5: 同一 component 重复出现 ====' AS info;
SELECT component, COUNT(*) AS cnt,
       GROUP_CONCAT(menu_id) AS menu_ids,
       GROUP_CONCAT(menu_name) AS names
  FROM sys_menu
 WHERE component LIKE 'takeout/%' AND component <> 'Layout'
 GROUP BY component
HAVING COUNT(*) > 1
 ORDER BY component;

-- ============================================================
-- 检查 6: 启用的外卖菜单中是否有 parent_id=0 的 C 类型菜单
--         （C 类型菜单挂在顶级意味着无法折叠成子菜单）
-- 期望：0 行
-- ============================================================
SELECT '==== CHECK 6: 顶级 C 类型外卖菜单 ====' AS info;
SELECT menu_id, menu_name, path, component, status
  FROM sys_menu
 WHERE parent_id = 0
   AND menu_type = 'C'
   AND (path LIKE 'takeout%' OR component LIKE 'takeout%' OR perms LIKE 'takeout:%')
   AND status = '0'
 ORDER BY menu_id;

-- ============================================================
-- 检查 7: 状态为 0 的外卖 V2 残留菜单（应已被全部停用）
-- 期望：0 行
-- ============================================================
SELECT '==== CHECK 7: V2 残留 ====' AS info;
SELECT menu_id, menu_name, path, parent_id, status
  FROM sys_menu
 WHERE (path = 'takeoutV2' OR menu_id BETWEEN 2090 AND 2096 OR parent_id = 2044)
   AND status = '0'
 ORDER BY menu_id;

-- ============================================================
-- 总结
-- ============================================================
SELECT '==== 总结 ====' AS info;
SELECT
  (SELECT COUNT(*) FROM sys_menu WHERE status='0' AND component LIKE 'takeout/%' AND menu_type='C') AS active_takeout_C,
  (SELECT COUNT(*) FROM sys_menu WHERE status='1' AND component LIKE 'takeout/%' AND menu_type='C') AS disabled_takeout_C,
  (SELECT COUNT(*) FROM sys_menu WHERE path='takeoutV2' AND status='0') AS active_v2_parent,
  (SELECT COUNT(*) FROM sys_menu WHERE path='takeoutV2' AND status='1') AS disabled_v2_parent,
  (SELECT COUNT(*) FROM sys_menu WHERE parent_id=0 AND menu_type='C' AND component LIKE 'takeout/%' AND status='0') AS orphan_top_level_C;
