package com.ruoyi.takeout.domain;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.annotation.Excel.ColumnType;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 订单对象 takeout_order
 *
 * @author ruoyi
 */
public class TakeoutOrder extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 订单ID */
    @Excel(name = "订单ID", cellType = ColumnType.NUMERIC)
    private Long orderId;

    /** 订单号 */
    @Excel(name = "订单号")
    private String orderNo;

    /** 下单用户ID */
    @Excel(name = "用户ID", cellType = ColumnType.NUMERIC)
    private Long userId;

    /** 商家ID */
    @Excel(name = "商家ID", cellType = ColumnType.NUMERIC)
    private Long merchantId;

    /** 商家名称（冗余，方便后台展示） */
    @Excel(name = "商家")
    private String merchantName;

    /** 订单总金额 */
    @Excel(name = "订单金额")
    private BigDecimal totalAmount;

    /** 配送费 */
    @Excel(name = "配送费")
    private BigDecimal deliveryFee;

    /** 订单状态（字典 takeout_order_status） */
    @Excel(name = "订单状态", readConverterExp = "0=待支付,1=已支付,2=商家接单,3=配送中,4=已送达,5=已完成,6=已取消,7=已退款")
    private String status;

    /** 支付状态：0未支付 1已支付 */
    @Excel(name = "支付状态", readConverterExp = "0=未支付,1=已支付")
    private String payStatus;

    /** 支付方式 */
    @Excel(name = "支付方式")
    private String payMethod;

    /** 收货人 */
    @Excel(name = "收货人")
    private String receiverName;

    /** 收货人电话 */
    @Excel(name = "收货电话")
    private String receiverPhone;

    /** 收货地址 */
    @Excel(name = "收货地址")
    private String address;

    /** 备注 */
    @Excel(name = "备注")
    private String remark;

    /** 优惠金额（用了优惠券后减免的金额） */
    @Excel(name = "优惠金额")
    private BigDecimal discountAmount;

    /** 实付金额 = totalAmount - discountAmount - deliveryFee（不含配送费时为 totalAmount - discountAmount） */
    @Excel(name = "实付金额")
    private BigDecimal actualAmount;

    /** 使用的优惠券领取记录ID（关联 takeout_coupon_user.id，可空） */
    @Excel(name = "用券ID", cellType = ColumnType.NUMERIC)
    private Long couponUserId;

    /** 期望送达时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date deliveryTime;

    /** 支付时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "支付时间")
    private Date payTime;

    /** 完成时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date completeTime;

    /** 取消原因 */
    private String cancelReason;

    /** 删除标志（0正常 2删除） */
    private String delFlag;

    /* ========== v2 扩展字段（2026-10-10）后厨/出餐/骑手时间戳 + 外键 ========== */

    /** 后厨ID（哪个后厨接单，takeout_kitchen.id，外键） */
    private Long kitchenId;

    /** 后厨接单时间（PAID → MAKING 时刻） */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date kitchenAcceptTime;

    /** 出餐完毕时间（MAKING → READY 时刻） */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date readyTime;

    /** 骑手ID（哪个骑手接单，takeout_rider.id，外键） */
    private Long riderId;

    /** 骑手接单时间（READY → DELIVERING 时刻） */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date riderAcceptTime;

    /* ========== /v2 ========== */

    /** 子表：订单商品列表（详情用） */
    private List<TakeoutOrderItem> orderItems;

    public Long getOrderId()
    {
        return orderId;
    }

    public void setOrderId(Long orderId)
    {
        this.orderId = orderId;
    }

    public String getOrderNo()
    {
        return orderNo;
    }

    public void setOrderNo(String orderNo)
    {
        this.orderNo = orderNo;
    }

    public Long getUserId()
    {
        return userId;
    }

    public void setUserId(Long userId)
    {
        this.userId = userId;
    }

    public Long getMerchantId()
    {
        return merchantId;
    }

    public void setMerchantId(Long merchantId)
    {
        this.merchantId = merchantId;
    }

    public String getMerchantName()
    {
        return merchantName;
    }

    public void setMerchantName(String merchantName)
    {
        this.merchantName = merchantName;
    }

    public BigDecimal getTotalAmount()
    {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount)
    {
        this.totalAmount = totalAmount;
    }

    public BigDecimal getDeliveryFee()
    {
        return deliveryFee;
    }

    public void setDeliveryFee(BigDecimal deliveryFee)
    {
        this.deliveryFee = deliveryFee;
    }

    public String getStatus()
    {
        return status;
    }

    public void setStatus(String status)
    {
        this.status = status;
    }

    public String getPayStatus()
    {
        return payStatus;
    }

    public void setPayStatus(String payStatus)
    {
        this.payStatus = payStatus;
    }

    public String getPayMethod()
    {
        return payMethod;
    }

    public void setPayMethod(String payMethod)
    {
        this.payMethod = payMethod;
    }

    public String getReceiverName()
    {
        return receiverName;
    }

    public void setReceiverName(String receiverName)
    {
        this.receiverName = receiverName;
    }

    public String getReceiverPhone()
    {
        return receiverPhone;
    }

    public void setReceiverPhone(String receiverPhone)
    {
        this.receiverPhone = receiverPhone;
    }

    public String getAddress()
    {
        return address;
    }

    public void setAddress(String address)
    {
        this.address = address;
    }

    @Override
    public String getRemark()
    {
        return remark;
    }

    @Override
    public void setRemark(String remark)
    {
        this.remark = remark;
    }

    public BigDecimal getDiscountAmount()
    {
        return discountAmount;
    }

    public void setDiscountAmount(BigDecimal discountAmount)
    {
        this.discountAmount = discountAmount;
    }

    public BigDecimal getActualAmount()
    {
        return actualAmount;
    }

    public void setActualAmount(BigDecimal actualAmount)
    {
        this.actualAmount = actualAmount;
    }

    public Long getCouponUserId()
    {
        return couponUserId;
    }

    public void setCouponUserId(Long couponUserId)
    {
        this.couponUserId = couponUserId;
    }

    public Date getDeliveryTime()
    {
        return deliveryTime;
    }

    public void setDeliveryTime(Date deliveryTime)
    {
        this.deliveryTime = deliveryTime;
    }

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    public Date getPayTime()
    {
        return payTime;
    }

    public void setPayTime(Date payTime)
    {
        this.payTime = payTime;
    }

    public Date getCompleteTime()
    {
        return completeTime;
    }

    public void setCompleteTime(Date completeTime)
    {
        this.completeTime = completeTime;
    }

    public String getCancelReason()
    {
        return cancelReason;
    }

    public void setCancelReason(String cancelReason)
    {
        this.cancelReason = cancelReason;
    }

    public String getDelFlag()
    {
        return delFlag;
    }

    public void setDelFlag(String delFlag)
    {
        this.delFlag = delFlag;
    }

    public List<TakeoutOrderItem> getOrderItems()
    {
        return orderItems;
    }

    public void setOrderItems(List<TakeoutOrderItem> orderItems)
    {
        this.orderItems = orderItems;
    }

    /* ========== v2 新增字段 getter/setter ========== */

    public Long getKitchenId()
    {
        return kitchenId;
    }

    public void setKitchenId(Long kitchenId)
    {
        this.kitchenId = kitchenId;
    }

    public Date getKitchenAcceptTime()
    {
        return kitchenAcceptTime;
    }

    public void setKitchenAcceptTime(Date kitchenAcceptTime)
    {
        this.kitchenAcceptTime = kitchenAcceptTime;
    }

    public Date getReadyTime()
    {
        return readyTime;
    }

    public void setReadyTime(Date readyTime)
    {
        this.readyTime = readyTime;
    }

    public Long getRiderId()
    {
        return riderId;
    }

    public void setRiderId(Long riderId)
    {
        this.riderId = riderId;
    }

    public Date getRiderAcceptTime()
    {
        return riderAcceptTime;
    }

    public void setRiderAcceptTime(Date riderAcceptTime)
    {
        this.riderAcceptTime = riderAcceptTime;
    }

    /* ========== /v2 ========== */

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Override
    public Date getCreateTime()
    {
        return super.getCreateTime();
    }

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Override
    public Date getUpdateTime()
    {
        return super.getUpdateTime();
    }

    @Override
    public String toString()
    {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
                .append("orderId", getOrderId())
                .append("orderNo", getOrderNo())
                .append("userId", getUserId())
                .append("merchantId", getMerchantId())
                .append("merchantName", getMerchantName())
                .append("totalAmount", getTotalAmount())
                .append("deliveryFee", getDeliveryFee())
                .append("status", getStatus())
                .append("payStatus", getPayStatus())
                .append("payMethod", getPayMethod())
                .append("receiverName", getReceiverName())
                .append("receiverPhone", getReceiverPhone())
                .append("address", getAddress())
                .append("remark", getRemark())
                .append("discountAmount", getDiscountAmount())
                .append("actualAmount", getActualAmount())
                .append("couponUserId", getCouponUserId())
                .append("deliveryTime", getDeliveryTime())
                .append("payTime", getPayTime())
                .append("completeTime", getCompleteTime())
                .append("cancelReason", getCancelReason())
                .append("delFlag", getDelFlag())
                .append("kitchenId", getKitchenId())
                .append("kitchenAcceptTime", getKitchenAcceptTime())
                .append("readyTime", getReadyTime())
                .append("riderId", getRiderId())
                .append("riderAcceptTime", getRiderAcceptTime())
                .append("createBy", getCreateBy())
                .append("createTime", getCreateTime())
                .append("updateBy", getUpdateBy())
                .append("updateTime", getUpdateTime())
                .toString();
    }
}
