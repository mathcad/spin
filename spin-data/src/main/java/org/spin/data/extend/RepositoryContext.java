package org.spin.data.extend;

import org.hibernate.CacheMode;
import org.hibernate.Criteria;
import org.hibernate.LockMode;
import org.hibernate.LockOptions;
import org.hibernate.ReplicationMode;
import org.hibernate.Session;
import org.hibernate.criterion.CriteriaSpecification;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.internal.CriteriaImpl;
import org.hibernate.jdbc.ReturningWork;
import org.hibernate.jdbc.Work;
import org.hibernate.query.Query;
import org.spin.core.Assert;
import org.spin.core.throwable.AssertFailException;
import org.spin.core.throwable.SimplifiedException;
import org.spin.core.util.BeanUtils;
import org.spin.core.util.ClassUtils;
import org.spin.core.util.StringUtils;
import org.spin.data.core.ARepository;
import org.spin.data.core.DataSourceContext;
import org.spin.data.core.IEntity;
import org.spin.data.core.Page;
import org.spin.data.core.PageRequest;
import org.spin.data.pk.generator.IdGenerator;
import org.spin.data.query.CriteriaBuilder;
import org.spin.data.query.QueryParam;
import org.spin.data.query.QueryParamParser;
import org.spin.data.sql.SQLManager;
import org.spin.data.throwable.SQLError;
import org.spin.data.throwable.SQLException;
import org.spin.data.util.EntityUtils;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.ApplicationContext;

import javax.persistence.Entity;
import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 存储对象上下文
 * <p>Created by xuweinan on 2016/9/24.</p>
 *
 * @author xuweinan
 */
public class RepositoryContext {
    private static final int MAX_RECORDS = 100000000;
    private static final String ORDER_ENTRIES = "orderEntries";
    private static final String ENTITY_CLS_LOST = "请指定正确的查询实体 ";

    private SQLManager sqlManager;
    private QueryParamParser paramParser;
    private Map<String, IdGenerator<?, ?>> idGenerators = new HashMap<>();
    private ApplicationContext applicationContext;

    /**
     * 所有注册到Spring上下文的Repository的缓存，用于快速获取
     */
    private final Map<String, ARepository> repositoryCache = new HashMap<>(128);

    public RepositoryContext(SQLManager sqlManager, QueryParamParser paramParser, ApplicationContext applicationContext) {
        this.sqlManager = Assert.notNull(sqlManager);
        this.paramParser = Assert.notNull(paramParser);
        this.applicationContext = Assert.notNull(applicationContext);
        applicationContext.getBeansOfType(IdGenerator.class).values()
            .forEach(idGen -> idGenerators.put(idGen.getIdType().getName(), idGen));
    }

    /**
     * 获取指定实体的持久化操作对象
     *
     * @param cls 实体类Class，必须具有{@link Entity}注解
     * @param <T> 实体类型
     * @param <P> 主键类型
     * @return 对应实体的持久化操作对象
     */
    public <T extends IEntity<P>, P extends Serializable> ARepository<T, P> getRepo(Class<T> cls) {
        Assert.notNull(cls, "请指定要查询实体类型");
        final String beanName = cls.getName() + "ARepository";
        if (repositoryCache.containsKey(beanName)) {
            //noinspection unchecked
            return repositoryCache.get(beanName);
        } else {
            if (null == cls.getAnnotation(Entity.class)) {
                throw new SimplifiedException(String.format("[%s]不是持久化类型，不能获取对应的持久化操作对象", cls.getName()));
            }
            @SuppressWarnings("unchecked")
            ARepository<T, P> repository = applicationContext.getBeansOfType(ARepository.class).values().stream()
                .filter(entry -> cls.isAssignableFrom(entry.getEntityClazz()))
                .findAny()
                .orElseGet(() -> {
                    // 为没有Repository的实体类创建持久化对象并向容器注册
                    DefaultListableBeanFactory acf = (DefaultListableBeanFactory) applicationContext.getAutowireCapableBeanFactory();
                    BeanDefinitionBuilder bdb = BeanDefinitionBuilder.rootBeanDefinition(ARepository.class);
                    bdb.addPropertyValue("entityClazz", cls);
                    bdb.addPropertyValue("sqlManager", sqlManager);
                    bdb.addPropertyValue("queryParamParser", paramParser);
                    IdGenerator<?, ?> idGenerator = idGenerators.get(EntityUtils.getPKField(cls).getType().getName());
                    if (Objects.nonNull(idGenerator)) {
                        bdb.addPropertyValue("idGenerator", idGenerator);
                    }
                    acf.registerBeanDefinition(beanName, bdb.getBeanDefinition());
                    return acf.getBean(beanName, ARepository.class);
                });
            repositoryCache.put(cls.getName(), repository);
            return repository;
        }
    }

    /**
     * 保存指定实体
     *
     * @param entity 需要保存的实体
     * @param <T>    实体类型
     * @param <P>    实体主键类型
     * @return 保存后的实体(has id)
     */
    public <T extends IEntity<P>, P extends Serializable> T save(final T entity) {
        //noinspection unchecked
        return (T) getRepo(entity.getClass()).save(entity, false);
    }

    /**
     * 保存指定实体
     *
     * @param entity     需要保存的实体
     * @param saveWithPk 用指定的ID执行insert
     * @param <T>        实体类型
     * @param <P>        实体主键类型
     * @return 保存后的实体(has id)
     */
    public <T extends IEntity<P>, P extends Serializable> T save(final T entity, boolean saveWithPk) {
        //noinspection unchecked
        return (T) getRepo(entity.getClass()).save(entity, saveWithPk);
    }

