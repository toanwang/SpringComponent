# SpringComponent
## dubbo-demo
SpringBoot+dubbo+nacos, 一个简单的demo
## Spring-demo
### resource-resolver
资源解析器，根据指定的路径寻找可用的class
在编写IoC容器之前，我们首先要实现@ComponentScan，即解决“在指定包下扫描所有Class”的问题。
Java的ClassLoader机制可以在指定的Classpath中根据类名加载指定的Class，但遗憾的是，给出一个包名，例如，org.example，它并不能获取到该包下的所有Class，也不能获取子包。
要在Classpath中扫描指定包名下的所有Class，包括子包，实际上是在Classpath中搜索所有文件，找出文件名匹配的.class文件。
例如，Classpath中搜索的文件org/example/Hello.class就符合包名org.example，
我们需要根据文件路径把它变为org.example.Hello，就相当于获得了类名。
因此，搜索Class变成了搜索文件。
### property-resolver
属性解析器