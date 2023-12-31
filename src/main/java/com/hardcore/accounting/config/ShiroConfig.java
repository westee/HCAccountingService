package com.hardcore.accounting.config;

import com.hardcore.accounting.shiro.CustomFormAuthenticationFilter;
import com.hardcore.accounting.shiro.CustomHttpFilter;
import com.hardcore.accounting.shiro.CustomShiroFilterFactoryBean;

import com.hardcore.accounting.shiro.RedisSessionDao;
import lombok.val;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.credential.HashedCredentialsMatcher;
import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.realm.Realm;
import org.apache.shiro.session.mgt.SessionManager;
import org.apache.shiro.spring.web.ShiroFilterFactoryBean;
import org.apache.shiro.web.mgt.DefaultWebSecurityManager;
import org.apache.shiro.web.session.mgt.DefaultWebSessionManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.LinkedHashMap;

@Configuration
public class ShiroConfig {

    private static final String HASH_ALGORITHM_NAME = "SHA-256";
    private static final int HASH_ITERATIONS = 1000;
    private static final long GLOBAL_SESSION_TIMEOUT = 1000 * 60 * 60 * 24;

    /**
     * The bean for Security manager.
     *
     * @param realm the specific realm.
     * @return Security manager.
     */
    @Bean
    public SecurityManager securityManager(Realm realm, SessionManager sessionManager) {
        val securityManager = new DefaultWebSecurityManager(realm);
        securityManager.setSessionManager(sessionManager);
        SecurityUtils.setSecurityManager(securityManager);
        return securityManager;
    }

    @Bean("CustomSessionManager")
    public SessionManager sessionManager(RedisSessionDao sessionDao) {
        DefaultWebSessionManager sessionManager = new DefaultWebSessionManager();
        sessionManager.setSessionDAO(sessionDao);
        sessionManager.setGlobalSessionTimeout(GLOBAL_SESSION_TIMEOUT);
        return sessionManager;
    }

    /**
     * The bean for security manager.
     *
     * @param securityManager security manager.
     * @return Shiro filter factory bean.
     */
    @Bean
    public ShiroFilterFactoryBean shiroFilterFactoryBean(
        SecurityManager securityManager) {

        val shiroFilterFactoryBean = new CustomShiroFilterFactoryBean();
        shiroFilterFactoryBean.setSecurityManager(securityManager);
        val filters = shiroFilterFactoryBean.getFilters();
        filters.put("custom", new CustomHttpFilter());
        filters.put("authc", new CustomFormAuthenticationFilter());
        val shiroFilterDefinitionMap = new LinkedHashMap<String, String>();
        shiroFilterDefinitionMap.put("/v1.0/session", "anon");
        shiroFilterDefinitionMap.put("/v1.0/tags/**", "authc");
        shiroFilterDefinitionMap.put("/v1.0/records/**", "authc");
        shiroFilterDefinitionMap.put("/v1.0/users/**::POST", "custom");

        //swagger related url.
        shiroFilterDefinitionMap.put("/swagger-ui.html/**", "anon");
        shiroFilterDefinitionMap.put("/swagger-resources/**", "anon");
        shiroFilterDefinitionMap.put("/v2/**", "anon");
        shiroFilterDefinitionMap.put("/webjars/**", "anon");

        shiroFilterDefinitionMap.put("/**", "authc");

        shiroFilterFactoryBean.setFilterChainDefinitionMap(shiroFilterDefinitionMap);

        return shiroFilterFactoryBean;

    }

    /**
     * The bean for HashedCredentialsMatcher.
     *
     * @return HashedCredentialsMatcher bean.
     */
    @Bean
    public HashedCredentialsMatcher matcher() {
        val matcher = new HashedCredentialsMatcher();
        matcher.setHashAlgorithmName(HASH_ALGORITHM_NAME);
        matcher.setHashIterations(HASH_ITERATIONS);
        matcher.setHashSalted(true);
        matcher.setStoredCredentialsHexEncoded(false);
        return matcher;
    }

}
