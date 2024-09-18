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
import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.net.URISyntaxException;
import java.util.*;
import java.util.stream.Collectors;

public class AnnotationConfigApplicationContext {

    protected final PropertyResolver propertyResolver;
    protected final Map<String, BeanDefinition> beans;
    private Set<String> creatingBeanNames;

    public AnnotationConfigApplicationContext(Class<?> configClass, PropertyResolver propertyResolver) throws IOException, URISyntaxException {
        this.propertyResolver = propertyResolver;
        // 扫描路径下的所有文件
        final Set<String> beanClassNames = scanForClassName(configClass);
        // 创建bean定义
        this.beans = createBeanDefinitions(beanClassNames);

        // 检测循环依赖
        this.creatingBeanNames = new HashSet<>();

        // 先处理configuration注解
        this.beans.values().stream()
                .filter(this::isConfigurationDefinition)
                .sorted().map(def -> {
                    createBeanAsEarlySingleton(def);
                    return def.getName();
                }).collect(Collectors.toList());

        createNormalBeans();

        // 依赖注入；初始化
        this.beans.values().forEach(def -> {
            injectBean(def);
        });
        // 调用int方法
        this.beans.values().forEach(def -> {
            initBean(def);
        });
    }

    /**
     * 注入依赖,不需要init方法
     */
    void injectBean(BeanDefinition def){
        try {
            injectProperties(def, def.getBeanClass(), def.getInstance());
        } catch (ReflectiveOperationException e) {
            throw new BeanBaseException(e);
        }
    }

    /**
     * 调用init方法
     */
    void initBean(BeanDefinition def) {
        // 调用init方法:
        callMethod(def.getInstance(), def.getInitMethod(), def.getInitMethodName());
    }

    /**
     * 注入属性
     */
    void injectProperties(BeanDefinition def, Class<?> clazz, Object bean) throws ReflectiveOperationException {
        // 在当前类查找Field和Method并注入:
        for (Field f : clazz.getDeclaredFields()) {
            tryInjectProperties(def, clazz, bean, f);
        }
        for (Method m : clazz.getDeclaredMethods()) {
            tryInjectProperties(def, clazz, bean, m);
        }
        // 在父类查找Field和Method并注入:
        Class<?> superClazz = clazz.getSuperclass();
        if (superClazz != null) {
            injectProperties(def, superClazz, bean);
        }
    }

    /**
     * 注入单个属性
     */
    void tryInjectProperties(BeanDefinition def, Class<?> clazz, Object bean, AccessibleObject acc) throws ReflectiveOperationException {
        Value value = acc.getAnnotation(Value.class);
        Autowired autowired = acc.getAnnotation(Autowired.class);
        if (value == null && autowired == null) {
            return;
        }

        Field field = null;
        Method method = null;
        if (acc instanceof Field) {
            Field f = (Field) acc;
            checkFieldOrMethod(f);
            f.setAccessible(true);
            field = f;
        }
        if (acc instanceof Method) {
            Method m = (Method) acc;
            checkFieldOrMethod(m);
            if (m.getParameters().length != 1) {
                throw new BeanBaseException(
                        String.format("Cannot inject a non-setter method %s for bean '%s': %s", m.getName(), def.getName(), def.getBeanClass().getName()));
            }
            m.setAccessible(true);
            method = m;
        }

        String accessibleName = field != null ? field.getName() : method.getName();
        Class<?> accessibleType = field != null ? field.getType() : method.getParameterTypes()[0];

        if (value != null && autowired != null) {
            throw new BeanBaseException(String.format("Cannot specify both @Autowired and @Value when inject %s.%s for bean '%s': %s",
                    clazz.getSimpleName(), accessibleName, def.getName(), def.getBeanClass().getName()));
        }

        // @Value注入:
        if (value != null) {
            Object propValue = this.propertyResolver.getRequiredProperty(value.value(), accessibleType);
            if (field != null) {
                field.set(bean, propValue);
            }
            if (method != null) {
                method.invoke(bean, propValue);
            }
        }

        // @Autowired注入:
        if (autowired != null) {
            String name = autowired.name();
            boolean required = autowired.value();
            Object depends = name.isEmpty() ? findBean(accessibleType) : findBean(name, accessibleType);
            if (required && depends == null) {
                throw new BeanBaseException(String.format("Dependency bean not found when inject %s.%s for bean '%s': %s", clazz.getSimpleName(),
                        accessibleName, def.getName(), def.getBeanClass().getName()));
            }
            if (depends != null) {
                if (field != null) {
                    field.set(bean, depends);
                }
                if (method != null) {
                    method.invoke(bean, depends);
                }
            }
        }
    }

