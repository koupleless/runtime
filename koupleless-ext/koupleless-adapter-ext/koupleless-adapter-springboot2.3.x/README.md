## Spring Boot 2.3.x.RELEASE Issue Collection
https://github.com/koupleless/koupleless/issues/188

## [ConfigFileApplicationListener.java](src%2Fmain%2Fjava%2Forg%2Fspringframework%2Fboot%2Fcontext%2Fconfig%2FConfigFileApplicationListener.java)
### Issue Encountered
The custom EnvironmentPostProcessor in the module cannot be loaded during the pedestal startup.
### Root Cause
In ConfigFileApplicationListener, SpringFactoriesLoader.loadFactories is used with the current class's ClassLoader to scan resource files, which only scans pedestal resource files.
### Solution
Override and replace the org.springframework.boot.context.config.ConfigFileApplicationListener class in Spring Boot. Change the second parameter of SpringFactoriesLoader.loadFactories to ClassUtils.getDefaultClassLoader().

## [DefaultResourceLoader.java](src%2Fmain%2Fjava%2Forg%2Fspringframework%2Fcore%2Fio%2FDefaultResourceLoader.java)
### Issue Encountered
Mybatis and MybatisPlus in the module encounter org.apache.ibatis.binding.BindingException: Invalid bound statement (not found) when reading xml files using the base ClassLoader.
### Root Cause
MybatisPlusProperties and MybatisProperties have resourceResolver as private static final, indicating that it is assigned during pedestal startup. Checking the constructor of PathMatchingResourcePatternResolver,
```
this.resourceLoader = new DefaultResourceLoader();
```
There is a difference between the constructors of DefaultResourceLoader in spring 5.2.x and spring 5.3. In older versions, a default ClassLoader is provided when instantiating, which is the base one. In newer versions, it is obtained when needed.
### Solution
Override and replace the org.springframework.core.io.DefaultResourceLoader class in Spring. Set the constructor to null, do nothing. This way, each time DefaultResourceLoader is used, the current thread's ClassLoader is obtained in real-time.