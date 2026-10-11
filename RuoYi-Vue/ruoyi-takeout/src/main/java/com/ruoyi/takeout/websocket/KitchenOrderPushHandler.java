package com.ruoyi.takeout.websocket;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * 订单状态 WebSocket 推送(2026-10-11)
 *
 * <p>订阅模式(URL query):
 * <ul>
 *   <li>/ws/order?orderId=xxx       — 顾客订阅某个订单</li>
 *   <li>/ws/kitchen                 — 后厨订阅(收到新订单/取消通知)</li>
 *   <li>/ws/rider?riderId=xxx       — 骑手订阅(收到抢单/派单通知)</li>
 * </ul>
 *
 * 推送消息 JSON: { type: "ORDER_UPDATE", orderId, status, kitchenStatus, riderStatus, ts }
 */
@Component
public class KitchenOrderPushHandler extends TextWebSocketHandler
{
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(KitchenOrderPushHandler.class);
    private static final ObjectMapper MAPPER = new ObjectMapper();
    // orderId -> session
    private static final Map<String, WebSocketSession> ORDER_SESSIONS = new ConcurrentHashMap<>();
    // 简易集合,记录订阅了 /ws/kitchen 的所有 session
    private static final java.util.Set<WebSocketSession> KITCHEN_SESSIONS = ConcurrentHashMap.newKeySet();
    // riderId -> session
    private static final Map<String, WebSocketSession> RIDER_SESSIONS = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session)
    {
        String uri = session.getUri() == null ? "" : session.getUri().toString();
        if (uri.contains("/ws/kitchen")) {
            KITCHEN_SESSIONS.add(session);
            log.info("[WS] kitchen session opened: {}", session.getId());
        } else if (uri.contains("/ws/rider")) {
            String riderId = extractQuery(uri, "riderId");
            if (riderId != null) RIDER_SESSIONS.put(riderId, session);
            log.info("[WS] rider session opened: riderId={} sid={}", riderId, session.getId());
        } else if (uri.contains("/ws/order")) {
            String orderId = extractQuery(uri, "orderId");
            if (orderId != null) ORDER_SESSIONS.put(orderId, session);
            log.info("[WS] order session opened: orderId={} sid={}", orderId, session.getId());
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status)
    {
        KITCHEN_SESSIONS.remove(session);
        ORDER_SESSIONS.values().removeIf(s -> s.getId().equals(session.getId()));
        RIDER_SESSIONS.values().removeIf(s -> s.getId().equals(session.getId()));
    }

    /** 推送订单状态变更(顾客端 + 后厨端都能收到) */
    public void pushOrder(String orderId, String status, String kitchenStatus, String riderStatus)
    {
        try {
            Map<String, Object> payload = Map.of(
                "type", "ORDER_UPDATE",
                "orderId", orderId,
                "status", status == null ? "" : status,
                "kitchenStatus", kitchenStatus == null ? "" : kitchenStatus,
                "riderStatus", riderStatus == null ? "" : riderStatus,
                "ts", System.currentTimeMillis()
            );
            String text = MAPPER.writeValueAsString(payload);
            // 顾客
            WebSocketSession orderSession = ORDER_SESSIONS.get(orderId);
            if (orderSession != null && orderSession.isOpen()) {
                orderSession.sendMessage(new TextMessage(text));
            }
            // 后厨(任意订单变动都推,后厨自己 filter)
            for (WebSocketSession s : KITCHEN_SESSIONS) {
                if (s.isOpen()) s.sendMessage(new TextMessage(text));
            }
        } catch (Exception e) {
            log.warn("[WS] pushOrder failed: {}", e.getMessage());
        }
    }

    /** 推送新订单给后厨 */
    public void pushNewOrderToKitchen(String orderId, String orderNo, String totalAmount)
    {
        try {
            Map<String, Object> payload = Map.of(
                "type", "NEW_ORDER",
                "orderId", orderId,
                "orderNo", orderNo,
                "totalAmount", totalAmount,
                "ts", System.currentTimeMillis()
            );
            String text = MAPPER.writeValueAsString(payload);
            for (WebSocketSession s : KITCHEN_SESSIONS) {
                if (s.isOpen()) s.sendMessage(new TextMessage(text));
            }
        } catch (Exception e) {
            log.warn("[WS] pushNewOrderToKitchen failed: {}", e.getMessage());
        }
    }

    /** 推送派单通知给特定骑手 */
    public void pushDispatchToRider(String riderId, String orderId, String dispatchId)
    {
        try {
            Map<String, Object> payload = Map.of(
                "type", "DISPATCH_NEW",
                "orderId", orderId,
                "dispatchId", dispatchId,
                "ts", System.currentTimeMillis()
            );
            String text = MAPPER.writeValueAsString(payload);
            WebSocketSession s = RIDER_SESSIONS.get(riderId);
            if (s != null && s.isOpen()) s.sendMessage(new TextMessage(text));
        } catch (Exception e) {
            log.warn("[WS] pushDispatchToRider failed: {}", e.getMessage());
        }
    }

    private static String extractQuery(String uri, String key)
    {
        int idx = uri.indexOf('?');
        if (idx < 0) return null;
        for (String part : uri.substring(idx + 1).split("&")) {
            int eq = part.indexOf('=');
            if (eq > 0 && part.substring(0, eq).equals(key)) {
                return part.substring(eq + 1);
            }
        }
        return null;
    }
}
