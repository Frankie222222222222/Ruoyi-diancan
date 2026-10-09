package com.ruoyi.takeout.service.impl;

import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.takeout.domain.TakeoutRider;
import com.ruoyi.takeout.mapper.TakeoutRiderMapper;
import com.ruoyi.takeout.support.BaseServiceTest;
import com.ruoyi.takeout.support.TestDataFactory;
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
import static org.mockito.Mockito.*;

/**
 * TakeoutRiderServiceImpl 单元测试
 *
 * <p>覆盖:
 *  <ol>
 *      <li>insertRider — 必填校验/手机号重复/默认值</li>
 *      <li>changeRiderStatus — 状态机 0/1/2/非法</li>
 *      <li>incrementDeliveries — 转发到 mapper</li>
 *      <li>selectRiderById — 不存在抛异常</li>
 *  </ol>
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("骑手服务 - 状态切换+累加单量")
class TakeoutRiderServiceImplTest extends BaseServiceTest
{
    @Mock private TakeoutRiderMapper riderMapper;

    @InjectMocks private TakeoutRiderServiceImpl service;

    @Test
    @DisplayName("insertRider_姓名缺失_抛异常")
    void insert_nameRequired_throws() {
        TakeoutRider r = new TakeoutRider();
        r.setName(null);
        r.setPhone("13800138000");
        assertThatThrownBy(() -> service.insertRider(r))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("姓名");
    }

    @Test
    @DisplayName("insertRider_手机号已存在_抛异常")
    void insert_phoneDuplicate_throws() {
        TakeoutRider r = TestDataFactory.aRider();
        when(riderMapper.selectRiderByPhone("13900139000")).thenReturn(r);
        assertThatThrownBy(() -> service.insertRider(r))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("手机号已存在");
    }

    @Test
    @DisplayName("insertRider_正常_默认值 status=1 休息中/rating=5.0/joinTime=now")
    void insert_success_defaults() {
        TakeoutRider r = new TakeoutRider();
        r.setName("张三");
        r.setPhone("13888888888");
        when(riderMapper.selectRiderByPhone("13888888888")).thenReturn(null);
        when(riderMapper.insertRider(any())).thenReturn(1);

        int rows = service.insertRider(r);

        assertThat(rows).isEqualTo(1);
        ArgumentCaptor<TakeoutRider> cap = ArgumentCaptor.forClass(TakeoutRider.class);
        verify(riderMapper).insertRider(cap.capture());
        assertThat(cap.getValue().getStatus()).isEqualTo("1");
        assertThat(cap.getValue().getRating()).isEqualByComparingTo("5.00");
        assertThat(cap.getValue().getJoinTime()).isNotNull();
    }

    @Test
    @DisplayName("changeRiderStatus_非法状态_抛异常")
    void changeStatus_invalid_throws() {
        assertThatThrownBy(() -> service.changeRiderStatus(1L, "9"))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("非法的骑手状态");
    }

    @Test
    @DisplayName("changeRiderStatus_正常_0接单中/1休息/2离线")
    void changeStatus_success() {
        when(riderMapper.updateRiderStatus(any())).thenReturn(1);
        service.changeRiderStatus(1L, "0");
        service.changeRiderStatus(1L, "1");
        service.changeRiderStatus(1L, "2");
        verify(riderMapper, times(3)).updateRiderStatus(any());
    }

    @Test
    @DisplayName("incrementDeliveries_转发到mapper")
    void incrementDeliveries_delegates() {
        service.incrementDeliveries(7001L);
        verify(riderMapper).incrementDeliveries(7001L);
    }

    @Test
    @DisplayName("selectRiderById_不存在_抛异常")
    void getById_notFound_throws() {
        when(riderMapper.selectRiderById(9999L)).thenReturn(null);
        assertThatThrownBy(() -> service.selectRiderById(9999L))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("骑手不存在");
    }
}
