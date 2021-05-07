package org.spin.jpa;

import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spin.core.Assert;
import org.spin.core.session.SessionUser;
import org.spin.core.throwable.AssertFailException;
import org.spin.core.throwable.SimplifiedException;
import org.spin.core.util.ArrayUtils;
import org.spin.core.util.CollectionUtils;
import org.spin.core.util.LambdaUtils;
import org.spin.core.util.StreamUtils;
import org.spin.data.core.IEntity;
import org.spin.data.core.IVo;
import org.spin.data.pk.generator.IdGenerator;
import org.spin.data.throwable.SQLError;
import org.spin.data.throwable.SQLException;
import org.spin.jpa.entity.AbstractEntity;
import org.spin.jpa.lin.Linu;
import org.spin.jpa.lin.impl.LindImpl;
import org.spin.jpa.lin.impl.LinqImpl;
import org.spin.jpa.lin.impl.LinuImpl;
import org.spin.jpa.strategy.GetEntityManagerFactoryStrategy;
import org.spin.jpa.vo.VoEntityMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.query.QueryUtils;
import org.springframework.orm.jpa.EntityManagerFactoryUtils;

import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.LockModeType;
import javax.persistence.NonUniqueResultException;
import javax.persistence.Query;
import javax.persistence.Tuple;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.SingularAttribute;
import java.beans.PropertyDescriptor;
import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.util.*;

import static org.springframework.data.jpa.repository.query.QueryUtils.DELETE_ALL_QUERY_STRING;
import static org.springframework.data.jpa.repository.query.QueryUtils.getQueryString;

/**
 * JPA存储上下文
 *
 * @author xuweinan
 */
public abstract class R {
    private static final Logger logger = LoggerFactory.getLogger(R.class);

    protected static GetEntityManagerFactoryStrategy getEntityManagerFactoryStrategy;
    protected static ApplicationContext applicationContext;

    private static IdGenerator<?, ?> idGenerator;

    public static void registerIdGenerator(IdGenerator<?, ?> idGenerator) {
        R.idGenerator = idGenerator;
    }

    /**
     * 创建Linq
     *
     * @param ignore 无
     * @param <R>    查询结果类范型
     * @return Linq
     */
    @SafeVarargs
    public static <R> LinqImpl<R> linq(R... ignore) {
        Class<R> resultClass = ArrayUtils.resolveArrayCompType(ignore);
        Class<?> domainClass = extractDomainClass(resultClass);
        return new LinqImpl<>(domainClass, resultClass);
    }

    /**
     * 创建Linq
     *
     * @param entityManager 实体类管理器
     * @param ignore        无
     * @param <R>           领查询结果类范型
     * @return Linq
     */
    @SafeVarargs
    public static <R> LinqImpl<R> linq(EntityManager entityManager, R... ignore) {
        Class<R> resultClass = ArrayUtils.resolveArrayCompType(ignore);
        Class<?> domainClass = extractDomainClass(resultClass);
        return new LinqImpl<>(domainClass, resultClass, entityManager);
    }

    /**
     * 创建Linq
     *
     * @param domainClass 领域类（实体类）
     * @param ignore      无
     * @param <T>         领域类（实体类）范型
     * @param <R>         返回结果泛型
     * @return Linq
     */
    @SafeVarargs
    public static <T, R> LinqImpl<R> linq(Class<T> domainClass, R... ignore) {
        Class<R> resultClass = ArrayUtils.resolveArrayCompType(ignore);
        return new LinqImpl<>(domainClass, resultClass);
    }

    /**
     * 创建Linq
     *
     * @param domainClass   领域类（实体类）
     * @param entityManager 实体类管理器
     * @param ignore        无
     * @param <T>           领域类（实体类）范型
     * @param <R>           返回结果泛型
     * @return Linq
     */
    @SafeVarargs
    public static <T, R> LinqImpl<R> linq(EntityManager entityManager, Class<T> domainClass, R... ignore) {
        Class<R> resultClass = ArrayUtils.resolveArrayCompType(ignore);
        return new LinqImpl<>(domainClass, resultClass, entityManager);
    }

    /**
     * 创建Lind
     *
     * @param ignore 无
     * @param <T>    领域类（实体类）范型
     * @return Lind
     */
    @SafeVarargs
    public static <T> LindImpl<T> lind(T... ignore) {
        Class<T> domainClass = ArrayUtils.resolveArrayCompType(ignore);
        return new LindImpl<>(domainClass);
    }

    /**
     * 创建Lind
     *
     * @param entityManager 实体类管理器
     * @param ignore        无
     * @param <T>           领域类（实体类）范型
     * @return Lind
     */
    @SafeVarargs
    public static <T> LindImpl<T> lind(EntityManager entityManager, T... ignore) {
        Class<T> domainClass = ArrayUtils.resolveArrayCompType(ignore);
        return new LindImpl<>(domainClass, entityManager);
    }

