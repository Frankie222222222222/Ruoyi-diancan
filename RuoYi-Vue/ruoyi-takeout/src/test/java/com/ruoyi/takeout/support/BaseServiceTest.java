package com.ruoyi.takeout.support;

import com.ruoyi.common.utils.SecurityUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

/**
 * Service 层单元测试的基类
 *
 * <p>职责:
 *  <ul>
 *      <li>Mock 掉 {@link SecurityUtils} 静态方法(测试环境没有 Spring Security 上下文)</li>
 *      <li>每个测试结束自动清理,避免污染其他测试</li>
 *  </ul>
 *
 * @author ruoyi
 */
public abstract class BaseServiceTest
{
    private MockedStatic<SecurityUtils> securityUtilsMock;

    @BeforeEach
    void setUpSecurity()
    {
        securityUtilsMock = Mockito.mockStatic(SecurityUtils.class, Mockito.CALLS_REAL_METHODS);
        // getUsername/getUserId 默认返回 test-user
        securityUtilsMock.when(SecurityUtils::getUsername).thenReturn("test-user");
        securityUtilsMock.when(SecurityUtils::getUserId).thenReturn(1L);
    }

    @AfterEach
    void tearDownSecurity()
    {
        if (securityUtilsMock != null) {
            securityUtilsMock.close();
        }
    }
}
