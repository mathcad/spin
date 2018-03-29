package org.spin.core.util;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;

class MethodUtilsTest {

    @Test
    public void test() {
        Method method = MethodUtils.getAccessibleMethod(MethodUtilsTest.class, "auditingProject", CollectionUtils.ofArray(long.class, boolean.class));
        String[] methodParamNames = MethodUtils.getMethodParamNames(method);
        System.out.println(JsonUtils.toJson(methodParamNames));
        assertTrue(true);
    }

    public void auditingProject(long id, boolean flag) {
    }
}
