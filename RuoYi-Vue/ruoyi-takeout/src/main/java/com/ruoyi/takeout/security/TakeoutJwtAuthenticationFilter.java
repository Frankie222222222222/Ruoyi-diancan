package com.ruoyi.takeout.security;

import java.io.IOException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import com.ruoyi.common.core.domain.model.LoginUser;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.StringUtils;

/**
 * C 端外卖 Token 鉴权 Filter
 *
 * <p>只处理 /takeout/user/me 之类需要 C 端登录态的接口，
 * 把 takeoutTokenService 解析得到的 LoginUser 注入到 SecurityContext，
 * 后续 Spring Security 的 @PreAuthorize 会基于该 principal 进行判断。</p>
 *
 * <p>B 端 token 由 JwtAuthenticationTokenFilter 负责解析；本 filter 不会重复处理
 * B 端用户(因为 B 端 token 在 takeout_login_tokens: 中查不到)。</p>
 *
 * @author ruoyi
 */
@Component
public class TakeoutJwtAuthenticationFilter extends OncePerRequestFilter
{
    @Autowired
    private TakeoutTokenService takeoutTokenService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException
    {
        // 如果当前已经有 B 端登录态, 直接放行
        if (SecurityUtils.getAuthentication() != null)
        {
            chain.doFilter(request, response);
            return;
        }
        // 解析 C 端 token
        String token = takeoutTokenService.getToken(request);
        LoginUser loginUser = takeoutTokenService.validateToken(token);
        if (StringUtils.isNotNull(loginUser))
        {
            UsernamePasswordAuthenticationToken auth =
                    new UsernamePasswordAuthenticationToken(loginUser, null, loginUser.getAuthorities());
            auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(auth);
        }
        chain.doFilter(request, response);
    }
}
