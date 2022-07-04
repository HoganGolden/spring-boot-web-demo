package my.demo.webservice.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


/**
 * 项目启动时自动执行任务服务
 */
@Component
@Slf4j
public class AutoExecWhenBootService {

    @Autowired
    MonitorApiService monitorApiService;

    /**
     * 启动执行监控推流服务
     */
//    @PostConstruct
    public void monitorStreamStartPush() {
        log.info("AutoExecWhenBootService exec monitorStreamStartPush");
        monitorApiService.startPush();
    }
}
