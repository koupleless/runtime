## 适配springboot 2.3.x.RELEASE问题合集issues
https://github.com/koupleless/koupleless/issues/188

## [ConfigFileApplicationListener.java](src%2Fmain%2Fjava%2Forg%2Fspringframework%2Fboot%2Fcontext%2Fconfig%2FConfigFileApplicationListener.java)
### 遇到问题
模块中自定义的模块自定义EnvironmentPostProcessor在基座启动时加载不到
### 问题原因
ConfigFileApplicationListener中使用SpringFactoriesLoader.loadFactories的地方都是使用当前类的ClassLoader来扫描资源文件，导致只能扫描到基座的资源文件
### 改动点
重写覆盖掉springboot的org.springframework.boot.context.config.ConfigFileApplicationListener类，将其中的SpringFactoriesLoader.loadFactories的第二个入参改成ClassUtils.getDefaultClassLoader()

## [DefaultResourceLoader.java](src%2Fmain%2Fjava%2Forg%2Fspringframework%2Fcore%2Fio%2FDefaultResourceLoader.java)
### 遇到问题
模块中的Mybatis、MybatisPlus读取xml文件时使用的base的ClassLoader导致org.apache.ibatis.binding.BindingException: Invalid bound statement (not found)问题
### 问题原因
MybatisPlusProperties和MybatisProperties中的resourceResolver都是private static final 修饰的，说明在基座启动的时候就会赋值。查看PathMatchingResourcePatternResolver的构造方法
```
this.resourceLoader = new DefaultResourceLoader();
```
spring 5.2.x和spring 5.3的DefaultResourceLoader的构造函数的差异，老版本是new的时候放了一个默认的ClassLoader，就是base的。高版本是用的时候再去获取的

### 改动点
重写覆盖掉spring的org.springframework.core.io.DefaultResourceLoader类，将其中的构造方法置空，什么都不做。这样每次使用DefaultResourceLoader时都会实时获取当前线程的ClassLoader