package org.spin.common.util;

import org.spin.common.throwable.BizException;
import org.spin.core.Assert;
import org.spin.core.throwable.SimplifiedException;
import org.spin.core.util.StringUtils;

import java.util.regex.Pattern;

/**
 * 版本号工具类
 * <p>支持Major.Minor.Build格式的版本解析, Major代表主版本, Minor代表次版本, Build代表构建版</p>
 * <p>主版本: 重大升级(项目结构, 架构, 使用的技术等), 依赖的核心技术的重大升级(如JDK, Spring等), 亦或是严重的破坏性升级应当选择升级主版本号</p>
 * <p>次版本: 一般的功能性升级或优化(允许轻微的破坏性升级), 或者部分模块在不改变外部接口情况下的重构等, 可以选择升级次版本号</p>
 * <p>构建版: 日常bug修复等, 每次新的发布都需要升级构建版</p>
 * <p>该版本号定义，构造了版本号字符串与自然数区间[1, 281474976710655]的双射，使得版本间的比较得以顺利进行</p>
 * <pre>
 *     版本号严格的形式化定义如下:
 *     1.版本号由3个不全为0的16bit无符号整数使用点分十进制字符串表示(形如"x.y.z"), x/y/z分别称为Major, Minor, Build
 *     2.一个字符串形式的版本号, 可以用一个48bit的无符号整数等价表示(高16bit为Major, 中间16bit为Minor, 低16bit表示Build), 称为版本号数值
 *     3.版本号间的顺序, 由版本号数值的的数值顺序定义
 *     4.版本号不允许重复使用, 每个新的构建产物必须分配新的版本号
 *     5.版本号只允许顺序地向后分配
 *
 *     推论:
 *     1.Major, Minor, Build都必须属于[0, 65535], 即必须是一个16bit无符号整数(由定义1推导出)
 *     2.版本号从0.0.1开始, 至65535.65535.65535止, 0.0.0不是合法的版本号(由定义1推导出)
 *     3.版本号数值属于自然数区间[1, 281474976710655](由定义1), 且满足全序关系(由定义3推导出)
 *     4.版本号数值可以使用一个Long类型变量完整表示(由定义2推导出, 其中高16位未使用)
 *     5.如果0.0.1之后下一次发布使用了0.0.4这个版本(由定义4约束), 那么4之前的所有未使用版本(0.0.2, 0.0.3)都将作废, 不能再使用(由定义5推导出)
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
