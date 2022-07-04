package my.demo.webservice.controller;

import my.demo.webservice.common.web.ResponseEntity;
import my.demo.webservice.service.MonitorApiService;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 监控推流、云直播相关
 */
@RestController
@RequestMapping("/monitor")
@RequiresAuthentication
public class MonitorApiController {

    @Autowired
    MonitorApiService monitorApiService;

    /**
     * 控制监控开始推流
     */
    @PostMapping(value = "/stream/push")
    @RequiresRoles(value = {"zbr", "zbdl"}, logical = Logical.OR)
    public ResponseEntity<Object> pushMonitorStream() {
        monitorApiService.startPush();
        return ResponseEntity.ok();
    }

    /**
     * 控制监控停止推流
     */
    @PostMapping(value = "/stream/stop")
    @RequiresRoles(value = {"zbr", "zbdl"}, logical = Logical.OR)
    public Object stopPushMonitorStream() {
        monitorApiService.stopPush();
        return ResponseEntity.ok();
    }

    /**
     * 获取监控直播地址
     */
    @PostMapping(value = "/stream/live/url")
    public ResponseEntity<String> getMonitorStreamLiveUrl() {
        return ResponseEntity.ok(monitorApiService.getLiveUrl());
    }

    /**
     * 获取rtmp推流地址
     */
    @PostMapping(value = "/stream/push/url")
    public ResponseEntity<String> getMonitorStreamRtmpPushUrl() {
        return ResponseEntity.ok(monitorApiService.genRtmpPushUrl());
    }

}