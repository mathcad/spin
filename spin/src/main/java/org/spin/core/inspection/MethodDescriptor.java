package org.spin.core.inspection;

import org.spin.core.Assert;
import org.spin.core.util.MethodUtils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Objects;

/**
 * 描述反射方法的关键信息
 * <p>Created by xuweinan on 2017/9/14.</p>
 *
 * @author xuweinan
 */
public class MethodDescriptor {
    private final Class<?> cls;
    private final Method method;
    private final String methodName;
    private final Class<?>[] paramTypes;
    private final String[] paramNames;
    private final int hashCode;

    private Object target;

    /**
     * 构造方法
     *
     * @param method 反射的方法，不能为空
     */
    public MethodDescriptor(Method method) {
        if (method == null) {
            throw new IllegalArgumentException("Method cannot be null");
        }

        this.cls = method.getDeclaringClass();
        this.method = method;
        this.methodName = method.getName();
        this.paramTypes = method.getParameterTypes();
        this.paramNames = MethodUtils.getMethodParamNames(method);

        this.hashCode = methodName.length() + (paramTypes.length + 1) * 1000;


    }

    /**
     * 调用方法
     *
     * @param args 实参列表
     * @return 调用结果
     */
    public Object invoke(Object... args) throws InvocationTargetException, IllegalAccessException {
        return method.invoke(Assert.notNull(target, "方法调用对象不能为空: " + methodName), args);
    }

    /**
     * 获取方法所属类
     *
     * @return 方法所属Class对象
     */
    public Class<?> getCls() {
        return cls;
    }

    /**
     * 获取方法
     *
     * @return 方法对象
     */
    public Method getMethod() {
        return method;
    }

    /**
     * 获取方法名称
     *
     * @return 方法名称
     */
    public String getMethodName() {
        return methodName;
    }

    /**
     * 获取方法形参类型列表
     *
     * @return 形参类型数组
     */
    public Class<?>[] getParamTypes() {
        return paramTypes;
    }

    /**
     * 获取方法形参名称列表
     *
     * @return 形参名称数组
     */
    public String[] getParamNames() {
        return paramNames;
    }

    /**
     * 设置方法调用对象
     *
     * @param target 调用对象
     */
    public void setTarget(Object target) {
        if (Objects.nonNull(target)) {
            if (cls.isAssignableFrom(target.getClass()))
                this.target = target;
            else throw new IllegalArgumentException("target 类型不符" + target.getClass().getName());
        }
    }

    /**
     * 方法描述器是否已经指定调用对象
     *
     * @return 是/否
     */
    public boolean hasTarget() {
        return Objects.nonNull(target);
    }

    /**
     * 判断方法描述器是否相同(不判断调用对象是否相同，只判断方法本身的相等性)
     *
     * @return 是/否是相等
     */
    @Override
    public boolean equals(final Object obj) {
        if (!(obj instanceof MethodDescriptor)) {
            return false;
        }
        final MethodDescriptor md = (MethodDescriptor) obj;

        return (
            methodName.equals(md.methodName) &&
                cls.equals(md.cls) &&
                java.util.Arrays.equals(paramTypes, md.paramTypes)
        );
    }

    /**
     * 返回方法名称的字符串长度。如果名称长度不同，则肯定不是同一方法，如果长度相同，
     * 进一步通过equals方法判断等价性
     *
     * @return 方法名称的字符串长度 + (方法参数个数 + 1) * 1000
     */
    @Override
    public int hashCode() {
        return hashCode;
    }
}
