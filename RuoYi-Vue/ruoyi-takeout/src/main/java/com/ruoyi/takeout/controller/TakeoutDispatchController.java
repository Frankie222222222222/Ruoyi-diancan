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
import com.ruoyi.takeout.domain.TakeoutDispatch;
import com.ruoyi.takeout.service.ITakeoutDispatchService;

/**
 * 派单 Controller
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/takeout/dispatch")
public class TakeoutDispatchController extends BaseController
{
    @Autowired
    private ITakeoutDispatchService dispatchService;

    /** 分页查询 */
    @PreAuthorize("@ss.hasPermi('takeout:dispatch:list')")
    @GetMapping("/list")
    public TableDataInfo list(TakeoutDispatch dispatch)
    {
        startPage();
        return getDataTable(dispatchService.selectDispatchList(dispatch));
    }

    /** 详情 */
    @PreAuthorize("@ss.hasPermi('takeout:dispatch:query')")
    @GetMapping("/{dispatchId}")
    public AjaxResult getInfo(@PathVariable Long dispatchId)
    {
        return success(dispatchService.selectDispatchById(dispatchId));
    }

    /** 创建派单(后台手动派单) */
    @PreAuthorize("@ss.hasPermi('takeout:dispatch:add')")
    @Log(title = "派单", businessType = BusinessType.INSERT)
    @PostMapping("/create")
    public AjaxResult create(@RequestBody TakeoutDispatch dispatch)
    {
        if (dispatch == null || dispatch.getOrderId() == null)
        {
            return error("订单ID不能为空");
        }
        int rows = dispatchService.createDispatchForOrder(dispatch.getOrderId(),
                dispatch.getRiderId(),
                dispatch.getDispatchType());
        return rows > 0 ? success() : error("派单失败，请重试");
    }

    /** 骑手抢单 */
    @Log(title = "派单", businessType = BusinessType.UPDATE)
    @PostMapping("/claim/{orderId}/{riderId}")
    public AjaxResult claim(@PathVariable Long orderId, @PathVariable Long riderId)
    {
        return toAjax(dispatchService.claimOrder(orderId, riderId));
    }

    /** 骑手接单 */
    @Log(title = "派单", businessType = BusinessType.UPDATE)
    @PutMapping("/accept/{dispatchId}/{riderId}")
    public AjaxResult accept(@PathVariable Long dispatchId, @PathVariable Long riderId)
    {
        return toAjax(dispatchService.acceptDispatch(dispatchId, riderId));
    }

    /** 骑手取餐 */
    @Log(title = "派单", businessType = BusinessType.UPDATE)
    @PutMapping("/pickup/{dispatchId}")
    public AjaxResult pickup(@PathVariable Long dispatchId)
    {
        return toAjax(dispatchService.pickup(dispatchId));
    }

    /** 完成配送 */
    @Log(title = "派单", businessType = BusinessType.UPDATE)
    @PutMapping("/complete/{dispatchId}")
    public AjaxResult complete(@PathVariable Long dispatchId)
    {
        return toAjax(dispatchService.completeDispatch(dispatchId));
    }

    /** 取消派单 */
    @Log(title = "派单", businessType = BusinessType.UPDATE)
    @PutMapping("/cancel/{dispatchId}")
    public AjaxResult cancel(@PathVariable Long dispatchId,
                             @RequestParam(required = false) String reason)
    {
        return toAjax(dispatchService.cancelDispatch(dispatchId, reason));
    }

    /** 改派：把进行中的派单转给新骑手 */
    @PreAuthorize("@ss.hasPermi('takeout:dispatch:edit')")
    @Log(title = "派单", businessType = BusinessType.UPDATE)
    @PutMapping("/reassign/{dispatchId}/{newRiderId}")
    public AjaxResult reassign(@PathVariable Long dispatchId,
                               @PathVariable Long newRiderId,
                               @RequestParam(required = false) String reason)
    {
        return toAjax(dispatchService.reassignDispatch(dispatchId, newRiderId, reason));
    }

    /** 查询订单的有效派单 */
    @GetMapping("/active/{orderId}")
    public AjaxResult activeByOrder(@PathVariable Long orderId)
    {
        return success(dispatchService.selectActiveDispatchByOrderId(orderId));
    }

    /** 查询骑手的配送记录 */
    @GetMapping("/rider/{riderId}")
    public AjaxResult listByRider(@PathVariable Long riderId)
    {
        List<TakeoutDispatch> list = dispatchService.selectDispatchList(new TakeoutDispatch() {{ setRiderId(riderId); }});
        return success(list);
    }

    /** 删除(逻辑) */
    @PreAuthorize("@ss.hasPermi('takeout:dispatch:remove')")
    @Log(title = "派单", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(dispatchService.deleteDispatchByIds(ids));
    }
}
