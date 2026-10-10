-- ============================================================
-- 外卖模块关联打通 - 数据迁移脚本
-- 创建时间: 2026-10-10
-- 描述:    补齐商家/菜品/订单关联所需的数据完整性约束
-- ============================================================

-- ---------- 0. 前置检查（迁移前请先跑） ----------

-- 0.1 检查 order_no 重复
SELECT order_no, COUNT(*) AS cnt
FROM takeout_order
GROUP BY order_no
HAVING cnt > 1;
-- 预期：0 行

-- 0.2 检查 (merchant_id, dish_name) 重复（菜品同店去重前置）
SELECT merchant_id, dish_name, COUNT(*) AS cnt
FROM takeout_dish
GROUP BY merchant_id, dish_name
HAVING cnt > 1;
-- 预期：0 行

-- ============================================================
-- 1. order_no 唯一约束（幂等防重）
-- ============================================================
-- 说明：经检查，本实例已存在 uk_order_no 唯一索引（SHOW INDEX FROM takeout_order）
-- 故本段为幂等保护 —— 已存在则忽略，不会重复添加。

ALTER TABLE takeout_order
    ADD UNIQUE INDEX uk_order_no (order_no);
-- 若已存在：ERROR 1061 (42000): Duplicate key name 'uk_order_no' —— 可忽略

-- ============================================================
-- 2. 业务说明
-- ============================================================
-- 2.1 菜品同店去重（不在本次 P0 范围）：
--       ALTER TABLE takeout_dish
--           ADD UNIQUE INDEX uk_merchant_dish (merchant_id, dish_name);
-- 2.2 分类加 merchant_id（不在本次 P0 范围）：
--       ALTER TABLE takeout_dish_category
--           ADD COLUMN merchant_id BIGINT DEFAULT NULL COMMENT '所属商家ID(NULL=平台公共)';
--       ALTER TABLE takeout_dish_category
--           ADD UNIQUE INDEX uk_merchant_name (merchant_id, category_name);

-- ============================================================
-- 回滚段（按需执行）
-- ============================================================
-- ALTER TABLE takeout_order DROP INDEX uk_order_no;
