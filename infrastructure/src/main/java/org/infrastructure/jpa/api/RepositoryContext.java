package org.infrastructure.jpa.api;

import org.hibernate.SessionFactory;
import org.infrastructure.jpa.core.ARepository;
import org.infrastructure.jpa.core.IEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class RepositoryContext implements ApplicationContextAware {
    private static final Logger logger = LoggerFactory.getLogger(RepositoryContext.class);

    @Autowired
    private SessionFactory sessFactory;

    private static ApplicationContext applicationContext;

    @Override
    @Autowired
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        RepositoryContext.applicationContext = applicationContext;
    }

    /**
     * 获取指定实体的持久化对象
     */
    @SuppressWarnings("unchecked")
    public <T extends IEntity<Long>> ARepository<T, Long> getRepo(Class<T> cls) throws BeansException {
        Optional<ARepository> optional = applicationContext.getBeansOfType(ARepository.class).values().stream().filter(entry -> cls.getName().equals(entry.getEntityClazz().getName())).findAny();
        if (optional.isPresent())
            return optional.get();
        // 为没有Repository的实体类创建持久化对象并向容器注册
        DefaultListableBeanFactory acf = (DefaultListableBeanFactory) applicationContext.getAutowireCapableBeanFactory();
        BeanDefinitionBuilder bdb = BeanDefinitionBuilder.rootBeanDefinition(ARepository.class);
        bdb.addPropertyValue("clazz", cls);
        bdb.addPropertyValue("sessFactory", sessFactory);
        String beanName = cls.getName() + "ARepository";
        acf.registerBeanDefinition(beanName, bdb.getBeanDefinition());
        return acf.getBean(beanName, ARepository.class);
    }

    /**
     * 获取指定实体的持久化对象
     */
    public <T extends IEntity<Long>> ARepository<T, Long> getRepo(String cls) throws BeansException, ClassNotFoundException {
        @SuppressWarnings("unchecked")
        Class<T> enCls = (Class<T>) Class.forName(cls);
        return this.getRepo(enCls);
    }

    /**
     * 获得实体对象
     *
     * @param cls Dao类Class
     * @param key 主键
     * @return 实体对象
     */
    public <T extends IEntity<Long>> T getEntity(Class<T> cls, Long key) {
        return this.getRepo(cls).get(key);
    }

    /**
     * 获得对象
     *
     * @param cls Dao类Class
     * @param key 主键
     * @return 实体对象
     */
    public <T extends IEntity<Long>> T getDto(Class<T> cls, Long key, int depth) {
        return this.getRepo(cls).getDto(key, depth);
    }

    /**
     * 获得对象
     *
     * @param en 实体
     * @return 已持久化的实体
     */
    public <T extends IEntity<Long>> T saveEntity(T en) {
        @SuppressWarnings("unchecked")
        Class<T> cls = (Class<T>) en.getClass();
        this.getRepo(cls).save(en);
        return en;
    }
}