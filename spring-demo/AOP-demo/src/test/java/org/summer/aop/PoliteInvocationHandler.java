package org.summer.aop;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

public class PoliteInvocationHandler implements InvocationHandler {
    @Override
    public Object invoke(Object bean, Method method, Object[] args) throws Throwable {
        // 如果是有polite注解
        if(method.getAnnotation(Polite.class) != null){
            // 获取方法执行结果
            String ret = (String) method.invoke(bean, args);
            // 处理结果
            if(ret.endsWith(".")){
                ret = ret.substring(0, ret.length()-1) + "!";
            }
            // 返回新的结果
            return ret;
        }
        // 否则执行原方法
        return method.invoke(bean, args);
    }
}
