-- =============================================================
-- 外卖业务 · 商家管理 · 菜单 SQL
-- 在 sys_menu 里追加 9 行（外卖目录 2000 / 商家菜单 2001 / 按钮 2010-2016）
-- 与官方菜单 id 段（1~117, 1000~1057）完全隔离
-- 依赖前置：sys_menu 表已存在（由 ry_20260417.sql 创建）
-- 执行顺序：先 ry_20260417.sql，再本文件
-- =============================================================

-- ----------------------------
-- 目录：外卖管理（parent_id=0）
-- ----------------------------
insert into sys_menu values('2000', '外卖管理', '0', '5', 'takeout', null, '', '', 1, 0, 'M', '0', '0', '', 'shopping', 'admin', sysdate(), '', null, '外卖管理目录');

-- ----------------------------
-- 菜单：商家管理（parent_id=2000）
-- ----------------------------
insert into sys_menu values('2001', '商家管理', '2000', '1', 'merchant', 'takeout/merchant/index', '', '', 1, 0, 'C', '0', '0', 'takeout:merchant:list', 'shop', 'admin', sysdate(), '', null, '商家管理菜单');

-- ----------------------------
-- 按钮：商家管理下的 7 个功能权限（parent_id=2001）
-- ----------------------------
insert into sys_menu values('2010', '商家查询', '2001', '1', '#', '', '', '', 1, 0, 'F', '0', '0', 'takeout:merchant:query',  '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2011', '商家新增', '2001', '2', '#', '', '', '', 1, 0, 'F', '0', '0', 'takeout:merchant:add',    '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2012', '商家修改', '2001', '3', '#', '', '', '', 1, 0, 'F', '0', '0', 'takeout:merchant:edit',   '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2013', '商家删除', '2001', '4', '#', '', '', '', 1, 0, 'F', '0', '0', 'takeout:merchant:remove', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2014', '商家导出', '2001', '5', '#', '', '', '', 1, 0, 'F', '0', '0', 'takeout:merchant:export', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2015', '修改状态', '2001', '6', '#', '', '', '', 1, 0, 'F', '0', '0', 'takeout:merchant:status', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2016', '商家审核', '2001', '7', '#', '', '', '', 1, 0, 'F', '0', '0', 'takeout:merchant:audit',  '#', 'admin', sysdate(), '', null, '');