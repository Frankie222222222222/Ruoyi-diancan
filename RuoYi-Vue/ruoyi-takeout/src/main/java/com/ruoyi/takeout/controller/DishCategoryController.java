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
import com.ruoyi.takeout.domain.DishCategory;
import com.ruoyi.takeout.service.IDishCategoryService;

/**
 * 菜品分类 Controller
 */
@RestController
@RequestMapping("/takeout/dishCategory")
public class DishCategoryController extends BaseController
{
    @Autowired
    private IDishCategoryService dishCategoryService;

    /**
     * 查询菜品分类列表（分页）
     */
    @PreAuthorize("@ss.hasPermi('takeout:dishCategory:list')")
    @GetMapping("/list")
    public TableDataInfo list(DishCategory query)
    {
        startPage();
        return getDataTable(dishCategoryService.list(query));
    }

    /**
     * 查询下拉选项（只查启用）
     */
    @PreAuthorize("@ss.hasPermi('takeout:dishCategory:query')")
    @GetMapping("/optionselect")
    public AjaxResult optionselect()
    {
        return AjaxResult.success(dishCategoryService.optionselect());
    }

    /**
     * 获取分类详情
     */
    @PreAuthorize("@ss.hasPermi('takeout:dishCategory:query')")
    @GetMapping("/{categoryId}")
    public AjaxResult getInfo(@PathVariable("categoryId") Long categoryId)
    {
        return success(dishCategoryService.getById(categoryId));
    }

    /**
     * 新增分类
     */
    @PreAuthorize("@ss.hasPermi('takeout:dishCategory:add')")
    @Log(title = "菜品分类", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody DishCategory category)
    {
        int rows = dishCategoryService.insert(category);
        return rows > 0 ? success(category.getCategoryId()) : error();
    }

    /**
     * 修改分类
     */
    @PreAuthorize("@ss.hasPermi('takeout:dishCategory:edit')")
    @Log(title = "菜品分类", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody DishCategory category)
    {
        return toAjax(dishCategoryService.update(category));
    }

    /**
     * 删除分类
     */
    @PreAuthorize("@ss.hasPermi('takeout:dishCategory:remove')")
    @Log(title = "菜品分类", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(dishCategoryService.removeByIds(ids));
    }
}
