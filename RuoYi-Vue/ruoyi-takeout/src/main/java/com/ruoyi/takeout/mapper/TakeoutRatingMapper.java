package com.ruoyi.takeout.mapper;

import java.util.List;
import com.ruoyi.takeout.domain.TakeoutRating;

/**
 * 订单评价 Mapper
 *
 * @author ruoyi
 */
public interface TakeoutRatingMapper
{
    /** 查询评价列表 */
    List<TakeoutRating> selectRatingList(TakeoutRating rating);

    /** 查询单个评价 */
    TakeoutRating selectRatingById(Long ratingId);

    /** 通过订单ID查询评价 */
    TakeoutRating selectRatingByOrderId(Long orderId);

    /** 新增评价 */
    int insertRating(TakeoutRating rating);

    /** 修改评价 */
    int updateRating(TakeoutRating rating);

    /** 删除评价(逻辑) */
    int deleteRatingByIds(Long[] ratingIds);

    /** 商家回复 */
    int replyRating(TakeoutRating rating);

    /** 按商家查询平均分 */
    Double selectAvgMerchantScoreByMerchantId(Long merchantId);

    /** 按骑手查询平均分 */
    Double selectAvgRiderScoreByRiderId(Long riderId);
}
