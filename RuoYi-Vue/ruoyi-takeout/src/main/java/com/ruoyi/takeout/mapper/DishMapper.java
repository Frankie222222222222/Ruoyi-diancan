package com.ruoyi.takeout.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.takeout.domain.Dish;

/**
 * 菜品Mapper
 */
public interface DishMapper
{
    Dish selectDishById(Long dishId);

    List<Dish> selectDishList(Dish query);

    int insertDish(Dish dish);

    int updateDish(Dish dish);

    int updateDishStatus(Dish dish);

    int adjustDishStock(Dish dish);

    /** 增加销量 sales = sales + delta */
    int incrDishSales(@Param("dishId") Long dishId, @Param("delta") Integer delta);

    /** 减少销量 sales = GREATEST(0, sales - delta) */
    int decrDishSales(@Param("dishId") Long dishId, @Param("delta") Integer delta);

    /** 销量 Top N */
    List<Dish> selectTopSales(@Param("merchantId") Long merchantId, @Param("limit") Integer limit);

    /** 列出某商家下在售菜品 */
    List<Dish> selectDishListByMerchant(@Param("merchantId") Long merchantId);

    int deleteDishByIds(Long[] ids);
}