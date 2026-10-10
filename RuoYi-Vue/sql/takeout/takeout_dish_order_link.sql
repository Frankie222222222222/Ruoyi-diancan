-- =============================================================
-- 外卖业务 · 订单-菜品联动
-- 1) 历史数据修复：把 takeout_dish.sales 从已完成订单回填
--    （之前的代码没联动，新增订单不增加 sales，所以销量都是 0）
-- 2) 销量榜菜单：takeout:dish:sales:view 权限 + 路由
-- =============================================================
SET NAMES utf8mb4;

-- ============= 1) 历史销量回填 =============
-- 把所有「已送达(4) + 已完成(5)」订单的明细销量累加到菜品 sales
-- 只回填 sales=0 的菜品，避免覆盖已正确累加的数据
UPDATE takeout_dish d
SET d.sales = (
    SELECT COALESCE(SUM(ti.quantity), 0)
    FROM takeout_order_item ti
    JOIN takeout_order o ON o.order_id = ti.order_id
    WHERE ti.dish_id = d.dish_id
      AND o.del_flag = '0'
      AND o.status IN ('4', '5')
)
WHERE d.sales = 0
  AND EXISTS (
    SELECT 1 FROM takeout_order_item ti
    JOIN takeout_order o ON o.order_id = ti.order_id
    WHERE ti.dish_id = d.dish_id
      AND o.del_flag = '0'
      AND o.status IN ('4', '5')
);

-- 注：历史库存不再回填（无法准确还原已发货/已收货的差异）
--     如确需回填库存，可参考：
--   stock = initial_stock - SUM(quantity) for all non-cancelled orders
--   但需要先有「初始库存」字段，建议跳过本步骤，前台展示时给提示

-- ============= 2) 销量榜菜单（路由 + 按钮权限） =============

-- 2.1 父菜单
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT '菜品销量榜', 2044, 9, 'dishSales', 'takeout/dishSales/index', 1, 0, 'C', '0', '0', 'takeout:dish:list', 'trendCharts', 'admin', NOW(), '外卖业务·销量榜'
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_name = '菜品销量榜' AND parent_id = 2044);

-- 2.2 拿到刚插入的菜单 ID
SET @sales_menu_id = (SELECT menu_id FROM sys_menu WHERE menu_name = '菜品销量榜' AND parent_id = 2044 LIMIT 1);

-- 2.3 关联到 admin 角色（id=1）和商家角色（视实际表里的角色 id 而定）
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (1, @sales_menu_id);
-- 如有商家角色（通常 menu_name='商家'），可执行：
-- INSERT IGNORE INTO sys_role_menu (role_id, menu_id) SELECT role_id, @sales_menu_id FROM sys_role WHERE role_name = '商家';

-- =============================================================
-- 完毕
-- =============================================================
