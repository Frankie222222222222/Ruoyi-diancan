package com.ruoyi.web.controller.takeout;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.takeout.domain.Dish;
import com.ruoyi.takeout.domain.DishCategory;
import com.ruoyi.takeout.domain.TakeoutComplaint;
import com.ruoyi.takeout.domain.TakeoutCoupon;
import com.ruoyi.takeout.domain.TakeoutMerchant;
import com.ruoyi.takeout.domain.TakeoutOrder;
import com.ruoyi.takeout.domain.TakeoutOrderItem;
import com.ruoyi.takeout.domain.TakeoutRating;
import com.ruoyi.takeout.domain.TakeoutRider;
import com.ruoyi.takeout.domain.TakeoutDispatch;
import com.ruoyi.takeout.domain.TakeoutUser;
import com.ruoyi.takeout.service.ITakeoutComplaintService;
import com.ruoyi.takeout.service.ITakeoutCouponService;
import com.ruoyi.takeout.service.ITakeoutDispatchService;
import com.ruoyi.takeout.service.ITakeoutMerchantService;
import com.ruoyi.takeout.service.ITakeoutOrderService;
import com.ruoyi.takeout.service.ITakeoutRatingService;
import com.ruoyi.takeout.service.ITakeoutRiderService;
import com.ruoyi.takeout.service.ITakeoutUserService;
import com.ruoyi.common.annotation.Anonymous;
import com.ruoyi.takeout.service.IDishService;
import com.ruoyi.takeout.service.IDishCategoryService;
import com.ruoyi.takeout.mapper.TakeoutDispatchMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 测试数据生成器（每个表 5~10 条 demo 数据）
 * 仅供本地调试使用，生产环境请删除该 Controller。
 * 路径：/takeout/testData/generate
 *
 * @author ruoyi
 */
@Anonymous
@RestController
@RequestMapping("/takeout/testData")
public class TakeoutTestDataController extends BaseController
{
    @Autowired private ITakeoutUserService userService;
    @Autowired private ITakeoutMerchantService merchantService;
    @Autowired private ITakeoutRiderService riderService;
    @Autowired private ITakeoutOrderService orderService;
    @Autowired private ITakeoutDispatchService dispatchService;
    @Autowired private ITakeoutRatingService ratingService;
    @Autowired private ITakeoutCouponService couponService;
    @Autowired private ITakeoutComplaintService complaintService;
    @Autowired private IDishService dishService;
    @Autowired private IDishCategoryService dishCategoryService;
    @Autowired private TakeoutDispatchMapper dispatchMapper;
    @Autowired private JdbcTemplate jdbcTemplate;

    private static final Random R = new Random(2026);
    private static final int PER_TABLE = 8;

