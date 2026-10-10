# 🚀 RUN_BOOK — 从零跑通智能点餐系统 v2

> 适用时间: 2026-10-10 之后
> 适用对象: 这台电脑 + 你 + 我
> 预计耗时: 30-45 分钟(主要是 uniCloud 部署)

---

## 阶段 0: 前置检查

```powershell
# PowerShell
node --version          # >= 14
mvn --version           # >= 3.6
java --version          # >= 1.8
mysql --version         # >= 5.7
```

✅ 你的环境已经 OK
- Node 已有
- Maven 已有
- Java 已有
- MySQL 8 已有

---

## 阶段 1: MySQL 数据库 (5 分钟)

### 1.1 已完成 ✅
你已经在 `ry-vue` 库执行了 `takeout_user.sql`,并把 8 个测试用户设好角色。

### 1.2 验证

```sql
USE ry-vue;
SELECT user_id, nickname, phone, role, status FROM takeout_user ORDER BY user_id;
```

应该看到 8 条记录,其中:
- `13800138001` (张师傅) role=`kitchen`
- `13900139002` (阿龙) role=`rider`

### 1.3 执行剩余 SQL (如果还没做)

```bash
# 这些是为了 RuoYi 后端能跑
mysql -uroot -p ry-vue < e:\ruoyi-vue3\sql\takeout_order.sql
mysql -uroot -p ry-vue < e:\ruoyi-vue3\sql\takeout_dish.sql
mysql -uroot -p ry-vue < e:\ruoyi-vue3\sql\takeout_payment.sql
mysql -uroot -p ry-vue < e:\ruoyi-vue3\sql\takeout_dispatch_supplement.sql
mysql -uroot -p ry-vue < e:\ruoyi-vue3\sql\takeout_supplement_no_if_exists.sql
```

---

## 阶段 2: 启动 RuoYi 后端 (5 分钟)

### 2.1 用 IDEA 打开

```
File → Open → E:\ruoyi-vue3\RuoYi-Vue
```

### 2.2 启动

```
找到 RuoYiApplication.java
右键 → Run
```

端口: `8080`

### 2.3 验证后端能响应

浏览器访问:
```
http://localhost:8080/takeout/user/roleDict
```

期望返回:
```json
{
  "code": 200,
  "msg": "操作成功",
  "data": {
    "user": "顾客",
    "kitchen": "后厨",
    "rider": "骑手",
    "admin": "管理员"
  }
}
```

✅ 如果能拿到这个,后端 OK

### 2.4 测试登录接口

```bash
curl -X POST http://localhost:8080/takeout/user/login \
  -H "Content-Type: application/json" \
  -d "{\"phone\":\"13800138001\"}"
```

期望返回 `userInfo.role = "kitchen"`

---

## 阶段 3: 部署 uniCloud (10-15 分钟)

### 3.1 打开 HBuilderX

- 菜单 → 文件 → 打开目录
- 选择 `e:\ruoyi-vue3\diancianapp-user`

### 3.2 关联云空间(只做一次)

- 右键 `uniCloud-aliyun` → 关联云空间或项目
- 选择 **阿里云**
- 如果没有空间: 创建新空间(免费)
  - 空间名: `ruoyi-takeout`
  - 阿里云需要实名认证(几分钟)

### 3.3 上传云函数

右键每个云函数 → 上传部署:
- `cloudfunctions/zhouhao`
- `cloudfunctions/rider-dispatch`
- `cloudfunctions/user-login`

> ⏳ 第一次上传每个函数需要 1-2 分钟,耐心等

### 3.4 上传数据库 Schema

- 菜单 → 工具 → uniCloud Web 控制台
- 打开浏览器,进入云数据库
- 点击 **"上传 DB Schema"**,依次上传:
  - `database/takeout_user.schema.json`
  - `database/takeout_order.schema.json`
  - `database/takeout_dish.schema.json`
- 然后 **"导入数据"** 上传 `database/db_init.json`

### 3.5 验证云函数

uniCloud Web 控制台 → 云函数 → zhouhao → 测试

```json
{
  "action": "listKitchenOrders"
}
```

期望: `code: 0`, `data: []`

---

## 阶段 4: 运行小程序 (5 分钟)

### 4.1 配置 AppID

打开 `manifest.json`:
- 微信小程序 AppID: 填你自己的(没有就用测试号)
- 其他默认

### 4.2 启动

HBuilderX → 运行 → 运行到小程序模拟器 → 微信开发者工具

### 4.3 跑流程

#### A. 顾客端
1. 启动后看到"未登录"提示 → 点一键登录
2. 用 **李雪(15200152005)** 登录
3. 进入首页,默认是顾客

#### B. 后厨端
1. 退出登录
2. 用 **张师傅(13800138001)** 登录
3. 看到后厨工作台,显示 0 个待接单
4. 打开另一窗口,用李雪下单
5. 切回后厨,下拉刷新,看到新订单
6. 点"接单" → "出餐"

#### C. 骑手端
1. 退出登录
2. 用 **阿龙(13900139002)** 登录
3. 看到骑手工作台,可抢订单列表
4. 点"抢单"
5. 点"已送达"

#### D. 角色切换测试
1. 在"我的"页 → "切换角色"
2. 选择"骑手" / "后厨" / "顾客"
3. 看是否能正确跳到对应首页

---

## 阶段 5: 常见问题 (5 分钟 debug)

### Q1. 启动小程序时 console 报错 "uniCloud.callFunction 不可用"
**A:** 这是正常的 — 只有在 HBuilderX 跑模拟器/真机时才能用。
如果你用纯前端 HTML 打开,会降级到 REST API。

### Q2. 后端返回 401
**A:** Token 失效,清 storage 重登

### Q3. 云函数报 "permission denied"
**A:** Schema 没上传,或权限没设对

### Q4. 用户注册不上
**A:** 检查 db_init.json 是否成功导入

### Q5. 顾客下单后后厨看不到
**A:**
1. 检查 schema 有没有上传
2. 看一下 `takeout_order` 集合的权限
3. 在云函数日志里看错误

---

## 阶段 6: 提交代码 (2 分钟)

所有改动已就绪,会自动同步到 GitHub dev 分支。

- 仓库: https://github.com/Frankie222222222222/Ruoyi-diancan
- 分支: `dev`
- 你手动 review 后合并到 `main`

---

## ✅ 完成清单

- [ ] MySQL 数据 OK
- [ ] RuoYi 后端启动,roleDict 可访问
- [ ] uniCloud 3 函数上传成功
- [ ] 3 个 schema 上传
- [ ] db_init.json 导入 8 用户 + 6 菜品
- [ ] HBuilderX 跑小程序
- [ ] 顾客下单成功
- [ ] 后厨能看到并接单
- [ ] 骑手能抢单
- [ ] 角色切换正常

跑完截图,我们再讨论下一阶段!
