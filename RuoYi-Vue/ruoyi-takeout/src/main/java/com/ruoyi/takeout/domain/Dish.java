package com.ruoyi.takeout.domain;

import java.math.BigDecimal;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * ��Ʒ���� takeout_dish
 *
 * @author ruoyi
 */
public class Dish extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** ��Ʒ���� */
    private Long dishId;

    /** ��Ʒ���� */
    @Excel(name = "��Ʒ����")
    private String dishName;

    /** ��������ID */
    private Long categoryId;

    /** 商家ID（菜品所属商家，必填） */
    @Excel(name = "商家ID", cellType = Excel.ColumnType.NUMERIC)
    private Long merchantId;

    /** �������ƣ�������ѯʹ�ã� */
    @Excel(name = "����")
    private String categoryName;

    /** ��ƷͼƬURL */
    @Excel(name = "ͼƬ")
    private String image;

    /** �ۼ� */
    @Excel(name = "�۸�")
    private BigDecimal price;

    /** ��� */
    @Excel(name = "���")
    private Integer stock;

    /** ���� */
    @Excel(name = "����")
    private Integer sales;

    /** ״̬ 0�¼� 1�ϼ� */
    @Excel(name = "״̬", readConverterExp = "0=�¼�,1=�ϼ�")
    private String status;

    /** ���� */
    @Excel(name = "����")
    private String description;

    public Long getDishId() { return dishId; }
    public void setDishId(Long dishId) { this.dishId = dishId; }

    public String getDishName() { return dishName; }
    public void setDishName(String dishName) { this.dishName = dishName; }

    public Long getCategoryId() { return categoryId; }
    public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }

    public Long getMerchantId() { return merchantId; }
    public void setMerchantId(Long merchantId) { this.merchantId = merchantId; }

    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }

    public String getImage() { return image; }
    public void setImage(String image) { this.image = image; }

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }

    public Integer getStock() { return stock; }
    public void setStock(Integer stock) { this.stock = stock; }

    public Integer getSales() { return sales; }
    public void setSales(Integer sales) { this.sales = sales; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Override
    public Date getCreateTime() { return super.getCreateTime(); }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
                .append("dishId", getDishId())
                .append("dishName", getDishName())
                .append("categoryId", getCategoryId())
                .append("merchantId", getMerchantId())
                .append("categoryName", getCategoryName())
                .append("image", getImage())
                .append("price", getPrice())
                .append("stock", getStock())
                .append("sales", getSales())
                .append("status", getStatus())
                .append("description", getDescription())
                .append("createBy", getCreateBy())
                .append("createTime", getCreateTime())
                .append("updateBy", getUpdateBy())
                .append("updateTime", getUpdateTime())
                .toString();
    }
}