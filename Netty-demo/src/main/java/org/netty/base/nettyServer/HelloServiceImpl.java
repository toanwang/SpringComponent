package org.netty.base.nettyServer;

import cn.hutool.core.util.StrUtil;
import org.netty.base.HelloService;

public class HelloServiceImpl implements HelloService {
    public static int count = 0;
    @Override
    public String hello(String mes){
        System.out.println("收到客户端消息 " + mes);
        if (StrUtil.isEmpty(mes)){
            return "你好客户端，我已经收到你的消息[" + mes + "]第" + (++count) + " 次";
        }else{
            return "你好客户端，我已经收到你的消息";
        }
    }
}
