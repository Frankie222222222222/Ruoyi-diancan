package com.ruoyi.takeout.mapper;

import java.util.List;
import com.ruoyi.takeout.domain.TakeoutCoupon;
import com.ruoyi.takeout.domain.TakeoutCouponUser;

/**
 * 优惠券 Mapper
 *
 * @author ruoyi
 */
public interface TakeoutCouponMapper
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

    /** 扣减剩余数量 */
    int decrementRemainCount(Long couponId);

    /** 查询用户优惠券列表 */
    List<TakeoutCouponUser> selectUserCouponList(TakeoutCouponUser userCoupon);

    /** 查询用户已领取的优惠券记录 */
    TakeoutCouponUser selectUserCoupon(Long userId, Long couponId);

    /** 新增用户领取记录 */
    int insertUserCoupon(TakeoutCouponUser userCoupon);

    /** 修改用户优惠券状态 */
    int updateUserCouponStatus(TakeoutCouponUser userCoupon);

    /** 统计用户已领券数量 */
    int countUserCoupons(Long userId, Long couponId);
}
