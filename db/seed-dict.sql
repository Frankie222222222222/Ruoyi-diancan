-- e:\ruoyi-vue3\db\seed-dict.sql
-- 字典兜底(各业务 sql 已内嵌 INSERT IGNORE,这里只补缺)
USE ry-vue;
SET NAMES utf8mb4;

-- 兜底字典类型
INSERT IGNORE INTO sys_dict_type (dict_id, dict_name, dict_type, status, create_by, create_time, remark) VALUES
(200, '订单状态(v2)',  'takeout_order_status_v2', '0', 'admin', NOW(), '订单状态枚举 v2'),
(201, '外卖用户角色',   'takeout_user_role',       '0', 'admin', NOW(), 'user/kitchen/rider/admin'),
(202, '桌台状态',      'takeout_table_status',     '0', 'admin', NOW(), '0空闲 1就餐中 2已结');

-- 兜底字典数据(其他 sql 已内嵌 INSERT IGNORE,此处基本 noop)
SELECT 'seed-dict OK' AS result;
