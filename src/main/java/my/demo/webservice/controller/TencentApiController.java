package my.demo.webservice.controller;

import my.demo.webservice.common.web.ResponseEntity;
import my.demo.webservice.model.dto.GetUserSigReq;
import my.demo.webservice.service.TencentApiService;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

/**
 * 腾讯音视频相关
 */
@RestController
@RequestMapping("/tencent/trtc")
@RequiresAuthentication
public class TencentApiController {

    @Autowired
    TencentApiService tencentApiService;

    /**
     * 获取腾讯音视频服务签名
     */
    @PostMapping(value = "/getUserSig")
    public ResponseEntity<String> getUserSig(@Valid @RequestBody GetUserSigReq req) {
        return ResponseEntity.ok(tencentApiService.genProUserSig(req.getUserId()));
    }
}