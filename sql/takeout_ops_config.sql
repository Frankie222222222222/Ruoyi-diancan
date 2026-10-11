-- =============================================================
--  takeout_ops_config.sql — 运营 4 张表 + 菜单/权限 seed
--  2026-10-11 智能点餐 v2.1 阶段 5
--  已被 TakeoutBannerController / TakeoutAnnouncementController / TakeoutHelpController 使用
-- =============================================================

DROP TABLE IF EXISTS `takeout_banner`;
CREATE TABLE `takeout_banner` (
  `banner_id`   BIGINT       NOT NULL AUTO_INCREMENT       COMMENT 'BannerID',
  `title`       VARCHAR(128) NOT NULL                      COMMENT '标题',
  `image`       VARCHAR(255) NOT NULL                      COMMENT '图片URL',
  `link`        VARCHAR(255)          DEFAULT NULL         COMMENT '跳转链接',
  `sort`        INT                  DEFAULT 0              COMMENT '排序(小到大)',
  `status`      CHAR(1)              DEFAULT '0'            COMMENT '状态(0启用 1禁用)',
  `del_flag`    CHAR(1)              DEFAULT '0'            COMMENT '删除标志(0存在 2删除)',
  `create_time` DATETIME              DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME              DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`banner_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT ='运营Banner';

DROP TABLE IF EXISTS `takeout_announcement`;
CREATE TABLE `takeout_announcement` (
  `announcement_id` BIGINT       NOT NULL AUTO_INCREMENT,
  `title`           VARCHAR(128) NOT NULL,
  `content`         TEXT         NOT NULL,
  `status`          CHAR(1)      DEFAULT '0' COMMENT '0启用 1禁用',
  `del_flag`        CHAR(1)      DEFAULT '0',
  `create_time`     DATETIME     DEFAULT CURRENT_TIMESTAMP,
  `update_time`     DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`announcement_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT ='平台公告';

DROP TABLE IF EXISTS `takeout_help_category`;
CREATE TABLE `takeout_help_category` (
  `category_id` BIGINT       NOT NULL AUTO_INCREMENT,
  `name`        VARCHAR(64)  NOT NULL,
  `sort`        INT          DEFAULT 0,
  `del_flag`    CHAR(1)      DEFAULT '0',
  `create_time` DATETIME     DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`category_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT ='帮助分类';

DROP TABLE IF EXISTS `takeout_help_article`;
CREATE TABLE `takeout_help_article` (
  `article_id`  BIGINT       NOT NULL AUTO_INCREMENT,
  `category_id` BIGINT       NOT NULL,
  `title`       VARCHAR(128) NOT NULL,
  `content`     TEXT         NOT NULL,
  `sort`        INT          DEFAULT 0,
  `del_flag`    CHAR(1)      DEFAULT '0',
  `create_time` DATETIME     DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`article_id`),
  KEY `idx_category` (`category_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT ='帮助文章';

-- =============================================================
-- sys_menu 挂载 4 个新父菜单
-- =============================================================
INSERT INTO `sys_menu` (`menu_name`, `parent_id`, `order_num`, `path`, `component`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `remark`)
VALUES ('运营管理', 0, 7, 'ops', 'Layout', 1, 0, 'M', '0', '0', '', 'operation', 'admin', NOW(), '运营内容管理目录')
ON DUPLICATE KEY UPDATE update_time = NOW();

SET @ops_menu_id = LAST_INSERT_ID();

-- Banner
INSERT INTO `sys_menu` (`menu_name`, `parent_id`, `order_num`, `path`, `component`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `remark`)
VALUES ('Banner管理', @ops_menu_id, 1, 'banner', 'takeout/ops/banner', 1, 0, 'C', '0', '0', 'takeout:banner:list', 'picture', 'admin', NOW(), '')
ON DUPLICATE KEY UPDATE update_time = NOW();

SET @banner_menu_id = LAST_INSERT_ID();
INSERT INTO `sys_menu` (`menu_name`, `parent_id`, `order_num`, `path`, `component`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `remark`)
SELECT 'Banner查询', @banner_menu_id, 1, '#', '', 1, 0, 'F', '0', '0', 'takeout:banner:query', '#', 'admin', NOW(), '' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'takeout:banner:query');
INSERT INTO `sys_menu` (`menu_name`, `parent_id`, `order_num`, `path`, `component`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `remark`)
SELECT 'Banner编辑', @banner_menu_id, 2, '#', '', 1, 0, 'F', '0', '0', 'takeout:banner:edit', '#', 'admin', NOW(), '' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'takeout:banner:edit');
INSERT INTO `sys_menu` (`menu_name`, `parent_id`, `order_num`, `path`, `component`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `remark`)
SELECT 'Banner删除', @banner_menu_id, 3, '#', '', 1, 0, 'F', '0', '0', 'takeout:banner:remove', '#', 'admin', NOW(), '' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'takeout:banner:remove');

-- 公告
INSERT INTO `sys_menu` (`menu_name`, `parent_id`, `order_num`, `path`, `component`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `remark`)
VALUES ('公告管理', @ops_menu_id, 2, 'announcement', 'takeout/ops/announcement', 1, 0, 'C', '0', '0', 'takeout:announcement:list', 'bell', 'admin', NOW(), '')
ON DUPLICATE KEY UPDATE update_time = NOW();
SET @ann_menu_id = LAST_INSERT_ID();
INSERT INTO `sys_menu` (`menu_name`, `parent_id`, `order_num`, `path`, `component`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `remark`)
SELECT '公告编辑', @ann_menu_id, 1, '#', '', 1, 0, 'F', '0', '0', 'takeout:announcement:edit', '#', 'admin', NOW(), '' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'takeout:announcement:edit');
INSERT INTO `sys_menu` (`menu_name`, `parent_id`, `order_num`, `path`, `component`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `remark`)
SELECT '公告删除', @ann_menu_id, 2, '#', '', 1, 0, 'F', '0', '0', 'takeout:announcement:remove', '#', 'admin', NOW(), '' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'takeout:announcement:remove');

