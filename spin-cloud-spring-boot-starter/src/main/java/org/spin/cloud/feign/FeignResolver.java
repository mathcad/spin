package org.spin.cloud.feign;

import org.spin.core.util.StringUtils;
import org.spin.core.util.SystemUtils;
import org.springframework.core.env.Environment;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;

/**
 * TITLE
 * <p>DESCRIPTION</p>
 * <p>Created by xuweinan on 2020/2/20</p>
 *
 * @author xuweinan
 * @version 1.0
 */
public class FeignResolver {

    private static Properties resolver;

    static void init(Environment environment) {
        String resolve = environment.getProperty("feign.resolve.path");
        if (null != resolver) {
            return;
        }
        resolver = new Properties();
        Properties properties = new Properties();
        if (StringUtils.isEmpty(resolve)) {
            resolve = SystemUtils.USER_HOME + File.separator + "feign-resolve.properties";
            try (InputStream is = new FileInputStream(new File(resolve))) {
                properties.load(is);
                properties.forEach((k, v) -> resolver.put(k.toString().toUpperCase(), v));
            } catch (Exception e) {
                // do nothing
            }
        } else {
            try (InputStream is = SpinFeignClientsRegistrar.class.getClassLoader().getResourceAsStream(resolve)) {
                if (null != is) {
                    properties.load(is);
                    properties.forEach((k, v) -> resolver.put(k.toString().toUpperCase(), v));
                }
            } catch (Exception e) {
                throw new RuntimeException("Feign-Resolve配置加载失败", e);
            }
        }
    }

    public static String getUrl(String appName) {
        return StringUtils.trimToNull(resolver.getProperty(StringUtils.toUpperCase(appName)));
    }
}
