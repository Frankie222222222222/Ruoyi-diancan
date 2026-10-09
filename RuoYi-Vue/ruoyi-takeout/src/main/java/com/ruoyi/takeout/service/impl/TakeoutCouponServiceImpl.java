package com.ruoyi.takeout.service.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.takeout.domain.TakeoutCoupon;
import com.ruoyi.takeout.domain.TakeoutCouponUser;
import com.ruoyi.takeout.mapper.TakeoutCouponMapper;
import com.ruoyi.takeout.service.ITakeoutCouponService;

/**
 * 优惠券 Service 业务实现
 *
 * @author ruoyi
 */
@Service
public class TakeoutCouponServiceImpl implements ITakeoutCouponService
{
    private static final Logger log = LoggerFactory.getLogger(TakeoutCouponServiceImpl.class);

    private static final int ERR_COUPON_NOT_FOUND    = 10001;
    private static final int ERR_COUPON_OFFLINE      = 10002;
    private static final int ERR_COUPON_SOLD_OUT     = 10003;
    private static final int ERR_LIMIT_EXCEEDED      = 10004;
    private static final int ERR_USER_COUPON_NOT_FOUND = 10005;
    private static final int ERR_COUPON_USED         = 10006;
    private static final int ERR_COUPON_EXPIRED      = 10007;

    @Autowired
    private TakeoutCouponMapper couponMapper;

    @Override
    public List<TakeoutCoupon> selectCouponList(TakeoutCoupon coupon)
    {
        return couponMapper.selectCouponList(coupon);
    }

    @Override
    public TakeoutCoupon selectCouponById(Long couponId)
    {
        return couponMapper.selectCouponById(couponId);
    }

    @Override
    public int insertCoupon(TakeoutCoupon coupon)
    {
        if (StringUtils.isEmpty(coupon.getStatus()))
        {
            coupon.setStatus("0");
        }
        if (coupon.getRemainCount() == null)
        {
            coupon.setRemainCount(coupon.getTotalCount());
        }
        if (coupon.getPerUserLimit() == null)
        {
            coupon.setPerUserLimit(1);
        }
        if (StringUtils.isEmpty(coupon.getColor()))
        {
            coupon.setColor("red");
        }
        coupon.setCreateBy(SecurityUtils.getUsername());
        return couponMapper.insertCoupon(coupon);
    }

    @Override
    public int updateCoupon(TakeoutCoupon coupon)
    {
        coupon.setUpdateBy(SecurityUtils.getUsername());
        return couponMapper.updateCoupon(coupon);
    }

