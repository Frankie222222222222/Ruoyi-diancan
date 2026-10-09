package com.ruoyi.takeout.mapper;

import java.util.List;
import com.ruoyi.takeout.domain.DishCategory;

/**
 * 菜品分类 Mapper
 */
public interface DishCategoryMapper
{
    DishCategory selectDishCategoryById(Long categoryId);

    List<DishCategory> selectDishCategoryList(DishCategory query);

    List<DishCategory> selectDishCategoryOptions();

    int insertDishCategory(DishCategory category);

    int updateDishCategory(DishCategory category);

    int deleteDishCategoryByIds(Long[] ids);

    int countDishByCategoryId(Long categoryId);
}
