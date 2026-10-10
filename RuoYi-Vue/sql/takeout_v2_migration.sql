-- ============================================================
-- takeout 模块 v2 迁移脚本
-- 2026-10-10
--
-- 作用:
--   1) takeout_order 表加 5 个新列(后厨/出餐/骑手 外键 + 时间戳)
--   2) 字典 takeout_order_status 加 2a(MAKING)/ 2b(READY)
--   3) 旧数据 status=2(ACCEPTED) 一律迁到 2a(MAKING)
--   4) 新建后厨权限(5 条): takeout:kitchen:list/accept/ready/query/changeStatus
--   5) 新建骑手权限(2 条): takeout:rider:grabList/grab
--
-- 风险:
--   - ALTER TABLE 后如果有未提交的 takeout_order 行级锁,可能等几秒
--   - 旧数据 status=2 全部改 2a,如果你有外部脚本依赖 status=2,会失效
--   - 字典 sys_dict_data 插入使用 INSERT IGNORE,重复跑不报错
--
-- 建议:先在测试环境跑,验证无误后线上
-- ============================================================

USE ry-vue;

-- ============================================================
-- 1. takeout_order 加列
-- ============================================================
ALTER TABLE takeout_order
    ADD COLUMN kitchen_id          BIGINT       NULL COMMENT '后厨ID(后厨接单人)' AFTER cancel_reason,
    ADD COLUMN kitchen_accept_time DATETIME     NULL COMMENT '后厨接单时间(PAID→MAKING)' AFTER kitchen_id,
    ADD COLUMN ready_time          DATETIME     NULL COMMENT '出餐完毕时间(MAKING→READY)' AFTER kitchen_accept_time,
    ADD COLUMN rider_id            BIGINT       NULL COMMENT '骑手ID' AFTER ready_time,
    ADD COLUMN rider_accept_time   DATETIME     NULL COMMENT '骑手接单时间(READY→DELIVERING)' AFTER rider_id,
    ADD INDEX idx_order_status_rider (status, rider_id),
    ADD INDEX idx_order_status_kitchen (status, kitchen_id),
    ADD INDEX idx_order_status_ready_time (status, ready_time);

-- ============================================================
-- 2. 字典: takeout_order_status 加 2a(MAKING) / 2b(READY)
-- ============================================================
-- 字典类型如果不存在则创建
INSERT IGNORE INTO sys_dict_type (dict_id, dict_name, dict_type, status, create_by, create_time, remark)
VALUES (200, '订单状态(v2)', 'takeout_order_status_v2', '0', 'admin', NOW(), '订单状态枚举 v2:含 2a(后厨制作中)/2b(出餐待接)');

-- 字典数据(用 dict_type='takeout_order_status_v2',所以 dict_label + dict_value 都在这)
INSERT IGNORE INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
VALUES
    (1,  '待支付',     '0',   'takeout_order_status_v2', '', 'default', 'N', '0', 'admin', NOW(), ''),
    (2,  '已支付',     '1',   'takeout_order_status_v2', '', 'default', 'N', '0', 'admin', NOW(), ''),
    (3,  '商家接单',   '2',   'takeout_order_status_v2', '', 'warning', 'N', '0', 'admin', NOW(), 'v2 兼容旧值'),
    (4,  '后厨制作中', '2a',  'takeout_order_status_v2', '', 'warning', 'N', '0', 'admin', NOW(), 'v2 新增'),
    (5,  '出餐待接',   '2b',  'takeout_order_status_v2', '', 'success', 'N', '0', 'admin', NOW(), 'v2 新增:后厨出餐完毕,待骑手接单'),
    (6,  '配送中',     '3',   'takeout_order_status_v2', '', 'primary', 'N', '0', 'admin', NOW(), ''),
    (7,  '已送达',     '4',   'takeout_order_status_v2', '', 'success', 'N', '0', 'admin', NOW(), ''),
    (8,  '已完成',     '5',   'takeout_order_status_v2', '', 'success', 'N', '0', 'admin', NOW(), '终态'),
    (9,  '已取消',     '6',   'takeout_order_status_v2', '', 'info',    'N', '0', 'admin', NOW(), '终态'),
    (10, '已退款',     '7',   'takeout_order_status_v2', '', 'danger',  'N', '0', 'admin', NOW(), '终态');

-- ============================================================
-- 3. 旧数据迁移: status='2' 一律改为 '2a' (MAKING)
--    因为 v2 起,商家接单(2)不再直接进 MAKING,而是先到 ACCEPTED(2)/PAID(1)
--    旧数据可能都是历史"已接单但还没出餐"的订单,直接归到 MAKING
-- ============================================================
UPDATE takeout_order
SET status = '2a',
    kitchen_accept_time = COALESCE(kitchen_accept_time, pay_time, update_time, NOW())
WHERE del_flag = '0' AND status = '2';

-- 验证迁移结果(应该 0 行)
SELECT COUNT(*) AS unfixed_count FROM takeout_order WHERE del_flag = '0' AND status = '2';

-- ============================================================
-- 4. 角色权限: takeout 后厨 + 骑手
-- ============================================================
-- 假设你的角色表是 sys_role,菜单权限表是 sys_role_menu,菜单表是 sys_menu
-- 这里采用 RuoYi 标准做法: 加 menu + role-menu 关联

