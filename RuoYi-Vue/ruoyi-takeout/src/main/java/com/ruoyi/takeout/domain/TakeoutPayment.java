package com.ruoyi.takeout.domain;

import java.math.BigDecimal;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 支付流水对象 takeout_payment
 *
 * @author ruoyi
 */
public class TakeoutPayment extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 主键 */
    private Long id;

    /** 订单ID */
    @Excel(name = "订单ID", cellType = Excel.ColumnType.NUMERIC)
    private Long orderId;

    /** 订单号 */
    @Excel(name = "订单号")
    private String orderNo;

    /** 渠道 */
    @Excel(name = "渠道")
    private String channel;

    /** 商户订单号 */
    @Excel(name = "商户订单号")
    private String outTradeNo;

    /** 渠道流水号 */
    @Excel(name = "渠道流水号")
    private String tradeNo;

    /** 金额(元) */
    @Excel(name = "金额")
    private BigDecimal amount;

    /** 状态 PENDING/SUCCESS/FAILED/REFUNDED/CLOSED */
    @Excel(name = "状态")
    private String status;

    /** 回调原始报文 */
    private String notifyRaw;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }

    public String getOrderNo() { return orderNo; }
    public void setOrderNo(String orderNo) { this.orderNo = orderNo; }

    public String getChannel() { return channel; }
    public void setChannel(String channel) { this.channel = channel; }

    public String getOutTradeNo() { return outTradeNo; }
    public void setOutTradeNo(String outTradeNo) { this.outTradeNo = outTradeNo; }

    public String getTradeNo() { return tradeNo; }
    public void setTradeNo(String tradeNo) { this.tradeNo = tradeNo; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getNotifyRaw() { return notifyRaw; }
    public void setNotifyRaw(String notifyRaw) { this.notifyRaw = notifyRaw; }

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    public Date getCreateTime() { return super.getCreateTime(); }

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    public Date getUpdateTime() { return super.getUpdateTime(); }
}
