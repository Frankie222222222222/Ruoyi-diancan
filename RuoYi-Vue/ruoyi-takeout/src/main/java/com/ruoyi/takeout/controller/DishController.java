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
import com.ruoyi.takeout.domain.Dish;
import com.ruoyi.takeout.domain.TakeoutOrder;
import com.ruoyi.takeout.mapper.TakeoutOrderMapper;
import com.ruoyi.takeout.service.IDishService;

/**
 * 菜品 Controller
 */
@RestController
@RequestMapping("/takeout/dish")
public class DishController extends BaseController
{
    @Autowired
    private IDishService dishService;

    @Autowired
    private TakeoutOrderMapper orderMapper;

    /**
     * 查询菜品列表（分页 + 筛选）
     */
    @PreAuthorize("@ss.hasPermi('takeout:dish:list')")
    @GetMapping("/list")
    public TableDataInfo list(Dish query)
    {
        startPage();
        return getDataTable(dishService.list(query));
    }

    /**
     * 获取菜品详情
     */
    @PreAuthorize("@ss.hasPermi('takeout:dish:query')")
    @GetMapping("/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id)
    {
        return AjaxResult.success(dishService.getById(id));
    }

    /**
     * 新增菜品
     */
    @PreAuthorize("@ss.hasPermi('takeout:dish:add')")
    @Log(title = "菜品", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody Dish dish)
    {
        int rows = dishService.insert(dish);
        return rows > 0 ? success(dish.getDishId()) : error();
    }

    /**
     * 修改菜品
     */
    @PreAuthorize("@ss.hasPermi('takeout:dish:edit')")
    @Log(title = "菜品", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody Dish dish)
    {
        return toAjax(dishService.update(dish));
    }

    /**
     * 删除菜品（支持批量）
     */
    @PreAuthorize("@ss.hasPermi('takeout:dish:remove')")
    @Log(title = "菜品", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(dishService.removeByIds(ids));
    }

    /**
     * 上下架切换
     */
    @PreAuthorize("@ss.hasPermi('takeout:dish:edit')")
    @Log(title = "菜品", businessType = BusinessType.UPDATE)
    @PutMapping("/changeStatus/{id}/{status}")
    public AjaxResult changeStatus(@PathVariable("id") Long id, @PathVariable("status") String status)
    {
        return toAjax(dishService.changeStatus(id, status));
    }

    /**
     * 调整库存 delta 可正可负
     */
    @PreAuthorize("@ss.hasPermi('takeout:dish:edit')")
    @Log(title = "菜品", businessType = BusinessType.UPDATE)
    @PutMapping("/adjustStock/{id}/{delta}")
    public AjaxResult adjustStock(@PathVariable("id") Long id, @PathVariable("delta") Integer delta)
    {
        return toAjax(dishService.adjustStock(id, delta));
    }

    /**
     * 销量 Top N（默认 10）
     */
    @PreAuthorize("@ss.hasPermi('takeout:dish:list')")
    @GetMapping("/topSales")
    public AjaxResult topSales(@RequestParam(required = false) Long merchantId,
                               @RequestParam(defaultValue = "10") Integer limit)
    {
        return success(dishService.topSales(merchantId, limit));
    }

    /**
     * 列出某商家下在售菜品（订单弹窗选用）
     */
    @PreAuthorize("@ss.hasPermi('takeout:order:add')")
    @GetMapping("/listDishByMerchant/{merchantId}")
    public AjaxResult listByMerchant(@PathVariable("merchantId") Long merchantId)
    {
        return success(dishService.listByMerchant(merchantId));
    }

    /**
     * 列出引用过某菜品的所有订单（菜品管理 - 订单来源）
     */
    @PreAuthorize("@ss.hasPermi('takeout:dish:query')")
    @GetMapping("/orders/{dishId}")
    public AjaxResult ordersByDishId(@PathVariable("dishId") Long dishId)
    {
        List<TakeoutOrder> list = orderMapper.selectOrdersByDishId(dishId);
        return success(list);
    }
}
