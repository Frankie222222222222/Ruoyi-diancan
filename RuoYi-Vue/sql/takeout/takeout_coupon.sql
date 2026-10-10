-- =============================================================
-- 外卖业务 · 优惠券表 (主表 + 用户领取记录)
-- =============================================================

-- ----------------------------
-- 1. 优惠券主表
-- ----------------------------
drop table if exists takeout_coupon;
create table takeout_coupon (
  coupon_id        bigint(20)    not null auto_increment    comment '优惠券ID',
  name             varchar(100)  not null                   comment '优惠券名称',
  type             char(1)       default '0'                comment '类型(0满减券 1折扣券 2新客券 3配送费券)',
  threshold_amount decimal(10,2) default 0.00              comment '使用门槛金额(0=无门槛)',
  discount_amount  decimal(10,2) default null               comment '优惠金额(满减用,元)',
  discount_rate    decimal(5,2)  default null               comment '折扣率(折扣券用,如85.00=85折)',
  max_discount     decimal(10,2) default null               comment '最高优惠金额(折扣券封顶)',
  total_count      int(11)       default 0                  comment '发放总数量',
  remain_count     int(11)       default 0                  comment '剩余数量',
  per_user_limit   int(11)       default 1                  comment '每人限领数量',
  start_time       datetime      not null                   comment '有效期开始',
  end_time         datetime      not null                   comment '有效期结束',
  status           char(1)       default '0'                comment '状态(0上架 1下架 2已过期)',
  merchant_id      bigint(20)    default null               comment '所属商家(为空=平台券)',
  color            varchar(20)   default 'red'              comment '卡片颜色(red/orange/green/blue)',
  description      varchar(500)  default null               comment '使用说明',
  del_flag         char(1)       default '0'                comment '删除标志(0存在 2删除)',
  create_by        varchar(64)   default null               comment '创建者',
  create_time      datetime      default null               comment '创建时间',
  update_by        varchar(64)   default null               comment '更新者',
  update_time      datetime      default null               comment '更新时间',
  remark           varchar(500)  default null               comment '备注',
  primary key (coupon_id),
  key idx_merchant_id (merchant_id),
  key idx_status (status),
  key idx_end_time (end_time)
) engine=InnoDB auto_increment=1000 default charset=utf8mb4 comment='外卖优惠券表';

-- ----------------------------
-- 2. 用户优惠券领取/使用记录表
-- ----------------------------
drop table if exists takeout_coupon_user;
create table takeout_coupon_user (
  id            bigint(20)    not null auto_increment    comment '记录ID',
  user_id       bigint(20)    not null                   comment '用户ID',
  coupon_id     bigint(20)    not null                   comment '优惠券ID',
  status        char(1)       default '0'                comment '状态(0未使用 1已使用 2已过期 3已退回)',
  receive_time  datetime      default null               comment '领取时间',
  used_time     datetime      default null               comment '使用时间',
  used_order_id bigint(20)    default null               comment '使用订单ID',
  expire_time   datetime      default null               comment '过期时间(从优惠券复制)',
  del_flag      char(1)       default '0'                comment '删除标志(0存在 2删除)',
  create_by     varchar(64)   default null               comment '创建者',
  create_time   datetime      default null               comment '创建时间',
  update_by     varchar(64)   default null               comment '更新者',
  update_time   datetime      default null               comment '更新时间',
  remark        varchar(500)  default null               comment '备注',
  primary key (id),
  key idx_user_id (user_id),
  key idx_coupon_id (coupon_id),
  key idx_status (status),
  key idx_expire_time (expire_time)
) engine=InnoDB auto_increment=1000 default charset=utf8mb4 comment='用户优惠券领取记录表';
