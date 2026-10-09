package com.ruoyi.takeout.service;

import java.util.List;
import com.ruoyi.takeout.domain.TakeoutDispatch;

/**
 * 派单记录 Service接口
 *
 * @author ruoyi
 */
public interface ITakeoutDispatchService
{
    /** 查询派单列表 */
    List<TakeoutDispatch> selectDispatchList(TakeoutDispatch dispatch);

    /** 查询单个派单 */
    TakeoutDispatch selectDispatchById(Long dispatchId);

    /** 订单创建时自动创建派单记录 */
    int createDispatchForOrder(Long orderId, Long riderId, String dispatchType);

    /** 骑手抢单 */
    int claimOrder(Long orderId, Long riderId);

    /** 骑手接单 */
    int acceptDispatch(Long dispatchId, Long riderId);

    /** 骑手取餐出发 */
    int pickup(Long dispatchId);

    /** 骑手完成配送 */
    int completeDispatch(Long dispatchId);

    /** 骑手取消配送 */
    int cancelDispatch(Long dispatchId, String reason);

    /** 后台改派：把进行中的派单转给新骑手（保持原状态，重置关键时间） */
    int reassignDispatch(Long dispatchId, Long newRiderId, String reason);

    /** 删除派单(逻辑) */
    int deleteDispatchByIds(Long[] dispatchIds);

    /** 查询订单的最新有效派单 */
    TakeoutDispatch selectActiveDispatchByOrderId(Long orderId);
}
