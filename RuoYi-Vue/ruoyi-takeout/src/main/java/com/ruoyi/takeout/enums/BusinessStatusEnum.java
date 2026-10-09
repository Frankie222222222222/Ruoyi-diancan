package com.ruoyi.takeout.enums;

import java.util.HashMap;
import java.util.Map;

/**
 * �����̼�Ӫҵ״̬ö��
 * <p>
 * ��Ӧ�ֵ䣺takeout_business_status
 * ���ݿ��ֶΣ�{@code takeout_merchant.status}
 * </p>
 *
 * @author ruoyi
 */
public enum BusinessStatusEnum
{
    /** Ӫҵ�� */
    OPEN("0", "Ӫҵ��"),

    /** �Ѵ��� */
    CLOSED("1", "�Ѵ���");

    private final String code;
    private final String description;

    BusinessStatusEnum(String code, String description)
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
     * ���� code ����ö�٣��Ҳ������� null�����÷����д�����
     */
    public static BusinessStatusEnum of(String code)
    {
        if (code == null)
        {
            return null;
        }
        for (BusinessStatusEnum v : values())
        {
            if (v.code.equals(code))
            {
                return v;
            }
        }
        return null;
    }

    /**
     * У�� code �Ƿ�Ϸ������� Service ��У�飩
     */
    public static boolean isValid(String code)
    {
        return of(code) != null;
    }

    /**
     * �ṩ��ǰ�˵��ֵ��б���key=code, value=description��
     */
    public static Map<String, String> toMap()
    {
        Map<String, String> map = new HashMap<>(2);
        for (BusinessStatusEnum v : values())
        {
            map.put(v.code, v.description);
        }
        return map;
    }
}