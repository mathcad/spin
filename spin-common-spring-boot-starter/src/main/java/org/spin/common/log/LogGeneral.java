package org.spin.common.log;

import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;
import org.springframework.util.StringUtils;

/**
 * LOG环境变量上下文填充
 * @author YIJIUE
 */
public class LogGeneral implements EnvironmentAware {

    public static String property = "UNKNOW";

    @Override
    public void setEnvironment(Environment environment) {
        String name = environment.getProperty("spring.application.name");
        if (!StringUtils.isEmpty(name)) {
            property = name;
        }
    }
}
