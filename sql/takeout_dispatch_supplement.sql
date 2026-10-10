-- ============================================================
-- 外卖系统 - 补充字段（派单表）
-- 数据库: ry-vue
-- 执行时机: 在 takeout_order.sql / takeout_dish.sql 之后
-- 兼容 MySQL 8.0.29+ （IF NOT EXISTS）
-- 老版本请删除 IF NOT EXISTS 后手动执行
-- ============================================================

USE `ry-vue`;

-- 派单表新增: 骑手实时经纬度 + 位置上报时间
ALTER TABLE `takeout_dispatch`
    ADD COLUMN IF NOT EXISTS `rider_lng`            decimal(10,6) DEFAULT NULL COMMENT '骑手实时经度(GCJ-02)' AFTER `actual_distance`,
    ADD COLUMN IF NOT EXISTS `rider_lat`            decimal(10,6) DEFAULT NULL COMMENT '骑手实时纬度(GCJ-02)' AFTER `rider_lng`,
    ADD COLUMN IF NOT EXISTS `location_update_time` datetime       DEFAULT NULL COMMENT '骑手位置上报时间' AFTER `rider_lat`,
    ADD KEY        IF NOT EXISTS `idx_rider_location` (`rider_lng`, `rider_lat`);
