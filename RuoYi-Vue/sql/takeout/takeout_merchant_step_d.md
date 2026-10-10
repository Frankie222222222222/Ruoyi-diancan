# ���� �� �̼ҹ��� �� ǰ�� + Controller ����嵥

������� 7 �� d ��"ǰ�˴���"Ϊ��׼������Ϊ"�̼ҹ���"ģ�鲹�� 3 ���ļ���
֮���Ժ��Ҳ���ļ�����������Ϊ���������˵�"������"������ԭ�򡪡����û�� Controller��

## ? �����ļ�

| ��� | ·�� | ��; | �ؼ��� |
|---|---|---|---|
| 1 | `RuoYi-Vue/ruoyi-admin/src/main/java/com/ruoyi/web/controller/takeout/TakeoutMerchantController.java` | �̼ҹ������ Controller | 8 ���˵㣺list / getInfo / add / edit / remove / changeStatus / audit / export��ȫ���� `@PreAuthorize` + `@Log` |
| 2 | `RuoYi-Vue3/src/api/takeout/merchant.js` | �̼ҹ���ǰ�� API | 8 ��������listMerchant / getMerchant / addMerchant / updateMerchant / delMerchant / changeMerchantStatus / auditMerchant / exportMerchant |
| 3 | `RuoYi-Vue3/src/views/takeout/merchant/index.vue` | �̼ҹ���ǰ��ҳ�� | ������ + ������ + ���� + ����/�޸ĶԻ��� + ��˵��� + ��ҳ + ������ |

## ? Ȩ�ޱ�ʶ���� sys_menu һһ��Ӧ��

| �˵� ID | Ȩ�ޱ�ʶ | ��; | �˵� |
|---|---|---|---|
| 2001 | `takeout:merchant:list` | �б���ѯ | `GET /takeout/merchant/list` |
| 2010 | `takeout:merchant:query` | �̼����� | `GET /takeout/merchant/{id}` |
| 2011 | `takeout:merchant:add` | ���� | `POST /takeout/merchant` |
| 2012 | `takeout:merchant:edit` | �޸� | `PUT /takeout/merchant` |
| 2013 | `takeout:merchant:remove` | ɾ�� | `DELETE /takeout/merchant/{ids}` |
| 2014 | `takeout:merchant:export` | ���� Excel | `POST /takeout/merchant/export` |
| 2015 | `takeout:merchant:status` | Ӫҵ״̬�л� | `PUT /takeout/merchant/changeStatus` |
| 2016 | `takeout:merchant:audit` | ��פ��� | `PUT /takeout/merchant/audit` |

## ? ���� 3 ��

### �� 1 �� �� �������
```
IDEA��Maven Reload �� Rebuild �� ���� RuoYiApplication
�����У�
  cd E:\ruoyi-vue3\RuoYi-Vue\ruoyi-admin
  mvn spring-boot:run
```

������־���� `(????)?? ���������ɹ�` ? ��ʾ Controller ��ע�ᵽ Spring ������

### �� 2 �� �� �� Redis ��¼����
```
redis-cli -h 127.0.0.1 -p 6379 -a <����> KEYS "login_tokens:*" | xargs -r redis-cli DEL
```
������֮ǰ�����ۣ���һ���ǲ˵�����ʾ�����Ԫ�ס���

### �� 3 �� �� ǰ�� + ���µ�¼
```
�������Ctrl + Shift + R ǿˢ �� �˳� admin �� ���µ�¼
```
- ���Ӧ���� **���������� �� �̼ҹ�����**
- ���ȥ �� ������ʾ�����ݣ�����δ���ӣ�
- ����һ�� �� �޸� �� ɾ�� �� ��� �� ��Ӫҵ״̬ �� ȫ����ͨ

## ?? ҵ���������

| ���� | ��Դ | ʵ�� |
|---|---|---|
| �̼����� + del_flag ����Ψһ | `takeout_merchant.sql` ���� uk_merchant_name | Service.checkMerchantNameUnique |
| sys_user.user_id Ψһ�� | `takeout_merchant.sql` ���� uk_user_id | Service.checkBindUserUnique |
| �Ѱ��û����̼Ҳ�����ɾ�� | Service У�� | �� ServiceException |
| ����ʱ��˱�ע���� | ҵ��Լ�� | ǰ���Զ��� validator |
| Ӫҵ״̬ / ���״̬ �ֵ� | `takeout_merchant_dict.sql` | ǰ�� useDict + ��� BusinessStatusEnum / AuditStatusEnum |

## ? ����ģ��Ԥ�棨�����������˳��

| ģ�� | ״̬ | ��ע |
|---|---|---|
| 1. �̼ҹ��� | ? ������� | �ֵ� + �˵� + Ȩ�� + Service + Controller + ǰ�� |
| 2. ��Ʒ���� | ? ������ | ���� / ��Ʒ / ��� / ��� |
| 3. �������� | ? ������ | 6 ״̬ö�٣���֧��/���ӵ�/������/�����/��ȡ��/�˿��У�|
| 4. ���ֹ��� | ? ������ | �ӵ��� + ʵʱλ�� |
| 5. �û����� | ? ������ | C ���û� + �ջ���ַ + ���ﳵ |
| 6. ͳ�Ʊ��� | ? ������ | ���� ruoyi-system ECharts |
| 7. ϵͳ���� | ? �������� | ����Ҫ�½� |

## ? �Բ�ű�����ѡ��

�������� `admin/admin123` ��¼�������ֱ�����⼸�� URL��
```
http://localhost:8080/takeout/merchant/list?pageNum=1&pageSize=10
http://localhost:8080/takeout/merchant/1
```
�������� JSON ���� �� Controller OK������ 404 �� ·�����ԣ������������־��