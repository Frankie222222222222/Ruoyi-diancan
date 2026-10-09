package com.ruoyi.takeout.service.impl;

import java.util.List;
import java.util.Arrays;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.takeout.domain.DishCategory;
import com.ruoyi.takeout.mapper.DishCategoryMapper;
import com.ruoyi.takeout.service.IDishCategoryService;

/**
 * 菜品分类 Service 业务层实现
 */
@Service
public class DishCategoryServiceImpl implements IDishCategoryService
{
    @Autowired
    private DishCategoryMapper dishCategoryMapper;

    @Override
    public DishCategory getById(Long id)
    {
        return dishCategoryMapper.selectDishCategoryById(id);
    }

    @Override
    public List<DishCategory> list(DishCategory query)
    {
        return dishCategoryMapper.selectDishCategoryList(query);
    }

    @Override
    public List<DishCategory> optionselect()
    {
        return dishCategoryMapper.selectDishCategoryOptions();
    }

    @Override
    public int insert(DishCategory category)
    {
        if (category.getStatus() == null || category.getStatus().isEmpty())
        {
            category.setStatus("1");
        }
        category.setCreateBy(SecurityUtils.getUsername());
        category.setCreateTime(DateUtils.getNowDate());
        return dishCategoryMapper.insertDishCategory(category);
    }

    @Override
    public int update(DishCategory category)
    {
        category.setUpdateBy(SecurityUtils.getUsername());
        category.setUpdateTime(DateUtils.getNowDate());
        return dishCategoryMapper.updateDishCategory(category);
    }

    @Override
    public int removeByIds(Long[] ids)
    {
        // 校验：分类下存在菜品则不允许删除
        List<Long> idList = Arrays.asList(ids);
        for (Long id : idList)
        {
            int cnt = dishCategoryMapper.countDishByCategoryId(id);
            if (cnt > 0)
            {
                throw new ServiceException(String.format("[ERR_1006] 分类【%s】下存在菜品，不允许删除",
                    Optional.ofNullable(getById(id)).map(DishCategory::getCategoryName).orElse(String.valueOf(id))));
            }
        }
        return dishCategoryMapper.deleteDishCategoryByIds(ids);
    }
}
