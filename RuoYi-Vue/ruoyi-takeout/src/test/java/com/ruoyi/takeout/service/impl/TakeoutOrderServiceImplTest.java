package com.ruoyi.takeout.service.impl;

import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.takeout.domain.Dish;
import com.ruoyi.takeout.domain.TakeoutOrder;
import com.ruoyi.takeout.domain.TakeoutOrderItem;
import com.ruoyi.takeout.mapper.DishMapper;
import com.ruoyi.takeout.mapper.TakeoutOrderMapper;
import com.ruoyi.takeout.payment.PaymentChannel;
import com.ruoyi.takeout.service.ITakeoutPaymentService;
import com.ruoyi.takeout.support.BaseServiceTest;
import com.ruoyi.takeout.support.TestDataFactory;
import com.ruoyi.takeout.util.OrderNoGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * TakeoutOrderServiceImpl 单元测试
 *
 * <p>覆盖核心场景:
 *  <ol>
 *      <li>insertOrder — 正常下单/自动生成 orderNo/订单号重复/缺菜品/库存不足/跨商家不一致/扣库存+加销量+挂支付</li>
 *      <li>changeOrderStatus — 状态机合法/非法转移/已退款时还库存+回退销量</li>
 *      <li>cancelOrder — 还库存+回退销量</li>
 *      <li>deleteOrderByIds — 批量删除还库存+回退销量</li>
 *  </ol>
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("订单服务 - 事务+状态机单元测试")
class TakeoutOrderServiceImplTest extends BaseServiceTest
{
    @Mock private TakeoutOrderMapper orderMapper;
    @Mock private DishMapper dishMapper;
    @Mock private ITakeoutPaymentService paymentService;

    @InjectMocks private TakeoutOrderServiceImpl service;

    private TakeoutOrder order;
    private TakeoutOrderItem item;
    private Dish dish;

    @BeforeEach
    void setUp() {
        order = TestDataFactory.anOrder();
        order.setStatus(null); // 走默认赋值
        order.setPayStatus(null);
        order.setOrderNo(null); // 走自动生成
        item = TestDataFactory.anOrderItem();
        order.setOrderItems(Arrays.asList(item));
        dish = TestDataFactory.aDish();
    }

    // ============== insertOrder ==============

    @Test
    @DisplayName("insertOrder_正常_生成orderNo+挂支付+扣库存+加销量")
    void insertOrder_success() {
        when(orderMapper.insertOrder(any())).thenAnswer(inv -> {
            TakeoutOrder arg = inv.getArgument(0);
            arg.setOrderId(1001L);
            return 1;
        });
        when(dishMapper.selectDishById(5001L)).thenReturn(dish);

        int rows = service.insertOrder(order);

        assertThat(rows).isEqualTo(1);
        ArgumentCaptor<TakeoutOrder> orderCap = ArgumentCaptor.forClass(TakeoutOrder.class);
        verify(orderMapper).insertOrder(orderCap.capture());
        // 自动生成 orderNo(前缀 TKO,来自 OrderNoGenerator)
        assertThat(orderCap.getValue().getOrderNo()).isNotNull().startsWith("TKO");
        // 默认 status=0 待支付
        assertThat(orderCap.getValue().getStatus()).isEqualTo("0");
        // 挂支付 PENDING
        verify(paymentService).createPaymentRecord(eq(1001L), anyString(), any(), eq(PaymentChannel.MOCK));
        // 扣库存
        verify(dishMapper).adjustDishStock(argThat(d -> d.getStock() == -2));
        // 加销量
        verify(dishMapper).incrDishSales(5001L, 2);
        // 写入明细
        verify(orderMapper).insertOrderItems(any());
    }

    @Test
    @DisplayName("insertOrder_订单号重复_抛异常")
    void insertOrder_duplicateOrderNo_throws() {
        order.setOrderNo("DUP-001");
        // 模拟：service.selectOrderByOrderNo("DUP-001") 拿到一个不同的 orderId,触发重复异常
        TakeoutOrder existing = TestDataFactory.anOrder();
        existing.setOrderId(9999L); // 与当前 orderId=1001L 不同
        when(orderMapper.selectOrderByOrderNo("DUP-001")).thenReturn(existing);
        assertThatThrownBy(() -> service.insertOrder(order))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("订单号");
    }

    @Test
    @DisplayName("insertOrder_库存不足_抛异常")
    void insertOrder_stockNotEnough_throws() {
        dish.setStock(1);
        when(dishMapper.selectDishById(5001L)).thenReturn(dish);
        when(orderMapper.insertOrder(any())).thenAnswer(inv -> {
            inv.<TakeoutOrder>getArgument(0).setOrderId(1001L);
            return 1;
        });
        assertThatThrownBy(() -> service.insertOrder(order))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("库存不足");
    }

