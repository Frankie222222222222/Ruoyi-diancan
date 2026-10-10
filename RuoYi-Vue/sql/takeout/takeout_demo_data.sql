-- =============================================================
-- 外卖业务 · 演示数据（10 个菜单对应的 11 张表）
--
-- 覆盖菜单（均位于父菜单「外卖管理」id=2044 下）：
--   商家管理 (takeout_merchant)
--   菜品分类 (takeout_dish_category)
--   菜品管理 (takeout_dish)
--   订单管理 (takeout_order / takeout_order_item)
--   C端用户 (takeout_user)
--   骑手管理 (takeout_rider)
--   派单管理 (takeout_dispatch)
--   评价管理 (takeout_rating)
--   优惠券管理 (takeout_coupon)
--   投诉管理 (takeout_complaint)
--   菜品销量榜 (依赖 takeout_dish)
--
-- 用法：
--   1) 先执行 takeout_dish.sql / takeout_merchant.sql / takeout_user.sql 等建表脚本
--   2) 在 navicat/mysql client 里直接执行本文件即可
--   3) 多次执行安全：先 DELETE 再 INSERT（清表顺序按外键反序）
-- =============================================================
SET NAMES utf8mb4;

-- ========== 0) 清空旧数据（按外键反序） ==========
DELETE FROM takeout_complaint;
DELETE FROM takeout_rating;
DELETE FROM takeout_dispatch;
DELETE FROM takeout_order_item;
DELETE FROM takeout_order;
DELETE FROM takeout_coupon;
DELETE FROM takeout_rider;
DELETE FROM takeout_dish;
DELETE FROM takeout_dish_category;
DELETE FROM takeout_merchant;
DELETE FROM takeout_user;

-- ========== 1) C 端用户 8 条 ==========
INSERT INTO takeout_user (user_id, nickname, phone, avatar, gender, city, total_orders, total_spend, status, del_flag, create_by, create_time, remark) VALUES
(1, '小张同学',   '13800138001', 'https://i.pravatar.cc/150?img=11', '1', '上海', 28, 1860.50, '0', '0', 'admin', NOW(), '老用户'),
(2, '阿龙',       '13900139002', 'https://i.pravatar.cc/150?img=12', '1', '北京', 12,  680.00, '0', '0', 'admin', NOW(), ''),
(3, 'Tom',        '15000150003', 'https://i.pravatar.cc/150?img=13', '1', '深圳',  3,  150.00, '0', '0', 'admin', NOW(), '新用户'),
(4, 'Lily',       '15100151004', 'https://i.pravatar.cc/150?img=14', '2', '广州',  7,  420.00, '0', '0', 'admin', NOW(), ''),
(5, '李雷',       '15200152005', 'https://i.pravatar.cc/150?img=15', '1', '杭州', 15,  920.00, '0', '0', 'admin', NOW(), 'VIP'),
(6, '韩梅梅',     '15800158006', 'https://i.pravatar.cc/150?img=16', '2', '上海', 22, 1320.00, '0', '0', 'admin', NOW(), ''),
(7, '老王',       '18600186007', 'https://i.pravatar.cc/150?img=17', '1', '北京', 41, 2580.00, '0', '0', 'admin', NOW(), '高客单'),
(8, 'Lucy',       '18700187008', 'https://i.pravatar.cc/150?img=18', '2', '深圳',  0,    0.00, '1', '0', 'admin', NOW(), '已封禁');

