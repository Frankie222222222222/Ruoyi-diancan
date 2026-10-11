-- =============================================================
--  takeout_address.sql — C 端用户收货地址簿
--  2026-10-11 智能点餐 v2.1 阶段 4 后台补齐
-- =============================================================
DROP TABLE IF EXISTS `takeout_address`;
CREATE TABLE `takeout_address` (
  `address_id`    BIGINT       NOT NULL AUTO_INCREMENT       COMMENT '地址ID',
  `user_id`       BIGINT       NOT NULL                      COMMENT '用户ID',
  `name`          VARCHAR(32)  NOT NULL                      COMMENT '收货人',
  `phone`         VARCHAR(20)  NOT NULL                      COMMENT '手机号',
  `address`       VARCHAR(255) NOT NULL                      COMMENT '详细地址',
  `house_number`  VARCHAR(32)           DEFAULT NULL         COMMENT '门牌号',
  `lat`           DECIMAL(10,6)         DEFAULT NULL         COMMENT '纬度',
  `lng`           DECIMAL(10,6)         DEFAULT NULL         COMMENT '经度',
  `is_default`    CHAR(1)              DEFAULT '0'            COMMENT '是否默认(0否 1是)',
  `remark`        VARCHAR(255)          DEFAULT NULL         COMMENT '备注',
  `del_flag`      CHAR(1)              DEFAULT '0'            COMMENT '删除标志(0存在 2删除)',
  `create_time`   DATETIME              DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time`   DATETIME              DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`address_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_del_flag` (`del_flag`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='外卖用户收货地址';

-- 同步 sys_menu
INSERT INTO `sys_menu` (`menu_name`, `parent_id`, `order_num`, `path`, `component`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `remark`)
VALUES ('收货地址', 0, 5, 'address', 'takeout/address/index', 1, 0, 'C', '0', '0', 'takeout:address:list', '#', 'admin', NOW(), '外卖收货地址管理')
ON DUPLICATE KEY UPDATE update_time = NOW();

-- 取刚插入的菜单 id
SET @addr_menu_id = LAST_INSERT_ID();

INSERT INTO `sys_menu` (`menu_name`, `parent_id`, `order_num`, `path`, `component`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `remark`)
SELECT '地址查询', @addr_menu_id, 1, '#', '', 1, 0, 'F', '0', '0', 'takeout:address:query', '#', 'admin', NOW(), '' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'takeout:address:query');

INSERT INTO `sys_menu` (`menu_name`, `parent_id`, `order_num`, `path`, `component`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `remark`)
SELECT '地址编辑', @addr_menu_id, 2, '#', '', 1, 0, 'F', '0', '0', 'takeout:address:edit', '#', 'admin', NOW(), '' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'takeout:address:edit');

INSERT INTO `sys_menu` (`menu_name`, `parent_id`, `order_num`, `path`, `component`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `remark`)
SELECT '地址删除', @addr_menu_id, 3, '#', '', 1, 0, 'F', '0', '0', 'takeout:address:remove', '#', 'admin', NOW(), '' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'takeout:address:remove');

-- admin 角色授权
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`)
SELECT 1, menu_id FROM sys_menu WHERE perms IN ('takeout:address:list', 'takeout:address:query', 'takeout:address:edit', 'takeout:address:remove')
ON DUPLICATE KEY UPDATE role_id = role_id;
