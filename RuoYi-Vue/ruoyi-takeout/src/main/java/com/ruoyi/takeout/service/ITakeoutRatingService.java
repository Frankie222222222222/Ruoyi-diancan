package com.ruoyi.takeout.service;

import java.util.List;
import com.ruoyi.takeout.domain.TakeoutRating;

/**
 * 订单评价 Service接口
 *
 * @author ruoyi
 */
public interface ITakeoutRatingService
{
    /** 查询评价列表 */
    List<TakeoutRating> selectRatingList(TakeoutRating rating);

    /** 查询单个评价 */
    TakeoutRating selectRatingById(Long ratingId);

    /** 通过订单ID查询评价 */
    TakeoutRating selectRatingByOrderId(Long orderId);

    /** 用户提交评价 */
    int submitRating(TakeoutRating rating);

    /** 商家回复评价 */
    int replyRating(Long ratingId, String reply);

    /** 删除评价(逻辑) */
    int deleteRatingByIds(Long[] ratingIds);
}
