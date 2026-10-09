package com.ruoyi.takeout.payment;

import java.math.BigDecimal;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * 支付宝客户端(占位实现,等待真实应用接入)
 *
 * <p>生产期需:
 *  <ol>
 *      <li>申请支付宝应用 + 私钥/公钥(签名方式:RSA2)</li>
 *      <li>配置 application.yml 中的 alipay.* 三个密钥</li>
 *      <li>用 {@code alipay-sdk-java} 的 {@code AlipayClient} 替换下面 stub</li>
 *  </ol>
 *
 * @author ruoyi
 */
@Component
public class AlipayClient implements PaymentClient
{
    private static final Logger log = LoggerFactory.getLogger(AlipayClient.class);

    @Override
    public String channel() { return PaymentChannel.ALIPAY.getCode(); }

    @Override
    public PaymentResult create(PaymentRequest request)
    {
        log.warn("[Alipay] 真实支付宝未接入,返回 FAILED");
        return PaymentResult.failed("支付宝暂未接入,使用 Mock");
    }

    @Override
    public PaymentResult query(String outTradeNo)
    {
        return PaymentResult.failed("支付宝暂未接入");
    }

    @Override
    public PaymentResult refund(String outTradeNo, String refundNo, BigDecimal amount, String reason)
    {
        return PaymentResult.failed("支付宝暂未接入");
    }

    @Override
    public Map<String, Object> parseNotify(Map<String, String> headers, String body)
    {
        // 真实实现:AlipaySignature.rsaCheckV1 + AlipayNotifyServiceFactory
        return Map.of();
    }
}
