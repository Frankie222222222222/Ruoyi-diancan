-- e:\ruoyi-vue3\db\fix-menus-and-grants.sql
-- 修正 takeout 菜单四大问题(顺序敏感):
--   1) 同 perms 重复的菜单(去重,保留最低 menu_id)
--   2) 菜单名 GBK 乱码(用 UTF-8 中文覆盖)
--   3) v2/v3 菜单被 auto_increment 挤到 10000+ 段(迁回 2000+ 段,或重新挂到正确 parent)
--   4) 角色授权不全(补齐 admin / 商家 / 后厨 / 骑手 / common 五个角色)

USE ry-vue;
SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- 备份
DROP TABLE IF EXISTS sys_menu_takeout_bak_pre_fix;
CREATE TABLE sys_menu_takeout_bak_pre_fix LIKE sys_menu;
INSERT INTO sys_menu_takeout_bak_pre_fix
  SELECT * FROM sys_menu
  WHERE (menu_id BETWEEN 2000 AND 2200) OR (menu_id BETWEEN 10000 AND 10999);

-- ============================================================
-- 阶段 1: 去重(同 perms 保留最低 menu_id,删除其他;perms 为空的保留)
-- 先收集要删的 menu_id,确保 sys_role_menu 没有指向它
-- ============================================================
DROP TEMPORARY TABLE IF EXISTS tmp_dup_menus;
CREATE TEMPORARY TABLE tmp_dup_menus (menu_id BIGINT PRIMARY KEY);
INSERT INTO tmp_dup_menus
SELECT m.menu_id FROM sys_menu m
INNER JOIN (
  SELECT perms, MIN(menu_id) AS keep_id
  FROM sys_menu
  WHERE perms IS NOT NULL AND perms <> ''
    AND perms LIKE 'takeout:%'
  GROUP BY perms
  HAVING COUNT(*) > 1
) k ON k.perms = m.perms
WHERE m.menu_id <> k.keep_id;

-- 先解绑 sys_role_menu 中要删的菜单
DELETE rm FROM sys_role_menu rm
INNER JOIN tmp_dup_menus d ON d.menu_id = rm.menu_id;

-- 再删菜单本身
DELETE m FROM sys_menu m
INNER JOIN tmp_dup_menus d ON d.menu_id = m.menu_id;

SELECT CONCAT('Deleted dup menus: ', COUNT(*)) AS info FROM tmp_dup_menus;
DROP TEMPORARY TABLE tmp_dup_menus;

-- ============================================================
-- 阶段 2: 修正 GBK 乱码的菜单名
-- 用 v2 SQL 的权威中文覆盖;用 perms 锚定更稳
-- ============================================================
-- 2.1 v1 顶层 13 个菜单(menu_id 是权威)
UPDATE sys_menu SET menu_name = '外卖管理'   WHERE menu_id = 2000;
UPDATE sys_menu SET menu_name = '订单管理'   WHERE menu_id = 2001;
UPDATE sys_menu SET menu_name = '商家管理'   WHERE menu_id = 2002;
UPDATE sys_menu SET menu_name = '菜品管理'   WHERE menu_id = 2003;
UPDATE sys_menu SET menu_name = '菜品分类'   WHERE menu_id = 2004;
UPDATE sys_menu SET menu_name = '骑手管理'   WHERE menu_id = 2005;
UPDATE sys_menu SET menu_name = '配送调度'   WHERE menu_id = 2006;
UPDATE sys_menu SET menu_name = '用户管理'   WHERE menu_id = 2007;
UPDATE sys_menu SET menu_name = '优惠券管理' WHERE menu_id = 2008;
UPDATE sys_menu SET menu_name = '评价管理'   WHERE menu_id = 2009;
UPDATE sys_menu SET menu_name = '投诉管理'   WHERE menu_id = 2010;
UPDATE sys_menu SET menu_name = '销量统计'   WHERE menu_id = 2011;
UPDATE sys_menu SET menu_name = '运营统计'   WHERE menu_id = 2012;

-- 2.2 v2/v3 段(用 perms 锁)
UPDATE sys_menu SET menu_name = '订单查询'   WHERE perms = 'takeout:order:query';
UPDATE sys_menu SET menu_name = '订单新增'   WHERE perms = 'takeout:order:add';
UPDATE sys_menu SET menu_name = '订单修改'   WHERE perms = 'takeout:order:edit';
UPDATE sys_menu SET menu_name = '订单删除'   WHERE perms = 'takeout:order:remove';
UPDATE sys_menu SET menu_name = '订单导出'   WHERE perms = 'takeout:order:export';
UPDATE sys_menu SET menu_name = '改订单状态' WHERE perms = 'takeout:order:changeStatus';
UPDATE sys_menu SET menu_name = '取消订单'   WHERE perms = 'takeout:order:cancel';

