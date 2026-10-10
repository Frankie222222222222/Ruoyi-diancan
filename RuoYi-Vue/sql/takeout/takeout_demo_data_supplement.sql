-- =============================================================
-- 外卖业务 · 补充测试数据 (3 张表)
-- 历史原因：takeout_demo_data.sql 中 ratings/coupons/complaints
--          三个 INSERT 在某次执行时被截断，本次单独补齐
-- 配合 sql/takeout/takeout_demo_data.sql 使用
-- 表结构已存在，无需 CREATE
-- 注意：takeout_rating.uk_order_id 是「一单一评」唯一约束，
--       如果演示需要同一订单多条评价，请先：
--         ALTER TABLE takeout_rating DROP INDEX uk_order_id;
-- =============================================================
SET NAMES utf8mb4;

-- ========== 1) 评价 8 条 ==========
-- 0=待审核 1=已隐藏
DELETE FROM takeout_rating;
-- 若需要多条评价/订单，先放宽唯一键
ALTER TABLE takeout_rating DROP INDEX uk_order_id;
INSERT INTO takeout_rating (rating_id, order_id, user_id, merchant_id, rider_id, merchant_score, rider_score, taste_score, packaging_score, delivery_score, content, taste_tags, is_anonymous, status, del_flag, create_by, create_time) VALUES
(1,  1, 1, 1, 1, 5, 5, 5, 4, 5, '味道很好，骑手小哥也专业，下次还会再来！',  '["好吃","分量足"]', '0', '0', '0', 'admin', NOW()),
(2,  2, 2, 2, 2, 4, 5, 4, 4, 5, '配送速度快，骑手态度好。',                  '["送得快"]',       '0', '0', '0', 'admin', NOW()),
(3,  7, 7, 1, 6, 5, 5, 5, 5, 4, '分量足，性价比超高，五星好评！',            '["实惠","好吃"]', '1', '0', '0', 'admin', NOW()),
(4,  9, 2, 5, 7, 5, 4, 5, 4, 4, '多肉葡萄头牌就是头牌！',                    '["必点"]',         '0', '0', '0', 'admin', NOW()),
(5,  1, 1, 1, 1, 5, 5, 4, 5, 5, '包装精美，分量足。',                        '["包装好"]',       '0', '0', '0', 'admin', NOW()),
(6,  2, 2, 2, 2, 3, 4, 3, 3, 4, '一般般，没想象中好。',                      '["一般"]',         '0', '0', '0', 'admin', NOW()),
(7,  7, 7, 1, 6, 5, 5, 5, 5, 5, '服务很贴心，会回购。',                      '["贴心"]',         '0', '0', '0', 'admin', NOW()),
(8,  9, 2, 5, 7, 4, 4, 5, 4, 4, '推荐给朋友了，真不错。',                    '["推荐"]',         '0', '0', '0', 'admin', NOW());

