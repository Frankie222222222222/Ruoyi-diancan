package com.ruoyi.takeout.security;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * C 端 token 权限映射（v3 - 2026-10-10）
 *
 * <p>按 takeout_user.role 字段注入 LoginUser.permissions，
 * 让 C 端 token 能通过 Spring Security 的 @PreAuthorize 校验。</p>
 *
 * <p>角色清单:
 * <ul>
 *   <li>{@code user}    — 普通顾客(下单/查自己订单/领券/评价/投诉)</li>
 *   <li>{@code kitchen} — 后厨(看本后厨待制订单 + 接受/出餐)</li>
 *   <li>{@code rider}   — 骑手(看抢单池 + 抢单 + 调度)</li>
 *   <li>{@code merchant}— 商家(管菜品/分类/看订单/评价/统计)</li>
 *   <li>{@code admin}   — 平台 admin(全权)</li>
 * </ul>
 *
 * @author ruoyi
 */
public final class RolePermissionMap
{
    private RolePermissionMap() {}

    private static final Set<String> P_USER = unmod(
        "takeout:order:list", "takeout:order:query", "takeout:order:add",
        "takeout:order:edit", "takeout:order:cancel", "takeout:order:export",
        "takeout:order:changeStatus",
        "takeout:rating:list", "takeout:rating:query",
        "takeout:complaint:list", "takeout:complaint:query",
        "takeout:coupon:list", "takeout:coupon:query",
        "takeout:coupon:receive", "takeout:coupon:userCoupon",
        "takeout:coupon:bestForOrder",
        "takeout:user:query", "takeout:user:list"
    );

    private static final Set<String> P_KITCHEN = unmod(
        "takeout:kitchen:list", "takeout:kitchen:accept",
        "takeout:kitchen:ready", "takeout:kitchen:query",
        "takeout:order:list", "takeout:order:query",
        "takeout:dish:query", "takeout:dish:list",
        "takeout:user:query"
    );

    private static final Set<String> P_RIDER = unmod(
        "takeout:rider:list", "takeout:rider:available",
        "takeout:rider:availableOrders", "takeout:rider:grabList",
        "takeout:rider:grab",
        "takeout:dispatch:list", "takeout:dispatch:query",
        "takeout:dispatch:accept", "takeout:dispatch:pickup",
        "takeout:dispatch:complete", "takeout:dispatch:cancel",
        "takeout:order:query",
        "takeout:riderLocation:report", "takeout:riderLocation:active",
        "takeout:user:query"
    );

    private static final Set<String> P_MERCHANT = unmod(
        "takeout:dish:list", "takeout:dish:query", "takeout:dish:add",
        "takeout:dish:edit", "takeout:dish:remove", "takeout:dish:changeStatus",
        "takeout:dish:adjustStock", "takeout:dish:topSales",
        "takeout:dishCategory:list", "takeout:dishCategory:query",
        "takeout:dishCategory:add", "takeout:dishCategory:edit", "takeout:dishCategory:remove",
        "takeout:order:list", "takeout:order:query", "takeout:order:changeStatus",
        "takeout:rating:list", "takeout:rating:query", "takeout:rating:reply",
        "takeout:statistics:list",
        "takeout:dineTable:list", "takeout:dineTable:query",
        "takeout:dineTable:add", "takeout:dineTable:edit", "takeout:dineTable:remove"
    );

    /** admin 在 sys_role 表里走 B 端权限,这里为空也无所谓 */
    private static final Set<String> P_ADMIN = unmod(
        "takeout:dish:list", "takeout:dish:query",
        "takeout:order:list", "takeout:order:query",
        "takeout:user:list", "takeout:user:query"
    );

    private static final Map<String, Set<String>> ROLE_PERMS = new HashMap<>();
    static
    {
        ROLE_PERMS.put("user", P_USER);
        ROLE_PERMS.put("kitchen", P_KITCHEN);
        ROLE_PERMS.put("rider", P_RIDER);
        ROLE_PERMS.put("merchant", P_MERCHANT);
        ROLE_PERMS.put("admin", P_ADMIN);
    }

    public static Set<String> get(String role)
    {
        if (role == null) return Collections.emptySet();
        Set<String> s = ROLE_PERMS.get(role.toLowerCase());
        return s == null ? Collections.emptySet() : s;
    }

    @SafeVarargs
    private static <T> Set<T> unmod(T... arr)
    {
        return Collections.unmodifiableSet(new HashSet<>(Arrays.asList(arr)));
    }
}
