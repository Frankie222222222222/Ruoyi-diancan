-- ============================================================
-- 外卖系统 - 订单管理
-- 数据库: ry-vue
-- 表: takeout_order, takeout_order_item
-- ============================================================

USE `ry-vue`;

-- ------------------------------------------------------------
-- 1) 订单主表
-- ------------------------------------------------------------
DROP TABLE IF EXISTS `takeout_order`;
CREATE TABLE `takeout_order` (
  `order_id`        bigint(20)     NOT NULL AUTO_INCREMENT      COMMENT '订单ID',
  `order_no`        varchar(64)    NOT NULL                    COMMENT '订单号(唯一)',
  `user_id`         bigint(20)              DEFAULT NULL        COMMENT '下单用户ID',
  `merchant_id`     bigint(20)     NOT NULL                    COMMENT '商家ID',
  `total_amount`    decimal(10,2)  NOT NULL DEFAULT 0.00       COMMENT '订单总金额',
  `delivery_fee`    decimal(10,2)  NOT NULL DEFAULT 0.00       COMMENT '配送费',
  `status`          char(1)        NOT NULL DEFAULT '0'         COMMENT '订单状态 0待支付 1已支付 2商家接单 3配送中 4已送达 5已完成 6已取消 7已退款',
  `pay_status`      char(1)        NOT NULL DEFAULT '0'         COMMENT '支付状态 0未支付 1已支付',
  `pay_method`      varchar(20)             DEFAULT NULL        COMMENT '支付方式',
  `receiver_name`   varchar(50)             DEFAULT NULL        COMMENT '收货人',
  `receiver_phone`  varchar(20)             DEFAULT NULL        COMMENT '收货人电话',
  `address`         varchar(255)            DEFAULT NULL        COMMENT '收货地址',
  `remark`          varchar(500)            DEFAULT NULL        COMMENT '用户备注',
  `delivery_time`   datetime                DEFAULT NULL        COMMENT '期望送达时间',
  `complete_time`   datetime                DEFAULT NULL        COMMENT '完成时间',
  `cancel_reason`   varchar(255)            DEFAULT NULL        COMMENT '取消原因',
  `del_flag`        char(1)        NOT NULL DEFAULT '0'         COMMENT '删除标志 0正常 2删除',
  `create_by`       varchar(64)             DEFAULT ''           COMMENT '创建者',
  `create_time`     datetime                DEFAULT NULL        COMMENT '创建时间',
  `update_by`       varchar(64)             DEFAULT ''           COMMENT '更新者',
  `update_time`     datetime                DEFAULT NULL        COMMENT '更新时间',
  PRIMARY KEY (`order_id`),
  UNIQUE KEY `uk_order_no`      (`order_no`),
  KEY `idx_merchant`            (`merchant_id`),
  KEY `idx_user`                (`user_id`),
  KEY `idx_status`              (`status`),
  KEY `idx_pay_status`          (`pay_status`),
  KEY `idx_create_time`         (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单主表';


-- ------------------------------------------------------------
-- 2) 订单商品明细表
-- ------------------------------------------------------------
DROP TABLE IF EXISTS `takeout_order_item`;
CREATE TABLE `takeout_order_item` (
  `item_id`     bigint(20)     NOT NULL AUTO_INCREMENT      COMMENT '明细ID',
  `order_id`    bigint(20)     NOT NULL                    COMMENT '订单ID',
  `dish_id`     bigint(20)     NOT NULL                    COMMENT '菜品ID',
  `dish_name`   varchar(100)   NOT NULL                    COMMENT '菜品名称(冗余)',
  `dish_image`  varchar(255)            DEFAULT ''          COMMENT '菜品图片(冗余)',
  `price`       decimal(10,2)  NOT NULL DEFAULT 0.00       COMMENT '下单单价',
  `quantity`    int(11)        NOT NULL DEFAULT 1           COMMENT '数量',
  `subtotal`    decimal(10,2)  NOT NULL DEFAULT 0.00       COMMENT '小计金额',
  PRIMARY KEY (`item_id`),
  KEY `idx_order` (`order_id`),
  KEY `idx_dish`  (`dish_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单商品明细';


-- ============================================================
-- 字典 (order status)
-- ============================================================
INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark)
SELECT '订单状态', 'takeout_order_status', '0', 'admin', NOW(), '订单状态字典'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_dict_type WHERE dict_type = 'takeout_order_status');

INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 1, '待支付', '0', 'takeout_order_status', '', 'info',  'N', '0', 'admin', NOW(), '' FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type='takeout_order_status' AND dict_value='0')
UNION ALL SELECT 2, '已支付', '1', 'takeout_order_status', '', 'primary','N', '0', 'admin', NOW(), '' FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type='takeout_order_status' AND dict_value='1')
UNION ALL SELECT 3, '商家接单', '2', 'takeout_order_status', '', 'warning','N', '0', 'admin', NOW(), '' FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type='takeout_order_status' AND dict_value='2')
UNION ALL SELECT 4, '配送中', '3', 'takeout_order_status', '', 'warning','N', '0', 'admin', NOW(), '' FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type='takeout_order_status' AND dict_value='3')
UNION ALL SELECT 5, '已送达', '4', 'takeout_order_status', '', 'success','N', '0', 'admin', NOW(), '' FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type='takeout_order_status' AND dict_value='4')
UNION ALL SELECT 6, '已完成', '5', 'takeout_order_status', '', 'success','N', '0', 'admin', NOW(), '' FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type='takeout_order_status' AND dict_value='5')
UNION ALL SELECT 7, '已取消', '6', 'takeout_order_status', '', 'danger', 'N', '0', 'admin', NOW(), '' FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type='takeout_order_status' AND dict_value='6')
UNION ALL SELECT 8, '已退款', '7', 'takeout_order_status', '', 'danger', 'N', '0', 'admin', NOW(), '' FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type='takeout_order_status' AND dict_value='7');


-- ============================================================
-- 菜单 SQL: 订单管理 (挂在"外卖管理"目录下)
-- 先查"外卖管理"菜单的 id 作为父菜单, 如果还没建外卖管理, 这里需先执行 takeout_dish.sql
-- ============================================================
SELECT @parentTakeoutId := menu_id FROM sys_menu WHERE menu_name = '外卖管理' AND parent_id = 0;

-- 订单管理子菜单
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT '订单管理', @parentTakeoutId, 3, 'order', 'takeout/order/index', 1, 0, 'C', '0', '1', 'takeout:order:list', 'list', 'admin', NOW(), '订单管理菜单'
FROM DUAL WHERE @parentTakeoutId IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_name = '订单管理' AND parent_id = @parentTakeoutId);

SELECT @menuId := menu_id FROM sys_menu WHERE menu_name = '订单管理' AND parent_id = @parentTakeoutId;

-- 订单管理按钮 (6 个: 查询 / 修改 / 删除 / 改状态 / 取消 / 导出)
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT '订单查询',     @menuId, 1, '#', '', 1, 0, 'F', '0', '1', 'takeout:order:query',        '#', 'admin', NOW(), '' FROM DUAL WHERE @menuId IS NOT NULL;
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT '订单新增',     @menuId, 2, '#', '', 1, 0, 'F', '0', '1', 'takeout:order:add',          '#', 'admin', NOW(), '' FROM DUAL WHERE @menuId IS NOT NULL;
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT '订单修改',     @menuId, 3, '#', '', 1, 0, 'F', '0', '1', 'takeout:order:edit',         '#', 'admin', NOW(), '' FROM DUAL WHERE @menuId IS NOT NULL;
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT '订单删除',     @menuId, 4, '#', '', 1, 0, 'F', '0', '1', 'takeout:order:remove',       '#', 'admin', NOW(), '' FROM DUAL WHERE @menuId IS NOT NULL;
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT '改订单状态',   @menuId, 5, '#', '', 1, 0, 'F', '0', '1', 'takeout:order:changeStatus', '#', 'admin', NOW(), '' FROM DUAL WHERE @menuId IS NOT NULL;
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT '取消订单',     @menuId, 6, '#', '', 1, 0, 'F', '0', '1', 'takeout:order:cancel',       '#', 'admin', NOW(), '' FROM DUAL WHERE @menuId IS NOT NULL;
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT '订单导出',     @menuId, 7, '#', '', 1, 0, 'F', '0', '1', 'takeout:order:export',       '#', 'admin', NOW(), '' FROM DUAL WHERE @menuId IS NOT NULL;

-- ============================================================
-- 补充字段（按用户推荐补齐逻辑外键）
-- 执行时机: 在 takeout_order.sql 主脚本之后
-- 说明: ALTER + IF NOT EXISTS 兼容 MySQL 8.0.29+; 老库可手动 IGNORE
-- ============================================================

-- 订单表新增: 优惠金额 / 实付金额 / 优惠券领取记录ID / 支付时间
ALTER TABLE `takeout_order` ADD COLUMN IF NOT EXISTS `discount_amount` decimal(10,2) NOT NULL DEFAULT 0.00 COMMENT '优惠金额' AFTER `delivery_fee`;
ALTER TABLE `takeout_order` ADD COLUMN IF NOT EXISTS `actual_amount`   decimal(10,2) NOT NULL DEFAULT 0.00 COMMENT '实付金额' AFTER `discount_amount`;
ALTER TABLE `takeout_order` ADD COLUMN IF NOT EXISTS `coupon_user_id`  bigint(20)              DEFAULT NULL COMMENT '用券记录ID(关联takeout_coupon_user.id)' AFTER `actual_amount`;
ALTER TABLE `takeout_order` ADD COLUMN IF NOT EXISTS `pay_time`        datetime                DEFAULT NULL COMMENT '支付时间' AFTER `delivery_time`;
ALTER TABLE `takeout_order` ADD KEY IF NOT EXISTS `idx_coupon_user` (`coupon_user_id`);
