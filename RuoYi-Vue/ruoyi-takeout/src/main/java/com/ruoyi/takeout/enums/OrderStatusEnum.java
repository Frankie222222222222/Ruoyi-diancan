package com.ruoyi.takeout.enums;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 订单状态枚举
 * <p>
 * 对应字典：takeout_order_status
 * 数据库字段：{@code takeout_order.status}
 * </p>
 *
 * <h3>v2 扩展（2026-10-10）</h3>
 * 拆分原 {@code ACCEPTED(2)} 为"后厨制作中 / 出餐待接"两步：
 * <ul>
 *   <li>{@code MAKING(2a)}  — 后厨正在制作</li>
 *   <li>{@code READY(2b)}   — 出餐完毕，待骑手接单</li>
 *   <li>{@code ACCEPTED(2)} 旧值保留兼容老数据；语义改为"已派单到后厨（待制作）"，
 *       一键可转 MAKING</li>
 * </ul>
 *
 * <h3>v3 堂食扩展（2026-10-10）</h3>
 * 堂食订单独立状态机（在主流程上新增一档 DRAFT）：
 * <ul>
 *   <li>{@code DRAFT(0a)} — 堂食专用：顾客点菜中未支付（可继续 addItem）</li>
 * </ul>
 *
 * <h3>状态机</h3>
 * <pre>
 *   UNPAID ──► PAID ──► MAKING ──► READY ──► DELIVERING ──► DELIVERED ──► COMPLETED
 *     │         │         │          │
 *     └─►CANCELLED ◄──┘     └─►REFUNDED ◄──┘
 *
 *   DRAFT(0a) ──[支付]──► PAID(1)    (堂食: 重复买加菜,每单独立支付)
 *     │
 *     └─[30min 超时/顾客取消]──► CANCELLED(6)
 * </pre>
 *
 * @author ruoyi
 */
public enum OrderStatusEnum
{
    /** 0 待支付 */
    UNPAID("0", "待支付"),

    /**
     * 0a 堂食点菜中（v3 - 2026-10-10）
     * <p>仅 {@code order_type=1} 堂食订单使用。顾客可继续 addItem 加菜；
     * 触发"立即结账"→ PAID(1)；30 分钟无动作或顾客手动取消 → CANCELLED(6)。</p>
     */
    DRAFT("0a", "堂食点菜中"),

    /** 1 已支付 */
    PAID("1", "已支付"),

    /**
     * 2 商家接单（v2 兼容旧值）
     * <p>语义已改为"已派单到后厨，待制作"。后厨首次接单时一键转 MAKING(2a)。</p>
     */
    @Deprecated
    ACCEPTED("2", "商家接单"),

    /**
     * 2a 后厨制作中
     */
    MAKING("2a", "后厨制作中"),

    /**
     * 2b 出餐完毕，待骑手接单
     * <p>骑手抢单台只显示此状态。READY → DELIVERING 由骑手触发。</p>
     */
    READY("2b", "出餐待接"),

    /** 3 配送中 */
    DELIVERING("3", "配送中"),

    /** 4 已送达 */
    DELIVERED("4", "已送达"),

    /** 5 已完成 */
    COMPLETED("5", "已完成"),

    /** 6 已取消 */
    CANCELLED("6", "已取消"),

    /** 7 已退款 */
    REFUNDED("7", "已退款"),

    /**
     * 8 堂食已上桌（v3 - 2026-10-10）
     * <p>仅 {@code order_type=1} 堂食订单。READY → DONE 由顾客/服务员点"已上桌"触发,
     * 触发后桌台可释放回空闲。不走 DELIVERING/DELIVERED（无骑手）。</p>
     */
    DONE("8", "已上桌");

    private final String code;
    private final String description;

    OrderStatusEnum(String code, String description)
    {
        this.code = code;
        this.description = description;
    }

    public String getCode()
    {
        return code;
    }

    public String getDescription()
    {
        return description;
    }

    /**
     * 根据 code 查找枚举
     */
    public static OrderStatusEnum of(String code)
    {
        if (code == null)
        {
            return null;
        }
        for (OrderStatusEnum v : values())
        {
            if (v.code.equals(code))
            {
                return v;
            }
        }
        return null;
    }

    /**
     * 校验 code 是否合法
     */
    public static boolean isValid(String code)
    {
        return of(code) != null;
    }

