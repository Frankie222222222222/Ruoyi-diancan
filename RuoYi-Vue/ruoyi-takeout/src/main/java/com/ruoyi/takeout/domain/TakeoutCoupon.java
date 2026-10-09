package com.ruoyi.takeout.domain;

import java.math.BigDecimal;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 外卖优惠券实体
 *
 * @author ruoyi
 */
public class TakeoutCoupon extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 优惠券ID */
    @Excel(name = "优惠券ID", cellType = Excel.ColumnType.NUMERIC)
    private Long couponId;

    /** 优惠券名称 */
    @Excel(name = "名称")
    private String name;

    /** 类型(0满减券 1折扣券 2新客券 3配送费券) */
    @Excel(name = "类型", readConverterExp = "0=满减券,1=折扣券,2=新客券,3=配送费券")
    private String type;

    /** 使用门槛金额 */
    @Excel(name = "门槛金额")
    private BigDecimal thresholdAmount;

    /** 优惠金额(满减用) */
    @Excel(name = "优惠金额")
    private BigDecimal discountAmount;

    /** 折扣率(折扣券用,如85.00=85折) */
    @Excel(name = "折扣率")
    private BigDecimal discountRate;

    /** 最高优惠金额 */
    @Excel(name = "最高优惠")
    private BigDecimal maxDiscount;

    /** 发放总数量 */
    @Excel(name = "总量", cellType = Excel.ColumnType.NUMERIC)
    private Integer totalCount;

    /** 剩余数量 */
    @Excel(name = "剩余", cellType = Excel.ColumnType.NUMERIC)
    private Integer remainCount;

    /** 每人限领数量 */
    @Excel(name = "限领", cellType = Excel.ColumnType.NUMERIC)
    private Integer perUserLimit;

    /** 有效期开始 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "开始时间")
    private Date startTime;

    /** 有效期结束 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "结束时间")
    private Date endTime;

    /** 状态(0上架 1下架 2已过期) */
    @Excel(name = "状态", readConverterExp = "0=上架,1=下架,2=已过期")
    private String status;

    /** 所属商家(为空=平台券) */
    @Excel(name = "商家ID", cellType = Excel.ColumnType.NUMERIC)
    private Long merchantId;

    /** 卡片颜色 */
    @Excel(name = "颜色")
    private String color;

    /** 使用说明 */
    @Excel(name = "说明")
    private String description;

    /** 删除标志(0存在 2删除) */
    private String delFlag;

    /** 扩展：商家名称 */
    private String merchantName;

    public Long getCouponId()
    {
        return couponId;
    }

    public void setCouponId(Long couponId)
    {
        this.couponId = couponId;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getType()
    {
        return type;
    }

    public void setType(String type)
    {
        this.type = type;
    }

    public BigDecimal getThresholdAmount()
    {
        return thresholdAmount;
    }

    public void setThresholdAmount(BigDecimal thresholdAmount)
    {
        this.thresholdAmount = thresholdAmount;
    }

    public BigDecimal getDiscountAmount()
    {
        return discountAmount;
    }

    public void setDiscountAmount(BigDecimal discountAmount)
    {
        this.discountAmount = discountAmount;
    }

    public BigDecimal getDiscountRate()
    {
        return discountRate;
    }

    public void setDiscountRate(BigDecimal discountRate)
    {
        this.discountRate = discountRate;
    }

    public BigDecimal getMaxDiscount()
    {
        return maxDiscount;
    }

    public void setMaxDiscount(BigDecimal maxDiscount)
    {
        this.maxDiscount = maxDiscount;
    }

    public Integer getTotalCount()
    {
        return totalCount;
    }

    public void setTotalCount(Integer totalCount)
    {
        this.totalCount = totalCount;
    }

    public Integer getRemainCount()
    {
        return remainCount;
    }

    public void setRemainCount(Integer remainCount)
    {
        this.remainCount = remainCount;
    }

    public Integer getPerUserLimit()
    {
        return perUserLimit;
    }

    public void setPerUserLimit(Integer perUserLimit)
    {
        this.perUserLimit = perUserLimit;
    }

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    public Date getStartTime()
    {
        return startTime;
    }

    public void setStartTime(Date startTime)
    {
        this.startTime = startTime;
    }

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    public Date getEndTime()
    {
        return endTime;
    }

    public void setEndTime(Date endTime)
    {
        this.endTime = endTime;
    }

    public String getStatus()
    {
        return status;
    }

    public void setStatus(String status)
    {
        this.status = status;
    }

    public Long getMerchantId()
    {
        return merchantId;
    }

    public void setMerchantId(Long merchantId)
    {
        this.merchantId = merchantId;
    }

    public String getColor()
    {
        return color;
    }

    public void setColor(String color)
    {
        this.color = color;
    }

    public String getDescription()
    {
        return description;
    }

    public void setDescription(String description)
    {
        this.description = description;
    }

    public String getDelFlag()
    {
        return delFlag;
    }

    public void setDelFlag(String delFlag)
    {
        this.delFlag = delFlag;
    }

    public String getMerchantName()
    {
        return merchantName;
    }

    public void setMerchantName(String merchantName)
    {
        this.merchantName = merchantName;
    }

    @Override
    public String toString()
    {
        return "TakeoutCoupon{" +
                "couponId=" + couponId +
                ", name='" + name + '\'' +
                ", type='" + type + '\'' +
                '}';
    }
}
