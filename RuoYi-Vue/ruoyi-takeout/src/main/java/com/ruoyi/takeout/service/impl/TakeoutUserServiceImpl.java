package com.ruoyi.takeout.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.takeout.domain.TakeoutUser;
import com.ruoyi.takeout.mapper.TakeoutUserMapper;
import com.ruoyi.takeout.service.ITakeoutUserService;

/**
 * C端用户 Service 业务实现
 *
 * @author ruoyi
 */
@Service
public class TakeoutUserServiceImpl implements ITakeoutUserService
{
    private static final int ERR_PHONE_EXISTS  = 6001;
    private static final int ERR_USER_NOT_FOUND = 6002;
    private static final int ERR_USER_DISABLED  = 6003;
    private static final int ERR_PHONE_INVALID  = 6004;

    @Autowired
    private TakeoutUserMapper userMapper;

    @Override
    public List<TakeoutUser> selectUserList(TakeoutUser user)
    {
        return userMapper.selectUserList(user);
    }

    @Override
    public TakeoutUser selectUserById(Long userId)
    {
        return userMapper.selectUserById(userId);
    }

    @Override
    public TakeoutUser selectUserByPhone(String phone)
    {
        return userMapper.selectUserByPhone(phone);
    }

    @Override
    public int registerUser(TakeoutUser user)
    {
        if (StringUtils.isEmpty(user.getPhone()) || !user.getPhone().matches("^1[3-9]\\d{9}$"))
        {
            throw new ServiceException("[ERR_" + ERR_PHONE_INVALID + "] 手机号格式不正确", ERR_PHONE_INVALID);
        }
        TakeoutUser exist = userMapper.selectUserByPhone(user.getPhone());
        if (exist != null)
        {
            throw new ServiceException("[ERR_" + ERR_PHONE_EXISTS + "] 该手机号已注册", ERR_PHONE_EXISTS);
        }
        if (StringUtils.isEmpty(user.getNickname()))
        {
            user.setNickname("用户" + user.getPhone().substring(7));
        }
        if (StringUtils.isEmpty(user.getGender()))
        {
            user.setGender("0");
        }
        if (StringUtils.isEmpty(user.getStatus()))
        {
            user.setStatus("0");
        }
        user.setRegTime(DateUtils.getNowDate());
        return userMapper.insertUser(user);
    }

    @Override
    public TakeoutUser login(String phone)
    {
        if (StringUtils.isEmpty(phone))
        {
            throw new ServiceException("[ERR_" + ERR_PHONE_INVALID + "] 手机号不能为空", ERR_PHONE_INVALID);
        }
        TakeoutUser user = userMapper.selectUserByPhone(phone);
        if (user == null)
        {
            throw new ServiceException("[ERR_" + ERR_USER_NOT_FOUND + "] 用户不存在，请先注册", ERR_USER_NOT_FOUND);
        }
        if (!"0".equals(user.getStatus()))
        {
            throw new ServiceException("[ERR_" + ERR_USER_DISABLED + "] 账号已被禁用", ERR_USER_DISABLED);
        }
        // 异步更新最后登录时间
        userMapper.updateLastLoginTime(user);
        return user;
    }

    @Override
    public int updateUserProfile(TakeoutUser user)
    {
        user.setUpdateTime(DateUtils.getNowDate());
        return userMapper.updateUser(user);
    }

    @Override
    public int deleteUserByIds(Long[] userIds)
    {
        return userMapper.deleteUserByIds(userIds);
    }

    @Override
    public void updateLastLoginTime(Long userId)
    {
        TakeoutUser u = new TakeoutUser();
        u.setUserId(userId);
        userMapper.updateLastLoginTime(u);
    }

    @Override
    public void incrementUserStats(Long userId, java.math.BigDecimal amount)
    {
        if (amount == null || amount.signum() <= 0)
        {
            return;
        }
        TakeoutUser u = new TakeoutUser();
        u.setUserId(userId);
        u.setTotalSpend(amount);
        userMapper.incrementUserStats(u);
    }
}
