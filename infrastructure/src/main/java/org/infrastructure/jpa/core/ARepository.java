package org.infrastructure.jpa.core;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.*;
import org.hibernate.cfg.Environment;
import org.hibernate.criterion.*;
import org.hibernate.query.Query;
import org.hibernate.sql.JoinType;
import org.infrastructure.jpa.api.CmdParser.DetachedCriteriaResult;
import org.infrastructure.jpa.sql.SQLManager;
import org.infrastructure.shiro.SessionManager;
import org.infrastructure.shiro.SessionUser;
import org.infrastructure.sys.Assert;
import org.infrastructure.sys.EnvCache;
import org.infrastructure.throwable.SimplifiedException;
import org.infrastructure.util.BeanUtils;
import org.infrastructure.util.ElUtils;
import org.infrastructure.util.ReflectionUtils;
import org.infrastructure.util.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.data.domain.PageRequest;
import org.springframework.orm.hibernate4.LocalSessionFactoryBean;
import org.springframework.orm.hibernate4.LocalSessionFactoryBuilder;
import org.springframework.stereotype.Component;

import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Transient;
import javax.sql.DataSource;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.sql.Timestamp;
import java.util.*;
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
public class ARepository<T extends IEntity<PK>, PK extends Serializable> extends SQLManager<T> {
    private static final Log logger = LogFactory.getLog(ARepository.class);

    private boolean checkWriteOperations = true;

    @Autowired
    protected SessionManager sessionMgr;

    protected LocalSessionFactoryBean sessFactory;

    protected static ThreadLocal<Stack<Session>> THREADLOCAL_SESSIONS = new ThreadLocal<Stack<Session>>() {
    };

    public ARepository() {
        //noinspection unchecked
        this.entityClazz = (Class<T>) ReflectionUtils.getSuperClassGenricType(this.getClass());
    }

