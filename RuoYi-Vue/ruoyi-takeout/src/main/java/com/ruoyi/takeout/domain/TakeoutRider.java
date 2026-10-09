package com.ruoyi.takeout.domain;

import java.math.BigDecimal;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 外卖骑手实体
 *
 * @author ruoyi
 */
public class TakeoutRider extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 骑手ID */
    @Excel(name = "骑手ID", cellType = Excel.ColumnType.NUMERIC)
    private Long riderId;

    /** 骑手姓名 */
    @Excel(name = "姓名")
    private String name;

    /** 联系电话 */
    @Excel(name = "电话")
    private String phone;

    /** 头像URL */
    private String avatar;

    /** 接单状态(0接单中 1休息中 2离线) */
    @Excel(name = "状态", readConverterExp = "0=接单中,1=休息中,2=离线")
    private String status;

    /** 服务城市 */
    @Excel(name = "城市")
    private String city;

    /** 平均评分 */
    @Excel(name = "评分")
    private BigDecimal rating;

    /** 累计配送单量 */
    @Excel(name = "配送单量", cellType = Excel.ColumnType.NUMERIC)
    private Integer totalDeliveries;

    /** 累计收入 */
    @Excel(name = "累计收入")
    private BigDecimal totalIncome;

    /** 银行卡号 */
    private String bankCard;

    /** 开户行 */
    @Excel(name = "开户行")
    private String bankName;

    /** 紧急联系人 */
    @Excel(name = "紧急联系人")
    private String emergencyContact;

    /** 紧急联系电话 */
    @Excel(name = "紧急电话")
    private String emergencyPhone;

    /** 入职时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "入职时间")
    private Date joinTime;

    /** 删除标志(0存在 2删除) */
    private String delFlag;

    public Long getRiderId()
    {
        return riderId;
    }

    public void setRiderId(Long riderId)
    {
        this.riderId = riderId;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getPhone()
    {
        return phone;
    }

    public void setPhone(String phone)
    {
        this.phone = phone;
    }

    public String getAvatar()
    {
        return avatar;
    }

    public void setAvatar(String avatar)
    {
        this.avatar = avatar;
    }

    public String getStatus()
    {
        return status;
    }

    public void setStatus(String status)
    {
        this.status = status;
    }

    public String getCity()
    {
        return city;
    }

    public void setCity(String city)
    {
        this.city = city;
    }

    public BigDecimal getRating()
    {
        return rating;
    }

    public void setRating(BigDecimal rating)
    {
        this.rating = rating;
    }

    public Integer getTotalDeliveries()
    {
        return totalDeliveries;
    }

    public void setTotalDeliveries(Integer totalDeliveries)
    {
        this.totalDeliveries = totalDeliveries;
    }

    public BigDecimal getTotalIncome()
    {
        return totalIncome;
    }

    public void setTotalIncome(BigDecimal totalIncome)
    {
        this.totalIncome = totalIncome;
    }

    public String getBankCard()
    {
        return bankCard;
    }

    public void setBankCard(String bankCard)
    {
        this.bankCard = bankCard;
    }

    public String getBankName()
    {
        return bankName;
    }

    public void setBankName(String bankName)
    {
        this.bankName = bankName;
    }

    public String getEmergencyContact()
    {
        return emergencyContact;
    }

    public void setEmergencyContact(String emergencyContact)
    {
        this.emergencyContact = emergencyContact;
    }

    public String getEmergencyPhone()
    {
        return emergencyPhone;
    }

    public void setEmergencyPhone(String emergencyPhone)
    {
        this.emergencyPhone = emergencyPhone;
    }

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    public Date getJoinTime()
    {
        return joinTime;
    }

    public void setJoinTime(Date joinTime)
    {
        this.joinTime = joinTime;
    }

    public String getDelFlag()
    {
        return delFlag;
    }

    public void setDelFlag(String delFlag)
    {
        this.delFlag = delFlag;
    }

    @Override
    public String toString()
    {
        return "TakeoutRider{" +
                "riderId=" + riderId +
                ", name='" + name + '\'' +
                ", phone='" + phone + '\'' +
                ", status='" + status + '\'' +
                '}';
    }
}
