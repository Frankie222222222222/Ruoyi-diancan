-- ==========================================================
-- 外卖业务测试数据（按真实表结构）
-- 商家表 takeout_merchant：字段是 user_id 不是 bind_user_id
-- 分类表 takeout_dish_category：category_name / sort_order / status
-- 菜品表 takeout_dish：dish_name / category_id / image / price / stock / sales / status / description
-- ==========================================================
SET NAMES utf8mb4;

-- 0) 兜底：菜品表若不存在则创建
CREATE TABLE IF NOT EXISTS takeout_dish (
  dish_id      BIGINT       NOT NULL AUTO_INCREMENT COMMENT '菜品ID',
  dish_name    VARCHAR(100) NOT NULL COMMENT '菜品名称',
  category_id  BIGINT       DEFAULT NULL COMMENT '分类ID',
  image        VARCHAR(255) DEFAULT NULL COMMENT '图片URL',
  price        DECIMAL(10,2) DEFAULT 0 COMMENT '价格',
  stock        INT          DEFAULT 0  COMMENT '库存',
  sales        INT          DEFAULT 0  COMMENT '销量',
  status       CHAR(1)      DEFAULT '1' COMMENT '状态 0下架 1上架',
  description  VARCHAR(500) DEFAULT NULL COMMENT '描述',
  remark       VARCHAR(500) DEFAULT NULL COMMENT '备注',
  create_by    VARCHAR(64)  DEFAULT NULL,
  create_time  DATETIME     DEFAULT CURRENT_TIMESTAMP,
  update_by    VARCHAR(64)  DEFAULT NULL,
  update_time  DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (dish_id) USING BTREE,
  KEY idx_category (category_id),
  KEY idx_status (status)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COMMENT='菜品表';

-- 1) 清旧测试数据（防止重复插入）
DELETE FROM takeout_dish;
DELETE FROM takeout_dish_category;
DELETE FROM takeout_merchant;

-- 2) 菜品分类（4 个）
INSERT INTO takeout_dish_category (category_id, category_name, sort_order, status, create_by, create_time, remark) VALUES
(1, '热销推荐', 1, '1', 'admin', NOW(), '店铺热销'),
(2, '招牌主食', 2, '1', 'admin', NOW(), '招牌饭面'),
(3, '美味小吃', 3, '1', 'admin', NOW(), '小食'),
(4, '饮品酒水', 4, '0', 'admin', NOW(), '暂时下架');

-- 3) 商家（3 个，user_id 留 NULL）
-- status: 0=营业中 1=已打烊
-- audit_status: 0=待审核 1=通过 2=驳回
INSERT INTO takeout_merchant (
    merchant_id, merchant_name, contact_name, contact_phone, address, logo, business_license,
    status, audit_status, audit_remark, user_id, del_flag, create_by, create_time, remark
) VALUES
(1, '老王川菜馆', '王老板', '13800138001', '北京市朝阳区建国路88号', 'https://img.zcool.cn/community/01a87d5e5e5e5a110.jpg', 'https://img.zcool.cn/license1.jpg',
 '0', '1', '资料齐全，准予通过', NULL, '0', 'admin', NOW(), '金牌商家'),

(2, '粤式茶餐厅', '李老板', '13800138002', '上海市浦东新区世纪大道100号', 'https://img.zcool.cn/community/01a87d5e5e5e5a111.jpg', 'https://img.zcool.cn/license2.jpg',
 '0', '1', '审核通过', NULL, '0', 'admin', NOW(), '银牌商家'),

(3, '兰州牛肉面', '马老板', '13800138003', '广州市天河区珠江新城50号', 'https://img.zcool.cn/community/01a87d5e5e5e5a112.jpg', 'https://img.zcool.cn/license3.jpg',
 '0', '0', NULL, NULL, '0', 'admin', NOW(), '待审核商家');

-- 4) 菜品（10 个，关联分类 id）
-- status: 0=下架 1=上架
INSERT INTO takeout_dish (dish_id, dish_name, category_id, image, price, stock, sales, status, description, create_by, create_time, remark) VALUES
(1, '宫保鸡丁',       1, 'https://img.zcool.cn/community/dish_gongbao.jpg',  28.00, 100, 256, '1', '经典川菜，鸡肉嫩滑，花生酥脆',         'admin', NOW(), ''),
(2, '麻婆豆腐',       1, 'https://img.zcool.cn/community/dish_mapo.jpg',     18.00, 150, 412, '1', '麻辣鲜香，下饭神器',                   'admin', NOW(), ''),
(3, '鱼香肉丝',       1, 'https://img.zcool.cn/community/dish_yuxiang.jpg',  26.00, 120, 198, '1', '酸甜微辣，开胃爽口',                   'admin', NOW(), ''),
(4, '扬州炒饭',       2, 'https://img.zcool.cn/community/dish_yangzhou.jpg', 15.00, 200, 520, '1', '粒粒分明，配料丰富',                   'admin', NOW(), ''),
(5, '担担面',         2, 'https://img.zcool.cn/community/dish_dandan.jpg',   16.00, 180, 360, '1', '成都名小吃，麻辣鲜香',                 'admin', NOW(), ''),
(6, '小笼包',         3, 'https://img.zcool.cn/community/dish_xiaolong.jpg', 22.00, 200, 480, '1', '皮薄汁多，咬一口满嘴鲜',               'admin', NOW(), ''),
(7, '煎饺',           3, 'https://img.zcool.cn/community/dish_jianjiao.jpg', 12.00, 250, 380, '1', '底部金黄酥脆，馅料鲜香',               'admin', NOW(), ''),
(8, '可乐',           4, 'https://img.zcool.cn/community/dish_cola.jpg',      5.00, 500,1200, '0', '冰镇可乐更爽',                         'admin', NOW(), '暂时下架'),
(9, '酸梅汤',         4, 'https://img.zcool.cn/community/dish_suanmei.jpg',   8.00, 300, 800, '0', '老北京酸梅汤',                         'admin', NOW(), '暂时下架'),
(10, '招牌套餐A',     1, 'https://img.zcool.cn/community/dish_taocan.jpg',   38.00,  80, 150, '1', '宫保鸡丁+米饭+酸梅汤，超值推荐',       'admin', NOW(), '套餐');

-- 5) 验证
SELECT '=== 商家 ===' AS '';
SELECT merchant_id, merchant_name, status, audit_status FROM takeout_merchant ORDER BY merchant_id;

SELECT '=== 菜品分类 ===' AS '';
SELECT category_id, category_name, sort_order, status FROM takeout_dish_category ORDER BY sort_order;

SELECT '=== 菜品 ===' AS '';
SELECT dish_id, dish_name, category_id, price, status FROM takeout_dish ORDER BY dish_id;