    /**
     * 保存指定的实体
     *
     * @param entities 需要保存的实体集合
     * @param <T>      实体类型
     * @param <P>      实体主键类型
     * @return 保存后的实体
     */
    public <T extends IEntity<P>, P extends Serializable> List<T> save(Iterable<T> entities) {
        List<T> result = new ArrayList<>();
        if (entities == null) {
            return result;
        }
        for (T entity : entities) {
            result.add(save(entity));
        }
        return result;
    }

    /**
     * 将指定实体的属性复制到相同Id的持久化对象中。
     * <p>如果Session中没有该Id的持久化对象，该Id的实体将先被加载，最后返回该持久化对象。</p>
     * <p>如果指定的实体是新记录，将保存该实体的副本到数据库，并将该副本作为持久化对象返回。</p>
     * <p>指定的对象不会与当前Session关联。</p>
     * <p>如果对象存在关联属性映射为{@code cascade="merge"}，该方法会级联处理对象属性</p>
     * 该方法的语义由 JSR-220定义.
     *
     * @param entity 待合并的瞬态实例
     * @param <T>    实体类型
     * @param <P>    实体主键类型
     * @return 更新后的持久化对象
     */
    public <T extends IEntity<P>, P extends Serializable> T merge(final T entity) {
        //noinspection unchecked
        return (T) getRepo(entity.getClass()).merge(entity);
    }

    /**
     * 用指定的复制机制持久化指定瞬态实体
     *
     * @param entity          待复制的实体
     * @param replicationMode Hibernate ReplicationMode
     * @param <T>             实体类型
     * @param <P>             实体主键类型
     * @see Session#replicate(Object, ReplicationMode)
     */
    public <T extends IEntity<P>, P extends Serializable> void replicate(final T entity, final ReplicationMode replicationMode) {
        //noinspection unchecked
        getRepo(entity.getClass()).replicate(entity, replicationMode);
    }


    /**
     * 返回指定Id的持久化对象，或者{@code null}(如果Id不存在)
     * <p>该方法是{@link Session#get(Class, Serializable)}的浅封装</p>
     *
     * @param entityClazz 实体类Class，必须具有{@link Entity}注解
     * @param id          主键值
     * @param <T>         实体类型
     * @param <P>         实体主键类型
     * @return 持久化对象, 或 {@code null}
     * @see Session#get(Class, Serializable)
     */
    public <T extends IEntity<P>, P extends Serializable> T get(Class<T> entityClazz, final P id) {
        return getRepo(entityClazz).get(id);
    }

    /**
     * 返回指定Id的持久化对象，或者{@code null}(如果Id不存在)
     * <p>如果实体存在，将获取指定的锁</p>
     * <p>该方法是{@link Session#get(Class, Serializable, LockMode)}的浅封装</p>
     *
     * @param entityClazz 实体类Class，必须具有{@link Entity}注解
     * @param id          主键值
     * @param lockMode    锁定模式
     * @param <T>         实体类型
     * @param <P>         实体主键类型
     * @return 持久化对象, 或 {@code null}
     * @see Session#get(Class, Serializable, LockMode)
     */
    public <T extends IEntity<P>, P extends Serializable> T get(Class<T> entityClazz, final P id, final LockMode lockMode) {
        return getRepo(entityClazz).get(id, lockMode);
    }

    /**
     * 获取持久态实体对象并锁定(FOR UPDATE悲观锁)
     *
     * @param entityClazz 实体类Class，必须具有{@link Entity}注解
     * @param id          主键值
     * @param <T>         实体类型
     * @param <P>         实体主键类型
     * @return 持久化对象
     */
    public <T extends IEntity<P>, P extends Serializable> T getWithLock(Class<T> entityClazz, final P id) {
        return getRepo(entityClazz).getWithLock(id);
    }

    /**
     * 返回指定Id的持久化对象，如果Id不存在，抛出异常
     *
     * @param entityClazz 实体类Class，必须具有{@link Entity}注解
     * @param id          主键值
     * @param <T>         实体类型
     * @param <P>         实体主键类型
     * @return 持久化对象
     * @throws org.springframework.orm.ObjectRetrievalFailureException 如果id不存在则抛出该异常
     * @see Session#load(Class, Serializable)
     */
    public <T extends IEntity<P>, P extends Serializable> T load(Class<T> entityClazz, final P id) {
        return getRepo(entityClazz).load(id);
    }

    /**
     * 返回指定Id的持久化对象，如果Id不存在，抛出异常
     * <p>如果实体存在，将获取指定的锁</p>
     *
     * @param entityClazz 实体类Class，必须具有{@link Entity}注解
     * @param id          主键值
     * @param lockMode    锁定模式
     * @param <T>         实体类型
     * @param <P>         实体主键类型
     * @return 持久化对象
     * @throws org.springframework.orm.ObjectRetrievalFailureException 如果id不存在则抛出该异常
     * @see Session#load(Class, Serializable)
     */
    public <T extends IEntity<P>, P extends Serializable> T load(Class<T> entityClazz, final P id, final LockMode lockMode) {
        return getRepo(entityClazz).load(id, lockMode);
    }

    /**
     * 主键获取指定深度的属性的瞬态对象
     *
     * @param entityClazz 实体类Class，必须具有{@link Entity}注解
     * @param id          主键值
     * @param depth       深度
     * @param <T>         实体类型
     * @param <P>         实体主键类型
     * @return 瞬态DTO对象
     */
    public <T extends IEntity<P>, P extends Serializable> T getDto(Class<T> entityClazz, final P id, int depth) {
        return getRepo(entityClazz).getDto(id, depth);
    }

    /**
     * 刷新指定持久化对象的状态
     *
     * @param entity 待刷新的持久化对象
     * @param <T>    实体类型
     * @param <P>    实体主键类型
     * @see Session#refresh(Object)
     */
    public <T extends IEntity<P>, P extends Serializable> void refresh(final T entity) {
        //noinspection unchecked
        getRepo(entity.getClass()).refresh(entity, null);
    }

