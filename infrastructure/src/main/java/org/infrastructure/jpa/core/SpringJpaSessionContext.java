package org.infrastructure.jpa.core;

import java.util.Map;

import org.hibernate.FlushMode;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.internal.SessionFactoryImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.SingletonBeanRegistry;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.orm.hibernate3.support.OpenSessionInViewFilter;
import org.springframework.orm.hibernate4.SessionHolder;
import org.springframework.orm.hibernate4.SpringSessionContext;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.context.ContextLoader;
import org.springframework.web.context.WebApplicationContext;

/**
 * 专供热编译调试，待验证 生产环境勿用
 * 
 * 
 * @author zhou
 * @contact 电话: 18963752887, QQ: 251915460
 * @create 2015年4月23日 下午10:32:50
 * @version V1.0
 */
public class SpringJpaSessionContext extends SpringSessionContext {
	private static final long serialVersionUID = -8835551246505582844L;
	static final Logger logger = LoggerFactory.getLogger(SpringJpaSessionContext.class);
	SessionFactoryImpl sessionFactory;

	/**
	 * @param sessionFactory
	 */
	public SpringJpaSessionContext(SessionFactoryImplementor sessionFactory) {
		super(sessionFactory);
		this.sessionFactory = (SessionFactoryImpl) sessionFactory;
	}

	@Override
	public Session currentSession() throws HibernateException {

		Object value = TransactionSynchronizationManager.getResource(this.sessionFactory);
		if (value instanceof Session) {
			return (Session) value;
		}

		/** 启用非WebContext的Session事务支持策略 */
		WebApplicationContext wac = ContextLoader.getCurrentWebApplicationContext();
		if (wac == null && value == null) {
			bindNewSessionHolder();
		}

		/** 需要动态刷新WebContext的SessionFactoryBean（基于jrebel） */
		if (value == null) {
			Map<Object, Object> map = TransactionSynchronizationManager.getResourceMap();
			if (map != null && TransactionSynchronizationManager.getResourceMap().keySet().size() > 0) {
				rebelSessionFactory(wac);
			}
		}

		return super.currentSession();
	}

	/**
	 * 重装载 Hibernate SessionFactory上下文
	 * 
	 * @version 1.0
	 */
	private void rebelSessionFactory(WebApplicationContext wac) {
		// 绑定到线程
		bindNewSessionHolder();

		/* 刷新context，更新sessionFactory中的最新记录 */
		try {

			if (wac != null) {
				String beanName = OpenSessionInViewFilter.DEFAULT_SESSION_FACTORY_BEAN_NAME;
				SessionFactory currentSessionFactory = wac.getBean(beanName, SessionFactory.class);
				if (currentSessionFactory != sessionFactory) {

					DefaultListableBeanFactory beanFactory = (DefaultListableBeanFactory) wac
							.getAutowireCapableBeanFactory();
					if (beanFactory.containsBeanDefinition(beanName)) {
						beanFactory.removeBeanDefinition(beanName);
					}
					if (beanFactory.containsBean(beanName)) {
						beanFactory.destroySingleton(beanName);
					}

					((SingletonBeanRegistry) beanFactory).registerSingleton(beanName, this.sessionFactory);
				}
			}
		} catch (Exception e) {
			logger.error("刷新webcontext报错", e);
			logger.debug("No session");
		}
	}

	public void bindNewSessionHolder() {
		Session session = sessionFactory.openSession();
		session.setFlushMode(FlushMode.AUTO);
		SessionHolder sessionHolder = new SessionHolder(session);
		TransactionSynchronizationManager.bindResource(sessionFactory, sessionHolder);
	}
}