    /**
     * 创建Linu
     *
     * @param ignore 无
     * @param <T>    领域类（实体类）范型
     * @return Linu
     */
    @SafeVarargs
    public static <T> Linu<T> linu(T... ignore) {
        Class<T> domainClass = ArrayUtils.resolveArrayCompType(ignore);
        return new LinuImpl<>(domainClass);
    }

    /**
     * 创建Linu
     *
     * @param entityManager 实体类管理器
     * @param ignore        无
     * @param <T>           领域类（实体类）范型
     * @return Linu
     */
    @SafeVarargs
    public static <T> Linu<T> linu(EntityManager entityManager, T... ignore) {
        Class<T> domainClass = ArrayUtils.resolveArrayCompType(ignore);
        return new LinuImpl<>(domainClass, entityManager);
    }

    /**
     * 创建Linu
     *
     * @param domainClass 领域类
     * @param <T>         领域类（实体类）范型
     * @return Linu
     */
    public static <T> Linu<T> linu(Class<T> domainClass) {
        return new LinuImpl<>(domainClass);
    }

    /**
     * 创建Linu
     *
     * @param entityManager 实体类管理器
     * @param domainClass   领域类
     * @param <T>           领域类（实体类）范型
     * @return Linu
     */
    public static <T> Linu<T> linu(EntityManager entityManager, Class<T> domainClass) {
        return new LinuImpl<>(domainClass, entityManager);
    }

    /**
     * 创建命名查询
     *
     * @param name 查询的名称
     * @return Query
     */
    public static Query namedQuery(String name) {
        return getEntityManager().createNamedQuery(name);
    }

    /**
     * 创建命名查询
     *
     * @param name          查询名称
     * @param entityManager 实体类管理器
     * @return Query
     */
    public static Query namedQuery(String name, EntityManager entityManager) {
        return entityManager.createNamedQuery(name);
    }

    /**
     * 创建本地查询
     *
     * @param sqlString 本地SQL查询字符串
     * @return Query
     */
    public static Query nativeQuery(String sqlString) {
        return getEntityManager().createNativeQuery(sqlString);
    }

    /**
     * 创建本地查询
     *
     * @param sqlString     本地SQL查询字符串
     * @param entityManager 实体类管理器
     * @return Query
     */
    public static Query nativeQuery(String sqlString, EntityManager entityManager) {
        return entityManager.createNativeQuery(sqlString);
    }

    /**
     * 创建本地查询
     *
     * @param sqlString   本地SQL查询字符串
     * @param resultClass 结果实例的class
     * @return Query
     */
    public static Query nativeQuery(String sqlString, Class<?> resultClass) {
        return getEntityManager().createNativeQuery(sqlString, resultClass);
    }

    /**
     * 创建本地查询
     *
     * @param sqlString     本地SQL查询字符串
     * @param resultClass   结果实例的class
     * @param entityManager 实体类管理器
     * @return Query
     */
    public static Query nativeQuery(String sqlString, Class<?> resultClass, EntityManager entityManager) {
        return entityManager.createNativeQuery(sqlString, resultClass);
    }

    /**
     * 创建本地查询
     *
     * @param sqlString        本地SQL查询字符串
     * @param resultSetMapping 结果集映射名称
     * @return Query
     */
    public static Query nativeQuery(String sqlString, String resultSetMapping) {
        return getEntityManager().createNativeQuery(sqlString, resultSetMapping);
    }

    /**
     * 创建本地查询
     *
     * @param sqlString        本地SQL查询字符串
     * @param resultSetMapping 结果集映射名称
     * @param entityManager    实体类管理器
     * @return Query
     */
    public static Query nativeQuery(String sqlString, String resultSetMapping, EntityManager entityManager) {
        return entityManager.createNativeQuery(sqlString, resultSetMapping);
    }

    /**
     * 根据主键查询数据
     *
     * @param id     主键ID
     * @param ignore 无
     * @param <T>    领域类（实体类）范型
     * @return 实体对象
     */
    @SafeVarargs
    public static <T> Optional<T> findById(Object id, T... ignore) {
        Class<T> domainClass = ArrayUtils.resolveArrayCompType(ignore);
        EntityManager em = getEntityManager(domainClass);
        return Optional.ofNullable(em.find(domainClass, id));
    }

    /**
     * 根据主键查询数据
     *
     * @param id       主键ID
     * @param lockMode 锁定模式
     * @param ignore   无
     * @param <T>      领域类（实体类）范型
     * @return 实体对象
     */
    @SafeVarargs
    public static <T> Optional<T> findById(Object id, LockModeType lockMode, T... ignore) {
        Class<T> domainClass = ArrayUtils.resolveArrayCompType(ignore);
        EntityManager em = getEntityManager(domainClass);
        if (null == lockMode) {
            return Optional.ofNullable(em.find(domainClass, id));
        } else {
            return Optional.ofNullable(em.find(domainClass, id, lockMode));
        }
    }

