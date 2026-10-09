package com.ruoyi.takeout.controller;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.takeout.mapper.TakeoutStatisticsMapper;

/**
 * 外卖统计报表 Controller
 * 仅聚合查询, 直接复用 TakeoutStatisticsMapper 的 SQL
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/takeout/statistics")
public class TakeoutStatisticsController extends BaseController
{
    @Autowired
    private TakeoutStatisticsMapper statisticsMapper;

    /** 仪表盘: 一次取所有关键指标 */
    @PreAuthorize("@ss.hasPermi('takeout:statistics:list')")
    @GetMapping("/dashboard")
    public AjaxResult dashboard()
    {
        return success(statisticsMapper.getDashboard());
    }

    /** 最近 N 天趋势(默认7天) */
    @PreAuthorize("@ss.hasPermi('takeout:statistics:list')")
    @GetMapping("/trend")
    public AjaxResult trend(@RequestParam(defaultValue = "7") int days)
    {
        return success(statisticsMapper.getOrderTrend(days));
    }

    /** 商家营收 Top N */
    @PreAuthorize("@ss.hasPermi('takeout:statistics:list')")
    @GetMapping("/topMerchants")
    public AjaxResult topMerchants(@RequestParam(defaultValue = "10") int limit)
    {
        return success(statisticsMapper.getTopMerchants(limit));
    }

    /** 菜品销量 Top N */
    @PreAuthorize("@ss.hasPermi('takeout:statistics:list')")
    @GetMapping("/topDishes")
    public AjaxResult topDishes(@RequestParam(defaultValue = "10") int limit)
    {
        return success(statisticsMapper.getTopDishes(limit));
    }

    /** 骑手配送排行 Top N */
    @PreAuthorize("@ss.hasPermi('takeout:statistics:list')")
    @GetMapping("/topRiders")
    public AjaxResult topRiders(@RequestParam(defaultValue = "10") int limit)
    {
        return success(statisticsMapper.getTopRiders(limit));
    }

    /** 健康检查 */
    @GetMapping("/ping")
    public AjaxResult ping()
    {
        Map<String, Object> data = new java.util.HashMap<>();
        data.put("pong", true);
        data.put("todayOrders", statisticsMapper.countTodayOrders());
        data.put("todayRevenue", statisticsMapper.sumTodayRevenue());
        return success(data);
    }
}
