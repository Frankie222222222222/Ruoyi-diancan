# 智能点餐系统 v2 — 角色分流版

> 📅 2026-10-10 完整重写版
> 🎯 核心: **用户 / 后厨 / 骑手 / 管理员** 4 角色分流
> 🔌 架构: `RuoYi 后端 (MySQL)` + `uniCloud (云函数 + 云数据库)` + `uni-app 小程序`

---

## 1. 项目结构

```
ruoyi-diancan/
├── RuoYi-Vue/                            # SpringBoot 后端 (MySQL)
│   └── ruoyi-takeout/                    # 外卖模块
│       ├── domain/TakeoutUser.java       # 实体(含 role 字段)
│       ├── enums/UserRoleEnum.java       # 角色枚举
│       ├── mapper/TakeoutUserMapper*     # MyBatis
│       ├── service/impl/TakeoutUser*     # 业务层(含 changeUserRole)
│       └── controller/TakeoutUser*       # REST(/takeout/user/...)
│
├── diancianapp-user/                     # uni-app 小程序 (顾客端+后厨+骑手)
│   ├── App.vue                           # 启动时恢复登录态
│   ├── pages/
│   │   ├── login/                        # 登录页(6 个测试账号一键登录)
│   │   ├── home/                         # 顾客首页
│   │   ├── order/                        # 订单页
│   │   ├── my/                           # 我的(显示角色+切换角色)
│   │   ├── kitchen-home/                 # 后厨工作台
│   │   ├── rider-home/                   # 骑手工作台
│   │   └── admin-home/                   # 管理员工作台
│   ├── common/
│   │   ├── storage.js                    # uniStorage 封装
│   │   ├── role.js                       # 角色常量 + 分流路由
│   │   ├── request.js                    # RuoYi REST 调用
│   │   └── unicloud.js                   # uniCloud 云函数调用
│   ├── store/index.js                    # Vuex(user/token/role)
│   ├── pages.json                        # 页面注册
│   ├── manifest.json                     # uni-app 清单
│   └── uniCloud-aliyun/                  # uniCloud
│       ├── cloudfunctions/
│       │   ├── zhouhao/                  # 后厨订单云函数
│       │   ├── rider-dispatch/           # 骑手抢单云函数
│       │   └── user-login/               # 用户登录云函数
│       └── database/
│           ├── takeout_user.schema.json
│           ├── takeout_order.schema.json
│           ├── takeout_dish.schema.json
│           └── db_init.json              # 初始化 8 个测试用户 + 6 个菜品
│
└── sql/                                  # MySQL 脚本
    ├── takeout_user.sql                  # ← 你已经执行
    ├── takeout_order.sql
    └── ...
```

---

## 2. 数据流图(角色分流)

```
                ┌──────────────┐
                │  小程序启动  │
                └──────┬───────┘
                       │
                       ▼
              ┌──────────────────┐
              │  读 storage      │
              │  还原 token+user │
              └────────┬─────────┘
                       │
                ┌──────┴──────┐
                │             │
            有 token      无 token
                │             │
                ▼             ▼
        ┌──────────┐   ┌──────────┐
        │ 读 role  │   │ 登录页   │
        └────┬─────┘   └────┬─────┘
             │              │
   ┌─────────┼─────────┐    │ 登录成功
   │         │         │    │
 user    kitchen   rider   │
   │         │         │    │
   ▼         ▼         ▼    ▼
┌──────┐ ┌────────┐ ┌────────┐
│首页  │ │后厨工作│ │骑手工作│
│点餐  │ │接单/出餐│ │抢单/送达│
└──────┘ └────────┘ └────────┘
```

---

## 3. 角色定义 (v2)

| role | 描述 | 默认入口 | 核心能力 |
|---|---|---|---|
| `user` | 顾客 | `/pages/home/home` | 点餐、查订单 |
| `kitchen` | 后厨 | `/pages/kitchen-home/kitchen-home` | 看订单、接单、出餐 |
| `rider` | 骑手 | `/pages/rider-home/rider-home` | 抢单、位置上报、送达 |
| `admin` | 管理员 | `/pages/admin-home/admin-home` | 全部 + RuoYi 后台 |

