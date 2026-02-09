package org.xyjh.xyjhstartweb;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@MapperScan("org.xyjh.xyjhstartweb.mapper")
public class XyjhStartWebApplication {

    public static void main(String[] args) {
        SpringApplication.run(XyjhStartWebApplication.class, args);
    }

}