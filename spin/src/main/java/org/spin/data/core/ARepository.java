package org.spin.data.core;

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
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.jdbc.ReturningWork;
import org.hibernate.jdbc.Work;
import org.hibernate.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spin.core.Assert;
import org.spin.core.ErrorCode;
import org.spin.core.session.SessionManager;
import org.spin.core.session.SessionUser;
import org.spin.core.throwable.SQLException;
import org.spin.core.throwable.SimplifiedException;
import org.spin.core.util.BeanUtils;
import org.spin.core.util.ReflectionUtils;
import org.spin.core.util.StringUtils;
import org.spin.data.pk.generator.IdGenerator;
import org.spin.data.query.CriteriaBuilder;
import org.spin.data.query.QueryParam;
import org.spin.data.query.QueryParamParser;
import org.spin.data.sql.SQLManager;
import org.spin.data.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.hibernate5.HibernateOptimisticLockingFailureException;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.sql.PreparedStatement;
import java.time.LocalDateTime;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;


/**
 * 通用数据访问层代码
 * <p>所有的Dao均继承此类。支持：
 * <pre>
 * 1、基于Jpa规范的Repository
 * 2、基于JdbcTemplate和NamedJdbcTemplate的动态SQL查询
 * </pre>
 * <p>Created by xuweinan on 2016/10/5.</p>
 *
 * @author xuweinan
 * @version V1.4
 */
public class ARepository<T extends IEntity<PK>, PK extends Serializable> {
    private static final Logger logger = LoggerFactory.getLogger(ARepository.class);
    private static final String ID_MUST_NOT_BE_NULL = "The given id must not be null!";
    private static final int MAX_RECORDS = 100000000;
    private static final ThreadLocal<Deque<Session>> THREADLOCAL_SESSIONS = new ThreadLocal<Deque<Session>>() {
    };

    private boolean checkWriteOperations = true;

    @Autowired
    private QueryParamParser queryParamParser;

    @Autowired(required = false)
    protected SessionFactory sessFactory;

    @Autowired
    protected SQLManager sqlManager;

    @Autowired(required = false)
    protected IdGenerator<PK, ?> idGenerator;

    protected Class<T> entityClazz;

    public ARepository() {
        //noinspection unchecked
        this.entityClazz = (Class<T>) ReflectionUtils.getSuperClassGenricType(this.getClass());
    }

    public ARepository(Class<T> entityClass) {
        this.entityClazz = entityClass;
    }

    /**
     * 获得当前线程的session 如果Thread Local变量中有绑定，返回该session
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
     * 打开一个新Session，如果线程上有其他Session，则返回最后一个Session
     */
    public Session openSession() {
        return openSession(false);
    }

    /**
     * 在当前线程上手动打开一个Session，其他的Thread local事务可能会失效
     *
     * @param requiredNew 强制打开新Session
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
     * 关闭当前线程上手动开启的所有Session
     */
    public void closeAllManualSession() {
        while (!THREADLOCAL_SESSIONS.get().isEmpty()) {
            closeManualSession();
        }
    }

