-- ============================================================
-- 修复 takeout 模块的菜单中文名
-- 注意：原 SQL 用 GBK 写入导致乱码，需 UPDATE 修正
-- ============================================================

USE `ry-vue`;

-- 假设菜单结构是：
-- 外卖管理 (parent)
--   ├── 菜品管理
--   │   ├── 菜品分类  (path=dishCategory)
--   │   └── 菜品列表  (path=dish)
--   └── 商家管理  (如果有)
--       └── 商家列表

-- 1. 修正一级菜单
UPDATE sys_menu SET menu_name = '外卖管理' WHERE menu_name LIKE '%取送%' OR menu_name LIKE '%外卖%' AND parent_id = 0;
-- 兜底：用 path 定位
UPDATE sys_menu SET menu_name = '外卖管理' WHERE path = 'takeout' AND parent_id = 0;

-- 2. 修正二级"菜品管理"
UPDATE sys_menu SET menu_name = '菜品管理' WHERE path = 'dish' AND parent_id IN (SELECT menu_id FROM (SELECT menu_id FROM sys_menu WHERE path='takeout' AND parent_id=0) t);

-- 3. 修正三级菜单（按 path 定位）
UPDATE sys_menu SET menu_name = '菜品分类' WHERE path = 'dishCategory' AND component LIKE 'takeout/dishCategory%';
UPDATE sys_menu SET menu_name = '菜品列表' WHERE path = 'dish' AND component LIKE 'takeout/dish/index%';

-- 4. 修正按钮名（按 perms 定位，因为按钮 menu_name 也是乱码）
UPDATE sys_menu SET menu_name = '分类查询' WHERE perms = 'takeout:dishCategory:query';
UPDATE sys_menu SET menu_name = '分类新增' WHERE perms = 'takeout:dishCategory:add';
UPDATE sys_menu SET menu_name = '分类修改' WHERE perms = 'takeout:dishCategory:edit';
UPDATE sys_menu SET menu_name = '分类删除' WHERE perms = 'takeout:dishCategory:remove';

UPDATE sys_menu SET menu_name = '菜品查询' WHERE perms = 'takeout:dish:query';
UPDATE sys_menu SET menu_name = '菜品新增' WHERE perms = 'takeout:dish:add';
UPDATE sys_menu SET menu_name = '菜品修改' WHERE perms = 'takeout:dish:edit';
UPDATE sys_menu SET menu_name = '菜品删除' WHERE perms = 'takeout:dish:remove';

-- 5. 修正 remark
UPDATE sys_menu SET remark = '外卖系统目录' WHERE path = 'takeout' AND parent_id = 0;
UPDATE sys_menu SET remark = '菜品管理目录' WHERE path = 'dish' AND component IS NULL AND parent_id IN (SELECT menu_id FROM (SELECT menu_id FROM sys_menu WHERE path='takeout' AND parent_id=0) t);
UPDATE sys_menu SET remark = '菜品分类菜单' WHERE path = 'dishCategory' AND component LIKE 'takeout/dishCategory%';
UPDATE sys_menu SET remark = '菜品列表菜单' WHERE path = 'dish' AND component LIKE 'takeout/dish/index%';

-- 6. 验证：查看结果
SELECT menu_id, parent_id, menu_name, path, component, perms FROM sys_menu WHERE path IN ('takeout','dish','dishCategory') OR perms LIKE 'takeout:%' ORDER BY parent_id, order_num;
