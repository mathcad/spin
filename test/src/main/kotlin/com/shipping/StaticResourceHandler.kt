package com.shipping


import org.spin.core.SpinContext
import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter

/**
 * 静态资源映射
 *
 * Created by xuweinan on 2016/10/13.
 *
 * @author xuweinan
 */
@Configuration
open class StaticResourceHandler : WebMvcConfigurerAdapter() {
    override fun addResourceHandlers(registry: ResourceHandlerRegistry?) {
        registry!!.addResourceHandler("/static/**").addResourceLocations("classpath:/static/")
        registry.addResourceHandler("/uploads/**").addResourceLocations("file:" + SpinContext.FILE_UPLOAD_DIR)
        super.addResourceHandlers(registry)
    }
}