UPDATE sys_menu SET menu_name = '商家查询'   WHERE perms = 'takeout:merchant:query';
UPDATE sys_menu SET menu_name = '商家新增'   WHERE perms = 'takeout:merchant:add';
UPDATE sys_menu SET menu_name = '商家修改'   WHERE perms = 'takeout:merchant:edit';
UPDATE sys_menu SET menu_name = '商家删除'   WHERE perms = 'takeout:merchant:remove';
UPDATE sys_menu SET menu_name = '修改状态'   WHERE perms = 'takeout:merchant:status';
UPDATE sys_menu SET menu_name = '商家审核'   WHERE perms = 'takeout:merchant:audit';
UPDATE sys_menu SET menu_name = '商家导出'   WHERE perms = 'takeout:merchant:export';

UPDATE sys_menu SET menu_name = '菜品查询'   WHERE perms = 'takeout:dish:query';
UPDATE sys_menu SET menu_name = '菜品新增'   WHERE perms = 'takeout:dish:add';
UPDATE sys_menu SET menu_name = '菜品修改'   WHERE perms = 'takeout:dish:edit';
UPDATE sys_menu SET menu_name = '菜品删除'   WHERE perms = 'takeout:dish:remove';
UPDATE sys_menu SET menu_name = '菜品导出'   WHERE perms = 'takeout:dish:export';

UPDATE sys_menu SET menu_name = '菜品分类查询' WHERE perms = 'takeout:dishCategory:query';
UPDATE sys_menu SET menu_name = '菜品分类新增' WHERE perms = 'takeout:dishCategory:add';
UPDATE sys_menu SET menu_name = '菜品分类修改' WHERE perms = 'takeout:dishCategory:edit';
UPDATE sys_menu SET menu_name = '菜品分类删除' WHERE perms = 'takeout:dishCategory:remove';
UPDATE sys_menu SET menu_name = '菜品分类导出' WHERE perms = 'takeout:dishCategory:export';

UPDATE sys_menu SET menu_name = '分类查询'   WHERE perms = 'takeout:category:query';
UPDATE sys_menu SET menu_name = '分类新增'   WHERE perms = 'takeout:category:add';
UPDATE sys_menu SET menu_name = '分类修改'   WHERE perms = 'takeout:category:edit';
UPDATE sys_menu SET menu_name = '分类删除'   WHERE perms = 'takeout:category:remove';
UPDATE sys_menu SET menu_name = '分类导出'   WHERE perms = 'takeout:category:export';

UPDATE sys_menu SET menu_name = '用户查询'   WHERE perms = 'takeout:user:query';
UPDATE sys_menu SET menu_name = '用户修改'   WHERE perms = 'takeout:user:edit';
UPDATE sys_menu SET menu_name = '用户删除'   WHERE perms = 'takeout:user:remove';
UPDATE sys_menu SET menu_name = '改用户角色' WHERE perms = 'takeout:user:changeRole';

UPDATE sys_menu SET menu_name = '派单查询'   WHERE perms = 'takeout:dispatch:query';
UPDATE sys_menu SET menu_name = '新增派单'   WHERE perms = 'takeout:dispatch:add';
UPDATE sys_menu SET menu_name = '删除派单'   WHERE perms = 'takeout:dispatch:remove';

UPDATE sys_menu SET menu_name = '骑手查询'   WHERE perms = 'takeout:rider:query';
UPDATE sys_menu SET menu_name = '骑手新增'   WHERE perms = 'takeout:rider:add';
UPDATE sys_menu SET menu_name = '骑手修改'   WHERE perms = 'takeout:rider:edit';
UPDATE sys_menu SET menu_name = '骑手删除'   WHERE perms = 'takeout:rider:remove';
UPDATE sys_menu SET menu_name = '修改状态'   WHERE perms = 'takeout:rider:status';
UPDATE sys_menu SET menu_name = '骑手抢单列表' WHERE perms = 'takeout:rider:grabList';
UPDATE sys_menu SET menu_name = '骑手抢单'   WHERE perms = 'takeout:rider:grab';

