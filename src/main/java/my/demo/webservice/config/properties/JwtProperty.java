package my.demo.webservice.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@ConfigurationProperties(prefix = "jwt")
@Component
public class JwtProperty {
  
  private Integer expireTime;
  private String secretKey;
  private String tokenName;
  private String defaultPwd;
}