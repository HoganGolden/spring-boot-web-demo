package my.demo.webservice.config.shiro;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTDecodeException;
import com.auth0.jwt.interfaces.DecodedJWT;

import java.util.Date;

public class JwtUtil {

    /**
     * 取得claim
     *
     * @param token
     * @return
     */
    public static String getClaim(String token, String name) {
        try {
            DecodedJWT jwt = JWT.decode(token);
            return jwt.getClaim(name).asString();
        } catch (JWTDecodeException e) {
            return null;
        }
    }

    /**
     * 取得登录类型
     */
    public static String getUserType(String token) {
        try {
            DecodedJWT jwt = JWT.decode(token);
            return jwt.getClaim("user_type").asString();
        } catch (JWTDecodeException e) {
            return null;
        }
    }

    /**
     * 校验TOKEN是否正确
     */
    public static boolean verify(String token, String secret) {

        try {
            Algorithm algorithm = Algorithm.HMAC256(secret);
            JWTVerifier verifier = JWT.require(algorithm).build();
            verifier.verify(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 生成JWT签名
     */
    public static String sign(String userId, Integer userType, String secret, Integer expiryTime) {

        Date date = new Date(System.currentTimeMillis() + expiryTime);
        Algorithm algorithm = Algorithm.HMAC256(secret);
        return JWT.create()
                .withClaim("userId", userId)
                .withClaim("userType", userType)
                .withExpiresAt(date)
                .sign(algorithm);
    }

}