    /**
     * 关闭当前线程上手动打开的最后一个Session，如果Session上有事务，提交之
     */
    public void closeManualSession() {
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
     * 在当前线程上打开一个Session，并启动事务
     *
     * @param requiredNew 强制开启事务
     */
    public Transaction openTransaction(boolean requiredNew) {
        Session session = openSession(requiredNew);
        Transaction tran = session.getTransaction() == null ? session.beginTransaction() : session.getTransaction();
        tran.begin();
        return tran;
    }

    /**
     * 保存指定实体
     *
     * @param entity 需要保存的实体
     * @return 保存后的实体(has id)
     */
    public T save(final T entity) {
        return save(entity, false);
    }

    /**
     * 保存指定实体
     *
     * @param entity     需要保存的实体
     * @param saveWithPk 用指定的ID执行insert
     * @return 保存后的实体(has id)
     */
    public T save(final T entity, boolean saveWithPk) {
        Assert.notNull(entity, "The entity to save MUST NOT be NULL");
        if (entity instanceof AbstractEntity) {
            AbstractEntity aEn = (AbstractEntity) entity;
            SessionUser user = SessionManager.getCurrentUser();
            aEn.setUpdateTime(LocalDateTime.now());
            aEn.setUpdateUserId(user == null ? null : user.getId());
            aEn.setUpdateUserName(user == null ? null : user.getUserName());
            if (null == aEn.getId() || saveWithPk) {
                aEn.setCreateTime(LocalDateTime.now());
                aEn.setCreateUserId(user == null ? null : user.getId());
                aEn.setCreateUserName(user == null ? null : user.getUserName());
            }
        }
        try {
            if (null == entity.getId() || saveWithPk) {
                if (null != idGenerator && null == entity.getId()) {
                    entity.setId(idGenerator.genId());
                }
                getSession().save(entity);
            } else {
                getSession().update(entity);
            }
        } catch (HibernateOptimisticLockingFailureException ope) {
            throw new SimplifiedException("The entity is expired", ope);
        }
        return get(entity.getId());
    }

    /**
     * 保存指定的实体
     *
     * @param entities 需要保存的实体集合
     * @return 保存后的实体
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

    /**
     * 将指定实体的属性复制到相同Id的持久化对象中。
     * <p>如果Session中没有该Id的持久化对象，该Id的实体将先被加载，最后返回该持久化对象。</p>
     * <p>如果指定的实体是新记录，将保存该实体的副本到数据库，并将该副本作为持久化对象返回。</p>
     * <p>指定的对象不会与当前Session关联。</p>
     * <p>如果对象存在关联属性映射为{@code cascade="merge"}，该方法会级联处理对象属性</p>
     * 该方法的语义由 JSR-220定义.
     *
     * @param entity 待合并的瞬态实例
     * @return 更新后的持久化对象
     */
    public T merge(final T entity) {
        //noinspection unchecked
        return (T) getSession().merge(entity);
    }

    /**
     * 用指定的复制机制持久化指定瞬态实体
     *
     * @param entity          待复制的实体
     * @param replicationMode Hibernate ReplicationMode
     * @see Session#replicate(Object, ReplicationMode)
     */
    public void replicate(final T entity, final ReplicationMode replicationMode) {
        checkWriteOperationAllowed(getSession());
        getSession().replicate(entity, replicationMode);
    }

    /**
     * 返回指定Id的持久化对象，或者{@code null}(如果Id不存在)
     * <p>该方法是{@link Session#get(Class, Serializable)}的浅封装</p>
     *
     * @param id 主键值
     * @return 持久化对象, 或 {@code null}
     * @see Session#get(Class, Serializable)
     */
    public T get(final PK id) {

        if (null == id) {
            return null;
        }

        return getSession().get(this.entityClazz, id);
    }

    /**
     * 返回指定Id的持久化对象，或者{@code null}(如果Id不存在)
     * <p>如果实体存在，将获取指定的锁</p>
     * <p>该方法是{@link Session#get(Class, Serializable, LockMode)}的浅封装</p>
     *
     * @param id       主键值
     * @param lockMode 锁定模式
     * @return 持久化对象, 或 {@code null}
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
     * 返回指定Id的持久化对象，如果Id不存在，抛出异常
     *
     * @param id 主键值
     * @return 持久化对象
     * @throws org.springframework.orm.ObjectRetrievalFailureException 如果id不存在则抛出该异常
     * @see Session#load(Class, Serializable)
     */
    public T load(final PK id) {
        return getSession().load(this.entityClazz, id);
    }

    /**
     * 返回指定Id的持久化对象，如果Id不存在，抛出异常
     * <p>如果实体存在，将获取指定的锁</p>
     *
     * @param id       主键值
     * @param lockMode 锁定模式
     * @return 持久化对象
     * @throws org.springframework.orm.ObjectRetrievalFailureException 如果id不存在则抛出该异常
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
        return EntityUtils.getDTO(get(k), depth);
    }

    /**
     * 刷新指定持久化对象的状态
     *
     * @param entity 待刷新的持久化对象
     * @see Session#refresh(Object)
     */
    public void refresh(final T entity) {
        refresh(entity, null);
    }

    /**
     * 刷新指定持久化对象的状态，并获取该实体上的锁
     *
     * @param entity   待刷新的持久化对象
     * @param lockMode 需要获取的锁
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
     * 检查Session缓存中是否存在指定的持久化对象
     *
     * @param entity 待检查的持久化对象
     * @see Session#contains
     */
    public boolean contains(final T entity) {
        return getSession().contains(entity);
    }

    /**
     * 删除指定实体
     *
     * @throws IllegalArgumentException 当待删除的实体为{@literal null}时抛出该异常
     */
    public void delete(T entity) {
        getSession().delete(Assert.notNull(entity, "The entity to be deleted is null"));
    }

    /**
     * 通过ID删除指定实体
     *
     * @throws IllegalArgumentException 当待删除的{@code id}为{@literal null}时抛出该异常
     */
    public void delete(PK k) {
        T entity = get(Assert.notNull(k, ID_MUST_NOT_BE_NULL));
        getSession().delete(Assert.notNull(entity, "Entity not found, or was deleted: [" + this.entityClazz.getSimpleName() + "|" + k + "]"));
        getSession().flush();
    }

    /**
     * 通过ID集合删除指定实体
     *
     * @throws IllegalArgumentException 当待删除的{@code ids}为{@literal null}时抛出该异常
     */
    public void delete(Iterator<PK> ids) {
        Assert.notNull(ids, ID_MUST_NOT_BE_NULL);
        ids.forEachRemaining(this::delete);
    }

    /**
     * 删除指定实体
     *
     * @throws IllegalArgumentException 当待删除的{@link Iterable}为{@literal null}时抛出该异常
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
        if (StringUtils.isEmpty(conditions))
            hql.append("where ").append(conditions);
        getSession().delete(hql);
    }

    /**
     * 逻辑删除指定实体
     *
     * @throws IllegalArgumentException 当待删除的实体为{@literal null}时抛出该异常
     */
    public void logicDelete(T entity) {
        Assert.notNull(entity, "The entity to be deleted is null");
        if (entity instanceof AbstractEntity) {
            ((AbstractEntity) entity).setValid(false);
            merge(entity);
            evict(entity);
        }
    }

    /**
     * 通过ID逻辑删除指定实体
     *
     * @throws IllegalArgumentException 当待删除的{@code id}为{@literal null}时抛出该异常
     */
    public void logicDelete(PK k) {
        T entity = get(Assert.notNull(k, ID_MUST_NOT_BE_NULL));
        Assert.notNull(entity, "Entity not found, or was deleted: [" + this.entityClazz.getSimpleName() + "|" + k + "]");
        if (entity instanceof AbstractEntity) {
            ((AbstractEntity) entity).setValid(false);
            merge(entity);
            evict(entity);
        }
    }

    /**
     * 通过ID集合逻辑删除指定实体
     *
     * @throws IllegalArgumentException 当待删除的{@code ids}为{@literal null}时抛出该异常
     */
    public void logicDelete(Iterator<PK> ids) {
        Assert.notNull(ids, ID_MUST_NOT_BE_NULL);
        ids.forEachRemaining(this::logicDelete);
    }

    /**
     * 逻辑删除指定实体
     *
     * @throws IllegalArgumentException 当待删除的{@link Iterable}为{@literal null}时抛出该异常
     */
    public void logicDelete(Iterable<? extends T> entities) {
        Assert.notNull(entities, "The given Iterable of entities not be null!");
        for (T entity : entities) {
            logicDelete(entity);
        }
    }

    /**
     * 批量逻辑删除实体
     * <p>如果条件为空，删除所有</p>
     */
    public void logicDelete(Criterion... cs) {
        DetachedCriteria dc = DetachedCriteria.forClass(this.entityClazz);
        for (Criterion c : cs)
            dc.add(c);

        List<T> enList = find(dc);
        enList.forEach(this::logicDelete);
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
        if (!entityClazz.equals(cb.getEnCls())) {
            cb.setEnCls(entityClazz);
        }
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
            if (!entityClazz.equals(cb.getEnCls())) {
                cb.setEnCls(entityClazz);
            }
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
        if (!entityClazz.equals(cb.getEnCls())) {
            cb.setEnCls(entityClazz);
        }
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

    /**
     * 判断是否有存在已有实体
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
     *
     * @param params 查询参数
     * @return 是/否
     */
    public boolean exist(Map<String, Object> params) {
        return exist(params, null, true);
    }

    /**
     * 判断是否有存在已有实体 默认多个字段条件为，条件并列and
     *
     * @param params 查询参数
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
     * 判断是否存在已有实体
     */
    public boolean exist(CriteriaBuilder cb, PK notId) {
        if (!entityClazz.equals(cb.getEnCls())) {
            cb.setEnCls(entityClazz);
        }
        if (notId != null)
            cb.notEq("id", notId);
        Long c = count(cb);
        return c > 0;
    }

    public Long count(CriteriaBuilder cb) {
        Session sess = getSession();
        if (!entityClazz.equals(cb.getEnCls())) {
            cb.setEnCls(entityClazz);
        }
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
        if (!entityClazz.equals(cb.getEnCls())) {
            cb.setEnCls(entityClazz);
        }
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
        List<T> res = EntityUtils.wrapperMapToBeanList(this.entityClazz, list);
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
            if (!entityClazz.equals(cb.getEnCls())) {
                cb.setEnCls(entityClazz);
            }
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
            if (!entityClazz.equals(cb.getEnCls())) {
                cb.setEnCls(entityClazz);
            }
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
        if (!entityClazz.equals(cb.getEnCls())) {
            cb.setEnCls(entityClazz);
        }
        List<Map<String, Object>> list = listMap(cb);
        return EntityUtils.wrapperMapToBeanList(this.entityClazz, list);
    }

    /**
     * 根据条件查询DTO列表
     */
    public List<T> list(QueryParam qp) {
        CriteriaBuilder cb;
        try {
            cb = queryParamParser.parseCriteria(qp);
            if (!entityClazz.equals(cb.getEnCls())) {
                cb.setEnCls(entityClazz);
            }
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
        if (!entityClazz.equals(cb.getEnCls())) {
            cb.setEnCls(entityClazz);
        }
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
            if (!entityClazz.equals(cb.getEnCls())) {
                cb.setEnCls(entityClazz);
            }
        } catch (ClassNotFoundException e) {
            throw new SimplifiedException("Can not find Entity Class[" + qp.getCls() + "]");
        }
        return listMap(cb);
    }

    /* ---BEGING---***********************委托SQLManager执行SQL语句**************************** */
    public Map<String, Object> findOneAsMapBySql(String sqlId, Map<String, ?> paramMap) {
        return sqlManager.findOneAsMap(sqlId, paramMap);
    }

    public T findOneBySql(String sqlId, Map<String, ?> paramMap) {
        return sqlManager.findOne(sqlId, entityClazz, paramMap);
    }

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

    public Page<T> pageBySql(String sqlId, Map<String, ?> paramMap, PageRequest pageRequest) {
        return sqlManager.listAsPage(sqlId, entityClazz, paramMap, pageRequest);
    }

    public Page<Map<String, Object>> pageMapBySql(String sqlId, Map<String, ?> paramMap, PageRequest pageRequest) {
        return sqlManager.listAsPageMap(sqlId, paramMap, pageRequest);
    }

    public int executeCUD(String sqlId, Map<String, ?> paramMap) {
        return sqlManager.executeCUD(sqlId, paramMap);
    }

    public void batchExec(String sqlId, List<Map<String, ?>> argsMap) {
        sqlManager.batchExec(sqlId, argsMap);
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
        getSession().flush();
    }

    /**
     * 将当前实体从Session缓存中剔除。对实体的改动将会被丢弃，不会同步到数据库中。如果实体的关联属性映射为
     * {@code cascade="evict"}，该操作将会级联剔除所有的关联实体
     *
     * @param entity 需要剔除的实体
     */
    public void evict(T entity) {
        if (Objects.nonNull(entity)) {
            getSession().evict(entity);
        }
    }

    /**
     * 从Session缓存中移除所有持久化对象，并取消所有已挂起的保存，更新和删除操作
     *
     * @see Session#clear
     */
    public void clear() {
        getSession().clear();
    }

    /**
     * 通过使用指定的jdbc连接执行用户自定义任务
     *
     * @param work 需要执行的任务
     */
    public void doWork(Work work) {
        getSession().doWork(work);
    }

    /**
     * 通过使用指定的jdbc连接执行CUD操作
     *
     * @param sql 需要执行的CUD语句
     * @return 受影响行数
     */
    public int doWork(String sql) {
        Assert.notEmpty(sql, "SQL语句不能为空");
        Assert.isTrue(!StringUtils.trimToEmpty(sql).toLowerCase().startsWith("select"), "不能执行select语句，只能执行CUD语句");
        int[] affects = {-1};
        getSession().doWork(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql);
            affects[0] = ps.executeUpdate();
        });
        return affects[0];
    }

    /**
     * 通过使用指定的jdbc连接执行用户自定义任务，可以获取执行厚度返回结果{@link ReturningWork#execute} call.
     *
     * @param work 需要执行的任务
     * @return 执行结果 {@link ReturningWork#execute}.
     */
    T doReturningWork(ReturningWork<T> work) {
        return getSession().doReturningWork(work);
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

    public void setSessFactory(SessionFactory sessFactory) {
        this.sessFactory = sessFactory;
    }

    public SQLManager getSqlManager() {
        return sqlManager;
    }

    public void setSqlManager(SQLManager sqlManager) {
        this.sqlManager = sqlManager;
    }

    public IdGenerator<PK, ?> getIdGenerator() {
        return idGenerator;
    }

    public void setIdGenerator(IdGenerator<PK, ?> idGenerator) {
        this.idGenerator = idGenerator;
    }

    /**
     * 检查当前Session是否允许write操作
     * <p>当{@code FlushMode.MANUAL}时，SQLException，可以通过子类重写该行为。
     *
     * @param session hibernate Session
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
            throw new SQLException(ErrorCode.OTHER.getCode(),
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

    private static Session popTreadSession() {
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
