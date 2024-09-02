package com.toan.service.impl;

import com.alibaba.nacos.api.config.annotation.NacosValue;
import com.toan.dubbo.api.DemoService;
import org.apache.dubbo.config.annotation.DubboService;

import java.util.concurrent.atomic.AtomicLong;


@DubboService(version = "1.0.0")
public class DemoServiceImpl implements DemoService {

    @NacosValue(value = "${dubboParams}", autoRefreshed = true)
    private String dubboParams;

    private AtomicLong atomicLong = new AtomicLong(0);

    /**
     * 第一次调用时，睡眠时间为1秒，第二次调用时为900毫秒
     */
    @Override
    public String getString() {
        long l = atomicLong.incrementAndGet();
        System.out.println("atomicLong.incrementAndGet()：" + l);
        try {
            Thread.sleep(1000 - (100 * l));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return dubboParams;
    }
}