    /**
     * 刷新指定持久化对象的状态，并获取该实体上的锁
     *
     * @param entity   待刷新的持久化对象
     * @param lockMode 需要获取的锁
     * @param <T>      实体类型
     * @param <P>      实体主键类型
     * @see Session#refresh(Object, LockMode)
     */
    public <T extends IEntity<P>, P extends Serializable> void refresh(final T entity, final LockMode lockMode) {
        //noinspection unchecked
        getRepo(entity.getClass()).refresh(entity, lockMode);
    }

    /**
     * 检查Session缓存中是否存在指定的持久化对象
     *
     * @param entity 待检查的持久化对象
     * @param <T>    实体类型
     * @param <P>    实体主键类型
     * @return 是否存在
     * @see Session#contains
     */
    public <T extends IEntity<P>, P extends Serializable> boolean contains(final T entity) {
        //noinspection unchecked
        return getRepo(entity.getClass()).contains(entity);
    }

    /**
     * 删除指定实体
     *
     * @param entity 要删除的实体
     * @param <T>    实体类型
     * @param <P>    实体主键类型
     * @throws AssertFailException 当待删除的实体为{@literal null}时抛出该异常
     */
    public <T extends IEntity<P>, P extends Serializable> void delete(T entity) {
        //noinspection unchecked
        getRepo(entity.getClass()).delete(entity);
    }

    /**
     * 通过ID删除指定实体
     *
     * @param entityClazz 实体类Class，必须具有{@link Entity}注解
     * @param id          主键值
     * @param <T>         实体类型
     * @param <P>         实体主键类型
     * @throws AssertFailException 当待删除的{@code id}为{@literal null}时抛出该异常
     */
    public <T extends IEntity<P>, P extends Serializable> void delete(Class<T> entityClazz, P id) {
        getRepo(entityClazz).delete(id);
    }

    /**
     * 通过ID集合删除指定实体
     *
     * @param entityClazz 实体类Class，必须具有{@link Entity}注解
     * @param ids         主键值集合
     * @param <T>         实体类型
     * @param <P>         实体主键类型
     * @throws AssertFailException 当待删除的{@code ids}为{@literal null}时抛出该异常
     */
    public <T extends IEntity<P>, P extends Serializable> void delete(Class<T> entityClazz, Iterator<P> ids) {
        getRepo(entityClazz).delete(ids);
    }

    /**
     * 删除指定实体
     *
     * @param entities 要删除的实例列表
     * @param <T>      实体类型
     * @param <P>      实体主键类型
     * @throws AssertFailException 当待删除的{@link Iterable}为{@literal null}时抛出该异常
     */
    public <T extends IEntity<P>, P extends Serializable> void delete(Iterable<? extends T> entities) {
        Assert.notNull(entities, "The given Iterable of entities not be null!");
        for (T entity : entities) {
            delete(entity);
        }
    }

    /**
     * 批量删除实体
     * <p>如果条件为空，删除所有</p>
     *
     * @param entityClazz 实体类Class，必须具有{@link Entity}注解
     * @param cs          删除条件
     * @param <T>         实体类型
     * @param <P>         实体主键类型
     */
    public <T extends IEntity<P>, P extends Serializable> void delete(Class<T> entityClazz, Criterion... cs) {
        getRepo(entityClazz).delete(cs);
    }

    /**
     * 通过hql批量删除实体
     * <p>如果条件为空，删除所有</p>
     *
     * @param entityClazz 实体类Class，必须具有{@link Entity}注解
     * @param conditions  hql删除条件
     * @param <T>         实体类型
     * @param <P>         实体主键类型
     */
    public <T extends IEntity<P>, P extends Serializable> void delete(Class<T> entityClazz, String conditions) {
        getRepo(entityClazz).delete(conditions);
    }

    /**
     * 逻辑删除指定实体
     *
     * @param entity 要逻辑删除的实体
     * @param <T>    实体类型
     * @param <P>    实体主键类型
     * @throws AssertFailException 当待删除的实体为{@literal null}时抛出该异常
     */
    public <T extends IEntity<P>, P extends Serializable> void logicDelete(T entity) {
        //noinspection unchecked
        getRepo(entity.getClass()).logicDelete(entity);
    }

    /**
     * 通过ID逻辑删除指定实体
     *
     * @param entityClazz 实体类Class，必须具有{@link Entity}注解
     * @param id          主键值
     * @param <T>         实体类型
     * @param <P>         实体主键类型
     * @throws AssertFailException 当待删除的{@code id}为{@literal null}时抛出该异常
     */
    public <T extends IEntity<P>, P extends Serializable> void logicDelete(Class<T> entityClazz, P id) {
        getRepo(entityClazz).logicDelete(id);
    }

    /**
     * 通过ID集合逻辑删除指定实体
     *
     * @param entityClazz 实体类Class，必须具有{@link Entity}注解
     * @param ids         主键值集合
     * @param <T>         实体类型
     * @param <P>         实体主键类型
     * @throws AssertFailException 当待删除的{@code ids}为{@literal null}时抛出该异常
     */
    public <T extends IEntity<P>, P extends Serializable> void logicDelete(Class<T> entityClazz, Iterator<P> ids) {
        getRepo(entityClazz).logicDelete(ids);
    }

    /**
     * 逻辑删除指定实体
     *
     * @param entities 要删除的实例列表
     * @param <T>      实体类型
     * @param <P>      实体主键类型
     * @throws AssertFailException 当待删除的{@link Iterable}为{@literal null}时抛出该异常
     */
    public <T extends IEntity<P>, P extends Serializable> void logicDelete(Iterable<? extends T> entities) {
        Assert.notNull(entities, "The given Iterable of entities not be null!");
        for (T entity : entities) {
            logicDelete(entity);
        }
    }

