-- =============================================================
-- 堂食扫码点餐 - 数据迁移脚本
-- 创建时间: 2026-10-10
-- 描述:
--   1) 新增桌台表 takeout_dine_table
--   2) takeout_order 加列 order_type / table_id
--   3) 字典 takeout_order_status 加 DRAFT(0a) 堂食专用
--   4) 字典 takeout_table_status 桌台状态
-- =============================================================
SET NAMES utf8mb4;

-- ============================================================
-- 1. 桌台表
-- ============================================================
DROP TABLE IF EXISTS takeout_dine_table;
CREATE TABLE takeout_dine_table (
    table_id      BIGINT(20)    NOT NULL AUTO_INCREMENT       COMMENT '桌台ID',
    merchant_id   BIGINT(20)    NOT NULL                      COMMENT '所属商家ID',
    table_no      VARCHAR(20)   NOT NULL                      COMMENT '桌号(A12 / VIP-3)',
    capacity      INT           DEFAULT 4                     COMMENT '容纳人数',
    status        CHAR(1)       DEFAULT '0'                   COMMENT '桌台状态(0空闲 1就餐中 2已结)',
    qr_url        VARCHAR(255)  DEFAULT NULL                  COMMENT '二维码图片URL(或模板)',
    remark        VARCHAR(255)  DEFAULT NULL                  COMMENT '备注',
    del_flag      CHAR(1)       DEFAULT '0'                   COMMENT '删除标志(0存在 2删除)',
    create_by     VARCHAR(64)   DEFAULT NULL                  COMMENT '创建者',
    create_time   DATETIME      DEFAULT NULL                  COMMENT '创建时间',
    update_by     VARCHAR(64)   DEFAULT NULL                  COMMENT '更新者',
    update_time   DATETIME      DEFAULT NULL                  COMMENT '更新时间',
    PRIMARY KEY (table_id),
    UNIQUE KEY uk_merchant_tableno (merchant_id, table_no, del_flag),
    KEY idx_merchant (merchant_id),
    KEY idx_status (status)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COMMENT='堂食桌台表';

-- ============================================================
-- 2. takeout_order 加列
-- ============================================================
-- 2.1 order_type: 0=外卖 1=堂食(默认0,兼容已有数据)
ALTER TABLE takeout_order
    ADD COLUMN order_type TINYINT NOT NULL DEFAULT 0 COMMENT '订单类型(0外卖 1堂食)' AFTER pay_status,
    ADD COLUMN table_id BIGINT(20) DEFAULT NULL COMMENT '堂食桌台ID(NULL=外卖)' AFTER order_type,
    ADD INDEX idx_order_type (order_type),
    ADD INDEX idx_table (table_id);

-- ============================================================
-- 3. 字典: 堂食订单状态
-- ============================================================
-- DRAFT(0a) 堂食专用 · 顾客点菜中未支付
-- 已有 0/1/2/2a/2b/3/4/5/6/7
INSERT INTO sys_dict_data (dict_type, dict_label, dict_value, dict_sort, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 'takeout_order_status', '堂食点菜中', '0a', 0, NULL, 'default', 'N', '0', 'admin', NOW(), '堂食专用·顾客点菜中未支付'
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type = 'takeout_order_status' AND dict_value = '0a');

-- ============================================================
-- 4. 字典: 桌台状态
-- ============================================================
INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark)
SELECT '桌台状态', 'takeout_table_status', '0', 'admin', NOW(), '堂食·桌台状态'
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_type WHERE dict_type = 'takeout_table_status');

INSERT INTO sys_dict_data (dict_type, dict_label, dict_value, dict_sort, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 'takeout_table_status', '空闲', '0', 1, NULL, 'success', 'Y', '0', 'admin', NOW(), '桌台空闲'
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type = 'takeout_table_status' AND dict_value = '0');

INSERT INTO sys_dict_data (dict_type, dict_label, dict_value, dict_sort, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 'takeout_table_status', '就餐中', '1', 2, NULL, 'warning', 'N', '0', 'admin', NOW(), '桌台就餐中'
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type = 'takeout_table_status' AND dict_value = '1');

INSERT INTO sys_dict_data (dict_type, dict_label, dict_value, dict_sort, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 'takeout_table_status', '已结', '2', 3, NULL, 'info', 'N', '0', 'admin', NOW(), '桌台已结账'
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type = 'takeout_table_status' AND dict_value = '2');

-- ============================================================
-- 5. 菜单: 堂食桌台管理
-- ============================================================
-- 假设外卖根菜单 parent_id = 2044 (参考 takeout_dish_order_link.sql 实际值)
-- 5.1 父菜单
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT '堂食桌台', 2044, 10, 'dineTable', 'takeout/dineTable/index', 1, 0, 'C', '0', '0', 'takeout:dineTable:list', 'table', 'admin', NOW(), '外卖业务·堂食桌台管理'
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_name = '堂食桌台' AND parent_id = 2044);

SET @dine_table_menu_id = (SELECT menu_id FROM sys_menu WHERE menu_name = '堂食桌台' AND parent_id = 2044 LIMIT 1);

-- 5.2 关联到 admin 角色
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (1, @dine_table_menu_id);

-- 5.3 按钮权限（增删改查）
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT '桌台查询', @dine_table_menu_id, 1, '', '', 1, 0, 'F', '0', '0', 'takeout:dineTable:query', '#', 'admin', NOW(), ''
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_name = '桌台查询' AND parent_id = @dine_table_menu_id);

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT '桌台新增', @dine_table_menu_id, 2, '', '', 1, 0, 'F', '0', '0', 'takeout:dineTable:add', '#', 'admin', NOW(), ''
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_name = '桌台新增' AND parent_id = @dine_table_menu_id);

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT '桌台修改', @dine_table_menu_id, 3, '', '', 1, 0, 'F', '0', '0', 'takeout:dineTable:edit', '#', 'admin', NOW(), ''
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_name = '桌台修改' AND parent_id = @dine_table_menu_id);

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT '桌台删除', @dine_table_menu_id, 4, '', '', 1, 0, 'F', '0', '0', 'takeout:dineTable:remove', '#', 'admin', NOW(), ''
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_name = '桌台删除' AND parent_id = @dine_table_menu_id);

-- 5.4 把这些按钮权限也绑给 admin
INSERT IGNORE INTO sys_role_menu (role_id, menu_id)
SELECT 1, menu_id FROM sys_menu
WHERE parent_id = @dine_table_menu_id AND menu_type = 'F';

-- ============================================================
-- 6. 回滚（按需执行）
-- ============================================================
-- DROP TABLE IF EXISTS takeout_dine_table;
-- ALTER TABLE takeout_order DROP COLUMN order_type, DROP COLUMN table_id;
-- DELETE FROM sys_dict_data WHERE dict_type = 'takeout_order_status' AND dict_value = '0a';
-- DELETE FROM sys_dict_data WHERE dict_type = 'takeout_table_status';
-- DELETE FROM sys_dict_type WHERE dict_type = 'takeout_table_status';
-- DELETE FROM sys_menu WHERE menu_name = '堂食桌台';
