package org.spin.common;

import org.junit.jupiter.api.Test;
import org.spin.common.internal.NetworkUtils;
import org.spin.common.web.annotation.Auth;
import org.spin.common.web.annotation.GetApi;
import org.spin.core.util.CollectionUtils;
import org.spin.core.util.MethodUtils;
import org.spin.core.util.NetUtils;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.annotation.AnnotationUtils;
import sun.net.util.IPAddressUtil;

import java.lang.reflect.Method;
import java.net.InetAddress;
import java.util.List;

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

    @Test
    void testNet() {
        List<NetUtils.NetAddress> networkInfo = NetUtils.getNetworkInfo();
        networkInfo.forEach(it -> {
            System.out.println(it.getAddress().getHostAddress() + "/" + it.getNetMask());
        });
        String s = NetUtils.longToIpv4(NetUtils.ipv4ToLong("192.168.12.55") >>> 8 << 8);
        System.out.println(s);


        try {
            IPAddressUtil.textToNumericFormatV6("fe80:0:0:0:ff00:0:0:0");
            InetAddress.getByName("192.168.12.55");
        } catch (Exception ignore) {

        }

        System.out.println(NetworkUtils.inSameVlan("192.168.12.54"));
    }
}
