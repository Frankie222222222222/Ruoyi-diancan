package com.ruoyi.takeout.domain;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 外卖派单记录实体
 *
 * @author ruoyi
 */
public class TakeoutDispatch extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 派单ID */
    @Excel(name = "派单ID", cellType = Excel.ColumnType.NUMERIC)
    private Long dispatchId;

    /** 订单ID */
    @Excel(name = "订单ID", cellType = Excel.ColumnType.NUMERIC)
    private Long orderId;

    /** 骑手ID */
    @Excel(name = "骑手ID", cellType = Excel.ColumnType.NUMERIC)
    private Long riderId;

    /** 分配方式(0派单 1抢单) */
    @Excel(name = "方式", readConverterExp = "0=派单,1=抢单")
    private String dispatchType;

    /** 配送状态(0待接单 1已接单 2配送中 3已完成 4已取消) */
    @Excel(name = "状态", readConverterExp = "0=待接单,1=已接单,2=配送中,3=已完成,4=已取消")
    private String status;

    /** 派单时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "派单时间")
    private Date assignTime;

    /** 接单时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "接单时间")
    private Date acceptTime;

    /** 取餐时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "取餐时间")
    private Date pickupTime;

    /** 完成时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "完成时间")
    private Date completeTime;

    /** 预计送达时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "预计送达")
    private Date estimatedArrival;

    /** 取消时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "取消时间")
    private Date cancelTime;

    /** 取消原因 */
    @Excel(name = "取消原因")
    private String cancelReason;

    /** 配送距离(米) */
    @Excel(name = "配送距离")
    private Double deliveryDistance;

    /** 实际配送距离(米) */
    @Excel(name = "实际距离")
    private Double actualDistance;

    /** 骑手实时经度(GCJ-02坐标系) */
    @Excel(name = "骑手经度")
    private Double riderLng;

    /** 骑手实时纬度(GCJ-02坐标系) */
    @Excel(name = "骑手纬度")
    private Double riderLat;

    /** 骑手位置更新时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "位置上报时间")
    private Date locationUpdateTime;

    /** 删除标志(0存在 2删除) */
    private String delFlag;

    /** 扩展：订单号(查询用) */
    private String orderNo;

    /** 扩展：骑手姓名(查询用) */
    private String riderName;

    /** 扩展：骑手电话(查询用) */
    private String riderPhone;

    public Long getDispatchId()
    {
        return dispatchId;
    }

    public void setDispatchId(Long dispatchId)
    {
        this.dispatchId = dispatchId;
    }

    public Long getOrderId()
    {
        return orderId;
    }

    public void setOrderId(Long orderId)
    {
        this.orderId = orderId;
    }

    public Long getRiderId()
    {
        return riderId;
    }

    public void setRiderId(Long riderId)
    {
        this.riderId = riderId;
    }

    public String getDispatchType()
    {
        return dispatchType;
    }

    public void setDispatchType(String dispatchType)
    {
        this.dispatchType = dispatchType;
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
    public Date getAssignTime()
    {
        return assignTime;
    }

    public void setAssignTime(Date assignTime)
    {
        this.assignTime = assignTime;
    }

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    public Date getAcceptTime()
    {
        return acceptTime;
    }

    public void setAcceptTime(Date acceptTime)
    {
        this.acceptTime = acceptTime;
    }

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    public Date getPickupTime()
    {
        return pickupTime;
    }

    public void setPickupTime(Date pickupTime)
    {
        this.pickupTime = pickupTime;
    }

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    public Date getCompleteTime()
    {
        return completeTime;
    }

    public void setCompleteTime(Date completeTime)
    {
        this.completeTime = completeTime;
    }

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    public Date getEstimatedArrival()
    {
        return estimatedArrival;
    }

    public void setEstimatedArrival(Date estimatedArrival)
    {
        this.estimatedArrival = estimatedArrival;
    }

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    public Date getCancelTime()
    {
        return cancelTime;
    }

    public void setCancelTime(Date cancelTime)
    {
        this.cancelTime = cancelTime;
    }

    public String getCancelReason()
    {
        return cancelReason;
    }

    public void setCancelReason(String cancelReason)
    {
        this.cancelReason = cancelReason;
    }

    public Double getDeliveryDistance()
    {
        return deliveryDistance;
    }

    public void setDeliveryDistance(Double deliveryDistance)
    {
        this.deliveryDistance = deliveryDistance;
    }

    public Double getActualDistance()
    {
        return actualDistance;
    }

    public void setActualDistance(Double actualDistance)
    {
        this.actualDistance = actualDistance;
    }

    public Double getRiderLng()
    {
        return riderLng;
    }

    public void setRiderLng(Double riderLng)
    {
        this.riderLng = riderLng;
    }

    public Double getRiderLat()
    {
        return riderLat;
    }

    public void setRiderLat(Double riderLat)
    {
        this.riderLat = riderLat;
    }

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    public Date getLocationUpdateTime()
    {
        return locationUpdateTime;
    }

    public void setLocationUpdateTime(Date locationUpdateTime)
    {
        this.locationUpdateTime = locationUpdateTime;
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

    public String getRiderName()
    {
        return riderName;
    }

    public void setRiderName(String riderName)
    {
        this.riderName = riderName;
    }

    public String getRiderPhone()
    {
        return riderPhone;
    }

    public void setRiderPhone(String riderPhone)
    {
        this.riderPhone = riderPhone;
    }

    @Override
    public String toString()
    {
        return "TakeoutDispatch{" +
                "dispatchId=" + dispatchId +
                ", orderId=" + orderId +
                ", riderId=" + riderId +
                ", status='" + status + '\'' +
                ", riderLng=" + riderLng +
                ", riderLat=" + riderLat +
                '}';
    }
}
