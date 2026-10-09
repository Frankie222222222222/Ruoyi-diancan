package com.ruoyi.takeout.payment;

import java.math.BigDecimal;

/**
 * 支付请求入参
 *
 * @author ruoyi
 */
public class PaymentRequest
{
    /** 订单ID */
    private Long orderId;
    /** 商户订单号(默认 = 订单号) */
    private String outTradeNo;
    /** 支付金额(元) */
    private BigDecimal amount;
    /** 订单标题 */
    private String subject;
    /** 用户标识(C 端 openId,后台可空) */
    private String openId;
    /** 异步回调地址(开发期可空,使用默认) */
    private String notifyUrl;
    /** 同步跳转地址(用户支付完成后回到哪里) */
    private String returnUrl;

    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }

    public String getOutTradeNo() { return outTradeNo; }
    public void setOutTradeNo(String outTradeNo) { this.outTradeNo = outTradeNo; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }

    public String getOpenId() { return openId; }
    public void setOpenId(String openId) { this.openId = openId; }

    public String getNotifyUrl() { return notifyUrl; }
    public void setNotifyUrl(String notifyUrl) { this.notifyUrl = notifyUrl; }

    public String getReturnUrl() { return returnUrl; }
    public void setReturnUrl(String returnUrl) { this.returnUrl = returnUrl; }
}
