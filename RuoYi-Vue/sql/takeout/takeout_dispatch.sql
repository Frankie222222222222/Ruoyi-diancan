-- =============================================================
-- 外卖业务 · 订单派单/抢单表
-- 记录每个订单的配送员分配与配送状态
-- =============================================================
drop table if exists takeout_dispatch;
create table takeout_dispatch (
  dispatch_id         bigint(20)    not null auto_increment    comment '派单ID',
  order_id            bigint(20)    not null                   comment '订单ID',
  rider_id            bigint(20)    default null               comment '骑手ID',
  dispatch_type       char(1)       default '0'                comment '分配方式(0派单 1抢单)',
  status              char(1)       default '0'                comment '配送状态(0待接单 1已接单 2配送中 3已完成 4已取消)',
  assign_time         datetime      default null               comment '派单时间',
  accept_time         datetime      default null               comment '接单时间',
  pickup_time         datetime      default null               comment '取餐时间',
  complete_time       datetime      default null               comment '完成时间',
  estimated_arrival   datetime      default null               comment '预计送达时间',
  cancel_time         datetime      default null               comment '取消时间',
  cancel_reason       varchar(255)  default null               comment '取消原因',
  delivery_distance   decimal(8,2)  default null               comment '配送距离(米)',
  actual_distance     decimal(8,2)  default null               comment '实际配送距离(米)',
  del_flag            char(1)       default '0'                comment '删除标志(0存在 2删除)',
  create_by          varchar(64)   default null               comment '创建者',
  create_time        datetime      default null               comment '创建时间',
  update_by          varchar(64)   default null               comment '更新者',
  update_time        datetime      default null               comment '更新时间',
  remark             varchar(500)  default null               comment '备注',
  primary key (dispatch_id),
  key idx_order_id (order_id),
  key idx_rider_id (rider_id),
  key idx_status (status),
  key idx_assign_time (assign_time)
) engine=InnoDB auto_increment=1000 default charset=utf8mb4 comment='外卖派单记录表';
