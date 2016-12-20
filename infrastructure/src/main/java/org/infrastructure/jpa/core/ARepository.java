package org.infrastructure.jpa.core;

import org.hibernate.*;
import org.hibernate.criterion.CriteriaSpecification;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.ProjectionList;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Property;
import org.hibernate.criterion.Restrictions;
import org.hibernate.query.Query;
import org.hibernate.sql.JoinType;
import org.infrastructure.jpa.query.DetachedCriteriaBag;
import org.infrastructure.jpa.query.QueryParam;
import org.infrastructure.jpa.query.QueryParamParser;
import org.infrastructure.jpa.sql.SQLManager;
import org.infrastructure.sys.Assert;
import org.infrastructure.sys.EnvCache;
import org.infrastructure.sys.ErrorAndExceptionCode;
import org.infrastructure.sys.SessionUser;
import org.infrastructure.throwable.SQLException;
import org.infrastructure.throwable.SimplifiedException;
import org.infrastructure.util.BeanUtils;
import org.infrastructure.util.CollectionUtils;
import org.infrastructure.util.EntityUtils;
import org.infrastructure.util.ReflectionUtils;
import org.infrastructure.util.SessionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.data.domain.PageRequest;
import org.springframework.orm.hibernate5.HibernateOptimisticLockingFailureException;
import org.springframework.stereotype.Component;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Temporal;
import javax.persistence.Transient;
import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.sql.Timestamp;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
 * @version V1.2
 */
