package org.spin.jpa.core;

import org.hibernate.CacheMode;
import org.hibernate.Criteria;
import org.hibernate.FlushMode;
import org.hibernate.LockMode;
import org.hibernate.LockOptions;
import org.hibernate.ReplicationMode;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.criterion.CriteriaSpecification;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.ProjectionList;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Property;
import org.hibernate.criterion.Restrictions;
import org.hibernate.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spin.jpa.query.CriteriaBuilder;
import org.spin.jpa.query.QueryParam;
import org.spin.jpa.query.QueryParamParser;
import org.spin.jpa.sql.SQLManager;
import org.spin.sys.Assert;
import org.spin.sys.SessionUser;
import org.spin.throwable.SQLException;
import org.spin.throwable.SimplifiedException;
import org.spin.util.BeanUtils;
import org.spin.util.EntityUtils;
import org.spin.util.ReflectionUtils;
import org.spin.util.SessionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.data.domain.PageRequest;
import org.springframework.orm.hibernate5.HibernateOptimisticLockingFailureException;
import org.springframework.stereotype.Component;

import javax.persistence.Transient;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


/**
 * 通用数据访问层代码
 * <p>所有的Dao均继承此类。支持：
 * <pre>
 * 1、基于Jpa规范的Repository
 * 2、基于JdbcTemplate和NamedJdbcTemplate的动态SQL查询
 * </pre>
 *
 * @author xuweinan
 * @version V1.3
 */
@Component
public class ARepository<T extends IEntity<PK>, PK extends Serializable> {
    private static final Logger logger = LoggerFactory.getLogger(ARepository.class);
    private static final String ID_MUST_NOT_BE_NULL = "The given id must not be null!";
    private static final int MAX_RECORDS = 100000000;
    protected static final ThreadLocal<Deque<Session>> THREADLOCAL_SESSIONS = new ThreadLocal<Deque<Session>>() {
    };

    private boolean checkWriteOperations = true;

    @Autowired
    private QueryParamParser queryParamParser;

    @Autowired
    protected SessionFactory sessFactory;

    @Autowired
    protected SQLManager sqlManager;
    protected Class<T> entityClazz;

    public ARepository() {
        //noinspection unchecked
        this.entityClazz = (Class<T>) ReflectionUtils.getSuperClassGenricType(this.getClass());
    }

    public ARepository(Class<T> entityClass) {
        this.entityClazz = entityClass;
    }

    /**
     * 获得当前线程的session 如果线程Local变量中有绑定，返回该session
     * 否则，调用sessFactory的getCurrentSession
     */
    public Session getSession() {
        Session sess = peekThreadSession();
        if (sess == null) {
            sess = sessFactory.getCurrentSession();
        }
        return sess;
    }

    /**
     * Open a new session, if thread has one more session, return the last session.
     */
    public Session openSession() {
        return openSession(false);
    }

    /**
     * Open a session manually associated with current thread,
     * other thread-local transaction may invalid.
     *
     * @param requiredNew force to open a new session
     */
    public Session openSession(boolean requiredNew) {
        Session session = peekThreadSession();
        if (requiredNew || session == null) {
            session = sessFactory.openSession();
            pushTreadSession(session);
        }
        return session;
    }

    /**
     * Close all the session in current thread opened manually
     */
    public void closeSession() {
        Session session = popTreadSession();
        if (session != null && session.isOpen()) {
            Transaction tran = session.getTransaction();
            if (tran != null && tran.isActive()) {
                tran.commit();
                logger.info("commit before closeSession");
            }
            session.close();
        }
    }

    /**
     * 打开事务 如果线程已有事务就返回，不重复打开
     */
    public Transaction openTransaction() {
        return openTransaction(false);
    }

    /**
     * Open a session associated with current thread, then open the transaction.
     *
     * @param requiredNew force to open the transaction
     */
    public Transaction openTransaction(boolean requiredNew) {
        Session session = openSession(requiredNew);
        Transaction tran = session.getTransaction() == null ? session.beginTransaction() : session.getTransaction();
        tran.begin();
        return tran;
    }