    /**
     * 批量逻辑删除实体
     * <p>如果条件为空，删除所有</p>
     *
     * @param entityClazz 实体类Class，必须具有{@link Entity}注解
     * @param cs          删除条件
     * @param <T>         实体类型
     * @param <P>         实体主键类型
     */
    public <T extends IEntity<P>, P extends Serializable> void logicDelete(Class<T> entityClazz, Criterion... cs) {
        getRepo(entityClazz).logicDelete(cs);
    }

    /**
     * 分页条件查询
     *
     * @param dc  离线条件
     * @param pr  分页参数
     * @param <T> 实体类型
     * @param <P> 实体主键类型
     * @return 查询结果
     */
    public <T extends IEntity<P>, P extends Serializable> List<T> find(DetachedCriteria dc, PageRequest... pr) {
        Session sess = DataSourceContext.getSession();
        Criteria ct = dc.getExecutableCriteria(sess);
        if (null != pr && pr.length > 0 && null != pr[0]) {
            ct.setFirstResult(pr[0].getOffset());
            ct.setMaxResults(pr[0].getPageSize());
        }
        ct.setCacheable(true);
        ct.setCacheMode(CacheMode.NORMAL);
        //noinspection unchecked
        return ct.list();
    }

    /**
     * 条件查询
     *
     * @param entityClazz 实体类Class，必须具有{@link Entity}注解
     * @param cs          查询条件
     * @param <T>         实体类型
     * @param <P>         实体主键类型
     * @return 查询结果
     */
    public <T extends IEntity<P>, P extends Serializable> List<T> find(Class<T> entityClazz, Criterion... cs) {
        Assert.notNull(entityClazz, ENTITY_CLS_LOST);
        DetachedCriteria dc = DetachedCriteria.forClass(entityClazz);
        for (Criterion c : cs) {
            if (null != c) {
                dc.add(c);
            }
        }
        return find(dc);
    }

    /**
     * 分页条件查询
     *
     * @param cb  查询参数
     * @param <T> 实体类型
     * @param <P> 实体主键类型
     * @return 查询结果
     */
    public <T extends IEntity<P>, P extends Serializable> List<T> find(CriteriaBuilder<T> cb) {
        checkCriteriaBuilder(cb);
        DetachedCriteria detachedCriteria = cb.buildDeCriteria(false);
        return find(detachedCriteria, cb.getPageRequest());
    }

    /**
     * 分页条件查询
     *
     * @param qp  查询参数
     * @param <T> 实体类型
     * @param <P> 实体主键类型
     * @return 查询结果
     */
    public <T extends IEntity<P>, P extends Serializable> List<T> find(QueryParam qp) {
        //noinspection unchecked
        return getRepo((Class<T>) checkQueryParam(qp)).find(qp);
    }

    /**
     * 根据hql查询
     *
     * @param entityClazz 实体类Class，必须具有{@link Entity}注解
     * @param hql         hql语句
     * @param args        查询参数
     * @param <T>         实体类型
     * @param <P>         实体主键类型
     * @return 查询结果
     */
    public <T extends IEntity<P>, P extends Serializable> List<T> find(Class<T> entityClazz, String hql, Object... args) {
        Session sess = DataSourceContext.getSession();
        Query<T> q = sess.createQuery(hql, entityClazz);
        if (args != null && args.length > 0) {
            for (int i = 0; i < args.length; i++) {
                q.setParameter(i, args[i]);
            }
        }
        return q.list();
    }

    /**
     * 全记录查询
     *
     * @param entityClazz 实体类Class，必须具有{@link Entity}注解
     * @param <T>         实体类型
     * @param <P>         实体主键类型
     * @return 结果数据
     */
    public <T extends IEntity<P>, P extends Serializable> List<T> findAll(Class<T> entityClazz) {
        Assert.notNull(entityClazz, ENTITY_CLS_LOST);
        Session sess = DataSourceContext.getSession();
        Criteria ct = DetachedCriteria.forClass(entityClazz).getExecutableCriteria(sess);
        ct.setCacheMode(CacheMode.NORMAL);
        //noinspection unchecked
        return ct.list();
    }

    /**
     * 通过唯一属性查询
     * <p>如果结果不唯一或不存在，抛出异常</p>
     *
     * @param cb  查询参数
     * @param <T> 实体类型
     * @param <P> 实体主键类型
     * @return 查询到的实体
     */
    public <T extends IEntity<P>, P extends Serializable> T unique(CriteriaBuilder<T> cb) {
        checkCriteriaBuilder(cb);
        DetachedCriteria dc = cb.buildDeCriteria(false);
        Session sess = DataSourceContext.getSession();
        Criteria ct = dc.getExecutableCriteria(sess);
        ct.setFirstResult(0);
        ct.setMaxResults(2);
        @SuppressWarnings("unchecked")
        List<T> list = ct.list();
        if (list.isEmpty()) {
            throw new SQLException(SQLError.RESULT_NOT_FOUND);
        } else if (list.size() > 1) {
            throw new SQLException(SQLError.UNIQUE_EXCEPT_ERROR);
        }
        return list.get(0);
    }

    /**
     * 通过属性查询
     * <p>如果结果不唯一或不存在，抛出异常</p>
     *
     * @param entityClazz 实体类Class，必须具有{@link Entity}注解
     * @param cts         条件数组
     * @param <T>         实体类型
     * @param <P>         实体主键类型
     * @return 查询到的实体
     */
    public <T extends IEntity<P>, P extends Serializable> T unique(Class<T> entityClazz, Criterion... cts) {
        Assert.notNull(entityClazz, ENTITY_CLS_LOST);
        return unique(CriteriaBuilder.forClass(entityClazz).addCriterion(cts));
    }