@Component
public class ARepository<T extends IEntity<PK>, PK extends Serializable> {
    private static final Logger logger = LoggerFactory.getLogger(ARepository.class);
    private static final String ID_MUST_NOT_BE_NULL = "The given id must not be null!";
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
        return this.openSession(false);
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
        return this.openTransaction(false);
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
        Assert.notNull(entity, "The entity to save must be a NON-NULL object");
        if (entity instanceof AbstractEntity) {
            AbstractEntity aEn = (AbstractEntity) entity;
            SessionUser user = SessionUtils.getCurrentUser();
            if (null == user)
                user = AbstractUser.ref(1L);
            aEn.setUpdateTime(new Timestamp(System.currentTimeMillis()));
            aEn.setUpdateUser(AbstractUser.ref(user.getId()));
            if (null == aEn.getId()) {
                aEn.setCreateUser(AbstractUser.ref(user.getId()));
                aEn.setCreateTime(new Timestamp(System.currentTimeMillis()));
            }
        }
        try {
            if (null == entity.getId())
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
     * @see org.hibernate.Session#replicate(Object, ReplicationMode)
     */
    public void replicate(final T entity, final ReplicationMode replicationMode) {
        checkWriteOperationAllowed(getSession());
        getSession().replicate(entity, replicationMode);
    }

    /**
     * Return the persistent instance of the actual entity class
     * with the given identifier, or {@code null} if not found.
     * <p>This method is a thin wrapper around
     * {@link org.hibernate.Session#get(Class, Serializable)} for convenience.
     * For an explanation of the exact semantics of this method, please do refer to
     * the Hibernate API documentation in the first instance.
     *
     * @param id the identifier of the persistent instance
     * @return the persistent instance, or {@code null} if not found
     * @throws DataAccessException in case of Hibernate errors
     * @see org.hibernate.Session#get(Class, Serializable)
     */
    public T get(final PK id) {
        return getSession().get(this.entityClazz, id);
    }

    /**
     * Return the persistent instance of the given identifier, or {@code null} if not found.
     * <p>Obtains the specified lock mode if the instance exists.
     * <p>This method is a thin wrapper around
     * {@link org.hibernate.Session#get(Class, Serializable, LockMode)} for convenience.
     * For an explanation of the exact semantics of this method, please do refer to
     * the Hibernate API documentation in the first instance.
     *
     * @param id       the identifier of the persistent instance
     * @param lockMode the lock mode to obtain
     * @return the persistent instance, or {@code null} if not found
     * @see org.hibernate.Session#get(Class, Serializable, LockMode)
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
     * {@link org.hibernate.Session#load(Class, Serializable)} for convenience.
     * For an explanation of the exact semantics of this method, please do refer to
     * the Hibernate API documentation in the first instance.
     *
     * @param id the identifier of the persistent instance
     * @return the persistent instance
     * @throws org.springframework.orm.ObjectRetrievalFailureException if not found
     * @throws DataAccessException                                     in case of Hibernate errors
     * @see org.hibernate.Session#load(Class, Serializable)
     */
    public T load(final PK id) {
        return getSession().load(this.entityClazz, id);
    }

    /**
     * Return the persistent instance of the given entity class
     * with the given identifier, throwing an exception if not found.
     * Obtains the specified lock mode if the instance exists.
     * <p>This method is a thin wrapper around
     * {@link org.hibernate.Session#load(Class, Serializable, LockMode)} for convenience.
     * For an explanation of the exact semantics of this method, please do refer to
     * the Hibernate API documentation in the first instance.
     *
     * @param id       the identifier of the persistent instance
     * @param lockMode the lock mode to obtain
     * @return the persistent instance
     * @throws org.springframework.orm.ObjectRetrievalFailureException if not found
     * @throws DataAccessException                                     in case of Hibernate errors
     * @see org.hibernate.Session#load(Class, Serializable)
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
     * @see org.hibernate.Session#refresh(Object)
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
     * @see org.hibernate.Session#refresh(Object, LockMode)
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
     * @see org.hibernate.Session#contains
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

        List<T> enList = this.find(dc);
        enList.forEach(this::delete);
    }

    /**
     * 批量删除实体
     * <p>如果条件为空，删除所有</p>
     */
    public void delete(String conditions) {
        StringBuilder hql = new StringBuilder("from ");
        hql.append(this.entityClazz.getSimpleName()).append(" ");
        if (conditions.length() > 0)
            hql.append("where ").append(conditions);
        getSession().delete(hql);
    }

    public Page<Map<String, Object>> findByFields(List<String> fields, DetachedCriteriaBag dr) {
        List<String> fieldsList = fields;
        if (CollectionUtils.isEmpty(fields))
            fieldsList = parseEntityColumns(dr.getEnCls());
        Map<String, Field> entityJoinFields = getJoinFields(dr.getEnCls());
        Map<String, Set<String>> referFields = new HashMap<>();
        Set<String> queryjoinFields = new HashSet<>();

        // 使用投影映射字段
        ProjectionList projectionList = Projections.projectionList();

        for (String pf : fieldsList) {
            // 如果是对象投影，不在查询中体现，后期通过对象Id来初始化
            if (entityJoinFields.containsKey(pf)) {
                referFields.put(pf, new HashSet<>());
                continue;
            }

            projectionList.add(Property.forName(pf), dr.getAliasMap().containsKey(pf) ? dr.getAliasMap().get(pf) : pf);

            int pjFieldPtIdx = pf.indexOf('.');
            if (pjFieldPtIdx > -1) {
                String objField = pf.split("\\.")[0];
                queryjoinFields.add(objField);
                if (pf.lastIndexOf('.') == pjFieldPtIdx && referFields.containsKey(objField))
                    referFields.get(objField).add(pf);
            }
        }

        // 总数查询
        Criteria ct = dr.getDeCriteria().getExecutableCriteria(getSession());
        ct.setCacheable(false);

        // 查询结果中需外连接的表
        queryjoinFields.addAll(referFields.keySet());
        queryjoinFields.stream().filter(jf -> !dr.getAliasMap().containsKey(jf)).forEach(jf -> ct.createAlias(jf, jf, JoinType.LEFT_OUTER_JOIN));

        // 关联对象，只抓取Id值
        for (String referField : referFields.keySet()) {
            Field mapField = entityJoinFields.get(referField);
            String pkf = EntityUtils.getPKField(mapField.getType()).getName();
            String fetchF = referField + "." + pkf;
            referFields.get(referField).add(fetchF);
            projectionList.add(Property.forName(fetchF), fetchF);
        }
        ct.setProjection(projectionList);
        ct.setResultTransformer(CriteriaSpecification.ALIAS_TO_ENTITY_MAP);
        if (null != dr.getPageRequest()) {
            ct.setFirstResult(dr.getPageRequest().getOffset());
            ct.setMaxResults(dr.getPageRequest().getPageSize());
        }

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> list = ct.list();

        // 关联对象，填充映射对象
        for (Map<String, Object> m : list) {
            for (String referField : referFields.keySet()) {
                referFields.get(referField).stream().filter(fetchField -> m.get(fetchField) != null).forEach(fetchField -> {
                    if (!m.containsKey(referField)) {
                        m.put(referField, new HashMap<>());
                    }
                    @SuppressWarnings("unchecked")
                    HashMap<String, Object> cell = (HashMap<String, Object>) m.get(referField);
                    String[] fetchFields = fetchField.split("\\.");
                    cell.put(fetchFields[1], m.get(fetchField));
                });
            }
        }

        Long total = (Long) ct.setProjection(Projections.rowCount()).uniqueResult();
        return new Page<>(list, total, null == dr.getPageRequest() ? total.intValue() : dr.getPageRequest().getPageSize());
    }

    /**
     * 基于QueryParam的投影查询
     *
     * @param qp 通用查询参数
     */
    public Page<Map<String, Object>> findByFields(QueryParam qp) {
        DetachedCriteriaBag dr;
        try {
            dr = queryParamParser.parseDetachedCriteria(qp);
        } catch (ClassNotFoundException e) {
            throw new SimplifiedException("Can not find Entity Class[" + qp.getCls() + "]");
        }
        return this.findByFields(qp.getFields(), dr);
    }

    /**
     * 分页条件查询，有性能问题
     *
     * @param dc 离线条件
     * @param pr 分页请求
     * @return Page列表
     */
    public Page<T> find(DetachedCriteria dc, PageRequest pr) {
        Session sess = getSession();
        Criteria ct = dc.getExecutableCriteria(sess);
        ct.setCacheable(true);
        ct.setCacheMode(CacheMode.NORMAL);
        Long total = (Long) ct.setProjection(Projections.rowCount()).uniqueResult();

        ct.setProjection(null);
        ct.setResultTransformer(CriteriaSpecification.ROOT_ENTITY);

        // 自动追加join策略
        if (EnvCache.REFER_JOIN_FIELDS.containsKey(this.entityClazz.getName())) {
            for (Field field : EnvCache.REFER_JOIN_FIELDS.get(this.entityClazz.getName()).values()) {
                ct.setFetchMode(field.getName(), FetchMode.JOIN);
            }
        }
        ct.setFirstResult(pr.getOffset());
        ct.setMaxResults(pr.getPageSize());
        @SuppressWarnings("unchecked")
        List<T> list = ct.list();
        return new Page<>(list, total, pr.getPageSize());
    }

    /**
     * 条件查询
     *
     * @param dc 离线条件
     * @return 结果数据
     */
    public List<T> find(DetachedCriteria dc) {
        Session sess = getSession();
        Criteria ct = dc.getExecutableCriteria(sess);
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
            dc.add(c);
        return find(dc);
    }

    /**
     * 通过条件查询
     */
    public List<T> find(CriteriaParam cp) {
        DetachedCriteria dc = DetachedCriteria.forClass(this.entityClazz);
        cp.criterions.forEach(dc::add);
        cp.orders.forEach(dc::addOrder);
        if (cp.pageRequest != null)
            return find(dc, cp.pageRequest).getData();
        else
            return find(dc);
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
    public T findOne(CriteriaParam cp) {
        DetachedCriteria dc = DetachedCriteria.forClass(this.entityClazz);
        cp.criterions.forEach(dc::add);
        cp.orders.forEach(dc::addOrder);
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
        CriteriaParam cp = new CriteriaParam();
        cp.addCriterion(cts);
        return findOne(cp);
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

    public boolean exist(PK id) {
        if (id == null) {
            return false;
        }
        Long c = this.count(new CriteriaParam().addCriterion(Restrictions.eq("id", id)));
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
     * 是否存在实体
     */
    public boolean exist(CriteriaParam p, PK notId) {
        if (notId != null)
            p.addCriterion(Restrictions.not(Restrictions.eq("id", notId)));
        Long c = this.count(p);
        return c > 0;
    }

    public Long count(CriteriaParam cp) {
        DetachedCriteria dc = DetachedCriteria.forClass(this.entityClazz);
        cp.criterions.forEach(dc::add);
        Session sess = getSession();
        Criteria ct = dc.getExecutableCriteria(sess);
        ct.setCacheable(false);
        return (Long) ct.setProjection(Projections.rowCount()).uniqueResult();
    }

    public Long count(String sqlId, Map<String, ?> paramMap) {
        return sqlManager.count(sqlId, paramMap);
    }

    /**
     * 根据条件查询DTO列表
     */
    public Page<T> page(CriteriaParam cp) {
        Assert.notNull(cp, "CriteriaParam need a non-null value");
        DetachedCriteria dc = DetachedCriteria.forClass(this.entityClazz);
        cp.criterions.forEach(dc::add);
        Session sess = getSession();
        Criteria ct = dc.getExecutableCriteria(sess);
        ct.setCacheable(false);
        Long total = (Long) ct.setProjection(Projections.rowCount()).uniqueResult();
        processProjectionQueryCriteria(cp, ct);
        ct.setResultTransformer(CriteriaSpecification.ALIAS_TO_ENTITY_MAP);
        cp.orders.forEach(ct::addOrder);
        if (cp.pageRequest != null) {
            ct.setFirstResult(cp.pageRequest.getOffset());
            ct.setMaxResults(cp.pageRequest.getPageSize());
        }
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> list = ct.list();
        List<T> enList = new ArrayList<>();
        list.forEach(map -> {
            try {
                enList.add(BeanUtils.wrapperMapToBean(this.entityClazz, map));
            } catch (Exception e) {
                throw new SimplifiedException(ErrorAndExceptionCode.BEAN_CREATE_FAIL, e);
            }
        });
        return new Page<>(enList, total, cp.pageRequest.getPageSize());
    }

    /**
     * 根据条件查询DTO列表
     */
    public List<T> list(Map<String, Object> map) {
        CriteriaParam cp = new CriteriaParam();
        cp.criterions.addAll(map.keySet().stream().map(key -> Restrictions.eq(key, map.get(key))).collect(Collectors.toList()));
        return list(cp);
    }

    /**
     * 根据条件查询DTO列表
     */
    public List<T> list(CriteriaParam cp) {
        Assert.notNull(cp, "CriteriaParam need a non-null value");
        DetachedCriteria dc = DetachedCriteria.forClass(this.entityClazz);
        cp.criterions.forEach(dc::add);

        Session sess = getSession();
        Criteria ct = dc.getExecutableCriteria(sess);
        ct.setCacheable(false);
        cp.orders.forEach(ct::addOrder);
        processProjectionQueryCriteria(cp, ct);
        ct.setResultTransformer(CriteriaSpecification.ALIAS_TO_ENTITY_MAP);
        if (cp.pageRequest != null) {
            ct.setFirstResult(cp.pageRequest.getOffset());
            ct.setMaxResults(cp.pageRequest.getPageSize());
        }
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> list = ct.list();
        List<T> enList = new ArrayList<>();
        /* Map查询后，回填对象 */
        list.forEach(map -> {
            try {
                enList.add(BeanUtils.wrapperMapToBean(this.entityClazz, map));
            } catch (Exception e) {
                throw new SimplifiedException(ErrorAndExceptionCode.BEAN_CREATE_FAIL, e);
            }
        });
        return enList;
    }

    /* ---BEGING---***********************委托SQLManager执行SQL语句**************************** */
    public List<T> listBySql(String sqlId, Object... mapParams) {
        return sqlManager.list(sqlId, entityClazz, mapParams);
    }

    public List<T> listBySql(String sqlId, Map<String, ?> paramMap) {
        return sqlManager.list(sqlId, entityClazz, paramMap);
    }

    public List<Map<String, Object>> listAsMapBySql(String sqlId, Object... mapParams) {
        return sqlManager.listAsMap(sqlId, mapParams);
    }

    public List<Map<String, Object>> listAsMapBySql(String sqlId, Map<String, ?> paramMap) {
        return sqlManager.listAsMap(sqlId, paramMap);
    }

    public Page<Map<String, Object>> listAsPageMap(String sqlId, QueryParam qp) {
        return sqlManager.listAsPageMap(sqlId, qp);
    }

    public Page<T> listAsPage(String sqlId, QueryParam qp) {
        return sqlManager.listAsPage(sqlId, entityClazz, qp);
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
     * @see org.hibernate.Session#flush
     */
    public void flush() {
        getSession().flush();
    }

    /**
     * Remove all objects from the {@link org.hibernate.Session} cache, and
     * cancel all pending saves, updates and deletes.
     *
     * @see org.hibernate.Session#clear
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

    /**
     * 解析实体中所有映射到数据库列的字段
     */
    private List<String> parseEntityColumns(Class entityClazz) {
        if (EnvCache.ENTITY_COLUMNS.containsKey(entityClazz.getName()))
            return EnvCache.ENTITY_COLUMNS.get(entityClazz.getName());

        Field[] fields = entityClazz.getDeclaredFields();
        List<String> columns = Arrays.stream(fields).filter(this::isColumn).map(Field::getName).collect(Collectors.toList());
        Class<?> superClass = entityClazz.getSuperclass();
        if (null != superClass)
            columns.addAll(parseEntityColumns(superClass));
        EnvCache.ENTITY_COLUMNS.put(entityClazz.getName(), columns);
        return columns;
    }

    /**
     * 判断字段是否是映射到数据库
     */
    private boolean isColumn(Field field) {
        Annotation[] annotations = field.getAnnotations();
        for (Annotation annotation : annotations) {
            if (annotation instanceof Column
                    || annotation instanceof Id
                    || annotation instanceof OneToOne
                    || annotation instanceof ManyToOne
                    || annotation instanceof Temporal
                    || annotation instanceof org.hibernate.annotations.Type)
                return true;
        }
        return false;
    }

    /**
     * 获得引用类型的n对一的引用字段列表
     *
     * @return 字段列表
     */
    private Map<String, Field> getJoinFields(final Class cls) {
        String clsName = cls.getName();
        if (!EnvCache.REFER_JOIN_FIELDS.containsKey(clsName)) {
            Map<String, Field> referJoinFields = new HashMap<>();
            ReflectionUtils.doWithFields(cls,
                    f -> referJoinFields.put(f.getName(), f),
                    f -> f.getAnnotation(ManyToOne.class) != null || f.getAnnotation(OneToOne.class) != null);
            EnvCache.REFER_JOIN_FIELDS.put(cls.getName(), referJoinFields);
        }
        return EnvCache.REFER_JOIN_FIELDS.get(clsName);
    }

    /**
     * 根据查询参数生成要查询的投影字段列表并添加到Criteria中，并在Criteria中为所有join属性创建连接查询
     * <p>
     * 投影字段列表, 包括所有非关联属性与所有n对一关联属性的ID与用户指定的属性
     * <p>
     * 如果查询参数的字段列表为空，不做任何操作
     */
    private void processProjectionQueryCriteria(CriteriaParam cp, Criteria ct) {
        if (cp == null || cp.fields.isEmpty())
            return;
        final Set<String> qjoinFields = new HashSet<>();
        final Map<String, Field> someToOneFields = getJoinFields(this.entityClazz);

        final ProjectionList pj = Projections.projectionList();
        // 所有非join字段加入投影字段
        ReflectionUtils.doWithFields(this.entityClazz,
                f -> pj.add(Property.forName(f.getName()), f.getName()),
                f -> f.getAnnotation(ManyToMany.class) == null && f.getAnnotation(OneToMany.class) == null && !someToOneFields.containsKey(f.getName()));

        // 所有用户自定义字段加入投影字段
        cp.fields.stream().filter(pjField -> !someToOneFields.containsKey(pjField)).forEach(pjField -> {
            pj.add(Property.forName(pjField), pjField);
            if (pjField.contains(".")) {
                qjoinFields.add(pjField.split("\\.")[0]);
            }
        });

        // 合并join字段到连接集合中
        qjoinFields.addAll(someToOneFields.keySet());
        // 为所有join字段，创建连接查询
        qjoinFields.forEach(jf -> ct.createAlias(jf, jf, JoinType.LEFT_OUTER_JOIN));

        // 将所有n对一关联属性的主键，加入投影字段
        for (String referField : someToOneFields.keySet()) {
            Field mapField = someToOneFields.get(referField);
            String pkf = EntityUtils.getPKField(mapField.getType()).getName();
            String fetchF = referField + "." + pkf;
            pj.add(Property.forName(fetchF), fetchF);
        }
        ct.setProjection(pj);
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