    /**
     * Saves a given entity. Use the returned instance for further operations as the save operation might have changed the
     * entity instance completely.
     *
     * @param entity entity to save
     * @return the saved entity (has id)
     */
    public T save(final T entity) {
        return save(entity, false);
    }

    /**
     * Saves a given entity. Use the returned instance for further operations as the save operation might have changed the
     * entity instance completely.
     *
     * @param entity     entity to save
     * @param saveWithPk insert with a assigned primary key
     * @return the saved entity (has id)
     */
    public T save(final T entity, boolean saveWithPk) {
        Assert.notNull(entity, "The entity to save must be a NON-NULL object");
        if (entity instanceof AbstractEntity) {
            AbstractEntity aEn = (AbstractEntity) entity;
            SessionUser user = SessionUtils.getCurrentUser();
            aEn.setUpdateTime(LocalDateTime.now());
            aEn.setUpdateBy(user == null ? null : BaseUser.ref(user.getId()));
            if (null == aEn.getId() || saveWithPk) {
                aEn.setCreateBy(user == null ? null : BaseUser.ref(user.getId()));
                aEn.setCreateTime(LocalDateTime.now());
            }
        }
        try {
            if (null == entity.getId() || saveWithPk)
                getSession().save(entity);
            else
                getSession().update(entity);
        } catch (HibernateOptimisticLockingFailureException ope) {
            throw new SimplifiedException("The entity is expired", ope);
        }
        return get(entity.getId());
    }

    /**
     * Saves all given entities.
     *
     * @param entities entities to save
     * @return the saved entities
     */
    public List<T> save(Iterable<T> entities) {
        List<T> result = new ArrayList<>();
        if (entities == null) {
            return result;
        }
        for (T entity : entities) {
            result.add(save(entity));
        }
        return result;
    }

    public T merge(final T entity) {
        //noinspection unchecked
        return (T) getSession().merge(entity);
    }

    /**
     * Persist the state of the given detached instance according to the
     * given replication mode, reusing the current identifier value.
     *
     * @param entity          the persistent object to replicate
     * @param replicationMode the Hibernate ReplicationMode
     * @throws DataAccessException in case of Hibernate errors
     * @see Session#replicate(Object, ReplicationMode)
     */
    public void replicate(final T entity, final ReplicationMode replicationMode) {
        checkWriteOperationAllowed(getSession());
        getSession().replicate(entity, replicationMode);
    }

    /**
     * Return the persistent instance of the actual entity class
     * with the given identifier, or {@code null} if not found.
     * <p>This method is a thin wrapper around
     * {@link Session#get(Class, Serializable)} for convenience.
     * For an explanation of the exact semantics of this method, please do refer to
     * the Hibernate API documentation in the first instance.
     *
     * @param id the identifier of the persistent instance
     * @return the persistent instance, or {@code null} if not found
     * @throws DataAccessException in case of Hibernate errors
     * @see Session#get(Class, Serializable)
     */
    public T get(final PK id) {

        if (null == id) {
            return null;
        }

        return getSession().get(this.entityClazz, id);
    }

    /**
     * Return the persistent instance of the given identifier, or {@code null} if not found.
     * <p>Obtains the specified lock mode if the instance exists.
     * <p>This method is a thin wrapper around
     * {@link Session#get(Class, Serializable, LockMode)} for convenience.
     * For an explanation of the exact semantics of this method, please do refer to
     * the Hibernate API documentation in the first instance.
     *
     * @param id       the identifier of the persistent instance
     * @param lockMode the lock mode to obtain
     * @return the persistent instance, or {@code null} if not found
     * @see Session#get(Class, Serializable, LockMode)
     */
    public T get(final PK id, final LockMode lockMode) {
        if (lockMode != null) {
            return getSession().get(this.entityClazz, id, new LockOptions(lockMode));
        } else {
            return getSession().get(this.entityClazz, id);
        }
    }

    /**
     * 获取持久态实体对象并锁定(FOR UPDATE悲观锁)
     */
    public T getWithLock(final PK k) {
        Session sess = getSession();
        T t = sess.get(this.entityClazz, k);
        sess.buildLockRequest(LockOptions.UPGRADE).lock(t);
        return t;
    }

