package com.ruoyi.takeout.dto;

/**
 * 骑手实时位置上报/查询 DTO
 *
 * <p>仅用于轻量级位置上报与地图展示,不参与派单核心状态机。
 * 真实坐标由骑手 APP 周期性上报,开发期可由后台管理员手动写入。
 *
 * @author ruoyi
 */
public class RiderLocationDTO
{
    /** 派单ID(可选,指明要更新哪条派单的位置) */
    private Long dispatchId;

    /** 骑手ID */
    private Long riderId;

    /** 经度(GCJ-02) */
    private Double lng;

    /** 纬度(GCJ-02) */
    private Double lat;

    /** 位置上报时间(可选,默认 now) */
    private java.util.Date reportTime;

    public Long getDispatchId() { return dispatchId; }
    public void setDispatchId(Long dispatchId) { this.dispatchId = dispatchId; }

    public Long getRiderId() { return riderId; }
    public void setRiderId(Long riderId) { this.riderId = riderId; }

    public Double getLng() { return lng; }
    public void setLng(Double lng) { this.lng = lng; }

    public Double getLat() { return lat; }
    public void setLat(Double lat) { this.lat = lat; }

    public java.util.Date getReportTime() { return reportTime; }
    public void setReportTime(java.util.Date reportTime) { this.reportTime = reportTime; }
}
