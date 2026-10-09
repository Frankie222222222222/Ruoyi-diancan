package com.ruoyi.takeout.enums;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 订单状态枚举
 * <p>
 * 对应字典：takeout_order_status
 * 数据库字段：{@code takeout_order.status}
 * 状态机：见 {@link #NEXT_ALLOWED}
 * </p>
 *
 * @author ruoyi
 */
public enum OrderStatusEnum
{
    /** 0 待支付 */
    UNPAID("0", "待支付"),

    /** 1 已支付 */
    PAID("1", "已支付"),

    /** 2 商家接单 */
    ACCEPTED("2", "商家接单"),

    /** 3 配送中 */
    DELIVERING("3", "配送中"),

    /** 4 已送达 */
    DELIVERED("4", "已送达"),

    /** 5 已完成 */
    COMPLETED("5", "已完成"),

    /** 6 已取消 */
    CANCELLED("6", "已取消"),

    /** 7 已退款 */
    REFUNDED("7", "已退款");

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
     * 状态机的合法后继（用于后台手动改状态时校验）
     * <ul>
     *   <li>UNPAID      -> PAID / CANCELLED</li>
     *   <li>PAID        -> ACCEPTED / REFUNDED / CANCELLED</li>
     *   <li>ACCEPTED    -> DELIVERING / REFUNDED</li>
     *   <li>DELIVERING  -> DELIVERED</li>
     *   <li>DELIVERED   -> COMPLETED</li>
     *   <li>COMPLETED   -> (终态)</li>
     *   <li>CANCELLED   -> (终态)</li>
     *   <li>REFUNDED    -> (终态)</li>
     * </ul>
     */
    private static final Map<String, List<String>> NEXT_ALLOWED = new HashMap<>();
    static
    {
        NEXT_ALLOWED.put(UNPAID.code,     Arrays.asList(PAID.code, CANCELLED.code));
        NEXT_ALLOWED.put(PAID.code,       Arrays.asList(ACCEPTED.code, REFUNDED.code, CANCELLED.code));
        NEXT_ALLOWED.put(ACCEPTED.code,   Arrays.asList(DELIVERING.code, REFUNDED.code));
        NEXT_ALLOWED.put(DELIVERING.code, Arrays.asList(DELIVERED.code));
        NEXT_ALLOWED.put(DELIVERED.code,  Arrays.asList(COMPLETED.code));
        NEXT_ALLOWED.put(COMPLETED.code,  java.util.Collections.emptyList());
        NEXT_ALLOWED.put(CANCELLED.code,  java.util.Collections.emptyList());
        NEXT_ALLOWED.put(REFUNDED.code,   java.util.Collections.emptyList());
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
        return NEXT_ALLOWED.getOrDefault(from, java.util.Collections.emptyList())
                .contains(to);
    }

    /**
     * 提供给前端"可用的下一个状态"列表（用于改状态弹窗下拉）
     */
    public static List<Map<String, String>> nextOptions(String from)
    {
        List<String> codes = NEXT_ALLOWED.getOrDefault(from, java.util.Collections.emptyList());
        return codes.stream().map(c -> {
            OrderStatusEnum e = of(c);
            Map<String, String> m = new HashMap<>(2);
            m.put("code", e.code);
            m.put("description", e.description);
            return m;
        }).collect(Collectors.toList());
    }
}
