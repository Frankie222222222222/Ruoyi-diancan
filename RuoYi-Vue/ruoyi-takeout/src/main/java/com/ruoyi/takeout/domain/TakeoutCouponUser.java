package com.ruoyi.takeout.domain;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 用户优惠券领取/使用记录实体
 *
 * @author ruoyi
 */
public class TakeoutCouponUser extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 记录ID */
    @Excel(name = "记录ID", cellType = Excel.ColumnType.NUMERIC)
    private Long id;

    /** 用户ID */
    @Excel(name = "用户ID", cellType = Excel.ColumnType.NUMERIC)
    private Long userId;

    /** 优惠券ID */
    @Excel(name = "优惠券ID", cellType = Excel.ColumnType.NUMERIC)
    private Long couponId;

    /** 状态(0未使用 1已使用 2已过期 3已退回) */
    @Excel(name = "状态", readConverterExp = "0=未使用,1=已使用,2=已过期,3=已退回")
    private String status;

    /** 领取时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "领取时间")
    private Date receiveTime;

    /** 使用时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "使用时间")
    private Date usedTime;

    /** 使用订单ID */
    @Excel(name = "使用订单", cellType = Excel.ColumnType.NUMERIC)
    private Long usedOrderId;

    /** 过期时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "过期时间")
    private Date expireTime;

    /** 删除标志(0存在 2删除) */
    private String delFlag;

    /** 扩展：优惠券名称 */
    private String couponName;

    /** 扩展：用户昵称 */
    private String userNickname;

    /** 扩展：订单号 */
    private String orderNo;

    public Long getId()
    {
        return id;
    }

    public void setId(Long id)
    {
        this.id = id;
    }

    public Long getUserId()
    {
        return userId;
    }

    public void setUserId(Long userId)
    {
        this.userId = userId;
    }

    public Long getCouponId()
    {
        return couponId;
    }

    public void setCouponId(Long couponId)
    {
        this.couponId = couponId;
    }

    public String getStatus()
    {
        return status;
    }

    public void setStatus(String status)
    {
        this.status = status;
    }

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    public Date getReceiveTime()
    {
        return receiveTime;
    }

    public void setReceiveTime(Date receiveTime)
    {
        this.receiveTime = receiveTime;
    }

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    public Date getUsedTime()
    {
        return usedTime;
    }

    public void setUsedTime(Date usedTime)
    {
        this.usedTime = usedTime;
    }

    public Long getUsedOrderId()
    {
        return usedOrderId;
    }

    public void setUsedOrderId(Long usedOrderId)
    {
        this.usedOrderId = usedOrderId;
    }

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    public Date getExpireTime()
    {
        return expireTime;
    }

    public void setExpireTime(Date expireTime)
    {
        this.expireTime = expireTime;
    }

    public String getDelFlag()
    {
        return delFlag;
    }

    public void setDelFlag(String delFlag)
    {
        this.delFlag = delFlag;
    }

    public String getCouponName()
    {
        return couponName;
    }

    public void setCouponName(String couponName)
    {
        this.couponName = couponName;
    }

    public String getUserNickname()
    {
        return userNickname;
    }

    public void setUserNickname(String userNickname)
    {
        this.userNickname = userNickname;
    }

    public String getOrderNo()
    {
        return orderNo;
    }

    public void setOrderNo(String orderNo)
    {
        this.orderNo = orderNo;
    }

    @Override
    public String toString()
    {
        return "TakeoutCouponUser{" +
                "id=" + id +
                ", userId=" + userId +
                ", couponId=" + couponId +
                ", status='" + status + '\'' +
                '}';
    }
}
