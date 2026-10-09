package com.ruoyi.takeout.service;

import java.util.List;
import com.ruoyi.takeout.domain.Dish;

/**
 * ��Ʒ Service
 */
public interface IDishService
{
    Dish getById(Long id);

    List<Dish> list(Dish query);

    int insert(Dish dish);

    int update(Dish dish);

    int changeStatus(Long id, String status);

    int adjustStock(Long id, Integer delta);

    int removeByIds(Long[] ids);

    /** 销量 Top N */
    List<Dish> topSales(Long merchantId, Integer limit);

    /** 列出某商家下在售菜品（订单弹窗选用） */
    List<Dish> listByMerchant(Long merchantId);
}