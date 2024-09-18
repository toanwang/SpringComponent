# SpringComponent
## dubbo-demo
SpringBoot+dubbo+nacos, 一个简单的demo
## Spring-demo
### SpringIOC
#### resource-resolver (@ComponentScan)
资源解析器:根据指定的路径寻找可用的class
* 目的：在编写IoC容器之前，我们首先要实现@ComponentScan，即解决“在指定包下扫描所有Class”的问题
1. Java的ClassLoader机制可以在指定的Classpath中根据类名加载指定的Class，但给出一个包名，例如，org.example，它并不能获取到该包下的所有Class，也不能获取子包
2. 要在Classpath中扫描指定包名下的所有Class，包括子包，实际上是在Classpath中搜索所有文件，找出文件名匹配的.class文件，例如，Classpath中搜索的文件org/example/Hello.class就符合包名org.example，我们需要根据文件路径把它变为org.example.Hello，就相当于获得了类名
3. 因此，搜索Class变成了搜索文件
#### property-resolver
属性解析器:获取配置文件中的信息
#### BeanDefinition
保存Bean的定义信息，不直接进行实例化的原因
* 可以支持延迟加载
* 方便管理和定义Bean的配置，比如单例、初始化/销毁方法
* 支持多种创建方式和生命周期管理：构造函数、工厂方法等
* 动态代理和AOP：可以在类实例化之前准备好代理对象，不需要修改原始的Bean类
* 灵活配置数据来源：XML、Java注解、Java配置类
#### BeanInstance
根据bean定义进行实例化操作
用到的注解
* @Component：核心注解，标识是一个需要被管理的Bean
* @Configuration：标记为配置类，其中用@Bean标记的属性，会被IOC管理
Bean实例化(注入)主要是两种方法，Spring框架推荐 构造函数注入
* 构造函数：优点：依赖项不可变；强制依赖注入；清晰的依赖关系。但是无法处理循环依赖
* setter：如果没有显式的构造函数，使用无参构造函数实例化该类，使用setter方法注入依赖
#### initBean
1. 对字段进行依赖注入
2. init初始化bean
### 大概流程
1. 初始化需要传入启动文件的class对象，查看是否使用了注解指定扫描路径，如果没有指定，使用类加载器获取当前文件所在的路径，扫描此路径下的class文件；如果指定，扫描指定路径下的class文件
2. 获取bean定义：根据第一步加载出来的对象，获取bean的基础定义
3. bean实例化：进行依赖注入，此处会有循环依赖的问题
4. PostProcessors：自定义过程，在实例化和初始化之前可以有额外的操作
5. bean初始化：一些属性设置，以及有PostConstruct注解方法

全部都是使用类的全限定名获取的class对象
使用反射获取字段、方法、注解等方法，完成依赖注入等操作
## 常见问题
1. ioc三级缓存，为了解决循环依赖的问题
   * setter注入可以解决，使用三级缓存，如果没有构造函数(或者只有无参)使用setter注入。使用两级缓存也可以解决，三级缓存是为了结构上的合理
   * 构造方法注入无法解决循环依赖问题，必须有显式的构造函数，才能使用构造方法注入（Spring推荐的注入方式）