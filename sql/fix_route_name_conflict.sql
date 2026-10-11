-- ============================================================
-- 修复菜单路由名冲突 (vue-router 不允许父子路由同名)
-- 父菜单"订单中心" path=order → routeName=Order
-- 子菜单 path=order → 改 route_name=OrderList (避免和父冲突)
-- 父菜单"骑手管理" path=rider → routeName=Rider
-- 子菜单 path=rider → 改 route_name=RiderList
-- ============================================================

UPDATE sys_menu SET route_name='OrderList'    WHERE menu_id=2001;  -- 订单管理（父级订单中心 path=order）
UPDATE sys_menu SET route_name='Statistics'   WHERE menu_id=2012;  -- 运营统计（父级工作台 path=dashboard）
UPDATE sys_menu SET route_name='Dish'         WHERE menu_id=2003;  -- 菜品管理
UPDATE sys_menu SET route_name='DishCategory' WHERE menu_id=2004;  -- 菜品分类
UPDATE sys_menu SET route_name='Merchant'     WHERE menu_id=2002;  -- 商家管理
UPDATE sys_menu SET route_name='DineTable'    WHERE menu_id=10041; -- 餐桌管理
UPDATE sys_menu SET route_name='RiderList'    WHERE menu_id=2005;  -- 骑手管理（父级骑手管理 path=rider）
UPDATE sys_menu SET route_name='User'         WHERE menu_id=2007;  -- 用户管理
UPDATE sys_menu SET route_name='Address'      WHERE menu_id=10047; -- 收货地址
UPDATE sys_menu SET route_name='Coupon'       WHERE menu_id=2008;  -- 优惠券
UPDATE sys_menu SET route_name='Rating'       WHERE menu_id=2009;  -- 评价管理
UPDATE sys_menu SET route_name='Complaint'    WHERE menu_id=2010;  -- 投诉管理
UPDATE sys_menu SET route_name='Dispatch'     WHERE menu_id=2006;  -- 调度中心
UPDATE sys_menu SET route_name='Kitchen'      WHERE menu_id=10035; -- 后厨管理

-- 5 个新增的（Print / RiderLocation / Banner / Announcement / Help）
-- 名字不冲突（父级 path 是 shop/rider/marketing/marketing/marketing），但仍然补一下
UPDATE sys_menu SET route_name='Print'         WHERE menu_name='打印扫码设置' AND route_name IS NULL;
UPDATE sys_menu SET route_name='RiderLocation' WHERE menu_name='骑手位置'     AND route_name IS NULL;
UPDATE sys_menu SET route_name='Banner'        WHERE menu_name='Banner管理'   AND route_name IS NULL;
UPDATE sys_menu SET route_name='Announcement'  WHERE menu_name='公告管理'     AND route_name IS NULL;
UPDATE sys_menu SET route_name='Help'          WHERE menu_name='帮助中心'     AND route_name IS NULL;

-- 验证
SELECT menu_id, menu_name, parent_id, path, route_name
FROM sys_menu
WHERE route_name IS NOT NULL
ORDER BY parent_id, order_num;