    @Override
    public int deleteCouponByIds(Long[] couponIds)
    {
        return couponMapper.deleteCouponByIds(couponIds);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int receiveCoupon(Long userId, Long couponId)
    {
        TakeoutCoupon coupon = couponMapper.selectCouponById(couponId);
        if (coupon == null)
        {
            throw new ServiceException("[ERR_" + ERR_COUPON_NOT_FOUND + "] 优惠券不存在", ERR_COUPON_NOT_FOUND);
        }
        if (!"0".equals(coupon.getStatus()))
        {
            throw new ServiceException("[ERR_" + ERR_COUPON_OFFLINE + "] 优惠券已下架或过期", ERR_COUPON_OFFLINE);
        }
        if (coupon.getRemainCount() == null || coupon.getRemainCount() <= 0)
        {
            throw new ServiceException("[ERR_" + ERR_COUPON_SOLD_OUT + "] 优惠券已领完", ERR_COUPON_SOLD_OUT);
        }
        // 校验每人限领数量
        int alreadyHave = couponMapper.countUserCoupons(userId, couponId);
        if (alreadyHave >= coupon.getPerUserLimit())
        {
            throw new ServiceException("[ERR_" + ERR_LIMIT_EXCEEDED + "] 已达到每人限领数量: " + coupon.getPerUserLimit(), ERR_LIMIT_EXCEEDED);
        }
        // 扣减剩余
        int dec = couponMapper.decrementRemainCount(couponId);
        if (dec == 0)
        {
            throw new ServiceException("[ERR_" + ERR_COUPON_SOLD_OUT + "] 优惠券已被领完", ERR_COUPON_SOLD_OUT);
        }
        // 写入用户券
        TakeoutCouponUser cu = new TakeoutCouponUser();
        cu.setUserId(userId);
        cu.setCouponId(couponId);
        cu.setStatus("0");
        cu.setReceiveTime(DateUtils.getNowDate());
        cu.setExpireTime(coupon.getEndTime());
        cu.setCreateBy(SecurityUtils.getUsername());
        return couponMapper.insertUserCoupon(cu);
    }

    @Override
    public List<TakeoutCouponUser> selectUserCouponList(Long userId, String status)
    {
        TakeoutCouponUser query = new TakeoutCouponUser();
        query.setUserId(userId);
        query.setStatus(status);
        return couponMapper.selectUserCouponList(query);
    }

    @Override
    public TakeoutCouponUser findBestCouponForOrder(Long userId, Long merchantId, BigDecimal orderAmount)
    {
        if (orderAmount == null || orderAmount.signum() <= 0)
        {
            return null;
        }
        // 查询该用户所有未使用且未过期的券
        List<TakeoutCouponUser> all = selectUserCouponList(userId, "0");
        if (all == null || all.isEmpty())
        {
            return null;
        }
        // 计算每张券的优惠金额，选最优
        BigDecimal bestDiscount = BigDecimal.ZERO;
        TakeoutCouponUser best = null;
        for (TakeoutCouponUser cu : all)
        {
            if (cu.getExpireTime() != null && cu.getExpireTime().before(DateUtils.getNowDate()))
            {
                continue;
            }
            TakeoutCoupon c = couponMapper.selectCouponById(cu.getCouponId());
            if (c == null || !"0".equals(c.getStatus()))
            {
                continue;
            }
            // 平台券或本商家券
            if (c.getMerchantId() != null && !c.getMerchantId().equals(merchantId))
            {
                continue;
            }
            // 门槛校验
            if (c.getThresholdAmount() != null && orderAmount.compareTo(c.getThresholdAmount()) < 0)
            {
                continue;
            }
            BigDecimal discount = calcDiscount(c, orderAmount);
            if (discount.compareTo(bestDiscount) > 0)
            {
                bestDiscount = discount;
                best = cu;
            }
        }
        return best;
    }

    private BigDecimal calcDiscount(TakeoutCoupon coupon, BigDecimal orderAmount)
    {
        if ("0".equals(coupon.getType())) // 满减
        {
            return coupon.getDiscountAmount() == null ? BigDecimal.ZERO : coupon.getDiscountAmount();
        }
        else if ("1".equals(coupon.getType())) // 折扣
        {
            if (coupon.getDiscountRate() == null)
            {
                return BigDecimal.ZERO;
            }
            BigDecimal discount = orderAmount.multiply(BigDecimal.ONE.subtract(coupon.getDiscountRate().divide(new BigDecimal("100"))));
            if (coupon.getMaxDiscount() != null && discount.compareTo(coupon.getMaxDiscount()) > 0)
            {
                discount = coupon.getMaxDiscount();
            }
            return discount;
        }
        else if ("2".equals(coupon.getType())) // 新客券
        {
            return coupon.getDiscountAmount() == null ? BigDecimal.ZERO : coupon.getDiscountAmount();
        }
        else if ("3".equals(coupon.getType())) // 配送费券
        {
            return coupon.getDiscountAmount() == null ? BigDecimal.ZERO : coupon.getDiscountAmount();
        }
        return BigDecimal.ZERO;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int useCoupon(Long userCouponId, Long orderId)
    {
        TakeoutCouponUser cu = couponMapper.selectUserCouponList(new TakeoutCouponUser() {{ setId(userCouponId); }}).stream()
                .filter(x -> x.getId().equals(userCouponId))
                .findFirst().orElse(null);
        if (cu == null)
        {
            throw new ServiceException("[ERR_" + ERR_USER_COUPON_NOT_FOUND + "] 用户优惠券不存在", ERR_USER_COUPON_NOT_FOUND);
        }
        if (!"0".equals(cu.getStatus()))
        {
            throw new ServiceException("[ERR_" + ERR_COUPON_USED + "] 优惠券已使用或不可用", ERR_COUPON_USED);
        }
        if (cu.getExpireTime() != null && cu.getExpireTime().before(DateUtils.getNowDate()))
        {
            throw new ServiceException("[ERR_" + ERR_COUPON_EXPIRED + "] 优惠券已过期", ERR_COUPON_EXPIRED);
        }
        TakeoutCouponUser upd = new TakeoutCouponUser();
        upd.setId(userCouponId);
        upd.setStatus("1");
        upd.setUsedTime(DateUtils.getNowDate());
        upd.setUsedOrderId(orderId);
        upd.setUpdateBy(SecurityUtils.getUsername());
        return couponMapper.updateUserCouponStatus(upd);
    }
}
