package com.ruoyi.takeout.payment;

/**
 * 支付渠道枚举
 *
 * <p>开发期使用 MOCK 跑通业务流程,生产期配置 WECHAT/ALIPAY 自动接管。
 *
 * @author ruoyi
 */
public enum PaymentChannel
{
    WECHAT("WECHAT", "微信支付"),
    ALIPAY("ALIPAY", "支付宝"),
    MOCK("MOCK", "Mock 模拟(开发/测试用)");

    private final String code;
    private final String label;

    PaymentChannel(String code, String label) {
        this.code = code;
        this.label = label;
    }

    public String getCode() { return code; }
    public String getLabel() { return label; }

    public static PaymentChannel fromCode(String code) {
        if (code == null) return MOCK;
        for (PaymentChannel c : values()) {
            if (c.code.equalsIgnoreCase(code)) return c;
        }
        return MOCK;
    }
}