    public ARepository(Class<T> entityClass) {
        this.entityClazz = entityClass;
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

    public SessionManager getSessionMgr() {
        return sessionMgr;
    }

    public void setSessionMgr(SessionManager sessionMgr) {
        this.sessionMgr = sessionMgr;
    }

    public LocalSessionFactoryBean getSessFactory() {
        return sessFactory;
    }

    /**
     * 将SessionFactory保存到属性 并根据SessionFactory的数据连接初始化jdbcTemplate的DataSource
     */
    @Autowired
    public void setSessFactory(LocalSessionFactoryBean sessFactory) {
        this.sessFactory = sessFactory;
        LocalSessionFactoryBuilder cfg = (LocalSessionFactoryBuilder) sessFactory.getConfiguration();
        DataSource dataSource = (DataSource) cfg.getProperties().get(Environment.DATASOURCE);
        super.initDataSource(dataSource);
    }

    /**
     * 获得引用类型的*ToOne的引用字段列表
     *
     * @return 字段列表
     */
    public static Map<String, Field> getJoinFields(final Class cls) {
        String clsName = cls.getName();
        if (!EnvCache.REFER_JOIN_FIELDS.containsKey(clsName))
            parseForJoinFetch(cls);
        return EnvCache.REFER_JOIN_FIELDS.get(clsName);
    }

    /**
     * 动态捕获要增加Fetch=Join的字段 默认ToOne的都Fetch=Join
     */
    public static void parseForJoinFetch(final Class cls) {
        EnvCache.REFER_JOIN_FIELDS.put(cls.getName(), new HashMap<>());
        ReflectionUtils.doWithFields(cls,
                f -> EnvCache.REFER_JOIN_FIELDS.get(cls.getName()).put(f.getName(), f),
                f -> f.getAnnotation(ManyToOne.class) != null || f.getAnnotation(OneToOne.class) != null);
    }

    /**
     * 获得当前线程的session 如果线程Local变量中有绑定，返回该session
     * 否则，调用sessFactory的getCurrentSession
     */
    public Session getSession() {
        Session sess = peekThreadSession();
        if (sess == null) {
            sess = sessFactory.getObject().getCurrentSession();
        }
        return sess;
    }

    /**
     * 打开新连接 如果线程已有session就返回 不重复打开
     */
    public Session openSession() {
        return this.openSession(false);
    }

    /**
     * 手动打开线程绑定的session 事务管理会失效
     *
     * @param requiredNew 强制打开新连接
     */
    public Session openSession(boolean requiredNew) {
        Session session = peekThreadSession();
        if (requiredNew || session == null) {
            session = sessFactory.getObject().openSession();
            pushTreadSession(session);
        }
        return session;
    }

    /**
     * 关闭线程中手动打开的session
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
     * 打开线程绑定的session, 并启动事务
     *
     * @param requiredNew 强制启动事务
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
     * @throws IllegalArgumentException in case the given entity is {@literal null}.
     */
    public T save(final T entity) throws IllegalArgumentException {
        if (null == entity)
            throw new IllegalArgumentException("The entity to save must be a NON-NULL object");
        try {
            if (entity instanceof AbstractEntity) {
                AbstractEntity aEn = (AbstractEntity) entity;
                // new Entity
                SessionUser user = sessionMgr.getCurrentUser();
                if (aEn.getId() == null) {
                    String createBy = null;
                    if (StringUtils.isNotEmpty(aEn.getCreateUserName()))
                        createBy = aEn.getCreateUserName();
                    if (createBy == null && user != null)
                        createBy = StringUtils.isNotEmpty(user.getRealName()) ? user.getRealName() : user.getLoginName();
                    aEn.setCreateUserName(createBy);

                    if (aEn.getCreateTime() == null)
                        aEn.setCreateTime(new Timestamp(new Date().getTime()));

                    if (user != null)
                        aEn.setCreateUser(GenericUser.ref(user.getId()));
                }

                // 追加时间戳
                Session sess = getSession();
                if (aEn.getId() == null) {
                    String lastUpdateBy = null;
                    if (StringUtils.isNotEmpty(aEn.getLastUpdateUserName()))
                        lastUpdateBy = aEn.getCreateUserName();
                    if (lastUpdateBy == null && user != null)
                        lastUpdateBy = StringUtils.isNotEmpty(user.getRealName()) ? user.getRealName() : user.getLoginName();
                    aEn.setLastUpdateUserName(lastUpdateBy);

                    if (aEn.getLastUpdateTime() == null)
                        aEn.setLastUpdateTime(new Timestamp(new Date().getTime()));

                    if (user != null)
                        aEn.setCreateUser(GenericUser.ref(user.getId()));
                    sess.save(aEn);
                } else {
                    aEn.setLastUpdateUserName(user == null ? "" : (StringUtils.isNotEmpty(user.getRealName()) ? user.getRealName() : user
                            .getLoginName()));
                    aEn.setLastUpdateTime(new Timestamp(new Date().getTime()));
                    if (user != null)
                        aEn.setLastUpdateUser(GenericUser.ref(user.getId()));
                    sess.update(aEn);
                }
            } else {
                Session sess = getSession();
                if (null == entity.getId())
                    sess.save(entity);
                else
                    sess.update(entity);
            }
        } catch (org.springframework.orm.hibernate4.HibernateOptimisticLockingFailureException ope) {
            logger.error("", ope);
            throw new SimplifiedException("实体已经更新，请重置后再编辑");
        }
        return get(entity.getId());
    }

    /**
     * Saves all given entities.
     *
     * @param entities entities to save
     * @return the saved entities
     * @throws IllegalArgumentException in case the given entity is {@literal null}.
     */
    public List<T> save(Iterable<T> entities) throws IllegalArgumentException {
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
    public void replicate(final T entity, final ReplicationMode replicationMode) throws DataAccessException {
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
        return ElUtils.getDto(get(k), depth);
    }

    /**
     * Re-read the state of the given persistent instance.
     *
     * @param entity the persistent instance to re-read
     * @throws DataAccessException in case of Hibernate errors
     * @see org.hibernate.Session#refresh(Object)
     */
    public void refresh(final T entity) throws DataAccessException {
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
    public void refresh(final T entity, final LockMode lockMode) throws DataAccessException {
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
    public boolean contains(final T entity) throws DataAccessException {
        return getSession().contains(entity);
    }

    /**
     * Remove the given object from the {@link org.hibernate.Session} cache.
     *
     * @param entity the persistent instance to evict
     * @throws DataAccessException in case of Hibernate errors
     * @see org.hibernate.Session#evict
     */
    public void evict(final T entity) throws DataAccessException {
        getSession().evict(entity);
    }

    /**
     * 删除实体(物理删除，不可恢复)
     */
    public void delete(T entity) {
        Assert.notNull(entity, "无法删除引用为null的实体");
        getSession().delete(entity);
    }

    /**
     * 删除实体(物理删除，不可恢复)
     */
    public void delete(PK k) {
        T t = get(k);
        Assert.notNull(t, "未找到实体,或者已经被删除,实体：" + this.entityClazz.getSimpleName() + ",主键:" + k);
        getSession().delete(t);
        getSession().flush();
    }

    /**
     * 批量删除实体(物理删除，不可恢复)
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
     * 批量删除实体(物理删除，不可恢复)
     * <p>如果条件为空，删除所有</p>
     */
    public void delete(String conditions) {
        StringBuilder hql = new StringBuilder("from ");
        hql.append(this.entityClazz.getSimpleName()).append(" ");
        if (conditions.length() > 0)
            hql.append("where ").append(conditions);
        getSession().delete(hql);
    }

    @SuppressWarnings("unchecked")
    public Page<Map> findByFields(List<String> fields, DetachedCriteriaResult dr, PageRequest pr, Order... orders) {
        Set<String> qjoinFields = new HashSet<>();
        Map<String, Field> enJoinFields = getJoinFields(this.entityClazz);
        Map<String, Set<String>> referFields = new HashMap<>();

        // 使用投影映射字段
        ProjectionList pj = Projections.projectionList();
        for (String pf : fields) {
            // 如果是对象投影，不在查询中体现，后期通过对象Id来初始化
            if (enJoinFields.containsKey(pf)) {
                referFields.put(pf, new HashSet<>());
                continue;
            }

            pj.add(Property.forName(pf), pf);
            int pjFieldPtIdx = pf.indexOf(".");
            if (pjFieldPtIdx > -1) {
                qjoinFields.add(pf.split("\\.")[0]);
            }
        }

        //将投影对象字典收集为对象
        for (String pf : fields) {
            int pjFieldPtIdx = pf.indexOf(".");
            if (pjFieldPtIdx > -1 && pf.lastIndexOf(".") == pjFieldPtIdx) {
                String objField = pf.split("\\.")[0];
                if (referFields.containsKey(objField)) {
                    referFields.get(objField).add(pf);
                }
            }
        }

        Page<Map> page;
        List<Map> list;
        Session sess = getSession();
        DetachedCriteria dc = dr.dc;
        // 总数查询
        Criteria ct = dc.getExecutableCriteria(sess);
        ct.setCacheable(false);
        Long total = (Long) ct.setProjection(Projections.rowCount()).uniqueResult();

        // 查询结果中需外连接的表
        qjoinFields.addAll(referFields.keySet());
        qjoinFields.stream().filter(jf -> !dr.aliasMap.containsKey(jf)).forEach(jf -> ct.createAlias(jf, jf, JoinType.LEFT_OUTER_JOIN));

        // 关联对象，只抓取Id值
        for (String referField : referFields.keySet()) {
            Field mapField = enJoinFields.get(referField);
            String pkf = ElUtils.getPKField(mapField.getType()).getName();
            String fetchF = referField + "." + pkf;
            referFields.get(referField).add(fetchF);
            pj.add(Property.forName(fetchF), fetchF);
        }

        ct.setProjection(pj);
        ct.setResultTransformer(org.hibernate.criterion.CriteriaSpecification.ALIAS_TO_ENTITY_MAP);

        if (orders != null) {
            for (Order order : orders)
                ct.addOrder(order);
        }

        ct.setFirstResult(pr.getOffset());
        ct.setMaxResults(pr.getPageSize());
        list = ct.list();

        // 关联对象，填充映射对象
        for (Map m : list) {
            for (String referField : referFields.keySet()) {
                referFields.get(referField).stream().filter(fetchField -> m.get(fetchField) != null).forEach(fetchField -> {
                    if (!m.containsKey(referField)) {
                        m.put(referField, new HashMap<String, Object>());
                    }
                    HashMap<String, Object> cell = ((HashMap<String, Object>) m.get(referField));
                    String[] fetchFields = fetchField.split("\\.");
                    cell.put(fetchFields[1], m.get(fetchField));
                });
            }
        }

        page = new Page<>(list, total);
        return page;
    }

    /**
     * 分页查询，有性能问题
     *
     * @param dc 离线条件
     * @param pr 分页请求
     * @return Page列表
     */
    @SuppressWarnings("unchecked")
    public Page<T> find(DetachedCriteria dc, PageRequest pr, Order... orders) {
        Page<T> page;
        List<T> list;
        Session sess = getSession();

        // 总数查询
        Criteria ct = dc.getExecutableCriteria(sess);
        ct.setCacheable(true);
        ct.setCacheMode(CacheMode.NORMAL);
        Long total = (Long) ct.setProjection(Projections.rowCount()).uniqueResult();

        // 分页查询
        ct.setProjection(null);
        ct.setResultTransformer(org.hibernate.criterion.CriteriaSpecification.ROOT_ENTITY);

        // 自动追加join策略
        if (EnvCache.REFER_JOIN_FIELDS.containsKey(this.entityClazz.getName())) {
            for (Field field : EnvCache.REFER_JOIN_FIELDS.get(this.entityClazz.getName()).values()) {
                ct.setFetchMode(field.getName(), FetchMode.JOIN);
            }
        }

        if (orders != null) {
            for (Order order : orders)
                ct.addOrder(order);
        }

        ct.setFirstResult(pr.getOffset());
        ct.setMaxResults(pr.getPageSize());
        list = ct.list();

        page = new Page<>(list, total);
        return page;
    }

    /**
     * 无分页的列表查询，最多1000条记录 （会产生性能问题）
     *
     * @param dc 离线条件
     * @return 结果数据
     */
    public List<T> find(DetachedCriteria dc) {
        Session sess = getSession();
        Criteria ct = dc.getExecutableCriteria(sess);
        ct.setFirstResult(0);
        ct.setMaxResults(10000);
        ct.setCacheMode(CacheMode.NORMAL);
        @SuppressWarnings("unchecked")
        List<T> list = ct.list();
        return list;
    }

    /**
     * 通过条件查询
     */
    public List<T> find(Criterion... cs) {
        DetachedCriteria dc = DetachedCriteria.forClass(this.entityClazz);
        for (Criterion c : cs)
            dc.add(c);

        return this.find(dc);
    }

    /**
     * 通过条件查询，并排序
     *
     * @param cs     条件组合
     * @param orders 排序
     */
    public List<T> find(Criterion[] cs, Order... orders) {
        DetachedCriteria dc = DetachedCriteria.forClass(this.entityClazz);
        for (Criterion c : cs)
            dc.add(c);
        for (Order order : orders)
            dc.addOrder(order);
        return this.find(dc);
    }

    /**
     * 分页的全记录查询，最多10000条记录
     *
     * @return 结果数据
     */
    public List<T> findAll() {
        Session sess = getSession();
        Criteria ct = DetachedCriteria.forClass(this.entityClazz).getExecutableCriteria(sess);
        ct.setFirstResult(0);
        ct.setMaxResults(10000);
        ct.setCacheMode(CacheMode.NORMAL);
        @SuppressWarnings("unchecked")
        List<T> list = ct.list();
        return list;
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
     * 通过唯一属性查询
     */
    public T findOne(CriteriaParam cp) {
        DetachedCriteria dc = DetachedCriteria.forClass(this.entityClazz);
        cp.criterions.forEach(dc::add);

        // 追加排序
        cp.orders.forEach(dc::addOrder);

        String pkf = ElUtils.getPKField(this.entityClazz).getName();

        Session sess = getSession();
        Criteria ct = dc.getExecutableCriteria(sess);

        final Map<String, Set<String>> referFields = new HashMap<>();
        beforeQueryCriteria(cp, ct, referFields);

        ct.setCacheable(false);
        ct.setProjection(getPropertyProjection(pkf));
        ct.setResultTransformer(org.hibernate.criterion.CriteriaSpecification.ALIAS_TO_ENTITY_MAP);
        ct.setFirstResult(0);
        ct.setMaxResults(1);

        @SuppressWarnings("unchecked")
        List<Map> list = ct.list();
        @SuppressWarnings("unchecked")
        PK pk = list.size() == 1 ? (PK) list.get(0).get(pkf) : null;
        return pk == null ? null : this.get(pk);
    }

    /**
     * 通过唯一属性查询
     *
     * @param cts 条件数组
     */
    public T findOne(Criterion... cts) {
        CriteriaParam cp = new CriteriaParam();
        cp.addCriterion(cts);
        return findOne(cp);
    }

    /**
     * 通过唯一属性查询
     *
     * @param prop  唯一属性名称
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
        T t = null;
        if (list.size() == 1) {
            t = list.get(0);
            sess.buildLockRequest(LockOptions.UPGRADE).lock(t);
        }
        return t;
    }

    public boolean exist(PK id) {
        if (id == null) {
            return false;
        }
        Long c = this.count(new CriteriaParam().addCriterion(Restrictions.eq("id", id)));
        return c > 0;
    }

    /**
     * 判断是否有存在已有实体 默认多个字段条件为，or匹配
     *
     * @param props  属性数组
     * @param values 对应的只数据组
     * @param notId  忽略匹配的Id
     * @return 是/否
     */
    public boolean exist(String[] props, Object[] values, PK notId) {
        return exist(props, values, notId, false);
    }

    /**
     * 判断是否有存在已有实体
     *
     * @param props  属性数组
     * @param values 对应的只数据组
     * @param notId  忽略匹配的Id
     * @param isAnd  使用and / or 连接条件
     * @return 是/否
     */
    public boolean exist(String[] props, Object[] values, PK notId, boolean isAnd) {
        DetachedCriteria dc = DetachedCriteria.forClass(this.entityClazz);

        List<Criterion> criteriaList = new ArrayList<>();
        for (int i = 0; i < props.length; i++) {
            if (values[i] != null && StringUtils.isNotEmpty(values[i].toString())) {
                criteriaList.add(Restrictions.eq(props[i], values[i]));
            }
        }

        if (criteriaList.size() == 0) {
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
        if (notId != null) {
            p.addCriterion(Restrictions.not(Restrictions.eq("id", notId)));
        }
        Long c = this.count(p);
        return c > 0;
    }

    /**
     * 统计数量
     */
    public Long count(CriteriaParam cp) {
        DetachedCriteria dc = DetachedCriteria.forClass(this.entityClazz);
        cp.criterions.forEach(dc::add);

        Session sess = getSession();
        Criteria ct = dc.getExecutableCriteria(sess);
        ct.setCacheable(false);
        // 总数查询
        return (Long) ct.setProjection(Projections.rowCount()).uniqueResult();
    }

    /**
     * 查询实体，返回dto的列表 （已避免性能问题）
     */
    public Page<T> page(CriteriaParam cp) {

        DetachedCriteria dc = DetachedCriteria.forClass(this.entityClazz);
        cp.criterions.forEach(dc::add);

        Session sess = getSession();
        Criteria ct = dc.getExecutableCriteria(sess);
        ct.setCacheable(false);

        // 查询列表数据
        final Map<String, Set<String>> referFields = new HashMap<>();

        final ProjectionList pj = beforeQueryCriteria(cp, ct, referFields);

        // 总数查询
        Long total = (Long) ct.setProjection(Projections.rowCount()).uniqueResult();

        // 申明查询字段
        ct.setProjection(pj);
        ct.setResultTransformer(org.hibernate.criterion.CriteriaSpecification.ALIAS_TO_ENTITY_MAP);

        // 追加排序
        cp.orders.forEach(ct::addOrder);

        if (cp.pageRequest != null) {
            ct.setFirstResult(cp.pageRequest.getOffset());
            ct.setMaxResults(cp.pageRequest.getPageSize());
        }

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> list = ct.list();

        ArrayList<T> enList = new ArrayList<>();
        for (Map<String, Object> map : list) {
            try {
                enList.add(BeanUtils.wrapperMapToBean(this.entityClazz, map));
            } catch (Exception ignored) {
            }
        }

        Page<T> page = new Page<>();
        page.data = enList;
        page.total = total;
        return page;
    }

    /**
     * 根据条件查询列表
     */
    public List<T> findByArgs(Map<String, Object> map) {
        CriteriaParam cp = new CriteriaParam();
        cp.criterions.addAll(map.keySet().stream().map(key -> Restrictions.eq(key, map.get(key))).collect(Collectors.toList()));
        return list(cp);
    }

    /**
     * 根据条件查询列表 与 findByArgs逻辑一致
     */
    public List<T> list(Map<String, Object> map) {
        return findByArgs(map);
    }

    /**
     * 查找对象列表，返回Dto对象
     */
    public List<T> list(CriteriaParam cp) {
        return findList(cp);
    }

    /**
     * Flush all pending saves, updates and deletes to the database.
     * <p>Only invoke this for selective eager flushing, for example when
     * JDBC code needs to see certain changes within the same transaction.
     * Else, it is preferable to rely on auto-flushing at transaction
     * completion.
     *
     * @throws DataAccessException in case of Hibernate errors
     * @see org.hibernate.Session#flush
     */
    public void flush() throws DataAccessException {
        getSession().flush();
    }

    /**
     * Remove all objects from the {@link org.hibernate.Session} cache, and
     * cancel all pending saves, updates and deletes.
     *
     * @throws DataAccessException in case of Hibernate errors
     * @see org.hibernate.Session#clear
     */
    public void clear() throws DataAccessException {
        getSession().clear();
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
     * @throws InvalidDataAccessApiUsageException if write operations are not allowed
     * @see #setCheckWriteOperations
     * @see Session#getFlushMode()
     * @see FlushMode#MANUAL
     */
    protected void checkWriteOperationAllowed(Session session) throws InvalidDataAccessApiUsageException {
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
     * 查询参数
     */
    private List<T> findList(CriteriaParam cp) {
        DetachedCriteria dc = DetachedCriteria.forClass(this.entityClazz);
        cp.criterions.forEach(dc::add);

        Session sess = getSession();
        Criteria ct = dc.getExecutableCriteria(sess);
        ct.setCacheable(false);

        // 查询列表数据
        final Map<String, Set<String>> referFields;
        referFields = new HashMap<>();
        final ProjectionList pj = beforeQueryCriteria(cp, ct, referFields);

        ct.setProjection(pj);
        ct.setResultTransformer(org.hibernate.criterion.CriteriaSpecification.ALIAS_TO_ENTITY_MAP);

        cp.orders.forEach(ct::addOrder);

        if (cp.pageRequest != null) {
            ct.setFirstResult(cp.pageRequest.getOffset());
            ct.setMaxResults(cp.pageRequest.getPageSize());
        }
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> list = ct.list();

        ArrayList<T> enList = new ArrayList<>();
        /* Map查询后，回填对象 */
        for (Map<String, Object> map : list) {
            try {
                enList.add(BeanUtils.wrapperMapToBean(this.entityClazz, map));
            } catch (Exception ignored) {
            }
        }
        return enList;
    }

    private ProjectionList beforeQueryCriteria(CriteriaParam cp, Criteria ct, final Map<String, Set<String>> referFields) {
        final Set<String> qjoinFields = new HashSet<>();
        final Map<String, Field> manyToOneFields = getJoinFields(this.entityClazz);

        // 使用投影映射字段,先查询，再反射
        final ProjectionList pj = Projections.projectionList();
        ReflectionUtils.doWithFields(this.entityClazz, f -> {
            ManyToMany m2m = f.getAnnotation(ManyToMany.class);
            if (m2m != null) {
                // TODO: 2016/8/13
                System.out.println("");
            } else {
                String pjField = f.getName();
                addProjectionFields(qjoinFields, manyToOneFields, referFields, pj, pjField);
            }
        });

        for (String pjField : cp.fields) {
            addProjectionFields(qjoinFields, manyToOneFields, referFields, pj, pjField);
        }

        // 查询结果中需外连接的表
        qjoinFields.addAll(referFields.keySet());
        for (String jf : qjoinFields)
            ct.createAlias(jf, jf, JoinType.LEFT_OUTER_JOIN);

        // 关联对象，默认值抓取id
        for (String referField : referFields.keySet()) {
            Field mapField = manyToOneFields.get(referField);
            String pkf = ElUtils.getPKField(mapField.getType()).getName();
            String fetchF = referField + "." + pkf;
            referFields.get(referField).add(fetchF);
            pj.add(Property.forName(fetchF), fetchF);
        }
        return pj;
    }

    private void addProjectionFields(final Set<String> qjoinFields, final Map<String, Field> manyToOneFields,
                                     final Map<String, Set<String>> referFields, final ProjectionList pj, String pjField) {
        // 如果是对象投影，不在查询中体现，后期通过对象Id来初始化
        if (manyToOneFields.containsKey(pjField) && !referFields.containsKey(pjField)) {
            referFields.put(pjField, new HashSet<>());
        } else {
            pj.add(Property.forName(pjField), pjField);
            if (pjField.contains(".")) {
                qjoinFields.add(pjField.split("\\.")[0]);
            }
        }
    }

    private Session peekThreadSession() {
        Stack<Session> sessStack = THREADLOCAL_SESSIONS.get();
        if (sessStack != null && !sessStack.empty()) {
            return sessStack.peek();
        }
        return null;
    }

    private void pushTreadSession(Session session) {
        Stack<Session> sessStack = THREADLOCAL_SESSIONS.get();
        if (sessStack == null) {
            sessStack = new Stack<>();
            THREADLOCAL_SESSIONS.set(sessStack);
        }
        sessStack.push(session);
    }

    private Session popTreadSession() {
        Session session = null;
        Stack<Session> sessStack = THREADLOCAL_SESSIONS.get();
        if (sessStack != null && !sessStack.empty()) {
            session = sessStack.pop();
        }
        if (sessStack == null || sessStack.empty()) {
            THREADLOCAL_SESSIONS.remove();
            logger.info("remove THREADLOCAL_SESSIONS");
        }
        return session;
    }
}