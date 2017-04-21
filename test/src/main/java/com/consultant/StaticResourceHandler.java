package com.consultant;


import org.spin.sys.EnvCache;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

/**
 * 静态资源映射
 * <p>Created by xuweinan on 2016/10/13.</p>
 *
 * @author xuweinan
 */
@Configuration
public class StaticResourceHandler extends WebMvcConfigurerAdapter {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/static/**").addResourceLocations("classpath:/static/");
        registry.addResourceHandler("/uploads/**").addResourceLocations("file:" + EnvCache.FileUploadDir);
        super.addResourceHandlers(registry);
    }
}
