# C 端 Token 注入 Role-Based Permissions

**Date:** 2026-10-10
**Status:** Approved
**Scope:** RuoYi-Vue/ruoyi-takeout 模块，C 端用户 token 鉴权

## 背景

当前 `TakeoutTokenService.login()` 把 `LoginUser.permissions` 设为 `Collections.emptySet()`，导致所有带 `@PreAuthorize("@ss.hasPermi('takeout:xxx')")` 的 C 端接口对 C 端 token 永远 403。

后果：v2 流程（后厨接单 / 骑手抢单）只有 B 端 admin token 能跑通。C 端 `kitchen` / `rider` 角色实际上没有 API 权限。

## 目标

登录时按 `takeout_user.role` 字段注入对应权限字符串集合到 `LoginUser.permissions`，使 C 端 token 能正常通过 `@PreAuthorize` 校验。

## 设计

### 1. ROLE_PERMS 映射（写入 TakeoutTokenService）

| Role | 注入的 permissions |
|------|-------------------|
| `user` | order:list/query/add/cancel/export, rating:list/query, complaint:list/query, coupon:list/query/receive/userCoupon/bestForOrder, user:query, user:list |
| `kitchen` | kitchen:list/accept/ready/query, order:list/query, dish:query, user:query |
| `rider` | rider:list/available/availableOrders/grab, dispatch:list/query/accept/pickup/complete/cancel, order:query, riderLocation:report/active, user:query |
| `merchant` | dish:list/query/add/edit/remove/changeStatus/adjustStock/topSales, dishCategory:*（5 个）, order:list/query/changeStatus, rating:list/query/reply, statistics:list |
| `admin` | `takeout:*:*`（通配符；如框架不支持则列全） |

### 2. 改动点

- `TakeoutTokenService.java`
  - 新增 `private static final Map<String, Set<String>> ROLE_PERMS = ...`
  - `login()` 方法里：`loginUser.setPermissions(ROLE_PERMS.getOrDefault(role, Set.of()))`
  - 取 role：从 `user.getRole()` 读

### 3. 不动

- `LoginUser` 类（permissions 字段已存在）
- `@PreAuthorize` 注解（保持原样）
- SecurityConfig 匿名白名单
- 数据库 schema
- `incrementStats` / `changeRole` 仍保留 admin 专用

### 4. 通配符处理

先确认 ruoyi `hasPermi` 是否支持 `takeout:*:*` 通配：
- 支持 → admin 直接用通配
- 不支持 → 列全所有 `takeout:*` 权限字符串

实现时先试通配，不行就全列。

## 风险

- **过度授权**：merchant 角色能看所有商家订单。v1 范围可接受。
- **角色未配置**：未匹配 role 走 `Set.of()`，相当于 C 端 token 啥都干不了（= 当前的 bug 状态）。这是预期的安全默认。
- **通配符行为**：先验证后写。

## 验证

1. 单元测试 `TakeoutTokenServiceTest`：4 种 role 登录后 permissions 集合正确
2. 集成测试：
   - C 端 user token → `POST /takeout/order` ✅ 200
   - C 端 user token → `PUT /takeout/kitchen/accept` ❌ 403
   - C 端 kitchen token → `PUT /takeout/kitchen/accept` ✅ 200
   - C 端 rider token → `PUT /takeout/rider/grab/{id}` ✅ 200
3. 已有用户（admin/kitchen1/rider1）登录后 permissions 大小 > 0

## 不在范围

- 数据级权限（merchant 只能看自己商家的订单）— v3 再做
- `@PreAuthorize` 注解的移除或重构
- unicloud 旁路鉴权（`/unicloud/order/**` 仍然匿名）
