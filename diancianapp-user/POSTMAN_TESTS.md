# 📮 POSTMAN_TESTS.md — 接口自测脚本

> 用 Postman / Apifox / curl 都能跑

## 1. 角色字典(无需登录)

```http
GET http://localhost:8080/takeout/user/roleDict
```

期望:
```json
{
  "code": 200,
  "data": {
    "user": "顾客", "kitchen": "后厨", "rider": "骑手", "admin": "管理员"
  }
}
```

---

## 2. 用户登录(各角色)

```http
POST http://localhost:8080/takeout/user/login
Content-Type: application/json

{"phone": "13800138001"}
```

期望: `data.userInfo.role = "kitchen"`

可换 phone 测试:
- `13800138001` → kitchen (张师傅)
- `13900139002` → rider (阿龙)
- `15000150003` → kitchen (Tom)
- `15200152005` → user (李雪)

---

## 3. 当前用户信息(需要 token)

```http
GET http://localhost:8080/takeout/user/me
Authorization: Bearer <token>
```

替换 `<token>` 为登录返回的 token。

---

## 4. 修改用户角色(管理员)

```http
PUT http://localhost:8080/takeout/user/changeRole/4/rider
Authorization: Bearer <admin_token>
```

期望: `code: 200`, 4 号用户 (Lily) 变成骑手

---

## 5. uniCloud 云函数测试

### 5.1 用户登录云函数

```http
POST https://<你的unicloud域名>/user-login
Content-Type: application/json

{"action":"loginByPhone","phone":"13800138001"}
```

### 5.2 后厨订单列表

```http
POST https://<你的unicloud域名>/zhouhao
Content-Type: application/json

{"action":"listKitchenOrders"}
```

### 5.3 创建订单

```http
POST https://<你的unicloud域名>/zhouhao
Content-Type: application/json

{
  "action": "createOrder",
  "userId": "user-005-lixue",
  "items": [
    {"dishId": "dish-001", "qty": 2},
    {"dishId": "dish-004", "qty": 1}
  ],
  "address": {"address": "北京东直门", "house_number": "1号"},
  "remark": "少辣"
}
```

期望:
```json
{
  "code": 0,
  "msg": "下单成功",
  "data": {
    "orderId": "...",
    "orderNo": "ORD...",
    "totalAmount": 68
  }
}
```

### 5.4 骑手抢单

```http
POST https://<你的unicloud域名>/rider-dispatch
Content-Type: application/json

{
  "action": "grabOrder",
  "orderId": "<上面返回的 orderId>",
  "riderId": "user-002-rider"
}
```

---

## 6. curl 一键脚本

```bash
# 1. 登录
TOKEN=$(curl -s -X POST http://localhost:8080/takeout/user/login \
  -H "Content-Type: application/json" \
  -d '{"phone":"13800138001"}' | jq -r .data.token)

# 2. 验证 token
curl -s http://localhost:8080/takeout/user/me \
  -H "Authorization: Bearer $TOKEN" | jq .
```
