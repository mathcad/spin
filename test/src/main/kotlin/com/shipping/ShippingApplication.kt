package com.shipping

import org.spin.boot.datasource.annotation.EnableDataSource
import org.spin.boot.datasource.annotation.EnableIdGenerator
import org.spin.boot.datasource.option.DataSourceType
import org.spin.core.SpinContext
import org.spin.core.security.RSA
import org.springframework.beans.factory.InitializingBean
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.core.env.Environment
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

/**
 * 启动类
 *
 * Created by xuweinan on 2017/7/14.
 *
 * @author xuweinan
 */
@EnableIdGenerator
@EnableDataSource(DataSourceType.DRUID)
@SpringBootApplication
class ShippingApplication : WebMvcConfigurer {

    @Autowired
    private lateinit var env: Environment

    override fun addResourceHandlers(registry: ResourceHandlerRegistry) {
        registry.addResourceHandler("/static/**").addResourceLocations("classpath:/static/")
        registry.addResourceHandler("/uploads/**").addResourceLocations("file:" + SpinContext.FILE_UPLOAD_DIR)
    }
}

fun main(args: Array<String>) {
    runApplication<ShippingApplication>(*args)
}
