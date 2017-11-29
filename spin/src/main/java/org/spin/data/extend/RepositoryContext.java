package org.spin.data.extend;

import org.hibernate.SessionFactory;
import org.spin.data.core.ARepository;
import org.spin.data.core.IEntity;
import org.spin.data.pk.generator.IdGenerator;
import org.spin.data.sql.SQLManager;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.Objects;
import java.util.Optional;

/**
 * 存储对象上下文
 * <p>Created by xuweinan on 2016/9/24.</p>
 *
 * @author xuweinan
 */
public class RepositoryContext {

    @Autowired
    private SessionFactory sessFactory;

    @Autowired
    private SQLManager sqlManager;

    @Autowired(required = false)
    private IdGenerator<?, ?> idGenerator;

    private ApplicationContext applicationContext;

    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    /**
     * 获取指定实体的持久化操作对象
     *
     * @param cls  实体类Class
     * @param <T>  实体类型
     * @param <PK> 主键类型
     * @return 对应实体的持久化操作对象
     */
    public <T extends IEntity<PK>, PK extends Serializable> ARepository<T, PK> getRepo(Class<T> cls) throws BeansException {
        Optional<ARepository> optional = applicationContext.getBeansOfType(ARepository.class).values().stream()
            .filter(entry -> cls.getName().equals(entry.getEntityClazz().getName())).findAny();
        //noinspection unchecked
        return optional.orElseGet(() -> {
            // 为没有Repository的实体类创建持久化对象并向容器注册
            DefaultListableBeanFactory acf = (DefaultListableBeanFactory) applicationContext.getAutowireCapableBeanFactory();
            BeanDefinitionBuilder bdb = BeanDefinitionBuilder.rootBeanDefinition(ARepository.class);
            bdb.addPropertyValue("entityClazz", cls);
            bdb.addPropertyValue("sessFactory", sessFactory);
            bdb.addPropertyValue("sqlManager", sqlManager);
            if (Objects.nonNull(idGenerator)) {
                bdb.addPropertyValue("idGenerator", idGenerator);
            }
            String beanName = cls.getName() + "ARepository";
            acf.registerBeanDefinition(beanName, bdb.getBeanDefinition());
            return acf.getBean(beanName, ARepository.class);
        });
    }

    /**
     * 获得实体对象
     *
     * @param cls  实体类Class
     * @param key  主键
     * @param <T>  实体类型
     * @param <PK> 主键类型
     * @return 实体对象
     */
    public <T extends IEntity<PK>, PK extends Serializable> T getEntity(Class<T> cls, PK key) {
        return getRepo(cls).get(key);
    }

    /**
     * 获得DTO对象
     *
     * @param cls  实体类Class
     * @param key  主键
     * @param <T>  实体类型
     * @param <PK> 主键类型
     * @return 实体对象
     */
    public <T extends IEntity<PK>, PK extends Serializable> T getDto(Class<T> cls, PK key, int depth) {
        return getRepo(cls).getDto(key, depth);
    }

    /**
     * 保存实体
     *
     * @param entity 实体
     * @param <T>    实体类型
     * @param <PK>   主键类型
     * @return 已持久化的实体
     */
    public <T extends IEntity<PK>, PK extends Serializable> T saveEntity(T entity) {
        @SuppressWarnings("unchecked")
        Class<T> cls = (Class<T>) entity.getClass();
        getRepo(cls).save(entity);
        return entity;
    }

    /**
     * 删除实体
     *
     * @param entity 实体
     * @param <T>    实体类型
     * @param <PK>   主键类型
     */
    public <T extends IEntity<PK>, PK extends Serializable> void deleteEntity(T entity) {
        @SuppressWarnings("unchecked")
        Class<T> cls = (Class<T>) entity.getClass();
        getRepo(cls).delete(entity);
    }

    /**
     * 删除实体
     *
     * @param cls  实体类Class
     * @param key  主键
     * @param <T>  实体类型
     * @param <PK> 主键类型
     */
    public <T extends IEntity<PK>, PK extends Serializable> void deleteEntity(Class<T> cls, PK key) {
        getRepo(cls).delete(key);
    }
}
