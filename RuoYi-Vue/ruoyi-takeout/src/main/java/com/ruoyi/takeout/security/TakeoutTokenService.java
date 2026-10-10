package com.ruoyi.takeout.security;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import com.ruoyi.common.constant.Constants;
import com.ruoyi.common.core.domain.entity.SysUser;
import com.ruoyi.common.core.domain.model.LoginUser;
import com.ruoyi.common.core.redis.RedisCache;
import com.ruoyi.common.utils.ServletUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.common.utils.ip.AddressUtils;
import com.ruoyi.common.utils.ip.IpUtils;
import com.ruoyi.common.utils.uuid.IdUtils;
import com.ruoyi.takeout.domain.TakeoutUser;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import jakarta.servlet.http.HttpServletRequest;

/**
 * C 端外卖用户 Token 服务
 *
 * <p>复用 ruoyi 的 io.jsonwebtoken(JJWT) + Redis 缓存方案：
 * 登录时签发 token → Redis 缓存 LoginUser 副本 (key=login_tokens:{uuid})。
 * 后续接口由 JwtAuthenticationTokenFilter 自动从请求头解析 token，
 * 通过 uuid 拿到 LoginUser，注入到 Spring Security 上下文。</p>
 *
 * <p>C 端与 B 端共用 token 命名空间，但用 takeout_user_id 区分；
 * LoginUser.userName 使用手机号, LoginUser.userId 使用 takeout userId。</p>
 *
 * @author ruoyi
 */
@Component
public class TakeoutTokenService
{
    private static final Logger log = LoggerFactory.getLogger(TakeoutTokenService.class);

    /** C端用户登录态 redis key 前缀 (与 B 端 login_tokens 隔离) */
    private static final String LOGIN_TOKEN_KEY = "takeout_login_tokens:";

    @Value("${token.header:Authorization}")
    private String header;

    @Value("${token.secret:abcdefghijklmnopqrstuvwxyz}")
    private String secret;

    @Value("${token.expireTime:30}")
    private int expireTime;

    @Value("${token.takeoutExpireTime:1440}")
    /** C 端默认 token 有效期 24 小时 (单位:分钟) */
    private int takeoutExpireMinutes;

    private static final long MILLIS_SECOND = 1000L;
    private static final long MILLIS_MINUTE = 60 * MILLIS_SECOND;
    private static final Long  MILLIS_MINUTE_TWENTY = 20 * 60 * 1000L;

    @Autowired
    private RedisCache redisCache;

    /**
     * C端登录: 签发 JWT
     *
     * @param user takeout_user 记录
     * @return token 字符串
     */
    public String login(TakeoutUser user)
    {
        if (user == null || user.getUserId() == null)
        {
            throw new IllegalArgumentException("takeout user cannot be null");
        }

        // 1) 构造 LoginUser, 把 takeout user 信息"投影"到 SysUser
        SysUser sysUser = new SysUser();
        sysUser.setUserId(user.getUserId());
        sysUser.setUserName(user.getPhone() != null ? user.getPhone() : ("u" + user.getUserId()));
        sysUser.setNickName(user.getNickname());
        sysUser.setAvatar(user.getAvatar());
        sysUser.setStatus("0".equals(user.getStatus()) ? "0" : "1");
        // 密码字段留空，C端不参与 Spring Security 的密码登录
        sysUser.setPassword("");

        LoginUser loginUser = new LoginUser();
        loginUser.setUserId(user.getUserId());
        loginUser.setUser(sysUser);
        // C 端不绑定权限，permissions 留空
        // 2026-10-10 v3: 按 role 注入 permissions,使 C 端 token 能通过 @PreAuthorize
        String role = user.getRole();
        java.util.Set<String> perms = com.ruoyi.takeout.security.RolePermissionMap.get(role);
        loginUser.setPermissions(perms == null ? java.util.Collections.emptySet() : perms);

        // 2) 生成 uuid 作 redis key
        String uuid = IdUtils.fastUUID();
        loginUser.setToken(uuid);
        refreshToken(loginUser);

        // 3) 写入 JWT claims
        Map<String, Object> claims = new HashMap<>();
        claims.put(Constants.LOGIN_USER_KEY, uuid);
        claims.put(Constants.JWT_USERNAME, loginUser.getUsername());
        return Jwts.builder()
                .setClaims(claims)
                .signWith(SignatureAlgorithm.HS512, secret)
                .compact();
    }

