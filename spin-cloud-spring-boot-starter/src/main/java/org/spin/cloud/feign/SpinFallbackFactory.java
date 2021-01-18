package org.spin.cloud.feign;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.spin.cloud.throwable.BizException;
import org.spin.core.Assert;
import org.spin.core.inspection.BytesClassLoader;
import org.spin.core.util.BeanUtils;
import org.spin.core.util.ClassUtils;
import org.spin.core.util.ConstructorUtils;
import org.springframework.cloud.openfeign.FallbackFactory;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

/**
 * TITLE
 * <p>DESCRIPTION</p>
 * <p>Created by xuweinan on 2021/1/14</p>
 *
 * @author xuweinan
 * @version 1.0
 */
public abstract class SpinFallbackFactory<T, F extends AbstractFallback> implements FallbackFactory<T> {

    private static final Class<?>[] FALLBACK_CONTRUCTOR_ARG = {Throwable.class};
    private static final ConcurrentHashMap<String, Function<Throwable, ?>> FALLBACK_INFO = new ConcurrentHashMap<>();
    private static final BytesClassLoader CLASS_LOADER = new BytesClassLoader(Thread.currentThread().getContextClassLoader());

    public SpinFallbackFactory() {
    }

    @Override
    @SuppressWarnings("unchecked")
    public T create(Throwable cause) {
        String className = this.getClass().getName();
        if (FALLBACK_INFO.containsKey(className)) {
            return (T) FALLBACK_INFO.get(className).apply(cause);
        }

        Type superclass = this.getClass().getGenericSuperclass();
        if (!(superclass instanceof ParameterizedType)) {
            throw new IllegalStateException(className + "中未声明有效的泛型信息, 无法推断Fallback实现类型");
        }

        ParameterizedType actual = (ParameterizedType) superclass;
        Type fallbackType = actual.getActualTypeArguments()[1];

        if (!(fallbackType instanceof Class)) {
            throw new IllegalStateException(className + "中声明的Fallback不合法, 无法推断Fallback实现类型");
        }

        Class<? extends AbstractFallback> fallbackClass = (Class<? extends AbstractFallback>) fallbackType;

        if (Modifier.isAbstract(fallbackClass.getModifiers())) {
            // 抽象类动态生成实现类
            fallbackClass = generateFallbackClass(fallbackClass, (Class<T>) actual.getActualTypeArguments()[0]);
        }
        String fallbackClassName = fallbackClass.getName();

        // 具体类直接调用
        Constructor<? extends AbstractFallback> accessibleConstructor = ConstructorUtils.getAccessibleConstructor(fallbackClass, FALLBACK_CONTRUCTOR_ARG);
        if (null == accessibleConstructor) {
            throw new IllegalStateException(fallbackClassName + "的实现不合法");
        }
        Function<Throwable, T> fallbackProducer = arg -> {
            try {
                return (T) accessibleConstructor.newInstance(cause);
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
                throw new BizException("无法创建" + fallbackClassName + "的实例", e);
            } catch (ClassCastException e) {
                throw new BizException("无法将" + fallbackClassName + "转换为当前Feign的接口类型");
            }
        };

        FALLBACK_INFO.put(className, fallbackProducer);
        return fallbackProducer.apply(cause);
    }

