-- =============================================================
-- 外卖业务 · 商家管理 · 建表 SQL
-- 兼容 MySQL 5.7+ / 8.0+，字符集 utf8mb4
-- 依赖前置：ry-vue 库（与若依 ry_20260417.sql 同库）
-- =============================================================

-- ----------------------------
-- 1、外卖商家表
-- ----------------------------
drop table if exists takeout_merchant;
create table takeout_merchant (
  merchant_id      bigint(20)    not null auto_increment    comment '商家ID',
  merchant_name    varchar(100)  not null                   comment '商家名称',
  contact_name     varchar(50)   default null               comment '联系人',
  contact_phone    varchar(20)   default null               comment '联系电话',
  address          varchar(255)  default null               comment '商家地址',
  logo             varchar(255)  default null               comment 'Logo图片',
  business_license varchar(255)  default null               comment '营业执照图片',
  status           char(1)       default '0'                comment '营业状态(0营业中 1已打烊)',
  audit_status     char(1)       default '0'                comment '审核状态(0待审核 1通过 2驳回)',
  audit_remark     varchar(255)  default null               comment '审核备注',
  user_id          bigint(20)    default null               comment '绑定sys_user.user_id',
  del_flag         char(1)       default '0'                comment '删除标志(0存在 2删除)',
  create_by        varchar(64)   default null               comment '创建者',
  create_time      datetime      default null               comment '创建时间',
  update_by        varchar(64)   default null               comment '更新者',
  update_time      datetime      default null               comment '更新时间',
  remark           varchar(500)  default null               comment '备注',
  primary key (merchant_id),
  unique key uk_merchant_name (merchant_name, del_flag),
  unique key uk_user_id (user_id),
  key idx_phone (contact_phone),
  key idx_status (status),
  key idx_audit (audit_status)
) engine=InnoDB auto_increment=1 default charset=utf8mb4 comment='外卖商家表';