    /**
     * C 端登出: 解析 token → 删除 redis 登录态
     */
    public void logout(HttpServletRequest request)
    {
        String token = getToken(request);
        if (StringUtils.isNotEmpty(token))
        {
            try
            {
                Claims claims = parseToken(token);
                String uuid = (String) claims.get(Constants.LOGIN_USER_KEY);
                if (StringUtils.isNotEmpty(uuid))
                {
                    String userKey = getTokenKey(uuid);
                    redisCache.deleteObject(userKey);
                }
            }
            catch (Exception e)
            {
                log.warn("C端登出解析token失败: {}", e.getMessage());
            }
        }
    }

    /**
     * 校验 token 是否有效 (从 redis 读取 loginUser)
     */
    public LoginUser validateToken(String token)
    {
        if (StringUtils.isEmpty(token))
        {
            return null;
        }
        try
        {
            Claims claims = parseToken(token);
            String uuid = (String) claims.get(Constants.LOGIN_USER_KEY);
            if (StringUtils.isEmpty(uuid))
            {
                return null;
            }
            String userKey = getTokenKey(uuid);
            LoginUser loginUser = redisCache.getCacheObject(userKey);
            if (loginUser != null)
            {
                verifyTtl(loginUser);
            }
            return loginUser;
        }
        catch (Exception e)
        {
            log.debug("C端token校验失败: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 取请求头里的 token
     */
    public String getToken(HttpServletRequest request)
    {
        if (request == null)
        {
            return null;
        }
        String token = request.getHeader(header);
        if (StringUtils.isNotEmpty(token) && token.startsWith(Constants.TOKEN_PREFIX))
        {
            return token.substring(Constants.TOKEN_PREFIX.length());
        }
        return token;
    }

    /**
     * 自动续期: 距过期 < 20 分钟则刷新 redis
     */
    private void verifyTtl(LoginUser loginUser)
    {
        long expire = loginUser.getExpireTime() == null ? 0L : loginUser.getExpireTime();
        long now = System.currentTimeMillis();
        if (expire - now <= MILLIS_MINUTE_TWENTY)
        {
            refreshToken(loginUser);
        }
    }

    /**
     * 刷新 redis 缓存 (使用 C 端独立的有效期)
     */
    private void refreshToken(LoginUser loginUser)
    {
        long now = System.currentTimeMillis();
        loginUser.setLoginTime(now);
        loginUser.setExpireTime(now + takeoutExpireMinutes * MILLIS_MINUTE);

        // 写登录信息
        String userKey = getTokenKey(loginUser.getToken());
        redisCache.setCacheObject(userKey, loginUser, takeoutExpireMinutes, TimeUnit.MINUTES);

        // 写IP/UA 信息
        try
        {
            HttpServletRequest req = ServletUtils.getRequest();
            if (req != null)
            {
                String ip = IpUtils.getIpAddr(req);
                loginUser.setIpaddr(ip);
                loginUser.setLoginLocation(AddressUtils.getRealAddressByIP(ip));
            }
        }
        catch (Exception ignored) {}
    }

    private Claims parseToken(String token)
    {
        return Jwts.parser()
                .setSigningKey(secret)
                .parseClaimsJws(token)
                .getBody();
    }

    private String getTokenKey(String uuid)
    {
        return LOGIN_TOKEN_KEY + uuid;
    }
}
