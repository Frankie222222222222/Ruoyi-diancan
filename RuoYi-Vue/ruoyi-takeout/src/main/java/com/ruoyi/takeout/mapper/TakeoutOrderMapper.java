package com.ruoyi.takeout.mapper;

import java.util.List;
import com.ruoyi.takeout.domain.TakeoutOrder;
import com.ruoyi.takeout.domain.TakeoutOrderItem;
import org.apache.ibatis.annotations.Param;

/**
 * 订单 Mapper
 *
 * @author ruoyi
 */
public interface TakeoutOrderMapper
{
    /**
     * 查询订单列表
     */
    List<TakeoutOrder> selectOrderList(TakeoutOrder query);

    /**
     * 通过订单ID查询订单
     */
    TakeoutOrder selectOrderById(Long orderId);

    /**
     * 通过订单ID查询订单明细
     */
    List<TakeoutOrderItem> selectOrderItemsByOrderId(Long orderId);

    /**
     * 通过订单号查询
     */
    TakeoutOrder selectOrderByOrderNo(@Param("orderNo") String orderNo);

    /**
     * 新增订单
     */
    int insertOrder(TakeoutOrder order);

    /**
     * 批量新增订单明细
     */
    int insertOrderItems(@Param("list") List<TakeoutOrderItem> items);

    /**
     * 修改订单
     */
    int updateOrder(TakeoutOrder order);

    /**
     * 修改订单状态
     */
    int updateOrderStatus(TakeoutOrder order);

    /**
     * 取消订单
     */
    int cancelOrder(TakeoutOrder order);

    /**
     * 逻辑删除订单（按 ID 批量）
     */
    int deleteOrderByIds(Long[] orderIds);

    /**
     * 物理删除订单明细（按订单ID）
     */
    int deleteOrderItemsByOrderId(Long orderId);

    /**
     * 统计某菜品被多少未完成订单引用
     */
    int countActiveOrdersByDishId(@Param("dishId") Long dishId);

    /**
     * 列出引用过某菜品的所有订单
     */
    List<TakeoutOrder> selectOrdersByDishId(@Param("dishId") Long dishId);

    /**
     * 统计某商家的"进行中"订单数（status ∈ {0,1,2,3}）—— 用于删除商家前拦截
     */
    int countUnfinishedByMerchantId(@Param("merchantId") Long merchantId);

    /**
     * 商家统计概览（详情抽屉用）
     * 返回：菜品数 / 订单总数 / 今日订单数 / 近 30 日实付总额 / 今日实付总额
     */
    java.util.Map<String, Object> statByMerchantId(@Param("merchantId") Long merchantId);
}
