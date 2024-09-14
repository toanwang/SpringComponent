package org.summer.ioc;


import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

/**
 * Unit test for simple App.
 */
public class ResourceResolverTest{
    @Test
    public void scanClass() throws IOException, URISyntaxException {
        ResourceResolver resourceResolver = new ResourceResolver("org.summer.scan");
        List<String> collectors = resourceResolver.scan();
        String[] listClasses = new String[] {
                // list of some scan classes:
                "org.summer.scan.convert.ValueConverterBean", //
                "org.summer.scan.destroy.AnnotationDestroyBean", //
                "org.summer.scan.init.SpecifyInitConfiguration", //
                "org.summer.scan.proxy.OriginBean", //
                "org.summer.scan.proxy.FirstProxyBeanPostProcessor", //
                "org.summer.scan.proxy.SecondProxyBeanPostProcessor", //
                "org.summer.scan.nested.OuterBean", //
                "org.summer.scan.nested.OuterBean$NestedBean", //
                "org.summer.scan.sub1.Sub1Bean", //
                "org.summer.scan.sub1.sub2.Sub2Bean", //
                "org.summer.scan.sub1.sub2.sub3.Sub3Bean", //
        };

        for (String clazz : listClasses) {
            System.out.println(collectors.contains(clazz));
        }
    }
}
