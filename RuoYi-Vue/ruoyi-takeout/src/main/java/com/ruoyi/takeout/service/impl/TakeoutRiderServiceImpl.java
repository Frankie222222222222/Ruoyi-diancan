package com.ruoyi.takeout.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.takeout.domain.TakeoutRider;
import com.ruoyi.takeout.mapper.TakeoutRiderMapper;
import com.ruoyi.takeout.service.ITakeoutRiderService;

/**
 * 骑手 Service 业务实现
 *
 * @author ruoyi
 */
@Service
public class TakeoutRiderServiceImpl implements ITakeoutRiderService
{
    private static final int ERR_PHONE_EXISTS    = 7001;
    private static final int ERR_RIDER_NOT_FOUND = 7002;
    private static final int ERR_INVALID_STATUS  = 7003;

    @Autowired
    private TakeoutRiderMapper riderMapper;

    @Override
    public List<TakeoutRider> selectRiderList(TakeoutRider rider)
    {
        return riderMapper.selectRiderList(rider);
    }

    @Override
    public TakeoutRider selectRiderById(Long riderId)
    {
        TakeoutRider r = riderMapper.selectRiderById(riderId);
        if (r == null)
        {
            throw new ServiceException("[ERR_" + ERR_RIDER_NOT_FOUND + "] 骑手不存在,id=" + riderId, ERR_RIDER_NOT_FOUND);
        }
        return r;
    }

    @Override
    public int insertRider(TakeoutRider rider)
    {
        validateRider(rider, true);
        TakeoutRider exist = riderMapper.selectRiderByPhone(rider.getPhone());
        if (exist != null)
        {
            throw new ServiceException("[ERR_" + ERR_PHONE_EXISTS + "] 手机号已存在", ERR_PHONE_EXISTS);
        }
        if (StringUtils.isEmpty(rider.getStatus()))
        {
            rider.setStatus("1"); // 默认休息中
        }
        if (rider.getRating() == null)
        {
            rider.setRating(new java.math.BigDecimal("5.00"));
        }
        if (rider.getJoinTime() == null)
        {
            rider.setJoinTime(DateUtils.getNowDate());
        }
        rider.setCreateBy(SecurityUtils.getUsername());
        return riderMapper.insertRider(rider);
    }

    @Override
    public int updateRider(TakeoutRider rider)
    {
        validateRider(rider, false);
        if (StringUtils.isNotEmpty(rider.getPhone()))
        {
            TakeoutRider exist = riderMapper.selectRiderByPhone(rider.getPhone());
            if (exist != null && !exist.getRiderId().equals(rider.getRiderId()))
            {
                throw new ServiceException("[ERR_" + ERR_PHONE_EXISTS + "] 手机号已被其他骑手使用", ERR_PHONE_EXISTS);
            }
        }
        rider.setUpdateBy(SecurityUtils.getUsername());
        return riderMapper.updateRider(rider);
    }

    @Override
    public int deleteRiderByIds(Long[] riderIds)
    {
        return riderMapper.deleteRiderByIds(riderIds);
    }

    @Override
    public int changeRiderStatus(Long riderId, String status)
    {
        if (!"0".equals(status) && !"1".equals(status) && !"2".equals(status))
        {
            throw new ServiceException("[ERR_" + ERR_INVALID_STATUS + "] 非法的骑手状态: " + status, ERR_INVALID_STATUS);
        }
        TakeoutRider r = new TakeoutRider();
        r.setRiderId(riderId);
        r.setStatus(status);
        r.setUpdateBy(SecurityUtils.getUsername());
        return riderMapper.updateRiderStatus(r);
    }

    @Override
    public List<TakeoutRider> selectAvailableRiders(String city)
    {
        return riderMapper.selectAvailableRiders(city);
    }

    @Override
    @Transactional
    public void incrementDeliveries(Long riderId)
    {
        riderMapper.incrementDeliveries(riderId);
    }

    private void validateRider(TakeoutRider rider, boolean isNew)
    {
        if (isNew)
        {
            if (StringUtils.isEmpty(rider.getName()))
            {
                throw new ServiceException("骑手姓名不能为空");
            }
            if (StringUtils.isEmpty(rider.getPhone()))
            {
                throw new ServiceException("联系电话不能为空");
            }
        }
    }
}