    /**
     * Return the persistent instance of the given entity class
     * with the given identifier, throwing an exception if not found.
     * <p>This method is a thin wrapper around
     * {@link Session#load(Class, Serializable)} for convenience.
     * For an explanation of the exact semantics of this method, please do refer to
     * the Hibernate API documentation in the first instance.
     *
     * @param id the identifier of the persistent instance
     * @return the persistent instance
     * @throws org.springframework.orm.ObjectRetrievalFailureException if not found
     * @throws DataAccessException                                     in case of Hibernate errors
     * @see Session#load(Class, Serializable)
     */
    public T load(final PK id) {
        return getSession().load(this.entityClazz, id);
    }

    /**
     * Return the persistent instance of the given entity class
     * with the given identifier, throwing an exception if not found.
     * Obtains the specified lock mode if the instance exists.
     * <p>This method is a thin wrapper around
     * {@link Session#load(Class, Serializable, LockMode)} for convenience.
     * For an explanation of the exact semantics of this method, please do refer to
     * the Hibernate API documentation in the first instance.
     *
     * @param id       the identifier of the persistent instance
     * @param lockMode the lock mode to obtain
     * @return the persistent instance
     * @throws org.springframework.orm.ObjectRetrievalFailureException if not found
     * @throws DataAccessException                                     in case of Hibernate errors
     * @see Session#load(Class, Serializable)
     */
    public T load(final PK id, final LockMode lockMode) {
        if (lockMode != null) {
            return getSession().load(entityClazz, id, new LockOptions(lockMode));
        } else {
            return getSession().load(entityClazz, id);
        }
    }

    /**
     * 主键获取指定深度的属性的瞬态对象
     */
    public T getDto(final PK k, int depth) {
        return EntityUtils.getDto(get(k), depth);
    }

    /**
     * Re-read the state of the given persistent instance.
     *
     * @param entity the persistent instance to re-read
     * @throws DataAccessException in case of Hibernate errors
     * @see Session#refresh(Object)
     */
    public void refresh(final T entity) {
        refresh(entity, null);
    }

    /**
     * Re-read the state of the given persistent instance.
     * Obtains the specified lock mode for the instance.
     *
     * @param entity   the persistent instance to re-read
     * @param lockMode the lock mode to obtain
     * @throws DataAccessException in case of Hibernate errors
     * @see Session#refresh(Object, LockMode)
     */
    public void refresh(final T entity, final LockMode lockMode) {
        if (lockMode != null) {
            getSession().refresh(entity, new LockOptions(lockMode));
        } else {
            getSession().refresh(entity);
        }
    }

    /**
     * Check whether the given object is in the Session cache.
     *
     * @param entity the persistence instance to check
     * @return whether the given object is in the Session cache
     * @throws DataAccessException if there is a Hibernate error
     * @see Session#contains
     */
    public boolean contains(final T entity) {
        return getSession().contains(entity);
    }

    /**
     * Deletes a given entity.
     *
     * @throws IllegalArgumentException in case the given entity is {@literal null}.
     */
    public void delete(T entity) {
        Assert.notNull(entity, "The entity to be deleted is null");
        getSession().delete(entity);
    }

    /**
     * Deletes the entity with the given id.
     *
     * @throws IllegalArgumentException in case the given {@code id} is {@literal null}
     */
    public void delete(PK k) {
        Assert.notNull(k, ID_MUST_NOT_BE_NULL);
        T entity = get(k);
        Assert.notNull(entity, "Entity not found, or was deleted: [" + this.entityClazz.getSimpleName() + "|" + k + "]");
        getSession().delete(entity);
        getSession().flush();
    }

    /**
     * Deletes the given entities.
     *
     * @throws IllegalArgumentException in case the given {@link Iterable} is {@literal null}.
     */
    public void delete(Iterable<? extends T> entities) {
        Assert.notNull(entities, "The given Iterable of entities not be null!");
        for (T entity : entities) {
            delete(entity);
        }
    }

    /**
     * 批量删除实体
     * <p>如果条件为空，删除所有</p>
     */
    public void delete(Criterion... cs) {
        DetachedCriteria dc = DetachedCriteria.forClass(this.entityClazz);
        for (Criterion c : cs)
            dc.add(c);

        List<T> enList = find(dc);
        enList.forEach(this::delete);
    }

