package org.spin.common.log;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spin.common.id.SnowFlake;

/**
 * 微服务日志封装工具类
 * @author YIJIUE
 */
public final class LoggerUtilI {

    private Logger innerLogger = null;

    private LoggerUtilI() {
    }

    /**
     * 通过类名获取log4jLogger
     * @param name 类名
     * @return LoggerUtilI
     */
    public static LoggerUtilI getLogger(String name) {
        LoggerUtilI loggerUtilI = new LoggerUtilI();
        loggerUtilI.innerLogger = LoggerFactory.getLogger(name);
        return loggerUtilI;
    }

    /**
     * 与原有log4j方法相同
     * @param clazz 类
     * @return LoggerUtilI
     */
    public static LoggerUtilI getLogger(Class<?> clazz) {
        return getLogger(clazz.getName());
    }

    /**
     * info级别日志打印 fileds自动填充 -&gt; 请使用 {} 占位符形式
     * @param str 打印语句
     * @param fileds 多字段
     */
    public void info(String str, String... fileds){
        SnowFlake snowFlake = new SnowFlake();
        String generate = generate(str, snowFlake.nextId());
        innerLogger.info(generate, fileds);
    }

    /**
     * 微服务链路调用跟踪方法
     * @param traceId 产生的mdc 请使用snowklake生成
     * @param str 字符串
     * @param fileds 拼接字段
     */
    public void info(long traceId, String str, String... fileds){
        String generate = generate(str, traceId);
        innerLogger.info(generate, fileds);
    }

    /**
     * debug级别日志打印 fileds自动填充 -&gt; 请使用 {} 占位符形式
     * @param str 打印语句
     * @param fileds 多字段
     */
    public void debug(String str, String... fileds){
        SnowFlake snowFlake = new SnowFlake();
        String generate = generate(str, snowFlake.nextId());
        innerLogger.debug(generate, fileds);
    }

    /**
     * error级别日志打印 fileds自动填充 -&gt; 请使用 {} 占位符形式
     * @param str 打印语句
     * @param fileds 多字段
     */
    public void error(String str, String... fileds){
        SnowFlake snowFlake = new SnowFlake();
        String generate = generate(str, snowFlake.nextId());
        innerLogger.error(generate, fileds);
    }

    /**
     * error级别日志打印 可填充异常信息
     * @param throwable 抛出的异常
     * @param str 打印语句
     * @param fileds 多字段
     */
    public void error(Throwable throwable, String str, String... fileds){
        SnowFlake snowFlake = new SnowFlake();
        String generate = generate(str, snowFlake.nextId());
        StringBuilder st = new StringBuilder();
        st.append(generate).append("errorMsg : ").append(throwable.toString()).append(" | ");
        innerLogger.error(generate, fileds);
    }

    /**
     * 生成自定义格式
     * @param str 字符串
     * @param nextId 下一id
     * @return String
     */
    public String generate(String str, long nextId){
        StringBuilder st = new StringBuilder();
        st.append("| ApplicationName : ").append(LogGeneral.property).append(" | MDC : ").append(nextId).append(" | msg : ").append(str).append(" | ");
        return st.toString();
    }



}
