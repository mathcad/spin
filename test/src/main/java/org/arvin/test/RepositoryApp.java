package org.arvin.test;

import com.alibaba.druid.pool.DruidDataSource;
import org.hibernate.SessionFactory;
import org.infrastructure.jpa.api.ImprovedNamingStrategy;
import org.infrastructure.jpa.core.SQLLoader;
import org.infrastructure.jpa.sql.ClasspathMdLoader;
import org.infrastructure.jpa.sql.resolver.FreemarkerResolver;
import org.infrastructure.jpa.sql.resolver.TemplateResolver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.orm.hibernate5.HibernateTransactionManager;
import org.springframework.orm.hibernate5.LocalSessionFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import java.util.Properties;

/**
 * Created by Arvin on 2016/9/14.
 */

@SpringBootApplication(exclude = HibernateJpaAutoConfiguration.class)
@EnableAspectJAutoProxy(proxyTargetClass = true)
@EnableTransactionManagement(proxyTargetClass = true)
@ComponentScan({"org.infrastructure", "org.arvin"})
public class RepositoryApp {
    public static void main(String[] args) {
        SpringApplication.run(RepositoryApp.class, args);
    }

    @Bean
    @Autowired
    public SQLLoader sqlLoader(TemplateResolver resolver) {
        SQLLoader loader = new ClasspathMdLoader();
        loader.setTemplateResolver(resolver);
        return loader;
    }

    @Bean
    public TemplateResolver templateResolver() {
        return new FreemarkerResolver();
    }

    @Bean
    public DataSource getDataSource() {
        DruidDataSource dataSource = new DruidDataSource();
        dataSource.setUrl("jdbc:mysql://192.168.20.235:3306/gsh56tms?useUnicode\\=true&autoReconnect\\=true&failOverReadOnly\\=false");
        dataSource.setUsername("tms");
        dataSource.setPassword("tms56");
        dataSource.setMaxActive(5);
        dataSource.setMinIdle(1);
        dataSource.setInitialSize(1);
        dataSource.setMaxWait(60000);
        dataSource.setRemoveAbandoned(true);
        dataSource.setRemoveAbandonedTimeout(120);
        Properties proper = new Properties();
        proper.setProperty("clientEncoding", "UTF-8");
        dataSource.setConnectProperties(proper);
        return dataSource;
    }

    @Bean
    @Autowired
    public LocalSessionFactoryBean getSessionFactory(DataSource dataSource) {
        LocalSessionFactoryBean sessionFactory = new LocalSessionFactoryBean();
        sessionFactory.setDataSource(dataSource);
        sessionFactory.setPhysicalNamingStrategy(new ImprovedNamingStrategy());
        sessionFactory.setPackagesToScan("org.arvin.test");
        Properties proper = new Properties();
//        proper.setProperty("hibernate.current_session_context_class", "org.springframework.orm.hibernate5.SpringSessionContext");
        proper.setProperty("hibernate.show_sql", "true");
        proper.setProperty("hibernate.dialect", "org.hibernate.dialect.MySQLDialect");
        sessionFactory.setHibernateProperties(proper);
        return sessionFactory;
    }

    @Bean
    @Autowired
    public PlatformTransactionManager getTransactionManager(SessionFactory sessionFactory) {
        return new HibernateTransactionManager(sessionFactory);
    }
}