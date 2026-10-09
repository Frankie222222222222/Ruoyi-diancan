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
import com.ruoyi.takeout.domain.TakeoutOrder;
import com.ruoyi.takeout.enums.OrderStatusEnum;
import com.ruoyi.takeout.service.ITakeoutOrderService;

/**
 * 订单 Controller
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/takeout/order")
public class TakeoutOrderController extends BaseController
{
    @Autowired
    private ITakeoutOrderService orderService;

    /**
     * 查询订单列表（分页 + 筛选）
     */
    @PreAuthorize("@ss.hasPermi('takeout:order:list')")
    @GetMapping("/list")
    public TableDataInfo list(TakeoutOrder query)
    {
        startPage();
        return getDataTable(orderService.selectOrderList(query));
    }

    /**
     * 获取订单详情（含明细）
     */
    @PreAuthorize("@ss.hasPermi('takeout:order:query')")
    @GetMapping("/{orderId}")
    public AjaxResult getInfo(@PathVariable("orderId") Long orderId)
    {
        return success(orderService.selectOrderById(orderId));
    }

    /**
     * 订单号唯一校验
     */
    @PreAuthorize("@ss.hasPermi('takeout:order:query')")
    @GetMapping("/checkOrderNoUnique")
    public AjaxResult checkOrderNoUnique(TakeoutOrder order)
    {
        return success(orderService.checkOrderNoUnique(order));
    }

    /**
     * 获取订单状态字典
     */
    @PreAuthorize("@ss.hasPermi('takeout:order:query')")
    @GetMapping("/dict/status")
    public AjaxResult statusDict()
    {
        return success(OrderStatusEnum.toMap());
    }

    /**
     * 新增订单
     */
    @PreAuthorize("@ss.hasPermi('takeout:order:add')")
    @Log(title = "订单", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody TakeoutOrder order)
    {
        int rows = orderService.insertOrder(order);
        return rows > 0 ? success(order.getOrderId()) : error();
    }

    /**
     * 修改订单
     */
    @PreAuthorize("@ss.hasPermi('takeout:order:edit')")
    @Log(title = "订单", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody TakeoutOrder order)
    {
        return toAjax(orderService.updateOrder(order));
    }

    /**
     * 修改订单状态（按状态机校验）
     */
    @PreAuthorize("@ss.hasPermi('takeout:order:changeStatus')")
    @Log(title = "订单", businessType = BusinessType.UPDATE)
    @PutMapping("/changeStatus/{orderId}/{status}")
    public AjaxResult changeStatus(@PathVariable("orderId") Long orderId,
                                   @PathVariable("status") String status)
    {
        return toAjax(orderService.changeOrderStatus(orderId, status));
    }

    /**
     * 取消订单
     */
    @PreAuthorize("@ss.hasPermi('takeout:order:cancel')")
    @Log(title = "订单", businessType = BusinessType.UPDATE)
    @PutMapping("/cancel/{orderId}")
    public AjaxResult cancel(@PathVariable("orderId") Long orderId,
                             @RequestParam(value = "reason", required = false) String reason)
    {
        return toAjax(orderService.cancelOrder(orderId, reason));
    }

    /**
     * 删除订单
     */
    @PreAuthorize("@ss.hasPermi('takeout:order:remove')")
    @Log(title = "订单", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(orderService.deleteOrderByIds(ids));
    }

    /**
     * 导出订单
     */
    @PreAuthorize("@ss.hasPermi('takeout:order:export')")
    @Log(title = "订单", businessType = BusinessType.EXPORT)
    @GetMapping("/export")
    public void export(HttpServletResponse response, TakeoutOrder query)
    {
        List<TakeoutOrder> list = orderService.selectOrderList(query);
        ExcelUtil<TakeoutOrder> util = new ExcelUtil<>(TakeoutOrder.class);
        util.exportExcel(response, list, "订单数据");
    }
}
