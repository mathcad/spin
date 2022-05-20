package org.spin.mybatis.query;

import com.baomidou.mybatisplus.core.enums.SqlMethod;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.override.MybatisMapperProxy;
import com.baomidou.mybatisplus.core.toolkit.ExceptionUtils;
import com.baomidou.mybatisplus.extension.toolkit.SqlHelper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.exceptions.PersistenceException;
import org.apache.ibatis.reflection.ExceptionUtil;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.MyBatisExceptionTranslator;
import org.mybatis.spring.SqlSessionHolder;
import org.mybatis.spring.SqlSessionTemplate;
import org.mybatis.spring.SqlSessionUtils;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spin.core.Assert;
import org.spin.core.inspection.BytesClassLoader;
import org.spin.core.throwable.SimplifiedException;
import org.spin.core.util.*;
import org.spin.data.core.IEntity;
import org.spin.data.rs.AffectedRows;
import org.spin.data.rs.RowMapper;
import org.spin.data.rs.RowMappers;
import org.spin.data.throwable.SQLError;
import org.spin.mybatis.util.ApplicationContextSupplier;
import org.springframework.dao.DataAccessException;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.io.Serializable;
import java.lang.reflect.*;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * 存储上下文工具类
 * <p>DESCRIPTION</p>
 * <p>Created by xuweinan on 2021/1/6</p>
 *
 * @author xuweinan
 * @version 1.0
 */
public class R extends ClassLoader {
    private static final Logger logger = LoggerFactory.getLogger(R.class);
    private static final ConcurrentHashMap<String, Object> MAPPERS = new ConcurrentHashMap<>();
    private static final Map<String, Object> CUSTOMER_MAPPERS = new HashMap<>();
    private static final String[] MYBATIS_PLUS_BASE_MAPPER_INTFS = {"com/baomidou/mybatisplus/core/mapper/BaseMapper"};

    private static volatile boolean parsed = false;

    private static final BytesClassLoader CLASS_LOADER = new BytesClassLoader(Thread.currentThread().getContextClassLoader());

    private static SqlSessionTemplate sqlSessionTemplate;

    private R() {
        throw new UnsupportedOperationException("工具类不允许实例化");
    }

    /**
     * 保存实体
     *
     * @param entity 实体
     * @param <E>    实体类型
     * @return 影响行数
     */
    @SuppressWarnings("unchecked")
    public static <E> AffectedRows insert(E entity) {
        Assert.notNull(entity, "新增的实体不能为null");
        return AffectedRows.of(getMapper((Class<E>) entity.getClass()).insert(entity));
    }

    /**
     * 批量保存实体
     * <pre>
     * 采用Batch操作模式
     *
     * 如果未开启MySQL JDBC的rewriteBatchedStatements参数:
     *   在2000的插入规模上, 性能大约是单SQL插入的1/4左右.
     *   随着规模提升, 性能差距会更大; 到达20000的规模时, 性能大约相差10倍.
     *
     * 如果开启该参数, 则batch性能与单sql性能没有显著差距
     *
     * Batch操作不受数据库最大sql长度的限制
     * </pre>
     *
     * @param collection 实体
     * @param <E>        实体类型
     * @return 是否成功
     */
    public static <E> boolean batchInsert(Collection<E> collection) {
        return batchInsert(collection, 1000);
    }

    /**
     * 批量保存实体
     * <pre>
     * 采用Batch操作模式
     *
     * 如果未开启MySQL JDBC的rewriteBatchedStatements参数:
     *   在2000的插入规模上, 性能大约是单SQL插入的1/4左右.
     *   随着规模提升, 性能差距会更大; 到达20000的规模时, 性能大约相差10倍.
     *
     * 如果开启该参数, 则batch性能与单sql性能没有显著差距
     *
     * Batch操作不受数据库最大sql长度的限制
     * </pre>
     *
     * @param collection 实体
     * @param batchSize  批次大小
     * @param <E>        实体类型
     * @return 是否成功
     */
    public static <E> boolean batchInsert(Collection<E> collection, int batchSize) {
        if (CollectionUtils.isEmpty(collection)) return false;
        @SuppressWarnings("unchecked")
        Class<E> type = (Class<E>) CollectionUtils.first(collection).getClass();
        InvocationHandler mapper = Proxy.getInvocationHandler(getMapper(type));
        if (!(mapper instanceof MybatisMapperProxy)) {
            throw new SimplifiedException(type.getName() + "'s Mapper class does not support BatchInsert");
        }
        String sqlStatement = ((Class<?>) BeanUtils.getFieldValue(mapper, "mapperInterface")).getName()
            + "." + SqlMethod.INSERT_ONE.getMethod();
        return executeBatch(collection, type, batchSize, (sqlSession, entity) -> sqlSession.insert(sqlStatement, entity));
    }

