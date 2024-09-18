package org.summer.aop;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ProxyResolverTest {
    @Test
    public void testProxyResolver() {
        OriginBean originBean = new OriginBean();
        originBean.name = "Bob";

//        assertEquals("hello, Bob.", originBean.hello());
        System.out.println(originBean.hello());

        OriginBean proxy = new ProxyResolver().createProxy(originBean, new PoliteInvocationHandler());
        // Proxy类名,类似OriginBean$ByteBuddy$9hQwRy3T:
        System.out.println(proxy.getClass().getName());

        // proxy class, not origin class:
//        assertNotSame(OriginBean.class, proxy.getClass());
        // proxy.name is null:
//        assertNull(proxy.name);
        System.out.println(proxy.hello());
        System.out.println(proxy.morning());

        // 带@Polite:
//        assertEquals("Hello, Bob!", proxy.hello());
//        // 不带@Polite:
//        assertEquals("Morning, Bob.", proxy.morning());
    }
}
