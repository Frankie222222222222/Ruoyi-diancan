package com.ruoyi.takeout.domain;

import java.math.BigDecimal;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 外卖C端用户实体
 *
 * @author ruoyi
 */
public class TakeoutUser extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 用户ID */
    @Excel(name = "用户ID", cellType = Excel.ColumnType.NUMERIC)
    private Long userId;

    /** 昵称 */
    @Excel(name = "昵称")
    private String nickname;

    /** 手机号 */
    @Excel(name = "手机号")
    private String phone;

    /** 头像URL */
    private String avatar;

    /** 性别(0未知 1男 2女) */
    @Excel(name = "性别", readConverterExp = "0=未知,1=男,2=女")
    private String gender;

    /** 所在城市 */
    @Excel(name = "城市")
    private String city;

    /** 累计下单数 */
    @Excel(name = "累计订单", cellType = Excel.ColumnType.NUMERIC)
    private Integer totalOrders;

    /** 累计消费 */
    @Excel(name = "累计消费")
    private BigDecimal totalSpend;

    /** 注册时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "注册时间")
    private Date regTime;

    /** 最后登录时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "最后登录")
    private Date lastLoginTime;

    /** 账号状态(0正常 1禁用) */
    @Excel(name = "状态", readConverterExp = "0=正常,1=禁用")
    private String status;

    /** 删除标志(0存在 2删除) */
    private String delFlag;

    /* ========== v2 扩展(2026-10-10)角色字段 ========== */

    /**
     * 角色
     * <ul>
     *   <li>user    - 顾客(默认)</li>
     *   <li>kitchen - 后厨</li>
     *   <li>rider   - 骑手</li>
     *   <li>admin   - 管理员(走 RuoYi 系统用户,本表只做关联冗余)</li>
     * </ul>
     */
    @Excel(name = "角色", readConverterExp = "user=顾客,kitchen=后厨,rider=骑手,admin=管理员")
    private String role;

    /* ========== /v2 ========== */

    public Long getUserId()
    {
        return userId;
    }

    public void setUserId(Long userId)
    {
        this.userId = userId;
    }

    public String getNickname()
    {
        return nickname;
    }

    public void setNickname(String nickname)
    {
        this.nickname = nickname;
    }

    public String getPhone()
    {
        return phone;
    }

    public void setPhone(String phone)
    {
        this.phone = phone;
    }

    public String getAvatar()
    {
        return avatar;
    }

    public void setAvatar(String avatar)
    {
        this.avatar = avatar;
    }

    public String getGender()
    {
        return gender;
    }

    public void setGender(String gender)
    {
        this.gender = gender;
    }

    public String getCity()
    {
        return city;
    }

    public void setCity(String city)
    {
        this.city = city;
    }

    public Integer getTotalOrders()
    {
        return totalOrders;
    }

    public void setTotalOrders(Integer totalOrders)
    {
        this.totalOrders = totalOrders;
    }

    public BigDecimal getTotalSpend()
    {
        return totalSpend;
    }

    public void setTotalSpend(BigDecimal totalSpend)
    {
        this.totalSpend = totalSpend;
    }

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    public Date getRegTime()
    {
        return regTime;
    }

    public void setRegTime(Date regTime)
    {
        this.regTime = regTime;
    }

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    public Date getLastLoginTime()
    {
        return lastLoginTime;
    }

    public void setLastLoginTime(Date lastLoginTime)
    {
        this.lastLoginTime = lastLoginTime;
    }

    public String getStatus()
    {
        return status;
    }

    public void setStatus(String status)
    {
        this.status = status;
    }

    public String getDelFlag()
    {
        return delFlag;
    }

    public void setDelFlag(String delFlag)
    {
        this.delFlag = delFlag;
    }

    /* ========== v2 角色 getter/setter ========== */

    public String getRole()
    {
        return role;
    }

    public void setRole(String role)
    {
        this.role = role;
    }

    /* ========== /v2 ========== */

    @Override
    public String toString()
    {
        return "TakeoutUser{" +
                "userId=" + userId +
                ", nickname='" + nickname + '\'' +
                ", phone='" + phone + '\'' +
                ", status='" + status + '\'' +
                ", role='" + role + '\'' +
                '}';
    }
}
