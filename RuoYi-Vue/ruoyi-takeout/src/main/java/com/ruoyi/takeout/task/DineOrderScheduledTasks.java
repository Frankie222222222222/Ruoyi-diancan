package com.ruoyi.takeout.task;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import com.ruoyi.takeout.service.ITakeoutOrderService;

/**
 * 堂食订单定时任务（v3 - 2026-10-10）
 *
 * <p>每分钟扫描一次:取消超过 30 分钟未支付的 DRAFT 堂食订单。</p>
 *
 * <p>启用方法: 在 ruoyi-admin 启动类加 {@code @EnableScheduling}。
 * 之所以放在 takeout 模块而不是 ruoyi-quartz: 避免 takeout → quartz 反向依赖。</p>
 *
 * @author ruoyi
 */
@Component("dineOrderScheduledTasks")
public class DineOrderScheduledTasks
{
    private static final Logger log = LoggerFactory.getLogger(DineOrderScheduledTasks.class);

    @Autowired
    private ITakeoutOrderService orderService;

    /**
     * 每 60 秒执行一次:cron = 0 * * * * ?
     * (秒 分 时 日 月 周)
     */
    @Scheduled(cron = "0 * * * * ?")
    public void autoCancelExpiredDineOrders()
    {
        try
        {
            int rows = orderService.autoCancelExpiredDineOrders();
            if (rows > 0)
            {
                log.info("[dineOrderScheduledTasks] auto cancelled {} expired dine orders", rows);
            }
        }
        catch (Exception e)
        {
            log.error("[dineOrderScheduledTasks] error: {}", e.getMessage(), e);
        }
    }
}