    public static <PK extends Serializable, T extends IEntity<PK, T>> T save(T entity) {
        return save(entity, false);
    }

    @SuppressWarnings("unchecked")
    public static <PK extends Serializable, T extends IEntity<PK, T>> T save(T entity, boolean saveWithPk) {
        Assert.notNull(entity, "The entity to save MUST NOT be NULL");
        if (entity instanceof AbstractEntity) {
            AbstractEntity<?> aEn = (AbstractEntity<?>) entity;
            SessionUser<Long> user = SessionUser.getCurrent();
            aEn.setUpdateTime(LocalDateTime.now());
            aEn.setUpdateBy(user == null ? -1 : user.getId());
            aEn.setUpdateUsername(user == null ? "" : user.getName());
            if (null == aEn.getId() || saveWithPk) {
                aEn.setCreateTime(LocalDateTime.now());
                aEn.setCreateBy(user == null ? -1 : user.getId());
                aEn.setCreateUsername(user == null ? "" : user.getName());
            }
        }
        try {
            if (null == entity.id() || saveWithPk) {
                if (null != idGenerator && null == entity.id()) {
                    try {
                        entity.id((PK) idGenerator.genId());
                    } catch (ClassCastException e) {
                        logger.warn("ID生成器类生成的数据类型与主键类型不匹配: {}", e.getMessage());
                    }
                }
                ((Session) getEntityManager()).save(entity);
            } else {
                ((Session) getEntityManager()).update(entity);
            }
        } catch (OptimisticLockingFailureException ope) {
            throw new SimplifiedException("The entity is expired", ope);
        }
        return entity;
    }

    @SafeVarargs
    public static <PK extends Serializable, T extends IEntity<PK, T>> int updateById(T entity, Prop<T, ?>... fields) {
        if (null == entity.id()) {
            throw new SQLException(SQLError.ID_NOT_FOUND, "The Id field must be nonnull when execute update by Id");
        }
        SessionUser<Long> user = SessionUser.getCurrent();
        if (entity instanceof AbstractEntity) {
            AbstractEntity<?> aEn = (AbstractEntity<?>) entity;
            aEn.setUpdateBy(null == user ? -1 : user.getId());
            aEn.setUpdateUsername(null == user ? "" : user.getName());
            aEn.setUpdateTime(LocalDateTime.now());
        }

        if (null == fields || 0 == fields.length) {
            persist(entity);
            return 1;
        }
        StringBuilder sb = new StringBuilder();
        sb.append("UPDATE ").append(entity.getClass().getSimpleName()).append(" t SET ");
        Map<String, Object> parameter = new HashMap<>();
        for (Prop<T, ?> field : fields) {
            String fieldName = org.spin.core.util.BeanUtils.toFieldName(LambdaUtils.resolveLambda(field).getImplMethodName());
            sb.append("t.").append(fieldName).append(" = :").append(fieldName).append(",");
            parameter.put(fieldName, org.spin.core.util.BeanUtils.getFieldValue(entity, fieldName));
        }
        if (entity instanceof AbstractEntity) {
            sb.append("t.").append("updateBy").append(" = :").append("updateBy").append(",");
            parameter.put("updateBy", org.spin.core.util.BeanUtils.getFieldValue(entity, "updateBy"));

            sb.append("t.").append("updateUsername").append(" = :").append("updateUsername").append(",");
            parameter.put("updateUsername", org.spin.core.util.BeanUtils.getFieldValue(entity, "updateUsername"));

            sb.append("t.").append("updateTime").append(" = :").append("updateTime").append(",");
            parameter.put("updateTime", org.spin.core.util.BeanUtils.getFieldValue(entity, "updateTime"));
        }
        sb.setLength(sb.length() - 1);
        sb.append(" WHERE id = :id");
        Query query = getEntityManager().createQuery(sb.toString());
        parameter.forEach(query::setParameter);
        return query.executeUpdate();
    }

    /**
     * 持久化实体对象
     *
     * @param entity 实体对象
     * @param <T>    领域类（实体类）范型
     * @return 托管实体类对象
     */
    public static <T> T persist(T entity) {
        EntityManager em = getEntityManager(entity);
        em.persist(entity);
        return entity;
    }

    /**
     * 批量持久化实体对象
     *
     * @param entities 实体对象集合
     * @param <T>      领域类（实体类）范型
     * @return 返回持久化以后的实体对象
     */
    public static <T> List<T> persist(Iterable<? extends T> entities) {
        List<T> result = new ArrayList<>();

        if (entities == null) {
            return result;
        }

        for (T entity : entities) {
            result.add(persist(entity));
        }

        return result;
    }

    /**
     * 更新实体对象
     *
     * @param entity 实体对象
     * @param <T>    领域类（实体类）范型
     * @return 托管实体类对象
     */
    public static <T> T merge(T entity) {
        EntityManager em = getEntityManager(entity);
        return em.merge(entity);
    }

