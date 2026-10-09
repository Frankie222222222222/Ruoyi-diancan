package com.ruoyi.common.filter;

import java.io.IOException;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletResponse;

/**
 * CharsetFilter - Force UTF-8 response encoding.
 * <p>
 * Only sets response character encoding to UTF-8.
 * Does NOT set Content-Type to avoid Jackson refusing to serialize AjaxResult
 * when Content-Type is preset to text/html.
 * </p>
 *
 * @author ruoyi
 */
public class CharsetFilter implements Filter
{
    @Override
    public void init(FilterConfig filterConfig) throws ServletException
    {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException
    {
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        // Only set default character encoding, do not override Content-Type
        httpResponse.setCharacterEncoding("UTF-8");
        chain.doFilter(request, response);
    }

    @Override
    public void destroy()
    {
    }
}