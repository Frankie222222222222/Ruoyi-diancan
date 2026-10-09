package com.ruoyi.takeout.service.impl;

import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.takeout.domain.TakeoutComplaint;
import com.ruoyi.takeout.mapper.TakeoutComplaintMapper;
import com.ruoyi.takeout.service.ITakeoutOrderService;
import com.ruoyi.takeout.support.BaseServiceTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * TakeoutComplaintServiceImpl 单元测试
 *
 * <p>覆盖:
 *  <ol>
 *      <li>submitComplaint — 必填原因/默认 status=0</li>
 *      <li>processComplaint — 已完成工单不能再处理/状态非法/正常推进 status=2(已退款) → 同步订单 status=7</li>
 *  </ol>
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("投诉工单服务 - 处理流程")
class TakeoutComplaintServiceImplTest extends BaseServiceTest
{
    @Mock private TakeoutComplaintMapper complaintMapper;
    @Mock private ITakeoutOrderService orderService;

    @InjectMocks private TakeoutComplaintServiceImpl service;

    private TakeoutComplaint complaint;

    @BeforeEach
    void setUp() {
        complaint = new TakeoutComplaint();
        complaint.setComplaintId(6001L);
        complaint.setOrderId(1001L);
        complaint.setReason("食物不新鲜");
        complaint.setType("0");
        complaint.setStatus("0");
    }

    @Test
    @DisplayName("submitComplaint_原因缺失_抛异常")
    void submit_noReason_throws() {
        TakeoutComplaint c = new TakeoutComplaint();
        assertThatThrownBy(() -> service.submitComplaint(c))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("投诉原因不能为空");
    }

    @Test
    @DisplayName("submitComplaint_正常_默认 status=0 待处理")
    void submit_success() {
        when(complaintMapper.insertComplaint(any())).thenReturn(1);
        int rows = service.submitComplaint(complaint);
        assertThat(rows).isEqualTo(1);
        ArgumentCaptor<TakeoutComplaint> cap = ArgumentCaptor.forClass(TakeoutComplaint.class);
        verify(complaintMapper).insertComplaint(cap.capture());
        assertThat(cap.getValue().getStatus()).isEqualTo("0");
    }

    @Test
    @DisplayName("processComplaint_工单不存在_抛异常")
    void process_notFound_throws() {
        when(complaintMapper.selectComplaintById(6001L)).thenReturn(null);
        assertThatThrownBy(() -> service.processComplaint(6001L, "1", null, "备注"))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("工单不存在");
    }

    @Test
    @DisplayName("processComplaint_工单已退款无法再次处理")
    void process_alreadyRefunded_throws() {
        complaint.setStatus("2");
        when(complaintMapper.selectComplaintById(6001L)).thenReturn(complaint);
        assertThatThrownBy(() -> service.processComplaint(6001L, "1", null, "备注"))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("已处理完成");
    }

    @Test
    @DisplayName("processComplaint_非法的处理状态_抛异常")
    void process_invalidStatus_throws() {
        when(complaintMapper.selectComplaintById(6001L)).thenReturn(complaint);
        assertThatThrownBy(() -> service.processComplaint(6001L, "9", null, "备注"))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("非法的处理状态");
    }

    @Test
    @DisplayName("processComplaint_正常退款_写approvedAmount+refundTime+refundFlowNo+同步订单status=7")
    void process_refund_success() {
        when(complaintMapper.selectComplaintById(6001L)).thenReturn(complaint);
        when(complaintMapper.processComplaint(any())).thenReturn(1);

        int rows = service.processComplaint(6001L, "2", new BigDecimal("30.00"), "客户证据充分");

        assertThat(rows).isEqualTo(1);
        ArgumentCaptor<TakeoutComplaint> cap = ArgumentCaptor.forClass(TakeoutComplaint.class);
        verify(complaintMapper).processComplaint(cap.capture());
        assertThat(cap.getValue().getStatus()).isEqualTo("2");
        assertThat(cap.getValue().getApprovedAmount()).isEqualByComparingTo("30.00");
        assertThat(cap.getValue().getRefundTime()).isNotNull();
        assertThat(cap.getValue().getRefundFlowNo()).startsWith("RF");
        verify(orderService).changeOrderStatus(1001L, "7"); // REFUNDED
    }
}
