-- =============================================================
-- 外卖业务 · 骑手/配送员表
-- =============================================================
drop table if exists takeout_rider;
create table takeout_rider (
  rider_id            bigint(20)    not null auto_increment    comment '骑手ID',
  name                varchar(50)   not null                   comment '骑手姓名',
  phone               varchar(20)   not null                   comment '联系电话(唯一)',
  avatar              varchar(255)  default null               comment '头像URL',
  status              char(1)       default '1'                comment '接单状态(0接单中 1休息中 2离线)',
  city                varchar(50)   default null               comment '服务城市',
  rating              decimal(3,2)  default 5.00              comment '平均评分(1.00-5.00)',
  total_deliveries    int(11)       default 0                  comment '累计配送单量',
  total_income        decimal(10,2) default 0.00             comment '累计收入',
  bank_card           varchar(50)   default null               comment '银行卡号',
  bank_name           varchar(100)  default null               comment '开户行',
  emergency_contact   varchar(50)   default null               comment '紧急联系人',
  emergency_phone     varchar(20)   default null               comment '紧急联系电话',
  join_time           datetime      default null               comment '入职时间',
  del_flag            char(1)       default '0'                comment '删除标志(0存在 2删除)',
  create_by           varchar(64)   default null               comment '创建者',
  create_time         datetime      default null               comment '创建时间',
  update_by           varchar(64)   default null               comment '更新者',
  update_time         datetime      default null               comment '更新时间',
  remark             varchar(500)  default null               comment '备注',
  primary key (rider_id),
  unique key uk_phone (phone),
  key idx_status (status),
  key idx_city (city)
) engine=InnoDB auto_increment=1000 default charset=utf8mb4 comment='外卖骑手表';