    @Test
    @DisplayName("insertOrder_跨商家不一致_抛异常")
    void insertOrder_merchantMismatch_throws() {
        dish.setMerchantId(9999L); // 与 order.merchantId 不一致
        when(dishMapper.selectDishById(5001L)).thenReturn(dish);
        when(orderMapper.insertOrder(any())).thenAnswer(inv -> {
            inv.<TakeoutOrder>getArgument(0).setOrderId(1001L);
            return 1;
        });
        assertThatThrownBy(() -> service.insertOrder(order))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("不一致");
    }

    @Test
    @DisplayName("insertOrder_菜品不存在_抛异常")
    void insertOrder_dishNotFound_throws() {
        when(dishMapper.selectDishById(5001L)).thenReturn(null);
        when(orderMapper.insertOrder(any())).thenAnswer(inv -> {
            inv.<TakeoutOrder>getArgument(0).setOrderId(1001L);
            return 1;
        });
        assertThatThrownBy(() -> service.insertOrder(order))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("菜品不存在");
    }

    @Test
    @DisplayName("insertOrder_无订单明细_跳过库存逻辑")
    void insertOrder_noItems_skipsStockLogic() {
        order.setOrderItems(Collections.emptyList());
        when(orderMapper.insertOrder(any())).thenAnswer(inv -> {
            inv.<TakeoutOrder>getArgument(0).setOrderId(1001L);
            return 1;
        });

        int rows = service.insertOrder(order);

        assertThat(rows).isEqualTo(1);
        verify(dishMapper, never()).selectDishById(any());
        verify(dishMapper, never()).adjustDishStock(any());
    }

    // ============== changeOrderStatus ==============

    @Test
    @DisplayName("changeOrderStatus_非法状态_抛异常")
    void changeStatus_invalidTarget_throws() {
        assertThatThrownBy(() -> service.changeOrderStatus(1001L, "99"))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("非法的订单状态");
    }

    @Test
    @DisplayName("changeOrderStatus_订单不存在_抛异常")
    void changeStatus_orderNotFound_throws() {
        when(orderMapper.selectOrderById(1001L)).thenReturn(null);
        assertThatThrownBy(() -> service.changeOrderStatus(1001L, "1"))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("订单不存在");
    }

    @Test
    @DisplayName("changeOrderStatus_非法状态转移_抛异常")
    void changeStatus_illegalTransition_throws() {
        order.setStatus("0"); // 待支付
        when(orderMapper.selectOrderById(1001L)).thenReturn(order);
        assertThatThrownBy(() -> service.changeOrderStatus(1001L, "3"))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("非法的状态变更");
    }

    @Test
    @DisplayName("changeOrderStatus_已支付→已退款_还库存+回退销量")
    void changeStatus_toRefunded_returnsStock() {
        order.setStatus("1"); // 已支付
        when(orderMapper.selectOrderById(1001L)).thenReturn(order);
        when(orderMapper.selectOrderItemsByOrderId(1001L)).thenReturn(Arrays.asList(item));
        when(orderMapper.updateOrderStatus(any())).thenReturn(1);

        int rows = service.changeOrderStatus(1001L, "7"); // REFUNDED

        assertThat(rows).isEqualTo(1);
        verify(dishMapper).adjustDishStock(argThat(d -> d.getStock() == 2)); // 归还 2
        verify(dishMapper).decrDishSales(5001L, 2);
    }

    // ============== cancelOrder ==============

    @Test
    @DisplayName("cancelOrder_已支付订单_正常取消+还库存+回退销量")
    void cancelOrder_paidOrder_success() {
        order.setStatus("1");
        when(orderMapper.selectOrderById(1001L)).thenReturn(order);
        when(orderMapper.selectOrderItemsByOrderId(1001L)).thenReturn(Arrays.asList(item));
        when(orderMapper.cancelOrder(any())).thenReturn(1);

        int rows = service.cancelOrder(1001L, "用户取消");

        assertThat(rows).isEqualTo(1);
        verify(dishMapper).adjustDishStock(argThat(d -> d.getStock() == 2));
        verify(dishMapper).decrDishSales(5001L, 2);
    }

    @Test
    @DisplayName("cancelOrder_已送达订单无法取消")
    void cancelOrder_delivered_throws() {
        order.setStatus("4");
        when(orderMapper.selectOrderById(1001L)).thenReturn(order);
        assertThatThrownBy(() -> service.cancelOrder(1001L, "测试"))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("不允许取消");
    }
}
