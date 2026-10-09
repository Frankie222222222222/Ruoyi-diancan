package com.ruoyi.takeout.service.impl;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.takeout.domain.TakeoutOrder;
import com.ruoyi.takeout.domain.TakeoutPayment;
import com.ruoyi.takeout.mapper.TakeoutOrderMapper;
import com.ruoyi.takeout.mapper.TakeoutPaymentMapper;
import com.ruoyi.takeout.payment.PaymentChannel;
import com.ruoyi.takeout.payment.PaymentClient;
import com.ruoyi.takeout.payment.PaymentRequest;
import com.ruoyi.takeout.payment.PaymentResult;
import com.ruoyi.takeout.service.ITakeoutOrderService;
import com.ruoyi.takeout.service.ITakeoutPaymentService;

/**
 * 支付 Service 实现
 *
 * <p>职责:
 *  <ul>
 *      <li>挂单:在 takeout_payment 写一条 PENDING 记录(状态机外,不阻塞下单事务)</li>
 *      <li>发起:按 channel 选 client 调 create,落 trade_no + 状态</li>
 *      <li>回调:解析报文 → 改 takeout_payment.status=SUCCESS + 改 takeout_order.payStatus=1</li>
 *  </ul>
 *
 * @author ruoyi
 */
@Service
public class TakeoutPaymentServiceImpl implements ITakeoutPaymentService
{
    private static final Logger log = LoggerFactory.getLogger(TakeoutPaymentServiceImpl.class);

    @Autowired
    private TakeoutPaymentMapper paymentMapper;

    @Autowired
    private TakeoutOrderMapper orderMapper;

    @Autowired
    @Lazy
    private ITakeoutOrderService orderService;

    @Autowired
    private List<PaymentClient> clients;

    /** 路由:channel → 对应 client */
    private PaymentClient route(PaymentChannel channel)
    {
        for (PaymentClient c : clients)
        {
            if (c.channel().equalsIgnoreCase(channel.getCode()))
            {
                return c;
            }
        }
        // 兜底:MOCK
        for (PaymentClient c : clients)
        {
            if (c.channel().equalsIgnoreCase(PaymentChannel.MOCK.getCode()))
            {
                return c;
            }
        }
        throw new ServiceException("没有可用的 PaymentClient(channel=" + channel + ")");
    }

