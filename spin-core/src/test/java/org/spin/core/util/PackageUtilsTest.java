package org.spin.core.util;

import org.junit.jupiter.api.Test;

import java.util.List;

/**
 * TITLE
 * <p>DESCRIPTION</p>
 * <p>Created by xuweinan on 2019/9/25</p>
 *
 * @author xuweinan
 * @version 1.0
 */
class PackageUtilsTest {

    @Test
    void testJar() {
        List<String> classNameByJar = PackageUtils.getClassNameByJar("file:/D:/ProgramData/m2Repository/org/mathcat/spin-common-spring-boot-starter/2.0.2-SNAPSHOT/spin-common-spring-boot-starter-2.0.2-SNAPSHOT.jar!/org/spin/common/util", true);

        System.out.println(classNameByJar);
    }
}
