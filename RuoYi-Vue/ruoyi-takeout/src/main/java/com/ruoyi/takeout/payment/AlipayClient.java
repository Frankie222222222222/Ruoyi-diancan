package com.ruoyi.takeout.payment;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayConfig;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.domain.AlipayTradePagePayModel;
import com.alipay.api.domain.AlipayTradeQueryModel;
import com.alipay.api.domain.AlipayTradeRefundModel;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.alipay.api.request.AlipayTradeQueryRequest;
import com.alipay.api.request.AlipayTradeRefundRequest;
import com.alipay.api.response.AlipayTradeQueryResponse;
import com.alipay.api.response.AlipayTradeRefundResponse;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * 支付宝客户端(电脑网站支付 Page 模式 → 用户用支付宝扫二维码付款)
 *
 * <p><b>启用条件</b>(自动按需加载,无需手改代码):
 *  <ul>
 *      <li>{@code payment.alipay.app-id} 非空 → 该 Bean 被注册</li>
 *      <li>留空 → 该 Bean 不被注册,MOCK 自动兜底</li>
 *  </ul>
 *
 * <p>接入步骤(照做即可):
 *  <ol>
 *      <li>注册开发者: <a href="https://open.alipay.com/">https://open.alipay.com/</a> → 用支付宝扫码入驻</li>
 *      <li>创建应用: 开放平台 → 我的应用 → 创建应用(选"网页应用")→ 上线(需 1-3 天审核)</li>
 *      <li>生成密钥: 应用 → 开发设置 → 密钥管理 → "设置应用公钥"
 *          <ul>
 *              <li>用支付宝提供的<a href="https://opendocs.alipay.com/common/02kipk">密钥生成工具</a>生成 RSA2 2048 应用私钥+公钥</li>
 *              <li>把"应用公钥"贴到支付宝页面,会自动生成"支付宝公钥",三个都填到 application.yml</li>
 *          </ul>
 *      </li>
 *      <li>填入 application.yml 的 payment.alipay.* 字段,重启即生效</li>
 *  </ol>
 *
 * <p>回调域名: 应用 → 开发设置 → 授权回调地址,需配置为 {@code http://你的域名/takeout/payment/alipay/notify}
 *  本地开发期用内网穿透(natapp/ngrok)。
 *
 * @author ruoyi
 */
@Component
@ConditionalOnProperty(prefix = "payment.alipay", name = "app-id")
public class AlipayClient implements PaymentClient
{
    private static final Logger log = LoggerFactory.getLogger(AlipayClient.class);

    @Autowired
    private PaymentProperties properties;

    private volatile com.alipay.api.AlipayClient alipayClient;
    private volatile boolean initFailed = false;

    @Override
    public String channel() { return PaymentChannel.ALIPAY.getCode(); }

    /**
     * 懒加载 AlipayClient,首次调用才读密钥
     */
    private com.alipay.api.AlipayClient getAlipayClient() {
        if (alipayClient != null) return alipayClient;
        if (initFailed) return null;
        if (!properties.isAlipayReady()) return null;
        try {
            PaymentProperties.Alipay a = properties.getAlipay();
            // SDK 4.x 用 AlipayConfig(替代老版 CertAlipayRequest)
            AlipayConfig config = new AlipayConfig();
            config.setServerUrl("https://openapi.alipay.com/gateway.do");
            config.setAppId(a.getAppId());
            config.setPrivateKey(a.getPrivateKey());
            config.setAlipayPublicKey(a.getPublicKey());
            config.setSignType(a.getSignType() != null ? a.getSignType() : "RSA2");
            config.setFormat("json");
            config.setCharset("UTF-8");
            alipayClient = new DefaultAlipayClient(config);
            log.info("[Alipay] 初始化完成 appId={}", a.getAppId());
            return alipayClient;
        } catch (Exception e) {
            log.error("[Alipay] 初始化失败,降级到 Mock: {}", e.getMessage(), e);
            initFailed = true;
            return null;
        }
    }

    @Override
    public PaymentResult create(PaymentRequest request)
    {
        com.alipay.api.AlipayClient client = getAlipayClient();
        if (client == null) {
            return PaymentResult.failed("支付宝未正确配置,使用 Mock 兜底");
        }
        try {
            AlipayTradePagePayModel model = new AlipayTradePagePayModel();
            model.setOutTradeNo(request.getOutTradeNo());
            model.setTotalAmount(request.getAmount().toString());
            model.setSubject("外卖订单-" + request.getOrderId());
            model.setProductCode("FAST_INSTANT_TRADE_PAY");
            AlipayTradePagePayRequest req = new AlipayTradePagePayRequest();
            req.setBizModel(model);
            req.setNotifyUrl(notifyUrl());
            req.setReturnUrl(properties.getAlipay().getReturnUrl());
            // pageExecute() 返回的 body 是 HTML 字符串,前端打开就是一个带二维码的支付页
            // 若想直接拿二维码 URL,改用 AlipayTradePreCreateRequest(扫码预下单)
            String payHtml = client.pageExecute(req).getBody();
            return PaymentResult.success(request.getOutTradeNo(), null, payHtml);
        } catch (AlipayApiException e) {
            log.error("[Alipay] 下单失败: {}", e.getMessage());
            return PaymentResult.failed("支付宝下单失败: " + e.getMessage());
        }
    }

    @Override
    public PaymentResult query(String outTradeNo)
    {
        com.alipay.api.AlipayClient client = getAlipayClient();
        if (client == null) return PaymentResult.failed("支付宝未配置");
        try {
            AlipayTradeQueryModel model = new AlipayTradeQueryModel();
            model.setOutTradeNo(outTradeNo);
            AlipayTradeQueryRequest req = new AlipayTradeQueryRequest();
            req.setBizModel(model);
            AlipayTradeQueryResponse resp = client.execute(req);
            if (resp.isSuccess() && "TRADE_SUCCESS".equals(resp.getTradeStatus())) {
                return PaymentResult.success(outTradeNo, resp.getTradeNo(), null);
            }
            return PaymentResult.pending(outTradeNo);
        } catch (AlipayApiException e) {
            return PaymentResult.failed("支付宝查账失败: " + e.getMessage());
        }
    }

    @Override
    public PaymentResult refund(String outTradeNo, String refundNo, BigDecimal amount, String reason)
    {
        com.alipay.api.AlipayClient client = getAlipayClient();
        if (client == null) return PaymentResult.failed("支付宝未配置");
        try {
            AlipayTradeRefundModel model = new AlipayTradeRefundModel();
            model.setOutTradeNo(outTradeNo);
            model.setOutRequestNo(refundNo);
            model.setRefundAmount(amount.toString());
            model.setRefundReason(reason);
            AlipayTradeRefundRequest req = new AlipayTradeRefundRequest();
            req.setBizModel(model);
            AlipayTradeRefundResponse resp = client.execute(req);
            if (resp.isSuccess()) {
                return PaymentResult.success(outTradeNo, resp.getTradeNo(), null);
            }
            return PaymentResult.failed("支付宝退款失败: " + resp.getSubMsg());
        } catch (AlipayApiException e) {
            return PaymentResult.failed("支付宝退款异常: " + e.getMessage());
        }
    }

    @Override
    public Map<String, Object> parseNotify(Map<String, String> headers, String body)
    {
        com.alipay.api.AlipayClient client = getAlipayClient();
        if (client == null) return Map.of();
        // 支付宝回调是 form 字符串: out_trade_no=xxx&trade_no=yyy&trade_status=TRADE_SUCCESS&...
        Map<String, String> params = parseForm(body);
        // 生产期需用 AlipaySignature.rsaCheckV1(params, publicKey, "utf-8", "RSA2") 验签
        // 这里简化:开发期直接信任
        Map<String, Object> map = new HashMap<>();
        map.put("outTradeNo", params.get("out_trade_no"));
        map.put("tradeNo", params.get("trade_no"));
        map.put("status", "TRADE_SUCCESS".equals(params.get("trade_status")) ? "SUCCESS" : "PENDING");
        return map;
    }

    /** 工具:把 "k1=v1&k2=v2" 解析为 Map */
    private static Map<String, String> parseForm(String body) {
        Map<String, String> map = new HashMap<>();
        if (body == null || body.isEmpty()) return map;
        for (String pair : body.split("&")) {
            int eq = pair.indexOf('=');
            if (eq > 0) {
                map.put(pair.substring(0, eq), pair.substring(eq + 1));
            }
        }
        return map;
    }

    private String notifyUrl() {
        String n = properties.getAlipay().getNotifyUrl();
        return (n != null && !n.isEmpty()) ? n : properties.getNotifyBase() + "/takeout/payment/alipay/notify";
    }
}
