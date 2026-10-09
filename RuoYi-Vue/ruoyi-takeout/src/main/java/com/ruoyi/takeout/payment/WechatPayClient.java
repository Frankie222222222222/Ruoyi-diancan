package com.ruoyi.takeout.payment;

import com.github.binarywang.wxpay.bean.notify.WxPayOrderNotifyResult;
import com.github.binarywang.wxpay.bean.order.WxPayNativeOrderResult;
import com.github.binarywang.wxpay.bean.request.WxPayRefundRequest;
import com.github.binarywang.wxpay.bean.request.WxPayUnifiedOrderRequest;
import com.github.binarywang.wxpay.bean.result.WxPayOrderQueryResult;
import com.github.binarywang.wxpay.bean.result.WxPayRefundResult;
import com.github.binarywang.wxpay.config.WxPayConfig;
import com.github.binarywang.wxpay.constant.WxPayConstants;
import com.github.binarywang.wxpay.exception.WxPayException;
import com.github.binarywang.wxpay.service.WxPayService;
import com.github.binarywang.wxpay.service.impl.WxPayServiceImpl;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * 微信支付客户端(扫码支付 Native 模式,用户用微信扫二维码付款)
 *
 * <p><b>启用条件</b>(自动按需加载,无需手改代码):
 *  <ul>
 *      <li>{@code payment.wechat.mch-id} 非空 → 该 Bean 被注册</li>
 *      <li>留空 → 该 Bean 不被注册,MOCK 自动兜底</li>
 *  </ul>
 *
 * <p>接入步骤(照做即可,每步都有"去哪点"):
 *  <ol>
 *      <li>开通商户号: <a href="https://pay.weixin.qq.com/">https://pay.weixin.qq.com/</a> 申请(需营业执照,1-3 工作日)</li>
 *      <li>设置 API 密钥(32 位): 商户平台 → 账户中心 → API 安全 → API 密钥 → "设置密钥"(32 位数字字母)</li>
 *      <li>(可选)下载 API 证书: 商户平台 → 账户中心 → API 安全 → API 证书 → "申请证书" → 下载压缩包解压
 *          <ul>
 *              <li>本 SDK 用 V2 模式,不需要证书也能用;退款等敏感接口才需要</li>
 *              <li>若用 V3 退款:把 apiclient_cert.pem 和 apiclient_key.pem 路径填到 application.yml</li>
 *          </ul>
 *      </li>
 *      <li>填入 application.yml 的 payment.wechat.* 字段,重启即生效</li>
 *  </ol>
 *
 * <p>回调域名: 商户平台 → 产品中心 → 开发配置 → 公众号支付 / Native 支付回调域名,需配置为
 *  {@code http://你的域名/takeout/payment/wechat/notify},本地开发期用内网穿透(natapp/ngrok)。
 *
 * @author ruoyi
 */
@Component
@ConditionalOnProperty(prefix = "payment.wechat", name = "mch-id")
public class WechatPayClient implements PaymentClient
{
    private static final Logger log = LoggerFactory.getLogger(WechatPayClient.class);

    @Autowired
    private PaymentProperties properties;

    private volatile WxPayService wxPayService;
    private volatile boolean initFailed = false;

    @Override
    public String channel() { return PaymentChannel.WECHAT.getCode(); }

    /**
     * 懒加载 WxPayService,首次调用才构造(避免应用启动期就要求密钥全配好)
     */
    private WxPayService getWxPayService() {
        if (wxPayService != null) return wxPayService;
        if (initFailed) return null;
        if (!properties.isWechatReady()) return null;
        try {
            PaymentProperties.Wechat w = properties.getWechat();
            WxPayConfig config = new WxPayConfig();
            // 必填(4 个):appId、mchId、mchKey(V2 密钥)、notifyUrl
            config.setAppId(w.getAppId());
            config.setMchId(w.getMchId());
            config.setMchKey(w.getApiV3Key());  // 字段名仍是 apiV3Key,但 SDK V2 也用 mchKey(32 位字符串密钥)
            config.setNotifyUrl(properties.getNotifyBase() + "/takeout/payment/wechat/notify");
            // V3 退款/敏感接口(可选):V3 密钥 + 商户私钥 + 证书序列号
            config.setApiV3Key(w.getApiV3Key());
            if (w.getPrivateKeyPath() != null && !w.getPrivateKeyPath().isEmpty()) {
                config.setPrivateKeyPath(w.getPrivateKeyPath());
            }
            if (w.getMchSerialNo() != null && !w.getMchSerialNo().isEmpty()) {
                config.setCertSerialNo(w.getMchSerialNo());
            }
            config.setSignType("MD5");  // V2 用 MD5
            // V2 模式关键:tradeType 决定 createOrder 返回类型(NATIVE → WxPayNativeOrderResult)
            config.setTradeType("NATIVE");
            WxPayServiceImpl svc = new WxPayServiceImpl();
            svc.setConfig(config);
            wxPayService = svc;
            log.info("[WechatPay] 初始化完成 mchId={} appId={}", w.getMchId(), w.getAppId());
            return wxPayService;
        } catch (Exception e) {
            log.error("[WechatPay] 初始化失败,降级到 Mock: {}", e.getMessage(), e);
            initFailed = true;
            return null;
        }
    }

    @Override
    public PaymentResult create(PaymentRequest request)
    {
        WxPayService svc = getWxPayService();
        if (svc == null) {
            return PaymentResult.failed("微信支付未正确配置,使用 Mock 兜底");
        }
        try {
            WxPayUnifiedOrderRequest orderReq = new WxPayUnifiedOrderRequest();
            orderReq.setOutTradeNo(request.getOutTradeNo());
            orderReq.setTotalFee(request.getAmount().multiply(new BigDecimal("100")).intValue()); // 元 → 分
            orderReq.setBody("外卖订单-" + request.getOrderId());
            orderReq.setSpbillCreateIp("127.0.0.1");
            // SDK 4.7.5 的 createOrder(TradeType, Request) 重载要求 TradeType.Specific<...>
            // 这里改用单参数 createOrder(Request),由 config 决定 tradeType
            WxPayNativeOrderResult nativeResult = svc.createOrder(orderReq);
            return PaymentResult.success(request.getOutTradeNo(), null, nativeResult.getCodeUrl());
        } catch (Exception e) {
            log.error("[WechatPay] 下单失败: {}", e.getMessage());
            return PaymentResult.failed("微信下单失败: " + e.getMessage());
        }
    }

    @Override
    public PaymentResult query(String outTradeNo)
    {
        WxPayService svc = getWxPayService();
        if (svc == null) return PaymentResult.failed("微信支付未配置");
        try {
            WxPayOrderQueryResult result = svc.queryOrder(null, outTradeNo);
            String state = result.getTradeState();
            String tradeNo = result.getTransactionId();
            if (WxPayConstants.WxpayTradeStatus.SUCCESS.equals(state)) {
                return PaymentResult.success(outTradeNo, tradeNo, null);
            }
            return PaymentResult.pending(outTradeNo);
        } catch (WxPayException e) {
            return PaymentResult.failed("微信查账失败: " + e.getMessage());
        }
    }

    @Override
    public PaymentResult refund(String outTradeNo, String refundNo, BigDecimal amount, String reason)
    {
        WxPayService svc = getWxPayService();
        if (svc == null) return PaymentResult.failed("微信支付未配置");
        try {
            int feeFen = amount.multiply(new BigDecimal("100")).intValue();
            WxPayRefundRequest req = WxPayRefundRequest.newBuilder()
                    .outTradeNo(outTradeNo)
                    .outRefundNo(refundNo)
                    .totalFee(feeFen)
                    .refundFee(feeFen)
                    .refundDesc(reason)
                    .notifyUrl(properties.getNotifyBase() + "/takeout/payment/wechat/notify")
                    .build();
            WxPayRefundResult result = svc.refund(req);
            return PaymentResult.success(outTradeNo, result.getRefundId(), null);
        } catch (WxPayException e) {
            log.error("[WechatPay] 退款失败: {}", e.getMessage());
            return PaymentResult.failed("微信退款失败: " + e.getMessage());
        }
    }

    @Override
    public Map<String, Object> parseNotify(Map<String, String> headers, String body)
    {
        WxPayService svc = getWxPayService();
        if (svc == null) return Map.of();
        try {
            // 微信 V2 回调是 XML 格式,SDK 自动验签
            WxPayOrderNotifyResult result = svc.parseOrderNotifyResult(body);
            Map<String, Object> map = new HashMap<>();
            map.put("outTradeNo", result.getOutTradeNo());
            map.put("tradeNo", result.getTransactionId());
            map.put("status", "SUCCESS".equals(result.getResultCode()) ? "SUCCESS" : "PENDING");
            return map;
        } catch (WxPayException e) {
            log.error("[WechatPay] 回调验签失败: {}", e.getMessage());
            return Map.of();
        }
    }
}
