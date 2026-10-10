-- =============================================================
-- һ����ϣ����������˵��Ƿ��ڽ�ɫȨ����
-- �ѽ������ AI ���ɶ�λ����
-- =============================================================

SELECT '===== 1. ������ز˵��Ƿ���� =====' AS step;
SELECT menu_id, parent_id, menu_name, menu_type, perms, visible, del_flag
FROM sys_menu
WHERE menu_id BETWEEN 2000 AND 2099
ORDER BY menu_id;

SELECT '===== 2. ��ͨ��ɫ(role_id=2) �Ƿ��õ������˵� =====' AS step;
SELECT rm.role_id, m.menu_id, m.menu_name, m.menu_type, m.perms
FROM sys_role_menu rm
JOIN sys_menu m ON rm.menu_id = m.menu_id
WHERE rm.role_id = 2
  AND m.menu_id BETWEEN 2000 AND 2099
ORDER BY m.menu_id;

SELECT '===== 3. ����(role_id=1) �Ƿ��õ������˵� =====' AS step;
SELECT rm.role_id, m.menu_id, m.menu_name, m.menu_type
FROM sys_role_menu rm
JOIN sys_menu m ON rm.menu_id = m.menu_id
WHERE rm.role_id = 1
  AND m.menu_id BETWEEN 2000 AND 2099
ORDER BY m.menu_id;

SELECT '===== 4. ��ǰ�˺ŵĽ�ɫ�� =====' AS step;
SELECT u.user_name, ur.role_id, r.role_name, r.role_key
FROM sys_user u
LEFT JOIN sys_user_role ur ON u.user_id = ur.user_id
LEFT JOIN sys_role r ON ur.role_id = r.role_id
WHERE u.user_name IN ('admin', 'ry');

SELECT '===== 5. �̼ҹ����˵��� component ·�� =====' AS step;
SELECT menu_id, menu_name, path, component, route_name, visible
FROM sys_menu
WHERE menu_id IN (2000, 2001);

-- =============================================================
-- ���������
-- 1) ���� 9 �У�2000/2001/2010~2016����visible='0' ��ʾ��ʾ
-- 2) admin Ӧ�� 9 �У���·�����ʵ����Ҫ��������ȨҲ�У�
--    ry Ӧ�� 9 �У�������Ȩ����Ȼ��������
-- 3) admin Ӧ�� 9 ��
-- 4) admin �� role_id=1 �� admin��ry �� role_id=2 �� common
-- 5) 2000 path=takeout component=NULL��2001 path=merchant component=takeout/merchant/index
-- =============================================================