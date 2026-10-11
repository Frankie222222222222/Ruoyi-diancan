-- e:\ruoyi-vue3\db\clean-v1.sql
-- 清理 v1 残留 takeout_* 表与菜单垃圾
USE ry-vue;
SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- 1) 旧版 takeout_category(v1 残留,v2 用 takeout_dish_category 替代)
-- 注意: 若 takeout_dish_category 已存在,需要先把 takeout_category 引用清掉
-- 简化: 若 takeout_category 存在但未被任何表引用,直接 drop
DROP TABLE IF EXISTS takeout_category;

-- 2) 残缺脏数据: 旧 v1 takeout_menu 留下的菜单垃圾(menu_id 在 2000~2200 且 path 不在白名单)
-- 注意: 不删 takeout_menu_v2.sql 用的 2044 父菜单及白名单内的菜单
DELETE FROM sys_role_menu
 WHERE menu_id IN (SELECT menu_id FROM sys_menu
                    WHERE menu_id BETWEEN 2000 AND 2200
                      AND path NOT IN ('takeout','merchantDish','merchant','dishCategory',
                                       'dish','order','category','rider','rating','coupon',
                                       'complaint','takeoutUser','statistics','dispatch',
                                       'dishSales','kitchen','dineTable'));
DELETE FROM sys_menu
 WHERE menu_id BETWEEN 2000 AND 2200
   AND path NOT IN ('takeout','merchantDish','merchant','dishCategory',
                    'dish','order','category','rider','rating','coupon',
                    'complaint','takeoutUser','statistics','dispatch',
                    'dishSales','kitchen','dineTable');

SET FOREIGN_KEY_CHECKS = 1;
SELECT 'clean-v1 OK' AS result;