    /**
     * 生成抽象Fallback类的具体实现
     *
     * @param targetClass 父类
     * @param clientClass feign接口
     * @return fallback实现类
     */
    private Class<F> generateFallbackClass(Class<? extends AbstractFallback> targetClass, Class<T> clientClass) {
        if (!Modifier.isInterface(clientClass.getModifiers())) {
            throw new BizException("Feign客户端" + clientClass.getName() + "必须声名为接口");
        }

        boolean full = targetClass == AbstractFallback.class;
        if (!full) {
            Assert.isTrue(targetClass.getSuperclass() == AbstractFallback.class, "Fallback类的直接父类必须是AbstractFallback");
            Assert.isTrue(targetClass.getInterfaces().length == 1, "Fallback类仅能实现" + clientClass.getName() + "接口");
            Assert.isTrue(targetClass.getInterfaces()[0] == clientClass, "Fallback类必须直接实现" + clientClass.getName() + "接口");
        }

        String name = clientClass.getPackage().getName().replace('.', '/') + "/" + clientClass.getSimpleName() + "Fallback";
        String superName = ClassUtils.getInternalName(targetClass);
        String intfName = ClassUtils.getInternalName(clientClass);
        String[] intfs = {intfName};
        ClassWriter cw = new ClassWriter(0);
        cw.visit(Opcodes.V1_8, Opcodes.ACC_PUBLIC,
            name,
            "L" + superName + ";" + (full ? "L" + intfName + ";" : ""),
            superName,
            full ? intfs : null
        );

        // 生成构造方法
        MethodVisitor mw = cw.visitMethod(Opcodes.ACC_PUBLIC, "<init>", "(Ljava/lang/Throwable;)V", null, null);
        mw.visitVarInsn(Opcodes.ALOAD, 0);
        mw.visitVarInsn(Opcodes.ALOAD, 1);
        mw.visitMethodInsn(Opcodes.INVOKESPECIAL, "org/spin/cloud/feign/AbstractFallback", "<init>", "(Ljava/lang/Throwable;)V", false);
        mw.visitInsn(Opcodes.RETURN);
        mw.visitMaxs(2, 2);
        mw.visitEnd();

        // 实现抽象方法
        List<Method> methods = new LinkedList<>();
        if (full) {
            Collections.addAll(methods, clientClass.getMethods());
            Arrays.stream(clientClass.getMethods()).filter(m -> !Modifier.isStatic(m.getModifiers()) && Modifier.isPublic(m.getModifiers())).forEach(methods::add);
        } else {
            Arrays.stream(targetClass.getMethods()).filter(m -> Modifier.isAbstract(m.getModifiers())).forEach(methods::add);
        }

        for (Method method : methods) {
            int locals = 1;

            Class<?> returnType = method.getReturnType();
            // 生成方法描述
            StringBuilder descriptor = new StringBuilder("(");
            Class<?>[] parameterTypes = method.getParameterTypes();
            for (Class<?> parameterType : parameterTypes) {
                ++locals;
                if (parameterType == long.class || parameterType == double.class) {
                    ++locals;
                }
                descriptor.append(ClassUtils.getTypeDescriptor(parameterType));
            }
            descriptor.append(")");
            descriptor.append(ClassUtils.getTypeDescriptor(returnType));

            // 生成异常表
            Class<?>[] exceptionTypes = method.getExceptionTypes();
            String[] exceptions = 0 == exceptionTypes.length ? null : new String[exceptionTypes.length];
            for (int i = 0, exceptionTypesLength = exceptionTypes.length; i < exceptionTypesLength; i++) {
                exceptions[i] = ClassUtils.getInternalName(exceptionTypes[i]);
            }

            mw = cw.visitMethod(Opcodes.ACC_PUBLIC, method.getName(), descriptor.toString(), BeanUtils.getFieldValue(method, "signature"), exceptions);
            mw.visitVarInsn(Opcodes.ALOAD, 0);
            mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "org/spin/cloud/feign/AbstractFallback", "rethrowException", "()Ljava/lang/Object;", false);

            if (ClassUtils.wrapperToPrimitive(returnType) == void.class) {
                mw.visitInsn(Opcodes.POP);
                mw.visitInsn(Opcodes.RETURN);
            } else {
                mw.visitTypeInsn(Opcodes.CHECKCAST, ClassUtils.getInternalName(ClassUtils.primitiveToWrapper(returnType)));
                if (returnType.isPrimitive()) {
                    switch (returnType.getName()) {
                        case "boolean":
                            mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/Boolean", "booleanValue", "()Z", false);
                            mw.visitInsn(Opcodes.IRETURN);
                        case "char":
                            mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/Character", "charValue", "()C", false);
                            mw.visitInsn(Opcodes.IRETURN);
                        case "byte":
                            mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/Byte", "byteValue", "()B", false);
                            mw.visitInsn(Opcodes.IRETURN);
                        case "short":
                            mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/Short", "shortValue", "()S", false);
                            mw.visitInsn(Opcodes.IRETURN);
                        case "int":
                            mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/Integer", "intValue", "()I", false);
                            mw.visitInsn(Opcodes.IRETURN);
                        case "long":
                            mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/Long", "longValue", "()J", false);
                            mw.visitInsn(Opcodes.LRETURN);
                        case "float":
                            mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/Float", "floatValue", "()F", false);
                            mw.visitInsn(Opcodes.FRETURN);
                        case "double":
                            mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/Double", "doubleValue", "()D", false);
                            mw.visitInsn(Opcodes.DRETURN);
                    }
                } else {
                    mw.visitInsn(Opcodes.ARETURN);
                }
            }
            mw.visitMaxs(returnType == double.class || returnType == long.class ? 2 : 1, locals);
            mw.visitEnd();
        }

        cw.visitEnd();
        byte[] bytes = cw.toByteArray();
        @SuppressWarnings("unchecked")
        Class<F> mClass = (Class<F>) CLASS_LOADER.defineClass(name.replaceAll("/", "."), bytes);
        return mClass;
    }
}
