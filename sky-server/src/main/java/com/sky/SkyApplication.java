package com.sky;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
/*
@SpringBootApplication包含的3个关键注解：
- @SpringBootConfiguration：实际上的作用就是@Configuration
- @EnableAutoConfiguration：作用为扫描引入的依赖jar包中的配置类，将其放入IOC容器管理
                            实现原理：通过起步依赖中的两个文件得到所有依赖中所以配置类的完全限定名，然后使用@import来导入IOC容器
- @ComponentScan(
    excludeFilters = {@Filter(
    type = FilterType.CUSTOM,
    classes = {TypeExcludeFilter.class}
), @Filter(
    type = FilterType.CUSTOM,
    classes = {AutoConfigurationExcludeFilter.class}
)}
)：作用为扫描启动类所在包及其子包的配置类
 */
@EnableTransactionManagement //开启注解方式的事务管理
@Slf4j
@EnableCaching  //开启注解方式的Cache管理
public class SkyApplication {
    public static void main(String[] args) {
        SpringApplication.run(SkyApplication.class, args);
        log.info("server started");
    }
}
