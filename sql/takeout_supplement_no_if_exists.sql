-- ============================================================
-- 外卖系统 - 补齐新字段（兼容老版本 MySQL，5.7 / 8.0 通用）
-- 不使用 IF NOT EXISTS 语法
-- 重复执行会报错但不影响数据（先查后改逻辑见末尾）
-- ============================================================

USE `ry-vue`;

-- 1. 订单表新增 4 个字段
ALTER TABLE `takeout_order` ADD COLUMN `discount_amount` DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '优惠金额' AFTER `delivery_fee`;
ALTER TABLE `takeout_order` ADD COLUMN `actual_amount`   DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '实付金额' AFTER `discount_amount`;
ALTER TABLE `takeout_order` ADD COLUMN `coupon_user_id`  BIGINT(20)              DEFAULT NULL COMMENT '用券记录ID' AFTER `actual_amount`;
ALTER TABLE `takeout_order` ADD COLUMN `pay_time`        DATETIME                DEFAULT NULL COMMENT '支付时间' AFTER `delivery_time`;
ALTER TABLE `takeout_order` ADD KEY  `idx_coupon_user`  (`coupon_user_id`);

-- 2. 菜品表新增 merchant_id
ALTER TABLE `takeout_dish` ADD COLUMN `merchant_id` BIGINT(20) NOT NULL DEFAULT 0 COMMENT '商家ID' AFTER `category_id`;
ALTER TABLE `takeout_dish` ADD KEY  `idx_merchant` (`merchant_id`);

-- 3. 派单表新增骑手位置
ALTER TABLE `takeout_dispatch` ADD COLUMN `rider_lng`            DECIMAL(10,6) DEFAULT NULL COMMENT '骑手经度' AFTER `actual_distance`;
ALTER TABLE `takeout_dispatch` ADD COLUMN `rider_lat`            DECIMAL(10,6) DEFAULT NULL COMMENT '骑手纬度' AFTER `rider_lng`;
ALTER TABLE `takeout_dispatch` ADD COLUMN `location_update_time` DATETIME       DEFAULT NULL COMMENT '位置上报时间' AFTER `rider_lat`;
ALTER TABLE `takeout_dispatch` ADD KEY  `idx_rider_location` (`rider_lng`, `rider_lat`);

-- ============================================================
-- 如果你已经跑过带 IF NOT EXISTS 失败的脚本, 字段可能已部分创建
-- 重复跑上面 ALTER 会报 "Duplicate column name" 错误, 可忽略
-- 或者用下方 SQL 检查哪些字段已存在:
-- ============================================================
/*
SELECT TABLE_NAME, COLUMN_NAME
FROM information_schema.COLUMNS
WHERE TABLE_SCHEMA = 'ry-vue'
  AND TABLE_NAME IN ('takeout_order','takeout_dish','takeout_dispatch')
  AND COLUMN_NAME IN ('discount_amount','actual_amount','coupon_user_id','pay_time',
                      'merchant_id','rider_lng','rider_lat','location_update_time')
ORDER BY TABLE_NAME, COLUMN_NAME;
*/
