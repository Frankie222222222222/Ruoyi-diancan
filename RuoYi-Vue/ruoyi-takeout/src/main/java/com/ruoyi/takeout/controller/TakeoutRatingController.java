package com.ruoyi.takeout.controller;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.takeout.domain.TakeoutRating;
import com.ruoyi.takeout.service.ITakeoutRatingService;

/**
 * 订单评价 Controller
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/takeout/rating")
public class TakeoutRatingController extends BaseController
{
    @Autowired
    private ITakeoutRatingService ratingService;

    /** 分页查询 */
    @PreAuthorize("@ss.hasPermi('takeout:rating:list')")
    @GetMapping("/list")
    public TableDataInfo list(TakeoutRating rating)
    {
        startPage();
        return getDataTable(ratingService.selectRatingList(rating));
    }

    /** 详情 */
    @PreAuthorize("@ss.hasPermi('takeout:rating:query')")
    @GetMapping("/{ratingId}")
    public AjaxResult getInfo(@PathVariable Long ratingId)
    {
        return success(ratingService.selectRatingById(ratingId));
    }

    /** 通过订单ID查评价 */
    @GetMapping("/byOrder/{orderId}")
    public AjaxResult byOrder(@PathVariable Long orderId)
    {
        return success(ratingService.selectRatingByOrderId(orderId));
    }

    /** C端用户提交评价 */
    @Log(title = "订单评价", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult submit(@RequestBody TakeoutRating rating)
    {
        int rows = ratingService.submitRating(rating);
        return rows > 0 ? success(rating.getRatingId()) : error();
    }

    /** 商家回复评价 */
    @PreAuthorize("@ss.hasPermi('takeout:rating:reply')")
    @Log(title = "订单评价", businessType = BusinessType.UPDATE)
    @PutMapping("/reply/{ratingId}")
    public AjaxResult reply(@PathVariable Long ratingId, @RequestParam String reply)
    {
        return toAjax(ratingService.replyRating(ratingId, reply));
    }

    /** 删除 */
    @PreAuthorize("@ss.hasPermi('takeout:rating:remove')")
    @Log(title = "订单评价", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(ratingService.deleteRatingByIds(ids));
    }
}
