package com.ruoyi.takeout.mapper;

import java.util.List;
import com.ruoyi.takeout.domain.TakeoutDispatch;

/**
 * 派单记录 Mapper
 *
 * @author ruoyi
 */
public interface TakeoutDispatchMapper
{
    /** 查询派单列表 */
    List<TakeoutDispatch> selectDispatchList(TakeoutDispatch dispatch);

    /** 查询单个派单 */
    TakeoutDispatch selectDispatchById(Long dispatchId);

    /** 通过订单ID查询最新有效派单 */
    TakeoutDispatch selectActiveDispatchByOrderId(Long orderId);

    /** 新增派单 */
    int insertDispatch(TakeoutDispatch dispatch);

    /** 修改派单 */
    int updateDispatch(TakeoutDispatch dispatch);

    /** 删除派单(逻辑) */
    int deleteDispatchByIds(Long[] dispatchIds);

    /** 更新派单状态 */
    int updateDispatchStatus(TakeoutDispatch dispatch);

    /** 骑手待完成的配送单数量 */
    int countActiveByRiderId(Long riderId);

    /** 查询骑手的配送记录 */
    List<TakeoutDispatch> selectByRiderId(Long riderId);
}
