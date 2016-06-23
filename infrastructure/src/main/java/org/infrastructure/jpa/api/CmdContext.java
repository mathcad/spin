package org.infrastructure.jpa.api;

import org.infrastructure.jpa.core.ARepository;
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
	static Logger logger = LoggerFactory.getLogger(CmdContext.class);

	@Autowired
	protected SessionManager sessionMgr;

	@Autowired
	LocalSessionFactoryBean sessFactory;

	public Map<String, ARepository> repoMap = new HashMap<String, ARepository>();

	private static ApplicationContext applicationContext; // Spring应用上下文环境

	/*
	 * 实现了ApplicationContextAware 接口，必须实现该方法；
	 * 通过传递applicationContext参数初始化成员变量applicationContext
	 */
	@Autowired
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		CmdContext.applicationContext = applicationContext;
	}

	public static ApplicationContext getApplicationContext() {
		return applicationContext;
	}

	/**
	 * 返回自动装配的对象仓储
	 * 
	 * @param cls
	 * @return
	 */
	public ARepository getRepo(Class cls) {
		try {
			return this.getRepo(cls.getName());
		} catch (BeansException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 获得对象
	 * 
	 * @param cls
	 * @param key
	 * @return
	 */
	public Object getEntity(Class cls, Long key) {
		return this.getRepo(cls).get(key);
	}

	/**
	 * 获得对象
	 * 
	 * @param cls
	 * @param key
	 * @return
	 */
	public Object getEntity(Class cls, Long key, int depth) {
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
	 * @param cls
	 * @param key
	 * @return 已持久化的实体
	 */
	public Object saveEntity(Object en) {
		Class cls = en.getClass();
		this.getRepo(cls).save(en);
		return en;
	}

	/**
	 * 如果系统中已申明了Repo，不再生成
	 * 
	 * @param cls
	 * @return
	 * @throws BeansException
	 * @throws ClassNotFoundException
	 */
	@SuppressWarnings("unchecked")
	public ARepository getRepo(String cls) throws BeansException, ClassNotFoundException {
		ARepository repo = null;

		String repoKey = cls + "Repository";
		Class<?> enCls = (Class<?>) Class.forName(cls);

		Class repoCls = ARepository.class;

		Map<String, Object> repos = applicationContext.getBeansOfType(repoCls);
		for (String rKey : repos.keySet()) {
			ARepository aRepo = (ARepository) repos.get(rKey);
			String rCls = aRepo.getClazz().getName();
			if (rCls.equals(cls)) {
				repo = aRepo;
				break;
			}
		}

		if (repo != null)
			return repo;

		if (repoMap.get(repoKey) == null) {
			DefaultListableBeanFactory acf = (DefaultListableBeanFactory) this.applicationContext
					.getAutowireCapableBeanFactory();

			SessionManager sessionMgr = applicationContext.getBean(SessionManager.class);
			LocalSessionFactoryBean sessFactory = applicationContext.getBean(LocalSessionFactoryBean.class);

			BeanDefinitionBuilder bdb = BeanDefinitionBuilder.rootBeanDefinition(ARepository.class);
			bdb.addPropertyValue("clazz", enCls);
			bdb.addPropertyValue("sessFactory", sessFactory);
			bdb.addPropertyValue("sessionMgr", sessionMgr);

			AbstractBeanDefinition abd = bdb.getBeanDefinition();
			// abd.setAttribute("clazz", enCls);
			// abd.setAttribute("jt",jt );
			// abd.setAttribute("sessFactory",sessFactory);
			// abd.setAttribute("sessionMgr",sessionMgr);

			String beanName = "ARepository" + "." + enCls.getSimpleName();
			acf.registerBeanDefinition(beanName, abd);

			repo = (ARepository) acf.getBean(beanName);
			repoMap.put(repoKey, repo);
		}

		return repoMap.get(repoKey);
	}

}