-- ========== 2) 商家 8 条 ==========
-- status 0=营业中 1=已打烊；audit_status 0=待审核 1=通过 2=驳回
INSERT INTO takeout_merchant (merchant_id, merchant_name, contact_name, contact_phone, address, logo, business_license, status, audit_status, audit_remark, user_id, del_flag, create_by, create_time, remark) VALUES
(1, '肯德基（朝阳店）',     '张经理', '13811111001', '北京市朝阳区建国路88号',         'https://img.zcool.cn/community/01a87d5e5e5e5a1_4_2.jpg', 'https://img.zcool.cn/license_1.jpg', '0', '1', '资料齐全', NULL, '0', 'admin', NOW(), '金牌商家'),
(2, '麦当劳（陆家嘴店）',   '王店长', '13811111002', '上海市浦东新区世纪大道100号',   'https://img.zcool.cn/community/01a87d5e5e5e5a2_4_2.jpg', 'https://img.zcool.cn/license_2.jpg', '0', '1', '审核通过', NULL, '0', 'admin', NOW(), '银牌商家'),
(3, '海底捞（天河店）',     '李老板', '13811111003', '广州市天河区珠江新城50号',       'https://img.zcool.cn/community/01a87d5e5e5e5a3_4_2.jpg', 'https://img.zcool.cn/license_3.jpg', '0', '1', '审核通过', NULL, '0', 'admin', NOW(), '热门商家'),
(4, '星巴克（静安店）',     '赵总',   '13811111004', '上海市静安区南京西路1601号',     'https://img.zcool.cn/community/01a87d5e5e5e5a4_4_2.jpg', 'https://img.zcool.cn/license_4.jpg', '0', '1', '审核通过', NULL, '0', 'admin', NOW(), '连锁咖啡'),
(5, '喜茶（南山店）',       '陈经理', '13811111005', '深圳市南山区科苑路100号',         'https://img.zcool.cn/community/01a87d5e5e5e5a5_4_2.jpg', 'https://img.zcool.cn/license_5.jpg', '0', '1', '审核通过', NULL, '0', 'admin', NOW(), '奶茶头部'),
(6, '瑞幸咖啡（海淀店）',   '钱主管', '13811111006', '北京市海淀区中关村大街27号',     'https://img.zcool.cn/community/01a87d5e5e5e5a6_4_2.jpg', 'https://img.zcool.cn/license_6.jpg', '0', '1', '审核通过', NULL, '0', 'admin', NOW(), '性价比'),
(7, '杨国福（人民广场店）', '孙总',   '13811111007', '上海市黄浦区人民大道1号',         'https://img.zcool.cn/community/01a87d5e5e5e5a7_4_2.jpg', 'https://img.zcool.cn/license_7.jpg', '1', '1', '审核通过', NULL, '0', 'admin', NOW(), '已打烊'),
(8, '新人小馆（待审核）',   '吴老板', '13811111008', '北京市西城区西直门内大街88号',   'https://img.zcool.cn/community/01a87d5e5e5e5a8_4_2.jpg', 'https://img.zcool.cn/license_8.jpg', '0', '0', NULL, NULL, '0', 'admin', NOW(), '待审核');

-- ========== 3) 菜品分类 4 条（公共分类，商家可复用） ==========
INSERT INTO takeout_dish_category (category_id, category_name, sort_order, status, create_by, create_time, remark) VALUES
(1, '热销推荐', 1, '1', 'admin', NOW(), '店铺热销'),
(2, '招牌主食', 2, '1', 'admin', NOW(), '招牌饭面'),
(3, '小食饮品', 3, '1', 'admin', NOW(), '小食饮料'),
(4, '超值套餐', 4, '1', 'admin', NOW(), '套餐');

