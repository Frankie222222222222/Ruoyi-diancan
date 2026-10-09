package com.ruoyi.takeout.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.takeout.domain.TakeoutRating;
import com.ruoyi.takeout.mapper.TakeoutRatingMapper;
import com.ruoyi.takeout.service.ITakeoutRatingService;

/**
 * 订单评价 Service 业务实现
 *
 * @author ruoyi
 */
@Service
public class TakeoutRatingServiceImpl implements ITakeoutRatingService
{
    private static final int ERR_RATING_NOT_FOUND  = 9001;
    private static final int ERR_ALREADY_RATED     = 9002;
    private static final int ERR_INVALID_SCORE     = 9003;

    @Autowired
    private TakeoutRatingMapper ratingMapper;

    @Override
    public List<TakeoutRating> selectRatingList(TakeoutRating rating)
    {
        return ratingMapper.selectRatingList(rating);
    }

    @Override
    public TakeoutRating selectRatingById(Long ratingId)
    {
        return ratingMapper.selectRatingById(ratingId);
    }

    @Override
    public TakeoutRating selectRatingByOrderId(Long orderId)
    {
        return ratingMapper.selectRatingByOrderId(orderId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int submitRating(TakeoutRating rating)
    {
        // 校验评分
        if (rating.getMerchantScore() != null && (rating.getMerchantScore() < 1 || rating.getMerchantScore() > 5))
        {
            throw new ServiceException("[ERR_" + ERR_INVALID_SCORE + "] 商家评分必须在1-5之间", ERR_INVALID_SCORE);
        }
        if (rating.getRiderScore() != null && (rating.getRiderScore() < 1 || rating.getRiderScore() > 5))
        {
            throw new ServiceException("[ERR_" + ERR_INVALID_SCORE + "] 骑手评分必须在1-5之间", ERR_INVALID_SCORE);
        }
        // 校验重复评价
        TakeoutRating exist = ratingMapper.selectRatingByOrderId(rating.getOrderId());
        if (exist != null)
        {
            throw new ServiceException("[ERR_" + ERR_ALREADY_RATED + "] 该订单已评价过", ERR_ALREADY_RATED);
        }
        if (rating.getStatus() == null)
        {
            rating.setStatus("0");
        }
        rating.setCreateBy(SecurityUtils.getUsername());
        return ratingMapper.insertRating(rating);
    }

    @Override
    public int replyRating(Long ratingId, String reply)
    {
        TakeoutRating r = ratingMapper.selectRatingById(ratingId);
        if (r == null)
        {
            throw new ServiceException("[ERR_" + ERR_RATING_NOT_FOUND + "] 评价不存在", ERR_RATING_NOT_FOUND);
        }
        TakeoutRating upd = new TakeoutRating();
        upd.setRatingId(ratingId);
        upd.setReply(reply);
        upd.setReplyBy(SecurityUtils.getUsername());
        return ratingMapper.replyRating(upd);
    }

    @Override
    public int deleteRatingByIds(Long[] ratingIds)
    {
        return ratingMapper.deleteRatingByIds(ratingIds);
    }
}