    void checkFieldOrMethod(Member m) {
        int mod = m.getModifiers();
        if (Modifier.isStatic(mod)) {
            throw new BeanBaseException("Cannot inject static field: " + m);
        }
        if (Modifier.isFinal(mod)) {
            if (m instanceof Field) {
                Field field = (Field) m;
                throw new BeanBaseException("Cannot inject final field: " + field);
            }
        }
    }

    @Nullable
    @SuppressWarnings("unchecked")
    protected <T> T findBean(Class<T> requiredType) {
        BeanDefinition def = findBeanDefinition(requiredType);
        if (def == null) {
            return null;
        }
        return (T) def.getRequiredInstance();
    }

    @Nullable
    @SuppressWarnings("unchecked")
    protected <T> T findBean(String name, Class<T> requiredType) {
        BeanDefinition def = findBeanDefinition(name, requiredType);
        if (def == null) {
            return null;
        }
        return (T) def.getRequiredInstance();
    }

    private void callMethod(Object beanInstance, Method method, String namedMethod) {
        // 调用init/destroy方法:
        if (method != null) {
            try {
                method.invoke(beanInstance);
            } catch (ReflectiveOperationException e) {
                throw new BeanBaseException(e);
            }
        } else if (namedMethod != null) {
            // 查找initMethod/destroyMethod="xyz"，注意是在实际类型中查找:
            Method named = ClassUtils.getNamedMethod(beanInstance.getClass(), namedMethod);
            named.setAccessible(true);
            try {
                named.invoke(beanInstance);
            } catch (ReflectiveOperationException e) {
                throw new BeanBaseException(e);
            }
        }
    }

    /**
     * 创建普通的Bean
     */
    void createNormalBeans() {
        // 获取BeanDefinition列表:
        List<BeanDefinition> defs = this.beans.values().stream()
                // filter bean definitions by not instantiation:
                .filter(def -> def.getInstance() == null).sorted().collect(Collectors.toList());

        defs.forEach(def -> {
            // 如果Bean未被创建(可能在其他Bean的构造方法注入前被创建):
            if (def.getInstance() == null) {
                // 创建Bean:
                createBeanAsEarlySingleton(def);
            }
        });
    }

