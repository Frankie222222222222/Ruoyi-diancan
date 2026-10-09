package com.ruoyi.takeout.domain;

import java.math.BigDecimal;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 外卖投诉退款工单实体
 *
 * @author ruoyi
 */
public class TakeoutComplaint extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 工单ID */
    @Excel(name = "工单ID", cellType = Excel.ColumnType.NUMERIC)
    private Long complaintId;

    /** 关联订单ID */
    @Excel(name = "订单ID", cellType = Excel.ColumnType.NUMERIC)
    private Long orderId;

    /** 投诉用户ID */
    @Excel(name = "用户ID", cellType = Excel.ColumnType.NUMERIC)
    private Long userId;

    /** 关联商家ID */
    @Excel(name = "商家ID", cellType = Excel.ColumnType.NUMERIC)
    private Long merchantId;

    /** 关联骑手ID */
    @Excel(name = "骑手ID", cellType = Excel.ColumnType.NUMERIC)
    private Long riderId;

    /** 类型(0退款申请 1投诉商家 2投诉骑手 3其他) */
    @Excel(name = "类型", readConverterExp = "0=退款申请,1=投诉商家,2=投诉骑手,3=其他")
    private String type;

    /** 投诉/退款原因 */
    @Excel(name = "原因")
    private String reason;

    /** 凭证图片(JSON数组) */
    private String images;

    /** 申请退款金额 */
    @Excel(name = "申请金额")
    private BigDecimal refundAmount;

    /** 审批退款金额 */
    @Excel(name = "审批金额")
    private BigDecimal approvedAmount;

    /** 处理状态(0待处理 1处理中 2已退款 3已拒绝 4已撤销 5已完成) */
    @Excel(name = "状态", readConverterExp = "0=待处理,1=处理中,2=已退款,3=已拒绝,4=已撤销,5=已完成")
    private String status;

    /** 处理备注 */
    @Excel(name = "处理备注")
    private String handleRemark;

    /** 处理人 */
    @Excel(name = "处理人")
    private String handleBy;

    /** 处理时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "处理时间")
    private Date handleTime;

    /** 退款时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "退款时间")
    private Date refundTime;

    /** 退款流水号 */
    @Excel(name = "退款流水")
    private String refundFlowNo;

    /** 用户申诉内容 */
    @Excel(name = "申诉内容")
    private String appealContent;

    /** 申诉时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "申诉时间")
    private Date appealTime;

    /** 申诉状态(0待审核 1通过 2驳回) */
    @Excel(name = "申诉状态", readConverterExp = "0=待审核,1=通过,2=驳回")
    private String appealStatus;

    /** 申诉处理结果 */
    @Excel(name = "申诉结果")
    private String appealHandle;

    /** 删除标志(0存在 2删除) */
    private String delFlag;

    /** 扩展：订单号 */
    private String orderNo;

    /** 扩展：用户昵称 */
    private String userNickname;

    /** 扩展：商家名称 */
    private String merchantName;

    /** 扩展：骑手姓名 */
    private String riderName;

    public Long getComplaintId()
    {
        return complaintId;
    }

    public void setComplaintId(Long complaintId)
    {
        this.complaintId = complaintId;
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

    public String getType()
    {
        return type;
    }

    public void setType(String type)
    {
        this.type = type;
    }

    public String getReason()
    {
        return reason;
    }

    public void setReason(String reason)
    {
        this.reason = reason;
    }

    public String getImages()
    {
        return images;
    }

    public void setImages(String images)
    {
        this.images = images;
    }

    public BigDecimal getRefundAmount()
    {
        return refundAmount;
    }

    public void setRefundAmount(BigDecimal refundAmount)
    {
        this.refundAmount = refundAmount;
    }

    public BigDecimal getApprovedAmount()
    {
        return approvedAmount;
    }

    public void setApprovedAmount(BigDecimal approvedAmount)
    {
        this.approvedAmount = approvedAmount;
    }

    public String getStatus()
    {
        return status;
    }

    public void setStatus(String status)
    {
        this.status = status;
    }

    public String getHandleRemark()
    {
        return handleRemark;
    }

    public void setHandleRemark(String handleRemark)
    {
        this.handleRemark = handleRemark;
    }

    public String getHandleBy()
    {
        return handleBy;
    }

    public void setHandleBy(String handleBy)
    {
        this.handleBy = handleBy;
    }

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    public Date getHandleTime()
    {
        return handleTime;
    }

    public void setHandleTime(Date handleTime)
    {
        this.handleTime = handleTime;
    }

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    public Date getRefundTime()
    {
        return refundTime;
    }

    public void setRefundTime(Date refundTime)
    {
        this.refundTime = refundTime;
    }

    public String getRefundFlowNo()
    {
        return refundFlowNo;
    }

    public void setRefundFlowNo(String refundFlowNo)
    {
        this.refundFlowNo = refundFlowNo;
    }

    public String getAppealContent()
    {
        return appealContent;
    }

    public void setAppealContent(String appealContent)
    {
        this.appealContent = appealContent;
    }

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    public Date getAppealTime()
    {
        return appealTime;
    }

    public void setAppealTime(Date appealTime)
    {
        this.appealTime = appealTime;
    }

    public String getAppealStatus()
    {
        return appealStatus;
    }

    public void setAppealStatus(String appealStatus)
    {
        this.appealStatus = appealStatus;
    }

    public String getAppealHandle()
    {
        return appealHandle;
    }

    public void setAppealHandle(String appealHandle)
    {
        this.appealHandle = appealHandle;
    }

    public String getDelFlag()
    {
        return delFlag;
    }

    public void setDelFlag(String delFlag)
    {
        this.delFlag = delFlag;
    }

    public String getOrderNo()
    {
        return orderNo;
    }

    public void setOrderNo(String orderNo)
    {
        this.orderNo = orderNo;
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

    @Override
    public String toString()
    {
        return "TakeoutComplaint{" +
                "complaintId=" + complaintId +
                ", orderId=" + orderId +
                ", type='" + type + '\'' +
                ", status='" + status + '\'' +
                '}';
    }
}
