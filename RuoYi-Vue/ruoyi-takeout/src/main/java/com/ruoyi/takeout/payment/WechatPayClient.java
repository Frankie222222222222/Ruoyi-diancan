package com.ruoyi.takeout.payment;

import java.math.BigDecimal;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * 微信支付 V3 客户端(占位实现,等待真实商户号接入)
 *
 * <p>生产期需:
 *  <ol>
 *      <li>申请微信支付商户号 + API V3 密钥</li>
 *      <li>配置 application.yml 中的 wechat.pay.* 三个密钥</li>
 *      <li>用 {@code weixin-java-pay} 的 {@code WxPayService} 替换下面 stub</li>
 *      <li>去掉 {@code @Component} 上的 lazy 或改成 @ConditionalOnProperty</li>
 *  </ol>
 *
 * <p>本类目前 <b>不会生效</b>(被 MockPaymentClient 的 @ConditionalOnMissingBean 兜底),
 *  保留是为了让代码结构完整、文档化接入路径。
 *
 * @author ruoyi
 */
@Component
public class WechatPayClient implements PaymentClient
{
    private static final Logger log = LoggerFactory.getLogger(WechatPayClient.class);

    @Override
    public String channel() { return PaymentChannel.WECHAT.getCode(); }

    @Override
    public PaymentResult create(PaymentRequest request)
    {
        log.warn("[WechatPay] 真实微信支付未接入,返回 FAILED");
        return PaymentResult.failed("微信支付暂未接入,使用 Mock");
    }

    @Override
    public PaymentResult query(String outTradeNo)
    {
        return PaymentResult.failed("微信支付暂未接入");
    }

    @Override
    public PaymentResult refund(String outTradeNo, String refundNo, BigDecimal amount, String reason)
    {
        return PaymentResult.failed("微信支付暂未接入");
    }

    @Override
    public Map<String, Object> parseNotify(Map<String, String> headers, String body)
    {
        // 真实实现:用 WxPayService.parseOrderNotifyResultNotification(headers, body)
        // 验签成功后返回 outTradeNo/tradeNo/status
        return Map.of();
    }
}
