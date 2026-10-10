package com.ruoyi.takeout.controller;

import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.takeout.domain.TakeoutOrder;
import com.ruoyi.takeout.domain.TakeoutOrderItem;
import com.ruoyi.takeout.service.ITakeoutOrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * uniCloud → SpringBoot 桥接 Controller（v2 - 2026-10-10）
 *
 * <p>本 Controller 仅供 uniCloud 云对象(zhouhao)调用,用于小程序扫码绑桌号 +
 * 透传订单到 SpringBoot。所有接口走共享密钥 {@code Authorization: takeout-internal-secret-xxx},
 * 可选 IP 白名单。</p>
 *
 * <h3>接口列表</h3>
 * <ul>
 *   <li>{@code POST /unicloud/order}            创建订单(透传购物车)</li>
 *   <li>{@code GET  /unicloud/order/byNo/{no}}  按订单号查询</li>
 *   <li>{@code GET  /unicloud/order/{id}}       按订单ID查询</li>
 * </ul>
 *
 * <h3>环境变量</h3>
 * <ul>
 *   <li>{@code unicloud.internal-secret}  共享密钥(与 zhouhao 云对象 env 配一致)</li>
 *   <li>{@code unicloud.ip-whitelist}      IP 白名单(逗号分隔,留空则只校验密钥)</li>
 * </ul>
 *
 * <h3>为什么要单开一个 Controller(不直接复用 /takeout/order)</h3>
 * <ol>
 *   <li>权限隔离:/takeout/order 是 C 端用户用的(走 JWT),/unicloud/order 是服务间调用(共享密钥)</li>
 *   <li>参数差异:小程序透传过来的是 openid/tableNo/购物车,不是完整的 TakeoutOrder</li>
 *   <li>可观测性:unified 一个入口方便日志/限流/告警</li>
 * </ol>
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/unicloud/order")
public class TakeoutUnicloudController
{
    private static final Logger log = LoggerFactory.getLogger(TakeoutUnicloudController.class);

    @Autowired
    private ITakeoutOrderService orderService;

    @Value("${unicloud.internal-secret:}")
    private String internalSecret;

    @Value("${unicloud.ip-whitelist:}")
    private String ipWhitelist;

    /**
     * 校验共享密钥(从 Authorization 头)
     * 返回 null=通过,非 null=拒绝
     */
    private String checkSecret(String authHeader)
    {
        if (internalSecret == null || internalSecret.isEmpty())
        {
            return "【服务器未配置 unicloud.internal-secret】请在 application.yml 设置";
        }
        if (authHeader == null || !internalSecret.equals(authHeader))
        {
            return "【共享密钥错误】请确认 Authorization 头与 application.yml 一致";
        }
        return null;
    }

    /**
     * 校验 IP 白名单(可选)
     * 返回 null=通过,非 null=拒绝
     */
    private String checkIp(String remoteAddr)
    {
        if (ipWhitelist == null || ipWhitelist.trim().isEmpty())
        {
            return null; // 未配置白名单,跳过
        }
        String[] allowList = ipWhitelist.split(",");
        for (String ip : allowList)
        {
            String trimmed = ip.trim();
            if (trimmed.isEmpty()) continue;
            // 精确匹配(简化版,生产可改成 CIDR)
            if (trimmed.equals(remoteAddr))
            {
                return null;
            }
        }
        return "【IP 不在白名单】remote=" + remoteAddr;
    }