    @Override
    public List<TakeoutPayment> selectPaymentList(TakeoutPayment query)
    {
        return paymentMapper.selectPaymentList(query);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createPaymentRecord(Long orderId, String orderNo, BigDecimal amount, PaymentChannel channel)
    {
        if (orderId == null || orderNo == null || amount == null)
        {
            log.warn("[payment] createPaymentRecord 参数缺失 orderId={} orderNo={} amount={}", orderId, orderNo, amount);
            return null;
        }
        TakeoutPayment p = new TakeoutPayment();
        p.setOrderId(orderId);
        p.setOrderNo(orderNo);
        p.setChannel(channel.getCode());
        p.setOutTradeNo(orderNo);
        p.setAmount(amount);
        p.setStatus("PENDING");
        p.setCreateBy(SecurityUtils.getUsername());
        p.setCreateTime(DateUtils.getNowDate());
        paymentMapper.insertPayment(p);
        return p.getId();
    }

    @Override
    public PaymentResult createPayment(Long paymentId)
    {
        TakeoutPayment record = paymentMapper.selectPaymentById(paymentId);
        if (record == null)
        {
            return PaymentResult.failed("支付记录不存在");
        }
        if ("SUCCESS".equals(record.getStatus()))
        {
            return PaymentResult.success(record.getOutTradeNo(), record.getTradeNo(), "已支付");
        }
        if ("REFUNDED".equals(record.getStatus()) || "CLOSED".equals(record.getStatus()))
        {
            return PaymentResult.failed("订单已 " + record.getStatus() + "，无法再支付");
        }
        PaymentChannel ch = PaymentChannel.fromCode(record.getChannel());
        PaymentClient client = route(ch);

        PaymentRequest req = new PaymentRequest();
        req.setOrderId(record.getOrderId());
        req.setOutTradeNo(record.getOutTradeNo());
        req.setAmount(record.getAmount());
        req.setSubject("外卖订单-" + record.getOrderNo());

        PaymentResult result = client.create(req);

        if (result.getCode() == PaymentResult.Code.SUCCESS)
        {
            // Mock 模式直接置为 SUCCESS;真实渠道是 PENDING,等回调
            TakeoutPayment upd = new TakeoutPayment();
            upd.setId(record.getId());
            upd.setTradeNo(result.getTradeNo());
            if (PaymentChannel.MOCK.equals(ch))
            {
                upd.setStatus("SUCCESS");
            }
            upd.setUpdateBy(SecurityUtils.getUsername());
            upd.setUpdateTime(new Date());
            paymentMapper.updatePayment(upd);
            if (PaymentChannel.MOCK.equals(ch))
            {
                // Mock 直接同步推进订单状态
                markOrderPaid(record);
            }
        }
        else
        {
            TakeoutPayment upd = new TakeoutPayment();
            upd.setId(record.getId());
            upd.setStatus("FAILED");
            upd.setUpdateBy(SecurityUtils.getUsername());
            upd.setUpdateTime(new Date());
            paymentMapper.updatePayment(upd);
        }
        return result;
    }

    @Override
    public PaymentResult queryPayment(Long paymentId)
    {
        TakeoutPayment record = paymentMapper.selectPaymentById(paymentId);
        if (record == null) return PaymentResult.failed("支付记录不存在");
        PaymentClient client = route(PaymentChannel.fromCode(record.getChannel()));
        PaymentResult result = client.query(record.getOutTradeNo());
        // 主动查账也可推进本地状态
        if (result.getCode() == PaymentResult.Code.SUCCESS && !"SUCCESS".equals(record.getStatus()))
        {
            TakeoutPayment upd = new TakeoutPayment();
            upd.setId(record.getId());
            upd.setStatus("SUCCESS");
            upd.setTradeNo(result.getTradeNo());
            paymentMapper.updatePayment(upd);
            markOrderPaid(record);
        }
        return result;
    }

    @Override
    public PaymentResult refundPayment(Long paymentId, String reason)
    {
        TakeoutPayment record = paymentMapper.selectPaymentById(paymentId);
        if (record == null) return PaymentResult.failed("支付记录不存在");
        if (!"SUCCESS".equals(record.getStatus()))
        {
            return PaymentResult.failed("仅已支付订单可退款,当前 status=" + record.getStatus());
        }
        PaymentClient client = route(PaymentChannel.fromCode(record.getChannel()));
        String refundNo = "RF" + System.currentTimeMillis();
        PaymentResult result = client.refund(record.getOutTradeNo(), refundNo, record.getAmount(), reason);
        if (result.getCode() == PaymentResult.Code.SUCCESS)
        {
            TakeoutPayment upd = new TakeoutPayment();
            upd.setId(record.getId());
            upd.setStatus("REFUNDED");
            upd.setUpdateBy(SecurityUtils.getUsername());
            paymentMapper.updatePayment(upd);
        }
        return result;
    }

    @Override
    public String handleNotify(PaymentChannel channel, Map<String, String> headers, String body)
    {
        try
        {
            PaymentClient client = route(channel);
            Map<String, Object> parsed = client.parseNotify(headers, body);
            String outTradeNo = (String) parsed.get("outTradeNo");
            String tradeNo = (String) parsed.get("tradeNo");
            String status = (String) parsed.get("status");
            if (StringUtils.isEmpty(outTradeNo))
            {
                log.warn("[payment.notify] 报文缺 outTradeNo channel={}", channel);
                return "FAIL";
            }
            TakeoutPayment record = paymentMapper.selectByOutTradeNo(outTradeNo);
            if (record == null)
            {
                log.warn("[payment.notify] 找不到记录 outTradeNo={}", outTradeNo);
                return "FAIL";
            }
            if ("SUCCESS".equals(status))
            {
                TakeoutPayment upd = new TakeoutPayment();
                upd.setId(record.getId());
                upd.setStatus("SUCCESS");
                upd.setTradeNo(tradeNo);
                upd.setNotifyRaw(body);
                upd.setUpdateBy("system-notify");
                paymentMapper.updatePayment(upd);
                markOrderPaid(record);
                return "SUCCESS";
            }
            return "SUCCESS"; // 其他状态不更新本地,等下次回调
        }
        catch (Exception e)
        {
            log.error("[payment.notify] 处理异常 channel={}", channel, e);
            return "FAIL";
        }
    }

    /**
     * 内部:支付成功后把订单 payStatus=1, status 维持/推进
     */
    private void markOrderPaid(TakeoutPayment record)
    {
        TakeoutOrder order = orderMapper.selectOrderById(record.getOrderId());
        if (order == null) return;
        try
        {
            // 仅把"待支付"推进到"已支付",其他状态不打断
            if ("0".equals(order.getStatus()))
            {
                orderService.changeOrderStatus(order.getOrderId(), "1");
            }
            // 同步 payStatus/payTime
            order.setPayStatus("1");
            order.setPayTime(DateUtils.getNowDate());
            order.setUpdateBy("system-payment");
            orderMapper.updateOrder(order);
        }
        catch (Exception e)
        {
            log.error("[payment] markOrderPaid 失败 orderId={}", record.getOrderId(), e);
        }
    }
}
