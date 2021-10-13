package org.spin.cloud.config;

import com.alibaba.cloud.nacos.NacosConfigBootstrapConfiguration;
import com.alibaba.cloud.nacos.NacosConfigProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spin.core.util.StringUtils;
import org.spin.core.util.SystemUtils;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * TITLE
 * <p>DESCRIPTION</p>
 * <p>Created by xuweinan on 2021/9/30</p>
 *
 * @author xuweinan
 * @version 1.0
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnClass(name = {"com.alibaba.cloud.nacos.NacosConfigBootstrapConfiguration"})
public class NacosBootstrapConfiguration {
    private static final Logger logger = LoggerFactory.getLogger(NacosBootstrapConfiguration.class);

    @Configuration(proxyBeanMethods = false)
    @AutoConfigureBefore(NacosConfigBootstrapConfiguration.class)
    @ConditionalOnProperty(name = "spring.cloud.nacos.config.enabled", matchIfMissing = true)
    public static class NacosPropertiesConfiguration {

        @Bean
        @ConditionalOnMissingBean
        public NacosConfigProperties nacosConfigProperties() {
            String env = System.getProperty("spring.profiles.active");

            if (StringUtils.isEmpty(env)) {
                Properties p = new Properties();
                try (InputStream is = SystemUtils.IS_OS_WINDOWS ?
                    new FileInputStream("C:\\opt\\settings\\server.properties") :
                    new FileInputStream("/opt/settings/server.properties")) {
                    p.load(is);
                } catch (IOException ignore) {
                    // do nothing
                }
                env = p.getProperty("env");
            }

            NacosConfigProperties nacosConfigProperties = new NacosConfigProperties();
            if (StringUtils.isEmpty(env)) {
                return nacosConfigProperties;
            }

            System.setProperty("spring.profiles.active", env);
            Properties meta = new Properties();
            try (InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream("nacos-env.properties")) {
                meta.load(is);
            } catch (IOException ignore) {
                // do nothing
            }
            String server = meta.getProperty(env + ".configMeta");
            if (StringUtils.isEmpty(server)) {
                try (InputStream is = SystemUtils.IS_OS_WINDOWS ?
                    new FileInputStream("C:\\opt\\settings\\nacos-env.properties") :
                    new FileInputStream("/opt/settings/nacos-env.properties")) {
                    meta.load(is);
                } catch (IOException ignore) {
                    // do nothing
                }
                server = meta.getProperty(env + ".configMeta");
            }

            if (StringUtils.isNotEmpty(server)) {
                String[] metaInfo = server.split("@");
                if (metaInfo.length == 2) {
                    server = metaInfo[1];
                    metaInfo = metaInfo[0].split(":");
                    if (metaInfo.length == 2) {
                        nacosConfigProperties.setUsername(metaInfo[0]);
                        nacosConfigProperties.setPassword(metaInfo[1]);
                    } else {
                        logger.error("nacos-env.properties配置不正确: {}", server);
                    }
                }

                String[] tmp = server.split("\\?");
                String params = null;
                if (tmp.length == 2) {
                    server = StringUtils.trimToEmpty(tmp[0]);
                    params = StringUtils.trimToEmpty(tmp[1]);
                } else if (tmp.length > 2) {
                    logger.error("nacos-env.properties参数不正确: {}", server);
                }

                if (StringUtils.isNotEmpty(server)) {
                    nacosConfigProperties.setServerAddr(server);
                } else {
                    logger.error("nacos-env.properties参数不正确: {}", server);
                }

                if (StringUtils.isNotEmpty(params)) {
                    tmp = params.split("&");
                    String v;
                    for (String p : tmp) {
                        p = StringUtils.trimToEmpty(p);
                        int pos = p.indexOf('=');
                        if (pos > 0 && pos < p.length() - 2) {
                            v = StringUtils.urlDecode(StringUtils.trimToEmpty(p.substring(pos + 1)));
                            p = StringUtils.trimToEmpty(p.substring(0, pos)).toLowerCase();
                            switch (p) {
                                case "cn":
                                    nacosConfigProperties.setClusterName(v);
                                    break;
                                case "ns":
                                    nacosConfigProperties.setNamespace(v);
                                    break;
                                case "g":
                                    nacosConfigProperties.setGroup(v);
                                    break;
                                case "ak":
                                    nacosConfigProperties.setAccessKey(v);
                                    break;
                                case "sk":
                                    nacosConfigProperties.setSecretKey(v);
                                    break;
                                case "fe":
                                    nacosConfigProperties.setFileExtension(v);
                                    break;
                            }
                        } else {
                            logger.error("nacos-env.properties配置不正确: {}", p);
                        }
                    }
                }
            }
            return nacosConfigProperties;
        }
    }

}