-- ========== 4) 菜品 24 条（每个商家 3 道菜，覆盖 4 个分类） ==========
-- 涵盖不同状态（1=上架 0=下架）和库存
INSERT INTO takeout_dish (dish_id, dish_name, category_id, merchant_id, image, price, stock, sales, status, description, create_by, create_time, remark) VALUES
-- 商家1 肯德基
( 1, '香辣鸡腿堡',          1, 1, 'https://img.zcool.cn/community/dish_burger.jpg',     18.50, 120, 256, '1', '经典香辣，一口爆汁',         'admin', NOW(), ''),
( 2, '黄金鸡块(5块)',       3, 1, 'https://img.zcool.cn/community/dish_chicken.jpg',    15.00, 200, 412, '1', '外酥里嫩',                   'admin', NOW(), ''),
( 3, '薯条(中份)',          3, 1, 'https://img.zcool.cn/community/dish_fries.jpg',       9.90, 300, 520, '1', '金黄酥脆',                   'admin', NOW(), ''),
-- 商家2 麦当劳
( 4, '巨无霸',              1, 2, 'https://img.zcool.cn/community/dish_bigmac.jpg',     25.00, 100, 198, '1', '两层牛肉，双重满足',         'admin', NOW(), ''),
( 5, '麦乐鸡(10块)',        3, 2, 'https://img.zcool.cn/community/dish_mcnugget.jpg',   22.50, 180, 360, '1', '蘸酱更香',                   'admin', NOW(), ''),
( 6, '圆筒冰淇淋',          3, 2, 'https://img.zcool.cn/community/dish_icecream.jpg',    5.00, 250, 480, '0', '下架维护中',                 'admin', NOW(), '下架'),
-- 商家3 海底捞
( 7, '麻辣鸳鸯锅底',        2, 3, 'https://img.zcool.cn/community/dish_hotpot.jpg',     68.00,  60, 150, '1', '一锅两吃，老少皆宜',         'admin', NOW(), ''),
( 8, '招牌毛肚',            1, 3, 'https://img.zcool.cn/community/dish_maodu.jpg',      48.00,  80, 320, '1', '七上八下，脆嫩爽口',         'admin', NOW(), ''),
( 9, '虾滑',                3, 3, 'https://img.zcool.cn/community/dish_xiahua.jpg',     38.00, 100, 280, '1', 'Q弹紧致',                   'admin', NOW(), ''),
-- 商家4 星巴克
(10, '拿铁(中杯)',          3, 4, 'https://img.zcool.cn/community/dish_latte.jpg',      32.00, 500, 800, '1', '经典意式风味',               'admin', NOW(), ''),
(11, '美式咖啡',            3, 4, 'https://img.zcool.cn/community/dish_americano.jpg',  28.00, 500, 600, '1', '提神醒脑',                   'admin', NOW(), ''),
(12, '星冰乐(抹茶)',        3, 4, 'https://img.zcool.cn/community/dish_matcha.jpg',     38.00, 200, 240, '1', '清凉一夏',                   'admin', NOW(), ''),
-- 商家5 喜茶
(13, '多肉葡萄',            3, 5, 'https://img.zcool.cn/community/dish_grape.jpg',      32.00, 300,1200, '1', '头牌必点',                   'admin', NOW(), '爆款'),
(14, '芝芝莓莓',            3, 5, 'https://img.zcool.cn/community/dish_berry.jpg',      33.00, 300, 880, '1', '莓香浓郁',                   'admin', NOW(), ''),
(15, '烤椰乳茶',            3, 5, 'https://img.zcool.cn/community/dish_coconut.jpg',    25.00, 300, 360, '0', '原料调整暂时下架',           'admin', NOW(), '下架'),
-- 商家6 瑞幸
(16, '生椰拿铁',            3, 6, 'https://img.zcool.cn/community/dish_coco_latte.jpg', 19.90, 999,1500, '1', '年度爆款',                   'admin', NOW(), '爆款'),
(17, '厚乳咖啡',            3, 6, 'https://img.zcool.cn/community/dish_thick.jpg',      21.00, 999, 700, '1', '奶香醇厚',                   'admin', NOW(), ''),
(18, '大师咖啡',            3, 6, 'https://img.zcool.cn/community/dish_master.jpg',     17.00, 999, 450, '1', '专业品质',                   'admin', NOW(), ''),
-- 商家7 杨国福
(19, '招牌麻辣烫',          2, 7, 'https://img.zcool.cn/community/dish_malatang.jpg',   32.00, 100, 380, '1', '麻辣鲜香',                   'admin', NOW(), ''),
(20, '骨汤麻辣烫',          2, 7, 'https://img.zcool.cn/community/dish_bone.jpg',       30.00, 100, 220, '1', '不辣也香',                   'admin', NOW(), ''),
(21, '番茄麻辣烫',          2, 7, 'https://img.zcool.cn/community/dish_tomato.jpg',     28.00, 100, 180, '0', '调整配方中',                 'admin', NOW(), '下架'),
-- 商家8 待审核商家（只挂一个套餐）
(22, '开业套餐(待审)',      4, 8, 'https://img.zcool.cn/community/dish_opening.jpg',    88.00,  50,   0, '1', '商家待审，仅展示',           'admin', NOW(), '待审商家'),
(23, '测试-低库存',          1, 1, 'https://img.zcool.cn/community/dish_low.jpg',         9.90,   3,  10, '1', '故意低库存测预警',           'admin', NOW(), '低库存'),
(24, '测试-零库存',          1, 1, 'https://img.zcool.cn/community/dish_zero.jpg',        5.00,   0,   5, '1', '故意零库存',                 'admin', NOW(), '零库存');

-- ========== 5) 骑手 8 条 ==========
-- status 0=可接单 1=配送中 2=休息
INSERT INTO takeout_rider (rider_id, name, phone, avatar, status, city, rating, total_deliveries, total_income, emergency_contact, emergency_phone, del_flag, create_by, create_time, remark) VALUES
(1, '刘骑手',  '13900001001', 'https://i.pravatar.cc/150?img=21', '0', '北京', 4.85,  823, 21500.00, '刘父', '13900001111', '0', 'admin', NOW(), '金牌骑手'),
(2, '陈骑手',  '13900001002', 'https://i.pravatar.cc/150?img=22', '0', '北京', 4.92, 1450, 38000.00, '陈母', '13900001112', '0', 'admin', NOW(), '五星骑手'),
(3, '黄骑手',  '13900001003', 'https://i.pravatar.cc/150?img=23', '1', '上海', 4.60,  520, 15600.00, '黄兄', '13900001113', '0', 'admin', NOW(), '正在配送'),
(4, '周骑手',  '13900001004', 'https://i.pravatar.cc/150?img=24', '0', '上海', 4.78,  980, 24800.00, '周姐', '13900001114', '0', 'admin', NOW(), ''),
(5, '吴骑手',  '13900001005', 'https://i.pravatar.cc/150?img=25', '2', '深圳', 4.55,  340,  9800.00, '吴嫂', '13900001115', '0', 'admin', NOW(), '休假中'),
(6, '徐骑手',  '13900001006', 'https://i.pravatar.cc/150?img=26', '0', '广州', 4.90, 1230, 32000.00, '徐母', '13900001116', '0', 'admin', NOW(), ''),
(7, '孙骑手',  '13900001007', 'https://i.pravatar.cc/150?img=27', '0', '杭州', 4.70,  650, 18600.00, '孙父', '13900001117', '0', 'admin', NOW(), ''),
(8, '马骑手',  '13900001008', 'https://i.pravatar.cc/150?img=28', '0', '北京', 4.40,   80,  2200.00, '马母', '13900001118', '0', 'admin', NOW(), '新骑手');

