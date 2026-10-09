package com.ruoyi.takeout.service.impl;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.takeout.domain.Dish;
import com.ruoyi.takeout.domain.TakeoutOrder;
import com.ruoyi.takeout.domain.TakeoutOrderItem;
import com.ruoyi.takeout.enums.OrderStatusEnum;
import com.ruoyi.takeout.mapper.DishMapper;
import com.ruoyi.takeout.mapper.TakeoutOrderMapper;
import com.ruoyi.takeout.service.ITakeoutOrderService;

/**
 * 订单 Service 业务实现
 *
 * G1/G2/G3 业务联动：下单扣库存+加销量，取消还库存，删除还库存+销量
 *
 * @author ruoyi
 */
@Service
public class TakeoutOrderServiceImpl implements ITakeoutOrderService
{
    private static final Logger log = LoggerFactory.getLogger(TakeoutOrderServiceImpl.class);

    private static final int ERR_ORDER_NO_DUP     = 2001;
    private static final int ERR_ORDER_NOT_FOUND  = 2002;
    private static final int ERR_INVALID_STATUS    = 2003;
    private static final int ERR_ILLEGAL_TRANSIT   = 2004;
    private static final int ERR_CANCEL_NOT_ALLOW  = 2005;
    private static final int ERR_STOCK_NOT_ENOUGH  = 2006;
    private static final int ERR_DISH_NOT_FOUND    = 2007;

    @Autowired
    private TakeoutOrderMapper orderMapper;

    @Autowired
    private DishMapper dishMapper;

    @Override
    public List<TakeoutOrder> selectOrderList(TakeoutOrder query)
    {
        return orderMapper.selectOrderList(query);
    }

    @Override
    public TakeoutOrder selectOrderById(Long orderId)
    {
        TakeoutOrder order = orderMapper.selectOrderById(orderId);
        if (order != null)
        {
            List<TakeoutOrderItem> items = orderMapper.selectOrderItemsByOrderId(orderId);
            order.setOrderItems(items);
        }
        return order;
    }

