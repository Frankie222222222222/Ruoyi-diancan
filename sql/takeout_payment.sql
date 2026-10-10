-- ============================================================
-- 外卖支付流水表
-- 表名: takeout_payment
-- 说明: 记录每次支付尝试(创建/回调/退款),与 TakeoutOrder 是 N:1 关系
--      一个订单可以有多条支付记录(重试/退款/分账)
-- ============================================================

DROP TABLE IF EXISTS `takeout_payment`;
CREATE TABLE `takeout_payment` (
  `id`            BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
  `order_id`      BIGINT       NOT NULL                COMMENT '订单ID',
  `order_no`      VARCHAR(64)  NOT NULL                COMMENT '订单号(冗余,方便查询)',
  `channel`       VARCHAR(16)  NOT NULL                COMMENT '渠道:WECHAT/ALIPAY/MOCK',
  `out_trade_no`  VARCHAR(64)  NOT NULL                COMMENT '商户订单号(默认 = order_no)',
  `trade_no`      VARCHAR(64)  DEFAULT NULL            COMMENT '渠道流水号(微信/支付宝返回)',
  `amount`        DECIMAL(10,2) NOT NULL               COMMENT '支付金额(元)',
  `status`        VARCHAR(16)  NOT NULL DEFAULT 'PENDING' COMMENT '状态:PENDING/SUCCESS/FAILED/REFUNDED/CLOSED',
  `notify_raw`    TEXT         DEFAULT NULL            COMMENT '回调原始报文',
  `create_by`     VARCHAR(64)  DEFAULT NULL            COMMENT '创建人',
  `create_time`   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by`     VARCHAR(64)  DEFAULT NULL            COMMENT '更新人',
  `update_time`   DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `remark`        VARCHAR(500) DEFAULT NULL            COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_out_trade_no` (`out_trade_no`),
  KEY `idx_order_id` (`order_id`),
  KEY `idx_status` (`status`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='外卖支付流水表';
