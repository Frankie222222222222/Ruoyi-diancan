-- ============================================================
-- ��������ϵͳ - ��Ʒ����
-- ��: ry-vue
-- ��: takeout_dish_category, takeout_dish
-- ============================================================

USE `ry-vue`;

-- 1) ��Ʒ����
DROP TABLE IF EXISTS `takeout_dish_category`;
CREATE TABLE `takeout_dish_category` (
  `category_id`  bigint(20)  NOT NULL AUTO_INCREMENT       COMMENT '��������',
  `category_name` varchar(50) NOT NULL                    COMMENT '��������',
  `sort_order`   int(4)      NOT NULL DEFAULT 0           COMMENT '��ʾ˳��',
  `status`       char(1)     NOT NULL DEFAULT '1'          COMMENT '״̬ 0ͣ�� 1����',
  `remark`       varchar(500)         DEFAULT NULL         COMMENT '��ע',
  `create_by`    varchar(64)          DEFAULT ''           COMMENT '������',
  `create_time`  datetime             DEFAULT NULL         COMMENT '����ʱ��',
  `update_by`    varchar(64)          DEFAULT ''           COMMENT '������',
  `update_time`  datetime             DEFAULT NULL         COMMENT '����ʱ��',
  PRIMARY KEY (`category_id`),
  UNIQUE KEY `uk_category_name` (`category_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='��Ʒ����';


-- 2) ��Ʒ
DROP TABLE IF EXISTS `takeout_dish`;
CREATE TABLE `takeout_dish` (
  `dish_id`      bigint(20)   NOT NULL AUTO_INCREMENT      COMMENT '��Ʒ����',
  `dish_name`    varchar(100) NOT NULL                    COMMENT '��Ʒ����',
  `category_id`  bigint(20)   NOT NULL                    COMMENT '��������ID',
  `image`        varchar(255)          DEFAULT ''          COMMENT '��ƷͼƬURL',
  `price`        decimal(10,2) NOT NULL DEFAULT 0.00       COMMENT '�ۼ�(Ԫ)',
  `stock`        int(11)      NOT NULL DEFAULT 0           COMMENT '���',
  `sales`        int(11)      NOT NULL DEFAULT 0           COMMENT '����',
  `status`       char(1)      NOT NULL DEFAULT '1'         COMMENT '״̬ 0�¼� 1�ϼ�',
  `description`  varchar(500)          DEFAULT NULL        COMMENT '����',
  `remark`       varchar(500)          DEFAULT NULL        COMMENT '��ע',
  `create_by`    varchar(64)           DEFAULT ''          COMMENT '������',
  `create_time`  datetime              DEFAULT NULL        COMMENT '����ʱ��',
  `update_by`    varchar(64)           DEFAULT ''          COMMENT '������',
  `update_time`  datetime              DEFAULT NULL        COMMENT '����ʱ��',
  PRIMARY KEY (`dish_id`),
  UNIQUE KEY `uk_category_dishname` (`category_id`, `dish_name`),
  KEY `idx_category` (`category_id`),
  KEY `idx_status`   (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='��Ʒ';


-- ============================================================
-- �˵� SQL: �������� / ��Ʒ���� / ��Ʒ���� / ��Ʒ�б�
-- ע: parent_id ����ʵ�� sys_menu, ���Ȳ����ֵ
-- ============================================================

-- (0) �������˵�"��������": ������������; �����Ȱ�"������"����
--    ʵ�ʲ���ʱ���� select * from sys_menu where menu_name='��������'
--    ���Ѵ���, �� (1) �� parent_id ��Ϊ�� menu_id
SELECT @parentId := MAX(menu_id) + 1 FROM sys_menu WHERE menu_name = '��������';
SELECT @grandId  := MAX(menu_id) + 1 FROM sys_menu;

-- 0. ���˵�"��������"
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
VALUES('��������', 0, 5, 'takeout', null, 1, 0, 'M', '0', '1', '', 'shopping', 'admin', NOW(), '����ϵͳĿ¼');

SELECT @menuId1 := LAST_INSERT_ID();

-- 1. ���˵�"��Ʒ����"
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
VALUES('��Ʒ����', @menuId1, 2, 'dish', null, 1, 0, 'M', '0', '1', '', 'dish', 'admin', NOW(), '��Ʒ����Ŀ¼');

SELECT @menuId2 := LAST_INSERT_ID();

-- 2. �Ӳ˵�"��Ʒ����"
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
VALUES('��Ʒ����', @menuId2, 1, 'dishCategory', 'takeout/dishCategory/index', 1, 0, 'C', '0', '1', 'takeout:dishCategory:list', 'category', 'admin', NOW(), '��Ʒ����˵�');

SELECT @menuId3 := LAST_INSERT_ID();

-- 3. �Ӳ˵�"��Ʒ�б�"
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
VALUES('��Ʒ�б�', @menuId2, 2, 'dish', 'takeout/dish/index', 1, 0, 'C', '0', '1', 'takeout:dish:list', 'food', 'admin', NOW(), '��Ʒ�б��˵�');

SELECT @menuId4 := LAST_INSERT_ID();


-- 4. ��Ʒ���ఴť (4 ��)
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
VALUES('�����ѯ', @menuId3, 1, '#', '', 1, 0, 'F', '0', '1', 'takeout:dishCategory:query', '#', 'admin', NOW(), '');
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
VALUES('��������', @menuId3, 2, '#', '', 1, 0, 'F', '0', '1', 'takeout:dishCategory:add',   '#', 'admin', NOW(), '');
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
VALUES('�����޸�', @menuId3, 3, '#', '', 1, 0, 'F', '0', '1', 'takeout:dishCategory:edit',  '#', 'admin', NOW(), '');
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
VALUES('����ɾ��', @menuId3, 4, '#', '', 1, 0, 'F', '0', '1', 'takeout:dishCategory:remove','#', 'admin', NOW(), '');

-- 5. ��Ʒ��ť (4 ��)
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
VALUES('��Ʒ��ѯ', @menuId4, 1, '#', '', 1, 0, 'F', '0', '1', 'takeout:dish:query',  '#', 'admin', NOW(), '');
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
VALUES('��Ʒ����', @menuId4, 2, '#', '', 1, 0, 'F', '0', '1', 'takeout:dish:add',    '#', 'admin', NOW(), '');
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
VALUES('��Ʒ�޸�', @menuId4, 3, '#', '', 1, 0, 'F', '0', '1', 'takeout:dish:edit',   '#', 'admin', NOW(), '');
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
VALUES('��Ʒɾ��', @menuId4, 4, '#', '', 1, 0, 'F', '0', '1', 'takeout:dish:remove', '#', 'admin', NOW(), '');

-- ============================================================
-- 补充字段（按用户推荐补齐逻辑外键）
-- 菜品表新增: merchant_id（菜品所属商家，避免多商家菜品串数据）
-- ============================================================

-- 先放宽分类+名称唯一约束, 改为商家+分类+名称唯一
ALTER TABLE `takeout_dish` DROP INDEX `uk_category_dishname`;
ALTER TABLE `takeout_dish` ADD UNIQUE KEY `uk_merchant_cat_dishname` (`merchant_id`, `category_id`, `dish_name`);

ALTER TABLE `takeout_dish` ADD COLUMN IF NOT EXISTS `merchant_id` bigint(20) NOT NULL DEFAULT 0 COMMENT '商家ID(菜品所属商家)' AFTER `category_id`;
ALTER TABLE `takeout_dish` ADD KEY IF NOT EXISTS `idx_merchant` (`merchant_id`);

