package com.ruoyi.takeout.exception;

/**
 * ��Ʒҵ���쳣��ͳһ�� 1xxx ��ʾ��
 * <ul>
 *   <li>1006 - �����´��ڲ�Ʒ</li>
 *   <li>1007 - ��Ʒ�ѱ���������</li>
 *   <li>1008 - ��治��</li>
 * </ul>
 */
public class DishBizException extends RuntimeException {
    private final int code;

    public DishBizException(int code, String message) {
        super(message);
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
