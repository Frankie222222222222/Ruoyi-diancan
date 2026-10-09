package com.ruoyi.takeout.payment;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 支付网关配置属性
 *
 * <p>对应 application.yml 里的 {@code payment.*} 配置块。
 * 三个渠道共用一套配置:
 *  <ul>
 *      <li>密钥全部留空 → 自动用 MOCK 兜底,开发/演示畅通无阻</li>
 *      <li>填上 WECHAT.* → 微信支付 V3 启用</li>
 *      <li>填上 ALIPAY.* → 支付宝启用</li>
 *  </ul>
 *
 * @author ruoyi
 */
@Component
@ConfigurationProperties(prefix = "payment")
public class PaymentProperties
{
    /** 默认渠道: MOCK / WECHAT / ALIPAY */
    private String defaultChannel = "MOCK";
    /** 回调地址前缀 */
    private String notifyBase = "http://localhost:8080";

    private Wechat wechat = new Wechat();
    private Alipay alipay = new Alipay();

    public String getDefaultChannel() { return defaultChannel; }
    public void setDefaultChannel(String defaultChannel) { this.defaultChannel = defaultChannel; }

    public String getNotifyBase() { return notifyBase; }
    public void setNotifyBase(String notifyBase) { this.notifyBase = notifyBase; }

    public Wechat getWechat() { return wechat; }
    public void setWechat(Wechat wechat) { this.wechat = wechat; }

    public Alipay getAlipay() { return alipay; }
    public void setAlipay(Alipay alipay) { this.alipay = alipay; }

    /**
     * 微信支付是否已配置(密钥非空即为真)
     */
    public boolean isWechatReady() {
        return wechat != null
                && notBlank(wechat.getMchId())
                && notBlank(wechat.getApiV3Key())
                && notBlank(wechat.getMchSerialNo())
                && notBlank(wechat.getPrivateKeyPath());
    }

    /**
     * 支付宝是否已配置(密钥非空即为真)
     */
    public boolean isAlipayReady() {
        return alipay != null
                && notBlank(alipay.getAppId())
                && notBlank(alipay.getPrivateKey())
                && notBlank(alipay.getPublicKey());
    }

    private static boolean notBlank(String s) { return s != null && !s.trim().isEmpty(); }

    // ==================== 内部配置类 ====================

    public static class Wechat {
        private String mchId;
        private String apiV3Key;
        private String mchSerialNo;
        private String privateKeyPath;
        private String appId;

        public String getMchId() { return mchId; }
        public void setMchId(String mchId) { this.mchId = mchId; }

        public String getApiV3Key() { return apiV3Key; }
        public void setApiV3Key(String apiV3Key) { this.apiV3Key = apiV3Key; }

        public String getMchSerialNo() { return mchSerialNo; }
        public void setMchSerialNo(String mchSerialNo) { this.mchSerialNo = mchSerialNo; }

        public String getPrivateKeyPath() { return privateKeyPath; }
        public void setPrivateKeyPath(String privateKeyPath) { this.privateKeyPath = privateKeyPath; }

        public String getAppId() { return appId; }
        public void setAppId(String appId) { this.appId = appId; }
    }

    public static class Alipay {
        private String appId;
        private String privateKey;
        private String publicKey;
        private String signType = "RSA2";
        private String notifyUrl;
        private String returnUrl;

        public String getAppId() { return appId; }
        public void setAppId(String appId) { this.appId = appId; }

        public String getPrivateKey() { return privateKey; }
        public void setPrivateKey(String privateKey) { this.privateKey = privateKey; }

        public String getPublicKey() { return publicKey; }
        public void setPublicKey(String publicKey) { this.publicKey = publicKey; }

        public String getSignType() { return signType; }
        public void setSignType(String signType) { this.signType = signType; }

        public String getNotifyUrl() { return notifyUrl; }
        public void setNotifyUrl(String notifyUrl) { this.notifyUrl = notifyUrl; }

        public String getReturnUrl() { return returnUrl; }
        public void setReturnUrl(String returnUrl) { this.returnUrl = returnUrl; }
    }
}
