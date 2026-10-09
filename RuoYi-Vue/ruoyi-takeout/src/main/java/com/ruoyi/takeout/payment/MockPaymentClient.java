package com.ruoyi.takeout.payment;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Component;

/**
 * Mock 支付客户端(开发/演示/单元测试用)
 *
 * <p>默认 Bean:当不存在其他 PaymentClient 时启用。
 * 直接返回 SUCCESS,无网络/SDK 依赖。
 *
 * @author ruoyi
 */
@Component
@ConditionalOnMissingBean(name = "wechatPayClient")
public class MockPaymentClient implements PaymentClient
{
    private static final Logger log = LoggerFactory.getLogger(MockPaymentClient.class);
    private static final DateTimeFormatter TS = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    @Override
    public String channel() { return PaymentChannel.MOCK.getCode(); }

    @Override
    public PaymentResult create(PaymentRequest request)
    {
        String tradeNo = "MOCK" + LocalDateTime.now().format(TS) + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        log.info("[MOCK] 创建支付订单 outTradeNo={} amount={} tradeNo={}",
                request.getOutTradeNo(), request.getAmount(), tradeNo);
        // prepayInfo 模拟成"假二维码内容",前端弹窗时直接显示
        String fakeQr = "mock://pay?outTradeNo=" + request.getOutTradeNo() + "&tradeNo=" + tradeNo;
        return PaymentResult.success(request.getOutTradeNo(), tradeNo, fakeQr);
    }

    @Override
    public PaymentResult query(String outTradeNo)
    {
        log.info("[MOCK] 查询支付 outTradeNo={} (默认成功)", outTradeNo);
        return PaymentResult.success(outTradeNo,
                "MOCK" + LocalDateTime.now().format(TS),
                "mock://paid");
    }

    @Override
    public PaymentResult refund(String outTradeNo, String refundNo, BigDecimal amount, String reason)
    {
        log.info("[MOCK] 退款 outTradeNo={} refundNo={} amount={} reason={}",
                outTradeNo, refundNo, amount, reason);
        return PaymentResult.success(outTradeNo, "REFUND" + UUID.randomUUID().toString().substring(0, 8), null);
    }

    @Override
    public Map<String, Object> parseNotify(Map<String, String> headers, String body)
    {
        // Mock 不验签,直接返回 JSON 字段
        // 真实场景下 body 应该是 JSON 字符串,这里简化:默认成功
        return Map.of(
                "outTradeNo", "MOCK_OUT_TRADE",
                "tradeNo", "MOCK_TRADE",
                "status", "SUCCESS"
        );
    }
}
