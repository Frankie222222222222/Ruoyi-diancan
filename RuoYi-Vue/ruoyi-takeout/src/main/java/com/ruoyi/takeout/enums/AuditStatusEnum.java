package com.ruoyi.takeout.enums;

import java.util.HashMap;
import java.util.Map;

/**
 * �����̼���פ���״̬ö��
 * <p>
 * ��Ӧ�ֵ䣺takeout_audit_status
 * ���ݿ��ֶΣ�{@code takeout_merchant.audit_status}
 * �µ�ģ����У�飺�� {@link #APPROVED} ���µ�
 * </p>
 *
 * @author ruoyi
 */
public enum AuditStatusEnum
{
    /** ����� */
    PENDING("0", "�����"),

    /** ͨ�� */
    APPROVED("1", "ͨ��"),

    /** ���� */
    REJECTED("2", "����");

    private final String code;
    private final String description;

    AuditStatusEnum(String code, String description)
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
     * ���� code ����ö��
     */
    public static AuditStatusEnum of(String code)
    {
        if (code == null)
        {
            return null;
        }
        for (AuditStatusEnum v : values())
        {
            if (v.code.equals(code))
            {
                return v;
            }
        }
        return null;
    }

    /**
     * У�� code �Ƿ�Ϸ�
     */
    public static boolean isValid(String code)
    {
        return of(code) != null;
    }

    /**
     * У���̼��Ƿ������µ��������ͨ�����̼ҿ��µ���
     */
    public static boolean canOrder(String code)
    {
        return APPROVED.code.equals(code);
    }

    /**
     * �ṩ��ǰ�˵��ֵ��б�
     */
    public static Map<String, String> toMap()
    {
        Map<String, String> map = new HashMap<>(4);
        for (AuditStatusEnum v : values())
        {
            map.put(v.code, v.description);
        }
        return map;
    }
}