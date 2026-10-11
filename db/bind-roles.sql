-- e:\ruoyi-vue3\db\bind-roles.sql
-- 绑定 takeout 菜单到 admin / 商家角色
USE ry-vue;
SET NAMES utf8mb4;

-- admin 角色(超级管理员 id=1)拿全部 takeout 菜单
INSERT IGNORE INTO sys_role_menu (role_id, menu_id)
SELECT 1, m.menu_id FROM sys_menu m
WHERE m.menu_id BETWEEN 2000 AND 2200
   OR m.perms LIKE 'takeout:%';

-- 商家角色(假设存在 id=100,若没有则跳过)
INSERT IGNORE INTO sys_role_menu (role_id, menu_id)
SELECT 100, m.menu_id FROM sys_menu m
WHERE m.path IN ('takeout','merchantDish','merchant','dishCategory','dish','order',
                 'dispatch','rating','complaint','statistics','dishSales')
  AND EXISTS (SELECT 1 FROM sys_role WHERE role_id = 100);

-- 普通管理员(若 ry_20260417 里有 id=2)
INSERT IGNORE INTO sys_role_menu (role_id, menu_id)
SELECT 2, m.menu_id FROM sys_menu m
WHERE m.menu_id BETWEEN 2000 AND 2200
  AND EXISTS (SELECT 1 FROM sys_role WHERE role_id = 2);

-- 验证
SELECT 'bind-roles OK' AS result,
      (SELECT COUNT(*) FROM sys_role_menu WHERE menu_id BETWEEN 2000 AND 2200) AS total_grants;