    /**
     * 批量持久化实体对象
     *
     * @param entities 实体对象集合
     * @param <T>      领域类（实体类）范型
     * @return 返回更新后的实体对象
     */
    public static <T> List<T> merge(Iterable<? extends T> entities) {
        List<T> result = new ArrayList<>();

        if (entities == null) {
            return result;
        }

        for (T entity : entities) {
            result.add(merge(entity));
        }

        return result;
    }

    /**
     * 持久化实体对象并刷新
     *
     * @param entity 实体对象
     * @param <T>    领域类（实体类）范型
     * @return 持久后的实体对象
     */
    public static <T> T persistAndFlush(T entity) {
        T result = persist(entity);
        flush(entity);

        return result;
    }

    /**
     * 更新实体对象并刷新
     *
     * @param entity 实体对象
     * @param <T>    领域类（实体类）范型
     * @return 更新后的实体对象
     */
    public static <T> T mergeAndFlush(T entity) {
        T result = merge(entity);
        flush(entity);

        return result;
    }

    /**
     * 刷新指定持久化对象的状态
     *
     * @param entity 待刷新的持久化对象
     * @param <T>    实体泛型
     * @see EntityManager#refresh(Object)
     */
    public static <T> void refresh(final T entity) {
        refresh(entity, null);
    }

    /**
     * 刷新指定持久化对象的状态，并获取该实体上的锁
     *
     * @param entity   待刷新的持久化对象
     * @param lockMode 需要获取的锁
     * @param <T>      实体泛型
     * @see EntityManager#refresh(Object, LockModeType)
     */
    public static <T> void refresh(final T entity, final LockModeType lockMode) {
        if (lockMode != null) {
            getEntityManager().refresh(entity, lockMode);
        } else {
            getEntityManager().refresh(entity);
        }
    }

    /**
     * 将当前实体从EM缓存中剔除。对实体的改动将会被丢弃，不会同步到数据库中。如果实体的关联属性映射为
     * {@code cascade="evict"}，该操作将会级联剔除所有的关联实体
     *
     * @param entity 需要剔除的实体
     * @param <T>    实体泛型
     */
    public static <T> void detach(T entity) {
        if (Objects.nonNull(entity)) {
            getEntityManager().detach(entity);
        }
    }

    /**
     * 从EM上下文缓存中移除所有持久化对象，并取消所有已挂起的保存，更新和删除操作
     *
     * @see EntityManager#clear
     */
    public void clear() {
        getEntityManager().clear();
    }

    /**
     * 删除实体对象
     *
     * @param entity 实体对象
     * @param <T>    领域类（实体类）范型
     */
    public static <T> void remove(T entity) {
        EntityManager em = getEntityManager(entity);
        em.remove(em.contains(entity) ? entity : em.merge(entity));
    }

    /**
     * 批量删除实体对象
     *
     * @param entities 实体对象集合
     * @param <T>      领域类（实体类）范型
     */
    public static <T> void remove(Iterable<? extends T> entities) {

        Assert.notNull(entities, "The given Iterable of entities not be null!");

        for (T entity : entities) {
            remove(entity);
        }
    }

    /**
     * 删除实体类对应的所有记录
     *
     * @param domainClass 领域类（实体类）
     * @param <T>         领域类（实体类）范型
     */
    public static <T> void removeAll(Class<T> domainClass) {
        EntityManager em = getEntityManager(domainClass);
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<T> cq = cb.createQuery(domainClass);
        cq.from(domainClass);
        List<T> result = findAll(cq);
        for (T element : result) {
            remove(element);
        }
    }

    /**
     * 批量删除实体类对应的所有记录
     *
     * @param domainClass 领域累（实体类）
     * @param <T>         领域类（实体类）范型
     */
    public static <T> void removeAllInBatch(Class<T> domainClass) {
        EntityManager em = getEntityManager(domainClass);
        em.createQuery(getQueryString(DELETE_ALL_QUERY_STRING, em.getMetamodel().entity(domainClass).getName())).executeUpdate();
    }

    /**
     * 逻辑删除指定实体
     *
     * @param entity 待删除实体
     * @param <T>    实体泛型
     * @throws AssertFailException 当待删除的实体为{@literal null}时抛出该异常
     */
    public static <T extends IEntity<? extends Serializable, T>> void logicDelete(T entity) {
        Assert.notNull(entity, "The entity to be deleted is null");
        if (entity instanceof AbstractEntity) {
            entity.setValid(false);
            merge(entity);
        }
    }