---

## 4. 测试账号

| 手机号 | 角色 | 昵称 | 用途 |
|---|---|---|---|
| 13800138001 | kitchen | 张师傅 | 后厨 1 |
| 13900139002 | rider | 阿龙 | 骑手 1 |
| 15000150003 | kitchen | Tom 大厨 | 后厨 2 |
| 15100151004 | user→rider | Lily | 骑手 2(可在"我的"切换) |
| 15200152005 | user | 李雪 | 顾客 |
| 15800158006 | user | 韩梅梅 | 顾客 |
| 18600186007 | user | 老王 | 顾客 |
| 18700187008 | user | Lucy | 顾客 |

---

## 5. 快速跑通(参考 RUN_BOOK.md)

```bash
# Step 1. MySQL 数据
mysql -uroot -p < sql/takeout_user.sql

# Step 2. 启动 RuoYi 后端
cd RuoYi-Vue
mvn spring-boot:run -pl ruoyi-admin

# Step 3. 部署 uniCloud
# 在 HBuilderX 中右键 uniCloud-aliyun → 上传云函数 + 上传数据库 Schema

# Step 4. 跑小程序
# HBuilderX → 运行 → 运行到微信小程序模拟器
```

---

## 6. 关键 API 速查

### RuoYi REST

| Method | URL | 说明 |
|---|---|---|
| POST | `/takeout/user/login` | 手机号登录(演示版) |
| GET  | `/takeout/user/me` | 当前用户信息 |
| PUT  | `/takeout/user/changeRole/{userId}/{role}` | 改角色(管理员) |
| GET  | `/takeout/user/roleDict` | 角色字典 |
| GET  | `/takeout/kitchen/list` | 后厨订单 |
| POST | `/takeout/rider/grab/{orderId}` | 骑手抢单 |
| GET  | `/takeout/rider/my` | 我的订单 |

### uniCloud 云函数

| 函数名 | action | 说明 |
|---|---|---|
| `user-login` | `loginByPhone` | 手机号登录 |
| `user-login` | `getUserById` | 查用户 |
| `user-login` | `getRoleDict` | 角色字典 |
| `zhouhao` | `createOrder` | 创建订单 |
| `zhouhao` | `listKitchenOrders` | 后厨订单 |
| `zhouhao` | `updateOrderStatus` | 更新状态 |
| `rider-dispatch` | `listAvailableOrders` | 可抢订单 |
| `rider-dispatch` | `grabOrder` | 抢单 |
| `rider-dispatch` | `myOrders` | 我的订单 |
| `rider-dispatch` | `markDelivered` | 标记送达 |
| `rider-dispatch` | `updateLocation` | 位置上报 |

---

## 7. 状态码约定

| 业务码 | 含义 |
|---|---|
| 0 | 成功 |
| 400 | 未知 action |
| 4001 | 必填参数缺失 |
| 4002 | 参数非法 |
| 4003 | 资源不存在 |
| 4004 | 资源被禁用 |
| 4005 | 菜品不存在 |
| 4006 | 数量非法 |
| 4007 | 订单不存在 |
| 4008 | 抢单失败(并发) |
| 500 | 服务器内部错误 |

---

## 8. 开发约定

- 所有云函数返回 `{ code, msg, data }` 三段式
- 持久化: `uniStorage` 存 token/user/role
- 错误统一通过 `uni.showToast({ icon: 'none' })` 提示
- 角色分流入口: `common/role.js` 的 `homePageByRole()`

---

## 9. 后续工作

- [ ] 支付模块(微信支付/支付宝)
- [ ] 实时位置(骑手 → 顾客)
- [ ] 评价系统
- [ ] 数据统计后台图表
- [ ] 推送消息
