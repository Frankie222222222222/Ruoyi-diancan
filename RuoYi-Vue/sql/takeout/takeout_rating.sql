-- =============================================================
-- 外卖业务 · 订单评价表
-- 用户对已完成订单的商家和骑手进行评价
-- =============================================================
drop table if exists takeout_rating;
create table takeout_rating (
  rating_id       bigint(20)    not null auto_increment    comment '评价ID',
  order_id        bigint(20)    not null                   comment '订单ID',
  user_id         bigint(20)    not null                   comment '评价用户ID',
  merchant_id     bigint(20)    not null                   comment '商家ID',
  rider_id        bigint(20)    default null               comment '骑手ID(可为空)',
  merchant_score  tinyint(1)    default 5                  comment '商家评分(1-5)',
  rider_score     tinyint(1)    default null               comment '骑手评分(1-5)',
  taste_score     tinyint(1)    default null               comment '口味评分(1-5)',
  packaging_score tinyint(1)    default null               comment '包装评分(1-5)',
  delivery_score  tinyint(1)    default null               comment '配送评分(1-5)',
  content         varchar(500)  default null               comment '文字评价',
  images          varchar(1000) default null               comment '评价图片(JSON数组)',
  taste_tags      varchar(255)  default null               comment '口味标签(JSON数组)',
  is_anonymous    char(1)       default '0'                comment '是否匿名(0否 1是)',
  reply           varchar(500)  default null               comment '商家回复',
  reply_time      datetime      default null               comment '回复时间',
  reply_by        varchar(64)   default null               comment '回复人',
  status          char(1)       default '0'                comment '状态(0显示 1隐藏)',
  del_flag        char(1)       default '0'                comment '删除标志(0存在 2删除)',
  create_by       varchar(64)  default null               comment '创建者',
  create_time     datetime      default null               comment '创建时间',
  update_by       varchar(64)  default null               comment '更新者',
  update_time     datetime      default null               comment '更新时间',
  primary key (rating_id),
  unique key uk_order_id (order_id),
  key idx_user_id (user_id),
  key idx_merchant_id (merchant_id),
  key idx_rider_id (rider_id),
  key idx_create_time (create_time)
) engine=InnoDB auto_increment=1000 default charset=utf8mb4 comment='外卖订单评价表';
