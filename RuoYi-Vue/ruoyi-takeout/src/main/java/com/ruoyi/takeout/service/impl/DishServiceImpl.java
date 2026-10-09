package com.ruoyi.takeout.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.takeout.constant.DishConstants;
import com.ruoyi.takeout.domain.Dish;
import com.ruoyi.takeout.mapper.DishMapper;
import com.ruoyi.takeout.mapper.TakeoutOrderMapper;
import com.ruoyi.takeout.service.IDishService;

/**
 * 菜品 Service 业务实现
 *
 * G4 防误删：被未完成订单引用的菜品禁止删除
 *
 * @author ruoyi
 */
@Service
public class DishServiceImpl implements IDishService
{
    private static final int ERR_DISH_IN_USE = 1009;

    @Autowired
    private DishMapper dishMapper;

    @Autowired
    private TakeoutOrderMapper orderMapper;

    @Override
    public Dish getById(Long id)
    {
        return dishMapper.selectDishById(id);
    }

    @Override
    public List<Dish> list(Dish query)
    {
        return dishMapper.selectDishList(query);
    }

    @Override
    public int insert(Dish dish)
    {
        if (dish.getStatus() == null || dish.getStatus().isEmpty())
        {
            dish.setStatus(DishConstants.NORMAL);
        }
        if (dish.getStock() == null) dish.setStock(0);
        if (dish.getSales() == null) dish.setSales(0);
        dish.setCreateBy(SecurityUtils.getUsername());
        dish.setCreateTime(DateUtils.getNowDate());
        return dishMapper.insertDish(dish);
    }

    @Override
    public int update(Dish dish)
    {
        dish.setUpdateBy(SecurityUtils.getUsername());
        dish.setUpdateTime(DateUtils.getNowDate());
        return dishMapper.updateDish(dish);
    }

    @Override
    public int changeStatus(Long id, String status)
    {
        Dish d = new Dish();
        d.setDishId(id);
        d.setStatus(status);
        d.setUpdateBy(SecurityUtils.getUsername());
        return dishMapper.updateDishStatus(d);
    }

    @Override
    public int adjustStock(Long id, Integer delta)
    {
        if (delta == null || delta == 0) return 0;
        // 校验不能扣成负数
        if (delta < 0)
        {
            Dish cur = dishMapper.selectDishById(id);
            if (cur == null) throw new ServiceException("[ERR_1007] 菜品不存在");
            if (cur.getStock() + delta < 0)
                throw new ServiceException("[ERR_1008] 库存不足, 当前库存=" + cur.getStock());
        }
        Dish d = new Dish();
        d.setDishId(id);
        d.setStock(delta);  // SQL 使用 GREATEST(0, stock+delta)
        d.setUpdateBy(SecurityUtils.getUsername());
        return dishMapper.adjustDishStock(d);
    }

    @Override
    public int removeByIds(Long[] ids)
    {
        // G4: 校验是否被未完成订单引用
        for (Long id : ids)
        {
            Dish d = dishMapper.selectDishById(id);
            if (d == null) continue;
            int refCount = orderMapper.countActiveOrdersByDishId(id);
            if (refCount > 0)
            {
                throw new ServiceException(
                    String.format("[ERR_%d] 菜品「%s」被 %d 个未完成订单使用，无法删除",
                        ERR_DISH_IN_USE, d.getDishName(), refCount),
                    ERR_DISH_IN_USE);
            }
        }
        return dishMapper.deleteDishByIds(ids);
    }

    @Override
    public List<Dish> topSales(Long merchantId, Integer limit)
    {
        int n = (limit == null || limit <= 0) ? 10 : Math.min(limit, 100);
        return dishMapper.selectTopSales(merchantId, n);
    }

    @Override
    public List<Dish> listByMerchant(Long merchantId)
    {
        if (merchantId == null) return java.util.Collections.emptyList();
        return dishMapper.selectDishListByMerchant(merchantId);
    }
}
