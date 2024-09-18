package org.summer.ioc.utils;

import com.sun.istack.internal.Nullable;
import org.summer.ioc.annotation.Bean;
import org.summer.ioc.annotation.Component;
import org.summer.ioc.exception.BeanBaseException;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class ClassUtils {
    public static <A extends Annotation> A findAnnotation(Class<?> target, Class<A> annoClass) {
        A a = target.getAnnotation(annoClass);
        for(Annotation anno: target.getAnnotations()){
            Class<? extends Annotation> annoType = anno.annotationType();
            if (!annoType.getPackage().getName().equals("java.lang.annotation")){
                A found = findAnnotation(annoType, annoClass);
                if (found != null){
                    if (a != null){
                        throw new BeanBaseException("Duplicate @" + annoClass.getSimpleName() + " found on class " + target.getSimpleName());
                    }
                    a = found;
                }
            }
        }
        return a;
    }

    public static String getBeanName(Class<?> clazz){
        String name = "";
        Component component = clazz.getAnnotation(Component.class);
        if(component != null){
            name = component.value();
        }else{
            for(Annotation anno: clazz.getAnnotations()){
                if(findAnnotation(anno.annotationType(), Component.class) != null){
                    try{
                        name = (String) anno.annotationType().getMethod("value").invoke(anno);
                    }catch (ReflectiveOperationException e){
                        throw new BeanBaseException("cannot get annotation value", e);
                    }
                }
            }
        }
        if(name.isEmpty()){
            name = clazz.getSimpleName();
            name = Character.toLowerCase(name.charAt(0)) + name.substring(1);
        }
        return name;
    }

    public static String getBeanName(Method method) {
        Bean bean = method.getAnnotation(Bean.class);
        String name = bean.value();
        if (name.isEmpty()) {
            name = method.getName();
        }
        return name;
    }


    @Nullable
    public static Method findAnnotationMethod(Class<?> clazz, Class<? extends Annotation> annoClass) {
        // try get declared method:
        List<Method> ms = Arrays.stream(clazz.getDeclaredMethods()).filter(m -> m.isAnnotationPresent(annoClass)).map(m -> {
            if (m.getParameterCount() != 0) {
                throw new BeanBaseException(
                        String.format("Method '%s' with @%s must not have argument: %s", m.getName(), annoClass.getSimpleName(), clazz.getName()));
            }
            return m;
        }).collect(Collectors.toList());
        if (ms.isEmpty()) {
            return null;
        }
        if (ms.size() == 1) {
            return ms.get(0);
        }
        throw new BeanBaseException(String.format("Multiple methods with @%s found in class: %s", annoClass.getSimpleName(), clazz.getName()));
    }
}
