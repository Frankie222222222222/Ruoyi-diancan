package com.ruoyi.takeout.domain;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.annotation.Excel.ColumnType;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 堂食桌台对象 takeout_dine_table
 *
 * @author ruoyi
 */
public class TakeoutDineTable extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 桌台ID */
    @Excel(name = "桌台ID", cellType = ColumnType.NUMERIC)
    private Long tableId;

    /** 所属商家ID */
    @Excel(name = "商家ID", cellType = ColumnType.NUMERIC)
    private Long merchantId;

    /** 桌号(A12 / VIP-3) */
    @Excel(name = "桌号")
    private String tableNo;

    /** 容纳人数 */
    @Excel(name = "容纳人数", cellType = ColumnType.NUMERIC)
    private Integer capacity;

    /** 桌台状态(0空闲 1就餐中 2已结) */
    @Excel(name = "状态", readConverterExp = "0=空闲,1=就餐中,2=已结")
    private String status;

    /** 二维码URL */
    @Excel(name = "二维码URL")
    private String qrUrl;

    /** 删除标志(0存在 2删除) */
    private String delFlag;

    public Long getTableId() { return tableId; }
    public void setTableId(Long tableId) { this.tableId = tableId; }

    public Long getMerchantId() { return merchantId; }
    public void setMerchantId(Long merchantId) { this.merchantId = merchantId; }

    public String getTableNo() { return tableNo; }
    public void setTableNo(String tableNo) { this.tableNo = tableNo; }

    public Integer getCapacity() { return capacity; }
    public void setCapacity(Integer capacity) { this.capacity = capacity; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getQrUrl() { return qrUrl; }
    public void setQrUrl(String qrUrl) { this.qrUrl = qrUrl; }

    public String getDelFlag() { return delFlag; }
    public void setDelFlag(String delFlag) { this.delFlag = delFlag; }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
                .append("tableId", getTableId())
                .append("merchantId", getMerchantId())
                .append("tableNo", getTableNo())
                .append("capacity", getCapacity())
                .append("status", getStatus())
                .append("qrUrl", getQrUrl())
                .append("createBy", getCreateBy())
                .append("createTime", getCreateTime())
                .append("updateBy", getUpdateBy())
                .append("updateTime", getUpdateTime())
                .append("remark", getRemark())
                .toString();
    }
}
