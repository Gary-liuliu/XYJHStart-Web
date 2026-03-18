package org.xyjh.xyjhstartweb.Config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC配置类
 * 配置静态资源映射，使上传的文件可以通过URL直接访问
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Value("${file.upload.path:D:/XYJHStart-Web/uploads}")
    private String uploadPath;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 配置文件访问的静态资源映射
        // 访问 /files/** 的请求会映射到实际的文件存储路径
        registry.addResourceHandler("/files/**")
                .addResourceLocations("file:" + uploadPath + "/");
        
        // 添加其他可能需要的静态资源配置
        registry.addResourceHandler("/static/**")
                .addResourceLocations("classpath:/static/");
    }
}