    /**
     * 通过ID逻辑删除指定实体
     *
     * @param pk     待删除主键
     * @param ignore 无
     * @param <T>    实体泛型
     * @throws AssertFailException 当待删除的{@code id}为{@literal null}时抛出该异常
     */
    @SafeVarargs
    public static <T extends IEntity<? extends Serializable, T>> void logicDelete(Serializable pk, T... ignore) {
        Assert.notNull(pk, "The given id must not be null!");
        Class<T> domainClass = ArrayUtils.resolveArrayCompType(ignore);
        T entity = getEntityManager().find(domainClass, pk);
        Assert.notNull(entity, "Entity not found, or was deleted: [" + domainClass.getSimpleName() + "|" + pk + "]");
        if (entity instanceof AbstractEntity) {
            entity.setValid(false);
            merge(entity);
        }
    }

    /**
     * 通过ID集合逻辑删除指定实体
     *
     * @param pks    待删除主键集合
     * @param ignore 无
     * @param <T>    实体泛型
     * @return 删除行数
     * @throws AssertFailException 当待删除的{@code ids}为{@literal null}时抛出该异常
     */
    @SafeVarargs
    public static <T extends IEntity<? extends Serializable, T>> int logicDelete(Iterable<? extends Serializable> pks, T... ignore) {
        if (null == pks || !pks.iterator().hasNext()) {
            return 0;
        }
        Class<T> domainClass = ArrayUtils.resolveArrayCompType(ignore);
        return linu(domainClass)
            .in("id", StreamUtils.stream(pks).toArray())
            .set(IEntity::getValid, false)
            .update();
    }

    /**
     * 逻辑删除指定实体
     *
     * @param entities 实体集合
     * @param <T>      实体泛型
     * @return 删除行数
     * @throws AssertFailException 当待删除的{@link Iterable}为{@literal null}时抛出该异常
     */
    public static <T extends IEntity<? extends Serializable, T>> int logicDelete(Iterable<T> entities) {
        if (null == entities || !entities.iterator().hasNext()) {
            return 0;
        }
        @SuppressWarnings("unchecked")
        Class<T> domainClass = (Class<T>) entities.iterator().next().getClass();
        return linu(domainClass)
            .in("id", StreamUtils.stream(entities).map(IEntity::id).toArray())
            .set(IEntity::getValid, false)
            .update();
    }

    /**
     * 查询并返回一条记录
     *
     * @param domainClass 领域类（实体类）
     * @param <T>         领域类（实体类）范型
     * @return 实体对象
     */
    public static <T> Optional<T> findOne(Class<T> domainClass) {
        EntityManager em = getEntityManager(domainClass);
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<T> cq = cb.createQuery(domainClass);
        cq.from(domainClass);
        TypedQuery<T> query = em.createQuery(cq);
        query.setMaxResults(2);
        List<T> resultList = query.getResultList();
        if (resultList.size() > 1) {
            throw new NonUniqueResultException("唯一查询的结果数量超过1条");
        }
        return Optional.ofNullable(CollectionUtils.first(resultList));
    }

    public static <T> Optional<T> findFirst(Class<T> domainClass) {
        EntityManager em = getEntityManager(domainClass);
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<T> cq = cb.createQuery(domainClass);
        cq.from(domainClass);
        TypedQuery<T> query = em.createQuery(cq);
        query.setMaxResults(1);
        List<T> resultList = query.getResultList();
        return Optional.ofNullable(CollectionUtils.first(resultList));
    }

    /**
     * 查询实体类的所有数据
     *
     * @param domainClass 领域类（实体类）
     * @param <T>         领域类（实体类）范型
     * @return 结果集合
     */
    public static <T> List<T> findAll(Class<T> domainClass) {
        EntityManager em = getEntityManager(domainClass);
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<T> cq = cb.createQuery(domainClass);
        cq.from(domainClass);
        return em.createQuery(cq).getResultList();
    }

    /**
     * 更具查询条件查询记录
     *
     * @param cq  条件
     * @param <T> 领域类（实体类）范型
     * @return 结果集合
     */
    public static <T> List<T> findAll(CriteriaQuery<T> cq) {
        Class<T> domainClass = cq.getResultType();
        if (CollectionUtils.isEmpty(cq.getRoots())) {
            cq.from(domainClass);
        }
        EntityManager em = getEntityManager(domainClass);
        return em.createQuery(cq).getResultList();
    }

    /**
     * 分页查询
     *
     * @param domainClass 领域类（实体类）
     * @param pageable    分页信息
     * @param <T>         领域类（实体类）范型
     * @return 分页结果信息
     */
    public static <T> Page<T> findAll(Class<T> domainClass, Pageable pageable) {
        EntityManager em = getEntityManager(domainClass);
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<T> cq = cb.createQuery(domainClass);
        cq.from(domainClass);
        return findAll(cq, pageable);
    }

