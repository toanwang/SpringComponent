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
        Class<?> targetClass = bean.getClass();
        Class<?> proxyClass = this.buddy
                .subclass(targetClass, ConstructorStrategy.Default.DEFAULT_CONSTRUCTOR)
                .method(ElementMatchers.isPublic()).intercept(InvocationHandlerAdapter.of(
                        new InvocationHandler() {
                            public Object invoke(Object o, Method method, Object[] objects) throws Throwable {
                                return handler.invoke(bean, method, objects);
                            }
                        }
                ))
                .make()
                .load(targetClass.getClassLoader()).getLoaded();
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
