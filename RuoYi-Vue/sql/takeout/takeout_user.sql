-- =============================================================
-- 外卖业务 · C端用户表
-- 若依外卖系统 - C端会员 (非 sys_user)
-- =============================================================
drop table if exists takeout_user;
create table takeout_user (
  user_id         bigint(20)    not null auto_increment    comment '用户ID',
  nickname        varchar(50)   default null               comment '昵称',
  phone           varchar(20)   not null                   comment '手机号(唯一登录凭证)',
  avatar          varchar(255)   default null               comment '头像URL',
  gender          char(1)       default '0'                comment '性别(0未知 1男 2女)',
  city            varchar(50)   default null               comment '所在城市',
  total_orders    int(11)       default 0                   comment '累计下单数',
  total_spend    decimal(10,2) default 0.00               comment '累计消费金额',
  reg_time        datetime      default null               comment '注册时间',
  last_login_time datetime      default null               comment '最后登录时间',
  status         char(1)       default '0'                comment '账号状态(0正常 1禁用)',
  del_flag       char(1)       default '0'                comment '删除标志(0存在 2删除)',
  create_by      varchar(64)   default null               comment '创建者',
  create_time    datetime      default null               comment '创建时间',
  update_by      varchar(64)   default null               comment '更新者',
  update_time    datetime      default null               comment '更新时间',
  remark         varchar(500)  default null               comment '备注',
  primary key (user_id),
  unique key uk_phone (phone),
  key idx_status (status),
  key idx_reg_time (reg_time)
) engine=InnoDB auto_increment=1000 default charset=utf8mb4 comment='外卖C端用户表';
