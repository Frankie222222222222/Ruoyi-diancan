-- =====================================================
-- 菜品分类表 + 菜单权限（UTF-8 完整版）
-- =====================================================
-- 使用前请先选择数据库：USE ry-vue;

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- -----------------------------------------------------
-- 1. 菜品分类表
-- -----------------------------------------------------
DROP TABLE IF EXISTS `takeout_category`;
CREATE TABLE `takeout_category` (
  `category_id`   BIGINT       NOT NULL AUTO_INCREMENT COMMENT '分类ID',
  `merchant_id`   BIGINT       NOT NULL COMMENT '所属商家ID',
  `category_name` VARCHAR(50)  NOT NULL COMMENT '分类名称',
  `sort_order`    INT          DEFAULT 0 COMMENT '排序',
  `status`        CHAR(1)      DEFAULT '0' COMMENT '状态(0启用 1停用)',
  `create_by`     VARCHAR(64)  DEFAULT '' COMMENT '创建者',
  `create_time`   DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by`     VARCHAR(64)  DEFAULT '' COMMENT '更新者',
  `update_time`   DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `remark`        VARCHAR(500) DEFAULT NULL COMMENT '备注',
  `del_flag`      CHAR(1)      DEFAULT '0' COMMENT '删除标志(0存在 2删除)',
  PRIMARY KEY (`category_id`) USING BTREE,
  KEY `idx_merchant` (`merchant_id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='菜品分类';

-- -----------------------------------------------------
-- 2. 菜单权限（确保父菜单存在）
-- -----------------------------------------------------

-- 2.1 父菜单：外卖管理（如果已存在则跳过）
INSERT INTO `sys_menu` (`menu_name`, `parent_id`, `order_num`, `path`, `component`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `remark`)
SELECT '外卖管理', 0, 5, 'takeout', null, 1, 0, 'M', '0', '0', null, 'shopping', 'admin', NOW(), '外卖管理目录'
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `sys_menu` WHERE `menu_name` = '外卖管理' AND `parent_id` = 0);

-- 2.2 父菜单 ID
SET @parentId = (SELECT `menu_id` FROM `sys_menu` WHERE `menu_name` = '外卖管理' AND `parent_id` = 0 LIMIT 1);

-- 2.3 商家管理（先确认存在，给菜品分类排序使用）
INSERT INTO `sys_menu` (`menu_name`, `parent_id`, `order_num`, `path`, `component`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `remark`)
SELECT '商家管理', @parentId, 1, 'merchant', 'takeout/merchant/index', 1, 0, 'C', '0', '0', 'takeout:merchant:list', 'peoples', 'admin', NOW(), '商家管理菜单'
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `sys_menu` WHERE `menu_name` = '商家管理' AND `parent_id` = @parentId);

-- 2.4 菜品分类
INSERT INTO `sys_menu` (`menu_name`, `parent_id`, `order_num`, `path`, `component`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `remark`)
SELECT '菜品分类', @parentId, 2, 'category', 'takeout/category/index', 1, 0, 'C', '0', '0', 'takeout:category:list', 'list', 'admin', NOW(), '菜品分类菜单'
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `sys_menu` WHERE `menu_name` = '菜品分类' AND `parent_id` = @parentId);

SET @catId = (SELECT `menu_id` FROM `sys_menu` WHERE `menu_name` = '菜品分类' AND `parent_id` = @parentId LIMIT 1);

-- 2.5 菜品分类按钮权限
INSERT INTO `sys_menu` (`menu_name`, `parent_id`, `order_num`, `path`, `component`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `remark`)
SELECT '分类查询', @catId, 1, '#', '', 1, 0, 'F', '0', '0', 'takeout:category:query', '#', 'admin', NOW(), ''
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `sys_menu` WHERE `perms` = 'takeout:category:query');

INSERT INTO `sys_menu` (`menu_name`, `parent_id`, `order_num`, `path`, `component`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `remark`)
SELECT '分类新增', @catId, 2, '#', '', 1, 0, 'F', '0', '0', 'takeout:category:add', '#', 'admin', NOW(), ''
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `sys_menu` WHERE `perms` = 'takeout:category:add');

INSERT INTO `sys_menu` (`menu_name`, `parent_id`, `order_num`, `path`, `component`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `remark`)
SELECT '分类修改', @catId, 3, '#', '', 1, 0, 'F', '0', '0', 'takeout:category:edit', '#', 'admin', NOW(), ''
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `sys_menu` WHERE `perms` = 'takeout:category:edit');

INSERT INTO `sys_menu` (`menu_name`, `parent_id`, `order_num`, `path`, `component`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `remark`)
SELECT '分类删除', @catId, 4, '#', '', 1, 0, 'F', '0', '0', 'takeout:category:remove', '#', 'admin', NOW(), ''
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `sys_menu` WHERE `perms` = 'takeout:category:remove');

INSERT INTO `sys_menu` (`menu_name`, `parent_id`, `order_num`, `path`, `component`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `remark`)
SELECT '分类导出', @catId, 5, '#', '', 1, 0, 'F', '0', '0', 'takeout:category:export', '#', 'admin', NOW(), ''
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `sys_menu` WHERE `perms` = 'takeout:category:export');

SET FOREIGN_KEY_CHECKS = 1;

-- =====================================================
-- 验证
-- =====================================================
SELECT m1.menu_id AS '外卖管理ID', m1.menu_name AS '父菜单'
FROM `sys_menu` m1 WHERE m1.menu_name = '外卖管理' AND m1.parent_id = 0;

SELECT m2.menu_id AS '菜品分类ID', m2.menu_name AS '子菜单', m2.parent_id AS '父ID', m2.perms
FROM `sys_menu` m2 WHERE m2.menu_name = '菜品分类';
