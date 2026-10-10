package com.ruoyi.takeout.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.ruoyi.common.annotation.Anonymous;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.takeout.domain.Dish;
import com.ruoyi.takeout.domain.TakeoutDineTable;
import com.ruoyi.takeout.domain.TakeoutOrder;
import com.ruoyi.takeout.domain.TakeoutOrderItem;
import com.ruoyi.takeout.service.IDishService;
import com.ruoyi.takeout.service.ITakeoutDineTableService;
import com.ruoyi.takeout.service.ITakeoutOrderService;

/**
 * 堂食扫码点餐 Controller（v3 - 2026-10-10）
 *
 * <p>顾客到店扫码 → 打开 H5 页面 → 选菜下单 → 支付 → 后厨接单 → 出餐 → 确认上桌。
 * 不走配送,不需要骑手。同一桌可多次加菜(DRAFT 状态可加),点"立即结账"后状态变 PAID。</p>
 *
 * <h3>接口列表</h3>
 * <ul>
 *   <li>{@code GET  /takeout/dineIn/table/{tableId}}  公开:扫码进店拉桌台信息 + 商家信息 + 在售菜品</li>
 *   <li>{@code GET  /takeout/dineIn/menu?merchantId=X}  公开:列出某商家在售菜品</li>
 *   <li>{@code POST /takeout/dineIn/order}             C端登录:点菜创建 DRAFT 订单</li>
 *   <li>{@code POST /takeout/dineIn/order/{id}/addItem} C端登录:DRAFT 状态加菜</li>
 *   <li>{@code POST /takeout/dineIn/order/{id}/pay}     C端登录:结账(DRAFT→PAID)</li>
 *   <li>{@code POST /takeout/dineIn/order/{id}/confirmServed} C端登录:确认上桌(READY→DONE)</li>
 *   <li>{@code POST /takeout/dineIn/order/{id}/cancel}  C端登录:取消(任意进行中→CANCELLED)</li>
 *   <li>{@code GET  /takeout/dineIn/order/{id}}        C端登录:订单详情</li>
 *   <li>{@code GET  /takeout/dineIn/order/active?tableId=X} 公开:某桌台当前进行中订单</li>
 * </ul>
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/takeout/dineIn")
public class TakeoutDineInController extends BaseController
{
    @Autowired
    private ITakeoutDineTableService dineTableService;

    @Autowired
    private ITakeoutOrderService orderService;

    @Autowired
    private IDishService dishService;

    /**
     * 扫码进店(公开): 返回桌台 + 在售菜品 + 商家基础信息
     * 前端拼 H5 链接: ${baseUrl}/dine-in?tableId={tableId}
     */
    @Anonymous
    @GetMapping("/table/{tableId}")
    public AjaxResult enterShop(@PathVariable("tableId") Long tableId)
    {
        TakeoutDineTable table = dineTableService.selectDineTableById(tableId);
        if (table == null)
        {
            return AjaxResult.error(404, "桌台不存在");
        }
        List<Dish> menu = dishService.listByMerchant(table.getMerchantId());
        Map<String, Object> data = new HashMap<>();
        data.put("table", table);
        data.put("menu", menu);
        return AjaxResult.success(data);
    }

    /**
     * 某商家在售菜品(公开)
     */
    @Anonymous
    @GetMapping("/menu")
    public AjaxResult menu(@RequestParam("merchantId") Long merchantId)
    {
        return AjaxResult.success(dishService.listByMerchant(merchantId));
    }

    /**
     * 桌台当前进行中订单(公开,用于加菜前查桌台 session)
     */
    @Anonymous
    @GetMapping("/order/active")
    public AjaxResult activeOrders(@RequestParam("tableId") Long tableId)
    {
        return AjaxResult.success(dineTableService.selectActiveDineOrdersByTable(tableId));
    }

    /**
     * 点菜下单(创建 DRAFT 订单)
     * 请求体示例: { "tableId": 1, "merchantId": 1, "remark": "微辣", "orderItems": [...] }
     */
    @PreAuthorize("@ss.hasPermi('takeout:order:add')")
    @PostMapping("/order")
    public AjaxResult createOrder(@RequestBody TakeoutOrder order)
    {
        // 堂食默认绑定当前登录 C 端 user
        try
        {
            Long loginUserId = SecurityUtils.getUserId();
            if (loginUserId != null)
            {
                order.setUserId(loginUserId);
            }
        }
        catch (Exception ignored) {}
        order.setOrderType(1);
        int rows = orderService.createDineOrder(order);
        if (rows > 0)
        {
            TakeoutOrder created = orderService.selectOrderById(order.getOrderId());
            return AjaxResult.success("堂食订单已创建,等待顾客结账", created);
        }
        return AjaxResult.error("创建失败");
    }

    /**
     * DRAFT 状态加菜
     * 请求体: { "dishId": 5001, "quantity": 2 }
     */
    @PreAuthorize("@ss.hasPermi('takeout:order:edit')")
    @PostMapping("/order/{orderId}/addItem")
    public AjaxResult addItem(@PathVariable("orderId") Long orderId, @RequestBody TakeoutOrderItem item)
    {
        int rows = orderService.addDineOrderItem(orderId, item);
        if (rows > 0)
        {
            return AjaxResult.success("已加菜", orderService.selectOrderById(orderId));
        }
        return AjaxResult.error("加菜失败");
    }

    /**
     * 结账(DRAFT → PAID,触发支付流程,前端跳支付)
     */
    @PreAuthorize("@ss.hasPermi('takeout:order:edit')")
    @PostMapping("/order/{orderId}/pay")
    public AjaxResult pay(@PathVariable("orderId") Long orderId)
    {
        int rows = orderService.payDineOrder(orderId);
        if (rows > 0)
        {
            TakeoutOrder order = orderService.selectOrderById(orderId);
            // 这里直接返回订单,前端可调 /takeout/payment/create 走支付(MOCK)
            return AjaxResult.success("结账成功,等待后厨接单", order);
        }
        return AjaxResult.error("结账失败");
    }

    /**
     * 确认上桌(READY → DONE,触发桌台释放)
     */
    @PreAuthorize("@ss.hasPermi('takeout:order:edit')")
    @PostMapping("/order/{orderId}/confirmServed")
    public AjaxResult confirmServed(@PathVariable("orderId") Long orderId)
    {
        int rows = orderService.confirmDineOrderServed(orderId);
        return rows > 0 ? AjaxResult.success("已确认上桌") : AjaxResult.error("操作失败");
    }

    /**
     * 取消堂食订单
     */
    @PreAuthorize("@ss.hasPermi('takeout:order:cancel')")
    @PostMapping("/order/{orderId}/cancel")
    public AjaxResult cancel(@PathVariable("orderId") Long orderId,
                             @RequestParam(value = "reason", required = false) String reason)
    {
        int rows = orderService.cancelDineOrder(orderId, reason);
        return rows > 0 ? AjaxResult.success("已取消") : AjaxResult.error("取消失败");
    }

    /**
     * 订单详情
     */
    @PreAuthorize("@ss.hasPermi('takeout:order:query')")
    @GetMapping("/order/{orderId}")
    public AjaxResult orderDetail(@PathVariable("orderId") Long orderId)
    {
        return AjaxResult.success(orderService.selectOrderById(orderId));
    }
}
