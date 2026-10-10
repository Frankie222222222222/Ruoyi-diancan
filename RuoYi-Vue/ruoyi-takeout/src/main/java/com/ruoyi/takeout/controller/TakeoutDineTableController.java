package com.ruoyi.takeout.controller;

import java.util.List;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.takeout.domain.TakeoutDineTable;
import com.ruoyi.takeout.service.ITakeoutDineTableService;

/**
 * 堂食桌台 Controller
 *
 * <p>管理端:商家/平台 admin 维护桌台,生成二维码链接。
 * 顾客端扫码走 {@code /takeout/dineIn/table/{id}} 不在本 controller。</p>
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/takeout/dineTable")
public class TakeoutDineTableController extends BaseController
{
    @Autowired
    private ITakeoutDineTableService dineTableService;

    @PreAuthorize("@ss.hasPermi('takeout:dineTable:list')")
    @GetMapping("/list")
    public TableDataInfo list(TakeoutDineTable query)
    {
        startPage();
        return getDataTable(dineTableService.selectDineTableList(query));
    }

    @PreAuthorize("@ss.hasPermi('takeout:dineTable:query')")
    @GetMapping("/{tableId}")
    public AjaxResult getInfo(@PathVariable("tableId") Long tableId)
    {
        return success(dineTableService.selectDineTableById(tableId));
    }

    @PreAuthorize("@ss.hasPermi('takeout:dineTable:query')")
    @GetMapping("/checkTableNoUnique")
    public AjaxResult checkUnique(TakeoutDineTable table)
    {
        return success(dineTableService.checkTableNoUnique(table));
    }

    @PreAuthorize("@ss.hasPermi('takeout:dineTable:add')")
    @Log(title = "堂食桌台", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody TakeoutDineTable table)
    {
        int rows = dineTableService.insertDineTable(table);
        return rows > 0 ? success(table) : error();
    }

    @PreAuthorize("@ss.hasPermi('takeout:dineTable:edit')")
    @Log(title = "堂食桌台", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody TakeoutDineTable table)
    {
        return toAjax(dineTableService.updateDineTable(table));
    }

    @PreAuthorize("@ss.hasPermi('takeout:dineTable:remove')")
    @Log(title = "堂食桌台", businessType = BusinessType.DELETE)
    @DeleteMapping("/{tableIds}")
    public AjaxResult remove(@PathVariable Long[] tableIds)
    {
        return toAjax(dineTableService.deleteDineTableByIds(tableIds));
    }

    @PreAuthorize("@ss.hasPermi('takeout:dineTable:export')")
    @Log(title = "堂食桌台", businessType = BusinessType.EXPORT)
    @GetMapping("/export")
    public void export(HttpServletResponse response, TakeoutDineTable query)
    {
        List<TakeoutDineTable> list = dineTableService.selectDineTableList(query);
        ExcelUtil<TakeoutDineTable> util = new ExcelUtil<>(TakeoutDineTable.class);
        util.exportExcel(response, list, "堂食桌台数据");
    }
}
