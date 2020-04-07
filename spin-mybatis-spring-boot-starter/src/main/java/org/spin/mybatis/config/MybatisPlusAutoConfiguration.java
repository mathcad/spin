package org.spin.mybatis.config;

import com.baomidou.mybatisplus.extension.plugins.OptimisticLockerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.PaginationInterceptor;
import org.spin.mybatis.DataPermissionInterceptor;
import org.spin.mybatis.handler.MybatisPlusMetaObjectHandler;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty("mybatis-plus.mapper-locations")
public class MybatisPlusAutoConfiguration {

    /**
     * 分页插件
     *
     * @return PaginationInterceptor
     */
    @Bean
    public PaginationInterceptor paginationInterceptor() {
        return new PaginationInterceptor();
    }

    /**
     * 数据权限控制插件
     *
     * @return DataPermissionInterceptor
     */
    @Bean
    @ConditionalOnClass(name = "org.spin.cloud.vo.CurrentUser")
    public DataPermissionInterceptor dataPermissionInterceptor() {
        return new DataPermissionInterceptor();
    }

    /**
     * 乐观锁
     *
     * @return optimisticLockerInterceptor
     */
    @Bean
    public OptimisticLockerInterceptor optimisticLockerInterceptor() {
        return new OptimisticLockerInterceptor();
    }

    /**
     * 自动填充
     *
     * @return mybatisPlusMetaObjectHandler
     */
    @Bean
    public MybatisPlusMetaObjectHandler mybatisPlusMetaObjectHandler() {
        return new MybatisPlusMetaObjectHandler();
    }
}
