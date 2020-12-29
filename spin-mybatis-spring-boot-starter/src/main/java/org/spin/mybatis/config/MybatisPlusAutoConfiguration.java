package org.spin.mybatis.config;

import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.OptimisticLockerInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.spin.mybatis.DataPermissionInterceptor;
import org.spin.mybatis.handler.MybatisMetaObjectHandler;
import org.spin.mybatis.handler.MybatisPlusMetaObjectHandler;
import org.spin.mybatis.handler.PermissionDataMetaObjectHandler;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.util.List;

@Configuration
@ConditionalOnProperty("mybatis-plus.mapper-locations")
public class MybatisPlusAutoConfiguration {

    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor());
        interceptor.addInnerInterceptor(new OptimisticLockerInnerInterceptor());
        return interceptor;
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

    @Bean
    public PermissionDataMetaObjectHandler permissionDataMetaObjectHandler() {
        return new PermissionDataMetaObjectHandler();
    }

    @Bean
    @Primary
    public MybatisPlusMetaObjectHandler mybatisPlusMetaObjectHandler(List<MybatisMetaObjectHandler> mybatisMetaObjectHandlers) {
        return new MybatisPlusMetaObjectHandler(mybatisMetaObjectHandlers);
    }
}
