# 外卖点餐端到端联调手册 v2 (2026-10-10)

## 1. 架构总览

```
┌──────────────┐  ┌──────────────┐  ┌──────────────┐
│  小程序顾客端 │  │  小程序后厨端 │  │  小程序骑手端 │
│  pages/home  │  │ subpackageRole │  │ subpackageRole│
│              │  │ /kitchen      │  │ /rider        │
└──────┬───────┘  └──────┬───────┘  └──────┬───────┘
       │  扫码/下单       │  接单/出餐        │  抢单/送达
       │                  │                  │
       ▼                  ▼                  ▼
┌──────────────┐  ┌──────────────┐  ┌──────────────┐
│  uniCloud    │  │  SpringBoot  │  │  SpringBoot  │
│  zhouhao云对象│──│  /takeout/   │  │  /takeout/   │
│  (扫码+桥接) │  │  kitchen/*   │  │  rider/*     │
└──────┬───────┘  └──────┬───────┘  └──────┬───────┘
       │                 │                  │
       │   Authorization: takeout-internal-secret
       │   X-Source: unicloud
       ▼                 ▼                  ▼
┌────────────────────────────────────────────────────┐
│           SpringBoot ruoyi-takeout 模块            │
│  + takeout_user(role 字段)                        │
│  + takeout_order(MAKING/READY 状态机)              │
└────────────────────────────────────────────────────┘
```

## 2. 角色分流启动

| 角色 | 启动入口 | 缓存 key |
|---|---|---|
| user (顾客) | `pages/home/home` | userInfo.role = 'user' |
| kitchen (后厨) | `subpackageRole/kitchen/kitchen` | userInfo.role = 'kitchen' |
| rider (骑手) | `subpackageRole/rider/rider` | userInfo.role = 'rider' |
| admin (管理员) | RuoYi 后台 webview | userInfo.role = 'admin' |

**App.vue onLaunch 决策流程**:
1. 读 `uni.getStorageSync('userInfo')`
2. 读 `userInfo.role`
3. role='kitchen' → reLaunch `/subpackageRole/kitchen/kitchen`
4. role='rider' → reLaunch `/subpackageRole/rider/rider`
5. role='user'/未登录 → 走默认 home

**切换身份入口**: 我的 → 切换身份 → dispatch 选角色

## 3. 后端 API 列表

### 3.1 C 端用户(/takeout/user/*)

| 方法 | 路径 | 鉴权 | 说明 |
|---|---|---|---|
| POST | /takeout/user/register | 匿名 | 注册(返 userId) |
| POST | /takeout/user/login | 匿名 | 登录(返 token + userInfo,**v2 加 role**) |
| GET | /takeout/user/me | JWT | 查当前用户 |
| PUT | /takeout/user/changeRole/{userId}/{role} | admin | **v2 新增** 改角色 |
| GET | /takeout/user/roleDict | 公开 | **v2 新增** 角色字典 |

### 3.2 订单(/takeout/order/*)

| 方法 | 路径 | 说明 |
|---|---|---|
| POST | /takeout/order | 创建订单(uniCloud 桥接) |
| GET | /takeout/order/{id} | 查订单详情 |
| GET | /takeout/order/byNo/{orderNo} | 按订单号查 |
| PUT | /takeout/order/status/{id}/{to} | 改状态(走状态机) |

### 3.3 后厨(/takeout/kitchen/*)

| 方法 | 路径 | 说明 |
|---|---|---|
| GET | /takeout/kitchen/list | 拉取所有进行中订单 |
| GET | /takeout/kitchen/dashboard | 待制/制作中/出餐待接 数量 |
| PUT | /takeout/kitchen/accept/{orderId}?kitchenId=xx | 接单 (PAID/ACCEPTED → MAKING) |
| PUT | /takeout/kitchen/ready/{orderId}?kitchenId=xx | 出餐完毕 (MAKING → READY) |

### 3.4 骑手(/takeout/rider/*)

| 方法 | 路径 | 说明 |
|---|---|---|
| GET | /takeout/rider/availableOrders | 可抢订单(status=READY) |
| PUT | /takeout/rider/grab/{orderId}?riderId=xx | 抢单 (READY → DELIVERING) |
| PUT | /takeout/rider/changeStatus/{riderId}/{status} | 改骑手状态 |

### 3.5 uniCloud 桥接(/unicloud/order/*)

| 方法 | 路径 | 鉴权 | 说明 |
|---|---|---|---|
| POST | /unicloud/order | Authorization: takeout-internal-secret | 创建订单(uniCloud 透传) |
| GET | /unicloud/order/byNo/{no} | 同上 | 按订单号查 |
| GET | /unicloud/order/{id} | 同上 | 按ID查 |

## 4. 状态机

