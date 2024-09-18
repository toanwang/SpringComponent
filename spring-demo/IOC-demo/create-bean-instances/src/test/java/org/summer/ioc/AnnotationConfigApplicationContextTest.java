package org.summer.ioc;

import org.junit.jupiter.api.Test;
import org.summer.ioc.context.AnnotationConfigApplicationContext;
import org.summer.ioc.context.BeanDefinition;
import org.summer.ioc.imported.LocalDateConfiguration;
import org.summer.ioc.imported.ZonedDateConfiguration;
import org.summer.ioc.io.PropertyResolver;
import org.summer.ioc.scan.ScanApplication;
import org.summer.ioc.scan.annotation.CustomAnnotationBean;
import org.summer.ioc.scan.nested.OuterBean;
import org.summer.ioc.scan.primary.DogBean;
import org.summer.ioc.scan.primary.PersonBean;
import org.summer.ioc.scan.primary.StudentBean;
import org.summer.ioc.scan.primary.TeacherBean;
import org.summer.ioc.scan.sub1.Sub1Bean;
import org.summer.ioc.scan.sub1.sub2.Sub2Bean;
import org.summer.ioc.scan.sub1.sub2.sub3.Sub3Bean;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class AnnotationConfigApplicationContextTest {
    @Test
    public void testAnnotationConfigApplicationContext() throws IOException, URISyntaxException {
        AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(ScanApplication.class, createPropertyResolver());
        // @CustomAnnotation:
        assertNotNull(ctx.findBeanDefinition(CustomAnnotationBean.class));
        assertNotNull(ctx.findBeanDefinition("customAnnotation"));

        // @Import():
        assertNotNull(ctx.findBeanDefinition(LocalDateConfiguration.class));
        assertNotNull(ctx.findBeanDefinition("startLocalDate"));
        assertNotNull(ctx.findBeanDefinition("startLocalDateTime"));
        assertNotNull(ctx.findBeanDefinition(ZonedDateConfiguration.class));
        assertNotNull(ctx.findBeanDefinition("startZonedDateTime"));
        // nested:
        assertNotNull(ctx.findBeanDefinition(OuterBean.class));
        assertNotNull(ctx.findBeanDefinition(OuterBean.NestedBean.class));

        BeanDefinition studentDef = ctx.findBeanDefinition(StudentBean.class);
        BeanDefinition teacherDef = ctx.findBeanDefinition(TeacherBean.class);
        // 2 PersonBean:
        List<BeanDefinition> defs = ctx.findBeanDefinitions(PersonBean.class);
        assertSame(studentDef, defs.get(0));
        assertSame(teacherDef, defs.get(1));
        // 1 @Primary PersonBean:
        BeanDefinition personPrimaryDef = ctx.findBeanDefinition(PersonBean.class);
        assertSame(teacherDef, personPrimaryDef);
    }

    PropertyResolver createPropertyResolver() {
        Properties ps = new Properties();
        ps.put("app.title", "Scan App");
        ps.put("app.version", "v1.0");
        ps.put("jdbc.url", "jdbc:hsqldb:file:testdb.tmp");
        ps.put("jdbc.username", "sa");
        ps.put("jdbc.password", "");
        ps.put("convert.boolean", "true");
        ps.put("convert.byte", "123");
        ps.put("convert.short", "12345");
        ps.put("convert.integer", "1234567");
        ps.put("convert.long", "123456789000");
        ps.put("convert.float", "12345.6789");
        ps.put("convert.double", "123456789.87654321");
        ps.put("convert.localdate", "2023-03-29");
        ps.put("convert.localtime", "20:45:01");
        ps.put("convert.localdatetime", "2023-03-29T20:45:01");
        ps.put("convert.zoneddatetime", "2023-03-29T20:45:01+08:00[Asia/Shanghai]");
        ps.put("convert.duration", "P2DT3H4M");
        ps.put("convert.zoneid", "Asia/Shanghai");
        PropertyResolver pr = new PropertyResolver(ps);
        return pr;
    }


    @Test
    public void testCustomAnnotation() throws IOException, URISyntaxException {
        AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(ScanApplication.class, createPropertyResolver());
        assertNotNull(ctx.getBean(CustomAnnotationBean.class));
        assertNotNull(ctx.getBean("customAnnotation"));
    }

    @Test
    public void testImport() throws IOException, URISyntaxException {
        AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(ScanApplication.class, createPropertyResolver());
        assertNotNull(ctx.getBean(LocalDateConfiguration.class));
        assertNotNull(ctx.getBean("startLocalDate"));
        assertNotNull(ctx.getBean("startLocalDateTime"));
        assertNotNull(ctx.getBean(ZonedDateConfiguration.class));
        assertNotNull(ctx.getBean("startZonedDateTime"));
    }

    @Test
    public void testNested() throws IOException, URISyntaxException {
        AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(ScanApplication.class, createPropertyResolver());
        ctx.getBean(OuterBean.class);
        ctx.getBean(OuterBean.NestedBean.class);
    }

    @Test
    public void testPrimary() throws IOException, URISyntaxException {
        AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(ScanApplication.class, createPropertyResolver());
        PersonBean person = ctx.getBean(PersonBean.class);
        assertEquals(TeacherBean.class, person.getClass());
        DogBean dog = ctx.getBean(DogBean.class);
        assertEquals("Husky", dog.type);
    }

    @Test
    public void testSub() throws IOException, URISyntaxException {
        AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(ScanApplication.class, createPropertyResolver());
        ctx.getBean(Sub1Bean.class);
        ctx.getBean(Sub2Bean.class);
        ctx.getBean(Sub3Bean.class);
    }
}
