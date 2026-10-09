package com.ruoyi.takeout.service;

import java.util.List;
import com.ruoyi.takeout.domain.TakeoutPayment;
import com.ruoyi.takeout.payment.PaymentChannel;
import com.ruoyi.takeout.payment.PaymentResult;

/**
 * 支付服务接口
 *
 * <p>对外暴露三个核心方法:
 *  <ul>
 *      <li>{@link #createPaymentRecord} — 创建挂单(不调渠道)</li>
 *      <li>{@link #createPayment}        — 调起支付(创建+落库)</li>
 *      <li>{@link #queryPayment}         — 主动查账</li>
 *      <li>{@link #refundPayment}        — 退款</li>
 *      <li>{@link #handleNotify}         — 处理渠道回调(供 Controller 调用)</li>
 *  </ul>
 *
 * @author ruoyi
 */
public interface ITakeoutPaymentService
{
    /** 分页查询 */
    List<TakeoutPayment> selectPaymentList(TakeoutPayment query);
    /**
     * 订单创建时挂一条 PENDING 记录(不调渠道,等用户点支付才发起)
     *
     * @param orderId 订单ID
     * @param orderNo 订单号
     * @param amount  金额
     * @param channel 默认渠道(可被前端覆盖)
     * @return 挂单支付记录ID
     */
    Long createPaymentRecord(Long orderId, String orderNo, java.math.BigDecimal amount, PaymentChannel channel);

    /** 创建并发起支付 */
    PaymentResult createPayment(Long paymentId);

    /** 主动查账 */
    PaymentResult queryPayment(Long paymentId);

    /** 退款 */
    PaymentResult refundPayment(Long paymentId, String reason);

    /**
     * 渠道回调入口(由 Controller 调用,验签已由 PaymentClient.parseNotify 完成)
     *
     * @return "SUCCESS" 给渠道回包,其他给 "FAIL"
     */
    String handleNotify(PaymentChannel channel, java.util.Map<String, String> headers, String body);
}
