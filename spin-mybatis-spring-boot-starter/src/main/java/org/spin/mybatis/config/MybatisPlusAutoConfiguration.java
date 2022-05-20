package org.spin.mybatis.config;

import com.baomidou.mybatisplus.core.incrementer.IdentifierGenerator;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.OptimisticLockerInnerInterceptor;
import org.apache.ibatis.session.SqlSession;
import org.mybatis.spring.SqlSessionTemplate;
import org.mybatis.spring.SqlSessionUtils;
import org.spin.data.pk.generator.DistributedIdGenerator;
import org.spin.datasource.schema.Schema;
import org.spin.mybatis.DataPermissionInterceptor;
import org.spin.mybatis.entity.SfIdGenerator;
import org.spin.mybatis.handler.MybatisMetaObjectHandler;
import org.spin.mybatis.handler.MybatisPlusMetaObjectHandler;
import org.spin.mybatis.handler.PermissionDataMetaObjectHandler;
import org.spin.mybatis.plus.PaginationInnerInterceptor;
import org.spin.mybatis.util.ApplicationContextSupplier;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.annotation.PostConstruct;
import java.util.List;

@Configuration
@ConditionalOnProperty("mybatis-plus.mapper-locations")
public class MybatisPlusAutoConfiguration {

    private final ApplicationContext applicationContext;

    public MybatisPlusAutoConfiguration(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @PostConstruct
    public void init() {
        ApplicationContextSupplier.init(applicationContext);
    }

    @Bean
    @ConditionalOnBean(DistributedIdGenerator.class)
    public IdentifierGenerator sfIdGenerator(DistributedIdGenerator idGenerator) {
        return new SfIdGenerator(idGenerator);
    }

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

    @Bean
    @ConditionalOnClass(name = "org.spin.datasource.schema.Schema")
    @ConditionalOnBean(SqlSessionTemplate.class)
    public InitializingBean initMybatisConnectionProvider(SqlSessionTemplate sqlSessionTemplate) {
        return () -> {
            try {
                Class.forName("org.spin.datasource.schema.Schema");
                Schema.setTransactionSyncConnectionProvider(() -> {
                    SqlSession sqlSession = SqlSessionUtils.getSqlSession(sqlSessionTemplate.getSqlSessionFactory(),
                        sqlSessionTemplate.getExecutorType(), sqlSessionTemplate.getPersistenceExceptionTranslator());
                    return sqlSession.getConnection();
                });
            } catch (ClassNotFoundException e) {
                // do nothing
            }
        };
    }
}