-- ========== 2) 优惠券 8 条 ==========
-- type 0=满减 1=折扣 2=免配送费 3=新人
DELETE FROM takeout_coupon;
INSERT INTO takeout_coupon (coupon_id, name, type, threshold_amount, discount_amount, discount_rate, max_discount, total_count, remain_count, per_user_limit, start_time, end_time, status, merchant_id, color, description, del_flag, create_by, create_time) VALUES
(1, '新人大礼包',     '3',  0.00, 15.00, NULL, NULL, 9999, 8500, 1, DATE_SUB(NOW(), INTERVAL 10 DAY), DATE_ADD(NOW(), INTERVAL 30 DAY), '0', NULL, '#FF6B6B', '新用户注册即领',                 '0', 'admin', NOW()),
(2, '满50减10',       '0', 50.00, 10.00, NULL, NULL, 5000, 4200, 2, DATE_SUB(NOW(), INTERVAL 5 DAY),  DATE_ADD(NOW(), INTERVAL 25 DAY), '0', NULL, '#4ECDC4', '满50元可用',                     '0', 'admin', NOW()),
(3, '满100减25',      '0',100.00, 25.00, NULL, NULL, 3000, 2800, 1, DATE_SUB(NOW(), INTERVAL 3 DAY),  DATE_ADD(NOW(), INTERVAL 27 DAY), '0', NULL, '#FFD93D', '满100元可用',                    '0', 'admin', NOW()),
(4, '9折折扣券',      '1', 30.00, NULL,    90, 30.00, 2000, 1800, 1, DATE_SUB(NOW(), INTERVAL 2 DAY),  DATE_ADD(NOW(), INTERVAL 20 DAY), '0', NULL, '#6BCB77', '9折封顶30元',                    '0', 'admin', NOW()),
(5, '免配送费券',     '2',  0.00,  6.00, NULL, NULL, 9999, 9000, 3, DATE_SUB(NOW(), INTERVAL 1 DAY),  DATE_ADD(NOW(), INTERVAL 60 DAY), '0', NULL, '#95E1D3', '免配送费',                       '0', 'admin', NOW()),
(6, '周末特惠',       '0', 40.00,  8.00, NULL, NULL, 1000,  650, 1, DATE_SUB(NOW(), INTERVAL 7 DAY),  DATE_ADD(NOW(), INTERVAL 7 DAY),  '0', NULL, '#F38181', '仅周末可用',                     '0', 'admin', NOW()),
(7, '夏日清凉券',     '0', 25.00,  5.00, NULL, NULL, 2000, 1500, 1, DATE_SUB(NOW(), INTERVAL 15 DAY), DATE_ADD(NOW(), INTERVAL 15 DAY), '0', NULL, '#3D5A80', '夏日限定',                       '0', 'admin', NOW()),
(8, '生日特权券',     '0',  0.00, 30.00, NULL, NULL,  500,  400, 1, DATE_SUB(NOW(), INTERVAL 30 DAY), DATE_ADD(NOW(), INTERVAL 365 DAY),'0', NULL, '#E98074', '生日当月专享',                   '0', 'admin', NOW());

-- ========== 3) 投诉 6 条 ==========
-- status 0=待审核 1=处理中 2=已退款 3=已驳回 4=已完成 5=已关闭
DELETE FROM takeout_complaint;
INSERT INTO takeout_complaint (complaint_id, order_id, user_id, merchant_id, rider_id, type, reason, refund_amount, approved_amount, status, handle_remark, handle_by, handle_time, del_flag, create_by, create_time) VALUES
(1, 1, 1, 1, 1, '0', '商家少送了一份薯条',           20.00, 20.00, '2', '已退款，赠送下次优惠券',  'admin', DATE_SUB(NOW(), INTERVAL 2 HOUR), '0', 'admin', DATE_SUB(NOW(), INTERVAL 3 HOUR)),
(2, 2, 2, 2, 2, '1', '汉堡凉了，口味不对',           15.00, 10.00, '4', '部分退款，商家道歉',      'admin', DATE_SUB(NOW(), INTERVAL 22 HOUR),'0', 'admin', DATE_SUB(NOW(), INTERVAL 23 HOUR)),
(3, 7, 7, 1, 6, '2', '配送超时 40 分钟',             12.00, 12.00, '2', '已退款，骑手扣分',        'admin', DATE_SUB(NOW(), INTERVAL 2 DAY),  '0', 'admin', DATE_SUB(NOW(), INTERVAL 2 DAY)),
(4, 9, 2, 5, 7, '3', '骑手服务态度差',               0.00,  0.00, '3', '已驳回，未发现违规',      'admin', DATE_SUB(NOW(), INTERVAL 4 HOUR), '0', 'admin', DATE_SUB(NOW(), INTERVAL 5 HOUR)),
(5, 1, 1, 1, 1, '0', '餐品质量有问题，怀疑不新鲜',   25.00, 15.00, '1', '正在与商家沟通',          'admin', NULL,                            '0', 'admin', DATE_SUB(NOW(), INTERVAL 1 HOUR)),
(6, 7, 7, 1, 6, '4', '包装破损严重',                 8.00,  8.00, '5', '用户已关闭投诉',          'admin', DATE_SUB(NOW(), INTERVAL 1 DAY),  '0', 'admin', DATE_SUB(NOW(), INTERVAL 2 DAY));

-- =============================================================
-- 验证
-- =============================================================
SELECT '=== 补充数据导入完成 ===' AS msg;
SELECT '评价' AS 模块, COUNT(*) AS 条数 FROM takeout_rating
UNION ALL SELECT '优惠券', COUNT(*) FROM takeout_coupon
UNION ALL SELECT '投诉',   COUNT(*) FROM takeout_complaint;