-- 帮助中心
INSERT INTO `sys_menu` (`menu_name`, `parent_id`, `order_num`, `path`, `component`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `remark`)
VALUES ('帮助中心', @ops_menu_id, 3, 'help', 'takeout/ops/help', 1, 0, 'C', '0', '0', 'takeout:help:list', 'document', 'admin', NOW(), '')
ON DUPLICATE KEY UPDATE update_time = NOW();
SET @help_menu_id = LAST_INSERT_ID();
INSERT INTO `sys_menu` (`menu_name`, `parent_id`, `order_num`, `path`, `component`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `remark`)
SELECT '帮助编辑', @help_menu_id, 1, '#', '', 1, 0, 'F', '0', '0', 'takeout:help:edit', '#', 'admin', NOW(), '' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'takeout:help:edit');
INSERT INTO `sys_menu` (`menu_name`, `parent_id`, `order_num`, `path`, `component`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `remark`)
SELECT '帮助删除', @help_menu_id, 2, '#', '', 1, 0, 'F', '0', '0', 'takeout:help:remove', '#', 'admin', NOW(), '' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'takeout:help:remove');

-- Kitchen 后台看板
INSERT INTO `sys_menu` (`menu_name`, `parent_id`, `order_num`, `path`, `component`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `remark`)
VALUES ('后厨看板', 0, 6, 'kitchen', 'takeout/kitchen/index', 1, 0, 'C', '0', '0', 'takeout:kitchen:list', 'food', 'admin', NOW(), '')
ON DUPLICATE KEY UPDATE update_time = NOW();
SET @kitchen_menu_id = LAST_INSERT_ID();
INSERT INTO `sys_menu` (`menu_name`, `parent_id`, `order_num`, `path`, `component`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `remark`)
SELECT '后厨接单', @kitchen_menu_id, 1, '#', '', 1, 0, 'F', '0', '0', 'takeout:kitchen:accept', '#', 'admin', NOW(), '' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'takeout:kitchen:accept');
INSERT INTO `sys_menu` (`menu_name`, `parent_id`, `order_num`, `path`, `component`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `remark`)
SELECT '后厨出餐', @kitchen_menu_id, 2, '#', '', 1, 0, 'F', '0', '0', 'takeout:kitchen:ready', '#', 'admin', NOW(), '' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'takeout:kitchen:ready');

-- 退款
INSERT INTO `sys_menu` (`menu_name`, `parent_id`, `order_num`, `path`, `component`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `remark`)
VALUES ('退款管理', 0, 5, 'refund', 'takeout/refund/index', 1, 0, 'C', '0', '0', 'takeout:refund:list', 'refresh', 'admin', NOW(), '')
ON DUPLICATE KEY UPDATE update_time = NOW();
SET @refund_menu_id = LAST_INSERT_ID();
INSERT INTO `sys_menu` (`menu_name`, `parent_id`, `order_num`, `path`, `component`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `remark`)
SELECT '退款查询', @refund_menu_id, 1, '#', '', 1, 0, 'F', '0', '0', 'takeout:refund:query', '#', 'admin', NOW(), '' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'takeout:refund:query');
INSERT INTO `sys_menu` (`menu_name`, `parent_id`, `order_num`, `path`, `component`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `remark`)
SELECT '退款处理', @refund_menu_id, 2, '#', '', 1, 0, 'F', '0', '0', 'takeout:refund:process', '#', 'admin', NOW(), '' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'takeout:refund:process');

-- 支付流水
INSERT INTO `sys_menu` (`menu_name`, `parent_id`, `order_num`, `path`, `component`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `remark`)
VALUES ('支付流水', 0, 5, 'payment', 'takeout/payment/index', 1, 0, 'C', '0', '0', 'takeout:payment:list', 'money', 'admin', NOW(), '')
ON DUPLICATE KEY UPDATE update_time = NOW();

-- 骑手位置
INSERT INTO `sys_menu` (`menu_name`, `parent_id`, `order_num`, `path`, `component`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `remark`)
VALUES ('骑手位置', 0, 5, 'riderLocation', 'takeout/riderLocation/index', 1, 0, 'C', '0', '0', 'takeout:riderLocation:list', 'location', 'admin', NOW(), '')
ON DUPLICATE KEY UPDATE update_time = NOW();

-- 收货地址
INSERT INTO `sys_menu` (`menu_name`, `parent_id`, `order_num`, `path`, `component`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `remark`)
VALUES ('收货地址', 0, 5, 'address', 'takeout/address/index', 1, 0, 'C', '0', '0', 'takeout:address:list', 'map-location', 'admin', NOW(), '')
ON DUPLICATE KEY UPDATE update_time = NOW();

-- admin 角色授权(新加的权限)
INSERT IGNORE INTO `sys_role_menu` (`role_id`, `menu_id`)
SELECT 1, menu_id FROM sys_menu
WHERE perms IN (
  'takeout:banner:list','takeout:banner:query','takeout:banner:edit','takeout:banner:remove',
  'takeout:announcement:list','takeout:announcement:edit','takeout:announcement:remove',
  'takeout:help:list','takeout:help:edit','takeout:help:remove',
  'takeout:kitchen:list','takeout:kitchen:accept','takeout:kitchen:ready',
  'takeout:refund:list','takeout:refund:query','takeout:refund:process',
  'takeout:payment:list','takeout:riderLocation:list','takeout:address:list','takeout:address:query','takeout:address:edit','takeout:address:remove'
);
