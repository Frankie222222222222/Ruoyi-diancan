package com.ruoyi.takeout.service;

import java.util.List;
import com.ruoyi.takeout.domain.TakeoutUser;

/**
 * C端用户 Service接口
 *
 * @author ruoyi
 */
public interface ITakeoutUserService
{
    /** 查询用户列表 */
    List<TakeoutUser> selectUserList(TakeoutUser user);

    /** 查询单个用户 */
    TakeoutUser selectUserById(Long userId);

    /** 通过手机号查询用户 */
    TakeoutUser selectUserByPhone(String phone);

    /** C端用户注册 */
    int registerUser(TakeoutUser user);

    /** C端用户登录 */
    TakeoutUser login(String phone);

    /** 修改用户资料 */
    int updateUserProfile(TakeoutUser user);

    /** 删除用户(逻辑) */
    int deleteUserByIds(Long[] userIds);

    /** 更新最后登录时间 */
    void updateLastLoginTime(Long userId);

    /** 累加用户订单数和消费额 */
    void incrementUserStats(Long userId, java.math.BigDecimal amount);
}