    @Override
    public boolean checkOrderNoUnique(TakeoutOrder order)
    {
        if (StringUtils.isEmpty(order.getOrderNo()))
        {
            return true;
        }
        Long currentId = StringUtils.isNull(order.getOrderId()) ? -1L : order.getOrderId();
        TakeoutOrder exist = orderMapper.selectOrderByOrderNo(order.getOrderNo());
        return exist == null || exist.getOrderId().equals(currentId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insertOrder(TakeoutOrder order)
    {
        if (StringUtils.isEmpty(order.getStatus()))
        {
            order.setStatus(OrderStatusEnum.UNPAID.getCode());
        }
        if (StringUtils.isEmpty(order.getPayStatus()))
        {
            order.setPayStatus("0");
        }
        if (!checkOrderNoUnique(order))
        {
            throw new ServiceException(
                String.format("[ERR_%d] 订单号「%s」已存在", ERR_ORDER_NO_DUP, order.getOrderNo()),
                ERR_ORDER_NO_DUP);
        }
        order.setCreateBy(SecurityUtils.getUsername());
        order.setCreateTime(DateUtils.getNowDate());
        int rows = orderMapper.insertOrder(order);
        if (rows > 0 && order.getOrderItems() != null && !order.getOrderItems().isEmpty())
        {
            // G1: 预校验 + 扣库存 + 加销量（任一失败回滚整笔订单）
            for (TakeoutOrderItem item : order.getOrderItems())
            {
                if (item.getDishId() == null || item.getQuantity() == null || item.getQuantity() <= 0)
                {
                    throw new ServiceException(
                        String.format("[ERR_%d] 订单明细缺少菜品ID或数量非法", ERR_STOCK_NOT_ENOUGH, item.getDishId()),
                        ERR_STOCK_NOT_ENOUGH);
                }
                Dish d = dishMapper.selectDishById(item.getDishId());
                if (d == null)
                {
                    throw new ServiceException(
                        String.format("[ERR_%d] 菜品不存在，dishId=%s", ERR_DISH_NOT_FOUND, item.getDishId()),
                        ERR_DISH_NOT_FOUND);
                }
                if (d.getStock() != null && d.getStock() < item.getQuantity())
                {
                    throw new ServiceException(
                        String.format("[ERR_%d] 菜品「%s」库存不足，当前=%s, 需=%s",
                            ERR_STOCK_NOT_ENOUGH, d.getDishName(), d.getStock(), item.getQuantity()),
                        ERR_STOCK_NOT_ENOUGH);
                }
            }
            for (TakeoutOrderItem item : order.getOrderItems())
            {
                item.setOrderId(order.getOrderId());
            }
            orderMapper.insertOrderItems(order.getOrderItems());
            // 真正扣减（带 GREATEST 兜底）
            for (TakeoutOrderItem item : order.getOrderItems())
            {
                Dish stockDelta = new Dish();
                stockDelta.setDishId(item.getDishId());
                stockDelta.setStock(-item.getQuantity());
                stockDelta.setUpdateBy(SecurityUtils.getUsername());
                dishMapper.adjustDishStock(stockDelta);
                // 销量累加
                dishMapper.incrDishSales(item.getDishId(), item.getQuantity());
            }
        }
        return rows;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateOrder(TakeoutOrder order)
    {
        order.setUpdateBy(SecurityUtils.getUsername());
        order.setUpdateTime(DateUtils.getNowDate());
        return orderMapper.updateOrder(order);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int changeOrderStatus(Long orderId, String targetStatus)
    {
        if (!OrderStatusEnum.isValid(targetStatus))
        {
            throw new ServiceException(
                String.format("[ERR_%d] 非法的订单状态：%s", ERR_INVALID_STATUS, targetStatus),
                ERR_INVALID_STATUS);
        }
        TakeoutOrder exist = orderMapper.selectOrderById(orderId);
        if (exist == null)
        {
            throw new ServiceException(
                String.format("[ERR_%d] 订单不存在，id=%s", ERR_ORDER_NOT_FOUND, orderId),
                ERR_ORDER_NOT_FOUND);
        }
        if (!OrderStatusEnum.canTransition(exist.getStatus(), targetStatus))
        {
            throw new ServiceException(
                String.format("[ERR_%d] 非法的状态变更：%s -> %s",
                    ERR_ILLEGAL_TRANSIT, exist.getStatus(), targetStatus),
                ERR_ILLEGAL_TRANSIT);
        }
        TakeoutOrder upd = new TakeoutOrder();
        upd.setOrderId(orderId);
        upd.setStatus(targetStatus);
        upd.setUpdateBy(SecurityUtils.getUsername());
        // 终态自动写完成时间
        if (OrderStatusEnum.COMPLETED.getCode().equals(targetStatus)
                || OrderStatusEnum.DELIVERED.getCode().equals(targetStatus))
        {
            upd.setCompleteTime(DateUtils.getNowDate());
        }
        int rows = orderMapper.updateOrderStatus(upd);
        log.info("order status changed: id={} from={} to={}", orderId, exist.getStatus(), targetStatus);
        return rows;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int cancelOrder(Long orderId, String reason)
    {
        TakeoutOrder exist = orderMapper.selectOrderById(orderId);
        if (exist == null)
        {
            throw new ServiceException(
                String.format("[ERR_%d] 订单不存在，id=%s", ERR_ORDER_NOT_FOUND, orderId),
                ERR_ORDER_NOT_FOUND);
        }
        // 仅待支付 / 已支付可取消
        String cur = exist.getStatus();
        if (!OrderStatusEnum.UNPAID.getCode().equals(cur)
                && !OrderStatusEnum.PAID.getCode().equals(cur))
        {
            throw new ServiceException(
                String.format("[ERR_%d] 当前状态「%s」不允许取消", ERR_CANCEL_NOT_ALLOW, cur),
                ERR_CANCEL_NOT_ALLOW);
        }
        // G2: 取消时还库存（仅未发货状态，因为已发货商品已出库）
        List<TakeoutOrderItem> items = orderMapper.selectOrderItemsByOrderId(orderId);
        if (items != null)
        {
            for (TakeoutOrderItem item : items)
            {
                Dish stockDelta = new Dish();
                stockDelta.setDishId(item.getDishId());
                stockDelta.setStock(item.getQuantity());
                stockDelta.setUpdateBy(SecurityUtils.getUsername());
                dishMapper.adjustDishStock(stockDelta);
            }
        }
        TakeoutOrder upd = new TakeoutOrder();
        upd.setOrderId(orderId);
        upd.setStatus(OrderStatusEnum.CANCELLED.getCode());
        upd.setCancelReason(reason);
        upd.setUpdateBy(SecurityUtils.getUsername());
        int rows = orderMapper.cancelOrder(upd);
        log.info("order cancelled: id={} reason={}", orderId, reason);
        return rows;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteOrderByIds(Long[] orderIds)
    {
        if (orderIds == null || orderIds.length == 0)
        {
            return 0;
        }
        // G3: 删除前先抓明细，还库存和销量（销量同步回滚，避免历史数据虚高）
        for (Long id : orderIds)
        {
            List<TakeoutOrderItem> items = orderMapper.selectOrderItemsByOrderId(id);
            if (items != null)
            {
                for (TakeoutOrderItem item : items)
                {
                    Dish stockDelta = new Dish();
                    stockDelta.setDishId(item.getDishId());
                    stockDelta.setStock(item.getQuantity());
                    stockDelta.setUpdateBy(SecurityUtils.getUsername());
                    dishMapper.adjustDishStock(stockDelta);
                    dishMapper.decrDishSales(item.getDishId(), item.getQuantity());
                }
            }
            orderMapper.deleteOrderItemsByOrderId(id);
        }
        return orderMapper.deleteOrderByIds(orderIds);
    }
}
