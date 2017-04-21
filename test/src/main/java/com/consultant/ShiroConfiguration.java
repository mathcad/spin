package com.consultant;

import com.consultant.service.UserService;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authc.pam.ModularRealmAuthenticator;
import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.realm.Realm;
import org.apache.shiro.spring.web.ShiroFilterFactoryBean;
import org.apache.shiro.web.mgt.DefaultWebSecurityManager;
import org.spin.shiro.AnyoneSuccessfulStrategy;
import org.spin.shiro.EnhancedAuthenticationFilter;
import org.spin.shiro.UsernamePasswordRealm;
import org.spin.util.DigestUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.servlet.Filter;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * shiro配置
 * <p>Created by xuweinan on 2016/10/13.</p>
 *
 * @author xuweinan
 */
@Configuration
public class ShiroConfiguration {

    @Bean
    public ShiroFilterFactoryBean shirFilter(SecurityManager securityManager, UserService userService) {
        ShiroFilterFactoryBean shiroFilterFactoryBean = new ShiroFilterFactoryBean();
        shiroFilterFactoryBean.setFilters(userShiroFilters(userService));
        shiroFilterFactoryBean.setSecurityManager(securityManager);
        Map<String, String> filterChainDefinitionMap = new LinkedHashMap<>();
        filterChainDefinitionMap.put("/favicon.ico", "anon");
        filterChainDefinitionMap.put("/logout", "logout");
        filterChainDefinitionMap.put("/login", "enhancedAuthc");
        filterChainDefinitionMap.put("/static/**", "anon");
        filterChainDefinitionMap.put("/uploads/**", "anon");
        filterChainDefinitionMap.put("/api/**", "anon");
        filterChainDefinitionMap.put("/sys/**", "anon");
        filterChainDefinitionMap.put("/err/**", "anon");
        filterChainDefinitionMap.put("/ad/**", "anon");
        filterChainDefinitionMap.put("/", "anon");
        filterChainDefinitionMap.put("/index", "anon");
        filterChainDefinitionMap.put("/", "anon");
        filterChainDefinitionMap.put("/**", "authc");
        shiroFilterFactoryBean.setLoginUrl("/login");
        shiroFilterFactoryBean.setSuccessUrl("/admin");
        shiroFilterFactoryBean.setFilterChainDefinitionMap(filterChainDefinitionMap);
        return shiroFilterFactoryBean;
    }

    @Bean
    public SecurityManager securityManager(List<Realm> realms) {
        DefaultWebSecurityManager securityManager = new DefaultWebSecurityManager();
        securityManager.setRealms(realms);
        ((ModularRealmAuthenticator) securityManager.getAuthenticator()).setAuthenticationStrategy(new AnyoneSuccessfulStrategy());
        return securityManager;
    }

    @Bean
    public Realm usernamePasswordRealm() {
        UsernamePasswordRealm realm = new UsernamePasswordRealm();
        realm.setCredentialsMatcher((t, i) -> {
            try {
                String pass = String.copyValueOf((char[]) t.getCredentials())
                    + new String(((SimpleAuthenticationInfo) i).getCredentialsSalt().getBytes(), "UTF8");
                return DigestUtils.sha256Hex(pass).equals(i.getCredentials());
            } catch (UnsupportedEncodingException e) {
                return false;
            }
        });
        return realm;
    }

    public Map<String, Filter> userShiroFilters(UserService userService) {
        Map<String, Filter> filterMap = new HashMap<>();
        EnhancedAuthenticationFilter filter = new EnhancedAuthenticationFilter();
        filter.setAuthenticator(userService);
        filterMap.put("enhancedAuthc", filter);
        return filterMap;
    }
}
