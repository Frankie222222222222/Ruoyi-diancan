package com.ruoyi.takeout.domain;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 外卖订单评价实体
 *
 * @author ruoyi
 */
public class TakeoutRating extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 评价ID */
    @Excel(name = "评价ID", cellType = Excel.ColumnType.NUMERIC)
    private Long ratingId;

    /** 订单ID */
    @Excel(name = "订单ID", cellType = Excel.ColumnType.NUMERIC)
    private Long orderId;

    /** 评价用户ID */
    @Excel(name = "用户ID", cellType = Excel.ColumnType.NUMERIC)
    private Long userId;

    /** 商家ID */
    @Excel(name = "商家ID", cellType = Excel.ColumnType.NUMERIC)
    private Long merchantId;

    /** 骑手ID */
    @Excel(name = "骑手ID", cellType = Excel.ColumnType.NUMERIC)
    private Long riderId;

    /** 商家评分(1-5) */
    @Excel(name = "商家评分", cellType = Excel.ColumnType.NUMERIC)
    private Integer merchantScore;

    /** 骑手评分(1-5) */
    @Excel(name = "骑手评分", cellType = Excel.ColumnType.NUMERIC)
    private Integer riderScore;

    /** 口味评分(1-5) */
    @Excel(name = "口味评分", cellType = Excel.ColumnType.NUMERIC)
    private Integer tasteScore;

    /** 包装评分(1-5) */
    @Excel(name = "包装评分", cellType = Excel.ColumnType.NUMERIC)
    private Integer packagingScore;

    /** 配送评分(1-5) */
    @Excel(name = "配送评分", cellType = Excel.ColumnType.NUMERIC)
    private Integer deliveryScore;

    /** 文字评价 */
    @Excel(name = "评价内容")
    private String content;

    /** 评价图片(JSON数组) */
    private String images;

    /** 口味标签(JSON数组) */
    private String tasteTags;

    /** 是否匿名(0否 1是) */
    @Excel(name = "匿名", readConverterExp = "0=否,1=是")
    private String isAnonymous;

    /** 商家回复 */
    @Excel(name = "商家回复")
    private String reply;

    /** 回复时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "回复时间")
    private Date replyTime;

    /** 回复人 */
    @Excel(name = "回复人")
    private String replyBy;

    /** 状态(0显示 1隐藏) */
    @Excel(name = "状态", readConverterExp = "0=显示,1=隐藏")
    private String status;

    /** 删除标志(0存在 2删除) */
    private String delFlag;

    /** 扩展：用户昵称 */
    private String userNickname;

    /** 扩展：商家名称 */
    private String merchantName;

    /** 扩展：骑手姓名 */
    private String riderName;

    /** 扩展：订单号 */
    private String orderNo;

    /** 查询用：回复状态（0未回复 1已回复，不存库） */
    private String replyStatus;

    public Long getRatingId()
    {
        return ratingId;
    }

    public void setRatingId(Long ratingId)
    {
        this.ratingId = ratingId;
    }

    public Long getOrderId()
    {
        return orderId;
    }

    public void setOrderId(Long orderId)
    {
        this.orderId = orderId;
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

    public Long getRiderId()
    {
        return riderId;
    }

    public void setRiderId(Long riderId)
    {
        this.riderId = riderId;
    }

    public Integer getMerchantScore()
    {
        return merchantScore;
    }

    public void setMerchantScore(Integer merchantScore)
    {
        this.merchantScore = merchantScore;
    }

    public Integer getRiderScore()
    {
        return riderScore;
    }

    public void setRiderScore(Integer riderScore)
    {
        this.riderScore = riderScore;
    }

    public Integer getTasteScore()
    {
        return tasteScore;
    }

    public void setTasteScore(Integer tasteScore)
    {
        this.tasteScore = tasteScore;
    }

    public Integer getPackagingScore()
    {
        return packagingScore;
    }

    public void setPackagingScore(Integer packagingScore)
    {
        this.packagingScore = packagingScore;
    }

    public Integer getDeliveryScore()
    {
        return deliveryScore;
    }

    public void setDeliveryScore(Integer deliveryScore)
    {
        this.deliveryScore = deliveryScore;
    }

    public String getContent()
    {
        return content;
    }

    public void setContent(String content)
    {
        this.content = content;
    }

    public String getImages()
    {
        return images;
    }

    public void setImages(String images)
    {
        this.images = images;
    }

    public String getTasteTags()
    {
        return tasteTags;
    }

    public void setTasteTags(String tasteTags)
    {
        this.tasteTags = tasteTags;
    }

    public String getIsAnonymous()
    {
        return isAnonymous;
    }

    public void setIsAnonymous(String isAnonymous)
    {
        this.isAnonymous = isAnonymous;
    }

    public String getReply()
    {
        return reply;
    }

    public void setReply(String reply)
    {
        this.reply = reply;
    }

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    public Date getReplyTime()
    {
        return replyTime;
    }

    public void setReplyTime(Date replyTime)
    {
        this.replyTime = replyTime;
    }

    public String getReplyBy()
    {
        return replyBy;
    }

    public void setReplyBy(String replyBy)
    {
        this.replyBy = replyBy;
    }

    public String getStatus()
    {
        return status;
    }

    public void setStatus(String status)
    {
        this.status = status;
    }

    public String getDelFlag()
    {
        return delFlag;
    }

    public void setDelFlag(String delFlag)
    {
        this.delFlag = delFlag;
    }

    public String getUserNickname()
    {
        return userNickname;
    }

    public void setUserNickname(String userNickname)
    {
        this.userNickname = userNickname;
    }

    public String getMerchantName()
    {
        return merchantName;
    }

    public void setMerchantName(String merchantName)
    {
        this.merchantName = merchantName;
    }

    public String getRiderName()
    {
        return riderName;
    }

    public void setRiderName(String riderName)
    {
        this.riderName = riderName;
    }

    public String getOrderNo()
    {
        return orderNo;
    }

    public void setOrderNo(String orderNo)
    {
        this.orderNo = orderNo;
    }

    public String getReplyStatus()
    {
        return replyStatus;
    }

    public void setReplyStatus(String replyStatus)
    {
        this.replyStatus = replyStatus;
    }

    @Override
    public String toString()
    {
        return "TakeoutRating{" +
                "ratingId=" + ratingId +
                ", orderId=" + orderId +
                ", merchantScore=" + merchantScore +
                '}';
    }
}
