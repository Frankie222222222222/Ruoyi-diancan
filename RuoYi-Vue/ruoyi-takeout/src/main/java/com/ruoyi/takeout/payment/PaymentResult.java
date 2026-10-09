package com.ruoyi.takeout.payment;

import java.util.Map;

/**
 * 支付结果统一封装
 *
 * <p>三种客户端(create/query/refund)返回统一格式,业务层无需关心渠道差异。
 *
 * @author ruoyi
 */
public class PaymentResult
{
    public enum Code { SUCCESS, FAILED, PENDING, INVALID }

    /** 业务结果 */
    private Code code;
    /** 渠道流水号(微信/支付宝返回,Mock 自动生成) */
    private String tradeNo;
    /** 商户订单号(回传) */
    private String outTradeNo;
    /** 给前端展示的二维码链接(扫码支付) / 跳转 URL(收银台) / 预支付标识(JSAPI) */
    private String prepayInfo;
    /** 错误描述(失败时) */
    private String errorMsg;
    /** 渠道返回的原始字段(调试用) */
    private Map<String, Object> raw;

    public Code getCode() { return code; }
    public void setCode(Code code) { this.code = code; }

    public String getTradeNo() { return tradeNo; }
    public void setTradeNo(String tradeNo) { this.tradeNo = tradeNo; }

    public String getOutTradeNo() { return outTradeNo; }
    public void setOutTradeNo(String outTradeNo) { this.outTradeNo = outTradeNo; }

    public String getPrepayInfo() { return prepayInfo; }
    public void setPrepayInfo(String prepayInfo) { this.prepayInfo = prepayInfo; }

    public String getErrorMsg() { return errorMsg; }
    public void setErrorMsg(String errorMsg) { this.errorMsg = errorMsg; }

    public Map<String, Object> getRaw() { return raw; }
    public void setRaw(Map<String, Object> raw) { this.raw = raw; }

    public static PaymentResult success(String outTradeNo, String tradeNo, String prepayInfo) {
        PaymentResult r = new PaymentResult();
        r.code = Code.SUCCESS;
        r.outTradeNo = outTradeNo;
        r.tradeNo = tradeNo;
        r.prepayInfo = prepayInfo;
        return r;
    }

    public static PaymentResult pending(String outTradeNo) {
        PaymentResult r = new PaymentResult();
        r.code = Code.PENDING;
        r.outTradeNo = outTradeNo;
        return r;
    }

    public static PaymentResult failed(String errorMsg) {
        PaymentResult r = new PaymentResult();
        r.code = Code.FAILED;
        r.errorMsg = errorMsg;
        return r;
    }
}
