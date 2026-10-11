-- ============================================================
-- 修复: takeout_order 缺少 merchant_name 列
-- 原因: TakeoutDispatchMapper.selectDispatchList / TakeoutOrderVo
--       引用了 o.merchant_name,但历史 takeout_order.sql 没建该列
-- 策略: 兼容 MySQL 8.0.29+ 的 IF NOT EXISTS;老库手动去掉 IF NOT EXISTS
-- 不破坏现有数据,只 ADD COLUMN
-- ============================================================

USE `ry-vue`;

ALTER TABLE `takeout_order`
    ADD COLUMN IF NOT EXISTS `merchant_name` varchar(100) DEFAULT NULL
        COMMENT '商家名称(冗余,加速查询/展示,避免每次 join 商家表)'
        AFTER `merchant_id`;

-- 兼容历史数据:从商家表回填 merchant_name(若商家表存在)
UPDATE `takeout_order` o
LEFT JOIN `takeout_merchant` m ON o.merchant_id = m.merchant_id
SET o.merchant_name = m.merchant_name
WHERE o.merchant_name IS NULL
  AND m.merchant_name IS NOT NULL;

-- 验证
SELECT COUNT(*) AS total, SUM(merchant_name IS NOT NULL) AS filled
FROM `takeout_order`;