-- ========== 6) 订单 12 条（覆盖 0~6 各种状态） ==========
-- 0待支付 1已支付 2接单 3配送中 4已送达 5已完成 6已取消
INSERT INTO takeout_order (order_id, order_no, user_id, merchant_id, total_amount, delivery_fee, discount_amount, actual_amount, coupon_user_id, status, pay_status, pay_method, receiver_name, receiver_phone, address, remark, pay_time, complete_time, del_flag, create_by, create_time) VALUES
( 1, 'TKO20261010001', 1, 1,  43.40, 5.00,  5.00, 43.40, NULL, '5', '1', '微信',  '小张',  '13800138001', '上海市浦东新区世纪大道100号', '少辣',           DATE_SUB(NOW(), INTERVAL 2 HOUR),  DATE_SUB(NOW(), INTERVAL 1 HOUR), '0', 'admin', DATE_SUB(NOW(), INTERVAL 3 HOUR)),
( 2, 'TKO20261010002', 2, 2,  47.50, 4.00,  0.00, 51.50, NULL, '4', '1', '支付宝','阿龙',  '13900139002', '北京市朝阳区建国路88号',     '不喝冷饮',       DATE_SUB(NOW(), INTERVAL 1 DAY),   DATE_SUB(NOW(), INTERVAL 23 HOUR),'0', 'admin', DATE_SUB(NOW(), INTERVAL 1 DAY)),
( 3, 'TKO20261010003', 3, 3, 116.00, 6.00, 10.00,112.00, NULL, '3', '1', '微信',  'Tom',   '15000150003', '深圳市南山区科苑路100号',   '毛肚七上八下',   DATE_SUB(NOW(), INTERVAL 30 MINUTE),NULL,               '0', 'admin', DATE_SUB(NOW(), INTERVAL 45 MINUTE)),
( 4, 'TKO20261010004', 4, 4,  60.00, 5.00,  0.00, 65.00, NULL, '2', '1', '余额',  'Lily',  '15100151004', '广州市天河区珠江新城50号',   '加浓',           DATE_SUB(NOW(), INTERVAL 10 MINUTE),NULL,               '0', 'admin', DATE_SUB(NOW(), INTERVAL 15 MINUTE)),
( 5, 'TKO20261010005', 5, 5,  65.00, 4.00,  0.00, 69.00, NULL, '1', '1', '支付宝','李雷',  '15200152005', '上海市静安区南京西路1601号', '少糖',           DATE_SUB(NOW(), INTERVAL 5 MINUTE), NULL,               '0', 'admin', DATE_SUB(NOW(), INTERVAL 6 MINUTE)),
( 6, 'TKO20261010006', 6, 6,  40.90, 3.00,  0.00, 43.90, NULL, '0', '0', NULL,    '韩梅梅','15800158006', '北京市海淀区中关村大街27号', '请快点',         NULL,                              NULL,               '0', 'admin', DATE_SUB(NOW(), INTERVAL 2 MINUTE)),
( 7, 'TKO20261010007', 7, 1,  37.40, 5.00,  0.00, 42.40, NULL, '5', '1', '微信',  '老王',  '18600186007', '上海市黄浦区人民大道1号',     '加一份番茄酱',   DATE_SUB(NOW(), INTERVAL 3 DAY),    DATE_SUB(NOW(), INTERVAL 2 DAY),   '0', 'admin', DATE_SUB(NOW(), INTERVAL 3 DAY)),
( 8, 'TKO20261010008', 1, 7,  62.00, 5.00,  0.00, 67.00, NULL, '6', '0', NULL,    '小张',  '13800138001', '上海市浦东新区世纪大道100号', '不要香菜',       NULL,                              NULL,               '0', 'admin', DATE_SUB(NOW(), INTERVAL 4 DAY)),
( 9, 'TKO20261010009', 2, 5,  33.00, 4.00,  0.00, 37.00, NULL, '4', '1', '微信',  '阿龙',  '13900139002', '深圳市南山区科苑路100号',     '正常',           DATE_SUB(NOW(), INTERVAL 5 HOUR),   DATE_SUB(NOW(), INTERVAL 4 HOUR),  '0', 'admin', DATE_SUB(NOW(), INTERVAL 6 HOUR)),
(10, 'TKO20261010010', 5, 3, 124.00, 6.00,  0.00,130.00, NULL, '1', '1', '支付宝','李雷',  '15200152005', '广州市天河区珠江新城50号',   '加份虾滑',       DATE_SUB(NOW(), INTERVAL 20 MINUTE),NULL,               '0', 'admin', DATE_SUB(NOW(), INTERVAL 25 MINUTE)),
(11, 'TKO20261010011', 3, 6,  21.00, 3.00,  0.00, 24.00, NULL, '0', '0', NULL,    'Tom',   '15000150003', '北京市海淀区中关村大街27号', '不加糖',         NULL,                              NULL,               '0', 'admin', NOW()),
(12, 'TKO20261010012', 7, 4,  60.00, 5.00,  6.00, 59.00, NULL, '2', '1', '微信',  '老王',  '18600186007', '上海市静安区南京西路1601号', '温度高点',       DATE_SUB(NOW(), INTERVAL 8 MINUTE), NULL,               '0', 'admin', DATE_SUB(NOW(), INTERVAL 12 MINUTE));

