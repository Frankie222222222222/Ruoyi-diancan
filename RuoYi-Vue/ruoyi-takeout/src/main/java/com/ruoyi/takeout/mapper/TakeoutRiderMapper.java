package com.ruoyi.takeout.mapper;

import java.util.List;
import com.ruoyi.takeout.domain.TakeoutRider;

/**
 * 骑手 Mapper
 *
 * @author ruoyi
 */
public interface TakeoutRiderMapper
{
    /** 查询骑手列表 */
    List<TakeoutRider> selectRiderList(TakeoutRider rider);

    /** 查询单个骑手 */
    TakeoutRider selectRiderById(Long riderId);

    /** 通过手机号查询 */
    TakeoutRider selectRiderByPhone(String phone);

    /** 新增骑手 */
    int insertRider(TakeoutRider rider);

    /** 修改骑手 */
    int updateRider(TakeoutRider rider);

    /** 删除骑手(逻辑) */
    int deleteRiderByIds(Long[] riderIds);

    /** 修改骑手状态 */
    int updateRiderStatus(TakeoutRider rider);

    /** 累加配送单量 */
    int incrementDeliveries(Long riderId);

    /** 查询在线骑手(接单中) */
    List<TakeoutRider> selectAvailableRiders(String city);
}
