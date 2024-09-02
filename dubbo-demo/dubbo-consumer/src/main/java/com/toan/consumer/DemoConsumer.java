package com.toan.consumer;

import com.toan.dubbo.api.DemoService;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DemoConsumer {

    // 引用远程服务，超时时间1000ms，重试次数1次
    @DubboReference(version = "1.0.0", timeout = 1000, retries = 1)
    private DemoService demoService;

    @GetMapping("/toan-test")
    public String tyronTest() {
        return demoService.getString();
    }

}