-- ========== 7) 订单明细（每单 1~3 道菜） ==========
INSERT INTO takeout_order_item (item_id, order_id, dish_id, dish_name, dish_image, price, quantity, subtotal) VALUES
-- 1
(  1,  1,  1, '香辣鸡腿堡',     'https://img.zcool.cn/community/dish_burger.jpg',     18.50, 1, 18.50),
(  2,  1,  2, '黄金鸡块(5块)',  'https://img.zcool.cn/community/dish_chicken.jpg',    15.00, 1, 15.00),
(  3,  1,  3, '薯条(中份)',     'https://img.zcool.cn/community/dish_fries.jpg',       9.90, 1,  9.90),
-- 2
(  4,  2,  4, '巨无霸',         'https://img.zcool.cn/community/dish_bigmac.jpg',     25.00, 1, 25.00),
(  5,  2,  5, '麦乐鸡(10块)',   'https://img.zcool.cn/community/dish_mcnugget.jpg',   22.50, 1, 22.50),
-- 3
(  6,  3,  7, '麻辣鸳鸯锅底',   'https://img.zcool.cn/community/dish_hotpot.jpg',     68.00, 1, 68.00),
(  7,  3,  8, '招牌毛肚',       'https://img.zcool.cn/community/dish_maodu.jpg',      48.00, 1, 48.00),
-- 4
(  8,  4, 10, '拿铁(中杯)',     'https://img.zcool.cn/community/dish_latte.jpg',      32.00, 1, 32.00),
(  9,  4, 11, '美式咖啡',       'https://img.zcool.cn/community/dish_americano.jpg',  28.00, 1, 28.00),
-- 5
( 10,  5, 13, '多肉葡萄',       'https://img.zcool.cn/community/dish_grape.jpg',      32.00, 1, 32.00),
( 11,  5, 14, '芝芝莓莓',       'https://img.zcool.cn/community/dish_berry.jpg',      33.00, 1, 33.00),
-- 6
( 12,  6, 16, '生椰拿铁',       'https://img.zcool.cn/community/dish_coco_latte.jpg', 19.90, 1, 19.90),
( 13,  6, 17, '厚乳咖啡',       'https://img.zcool.cn/community/dish_thick.jpg',      21.00, 1, 21.00),
-- 7
( 14,  7,  2, '黄金鸡块(5块)',  'https://img.zcool.cn/community/dish_chicken.jpg',    15.00, 1, 15.00),
( 15,  7,  3, '薯条(中份)',     'https://img.zcool.cn/community/dish_fries.jpg',       9.90, 1,  9.90),
( 16,  7,  1, '香辣鸡腿堡',     'https://img.zcool.cn/community/dish_burger.jpg',     18.50, 1, 18.50),
-- 8
( 17,  8, 19, '招牌麻辣烫',     'https://img.zcool.cn/community/dish_malatang.jpg',   32.00, 1, 32.00),
( 18,  8, 20, '骨汤麻辣烫',     'https://img.zcool.cn/community/dish_bone.jpg',       30.00, 1, 30.00),
-- 9
( 19,  9, 13, '多肉葡萄',       'https://img.zcool.cn/community/dish_grape.jpg',      32.00, 1, 33.00),
-- 10
( 20, 10,  7, '麻辣鸳鸯锅底',   'https://img.zcool.cn/community/dish_hotpot.jpg',     68.00, 1, 68.00),
( 21, 10,  8, '招牌毛肚',       'https://img.zcool.cn/community/dish_maodu.jpg',      48.00, 1, 48.00),
( 22, 10,  9, '虾滑',           'https://img.zcool.cn/community/dish_xiahua.jpg',     38.00, 1,  8.00),
-- 11
( 23, 11, 17, '厚乳咖啡',       'https://img.zcool.cn/community/dish_thick.jpg',      21.00, 1, 21.00),
-- 12
( 24, 12, 10, '拿铁(中杯)',     'https://img.zcool.cn/community/dish_latte.jpg',      32.00, 1, 32.00),
( 25, 12, 11, '美式咖啡',       'https://img.zcool.cn/community/dish_americano.jpg',  28.00, 1, 28.00);

