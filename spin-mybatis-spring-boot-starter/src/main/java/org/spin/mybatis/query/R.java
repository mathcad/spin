package org.spin.mybatis.query;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionTemplate;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.spin.cloud.util.BeanHolder;
import org.spin.core.Assert;
import org.spin.core.inspection.BytesClassLoader;
import org.spin.core.util.ArrayUtils;
import org.spin.core.util.ClassUtils;
import org.spin.data.core.IEntity;

import java.io.Serializable;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 存储上下文工具类
 * <p>DESCRIPTION</p>
 * <p>Created by xuweinan on 2021/1/6</p>
 *
 * @author xuweinan
 * @version 1.0
 */
public class R extends ClassLoader {

    private static final ConcurrentHashMap<String, BaseMapper<?>> MAPPERS = new ConcurrentHashMap<>();
    private static final Map<String, BaseMapper<?>> CUSTOMER_MAPPERS = new HashMap<>();
    private static final String[] MYBATIS_PLUS_BASE_MAPPER_INTFS = {"com/baomidou/mybatisplus/core/mapper/BaseMapper"};

    private static volatile boolean parsed = false;

    private static final BytesClassLoader CLASS_LOADER = new BytesClassLoader(Thread.currentThread().getContextClassLoader());

    private R() {
    }

    /**
     * 保存实体
     *
     * @param entity 实体
     * @param <E>    实体类型
     * @return 影响行数
     */
    @SuppressWarnings("unchecked")
    public static <E> int insert(E entity) {
        Assert.notNull(entity, "新增的实体不能为null");
        return getMapper((Class<E>) entity.getClass()).insert(entity);
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
    public static <E> int delete(Serializable id, E... ignore) {
        Class<E> type = ArrayUtils.resolveArrayCompType(ignore);
        return getMapper(type).deleteById(id);
    }

    /**
     * 根据ID删除实体
     *
     * @param entity 实体
     * @param <E>    实体类型
     * @return 影响行数
     */
    @SuppressWarnings("unchecked")
    public static <E extends IEntity<?, E>> int delete(E entity) {
        Assert.notNull(entity, "删除的实体不能为null");
        Assert.notNull(entity.id(), "删除的实体ID不能为null");
        return getMapper((Class<E>) entity.getClass()).deleteById(entity.id());
    }

    /**
     * 根据ID更新
     *
     * @param entity 实体
     * @param <E>    实体类型
     * @return 影响行数
     */
    @SuppressWarnings("unchecked")
    public static <E extends IEntity<?, E>> int update(E entity) {
        Assert.notNull(entity, "更新的实体不能为null");
        Assert.notNull(entity.id(), "更新的实体ID不能为null");
        return getMapper((Class<E>) entity.getClass()).updateById(entity);
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

    @SuppressWarnings("unchecked")
    private static <E> BaseMapper<E> getMapper(Class<E> eClass) {
        if (!parsed) {
            initMapperBeans();
        }
        MAPPERS.computeIfAbsent(eClass.getName(), k -> {
            SqlSessionTemplate sessionTemplate = BeanHolder.getApplicationContext().getBean(SqlSessionTemplate.class);
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
            @SuppressWarnings("rawtypes")
            Map<String, BaseMapper> beans = BeanHolder.getApplicationContext().getBeansOfType(BaseMapper.class);
            for (BaseMapper<?> v : beans.values()) {
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
