package my.demo.webservice.config.shiro;

import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.mgt.DefaultSessionStorageEvaluator;
import org.apache.shiro.mgt.DefaultSubjectDAO;
import org.apache.shiro.spring.LifecycleBeanPostProcessor;
import org.apache.shiro.spring.security.interceptor.AuthorizationAttributeSourceAdvisor;
import org.apache.shiro.spring.web.ShiroFilterFactoryBean;
import org.apache.shiro.web.filter.mgt.DefaultFilter;
import org.apache.shiro.web.mgt.DefaultWebSecurityManager;
import org.springframework.aop.framework.autoproxy.DefaultAdvisorAutoProxyCreator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;

import javax.servlet.Filter;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Configuration
public class ShiroConfig {

    @Value("${shiro.anonymousUrls}")
    private List<String> anonymousUrls;

    @Autowired
    private JwtFilter jwtFilter;

    @Bean
    public static LifecycleBeanPostProcessor lifecycleBeanPostProcessor() {
        return new LifecycleBeanPostProcessor();
    }

    @Bean
    @DependsOn("lifecycleBeanPostProcessor")
    public static DefaultAdvisorAutoProxyCreator getLifecycleBeanPostProcessor() {
        DefaultAdvisorAutoProxyCreator defaultAdvisorAutoProxyCreator = new DefaultAdvisorAutoProxyCreator();
        defaultAdvisorAutoProxyCreator.setUsePrefix(true);
        // 强制使用cglib，防止重复代理和可能引起代理出错的问题
        defaultAdvisorAutoProxyCreator.setProxyTargetClass(true);
        return defaultAdvisorAutoProxyCreator;
    }

    @Bean
    public AuthorizationAttributeSourceAdvisor authorizationAttributeSourceAdvisor(DefaultWebSecurityManager securityManager) {
        AuthorizationAttributeSourceAdvisor advisor = new AuthorizationAttributeSourceAdvisor();
        advisor.setSecurityManager(securityManager);
        return advisor;
    }

    @Bean("securityManager")
    public DefaultWebSecurityManager getManager(JwtRealm jwtRealm) {

        // 设定自定义JWTRealm
        DefaultWebSecurityManager securityManager = new DefaultWebSecurityManager();
        securityManager.setRealm(jwtRealm);

        // 关闭shiro自带的session
        DefaultSubjectDAO subjectDAO = new DefaultSubjectDAO();
        DefaultSessionStorageEvaluator defaultSessionStorageEvaluator = new DefaultSessionStorageEvaluator();
        defaultSessionStorageEvaluator.setSessionStorageEnabled(false);
        subjectDAO.setSessionStorageEvaluator(defaultSessionStorageEvaluator);
        securityManager.setSubjectDAO(subjectDAO);

        //自定义缓存管理
        return securityManager;
    }

    /**
     * 进行shiro校验之前的工作
     * setFilterChainDefinitionMap anon 的配置会覆盖{@link JwtFilter}中的校验，但不会覆盖shiro权限注释的校验，如：@RequiresAuthentication
     */
    @Bean(name = "shiroFilter")
    public ShiroFilterFactoryBean factory(@Qualifier("securityManager") DefaultWebSecurityManager securityManager,
                                          @Qualifier("jwtFilterRegBean") FilterRegistrationBean<JwtFilter> jwtFilterRegBean) {

        ShiroFilterFactoryBean shiroFilter = new ShiroFilterFactoryBean();
        shiroFilter.setSecurityManager(securityManager);

        // 添加jwt过滤器
        Map<String, Filter> filterMap = new HashMap<>();
        filterMap.put("jwt", jwtFilterRegBean.getFilter());
        shiroFilter.setFilters(filterMap);

        log.info("shiroFilter 取得Shiro权限校验白名单列表，长度={}", anonymousUrls.size());

        //拦截器
        Map<String, String> filterRuleMap = new LinkedHashMap<>();
        for (String anonymousUrl : anonymousUrls) {
            filterRuleMap.put(anonymousUrl, DefaultFilter.anon.name());
            log.info("Shiro权限校验白名单，URL=" + anonymousUrl);
        }

        // 其他所有路由都需要通过JwtFilter，该配置需要放在最后一个
        filterRuleMap.put("/**", "jwt");

        shiroFilter.setFilterChainDefinitionMap(filterRuleMap);

        // 返回Filter设置
        return shiroFilter;
    }

    /**
     * 注册jwtFilter
     */
    @Bean
    public FilterRegistrationBean<JwtFilter> jwtFilterRegBean() {
        FilterRegistrationBean<JwtFilter> filterRegistrationBean = new FilterRegistrationBean<>();
        // 添加JwtFilter  并设置为未注册状态
        filterRegistrationBean.setFilter(jwtFilter);
        // 设置为true时，所有URL都会被自定义Filter过滤，而不是Shiro中配置的一部分URL；JwtFilter中的getSubject(request, response).login 方法会执行失败
        filterRegistrationBean.setEnabled(false);
        return filterRegistrationBean;
    }

}
