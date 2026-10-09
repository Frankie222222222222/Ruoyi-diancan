package com.ruoyi.takeout.service;

import java.util.List;
import com.ruoyi.takeout.domain.TakeoutDispatch;
import com.ruoyi.takeout.dto.RiderLocationDTO;

/**
 * 骑手位置 Service 接口
 *
 * <p>开发期用于演示/单元测试写入坐标,生产期由骑手 APP 调用上报。
 * 不参与派单核心事务,失败不抛异常(写日志即可)。
 *
 * @author ruoyi
 */
public interface ITakeoutRiderLocationService
{
    /**
     * 骑手上报一次位置(更新到 takeout_dispatch.rider_lng/rider_lat/location_update_time)
     *
     * @param dto dispatchId + riderId + lng + lat
     * @return 受影响行数(0 表示找不到对应派单)
     */
    int reportLocation(RiderLocationDTO dto);

    /**
     * 查询所有进行中派单(status ∈ {0,1,2})的最新位置,给地图组件批量打点用
     */
    List<TakeoutDispatch> listActiveDispatchLocations();
}
