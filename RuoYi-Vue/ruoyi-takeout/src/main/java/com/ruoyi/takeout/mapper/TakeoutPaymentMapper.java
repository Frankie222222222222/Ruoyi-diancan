package com.ruoyi.takeout.mapper;

import java.util.List;
import com.ruoyi.takeout.domain.TakeoutPayment;

/**
 * 支付流水 Mapper
 *
 * @author ruoyi
 */
public interface TakeoutPaymentMapper
{
    /** 列表(简单分页,内部用 PageHelper) */
    List<TakeoutPayment> selectPaymentList(TakeoutPayment query);

    /** 按主键 */
    TakeoutPayment selectPaymentById(Long id);

    /** 按商户订单号 */
    TakeoutPayment selectByOutTradeNo(String outTradeNo);

    /** 新增 */
    int insertPayment(TakeoutPayment payment);

    /** 更新 */
    int updatePayment(TakeoutPayment payment);
}