    /**
     * 分页条件查询
     *
     * @param cq       条件
     * @param pageable 分页信息
     * @param <T>      领域类（实体类）范型
     * @return 分页结果
     */
    @SuppressWarnings("unchecked")
    public static <T> Page<T> findAll(CriteriaQuery<T> cq, Pageable pageable) {
        Class<T> domainClass = cq.getResultType();
        Root<T> root;
        if (CollectionUtils.isEmpty(cq.getRoots())) {
            root = cq.from(domainClass);
        } else {
            root = (Root<T>) cq.getRoots().iterator().next();
        }
        EntityManager em = getEntityManager(domainClass);
        if (pageable == null) {
            List<T> list = findAll(cq);
            return new PageImpl<>(list);
        } else {
            Sort sort = pageable.getSort();
            cq.orderBy(QueryUtils.toOrders(sort, root, em.getCriteriaBuilder()));
            TypedQuery<T> query = em.createQuery(cq);

            long offset = pageable.getOffset();
            query.setFirstResult((int) offset);
            query.setMaxResults(pageable.getPageSize());

            Long total = count(cq);
            List<T> content = total > pageable.getOffset() ? query.getResultList() : Collections.emptyList();

            return new PageImpl<>(content, pageable, total);
        }
    }

    /**
     * 根据实体对象，返回EmtityManager
     *
     * @param entity 实体类
     * @param <T>    领域类（实体类）范型
     * @return EntityManager
     */
    public static <T> EntityManager getEntityManager(T entity) {
        Assert.notNull(entity, "entity can not be null.");
        return getEntityManager(entity.getClass());
    }

    /**
     * 返回默认的EntityManager
     *
     * @return EntityManager
     */
    public static EntityManager getEntityManager() {
        return getEntityManager((Class<?>) null);
    }

    /**
     * 创建默认的EntityManager
     *
     * @return EntityManager
     */
    public static EntityManager createEntityManager() {
        return createEntityManager((Class<?>) null);
    }

    /**
     * 根据领域类（实体类），返回EmtityManager
     *
     * @param domainClass 领域类（实体类）
     * @param <T>         领域类（实体类）范型
     * @return EntityManager
     */
    public static <T> EntityManager getEntityManager(Class<T> domainClass) {
        EntityManagerFactory emf = getEntityManagerFactoryStrategy.getEntityManagerFactory(domainClass);
        return EntityManagerFactoryUtils.getTransactionalEntityManager(emf);
    }

    /**
     * 实体管理器工厂在spring中的名称，返回EmtityManager
     *
     * @param entityManagerFactoryName 实体管理器工厂在spring中的名称
     * @return EntityManager
     */
    public static EntityManager getEntityManager(String entityManagerFactoryName) {
        EntityManagerFactory emf = getEntityManagerFactory(entityManagerFactoryName);
        return EntityManagerFactoryUtils.getTransactionalEntityManager(emf);
    }

    /**
     * 根据实体对象，创建EmtityManager
     *
     * @param entity 实体类
     * @param <T>    领域类（实体类）范型
     * @return EntityManager
     */
    public static <T> EntityManager createEntityManager(T entity) {
        Assert.notNull(entity, "entity can not be null.");
        return createEntityManager(entity.getClass());
    }

    /**
     * 根据领域类（实体类），创建EmtityManager
     *
     * @param domainClass 领域类（实体类）
     * @param <T>         领域类（实体类）范型
     * @return EntityManager
     */
    public static <T> EntityManager createEntityManager(Class<T> domainClass) {
        EntityManagerFactory emf = getEntityManagerFactoryStrategy.getEntityManagerFactory(domainClass);
        return emf.createEntityManager();
    }

    /**
     * 实体管理器工厂在spring中的名称，创建EmtityManager
     *
     * @param entityManagerFactoryName 实体管理器工厂在spring中的名称
     * @return EntityManager
     */
    public static EntityManager createEntityManager(String entityManagerFactoryName) {
        EntityManagerFactory emf = getEntityManagerFactory(entityManagerFactoryName);
        return emf.createEntityManager();
    }

    /**
     * 实体管理器工厂在spring中的名称，获取EntityManagerFactory
     *
     * @param entityManagerFactoryName 实体管理器工厂在spring中的名称
     * @return EntityManagerFactory
     */
    public static EntityManagerFactory getEntityManagerFactory(String entityManagerFactoryName) {
        return (EntityManagerFactory) applicationContext.getBean(entityManagerFactoryName);
    }

    /**
     * 根据领域类（实体类），获取EntityManagerFactory
     *
     * @param domainClass 领域类（实体类）
     * @param <T>         领域类（实体类）范型
     * @return EntityManagerFactory
     */
    public static <T> EntityManagerFactory getEntityManagerFactory(Class<T> domainClass) {
        return getEntityManagerFactoryStrategy.getEntityManagerFactory(domainClass);
    }

    /**
     * 获取默认EntityManagerFactory
     *
     * @return EntityManagerFactory
     */
    public static EntityManagerFactory getEntityManagerFactory() {
        return getEntityManagerFactory((Class<?>) null);
    }

