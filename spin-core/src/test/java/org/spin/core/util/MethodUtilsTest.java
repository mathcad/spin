package org.spin.core.util;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MethodUtilsTest {

    @Test
    public void test() {
        Method method = MethodUtils.getAccessibleMethod(MethodUtilsTest.class, "auditingProject", CollectionUtils.ofArray(boolean.class, long.class, boolean.class, double.class,int.class));
        String[] methodParamNames = MethodUtils.getMethodParamNames(method);
        System.out.println(StringUtils.join(methodParamNames));
        assertEquals("abcde", StringUtils.join(methodParamNames));
    }

    public void auditingProject(boolean a, long b, boolean c, double d, int e) {
        // 1,2,4,5,7
    }
}
