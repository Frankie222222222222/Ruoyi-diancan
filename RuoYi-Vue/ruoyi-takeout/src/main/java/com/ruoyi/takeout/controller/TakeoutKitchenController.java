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
import com.ruoyi.takeout.domain.TakeoutOrder;
import com.ruoyi.takeout.service.ITakeoutOrderService;

/**
 * 后厨 Controller（v2 - 2026-10-10）
 * <p>
 * 后厨角色(RBAC permi: takeout:kitchen:*) 的工作台接口：
 * <ul>
 *   <li>{@code GET  /takeout/kitchen/list}        拉取可见订单(待制作/制作中)</li>
 *   <li>{@code PUT  /takeout/kitchen/accept/{id}} 接单(PAID/ACCEPTED → MAKING)</li>
 *   <li>{@code PUT  /takeout/kitchen/ready/{id}}  出餐完毕(MAKING → READY)</li>
 *   <li>{@code GET  /takeout/kitchen/dashboard}   看板统计(待制/制作中/出餐待接 数量)</li>
 * </ul>
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/takeout/kitchen")
public class TakeoutKitchenController extends BaseController
{
    @Autowired
    private ITakeoutOrderService orderService;

    /**
     * 后厨拉取可见订单
     * <p>返回 status ∈ {1=已支付, 2=旧值待制作, 2a=制作中} 的订单</p>
     */
    @PreAuthorize("@ss.hasPermi('takeout:kitchen:list')")
    @GetMapping("/list")
    public TableDataInfo list()
    {
        startPage();
        List<TakeoutOrder> list = orderService.selectKitchenVisibleOrders();
        return getDataTable(list);
    }

    /**
     * 后厨接单(PAID 或旧值 ACCEPTED → MAKING)
     * @param orderId 订单ID
     * @param kitchenId 后厨用户ID(可选,传了会写入 takeout_order.kitchen_id)
     */
    @PreAuthorize("@ss.hasPermi('takeout:kitchen:accept')")
    @Log(title = "后厨接单", businessType = BusinessType.UPDATE)
    @PutMapping("/accept/{orderId}")
    public AjaxResult accept(@PathVariable("orderId") Long orderId,
                             @RequestParam(value = "kitchenId", required = false) Long kitchenId)
    {
        int rows = orderService.kitchenAcceptOrder(orderId, kitchenId);
        return rows > 0 ? success() : error();
    }

    /**
     * 后厨出餐完毕(MAKING → READY,触发骑手可见)
     */
    @PreAuthorize("@ss.hasPermi('takeout:kitchen:ready')")
    @Log(title = "后厨出餐", businessType = BusinessType.UPDATE)
    @PutMapping("/ready/{orderId}")
    public AjaxResult ready(@PathVariable("orderId") Long orderId,
                            @RequestParam(value = "kitchenId", required = false) Long kitchenId)
    {
        int rows = orderService.kitchenReadyOrder(orderId, kitchenId);
        return rows > 0 ? success() : error();
    }

    /**
     * 后厨看板统计
     * <p>返回字段: paidCount / acceptedCount / makingCount / readyCount / totalCount</p>
     */
    @PreAuthorize("@ss.hasPermi('takeout:kitchen:query')")
    @GetMapping("/dashboard")
    public AjaxResult dashboard()
    {
        return success(orderService.countKitchenPending());
    }
}