    /**
     * 通过hql批量删除实体
     * <p>如果条件为空，删除所有</p>
     */
    public void delete(String conditions) {
        StringBuilder hql = new StringBuilder("from ");
        hql.append(this.entityClazz.getSimpleName()).append(" ");
        if (conditions.length() > 0)
            hql.append("where ").append(conditions);
        getSession().delete(hql);
    }

    /**
     * 分页条件查询
     *
     * @param dc 离线条件
     * @param pr 分页请求
     */
    public List<T> find(DetachedCriteria dc, PageRequest... pr) {
        Session sess = getSession();
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
     */
    public List<T> find(Criterion... cs) {
        DetachedCriteria dc = DetachedCriteria.forClass(this.entityClazz);
        for (Criterion c : cs)
            if (null != c)
                dc.add(c);
        return find(dc);
    }

    /**
     * 分页条件查询
     */
    public List<T> find(CriteriaBuilder cb) {
        DetachedCriteria detachedCriteria = cb.buildDeCriteria(false);
        return find(detachedCriteria, cb.getPageRequest());
    }

    /**
     * 分页条件查询
     */
    public List<T> find(QueryParam qp) {
        CriteriaBuilder cb;
        try {
            cb = queryParamParser.parseCriteria(qp);
        } catch (ClassNotFoundException e) {
            throw new SimplifiedException("Can not find Entity Class[" + qp.getCls() + "]");
        }
        return find(cb);
    }

    /**
     * 根据hql查询
     */
    public List<T> find(String hql, Object... args) {
        Session sess = getSession();
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
     * @return 结果数据
     */
    public List<T> findAll() {
        Session sess = getSession();
        Criteria ct = DetachedCriteria.forClass(this.entityClazz).getExecutableCriteria(sess);
        ct.setCacheMode(CacheMode.NORMAL);
        //noinspection unchecked
        return ct.list();
    }

    /**
     * 通过唯一属性查询
     * <p>如果不是唯一属性，则返回第一个满足条件的实体</p>
     */
    public T findOne(CriteriaBuilder cb) {
        DetachedCriteria dc = cb.buildDeCriteria(false);
        Session sess = getSession();
        Criteria ct = dc.getExecutableCriteria(sess);
        ct.setFirstResult(0);
        ct.setMaxResults(1);
        @SuppressWarnings("unchecked")
        List<T> list = ct.list();
        if (list.size() < 1)
            return null;
        return list.get(0);
    }

    /**
     * 通过属性查询
     * <p>如果不是唯一属性，则返回第一个满足条件的实体</p>
     *
     * @param cts 条件数组
     */
    public T findOne(Criterion... cts) {
        return findOne(CriteriaBuilder.forClass(entityClazz).addCriterion(cts));
    }

    /**
     * 通过属性查询
     * <p>如果不是唯一属性，则返回第一个满足条件的实体</p>
     *
     * @param prop  属性名称
     * @param value 值
     */
    public T findOne(String prop, Object value) {
        return findOne(Restrictions.eq(prop, value));
    }

    /**
     * 查询且锁定
     */
    public T findOneWithLock(String prop, Object value) {
        Session sess = getSession();
        DetachedCriteria dc = DetachedCriteria.forClass(this.entityClazz);
        dc.add(Restrictions.eq(prop, value));
        Criteria ct = dc.getExecutableCriteria(sess);
        ct.setCacheable(true);
        ct.setFirstResult(0);
        ct.setMaxResults(1);

        @SuppressWarnings("unchecked")
        List<T> list = ct.list();
        T t;
        if (list.size() < 1)
            throw new SQLException(SQLException.RESULT_NOT_FOUND);
        t = list.get(0);
        sess.buildLockRequest(LockOptions.UPGRADE).lock(t);
        return t;
    }

    public Map<String, Object> findOneAsMapBySql(String sqlId, Map<String, ?> paramMap) {
        return sqlManager.findOneAsMap(sqlId, paramMap);
    }

    public T findOneBySql(String sqlId, Map<String, ?> paramMap) {
        return sqlManager.findOne(sqlId, entityClazz, paramMap);
    }

    /**
     * 判断是否有存在已有实体
     * <p>
     *
     * @param id 指定ID
     * @return 是/否
     */
    public boolean exist(PK id) {
        if (id == null) {
            return false;
        }
        Long c = count(CriteriaBuilder.forClass(entityClazz).eq("id", id));
        return c > 0;
    }

    /**
     * 判断是否有存在已有实体 默认多个字段条件为，条件并列and
     * <p>
     * * @param params 查询参数
     *
     * @return 是/否
     */
    public boolean exist(Map<String, Object> params) {
        return exist(params, null, false);
    }

    /**
     * 判断是否有存在已有实体 默认多个字段条件为，条件并列and
     * <p>
     * * @param params 查询参数
     *
     * @param notId 忽略匹配的Id
     * @return 是/否
     */
    public boolean exist(Map<String, Object> params, PK notId) {
        return exist(params, notId, true);
    }

    /**
     * 判断是否有存在已有实体
     *
     * @param params 查询参数
     * @param notId  忽略匹配的Id
     * @param isAnd  使用and / or 连接条件
     * @return 是/否
     */
    public boolean exist(Map<String, Object> params, PK notId, boolean isAnd) {
        DetachedCriteria dc = DetachedCriteria.forClass(this.entityClazz);
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
        Session sess = getSession();
        // 总数查询
        Criteria ct = dc.getExecutableCriteria(sess);
        Long total = (Long) ct.setProjection(Projections.rowCount()).uniqueResult();
        return total > 0;
    }

    /**
     * 判断是否有存在已有实体
     */
    public boolean exist(CriteriaBuilder cb, PK notId) {
        if (notId != null)
            cb.notEq("id", notId);
        Long c = count(cb);
        return c > 0;
    }

    public Long count(CriteriaBuilder cb) {
        Session sess = getSession();
        Criteria ct = cb.buildDeCriteria(false).getExecutableCriteria(sess);
        ct.setCacheable(false);
        return (Long) ct.setProjection(Projections.rowCount()).uniqueResult();
    }

    public Long count(String sqlId, Map<String, ?> paramMap) {
        return sqlManager.count(sqlId, paramMap);
    }

    /**
     * 根据条件查询DTO列表
     */
    public Page<T> page(CriteriaBuilder cb) {
        Assert.notNull(cb, "CriteriaBuilder need a non-null value");
        Session sess = getSession();
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
        Long total = (Long) ct.setProjection(Projections.rowCount()).uniqueResult();
        List<T> res = BeanUtils.wrapperMapToBeanList(this.entityClazz, list);
        return new Page<>(res, total, cb.getPageRequest() == null ? total.intValue() : cb.getPageRequest().getPageSize());
    }

    /**
     * 根据条件查询DTO列表
     *
     * @param qp 通用查询参数
     */
    public Page<T> page(QueryParam qp) {
        CriteriaBuilder cb;
        try {
            cb = queryParamParser.parseCriteria(qp);
        } catch (ClassNotFoundException e) {
            throw new SimplifiedException("Can not find Entity Class[" + qp.getCls() + "]");
        }
        return page(cb);
    }

    /**
     * 根据条件查询DTO（HashMap）
     */
    public Page<Map<String, Object>> pageMap(CriteriaBuilder cb) {
        // 总数查询
        Criteria ct = cb.buildDeCriteria(true).getExecutableCriteria(getSession());
        ct.setCacheable(false);

        ct.setResultTransformer(CriteriaSpecification.ALIAS_TO_ENTITY_MAP);
        if (null != cb.getPageRequest()) {
            ct.setFirstResult(cb.getPageRequest().getOffset());
            ct.setMaxResults(cb.getPageRequest().getPageSize());
        }

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> list = ct.list();

        ct.setFirstResult(0);
        ct.setMaxResults(MAX_RECORDS);
        Long total = (Long) ct.setProjection(Projections.rowCount()).uniqueResult();
        // 关联对象，填充映射对象
        list = list.stream().map(BeanUtils::wrapperFlatMap).collect(Collectors.toList());
        return new Page<>(list, total, null == cb.getPageRequest() ? total.intValue() : cb.getPageRequest().getPageSize());
    }

    /**
     * 根据条件查询DTO（HashMap）
     *
     * @param qp 通用查询参数
     */
    public Page<Map<String, Object>> pageMap(QueryParam qp) {
        CriteriaBuilder cb;
        try {
            cb = queryParamParser.parseCriteria(qp);
        } catch (ClassNotFoundException e) {
            throw new SimplifiedException("Can not find Entity Class[" + qp.getCls() + "]");
        }
        return pageMap(cb);
    }

    /**
     * 根据条件查询DTO列表
     */
    public List<T> list(Map<String, Object> map) {
        CriteriaBuilder cb = CriteriaBuilder.forClass(entityClazz)
            .addCriterion(map.entrySet().stream().map(entry -> Restrictions.eq(entry.getKey(), entry.getValue())).collect(Collectors.toList()));
        return list(cb);
    }

    /**
     * 根据条件查询DTO列表
     */
    public List<T> list(CriteriaBuilder cb) {
        List<Map<String, Object>> list = listMap(cb);
        return BeanUtils.wrapperMapToBeanList(this.entityClazz, list);
    }

    /**
     * 根据条件查询DTO列表
     */
    public List<T> list(QueryParam qp) {
        CriteriaBuilder cb;
        try {
            cb = queryParamParser.parseCriteria(qp);
        } catch (ClassNotFoundException e) {
            throw new SimplifiedException("Can not find Entity Class[" + qp.getCls() + "]");
        }
        return list(cb);
    }

    /**
     * 根据条件查询DTO（HashMap）
     */
    public List<Map<String, Object>> listMap(CriteriaBuilder cb) {
        Assert.notNull(cb, "CriteriaBuilder need a non-null value");
        Session sess = getSession();
        Criteria ct = cb.buildDeCriteria(true).getExecutableCriteria(sess);
        ct.setCacheable(false);
        ct.setResultTransformer(CriteriaSpecification.ALIAS_TO_ENTITY_MAP);
        if (cb.getPageRequest() != null) {
            ct.setFirstResult(cb.getPageRequest().getOffset());
            ct.setMaxResults(cb.getPageRequest().getPageSize());
        }
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> list = ct.list();
        return list;
    }

    /**
     * 根据条件查询DTO（HashMap）
     */
    public List<Map<String, Object>> listMap(QueryParam qp) {
        CriteriaBuilder cb;
        try {
            cb = queryParamParser.parseCriteria(qp);
        } catch (ClassNotFoundException e) {
            throw new SimplifiedException("Can not find Entity Class[" + qp.getCls() + "]");
        }
        return listMap(cb);
    }

    /* ---BEGING---***********************委托SQLManager执行SQL语句**************************** */
    public List<T> listBySql(String sqlId, Object... mapParams) {
        return sqlManager.list(sqlId, entityClazz, mapParams);
    }

    public List<T> listBySql(String sqlId, Map<String, ?> paramMap) {
        return sqlManager.list(sqlId, entityClazz, paramMap);
    }

    public List<Map<String, Object>> listMapBySql(String sqlId, Object... mapParams) {
        return sqlManager.listAsMap(sqlId, mapParams);
    }

    public List<Map<String, Object>> listMapBySql(String sqlId, Map<String, ?> paramMap) {
        return sqlManager.listAsMap(sqlId, paramMap);
    }

    public Page<T> pageBySql(String sqlId, QueryParam qp) {
        return sqlManager.listAsPage(sqlId, entityClazz, qp);
    }

    public Page<Map<String, Object>> pageMapBySql(String sqlId, QueryParam qp) {
        return sqlManager.listAsPageMap(sqlId, qp);
    }

    public int executeCUD(String sqlId, Map<String, ?> paramMap) {
        return sqlManager.executeCUD(sqlId, paramMap);
    }

    public void batchExec(String sqlId, List<Map<String, ?>> argsMap) {
        sqlManager.batchExec(sqlId, argsMap);
    }
    /* ---END---***********************委托SQLManager执行SQL语句******************************* */

    /**
     * Flush all pending saves, updates and deletes to the database.
     * <p>Only invoke this for selective eager flushing, for example when
     * JDBC code needs to see certain changes within the same transaction.
     * Else, it is preferable to rely on auto-flushing at transaction
     * completion.
     *
     * @see Session#flush
     */
    public void flush() {
        getSession().flush();
    }

    /**
     * Remove all objects from the {@link Session} cache, and
     * cancel all pending saves, updates and deletes.
     *
     * @see Session#clear
     */
    public void clear() {
        getSession().clear();
    }

    public Class<T> getEntityClazz() {
        return entityClazz;
    }

    public void setEntityClazz(Class<T> entityClazz) {
        this.entityClazz = entityClazz;
    }

    /**
     * Return whether to check that the Hibernate Session is not in read-only
     * mode in case of write operations (save/update/delete).
     */
    public boolean isCheckWriteOperations() {
        return checkWriteOperations;
    }

    /**
     * Set whether to check that the Hibernate Session is not in read-only mode
     * in case of write operations (save/update/delete).
     * <p>Default is "true", for fail-fast behavior when attempting write operations
     * within a read-only transaction. Turn this off to allow save/update/delete
     * on a Session with flush mode MANUAL.
     *
     * @see #checkWriteOperationAllowed
     * @see org.springframework.transaction.TransactionDefinition#isReadOnly
     */
    public void setCheckWriteOperations(boolean checkWriteOperations) {
        this.checkWriteOperations = checkWriteOperations;
    }

    public SessionFactory getSessFactory() {
        return sessFactory;
    }

    protected void addProjections(final ProjectionList plist, Class enCls) {
        ReflectionUtils.doWithFields(enCls, f -> {
            if (f.getAnnotation(Transient.class) == null) {
                logger.info(f.getName());
                plist.add(Property.forName(f.getName()), f.getName());
            }
        });
    }

    protected ProjectionList getPropertyProjection(String pkf) {
        ProjectionList pj = Projections.projectionList();
        pj.add(Property.forName(pkf), pkf);
        return pj;
    }

    /**
     * Check whether write operations are allowed on the given Session.
     * <p>Default implementation throws an InvalidDataAccessApiUsageException in
     * case of {@code FlushMode.MANUAL}. Can be overridden in subclasses.
     *
     * @param session current Hibernate Session
     * @see #setCheckWriteOperations
     * @see Session#getFlushMode()
     * @see FlushMode#MANUAL
     */
    protected void checkWriteOperationAllowed(Session session) {
        Method getFlushMode;
        try {
            // Hibernate 5.2+ getHibernateFlushMode()
            getFlushMode = Session.class.getMethod("getHibernateFlushMode");
        } catch (NoSuchMethodException ex) {
            try {
                // Hibernate 5.0/5.1 getFlushMode() with FlushMode return type
                getFlushMode = Session.class.getMethod("getFlushMode");
            } catch (NoSuchMethodException ex2) {
                throw new IllegalStateException("No compatible Hibernate getFlushMode signature found", ex2);
            }
        }
        if (isCheckWriteOperations() && ((FlushMode) ReflectionUtils.invokeMethod(getFlushMode, session)).lessThan(FlushMode.COMMIT)) {
            throw new InvalidDataAccessApiUsageException(
                "Write operations are not allowed in read-only mode (FlushMode.MANUAL): " +
                    "Turn your Session into FlushMode.COMMIT/AUTO or remove 'readOnly' marker from transaction definition.");
        }
    }

    private Session peekThreadSession() {
        Deque<Session> sessQueue = THREADLOCAL_SESSIONS.get();
        if (sessQueue != null && !sessQueue.isEmpty()) {
            return sessQueue.peek();
        }
        return null;
    }

    private void pushTreadSession(Session session) {
        Deque<Session> sessQueue = THREADLOCAL_SESSIONS.get();
        if (sessQueue == null) {
            sessQueue = new ArrayDeque<>();
            THREADLOCAL_SESSIONS.set(sessQueue);
        }
        sessQueue.push(session);
    }

    private Session popTreadSession() {
        Session session = null;
        Deque<Session> sessQueue = THREADLOCAL_SESSIONS.get();
        if (sessQueue != null && !sessQueue.isEmpty()) {
            session = sessQueue.pop();
        }
        if (sessQueue == null || sessQueue.isEmpty()) {
            THREADLOCAL_SESSIONS.remove();
            logger.info("remove THREADLOCAL_SESSIONS");
        }
        return session;
    }
}
