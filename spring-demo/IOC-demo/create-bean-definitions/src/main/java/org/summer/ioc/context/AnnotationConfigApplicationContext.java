package org.summer.ioc.context;

import com.sun.istack.internal.Nullable;
import org.summer.ioc.annotation.*;
import org.summer.ioc.exception.BeanBaseException;
import org.summer.ioc.io.PropertyResolver;
import org.summer.ioc.io.ResourceResolver;
import org.summer.ioc.utils.ClassUtils;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URISyntaxException;
import java.util.*;
import java.util.stream.Collectors;

public class AnnotationConfigApplicationContext {

    protected final PropertyResolver propertyResolver;
    protected final Map<String, BeanDefinition> beans;
    public AnnotationConfigApplicationContext(Class<?> configClass, PropertyResolver propertyResolver) throws IOException, URISyntaxException {
        this.propertyResolver = propertyResolver;
        final Set<String> beanClassNames = scanForClassName(configClass);
        this.beans = createBeanDefinitions(beanClassNames);
    }

    Map<String, BeanDefinition> createBeanDefinitions(Set<String> classNameSet){
        Map<String, BeanDefinition> defs = new HashMap<>();
        for(String className: classNameSet){
            Class<?> clazz = null;
            try{
                clazz = Class.forName(className);
            }catch (ClassNotFoundException e){
                throw new BeanBaseException(e);
            }

            if(clazz.isAnnotation() || clazz.isEnum() || clazz.isInstance(null)){
                continue;
            }

            Component component = ClassUtils.findAnnotation(clazz, Component.class);
            if (component != null){
                String beanName = ClassUtils.getBeanName(clazz);
                BeanDefinition def = new BeanDefinition(beanName, clazz, getSuitableConstructor(clazz), getOrder(clazz),
                        clazz.isAnnotationPresent(Primary.class), null, null,
                        ClassUtils.findAnnotationMethod(clazz, PostConstruct.class),
                        ClassUtils.findAnnotationMethod(clazz, PreDestroy.class));
                addBeanDefinitions(defs, def);
                Configuration configuration = ClassUtils.findAnnotation(clazz, Configuration.class);
                if (configuration != null) {
                    scanFactoryMethods(beanName, clazz, defs);
                }
            }
        }
        return defs;
    }

    void addBeanDefinitions(Map<String, BeanDefinition> defs, BeanDefinition def) {
        if (defs.put(def.getName(), def) != null) {
            throw new BeanBaseException("Duplicate bean name: " + def.getName());
        }
    }

    Constructor<?> getSuitableConstructor(Class<?> clazz){
        Constructor<?>[] cons = clazz.getConstructors();
        if (cons.length == 0){
            cons = clazz.getDeclaredConstructors();
            if (cons.length != 1){
                throw new BeanBaseException("More than one constructor found in class " + clazz.getName());
            }
        }
        if (cons.length != 1){
            throw new BeanBaseException("More than one constructor found in class " + clazz.getName());
        }
        return cons[0];
    }

    protected Set<String> scanForClassName(Class<?> configClass) throws IOException, URISyntaxException {
        ComponentScan scan = ClassUtils.findAnnotation(configClass, ComponentScan.class);
        final String[] scanPackages = scan == null || scan.value().length == 0 ? new String[] { configClass.getPackage().getName() } : scan.value();
        Set<String> classNameSet = new HashSet<>();

        for(String pkg: scanPackages){
            ResourceResolver rr = new ResourceResolver(pkg);
            List<String> classList = rr.scan();

            classNameSet.addAll(classList);
        }

        Import importConfig = configClass.getAnnotation(Import.class);
        if(importConfig != null){
            for (Class<?> importConfigClass: importConfig.value()){
                String importClassName = importConfigClass.getName();
                if (classNameSet.contains(importClassName)){
                    System.out.println(importClassName + " already been scanned");
                }else{
                    classNameSet.add(importClassName);
                }
            }
        }

        return classNameSet;
    }

    int getOrder(Class<?> clazz) {
        Order order = clazz.getAnnotation(Order.class);
        return order == null ? Integer.MAX_VALUE : order.value();
    }

    int getOrder(Method method) {
        Order order = method.getAnnotation(Order.class);
        return order == null ? Integer.MAX_VALUE : order.value();
    }

    void scanFactoryMethods(String factoryBeanName, Class<?> clazz, Map<String, BeanDefinition> defs) {
        for (Method method : clazz.getDeclaredMethods()) {
            Bean bean = method.getAnnotation(Bean.class);
            if (bean != null) {
                int mod = method.getModifiers();
                if (Modifier.isAbstract(mod)) {
                    throw new BeanBaseException("@Bean method " + clazz.getName() + "." + method.getName() + " must not be abstract.");
                }
                if (Modifier.isFinal(mod)) {
                    throw new BeanBaseException("@Bean method " + clazz.getName() + "." + method.getName() + " must not be final.");
                }
                if (Modifier.isPrivate(mod)) {
                    throw new BeanBaseException("@Bean method " + clazz.getName() + "." + method.getName() + " must not be private.");
                }
                Class<?> beanClass = method.getReturnType();
                if (beanClass.isPrimitive()) {
                    throw new BeanBaseException("@Bean method " + clazz.getName() + "." + method.getName() + " must not return primitive type.");
                }
                if (beanClass == void.class || beanClass == Void.class) {
                    throw new BeanBaseException("@Bean method " + clazz.getName() + "." + method.getName() + " must not return void.");
                }
                BeanDefinition def = new BeanDefinition(ClassUtils.getBeanName(method), beanClass, factoryBeanName, method, getOrder(method),
                        method.isAnnotationPresent(Primary.class),
                        // init method:
                        bean.initMethod().isEmpty() ? null : bean.initMethod(),
                        // destroy method:
                        bean.destroyMethod().isEmpty() ? null : bean.destroyMethod(),
                        // @PostConstruct / @PreDestroy method:
                        null, null);
                addBeanDefinitions(defs, def);
            }
        }
    }

    @Nullable
    public BeanDefinition findBeanDefinition(String name) {
        return this.beans.get(name);
    }

    @Nullable
    public BeanDefinition findBeanDefinition(Class<?> type) {
        List<BeanDefinition> defs = findBeanDefinitions(type);
        if (defs.isEmpty()) {
            return null;
        }
        if (defs.size() == 1) {
            return defs.get(0);
        }
        // more than 1 beans, require @Primary:
        List<BeanDefinition> primaryDefs = defs.stream().filter(def -> def.isPrimary()).collect(Collectors.toList());
        if (primaryDefs.size() == 1) {
            return primaryDefs.get(0);
        }
        if (primaryDefs.isEmpty()) {
            throw new BeanBaseException(String.format("Multiple bean with type '%s' found, but no @Primary specified.", type.getName()));
        } else {
            throw new BeanBaseException(String.format("Multiple bean with type '%s' found, and multiple @Primary specified.", type.getName()));
        }
    }
    public List<BeanDefinition> findBeanDefinitions(Class<?> type) {
        return this.beans.values().stream()
                // filter by type and sub-type:
                .filter(def -> type.isAssignableFrom(def.getBeanClass()))
                // 排序:
                .sorted().collect(Collectors.toList());
    }
}