    /**
     * 判断类是否为领域类（实体类）
     *
     * @param domainClass 类
     * @param <T>         领域类（实体类）范型
     * @return true是实体类，否则不是
     */
    public static <T> boolean isEntityClass(Class<T> domainClass) {
        try {
            getEntityManagerFactory(domainClass);
            return true;
        } catch (IllegalArgumentException e) {
            //e.printStackTrace();
        }
        return false;
    }

    /**
     * 根据领域类（实体类）获得总记录数
     *
     * @param domainClass 领域类（实体类）
     * @param <T>         领域类（实体类）范型
     * @return 纪录总数
     */
    public static <T> Long count(Class<T> domainClass) {
        EntityManager em = getEntityManager(domainClass);
        CriteriaBuilder cb = em.getCriteriaBuilder();
        return count(cb.createQuery(domainClass));
    }

    /**
     * 根据领域类（实体类）获得总记录数
     *
     * @param cq  CriteriaQuery
     * @param <T> 领域类（实体类）范型
     * @return 纪录总数
     */
    public static <T> Long count(CriteriaQuery<T> cq) {
        return executeCountQuery(getCountQuery(cq));
    }

    /**
     * 根据领域类（实体类）判断记录是否存在
     *
     * @param domainClass 领域类（实体类）
     * @param <T>         领域类（实体类）范型
     * @return true则存在，否则不存在
     */
    public static <T> boolean exists(Class<T> domainClass) {
        EntityManager em = getEntityManager(domainClass);
        CriteriaBuilder cb = em.getCriteriaBuilder();
        return exists(cb.createQuery(domainClass));
    }

    /**
     * 根据条件判断记录是否存在
     *
     * @param cq  CriteriaQuery
     * @param <T> 领域类（实体类）范型
     * @return true则存在，否则不存在
     */
    public static <T> boolean exists(CriteriaQuery<T> cq) {
        return count(cq) > 0;
    }

    /**
     * 刷新实体对象对应的EntityManager
     *
     * @param entity 实体对象
     * @param <T>    领域类（实体类）范型
     */
    public static <T> void flush(T entity) {
        Assert.notNull(entity, "entity can not be null.");
        EntityManager em = getEntityManager(entity.getClass());
        em.flush();
    }

    /**
     * 刷新领域类（实体类）对应的EntityManager
     *
     * @param domainClass 实体对象
     */
    public static void flush(Class<?> domainClass) {
        EntityManager em = getEntityManager(domainClass);
        em.flush();
    }

    public static Long executeCountQuery(TypedQuery<Long> query) {

        Assert.notNull(query, "query can not be null.");

        List<Long> totals = query.getResultList();
        long total = 0L;

        for (Long element : totals) {
            total += element == null ? 0 : element;
        }

        return total;
    }

    @SuppressWarnings("unchecked")
    public static <T> TypedQuery<Long> getCountQuery(CriteriaQuery<T> cq) {
        Class<T> domainClass = cq.getResultType();
        EntityManager em = getEntityManager(domainClass);
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Long> countCq = cb.createQuery(Long.class);
        Root<T> root;
        if (cq.getRestriction() != null) {
            countCq.where(cq.getRestriction());
        }
        if (cq.getGroupRestriction() != null) {
            countCq.having(cq.getGroupRestriction());
        }
        if (cq.getRoots().isEmpty()) {
            root = countCq.from(domainClass);
        } else {
            countCq.getRoots().addAll(cq.getRoots());
            root = (Root<T>) countCq.getRoots().iterator().next();
        }
        countCq.groupBy(cq.getGroupList());
        if (cq.isDistinct()) {
            countCq.select(cb.countDistinct(root));
        } else {
            countCq.select(cb.count(root));
        }

        return em.createQuery(countCq);
    }

    /**
     * 根据属性收集属性对应的数据
     *
     * @param source       源
     * @param propertyName 属性名
     * @param <T>          范型
     * @return source集合每个对象的propertyName属性值的一个集合
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    public static <T> Set<T> collect(Collection<?> source, String propertyName) {
        if (CollectionUtils.isEmpty(source)) {
            return Collections.EMPTY_SET;
        }
        Set result = new HashSet(source.size());

        for (Object obj : source) {
            Object value = getValue(propertyName, obj);

            if (value != null) {
                result.add(value);
            }
        }
        return result;
    }

    /**
     * 根据属性收集属性对应的数据
     *
     * @param source 源
     * @param <T>    领域类（实体类）范型
     * @return source集合每个对象的propertyName属性值的一个集合
     */
    @SuppressWarnings({"unchecked"})
    public static <T> Set<T> collectId(Collection<?> source) {
        if (CollectionUtils.isEmpty(source)) {
            return Collections.EMPTY_SET;
        }
        String idName = getIdName(source.iterator().next().getClass());
        return collect(source, idName);
    }

