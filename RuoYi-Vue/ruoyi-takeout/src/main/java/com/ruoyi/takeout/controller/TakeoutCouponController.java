package com.ruoyi.takeout.controller;

import java.math.BigDecimal;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.takeout.domain.TakeoutCoupon;
import com.ruoyi.takeout.domain.TakeoutCouponUser;
import com.ruoyi.takeout.service.ITakeoutCouponService;

/**
 * 优惠券 Controller
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/takeout/coupon")
public class TakeoutCouponController extends BaseController
{
    @Autowired
    private ITakeoutCouponService couponService;

    /** 优惠券列表(后台) */
    @PreAuthorize("@ss.hasPermi('takeout:coupon:list')")
    @GetMapping("/list")
    public TableDataInfo list(TakeoutCoupon coupon)
    {
        startPage();
        return getDataTable(couponService.selectCouponList(coupon));
    }

    /** 详情 */
    @PreAuthorize("@ss.hasPermi('takeout:coupon:query')")
    @GetMapping("/{couponId}")
    public AjaxResult getInfo(@PathVariable Long couponId)
    {
        return success(couponService.selectCouponById(couponId));
    }

    /** 新增 */
    @PreAuthorize("@ss.hasPermi('takeout:coupon:add')")
    @Log(title = "优惠券", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody TakeoutCoupon coupon)
    {
        int rows = couponService.insertCoupon(coupon);
        return rows > 0 ? success(coupon.getCouponId()) : error();
    }

    /** 修改 */
    @PreAuthorize("@ss.hasPermi('takeout:coupon:edit')")
    @Log(title = "优惠券", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody TakeoutCoupon coupon)
    {
        return toAjax(couponService.updateCoupon(coupon));
    }

    /** 删除 */
    @PreAuthorize("@ss.hasPermi('takeout:coupon:remove')")
    @Log(title = "优惠券", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(couponService.deleteCouponByIds(ids));
    }

    /** C端领券 */
    @Log(title = "优惠券", businessType = BusinessType.INSERT)
    @PostMapping("/receive/{couponId}/{userId}")
    public AjaxResult receive(@PathVariable Long couponId, @PathVariable Long userId)
    {
        return toAjax(couponService.receiveCoupon(userId, couponId));
    }

    /** C端我的优惠券 */
    @GetMapping("/userCoupon/{userId}")
    public AjaxResult userCoupons(@PathVariable Long userId,
                                  @RequestParam(required = false) String status)
    {
        List<TakeoutCouponUser> list = couponService.selectUserCouponList(userId, status);
        return success(list);
    }

    /** 计算订单最优可用券 */
    @GetMapping("/bestForOrder")
    public AjaxResult bestForOrder(@RequestParam Long userId,
                                   @RequestParam(required = false) Long merchantId,
                                   @RequestParam BigDecimal orderAmount)
    {
        return success(couponService.findBestCouponForOrder(userId, merchantId, orderAmount));
    }

    /** 下单核销 */
    @Log(title = "优惠券", businessType = BusinessType.UPDATE)
    @PutMapping("/use/{userCouponId}/{orderId}")
    public AjaxResult use(@PathVariable Long userCouponId, @PathVariable Long orderId)
    {
        return toAjax(couponService.useCoupon(userCouponId, orderId));
    }
}
