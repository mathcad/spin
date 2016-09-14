package org.arvin.test;

import com.alibaba.druid.pool.DruidDataSource;
import org.arvin.test.domain.User;
import org.infrastructure.jpa.core.ARepository;
import org.infrastructure.jpa.core.SQLLoader;
import org.infrastructure.jpa.sql.ClasspathMdLoader;
import org.infrastructure.jpa.sql.resolver.FreemarkerResolver;
import org.infrastructure.jpa.sql.resolver.TemplateResolver;
import org.infrastructure.shiro.SessionManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.orm.hibernate5.LocalSessionFactoryBean;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.util.Properties;

/**
 * Created by Arvin on 2016/9/14.
 */
@RestController
@SpringBootApplication
@EnableTransactionManagement
public class RepositoryApp {
    public static void main(String[] args) {
        SpringApplication.run(RepositoryApp.class, args);
    }

    @RequestMapping("/")
    public String demo() {
        return new UserDao().get(1L).toString();
    }

    @Bean
    @Autowired
    public SQLLoader sqlLoader(TemplateResolver resolver) {
        SQLLoader loader = new ClasspathMdLoader();
        loader.setTemplateResolver(resolver);
        return loader;
    }

    @Bean
    public SessionManager sessionManager() {
        return new SessionManager();
    }

    @Bean
    public TemplateResolver templateResolver() {
        return new FreemarkerResolver();
    }

//    @Bean
//    public UserDao userDao() {
//        return new UserDao();
//    }

    @Bean
    public DataSource dataSource() {
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
    public LocalSessionFactoryBean sessionFactory(DataSource dataSource) {
        LocalSessionFactoryBean sessionFactory = new LocalSessionFactoryBean();
        sessionFactory.setDataSource(dataSource);
        sessionFactory.setPhysicalNamingStrategy(new ImprovedNamingStrategy());
        return sessionFactory;
    }

    private class UserDao extends ARepository<User, Long> {

    }
}