    /**
     * 通过属性查询
     * <p>如果结果不唯一或不存在，抛出异常</p>
     *
     * @param entityClazz 实体类Class，必须具有{@link Entity}注解
     * @param prop        属性名称
     * @param value       值
     * @param <T>         实体类型
     * @param <P>         实体主键类型
     * @return 查询到的实体
     */
    public <T extends IEntity<P>, P extends Serializable> T unique(Class<T> entityClazz, String prop, Object value) {
        return unique(entityClazz, Restrictions.eq(prop, value));
    }

    /**
     * 查询且锁定
     * <p>如果结果不唯一或不存在，抛出异常</p>
     *
     * @param entityClazz 实体类Class，必须具有{@link Entity}注解
     * @param prop        属性名
     * @param value       属性值
     * @param <T>         实体类型
     * @param <P>         实体主键类型
     * @return 查询到的实体
     */
    public <T extends IEntity<P>, P extends Serializable> T uniqueWithLock(Class<T> entityClazz, String prop, Object value) {
        Assert.notNull(entityClazz, ENTITY_CLS_LOST);
        Session sess = DataSourceContext.getSession();
        DetachedCriteria dc = DetachedCriteria.forClass(entityClazz);
        dc.add(Restrictions.eq(prop, value));
        Criteria ct = dc.getExecutableCriteria(sess);
        ct.setCacheable(true);
        ct.setFirstResult(0);
        ct.setMaxResults(2);

        @SuppressWarnings("unchecked")
        List<T> list = ct.list();
        T t;
        if (list.isEmpty()) {
            throw new SQLException(SQLError.RESULT_NOT_FOUND);
        } else if (list.size() > 1) {
            throw new SQLException(SQLError.UNIQUE_EXCEPT_ERROR);
        }
        t = list.get(0);
        sess.buildLockRequest(LockOptions.UPGRADE).lock(t);
        return t;
    }

    /**
     * 通过唯一属性查询
     * <p>如果不是唯一属性，则返回第一个满足条件的实体</p>
     *
     * @param cb  查询参数
     * @param <T> 实体类型
     * @param <P> 实体主键类型
     * @return 查询结果
     */
    public <T extends IEntity<P>, P extends Serializable> T findOne(CriteriaBuilder<T> cb) {
        checkCriteriaBuilder(cb);
        DetachedCriteria dc = cb.buildDeCriteria(false);
        Session sess = DataSourceContext.getSession();
        Criteria ct = dc.getExecutableCriteria(sess);
        ct.setFirstResult(0);
        ct.setMaxResults(1);
        @SuppressWarnings("unchecked")
        List<T> list = ct.list();
        if (list.isEmpty())
            return null;
        return list.get(0);
    }

    /**
     * 通过属性查询
     * <p>如果不是唯一属性，则返回第一个满足条件的实体</p>
     *
     * @param entityClazz 实体类Class，必须具有{@link Entity}注解
     * @param cts         条件数组
     * @param <T>         实体类型
     * @param <P>         实体主键类型
     * @return 查询结果
     */
    public <T extends IEntity<P>, P extends Serializable> T findOne(Class<T> entityClazz, Criterion... cts) {
        Assert.notNull(entityClazz, ENTITY_CLS_LOST);
        return findOne(CriteriaBuilder.forClass(entityClazz).addCriterion(cts));
    }

    /**
     * 通过属性查询
     * <p>如果不是唯一属性，则返回第一个满足条件的实体</p>
     *
     * @param entityClazz 实体类Class，必须具有{@link Entity}注解
     * @param prop        属性名称
     * @param value       值
     * @param <T>         实体类型
     * @param <P>         实体主键类型
     * @return 查询到的实体
     */
    public <T extends IEntity<P>, P extends Serializable> T findOne(Class<T> entityClazz, String prop, Object value) {
        return findOne(entityClazz, Restrictions.eq(prop, value));
    }

    /**
     * 查询且锁定
     *
     * @param entityClazz 实体类Class，必须具有{@link Entity}注解
     * @param prop        属性名
     * @param value       属性值
     * @param <T>         实体类型参数
     * @param <P>         实体主键类型
     * @return 查询到的实体
     */
    public <T extends IEntity<P>, P extends Serializable> T findOneWithLock(Class<T> entityClazz, String prop, Object value) {
        Assert.notNull(entityClazz, ENTITY_CLS_LOST);
        Session sess = DataSourceContext.getSession();
        DetachedCriteria dc = DetachedCriteria.forClass(entityClazz);
        dc.add(Restrictions.eq(prop, value));
        Criteria ct = dc.getExecutableCriteria(sess);
        ct.setCacheable(true);
        ct.setFirstResult(0);
        ct.setMaxResults(1);

        @SuppressWarnings("unchecked")
        List<T> list = ct.list();
        T t;
        if (list.isEmpty()) {
            throw new SQLException(SQLError.RESULT_NOT_FOUND);
        }
        t = list.get(0);
        sess.buildLockRequest(LockOptions.UPGRADE).lock(t);
        return t;
    }


    /**
     * 判断是否有存在已有实体
     *
     * @param entityClazz 实体类Class，必须具有{@link Entity}注解
     * @param id          指定ID
     * @param <T>         实体类型
     * @param <P>         实体主键类型
     * @return 是/否
     */
    public <T extends IEntity<P>, P extends Serializable> boolean exist(Class<T> entityClazz, P id) {
        if (id == null) {
            return false;
        }
        Assert.notNull(entityClazz, ENTITY_CLS_LOST);
        Long c = count(CriteriaBuilder.forClass(entityClazz).eq("id", id));
        return c > 0;
    }

    /**
     * 判断是否有存在已有实体 默认多个字段条件为，条件并列and
     *
     * @param entityClazz 实体类Class，必须具有{@link Entity}注解
     * @param params      查询参数
     * @param <T>         实体类型
     * @param <P>         实体主键类型
     * @return 是/否
     */
    public <T extends IEntity<P>, P extends Serializable> boolean exist(Class<T> entityClazz, Map<String, Object> params) {
        return exist(entityClazz, params, null, true);
    }

