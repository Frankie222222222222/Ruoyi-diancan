package com.ruoyi.takeout.service;

import java.util.List;
import com.ruoyi.takeout.domain.TakeoutRider;

/**
 * 骑手 Service接口
 *
 * @author ruoyi
 */
public interface ITakeoutRiderService
{
    /** 查询骑手列表 */
    List<TakeoutRider> selectRiderList(TakeoutRider rider);

    /** 查询单个骑手 */
    TakeoutRider selectRiderById(Long riderId);

    /** 新增骑手 */
    int insertRider(TakeoutRider rider);

    /** 修改骑手 */
    int updateRider(TakeoutRider rider);

    /** 删除骑手(逻辑) */
    int deleteRiderByIds(Long[] riderIds);

    /** 修改骑手接单状态 */
    int changeRiderStatus(Long riderId, String status);

    /** 查询在线骑手 */
    List<TakeoutRider> selectAvailableRiders(String city);

    /** 骑手接单后累加配送单量 */
    void incrementDeliveries(Long riderId);
}
