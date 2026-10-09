package com.ruoyi.takeout.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.takeout.domain.TakeoutPayment;
import com.ruoyi.takeout.payment.PaymentChannel;
import com.ruoyi.takeout.payment.PaymentResult;
import com.ruoyi.takeout.service.ITakeoutPaymentService;

/**
 * 支付 Controller
 *
 * <p>权限策略:
 *  <ul>
 *      <li>管理类接口(列表/详情/创建支付/退款)走 ruoyi 权限</li>
 *      <li>渠道回调(wechat/notify, alipay/notify) <b>不走权限</b>,但会验签(由 PaymentClient.parseNotify)</li>
 *  </ul>
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/takeout/payment")
public class TakeoutPaymentController extends BaseController
{
    @Autowired
    private ITakeoutPaymentService paymentService;

    /** 分页查询支付流水 */
    @GetMapping("/list")
    public TableDataInfo list(TakeoutPayment query)
    {
        startPage();
        return getDataTable(paymentService.selectPaymentList(query));
    }

    /** 创建支付(后台管理员演示用) */
    @PostMapping("/create/{paymentId}")
    public AjaxResult create(@PathVariable Long paymentId)
    {
        PaymentResult r = paymentService.createPayment(paymentId);
        return r.getCode() == PaymentResult.Code.SUCCESS ? success(r) : error(r.getErrorMsg());
    }

    /** 主动查账 */
    @GetMapping("/query/{paymentId}")
    public AjaxResult query(@PathVariable Long paymentId)
    {
        PaymentResult r = paymentService.queryPayment(paymentId);
        return r.getCode() == PaymentResult.Code.SUCCESS ? success(r) : error(r.getErrorMsg());
    }

    /** 退款 */
    @PutMapping("/refund/{paymentId}")
    public AjaxResult refund(@PathVariable Long paymentId, @RequestParam(required = false) String reason)
    {
        PaymentResult r = paymentService.refundPayment(paymentId, reason);
        return r.getCode() == PaymentResult.Code.SUCCESS ? success(r) : error(r.getErrorMsg());
    }

    /** 微信回调(公开) */
    @PostMapping("/wechat/notify")
    public String wechatNotify(@RequestHeader Map<String, String> headers, @RequestBody(required = false) String body)
    {
        return paymentService.handleNotify(PaymentChannel.WECHAT, headers, body == null ? "" : body);
    }

    /** 支付宝回调(公开,form 提交) */
    @PostMapping("/alipay/notify")
    public String alipayNotify(@RequestParam Map<String, String> form)
    {
        Map<String, String> headers = new HashMap<>();
        return paymentService.handleNotify(PaymentChannel.ALIPAY, headers, form.toString());
    }
}