    /**
     * 判断是否有存在已有实体 默认多个字段条件为，条件并列and
     *
     * @param entityClazz 实体类Class，必须具有{@link Entity}注解
     * @param params      查询参数
     * @param notId       忽略匹配的Id
     * @param <T>         实体类型
     * @param <P>         实体主键类型
     * @return 是/否
     */
    public <T extends IEntity<P>, P extends Serializable> boolean exist(Class<T> entityClazz, Map<String, Object> params, P notId) {
        return exist(entityClazz, params, notId, true);
    }

    /**
     * 判断是否有存在已有实体
     *
     * @param entityClazz 实体类Class，必须具有{@link Entity}注解
     * @param params      查询参数
     * @param notId       忽略匹配的Id
     * @param isAnd       使用and / or 连接条件
     * @param <T>         实体类型参数
     * @param <P>         实体主键类型
     * @return 是/否
     */
    public <T extends IEntity<P>, P extends Serializable> boolean exist(Class<T> entityClazz, Map<String, Object> params, P notId, boolean isAnd) {
        Assert.notNull(entityClazz, ENTITY_CLS_LOST);
        DetachedCriteria dc = DetachedCriteria.forClass(entityClazz);
        List<Criterion> criteriaList = params.entrySet().stream().filter(e -> e.getValue() != null)
            .map(entry -> Restrictions.eq(entry.getKey(), entry.getValue())).collect(Collectors.toList());
        if (criteriaList.isEmpty()) {
            return false;
        }
        if (isAnd)
            dc.add(Restrictions.and(criteriaList.toArray(new Criterion[]{})));
        else
            dc.add(Restrictions.or(criteriaList.toArray(new Criterion[]{})));
        if (notId != null)
            dc.add(Restrictions.ne("id", notId));
        Session sess = DataSourceContext.getSession();
        // 总数查询
        Criteria ct = dc.getExecutableCriteria(sess);
        List<CriteriaImpl.OrderEntry> orderEntries = BeanUtils.getFieldValue(ct, ORDER_ENTRIES);
        orderEntries.clear();
        Long total = (Long) ct.setProjection(Projections.rowCount()).uniqueResult();
        return total > 0;
    }

    /**
     * 判断是否存在已有实体
     *
     * @param cb    查询参数
     * @param notId 拆除的id
     * @param <T>   实体类型
     * @param <P>   实体主键类型
     * @return 查询结果
     */
    public <T extends IEntity<P>, P extends Serializable> boolean exist(CriteriaBuilder<T> cb, P notId) {
        checkCriteriaBuilder(cb);
        if (notId != null)
            cb.notEq("id", notId);
        Long c = count(cb);
        return c > 0;
    }

    public Long count(CriteriaBuilder<? extends IEntity> cb) {
        checkCriteriaBuilder(cb);
        Session sess = DataSourceContext.getSession();
        Criteria ct = cb.buildDeCriteria(false).getExecutableCriteria(sess);
        ct.setCacheable(false);
        List<CriteriaImpl.OrderEntry> orderEntries = BeanUtils.getFieldValue(ct, ORDER_ENTRIES);
        orderEntries.clear();
        return (Long) ct.setProjection(Projections.rowCount()).uniqueResult();
    }

    /**
     * 根据条件查询DTO列表
     *
     * @param cb  查询参数
     * @param <T> 实体类型
     * @param <P> 实体主键类型
     * @return 查询结果
     */
    public <T extends IEntity<P>, P extends Serializable> Page<T> page(CriteriaBuilder<T> cb) {
        checkCriteriaBuilder(cb);
        Session sess = DataSourceContext.getSession();
        Criteria ct = cb.buildDeCriteria(true).getExecutableCriteria(sess);
        ct.setCacheable(false);
        ct.setResultTransformer(CriteriaSpecification.ALIAS_TO_ENTITY_MAP);
        if (cb.getPageRequest() != null) {
            ct.setFirstResult(cb.getPageRequest().getOffset());
            ct.setMaxResults(cb.getPageRequest().getPageSize());
        }
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> list = ct.list();
        ct.setFirstResult(0);
        ct.setMaxResults(MAX_RECORDS);
        List<CriteriaImpl.OrderEntry> orderEntries = BeanUtils.getFieldValue(ct, ORDER_ENTRIES);
        orderEntries.clear();
        Long total = (Long) ct.setProjection(Projections.rowCount()).uniqueResult();
        List<T> res = EntityUtils.wrapperMapToBeanList(cb.getEnCls(), list);
        return new Page<>(res, total, cb.getPageRequest() == null ? total.intValue() : cb.getPageRequest().getPageSize());
    }

    /**
     * 根据条件查询DTO列表
     *
     * @param qp  通用查询参数
     * @param <T> 实体类型
     * @param <P> 实体主键类型
     * @return 查询结果
     */
    public <T extends IEntity<P>, P extends Serializable> Page<T> page(QueryParam qp) {
        //noinspection unchecked
        return getRepo((Class<T>) checkQueryParam(qp)).page(qp);
    }

    /**
     * 根据条件查询DTO（扁平化的Map）
     *
     * @param cb  查询参数
     * @param <T> 实体类型
     * @param <P> 实体主键类型
     * @return 查询结果
     */
    public <T extends IEntity<P>, P extends Serializable> Page<Map<String, Object>> pageFlatMap(CriteriaBuilder<T> cb) {
        return getRepo(cb.getEnCls()).pageFlatMap(cb);
    }