    @GetMapping("/generate")
    public AjaxResult generate(@RequestParam(defaultValue = "false") boolean clean) {
        long t0 = System.currentTimeMillis();

        // 0. 如果传了 clean=true, 用 SQL 硬清表(避免各 service delete 方法签名差异)
        if (clean) {
            String[] tables = {
                "takeout_dispatch", "takeout_dish", "takeout_dish_category",
                "takeout_rating", "takeout_coupon", "takeout_complaint",
                "takeout_order_item", "takeout_order",
                "takeout_rider", "takeout_merchant", "takeout_user"
            };
            for (String tbl : tables) {
                try { jdbcTemplate.execute("DELETE FROM " + tbl); } catch (Exception e) { /* 表可能不存在, 忽略 */ }
            }
        }

        // 1. C 端用户
        List<TakeoutUser> users = new ArrayList<>();
        String[] nicknames = {"小张", "阿龙", "Tom", "Lily", "李雷", "韩梅梅", "老王", "Tony", "Lucy", "阿明"};
        String[] phones = {"138", "139", "150", "151", "152", "158", "186", "187", "188", "199"};
        String[] cities = {"上海", "北京", "深圳", "广州", "杭州"};
        String[] genders = {"1", "2", "0"};
        for (int i = 0; i < PER_TABLE; i++) {
            TakeoutUser u = new TakeoutUser();
            u.setNickname(nicknames[i % nicknames.length] + (i + 1));
            u.setPhone(phones[i % phones.length] + String.format("%08d", R.nextInt(100000000)));
            u.setGender(genders[R.nextInt(genders.length)]);
            u.setCity(cities[R.nextInt(cities.length)]);
            u.setTotalOrders(R.nextInt(50) + 1);
            u.setTotalSpend(BigDecimal.valueOf(R.nextInt(5000) + 100));
            u.setStatus("0");
            u.setDelFlag("0");
            userService.registerUser(u);
            users.add(u);
        }

        // 2. 商家
        List<TakeoutMerchant> merchants = new ArrayList<>();
        String[] mNames = {"肯德基", "麦当劳", "海底捞", "星巴克", "喜茶", "瑞幸咖啡", "杨国福", "正新鸡排", "霸王茶姬", "必胜客"};
        String[] contacts = {"张经理", "王店长", "李老板", "赵总", "陈经理"};
        for (int i = 0; i < PER_TABLE; i++) {
            TakeoutMerchant m = new TakeoutMerchant();
            m.setMerchantName(mNames[i % mNames.length] + "（测试" + (i + 1) + "店）");
            m.setContactName(contacts[i % contacts.length]);
            m.setContactPhone(phones[R.nextInt(phones.length)] + String.format("%08d", R.nextInt(100000000)));
            m.setAddress(cities[R.nextInt(cities.length)] + "市某区某路 " + (R.nextInt(900) + 100) + " 号");
            m.setStatus("0");
            m.setAuditStatus("1");
            m.setAuditRemark("测试数据-自动审核通过");
            m.setDelFlag("0");
            m.setRemark("由测试数据生成器创建");
            merchantService.insertMerchant(m);
            merchants.add(m);
        }

        // 2.5 菜品分类（每个商家挂 3 个分类）
        List<DishCategory> categories = new ArrayList<>();
        String[] catNames = {"热销推荐", "招牌主食", "小食饮品", "超值套餐"};
        for (int i = 0; i < catNames.length; i++) {
            DishCategory c = new DishCategory();
            c.setCategoryName(catNames[i]);
            c.setSortOrder(i + 1);
            c.setStatus("1");
            dishCategoryService.insert(c);
            categories.add(c);
        }

        // 2.6 菜品（每个商家 4-5 个菜，必带 merchantId）
        List<Dish> dishes = new ArrayList<>();
        String[] dishNames = {"香辣鸡腿堡", "黄焖鸡米饭", "麻辣香锅", "珍珠奶茶", "牛肉拉面",
                              "宫保鸡丁", "蛋炒饭", "酸辣粉", "红烧牛肉面", "可乐", "薯条", "鸡翅"};
        for (TakeoutMerchant m : merchants) {
            for (int j = 0; j < 5; j++) {
                Dish d = new Dish();
                d.setDishName(dishNames[(int) (R.nextInt(dishNames.length))] + "-" + m.getMerchantId());
                d.setCategoryId(categories.get(R.nextInt(categories.size())).getCategoryId());
                d.setMerchantId(m.getMerchantId()); // ★ 关键：多商家隔离
                d.setImage("https://img.example.com/dish/" + R.nextInt(1000) + ".jpg");
                d.setPrice(BigDecimal.valueOf(8 + R.nextInt(40)).setScale(2, BigDecimal.ROUND_HALF_UP));
                d.setStock(50 + R.nextInt(200));
                d.setSales(R.nextInt(500));
                d.setStatus("1");
                d.setDescription("测试菜品 - 商家" + m.getMerchantId() + "的招牌菜");
                d.setRemark("由测试数据生成器创建");
                dishService.insert(d);
                dishes.add(d);
            }
        }
        List<TakeoutRider> riders = new ArrayList<>();
        String[] rNames = {"刘骑手", "陈骑手", "黄骑手", "周骑手", "吴骑手", "徐骑手", "孙骑手", "马骑手"};
        for (int i = 0; i < PER_TABLE; i++) {
            TakeoutRider r = new TakeoutRider();
            r.setName(rNames[i % rNames.length] + (i + 1));
            r.setPhone(phones[R.nextInt(phones.length)] + String.format("%08d", R.nextInt(100000000)));
            r.setStatus(new String[]{"0", "1", "2"}[R.nextInt(3)]);
            r.setCity(cities[R.nextInt(cities.length)]);
            r.setRating(BigDecimal.valueOf(4 + R.nextDouble()).setScale(2, BigDecimal.ROUND_HALF_UP));
            r.setTotalDeliveries(R.nextInt(800) + 50);
            r.setTotalIncome(BigDecimal.valueOf(R.nextInt(30000) + 2000));
            r.setEmergencyContact(contacts[R.nextInt(contacts.length)]);
            r.setEmergencyPhone(phones[R.nextInt(phones.length)] + String.format("%08d", R.nextInt(100000000)));
            r.setDelFlag("0");
            r.setRemark("由测试数据生成器创建");
            riderService.insertRider(r);
            riders.add(r);
        }

        // 4. 订单
        List<TakeoutOrder> orders = new ArrayList<>();
        String[] rNames2 = {"张三", "李四", "王五", "赵六", "钱七", "孙八", "周九", "吴十"};
        String[] addrSuf = {"路", "街", "大道", "巷"};
        String[] payMethods = {"微信", "支付宝", "余额"};
        for (int i = 0; i < PER_TABLE; i++) {
            TakeoutMerchant m = merchants.get(R.nextInt(merchants.size()));
            TakeoutUser u = users.get(R.nextInt(users.size()));
            TakeoutOrder o = new TakeoutOrder();
            o.setOrderNo("TKO" + System.currentTimeMillis() + i);
            o.setUserId(u.getUserId());
            o.setMerchantId(m.getMerchantId());
            o.setMerchantName(m.getMerchantName());
            o.setTotalAmount(BigDecimal.valueOf(20 + R.nextInt(200)).setScale(2, BigDecimal.ROUND_HALF_UP));
            o.setDeliveryFee(BigDecimal.valueOf(3 + R.nextInt(5)));
            // ★ 优惠/实付金额（70% 概率有优惠）
            boolean useCoupon = R.nextInt(10) < 7 && !"0".equals(o.getPayStatus());
            if (useCoupon) {
                o.setDiscountAmount(BigDecimal.valueOf(3 + R.nextInt(15)).setScale(2, BigDecimal.ROUND_HALF_UP));
            } else {
                o.setDiscountAmount(BigDecimal.ZERO);
            }
            o.setActualAmount(o.getTotalAmount().add(o.getDeliveryFee()).subtract(o.getDiscountAmount()));
            o.setCouponUserId(null);
            // ★ 支付时间
            if ("1".equals(o.getPayStatus())) {
                o.setPayTime(new Date(System.currentTimeMillis() - R.nextInt(7) * 24L * 60 * 60 * 1000));
            }
            String[] statuses = {"0", "1", "2", "3", "4", "5", "6"};
            String st = statuses[R.nextInt(statuses.length)];
            o.setStatus(st);
            o.setPayStatus(R.nextInt(10) < 7 ? "1" : "0");
            o.setPayMethod(payMethods[R.nextInt(payMethods.length)]);
            o.setReceiverName(rNames2[R.nextInt(rNames2.length)]);
            o.setReceiverPhone(phones[R.nextInt(phones.length)] + String.format("%08d", R.nextInt(100000000)));
            o.setAddress(cities[R.nextInt(cities.length)] + "市某区某" + addrSuf[R.nextInt(addrSuf.length)] + (R.nextInt(900) + 100) + "号");
            o.setRemark("请尽快配送");
            o.setDelFlag("0");
            if ("4".equals(st) || "5".equals(st)) {
                o.setCompleteTime(new Date(System.currentTimeMillis() - R.nextInt(7) * 24L * 60 * 60 * 1000));
            }
            orderService.insertOrder(o);
            orders.add(o);
        }

        // 5. 派单
        for (int i = 0; i < PER_TABLE; i++) {
            TakeoutOrder o = orders.get(i);
            TakeoutRider r = riders.get(R.nextInt(riders.size()));
            dispatchService.createDispatchForOrder(o.getOrderId(), r.getRiderId(), R.nextInt(10) < 5 ? "0" : "1");
            // ★ 补齐骑手实时位置（mock 坐标：北京天安门附近，配送中状态实时上报）
            TakeoutDispatch active = dispatchService.selectActiveDispatchByOrderId(o.getOrderId());
            if (active != null) {
                TakeoutDispatch upd = new TakeoutDispatch();
                upd.setDispatchId(active.getDispatchId());
                upd.setRiderLng(116.397128 + (R.nextDouble() - 0.5) * 0.05); // 北京天安门 + 抖动
                upd.setRiderLat(39.916527 + (R.nextDouble() - 0.5) * 0.05);
                upd.setLocationUpdateTime(new Date());
                upd.setUpdateBy("testData");
                // 直接走 service.update 风格: 这里没有 update 方法, 调 mapper
                updateDispatchLocation(upd);
            }
        }

        // 6. 评价
        String[] contents = {"味道很好，下次还会再来！", "配送速度快，骑手态度好。", "分量足，性价比高。", "包装有点破损，希望改进。", "非常满意，五星好评！", "一般般，没想象中好。", "服务很贴心，会回购。", "推荐朋友了，好吃！"};
        for (int i = 0; i < PER_TABLE; i++) {
            TakeoutOrder o = orders.get(i);
            TakeoutRating rt = new TakeoutRating();
            rt.setOrderId(o.getOrderId());
            rt.setUserId(o.getUserId());
            rt.setMerchantId(o.getMerchantId());
            rt.setRiderId(riders.get(i).getRiderId());
            rt.setMerchantScore(3 + R.nextInt(3));
            rt.setRiderScore(3 + R.nextInt(3));
            rt.setTasteScore(3 + R.nextInt(3));
            rt.setPackagingScore(3 + R.nextInt(3));
            rt.setDeliveryScore(3 + R.nextInt(3));
            rt.setContent(contents[i % contents.length]);
            rt.setTasteTags("[\"好吃\", \"分量足\"]");
            rt.setIsAnonymous(R.nextInt(10) < 3 ? "1" : "0");
            rt.setStatus("0");
            rt.setDelFlag("0");
            ratingService.submitRating(rt);
        }

        // 7. 优惠券
        String[] cNames = {"新人大礼包", "满50减10", "满100减25", "9折折扣券", "免配送费券", "周末特惠", "夏日清凉券", "生日特权券"};
        for (int i = 0; i < PER_TABLE; i++) {
            TakeoutCoupon c = new TakeoutCoupon();
            c.setName(cNames[i % cNames.length]);
            c.setType(String.valueOf(i % 4));
            c.setThresholdAmount(BigDecimal.valueOf(20 + i * 10));
            c.setDiscountAmount(BigDecimal.valueOf(5 + R.nextInt(30)));
            c.setDiscountRate(BigDecimal.valueOf(70 + R.nextInt(25)));
            c.setMaxDiscount(BigDecimal.valueOf(20 + R.nextInt(30)));
            c.setTotalCount(500 + R.nextInt(2000));
            c.setRemainCount(200 + R.nextInt(1500));
            c.setPerUserLimit(1 + R.nextInt(3));
            c.setStartTime(daysAgo(10));
            c.setEndTime(daysAfter(60 + R.nextInt(60)));
            c.setStatus("0");
            c.setColor(new String[]{"#FF6B6B", "#4ECDC4", "#FFD93D", "#6BCB77"}[i % 4]);
            c.setDescription("测试优惠券-" + cNames[i % cNames.length]);
            c.setDelFlag("0");
            couponService.insertCoupon(c);
        }

        // 8. 投诉
        String[] reasons = {"商家少送了", "商品口味不对", "配送超时", "骑手服务态度差", "餐品质量有问题", "包装破损", "想要退款", "其他问题"};
        for (int i = 0; i < PER_TABLE; i++) {
            TakeoutOrder o = orders.get(i);
            TakeoutComplaint cp = new TakeoutComplaint();
            cp.setOrderId(o.getOrderId());
            cp.setOrderNo(o.getOrderNo());
            cp.setUserId(o.getUserId());
            cp.setMerchantId(o.getMerchantId());
            cp.setRiderId(riders.get(i).getRiderId());
            cp.setType(String.valueOf(i % 4));
            cp.setReason(reasons[i % reasons.length]);
            cp.setRefundAmount(BigDecimal.valueOf(10 + R.nextInt(50)));
            cp.setApprovedAmount(BigDecimal.valueOf(R.nextInt(40)));
            String[] cstatuses = {"0", "1", "2", "3", "4", "5"};
            cp.setStatus(cstatuses[R.nextInt(cstatuses.length)]);
            if (R.nextInt(10) < 5) {
                cp.setHandleBy("admin");
                cp.setHandleTime(daysAgo(R.nextInt(15)));
                cp.setHandleRemark("已与用户沟通解决");
            }
            cp.setDelFlag("0");
            complaintService.submitComplaint(cp);
        }

        long cost = System.currentTimeMillis() - t0;
        return success(String.format("测试数据生成完成，共 %d 张表各 %d 条，耗时 %d ms", 10, PER_TABLE, cost));
    }

    private static Date daysAgo(int n) {
        return new Date(System.currentTimeMillis() - (long) n * 24 * 60 * 60 * 1000);
    }
    private static Date daysAfter(int n) {
        return new Date(System.currentTimeMillis() + (long) n * 24 * 60 * 60 * 1000);
    }

    /**
     * 直接调 mapper 更新骑手实时位置（service 没暴露此接口, 临时用于测试数据）
     */
    private void updateDispatchLocation(TakeoutDispatch d) {
        dispatchMapper.updateDispatch(d);
    }
}
