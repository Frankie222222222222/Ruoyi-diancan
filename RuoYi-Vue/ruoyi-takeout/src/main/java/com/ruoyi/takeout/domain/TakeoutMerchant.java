package com.ruoyi.takeout.domain;

import java.util.Date;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.annotation.Excel.ColumnType;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * �����̼Ҷ��� takeout_merchant
 *
 * @author ruoyi
 */
public class TakeoutMerchant extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** �̼�ID */
    @Excel(name = "�̼����", cellType = ColumnType.NUMERIC)
    private Long merchantId;

    /** �̼����� */
    @Excel(name = "�̼�����")
    @NotBlank(message = "�̼����Ʋ���Ϊ��")
    @Size(min = 0, max = 100, message = "�̼����Ʋ��ܳ���100���ַ�")
    private String merchantName;

    /** ��ϵ�� */
    @Excel(name = "��ϵ��")
    @Size(min = 0, max = 50, message = "��ϵ�˲��ܳ���50���ַ�")
    private String contactName;

    /** ��ϵ�绰 */
    @Excel(name = "��ϵ�绰")
    @Size(min = 0, max = 20, message = "��ϵ�绰���ܳ���20���ַ�")
    private String contactPhone;

    /** �̼ҵ�ַ */
    @Excel(name = "�̼ҵ�ַ")
    @Size(min = 0, max = 255, message = "�̼ҵ�ַ���ܳ���255���ַ�")
    private String address;

    /** LogoͼƬ */
    @Size(min = 0, max = 255, message = "Logo��ַ���Ȳ��ܳ���255���ַ�")
    private String logo;

    /** Ӫҵִ��ͼƬ */
    @Size(min = 0, max = 255, message = "Ӫҵִ�յ�ַ���Ȳ��ܳ���255���ַ�")
    private String businessLicense;

    /** Ӫҵ״̬��0Ӫҵ�� 1�Ѵ��ȣ� */
    @Excel(name = "Ӫҵ״̬", readConverterExp = "0=Ӫҵ��,1=�Ѵ���")
    private String status;

    /** ���״̬��0����� 1ͨ�� 2���أ� */
    @Excel(name = "���״̬", readConverterExp = "0=�����,1=ͨ��,2=����")
    private String auditStatus;

    /** ��˱�ע */
    @Size(min = 0, max = 255, message = "��˱�ע���ܳ���255���ַ�")
    private String auditRemark;

    /** ��sys_user.user_id */
    @Excel(name = "���û�ID", cellType = ColumnType.NUMERIC)
    private Long userId;

    /** ��ϵͳ�û����ǳƣ���չʾ�ã����־û��� */
    @Excel(name = "���û�")
    private String bindUserName;

    /** ɾ����־��0���� 2ɾ���� */
    private String delFlag;

    public Long getMerchantId()
    {
        return merchantId;
    }

    public void setMerchantId(Long merchantId)
    {
        this.merchantId = merchantId;
    }

    @NotBlank(message = "�̼����Ʋ���Ϊ��")
    @Size(min = 0, max = 100, message = "�̼����Ʋ��ܳ���100���ַ�")
    public String getMerchantName()
    {
        return merchantName;
    }

    public void setMerchantName(String merchantName)
    {
        this.merchantName = merchantName;
    }

    @Size(min = 0, max = 50, message = "��ϵ�˲��ܳ���50���ַ�")
    public String getContactName()
    {
        return contactName;
    }

    public void setContactName(String contactName)
    {
        this.contactName = contactName;
    }

    @Size(min = 0, max = 20, message = "��ϵ�绰���ܳ���20���ַ�")
    public String getContactPhone()
    {
        return contactPhone;
    }

    public void setContactPhone(String contactPhone)
    {
        this.contactPhone = contactPhone;
    }

    @Size(min = 0, max = 255, message = "�̼ҵ�ַ���ܳ���255���ַ�")
    public String getAddress()
    {
        return address;
    }

    public void setAddress(String address)
    {
        this.address = address;
    }

    public String getLogo()
    {
        return logo;
    }

    public void setLogo(String logo)
    {
        this.logo = logo;
    }

    public String getBusinessLicense()
    {
        return businessLicense;
    }

    public void setBusinessLicense(String businessLicense)
    {
        this.businessLicense = businessLicense;
    }

    public String getStatus()
    {
        return status;
    }

    public void setStatus(String status)
    {
        this.status = status;
    }

    public String getAuditStatus()
    {
        return auditStatus;
    }

    public void setAuditStatus(String auditStatus)
    {
        this.auditStatus = auditStatus;
    }

    public String getAuditRemark()
    {
        return auditRemark;
    }

    public void setAuditRemark(String auditRemark)
    {
        this.auditRemark = auditRemark;
    }

    public Long getUserId()
    {
        return userId;
    }

    public void setUserId(Long userId)
    {
        this.userId = userId;
    }

    public String getBindUserName()
    {
        return bindUserName;
    }

    public void setBindUserName(String bindUserName)
    {
        this.bindUserName = bindUserName;
    }

    public String getDelFlag()
    {
        return delFlag;
    }

    public void setDelFlag(String delFlag)
    {
        this.delFlag = delFlag;
    }

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    public Date getCreateTime()
    {
        return super.getCreateTime();
    }

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    public Date getUpdateTime()
    {
        return super.getUpdateTime();
    }

    @Override
    public String toString()
    {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
            .append("merchantId", getMerchantId())
            .append("merchantName", getMerchantName())
            .append("contactName", getContactName())
            .append("contactPhone", getContactPhone())
            .append("address", getAddress())
            .append("logo", getLogo())
            .append("businessLicense", getBusinessLicense())
            .append("status", getStatus())
            .append("auditStatus", getAuditStatus())
            .append("auditRemark", getAuditRemark())
            .append("userId", getUserId())
            .append("delFlag", getDelFlag())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("remark", getRemark())
            .toString();
    }
}