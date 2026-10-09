package com.ruoyi.takeout.payment;

import java.util.Map;

/**
 * 支付客户端统一接口
 *
 * <p>三种实现:
 *  <ul>
 *      <li>{@link MockPaymentClient} — 默认,无真实依赖,直接返回 SUCCESS</li>
 *      <li>{@link WechatPayClient}   — 微信 V3(待接入真实商户号)</li>
 *      <li>{@link AlipayClient}      — 支付宝(待接入真实应用)</li>
 *  </ul>
 *
 * @author ruoyi
 */
public interface PaymentClient
{
    /** 渠道标识,见 {@link PaymentChannel#getCode()} */
    String channel();

    /** 创建支付(下单后用户去支付) */
    PaymentResult create(PaymentRequest request);

    /** 查询支付状态(主动查账) */
    PaymentResult query(String outTradeNo);

    /** 申请退款 */
    PaymentResult refund(String outTradeNo, String refundNo, java.math.BigDecimal amount, String reason);

    /**
     * 解析回调报文(验签 + 提取关键字段)
     *
     * @param headers HTTP 头(微信 V3 需 Wechatpay-Signature 等)
     * @param body    回调原始报文
     * @return 解析后的 key-value 集合
     */
    Map<String, Object> parseNotify(Map<String, String> headers, String body);
}
