package org.spin.common;

import org.junit.jupiter.api.Test;
import org.spin.common.web.annotation.Auth;
import org.spin.common.web.annotation.GetApi;
import org.spin.core.util.CollectionUtils;
import org.spin.core.util.MethodUtils;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.annotation.AnnotationUtils;

import java.lang.reflect.Method;

/**
 * TITLE
 * <p>DESCRIPTION</p>
 * <p>Created by xuweinan on 2019/3/15</p>
 *
 * @author xuweinan
 * @version 1.0
 */
class AnnotationTest {

    @Test
    void testAnno() {
        Method anno = MethodUtils.getAccessibleMethod(AnnotationTest.class, "anno", CollectionUtils.ofArray());

        GetApi annotation = AnnotationUtils.getAnnotation(anno, GetApi.class);

        Auth annotation1 = AnnotationUtils.getAnnotation(annotation, Auth.class);

        Auth mergedAnnotation = AnnotatedElementUtils.getMergedAnnotation(anno, Auth.class);

        System.out.println(annotation1.value());
    }

    @GetApi(value = "aa", auth = false, authName = "auth")
    void anno() {
    }
}
