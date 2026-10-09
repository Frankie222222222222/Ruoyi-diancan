package com.ruoyi.takeout.mapper;

import java.util.List;
import com.ruoyi.takeout.domain.TakeoutUser;

/**
 * C端用户 Mapper
 *
 * @author ruoyi
 */
public interface TakeoutUserMapper
{
    /** 查询用户列表 */
    List<TakeoutUser> selectUserList(TakeoutUser user);

    /** 查询单个用户 */
    TakeoutUser selectUserById(Long userId);

    /** 通过手机号查询用户 */
    TakeoutUser selectUserByPhone(String phone);

    /** 新增用户 */
    int insertUser(TakeoutUser user);

    /** 修改用户 */
    int updateUser(TakeoutUser user);

    /** 删除用户(逻辑) */
    int deleteUserByIds(Long[] userIds);

    /** 更新最后登录时间 */
    int updateLastLoginTime(TakeoutUser user);

    /** 累加订单数和消费额 */
    int incrementUserStats(TakeoutUser user);
}
