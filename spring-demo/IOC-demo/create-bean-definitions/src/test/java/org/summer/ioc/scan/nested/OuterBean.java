package org.summer.ioc.scan.nested;

import org.summer.ioc.annotation.Component;

@Component
public class OuterBean {

    @Component
    public static class NestedBean {

    }
}
