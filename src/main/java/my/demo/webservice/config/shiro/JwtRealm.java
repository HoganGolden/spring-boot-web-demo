package my.demo.webservice.config.shiro;

import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;
import my.demo.webservice.common.web.Code;
import my.demo.webservice.config.properties.JwtProperty;
import my.demo.webservice.exception.ApiException;
import my.demo.webservice.model.dto.UserDto;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collections;

@Component
@Slf4j
public class JwtRealm extends AuthorizingRealm {

    @Override
    public boolean supports(AuthenticationToken token) {
        return token instanceof JwtToken;
    }

    @Autowired
    private JwtProperty jwtProperty;

    /**
     * 授权处理
     * 取得用户的权限、角色信息
     */
    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
        log.debug("doGetAuthorizationInfo");
        UserDto user = (UserDto) principals.fromRealm(getName()).iterator().next();
        if (user != null) {
            SimpleAuthorizationInfo info = new SimpleAuthorizationInfo();
            info.addRole(user.getUser_type());
            info.addStringPermissions(Collections.singleton(user.getUser_type()));
            return info;
        } else {
            return null;
        }
    }

    /**
     * 鉴权处理
     * 验证用户的JWT是否合法
     */
    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken auth) throws ApiException {

        // 取得并解密JWTToken
        String token = (String) auth.getCredentials();
        String userId = JwtUtil.getClaim(token, "user_id");
        String userUid = JwtUtil.getClaim(token, "user_uid");
        String userType = JwtUtil.getUserType(token);
        boolean verifyResult = JwtUtil.verify(token, jwtProperty.getSecretKey());
        log.debug("doGetAuthenticationInfo token=" + token);
        log.debug("doGetAuthenticationInfo userId=" + userId);
        log.debug("doGetAuthenticationInfo userType=" + userType);
        log.debug("doGetAuthenticationInfo userUid=" + userUid);
        log.debug("doGetAuthenticationInfo verifyResult =" + verifyResult);

        if (StrUtil.isBlank(userType) || StrUtil.isBlank(userId) || !verifyResult) {
            throw new ApiException(Code.UNAUTHORIZED, "TOKEN不合法");
        }

        UserDto user = UserDto.builder()
                .user_id(userId)
                .user_uid(userUid)
                .user_type(userType)
                .user_name(JwtUtil.getClaim(token, "user_name"))
                .real_name(JwtUtil.getClaim(token, "real_name"))
                .avatar(JwtUtil.getClaim(token, "avatar"))
                .build();

        // 返回AuthenticationInfo
        return new SimpleAuthenticationInfo(user, token, getName());
    }
}
