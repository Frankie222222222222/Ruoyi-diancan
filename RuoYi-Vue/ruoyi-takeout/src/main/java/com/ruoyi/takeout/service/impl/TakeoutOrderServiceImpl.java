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
import com.ruoyi.takeout.domain.TakeoutDineTable;
import com.ruoyi.takeout.domain.TakeoutOrder;
import com.ruoyi.takeout.domain.TakeoutOrderItem;
import com.ruoyi.takeout.enums.OrderStatusEnum;
import com.ruoyi.takeout.mapper.DishMapper;
import com.ruoyi.takeout.mapper.TakeoutDineTableMapper;
import com.ruoyi.takeout.mapper.TakeoutOrderMapper;
import com.ruoyi.takeout.payment.PaymentChannel;
import com.ruoyi.takeout.service.ITakeoutOrderService;
import com.ruoyi.takeout.service.ITakeoutPaymentService;
import com.ruoyi.takeout.util.OrderNoGenerator;
import org.springframework.context.annotation.Lazy;

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
    private static final int ERR_DISH_MERCHANT_MISMATCH = 2008;

    @Autowired
    private TakeoutOrderMapper orderMapper;

    @Autowired
    private DishMapper dishMapper;

    @Autowired
    private TakeoutDineTableMapper dineTableMapper;

    @Autowired
    @Lazy
    private ITakeoutPaymentService paymentService;

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
    public TakeoutOrder selectOrderByOrderNo(String orderNo)
    {
        TakeoutOrder order = orderMapper.selectOrderByOrderNo(orderNo);
        if (order != null)
        {
            List<TakeoutOrderItem> items = orderMapper.selectOrderItemsByOrderId(order.getOrderId());
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
            // v3 堂食订单默认 DRAFT, 外卖默认 UNPAID
            if (Integer.valueOf(1).equals(order.getOrderType()))
            {
                order.setStatus(OrderStatusEnum.DRAFT.getCode());
            }
            else
            {
                order.setStatus(OrderStatusEnum.UNPAID.getCode());
            }
        }
        if (StringUtils.isEmpty(order.getPayStatus()))
        {
            order.setPayStatus("0");
        }
        if (order.getOrderType() == null)
        {
            order.setOrderType(0);
        }
        // P2-B: 调用方没传 orderNo 时，服务端兜底生成（避免 controller 漏传导致 NULL）
        if (StringUtils.isEmpty(order.getOrderNo()))
        {
            order.setOrderNo(OrderNoGenerator.generate());
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
        // P2: 订单创建成功后挂一条 PENDING 支付记录(失败不阻塞下单,仅记日志)
        if (rows > 0)
        {
            try
            {
                String defaultChannel = StringUtils.isEmpty(order.getPayMethod()) ? "MOCK" : order.getPayMethod();
                paymentService.createPaymentRecord(
                        order.getOrderId(),
                        order.getOrderNo(),
                        order.getTotalAmount(),
                        PaymentChannel.fromCode(defaultChannel));
            }
            catch (Exception e)
            {
                log.warn("[order.create] 挂单支付记录失败 orderId={} err={}", order.getOrderId(), e.getMessage());
            }
        }
        if (rows > 0 && order.getOrderItems() != null && !order.getOrderItems().isEmpty())
        {
            // G1: 预校验 + 跨商家一致性 + 扣库存 + 加销量（任一失败回滚整笔订单）
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
                // 2026-10-11 v4 修复: 把菜品信息(名称/单价/小计)补到 item,避免数据库 NOT NULL 失败
                if (item.getDishName() == null) item.setDishName(d.getDishName());
                if (item.getDishImage() == null) item.setDishImage(d.getImage());
                if (item.getPrice() == null) item.setPrice(d.getPrice());
                if (item.getSubtotal() == null && item.getPrice() != null && item.getQuantity() != null)
                {
                    item.setSubtotal(item.getPrice().multiply(new java.math.BigDecimal(item.getQuantity())));
                }
                // G6: 跨商家一致性 —— 订单的所有明细菜品必须属于同一个商家
                if (order.getMerchantId() != null && d.getMerchantId() != null
                        && !order.getMerchantId().equals(d.getMerchantId()))
                {
                    throw new ServiceException(
                        String.format("[ERR_%d] 订单所属商家(%s)与菜品「%s」所属商家(%s)不一致，请勿混合下单",
                            ERR_DISH_MERCHANT_MISMATCH, order.getMerchantId(), d.getDishName(), d.getMerchantId()),
                        ERR_DISH_MERCHANT_MISMATCH);
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
                // P2-B 修复: subtotal 是 NOT NULL,服务端兜底计算 (price * qty)
                if (item.getPrice() != null && item.getQuantity() != null && item.getSubtotal() == null)
                {
                    item.setSubtotal(item.getPrice().multiply(new java.math.BigDecimal(item.getQuantity())));
                }
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
        // v2 (2026-10-10): 进入 MAKING 记后厨接单时间
        if (OrderStatusEnum.MAKING.getCode().equals(targetStatus))
        {
            upd.setKitchenAcceptTime(DateUtils.getNowDate());
        }
        // v2: 进入 READY 记出餐完毕时间
        if (OrderStatusEnum.READY.getCode().equals(targetStatus))
        {
            upd.setReadyTime(DateUtils.getNowDate());
        }
        // v2: 进入 DELIVERING 记骑手接单时间
        if (OrderStatusEnum.DELIVERING.getCode().equals(targetStatus))
        {
            upd.setRiderAcceptTime(DateUtils.getNowDate());
        }
        // P2-A: 状态变更为"已退款(7)"时，归还库存并回退销量（与 cancelOrder 对称）
        if (OrderStatusEnum.REFUNDED.getCode().equals(targetStatus))
        {
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
                    dishMapper.decrDishSales(item.getDishId(), item.getQuantity());
                }
            }
            log.info("order refunded: stock+sales reverted, id={}", orderId);
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
        // 仅待支付 / 已支付 / 堂食点菜中(DRAFT) 可取消
        String cur = exist.getStatus();
        if (!OrderStatusEnum.UNPAID.getCode().equals(cur)
                && !OrderStatusEnum.PAID.getCode().equals(cur)
                && !OrderStatusEnum.DRAFT.getCode().equals(cur))
        {
            throw new ServiceException(
                String.format("[ERR_%d] 当前状态「%s」不允许取消", ERR_CANCEL_NOT_ALLOW, cur),
                ERR_CANCEL_NOT_ALLOW);
        }
        // G2: 取消时还库存 + 减销量（库存因取消归还，销量因订单未成交回退）
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
                // P2-A: 取消时同步回退销量，避免"取消后销量虚高"
                dishMapper.decrDishSales(item.getDishId(), item.getQuantity());
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

    @Override
    public java.util.Map<String, Object> statByMerchantId(Long merchantId)
    {
        if (merchantId == null)
        {
            return new java.util.HashMap<>();
        }
        return orderMapper.statByMerchantId(merchantId);
    }

    /* ========== v2 扩展(2026-10-10) ========== */

    @Override
    public List<TakeoutOrder> selectRiderAvailableOrders()
    {
        List<TakeoutOrder> list = orderMapper.selectRiderAvailableOrders();
        if (list != null)
        {
            for (TakeoutOrder o : list)
            {
                List<TakeoutOrderItem> items = orderMapper.selectOrderItemsByOrderId(o.getOrderId());
                o.setOrderItems(items);
            }
        }
        return list;
    }

    @Override
    public List<TakeoutOrder> selectKitchenVisibleOrders()
    {
        List<TakeoutOrder> list = orderMapper.selectKitchenVisibleOrders();
        if (list != null)
        {
            for (TakeoutOrder o : list)
            {
                List<TakeoutOrderItem> items = orderMapper.selectOrderItemsByOrderId(o.getOrderId());
                o.setOrderItems(items);
            }
        }
        return list;
    }

    @Override
    public java.util.Map<String, Object> countKitchenPending()
    {
        return orderMapper.countKitchenPending();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int riderGrabOrder(Long orderId, Long riderId)
    {
        if (orderId == null || riderId == null)
        {
            throw new ServiceException("[ERR_2002] 订单ID/骑手ID不能为空", 2002);
        }
        // 行锁 + 状态校验(SELECT ... FOR UPDATE)
        TakeoutOrder exist = orderMapper.selectOrderForRiderGrab(orderId);
        if (exist == null)
        {
            throw new ServiceException(
                String.format("[ERR_%d] 订单不存在或已被其他骑手抢走，请刷新", 2002, orderId),
                2002);
        }
        if (!OrderStatusEnum.READY.getCode().equals(exist.getStatus()))
        {
            throw new ServiceException(
                String.format("[ERR_%d] 订单状态已变更(当前=%s)，无法抢单",
                    2003, exist.getStatus()),
                2003);
        }
        // 复用状态机:READY → DELIVERING
        TakeoutOrder upd = new TakeoutOrder();
        upd.setOrderId(orderId);
        upd.setStatus(OrderStatusEnum.DELIVERING.getCode());
        upd.setRiderId(riderId);
        upd.setRiderAcceptTime(DateUtils.getNowDate());
        upd.setUpdateBy("rider:" + riderId);
        int rows = orderMapper.updateOrderStatus(upd);
        if (rows > 0)
        {
            log.info("rider grab order: orderId={} riderId={}", orderId, riderId);
        }
        return rows;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int kitchenAcceptOrder(Long orderId, Long kitchenId)
    {
        if (orderId == null)
        {
            throw new ServiceException("[ERR_2002] 订单ID不能为空", 2002);
        }
        // 校验当前状态(PAID 或 旧值 ACCEPTED 才能转 MAKING)
        TakeoutOrder exist = orderMapper.selectOrderById(orderId);
        if (exist == null)
        {
            throw new ServiceException(String.format("[ERR_2002] 订单不存在 id=%s", orderId), 2002);
        }
        String target = OrderStatusEnum.MAKING.getCode();
        if (!OrderStatusEnum.canTransition(exist.getStatus(), target))
        {
            throw new ServiceException(
                String.format("[ERR_2004] 当前状态「%s」无法转为「%s」",
                    exist.getStatus(), target),
                2004);
        }
        // 直接走 changeOrderStatus 写时间戳,再 updateOrder 写 kitchenId
        changeOrderStatus(orderId, target);
        if (kitchenId != null)
        {
            TakeoutOrder upd = new TakeoutOrder();
            upd.setOrderId(orderId);
            upd.setKitchenId(kitchenId);
            upd.setUpdateBy("kitchen:" + kitchenId);
            return orderMapper.updateOrder(upd);
        }
        return 1;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int kitchenReadyOrder(Long orderId, Long kitchenId)
    {
        if (orderId == null)
        {
            throw new ServiceException("[ERR_2002] 订单ID不能为空", 2002);
        }
        TakeoutOrder exist = orderMapper.selectOrderById(orderId);
        if (exist == null)
        {
            throw new ServiceException(String.format("[ERR_2002] 订单不存在 id=%s", orderId), 2002);
        }
        String target = OrderStatusEnum.READY.getCode();
        if (!OrderStatusEnum.canTransition(exist.getStatus(), target))
        {
            throw new ServiceException(
                String.format("[ERR_2004] 当前状态「%s」无法转为「%s」(后厨必须先接单)",
                    exist.getStatus(), target),
                2004);
        }
        changeOrderStatus(orderId, target);
        if (kitchenId != null && exist.getKitchenId() == null)
        {
            // 兼容:后厨一直没声明过 kitchenId,补一下
            TakeoutOrder upd = new TakeoutOrder();
            upd.setOrderId(orderId);
            upd.setKitchenId(kitchenId);
            upd.setUpdateBy("kitchen:" + kitchenId);
            return orderMapper.updateOrder(upd);
        }
        return 1;
    }

    /* ========== v3 堂食扩展(2026-10-10) ========== */

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int createDineOrder(TakeoutOrder order)
    {
        if (order == null || order.getMerchantId() == null || order.getTableId() == null)
        {
            throw new ServiceException("[ERR_2002] 堂食订单缺少 merchantId / tableId", 2002);
        }
        if (order.getOrderItems() == null || order.getOrderItems().isEmpty())
        {
            throw new ServiceException("[ERR_2006] 堂食订单至少需要 1 个菜品", 2006);
        }
        // 校验桌台存在
        TakeoutDineTable table = dineTableMapper.selectDineTableById(order.getTableId());
        if (table == null || !table.getMerchantId().equals(order.getMerchantId()))
        {
            throw new ServiceException(
                String.format("[ERR_2002] 桌台不存在或与商家不匹配 tableId=%s", order.getTableId()),
                2002);
        }
        // 落库(复用 insertOrder,orderType=1 自动 DRAFT)
        order.setOrderType(1);
        // 堂食无配送费
        if (order.getDeliveryFee() == null)
        {
            order.setDeliveryFee(java.math.BigDecimal.ZERO);
        }
        int rows = insertOrder(order);
        if (rows > 0)
        {
            // 桌台置就餐中
            dineTableMapper.updateDineTableStatus(toStatusTable(order.getTableId(), "1"));
        }
        return rows;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int addDineOrderItem(Long orderId, TakeoutOrderItem item)
    {
        if (orderId == null || item == null || item.getDishId() == null)
        {
            throw new ServiceException("[ERR_2002] 加菜缺少 orderId/dishId", 2002);
        }
        TakeoutOrder exist = orderMapper.selectOrderById(orderId);
        if (exist == null || !Integer.valueOf(1).equals(exist.getOrderType()))
        {
            throw new ServiceException("[ERR_2002] 堂食订单不存在", 2002);
        }
        if (!OrderStatusEnum.DRAFT.getCode().equals(exist.getStatus()))
        {
            throw new ServiceException(
                String.format("[ERR_2003] 仅 DRAFT 状态可加菜,当前=%s", exist.getStatus()),
                2003);
        }
        Dish d = dishMapper.selectDishById(item.getDishId());
        if (d == null)
        {
            throw new ServiceException(String.format("[ERR_2007] 菜品不存在 dishId=%s", item.getDishId()), 2007);
        }
        if (!d.getMerchantId().equals(exist.getMerchantId()))
        {
            throw new ServiceException("[ERR_2008] 菜品商家不匹配", 2008);
        }
        int qty = item.getQuantity() == null || item.getQuantity() <= 0 ? 1 : item.getQuantity();
        item.setOrderId(orderId);
        item.setDishName(d.getDishName());
        item.setDishImage(d.getImage());
        item.setPrice(d.getPrice());
        item.setSubtotal(d.getPrice().multiply(new java.math.BigDecimal(qty)));
        orderMapper.insertOrderItems(java.util.Collections.singletonList(item));
        // 重新计算金额
        java.math.BigDecimal total = recomputeDineOrderTotal(orderId);
        TakeoutOrder upd = new TakeoutOrder();
        upd.setOrderId(orderId);
        upd.setTotalAmount(total);
        upd.setActualAmount(total.subtract(exist.getDiscountAmount() == null ? java.math.BigDecimal.ZERO : exist.getDiscountAmount()));
        upd.setUpdateBy("dine:addItem");
        return orderMapper.updateOrder(upd);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int payDineOrder(Long orderId)
    {
        if (orderId == null)
        {
            throw new ServiceException("[ERR_2002] orderId 必填", 2002);
        }
        TakeoutOrder exist = orderMapper.selectOrderById(orderId);
        if (exist == null)
        {
            throw new ServiceException("[ERR_2002] 订单不存在", 2002);
        }
        if (!OrderStatusEnum.DRAFT.getCode().equals(exist.getStatus()))
        {
            throw new ServiceException(
                String.format("[ERR_2003] 仅 DRAFT 可结账,当前=%s", exist.getStatus()),
                2003);
        }
        // 走状态机 DRAFT → PAID(状态机已支持),但走 changeOrderStatus 会校验时序,所以直接走专用路径:
        TakeoutOrder upd = new TakeoutOrder();
        upd.setOrderId(orderId);
        upd.setStatus(OrderStatusEnum.PAID.getCode());
        upd.setPayStatus("1");
        upd.setPayTime(DateUtils.getNowDate());
        upd.setUpdateBy("dine:pay");
        int rows = orderMapper.updateOrderStatus(upd);
        if (rows > 0)
        {
            log.info("dine order paid: id={} amount={}", orderId, exist.getTotalAmount());
        }
        return rows;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int confirmDineOrderServed(Long orderId)
    {
        // READY → DONE
        changeOrderStatus(orderId, OrderStatusEnum.DONE.getCode());
        // 释放桌台:仅当该桌台没有其他进行中订单
        TakeoutOrder order = orderMapper.selectOrderById(orderId);
        if (order != null && order.getTableId() != null)
        {
            int active = dineTableMapper.countActiveDineOrdersByTableId(order.getTableId());
            if (active == 0)
            {
                TakeoutDineTable t = toStatusTable(order.getTableId(), "0");
                dineTableMapper.updateDineTableStatus(t);
            }
        }
        return 1;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int cancelDineOrder(Long orderId, String reason)
    {
        TakeoutOrder exist = orderMapper.selectOrderById(orderId);
        if (exist == null)
        {
            throw new ServiceException("[ERR_2002] 订单不存在", 2002);
        }
        if (!Integer.valueOf(1).equals(exist.getOrderType()))
        {
            throw new ServiceException("[ERR_2008] 非堂食订单,请用 /takeout/order/cancel", 2008);
        }
        // 复用通用 cancelOrder(已扩展支持 DRAFT/PAID)
        int rows = cancelOrder(orderId, reason == null ? "堂食取消" : reason);
        // 释放桌台
        if (exist.getTableId() != null)
        {
            int active = dineTableMapper.countActiveDineOrdersByTableId(exist.getTableId());
            if (active == 0)
            {
                dineTableMapper.updateDineTableStatus(toStatusTable(exist.getTableId(), "0"));
            }
        }
        return rows;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int autoCancelExpiredDineOrders()
    {
        // 30 分钟未支付的 DRAFT 堂食订单
        int rows = orderMapper.cancelExpiredDineOrders(30);
        if (rows > 0)
        {
            log.info("auto cancelled {} expired dine orders (>30min DRAFT)", rows);
            // 同步释放桌台(找出受影响的桌台)
            TakeoutOrder q = new TakeoutOrder();
            q.setOrderType(1);
            q.setStatus(OrderStatusEnum.CANCELLED.getCode());
            List<TakeoutOrder> cancelled = orderMapper.selectOrderList(q);
            if (cancelled != null)
            {
                for (TakeoutOrder o : cancelled)
                {
                    if (o.getTableId() == null) continue;
                    int active = dineTableMapper.countActiveDineOrdersByTableId(o.getTableId());
                    if (active == 0)
                    {
                        dineTableMapper.updateDineTableStatus(toStatusTable(o.getTableId(), "0"));
                    }
                }
            }
        }
        return rows;
    }

    @Override
    public List<TakeoutOrder> selectDineOrdersByTable(Long tableId, String status)
    {
        return orderMapper.selectDineOrdersByTable(tableId, status);
    }

    @Override
    public List<TakeoutOrder> selectActiveDineOrdersByTable(Long tableId)
    {
        return orderMapper.selectDineOrdersByTable(tableId, null);
    }

    /** 重新计算某堂食订单的 total/actual amount(基于明细) */
    private java.math.BigDecimal recomputeDineOrderTotal(Long orderId)
    {
        List<TakeoutOrderItem> items = orderMapper.selectOrderItemsByOrderId(orderId);
        java.math.BigDecimal total = java.math.BigDecimal.ZERO;
        if (items != null)
        {
            for (TakeoutOrderItem i : items)
            {
                if (i.getSubtotal() != null)
                {
                    total = total.add(i.getSubtotal());
                }
                else if (i.getPrice() != null && i.getQuantity() != null)
                {
                    total = total.add(i.getPrice().multiply(new java.math.BigDecimal(i.getQuantity())));
                }
            }
        }
        return total;
    }

    /** 桌台状态更新 helper(避免重复 set) */
    private TakeoutDineTable toStatusTable(Long tableId, String status)
    {
        TakeoutDineTable t = new TakeoutDineTable();
        t.setTableId(tableId);
        t.setStatus(status);
        t.setUpdateBy("system");
        return t;
    }
}