    /**
     * 提供给前端的字典列表
     */
    public static Map<String, String> toMap()
    {
        Map<String, String> map = new HashMap<>(16);
        for (OrderStatusEnum v : values())
        {
            map.put(v.code, v.description);
        }
        return map;
    }

    /**
     * v2 状态机的合法后继
     * <ul>
     *   <li>UNPAID     → PAID / CANCELLED</li>
     *   <li>PAID       → MAKING / REFUNDED / CANCELLED  （v2: 移除 ACCEPTED 直接转 MAKING）</li>
     *   <li>ACCEPTED   → MAKING / REFUNDED  （v2: 旧值一键升级）</li>
     *   <li>MAKING     → READY / REFUNDED  （v2: 后厨出餐完毕）</li>
     *   <li>READY      → DELIVERING / REFUNDED  （v2: 骑手接单）</li>
     *   <li>DELIVERING → DELIVERED</li>
     *   <li>DELIVERED  → COMPLETED</li>
     *   <li>COMPLETED  → (终态)</li>
     *   <li>CANCELLED  → (终态)</li>
     *   <li>REFUNDED   → (终态)</li>
     * </ul>
     */
    private static final Map<String, List<String>> NEXT_ALLOWED = new HashMap<>();
    static
    {
        // 堂食点菜中: 可支付 或 取消
        NEXT_ALLOWED.put(DRAFT.code,      Arrays.asList(PAID.code, CANCELLED.code));
        NEXT_ALLOWED.put(UNPAID.code,     Arrays.asList(PAID.code, CANCELLED.code));
        NEXT_ALLOWED.put(PAID.code,       Arrays.asList(MAKING.code, REFUNDED.code, CANCELLED.code));
        NEXT_ALLOWED.put(ACCEPTED.code,   Arrays.asList(MAKING.code, REFUNDED.code));
        NEXT_ALLOWED.put(MAKING.code,     Arrays.asList(READY.code, REFUNDED.code));
        // 堂食订单: READY → DONE (无骑手)
        // 堂食订单: PAID → MAKING → READY → DONE
        NEXT_ALLOWED.put(READY.code,      Arrays.asList(DELIVERING.code, DONE.code, REFUNDED.code));
        NEXT_ALLOWED.put(DELIVERING.code, Arrays.asList(DELIVERED.code));
        NEXT_ALLOWED.put(DELIVERED.code,  Arrays.asList(COMPLETED.code));
        NEXT_ALLOWED.put(DONE.code,       Collections.emptyList());
        NEXT_ALLOWED.put(COMPLETED.code,  Collections.emptyList());
        NEXT_ALLOWED.put(CANCELLED.code,  Collections.emptyList());
        NEXT_ALLOWED.put(REFUNDED.code,   Collections.emptyList());
    }

    /**
     * 状态机校验：from -> to 是否合法
     */
    public static boolean canTransition(String from, String to)
    {
        if (!isValid(from) || !isValid(to))
        {
            return false;
        }
        return NEXT_ALLOWED.getOrDefault(from, Collections.emptyList()).contains(to);
    }

    /**
     * 提供给前端"可用的下一个状态"列表（用于改状态弹窗下拉）
     */
    public static List<Map<String, String>> nextOptions(String from)
    {
        List<String> codes = NEXT_ALLOWED.getOrDefault(from, Collections.emptyList());
        return codes.stream().map(c -> {
            OrderStatusEnum e = of(c);
            Map<String, String> m = new HashMap<>(2);
            m.put("code", e.code);
            m.put("description", e.description);
            return m;
        }).collect(Collectors.toList());
    }

    /**
     * v2: 骑手可见订单的 status 集合
     */
    public static final List<String> RIDER_VISIBLE_STATUSES = Collections.singletonList(READY.code);

    /**
     * v2: 后厨可见订单的 status 集合（制作中 + 旧值兼容）
     */
    public static final List<String> KITCHEN_VISIBLE_STATUSES = Arrays.asList(PAID.code, ACCEPTED.code, MAKING.code);

    /**
     * v2: 判断订单是否对骑手可见（可抢单）
     */
    public static boolean isRiderVisible(String status)
    {
        return RIDER_VISIBLE_STATUSES.contains(status);
    }

    /**
     * v2: 判断订单是否对后厨可见（待制作 / 制作中）
     */
    public static boolean isKitchenVisible(String status)
    {
        return KITCHEN_VISIBLE_STATUSES.contains(status);
    }
}
