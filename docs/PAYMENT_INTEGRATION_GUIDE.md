# 真实支付接入指南(微信 + 支付宝)

本指南面向**没有支付对接经验**的开发者,按本文操作即可完成真实渠道接入。
整个接入过程**不需要修改任何 Java 代码**,只需要在两个地方填密钥 + 改两个网址。

## 0. 接入全景图

```
用户扫码 → 微信/支付宝 → 回调 /takeout/payment/{channel}/notify
                                ↓
                          PaymentService.handleNotify
                                ↓
                       更新 takeout_payment + takeout_order
```

| 渠道 | 支付方式 | 用户体验 | 你的工作 |
|---|---|---|---|
| **微信支付** | Native 扫码 | 微信扫二维码付款 | 申请商户号 + 填 4 个密钥 |
| **支付宝** | Page 网页 | 跳到支付宝页(有二维码) | 申请应用 + 填 3 个密钥 |
| **Mock 兜底** | 无 | 直接返回成功(开发演示) | 不需要任何配置(默认) |

---

## 1. 微信支付接入

### 1.1 申请商户号(需要营业执照)

1. 打开 [https://pay.weixin.qq.com/](https://pay.weixin.qq.com/)
2. 点击右上角"接入微信支付"→ 用营业执照注册
3. 审核通过(1-3 个工作日)后,会得到一个**商户号**(纯数字,类似 1234567890)

### 1.2 获取 4 个密钥

进入 **商户平台 → 账户中心 → API 安全**:

| 字段 | 怎么填 | 示例 |
|---|---|---|
| **APPID** | 公众号/小程序/移动应用 的 AppID | wx1234567890abcdef |
| **商户号 (mch-id)** | 上一步申请到的 | 1234567890 |
| **API 密钥 (api-v3-key)** | 点击"API 密钥"→ "设置新密钥"→ 填 32 位数字字母 | aBcD1234eFgH5678iJkL9012mNoP3456 |
| **商户证书序列号 (mch-serial-no)** | 点击"API 证书"→ "申请证书"→ 下载 cert.zip → 解压后打开 .pem 文件末尾的"证书序列号" | 4A5B6C7D8E9F... |

> **注意**: 退款等敏感接口需要先下载 `apiclient_cert.pem` 和 `apiclient_key.pem`,路径填到 `private-key-path`。
> 纯下单/查账不需要证书,V2 模式(本项目默认)即可。

### 1.3 填入 application.yml

打开 [`RuoYi-Vue/ruoyi-admin/src/main/resources/application.yml`](../RuoYi-Vue/ruoyi-admin/src/main/resources/application.yml),找到 `payment.wechat` 块:

```yaml
payment:
  default-channel: WECHAT  # 改为 WECHAT,默认走真渠道
  notify-base: http://你的域名  # 公网可访问的回调前缀(开发期用内网穿透)

  wechat:
    mch-id: 1234567890                        # 商户号
    api-v3-key: aBcD1234eFgH5678iJkL9012mNoP3456  # 32 位 API 密钥
    mch-serial-no: 4A5B6C7D8E9F...            # 商户证书序列号
    private-key-path: D:/cert/apiclient_key.pem  # 商户私钥绝对路径(选填,仅退款用)
    app-id: wx1234567890abcdef                 # AppID
```

### 1.4 配置回调域名

**商户平台 → 产品中心 → 开发配置 → 公众号支付**:
- 支付授权目录: `http://你的域名/`
- (Native 支付)回调 URL: `http://你的域名/takeout/payment/wechat/notify`

### 1.5 本地开发内网穿透

微信回调必须是公网可访问的 URL。本地用 [natapp](https://natapp.cn/) 或 [ngrok](https://ngrok.com/) 暴露:

```bash
# natapp 示例
natapp -authtoken=你的authtoken
# 得到: Forwarding http://xxxx.natappfree.cc -> localhost:8080
# 然后 notify-base 填: http://xxxx.natappfree.cc
```

### 1.6 验证

1. 重启后端,日志看到 `[WechatPay] 初始化完成 mchId=xxx` = 成功
2. 订单列表 → 点"模拟支付"(已改为"发起支付")→ 弹出二维码 → 用微信扫码
3. 支付成功后,订单状态自动变为"已支付"

---

## 2. 支付宝接入

### 2.1 注册开发者

1. 打开 [https://open.alipay.com/](https://open.alipay.com/)
2. 用支付宝扫码 → "我是开发者" → 完成实名认证

### 2.2 创建应用

1. **控制台 → 我的应用 → 创建应用**
2. 应用类型选**"网页应用"**(因为 Page 模式是电脑网站支付)
3. 提交审核(1-3 天),通过后获得 **APPID**(16 位纯数字)

### 2.3 生成密钥

进入 **应用 → 开发设置 → 接口加签方式(密钥管理)**:

1. 下载"支付宝开放平台密钥工具" [https://opendocs.alipay.com/common/02kipk](https://opendocs.alipay.com/common/02kipk)
2. 选择 **PKCS1 / RSA2 / 2048** → 生成密钥
3. 把"**应用公钥**"粘贴到支付宝页面 → 点"保存"→ 自动生成"**支付宝公钥**"
4. 此时你手上有 3 个东西:
   - 应用私钥(你自己生成的, .txt 文件内容)
   - 应用公钥(你自己生成的, 已上传给支付宝)
   - 支付宝公钥(支付宝页面显示的, 你要保存)

### 2.4 填入 application.yml

```yaml
payment:
  default-channel: ALIPAY  # 改默认渠道
  alipay:
    app-id: 2021000123456789            # 16 位 APPID
    private-key: MIIEvAIBADANBgkqh...  # 应用私钥(PKCS1 格式,完整粘贴,带换行)
    public-key: MIIBIjANBgkqhkiG...    # 支付宝公钥(完整粘贴)
    sign-type: RSA2                      # 推荐
    notify-url:                          # 留空走 payment.notify-base
    return-url: http://你的域名/支付完成
```

### 2.5 配置回调域名

**应用 → 开发设置 → 授权回调地址**:
- 添加 `http://你的域名/takeout/payment/alipay/notify`

### 2.6 验证

1. 重启,日志看到 `[Alipay] 初始化完成 appId=xxx` = 成功
2. 订单 → "发起支付" → 跳到支付宝页 → 扫码 → 完成
3. 订单状态自动更新

---

## 3. 双渠道并存 + Mock 兜底

三种客户端**自动切换**,你不需要手改代码:

| 条件 | 启用的客户端 |
|---|---|
| `payment.wechat.mch-id` 留空,`payment.alipay.app-id` 留空 | **Mock 兜底**(开发演示) |
| `payment.wechat.mch-id` 填值 | **微信**(默认渠道填 WECHAT 时用它) |
| `payment.alipay.app-id` 填值 | **支付宝**(默认渠道填 ALIPAY 时用它) |
| 两个都填值 | 微信 + 支付宝并存,前端按钮可切换 |

> **生产期**: 建议都填上(微信+支付宝并存),客户选哪种都行。
> **开发期**: 都留空,Mock 自动接管,免去申请商户号的麻烦。

---

## 4. 关键文件位置

| 文件 | 作用 |
|---|---|
| [`RuoYi-Vue/ruoyi-admin/src/main/resources/application.yml`](../RuoYi-Vue/ruoyi-admin/src/main/resources/application.yml) | **唯一需要改的配置** |
| `RuoYi-Vue/ruoyi-takeout/src/main/java/com/ruoyi/takeout/payment/PaymentProperties.java` | 读配置的 Bean |
| `RuoYi-Vue/ruoyi-takeout/src/main/java/com/ruoyi/takeout/payment/WechatPayClient.java` | 微信客户端(自动按需加载) |
| `RuoYi-Vue/ruoyi-takeout/src/main/java/com/ruoyi/takeout/payment/AlipayClient.java` | 支付宝客户端(自动按需加载) |
| `RuoYi-Vue/ruoyi-takeout/src/main/java/com/ruoyi/takeout/payment/MockPaymentClient.java` | Mock 兜底 |
| `RuoYi-Vue/ruoyi-takeout/src/main/java/com/ruoyi/takeout/service/impl/TakeoutPaymentServiceImpl.java` | 业务核心(订单状态联动) |
| `RuoYi-Vue/ruoyi-takeout/src/main/java/com/ruoyi/takeout/controller/TakeoutPaymentController.java` | 公开回调入口 |
| `RuoYi-Vue/ruoyi-takeout/src/test/.../Takeout*ServiceImplTest.java` | 单元测试(45 个) |

## 5. 常见问题

### Q1: 重启后日志报 "微信支付未正确配置" 怎么办?
A: 检查 application.yml 的 `payment.wechat.mch-id` 是不是真的填了(不能有空格、不能是注释)。日志会打印 `isWechatReady()` 的判断依据。

### Q2: 回调接收不到?
A: 99% 是域名问题。本地必须用内网穿透(natapp/ngrok),微信/支付宝要求回调 URL 是公网 HTTPS(开发期 HTTP 也能测)。

### Q3: 想临时切换回 Mock 调试?
A: 把 application.yml 的 `mch-id` 和 `app-id` 全部清空(设为 `""` 或注释),重启即可。无需改 Java 代码。

### Q4: 申请商户号要营业执照,个体工商户可以吗?
A: 可以! 微信和支付宝都支持个体工商户营业执照申请。支付宝审核更快(一般当天)。

### Q5: 真实接入了之后,Mock 数据怎么清理?
A: `DELETE FROM takeout_payment;` (Mock 数据用 MOCK 前缀,真实数据不会撞)

## 6. 一句话总结

> **开发期**:啥都不填,直接用 Mock 跑通全流程
> **生产期**:申请商户号 → 填 4 个微信字段 / 3 个支付宝字段 → 配回调域名 → 完事
