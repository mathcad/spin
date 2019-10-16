package org.spin.core.util.http.useragent;

import org.spin.core.util.CollectionUtils;

import java.util.List;

/**
 * 引擎对象
 */
public class Engine extends UserAgentInfo {

    /**
     * 未知
     */
    public static final Engine Unknown = new Engine(NameUnknown, null);

    /**
     * 支持的引擎类型
     */
    public static final List<Engine> engines = CollectionUtils.ofArrayList(//
        new Engine("Trident", "trident"), //
        new Engine("Webkit", "webkit"), //
        new Engine("Chrome", "chrome"), //
        new Engine("Opera", "opera"), //
        new Engine("Presto", "presto"), //
        new Engine("Gecko", "gecko"), //
        new Engine("KHTML", "khtml"), //
        new Engine("Konqeror", "konqueror"), //
        new Engine("MIDP", "MIDP")//
    );

    /**
     * 构造
     *
     * @param name  引擎名称
     * @param regex 关键字或表达式
     */
    public Engine(String name, String regex) {
        super(name, regex);
    }

}