    /**
    * 根据类的全限定名，创建类的定义
    * */
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
                // 这个类中包含多个bean，需要被管理
                if (configuration != null) {
                    scanFactoryMethods(beanName, clazz, defs);
                }
            }
        }
        return defs;
    }

    /**
     * bean定义加入到集合中
     */
    void addBeanDefinitions(Map<String, BeanDefinition> defs, BeanDefinition def) {
        if (defs.put(def.getName(), def) != null) {
            throw new BeanBaseException("Duplicate bean name: " + def.getName());
        }
    }

    /**
     * 获取类的构造函数，如果大于一个就报错
     */
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

    /**
     * 获取需要扫描的路径，获取其中的class文件的全限定名
     */
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
    public BeanDefinition findBeanDefinition(String name, Class<?> requiredType) {
        BeanDefinition def = findBeanDefinition(name);
        if (def == null) {
            return null;
        }
        if (!requiredType.isAssignableFrom(def.getBeanClass())) {
            throw new BeanBaseException(String.format("Autowire required type '%s' but bean '%s' has actual type '%s'.", requiredType.getName(),
                    name, def.getBeanClass().getName()));
        }
        return def;
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

    /**
     * 根据bean定义，创建configuration注解的bean
     */
    public Object createBeanAsEarlySingleton(BeanDefinition def){
        if (!this.creatingBeanNames.add(def.getName())) {
            throw new BeanBaseException(String.format("Circular dependency detected when create bean '%s'", def.getName()));
        }

        Executable createFn = null;
        if (def.getFactoryName() == null){
            createFn = def.getConstructor();
        }else{
            createFn = def.getFactoryMethod();
        }

        final Parameter[] parameters = createFn.getParameters();
        final Annotation[][] parametersAnnos = createFn.getParameterAnnotations();
        Object[] args = new Object[parameters.length];

        for (int i = 0; i < parameters.length; i++) {
            final Parameter param = parameters[i];
            final Annotation[] paramAnnos = parametersAnnos[i];
            final Value value = ClassUtils.getAnnotation(paramAnnos, Value.class);
            final Autowired autowired = ClassUtils.getAnnotation(paramAnnos, Autowired.class);

            // @Configuration类型的Bean是工厂，不允许使用@Autowired创建:
            final boolean isConfiguration = isConfigurationDefinition(def);
            if (isConfiguration && autowired != null) {
                throw new BeanBaseException(
                        String.format("Cannot specify @Autowired when create @Configuration bean '%s': %s.", def.getName(), def.getBeanClass().getName()));
            }

            // 参数需要@Value或@Autowired两者之一:
            if (value != null && autowired != null) {
                throw new BeanBaseException(
                        String.format("Cannot specify both @Autowired and @Value when create bean '%s': %s.", def.getName(), def.getBeanClass().getName()));
            }
//            if (value == null && autowired == null) {
//                throw new BeanBaseException(
//                        String.format("Must specify @Autowired or @Value when create bean '%s': %s.", def.getName(), def.getBeanClass().getName()));
//            }
            // 参数类型:
            final Class<?> type = param.getType();
            if (value != null) {
                // 参数是@Value:
                args[i] = this.propertyResolver.getRequiredProperty(value.value(), type);
            } else if (autowired != null) {
                // 参数是@Autowired:
                String name = autowired.name();
                boolean required = autowired.value();
                // 依赖的BeanDefinition:
                BeanDefinition dependsOnDef = name.isEmpty() ? findBeanDefinition(type) : findBeanDefinition(name, type);
                // 检测required==true?
                if (required && dependsOnDef == null) {
                    throw new BeanBaseException(String.format("Missing autowired bean with type '%s' when create bean '%s': %s.", type.getName(),
                            def.getName(), def.getBeanClass().getName()));
                }
                if (dependsOnDef != null) {
                    // 获取依赖Bean:
                    Object autowiredBeanInstance = dependsOnDef.getInstance();
                    if (autowiredBeanInstance == null && !isConfiguration) {
                        // 当前依赖Bean尚未初始化，递归调用初始化该依赖Bean:
                        autowiredBeanInstance = createBeanAsEarlySingleton(dependsOnDef);
                    }
                    args[i] = autowiredBeanInstance;
                } else {
                    args[i] = null;
                }
            }
        }

        // 创建Bean实例:
        Object instance = null;
        if (def.getFactoryName() == null) {
            // 用构造方法创建:
            try {
                instance = def.getConstructor().newInstance(args);
            } catch (Exception e) {
                throw new BeanBaseException(String.format("Exception when create bean '%s': %s", def.getName(), def.getBeanClass().getName()), e);
            }
        } else {
            // 用@Bean方法创建:
            Object configInstance = getBean(def.getFactoryName());
            try {
                instance = def.getFactoryMethod().invoke(configInstance, args);
            } catch (Exception e) {
                throw new BeanBaseException(String.format("Exception when create bean '%s': %s", def.getName(), def.getBeanClass().getName()), e);
            }
        }
        def.setInstance(instance);
        return def.getInstance();
    }

    boolean isConfigurationDefinition(BeanDefinition def) {
        return ClassUtils.findAnnotation(def.getBeanClass(), Configuration.class) != null;
    }

    /**
     * 通过Name查找Bean，不存在时抛出NoSuchBeanDefinitionException
     */
    @SuppressWarnings("unchecked")
    public <T> T getBean(String name) {
        BeanDefinition def = this.beans.get(name);
        if (def == null) {
            throw new BeanBaseException(String.format("No bean defined with name '%s'.", name));
        }
        return (T) def.getRequiredInstance();
    }

    /**
     * 通过Type查找Bean，不存在抛出NoSuchBeanDefinitionException，存在多个但缺少唯一@Primary标注抛出NoUniqueBeanDefinitionException
     */
    public <T> T getBean(Class<T> requiredType) {
        BeanDefinition def = findBeanDefinition(requiredType);
        if (def == null) {
            throw new BeanBaseException(String.format("No bean defined with type '%s'.", requiredType));
        }
        return (T) def.getRequiredInstance();
    }
}