UPDATE sys_menu SET menu_name = '评价查询'   WHERE perms = 'takeout:rating:query';
UPDATE sys_menu SET menu_name = '评价回复'   WHERE perms = 'takeout:rating:reply';
UPDATE sys_menu SET menu_name = '评价删除'   WHERE perms = 'takeout:rating:remove';

UPDATE sys_menu SET menu_name = '券查询'     WHERE perms = 'takeout:coupon:query';
UPDATE sys_menu SET menu_name = '券新增'     WHERE perms = 'takeout:coupon:add';
UPDATE sys_menu SET menu_name = '券修改'     WHERE perms = 'takeout:coupon:edit';
UPDATE sys_menu SET menu_name = '券删除'     WHERE perms = 'takeout:coupon:remove';

UPDATE sys_menu SET menu_name = '工单查询'   WHERE perms = 'takeout:complaint:query';
UPDATE sys_menu SET menu_name = '处理工单'   WHERE perms = 'takeout:complaint:process';
UPDATE sys_menu SET menu_name = '工单删除'   WHERE perms = 'takeout:complaint:remove';

UPDATE sys_menu SET menu_name = '后厨接单'   WHERE perms = 'takeout:kitchen:accept';
UPDATE sys_menu SET menu_name = '后厨出餐'   WHERE perms = 'takeout:kitchen:ready';
UPDATE sys_menu SET menu_name = '后厨看板'   WHERE perms = 'takeout:kitchen:query';

UPDATE sys_menu SET menu_name = '堂食桌台'   WHERE perms = 'takeout:dineTable:list';
UPDATE sys_menu SET menu_name = '桌台查询'   WHERE perms = 'takeout:dineTable:query';
UPDATE sys_menu SET menu_name = '桌台新增'   WHERE perms = 'takeout:dineTable:add';
UPDATE sys_menu SET menu_name = '桌台修改'   WHERE perms = 'takeout:dineTable:edit';
UPDATE sys_menu SET menu_name = '桌台删除'   WHERE perms = 'takeout:dineTable:remove';

UPDATE sys_menu SET menu_name = '菜品销量榜' WHERE perms = 'takeout:dishSales:list' OR perms = 'takeout:dish:sales:view';

-- 2.3 路径型菜单(无 perms 的子菜单)
UPDATE sys_menu SET menu_name = '后厨订单' WHERE path = 'kitchen' AND component = 'takeout/kitchen/index';
UPDATE sys_menu SET menu_name = 'C端用户'  WHERE path = 'takeoutUser';

-- ============================================================
-- 阶段 3: 把 10000+ 段菜单挂到正确 parent
-- (不去改 menu_id 本身,免得破坏 sys_role_menu 引用)
-- ============================================================
-- 3.1 找出外卖业务顶级目录(2044),不存在则创建
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
VALUES (2044, '外卖业务', 0, 5, 'takeoutV2', NULL, 1, 0, 'M', '0', '0', NULL, 'shopping', 'admin', NOW(), '外卖业务 v2 顶级目录')
ON DUPLICATE KEY UPDATE menu_name = VALUES(menu_name);

-- 3.2 v2 子菜单(2090-2096)如不存在则建
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES
(2090, '派单管理',  2044, 1, 'dispatch',  'takeout/dispatch/index',  1, 0, 'C', '0', '0', 'takeout:dispatch:list',  'guide',   'admin', NOW(), ''),
(2091, '骑手管理',  2044, 2, 'rider',     'takeout/rider/index',     1, 0, 'C', '0', '0', 'takeout:rider:list',     'bicycle', 'admin', NOW(), ''),
(2092, 'C端用户',   2044, 3, 'takeoutUser','takeout/user/index',     1, 0, 'C', '0', '0', 'takeout:user:list',      'user',    'admin', NOW(), ''),
(2093, '评价管理',  2044, 4, 'rating',    'takeout/rating/index',    1, 0, 'C', '0', '0', 'takeout:rating:list',    'star',    'admin', NOW(), ''),
(2094, '优惠券管理',2044, 5, 'coupon',    'takeout/coupon/index',    1, 0, 'C', '0', '0', 'takeout:coupon:list',    'money',   'admin', NOW(), ''),
(2095, '投诉管理',  2044, 6, 'complaint', 'takeout/complaint/index', 1, 0, 'C', '0', '0', 'takeout:complaint:list', 'message', 'admin', NOW(), ''),
(2096, '数据统计',  2044, 7, 'statistics','takeout/statistics/index',1, 0, 'C', '0', '0', 'takeout:statistics:list','chart',   'admin', NOW(), '')
ON DUPLICATE KEY UPDATE menu_name = VALUES(menu_name);

