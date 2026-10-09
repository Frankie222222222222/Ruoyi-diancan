package com.ruoyi.takeout.service;

import java.util.List;
import com.ruoyi.takeout.domain.DishCategory;

/**
 * 菜品分类 Service
 */
public interface IDishCategoryService
{
    DishCategory getById(Long id);

    List<DishCategory> list(DishCategory query);

    List<DishCategory> optionselect();

    int insert(DishCategory category);

    int update(DishCategory category);

    int removeByIds(Long[] ids);
}
