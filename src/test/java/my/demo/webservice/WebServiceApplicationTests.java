package my.demo.webservice;

import cn.hutool.core.date.DateUtil;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class WebServiceApplicationTests {

    @Test
    void contextLoads() {
        System.out.println(" time " + DateUtil.date().getTime() / 1000);
    }
}