    /**
     * source转Map，Key为propertyName对应的值，Value为source中propertyName属性值相同的元素
     *
     * @param source       源
     * @param propertyName 属性名
     * @param <K>          propertyName对应的属性的类型
     * @param <V>          source集合元素类型
     * @return 分类Map
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    public static <K, V> Map<K, List<V>> classify(Collection<V> source, String propertyName) {
        if (CollectionUtils.isEmpty(source)) {
            return Collections.EMPTY_MAP;
        }
        Map result = new HashMap();

        for (Object obj : source) {
            Object value = getValue(propertyName, obj);
            Object target = result.get(value);
            if (target != null) {
                ((List) target).add(obj);
            } else {
                List list = new ArrayList();
                list.add(obj);
                result.put(value, list);
            }
        }
        return result;
    }

    @SuppressWarnings("rawtypes")
    private static Object getValue(String propertyName, Object obj) {
        if (obj instanceof Map) {
            return ((Map) obj).get(propertyName);
        } else if (obj instanceof Tuple) {
            return ((Tuple) obj).get(propertyName);
        } else if (obj != null) {
            PropertyDescriptor pd = BeanUtils.getPropertyDescriptor(obj.getClass(), propertyName);
            try {
                if (null == pd) {
                    return null;
                }
                return pd.getReadMethod().invoke(obj);
            } catch (Exception e) {
                logger.warn("Read propname [{}] of [{}] failed", propertyName, obj.getClass().getName());
            }
        }
        return null;
    }

    /**
     * source转Map，Key为source元素的propertyName属性值，Value为该元素
     *
     * @param source       源集合
     * @param propertyName 属性名
     * @param <K>          propertyName对应的属性的类型
     * @param <V>          source集合元素类型
     * @return 索引Map
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    public static <K, V> Map<K, V> index(Collection<V> source, String propertyName) {
        if (CollectionUtils.isEmpty(source)) {
            return Collections.EMPTY_MAP;
        }
        Map result = new HashMap();

        for (Object obj : source) {
            Object value = getValue(propertyName, obj);

            result.put(value, obj);
        }
        return result;
    }

    /**
     * source转Map，Key为source元素主键属性属性值，Value为该元素
     *
     * @param source 源集合
     * @param <K>    propertyName对应的属性的类型
     * @param <V>    source集合元素类型
     * @return 索引Map
     */
    @SuppressWarnings("unchecked")
    public static <K, V> Map<K, V> index(Collection<V> source) {
        if (CollectionUtils.isEmpty(source)) {
            return Collections.EMPTY_MAP;
        }
        String idName = getIdName(source.iterator().next().getClass());
        return index(source, idName);

    }

    /**
     * 获取领域类（实体类）的主键属性名称<br>
     * 注意：<br>
     * 不适用组合主键
     *
     * @param domainClass 领域类（实体类）
     * @param <T>         领域类（实体类）范型
     * @return ID属性名
     */
    public static <T> String getIdName(Class<T> domainClass) {
        EntityManagerFactory emf = getEntityManagerFactory(domainClass);
        EntityType<T> entityType = emf.getMetamodel().entity(domainClass);
        return entityType.getId(entityType.getIdType().getJavaType()).getName();
    }

    /**
     * 获取领域类（实体类）的主键属性
     *
     * @param domainClass 领域类（实体类）
     * @param <T>         领域类（实体类）范型
     * @return SingularAttribute
     */
    public static <T> SingularAttribute<? super T, ?> getId(Class<T> domainClass) {
        EntityManagerFactory emf = getEntityManagerFactory(domainClass);
        EntityType<T> entityType = emf.getMetamodel().entity(domainClass);
        return entityType.getId(entityType.getIdType().getJavaType());
    }

    public static void setGetEntityManagerFactoryStrategy(
        GetEntityManagerFactoryStrategy getEntityManagerFactoryStrategy) {
        R.getEntityManagerFactoryStrategy = getEntityManagerFactoryStrategy;

    }

    public static ApplicationContext getApplicationContext() {
        return applicationContext;
    }

    public static void setApplicationContext(ApplicationContext ctx) {
        applicationContext = ctx;
    }

    private static <T> Class<?> extractDomainClass(Class<T> resultClass) {
        if (null != resultClass.getAnnotation(Entity.class)) {
            return resultClass;
        }

        Type[] genericInterfaces = resultClass.getGenericInterfaces();
        Class<?> domainClass = null;
        for (Type genericInterface : genericInterfaces) {
            if (genericInterface instanceof ParameterizedType) {
                Type rawType = ((ParameterizedType) genericInterface).getRawType();
                if (VoEntityMapper.class == rawType || IVo.class == rawType) {
                    domainClass = (Class<?>) ((ParameterizedType) genericInterface).getActualTypeArguments()[1];
                    break;
                }
            }
        }

        if (null == domainClass) {
            return resultClass;
        }
        return domainClass;
    }
}
