-- =============================================================
-- ����ҵ�� �� �̼ҹ��� �� ��ɫ��Ȩ SQL
-- �� 2001~2016 �� 9 ���˵�����������Ŀ¼+�̼ҹ����˵�+7����ť��
-- ��������ɫ��admin(role_id=1) ��������Ĭ�� bypass��������Ȩ��
-- ���ű��ص����ͨ��ɫ role_id=2 ��Ȩ������㽨���Զ����ɫ��
-- �� WHERE role_id = 2 �������Ǹ� role_id ���ɡ�
-- =============================================================

-- ----------------------------
-- 1����ϣ��ȿ��������м�����ɫ������ʲô
-- ----------------------------
-- SELECT role_id, role_name, role_key, status FROM sys_role ORDER BY role_id;

-- ----------------------------
-- 2������������Ա��role_id=1����Ȩ
--    ע�⣺������ admin ͨ�� isAdmin() ��·��죬
--    ���������Ҳ��һ�飬����ĳ��ĳ��ϸ�ģʽ�󷭳�
-- ----------------------------
INSERT IGNORE INTO sys_role_menu(role_id, menu_id)
SELECT 1, m.menu_id
FROM sys_menu m
WHERE m.menu_id BETWEEN 2001 AND 2099
  AND m.del_flag = '0';

-- ----------------------------
-- 3������ͨ��ɫ��role_id=2������ͨ��ɫ������Ȩ
--    ���ͣ�2099 ��Ԥ��������Ŀ¼ 2000 ������λ��
--    BETWEEN 2000 AND 2099 һ���԰������������� + �Ӳ˵� + ��ť ������
-- ----------------------------
INSERT IGNORE INTO sys_role_menu(role_id, menu_id)
SELECT 2, m.menu_id
FROM sys_menu m
WHERE m.menu_id BETWEEN 2000 AND 2099
  AND m.del_flag = '0';

-- ----------------------------
-- 4������ѡ�����Զ����ɫ��Ȩʾ��
--    �����㽨��һ�����̼ҹ���Ա����ɫ���������� role_id=100��
--    �������ע�Ͳ�ִ�У�
-- ----------------------------
-- INSERT IGNORE INTO sys_role_menu(role_id, menu_id)
-- SELECT 100, m.menu_id
-- FROM sys_menu m
-- WHERE m.menu_id BETWEEN 2000 AND 2099
--   AND m.del_flag = '0';

-- ----------------------------
-- 5��У�飺��ͨ��ɫ role_id=2 �����õ��˶����������˵�
-- ----------------------------
-- SELECT m.menu_id, m.menu_name, m.menu_type, m.perms
-- FROM sys_role_menu rm
-- JOIN sys_menu m ON rm.menu_id = m.menu_id
-- WHERE rm.role_id = 2
--   AND m.menu_id BETWEEN 2000 AND 2099
-- ORDER BY m.menu_id;