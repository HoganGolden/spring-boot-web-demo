package my.demo.webservice.config;

import my.demo.webservice.filter.RequestFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;

@Configuration
public class GlobalConfig {

    @Bean
    public FilterRegistrationBean<RequestFilter> requestFilter() {

        FilterRegistrationBean<RequestFilter> filter = new FilterRegistrationBean<>();

        filter.setOrder(Ordered.HIGHEST_PRECEDENCE + 9);
        filter.setFilter(new RequestFilter());
        filter.addUrlPatterns("/*");

        return filter;
    }

    @Bean
    public FilterRegistrationBean<CorsFilter> corsFilter() {

        FilterRegistrationBean<CorsFilter> filter = new FilterRegistrationBean<>();
        filter.setFilter(new CorsFilter());
        filter.addUrlPatterns("/*");

        return filter;
    }

}