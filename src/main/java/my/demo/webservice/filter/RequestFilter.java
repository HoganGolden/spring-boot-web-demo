package my.demo.webservice.filter;

import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;

/**
 * Web请求过滤器
 */
@Slf4j
public class RequestFilter extends OncePerRequestFilter {

    /**
     * 设置响应头，响应头名字（惯例是大写）
     */
    private static final String REQUEST_ID_HEADER = "RequestId";

    /**
     * 设置到 MDC 里的key
     */
    public static final String REQUEST_ID = "requestId";
    public static final String REQUEST_IP = "ip";

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse rsp, FilterChain filterChain) throws ServletException, IOException {

        String requestId = req.getHeader(REQUEST_ID_HEADER);
        if (requestId == null) {
            requestId = UUID.randomUUID().toString();
        }
        String ip = getRequestSourceIp(req);

        MDC.put(REQUEST_ID, requestId);
        MDC.put(REQUEST_IP, ip);

        rsp.setHeader(REQUEST_ID_HEADER, requestId);

        String url = req.getRequestURL().toString();
        String method = req.getMethod();
        log.info("收到请求：[{}] {}", method, url);

        // 继续下一个过滤器
        filterChain.doFilter(req, rsp);

        MDC.clear();
    }

    /**
     * 可能的请求头列表
     */
    private final String[] HEADER_LIST = {
            "HTTP_CLIENT_IP",
            "HTTP_X_FORWARDED_FOR",
            "HTTP_X_FORWARDED",
            "HTTP_FORWARDED",
            "X-FORWARDED-FOR",
            "Proxy-Client-IP",
            "WL-Proxy-Client-IP",
            "WL-Proxy-Client-IP",
    };

    /**
     * 获取请求来源的IP,可以适应前置部署有Nginx等反向代理软件等的情况. HTTP_CLIENT_IP 无法伪造，所以放在第一个
     * http://blog.csdn.net/songylwq/article/details/7701718
     */
    private String getRequestSourceIp(HttpServletRequest request) {
        String ip = "";

        // 根据不同的请求头部遍历 获取ip地址
        for (String header : HEADER_LIST) {
            ip = request.getHeader(header);
            if (!StrUtil.isEmpty(ip) && "unknown".equalsIgnoreCase(ip)) {
                break;
            }
        }

        // 如果所有的请求头部都没有获取到ip地址 则获取getRemoteAddr
        if (StrUtil.isEmpty(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }

        return ip;
    }
}