```
        ┌──────┐
        │ 0 草稿 │(uniCloud 跳过)
        └───┬──┘
            ▼
        ┌──────┐
   ┌───│1 已支付│──┐
   │    └───┬──┘   │
   │  cancel│      │ 后厨 accept (kitchen)
   │        ▼      ▼
   │    ┌──────┐  ┌──────┐
   │    │7 取消 │  │2a制作中│
   │    └──────┘  └───┬──┘
   │                  │ 后厨 ready (kitchen)
   │                  ▼
   │              ┌──────┐
   │              │2b出餐 │ ← 骑手可见
   │              └───┬──┘
   │              骑手│grab (rider)
   │                  ▼
   │              ┌──────┐
   │              │3配送中│
   │              └───┬──┘
   │                  │ 骑手送达
   │                  ▼
   │              ┌──────┐
   │              │4已送达│
   │              └───┬──┘
   │                  │ 顾客确认/自动
   │                  ▼
   │              ┌──────┐
   │              │5完成  │
   │              └──────┘
   │
   └─→ cancel/refund 也可走其他状态
```

## 5. uniCloud → SpringBoot 鉴权

### 5.1 共享密钥
**两个地方要保持一致**:
- 后端 `application.yml` → `unicloud.internal-secret`
- uniCloud zhouhao 云对象 → 环境变量 `SPRINGBOOT_INTERNAL_SECRET`

默认开发值:`takeout-internal-secret-dev`

### 5.2 阿里云函数出口 IP 白名单
**生产必须配置**:
```yaml
unicloud:
  ip-whitelist: 47.96.0.0/16, 47.110.0.0/16
```

实际 IP 段以 https://help.aliyun.com/document_detail/179380.html 为准。

### 5.3 uniCloud 配置
在 `uniCloud Web 控制台` → 服务空间 → 云函数/云对象 → `zhouhao` → 配置:
- `SPRINGBOOT_URL` = `http://8.130.xxx.xxx:8080`(生产用 https)
- `SPRINGBOOT_INTERNAL_SECRET` = 与后端 application.yml 一致

## 6. 联调全链路

### 6.1 准备
1. 后端: 跑 `sql/takeout_v2_migration.sql` 完成数据库迁移
2. 后端: 启动 ruoyi-admin (8080)
3. 后端: 给一个测试用户改 role
   ```sql
   UPDATE takeout_user SET role='kitchen' WHERE user_id = 1001;
   UPDATE takeout_user SET role='rider'   WHERE user_id = 1002;
   ```
4. 小程序: HBuilderX → 运行到微信开发者工具
5. 登录测试用户(模拟后厨/骑手)

### 6.2 联调步骤
1. **顾客端**下单(走 uniCloud → SpringBoot)
   - 桌号扫码 → bindTable
   - 加菜 → 提交 → createFromCart → /unicloud/order
   - 订单状态: PAID (1)

2. **后厨端**接单 + 出餐
   - kitchen.vue 拉 `/takeout/kitchen/list` → 看到新订单
   - 点"接单" → `/takeout/kitchen/accept/1001?kitchenId=1001`
   - 状态: MAKING (2a)
   - 点"出餐完毕" → `/takeout/kitchen/ready/1001?kitchenId=1001`
   - 状态: READY (2b)

3. **骑手端**抢单 + 送达
   - rider.vue 拉 `/takeout/rider/availableOrders` → 看到 READY 订单
   - 点"抢单" → `/takeout/rider/grab/1001?riderId=1002`
   - 状态: DELIVERING (3)
   - 点"已送达" → /takeout/rider/changeStatus/...
   - 状态: DELIVERED (4) → COMPLETED (5)

4. **顾客端**轮询看进度
   - home.vue 轮询 `/unicloud/order/byNo/xxx` 看状态变化

## 7. 验证清单

- [ ] SQL 脚本执行完成
- [ ] 后端启动 8080 正常
- [ ] uniCloud zhouhao 重新上传(让 3 个新方法生效)
- [ ] 小程序分包 subpackageRole 加载正常
- [ ] 改角色接口 + role 字典返回正确
- [ ] login 返回 userInfo 含 role
- [ ] 切换身份 → 进对应工作台
- [ ] 后厨/骑手工作台能拉取订单
- [ ] 全状态机流转: PAID → MAKING → READY → DELIVERING → DELIVERED → COMPLETED
- [ ] 退款/取消路径也正常

## 8. 常见问题

### Q1: 小程序启动还是进 home,没进后厨
- A1: 检查 `uni.getStorageSync('userInfo').role` 是不是真的设了
- A2: 检查后端 login 返回 userInfo 含 role 字段
- A3: 在控制台 `console.log(userInfo)` 看值

### Q2: 401 鉴权错误
- 检查 Authorization 头有没有 `Bearer ` 前缀(后端要求**裸 secret**,无 Bearer)
- 检查 application.yml unicloud.internal-secret 是不是改了

### Q3: NoClassDefFoundError DishMapper(单测)
- 跑 `mvn -pl ruoyi-takeout clean test` 而不是直接 test

## 9. 部署清单

| 组件 | 部署位置 | 操作 |
|---|---|---|
| SpringBoot | 8.130.xxx.xxx:8080 | `mvn package` → 启动 jar |
| MySQL | 同上 | 跑 sql/takeout_v2_migration.sql |
| Redis | 同上 | 无变化 |
| uniCloud | aliyun | HBuilderX 上传 zhouhao |
| 小程序 | 微信 | 提交审核 → 发版 |

## 10. 联系

- 后端变更: Frank (ruoyi-takeout 模块)
- uniCloud: 看 zhouhao 云对象
- 小程序: pages.json 新增 subpackageRole