    public static <E> boolean executeBatch(Collection<E> collection, int batchSize, BiConsumer<SqlSession, E> consumer) {
        if (CollectionUtils.isEmpty(collection)) return false;
        Assert.ge(batchSize, 1, "batchSize must not be less than one");
        @SuppressWarnings("unchecked")
        Class<E> type = (Class<E>) CollectionUtils.first(collection).getClass();
        return executeBatch(collection, type, batchSize, consumer);
    }

    public static <E> boolean executeBatch(Collection<E> collection, Class<E> entityClass, int batchSize, BiConsumer<SqlSession, E> consumer) {
        if (CollectionUtils.isEmpty(collection)) return false;
        Assert.ge(batchSize, 1, "batchSize must not be less than one");
        return executeBatch(entityClass, sqlSession -> {
            int size = collection.size();
            int i = 1;
            for (E element : collection) {
                consumer.accept(sqlSession, element);
                if ((i % batchSize == 0) || i == size) {
                    sqlSession.flushStatements();
                }
                i++;
            }
        });
    }

    /**
     * 执行批量操作
     *
     * @param entityClass 实体
     * @param consumer    consumer
     * @return 操作结果
     */
    public static boolean executeBatch(Class<?> entityClass, Consumer<SqlSession> consumer) {
        SqlSessionFactory sqlSessionFactory = SqlHelper.sqlSessionFactory(entityClass);
        SqlSessionHolder sqlSessionHolder = (SqlSessionHolder) TransactionSynchronizationManager.getResource(sqlSessionFactory);
        boolean transaction = TransactionSynchronizationManager.isSynchronizationActive();
        if (sqlSessionHolder != null) {
            SqlSession sqlSession = sqlSessionHolder.getSqlSession();
            //原生无法支持执行器切换，当存在批量操作时，会嵌套两个session的，优先commit上一个session
            //按道理来说，这里的值应该一直为false。
            sqlSession.commit(!transaction);
        }
        SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH);
        if (!transaction) {
            logger.warn("SqlSession [" + sqlSession + "] Transaction not enabled");
        }
        try {
            consumer.accept(sqlSession);
            //非事物情况下，强制commit。
            sqlSession.commit(!transaction);
            return true;
        } catch (Throwable t) {
            sqlSession.rollback();
            Throwable unwrapped = ExceptionUtil.unwrapThrowable(t);
            if (unwrapped instanceof PersistenceException) {
                MyBatisExceptionTranslator myBatisExceptionTranslator
                    = new MyBatisExceptionTranslator(sqlSessionFactory.getConfiguration().getEnvironment().getDataSource(), true);
                DataAccessException throwable = myBatisExceptionTranslator.translateExceptionIfPossible((PersistenceException) unwrapped);
                if (throwable != null) {
                    throw throwable;
                }
            }
            throw ExceptionUtils.mpe(unwrapped);
        } finally {
            sqlSession.close();
        }
    }

    /**
     * 根据ID删除实体
     *
     * @param id     id
     * @param ignore 无
     * @param <E>    实体类型
     * @return 影响行数
     */
    @SafeVarargs
    public static <E> AffectedRows delete(Serializable id, E... ignore) {
        Assert.notNull(id, "ID不能为null");
        Class<E> type = ArrayUtils.resolveArrayCompType(ignore);
        return AffectedRows.of(getMapper(type).deleteById(id));
    }

    /**
     * 根据ID删除实体
     *
     * @param entity 实体
     * @param <E>    实体类型
     * @return 影响行数
     */
    @SuppressWarnings("unchecked")
    public static <E extends IEntity<?, E>> AffectedRows delete(E entity) {
        Assert.notNull(entity, "删除的实体不能为null");
        Assert.notNull(entity.id(), "删除的实体ID不能为null");
        return AffectedRows.of(getMapper((Class<E>) entity.getClass()).deleteById(entity.id()));
    }

    /**
     * 根据ID更新
     *
     * @param entity 实体
     * @param <E>    实体类型
     * @return 影响行数
     */
    @SuppressWarnings("unchecked")
    public static <E extends IEntity<?, E>> AffectedRows update(E entity) {
        Assert.notNull(entity, "更新的实体不能为null");
        Assert.notNull(entity.id(), "更新的实体ID不能为null");
        return AffectedRows.of(getMapper((Class<E>) entity.getClass()).updateById(entity));
    }

    /**
     * 根据主键查询实体
     *
     * @param id     主键
     * @param ignore 无
     * @param <E>    实体类型
     * @return 实体
     */
    @SafeVarargs
    public static <E extends IEntity<?, E>> Optional<E> getById(Serializable id, E... ignore) {
        Assert.notNull(id, "ID不能为null");
        Class<E> type = ArrayUtils.resolveArrayCompType(ignore);
        return Optional.ofNullable(getMapper(type).selectById(id));
    }

    /**
     * 获取查询上下文
     *
     * @param ignore 用来获取类型, 无实际意义
     * @param <E>    实体类型
     * @return 查询条件上下文
     */
    @SafeVarargs
    public static <E> LambdaQueryExecutor<E> query(E... ignore) {
        Class<E> type = ArrayUtils.resolveArrayCompType(ignore);
        Assert.notTrue(Modifier.isAbstract(type.getModifiers()), "不允许获取抽象类的Mapper对象");
        LambdaQueryExecutor<E> lambda = new LambdaQueryExecutor<>(type);
        BaseMapper<E> baseMapper = getMapper(type);
        lambda.setRepo(baseMapper);
        return lambda;
    }

    /**
     * 获取条件更新上下文
     *
     * @param ignore 用来获取类型, 无实际意义
     * @param <E>    实体类型
     * @return 条件更新上下文
     */
    @SafeVarargs
    public static <E> LambdaUpdateExecutor<E> forUpdate(E... ignore) {
        Class<E> type = ArrayUtils.resolveArrayCompType(ignore);
        Assert.notTrue(Modifier.isAbstract(type.getModifiers()), "不允许获取抽象类的Mapper对象");
        LambdaUpdateExecutor<E> lambda = new LambdaUpdateExecutor<>(type);
        BaseMapper<E> baseMapper = getMapper(type);
        lambda.setRepo(baseMapper);
        return lambda;
    }

    /**
     * 获取操作指定类型实体的Mapper对象，如果不存在用户创建的Mapper，会自动生成一个BaseMapper实例
     *
     * @param ignore 用来推断类型, 无实际意义
     * @param <E>    实体类型
     * @return BaseMapper的实例
     */
    @SafeVarargs
    public static <E> BaseMapper<E> repo(E... ignore) {
        Class<E> type = ArrayUtils.resolveArrayCompType(ignore);
        return getMapper(type);
    }

    /**
     * 获取Mapper(只能获取用户手动创建的Mapper)
     *
     * @param ignore 用来获取类型, 无实际意义
     * @param <M>    Mapper具体类型
     * @return Mapper对象
     */
    @SafeVarargs
    @SuppressWarnings("unchecked")
    public static <M> M get(M... ignore) {
        Assert.notNull(ignore, "类型参数不能为null");
        Class<M> type;
        if (ignore.length > 0) {
            type = (Class<M>) ignore[0].getClass();
        } else {
            type = (Class<M>) ignore.getClass().getComponentType();
        }
        if (!parsed) {
            initMapperBeans();
        }
        return Assert.notNull((M) CUSTOMER_MAPPERS.get(type.getName()), () -> "不存在类型为" + type.getSimpleName() + "的Mapper");
    }

    /**
     * 执行SQL并返回结果
     *
     * @param sql    sql
     * @param ignore 无
     * @param <E>    查询结果泛型
     * @return 查询结果
     */
    @SafeVarargs
    public static <E> Optional<E> executeForSingle(String sql, E... ignore) {
        if (null == sqlSessionTemplate) {
            sqlSessionTemplate = ApplicationContextSupplier.getApplicationContext().getBean(SqlSessionTemplate.class);
        }
        SqlSession sqlSession = SqlSessionUtils.getSqlSession(sqlSessionTemplate.getSqlSessionFactory(),
            sqlSessionTemplate.getExecutorType(), sqlSessionTemplate.getPersistenceExceptionTranslator());
        try (PreparedStatement ps = sqlSession.getConnection().prepareStatement(sql)) {
            boolean isResultSet = ps.execute();

            Class<E> resultType = ArrayUtils.resolveArrayCompType(ignore);
            if (Void.class == resultType) {
                return Optional.empty();
            }

            if (isResultSet) {
                RowMapper<E> mapper = null;
                if (ClassUtils.wrapperToPrimitive(resultType) == null) {
                    mapper = RowMappers.getMapper(resultType);
                }
                try (ResultSet rs = ps.getResultSet()) {
                    if (null != mapper) {
                        List<E> tList = mapper.extractData(rs, 1);
                        if (CollectionUtils.isEmpty(tList)) {
                            return Optional.empty();
                        } else {
                            return Optional.ofNullable(tList.get(0));
                        }
                    } else {
                        boolean next = rs.next();
                        if (next) {
                            long aLong = rs.getLong(1);
                            return Optional.ofNullable(ObjectUtils.convert(resultType, aLong));
                        } else {
                            return Optional.empty();
                        }
                    }
                }
            } else {
                if (ClassUtils.wrapperToPrimitive(resultType) == null) {
                    throw new org.spin.data.throwable.SQLException(SQLError.MAPPING_ERROR, "CUD语句不支持返回结果映射");
                }
                int updateCount = ps.getUpdateCount();
                return Optional.ofNullable(ObjectUtils.convert(resultType, updateCount));
            }

        } catch (SQLException e) {
            throw new org.spin.data.throwable.SQLException(SQLError.SQL_EXCEPTION, e);
        }
    }

    /**
     * 执行SQL并返回结果
     *
     * @param sql    sql
     * @param ignore 无
     * @param <E>    查询结果泛型
     * @return 查询结果
     */
    @SafeVarargs
    public static <E> List<E> executeQuery(String sql, E... ignore) {
        if (null == sqlSessionTemplate) {
            sqlSessionTemplate = ApplicationContextSupplier.getApplicationContext().getBean(SqlSessionTemplate.class);
        }
        SqlSession sqlSession = SqlSessionUtils.getSqlSession(sqlSessionTemplate.getSqlSessionFactory(),
            sqlSessionTemplate.getExecutorType(), sqlSessionTemplate.getPersistenceExceptionTranslator());
        try (PreparedStatement ps = sqlSession.getConnection().prepareStatement(sql)) {

            Class<E> resultType = ArrayUtils.resolveArrayCompType(ignore);
            if (Void.class == resultType) {
                return Collections.emptyList();
            }

            RowMapper<E> mapper = RowMappers.getMapper(resultType);
            try (ResultSet rs = ps.executeQuery()) {
                return mapper.extractData(rs);
            }

        } catch (SQLException e) {
            throw new org.spin.data.throwable.SQLException(SQLError.SQL_EXCEPTION, e);
        }
    }

    @SuppressWarnings("unchecked")
    private static <E> BaseMapper<E> getMapper(Class<E> eClass) {
        if (!parsed) {
            initMapperBeans();
        }
        MAPPERS.computeIfAbsent(eClass.getName(), k -> {
            SqlSessionTemplate sessionTemplate = ApplicationContextSupplier.getApplicationContext().getBean(SqlSessionTemplate.class);
            Class<BaseMapper<E>> baseMapperClass = generateMapperClass(sessionTemplate.getSqlSessionFactory(), eClass);
            return sessionTemplate.getMapper(baseMapperClass);
        });
        return (BaseMapper<E>) MAPPERS.get(eClass.getName());
    }

    @SuppressWarnings("unchecked")
    private static <E, M extends BaseMapper<E>> Class<M> generateMapperClass(SqlSessionFactory sessionFactory, Class<E> eClass) {
        String name = "org/spin/mybatis/mapper/internal/" + eClass.getSimpleName() + "Mapper";
        ClassWriter cw = new ClassWriter(0);
        cw.visit(Opcodes.V1_8, Opcodes.ACC_PUBLIC + Opcodes.ACC_ABSTRACT + Opcodes.ACC_INTERFACE,
            "org/spin/mybatis/mapper/internal/" + eClass.getSimpleName() + "Mapper",
            "Ljava/lang/Object;Lcom/baomidou/mybatisplus/core/mapper/BaseMapper<L" + eClass.getName().replaceAll("\\.", "/") + ";>;",
            "java/lang/Object",
            MYBATIS_PLUS_BASE_MAPPER_INTFS
        );
        cw.visitEnd();
        byte[] bytes = cw.toByteArray();
        Class<M> mClass = (Class<M>) CLASS_LOADER.defineClass(name.replaceAll("/", "."), bytes);
        sessionFactory.getConfiguration().addMapper(mClass);
        return mClass;
    }

    private static synchronized void initMapperBeans() {
        if (!parsed) {
            parsed = true;
            Map<String, Object> beans = ApplicationContextSupplier.getApplicationContext().getBeansWithAnnotation(Mapper.class);
            for (Object v : beans.values()) {
                Class<?> c = v.getClass();
                if (Proxy.isProxyClass(c)) {
                    c = c.getInterfaces()[0];
                }
                CUSTOMER_MAPPERS.put(c.getName(), v);
                for (Type it : c.getGenericInterfaces()) {
                    if (!(it instanceof ParameterizedType)) {
                        continue;
                    }
                    ParameterizedType pt = (ParameterizedType) it;
                    if (pt.getRawType() instanceof Class && ClassUtils.isAssignable((Class<?>) pt.getRawType(), BaseMapper.class)) {
                        Type type = pt.getActualTypeArguments()[0];
                        MAPPERS.put(type.getTypeName(), v);
                        break;
                    }
                }
            }
        }
    }
}