-- ========== 8) 派单 10 条（订单 1/2/3/4/5/7/9/10/12 关联骑手，0 待接/1 已接/2 取餐/3 配送/4 送达） ==========
-- status 0=待接单 1=已接单 2=取餐中 3=配送中 4=已送达
-- dispatch_type 0=系统派单 1=骑手抢单
INSERT INTO takeout_dispatch (dispatch_id, order_id, rider_id, dispatch_type, status, assign_time, accept_time, pickup_time, complete_time, estimated_arrival, delivery_distance, rider_lng, rider_lat, location_update_time, del_flag, create_by, create_time, remark) VALUES
( 1,  1, 1, '0', '4', DATE_SUB(NOW(), INTERVAL 3 HOUR),     DATE_SUB(NOW(), INTERVAL 2 HOUR),     DATE_SUB(NOW(), INTERVAL 110 MINUTE), DATE_SUB(NOW(), INTERVAL 1 HOUR),   DATE_SUB(NOW(), INTERVAL 2 HOUR),  2.8, 116.397128, 39.916527, DATE_SUB(NOW(), INTERVAL 1 HOUR),    '0', 'admin', DATE_SUB(NOW(), INTERVAL 3 HOUR),     '系统派单'),
( 2,  2, 2, '0', '4', DATE_SUB(NOW(), INTERVAL 1 DAY),       DATE_SUB(NOW(), INTERVAL 1 DAY),       DATE_SUB(NOW(), INTERVAL 1 DAY),       DATE_SUB(NOW(), INTERVAL 23 HOUR),  DATE_SUB(NOW(), INTERVAL 23 HOUR), 3.2, 116.407128, 39.926527, DATE_SUB(NOW(), INTERVAL 23 HOUR),  '0', 'admin', DATE_SUB(NOW(), INTERVAL 1 DAY),       ''),
( 3,  3, 3, '1', '3', DATE_SUB(NOW(), INTERVAL 45 MINUTE),   DATE_SUB(NOW(), INTERVAL 40 MINUTE),   DATE_SUB(NOW(), INTERVAL 30 MINUTE),   NULL,                                DATE_SUB(NOW(), INTERVAL 10 MINUTE),4.5, 113.943126, 22.542874, NOW(),                                  '0', 'admin', DATE_SUB(NOW(), INTERVAL 45 MINUTE),   '骑手抢单'),
( 4,  4, 4, '0', '1', DATE_SUB(NOW(), INTERVAL 15 MINUTE),   DATE_SUB(NOW(), INTERVAL 14 MINUTE),   NULL,                                 NULL,                                DATE_SUB(NOW(), INTERVAL 5 MINUTE),  1.5, 121.473701, 31.230416, DATE_SUB(NOW(), INTERVAL 5 MINUTE),   '0', 'admin', DATE_SUB(NOW(), INTERVAL 15 MINUTE),   '已接单'),
( 5,  5, 1, '0', '0', NOW(),                                  NULL,                                 NULL,                                 NULL,                                DATE_SUB(NOW(), INTERVAL 5 MINUTE), 2.0, 116.397128, 39.916527, NULL,                                '0', 'admin', NOW(),                                 '待接单'),
( 6,  7, 6, '1', '4', DATE_SUB(NOW(), INTERVAL 3 DAY),       DATE_SUB(NOW(), INTERVAL 3 DAY),       DATE_SUB(NOW(), INTERVAL 3 DAY),       DATE_SUB(NOW(), INTERVAL 2 DAY),     DATE_SUB(NOW(), INTERVAL 2 DAY),    5.0, 121.473701, 31.230416, DATE_SUB(NOW(), INTERVAL 2 DAY),     '0', 'admin', DATE_SUB(NOW(), INTERVAL 3 DAY),       ''),
( 7,  9, 7, '0', '4', DATE_SUB(NOW(), INTERVAL 6 HOUR),      DATE_SUB(NOW(), INTERVAL 6 HOUR),      DATE_SUB(NOW(), INTERVAL 5 HOUR),      DATE_SUB(NOW(), INTERVAL 4 HOUR),    DATE_SUB(NOW(), INTERVAL 5 HOUR),   2.3, 113.943126, 22.542874, DATE_SUB(NOW(), INTERVAL 4 HOUR),    '0', 'admin', DATE_SUB(NOW(), INTERVAL 6 HOUR),      ''),
( 8, 10, 2, '0', '2', DATE_SUB(NOW(), INTERVAL 25 MINUTE),   DATE_SUB(NOW(), INTERVAL 24 MINUTE),   DATE_SUB(NOW(), INTERVAL 20 MINUTE),   NULL,                                DATE_SUB(NOW(), INTERVAL 10 MINUTE),3.8, 113.321123, 23.128456, NOW(),                                  '0', 'admin', DATE_SUB(NOW(), INTERVAL 25 MINUTE),   '取餐中'),
( 9, 12, 8, '0', '1', DATE_SUB(NOW(), INTERVAL 12 MINUTE),   DATE_SUB(NOW(), INTERVAL 10 MINUTE),   NULL,                                 NULL,                                DATE_SUB(NOW(), INTERVAL 5 MINUTE), 1.8, 121.473701, 31.230416, DATE_SUB(NOW(), INTERVAL 5 MINUTE),   '0', 'admin', DATE_SUB(NOW(), INTERVAL 12 MINUTE),   '新骑手首单'),
(10,  4, 5, '1', '5', DATE_SUB(NOW(), INTERVAL 14 MINUTE),   NULL,                                 NULL,                                 NULL,                                NULL,                                NULL,   NULL,        NULL,       NULL,                                '0', 'admin', DATE_SUB(NOW(), INTERVAL 14 MINUTE),   '已取消演示');

