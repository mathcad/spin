package com.spin.mybatis;

import org.junit.jupiter.api.Test;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.spin.mybatis.entity.BasicEntity;
import org.spin.mybatis.query.R;

/**
 * TITLE
 * <p>DESCRIPTION</p>
 * <p>Created by xuweinan on 2021/1/6</p>
 *
 * @author xuweinan
 * @version 1.0
 */
public class StaticTest extends ClassLoader {

    @Test
    void testA() {
        A res = R.<A>query().or(a -> a.like(A::getName, "1")).or(a -> a.like(A::getPhone, "1")).unique().orElse(null);

        res = R.<A>query().dataPerm().or(a -> a.like(A::getName, "1")).or(a -> a.like(A::getPhone, "1")).single().orElse(null);
        System.out.println(A.<A>refId(1L));
    }

    @Test
    void testAsm() {
        String name = "org/spin/mybatis/mapper/internal/" + A.class.getSimpleName() + "Mapper";
        String[] intfs = {"com/baomidou/mybatisplus/core/mapper/BaseMapper"};
        ClassWriter cw = new ClassWriter(0);
        cw.visit(Opcodes.V1_8, Opcodes.ACC_PUBLIC + Opcodes.ACC_ABSTRACT + Opcodes.ACC_INTERFACE,
            "org/spin/mybatis/mapper/internal/" + A.class.getSimpleName() + "Mapper",
            "Ljava/lang/Object;Lcom/baomidou/mybatisplus/core/mapper/BaseMapper<L" + A.class.getName().replaceAll("\\.", "/") + ";>;",
//            null,
            "java/lang/Object",
            intfs
        );
        cw.visitEnd();
        byte[] bytes = cw.toByteArray();
        Class<?> aClass = new StaticTest().defineClass(name.replaceAll("/", "."), bytes, 0, bytes.length);
        System.out.println(aClass.getName());
    }
}

class A extends BasicEntity<A> {
    private String name;

    private String phone;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }
}

class B extends BasicEntity<B> {

}
