package org.spin.data.pk.generator.provider;

import org.spin.core.Assert;
import org.spin.core.ErrorCode;
import org.spin.core.throwable.AssertFailException;
import org.spin.core.throwable.SpinException;
import org.spin.core.util.StringUtils;
import org.spin.core.util.SystemUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * 基于外部读取的机器ID提供者
 * <p>DESCRIPTION</p>
 * <p>Created by xuweinan on 2020/1/2</p>
 *
 * @author xuweinan
 * @version 1.0
 */
public class ExternalMachineIdProvider implements MachineIdProvider {

    private long machineId = -1;

    @Override
    public void init(String initParams) {
        if (StringUtils.isNotEmpty(initParams)) {
            String[] strings = initParams.split("=");
            if (strings.length != 2) {
                throw new SpinException(this.getClass().getName() + "[Illegal init params]: " + initParams);
            }

            if (!StringUtils.trimToEmpty(strings[0]).equals("configFile")) {
                throw new SpinException(this.getClass().getName() + "[init params does not contains machineId]: " + initParams);
            }

            readConfigFile(StringUtils.trimToEmpty(strings[1]));
        }

        try {
            readConfigFile("classpath://idgen.properties");
        } catch (AssertFailException e) {
            throw e;
        } catch (Exception ignore) {
            // do nothing
        }

        String configFile;
        if (SystemUtils.IS_OS_WINDOWS) {
            configFile = "C:\\opt\\settings\\server.properties";
        } else {
            configFile = "/opt/settings/server.properties";
        }

        try {
            readConfigFile(configFile);
        } catch (AssertFailException e) {
            throw e;
        } catch (Exception ignore) {
            // do nothing
        }

        readEnv();

        if (machineId == -1) {
            machineId = 0;
        }
    }

    @Override
    public long getMachineId() {
        return machineId;
    }

    private void readConfigFile(String configFile) {
        if (StringUtils.isEmpty(configFile)) {
            throw new SpinException(ErrorCode.IO_FAIL, "the configuration file could not be empty");
        }
        try (InputStream is = configFile.startsWith("classpath://") ?
            Thread.currentThread().getContextClassLoader().getResourceAsStream(configFile.substring(12)) :
            new FileInputStream(new File(configFile))) {
            if (null != is) {
                Properties properties = new Properties();
                properties.load(is);
                if (properties.containsKey("machineId")) {
                    String mid = StringUtils.toStringEmpty(properties.get("machineId"));
                    if (StringUtils.isNotEmpty(mid)) {
                        try {
                            this.machineId = Assert.inclusiveBetween(0, 1023, Long.parseLong(StringUtils.trimToEmpty(mid)),
                                "[machineId must less then 10 bit(0-1023)]: " + mid);
                        } catch (NumberFormatException ignore) {
                            throw new AssertFailException("[machineId must be a number: " + mid);
                        }
                    }
                }
            } else {
                throw new SpinException(ErrorCode.IO_FAIL, "the configuration file does not exist: " + configFile);
            }
        } catch (FileNotFoundException ignore) {
            throw new SpinException(ErrorCode.IO_FAIL, "the configuration file does not exist: " + configFile);
        } catch (IOException e) {
            throw new SpinException(ErrorCode.IO_FAIL, "the configuration read failed: " + configFile);
        }
    }

    private void readEnv() {
        if (-1 == machineId) {
            String mid = System.getenv("machineId");

            if (StringUtils.isEmpty(mid)) {
                mid = System.getenv("MACHINE_ID");
            }

            if (StringUtils.isNotEmpty(mid)) {
                try {
                    machineId = Assert.inclusiveBetween(0, 1023, Long.parseLong(StringUtils.trimToEmpty(mid)),
                        "[machineId must less then 10 bit(0-1023)]: " + mid);
                } catch (NumberFormatException ignore) {
                    throw new AssertFailException("[machineId must be a number: " + mid);
                }
            }
        }
    }
}
