-- =====================================================
-- 菜品分类表 + 菜单权限（UTF-8 修正版）
-- =====================================================
-- 使用前请先选择数据库：USE ry-vue;
-- 注意：旧的 takeout_category 表会被删除（如果存在）

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- -----------------------------------------------------
-- 0. 清理旧表（takeout_category）
-- -----------------------------------------------------
DROP TABLE IF EXISTS `takeout_category`;

-- -----------------------------------------------------
-- 1. 菜品分类表 takeout_dish_category
-- -----------------------------------------------------
DROP TABLE IF EXISTS `takeout_dish_category`;
CREATE TABLE `takeout_dish_category` (
  `category_id`   BIGINT       NOT NULL AUTO_INCREMENT COMMENT '分类ID',
  `category_name` VARCHAR(50)  NOT NULL COMMENT '分类名称',
  `sort_order`    INT          DEFAULT 0 COMMENT '显示顺序',
  `status`        CHAR(1)      DEFAULT '1' COMMENT '状态 0停用 1启用',
  `remark`        VARCHAR(500) DEFAULT NULL COMMENT '备注',
  `create_by`     VARCHAR(64)  DEFAULT '' COMMENT '创建者',
  `create_time`   DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by`     VARCHAR(64)  DEFAULT '' COMMENT '更新者',
  `update_time`   DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`category_id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='菜品分类';

-- -----------------------------------------------------
-- 2. 菜单权限
-- -----------------------------------------------------

-- 2.1 父菜单：外卖管理（已存在则跳过）
INSERT INTO `sys_menu` (`menu_name`, `parent_id`, `order_num`, `path`, `component`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `remark`)
SELECT '外卖管理', 0, 5, 'takeout', null, 1, 0, 'M', '0', '0', null, 'shopping', 'admin', NOW(), '外卖管理目录'
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `sys_menu` WHERE `menu_name` = '外卖管理' AND `parent_id` = 0);

SET @parentId = (SELECT `menu_id` FROM `sys_menu` WHERE `menu_name` = '外卖管理' AND `parent_id` = 0 LIMIT 1);

-- 2.2 清理旧的"菜品分类"子菜单（如果之前用过 takeout:category:* 命名）
DELETE FROM `sys_menu` WHERE `perms` IN (
  'takeout:category:list','takeout:category:query','takeout:category:add',
  'takeout:category:edit','takeout:category:remove','takeout:category:export'
);

-- 2.3 菜品分类菜单
INSERT INTO `sys_menu` (`menu_name`, `parent_id`, `order_num`, `path`, `component`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `remark`)
SELECT '菜品分类', @parentId, 1, 'dishCategory', 'takeout/dishCategory/index', 1, 0, 'C', '0', '0', 'takeout:dishCategory:list', 'list', 'admin', NOW(), '菜品分类菜单'
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `sys_menu` WHERE `menu_name` = '菜品分类' AND `parent_id` = @parentId);

SET @catId = (SELECT `menu_id` FROM `sys_menu` WHERE `menu_name` = '菜品分类' AND `parent_id` = @parentId LIMIT 1);

-- 2.4 按钮权限
INSERT INTO `sys_menu` (`menu_name`, `parent_id`, `order_num`, `path`, `component`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `remark`)
SELECT '分类查询', @catId, 1, '#', '', 1, 0, 'F', '0', '0', 'takeout:dishCategory:query', '#', 'admin', NOW(), ''
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `sys_menu` WHERE `perms` = 'takeout:dishCategory:query');

INSERT INTO `sys_menu` (`menu_name`, `parent_id`, `order_num`, `path`, `component`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `remark`)
SELECT '分类新增', @catId, 2, '#', '', 1, 0, 'F', '0', '0', 'takeout:dishCategory:add', '#', 'admin', NOW(), ''
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `sys_menu` WHERE `perms` = 'takeout:dishCategory:add');

INSERT INTO `sys_menu` (`menu_name`, `parent_id`, `order_num`, `path`, `component`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `remark`)
SELECT '分类修改', @catId, 3, '#', '', 1, 0, 'F', '0', '0', 'takeout:dishCategory:edit', '#', 'admin', NOW(), ''
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `sys_menu` WHERE `perms` = 'takeout:dishCategory:edit');

INSERT INTO `sys_menu` (`menu_name`, `parent_id`, `order_num`, `path`, `component`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `remark`)
SELECT '分类删除', @catId, 4, '#', '', 1, 0, 'F', '0', '0', 'takeout:dishCategory:remove', '#', 'admin', NOW(), ''
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `sys_menu` WHERE `perms` = 'takeout:dishCategory:remove');

SET FOREIGN_KEY_CHECKS = 1;

-- =====================================================
-- 验证
-- =====================================================
SELECT category_id, category_name, sort_order, status FROM takeout_dish_category;

SELECT m1.menu_id AS parentId, m1.menu_name AS 父菜单 FROM sys_menu m1 WHERE m1.menu_name = '外卖管理' AND m1.parent_id = 0;
SELECT m2.menu_id, m2.menu_name, m2.perms FROM sys_menu m2 WHERE m2.parent_id = (SELECT menu_id FROM sys_menu WHERE menu_name='菜品分类' LIMIT 1);