-- ========== 9) 评价 8 条（每单最多 1 条，对应完成订单） ==========
INSERT INTO takeout_rating (rating_id, order_id, user_id, merchant_id, rider_id, merchant_score, rider_score, taste_score, packaging_score, delivery_score, content, taste_tags, is_anonymous, status, del_flag, create_by, create_time) VALUES
(1,  1, 1, 1, 1, 5, 5, 5, 4, 5, '味道很好，骑手小哥也专业，下次还会再来！',  '["好吃","分量足"]', '0', '0', '0', 'admin', NOW()),
(2,  2, 2, 2, 2, 4, 5, 4, 4, 5, '配送速度快，骑手态度好。',                  '["送得快"]',       '0', '0', '0', 'admin', NOW()),
(3,  7, 7, 1, 6, 5, 5, 5, 5, 4, '分量足，性价比超高，五星好评！',            '["实惠","好吃"]', '1', '0', '0', 'admin', NOW()),
(4,  9, 2, 5, 7, 5, 4, 5, 4, 4, '多肉葡萄头牌就是头牌！',                    '["必点"]',         '0', '0', '0', 'admin', NOW()),
(5,  1, 1, 1, 1, 5, 5, 4, 5, 5, '包装精美，分量足。',                        '["包装好"]',       '0', '0', '0', 'admin', NOW()),
(6,  2, 2, 2, 2, 3, 4, 3, 3, 4, '一般般，没想象中好。',                      '["一般"]',         '0', '0', '0', 'admin', NOW()),
(7,  7, 7, 1, 6, 5, 5, 5, 5, 5, '服务很贴心，会回购。',                      '["贴心"]',         '0', '0', '0', 'admin', NOW()),
(8,  9, 2, 5, 7, 4, 4, 5, 4, 4, '推荐给朋友了，真不错。',                    '["推荐"]',         '0', '0', '0', 'admin', NOW());

-- ========== 10) 优惠券 8 条 ==========
-- type 0=满减 1=折扣 2=免配送费 3=新人
INSERT INTO takeout_coupon (coupon_id, name, type, threshold_amount, discount_amount, discount_rate, max_discount, total_count, remain_count, per_user_limit, start_time, end_time, status, merchant_id, color, description, del_flag, create_by, create_time) VALUES
(1, '新人大礼包',     '3',  0.00, 15.00, NULL, NULL, 9999, 8500, 1, DATE_SUB(NOW(), INTERVAL 10 DAY), DATE_ADD(NOW(), INTERVAL 30 DAY), '0', NULL, '#FF6B6B', '新用户注册即领',                 '0', 'admin', NOW()),
(2, '满50减10',       '0', 50.00, 10.00, NULL, NULL, 5000, 4200, 2, DATE_SUB(NOW(), INTERVAL 5 DAY),  DATE_ADD(NOW(), INTERVAL 25 DAY), '0', NULL, '#4ECDC4', '满50元可用',                     '0', 'admin', NOW()),
(3, '满100减25',      '0',100.00, 25.00, NULL, NULL, 3000, 2800, 1, DATE_SUB(NOW(), INTERVAL 3 DAY),  DATE_ADD(NOW(), INTERVAL 27 DAY), '0', NULL, '#FFD93D', '满100元可用',                    '0', 'admin', NOW()),
(4, '9折折扣券',      '1', 30.00, NULL,    90, 30.00, 2000, 1800, 1, DATE_SUB(NOW(), INTERVAL 2 DAY),  DATE_ADD(NOW(), INTERVAL 20 DAY), '0', NULL, '#6BCB77', '9折封顶30元',                    '0', 'admin', NOW()),
(5, '免配送费券',     '2',  0.00,  6.00, NULL, NULL, 9999, 9000, 3, DATE_SUB(NOW(), INTERVAL 1 DAY),  DATE_ADD(NOW(), INTERVAL 60 DAY), '0', NULL, '#95E1D3', '免配送费',                       '0', 'admin', NOW()),
(6, '周末特惠',       '0', 40.00,  8.00, NULL, NULL, 1000,  650, 1, DATE_SUB(NOW(), INTERVAL 7 DAY),  DATE_ADD(NOW(), INTERVAL 7 DAY),  '0', NULL, '#F38181', '仅周末可用',                     '0', 'admin', NOW()),
(7, '夏日清凉券',     '0', 25.00,  5.00, NULL, NULL, 2000, 1500, 1, DATE_SUB(NOW(), INTERVAL 15 DAY), DATE_ADD(NOW(), INTERVAL 15 DAY), '0', NULL, '#3D5A80', '夏日限定',                       '0', 'admin', NOW()),
(8, '生日特权券',     '0',  0.00, 30.00, NULL, NULL,  500,  400, 1, DATE_SUB(NOW(), INTERVAL 30 DAY), DATE_ADD(NOW(), INTERVAL 365 DAY),'0', NULL, '#E98074', '生日当月专享',                   '0', 'admin', NOW());

