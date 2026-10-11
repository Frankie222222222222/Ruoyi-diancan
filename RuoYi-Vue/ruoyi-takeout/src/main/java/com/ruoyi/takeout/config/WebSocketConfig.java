package com.ruoyi.takeout.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import com.ruoyi.takeout.websocket.KitchenOrderPushHandler;

/**
 * WebSocket 配置(2026-10-11)
 *
 * <p>注册 3 个推送端点:
 * <ul>
 *   <li>/ws/order — 顾客订单状态订阅</li>
 *   <li>/ws/kitchen — 后厨订单广播</li>
 *   <li>/ws/rider — 骑手派单推送</li>
 * </ul>
 *
 * 注意:鉴权在 nginx / 网关层做(IP白名单或短 token),此处简化放行。
 */
@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer
{
    @Autowired
    private KitchenOrderPushHandler pushHandler;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry)
    {
        registry.addHandler(pushHandler, "/ws/order", "/ws/kitchen", "/ws/rider")
                .setAllowedOriginPatterns("*");
    }
}
