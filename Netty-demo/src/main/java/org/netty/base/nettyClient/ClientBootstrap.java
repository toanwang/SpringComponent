package org.netty.base.nettyClient;

import org.netty.base.HelloService;

public class ClientBootstrap {
    public static final String providerName = "HelloService#";

    public static void main(String[] args) throws InterruptedException {
        NettyClient customer = new NettyClient();
        HelloService service = (HelloService) customer.getBean(HelloService.class, providerName);
        for(; ; ){
            Thread.sleep(2 * 1000);
            String res = service.hello(" 你好,dubbo");
            System.out.println("调用结果 " + res);
        }
    }
}
