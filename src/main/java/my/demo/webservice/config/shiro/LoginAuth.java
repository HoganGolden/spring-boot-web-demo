package my.demo.webservice.config.shiro;

import my.demo.webservice.model.dto.UserDto;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

@Data
@NoArgsConstructor
public class LoginAuth implements Serializable {

    private static final long serialVersionUID = 3830542577385426898L;

    private UserDto user;
    private String type;

    private Set<String> roles = new HashSet<>();    //用户所有角色值，用于shiro做角色权限的判断
    private Set<String> perms = new HashSet<>();    //用户所有权限值，用于shiro做资源权限的判断

    public LoginAuth(UserDto user, String type) {
        this.user = user;
        this.type = type;
        this.roles.add(type);
    }

}
