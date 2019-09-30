package org.spin.common.util;

import org.spin.common.throwable.BizException;
import org.spin.core.Assert;
import org.spin.core.throwable.SimplifiedException;
import org.spin.core.util.StringUtils;

import java.util.regex.Pattern;

/**
 * 版本号工具类
 * <p>支持Major.Minor.Build格式的版本解析</p>
 * 合法的版本号定义如下:
 * <pre>
 *     1.版本号(字符串形式)由3个非负整数,中间由“.”隔开的字符串表示, 从左至右分别称为Major, Minor, Build
 *     2.Major, Minor, Build都必须属于[0, 65535], 即必须是一个16bit无符号数
 *     3.当Major, Minor都为0时, Build不能为0, 即0.0.0不是合法的版本号
 *     4.一个字符串形式的版本号, 可以用一个48bit的无符号正整数等价表示(高16bit为Major, 中间16bit为Minor, 低16bit表示Build)
 *     5.版本号间的顺序, 由其数值表示法的数值定义
 *     6.版本号应当从1(由3, 4推导出)开始, 满足全序关系
 * </pre>
 * <p>Created by xuweinan on 2019/9/20</p>
 *
 * @author xuweinan
 * @version 1.0
 */
public abstract class VersionNumberUtils {
    private static final String VERSION_PATTERN_STR = "(0|([1-9]\\d*))\\.(0|([1-9]\\d*))\\.(0|([1-9]\\d*))";
    private static final Pattern VERSION_PATTERN = Pattern.compile(VERSION_PATTERN_STR);
    private static final String VERSION_ILLEGAL_MESSAGE = "版本号格式不合法，版本号为Major.Minor.Build，其中Major,Minor,Build均必须为10进制非0开头的数字(0-65535之间)";

    private VersionNumberUtils() {
    }

    /**
     * 获取主版本
     *
     * @param versionStr 版本号
     * @return 主版本
     */
    public static int getMajor(String versionStr) {
        if (!VERSION_PATTERN.matcher(versionStr).matches()) {
            throw new BizException(VERSION_ILLEGAL_MESSAGE);
        }
        try {
            return Integer.parseInt(versionStr.split("\\.")[0]);
        } catch (Exception ignore) {
            throw new BizException(VERSION_ILLEGAL_MESSAGE);
        }
    }

    /**
     * 获取次版本
     *
     * @param versionStr 版本号
     * @return 次版本
     */
    public static int getMinor(String versionStr) {
        if (!VERSION_PATTERN.matcher(versionStr).matches()) {
            throw new BizException(VERSION_ILLEGAL_MESSAGE);
        }
        try {
            return Integer.parseInt(versionStr.split("\\.")[1]);
        } catch (Exception ignore) {
            throw new BizException(VERSION_ILLEGAL_MESSAGE);
        }
    }

    /**
     * 获取构建版
     *
     * @param versionStr 版本号
     * @return 构建版
     */
    public static int getBuild(String versionStr) {
        if (!VERSION_PATTERN.matcher(versionStr).matches()) {
            throw new BizException(VERSION_ILLEGAL_MESSAGE);
        }
        try {
            return Integer.parseInt(versionStr.split("\\.")[2]);
        } catch (Exception ignore) {
            throw new BizException(VERSION_ILLEGAL_MESSAGE);
        }
    }

    /**
     * 获取主版本
     *
     * @param version 版本号
     * @return 主版本
     */
    public static int getMajor(Long version) {
        if (version < 0 || version > 281474976710655L) {
            throw new BizException(VERSION_ILLEGAL_MESSAGE);
        }
        return (int) (version >>> 32);
    }

    /**
     * 获取次版本
     *
     * @param version 版本号
     * @return 次版本
     */
    public static int getMinor(Long version) {
        if (version < 0 || version > 281474976710655L) {
            throw new BizException(VERSION_ILLEGAL_MESSAGE);
        }
        return (int) ((version >>> 16) & 0xFFFFL);
    }

    /**
     * 获取构建版
     *
     * @param version 版本号
     * @return 构建版
     */
    public static int getBuild(Long version) {
        if (version < 0 || version > 281474976710655L) {
            throw new BizException(VERSION_ILLEGAL_MESSAGE);
        }
        return (int) (version & 0xFFFFL);
    }

    /**
     * 将字符串形式的版本号解码为long
     *
     * @param versionStr 版本号字符串
     * @return 版本号
     */
    public static long decodeVersion(String versionStr) {
        String[] versions = StringUtils.trimToEmpty(versionStr).split("\\.");
        if (versions.length != 3) {
            throw new BizException(VERSION_ILLEGAL_MESSAGE);
        }

        long major;
        long minor;
        long build;

        try {
            major = Assert.inclusiveBetween(0L, 65535L, Long.parseLong(versions[0]), "主版本号必须在0-65535之间");
            minor = Assert.inclusiveBetween(0L, 65535L, Long.parseLong(versions[1]), "次版本号必须在0-65535之间");
            build = Assert.inclusiveBetween(0L, 65535L, Long.parseLong(versions[2]), "构建版本号必须在0-65535之间");
        } catch (SimplifiedException e) {
            throw e;
        } catch (Exception ignore) {
            throw new BizException(VERSION_ILLEGAL_MESSAGE);
        }

        return (major << 32) | (minor << 16) | build;
    }

    /**
     * 将版本号编码为字符串格式
     *
     * @param version 版本号
     * @return 版本号字符串
     */
    public static String encodeVersion(long version) {
        long major = Assert.inclusiveBetween(0L, 281474976710655L, version, "版本号不合法") >>> 32;
        long minor = (version >>> 16) & 0xFFFFL;
        long build = version & 0xFFFFL;
        return major + "." + minor + "." + build;
    }

    /**
     * 判断给定的版本号字符串是否合法
     *
     * @param versionStr 版本号
     * @return 是否合法
     */
    public static boolean isValidVersion(String versionStr) {
        if (!VERSION_PATTERN.matcher(versionStr).matches()) {
            return false;
        }
        String[] split = versionStr.split("\\.");
        try {
            int h = Integer.parseInt(split[0]);
            int m = Integer.parseInt(split[1]);
            int l = Integer.parseInt(split[2]);
            return h < 65535 && m < 65535 && l < 65535 && ((h + m + l) != 0);
        } catch (NumberFormatException | IndexOutOfBoundsException ignore) {
            return false;
        }
    }
}
