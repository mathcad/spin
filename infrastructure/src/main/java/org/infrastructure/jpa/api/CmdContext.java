package org.infrastructure.jpa.api;

import org.infrastructure.jpa.core.ARepository;
import org.infrastructure.jpa.core.IEntity;
import org.infrastructure.shiro.SessionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.orm.hibernate4.LocalSessionFactoryBean;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * 封装了常用的Context命令
 */
@Component
public class CmdContext implements ApplicationContextAware {
    private static final Logger logger = LoggerFactory.getLogger(CmdContext.class);

    @Autowired
    private SessionManager sessionMgr;

    @Autowired
    private LocalSessionFactoryBean sessFactory;

    private Map<String, ARepository> repoMap = new HashMap<>();

    private static ApplicationContext applicationContext; // Spring应用上下文环境

    /**
     * 实现了ApplicationContextAware 接口，必须实现该方法；
     * 通过传递applicationContext参数初始化成员变量applicationContext
     */
    @Autowired
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        CmdContext.applicationContext = applicationContext;
    }

    /**
     * 返回自动装配的对象仓储
     */
    public <T extends IEntity<Long>> ARepository<T, Long> getRepo(Class<T> cls) {
        try {
            return this.getRepo(cls.getName());
        } catch (BeansException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 如果系统中已声明了Repo，不再生成
     *
     * @throws BeansException
     * @throws ClassNotFoundException
     */
    @SuppressWarnings("unchecked")
    public <T extends IEntity<Long>> ARepository<T, Long> getRepo(String cls) throws BeansException, ClassNotFoundException {
        ARepository repo = null;

        String repoKey = cls + "Repository";
        Class<?> enCls = Class.forName(cls);

        Class repoCls = ARepository.class;

        Map<String, Object> repos = applicationContext.getBeansOfType(repoCls);
        for (String rKey : repos.keySet()) {
            ARepository aRepo = (ARepository) repos.get(rKey);
            String rCls = aRepo.getEntityClazz().getName();
            if (rCls.equals(cls)) {
                repo = aRepo;
                break;
            }
        }

        if (repo != null)
            return repo;

        if (repoMap.get(repoKey) == null) {
            DefaultListableBeanFactory acf = (DefaultListableBeanFactory) applicationContext
                    .getAutowireCapableBeanFactory();

            SessionManager sessionMgr = applicationContext.getBean(SessionManager.class);
            LocalSessionFactoryBean sessFactory = applicationContext.getBean(LocalSessionFactoryBean.class);

            BeanDefinitionBuilder bdb = BeanDefinitionBuilder.rootBeanDefinition(ARepository.class);
            bdb.addPropertyValue("clazz", enCls);
            bdb.addPropertyValue("sessFactory", sessFactory);
            bdb.addPropertyValue("sessionMgr", sessionMgr);

            AbstractBeanDefinition abd = bdb.getBeanDefinition();

            String beanName = "ARepository" + "." + enCls.getSimpleName();
            acf.registerBeanDefinition(beanName, abd);

            repo = (ARepository) acf.getBean(beanName);
            repoMap.put(repoKey, repo);
        }

        return repoMap.get(repoKey);
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
    public <T extends IEntity<Long>> T getEntity(Class<T> cls, Long key, int depth) {
        try {
            return this.getRepo(cls).get(key, depth);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
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