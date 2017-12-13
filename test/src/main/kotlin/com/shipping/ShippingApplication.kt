package com.shipping

import com.shipping.internal.InfoCache
import org.spin.boot.annotation.EnableIdGenerator
import org.spin.boot.annotation.EnableSecretManager
import org.spin.boot.properties.DruidDataSourceProperties
import org.spin.core.security.RSA
import org.springframework.beans.factory.InitializingBean
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.EnableAspectJAutoProxy
import org.springframework.core.env.Environment
import org.springframework.transaction.annotation.EnableTransactionManagement

/**
 * 启动类
 *
 * Created by xuweinan on 2017/7/14.
 *
 * @author xuweinan
 */
@SpringBootApplication(scanBasePackages = ["com.shipping"])
@EnableAspectJAutoProxy(proxyTargetClass = true)
@EnableTransactionManagement(proxyTargetClass = true)
@EnableConfigurationProperties(DruidDataSourceProperties::class)
@EnableSecretManager
@EnableIdGenerator
open class ShippingApplication {

    @Autowired
    internal var env: Environment? = null

    @Bean
    open fun readConfig(): InitializingBean {
        return InitializingBean {
            InfoCache.RSA_PUBKEY = RSA.getRSAPublicKey(env!!.getProperty("encrypt.rsa.publicKey"))
            InfoCache.RSA_PRIKEY = RSA.getRSAPrivateKey(env!!.getProperty("encrypt.rsa.privatekey"))
        }
    }
}

fun main(args: Array<String>) {
    SpringApplication.run(arrayOf<Any>(ShippingApplication::class.java), args)
}
