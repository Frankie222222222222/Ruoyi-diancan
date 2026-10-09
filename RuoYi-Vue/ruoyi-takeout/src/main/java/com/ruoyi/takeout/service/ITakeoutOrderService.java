package com.ruoyi.takeout.service;

import java.util.List;
import com.ruoyi.takeout.domain.TakeoutOrder;

/**
 * 订单 Service
 *
 * @author ruoyi
 */
public interface ITakeoutOrderService
{
    /**
     * 查询订单列表
     */
    List<TakeoutOrder> selectOrderList(TakeoutOrder query);

    /**
     * 查询订单详情（包含明细）
     */
    TakeoutOrder selectOrderById(Long orderId);

    /**
     * 校验订单号唯一
     */
    boolean checkOrderNoUnique(TakeoutOrder order);

    /**
     * 新增订单（含明细）
     */
    int insertOrder(TakeoutOrder order);

    /**
     * 修改订单
     */
    int updateOrder(TakeoutOrder order);

    /**
     * 修改订单状态
     */
    int changeOrderStatus(Long orderId, String targetStatus);

    /**
     * 取消订单
     */
    int cancelOrder(Long orderId, String reason);

    /**
     * 逻辑删除订单
     */
    int deleteOrderByIds(Long[] orderIds);
}
