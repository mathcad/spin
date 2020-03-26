package org.spin.cloud;

import org.junit.jupiter.api.Test;
import org.spin.core.util.SystemUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;

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
    void testSwitch() {
        String str = "a";
        switch (str) {
            default:
                System.out.println("default");
            case "b":
                System.out.println("1");
            case "a":
                System.out.println("2");
            case "c":
                System.out.println("3");
        }
    }

    @Test
    void testSwitch2() {
        int str = 2;
        switch (str) {
            default:
                System.out.println("default");
            case 1:
                System.out.println("1");
                break;
            case 2:
                System.out.println("2");
                break;
            case 3:
                System.out.println("3");
                break;
        }
    }

    @Test
    void testProperties() {
        Properties properties = new Properties();
        Properties resolver = new Properties();
        String s = SystemUtils.USER_HOME + File.separator + "feign-resolve.properties";
        try (InputStream is = new FileInputStream(new File(s))) {
            properties.load(is);
            properties.forEach((k, v) -> resolver.put(k.toString().toUpperCase(), v));
        } catch (Exception e) {
            // do nothing
        }
    }
}
