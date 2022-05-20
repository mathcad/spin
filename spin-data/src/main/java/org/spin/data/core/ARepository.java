package org.spin.data.core;

import org.hibernate.CacheMode;
import org.hibernate.Criteria;
import org.hibernate.FlushMode;
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
import org.spin.core.session.SessionUser;
import org.spin.core.throwable.AssertFailException;
import org.spin.core.throwable.SimplifiedException;
import org.spin.core.util.BeanUtils;
import org.spin.core.util.LambdaUtils;
import org.spin.core.util.ReflectionUtils;
import org.spin.core.util.StringUtils;
import org.spin.data.pk.generator.IdGenerator;
import org.spin.data.query.CriteriaBuilder;
import org.spin.data.query.QueryParam;
import org.spin.data.query.QueryParamParser;
import org.spin.data.sql.SQLManager;
import org.spin.data.throwable.SQLError;
import org.spin.data.throwable.SQLException;
import org.spin.data.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.OptimisticLockingFailureException;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;


/**
 * 通用数据访问层代码
 * <p>所有自定义Dao均应从此类继承。支持：
 * <pre>
 * 1、基于Jpa规范的Repository
 * 2、基于{@link SQLManager}的动态SQL查询
 * 3、基于JTA的多数据源分布式事务
 * 4、线程安全，Session与线程绑定
 * 5、支持动态切换Schema
 * </pre>
 * 重要提示：
 * <ul>
 * <li><strong>手动开启的Session，Transaction需要在恰当的时机手动关闭，避免泄露</strong></li>
 * <li><strong>手动创建的Statement，ResultSet需要在恰当的时机手动关闭，避免游标超出数据库允许的最大值错误</strong></li>
 * </ul>
 * <p>Created by xuweinan on 2016/10/5.</p>
 *
 * @author xuweinan
 * @version V1.6
 */
public class ARepository<T extends IEntity<PK, T>, PK extends Serializable> {
    private static final String ID_MUST_NOT_BE_NULL = "The given id must not be null!";
    private static final String ORDER_ENTRIES = "orderEntries";
    private static final int MAX_RECORDS = 100000000;

    private boolean checkWriteOperations = true;

    @Autowired
    private QueryParamParser queryParamParser;

    @Autowired
    protected SQLManager sqlManager;

    @Autowired(required = false)
    protected IdGenerator<PK, ?> idGenerator;

    protected Class<T> entityClazz;

    public ARepository() {
        //noinspection unchecked
        this.entityClazz = (Class<T>) ReflectionUtils.getSuperClassGenericType(this.getClass());
    }

