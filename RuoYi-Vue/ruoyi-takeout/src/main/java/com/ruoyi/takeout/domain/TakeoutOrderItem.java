package com.ruoyi.takeout.domain;

import java.math.BigDecimal;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.annotation.Excel.ColumnType;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 订单商品对象 takeout_order_item
 *
 * @author ruoyi
 */
public class TakeoutOrderItem extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 明细ID */
    @Excel(name = "明细ID", cellType = ColumnType.NUMERIC)
    private Long itemId;

    /** 订单ID */
    @Excel(name = "订单ID", cellType = ColumnType.NUMERIC)
    private Long orderId;

    /** 菜品ID */
    @Excel(name = "菜品ID", cellType = ColumnType.NUMERIC)
    private Long dishId;

    /** 菜品名称（冗余） */
    @Excel(name = "菜品")
    private String dishName;

    /** 菜品图片（冗余） */
    private String dishImage;

    /** 下单单价 */
    @Excel(name = "单价")
    private BigDecimal price;

    /** 数量 */
    @Excel(name = "数量", cellType = ColumnType.NUMERIC)
    private Integer quantity;

    /** 小计金额 */
    @Excel(name = "小计")
    private BigDecimal subtotal;

    public Long getItemId()
    {
        return itemId;
    }

    public void setItemId(Long itemId)
    {
        this.itemId = itemId;
    }

    public Long getOrderId()
    {
        return orderId;
    }

    public void setOrderId(Long orderId)
    {
        this.orderId = orderId;
    }

    public Long getDishId()
    {
        return dishId;
    }

    public void setDishId(Long dishId)
    {
        this.dishId = dishId;
    }

    public String getDishName()
    {
        return dishName;
    }

    public void setDishName(String dishName)
    {
        this.dishName = dishName;
    }

    public String getDishImage()
    {
        return dishImage;
    }

    public void setDishImage(String dishImage)
    {
        this.dishImage = dishImage;
    }

    public BigDecimal getPrice()
    {
        return price;
    }

    public void setPrice(BigDecimal price)
    {
        this.price = price;
    }

    public Integer getQuantity()
    {
        return quantity;
    }

    public void setQuantity(Integer quantity)
    {
        this.quantity = quantity;
    }

    public BigDecimal getSubtotal()
    {
        return subtotal;
    }

    public void setSubtotal(BigDecimal subtotal)
    {
        this.subtotal = subtotal;
    }

    @Override
    public String toString()
    {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
                .append("itemId", getItemId())
                .append("orderId", getOrderId())
                .append("dishId", getDishId())
                .append("dishName", getDishName())
                .append("dishImage", getDishImage())
                .append("price", getPrice())
                .append("quantity", getQuantity())
                .append("subtotal", getSubtotal())
                .toString();
    }
}