    /**
     * 根据条件查询DTO（扁平化的Map）
     *
     * @param qp  通用查询参数
     * @param <T> 实体类型
     * @param <P> 实体主键类型
     * @return 查询结果
     */
    public <T extends IEntity<P>, P extends Serializable> Page<Map<String, Object>> pageFlatMap(QueryParam qp) {
        //noinspection unchecked
        return getRepo((Class<T>) checkQueryParam(qp)).pageFlatMap(qp);
    }

    /**
     * 根据条件查询DTO（层次化的Map）
     *
     * @param cb  查询参数
     * @param <T> 实体类型
     * @param <P> 实体主键类型
     * @return 查询结果
     */
    public <T extends IEntity<P>, P extends Serializable> Page<Map<String, Object>> pageMap(CriteriaBuilder<T> cb) {
        return getRepo(cb.getEnCls()).pageMap(cb);
    }

    /**
     * 根据条件查询DTO（层次化的Map）
     *
     * @param qp  通用查询参数
     * @param <T> 实体类型
     * @param <P> 实体主键类型
     * @return 查询结果
     */
    public <T extends IEntity<P>, P extends Serializable> Page<Map<String, Object>> pageMap(QueryParam qp) {
        //noinspection unchecked
        return getRepo((Class<T>) checkQueryParam(qp)).pageMap(qp);
    }


    /**
     * 根据条件查询DTO列表
     *
     * @param entityClazz 实体类Class，必须具有{@link Entity}注解
     * @param map         参数
     * @param <T>         实体类型参数
     * @param <P>         实体主键类型
     * @return 查询结果
     */
    public <T extends IEntity<P>, P extends Serializable> List<T> list(Class<T> entityClazz, Map<String, Object> map) {
        Assert.notNull(entityClazz, ENTITY_CLS_LOST);
        CriteriaBuilder<T> cb = CriteriaBuilder.forClass(entityClazz)
            .addCriterion(map.entrySet().stream().map(entry -> Restrictions.eq(entry.getKey(), entry.getValue())).collect(Collectors.toList()));
        return list(cb);
    }

    /**
     * 根据条件查询DTO列表
     *
     * @param cb  查询参数
     * @param <T> 实体类型
     * @param <P> 实体主键类型
     * @return 查询结果
     */
    public <T extends IEntity<P>, P extends Serializable> List<T> list(CriteriaBuilder<T> cb) {
        List<Map<String, Object>> list = listFlatMap(cb);
        return EntityUtils.wrapperMapToBeanList(cb.getEnCls(), list);
    }

    /**
     * 根据条件查询DTO列表
     *
     * @param qp  查询参数
     * @param <T> 实体类型
     * @param <P> 实体主键类型
     * @return 查询结果
     */
    public <T extends IEntity<P>, P extends Serializable> List<T> list(QueryParam qp) {
        //noinspection unchecked
        return getRepo((Class<T>) checkQueryParam(qp)).list(qp);
    }

    /**
     * 根据条件查询DTO（扁平化的Map）
     *
     * @param cb  查询参数
     * @param <T> 实体类型
     * @param <P> 实体主键类型
     * @return 查询结果
     */
    public <T extends IEntity<P>, P extends Serializable> List<Map<String, Object>> listFlatMap(CriteriaBuilder<T> cb) {
        return listMap(cb, false);
    }

    /**
     * 根据条件查询DTO（扁平化的Map）
     *
     * @param qp  查询参数
     * @param <T> 实体类型
     * @param <P> 实体主键类型
     * @return 查询结果
     */
    public <T extends IEntity<P>, P extends Serializable> List<Map<String, Object>> listFlatMap(QueryParam qp) {
        //noinspection unchecked
        return getRepo((Class<T>) checkQueryParam(qp)).listFlatMap(qp);
    }

    /**
     * 根据条件查询DTO（层次化的Map）
     *
     * @param cb  查询参数
     * @param <T> 实体类型
     * @param <P> 实体主键类型
     * @return 查询结果
     */
    public <T extends IEntity<P>, P extends Serializable> List<Map<String, Object>> listMap(CriteriaBuilder<T> cb) {
        return listMap(cb, true);
    }

    /**
     * 根据条件查询DTO（层次化的Map）
     *
     * @param qp  查询参数
     * @param <T> 实体类型
     * @param <P> 实体主键类型
     * @return 查询结果
     */
    public <T extends IEntity<P>, P extends Serializable> List<Map<String, Object>> listMap(QueryParam qp) {
        //noinspection unchecked
        return getRepo((Class<T>) checkQueryParam(qp)).listMap(qp);
    }


    /* ---BEGING---***********************委托SQLManager执行SQL语句**************************** */
    public Map<String, Object> findOneAsMapBySql(String sqlId, Map<String, ?> paramMap) {
        return doReturningWork(connection -> sqlManager.findOneAsMap(connection, sqlId, paramMap));
    }

    public <T extends IEntity<P>, P extends Serializable> T findOneBySql(Class<T> entityClazz, String sqlId, Map<String, ?> paramMap) {
        return doReturningWork(connection -> sqlManager.findOne(connection, sqlId, entityClazz, paramMap));
    }

    public <T extends IEntity<P>, P extends Serializable> List<T> listBySql(Class<T> entityClazz, String sqlId, Object... mapParams) {
        return doReturningWork(connection -> sqlManager.list(connection, sqlId, entityClazz, mapParams));
    }

    public <T extends IEntity<P>, P extends Serializable> List<T> listBySql(Class<T> entityClazz, String sqlId, Map<String, ?> paramMap) {
        return doReturningWork(connection -> sqlManager.list(connection, sqlId, entityClazz, paramMap));
    }

    public List<Map<String, Object>> listMapBySql(String sqlId, Object... mapParams) {
        return doReturningWork(connection -> sqlManager.listAsMap(connection, sqlId, mapParams));
    }

    public List<Map<String, Object>> listMapBySql(String sqlId, Map<String, ?> paramMap) {
        return doReturningWork(connection -> sqlManager.listAsMap(connection, sqlId, paramMap));
    }

