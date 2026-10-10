package com.ruoyi.takeout.controller;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.takeout.domain.TakeoutUser;
import com.ruoyi.takeout.security.TakeoutTokenService;
import com.ruoyi.takeout.service.ITakeoutUserService;

/**
 * C端用户 Controller
 *
 * <p>登录流程: 手机号 + (演示版) 跳过短信验证码 → 校验存在 → 签发 JWT
 * JWT 复用 ruoyi-framework 的 TokenService 流程 + JJWT 库
 * 登录态通过 redis 缓存, 前端请求时由 JwtAuthenticationTokenFilter 解析</p>
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/takeout/user")
public class TakeoutUserController extends BaseController
{
    @Autowired
    private ITakeoutUserService userService;

    @Autowired
    private TakeoutTokenService tokenService;

    /** 分页查询(后台管理) */
    @PreAuthorize("@ss.hasPermi('takeout:user:list')")
    @GetMapping("/list")
    public TableDataInfo list(TakeoutUser user)
    {
        startPage();
        return getDataTable(userService.selectUserList(user));
    }

    /** 角色字典(给前端下拉用,公开) */
    @GetMapping("/roleDict")
    public AjaxResult roleDict()
    {
        return success(com.ruoyi.takeout.enums.UserRoleEnum.toMap());
    }

    /** 详情 */
    @PreAuthorize("@ss.hasPermi('takeout:user:query')")
    @GetMapping("/{userId}")
    public AjaxResult getInfo(@PathVariable Long userId)
    {
        return success(userService.selectUserById(userId));
    }

    /**
     * C端注册
     * 允许匿名访问 (SecurityConfig 已放行 register, 此处不加 @PreAuthorize)
     */
    @Log(title = "C端用户", businessType = BusinessType.INSERT)
    @PostMapping("/register")
    public AjaxResult register(@RequestBody TakeoutUser user)
    {
        int rows = userService.registerUser(user);
        return rows > 0 ? success(user.getUserId()) : error();
    }

    /**
     * C端登录
     * 演示版直接用手机号登录 (生产应先校验短信验证码)
     * 成功返回 { token, userInfo }
     */
    @Log(title = "C端用户", businessType = BusinessType.OTHER)
    @PostMapping("/login")
    public AjaxResult login(@RequestBody Map<String, String> body)
    {
        String phone = body.get("phone");
        TakeoutUser user = userService.login(phone);
        // 1. 签发 JWT
        String token = tokenService.login(user);
        // 2. 返回 userInfo(v2 加 role 字段,小程序按 role 分流)
        Map<String, Object> data = new HashMap<>();
        data.put("token", token);
        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("userId", user.getUserId());
        userInfo.put("nickname", user.getNickname());
        userInfo.put("phone", user.getPhone());
        userInfo.put("avatar", user.getAvatar());
        userInfo.put("gender", user.getGender());
        userInfo.put("city", user.getCity());
        // v2:角色(默认 user,后厨 kitchen,骑手 rider,管理员 admin)
        userInfo.put("role", user.getRole() == null ? "user" : user.getRole());
        data.put("userInfo", userInfo);
        return AjaxResult.success("登录成功", data);
    }

    /**
     * C端登出 - 删除 redis 中的登录态
     * 允许匿名访问 (即使 token 已失效也允许)
     */
    @Log(title = "C端用户", businessType = BusinessType.OTHER)
    @PostMapping("/logout")
    public AjaxResult logout(HttpServletRequest request)
    {
        tokenService.logout(request);
        return success("已退出登录");
    }

    /**
     * C端"我": 通过 token 查询当前用户信息
     * 供前端在用户中心 / 拦截器启动时拉取
     */
    @GetMapping("/me")
    public AjaxResult me(HttpServletRequest request)
    {
        String token = tokenService.getToken(request);
        com.ruoyi.common.core.domain.model.LoginUser loginUser = tokenService.validateToken(token);
        if (loginUser == null || loginUser.getUserId() == null)
        {
            return AjaxResult.error(401, "未登录或登录已过期");
        }
        TakeoutUser u = userService.selectUserById(loginUser.getUserId());
        if (u == null)
        {
            return AjaxResult.error(404, "用户不存在");
        }
        // 隐藏敏感字段
        u.setRemark(null);
        return success(u);
    }

    /** 修改资料(后台管理/C端均可) */
    @PreAuthorize("@ss.hasPermi('takeout:user:edit')")
    @Log(title = "C端用户", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody TakeoutUser user)
    {
        return toAjax(userService.updateUserProfile(user));
    }

    /** 删除(逻辑) */
    @PreAuthorize("@ss.hasPermi('takeout:user:remove')")
    @Log(title = "C端用户", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(userService.deleteUserByIds(ids));
    }

    /** 累加消费(内部调用,供订单完成时统计) */
    @PostMapping("/incrementStats/{userId}")
    public AjaxResult incrementStats(@PathVariable Long userId, @RequestParam BigDecimal amount)
    {
        userService.incrementUserStats(userId, amount);
        return success();
    }

    /* ========== v2 扩展(2026-10-10) ========== */

    /**
     * 修改用户角色(后台管理员用)
     * @param userId 用户ID
     * @param role   角色(user/kitchen/rider/admin)
     */
    @PreAuthorize("@ss.hasPermi('takeout:user:changeRole')")
    @Log(title = "C端用户-改角色", businessType = BusinessType.UPDATE)
    @PutMapping("/changeRole/{userId}/{role}")
    public AjaxResult changeRole(@PathVariable Long userId, @PathVariable String role)
    {
        int rows = userService.changeUserRole(userId, role);
        return rows > 0 ? success() : error();
    }
}