    public ARepository(Class<T> entityClass) {
        this.entityClazz = entityClass;
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
            AbstractEntity<?> aEn = (AbstractEntity<?>) entity;
            SessionUser<Long> user = SessionUser.getCurrent();
            aEn.setUpdateTime(LocalDateTime.now());
            aEn.setUpdateBy(user == null ? null : user.getId());
            aEn.setUpdateUsername(user == null ? null : user.getName());
            if (null == aEn.getId() || saveWithPk) {
                aEn.setCreateTime(LocalDateTime.now());
                aEn.setCreateBy(user == null ? null : user.getId());
                aEn.setCreateUsername(user == null ? null : user.getName());
            }
        }
        try {
            if (null == entity.id() || saveWithPk) {
                if (null != idGenerator && null == entity.id()) {
                    entity.id(idGenerator.genId());
                }
                DataSourceContext.getSession().save(entity);
            } else {
                DataSourceContext.getSession().update(entity);
            }
        } catch (OptimisticLockingFailureException ope) {
            throw new SimplifiedException("The entity is expired", ope);
        }
        return get(entity.id());
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
        return (T) DataSourceContext.getSession().merge(entity);
    }

    /**
     * 根据ID更新实体的指定字段，当字段列表为空时，更新所有字段
     *
     * @param entity 待更新的实体
     * @param fields 需要更新的字段列表, 为空时与save操作结果相同
     * @return 更新的行数
     */
    @SafeVarargs
    public final int updateById(T entity, org.spin.core.function.serializable.Function<T, ?>... fields) {
        if (null == entity.id()) {
            throw new SQLException(SQLError.ID_NOT_FOUND, "The Id field must be nonnull when execute update by Id");
        }
        if (null == fields || 0 == fields.length) {
            return null == save(entity) ? 0 : 1;
        }
        StringBuilder sb = new StringBuilder();
        sb.append("UPDATE ").append(entity.getClass().getSimpleName()).append(" t SET ");
        for (org.spin.core.function.serializable.Function<T, ?> field : fields) {
            String fieldName = BeanUtils.toFieldName(LambdaUtils.resolveLambda(field).getImplMethodName());
            sb.append("t.").append(fieldName).append(" = :").append(fieldName).append(",");
        }
        sb.setLength(sb.length() - 1);
        sb.append(" WHERE id = :id");
        Query<?> query = DataSourceContext.getSession().createQuery(sb.toString());
        query.setProperties(entity);
        return query.executeUpdate();
    }

    /**
     * 用指定的复制机制持久化指定瞬态实体
     *
     * @param entity          待复制的实体
     * @param replicationMode Hibernate ReplicationMode
     * @see Session#replicate(Object, ReplicationMode)
     */
    public void replicate(final T entity, final ReplicationMode replicationMode) {
        checkWriteOperationAllowed(DataSourceContext.getSession());
        DataSourceContext.getSession().replicate(entity, replicationMode);
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

        return DataSourceContext.getSession().get(this.entityClazz, id);
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
            return DataSourceContext.getSession().get(this.entityClazz, id, new LockOptions(lockMode));
        } else {
            return DataSourceContext.getSession().get(this.entityClazz, id);
        }
    }

    /**
     * 获取持久态实体对象并锁定(FOR UPDATE悲观锁)
     *
     * @param k 主键
     * @return 锁定后的实体
     */
    public T getWithLock(final PK k) {
        Session sess = DataSourceContext.getSession();
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
        return DataSourceContext.getSession().load(this.entityClazz, id);
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
            return DataSourceContext.getSession().load(entityClazz, id, new LockOptions(lockMode));
        } else {
            return DataSourceContext.getSession().load(entityClazz, id);
        }
    }

    /**
     * 主键获取指定深度的属性的瞬态对象
     *
     * @param k     主键
     * @param depth 深度
     * @return DTO对象
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
            DataSourceContext.getSession().refresh(entity, new LockOptions(lockMode));
        } else {
            DataSourceContext.getSession().refresh(entity);
        }
    }

    /**
     * 检查Session缓存中是否存在指定的持久化对象
     *
     * @param entity 待检查的持久化对象
     * @return 是否存在于缓存中
     * @see Session#contains
     */
    public boolean contains(final T entity) {
        return DataSourceContext.getSession().contains(entity);
    }

    /**
     * 删除指定实体
     *
     * @param entity 待删除实体
     * @throws AssertFailException 当待删除的实体为{@literal null}时抛出该异常
     */
    public void delete(T entity) {
        DataSourceContext.getSession().delete(Assert.notNull(entity, "The entity to be deleted is null"));
    }

    /**
     * 通过ID删除指定实体
     *
     * @param k 待删除实体id
     * @throws AssertFailException 当待删除的{@code id}为{@literal null}时抛出该异常
     */
    public void delete(PK k) {
        T entity = get(Assert.notNull(k, ID_MUST_NOT_BE_NULL));
        DataSourceContext.getSession().delete(Assert.notNull(entity, "Entity not found, or was deleted: [" + this.entityClazz.getSimpleName() + "|" + k + "]"));
        DataSourceContext.getSession().flush();
    }

    /**
     * 通过ID集合删除指定实体
     *
     * @param ids 待删除实体主键集合
     * @throws AssertFailException 当待删除的{@code ids}为{@literal null}时抛出该异常
     */
    public void delete(Iterator<PK> ids) {
        Assert.notNull(ids, ID_MUST_NOT_BE_NULL);
        ids.forEachRemaining(this::delete);
    }

    /**
     * 删除指定实体
     *
     * @param entities 待删除实体集合
     * @throws AssertFailException 当待删除的{@link Iterable}为{@literal null}时抛出该异常
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
     *
     * @param cs 删除条件
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
     *
     * @param conditions 删除条件(HQL)
     */
    public void delete(String conditions) {
        StringBuilder hql = new StringBuilder("from ");
        hql.append(this.entityClazz.getSimpleName()).append(" ");
        if (StringUtils.isEmpty(conditions)) {
            hql.append("where ").append(conditions);
        }
        DataSourceContext.getSession().delete(hql);
    }

    /**
     * 逻辑删除指定实体
     *
     * @param entity 待删除实体
     * @throws AssertFailException 当待删除的实体为{@literal null}时抛出该异常
     */
    public void logicDelete(T entity) {
        Assert.notNull(entity, "The entity to be deleted is null");
        if (entity instanceof AbstractEntity) {
            entity.setValid(false);
            merge(entity);
        }
    }

    /**
     * 通过ID逻辑删除指定实体
     *
     * @param k 待删除主键
     * @throws AssertFailException 当待删除的{@code id}为{@literal null}时抛出该异常
     */
    public void logicDelete(PK k) {
        T entity = get(Assert.notNull(k, ID_MUST_NOT_BE_NULL));
        Assert.notNull(entity, "Entity not found, or was deleted: [" + this.entityClazz.getSimpleName() + "|" + k + "]");
        if (entity instanceof AbstractEntity) {
            entity.setValid(false);
            merge(entity);
        }
    }

    /**
     * 通过ID集合逻辑删除指定实体
     *
     * @param ids 待删除主键集合
     * @throws AssertFailException 当待删除的{@code ids}为{@literal null}时抛出该异常
     */
    public void logicDelete(Iterator<PK> ids) {
        Assert.notNull(ids, ID_MUST_NOT_BE_NULL);
        ids.forEachRemaining(this::logicDelete);
    }

    /**
     * 逻辑删除指定实体
     *
     * @param entities 实体集合
     * @throws AssertFailException 当待删除的{@link Iterable}为{@literal null}时抛出该异常
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
     *
     * @param cs 删除条件
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
     * @return 查询结果
     */
    public List<T> find(DetachedCriteria dc, PageRequest... pr) {
        Session sess = DataSourceContext.getSession();
        Criteria ct = dc.getExecutableCriteria(sess);
        if (null != pr && pr.length > 0 && null != pr[0]) {
            ct.setFirstResult(pr[0].getOffset());
            ct.setMaxResults(pr[0].getSize());
        }
        ct.setCacheable(true);
        ct.setCacheMode(CacheMode.NORMAL);
        //noinspection unchecked
        return ct.list();
    }

    /**
     * 条件查询
     *
     * @param cs 查询条件
     * @return 查询结果
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
     *
     * @param cb 查询参数
     * @return 查询结果
     */
    public List<T> find(CriteriaBuilder<T> cb) {
        if (!entityClazz.equals(cb.getEnCls())) {
            cb.setEnCls(entityClazz);
        }
        DetachedCriteria detachedCriteria = cb.buildDeCriteria(false);
        return find(detachedCriteria, cb.getPageRequest());
    }

    /**
     * 分页条件查询
     *
     * @param qp 查询参数
     * @return 查询结果
     */
    public List<T> find(QueryParam qp) {
        return find(compileCondition(qp));
    }

    /**
     * 根据hql查询
     *
     * @param hql  查询语句
     * @param args 查询参数
     * @return 查询结果
     */
    public List<T> find(String hql, Object... args) {
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
     * @return 结果数据
     */
    public List<T> findAll() {
        Session sess = DataSourceContext.getSession();
        Criteria ct = DetachedCriteria.forClass(this.entityClazz).getExecutableCriteria(sess);
        ct.setCacheMode(CacheMode.NORMAL);
        //noinspection unchecked
        return ct.list();
    }

    /**
     * 通过唯一属性查询
     * <p>如果结果不唯一或不存在，抛出异常</p>
     *
     * @param cb 查询参数
     * @return 查询到的实体
     */
    public T unique(CriteriaBuilder<T> cb) {
        if (!entityClazz.equals(cb.getEnCls())) {
            cb.setEnCls(entityClazz);
        }
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
     * @param cts 条件数组
     * @return 查询到的实体
     */
    public T unique(Criterion... cts) {
        return unique(CriteriaBuilder.forClass(entityClazz).addCriterion(cts));
    }

    /**
     * 通过属性查询
     * <p>如果结果不唯一或不存在，抛出异常</p>
     *
     * @param prop  属性名称
     * @param value 值
     * @return 查询到的实体
     */
    public T unique(String prop, Object value) {
        return unique(Restrictions.eq(prop, value));
    }

    /**
     * 查询且锁定
     * <p>如果结果不唯一或不存在，抛出异常</p>
     *
     * @param prop  属性名
     * @param value 属性值
     * @return 查询到的实体
     */
    public T uniqueWithLock(String prop, Object value) {
        Session sess = DataSourceContext.getSession();
        DetachedCriteria dc = DetachedCriteria.forClass(this.entityClazz);
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
     * @param cb 查询参数
     * @return 查询到的实体
     */
    public T findOne(CriteriaBuilder<T> cb) {
        if (!entityClazz.equals(cb.getEnCls())) {
            cb.setEnCls(entityClazz);
        }
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
     * @param cts 条件数组
     * @return 查询到的实体
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
     * @return 查询到的实体
     */
    public T findOne(String prop, Object value) {
        return findOne(Restrictions.eq(prop, value));
    }

    /**
     * 查询且锁定
     *
     * @param prop  属性名
     * @param value 属性值
     * @return 查询到的实体
     */
    public T findOneWithLock(String prop, Object value) {
        Session sess = DataSourceContext.getSession();
        DetachedCriteria dc = DetachedCriteria.forClass(this.entityClazz);
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
     * 判断是否存在已有实体
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
     * @param notId  排除的id
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
     * @param notId 排除的id
     * @return 是否存在
     */
    public boolean exist(CriteriaBuilder<T> cb, PK notId) {
        if (!entityClazz.equals(cb.getEnCls())) {
            cb.setEnCls(entityClazz);
        }
        if (notId != null)
            cb.notEq("id", notId);
        Long c = count(cb);
        return c > 0;
    }

    public Long count(CriteriaBuilder<T> cb) {
        Session sess = DataSourceContext.getSession();
        if (!entityClazz.equals(cb.getEnCls())) {
            cb.setEnCls(entityClazz);
        }
        Criteria ct = cb.buildDeCriteria(false).getExecutableCriteria(sess);
        ct.setCacheable(false);
        List<CriteriaImpl.OrderEntry> orderEntries = BeanUtils.getFieldValue(ct, ORDER_ENTRIES);
        orderEntries.clear();
        return (Long) ct.setProjection(Projections.rowCount()).uniqueResult();
    }

    /**
     * 根据条件查询DTO列表
     *
     * @param cb 查询参数
     * @return 查询结果
     */
    public Page<T> page(CriteriaBuilder<T> cb) {
        Assert.notNull(cb, "CriteriaBuilder need a non-null value");
        if (!entityClazz.equals(cb.getEnCls())) {
            cb.setEnCls(entityClazz);
        }
        Session sess = DataSourceContext.getSession();
        Criteria ct = cb.buildDeCriteria(true).getExecutableCriteria(sess);
        ct.setCacheable(false);
        ct.setResultTransformer(CriteriaSpecification.ALIAS_TO_ENTITY_MAP);
        if (cb.getPageRequest() != null) {
            ct.setFirstResult(cb.getPageRequest().getOffset());
            ct.setMaxResults(cb.getPageRequest().getSize());
        }
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> list = ct.list();
        ct.setFirstResult(0);
        ct.setMaxResults(MAX_RECORDS);
        List<CriteriaImpl.OrderEntry> orderEntries = BeanUtils.getFieldValue(ct, ORDER_ENTRIES);
        orderEntries.clear();
        Long total = (Long) ct.setProjection(Projections.rowCount()).uniqueResult();
        List<T> res = BeanUtils.wrapperMapToBeanList(this.entityClazz, list);
        return new Page<>(res, cb.getPageRequest() == null ? 1L : cb.getPageRequest().getCurrent(), total, cb.getPageRequest() == null ? total.intValue() : cb.getPageRequest().getSize());
    }

    /**
     * 根据条件查询DTO列表
     *
     * @param qp 通用查询参数
     * @return 查询结果
     */
    public Page<T> page(QueryParam qp) {
        return page(compileCondition(qp));
    }

    /**
     * 根据条件查询DTO（扁平化的Map）
     *
     * @param cb 查询参数
     * @return 查询结果
     */
    public Page<Map<String, Object>> pageFlatMap(CriteriaBuilder<T> cb) {
        return pageMap(cb, false);
    }

    /**
     * 根据条件查询DTO（扁平化的Map）
     *
     * @param qp 通用查询参数
     * @return 查询结果
     */
    public Page<Map<String, Object>> pageFlatMap(QueryParam qp) {
        return pageFlatMap(compileCondition(qp));
    }

    /**
     * 根据条件查询DTO（层次化的Map）
     *
     * @param cb 查询参数
     * @return 查询结果
     */
    public Page<Map<String, Object>> pageMap(CriteriaBuilder<T> cb) {
        return pageMap(cb, true);
    }

    /**
     * 根据条件查询DTO（层次化的Map）
     *
     * @param qp 通用查询参数
     * @return 查询结果
     */
    public Page<Map<String, Object>> pageMap(QueryParam qp) {
        return pageMap(compileCondition(qp));
    }

    /**
     * 根据条件查询DTO列表
     *
     * @param map 查询参数
     * @return 查询结果
     */
    public List<T> list(Map<String, Object> map) {
        CriteriaBuilder<T> cb = CriteriaBuilder.forClass(entityClazz)
            .addCriterion(map.entrySet().stream().map(entry -> Restrictions.eq(entry.getKey(), entry.getValue())).collect(Collectors.toList()));
        return list(cb);
    }

    /**
     * 根据条件查询DTO列表
     *
     * @param cb 查询参数
     * @return 查询结果
     */
    public List<T> list(CriteriaBuilder<T> cb) {
        if (!entityClazz.equals(cb.getEnCls())) {
            cb.setEnCls(entityClazz);
        }
        List<Map<String, Object>> list = listFlatMap(cb);
        return BeanUtils.wrapperMapToBeanList(this.entityClazz, list);
    }

    /**
     * 根据条件查询DTO列表
     *
     * @param qp 查询参数
     * @return 查询结果
     */
    public List<T> list(QueryParam qp) {
        return list(compileCondition(qp));
    }

    /**
     * 根据条件查询DTO（扁平化的Map）
     *
     * @param cb 查询参数
     * @return 查询结果
     */
    public List<Map<String, Object>> listFlatMap(CriteriaBuilder<T> cb) {
        return listMap(cb, false);
    }

    /**
     * 根据条件查询DTO（扁平化的Map）
     *
     * @param qp 查询参数
     * @return 查询结果
     */
    public List<Map<String, Object>> listFlatMap(QueryParam qp) {
        return listFlatMap(compileCondition(qp));
    }

    /**
     * 根据条件查询DTO（层次化的Map）
     *
     * @param cb 查询参数
     * @return 查询结果
     */
    public List<Map<String, Object>> listMap(CriteriaBuilder<T> cb) {
        return listMap(cb, true);
    }

    /**
     * 根据条件查询DTO（层次化的Map）
     *
     * @param qp 查询参数
     * @return 查询结果
     */
    public List<Map<String, Object>> listMap(QueryParam qp) {
        return listMap(compileCondition(qp));
    }

    /* ---BEGING---***********************委托SQLManager执行SQL语句**************************** */
    public Optional<Map<String, Object>> findOneAsMapBySql(String sqlId, Map<String, ?> paramMap) {
        return doReturningWork(connection -> sqlManager.findOneAsMap(connection, sqlId, paramMap));
    }

    public Optional<T> findOneBySql(String sqlId, Map<String, ?> paramMap) {
        return doReturningWork(connection -> sqlManager.findOne(connection, sqlId, entityClazz, paramMap));
    }

    public List<T> listBySql(String sqlId, Object... mapParams) {
        return doReturningWork(connection -> sqlManager.list(connection, sqlId, entityClazz, mapParams));
    }

    public List<T> listBySql(String sqlId, Map<String, ?> paramMap) {
        return doReturningWork(connection -> sqlManager.list(connection, sqlId, entityClazz, paramMap));
    }

    public List<Map<String, Object>> listMapBySql(String sqlId, Object... mapParams) {
        return doReturningWork(connection -> sqlManager.listAsMap(connection, sqlId, mapParams));
    }

    public List<Map<String, Object>> listMapBySql(String sqlId, Map<String, ?> paramMap) {
        return doReturningWork(connection -> sqlManager.listAsMap(connection, sqlId, paramMap));
    }

    public Page<T> pageBySql(String sqlId, Map<String, ?> paramMap, PageRequest pageRequest) {
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
     */
    public void evict(T entity) {
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

    public Class<T> getEntityClazz() {
        return entityClazz;
    }

    public void setEntityClazz(Class<T> entityClazz) {
        this.entityClazz = entityClazz;
    }

    public void setQueryParamParser(QueryParamParser queryParamParser) {
        this.queryParamParser = queryParamParser;
    }

    /**
     * Return whether to check that the Hibernate Session is not in read-only
     * mode in case of write operations (save/update/delete).
     *
     * @return 是否检查写操作
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
     * @param checkWriteOperations 是否检查写操作
     * @see #checkWriteOperationAllowed
     * @see org.springframework.transaction.TransactionDefinition#isReadOnly
     */
    public void setCheckWriteOperations(boolean checkWriteOperations) {
        this.checkWriteOperations = checkWriteOperations;
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
            throw new SQLException(SQLError.WRITE_NOT_PERMISSION,
                "Write operations are not allowed in read-only mode (FlushMode.MANUAL): " +
                    "Turn your Session into FlushMode.COMMIT/AUTO or remove 'readOnly' marker from transaction definition.");
        }
    }

    @SuppressWarnings("CastCanBeRemovedNarrowingVariableType")
    private CriteriaBuilder<T> compileCondition(QueryParam qp) {
        Assert.notNull(qp, "查询条件参数不能为null");
        CriteriaBuilder<?> cb;
        try {
            cb = queryParamParser.parseCriteria(qp);
        } catch (ClassNotFoundException e) {
            throw new SimplifiedException("Can not find Entity Class[" + qp.getCls() + "]");
        }
        if (!entityClazz.equals(cb.getEnCls())) {
            throw new SimplifiedException("查询参数的实体与Repository中指定的实体类型不一致");
        }
        //noinspection unchecked
        return (CriteriaBuilder<T>) cb;
    }

    /**
     * 根据条件查询DTO（Map）
     *
     * @param cb   查询条件
     * @param wrap 是否需要转换成层次Map
     * @return 查询结果
     */
    public List<Map<String, Object>> listMap(CriteriaBuilder<T> cb, boolean wrap) {
        Assert.notNull(cb, "CriteriaBuilder need a non-null value");
        if (!entityClazz.equals(cb.getEnCls())) {
            cb.setEnCls(entityClazz);
        }
        Session sess = DataSourceContext.getSession();
        Criteria ct = cb.buildDeCriteria(true).getExecutableCriteria(sess);
        ct.setCacheable(false);
        ct.setResultTransformer(CriteriaSpecification.ALIAS_TO_ENTITY_MAP);
        if (cb.getPageRequest() != null) {
            ct.setFirstResult(cb.getPageRequest().getOffset());
            ct.setMaxResults(cb.getPageRequest().getSize());
        }
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> list = ct.list();
        if (wrap) {
            list = list.stream().map(BeanUtils::wrapperFlatMap).collect(Collectors.toList());
        }
        return list;
    }

    /**
     * 根据条件查询DTO（Map）
     *
     * @param cb   查询条件
     * @param wrap 是否需要转换成层次Map
     */
    private Page<Map<String, Object>> pageMap(CriteriaBuilder<T> cb, boolean wrap) {
        if (!entityClazz.equals(cb.getEnCls())) {
            cb.setEnCls(entityClazz);
        }
        // 总数查询
        Criteria ct = cb.buildDeCriteria(true).getExecutableCriteria(DataSourceContext.getSession());
        ct.setCacheable(false);

        ct.setResultTransformer(CriteriaSpecification.ALIAS_TO_ENTITY_MAP);
        if (null != cb.getPageRequest()) {
            ct.setFirstResult(cb.getPageRequest().getOffset());
            ct.setMaxResults(cb.getPageRequest().getSize());
        }

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> list = ct.list();

        ct.setFirstResult(0);
        ct.setMaxResults(MAX_RECORDS);
        List<CriteriaImpl.OrderEntry> orderEntries = BeanUtils.getFieldValue(ct, ORDER_ENTRIES);
        orderEntries.clear();
        Long total = (Long) ct.setProjection(Projections.rowCount()).uniqueResult();
        // 关联对象，填充映射对象
        if (wrap) {
            list = list.stream().map(BeanUtils::wrapperFlatMap).collect(Collectors.toList());
        }
        return new Page<>(list, null == cb.getPageRequest() ? 1L : cb.getPageRequest().getCurrent(), total, null == cb.getPageRequest() ? total.intValue() : cb.getPageRequest().getSize());
    }
}
