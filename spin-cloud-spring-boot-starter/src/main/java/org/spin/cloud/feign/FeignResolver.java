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
        resolver = new Properties();
        Properties properties = new Properties();
        if (StringUtils.isEmpty(resolve)) {
            resolve = SystemUtils.USER_HOME + File.separator + "feign-resolve.properties";
            try (InputStream is = new FileInputStream(resolve)) {
                properties.load(is);
                properties.forEach((k, v) -> {
                    if (StringUtils.isNotBlank(k.toString()) && StringUtils.isNotBlank(v.toString())) {
                        resolver.put(StringUtils.trimToEmpty(k.toString().toUpperCase()), StringUtils.trimToEmpty(v.toString()));
                    }
                });
            } catch (Exception e) {
                // do nothing
            }
        } else {
            try (InputStream is = SpinFeignClientsRegistrar.class.getClassLoader().getResourceAsStream(resolve)) {
                if (null != is) {
                    properties.load(is);
                    properties.forEach((k, v) -> {
                        if (StringUtils.isNotBlank(k.toString()) && StringUtils.isNotBlank(v.toString())) {
                            resolver.put(StringUtils.trimToEmpty(k.toString().toUpperCase()), StringUtils.trimToEmpty(v.toString()));
                        }
                    });
                }
            } catch (Exception e) {
                throw new RuntimeException("Feign-Resolve配置加载失败", e);
            }
        }
    }

    public static String getUrl(String appName) {
        return resolver.getProperty(StringUtils.toUpperCase(StringUtils.trimToEmpty(appName)));
    }
}
