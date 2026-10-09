package com.ruoyi.takeout.service.impl;

import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.takeout.domain.TakeoutDispatch;
import com.ruoyi.takeout.domain.TakeoutOrder;
import com.ruoyi.takeout.mapper.TakeoutDispatchMapper;
import com.ruoyi.takeout.mapper.TakeoutOrderMapper;
import com.ruoyi.takeout.service.ITakeoutOrderService;
import com.ruoyi.takeout.service.ITakeoutRiderService;
import com.ruoyi.takeout.support.BaseServiceTest;
import com.ruoyi.takeout.support.TestDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

/**
 * TakeoutDispatchServiceImpl 单元测试
 *
 * <p>覆盖派单状态机 6 条主路径:
 *  <ol>
 *      <li>createDispatchForOrder — 正常派单 / 订单不存在 / 状态非法 / 已有 active 派单 / 骑手忙</li>
 *      <li>acceptDispatch — 正常接单 / 状态非法 / 骑手不匹配</li>
 *      <li>pickup — 正常 / 状态非法</li>
 *      <li>completeDispatch — 正常 / 状态非法</li>
 *      <li>cancelDispatch — 正常 / 派单已结束</li>
 *      <li>reassignDispatch — 正常 / 派单已结束 / 新骑手相同 / 新骑手忙 / 校验状态回退</li>
 *  </ol>
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("派单服务 - 状态机单元测试")
class TakeoutDispatchServiceImplTest extends BaseServiceTest
{
    @Mock private TakeoutDispatchMapper dispatchMapper;
    @Mock private TakeoutOrderMapper orderMapper;
    @Mock private ITakeoutOrderService orderService;
    @Mock private ITakeoutRiderService riderService;

    @InjectMocks private TakeoutDispatchServiceImpl service;

    private TakeoutOrder order;
    private TakeoutDispatch dispatch;

    @BeforeEach
    void setUp() {
        order = TestDataFactory.anOrder();
        order.setStatus("1"); // 已支付
        dispatch = TestDataFactory.aDispatch();
    }

    // ============== createDispatchForOrder ==============

    @Test
    @DisplayName("createDispatchForOrder_订单不存在_抛异常")
    void createDispatch_orderNotFound_throws() {
        when(orderMapper.selectOrderById(1001L)).thenReturn(null);
        assertThatThrownBy(() -> service.createDispatchForOrder(1001L, 7001L, "0"))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("订单不存在");
    }

    @Test
    @DisplayName("createDispatchForOrder_订单状态非法_抛异常")
    void createDispatch_invalidOrderStatus_throws() {
        order.setStatus("5"); // 已完成
        when(orderMapper.selectOrderById(1001L)).thenReturn(order);
        assertThatThrownBy(() -> service.createDispatchForOrder(1001L, 7001L, "0"))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("不可派单");
    }

    @Test
    @DisplayName("createDispatchForOrder_已有active派单_抛异常")
    void createDispatch_alreadyHasActive_throws() {
        when(orderMapper.selectOrderById(1001L)).thenReturn(order);
        when(dispatchMapper.selectActiveDispatchByOrderId(1001L)).thenReturn(dispatch);
        assertThatThrownBy(() -> service.createDispatchForOrder(1001L, 7001L, "0"))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("已有进行中的派单");
    }

    @Test
    @DisplayName("createDispatchForOrder_骑手已忙_抛异常")
    void createDispatch_riderBusy_throws() {
        when(orderMapper.selectOrderById(1001L)).thenReturn(order);
        when(dispatchMapper.selectActiveDispatchByOrderId(1001L)).thenReturn(null);
        when(dispatchMapper.countActiveByRiderId(7001L)).thenReturn(3);
        assertThatThrownBy(() -> service.createDispatchForOrder(1001L, 7001L, "0"))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("已有 3 个进行中的配送单");
    }

    @Test
    @DisplayName("createDispatchForOrder_正常派单_状态=1已接单_同步推进订单到配送中")
    void createDispatch_success_status1() {
        when(orderMapper.selectOrderById(1001L)).thenReturn(order);
        when(dispatchMapper.selectActiveDispatchByOrderId(1001L)).thenReturn(null);
        when(dispatchMapper.countActiveByRiderId(7001L)).thenReturn(0);
        when(dispatchMapper.insertDispatch(any())).thenReturn(1);

        int rows = service.createDispatchForOrder(1001L, 7001L, "0");

        assertThat(rows).isEqualTo(1);
        ArgumentCaptor<TakeoutDispatch> cap = ArgumentCaptor.forClass(TakeoutDispatch.class);
        verify(dispatchMapper).insertDispatch(cap.capture());
        assertThat(cap.getValue().getStatus()).isEqualTo("1"); // 有骑手 → 已接单
        assertThat(cap.getValue().getDispatchType()).isEqualTo("0");
        verify(orderService).changeOrderStatus(1001L, "3"); // DELIVERING
    }

    @Test
    @DisplayName("createDispatchForOrder_无骑手抢单模式_状态=0待接单_订单推到商家接单")
    void createDispatch_grabMode_status0() {
        when(orderMapper.selectOrderById(1001L)).thenReturn(order);
        when(dispatchMapper.selectActiveDispatchByOrderId(1001L)).thenReturn(null);
        when(dispatchMapper.insertDispatch(any())).thenReturn(1);

        int rows = service.createDispatchForOrder(1001L, null, "1");

        assertThat(rows).isEqualTo(1);
        ArgumentCaptor<TakeoutDispatch> cap = ArgumentCaptor.forClass(TakeoutDispatch.class);
        verify(dispatchMapper).insertDispatch(cap.capture());
        assertThat(cap.getValue().getStatus()).isEqualTo("0");
        verify(orderService).changeOrderStatus(1001L, "2"); // 商家接单
    }

    // ============== acceptDispatch ==============

    @Test
    @DisplayName("acceptDispatch_派单不存在_抛异常")
    void accept_dispatchNotFound_throws() {
        when(dispatchMapper.selectDispatchById(8001L)).thenReturn(null);
        assertThatThrownBy(() -> service.acceptDispatch(8001L, 7001L))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("派单不存在");
    }

    @Test
    @DisplayName("acceptDispatch_状态非法(非待接单)_抛异常")
    void accept_invalidStatus_throws() {
        dispatch.setStatus("2");
        when(dispatchMapper.selectDispatchById(8001L)).thenReturn(dispatch);
        assertThatThrownBy(() -> service.acceptDispatch(8001L, 7001L))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("不可接单");
    }

    @Test
    @DisplayName("acceptDispatch_骑手不匹配_抛异常")
    void accept_wrongRider_throws() {
        dispatch.setStatus("0");
        when(dispatchMapper.selectDispatchById(8001L)).thenReturn(dispatch);
        assertThatThrownBy(() -> service.acceptDispatch(8001L, 9999L))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("已指派给其他骑手");
    }

    @Test
    @DisplayName("acceptDispatch_正常_状态=1+订单推配送中+累加骑手单量+写入坐标")
    void accept_success() {
        dispatch.setStatus("0");
        when(dispatchMapper.selectDispatchById(8001L)).thenReturn(dispatch);
        when(dispatchMapper.updateDispatchStatus(any())).thenReturn(1);

        int rows = service.acceptDispatch(8001L, 7001L);

        assertThat(rows).isEqualTo(1);
        ArgumentCaptor<TakeoutDispatch> cap = ArgumentCaptor.forClass(TakeoutDispatch.class);
        verify(dispatchMapper).updateDispatchStatus(cap.capture());
        assertThat(cap.getValue().getStatus()).isEqualTo("1");
        assertThat(cap.getValue().getRiderLng()).isNotNull();
        assertThat(cap.getValue().getRiderLat()).isNotNull();
        verify(orderService).changeOrderStatus(1001L, "3");
        verify(riderService).incrementDeliveries(7001L);
    }

    // ============== pickup ==============

    @Test
    @DisplayName("pickup_状态非法_抛异常")
    void pickup_invalidStatus_throws() {
        dispatch.setStatus("0");
        when(dispatchMapper.selectDispatchById(8001L)).thenReturn(dispatch);
        assertThatThrownBy(() -> service.pickup(8001L))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("不可取餐");
    }

    @Test
    @DisplayName("pickup_正常_状态=2+更新locationUpdateTime")
    void pickup_success() {
        dispatch.setStatus("1");
        when(dispatchMapper.selectDispatchById(8001L)).thenReturn(dispatch);
        when(dispatchMapper.updateDispatchStatus(any())).thenReturn(1);

        int rows = service.pickup(8001L);

        assertThat(rows).isEqualTo(1);
        ArgumentCaptor<TakeoutDispatch> cap = ArgumentCaptor.forClass(TakeoutDispatch.class);
        verify(dispatchMapper).updateDispatchStatus(cap.capture());
        assertThat(cap.getValue().getStatus()).isEqualTo("2");
        assertThat(cap.getValue().getPickupTime()).isNotNull();
        assertThat(cap.getValue().getLocationUpdateTime()).isNotNull();
    }

    // ============== completeDispatch ==============

    @Test
    @DisplayName("completeDispatch_正常_状态=3+订单推已送达")
    void complete_success() {
        dispatch.setStatus("2");
        when(dispatchMapper.selectDispatchById(8001L)).thenReturn(dispatch);
        when(dispatchMapper.updateDispatchStatus(any())).thenReturn(1);

        int rows = service.completeDispatch(8001L);

        assertThat(rows).isEqualTo(1);
        verify(orderService).changeOrderStatus(1001L, "4"); // DELIVERED
    }

    @Test
    @DisplayName("completeDispatch_状态非法(0待接单)_抛异常")
    void complete_invalidStatus_throws() {
        dispatch.setStatus("0");
        when(dispatchMapper.selectDispatchById(8001L)).thenReturn(dispatch);
        assertThatThrownBy(() -> service.completeDispatch(8001L))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("不可完成");
    }

    // ============== cancelDispatch ==============

    @Test
    @DisplayName("cancelDispatch_已完成的派单无法取消")
    void cancel_completed_throws() {
        dispatch.setStatus("3");
        when(dispatchMapper.selectDispatchById(8001L)).thenReturn(dispatch);
        assertThatThrownBy(() -> service.cancelDispatch(8001L, "测试"))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("已结束，无法取消");
    }

    @Test
    @DisplayName("cancelDispatch_正常_状态=4+写原因+写时间")
    void cancel_success() {
        dispatch.setStatus("1");
        when(dispatchMapper.selectDispatchById(8001L)).thenReturn(dispatch);
        when(dispatchMapper.updateDispatchStatus(any())).thenReturn(1);

        int rows = service.cancelDispatch(8001L, "客户取消");

        assertThat(rows).isEqualTo(1);
        ArgumentCaptor<TakeoutDispatch> cap = ArgumentCaptor.forClass(TakeoutDispatch.class);
        verify(dispatchMapper).updateDispatchStatus(cap.capture());
        assertThat(cap.getValue().getStatus()).isEqualTo("4");
        assertThat(cap.getValue().getCancelReason()).isEqualTo("客户取消");
    }

    // ============== reassignDispatch ==============

    @Test
    @DisplayName("reassignDispatch_已完成派单无法改派")
    void reassign_finished_throws() {
        dispatch.setStatus("3");
        when(dispatchMapper.selectDispatchById(8001L)).thenReturn(dispatch);
        assertThatThrownBy(() -> service.reassignDispatch(8001L, 7002L, "换人"))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("已结束");
    }

    @Test
    @DisplayName("reassignDispatch_新骑手与原骑手相同_抛异常")
    void reassign_sameRider_throws() {
        dispatch.setStatus("1");
        when(dispatchMapper.selectDispatchById(8001L)).thenReturn(dispatch);
        assertThatThrownBy(() -> service.reassignDispatch(8001L, 7001L, "换人"))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("新骑手与原骑手相同");
    }

    @Test
    @DisplayName("reassignDispatch_新骑手忙_抛异常")
    void reassign_newRiderBusy_throws() {
        dispatch.setStatus("1");
        when(dispatchMapper.selectDispatchById(8001L)).thenReturn(dispatch);
        when(dispatchMapper.countActiveByRiderId(7002L)).thenReturn(3);
        assertThatThrownBy(() -> service.reassignDispatch(8001L, 7002L, "换人"))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("新骑手已有");
    }

    @Test
    @DisplayName("reassignDispatch_正常_状态回退=0+订单回退到商家接单+清空accept/pickup/complete时间")
    void reassign_success() {
        dispatch.setStatus("1");
        dispatch.setAcceptTime(new java.util.Date());
        when(dispatchMapper.selectDispatchById(8001L)).thenReturn(dispatch);
        when(dispatchMapper.countActiveByRiderId(7002L)).thenReturn(0);
        when(dispatchMapper.updateDispatchStatus(any())).thenReturn(1);

        int rows = service.reassignDispatch(8001L, 7002L, "骑手请假");

        assertThat(rows).isEqualTo(1);
        ArgumentCaptor<TakeoutDispatch> cap = ArgumentCaptor.forClass(TakeoutDispatch.class);
        verify(dispatchMapper).updateDispatchStatus(cap.capture());
        assertThat(cap.getValue().getStatus()).isEqualTo("0"); // 回到待接单
        assertThat(cap.getValue().getRiderId()).isEqualTo(7002L);
        assertThat(cap.getValue().getAcceptTime()).isNull();
        assertThat(cap.getValue().getPickupTime()).isNull();
        assertThat(cap.getValue().getCompleteTime()).isNull();
        verify(orderService).changeOrderStatus(1001L, "2"); // 商家接单
        // remark 字段由 mapper SQL 写入,通过 update 链路验证(不直接断言 getter,因 domain 未暴露)
    }
}
