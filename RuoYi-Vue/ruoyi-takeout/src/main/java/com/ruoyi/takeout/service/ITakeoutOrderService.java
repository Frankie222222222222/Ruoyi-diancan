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
     * 通过订单号查询(包含明细)
     */
    TakeoutOrder selectOrderByOrderNo(String orderNo);

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

    /**
     * 商家统计概览（详情抽屉用）
     * @return Map { dishCount, orderCount, todayOrderCount, totalAmount30d, todayAmount }
     */
    java.util.Map<String, Object> statByMerchantId(Long merchantId);

    /* ========== v2 扩展(2026-10-10) ========== */

    /**
     * 骑手可见订单(status = 2b/READY)
     */
    List<TakeoutOrder> selectRiderAvailableOrders();

    /**
     * 后厨可见订单(PAID + 旧值 ACCEPTED + MAKING)
     */
    List<TakeoutOrder> selectKitchenVisibleOrders();

    /**
     * 后厨看板统计
     */
    java.util.Map<String, Object> countKitchenPending();

    /**
     * 骑手抢单(原子操作:行锁 + 状态校验 + 状态机)
     * @param orderId 订单ID
     * @param riderId 骑手ID
     * @return 影响的行数
     * @throws ServiceException 订单非 READY 状态、订单不存在、骑手状态异常
     */
    int riderGrabOrder(Long orderId, Long riderId);

    /**
     * 后厨一键升级(ACCEPTED → MAKING,旧数据兼容)
     */
    int kitchenAcceptOrder(Long orderId, Long kitchenId);

    /**
     * 后厨出餐(MAKING → READY)
     */
    int kitchenReadyOrder(Long orderId, Long kitchenId);

    /* ========== v3 堂食扩展(2026-10-10) ========== */

    /**
     * 堂食点菜: 创建 DRAFT 订单(order_type=1, status='0a')
     * <p>不写 delivery_address / dispatch 字段;桌台状态联动置为"就餐中"。</p>
     */
    int createDineOrder(TakeoutOrder order);

    /**
     * 堂食加菜(DRAFT 状态追加菜品并重算金额)
     */
    int addDineOrderItem(Long orderId, com.ruoyi.takeout.domain.TakeoutOrderItem item);

    /**
     * 堂食订单结账(DRAFT → PAID,复用支付流程)
     * <p>顾客扫码后调此接口,触发支付单生成 + 状态机迁移。</p>
     */
    int payDineOrder(Long orderId);

    /**
     * 堂食订单确认上桌(READY → DONE,触发桌台释放回空闲)
     */
    int confirmDineOrderServed(Long orderId);

    /**
     * 堂食订单取消(任意进行中状态 → CANCELLED,还库存+还桌台)
     */
    int cancelDineOrder(Long orderId, String reason);

    /**
     * 定时任务:超时 30 分钟未支付的 DRAFT 堂食订单自动取消
     * @return 影响的行数
     */
    int autoCancelExpiredDineOrders();

    /**
     * 堂食订单列表(按桌台 + 状态)
     */
    List<TakeoutOrder> selectDineOrdersByTable(Long tableId, String status);

    /**
     * 桌台当前进行中堂食订单
     */
    List<TakeoutOrder> selectActiveDineOrdersByTable(Long tableId);
}
