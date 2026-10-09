package com.ruoyi.takeout.service;

import java.util.List;
import com.ruoyi.takeout.domain.TakeoutCoupon;
import com.ruoyi.takeout.domain.TakeoutCouponUser;

/**
 * 优惠券 Service接口
 *
 * @author ruoyi
 */
public interface ITakeoutCouponService
{
    /** 查询优惠券列表 */
    List<TakeoutCoupon> selectCouponList(TakeoutCoupon coupon);

    /** 查询单个优惠券 */
    TakeoutCoupon selectCouponById(Long couponId);

    /** 新增优惠券 */
    int insertCoupon(TakeoutCoupon coupon);

    /** 修改优惠券 */
    int updateCoupon(TakeoutCoupon coupon);

    /** 删除优惠券(逻辑) */
    int deleteCouponByIds(Long[] couponIds);

    /** C端用户领券 */
    int receiveCoupon(Long userId, Long couponId);

    /** 查询用户优惠券列表 */
    List<TakeoutCouponUser> selectUserCouponList(Long userId, String status);

    /** 计算订单可用的最优优惠券 */
    TakeoutCouponUser findBestCouponForOrder(Long userId, Long merchantId, java.math.BigDecimal orderAmount);

    /** 下单时核销优惠券 */
    int useCoupon(Long userCouponId, Long orderId);
}