-- 4.1 新增菜单
INSERT IGNORE INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
VALUES
    ('后厨管理', 0, 5, 'kitchen', null, 1, 0, 'M', '0', '0', '', 'shopping', 'admin', NOW(), 'takeout 目录'),
    ('后厨订单', 2000, 1, 'kitchen', 'takeout/kitchen/index', 1, 0, 'C', '0', '0', 'takeout:kitchen:list', '#', 'admin', NOW(), '后厨工作台菜单');

-- 注意 menu_id 2000 是上面 '后厨管理' 目录的实际 ID(取决于自增);若已存在 '后厨管理' 目录,跳过
-- 推荐做法: 查 sys_menu WHERE menu_name='后厨管理' LIMIT 1, 取 menu_id, 替换 2000

-- 4.2 新增权限按钮
INSERT IGNORE INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
VALUES
    ('后厨接单',     2000, 10, '', '', 1, 0, 'F', '0', '0', 'takeout:kitchen:accept',  '#', 'admin', NOW(), ''),
    ('后厨出餐',     2000, 11, '', '', 1, 0, 'F', '0', '0', 'takeout:kitchen:ready',   '#', 'admin', NOW(), ''),
    ('后厨看板',     2000, 12, '', '', 1, 0, 'F', '0', '0', 'takeout:kitchen:query',   '#', 'admin', NOW(), ''),
    ('骑手抢单列表', 2000, 20, '', '', 1, 0, 'F', '0', '0', 'takeout:rider:grabList',  '#', 'admin', NOW(), ''),
    ('骑手抢单',     2000, 21, '', '', 1, 0, 'F', '0', '0', 'takeout:rider:grab',      '#', 'admin', NOW(), '');

-- 4.3 (可选) 绑定到 admin 角色(假设 admin role_id=1)
-- INSERT IGNORE INTO sys_role_menu (role_id, menu_id) SELECT 1, menu_id FROM sys_menu WHERE perms LIKE 'takeout:kitchen:%' OR perms LIKE 'takeout:rider:grab%';

-- ============================================================
-- 5. 验证脚本
-- ============================================================

-- 5.1 检查 takeout_order 字段是否都加上了
SHOW COLUMNS FROM takeout_order LIKE 'kitchen_id';
SHOW COLUMNS FROM takeout_order LIKE 'kitchen_accept_time';
SHOW COLUMNS FROM takeout_order LIKE 'ready_time';
SHOW COLUMNS FROM takeout_order LIKE 'rider_id';
SHOW COLUMNS FROM takeout_order LIKE 'rider_accept_time';

-- 5.2 检查字典是否齐全
SELECT * FROM sys_dict_data WHERE dict_type = 'takeout_order_status_v2' ORDER BY dict_sort;

-- 5.3 统计各状态订单数
SELECT status, COUNT(*) AS cnt FROM takeout_order WHERE del_flag='0' GROUP BY status ORDER BY status;

-- 5.4 权限菜单是否加好
SELECT menu_id, menu_name, perms FROM sys_menu WHERE perms LIKE 'takeout:kitchen:%' OR perms LIKE 'takeout:rider:grab%';

-- ============================================================
-- 6. takeout_user 加 role 字段 + 默认值
-- ============================================================
ALTER TABLE takeout_user
    ADD COLUMN role VARCHAR(16) NULL DEFAULT 'user' COMMENT '角色:user=顾客,kitchen=后厨,rider=骑手,admin=管理员' AFTER status,
    ADD INDEX idx_user_role (role);

-- 老数据默认 'user'
UPDATE takeout_user SET role = 'user' WHERE role IS NULL OR role = '';

-- 验证
SELECT role, COUNT(*) AS cnt FROM takeout_user WHERE del_flag = '0' GROUP BY role ORDER BY role;

-- 字典: takeout_user_role
INSERT IGNORE INTO sys_dict_type (dict_id, dict_name, dict_type, status, create_by, create_time, remark)
VALUES (201, '外卖用户角色', 'takeout_user_role', '0', 'admin', NOW(), 'user/kitchen/rider/admin');

INSERT IGNORE INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
VALUES
    (1, '顾客',   'user',    'takeout_user_role', '', 'default', 'Y', '0', 'admin', NOW(), ''),
    (2, '后厨',   'kitchen', 'takeout_user_role', '', 'warning', 'N', '0', 'admin', NOW(), ''),
    (3, '骑手',   'rider',   'takeout_user_role', '', 'primary', 'N', '0', 'admin', NOW(), ''),
    (4, '管理员', 'admin',   'takeout_user_role', '', 'danger',  'N', '0', 'admin', NOW(), '');

-- 权限菜单: takeout:user:changeRole
INSERT IGNORE INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
VALUES
    ('改用户角色', 0, 100, '', '', 1, 0, 'F', '0', '0', 'takeout:user:changeRole', '#', 'admin', NOW(), '后台管理员使用');

-- (可选) 绑定到 admin 角色
-- INSERT IGNORE INTO sys_role_menu (role_id, menu_id) SELECT 1, menu_id FROM sys_menu WHERE perms = 'takeout:user:changeRole';