-- 3.3 把 10000+ 段那批按钮挂到对应 v2 父菜单(只挂,不改 menu_id)
UPDATE sys_menu SET parent_id = 2090 WHERE perms IN ('takeout:dispatch:query','takeout:dispatch:add','takeout:dispatch:remove') AND parent_id NOT IN (2090, 0);
UPDATE sys_menu SET parent_id = 2091 WHERE perms IN ('takeout:rider:query','takeout:rider:add','takeout:rider:edit','takeout:rider:remove','takeout:rider:status') AND parent_id NOT IN (2091, 0);
UPDATE sys_menu SET parent_id = 2092 WHERE perms IN ('takeout:user:query','takeout:user:edit','takeout:user:remove') AND parent_id NOT IN (2092, 0);
UPDATE sys_menu SET parent_id = 2093 WHERE perms IN ('takeout:rating:query','takeout:rating:reply','takeout:rating:remove') AND parent_id NOT IN (2093, 0);
UPDATE sys_menu SET parent_id = 2094 WHERE perms IN ('takeout:coupon:query','takeout:coupon:add','takeout:coupon:edit','takeout:coupon:remove') AND parent_id NOT IN (2094, 0);
UPDATE sys_menu SET parent_id = 2095 WHERE perms IN ('takeout:complaint:query','takeout:complaint:process','takeout:complaint:remove') AND parent_id NOT IN (2095, 0);
UPDATE sys_menu SET parent_id = 2007 WHERE perms = 'takeout:user:changeRole' AND parent_id <> 2007;
UPDATE sys_menu SET parent_id = 2000 WHERE perms IN ('takeout:kitchen:accept','takeout:kitchen:ready','takeout:kitchen:query','takeout:rider:grabList','takeout:rider:grab') AND parent_id <> 2000;
UPDATE sys_menu SET parent_id = 2000 WHERE path = 'kitchen' AND component = 'takeout/kitchen/index' AND parent_id <> 2000;
UPDATE sys_menu SET parent_id = 2000 WHERE path = 'kitchen' AND component IS NULL AND parent_id <> 2000;

-- 3.4 堂食桌台:先确保 10041 的 parent 是 2044(没创建 2044 子目录,直接当 2044 的子)
UPDATE sys_menu SET parent_id = 2044 WHERE menu_id = 10041 AND parent_id <> 2044;

-- 3.5 dishSales 2011:已经在 2000 下,保留

-- ============================================================
-- 阶段 4: 角色授权(覆盖式)
-- ============================================================
-- 4.1 admin (role_id=1) 拿全部 takeout 菜单
DELETE FROM sys_role_menu
 WHERE role_id = 1
   AND menu_id IN (SELECT menu_id FROM sys_menu
                    WHERE (menu_id BETWEEN 2000 AND 2200)
                       OR (perms LIKE 'takeout:%'));
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 1, m.menu_id FROM sys_menu m
WHERE (m.menu_id BETWEEN 2000 AND 2200)
   OR (m.perms LIKE 'takeout:%');

-- 4.2 商家角色(可能 role_id=100 或 101,看实际 sys_role)
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT r.role_id, m.menu_id FROM sys_role r
CROSS JOIN sys_menu m
WHERE r.role_key IN ('merchant','shop')
  AND (
    (m.menu_id BETWEEN 2000 AND 2200)
    OR (m.perms LIKE 'takeout:merchant%')
    OR (m.perms LIKE 'takeout:dish%')
    OR (m.perms LIKE 'takeout:dishCategory%')
    OR (m.perms LIKE 'takeout:category%')
    OR (m.perms LIKE 'takeout:order%')
    OR (m.perms LIKE 'takeout:rating%')
    OR (m.perms LIKE 'takeout:complaint%')
    OR (m.perms LIKE 'takeout:dispatch%')
    OR (m.perms LIKE 'takeout:statistics%')
    OR (m.perms LIKE 'takeout:dishSales%')
  )
ON DUPLICATE KEY UPDATE sys_role_menu.role_id = sys_role_menu.role_id;

-- 4.3 后厨角色
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT r.role_id, m.menu_id FROM sys_role r
CROSS JOIN sys_menu m
WHERE r.role_key = 'kitchen'
  AND (m.perms IN ('takeout:kitchen:list','takeout:kitchen:accept',
                   'takeout:kitchen:ready','takeout:kitchen:query')
       OR m.path = 'kitchen')
