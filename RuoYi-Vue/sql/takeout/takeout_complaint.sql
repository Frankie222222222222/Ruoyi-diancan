-- =============================================================
-- 外卖业务 · 投诉与退款工单表
-- =============================================================
drop table if exists takeout_complaint;
create table takeout_complaint (
  complaint_id    bigint(20)    not null auto_increment    comment '工单ID',
  order_id         bigint(20)    not null                   comment '关联订单ID',
  user_id          bigint(20)    not null                   comment '投诉用户ID',
  merchant_id      bigint(20)    default null               comment '关联商家ID',
  rider_id         bigint(20)    default null               comment '关联骑手ID',
  type             char(1)       default '0'                comment '类型(0退款申请 1投诉商家 2投诉骑手 3其他)',
  reason           varchar(500)  not null                   comment '投诉/退款原因',
  images           varchar(1000) default null               comment '凭证图片(JSON数组)',
  refund_amount    decimal(10,2) default null               comment '申请退款金额',
  approved_amount  decimal(10,2) default null               comment '审批退款金额',
  status           char(1)       default '0'                comment '处理状态(0待处理 1处理中 2已退款 3已拒绝 4已撤销 5已完成)',
  handle_remark    varchar(500)  default null               comment '处理备注',
  handle_by        varchar(64)   default null               comment '处理人',
  handle_time      datetime      default null               comment '处理时间',
  refund_time      datetime      default null               comment '退款时间',
  refund_flow_no   varchar(64)   default null               comment '退款流水号',
  appeal_content   varchar(500)  default null               comment '用户申诉内容',
  appeal_time      datetime      default null               comment '申诉时间',
  appeal_status    char(1)       default '0'                comment '申诉状态(0待审核 1通过 2驳回)',
  appeal_handle    varchar(500)  default null               comment '申诉处理结果',
  del_flag         char(1)       default '0'                comment '删除标志(0存在 2删除)',
  create_by        varchar(64)   default null               comment '创建者',
  create_time      datetime      default null               comment '创建时间',
  update_by        varchar(64)   default null               comment '更新者',
  update_time      datetime      default null               comment '更新时间',
  remark          varchar(500)  default null               comment '备注',
  primary key (complaint_id),
  key idx_order_id (order_id),
  key idx_user_id (user_id),
  key idx_merchant_id (merchant_id),
  key idx_status (status),
  key idx_create_time (create_time)
) engine=InnoDB auto_increment=1000 default charset=utf8mb4 comment='外卖投诉退款工单表';
