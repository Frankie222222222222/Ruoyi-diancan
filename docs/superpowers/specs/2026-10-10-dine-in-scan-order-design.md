# 堂食扫码点餐 (Dine-In Scan-Order) Design

**Date:** 2026-10-10
**Status:** Approved (brainstormed)
**Scope:** RuoYi-Vue/ruoyi-takeout 模块新增堂食点餐能力

## 业务目标

顾客到店 → 扫桌上二维码 → H5 进入点餐页 → 选菜下单 + 支付 → 后厨接单做菜 → 菜上桌 → 完结。

不需要骑手，不调度。同一桌多人扫码可加单（支付后关单）。

## 关键决策（已对齐）

| 维度 | 决定 |
|------|------|
| 场景 | 堂食（dine-in） |
| 桌号粒度 | 每张桌子独立 |
| 会话合并 | 同一桌可追加，支付前都是 DRAFT |
| 支付时机 | 每点一道就结账一次（多笔独立支付） |
| 二维码 | URL 含 tableId，前端拼装 |
| 后厨接单 | 复用现有 kitchen 流程 |
| 骑手 | 不参与 |
| 订单表 | takeout_order 加 order_type + table_id 列 |
| 优惠券 | 复用现有 takeout_coupon |

## 数据模型改动

### 1. 新表 `takeout_dine_table`

```sql
CREATE TABLE takeout_dine_table (
  table_id      BIGINT PRIMARY KEY AUTO_INCREMENT,
  merchant_id   BIGINT NOT NULL,
  table_no      VARCHAR(20) NOT NULL,         -- "A12" "VIP-3"
  capacity      INT DEFAULT 4,
  status        CHAR(1) DEFAULT '0',          -- 0空闲 1就餐中 2已结
  qr_url        VARCHAR(255),                 -- 生成的完整 URL
  remark        VARCHAR(255),
  del_flag      CHAR(1) DEFAULT '0',
  create_by     VARCHAR(64),
  create_time   DATETIME,
  update_by     VARCHAR(64),
  update_time   DATETIME,
  UNIQUE KEY uk_merchant_tableno (merchant_id, table_no)
);
```

### 2. 改表 `takeout_order` 加列

```sql
ALTER TABLE takeout_order
  ADD COLUMN order_type TINYINT NOT NULL DEFAULT 0,    -- 0=外卖 1=堂食
  ADD COLUMN table_id BIGINT NULL,                     -- 堂食专用
  ADD INDEX idx_table (table_id),
  ADD INDEX idx_order_type (order_type);
```

> `delivery_address` / `dispatch_*` 列对堂食为 NULL。

### 3. 不新建 `takeout_dine_order` 独立表

## 状态机（堂食订单）

```
DRAFT (点菜中) ──支付──> PAID ──后厨接单──> KITCHEN_ACCEPT ──出餐──> READY ──上桌确认──> DONE
   │                       │                    │                       │
   └──── cancel ───────────┴────────────────────┴───────────────────────┴───> CANCELED
```

注意：与外卖 `OrderStatusEnum` 复用，但**堂食不使用** `DISPATCHING / DELIVERING / DELIVERED` 这些态。

`order_type=1` 的订单：
- DRAFT：顾客点菜但未支付（仅此状态允许 edit/add）
- PAID：支付完成，推到后厨
- KITCHEN_ACCEPT：后厨接单
- READY：出餐
- DONE：服务员确认上桌
- CANCELED：取消

## 模块结构

### 新增 controller
`TakeoutDineTableController` `/takeout/dineTable/*`（管理端，CRUD + 生成二维码）

### 新增端点（顾客端，挂在 `/takeout/dineIn/*`）

| Method | Path | 说明 | 权限 |
|--------|------|------|------|
| GET | `/takeout/dineIn/menu?tableId=X` | 进店拉菜品菜单（公开） | permitAll |
| POST | `/takeout/dineIn/order` | 顾客点菜下单（生成 DRAFT 订单） | takeout:order:add |
| GET | `/takeout/dineIn/order/{orderNo}` | 查自己堂食订单 | takeout:order:query |
| PUT | `/takeout/dineIn/order/{orderId}/addItem` | DRAFT 状态追加菜品 | takeout:order:edit |
| POST | `/takeout/dineIn/order/{orderId}/pay` | 触发支付 | takeout:order:add |
| PUT | `/takeout/dineIn/order/{orderId}/confirmServed` | 顾客点"已上桌" → DONE | takeout:order:edit |
| PUT | `/takeout/dineIn/order/{orderId}/cancel` | 取消订单 | takeout:order:cancel |
| GET | `/takeout/dineIn/table/{tableId}` | 桌子信息 + 当前进行中订单 | permitAll |

### 二维码生成
- 在 `TakeoutDineTableController.add` 时调用 `QrCodeService` 生成 PNG → 上传 MinIO
- 字段 `qr_url` 存的是图片 URL（顾客扫码后跳 `${H5_BASE}/dine-in?tableId={tableId}`）

## 业务逻辑关键点

### 1. 状态机校验
- 仅 DRAFT 可调 addItem / pay
- 仅 PAID 状态可被 kitchen 接受
- DRAFT 状态支付前不创建支付单（创建 TakeoutOrder 后立即 update pay 是不合理的——参考 TakeoutOrderServiceImpl）

### 2. 桌台状态联动
- 创建 DRAFT 订单：`table.status = 1`（就餐中）
- 桌所有订单 DONE/CANCELED：`table.status = 0`（空闲）

### 3. 与外卖接口兼容
- `TakeoutOrderServiceImpl.createOrder()` 增加分支：orderType=1 走堂食路径（不写 delivery_address，不创建 dispatch）
- 现有 `changeStatus` 校验：堂食订单禁用某些外卖专属状态

### 4. 支付复用
- DRAFT → PAID：复用 `TakeoutPaymentServiceImpl.create()`
- 支付金额 = 当前订单总额（包含 addItem 累计）

CANCELED 任何阶段都可触发（顾客手动 / 后厨拒单 / 管理端取消）。
- DRAFT 状态：顾客 30 分钟未支付，系统定时任务（Quartz）自动 → CANCELED，桌台释放。
- PAID 之后：仅管理端可取消，触发退款。
- CANCELED 订单不计入营业额统计。

## 边界 / 不做

- ❌ 服务员端 app（顾客自助点餐即可）
- ❌ 桌台转台 / 并桌
- ❌ 堂食专属优惠券
- ❌ 库存不足自动停售（保持现状，库存可卖负数）
- ❌ 多语言

## 风险

1. **迁移**：takeout_order 加列有现有数据，需 default 0 兼容
2. **二维码图片**：MinIO 上传失败时降级为返回 tableId 让前端生成
3. **状态机错乱**：cancel / pay 竞态 → 用 `version` 字段或悲观锁（`select ... for update`）

## 验证

- [ ] 单元：TakeoutDineTableService CRUD + 唯一约束
- [ ] 单元：TakeoutOrderService 新增 orderType 分支
- [ ] 集成：扫码 → 选菜 → 支付 → 后厨接单 → 出餐 → 确认上桌 端到端
- [ ] 集成：同桌追加菜品（同桌第 2 单 DRAFT）
- [ ] 集成：取消订单 → 桌台释放回空闲