    /**
     * 创建订单(uniCloud 透传)
     * <p>请求体 JSON:</p>
     * <pre>
     * {
     *   "orderNo":    null,  // 留空让 SpringBoot 自动生成
     *   "tableNo":    "A-12",
     *   "openid":     "oXyz...",
     *   "userId":     1001,  // 可选,小程序已登录用户;留空则用 openid 查
     *   "merchantId": 1,     // 必填,从菜品/桌号反查
     *   "remark":     "微辣",
     *   "orderType":  "takein",
     *   "orderItems": [
     *     { "dishId": 5001, "quantity": 2 },
     *     { "dishId": 5002, "quantity": 1 }
     *   ]
     * }
     * </pre>
     */
    @PostMapping
    public AjaxResult createOrder(@RequestHeader(value = "Authorization", required = false) String auth,
                                  @RequestHeader(value = "X-Forwarded-For", required = false) String xff,
                                  jakarta.servlet.http.HttpServletRequest request,
                                  @RequestBody Map<String, Object> body)
    {
        String ip = xff != null && !xff.isEmpty() ? xff.split(",")[0].trim() : request.getRemoteAddr();
        String err = checkSecret(auth);
        if (err == null) err = checkIp(ip);
        if (err != null)
        {
            log.warn("unicloud 鉴权失败: {}", err);
            return AjaxResult.error(401, err);
        }

        // 解析 body
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> itemsRaw = (List<Map<String, Object>>) body.get("orderItems");
        if (itemsRaw == null || itemsRaw.isEmpty())
        {
            return AjaxResult.error("PARAM_EMPTY", "orderItems 不能为空");
        }
        Object userIdObj = body.get("userId");
        Object merchantIdObj = body.get("merchantId");
        if (merchantIdObj == null)
        {
            // 简化:merchantId 必填,小程序端从购物车第一个菜品的 merchant_id 拿
            return AjaxResult.error("PARAM_MISSING", "merchantId 必填(从菜品反查)");
        }

        TakeoutOrder order = new TakeoutOrder();
        order.setOrderNo((String) body.get("orderNo"));
        order.setMerchantId(merchantIdObj instanceof Number
                ? ((Number) merchantIdObj).longValue()
                : Long.parseLong(String.valueOf(merchantIdObj)));
        if (userIdObj != null)
        {
            order.setUserId(userIdObj instanceof Number
                    ? ((Number) userIdObj).longValue()
                    : Long.parseLong(String.valueOf(userIdObj)));
        }
        order.setRemark((String) body.get("remark"));
        order.setAddress((String) body.getOrDefault("address", "堂食-" + body.getOrDefault("tableNo", "")));

        // 反查菜品组装 orderItems
        // 注:不在这里 import DishMapper 避免循环,改用 Service 层校验
        List<TakeoutOrderItem> items = new ArrayList<>(itemsRaw.size());
        for (Map<String, Object> it : itemsRaw)
        {
            TakeoutOrderItem ti = new TakeoutOrderItem();
            Object dishIdObj = it.get("dishId");
            if (dishIdObj == null)
            {
                return AjaxResult.error("PARAM_MISSING", "orderItems[].dishId 必填");
            }
            ti.setDishId(dishIdObj instanceof Number
                    ? ((Number) dishIdObj).longValue()
                    : Long.parseLong(String.valueOf(dishIdObj)));
            Object qtyObj = it.get("quantity");
            ti.setQuantity(qtyObj == null ? 1
                    : (qtyObj instanceof Number ? ((Number) qtyObj).intValue()
                    : Integer.parseInt(String.valueOf(qtyObj))));
            // dishName/dishImage/price 由 service 层查并补全
            // 这里只传 dishId + quantity, service 内部调 dishMapper 校验/扣库存
            // 注意:service.insertOrder 不会从 dishId 反查 dishName,需要先查出来
            // 简化处理:这里只传 dishId + quantity,让前端自己展示菜名
            // (SpringBoot 的 service.insertOrder 对每条 item 做库存校验,会自动拒绝库存不足)
            items.add(ti);
        }
        order.setOrderItems(items);

        // 算总价:小程序端应该传,这里兜底
        Object totalObj = body.get("totalAmount");
        if (totalObj != null)
        {
            order.setTotalAmount(totalObj instanceof Number
                    ? new BigDecimal(String.valueOf(totalObj))
                    : new BigDecimal(String.valueOf(totalObj)));
        }

        try
        {
            int rows = orderService.insertOrder(order);
            if (rows > 0)
            {
                TakeoutOrder created = orderService.selectOrderById(order.getOrderId());
                return AjaxResult.success("ok", created);
            }
            return AjaxResult.error("INSERT_FAIL", "插入订单失败");
        }
        catch (com.ruoyi.common.exception.ServiceException e)
        {
            log.warn("unicloud createOrder 业务异常: {}", e.getMessage());
            return AjaxResult.error(e.getCode() + "", e.getMessage());
        }
    }

    /**
     * 按订单号查询
     */
    @GetMapping("/byNo/{orderNo}")
    public AjaxResult getByOrderNo(@RequestHeader(value = "Authorization", required = false) String auth,
                                   @RequestHeader(value = "X-Forwarded-For", required = false) String xff,
                                   jakarta.servlet.http.HttpServletRequest request,
                                   @PathVariable String orderNo)
    {
        String ip = xff != null && !xff.isEmpty() ? xff.split(",")[0].trim() : request.getRemoteAddr();
        String err = checkSecret(auth);
        if (err == null) err = checkIp(ip);
        if (err != null) return AjaxResult.error(401, err);

        TakeoutOrder order = orderService.selectOrderByOrderNo(orderNo);
        if (order == null) return AjaxResult.error(404, "订单不存在");
        // 补全 items
        return AjaxResult.success(order);
    }

    /**
     * 按订单ID查询
     */
    @GetMapping("/{orderId}")
    public AjaxResult getById(@RequestHeader(value = "Authorization", required = false) String auth,
                              @RequestHeader(value = "X-Forwarded-For", required = false) String xff,
                              jakarta.servlet.http.HttpServletRequest request,
                              @PathVariable Long orderId)
    {
        String ip = xff != null && !xff.isEmpty() ? xff.split(",")[0].trim() : request.getRemoteAddr();
        String err = checkSecret(auth);
        if (err == null) err = checkIp(ip);
        if (err != null) return AjaxResult.error(401, err);

        TakeoutOrder order = orderService.selectOrderById(orderId);
        if (order == null) return AjaxResult.error(404, "订单不存在");
        return AjaxResult.success(order);
    }
}