    public <T extends IEntity<P>, P extends Serializable> Page<T> pageBySql(Class<T> entityClazz, String sqlId, Map<String, ?> paramMap, PageRequest pageRequest) {
        return doReturningWork(connection -> sqlManager.listAsPage(connection, sqlId, entityClazz, paramMap, pageRequest));
    }

    public Page<Map<String, Object>> pageMapBySql(String sqlId, Map<String, ?> paramMap, PageRequest pageRequest) {
        return doReturningWork(connection -> sqlManager.listAsPageMap(connection, sqlId, paramMap, pageRequest));
    }

    public Long count(String sqlId, Map<String, ?> paramMap) {
        return doReturningWork(connection -> sqlManager.count(connection, sqlId, paramMap));
    }

    public int executeCUD(String sqlId, Map<String, ?> paramMap) {
        return doReturningWork(connection -> sqlManager.executeCUD(connection, sqlId, paramMap));
    }

    public int[] executeBatch(String sqlId, List<Map<String, ?>> paramMaps) {
        return doReturningWork(connection -> sqlManager.executeBatch(connection, sqlId, paramMaps));
    }
    /* ---END---***********************委托SQLManager执行SQL语句******************************* */


    /**
     * 将所有挂起的保存、更新和删除操作更新到数据库。
     * <p>Only invoke this for selective eager flushing, for example when
     * JDBC code needs to see certain changes within the same transaction.
     * Else, it is preferable to rely on auto-flushing at transaction
     * completion.
     *
     * @see Session#flush
     */
    public void flush() {
        DataSourceContext.getSession().flush();
    }

    /**
     * 将当前实体从Session缓存中剔除。对实体的改动将会被丢弃，不会同步到数据库中。如果实体的关联属性映射为
     * {@code cascade="evict"}，该操作将会级联剔除所有的关联实体
     *
     * @param entity 需要剔除的实体
     * @param <T>    实体类型
     * @param <P>    实体主键类型
     */
    public <T extends IEntity<P>, P extends Serializable> void evict(T entity) {
        if (Objects.nonNull(entity)) {
            DataSourceContext.getSession().evict(entity);
        }
    }

    /**
     * 从Session缓存中移除所有持久化对象，并取消所有已挂起的保存，更新和删除操作
     *
     * @see Session#clear
     */
    public void clear() {
        DataSourceContext.getSession().clear();
    }

    /**
     * 通过使用指定的jdbc连接执行用户自定义任务
     *
     * @param work 需要执行的任务
     */
    public void doWork(Work work) {
        if (null != work) {
            DataSourceContext.getSession().doWork(work);
        }
    }

    /**
     * 通过使用指定的jdbc连接执行CUD操作
     *
     * @param sql 需要执行的CUD语句
     * @return 受影响行数 {@link PreparedStatement#executeUpdate}.
     */
    public int doWork(String sql) {
        Assert.notEmpty(sql, "SQL语句不能为空");
        Assert.isTrue(!StringUtils.trimToEmpty(sql).toLowerCase().startsWith("select"), "不能执行select语句，只能执行CUD语句");
        int[] affects = {-1};
        DataSourceContext.getSession().doWork(connection -> {
            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                affects[0] = ps.executeUpdate();
            }
        });
        return affects[0];
    }

    /**
     * 通过使用指定的jdbc连接执行用户自定义任务，可以获取执行后的返回结果{@link ReturningWork#execute} call.
     *
     * @param work 需要执行的任务
     * @param <R>  转换结果类型
     * @return 执行结果
     */
    public <R> R doReturningWork(ReturningWork<R> work) {
        if (null == work) {
            return null;
        }
        return DataSourceContext.getSession().doReturningWork(work);
    }

    /**
     * 通过使用指定的jdbc连接执行用户自定义查询，可以获取执行后的返回结果{@link ReturningWork#execute} call.
     * <p>只有执行SELECT语句时才有返回结果</p>
     *
     * @param sql         需要执行的查询语句
     * @param transformer 结果转换器
     * @param <R>         转换结果类型
     * @return 执行结果 {@link PreparedStatement#execute}
     */
    public <R> R doReturningWork(String sql, Function<ResultSet, R> transformer) {
        Assert.notEmpty(sql, "SQL语句不能为空");
        return DataSourceContext.getSession().doReturningWork(connection -> {
            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                boolean isRs = ps.execute();
                try (ResultSet rs = isRs ? ps.getResultSet() : null) {
                    return isRs ? transformer.apply(rs) : null;
                }
            }
        });
    }

    /**
     * 根据条件查询DTO（Map）
     *
     * @param cb   查询条件
     * @param wrap 是否需要转换成层次Map
     * @param <T>  实体类型
     * @param <P>  实体主键类型
     * @return 查询到的DTO
     */
    public <T extends IEntity<P>, P extends Serializable> List<Map<String, Object>> listMap(CriteriaBuilder<T> cb, boolean wrap) {
        return getRepo(cb.getEnCls()).listMap(cb, wrap);
    }

    private void checkCriteriaBuilder(CriteriaBuilder<?> cb) {
        Assert.notNull(cb, "CriteriaBuilder need a non-null value");
        if (null == cb.getEnCls() || null == cb.getEnCls().getAnnotation(Entity.class)) {
            throw new SimplifiedException(ENTITY_CLS_LOST);
        }
    }

    private Class<?> checkQueryParam(QueryParam queryParam) {
        Assert.notNull(queryParam, "查询条件参数不能为null");
        Assert.notEmpty(queryParam.getCls(), "必须指定查询的实体");
        try {
            return ClassUtils.getClass(queryParam.getCls());
        } catch (ClassNotFoundException e) {
            throw new SimplifiedException("查询的实体类型不存在");
        }
    }
}
