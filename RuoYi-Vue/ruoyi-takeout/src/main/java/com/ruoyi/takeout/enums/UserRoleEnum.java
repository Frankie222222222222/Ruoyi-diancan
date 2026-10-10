package com.ruoyi.takeout.enums;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 外卖用户角色枚举（v2 - 2026-10-10）
 *
 * @author ruoyi
 */
public enum UserRoleEnum
{
    /** 顾客(默认) */
    USER("user", "顾客"),
    /** 后厨 */
    KITCHEN("kitchen", "后厨"),
    /** 骑手 */
    RIDER("rider", "骑手"),
    /** 管理员(走 RuoYi sys_user,本字段做冗余) */
    ADMIN("admin", "管理员");

    private final String code;
    private final String description;

    UserRoleEnum(String code, String description)
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

    public static UserRoleEnum of(String code)
    {
        if (code == null)
        {
            return null;
        }
        for (UserRoleEnum v : values())
        {
            if (v.code.equals(code))
            {
                return v;
            }
        }
        return null;
    }

    public static boolean isValid(String code)
    {
        return of(code) != null;
    }

    public static Map<String, String> toMap()
    {
        Map<String, String> m = new HashMap<>(8);
        for (UserRoleEnum v : values())
        {
            m.put(v.code, v.description);
        }
        return m;
    }

    /** 小程序启动分流的合法角色集合 */
    public static final List<String> ALL_CODES = Collections.unmodifiableList(
            Arrays.stream(values()).map(UserRoleEnum::getCode).collect(Collectors.toList()));
}
