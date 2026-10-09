package com.ruoyi.takeout.support;

import com.ruoyi.takeout.domain.*;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 测试用 Builder 工厂
 *
 * <p>提供常用 domain 对象的默认值,减少每个测试的样板代码。
 * 所有 setter 返回 this,支持链式调用 + 单字段覆盖。
 *
 * @author ruoyi
 */
public final class TestDataFactory
{
    private TestDataFactory() {}

    public static TakeoutOrder anOrder() {
        TakeoutOrder o = new TakeoutOrder();
        o.setOrderId(1001L);
        o.setOrderNo("ORD202610090001");
        o.setUserId(2001L);
        o.setMerchantId(3001L);
        o.setMerchantName("测试商家");
        o.setTotalAmount(new BigDecimal("50.00"));
        o.setDeliveryFee(new BigDecimal("5.00"));
        o.setActualAmount(new BigDecimal("50.00"));
        o.setStatus("0"); // 待支付
        o.setPayStatus("0");
        o.setReceiverName("张三");
        o.setReceiverPhone("13800138000");
        o.setAddress("浙江省杭州市西湖区文一路 1 号");
        // createTime/updateTime 由 BaseEntity 管理,测试不需要显式 set
        return o;
    }

    public static TakeoutOrderItem anOrderItem() {
        TakeoutOrderItem item = new TakeoutOrderItem();
        item.setItemId(9001L);
        item.setOrderId(1001L);
        item.setDishId(5001L);
        item.setDishName("红烧肉");
        item.setQuantity(2);
        item.setPrice(new BigDecimal("25.00"));
        return item;
    }

    public static TakeoutRider aRider() {
        TakeoutRider r = new TakeoutRider();
        r.setRiderId(7001L);
        r.setName("骑手A");
        r.setPhone("13900139000");
        r.setStatus("0"); // 接单中
        r.setCity("杭州");
        r.setRating(new BigDecimal("4.8"));
        r.setTotalDeliveries(50);
        r.setTotalIncome(new BigDecimal("2000.00"));
        return r;
    }

    public static TakeoutDispatch aDispatch() {
        TakeoutDispatch d = new TakeoutDispatch();
        d.setDispatchId(8001L);
        d.setOrderId(1001L);
        d.setRiderId(7001L);
        d.setDispatchType("0");
        d.setStatus("1"); // 已接单
        d.setAssignTime(new Date());
        return d;
    }

    public static Dish aDish() {
        Dish d = new Dish();
        d.setDishId(5001L);
        d.setDishName("红烧肉");
        d.setPrice(new BigDecimal("25.00"));
        d.setStock(100);
        d.setSales(0);
        d.setStatus("1");
        return d;
    }
}
