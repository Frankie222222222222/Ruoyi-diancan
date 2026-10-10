-- =============================================================
-- 外卖业务 · 首页运营配置（4 张基础表 + 测试数据）
--   1) takeout_banner      首页轮播图
--   2) takeout_announcement 公告
--   3) takeout_help_category 帮助中心分类
--   4) takeout_help_article  帮助中心文章
--
-- 适用于 RuoYi-Vue3 外卖后台「运营统计」+ 小程序首页
-- 字符集 utf8mb4
-- 多次执行安全：先清旧数据再插入
-- =============================================================
SET NAMES utf8mb4;

-- ============================================================
-- 1. 建表
-- ============================================================
DROP TABLE IF EXISTS takeout_banner;
CREATE TABLE takeout_banner (
  banner_id    BIGINT       NOT NULL AUTO_INCREMENT COMMENT '轮播ID',
  title        VARCHAR(100) NOT NULL                COMMENT '标题',
  subtitle     VARCHAR(200) DEFAULT NULL            COMMENT '副标题',
  image        VARCHAR(500) NOT NULL                COMMENT '图片URL',
  link_type    CHAR(1)      DEFAULT '0'             COMMENT '链接类型 0不跳转 1商家 2菜品 3活动 4H5',
  link_target  VARCHAR(500) DEFAULT NULL            COMMENT '跳转目标(JSON)',
  sort_order   INT          DEFAULT 0               COMMENT '排序',
  start_time   DATETIME     DEFAULT NULL            COMMENT '生效时间',
  end_time     DATETIME     DEFAULT NULL            COMMENT '失效时间',
  status       CHAR(1)      DEFAULT '0'             COMMENT '状态 0启用 1停用',
  click_count  INT          DEFAULT 0               COMMENT '点击数',
  del_flag     CHAR(1)      DEFAULT '0'             COMMENT '删除标志(0存在 2删除)',
  create_by    VARCHAR(64)  DEFAULT NULL,
  create_time  DATETIME     DEFAULT NULL,
  update_by    VARCHAR(64)  DEFAULT NULL,
  update_time  DATETIME     DEFAULT NULL,
  remark       VARCHAR(500) DEFAULT NULL,
  PRIMARY KEY (banner_id),
  KEY idx_status (status),
  KEY idx_sort   (sort_order)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='外卖首页轮播';

DROP TABLE IF EXISTS takeout_announcement;
CREATE TABLE takeout_announcement (
  ann_id       BIGINT       NOT NULL AUTO_INCREMENT COMMENT '公告ID',
  title        VARCHAR(100) NOT NULL                COMMENT '标题',
  content      TEXT         NOT NULL                COMMENT '内容',
  type         CHAR(1)      DEFAULT '0'             COMMENT '类型 0系统 1活动 2维护 3紧急',
  level        CHAR(1)      DEFAULT '0'             COMMENT '级别 0普通 1重要 2紧急',
  top_flag     CHAR(1)      DEFAULT '0'             COMMENT '是否置顶 0否 1是',
  start_time   DATETIME     DEFAULT NULL            COMMENT '生效时间',
  end_time     DATETIME     DEFAULT NULL            COMMENT '失效时间',
  status       CHAR(1)      DEFAULT '0'             COMMENT '状态 0待发布 1已发布 2已下线',
  view_count   INT          DEFAULT 0               COMMENT '浏览数',
  del_flag     CHAR(1)      DEFAULT '0'             COMMENT '删除标志(0存在 2删除)',
  create_by    VARCHAR(64)  DEFAULT NULL,
  create_time  DATETIME     DEFAULT NULL,
  update_by    VARCHAR(64)  DEFAULT NULL,
  update_time  DATETIME     DEFAULT NULL,
  remark       VARCHAR(500) DEFAULT NULL,
  PRIMARY KEY (ann_id),
  KEY idx_status (status),
  KEY idx_type   (type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='外卖公告';

DROP TABLE IF EXISTS takeout_help_category;
CREATE TABLE takeout_help_category (
  cat_id       BIGINT       NOT NULL AUTO_INCREMENT COMMENT '分类ID',
  cat_name     VARCHAR(50)  NOT NULL                COMMENT '分类名称',
  icon         VARCHAR(255) DEFAULT NULL            COMMENT '图标',
  sort_order   INT          DEFAULT 0               COMMENT '排序',
  status       CHAR(1)      DEFAULT '0'             COMMENT '状态 0启用 1停用',
  del_flag     CHAR(1)      DEFAULT '0'             COMMENT '删除标志(0存在 2删除)',
  create_by    VARCHAR(64)  DEFAULT NULL,
  create_time  DATETIME     DEFAULT NULL,
  update_by    VARCHAR(64)  DEFAULT NULL,
  update_time  DATETIME     DEFAULT NULL,
  remark       VARCHAR(500) DEFAULT NULL,
  PRIMARY KEY (cat_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='帮助中心分类';

DROP TABLE IF EXISTS takeout_help_article;
CREATE TABLE takeout_help_article (
  article_id   BIGINT       NOT NULL AUTO_INCREMENT COMMENT '文章ID',
  cat_id       BIGINT       NOT NULL                COMMENT '分类ID',
  title        VARCHAR(200) NOT NULL                COMMENT '标题',
  content      MEDIUMTEXT   NOT NULL                COMMENT '正文',
  tag          VARCHAR(100) DEFAULT NULL            COMMENT '标签(逗号分隔)',
  view_count   INT          DEFAULT 0               COMMENT '浏览数',
  useful_count INT          DEFAULT 0               COMMENT '有帮助数',
  sort_order   INT          DEFAULT 0               COMMENT '排序',
  status       CHAR(1)      DEFAULT '0'             COMMENT '状态 0发布 1草稿 2下线',
  del_flag     CHAR(1)      DEFAULT '0'             COMMENT '删除标志(0存在 2删除)',
  create_by    VARCHAR(64)  DEFAULT NULL,
  create_time  DATETIME     DEFAULT NULL,
  update_by    VARCHAR(64)  DEFAULT NULL,
  update_time  DATETIME     DEFAULT NULL,
  remark       VARCHAR(500) DEFAULT NULL,
  PRIMARY KEY (article_id),
  KEY idx_cat    (cat_id),
  KEY idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='帮助中心文章';

-- ============================================================
-- 2. 测试数据
-- ============================================================
DELETE FROM takeout_banner;
INSERT INTO takeout_banner (banner_id, title, subtitle, image, link_type, link_target, sort_order, start_time, end_time, status, click_count, del_flag, create_by, create_time, remark) VALUES
(1, '夏日清凉满30减5',         '全场饮料冰品 8 折起',    'https://img.zcool.cn/community/banner_summer.jpg',   '3', '{"activityId":7}',  1, DATE_SUB(NOW(), INTERVAL 5 DAY),  DATE_ADD(NOW(), INTERVAL 15 DAY), '0', 1280, '0', 'admin', NOW(), '夏日活动'),
(2, '新人首单立减 15',          '注册即领，无门槛使用',    'https://img.zcool.cn/community/banner_newuser.jpg',  '3', '{"couponId":1}',     2, DATE_SUB(NOW(), INTERVAL 30 DAY), DATE_ADD(NOW(), INTERVAL 60 DAY), '0', 3560, '0', 'admin', NOW(), '拉新'),
(3, '海底捞 5 折抢',            '限新客 12:00 准时开抢',  'https://img.zcool.cn/community/banner_haidilao.jpg','2', '{"dishId":7}',        3, DATE_SUB(NOW(), INTERVAL 1 DAY),  DATE_ADD(NOW(), INTERVAL 7 DAY),  '0',  860, '0', 'admin', NOW(), '商家推广'),
(4, '周末特惠 满 40 减 8',       '每周六日 0 点开抢',     'https://img.zcool.cn/community/banner_weekend.jpg', '3', '{"couponId":6}',     4, DATE_SUB(NOW(), INTERVAL 7 DAY),  DATE_ADD(NOW(), INTERVAL 7 DAY),  '0',  420, '0', 'admin', NOW(), '周末'),
(5, '骑手招募',                  '月入过万，时间自由',     'https://img.zcool.cn/community/banner_rider.jpg',   '4', '{"url":"/pages/recruit/rider"}', 5, DATE_SUB(NOW(), INTERVAL 60 DAY), DATE_ADD(NOW(), INTERVAL 180 DAY),'0',  240, '0', 'admin', NOW(), '招募'),
(6, '商家入驻',                  '0 佣金上线 30 天',      'https://img.zcool.cn/community/banner_merchant.jpg','4', '{"url":"/pages/recruit/merchant"}', 6, DATE_SUB(NOW(), INTERVAL 60 DAY), DATE_ADD(NOW(), INTERVAL 180 DAY),'0',  180, '0', 'admin', NOW(), '招商'),
(7, '下架示例',                  '用于演示停用状态',      'https://img.zcool.cn/community/banner_disabled.jpg','0', NULL,                                           99, DATE_SUB(NOW(), INTERVAL 90 DAY), DATE_ADD(NOW(), INTERVAL 90 DAY), '1',   10, '0', 'admin', NOW(), '已下线');

DELETE FROM takeout_announcement;
INSERT INTO takeout_announcement (ann_id, title, content, type, level, top_flag, start_time, end_time, status, view_count, del_flag, create_by, create_time, remark) VALUES
(1, '【系统】10 月 15 日凌晨 1:00-3:00 维护', '为提升服务质量，我们将于 10 月 15 日 01:00 - 03:00 进行系统升级，期间下单、支付可能短暂不可用，请提前安排好您的用餐计划。', '2', '1', '1', DATE_SUB(NOW(), INTERVAL 1 DAY), DATE_ADD(NOW(), INTERVAL 7 DAY), '1', 5420, '0', 'admin', NOW(), ''),
(2, '【活动】国庆狂欢 全场满 50 减 10',       '10 月 1 日 - 7 日，全场满 50 减 10，满 100 减 25，会员可叠加使用。详情见首页活动 banner。',                              '1', '0', '0', DATE_SUB(NOW(), INTERVAL 10 DAY), DATE_ADD(NOW(), INTERVAL 5 DAY),  '1', 12800,'0', 'admin', NOW(), ''),
(3, '【紧急】关于 9 月底骑手服务投诉的说明', '近期收到多起配送超时与服务态度投诉，已对涉事骑手进行停岗培训。感谢您的反馈与监督。',                                          '3', '2', '1', DATE_SUB(NOW(), INTERVAL 3 DAY),  DATE_ADD(NOW(), INTERVAL 14 DAY),'1',  8600,'0', 'admin', NOW(), ''),
(4, '【活动】新人大礼包 0 元领',              '注册即领 15 元无门槛券 + 6 元免配送费 + 9 折折扣券，会员专享。',                                                            '1', '0', '0', DATE_SUB(NOW(), INTERVAL 30 DAY), DATE_ADD(NOW(), INTERVAL 60 DAY),'1', 22000,'0', 'admin', NOW(), ''),
(5, '【系统】客户端 2.0.0 上线',              '全新 UI 改版，新增「订单实时轨迹」「智能搜索」「店铺收藏」等功能，欢迎体验。',                                              '0', '0', '0', DATE_SUB(NOW(), INTERVAL 15 DAY), DATE_ADD(NOW(), INTERVAL 90 DAY),'1', 18500,'0', 'admin', NOW(), ''),
(6, '草稿：双 11 大促预告',                   '内容编辑中，暂未发布。',                                                                                                  '1', '0', '0', NULL,                              NULL,                            '0',     0,'0', 'admin', NOW(), '草稿示例');

DELETE FROM takeout_help_category;
INSERT INTO takeout_help_category (cat_id, cat_name, icon, sort_order, status, del_flag, create_by, create_time, remark) VALUES
(1, '新手指南',  'guide',     1, '0', '0', 'admin', NOW(), '下单、支付、优惠'),
(2, '订单问题',  'order',     2, '0', '0', 'admin', NOW(), '查询、修改、取消'),
(3, '配送服务',  'car',       3, '0', '0', 'admin', NOW(), '配送费、骑手、超时'),
(4, '退款售后',  'refund',    4, '0', '0', 'admin', NOW(), '投诉、退款流程'),
(5, '账户与安全','user',      5, '0', '0', 'admin', NOW(), '注册、登录、隐私'),
(6, '商家入驻',  'shop',      6, '0', '0', 'admin', NOW(), '入驻流程、资质');

DELETE FROM takeout_help_article;
INSERT INTO takeout_help_article (article_id, cat_id, title, content, tag, view_count, useful_count, sort_order, status, del_flag, create_by, create_time, remark) VALUES
(1, 1, '如何下单？',
'1. 打开小程序，点击首页搜索框或分类浏览\n2. 选择商品加入购物车\n3. 购物车点击「结算」\n4. 选择收货地址、优惠券、备注\n5. 提交订单并完成支付',
'下单,流程', 5200, 480, 1, '0', '0', 'admin', NOW(), ''),

(2, 1, '支持的支付方式',
'目前支持微信支付、支付宝、平台余额。\n微信支付：实时到账\n支付宝：实时到账\n余额：使用平台余额（充值后可用）',
'支付,微信,支付宝', 3100, 260, 2, '0', '0', 'admin', NOW(), ''),

(3, 1, '如何使用优惠券？',
'下单结算页会自动列出可用的优惠券（满足门槛的）。\n手动选择使用，未使用的券不消耗。\n优惠券一旦下单成功使用，将从账户中扣除。',
'优惠券,使用', 4800, 420, 3, '0', '0', 'admin', NOW(), ''),

(4, 2, '如何取消订单？',
'订单状态为「待支付」时，可直接在订单详情页点击「取消订单」。\n订单状态为「已支付/商家接单」时，需联系客服或商家协商。\n订单状态为「配送中」时，无法取消。',
'取消,订单', 2800, 230, 1, '0', '0', 'admin', NOW(), ''),

(5, 2, '订单状态说明',
'0 待支付 / 1 已支付 / 2 接单 / 3 配送中 / 4 已送达 / 5 已完成 / 6 已取消\n正常流程为 1→2→3→4→5，可在订单详情页查看实时状态。',
'状态,说明', 3500, 310, 2, '0', '0', 'admin', NOW(), ''),

(6, 2, '如何修改收货地址？',
'订单状态为「待支付」时可直接修改。\n订单状态为「已支付/商家接单」前，可在订单详情页点击「修改地址」，超时不可修改。',
'地址,修改', 1800, 150, 3, '0', '0', 'admin', NOW(), ''),

(7, 3, '配送费如何计算？',
'基础配送费 3-6 元，根据距离、天气、订单金额动态调整。\n会员可使用「免配送费券」抵扣。\n满 50 元订单可享受平台补贴运费减免。',
'配送费,计算', 4200, 380, 1, '0', '0', 'admin', NOW(), ''),

(8, 3, '骑手迟迟未到怎么办？',
'1. 在订单详情页查看骑手实时位置\n2. 可点击「联系骑手」直接拨打电话\n3. 超过预计时间 30 分钟，可申请「配送超时赔付」\n4. 极端情况联系在线客服',
'骑手,超时,联系', 5600, 510, 2, '0', '0', 'admin', NOW(), ''),

(9, 3, '恶劣天气配送说明',
'暴雨/暴雪等极端天气，平台会启动「配送加价补贴」，可能延长配送时间。\n骑手优先保证安全，感谢您的理解。',
'天气,补贴', 1200, 100, 3, '0', '0', 'admin', NOW(), ''),

(10, 4, '退款流程',
'1. 订单详情页点击「申请退款」\n2. 选择退款原因并提交\n3. 商家/客服 24 小时内审核\n4. 审核通过后 1-3 个工作日原路退回',
'退款,流程', 6300, 580, 1, '0', '0', 'admin', NOW(), ''),

(11, 4, '餐品质量问题如何处理？',
'建议第一时间拍照留证，并在订单详情页提交「投诉」，附上图片。\n平台会介入与商家协商，通常 24 小时内给出处理结果。',
'质量,投诉,图片', 3400, 320, 2, '0', '0', 'admin', NOW(), ''),

(12, 4, '退款多久到账？',
'原路退回：微信/支付宝 1-3 个工作日，余额实时到账。\n银行处理可能有延迟，请耐心等待。',
'到账,时间', 2900, 270, 3, '0', '0', 'admin', NOW(), ''),

(13, 5, '如何注册账号？',
'小程序支持微信一键登录，无需单独注册。\n如需平台账号，可使用手机号 + 验证码注册。',
'注册,登录', 2100, 190, 1, '0', '0', 'admin', NOW(), ''),

(14, 5, '账号安全',
'请勿将验证码、密码告知他人。\n平台不会主动要求您转账或提供验证码。\n发现异常请立即冻结账号。',
'安全,诈骗', 1600, 140, 2, '0', '0', 'admin', NOW(), ''),

(15, 5, '隐私政策',
'我们仅收集提供服务所必需的信息（手机号、收货地址）。\n所有数据加密存储，不会泄露给第三方。\n完整协议请见《用户协议》与《隐私政策》。',
'隐私,协议',  900,  80, 3, '0', '0', 'admin', NOW(), ''),

(16, 6, '商家入驻流程',
'1. 点击首页「商家入驻」\n2. 提交营业执照、身份证、银行账户\n3. 平台 1-3 个工作日审核\n4. 审核通过后开通管理后台\n5. 上传菜品，正式营业',
'入驻,流程', 2700, 240, 1, '0', '0', 'admin', NOW(), ''),

(17, 6, '入驻需要哪些资质？',
'必备：营业执照、法人身份证、银行开户许可。\n食品类目需：食品经营许可证。\n详细清单请咨询招商经理。',
'资质,材料', 1900, 170, 2, '0', '0', 'admin', NOW(), ''),

(18, 6, '平台佣金是多少？',
'目前新店首月 0 佣金，次月起 5%-8% 按类目阶梯收取。\n详情可联系招商经理。',
'佣金,费率', 1500, 130, 3, '0', '0', 'admin', NOW(), '');

-- ============================================================
-- 3. 验证
-- ============================================================
SELECT '=== 首页运营配置导入完成 ===' AS msg;
SELECT '轮播图'   AS 模块, COUNT(*) AS 条数 FROM takeout_banner
UNION ALL SELECT '公告',     COUNT(*) FROM takeout_announcement
UNION ALL SELECT '帮助分类', COUNT(*) FROM takeout_help_category
UNION ALL SELECT '帮助文章', COUNT(*) FROM takeout_help_article;
