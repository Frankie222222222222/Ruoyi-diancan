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
import com.ruoyi.takeout.domain.TakeoutRider;
import com.ruoyi.takeout.service.ITakeoutRiderService;

/**
 * 骑手 Controller
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/takeout/rider")
public class TakeoutRiderController extends BaseController
{
    @Autowired
    private ITakeoutRiderService riderService;

    /** 分页查询 */
    @PreAuthorize("@ss.hasPermi('takeout:rider:list')")
    @GetMapping("/list")
    public TableDataInfo list(TakeoutRider rider)
    {
        startPage();
        return getDataTable(riderService.selectRiderList(rider));
    }

    /** 详情 */
    @PreAuthorize("@ss.hasPermi('takeout:rider:query')")
    @GetMapping("/{riderId}")
    public AjaxResult getInfo(@PathVariable Long riderId)
    {
        return success(riderService.selectRiderById(riderId));
    }

    /** 新增 */
    @PreAuthorize("@ss.hasPermi('takeout:rider:add')")
    @Log(title = "骑手", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody TakeoutRider rider)
    {
        int rows = riderService.insertRider(rider);
        return rows > 0 ? success(rider.getRiderId()) : error();
    }

    /** 修改 */
    @PreAuthorize("@ss.hasPermi('takeout:rider:edit')")
    @Log(title = "骑手", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody TakeoutRider rider)
    {
        return toAjax(riderService.updateRider(rider));
    }

    /** 删除 */
    @PreAuthorize("@ss.hasPermi('takeout:rider:remove')")
    @Log(title = "骑手", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(riderService.deleteRiderByIds(ids));
    }

    /** 修改接单状态 */
    @PreAuthorize("@ss.hasPermi('takeout:rider:status')")
    @Log(title = "骑手", businessType = BusinessType.UPDATE)
    @PutMapping("/changeStatus/{riderId}/{status}")
    public AjaxResult changeStatus(@PathVariable Long riderId, @PathVariable String status)
    {
        return toAjax(riderService.changeRiderStatus(riderId, status));
    }

    /** 查询可用骑手(对外给派单系统用) */
    @GetMapping("/available")
    public AjaxResult available(@RequestParam(required = false) String city)
    {
        List<TakeoutRider> list = riderService.selectAvailableRiders(city);
        return success(list);
    }
}
