-- =============================================================
-- 外卖业务 · 商家管理 · 字典 SQL
-- 在 sys_dict_type / sys_dict_data 追加：营业状态 + 审核状态
-- 依赖前置：sys_dict_type、sys_dict_data 表已存在（ry_20260417.sql 创建）
-- 执行顺序：先 ry_20260417.sql，再本文件
-- =============================================================

-- ----------------------------
-- 1、字典类型：营业状态
-- ----------------------------
insert into sys_dict_type values(100, 0, '营业状态', 'takeout_business_status', '0', 'admin', sysdate(), '', null, '外卖商家营业状态（0=营业中 1=已打烊）');

-- ----------------------------
-- 2、字典类型：审核状态
-- ----------------------------
insert into sys_dict_type values(101, 0, '审核状态', 'takeout_audit_status',     '0', 'admin', sysdate(), '', null, '外卖商家入驻审核状态（0=待审核 1=通过 2=驳回）');

-- ----------------------------
-- 3、字典数据：营业状态 2 项
-- ----------------------------
insert into sys_dict_data values(100, 1,  '营业中', '0', 'takeout_business_status', '', 'default', '0', 'admin', sysdate(), '', null, '营业中');
insert into sys_dict_data values(101, 2,  '已打烊', '1', 'takeout_business_status', '', '',         '0', 'admin', sysdate(), '', null, '已打烊');

-- ----------------------------
-- 4、字典数据：审核状态 3 项
-- ----------------------------
insert into sys_dict_data values(102, 1,  '待审核', '0', 'takeout_audit_status', '', 'default', '0', 'admin', sysdate(), '', null, '待审核');
insert into sys_dict_data values(103, 2,  '通过',   '1', 'takeout_audit_status', '', '',         '0', 'admin', sysdate(), '', null, '通过');
insert into sys_dict_data values(104, 3,  '驳回',   '2', 'takeout_audit_status', '', '',         '0', 'admin', sysdate(), '', null, '驳回');