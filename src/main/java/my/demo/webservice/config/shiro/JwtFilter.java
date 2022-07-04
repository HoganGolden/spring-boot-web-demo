package my.demo.webservice.config.shiro;

import cn.hutool.json.JSONUtil;
import my.demo.webservice.common.web.Code;
import my.demo.webservice.common.web.ResponseEntity;
import my.demo.webservice.config.properties.JwtProperty;
import my.demo.webservice.exception.ApiException;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.spring.web.ShiroFilterFactoryBean;
import org.apache.shiro.web.filter.authc.BasicHttpAuthenticationFilter;
import org.apache.shiro.web.filter.mgt.DefaultFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import javax.servlet.ServletOutputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * 代码的执行流程preHandle->isAccessAllowed
 * ->isLoginAttempt
 * ->executeLogin
 */
@Component
@Slf4j
public class JwtFilter extends BasicHttpAuthenticationFilter {

    @Autowired
    private JwtProperty jwtProperty;
    @Autowired
    private ApplicationContext context;

    /**
     * 判断用户是否想登入（检测header中是否有JWT字段即可）
     */
    @Override
    protected boolean isLoginAttempt(ServletRequest request, ServletResponse response) {
        HttpServletRequest req = (HttpServletRequest) request;
        String jwtToken = req.getHeader(jwtProperty.getTokenName());

        boolean isLoginAttempt = true;
        // 获取jwtFilter配置的路径鉴权授权规则
        Map<String, String> filterChainDefinitionMap = context.getBean(ShiroFilterFactoryBean.class).getFilterChainDefinitionMap();
        for (String key : filterChainDefinitionMap.keySet()) {
            String value = filterChainDefinitionMap.get(key);
            // 获取 - 指定url可以匿名访问的规则
            if (DefaultFilter.anon.name().equals(value)) {
                if (key.equals(req.getRequestURI())) {
                    isLoginAttempt = false;
                    break;
                }
            }
        }

        log.info("取得TOKEN，request getRequestURI ={}, filterChainDefinitionMap = {}", req.getRequestURI(), filterChainDefinitionMap);
        log.info("isLoginAttempt = {}, 取得TOKEN，token名={}, token值={}", isLoginAttempt, jwtProperty.getTokenName(), jwtToken);
        return isLoginAttempt;
    }

    @Override
    protected boolean executeLogin(ServletRequest request, ServletResponse response) {

        log.info("executeLogin");

        // 取得TOKEN
        HttpServletRequest req = (HttpServletRequest) request;
        String jwtToken = req.getHeader(jwtProperty.getTokenName());
        // 校验token
        boolean verifyResult = JwtUtil.verify(jwtToken, jwtProperty.getSecretKey());
        log.info("executeLogin verifyResult = " + verifyResult);
        if (!verifyResult) {
            return false;
        }
        // 提交Realm进行登录验证
        JwtToken token = new JwtToken(jwtToken);
        // 执行登录，以便之后通过使用shiro注解控制授权
        getSubject(request, response).login(token);

        // 设置当前的登录用户
        String userId = JwtUtil.getClaim(jwtToken, "user_id");
        String userUid = JwtUtil.getClaim(jwtToken, "user_uid");
        String userType = JwtUtil.getUserType(jwtToken);

        request.setAttribute("userUid", userUid);
        request.setAttribute("userId", userId);
        request.setAttribute("userType", userType);

        // 如果没有抛异常表示登录成功
        return true;
    }

    /**
     * 如果没有登录请求，返回true
     */
    @Override
    protected boolean isAccessAllowed(ServletRequest request, ServletResponse response, Object mappedValue) {

        if (isLoginAttempt(request, response)) {
            boolean isLogin = false;
            // 用户想登录，则执行登录方法
            try {
                isLogin = this.executeLogin(request, response);
            } catch (Exception e) {
                log.error("JwtFilter isAccessAllowed error:", e);
            }
            if (!isLogin) {
                // 返回异常相应
                responseError(response, Code.UNAUTHORIZED.getValue(), "访问未授权");
                return false;
            }
        }

        return true;
    }

    /**
     * 返回异常相应
     */
    private void responseError(ServletResponse response, int code, String message) {

        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json; charset=utf-8");
        ServletOutputStream output = null;

        try {
            output = response.getOutputStream();
            ResponseEntity<Object> responseEntity = ResponseEntity.fail(code, message);
            output.write(JSONUtil.toJsonStr(responseEntity).getBytes(StandardCharsets.UTF_8));
            output.flush();
        } catch (IOException e) {
            throw new ApiException(Code.INTERNAL_ERROR);
        } finally {
            if (null != output) {
                try {
                    output.close();
                } catch (IOException e) {
                    log.error("responseError error:", e);
                }
            }
        }
    }
}
