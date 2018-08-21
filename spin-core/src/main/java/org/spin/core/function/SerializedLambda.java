package org.spin.core.function;


import org.spin.core.function.serializable.Function;
import org.spin.core.throwable.SimplifiedException;
import org.spin.core.util.SerializeUtils;

import java.io.*;

/**
 * 从java.lang.invoke.SerializedLambda复制过来，用来做一些特殊操作
 * <p>Created by xuweinan on 2018/7/10.</p>
 *
 * @author xuweinan
 */
public class SerializedLambda implements Serializable {

    private static final long serialVersionUID = 8025925345765570181L;
    private Class<?> capturingClass;
    private String functionalInterfaceClass;
    private String functionalInterfaceMethodName;
    private String functionalInterfaceMethodSignature;
    private String implClass;
    private String implMethodName;
    private String implMethodSignature;
    private int implMethodKind;
    private String instantiatedMethodType;
    private Object[] capturedArgs;

    public static <T> SerializedLambda convert(Function<T, ?> lambda) {
        byte[] bytes = SerializeUtils.serialize(lambda);
        try (ObjectInputStream objIn = new ObjectInputStream(new ByteArrayInputStream(bytes)) {
            @Override
            protected Class<?> resolveClass(ObjectStreamClass objectStreamClass) throws IOException, ClassNotFoundException {
                Class<?> clazz = super.resolveClass(objectStreamClass);
                return clazz == java.lang.invoke.SerializedLambda.class ? SerializedLambda.class : clazz;
            }
        }) {
            Object o = objIn.readObject();
            return (SerializedLambda) o;
        } catch (ClassNotFoundException | IOException e) {
            throw new SimplifiedException("SerializedLambda解析失败", e);
        }
    }

    public Class<?> getCapturingClass() {
        return capturingClass;
    }

    public void setCapturingClass(Class<?> capturingClass) {
        this.capturingClass = capturingClass;
    }

    public String getFunctionalInterfaceClass() {
        return functionalInterfaceClass;
    }

    public void setFunctionalInterfaceClass(String functionalInterfaceClass) {
        this.functionalInterfaceClass = functionalInterfaceClass;
    }

    public String getFunctionalInterfaceMethodName() {
        return functionalInterfaceMethodName;
    }

    public void setFunctionalInterfaceMethodName(String functionalInterfaceMethodName) {
        this.functionalInterfaceMethodName = functionalInterfaceMethodName;
    }

    public String getFunctionalInterfaceMethodSignature() {
        return functionalInterfaceMethodSignature;
    }

    public void setFunctionalInterfaceMethodSignature(String functionalInterfaceMethodSignature) {
        this.functionalInterfaceMethodSignature = functionalInterfaceMethodSignature;
    }

    public String getImplClass() {
        return implClass;
    }

    public void setImplClass(String implClass) {
        this.implClass = implClass;
    }

    public String getImplMethodName() {
        return implMethodName;
    }

    public void setImplMethodName(String implMethodName) {
        this.implMethodName = implMethodName;
    }

    public String getImplMethodSignature() {
        return implMethodSignature;
    }

    public void setImplMethodSignature(String implMethodSignature) {
        this.implMethodSignature = implMethodSignature;
    }

    public int getImplMethodKind() {
        return implMethodKind;
    }

    public void setImplMethodKind(int implMethodKind) {
        this.implMethodKind = implMethodKind;
    }

    public String getInstantiatedMethodType() {
        return instantiatedMethodType;
    }

    public void setInstantiatedMethodType(String instantiatedMethodType) {
        this.instantiatedMethodType = instantiatedMethodType;
    }

    public Object[] getCapturedArgs() {
        return capturedArgs;
    }

    public void setCapturedArgs(Object[] capturedArgs) {
        this.capturedArgs = capturedArgs;
    }
}
