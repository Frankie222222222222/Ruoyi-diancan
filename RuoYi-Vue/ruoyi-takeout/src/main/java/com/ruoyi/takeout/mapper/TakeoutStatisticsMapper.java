package com.ruoyi.takeout.mapper;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 统计聚合 Mapper (直接 SQL 聚合, 无 Domain 映射)
 *
 * @author ruoyi
 */
public interface TakeoutStatisticsMapper
{
    /** 仪表盘: 各表总数 */
    Map<String, Object> getDashboard();

    /** 趋势: 最近 N 天每日订单数和营收 */
    List<Map<String, Object>> getOrderTrend(int days);

    /** 商家营收 Top N */
    List<Map<String, Object>> getTopMerchants(int limit);

    /** 菜品销量 Top N */
    List<Map<String, Object>> getTopDishes(int limit);

    /** 骑手配送单量 Top N */
    List<Map<String, Object>> getTopRiders(int limit);

    /** 今日订单数 */
    int countTodayOrders();

    /** 今日营收 */
    BigDecimal sumTodayRevenue();
}