ON DUPLICATE KEY UPDATE sys_role_menu.role_id = sys_role_menu.role_id;

-- 4.4 骑手角色
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT r.role_id, m.menu_id FROM sys_role r
CROSS JOIN sys_menu m
WHERE r.role_key = 'rider'
  AND (m.perms IN ('takeout:rider:grabList','takeout:rider:grab')
       OR m.path = 'rider')
ON DUPLICATE KEY UPDATE sys_role_menu.role_id = sys_role_menu.role_id;

-- 4.5 普通管理员(common role_key)
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT r.role_id, m.menu_id FROM sys_role r
CROSS JOIN sys_menu m
WHERE r.role_key = 'common'
  AND ((m.menu_id BETWEEN 2000 AND 2200)
       OR (m.perms LIKE 'takeout:%'))
ON DUPLICATE KEY UPDATE sys_role_menu.role_id = sys_role_menu.role_id;

-- ============================================================
-- 阶段 5: 补 takeout_dish_category(目前 0 行,菜品分类菜单空挂)
-- 注: 当前 takeout_dish_category 表无 merchant_id 字段(早期 schema),
--     不加这列,只填名字即可
-- ============================================================
INSERT IGNORE INTO takeout_dish_category (category_name, sort_order, status, create_by, create_time, remark)
SELECT '热销套餐', 1, '1', 'admin', NOW(), 'demo'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM takeout_dish_category WHERE category_name='热销套餐');
INSERT IGNORE INTO takeout_dish_category (category_name, sort_order, status, create_by, create_time, remark)
SELECT '主食', 2, '1', 'admin', NOW(), 'demo'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM takeout_dish_category WHERE category_name='主食');
INSERT IGNORE INTO takeout_dish_category (category_name, sort_order, status, create_by, create_time, remark)
SELECT '小食', 3, '1', 'admin', NOW(), 'demo'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM takeout_dish_category WHERE category_name='小食');
INSERT IGNORE INTO takeout_dish_category (category_name, sort_order, status, create_by, create_time, remark)
SELECT '饮品', 4, '1', 'admin', NOW(), 'demo'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM takeout_dish_category WHERE category_name='饮品');

-- 现有菜品 category_id 已经有 1/2/3, 没问题(FK 已存在); 把 0 改成 1
UPDATE takeout_dish SET category_id = 1 WHERE category_id = 0 OR category_id IS NULL;

-- 修正 sys_user / sys_role 名称乱码(顺手)
UPDATE sys_user SET nick_name = '超级管理员' WHERE user_id = 1;
UPDATE sys_role SET role_name = '超级管理员' WHERE role_id = 1;
UPDATE sys_role SET role_name = '普通角色'   WHERE role_id = 2;

SET FOREIGN_KEY_CHECKS = 1;

-- ============================================================
-- 阶段 6: 验证
-- ============================================================
SELECT 'A. 菜单总数' AS phase, COUNT(*) AS value
  FROM sys_menu
  WHERE (menu_id BETWEEN 2000 AND 2200) OR (perms LIKE 'takeout:%')
UNION ALL
SELECT 'B. admin 授权数', COUNT(*)
  FROM sys_role_menu rm
  JOIN sys_menu m ON m.menu_id = rm.menu_id
  WHERE rm.role_id = 1
    AND ((m.menu_id BETWEEN 2000 AND 2200) OR (m.perms LIKE 'takeout:%'))
UNION ALL
SELECT 'C. v1 菜单(2000-2012)名字正常',
       SUM(CASE WHEN CHAR_LENGTH(menu_name) >= 2 AND menu_name NOT LIKE '%?%' THEN 1 ELSE 0 END)
  FROM sys_menu
  WHERE menu_id BETWEEN 2000 AND 2012
UNION ALL
SELECT 'D. takeout_dish_category 行数', COUNT(*) FROM takeout_dish_category
UNION ALL
SELECT 'E. 字典 takeout_order_status_v2 项数',
       (SELECT COUNT(*) FROM sys_dict_data WHERE dict_type='takeout_order_status_v2')
UNION ALL
SELECT 'F. duplicate perms(应=0)',
       (SELECT COUNT(*) FROM (
          SELECT perms FROM sys_menu WHERE perms LIKE 'takeout:%' GROUP BY perms HAVING COUNT(*) > 1
        ) x);
