package org.summer.aop;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.dynamic.scaffold.subclass.ConstructorStrategy;
import net.bytebuddy.implementation.InvocationHandlerAdapter;
import net.bytebuddy.matcher.ElementMatchers;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

public class ProxyResolver {
    ByteBuddy buddy = new ByteBuddy();

    public <T> T createProxy(final T bean, final InvocationHandler handler){
        // 获取原始类对象
        Class<?> targetClass = bean.getClass();
        Class<?> proxyClass = this.buddy
                // 使用指定构造策略，构造一个继承targetClass的子类
                .subclass(targetClass, ConstructorStrategy.Default.DEFAULT_CONSTRUCTOR)
                // 选择目标类中的公共方法
                .method(ElementMatchers.isPublic())
                // 设置拦截器，是实现InvocationHandler接口的类
                .intercept(InvocationHandlerAdapter.of(
                        new InvocationHandler() {
                            // 拦截器的invoke实现，对象+方法+参数，此处的invoke传入的是proxy实例
                            public Object invoke(Object o, Method method, Object[] objects) throws Throwable {
                                // 内层invoke将调用转发到原始Bean
                                return handler.invoke(bean, method, objects);
                            }
                        }
                ))
                // 创建代理类
                .make()
                // 使用目标类相同的类加载器
                .load(targetClass.getClassLoader())
                // 将动态类加载到JVM，并且获得class对象
                .getLoaded();
        Object proxy;
        try{
            proxy = proxyClass.getConstructor().newInstance();
        }catch (RuntimeException e){
            throw e;
        }catch (Exception e){
            throw new RuntimeException(e);
        }
        return (T) proxy;
    }
}