-- ========== 11) 投诉 6 条 ==========
-- status 0=待审核 1=处理中 2=已退款 3=已驳回 4=已完成 5=已关闭
INSERT INTO takeout_complaint (complaint_id, order_id, user_id, merchant_id, rider_id, type, reason, refund_amount, approved_amount, status, handle_remark, handle_by, handle_time, del_flag, create_by, create_time) VALUES
(1, 1, 1, 1, 1, '0', '商家少送了一份薯条',           20.00, 20.00, '2', '已退款，赠送下次优惠券',  'admin', DATE_SUB(NOW(), INTERVAL 2 HOUR), '0', 'admin', DATE_SUB(NOW(), INTERVAL 3 HOUR)),
(2, 2, 2, 2, 2, '1', '汉堡凉了，口味不对',           15.00, 10.00, '4', '部分退款，商家道歉',      'admin', DATE_SUB(NOW(), INTERVAL 22 HOUR),'0', 'admin', DATE_SUB(NOW(), INTERVAL 23 HOUR)),
(3, 7, 7, 1, 6, '2', '配送超时 40 分钟',             12.00, 12.00, '2', '已退款，骑手扣分',        'admin', DATE_SUB(NOW(), INTERVAL 2 DAY),  '0', 'admin', DATE_SUB(NOW(), INTERVAL 2 DAY)),
(4, 9, 2, 5, 7, '3', '骑手服务态度差',               0.00,  0.00, '3', '已驳回，未发现违规',      'admin', DATE_SUB(NOW(), INTERVAL 4 HOUR), '0', 'admin', DATE_SUB(NOW(), INTERVAL 5 HOUR)),
(5, 1, 1, 1, 1, '0', '餐品质量有问题，怀疑不新鲜',   25.00, 15.00, '1', '正在与商家沟通',          'admin', NULL,                            '0', 'admin', DATE_SUB(NOW(), INTERVAL 1 HOUR)),
(6, 7, 7, 1, 6, '4', '包装破损严重',                 8.00,  8.00, '5', '用户已关闭投诉',          'admin', DATE_SUB(NOW(), INTERVAL 1 DAY),  '0', 'admin', DATE_SUB(NOW(), INTERVAL 2 DAY));

-- =============================================================
-- 完毕
-- 数据统计：
--   takeout_user          8 条
--   takeout_merchant      8 条
--   takeout_dish_category 4 条
--   takeout_dish         24 条
--   takeout_rider         8 条
--   takeout_order        12 条 + takeout_order_item 25 条
--   takeout_dispatch     10 条
--   takeout_rating        8 条
--   takeout_coupon        8 条
--   takeout_complaint     6 条
-- 共 11 张表，11 个菜单项立即可见
-- =============================================================
SELECT '=== 演示数据导入完成 ===' AS '';
SELECT 'C端用户' AS 模块, COUNT(*) AS 条数 FROM takeout_user
UNION ALL SELECT '商家', COUNT(*) FROM takeout_merchant
UNION ALL SELECT '菜品分类', COUNT(*) FROM takeout_dish_category
UNION ALL SELECT '菜品', COUNT(*) FROM takeout_dish
UNION ALL SELECT '骑手', COUNT(*) FROM takeout_rider
UNION ALL SELECT '订单', COUNT(*) FROM takeout_order
UNION ALL SELECT '订单明细', COUNT(*) FROM takeout_order_item
UNION ALL SELECT '派单', COUNT(*) FROM takeout_dispatch
UNION ALL SELECT '评价', COUNT(*) FROM takeout_rating
UNION ALL SELECT '优惠券', COUNT(*) FROM takeout_coupon
UNION ALL SELECT '投诉', COUNT(*) FROM takeout_complaint;
